-- ============================================================
-- Jeu de données de démonstration — moyens de paiement (dev/test)
-- ============================================================

-- Moyen de paiement par defaut pour Aya Kouassi (0708123456)
INSERT INTO payment_accounts (passenger_id, method, label, reference, is_default)
SELECT p.id, 'MOBILE_MONEY', 'Orange Money principal', '0708123456', true
FROM passengers p WHERE p.phone = '0708123456';
