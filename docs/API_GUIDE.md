# Guide API — Transport Server (module Flotte)

Ce document décrit l'API REST exposée par `transport-server-impl-quarkus` pour le
développement du front. Il couvre uniquement ce qui est **réellement implémenté**
aujourd'hui : le référentiel flotte (véhicules, documents, entretiens, chauffeurs) et
le tableau de bord associé.

> Pour explorer l'API de façon interactive : **Swagger UI** sur
> `http://localhost:8080/api/swagger-ui` (spec OpenAPI brute sur `/api/openapi`).
> Le schéma conceptuel des tables est disponible ici :
> https://claude.ai/code/artifact/080aeab6-3ad5-4d6d-bb38-e0f50ff119cc

---

## 1. Ce qui est exposé (et ce qui ne l'est pas encore)

La base contient deux périmètres (voir les migrations `V1__auth_schema.sql` et
`V3__fleet_schema.sql`) :

| Périmètre | Tables | Exposé en REST ? |
|---|---|---|
| **Identité** (M1) | `staff_profiles`, `passengers`, `access_codes`, `login_attempts` | ❌ pas encore — géré via Keycloak / modules à venir |
| **Flotte** (M2/M3) | `vehicles`, `vehicle_documents`, `maintenances`, `drivers` | ✅ objet de ce guide |

`staff_profiles` et Keycloak restent la source de vérité pour les comptes/rôles :
l'API ne gère ni login, ni mot de passe, ni session — uniquement les données métier
de la flotte.

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
financière), mais `/fleet/alerts` (accessible à `MANAGER`) renvoie aussi
`monthlyPayroll` dans sa réponse. Un `MANAGER` peut donc voir la masse salariale
via le tableau de bord. À garder en tête côté front (et à corriger côté API si ce
n'est pas voulu).

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

Champs en lecture seule dans `DriverDto` : `daysUntilLicenseExpiry` (calculé),
`hasAccount` (booléen — `true` si le chauffeur est relié à un compte
Keycloak/`staff_profiles`, sinon chauffeur "hors-système").

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

Pour le détail des tables, colonnes et cardinalités (y compris le périmètre
Identité pas encore exposé en REST), voir le diagramme conceptuel (notation
Merise) : https://claude.ai/code/artifact/080aeab6-3ad5-4d6d-bb38-e0f50ff119cc
