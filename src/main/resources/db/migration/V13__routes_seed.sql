-- ============================================================
-- Jeu de données de démonstration — lignes/horaires (dev/test)
-- ============================================================

INSERT INTO routes (code, name, origin, destination, distance_km, status) VALUES
  ('L01', 'Adjame - Yopougon', 'Adjame', 'Yopougon', 12, 'ACTIVE'),
  ('L02', 'Cocody - Marcory',  'Cocody', 'Marcory',   9, 'ACTIVE');

INSERT INTO route_schedules (route_id, day_of_week, departure_time)
SELECT id, 'LUNDI', TIME '06:00' FROM routes WHERE code = 'L01'
UNION ALL
SELECT id, 'LUNDI', TIME '14:00' FROM routes WHERE code = 'L01'
UNION ALL
SELECT id, 'MERCREDI', TIME '06:00' FROM routes WHERE code = 'L01'
UNION ALL
SELECT id, 'MARDI', TIME '07:00' FROM routes WHERE code = 'L02';

-- Rattache la rotation deja EN_COURS (M. Koné / 4521 CI 01) a la ligne L01
UPDATE rotations SET route_id = (SELECT id FROM routes WHERE code = 'L01')
WHERE id IN (
    SELECT r.id FROM rotations r
    JOIN vehicles v ON v.id = r.vehicle_id
    WHERE v.plate_number = '4521 CI 01' AND r.status = 'EN_COURS'
);
