-- ============================================================
-- M9 · Moyens de paiement enregistres
--   un usager peut enregistrer plusieurs moyens de paiement reutilisables
-- ============================================================

CREATE TABLE payment_accounts (
    -- identifiant technique du moyen de paiement
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- usager titulaire
    passenger_id UUID         NOT NULL REFERENCES passengers(id),
    -- mode de paiement (ESPECES / MOBILE_MONEY / VIREMENT / AUTRE)
    method       VARCHAR(20)  NOT NULL,
    -- libelle libre (ex. "Orange Money principal")
    label        VARCHAR(100),
    -- reference du moyen (numero mobile money, compte...)
    reference    VARCHAR(100) NOT NULL,
    -- moyen par defaut de l'usager (un seul actif a la fois)
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    -- moyen actif (desactivable sans suppression)
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    -- date de creation de l'enregistrement
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de derniere mise a jour de l'enregistrement
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_pa_method CHECK (method IN ('ESPECES','MOBILE_MONEY','VIREMENT','AUTRE'))
);
CREATE INDEX idx_pa_passenger ON payment_accounts(passenger_id);

-- Lien optionnel vers le moyen de paiement utilise pour un paiement d'abonnement.
ALTER TABLE payments ADD COLUMN payment_account_id UUID REFERENCES payment_accounts(id) ON DELETE SET NULL;
CREATE INDEX idx_pay_account ON payments(payment_account_id);
