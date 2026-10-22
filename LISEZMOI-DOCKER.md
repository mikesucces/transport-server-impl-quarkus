# Transit : lancer toute la plateforme avec Docker

> `docker-compose.yml` (README principal) ne démarre que l'API et la base, sans authentification.
> Ce guide concerne `docker-compose.full.yml` : plateforme complète avec Keycloak et l'administration web.

Une seule commande démarre la base de données (avec les données de démonstration),
Keycloak (connexion et rôles), l'API et l'administration web. Aucun besoin d'installer
Java, Maven ou PostgreSQL : seul **Docker Desktop** est requis.

## Démarrage

```bash
docker compose -f docker-compose.full.yml up --build
```

Le premier lancement construit les images (5 à 10 minutes selon la connexion). Les suivants
démarrent en moins d'une minute. Quand le terminal affiche que `admin` est démarré, tout est prêt.

| Service | Adresse | Identifiants |
|---|---|---|
| **Administration web** | http://localhost:8081 | `admin` / `Transit2026!` |
| **API** | http://localhost:8080/api | jeton Keycloak (voir plus bas) |
| **Swagger UI** | http://localhost:8080/swagger-ui/ (spécification : `/openapi`) | bouton **Authorize** avec un jeton |
| **Keycloak (console)** | http://localhost:8180 | `admin` / `admin` |
| **PostgreSQL** | `localhost:5432`, base `transit` | utilisateur `transit` / mot de passe `transit` |

Pour arrêter : `Ctrl+C`, ou `docker compose -f docker-compose.full.yml down` (les données de la base sont conservées).
Pour repartir de zéro (base remise aux données de démonstration) : ajouter `-v` à la commande `down`.

> Astuce : exporter `COMPOSE_FILE=docker-compose.full.yml` évite de répéter `-f` à chaque commande.

## Comptes de test (realm Keycloak `transit`)

Mot de passe commun : `Transit2026!`

| Utilisateur | Rôle | Usage prévu |
|---|---|---|
| `admin` | OWNER | accès complet, y compris les données financières |
| `manager` | MANAGER | exploitation (flotte, lignes, abonnements) |
| `controller` | CONTROLLER | contrôle des embarquements |
| `driver` | DRIVER | rotations du chauffeur, kilométrage |

Les droits de chaque rôle sont détaillés dans `docs/API_GUIDE.md`.

## Appeler l'API (Postman, curl, application mobile)

L'API exige un jeton. Le client Keycloak `transport-dev` (développement uniquement) permet
d'en obtenir un avec un identifiant et un mot de passe :

```bash
curl -s -X POST http://localhost:8180/realms/transit/protocol/openid-connect/token \
  -d grant_type=password -d client_id=transport-dev \
  -d username=driver -d password=Transit2026!
```

Utiliser ensuite `access_token` : `Authorization: Bearer <jeton>`.
Dans Swagger UI (http://localhost:8080/swagger-ui/), le bouton **Authorize** accepte ce même jeton.

Exemple : `curl -H "Authorization: Bearer <jeton>" http://localhost:8080/api/vehicles`

> Le jeton est valable 30 minutes. L'émetteur (`iss`) est toujours `http://localhost:8180/realms/transit`,
> y compris pour les appels faits depuis un conteneur.

## Accès direct à la base

```bash
docker compose -f docker-compose.full.yml exec db psql -U transit -d transit
```

Les tables et les données de démonstration viennent des migrations Flyway
(`src/main/resources/db/migration`). Elles sont rejouées
automatiquement au démarrage de l'API.

## Variables optionnelles

Créer un fichier `.env` à côté de `docker-compose.full.yml` (voir `.env.example`) :

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_PASSWORD` | `transit` | mot de passe PostgreSQL |
| `DB_PORT` | `5432` | port exposé de PostgreSQL (utile si 5432 est déjà pris) |
| `KEYCLOAK_ADMIN_PASSWORD` | `admin` | mot de passe de la console Keycloak |
| `OIDC_STATE_SECRET` | valeur de dev | secret de chiffrement de session de l'administration (32 caractères minimum) |

## Problèmes fréquents

- **Un port est déjà utilisé** (8080, 8081, 8180 ou 5432) : arrêter l'application qui l'occupe
  (par exemple une base PostgreSQL locale), ou changer `DB_PORT` pour la base.
- **L'administration affiche « Forbidden » ou boucle sur la connexion** : vider les cookies de
  `localhost`, ou ouvrir une fenêtre de navigation privée.
- **Safari** : Keycloak 26 pose ses cookies de session en `Secure`, que Safari refuse sur
  `http://localhost`. Utiliser Chrome, Edge ou Firefox pour se connecter en local.
- **Modifier le code de l'API ou de l'administration** : relancer avec `docker compose -f docker-compose.full.yml up --build`
  (les images sont reconstruites, la base est conservée).
- **Les ports 8081 ou 8180 doivent changer** : modifier aussi les adresses de redirection du
  client `transport` dans `keycloak/realm-transit.json` puis recréer Keycloak
  (`down` puis `up`).

## Pour l'application mobile

- Émulateur Android : l'hôte `localhost` de l'ordinateur s'appelle `10.0.2.2` depuis l'émulateur.
  Les jetons Keycloak étant émis pour `http://localhost:8180`, le plus simple est de rediriger les
  ports avec `adb reverse tcp:8080 tcp:8080` et `adb reverse tcp:8180 tcp:8180`.
- Téléphone réel : utiliser l'adresse IP de l'ordinateur, et adapter `KC_HOSTNAME` dans
  `docker-compose.full.yml` ainsi que les `redirectUris` du client mobile à créer dans Keycloak.

## Sécurité

Cette configuration est faite pour le **développement local** : mots de passe par défaut, Keycloak
en mode `start-dev`, HTTP sans chiffrement, client `transport-dev` permettant la connexion directe.
Ne pas l'exposer sur Internet ni l'utiliser en production sans durcissement.
