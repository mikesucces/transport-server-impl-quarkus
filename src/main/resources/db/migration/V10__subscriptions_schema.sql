-- ============================================================
-- M5 · Abonnements / Paiements
--   un usager doit avoir un abonnement actif pour embarquer ;
--   un paiement cree ou renouvelle un abonnement
-- ============================================================

-- ---------- Abonnements ----------
CREATE TABLE subscriptions (
    -- identifiant technique de l'abonnement
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- usager titulaire de l'abonnement
    passenger_id UUID        NOT NULL REFERENCES passengers(id),
    -- formule (HEBDOMADAIRE / MENSUEL / TRIMESTRIEL)
    plan        VARCHAR(20)  NOT NULL,
    -- statut courant (ACTIVE / EXPIRED / CANCELLED)
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    -- debut de la periode couverte
    starts_on   DATE         NOT NULL,
    -- fin de la periode couverte
    ends_on     DATE         NOT NULL,
    -- date de creation de l'enregistrement
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de derniere mise a jour de l'enregistrement
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_sub_plan   CHECK (plan IN ('HEBDOMADAIRE','MENSUEL','TRIMESTRIEL')),
    CONSTRAINT chk_sub_status CHECK (status IN ('ACTIVE','EXPIRED','CANCELLED')),
    CONSTRAINT chk_sub_dates  CHECK (ends_on >= starts_on)
);
CREATE INDEX idx_sub_passenger ON subscriptions(passenger_id);
CREATE INDEX idx_sub_status    ON subscriptions(status);

-- ---------- Paiements ----------
CREATE TABLE payments (
    -- identifiant technique du paiement
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- abonnement finance par ce paiement
    subscription_id UUID         NOT NULL REFERENCES subscriptions(id),
    -- montant encaisse (FCFA)
    amount          NUMERIC(12,2) NOT NULL,
    -- mode de paiement (ESPECES / MOBILE_MONEY / VIREMENT / AUTRE)
    method          VARCHAR(20)  NOT NULL,
    -- staff ayant encaisse le paiement (champ libre, non valide contre
    -- staff_profiles : aucune entite StaffProfile cote application)
    collected_by    UUID,
    -- date/heure de l'encaissement
    paid_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_pay_amount CHECK (amount > 0),
    CONSTRAINT chk_pay_method CHECK (method IN ('ESPECES','MOBILE_MONEY','VIREMENT','AUTRE'))
);
CREATE INDEX idx_pay_subscription ON payments(subscription_id);
