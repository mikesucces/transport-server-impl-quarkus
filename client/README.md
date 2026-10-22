# Transit : administration web

Application d'administration (Quarkus + PrimeFaces) qui consomme l'API REST
`transport-server-impl-quarkus`. Connexion via Keycloak (Authorization Code + PKCE) ; le jeton de
l'utilisateur est transmis à l'API à chaque appel.

## Écrans

Tableau de bord (indicateurs, état du parc, points à surveiller), cars, documents des cars,
entretiens, chauffeurs, lignes, horaires, rotations, usagers, abonnements, locations.
Interface en français, adaptée au téléphone (tableaux en cartes) et à la tablette.

## Prérequis

- **Java 21** et **Maven 3.9+**
- l'API démarrée avec la sécurité active (`OIDC_ENABLED=true` **à la compilation**, voir le README de l'API)
- un Keycloak avec le realm `transit` et le client public `transport`
  (redirection `http://localhost:8081/*`, PKCE S256), des utilisateurs ayant les rôles
  `OWNER`, `MANAGER`, `CONTROLLER` ou `DRIVER`

## Lancer

```bash
mvn quarkus:dev          # http://localhost:8081
```

| Variable | Défaut | Rôle |
|---|---|---|
| `TRANSPORT_API_URL` | `http://localhost:8080/api` | adresse de l'API |
| `KEYCLOAK_URL` | `http://localhost:8230/auth/realms/transit` | realm Keycloak (émetteur des jetons) |
| `OIDC_STATE_SECRET` | valeur de développement | secret de chiffrement de session, **32 caractères minimum, à changer hors développement** |

## Tests de bout en bout

Le dossier [`e2e/`](e2e/README.md) contient un script (Python 3, sans dépendance) qui se connecte via
Keycloak et rejoue les actions de l'interface sur tous les modules :

```bash
python e2e/e2e.py e2e/scenario_complet.py
```

## Organisation

- `src/main/java/ci/transit/admin/client` : client REST de l'API
- `src/main/java/ci/transit/admin/dto` : copies des DTO de l'API utilisés par l'écran
- `src/main/java/ci/transit/admin/bean` : beans JSF (un par écran) et aides d'affichage (`UiBean`)
- `src/main/resources/META-INF/resources` : pages `.xhtml`, gabarit `templates/layout.xhtml`, style `resources/css/admin.css`

Les DTO sont des copies partielles de ceux de l'API : en cas de changement côté API, mettre à jour la copie.
