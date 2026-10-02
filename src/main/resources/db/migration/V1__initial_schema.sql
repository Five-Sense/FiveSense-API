CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'VIEWER')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'FIRST_ACCESS')),
    auth_version INTEGER NOT NULL DEFAULT 0,
    credential_email_sent_at TIMESTAMPTZ,
    credential_email_window_started_at TIMESTAMPTZ,
    credential_email_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255),
    session_type VARCHAR(20) NOT NULL CHECK (session_type IN ('ACCESS_ONLY', 'REFRESHABLE', 'PASSWORD_CHANGE')),
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT uq_auth_session_refresh_hash UNIQUE (refresh_token_hash)
);
CREATE INDEX ix_auth_session_user_active ON auth_session(user_id, expires_at) WHERE revoked_at IS NULL;

CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE bootstrap_state (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    consumed BOOLEAN NOT NULL DEFAULT FALSE
);
INSERT INTO bootstrap_state(id, consumed) VALUES (1, FALSE);

CREATE TABLE team (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(255),
    representatives VARCHAR(255) NOT NULL DEFAULT '',
    schedule VARCHAR(255) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_DOING_5S' CHECK (status IN ('DOING_5S', 'NOT_DOING_5S')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_team_name_or_code CHECK (name IS NOT NULL OR code IS NOT NULL)
);
CREATE UNIQUE INDEX uq_team_name_ci ON team(LOWER(name)) WHERE name IS NOT NULL;
CREATE UNIQUE INDEX uq_team_code_ci ON team(LOWER(code)) WHERE code IS NOT NULL;

CREATE TABLE problem (
    id UUID PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    related_email VARCHAR(255) NOT NULL,
    default_response VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE material (
    id UUID PRIMARY KEY,
    name VARCHAR(55) NOT NULL UNIQUE,
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity BETWEEN 0 AND 999),
    minimum_stock INTEGER NOT NULL CHECK (minimum_stock BETWEEN 0 AND 999),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE occurrence (
    id UUID PRIMARY KEY,
    problem_id UUID NOT NULL REFERENCES problem(id) ON DELETE RESTRICT,
    material_id UUID NOT NULL REFERENCES material(id) ON DELETE RESTRICT,
    reported_by_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    affected_quantity INTEGER NOT NULL CHECK (affected_quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_occurrence_created_at ON occurrence(created_at DESC);
