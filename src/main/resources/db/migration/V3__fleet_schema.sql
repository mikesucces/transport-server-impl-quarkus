-- ============================================================
-- M2 · Référentiel flotte
--   vehicles, vehicle_documents, maintenances, drivers
-- ============================================================

-- ---------- Cars ----------
CREATE TABLE vehicles (
    -- identifiant technique du véhicule
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- numéro d'immatriculation (ex. 4521 CI 01)
    plate_number     VARCHAR(30)  NOT NULL UNIQUE,
    -- marque du véhicule
    brand            VARCHAR(60)  NOT NULL,
    -- modèle du véhicule
    model            VARCHAR(60)  NOT NULL,
    -- année de mise en circulation
    year             SMALLINT,
    -- nombre de places assises
    capacity         SMALLINT     NOT NULL,
    -- kilométrage actuel du véhicule
    mileage_km       INTEGER      NOT NULL DEFAULT 0,
    -- état courant du véhicule (DISPONIBLE / EN_SERVICE / ENTRETIEN / HORS_SERVICE)
    status           VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    -- date de mise en service dans la flotte
    commissioned_at  DATE,
    -- remarques libres sur le véhicule
    notes            TEXT,
    -- date de création de l'enregistrement
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de dernière mise à jour de l'enregistrement
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_veh_status   CHECK (status IN ('DISPONIBLE','EN_SERVICE','ENTRETIEN','HORS_SERVICE')),
    CONSTRAINT chk_veh_capacity CHECK (capacity > 0 AND capacity <= 120),
    CONSTRAINT chk_veh_mileage  CHECK (mileage_km >= 0)
);
CREATE INDEX idx_veh_status ON vehicles(status);
CREATE INDEX idx_veh_plate  ON vehicles(plate_number);

-- La FK laissée ouverte par M1 peut maintenant être posée.
ALTER TABLE access_codes
    ADD CONSTRAINT fk_ac_vehicle FOREIGN KEY (vehicle_id)
    REFERENCES vehicles(id) ON DELETE CASCADE;

-- ---------- Documents du car ----------
CREATE TABLE vehicle_documents (
    -- identifiant technique du document
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- véhicule auquel le document est rattaché
    vehicle_id    UUID         NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    -- type de document (ASSURANCE / VISITE_TECHNIQUE / CARTE_GRISE / LICENCE_TRANSPORT / AUTRE)
    doc_type      VARCHAR(30)  NOT NULL,
    -- numéro/référence du document
    reference     VARCHAR(80),
    -- organisme émetteur du document
    issuer        VARCHAR(120),
    -- date d'émission du document
    issued_on     DATE,
    -- date d'expiration du document
    expires_on    DATE         NOT NULL,
    -- lien vers le fichier numérisé du document
    file_url      VARCHAR(500),
    -- date de création de l'enregistrement
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de dernière mise à jour de l'enregistrement
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_doc_type  CHECK (doc_type IN ('ASSURANCE','VISITE_TECHNIQUE','CARTE_GRISE','LICENCE_TRANSPORT','AUTRE')),
    CONSTRAINT chk_doc_dates CHECK (issued_on IS NULL OR expires_on >= issued_on)
);
CREATE INDEX idx_vd_vehicle ON vehicle_documents(vehicle_id);
CREATE INDEX idx_vd_expiry  ON vehicle_documents(expires_on);

