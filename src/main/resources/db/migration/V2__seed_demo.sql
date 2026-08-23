-- ============================================================
-- Jeu de données de démonstration (dev/test uniquement)
-- Le staff est créé dans Keycloak (voir keycloak/realm-transit.json).
-- Ici : quelques usagers et un code d'accès du jour.
-- ============================================================

INSERT INTO passengers (full_name, phone) VALUES
  ('Aya Kouassi',    '0708123456'),
  ('Ibrahim Bamba',  '0544902100'),
  ('Fatou Diallo',   '0192550800');

-- Le code d'accès de démonstration est créé dans V4, une fois les cars
-- insérés : access_codes.vehicle_id référence vehicles(id) à partir de M2.
