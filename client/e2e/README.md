# Test general de l'administration

Script de bout en bout (Python 3, aucune dependance) : il se connecte via le formulaire
Keycloak puis rejoue les requetes JSF/ajax des boutons de l'interface (creation,
modification, changement de statut, suppression) sur tous les ecrans.

## Prerequis
- Base, API (`OIDC_ENABLED=true`) et client d'administration demarres (client sur :8081).
- Utilisateur `admin` (role OWNER) dans le realm `transit`.
- Donnees de demonstration presentes (au moins un usager actif, un car et un chauffeur DISPONIBLE, une ligne).

## Lancement
```bash
python e2e/e2e.py e2e/scenario_complet.py
```
Variables optionnelles : `ADMIN_URL`, `ADMIN_USER`, `ADMIN_PASSWORD`.

## Limites
- Verifie le fonctionnement, pas l'apparence (CSS, dialogues).
- Un abonnement annule conserve ses paiements : l'API refuse de le supprimer (400, message clair).
  Le test cree donc un abonnement qui reste en base a chaque execution. Nettoyage :
  ```bash
  docker exec transport-server-impl-quarkus-db-1 psql -U transit -d transit -c "delete from payments where subscription_id in (select id from subscriptions where status='CANCELLED'); delete from subscriptions where status='CANCELLED';"
  ```
