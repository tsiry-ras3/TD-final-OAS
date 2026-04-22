CREATE DATABASE agricultural_federation_db;

\c agricultural_federation_db;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS member_referees CASCADE;
DROP TABLE IF EXISTS collectivity_members CASCADE;
DROP TABLE IF EXISTS collectivity_structure CASCADE;
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
    occupation member_occupation NOT NULL,
    collectivity_id UUID,
    registration_fee_paid BOOLEAN NOT NULL DEFAULT FALSE,
    membership_dues_paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_members_collectivity
        FOREIGN KEY (collectivity_id)
        REFERENCES collectivities(id)
        ON DELETE SET NULL
);

CREATE TABLE collectivity_structure
(
    collectivity_id UUID PRIMARY KEY,
    president_id UUID NOT NULL,
    vice_president_id UUID NOT NULL,
    treasurer_id UUID NOT NULL,
    secretary_id UUID NOT NULL,

    CONSTRAINT fk_structure_collectivity
        FOREIGN KEY (collectivity_id)
        REFERENCES collectivities(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_structure_president
        FOREIGN KEY (president_id)
        REFERENCES members(id),

    CONSTRAINT fk_structure_vice_president
        FOREIGN KEY (vice_president_id)
        REFERENCES members(id),

    CONSTRAINT fk_structure_treasurer
        FOREIGN KEY (treasurer_id)
        REFERENCES members(id),

    CONSTRAINT fk_structure_secretary
        FOREIGN KEY (secretary_id)
        REFERENCES members(id)
);

CREATE TABLE collectivity_members
(
    collectivity_id UUID NOT NULL,
    member_id UUID NOT NULL,

    PRIMARY KEY (collectivity_id, member_id),

    CONSTRAINT fk_cm_collectivity
        FOREIGN KEY (collectivity_id)
        REFERENCES collectivities(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cm_member
        FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE CASCADE
);

CREATE TABLE member_referees
(
    member_id UUID NOT NULL,
    referee_id UUID NOT NULL,

    PRIMARY KEY (member_id, referee_id),

    CONSTRAINT fk_member_ref
        FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_referee
        FOREIGN KEY (referee_id)
        REFERENCES members(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_no_self_referee
        CHECK (member_id <> referee_id)
);

CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_members_phone ON members(phone_number);
CREATE INDEX idx_members_occupation ON members(occupation);
CREATE INDEX idx_members_collectivity ON members(collectivity_id);

CREATE INDEX idx_collectivities_location ON collectivities(location);
CREATE INDEX idx_collectivities_federation_approval ON collectivities(federation_approval);

CREATE INDEX idx_collectivity_members_collectivity ON collectivity_members(collectivity_id);
CREATE INDEX idx_collectivity_members_member ON collectivity_members(member_id);

CREATE INDEX idx_member_referees_member ON member_referees(member_id);
CREATE INDEX idx_member_referees_referee ON member_referees(referee_id);

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

INSERT INTO members (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, occupation, registration_fee_paid, membership_dues_paid)
VALUES
    (gen_random_uuid(), 'Jean', 'Dupont', '1980-01-01', 'MALE', '12 rue de Paris', 'Agriculteur', '0612345678', 'jean.dupont@example.com', 'PRESIDENT', true, true),
    (gen_random_uuid(), 'Marie', 'Martin', '1985-05-15', 'FEMALE', '24 rue de Lyon', 'Vétérinaire', '0698765432', 'marie.martin@example.com', 'VICE_PRESIDENT', true, true),
    (gen_random_uuid(), 'Pierre', 'Bernard', '1975-08-20', 'MALE', '8 rue de Marseille', 'Comptable', '0623456789', 'pierre.bernard@example.com', 'TREASURER', true, true),
    (gen_random_uuid(), 'Sophie', 'Petit', '1990-12-10', 'FEMALE', '15 rue de Bordeaux', 'Secrétaire', '0634567890', 'sophie.petit@example.com', 'SECRETARY', true, true),
    (gen_random_uuid(), 'Lucas', 'Robert', '1995-03-25', 'MALE', '32 rue de Nantes', 'Étudiant', '0645678901', 'lucas.robert@example.com', 'JUNIOR', true, true),
    (gen_random_uuid(), 'Julie', 'Dubois', '1988-07-18', 'FEMALE', '45 rue de Lille', 'Ingénieur', '0656789012', 'julie.dubois@example.com', 'SENIOR', true, true);

INSERT INTO collectivities (id, location, federation_approval)
VALUES (gen_random_uuid(), 'Paris', true);

DO $$
DECLARE
    v_collectivity_id UUID;
    v_president_id UUID;
    v_vice_president_id UUID;
    v_treasurer_id UUID;
    v_secretary_id UUID;
    v_member1_id UUID;
    v_member2_id UUID;
BEGIN
    SELECT id INTO v_president_id FROM members WHERE email = 'jean.dupont@example.com';
    SELECT id INTO v_vice_president_id FROM members WHERE email = 'marie.martin@example.com';
    SELECT id INTO v_treasurer_id FROM members WHERE email = 'pierre.bernard@example.com';
    SELECT id INTO v_secretary_id FROM members WHERE email = 'sophie.petit@example.com';
    SELECT id INTO v_member1_id FROM members WHERE email = 'lucas.robert@example.com';
    SELECT id INTO v_member2_id FROM members WHERE email = 'julie.dubois@example.com';
    SELECT id INTO v_collectivity_id FROM collectivities WHERE location = 'Paris';

    UPDATE members SET collectivity_id = v_collectivity_id
    WHERE id IN (v_president_id, v_vice_president_id, v_treasurer_id, v_secretary_id, v_member1_id, v_member2_id);

    INSERT INTO collectivity_structure (collectivity_id, president_id, vice_president_id, treasurer_id, secretary_id)
    VALUES (v_collectivity_id, v_president_id, v_vice_president_id, v_treasurer_id, v_secretary_id);

    INSERT INTO collectivity_members (collectivity_id, member_id)
    VALUES
        (v_collectivity_id, v_member1_id),
        (v_collectivity_id, v_member2_id);

    INSERT INTO member_referees (member_id, referee_id)
    VALUES
        (v_member1_id, v_president_id),
        (v_member2_id, v_vice_president_id);
END $$;


-- all enum values
CREATE TYPE frequency_type AS ENUM (
    'WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY'
);

CREATE TYPE activity_status AS ENUM (
    'ACTIVE', 'INACTIVE'
);

CREATE TYPE payment_mode AS ENUM (
    'CASH', 'MOBILE_BANKING', 'BANK_TRANSFER'
);

CREATE TYPE account_type AS ENUM (
    'CASH', 'MOBILE_BANKING', 'BANK'
);

CREATE TYPE mobile_banking_service AS ENUM (
    'AIRTEL_MONEY', 'MVOLA', 'ORANGE_MONEY'
);

CREATE TYPE bank_name AS ENUM (
    'BRED', 'MCB', 'BMOI', 'BOA', 'BGFI',
    'AFG', 'ACCES_BAQUE', 'BAOBAB', 'SIPEM'
);

-- Adding unique constraints to ensure collectivity names and numbers are unique across the federation
ALTER TABLE collectivities
    ADD COLUMN name VARCHAR(255) UNIQUE,
    ADD COLUMN number INT UNIQUE;

-- financial account
CREATE TABLE financial_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collectivity_id UUID NOT NULL,
    account_type account_type NOT NULL,
    amount NUMERIC(12,2) NOT NULL DEFAULT 0,

    -- Mobile banking
    holder_name VARCHAR(255),
    mobile_banking_service mobile_banking_service,
    mobile_number VARCHAR(20),

    -- Bank
    bank_name bank_name,
    bank_code INT,
    bank_branch_code INT,
    bank_account_number INT,
    bank_account_key INT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_fa_collectivity
        FOREIGN KEY (collectivity_id)
        REFERENCES collectivities(id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_financial_accounts_updated_at
BEFORE UPDATE ON financial_accounts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- membership fees : depends on collectivity and can be different for each collectivity, but also can be different for each member depending on their occupation and age. For simplicity, we will link it to collectivity and add a label to differentiate the fees (e.g. "Junior fee", "Senior fee", "Occupation fee"). We will also add eligibility criteria based on age and occupation, as well as a frequency to specify how often the fee should be paid.
CREATE TABLE membership_fees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collectivity_id UUID NOT NULL,
    label VARCHAR(255),
    eligible_from DATE NOT NULL,
    frequency frequency_type NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    status activity_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_mf_collectivity
        FOREIGN KEY (collectivity_id)
        REFERENCES collectivities(id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_membership_fees_updated_at
BEFORE UPDATE ON membership_fees
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- members payements
CREATE TABLE member_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL,
    membership_fee_id UUID NOT NULL,
    account_credited_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    payment_mode payment_mode NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_mp_member
        FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_mp_membership_fee
        FOREIGN KEY (membership_fee_id)
        REFERENCES membership_fees(id),

    CONSTRAINT fk_mp_account
        FOREIGN KEY (account_credited_id)
        REFERENCES financial_accounts(id)
);

-- collectivity transactions
CREATE TABLE collectivity_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collectivity_id UUID NOT NULL,
    member_debited_id UUID NOT NULL,
    account_credited_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    payment_mode payment_mode NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ct_collectivity
        FOREIGN KEY (collectivity_id)
        REFERENCES collectivities(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ct_member
        FOREIGN KEY (member_debited_id)
        REFERENCES members(id),

    CONSTRAINT fk_ct_account
        FOREIGN KEY (account_credited_id)
        REFERENCES financial_accounts(id)
);