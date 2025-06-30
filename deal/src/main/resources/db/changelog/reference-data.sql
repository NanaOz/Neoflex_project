-- Создаем enum типы в схеме deal_schema
CREATE TYPE deal_schema.employment_status AS ENUM (
    'UNEMPLOYED',
    'SELF_EMPLOYED',
    'EMPLOYED',
    'BUSINESS_OWNER'
);

CREATE TYPE deal_schema.employment_position AS ENUM (
    'WORKER',
    'MID_MANAGER',
    'TOP_MANAGER',
    'OWNER'
);

CREATE TYPE deal_schema.gender AS ENUM (
    'MALE',
    'FEMALE',
    'NON_BINARY'
);

CREATE TYPE deal_schema.marital_status AS ENUM (
    'MARRIED',
    'DIVORCED',
    'SINGLE',
    'WIDOW_WIDOWER'
);

CREATE TYPE deal_schema.application_status AS ENUM (
    'PREAPPROVAL',
    'APPROVED',
    'CC_DENIED',
    'CC_APPROVED',
    'PREPARE_DOCUMENTS',
    'DOCUMENT_CREATED',
    'CLIENT_DENIED',
    'DOCUMENT_SIGNED',
    'CREDIT_ISSUED'
);

CREATE TYPE deal_schema.credit_status AS ENUM (
    'CALCULATED',
    'ISSUED'
);

CREATE TYPE deal_schema.change_type AS ENUM (
    'AUTOMATIC',
    'MANUAL'
);
