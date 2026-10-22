# Transit : gestion de transport par abonnement

Plateforme de gestion d'une activité de transport en cars : flotte, chauffeurs, lignes et horaires,
rotations, usagers, abonnements, locations et contrôle d'embarquement. Les montants sont en francs CFA.

Elle se compose d'une **API REST**, d'une **administration web** et d'un **serveur d'authentification**.
Une application mobile est prévue (voir [État d'avancement](#état-davancement)).

## Architecture

```
 Navigateur ──► Administration web  ──► API REST (Quarkus) ──► PostgreSQL
                (Quarkus + PrimeFaces)   /api                    (migrations Flyway)
                :8081            ▲          :8080
                    │            │ jeton                    
                    └──► Keycloak ◄────────┘  (connexion, rôles OWNER / MANAGER / CONTROLLER / DRIVER)
                         :8180 (Docker)
```

- L'utilisateur se connecte à l'administration via Keycloak (Authorization Code + PKCE).
- L'administration transmet son jeton à l'API, qui contrôle le rôle de chaque appel.
- Keycloak reste seul maître des comptes et des mots de passe ; la base ne stocke que les données métier.
  Configuration détaillée : [`docs/KEYCLOAK.md`](docs/KEYCLOAK.md).

## Contenu du dépôt

| Dossier ou fichier | Rôle |
|---|---|
| `src/`, `pom.xml` | **L'API** REST (Quarkus) : code, migrations Flyway, données de démonstration. Voir [`README.md`](README.md) |
| [`client/`](client/README.md) | **L'administration web** (Quarkus + PrimeFaces) |
| [`docs/API_GUIDE.md`](docs/API_GUIDE.md) | Guide de l'API : endpoints, rôles, règles métier |
| [`docs/KEYCLOAK.md`](docs/KEYCLOAK.md) | **Configuration de Keycloak** : realm, rôles, clients, connexion de l'API et de l'administration |
| [`docker-compose.full.yml`](docker-compose.full.yml) | Plateforme complète : base, Keycloak, API, administration |
| [`keycloak/realm-transit.json`](keycloak/realm-transit.json) | Realm Keycloak importé automatiquement par Docker |
| [`LISEZMOI-DOCKER.md`](LISEZMOI-DOCKER.md) | Guide Docker (lancement, jetons, dépannage) |
| `docker-compose.yml` | API et base seules, sans authentification (développement du front) |

## Démarrage rapide (Docker, recommandé)

Seul Docker est nécessaire. ```bash
docker compose -f docker-compose.full.yml up --build
```

| Service | Adresse | Identifiants |
|---|---|---|
| Administration web | http://localhost:8081/transit/index.xhtml | `admin` / `Transit2026!` |
| API | http://localhost:8080/api | jeton Keycloak |
| Swagger UI | http://localhost:8080/swagger-ui/ | bouton *Authorize* avec un jeton |
| Keycloak (console) | http://localhost:8180 | `admin` / `admin` |
| PostgreSQL | `localhost:5432` | base `transit`, utilisateur `transit` / `transit` |

Le premier lancement construit les images (5 à 10 minutes). Les données de démonstration sont créées
automatiquement. Détails, jetons pour Postman et dépannage : [`LISEZMOI-DOCKER.md`](LISEZMOI-DOCKER.md).

> Utiliser Chrome, Edge ou Firefox : Safari refuse les cookies de session Keycloak sur `http://localhost`.

### Sans Docker (développement)

Prérequis : Java 21, Maven 3.9+, PostgreSQL et un Keycloak avec le realm `transit`.

1. API : `mvn quarkus:dev` (sans sécurité en développement), ou voir le [README de l'API](README.md).
2. Administration : `mvn quarkus:dev` dans `client/` (nécessite l'API **compilée avec `OIDC_ENABLED=true`** et Keycloak).

## Comptes de test et rôles

Mot de passe commun : `Transit2026!`. Ces comptes sont affichés sur la page de connexion (un clic remplit le formulaire).

| Utilisateur | Rôle | Accès |
|---|---|---|
| `admin` | OWNER | tout, y compris la masse salariale |
| `manager` | MANAGER | flotte, lignes, rotations, abonnements, tableau de bord |
| `controller` | CONTROLLER | contrôle des embarquements |
| `driver` | DRIVER | ses rotations, kilométrage des cars |

Le détail des droits endpoint par endpoint est dans [`docs/API_GUIDE.md`](docs/API_GUIDE.md) ; la définition des comptes et des rôles dans [`docs/KEYCLOAK.md`](docs/KEYCLOAK.md).

## Modules fonctionnels

| Module | Contenu |
|---|---|
| M1 Identité | usagers, codes d'accès, historique de connexions |
| M2 Flotte | cars, documents (assurance, visite technique…), entretiens, chauffeurs |
| M3 Rotations | affectation chauffeur / car, rattachement optionnel à une ligne |
| M4 Contrôle | génération et vérification des codes d'embarquement, présences |
| M5 Abonnements | formules hebdomadaire, mensuelle, trimestrielle, paiements |
| M6 Lignes | lignes et horaires par jour |
| M7 Tableau de bord | indicateurs et alertes d'échéance |
| M8 Personnel | profils du personnel (comptes gérés par Keycloak) |
| M9 Moyens de paiement | comptes de paiement enregistrés des usagers |
| M10 Locations | réservation et location de cars, paiements |

## Technologies

| Couche | Choix |
|---|---|
| API | Java 21, Quarkus 3.15, Hibernate ORM, Flyway, SmallRye OpenAPI |
| Base de données | PostgreSQL 16 |
| Administration | Quarkus + PrimeFaces (extension Quarkiverse `quarkus-primefaces`), client REST |
| Authentification | Keycloak (OIDC), jetons JWT, rôles dans `realm_access.roles` ([configuration](docs/KEYCLOAK.md)) |
| Conteneurs | Docker Compose (images construites depuis les sources) |

## Administration web

Interface en français, adaptée au téléphone et à la tablette : tableau de bord (indicateurs, état du parc,
points à surveiller), cars, documents, entretiens, chauffeurs, lignes, horaires, rotations, usagers,
abonnements, locations. Recherche et filtres, confirmation avant suppression, messages d'erreur de l'API.

## Tests

Un test de bout en bout rejoue les actions de l'interface (connexion Keycloak, création, changement de
statut, suppression, recherche) sur tous les modules. Python 3 suffit :

```bash
python client/e2e/e2e.py client/e2e/scenario_complet.py
```

Dernier résultat sur une plateforme Docker neuve : 39 vérifications sur 39. Voir
[`client/e2e/README.md`](client/e2e/README.md).

## État d'avancement

**Terminé**
- API REST des 10 modules, sécurisée par Keycloak, migrations et données de démonstration
- Administration web complète (11 écrans), responsive
- Plateforme Docker complète et testée

**À faire**
- **Application mobile** : périmètre (contrôleur, chauffeur, usager) et technologie (Flutter, React Native,
  web mobile ou Android natif) à choisir.
- Écrans d'administration non réalisés : moyens de paiement, codes d'accès, historique des connexions, personnel.
- Modification d'un document, d'un entretien ou d'une location existants (aujourd'hui : ajout et suppression).
- Page de connexion Keycloak aux couleurs de Transit.

**Points d'attention connus**
- La suppression d'un abonnement ayant des paiements est refusée (400, message clair) : un abonnement
  se clôt par l'annulation. Correction présente en local dans `SubscriptionService.java`, pas encore commitée.
- Le tableau de bord (`/dashboard`) renvoie `monthlyPayroll` à un `MANAGER`, alors que `/drivers/payroll`
  est réservé à `OWNER` (voir `docs/API_GUIDE.md`).
- Configuration Docker prévue pour le développement local uniquement : mots de passe par défaut,
  Keycloak en `start-dev`, HTTP sans chiffrement.
