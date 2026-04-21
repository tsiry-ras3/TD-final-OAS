CREATE DATABASE agricultural_federation_db;

\c agricultural_federation_db;

CREATE EXTENSION IF NOT EXISTS pgcrypto;


DROP TABLE IF EXISTS member_referees CASCADE;
DROP TABLE IF EXISTS members CASCADE;
DROP TABLE IF EXISTS collectivities CASCADE;

DROP TYPE IF EXISTS gender CASCADE;
DROP TYPE IF EXISTS member_occupation CASCADE;


CREATE TYPE gender AS ENUM (
    'MALE',
    'FEMALE'
    );

CREATE TYPE member_occupation AS ENUM (
    'JUNIOR',
    'SENIOR',
    'SECRETARY',
    'TREASURER',
    'VICE_PRESIDENT',
    'PRESIDENT'
    );


CREATE TABLE collectivities
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    location VARCHAR(255) NOT NULL,
    federation_approval BOOLEAN NOT NULL DEFAULT FALSE,

    president_id UUID,
    vice_president_id UUID,
    treasurer_id UUID,
    secretary_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE TABLE members
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender gender NOT NULL,

    address VARCHAR(255),
    profession VARCHAR(150),
    phone_number VARCHAR(20),

    email VARCHAR(255) UNIQUE NOT NULL,

    occupation member_occupation NOT NULL DEFAULT 'JUNIOR',

    collectivity_id UUID,

    registration_fee_paid BOOLEAN NOT NULL DEFAULT FALSE,
    membership_dues_paid BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


ALTER TABLE members
    ADD CONSTRAINT fk_member_collectivity
        FOREIGN KEY (collectivity_id)
            REFERENCES collectivities(id)
            ON DELETE SET NULL;


ALTER TABLE collectivities
    ADD CONSTRAINT fk_president
        FOREIGN KEY (president_id)
            REFERENCES members(id)
            ON DELETE SET NULL;

ALTER TABLE collectivities
    ADD CONSTRAINT fk_vice_president
        FOREIGN KEY (vice_president_id)
            REFERENCES members(id)
            ON DELETE SET NULL;

ALTER TABLE collectivities
    ADD CONSTRAINT fk_treasurer
        FOREIGN KEY (treasurer_id)
            REFERENCES members(id)
            ON DELETE SET NULL;

ALTER TABLE collectivities
    ADD CONSTRAINT fk_secretary
        FOREIGN KEY (secretary_id)
            REFERENCES members(id)
            ON DELETE SET NULL;


CREATE TABLE member_referees
(
    member_id UUID NOT NULL,
    referee_id UUID NOT NULL,

    PRIMARY KEY(member_id, referee_id),

    CONSTRAINT fk_member_ref
        FOREIGN KEY(member_id)
            REFERENCES members(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_referee
        FOREIGN KEY(referee_id)
            REFERENCES members(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_no_self_referee
        CHECK (member_id <> referee_id)
);


CREATE INDEX idx_members_collectivity ON members(collectivity_id);
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_collectivities_location ON collectivities(location);


CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_members_updated_at
    BEFORE UPDATE ON members
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_collectivities_updated_at
    BEFORE UPDATE ON collectivities
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();