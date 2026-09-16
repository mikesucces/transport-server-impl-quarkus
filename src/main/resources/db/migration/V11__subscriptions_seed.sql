-- ============================================================
-- Jeu de données de démonstration — abonnements/paiements (dev/test)
-- ============================================================

-- Abonnement mensuel actif pour Aya Kouassi (0708123456)
INSERT INTO subscriptions (passenger_id, plan, status, starts_on, ends_on)
SELECT p.id, 'MENSUEL', 'ACTIVE', CURRENT_DATE - 5, CURRENT_DATE + 25
FROM passengers p WHERE p.phone = '0708123456';

-- Paiement initial de cet abonnement
INSERT INTO payments (subscription_id, amount, method, paid_at)
SELECT s.id, 15000, 'ESPECES', now() - interval '5 days'
FROM subscriptions s
JOIN passengers p ON p.id = s.passenger_id
WHERE p.phone = '0708123456' AND s.plan = 'MENSUEL' AND s.status = 'ACTIVE';
