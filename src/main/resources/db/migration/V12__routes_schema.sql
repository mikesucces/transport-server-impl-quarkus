-- ============================================================
-- M6 · Lignes / Trajets fixes
--   ligne (point A -> point B), horaires recurrents, lien optionnel
--   depuis les rotations
-- ============================================================

-- ---------- Lignes ----------
CREATE TABLE routes (
    -- identifiant technique de la ligne
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- code court unique de la ligne (ex. L01)
    code         VARCHAR(20)  NOT NULL UNIQUE,
    -- nom de la ligne
    name         VARCHAR(150) NOT NULL,
    -- point de depart
    origin       VARCHAR(150) NOT NULL,
    -- point d'arrivee
    destination  VARCHAR(150) NOT NULL,
    -- distance indicative (km)
    distance_km  INTEGER,
    -- statut de la ligne (ACTIVE / SUSPENDUE)
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    -- date de creation de l'enregistrement
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- date de derniere mise a jour de l'enregistrement
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_rte_status   CHECK (status IN ('ACTIVE','SUSPENDUE')),
    CONSTRAINT chk_rte_distance CHECK (distance_km IS NULL OR distance_km >= 0)
);
CREATE INDEX idx_rte_status ON routes(status);

-- ---------- Horaires recurrents ----------
CREATE TABLE route_schedules (
    -- identifiant technique de l'horaire
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ligne concernee
    route_id        UUID        NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    -- jour de la semaine concerne
    day_of_week     VARCHAR(10) NOT NULL,
    -- heure de depart recurrente
    departure_time  TIME        NOT NULL,
    -- date de creation de l'enregistrement
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_rts_day CHECK (day_of_week IN
        ('LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI','DIMANCHE'))
);
CREATE INDEX idx_rts_route ON route_schedules(route_id);

-- ---------- Lien optionnel depuis les rotations ----------
ALTER TABLE rotations ADD COLUMN route_id UUID REFERENCES routes(id) ON DELETE SET NULL;
CREATE INDEX idx_rot_route ON rotations(route_id);
