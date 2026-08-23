# transport-server-impl-quarkus
Gestion des transports du personnel

API REST (Quarkus) exposant le référentiel flotte (véhicules, chauffeurs, documents, entretiens) et le module identité usagers (usagers, codes d'accès, historique de connexions).

## Prérequis

- **Java 21**
- **Maven 3.9+**
- **Docker** + **Docker Compose**

## Démarrage rapide

```bash
git clone https://github.com/mikesucces/transport-server-impl-quarkus.git
cd transport-server-impl-quarkus

mvn package -DskipTests   # produit target/quarkus-app, requis avant le build Docker
docker compose up --build
```

Ça démarre deux conteneurs : la base Postgres (pré-remplie via les migrations Flyway) et l'API.

- **API** : http://localhost:8080/api
- **Swagger UI** : http://localhost:8080/swagger-ui/ (spec brute sur `/openapi`)
- **Aucun token requis** : `DEV_BYPASS_ROLES=true` est déjà activé dans `docker-compose.yml`, tous les rôles sont accordés automatiquement en local. **Ne jamais activer cette variable ailleurs qu'en local.**
- Des données de démonstration sont déjà en base au premier démarrage (véhicules, chauffeurs, usagers, un code d'accès actif, un historique de tentatives de connexion) — voir `src/main/resources/db/migration/V2`, `V4` et `V5`.

Pour arrêter : `docker compose down` (ajouter `-v` pour repartir d'une base vide au prochain démarrage).

## Mode développement (hot-reload)

Sans passer par Docker pour l'API — utile pour itérer sur le code :

```bash
docker run -d --name transport-pg \
  -e POSTGRES_USER=transit -e POSTGRES_PASSWORD=transit -e POSTGRES_DB=transit \
  -p 5432:5432 postgres:16

mvn quarkus:dev
```

Quarkus recharge automatiquement à chaque requête après une modification du code. Sans `DEV_BYPASS_ROLES=true` dans l'environnement, les endpoints protégés répondent 403 (comportement normal, voir la variable ci-dessous).

## Variables d'environnement

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/transit` | URL JDBC de la base |
| `DB_USER` / `DB_PASSWORD` | `transit` / `transit` | Identifiants base |
| `OIDC_ENABLED` | `false` | Active la vérification des tokens Keycloak |
| `DEV_BYPASS_ROLES` | `false` | Accorde tous les rôles aux requêtes anonymes — **local/dev uniquement** |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origines front autorisées (CRA/Vite) |

## Documentation API

Le détail des endpoints, rôles, règles métier et énumérations est dans [`docs/API_GUIDE.md`](docs/API_GUIDE.md).
