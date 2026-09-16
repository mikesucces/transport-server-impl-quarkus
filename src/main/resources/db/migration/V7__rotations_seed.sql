-- ============================================================
-- Jeu de données de démonstration — rotations (dev/test)
-- ============================================================

-- Rotation en cours : reprend l'état déjà seedé (M. Koné EN_ROTATION sur 4521 CI 01)
INSERT INTO rotations (driver_id, vehicle_id, status, scheduled_start, started_at, start_mileage_km)
SELECT d.id, v.id, 'EN_COURS', now() - interval '3 hours', now() - interval '3 hours', 142300
FROM drivers d, vehicles v
WHERE d.phone = '0708110001' AND v.plate_number = '4521 CI 01';

-- Rotation planifiée pour demain
INSERT INTO rotations (driver_id, vehicle_id, status, scheduled_start, scheduled_end)
SELECT d.id, v.id, 'PLANIFIEE', now() + interval '1 day', now() + interval '1 day' + interval '8 hours'
FROM drivers d, vehicles v
WHERE d.phone = '0544110002' AND v.plate_number = '7812 CI 02';

-- Rotation terminée (historique)
INSERT INTO rotations (driver_id, vehicle_id, status, scheduled_start, scheduled_end,
                       started_at, ended_at, start_mileage_km, end_mileage_km)
SELECT d.id, v.id, 'TERMINEE',
       now() - interval '2 days', now() - interval '2 days' + interval '8 hours',
       now() - interval '2 days', now() - interval '2 days' + interval '8 hours',
       201500, 201700
FROM drivers d, vehicles v
WHERE d.phone = '0192110003' AND v.plate_number = '3390 CI 01';
