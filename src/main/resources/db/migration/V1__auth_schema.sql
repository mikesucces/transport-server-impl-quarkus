-- ============================================================
-- M1 · Auth (Keycloak) & profils locaux
--
-- Keycloak est la SOURCE DE VÉRITÉ de l'identité du staff :
-- comptes, mots de passe, rôles, MFA, SSO y sont gérés.
--
-- Cette base ne stocke donc AUCUN credential ni rôle du staff.
-- Elle garde seulement :
--   1. staff_profiles : données MÉTIER du personnel (matricule, salaire)
--      + ancrage des clés étrangères pour les modules suivants
--      (rotations.driver_id, attendances.controller_id, payments.collected_by),
--      qui ne peuvent pas référencer Keycloak.
--   2. passengers : les usagers, qui ne passent PAS par Keycloak
--      (accès simplifié n° de car + code du jour).
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ---------- Profils du personnel (miroir métier de Keycloak) ----------
CREATE TABLE staff_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_sub    UUID         NOT NULL UNIQUE,   -- claim "sub" du token
    username        VARCHAR(100) NOT NULL,          -- copie pour affichage/reporting
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(30),
    email           VARCHAR(150),
    staff_type      VARCHAR(20)  NOT NULL,          -- OWNER|MANAGER|CONTROLLER|DRIVER
    matricule       VARCHAR(30)  UNIQUE,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    first_seen_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_login_at   TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_staff_type CHECK (staff_type IN ('OWNER','MANAGER','CONTROLLER','DRIVER'))
);
CREATE INDEX idx_sp_sub    ON staff_profiles(keycloak_sub);
CREATE INDEX idx_sp_type   ON staff_profiles(staff_type) WHERE active;

-- ---------- Usagers (hors Keycloak) ----------
-- M5 enrichira cette table avec les abonnements.
CREATE TABLE passengers (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(150) NOT NULL,
    phone         VARCHAR(30)  NOT NULL UNIQUE,     -- identifiant de l'usager
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_pax_status CHECK (status IN ('ACTIVE','SUSPENDED','DISABLED'))
);
CREATE INDEX idx_pax_phone ON passengers(phone);

-- ---------- Codes d'accès temporaires (usagers) ----------
-- Le code est lié au CAR, pas à l'usager : changer de car = nouveau code
-- diffusé, sans modifier les comptes. Généré par M4, vérifié ici.
CREATE TABLE access_codes (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id     UUID         NOT NULL,           -- FK vers vehicles ajoutée par M2
    vehicle_number VARCHAR(30)  NOT NULL,
    code_hash      VARCHAR(255) NOT NULL,
    valid_from     TIMESTAMPTZ  NOT NULL,
    valid_until    TIMESTAMPTZ  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_ac_vehicle ON access_codes(vehicle_number, valid_until);

-- ---------- Traçabilité des connexions usagers ----------
-- (les connexions staff sont tracées par Keycloak)
CREATE TABLE login_attempts (
    id           BIGSERIAL PRIMARY KEY,
    identifier   VARCHAR(150) NOT NULL,
    passenger_id UUID REFERENCES passengers(id) ON DELETE SET NULL,
    success      BOOLEAN      NOT NULL,
    ip_address   VARCHAR(45),
    attempted_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_la_ident ON login_attempts(identifier, attempted_at DESC);
