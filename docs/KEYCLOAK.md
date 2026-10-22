# Configuration de Keycloak

Keycloak gère la **connexion**, les **mots de passe** et les **rôles** des utilisateurs. Ni l'API ni la base
de données ne stockent de mot de passe : l'API vérifie seulement le jeton (JWT) envoyé par l'appelant et lit
le rôle qu'il contient.

- [1. Où se trouvent les paramètres](#1-où-se-trouvent-les-paramètres)
- [2. Le realm `transit`](#2-le-realm-transit)
- [3. Comment l'API se connecte](#3-comment-lapi-se-connecte)
- [4. Comment l'administration se connecte](#4-comment-ladministration-se-connecte)
- [5. Adresses selon l'environnement](#5-adresses-selon-lenvironnement)
- [6. Obtenir un jeton](#6-obtenir-un-jeton)
- [7. Installer le realm sur un Keycloak existant](#7-installer-le-realm-sur-un-keycloak-existant)
- [8. Ajouter un utilisateur, un rôle, un client](#8-ajouter-un-utilisateur-un-rôle-un-client)
- [9. Dépannage](#9-dépannage)
- [10. Sécurité](#10-sécurité)

## 1. Où se trouvent les paramètres

| Fichier | Ce qu'il contient |
|---|---|
| [`keycloak/realm-transit.json`](../keycloak/realm-transit.json) | Le realm complet : rôles, clients, utilisateurs de test. Importé automatiquement par Docker |
| [`docker-compose.full.yml`](../docker-compose.full.yml) | Le conteneur Keycloak (version, adresse publique, import du realm) et les adresses données à l'API et à l'administration |
| [`src/main/resources/application.properties`](../src/main/resources/application.properties) | Connexion de l'**API** : propriétés `quarkus.oidc.*` |
| [`client/src/main/resources/application.properties`](../client/src/main/resources/application.properties) | Connexion de l'**administration** : propriétés `quarkus.oidc.*` |

Tout ce qui varie d'un environnement à l'autre se règle par variables d'environnement :

| Variable | Utilisée par | Rôle |
|---|---|---|
| `KEYCLOAK_URL` | API, administration | Adresse du realm, **émetteur des jetons** (voir §5) |
| `OIDC_ENABLED` | API | Active la vérification des jetons. **Lue à la compilation** : recompiler après l'avoir changée |
| `DEV_BYPASS_ROLES` | API | Accorde tous les rôles aux appels anonymes. **Développement local uniquement** |
| `OIDC_STATE_SECRET` | administration | Secret de chiffrement de la session, 32 caractères minimum |
| `CORS_ORIGINS` | API | Origines web autorisées à appeler l'API depuis un navigateur |
| `KEYCLOAK_ADMIN_PASSWORD` | Docker | Mot de passe de la console d'administration de Keycloak |

## 2. Le realm `transit`

### Rôles

| Rôle | Usage |
|---|---|
| `OWNER` | Propriétaire : accès complet, y compris la masse salariale |
| `MANAGER` | Gestionnaire : flotte, lignes, rotations, abonnements, tableau de bord |
| `CONTROLLER` | Contrôleur : vérification des embarquements |
| `DRIVER` | Chauffeur : ses rotations, kilométrage des cars |

Les droits de chaque rôle, endpoint par endpoint, sont dans [`API_GUIDE.md`](API_GUIDE.md).
Les noms doivent être **exactement** ceux-ci (majuscules) : l'API les compare tels quels.

### Clients

| Client | Type | Usage |
|---|---|---|
| `transport` | public, flux *Authorization Code* + PKCE (S256) | Connexion de l'**administration web**. Redirection autorisée : `http://localhost:8081/*` |
| `transport-dev` | public, *Direct Access Grants* | Obtenir un jeton avec identifiant et mot de passe (Postman, curl, Swagger, tests). **Développement uniquement** |

L'API n'a pas besoin de client dédié : elle se contente de vérifier les jetons émis par les autres clients.

### Utilisateurs de test

Mot de passe commun : `Transit2026!`

| Utilisateur | Rôle |
|---|---|
| `admin` | OWNER |
| `manager` | MANAGER |
| `controller` | CONTROLLER |
| `driver` | DRIVER |

Durée de vie d'un jeton d'accès : 30 minutes.

## 3. Comment l'API se connecte

```properties
quarkus.oidc.enabled=${OIDC_ENABLED:false}
quarkus.oidc.auth-server-url=${KEYCLOAK_URL:http://localhost:8180/realms/transit}
quarkus.oidc.client-id=transport-server
quarkus.oidc.application-type=service
quarkus.oidc.roles.source=accesstoken
quarkus.oidc.roles.role-claim-path=realm_access/roles
```

- `application-type=service` : l'API ne gère pas de connexion, elle **valide** un jeton `Authorization: Bearer <jeton>`.
- Les rôles sont lus dans le **jeton d'accès**, à `realm_access.roles`. Si cette section est absente du jeton,
  tous les appels reçoivent `403` (voir §9).
- Sans jeton valide : `401`. Avec un jeton valide mais un rôle insuffisant : `403`.
- En profil `dev` (`mvn quarkus:dev`) et `test`, OIDC est désactivé : aucun jeton n'est demandé.
- `OIDC_ENABLED` est fixée **à la compilation**. Pour une API sécurisée :
  `OIDC_ENABLED=true mvn package -DskipTests`. L'image Docker est déjà construite ainsi.

## 4. Comment l'administration se connecte

```properties
quarkus.oidc.application-type=web-app
quarkus.oidc.auth-server-url=${KEYCLOAK_URL:http://localhost:8230/auth/realms/transit}
quarkus.oidc.client-id=transport
quarkus.oidc.roles.role-claim-path=realm_access/roles
quarkus.oidc.authentication.pkce-required=true
quarkus.oidc.authentication.scopes=openid,profile
quarkus.oidc.token-state-manager.encryption-secret=${OIDC_STATE_SECRET:...}
quarkus.oidc.logout.path=/logout
quarkus.oidc.logout.post-logout-path=/index.xhtml
quarkus.http.auth.permission.pages.paths=/*
quarkus.http.auth.permission.pages.policy=authenticated
```

1. Un visiteur non connecté est redirigé vers la page de connexion Keycloak.
2. Après connexion, Keycloak le renvoie vers l'administration (`http://localhost:8081/...`) avec un code.
3. L'administration échange ce code contre un jeton et ouvre une session chiffrée.
4. À chaque appel de l'API, le jeton de l'utilisateur est transmis (extension `rest-client-oidc-token-propagation`) :
   **c'est donc le rôle de la personne connectée qui est contrôlé par l'API**, pas celui d'un compte technique.
5. Le lien « Se déconnecter » appelle `/logout`, qui ferme aussi la session Keycloak.

## 5. Adresses selon l'environnement

L'**émetteur** (`iss`) contenu dans le jeton doit être **identique** à l'adresse `KEYCLOAK_URL` utilisée par l'API
pour le vérifier. C'est la principale source d'erreurs `401`.

| Environnement | Navigateur | `KEYCLOAK_URL` |
|---|---|---|
| **Docker** (`docker-compose.full.yml`) | `http://localhost:8180` | `http://keycloak:8080/realms/transit` (entre conteneurs) |
| **Local** avec le Keycloak 11 partagé | `http://localhost:8230/auth` | `http://localhost:8230/auth/realms/transit` |
| Autre Keycloak (≥ 17) | `http://hote:port` | `http://hote:port/realms/transit` |

Particularités de Docker : le navigateur ne connaît pas le nom `keycloak`. Le conteneur est donc lancé avec
`KC_HOSTNAME=http://localhost:8180` et `KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true` : l'émetteur reste
`http://localhost:8180/realms/transit` pour tout le monde, même quand l'API l'interroge via `keycloak:8080`.

Les versions de Keycloak avant 17 ajoutent `/auth` devant le chemin ; les versions récentes non.

## 6. Obtenir un jeton

Avec le client `transport-dev` (Docker) :

```bash
curl -s -X POST http://localhost:8180/realms/transit/protocol/openid-connect/token \
  -d grant_type=password -d client_id=transport-dev \
  -d username=manager -d password=Transit2026!
```

Puis, avec le champ `access_token` de la réponse :

```bash
curl -H "Authorization: Bearer <jeton>" http://localhost:8080/api/vehicles
```

Pour vérifier le contenu d'un jeton, le coller sur https://jwt.io ou décoder sa partie centrale (base64).
Il doit contenir :

```json
{ "iss": "http://localhost:8180/realms/transit",
  "preferred_username": "manager",
  "realm_access": { "roles": ["MANAGER", "offline_access", "uma_authorization"] } }
```

## 7. Installer le realm sur un Keycloak existant

### Option A : importer le fichier

Dans la console d'administration : **Create realm** (ou *Add realm* sur les anciennes versions) puis
**Import / Select file** et choisir [`keycloak/realm-transit.json`](../keycloak/realm-transit.json).
Rôles, clients et utilisateurs de test sont créés d'un coup. Cette option n'a été testée que sur Keycloak 26.

### Option B : création manuelle

1. **Realm** : créer `transit`.
2. **Realm roles** : créer `OWNER`, `MANAGER`, `CONTROLLER`, `DRIVER`.
3. **Clients** : créer `transport`
   - *Client authentication* : désactivée (client **public**)
   - *Standard flow* : activé ; *Direct access grants* : désactivé
   - *Valid redirect URIs* : `http://localhost:8081/*` ; *Web origins* : `http://localhost:8081`
   - *Advanced* > *Proof Key for Code Exchange* : `S256`
   - *Valid post logout redirect URIs* : `http://localhost:8081/*`
4. **Users** : créer un utilisateur, lui définir un mot de passe (*Temporary* désactivé), puis dans
   *Role mapping* lui attribuer un rôle du realm.
5. **Vérifier** qu'un jeton obtenu pour cet utilisateur contient bien `realm_access.roles` (§6). Il vient du
   *client scope* `roles`, présent par défaut sur les clients.
6. Régler `KEYCLOAK_URL` de l'API et de l'administration (§5) et **recompiler l'API avec `OIDC_ENABLED=true`**.

## 8. Ajouter un utilisateur, un rôle, un client

- **Nouvel utilisateur** : console Keycloak, realm `transit`, *Users* > *Add user*, mot de passe dans *Credentials*,
  rôle dans *Role mapping*. Aucune modification de l'API n'est nécessaire.
- **Changer le rôle de quelqu'un** : *Users* > l'utilisateur > *Role mapping*. Il doit se reconnecter pour que le
  nouveau rôle apparaisse dans son jeton.
- **Nouvel écran qui limite un rôle** : le rôle doit déjà exister dans Keycloak ; la règle se déclare côté API
  avec `@RolesAllowed(...)`.
- **Application mobile** (à venir) : créer un client **public** avec PKCE (`S256`) et pour redirection l'adresse
  propre à l'application (schéma personnalisé du type `ci.transit.mobile:/callback`). L'API n'est pas modifiée.

Les changements faits dans la console ne sont **pas** reportés dans `realm-transit.json` : après
`docker compose down -v`, le realm est recréé à partir du fichier. Pour conserver un ajout, le reporter dans le
fichier (ou exporter le realm depuis la console).

## 9. Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| L'API répond `401` | Jeton absent, expiré, ou émetteur différent de `KEYCLOAK_URL` | Comparer `iss` du jeton (§6) et `KEYCLOAK_URL` (§5) |
| L'API répond `403` avec un jeton valide | Rôle insuffisant, ou `realm_access` absent du jeton | Vérifier le rôle de l'utilisateur et le *client scope* `roles` du client |
| L'API ne vérifie aucun jeton | API compilée sans `OIDC_ENABLED=true` | Recompiler avec `OIDC_ENABLED=true` |
| L'administration affiche « Forbidden » | Cookies d'une ancienne session | Vider les cookies de `localhost`, ou navigation privée |
| Boucle sur la page de connexion | Redirection non autorisée dans le client `transport` | Vérifier *Valid redirect URIs* (`http://localhost:8081/*`) |
| « Invalid parameter: redirect_uri » | Port ou adresse de l'administration différent | Mettre à jour les redirections du client `transport` |
| Connexion impossible sous Safari | Cookies `Secure` refusés sur `http://localhost` | Utiliser Chrome, Edge ou Firefox, ou passer en HTTPS |
| L'administration ne démarre pas | Keycloak injoignable ou `KEYCLOAK_URL` erronée | Ouvrir `KEYCLOAK_URL/.well-known/openid-configuration` dans un navigateur |

## 10. Sécurité

Ce paramétrage est destiné au **développement local** :

- mots de passe de test connus (`Transit2026!`, `admin` / `admin`) ;
- Keycloak en mode `start-dev`, en HTTP, avec base H2 intégrée ;
- client `transport-dev` autorisant la connexion directe par mot de passe ;
- secret de session de l'administration fixé par défaut (`OIDC_STATE_SECRET`).

Avant toute mise en ligne : Keycloak en mode production derrière HTTPS avec une vraie base, nouveaux mots de
passe, suppression des comptes de test et du client `transport-dev`, adresses de redirection du client
`transport` limitées à l'adresse réelle, `DEV_BYPASS_ROLES` laissée à `false` et un `OIDC_STATE_SECRET` propre.
