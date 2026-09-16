-- ============================================================
-- M10 · Location de vehicules
--   location d'un vehicule a un usager, independante du systeme
--   abonnement/rotation
-- ============================================================

-- ---------- Locations ----------
CREATE TABLE rentals (
    -- identifiant technique de la location
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- usager locataire
    passenger_id        UUID         NOT NULL REFERENCES passengers(id),
    -- vehicule loue
    vehicle_id          UUID         NOT NULL REFERENCES vehicles(id),
    -- statut de la location (RESERVEE / EN_COURS / TERMINEE / ANNULEE)
    status              VARCHAR(20)  NOT NULL DEFAULT 'RESERVEE',
    -- date de debut prevue
    start_date          DATE         NOT NULL,
    -- date de fin prevue
    end_date            DATE         NOT NULL,
    -- date de restitution reelle du vehicule
    actual_return_date  DATE,
    -- kilometrage du vehicule au debut de la location
    start_mileage_km    INTEGER,
    -- kilometrage du vehicule a la fin de la location
    end_mileage_km      INTEGER,
    -- montant total du a la location, saisi manuellement (FCFA)
    total_amount        NUMERIC(12,2) NOT NULL,
    -- caution demandee (FCFA)
    deposit_amount      NUMERIC(12,2),
    -- remarques libres
    notes               TEXT,
    -- date de creation de l'enregistrement
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de derniere mise a jour de l'enregistrement
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_rnt_status   CHECK (status IN ('RESERVEE','EN_COURS','TERMINEE','ANNULEE')),
    CONSTRAINT chk_rnt_dates    CHECK (end_date >= start_date),
    CONSTRAINT chk_rnt_amount   CHECK (total_amount >= 0),
    CONSTRAINT chk_rnt_deposit  CHECK (deposit_amount IS NULL OR deposit_amount >= 0)
);
CREATE INDEX idx_rnt_passenger ON rentals(passenger_id);
CREATE INDEX idx_rnt_vehicle   ON rentals(vehicle_id);
CREATE INDEX idx_rnt_status    ON rentals(status);

-- ---------- Paiements de location ----------
CREATE TABLE rental_payments (
    -- identifiant technique du paiement
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- location financee
    rental_id          UUID         NOT NULL REFERENCES rentals(id),
    -- moyen de paiement enregistre utilise, optionnel (M9)
    payment_account_id UUID         REFERENCES payment_accounts(id) ON DELETE SET NULL,
    -- montant encaisse (FCFA)
    amount             NUMERIC(12,2) NOT NULL,
    -- mode de paiement (ESPECES / MOBILE_MONEY / VIREMENT / AUTRE)
    method             VARCHAR(20)  NOT NULL,
    -- staff ayant encaisse le paiement (champ libre, non valide contre staff_profiles)
    collected_by       UUID,
    -- date/heure de l'encaissement
    paid_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_rntpay_amount CHECK (amount > 0),
    CONSTRAINT chk_rntpay_method CHECK (method IN ('ESPECES','MOBILE_MONEY','VIREMENT','AUTRE'))
);
CREATE INDEX idx_rntpay_rental ON rental_payments(rental_id);
