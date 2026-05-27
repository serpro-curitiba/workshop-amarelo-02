-- ============================================================================
-- V3__init_beneficiary_module.sql — Migração Flyway
-- Módulo: beneficiary | Bounded context: SIFAP 2.0
-- ============================================================================
-- Rastreabilidade:
--   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
--                  Arquivo Adabas 150 — ~4.2M registros
--                  01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN
-- ============================================================================

CREATE TABLE beneficiary (
    id                  BIGSERIAL    PRIMARY KEY,
    -- Identificação
    cpf                 VARCHAR(11)  NOT NULL UNIQUE,              -- AB NUM-CPF (DE)
    registration_number VARCHAR(11),                               -- AA NUM-INSCRICAO / NIS
    full_name           VARCHAR(60)  NOT NULL,                     -- AC NOME-COMPLETO
    mother_name         VARCHAR(60)  NOT NULL,                     -- AD NOME-MAE
    father_name         VARCHAR(60),                               -- AE NOME-PAI
    birth_date          DATE         NOT NULL,                     -- AF DT-NASCIMENTO
    gender              VARCHAR(1),                                -- AG SEXO M/F/I
    marital_status      VARCHAR(1),                                -- AH EST-CIVIL
    -- Endereço (GRP-ENDERECO BA-BJ)
    logradouro          VARCHAR(60),
    numero              VARCHAR(10),
    complemento         VARCHAR(30),
    bairro              VARCHAR(40),
    municipio           VARCHAR(40),
    uf                  VARCHAR(2),
    cep                 VARCHAR(8),
    cod_ibge            INTEGER,
    cod_regiao          VARCHAR(2),
    -- Dados do Benefício
    program_code        VARCHAR(4)   NOT NULL,                     -- CA COD-PROGRAMA
    benefit_start_date  DATE,                                      -- CC DT-INICIO-BENEF
    benefit_end_date    DATE,                                      -- CD DT-FIM-BENEF (NULL=sem prazo)
    status              VARCHAR(1)   NOT NULL DEFAULT 'A'          -- CE SIT-BENEFICIARIO
                            CONSTRAINT beneficiary_status_check
                            CHECK (status IN ('A','S','C','I','D')),
    status_reason       VARCHAR(3),                                -- CF MOT-SITUACAO
    status_changed_date DATE,                                      -- CG DT-ULT-SITUACAO
    family_income       NUMERIC(9,2),                              -- CH VLR-RENDA-FAMILIAR
    family_size         INTEGER,                                   -- CI QTD-MEMBROS-FAMILIA
    income_per_capita   NUMERIC(7,2),                              -- CJ IND-RENDA-PERCAP
    -- Contato (adicionado 2015)
    phone               VARCHAR(15),                               -- EB TEL-CELULAR
    email               VARCHAR(80),                               -- EC EMAIL
    -- Controle
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ
);

-- GRP-DEPENDENTE (PE/10) → tabela filha
CREATE TABLE beneficiary_dependent (
    id              BIGSERIAL   PRIMARY KEY,
    beneficiary_id  BIGINT      NOT NULL REFERENCES beneficiary(id),
    cpf             VARCHAR(11),                    -- DB CPF-DEPENDENTE (NULL = sem CPF)
    name            VARCHAR(60) NOT NULL,           -- DC NOME-DEPENDENTE
    birth_date      DATE,                           -- DD DT-NASC-DEPEND
    relationship    VARCHAR(2)  NOT NULL            -- DE PARENTESCO FI/CJ/NT/TU
                        CONSTRAINT dep_relationship_check
                        CHECK (relationship IN ('FI','CJ','NT','TU','OU')),
    status          VARCHAR(1)  NOT NULL DEFAULT 'A'
                        CONSTRAINT dep_status_check
                        CHECK (status IN ('A','I','D')),
    has_disability  VARCHAR(1)  NOT NULL DEFAULT 'N'  -- DG IND-DEFICIENCIA S/N
);

-- Índices (superdescriptors S2 e S3 do DDM)
CREATE INDEX idx_beneficiary_status        ON beneficiary (status);
CREATE INDEX idx_beneficiary_program_status ON beneficiary (program_code, status);
CREATE INDEX idx_beneficiary_cpf_status    ON beneficiary (cpf, status);
CREATE INDEX idx_dep_beneficiary_id        ON beneficiary_dependent (beneficiary_id);

COMMENT ON TABLE beneficiary IS
    'Cadastro de beneficiários — DDM BENEFICIARIO arquivo 150. ~4.2M registros (2018). '
    'CPF nunca expor em logs sem mascaramento (LGPD).';
COMMENT ON TABLE beneficiary_dependent IS
    'Dependentes vinculados ao beneficiário — GRP-DEPENDENTE PE/10 do DDM BENEFICIARIO.';
