-- ============================================================
-- M4 · Controle / Embarquement
--   generation des codes d'acces par rotation + presences validees
-- ============================================================

-- Un code peut desormais etre rattache a une rotation precise (en plus du
-- vehicule). Reste nullable : un code peut toujours etre cree sans rotation.
ALTER TABLE access_codes ADD COLUMN rotation_id UUID REFERENCES rotations(id) ON DELETE SET NULL;
CREATE INDEX idx_ac_rotation ON access_codes(rotation_id);

-- ---------- Presences validees a l'embarquement ----------
CREATE TABLE attendances (
    -- identifiant technique de la presence
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- rotation pendant laquelle l'usager a embarque
    rotation_id     UUID         NOT NULL REFERENCES rotations(id),
    -- usager dont la presence est validee
    passenger_id    UUID         NOT NULL REFERENCES passengers(id),
    -- code d'acces utilise pour valider l'embarquement
    access_code_id  UUID         REFERENCES access_codes(id) ON DELETE SET NULL,
    -- controller ayant effectue le controle (champ libre, non valide contre
    -- staff_profiles : aucune entite StaffProfile cote application pour l'instant)
    controller_id   UUID,
    -- date/heure de l'embarquement valide
    boarded_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_att_rotation  ON attendances(rotation_id);
CREATE INDEX idx_att_passenger ON attendances(passenger_id);
