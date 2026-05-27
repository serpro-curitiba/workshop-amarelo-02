-- ============================================================================
-- V4__init_agreement_module.sql — Migração Flyway
-- Módulo: agreement | Bounded context: SIFAP 2.0
-- ============================================================================
-- Rastreabilidade:
--   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm
--                  Arquivo Adabas 151 — ~45 programas ativos (2018)
--                  01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
-- ============================================================================

CREATE TABLE agreement (
    id                     BIGSERIAL    PRIMARY KEY,
    code                   VARCHAR(4)   NOT NULL UNIQUE,   -- AA COD-PROGRAMA (DE)
    name                   VARCHAR(60)  NOT NULL,          -- AB NOME-PROGRAMA
    acronym                VARCHAR(10),                    -- AC SIGLA-PROGRAMA
    type                   VARCHAR(1)   NOT NULL           -- AD TIPO-PROGRAMA
                               CONSTRAINT agreement_type_check
                               CHECK (type IN ('A','T','P')),
    governing_body         VARCHAR(10),                    -- AE ORGAO-RESPONSAVEL
    legal_basis            VARCHAR(20),                    -- AF LEI-CRIACAO
    created_date           DATE,                           -- AG DT-CRIACAO
    closed_date            DATE,                           -- AH DT-ENCERRAMENTO
    status                 VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE'
                               CONSTRAINT agreement_status_check
                               CHECK (status IN ('ACTIVE','INACTIVE','CLOSED')),
    -- Valores base
    base_value_individual  NUMERIC(7,2),  -- BA VLR-BASE-INDIVIDUAL
    base_value_family      NUMERIC(7,2),  -- BB VLR-BASE-FAMILIAR
    benefit_ceiling        NUMERIC(9,2),  -- BC VLR-TETO-BENEF
    benefit_floor          NUMERIC(7,2),  -- BD VLR-PISO-BENEF
    annual_adjustment_pct  NUMERIC(3,2),  -- BE PCT-REAJUSTE-ANUAL
    last_adjustment_date   DATE,          -- BF DT-ULT-REAJUSTE
    -- FATOR-K — undocumented, inserido ago/2008. NÃO USAR sem entender. Ver mysteries-found.md
    factor_k               NUMERIC(5,4),  -- BG FATOR-K
    -- Elegibilidade
    max_income_per_capita  NUMERIC(7,2),  -- CA RENDA-MAX-PERCAP
    min_age                INTEGER,       -- CB IDADE-MIN (0/NULL=sem)
    max_age                INTEGER,       -- CC IDADE-MAX (0/NULL=sem)
    requires_children      VARCHAR(1) DEFAULT 'N', -- CD IND-EXIGE-FILHOS
    min_children           INTEGER,                -- CE QTD-MIN-FILHOS
    requires_school        VARCHAR(1) DEFAULT 'N', -- CF IND-EXIGE-ESCOLA
    -- Controle
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ
);

-- GRP-FAIXA-CALCULO (PE/5) → tabela filha
CREATE TABLE agreement_income_range (
    id              BIGSERIAL    PRIMARY KEY,
    agreement_id    BIGINT       NOT NULL REFERENCES agreement(id),
    income_from     NUMERIC(7,2) NOT NULL, -- DB RENDA-INICIO
    income_to       NUMERIC(7,2) NOT NULL, -- DC RENDA-FIM
    multiplier      NUMERIC(3,4) NOT NULL, -- DD FATOR-MULTIPLICADOR
    additional_amount NUMERIC(7,2),        -- DE VLR-ADICIONAL
    cumulative      VARCHAR(1) DEFAULT 'N' -- DF IND-ACUMULATIVO
);

-- TIPO-DSCT-APLIC (MU/8) → tabela de associação
CREATE TABLE agreement_discount_type (
    agreement_id  BIGINT      NOT NULL REFERENCES agreement(id),
    discount_type VARCHAR(10) NOT NULL
                      CONSTRAINT adt_type_check
                      CHECK (discount_type IN ('IR','JD','CS','PA','EM','TX','OU','EX')),
    PRIMARY KEY (agreement_id, discount_type)
);

CREATE INDEX idx_agreement_status    ON agreement (status);
CREATE INDEX idx_income_range_agr_id ON agreement_income_range (agreement_id);

COMMENT ON TABLE agreement IS
    'Programas sociais (PROGRAMA-SOCIAL DDM 151). ~45 registros ativos. '
    'FATOR-K (factor_k) é undocumented — não usar sem aprovação SENARC.';
COMMENT ON COLUMN agreement.factor_k IS
    'Campo não documentado inserido ago/2008 por Adilson Batista. '
    '"Atende solicitação SENARC". Ver mysteries-found.md.';
