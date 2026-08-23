-- ============================================================
-- Jeu de données de démonstration — module identité (dev/test)
-- Complète V2 (passengers) et V4 (access_codes) avec :
--   - un code d'accès supplémentaire sur un 2e car
--   - l'historique de tentatives de connexion usagers
-- ============================================================

-- Code d'accès du jour "5190" pour le car 7812 CI 02
INSERT INTO access_codes (vehicle_id, vehicle_number, code_hash, valid_from, valid_until)
SELECT id, plate_number,
       encode(digest('5190','sha256'),'hex'),
       now() - interval '2 hours',
       now() + interval '10 hours'
FROM vehicles WHERE plate_number = '7812 CI 02';

-- Tentatives de connexion usagers (succès et échecs, avec et sans usager identifié)
INSERT INTO login_attempts (identifier, passenger_id, success, ip_address, attempted_at)
SELECT '4521 CI 01', id, true, '10.0.0.12', now() - interval '3 hours'
FROM passengers WHERE phone = '0708123456';

INSERT INTO login_attempts (identifier, passenger_id, success, ip_address, attempted_at)
SELECT '4521 CI 01', id, false, '10.0.0.12', now() - interval '3 hours 5 minutes'
FROM passengers WHERE phone = '0708123456';

INSERT INTO login_attempts (identifier, passenger_id, success, ip_address, attempted_at)
SELECT '7812 CI 02', id, true, '10.0.0.44', now() - interval '1 hour'
FROM passengers WHERE phone = '0544902100';

INSERT INTO login_attempts (identifier, passenger_id, success, ip_address, attempted_at)
SELECT '4521 CI 01', id, true, '10.0.0.61', now() - interval '20 minutes'
FROM passengers WHERE phone = '0192550800';

-- Tentative sans usager identifié (mauvais numero de car ou code inconnu)
INSERT INTO login_attempts (identifier, success, ip_address, attempted_at) VALUES
  ('9999 XX 99', false, '10.0.0.77', now() - interval '40 minutes');