-- ---------- Entretiens ----------
CREATE TABLE maintenances (
    -- identifiant technique de l'entretien
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- véhicule concerné par l'entretien
    vehicle_id        UUID         NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    -- type d'entretien (VIDANGE / REPARATION / CONTROLE / PNEUMATIQUES / AUTRE)
    maintenance_type  VARCHAR(30)  NOT NULL,
    -- statut de l'entretien (PLANIFIE / EN_COURS / TERMINE / ANNULE)
    status            VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIE',
    -- date planifiée de l'entretien
    scheduled_on      DATE,
    -- date à laquelle l'entretien a été réalisé
    performed_on      DATE,
    -- kilométrage du véhicule au moment de l'entretien
    mileage_km        INTEGER,
    -- kilométrage prévu pour le prochain entretien
    next_due_km       INTEGER,
    -- date prévue pour le prochain entretien
    next_due_on       DATE,
    -- coût de l'entretien (FCFA)
    cost_amount       NUMERIC(12,2),
    -- garage ayant réalisé l'entretien
    garage            VARCHAR(150),
    -- description libre de l'entretien
    description       TEXT,
    -- date de création de l'enregistrement
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de dernière mise à jour de l'enregistrement
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_mnt_type   CHECK (maintenance_type IN ('VIDANGE','REPARATION','CONTROLE','PNEUMATIQUES','AUTRE')),
    CONSTRAINT chk_mnt_status CHECK (status IN ('PLANIFIE','EN_COURS','TERMINE','ANNULE')),
    CONSTRAINT chk_mnt_cost   CHECK (cost_amount IS NULL OR cost_amount >= 0)
);
CREATE INDEX idx_mnt_vehicle   ON maintenances(vehicle_id);
CREATE INDEX idx_mnt_scheduled ON maintenances(scheduled_on) WHERE status IN ('PLANIFIE','IN_PROGRESS');

-- ---------- Chauffeurs ----------
-- staff_profile_id est NULLABLE : un chauffeur peut exister en base avant
-- d'avoir un compte Keycloak (branché plus tard). Le lien est unique.
CREATE TABLE drivers (
    -- identifiant technique du chauffeur
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- compte Keycloak associé (nullable tant que non branché)
    staff_profile_id   UUID UNIQUE REFERENCES staff_profiles(id) ON DELETE SET NULL,
    -- nom complet du chauffeur
    full_name          VARCHAR(150) NOT NULL,
    -- numéro de téléphone du chauffeur
    phone              VARCHAR(30)  NOT NULL UNIQUE,
    -- matricule interne du chauffeur
    matricule          VARCHAR(30)  UNIQUE,
    -- numéro du permis de conduire
    license_number     VARCHAR(50)  NOT NULL,
    -- catégorie du permis de conduire
    license_category   VARCHAR(10)  NOT NULL DEFAULT 'D',
    -- date d'expiration du permis de conduire
    license_expires_on DATE         NOT NULL,
    -- mode de rémunération (FIXE / PAR_TRAJET / COMMISSION)
    remuneration_type  VARCHAR(20)  NOT NULL DEFAULT 'FIXE',
    -- salaire mensuel en FCFA, si rémunération FIXE
    monthly_salary     NUMERIC(12,2),
    -- montant par trajet en FCFA, si rémunération PAR_TRAJET
    trip_rate          NUMERIC(12,2),
    -- taux de commission en %, si rémunération COMMISSION
    commission_rate    NUMERIC(5,2),
    -- statut courant du chauffeur (DISPONIBLE / EN_ROTATION / REPOS / SUSPENDU)
    status             VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    -- date d'embauche du chauffeur
    hired_on           DATE,
    -- date de création de l'enregistrement
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de dernière mise à jour de l'enregistrement
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_drv_status CHECK (status IN ('DISPONIBLE','EN_ROTATION','REPOS','SUSPENDU')),
    CONSTRAINT chk_drv_remun  CHECK (remuneration_type IN ('FIXE','PAR_TRAJET','COMMISSION')),
    -- cohérence : le montant correspondant au mode de rémunération est requis
    CONSTRAINT chk_drv_amount CHECK (
        (remuneration_type = 'FIXE'      AND monthly_salary  IS NOT NULL) OR
        (remuneration_type = 'PAR_TRAJET'   AND trip_rate       IS NOT NULL) OR
        (remuneration_type = 'COMMISSION' AND commission_rate IS NOT NULL)
    )
);
CREATE INDEX idx_drv_status  ON drivers(status);
CREATE INDEX idx_drv_licexp  ON drivers(license_expires_on);
