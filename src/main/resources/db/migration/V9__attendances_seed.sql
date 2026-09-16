-- ============================================================
-- Jeu de données de démonstration — contrôle/embarquement (dev/test)
-- ============================================================

-- Presence validee sur la rotation en cours (M. Koné / 4521 CI 01),
-- pour l'usager Aya Kouassi, avec le code du jour deja seede sur ce car.
INSERT INTO attendances (rotation_id, passenger_id, access_code_id, boarded_at)
SELECT r.id, p.id, ac.id, now() - interval '2 hours'
FROM rotations r
JOIN vehicles v ON v.id = r.vehicle_id
JOIN passengers p ON p.phone = '0708123456'
JOIN access_codes ac ON ac.vehicle_id = v.id
WHERE v.plate_number = '4521 CI 01' AND r.status = 'EN_COURS'
LIMIT 1;
