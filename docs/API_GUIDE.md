# Guide API — Transport Server

Ce document décrit l'API REST exposée par `transport-server-impl-quarkus` pour le
développement du front. Il couvre ce qui est **réellement implémenté** aujourd'hui :
le référentiel flotte (véhicules, documents, entretiens, chauffeurs) et son tableau
de bord, les usagers et leurs codes d'accès, les rotations (affectation chauffeur/
véhicule) et leur rattachement optionnel à une ligne fixe, le contrôle/embarquement
(génération de code + vérification) et les abonnements/paiements usagers.

> Pour explorer l'API de façon interactive : **Swagger UI** sur
> `http://localhost:8080/api/swagger-ui` (spec OpenAPI brute sur `/api/openapi`).
> Le schéma conceptuel des tables est disponible ici :
> https://claude.ai/code/artifact/080aeab6-3ad5-4d6d-bb38-e0f50ff119cc

---

## 1. Ce qui est exposé (et ce qui ne l'est pas encore)

La base contient plusieurs périmètres, chacun rattaché à un module de la
feuille de route (voir les commentaires de `V1__auth_schema.sql`) :

| Périmètre | Tables | Exposé en REST ? |
|---|---|---|
| **Identité** (M1) | `passengers`, `access_codes`, `login_attempts` | ✅ §10bis |
| **Personnel** (M8, table M1) | `staff_profiles` | ✅ §10quater — Keycloak reste seul maître des identifiants |
| **Flotte** (M2) | `vehicles`, `vehicle_documents`, `maintenances`, `drivers` | ✅ §6 à §10 |
| **Rotations** (M3) | `rotations` | ✅ §8bis |
| **Contrôle / Embarquement** (M4) | `attendances` + génération de `access_codes` | ✅ §8ter |
| **Abonnements / Paiements** (M5) | `subscriptions`, `payments` | ✅ §10ter |
| **Lignes** (M6) | `routes`, `route_schedules` | ✅ §8quater |
| **Tableau de bord global** (M7) | — (agrégation, pas de nouvelle table) | ✅ §8quinquies |
| **Moyens de paiement** (M9) | `payment_accounts` | ✅ §10quinquies |
| **Location de véhicules** (M10) | `rentals`, `rental_payments` | ✅ §8sexies |

`staff_profiles` et Keycloak restent la source de vérité pour les comptes/rôles :
l'API ne gère ni login, ni mot de passe, ni session pour le personnel — uniquement
les données métier (flotte, usagers, rotations, contrôle).

---

## 2. Base URL, auth, CORS

- **Base URL locale** : `http://localhost:8080/api` (`quarkus.http.root-path=/api`)
- **Authentification** : OIDC/Keycloak (Bearer JWT), rôles lus dans le claim
  `realm_access.roles` du token.
  - En profil **dev** et **test**, l'OIDC est désactivé (`%dev.quarkus.oidc.enabled=false`)
    → **aucun token requis en local**, tous les endpoints répondent directement.
  - En prod, il faut envoyer `Authorization: Bearer <token>`.
- **CORS** : origines autorisées par défaut `http://localhost:3000` et
  `http://localhost:5173` (CRA / Vite). Réglable via la variable d'env `CORS_ORIGINS`
  si le front tourne ailleurs.

---

## 3. Rôles & permissions

Quatre rôles existent : `OWNER`, `MANAGER`, `CONTROLLER`, `DRIVER`.

⚠️ Le tableau ci-dessous ne couvre que le périmètre **Flotte** (M2) — chaque
module ajouté depuis a son propre tableau de rôles dans sa section : Identité
(§10bis), Rotations (§8bis), Contrôle/Embarquement (§8ter), Lignes
(§8quater), Tableau de bord global (§8quinquies), Abonnements/Paiements
(§10ter), Personnel (§10quater).

| Endpoint | Méthode | Rôles autorisés |
|---|---|---|
| `/vehicles` | GET | OWNER, MANAGER, CONTROLLER, DRIVER |
| `/vehicles/available` | GET | OWNER, MANAGER |
| `/vehicles/{id}` | GET | OWNER, MANAGER, CONTROLLER, DRIVER |
| `/vehicles` | POST | OWNER, MANAGER |
| `/vehicles/{id}` | PUT | OWNER, MANAGER |
| `/vehicles/{id}/status` | PATCH | OWNER, MANAGER |
| `/vehicles/{id}/mileage` | PATCH | OWNER, MANAGER, DRIVER |
| `/vehicles/{id}` | DELETE | OWNER |
| `/vehicles/{id}/documents` | GET, POST | OWNER, MANAGER |
| `/vehicles/{id}/maintenances` | GET, POST | OWNER, MANAGER |
| `/vehicle-documents/expiring` | GET | OWNER, MANAGER |
| `/vehicle-documents/{id}` | PUT, DELETE | OWNER, MANAGER |
| `/maintenances/upcoming` | GET | OWNER, MANAGER |
| `/maintenances/{id}` | PUT, DELETE | OWNER, MANAGER |
| `/drivers` | GET, POST | OWNER, MANAGER |
| `/drivers/available` | GET | OWNER, MANAGER |
| `/drivers/expiring-licenses` | GET | OWNER, MANAGER |
| `/drivers/payroll` | GET | **OWNER uniquement** |
| `/drivers/{id}` | GET, PUT | OWNER, MANAGER |
| `/drivers/{id}/status` | PATCH | OWNER, MANAGER |
| `/drivers/{id}` | DELETE | OWNER |
| `/fleet/alerts` | GET | OWNER, MANAGER |

