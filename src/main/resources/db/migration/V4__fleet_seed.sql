-- ============================================================
-- Jeu de données de démonstration — flotte (dev/test)
-- ============================================================

INSERT INTO vehicles (plate_number, brand, model, year, capacity, mileage_km, status, commissioned_at) VALUES
  ('4521 CI 01','Toyota','Coaster',        2019, 33, 142300, 'EN_SERVICE',  DATE '2019-03-01'),
  ('7812 CI 02','Mercedes','Sprinter',     2021, 22,  88500, 'DISPONIBLE',   DATE '2021-07-01'),
  ('3390 CI 01','Hyundai','County',        2017, 29, 201700, 'ENTRETIEN', DATE '2017-01-01');

-- Documents
INSERT INTO vehicle_documents (vehicle_id, doc_type, reference, issuer, issued_on, expires_on)
SELECT id, 'ASSURANCE', 'NSIA-2026-4521', 'NSIA Assurances',
       CURRENT_DATE - 359, CURRENT_DATE + 6
FROM vehicles WHERE plate_number = '4521 CI 01';

INSERT INTO vehicle_documents (vehicle_id, doc_type, reference, issuer, issued_on, expires_on)
SELECT id, 'VISITE_TECHNIQUE', 'VT-2025-8871', 'SICTA',
       CURRENT_DATE - 100, CURRENT_DATE + 270
FROM vehicles WHERE plate_number = '4521 CI 01';

INSERT INTO vehicle_documents (vehicle_id, doc_type, reference, issuer, issued_on, expires_on)
SELECT id, 'ASSURANCE', 'NSIA-2026-7812', 'NSIA Assurances',
       CURRENT_DATE - 120, CURRENT_DATE + 240
FROM vehicles WHERE plate_number = '7812 CI 02';

INSERT INTO vehicle_documents (vehicle_id, doc_type, reference, issuer, issued_on, expires_on)
SELECT id, 'VISITE_TECHNIQUE', 'VT-2025-3390', 'SICTA',
       CURRENT_DATE - 345, CURRENT_DATE + 20
FROM vehicles WHERE plate_number = '3390 CI 01';

-- Entretiens
INSERT INTO maintenances (vehicle_id, maintenance_type, status, performed_on, mileage_km, next_due_km, cost_amount, garage, description)
SELECT id, 'VIDANGE', 'TERMINE', CURRENT_DATE - 45, 128000, 152000, 35000, 'Garage Adjamé', 'Vidange + filtres'
FROM vehicles WHERE plate_number = '4521 CI 01';

INSERT INTO maintenances (vehicle_id, maintenance_type, status, scheduled_on, mileage_km, cost_amount, garage, description)
SELECT id, 'VIDANGE', 'PLANIFIE', CURRENT_DATE + 2, 201700, 45000, 'Garage Adjamé', 'Vidange prévue'
FROM vehicles WHERE plate_number = '3390 CI 01';

-- Chauffeurs (pas encore de compte Keycloak : staff_profile_id NULL)
INSERT INTO drivers (full_name, phone, matricule, license_number, license_category,
                     license_expires_on, remuneration_type, monthly_salary, status, hired_on) VALUES
  ('M. Koné',      '0708110001','DRV-001','CI-D-884512','D', CURRENT_DATE + 12,  'FIXE', 150000, 'EN_ROTATION',   DATE '2023-02-01'),
  ('A. Bakayoko',  '0544110002','DRV-002','CI-D-773901','D', CURRENT_DATE + 210, 'FIXE', 150000, 'DISPONIBLE', DATE '2022-06-15'),
  ('S. Ouattara',  '0192110003','DRV-003','CI-D-651277','D', CURRENT_DATE + 400, 'FIXE', 140000, 'DISPONIBLE', DATE '2024-01-10'),
  ('Y. Dosso',     '0733110004','DRV-004','CI-D-559834','D', CURRENT_DATE + 320, 'FIXE', 140000, 'DISPONIBLE', DATE '2024-09-01');

-- Code d'accès du jour "8342" pour le car 4521 CI 01
INSERT INTO access_codes (vehicle_id, vehicle_number, code_hash, valid_from, valid_until)
SELECT id, plate_number,
       encode(digest('8342','sha256'),'hex'),
       now() - interval '1 hour',
       now() + interval '12 hours'
FROM vehicles WHERE plate_number = '4521 CI 01';
