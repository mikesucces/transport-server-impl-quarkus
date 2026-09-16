-- ============================================================
-- M3 · Rotations
--   affectation chauffeur + car pour une vacation (plage horaire)
-- ============================================================

-- ---------- Rotations ----------
CREATE TABLE rotations (
    -- identifiant technique de la rotation
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- chauffeur affecte a la rotation
    driver_id              UUID         NOT NULL REFERENCES drivers(id),
    -- vehicule affecte a la rotation
    vehicle_id             UUID         NOT NULL REFERENCES vehicles(id),
    -- statut courant de la rotation (PLANIFIEE / EN_COURS / TERMINEE / ANNULEE)
    status                 VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIEE',
    -- debut planifie de la vacation
    scheduled_start        TIMESTAMPTZ  NOT NULL,
    -- fin planifiee de la vacation
    scheduled_end          TIMESTAMPTZ,
    -- date/heure a laquelle la rotation a reellement demarre
    started_at             TIMESTAMPTZ,
    -- date/heure a laquelle la rotation s'est reellement terminee
    ended_at               TIMESTAMPTZ,
    -- kilometrage du vehicule au debut de la rotation
    start_mileage_km       INTEGER,
    -- kilometrage du vehicule a la fin de la rotation
    end_mileage_km         INTEGER,
    -- reference libre reservee a M4 (generation des codes d'acces usagers par rotation)
    access_code_reference  VARCHAR(64),
    -- remarques libres sur la rotation
    notes                  TEXT,
    -- date de creation de l'enregistrement
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de derniere mise a jour de l'enregistrement
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_rot_status  CHECK (status IN ('PLANIFIEE','EN_COURS','TERMINEE','ANNULEE')),
    CONSTRAINT chk_rot_dates   CHECK (scheduled_end IS NULL OR scheduled_end > scheduled_start),
    CONSTRAINT chk_rot_mileage CHECK (
        end_mileage_km IS NULL OR start_mileage_km IS NULL OR end_mileage_km >= start_mileage_km
    )
);
CREATE INDEX idx_rot_driver  ON rotations(driver_id);
CREATE INDEX idx_rot_vehicle ON rotations(vehicle_id);
CREATE INDEX idx_rot_status  ON rotations(status);