Un appel avec un rôle non autorisé renvoie `403 Forbidden`.

⚠️ **Point d'attention** : `/drivers/payroll` est réservé à `OWNER` (donnée
financière), mais `/fleet/alerts` **et** `/dashboard` (M7, §8quinquies —
qui embarque `fleetAlerts`), tous deux accessibles à `MANAGER`, renvoient
aussi `monthlyPayroll`. Un `MANAGER` peut donc voir la masse salariale via
ces deux tableaux de bord. À garder en tête côté front (et à corriger côté
API si ce n'est pas voulu).

---

## 4. Format des erreurs

Toutes les erreurs métier suivent la même forme :

```json
{
  "code": "BAD_REQUEST",
  "message": "Un car avec l'immatriculation 4521 CI 01 existe deja",
  "timestamp": "2026-08-23T10:04:12.123Z"
}
```

| HTTP | `code` | Origine |
|---|---|---|
| 404 | `NOT_FOUND` | ressource inexistante |
| 400 | `BAD_REQUEST` | règle métier violée (doublon, statut invalide, cohérence de dates…) |
| 400 | `VALIDATION_ERROR` | échec de validation Bean Validation (`@NotBlank`, `@Min`…) — `message` concatène toutes les violations séparées par ` ; ` |
| 403 | `FORBIDDEN` | rôle insuffisant |

---

## 5. Énumérations — valeurs exactes

Les champs `status` / `*Type` sont sérialisés avec le **nom de la constante Java**
(`UPPER_SNAKE_CASE`), pas le libellé "humain" à espaces (celui-ci n'existe que côté
Java et n'est jamais envoyé au front). Ce sont aussi les valeurs à envoyer dans les
requêtes.

| Enum | Valeurs valides |
|---|---|
| `VehicleStatus` (véhicules) | `DISPONIBLE`, `EN_SERVICE`, `ENTRETIEN`, `HORS_SERVICE` |
| `DriverStatus` (chauffeurs) | `DISPONIBLE`, `EN_ROTATION`, `REPOS`, `SUSPENDU` |
| `RemunerationType` (chauffeurs) | `FIXE`, `PAR_TRAJET`, `COMMISSION` |
| `DocumentType` (documents véhicule) | `ASSURANCE`, `VISITE_TECHNIQUE`, `CARTE_GRISE`, `LICENCE_TRANSPORT`, `AUTRE` |
| `MaintenanceType` (entretiens) | `VIDANGE`, `REPARATION`, `CONTROLE`, `PNEUMATIQUES`, `AUTRE` |
| `MaintenanceStatus` (entretiens) | `PLANIFIE`, `EN_COURS`, `TERMINE`, `ANNULE` |
| `RotationStatus` (rotations, M3) | `PLANIFIEE`, `EN_COURS`, `TERMINEE`, `ANNULEE` |
| `PassengerStatus` (usagers, M1) | `ACTIVE`, `SUSPENDED`, `DISABLED` |
| `PaymentMethod` (paiements, M5) | `ESPECES`, `MOBILE_MONEY`, `VIREMENT`, `AUTRE` |
| `RentalStatus` (locations, M10) | `RESERVEE`, `EN_COURS`, `TERMINEE`, `ANNULEE` |
| `SubscriptionPlan` (abonnements, M5) | `HEBDOMADAIRE`, `MENSUEL`, `TRIMESTRIEL` |
| `SubscriptionStatus` (abonnements, M5) | `ACTIVE`, `EXPIRED`, `CANCELLED` |
| `RouteStatus` (lignes, M6) | `ACTIVE`, `SUSPENDUE` |
| `ScheduleDay` (horaires de ligne, M6) | `LUNDI`, `MARDI`, `MERCREDI`, `JEUDI`, `VENDREDI`, `SAMEDI`, `DIMANCHE` |
| `StaffType` (personnel, M8) | `OWNER`, `MANAGER`, `CONTROLLER`, `DRIVER` |

Une valeur inconnue envoyée par le front déclenche un `400 BAD_REQUEST` explicite
(ex. `"Statut de car invalide : XXX"`).

---

## 6. Ressource : Véhicules (`/vehicles`)

### Champs (`VehicleDto` en lecture, `VehicleRequest` en écriture)

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `plateNumber` | string | oui | trim + uppercase côté serveur, **unique** |
| `brand` | string | oui | |
| `model` | string | oui | |
| `year` | number | non | 1950 – 2100 |
| `capacity` | number | oui | 1 – 120 (nombre de places) |
| `mileageKm` | number | non | ≥ 0, défaut 0 |
| `status` | enum `VehicleStatus` | non | défaut `DISPONIBLE` |
| `commissionedAt` | date (`YYYY-MM-DD`) | non | |
| `notes` | string | non | |

`GET /vehicles/{id}` renvoie un objet enrichi (`VehicleDetailDto`) :
```json
{
  "vehicle": { "...": "VehicleDto" },
  "documents": [ "...DocumentDto" ],
  "maintenances": [ "...MaintenanceDto" ]
}
```

### Endpoints

| Méthode | Path | Description |
|---|---|---|
| GET | `/vehicles?status=` | liste, filtrable par statut |
| GET | `/vehicles/available` | véhicules `DISPONIBLE` uniquement |
| GET | `/vehicles/{id}` | détail + documents + entretiens |
| POST | `/vehicles` | création |
| PUT | `/vehicles/{id}` | remplacement complet |
| PATCH | `/vehicles/{id}/status` | `{ "status": "EN_SERVICE" }` |
| PATCH | `/vehicles/{id}/mileage` | `{ "mileageKm": 12450 }` |
| DELETE | `/vehicles/{id}` | suppression |

### Règles métier à connaître côté UI

- **Immatriculation unique** : la création/modification échoue en `400` si la
  plaque existe déjà sur un autre véhicule.
- **Le kilométrage ne peut jamais diminuer** — `PATCH /mileage` rejette toute
  valeur inférieure au kilométrage actuel (erreur de saisie présumée). Prévoir un
  message d'erreur clair côté UI plutôt qu'un simple champ numérique libre.
- **Suppression bloquée** si le véhicule est `EN_SERVICE`.

---

## 7. Ressource : Documents véhicule

Deux points d'entrée pour le même objet (`DocumentDto` / `DocumentRequest`) :
- imbriqué sous un véhicule : `GET`/`POST /vehicles/{id}/documents`
- global : `GET /vehicle-documents/expiring?days=30`, `PUT`/`DELETE /vehicle-documents/{id}`

| Champ | Type | Obligatoire | Règles |
|---|---|---|---|
| `docType` | enum `DocumentType` | oui | |
| `reference` | string | non | |
| `issuer` | string | non | |
| `issuedOn` | date | non | |
| `expiresOn` | date | oui | doit être ≥ `issuedOn` si celle-ci est renseignée |
| `fileUrl` | string | non | lien vers le fichier scanné (pas d'upload géré par cette API) |
| `daysUntilExpiry`, `expired` | — | lecture seule, calculés par le serveur |

`GET /vehicle-documents/expiring?days=N` sert à alimenter les alertes (documents
expirés ou arrivant à échéance sous N jours).

---

## 8. Ressource : Entretiens (`maintenances`)

Deux points d'entrée : imbriqué (`/vehicles/{id}/maintenances` en GET/POST) et
global (`/maintenances/upcoming`, `PUT`/`DELETE /maintenances/{id}`).

| Champ | Type | Obligatoire | Règles |
|---|---|---|---|
| `maintenanceType` | enum `MaintenanceType` | oui | |
| `status` | enum `MaintenanceStatus` | non | défaut `PLANIFIE` |
| `scheduledOn`, `performedOn` | date | non | |
| `mileageKm`, `nextDueKm` | number | non | ≥ 0 |
| `nextDueOn` | date | non | |
| `costAmount` | number | non | ≥ 0 (FCFA) |
| `garage`, `description` | string | non | |

### ⚠️ Effets de bord automatiques sur le véhicule

Ce sont les règles les plus importantes à connaître pour l'UI, car elles modifient
un autre objet (`Vehicle`) sans action explicite de l'utilisateur dessus :

- **Passer un entretien à `EN_COURS`** (à la création) → le véhicule bascule
  automatiquement en statut `ENTRETIEN`.
- **Passer un entretien à `TERMINE`** (en modification) → si le véhicule était
  `ENTRETIEN`, il repasse à `DISPONIBLE`, **et** son `mileageKm` est mis à jour si
  le kilométrage saisi sur l'entretien est supérieur au kilométrage actuel du
  véhicule.

➡️ Après une création/modification d'entretien, **recharger la fiche véhicule**
(`GET /vehicles/{id}`) si elle est affichée, son statut/kilométrage peut avoir
changé silencieusement.

---

## 8bis. Ressource : Rotations (`/rotations`)

Une rotation affecte un **chauffeur** à un **véhicule** pour une vacation
(plage horaire). C'est le module M3, prérequis pour qu'un futur module (M4)
puisse générer des codes d'accès usagers rattachés à une vacation précise.

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `driverId` / `driverIdentifier` | UUID | oui (écriture) | doit référencer un chauffeur existant |
| `vehicleId` / `vehicleIdentifier` | UUID | oui (écriture) | doit référencer un véhicule existant |
| `routeId` / `routeIdentifier` | UUID | non | ligne fixe optionnelle (M6, §8quater) — absent/`null` = rotation libre sans ligne |
| `status` | enum `RotationStatus` | non | défaut `PLANIFIEE` |
| `scheduledStart` | date-heure ISO 8601 | oui | |
| `scheduledEnd` | date-heure ISO 8601 | non | doit être postérieure à `scheduledStart` |
| `startedAt`, `endedAt` | date-heure ISO 8601 | non | renseignées automatiquement au passage en `EN_COURS`/`TERMINEE` si absentes |
| `startMileageKm`, `endMileageKm` | number | non | ≥ 0 |
| `accessCodeReference` | string | non | champ libre réservé à M4, non utilisé par ce module |
| `notes` | string | non | |

`RotationStatus` : `PLANIFIEE`, `EN_COURS`, `TERMINEE`, `ANNULEE`.

### Endpoints

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/rotations?status=&driverId=&vehicleId=` | OWNER, MANAGER | liste filtrable |
| GET | `/rotations/active` | OWNER, MANAGER | rotations en cours |
| GET | `/rotations/{id}` | OWNER, MANAGER | détail |
| POST | `/rotations` | OWNER, MANAGER | création |
| PUT | `/rotations/{id}` | OWNER, MANAGER | remplacement complet |
| PATCH | `/rotations/{id}/status` | OWNER, MANAGER | `{ "status": "EN_COURS" }` |
| DELETE | `/rotations/{id}` | OWNER | suppression |
| GET | `/drivers/{id}/rotations` | OWNER, MANAGER | historique par chauffeur |
| GET | `/vehicles/{id}/rotations` | OWNER, MANAGER | historique par véhicule |

### ⚠️ Effets de bord automatiques sur le chauffeur et le véhicule

Comme pour les entretiens (§8), ces règles modifient d'autres objets sans
action explicite de l'utilisateur dessus :

- **Passer une rotation à `EN_COURS`** → rejetée en `400` si le chauffeur
  n'est pas `DISPONIBLE` ou le véhicule n'est pas `DISPONIBLE`. Sinon, le
  chauffeur bascule en `EN_ROTATION` et le véhicule en `EN_SERVICE`.
- **Passer une rotation à `TERMINEE`** → le chauffeur et le véhicule
  repassent à `DISPONIBLE` (s'ils étaient dans l'état actif ci-dessus), et
  le `mileageKm` du véhicule est mis à jour si `endMileageKm` est supérieur
  au kilométrage actuel.
- **Passer une rotation à `ANNULEE`** → même remise à disponible, sans
  reprise de kilométrage.
- **Suppression bloquée** si la rotation est `EN_COURS`.

➡️ Après une transition de statut de rotation, **recharger la fiche
chauffeur et/ou véhicule** si elles sont affichées.

---

## 8ter. Contrôle / Embarquement

Module M4 : le rôle `CONTROLLER` génère un code d'accès pour une rotation,
puis vérifie le code saisi par un usager à l'embarquement. Une vérification
réussie enregistre une **présence validée** (`attendance`).

### Génération d'un code — `POST /access-codes/generate`

Rôles : `OWNER`, `MANAGER`, `CONTROLLER`.

| Champ (requête) | Type | Obligatoire | Règles |
|---|---|---|---|
| `rotationId` | UUID | oui | rotation existante |
| `validityHours` | number | non | défaut 12h |

Réponse (`201`) — **seul endpoint à renvoyer le code en clair**, une seule fois :

```json
{
  "identifier": "...",
  "rotationIdentifier": "...",
  "vehicleIdentifier": "...",
  "vehicleNumber": "4521 CI 01",
  "code": "7391",
  "validFrom": "2026-09-16T20:00:00Z",
  "validUntil": "2026-09-17T08:00:00Z"
}
```

➡️ **Effet de bord** : `Rotation.accessCodeReference` est mis à jour avec
l'identifiant du code généré.

Le reste de la ressource `/access-codes` (CRUD standard) reste réservé à
`OWNER`/`MANAGER` comme avant ; `rotationIdentifier` (nullable) apparaît
désormais aussi dans `AccessCodeDto` classique.

### Vérification à l'embarquement — `POST /boarding/verify`

Rôles : `OWNER`, `MANAGER`, `CONTROLLER`.

| Champ (requête) | Type | Obligatoire | Règles |
|---|---|---|---|
| `rotationId` | UUID | oui | `404` si la rotation n'existe pas |
| `code` | string | oui | code saisi par l'usager |
| `phone` | string | oui | téléphone de l'usager |
| `controllerId` | UUID | non | champ libre, non validé contre une table (pas d'entité staff côté API) |

⚠️ Cet endpoint renvoie **toujours `200`** — un code faux ou un usager non
identifié n'est pas une erreur HTTP, c'est un résultat de vérification :

```json
{
  "success": false,
  "message": "Code invalide ou expire",
  "attendance": null,
  "loginAttemptId": 42
}
```

En cas de succès, `attendance` contient la présence créée. Dans tous les cas,
un `login_attempt` est enregistré (traçabilité générique, y compris les
échecs) ; **`attendance` n'est créée que si la vérification réussit.**

⚠️ **Depuis M5** : la vérification exige aussi que l'usager ait un
**abonnement actif** couvrant la date du jour (voir §10ter). Sans abonnement
actif, `success:false` avec le message `"Abonnement expire ou inexistant"`,
même si le code est correct.

### Historique des présences

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/attendances?rotationId=` | OWNER, MANAGER | liste filtrable |
| GET | `/attendances/{id}` | OWNER, MANAGER | détail |
| DELETE | `/attendances/{id}` | OWNER | correction d'audit |
| GET | `/rotations/{id}/attendances` | OWNER, MANAGER | historique par rotation |

---

## 8quater. Ressource : Lignes (`/routes`)

Module M6 : une ligne est un trajet fixe (point A → point B) avec des
horaires récurrents, auquel une rotation peut être rattachée
(`Rotation.routeId`, optionnel — voir §8bis).

### Champs (`RouteDto`)

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `code` | string | oui | **unique**, normalisé en majuscules |
| `name` | string | oui | |
| `origin` / `destination` | string | oui | |
| `distanceKm` | number | non | ≥ 0 |
| `status` | enum `RouteStatus` | non | défaut `ACTIVE` — `ACTIVE`, `SUSPENDUE` |

`GET /routes/{id}` renvoie un objet enrichi (`RouteDetailDto`) :
```json
{
  "route": { "...": "RouteDto" },
  "schedules": [ "...RouteScheduleDto" ]
}
```

### Endpoints

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/routes?status=` | OWNER, MANAGER, CONTROLLER, DRIVER | liste filtrable |
| GET | `/routes/{id}` | OWNER, MANAGER, CONTROLLER, DRIVER | détail + horaires |
| POST | `/routes` | OWNER, MANAGER | création |
| PUT | `/routes/{id}` | OWNER, MANAGER | remplacement complet |
| PATCH | `/routes/{id}/status` | OWNER, MANAGER | `{ "status": "SUSPENDUE" }` |
| DELETE | `/routes/{id}` | OWNER | bloqué si des rotations référencent la ligne |
| GET | `/routes/{id}/rotations` | OWNER, MANAGER | historique des rotations sur cette ligne |

### Horaires récurrents (`RouteScheduleDto`)

| Champ | Type | Obligatoire | Règles |
|---|---|---|---|
| `dayOfWeek` | enum `ScheduleDay` | oui | `LUNDI`…`DIMANCHE` |
| `departureTime` | heure (`HH:mm`) | oui | pas de date précise — planning type récurrent |

| Méthode | Path | Rôles |
|---|---|---|
| GET | `/routes/{id}/schedules` | OWNER, MANAGER |
| POST | `/routes/{id}/schedules` | OWNER, MANAGER |
| PUT | `/route-schedules/{id}` | OWNER, MANAGER |
| DELETE | `/route-schedules/{id}` | OWNER, MANAGER |

---

## 8quinquies. Tableau de bord global (`/dashboard`)

Module M7 : agrège en un seul appel ce que `/fleet/alerts` (§10) couvrait
déjà (flotte) **et** ce que les modules ajoutés depuis ont apporté
(rotations, abonnements, revenus). Aucune nouvelle table — lecture pure sur
les données existantes.

`GET /dashboard?days=30` (rôles `OWNER`, `MANAGER`) :

```json
{
  "fleetAlerts": { "...": "FleetAlertsDto, identique a /fleet/alerts" },
  "expiringSubscriptions": [ "...SubscriptionDto" ],
  "todayRotations": [ "...RotationDto" ],
  "activeRotationsCount": 1,
  "activeSubscriptionsCount": 3,
  "monthlyRevenue": 45000.00
}
```

- `fleetAlerts` : identique à la réponse de `GET /fleet/alerts?days=` (même
  paramètre `days`, même contenu — voir §10).
- `expiringSubscriptions` : abonnements `ACTIVE` dont `endsOn` tombe dans les
  `days` prochains jours.
- `todayRotations` : rotations dont `scheduledStart` tombe dans la journée
  calendaire courante (fuseau du serveur).
- `activeRotationsCount` : nombre de rotations `EN_COURS` en ce moment.
- `activeSubscriptionsCount` : nombre total d'abonnements `ACTIVE`.
- `monthlyRevenue` : somme des paiements encaissés depuis le 1er du mois
  civil en cours (FCFA).

---

## 8sexies. Ressource : Location de véhicules (`/rentals`)

Module M10 : loue un véhicule à un usager (`Passenger`) pour une période
donnée, **indépendamment** du système abonnement/rotation. Comme
`EN_SERVICE` est le statut générique « véhicule occupé » (déjà utilisé par
les rotations), un véhicule loué (`EN_COURS`) ne peut pas être affecté à une
rotation en même temps, et inversement — aucune règle de conflit
supplémentaire n'est nécessaire.

Rôles : `OWNER`, `MANAGER` pour tout (`GET`/`POST`/`PUT`/`PATCH`/paiements) ;
`DELETE` réservé à `OWNER`. Pas d'accès `CONTROLLER` (activité distincte du
contrôle d'embarquement de M4).

### Champs (`RentalDto`)

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `passengerId` / `passengerIdentifier` | UUID | oui (écriture) | usager existant |
| `vehicleId` / `vehicleIdentifier` | UUID | oui (écriture) | véhicule existant |
| `status` | enum `RentalStatus` | non | défaut `RESERVEE` |
| `startDate` / `endDate` | date | oui | `endDate ≥ startDate` |
| `actualReturnDate` | date | non | renseignée automatiquement au passage en `TERMINEE` si absente |
| `startMileageKm` / `endMileageKm` | number | non | ≥ 0 |
| `totalAmount` | number | oui | **saisi manuellement** (FCFA) — pas de calcul tarif × durée |
| `depositAmount` | number | non | caution (FCFA) |
| `notes` | string | non | |

`GET /rentals/{id}` renvoie un objet enrichi (`RentalDetailDto`) :
```json
{
  "rental": { "...": "RentalDto" },
  "payments": [ "...RentalPaymentDto" ]
}
```

### ⚠️ Effets de bord automatiques sur le véhicule

Même patron que les rotations (§8bis) :

- **Passer une location à `EN_COURS`** → rejetée en `400` si le véhicule
  n'est pas `DISPONIBLE`. Sinon, le véhicule bascule en `EN_SERVICE`.
- **Passer une location à `TERMINEE`** → le véhicule redevient `DISPONIBLE`,
  et son `mileageKm` est repris si `endMileageKm` est supérieur au
  kilométrage actuel.
- **Passer une location à `ANNULEE`** → même remise à disponible, sans
  reprise de kilométrage.
- **Suppression bloquée** si la location est `EN_COURS`.

### Paiements de location (`/rentals/{id}/payments`)

Circuit **séparé** de `/payments` (M5) — n'affecte jamais `Payment`/
`Subscription`. Un paiement de location peut référencer un moyen de paiement
enregistré (`paymentAccountId`, M9), vérifié comme appartenant au même
usager que la location.

| Champ | Type | Obligatoire | Règles |
|---|---|---|---|
| `amount` | number | oui | `≥ 0` (FCFA) |
| `method` | enum `PaymentMethod` | oui | |
| `collectedBy` | UUID | non | champ libre, non validé contre une table |
| `paymentAccountId` | UUID | non | doit appartenir à l'usager de la location, sinon `400` |

### Endpoints

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/rentals?status=&passengerId=&vehicleId=` | OWNER, MANAGER | liste filtrable |
| GET | `/rentals/{id}` | OWNER, MANAGER | détail + paiements |
| POST | `/rentals` | OWNER, MANAGER | création |
| PUT | `/rentals/{id}` | OWNER, MANAGER | remplacement complet |
| PATCH | `/rentals/{id}/status` | OWNER, MANAGER | `{ "status": "EN_COURS" }` |
| DELETE | `/rentals/{id}` | OWNER | suppression |
| GET, POST | `/rentals/{id}/payments` | OWNER, MANAGER | historique / encaissement |
| GET | `/vehicles/{id}/rentals` | OWNER, MANAGER | historique par véhicule |
| GET | `/passengers/{id}/rentals` | OWNER, MANAGER | historique par usager |

---

## 9. Ressource : Chauffeurs (`/drivers`)

| Champ | Type | Obligatoire | Règles |
|---|---|---|---|
| `fullName` | string | oui | |
| `phone` | string | oui | **unique** |
| `matricule` | string | non | |
| `licenseNumber` | string | oui | |
| `licenseCategory` | string | non | uppercase, défaut `D` |
| `licenseExpiresOn` | date | oui | |
| `remunerationType` | enum `RemunerationType` | non | défaut `FIXE` |
| `monthlySalary` | number | conditionnel | requis si `remunerationType = FIXE` |
| `tripRate` | number | conditionnel | requis si `remunerationType = PAR_TRAJET` |
| `commissionRate` | number | conditionnel | requis si `remunerationType = COMMISSION`, ≤ 100 |
| `status` | enum `DriverStatus` | non | défaut `DISPONIBLE` |
| `hiredOn` | date | non | |
| `staffProfileId` | UUID | non | rattache le chauffeur à un profil personnel (M8, §10quater) — doit référencer un profil existant |

Champs en lecture seule dans `DriverDto` : `daysUntilLicenseExpiry` (calculé),
`hasAccount` (booléen — `true` si le chauffeur est relié à un compte
Keycloak/`staff_profiles` via `staffProfileId`, sinon chauffeur "hors-système").

### Règles métier

- **Téléphone unique** parmi les chauffeurs.
- **Le montant doit correspondre au mode de rémunération** : le serveur exige
  exactement le champ correspondant à `remunerationType` (voir tableau
  ci-dessus) — à valider aussi côté formulaire pour éviter un aller-retour serveur.
- **Suppression bloquée** si le chauffeur est `EN_ROTATION`.
- `GET /drivers/expiring-licenses?days=30` liste les permis arrivant à échéance.
- `GET /drivers/payroll` (**OWNER seulement**) renvoie
  `{ "monthlyPayroll": 1234567.00 }`, la somme des `monthlySalary`.

---

## 10. Tableau de bord : `/fleet/alerts`

`GET /fleet/alerts?days=30` (rôles `OWNER`, `MANAGER`) agrège tout ce qui doit
apparaître dans un bloc « À surveiller » :

```json
{
  "expiringDocuments": [ "...DocumentDto" ],
  "expiringLicenses": [ "...DriverDto" ],
  "upcomingMaintenances": [ "...MaintenanceDto" ],
  "vehiclesTotal": 12,
  "vehiclesInService": 8,
  "vehiclesInMaintenance": 1,
  "monthlyPayroll": 3450000.00
}
```

`days` s'applique aux trois listes (documents, permis, entretiens) — la fenêtre
d'alerte par défaut est aussi pilotable côté serveur via
`transit.system.transport.alert-threshold-days` (30 jours par défaut).

---

## 10bis. Ressource : Identité usagers (M1)

Trois ressources distinctes : usagers, codes d'accès et tentatives de
connexion. `staff_profiles` (personnel) n'est jamais exposé — Keycloak reste
la source de vérité pour les comptes du personnel.

### Usagers (`/passengers`)

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `fullName` | string | oui | |
| `phone` | string | oui | **unique** |
| `status` | enum `PassengerStatus` | non | défaut `ACTIVE` — `ACTIVE`, `SUSPENDED`, `DISABLED` |

| Méthode | Path | Rôles |
|---|---|---|
| GET | `/passengers?status=` | OWNER, MANAGER, CONTROLLER |
| GET | `/passengers/{id}` | OWNER, MANAGER, CONTROLLER |
| POST | `/passengers` | OWNER, MANAGER |
| PUT | `/passengers/{id}` | OWNER, MANAGER |
| PATCH | `/passengers/{id}/status` | OWNER, MANAGER |
| DELETE | `/passengers/{id}` | OWNER |

### Codes d'accès (`/access-codes`)

CRUD manuel réservé à `OWNER`/`MANAGER` (création avec un code choisi à la
main). Pour la génération automatique liée à une rotation par le rôle
`CONTROLLER`, voir `POST /access-codes/generate` en §8ter.

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `vehicleIdentifier` | UUID | oui | véhicule existant |
| `rotationIdentifier` | UUID | — | lecture seule, renseigné si le code a été généré pour une rotation (§8ter) |
| `code` | string | oui (écriture) | jamais renvoyé en lecture — seul `POST /access-codes/generate` le renvoie en clair |
| `validFrom` / `validUntil` | date-heure | oui | `validUntil` postérieure à `validFrom` |
| `active` | — | lecture seule | calculé (`validFrom <= now <= validUntil`) |

| Méthode | Path | Rôles |
|---|---|---|
| GET | `/access-codes?vehicleId=` | OWNER, MANAGER |
| GET | `/access-codes/{id}` | OWNER, MANAGER |
| POST | `/access-codes` | OWNER, MANAGER |
| PUT | `/access-codes/{id}` | OWNER, MANAGER |
| DELETE | `/access-codes/{id}` | OWNER, MANAGER |
| POST | `/access-codes/generate` | OWNER, MANAGER, **CONTROLLER** — voir §8ter |

### Tentatives de connexion (`/login-attempts`)

Traçabilité générique des vérifications de code usager (succès et échecs).
Alimentée automatiquement par `POST /boarding/verify` (§8ter) ; CRUD manuel
disponible pour `OWNER`/`MANAGER`.

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `id` | number | — | entier auto-incrémenté (pas un UUID) |
| `identifier` | string | oui | texte libre (téléphone, plaque…) |
| `passengerIdentifier` | UUID | non | usager résolu, si trouvé |
| `success` | boolean | oui | |
| `ipAddress` | string | non | |
| `attemptedAt` | date-heure | — | lecture seule |

| Méthode | Path | Rôles |
|---|---|---|
| GET | `/login-attempts?passengerId=` | OWNER, MANAGER |
| GET | `/login-attempts/{id}` | OWNER, MANAGER |
| POST | `/login-attempts` | OWNER, MANAGER |
| DELETE | `/login-attempts/{id}` | OWNER, MANAGER |

---

## 10ter. Abonnements & Paiements (M5)

Un usager doit avoir un **abonnement actif** (couvrant la date du jour) pour
qu'un embarquement soit validé (§8ter). Un **paiement** ne s'enregistre
jamais seul : il finance toujours la création (`POST /subscriptions`) ou le
renouvellement (`POST /subscriptions/{id}/renew`) d'un abonnement — il n'y a
pas d'endpoint `POST /payments` autonome.

### Abonnements (`/subscriptions`)

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `passengerIdentifier` | UUID | oui (`passengerId` en écriture) | usager existant |
| `plan` | enum `SubscriptionPlan` | oui | `HEBDOMADAIRE` (7j), `MENSUEL` (30j), `TRIMESTRIEL` (90j) |
| `status` | enum `SubscriptionStatus` | — | `ACTIVE` (défaut à la création), `EXPIRED`, `CANCELLED` |
| `startsOn` / `endsOn` | date | — | calculées côté serveur à partir de `plan` |
| `daysRemaining` | — | lecture seule | jours restants avant `endsOn` (peut être négatif) |

`GET /subscriptions/{id}` renvoie un objet enrichi (`SubscriptionDetailDto`) :
```json
{
  "subscription": { "...": "SubscriptionDto" },
  "payments": [ "...PaymentDto" ]
}
```

#### Souscription initiale — `POST /subscriptions`

Rôles : `OWNER`, `MANAGER`, `CONTROLLER` (encaissement possible sur le
terrain, même logique que la génération de code en M4).

| Champ (requête) | Type | Obligatoire | Règles |
|---|---|---|---|
| `passengerId` | UUID | oui | |
| `plan` | string | oui | |
| `amount` | number | oui | `≥ 0`, montant du premier paiement (FCFA) |
| `method` | enum `PaymentMethod` | oui | `ESPECES`, `MOBILE_MONEY`, `VIREMENT`, `AUTRE` |
| `collectedBy` | UUID | non | champ libre, non validé contre une table |
| `paymentAccountId` | UUID | non | moyen de paiement enregistré (M9, §10quinquies) — doit appartenir au même usager, sinon `400` |

Crée l'abonnement (`startsOn = aujourd'hui`, `endsOn = aujourd'hui + durée du
plan`, `status = ACTIVE`) **et** le paiement associé en une seule opération.

#### Renouvellement — `POST /subscriptions/{id}/renew`

Mêmes rôles et mêmes champs que la souscription (`plan` optionnel — reconduit
la formule actuelle si absent), sans `passengerId`.

- Renouveler **avant** l'expiration prolonge `endsOn` à partir de la date de
  fin actuelle (pas de jours perdus).
- Renouveler **après** l'expiration repart de la date du jour et remet
  `status` à `ACTIVE`.

#### Autres endpoints

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/subscriptions?passengerId=&status=` | OWNER, MANAGER, CONTROLLER | liste filtrable |
| GET | `/subscriptions/{id}` | OWNER, MANAGER, CONTROLLER | détail + paiements |
| PATCH | `/subscriptions/{id}/status` | OWNER, MANAGER | override manuel (ex. `CANCELLED`) |
| DELETE | `/subscriptions/{id}` | OWNER | bloqué si `status == ACTIVE` |
| GET | `/passengers/{id}/subscriptions` | OWNER, MANAGER, CONTROLLER | historique par usager |

### Paiements (`/payments`) — lecture seule

Le ledger complet des encaissements ; écriture uniquement via
`/subscriptions` (souscription ou renouvellement) ci-dessus.

| Champ | Type | Règles |
|---|---|---|
| `identifier` | UUID | généré |
| `subscriptionIdentifier` | UUID | |
| `passengerIdentifier` / `passengerFullName` | — | résolus via l'abonnement |
| `amount` | number | FCFA |
| `method` | enum `PaymentMethod` | |
| `collectedBy` | UUID | champ libre, peut être `null` |
| `paymentAccountId` | UUID | moyen de paiement enregistré utilisé, peut être `null` (M9) |
| `paidAt` | date-heure | |

| Méthode | Path | Rôles |
|---|---|---|
| GET | `/payments?subscriptionId=` | OWNER, MANAGER |
| GET | `/payments/{id}` | OWNER, MANAGER |
| DELETE | `/payments/{id}` | OWNER (correction d'audit) |

---

## 10quater. Ressource : Personnel (`/staff-profiles`)

Module M8 : `staff_profiles` (table existante depuis M1) est le **miroir
métier** des comptes du personnel — Keycloak reste l'unique source de
vérité pour les identifiants/mots de passe/rôles réellement appliqués par
`@RolesAllowed`. Cette ressource ne gère ni authentification ni session.

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `keycloakSub` | UUID | oui | claim `sub` du compte Keycloak, **unique**, à copier manuellement depuis la console Keycloak — non validé par l'API (aucune intégration JWT) |
| `username` | string | oui | |
| `fullName` | string | oui | |
| `phone` / `email` | string | non | |
| `staffType` | enum `StaffType` | oui | `OWNER`, `MANAGER`, `CONTROLLER`, `DRIVER` — informatif côté API, ne modifie pas les rôles réels du token Keycloak |
| `matricule` | string | non | **unique** si renseigné |
| `active` | boolean | non | défaut `true` |
| `firstSeenAt` | date-heure | — | lecture seule, figée à la création |
| `lastLoginAt` | date-heure | — | lecture seule, jamais alimentée aujourd'hui (aucune intégration Keycloak) |

### Endpoints

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/staff-profiles?staffType=` | OWNER, MANAGER | liste filtrable |
| GET | `/staff-profiles/{id}` | OWNER, MANAGER | détail |
| POST | `/staff-profiles` | **OWNER seul** | création |
| PUT | `/staff-profiles/{id}` | **OWNER seul** | remplacement complet |
| PATCH | `/staff-profiles/{id}/active` | **OWNER seul** | `{ "active": "false" }` |
| DELETE | `/staff-profiles/{id}` | **OWNER seul** | bloqué si un chauffeur y est rattaché |

⚠️ Écriture réservée à `OWNER` uniquement (plus restrictif que `/drivers`,
`OWNER`+`MANAGER`) : `staffType` reflète qui a accès à quoi dans
l'organisation, donnée plus sensible que la gestion opérationnelle des
chauffeurs.

### Rattachement à un chauffeur

`Driver.staffProfileId` (voir §9) relie un chauffeur à un profil personnel
existant. La FK est posée en base depuis M2 mais n'était jusqu'ici
atteignable par aucun champ de `DriverRequest` — c'est désormais possible
via `PUT /drivers/{id}` (ou `POST /drivers`). Un profil rattaché à un
chauffeur ne peut pas être supprimé (`DELETE /staff-profiles/{id}` renvoie
`400`) tant que le chauffeur n'est pas détaché ou supprimé.

---

## 10quinquies. Moyens de paiement enregistrés (`/payment-accounts`)

Module M9 : un usager peut enregistrer un ou plusieurs moyens de paiement
réutilisables (numéro Mobile Money, compte…) au lieu de resaisir une
référence à chaque paiement d'abonnement.

| Champ | Type | Obligatoire (création) | Règles |
|---|---|---|---|
| `identifier` | UUID | — | généré, lecture seule |
| `passengerIdentifier` | UUID | oui (`passengerId` en écriture) | usager existant |
| `method` | enum `PaymentMethod` | oui | `ESPECES`, `MOBILE_MONEY`, `VIREMENT`, `AUTRE` |
| `label` | string | non | libellé libre (ex. « Orange Money principal ») |
| `reference` | string | oui | numéro/compte du moyen de paiement |
| `defaultAccount` | boolean | non | défaut `false` — un seul moyen par défaut actif par usager (en fixer un nouveau désactive automatiquement l'ancien) |
| `active` | boolean | non | défaut `true` |

### Endpoints

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| GET | `/payment-accounts/{id}` | OWNER, MANAGER, CONTROLLER | détail |
| POST | `/payment-accounts` | OWNER, MANAGER, CONTROLLER | création |
| PUT | `/payment-accounts/{id}` | OWNER, MANAGER | remplacement complet |
| PATCH | `/payment-accounts/{id}/active` | OWNER, MANAGER | `{ "active": "false" }` |
| DELETE | `/payment-accounts/{id}` | OWNER | suppression |
| GET | `/passengers/{id}/payment-accounts` | OWNER, MANAGER, CONTROLLER | liste par usager |

Un moyen de paiement enregistré peut être référencé par `paymentAccountId`
lors de la souscription/renouvellement d'un abonnement (voir §10ter) — le
serveur vérifie qu'il appartient bien à l'usager de l'abonnement, sinon
`400`.

---

## 11. Exemples rapides (curl, en local sans token)

```bash
# Lister les véhicules disponibles
curl http://localhost:8080/api/vehicles/available

# Créer un véhicule
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -d '{"plateNumber":"4521 CI 01","brand":"Toyota","model":"Coaster","capacity":30}'

# Changer son statut
curl -X PATCH http://localhost:8080/api/vehicles/<id>/status \
  -H "Content-Type: application/json" \
  -d '{"status":"EN_SERVICE"}'

# Tableau de bord
curl http://localhost:8080/api/fleet/alerts?days=30
```

---

## 12. Schéma de données

Pour le détail des tables, colonnes et cardinalités, voir le diagramme
conceptuel (notation Merise) :
https://claude.ai/code/artifact/080aeab6-3ad5-4d6d-bb38-e0f50ff119cc

⚠️ Ce diagramme date d'avant M3-M8 (rotations, contrôle, abonnements,
lignes, tableau de bord, personnel) — il ne couvre que le périmètre initial
(Flotte + Identité). Se référer aux migrations `V6` à `V13` pour le schéma
à jour de ces modules.
