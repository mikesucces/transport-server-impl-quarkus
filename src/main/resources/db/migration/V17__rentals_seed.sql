-- ============================================================
-- Jeu de données de démonstration — location de véhicules (dev/test)
-- ============================================================

-- Location reservee pour Fatou Diallo (0192550800) sur 7812 CI 02
INSERT INTO rentals (passenger_id, vehicle_id, status, start_date, end_date, total_amount, deposit_amount)
SELECT p.id, v.id, 'RESERVEE', CURRENT_DATE + 2, CURRENT_DATE + 5, 60000, 20000
FROM passengers p, vehicles v
WHERE p.phone = '0192550800' AND v.plate_number = '7812 CI 02';

-- Acompte deja verse pour cette location
INSERT INTO rental_payments (rental_id, amount, method, paid_at)
SELECT r.id, 20000, 'ESPECES', now() - interval '1 hour'
FROM rentals r
JOIN passengers p ON p.id = r.passenger_id
WHERE p.phone = '0192550800' AND r.status = 'RESERVEE';
