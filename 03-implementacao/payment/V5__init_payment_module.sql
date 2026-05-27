-- ============================================================================
-- V5__init_payment_module.sql — Migração Flyway
-- Módulo: payment | Bounded context: SIFAP 2.0
-- ============================================================================
-- Rastreabilidade:
--   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
--                  Arquivo Adabas 152 — ~180M registros (2018)
--                  01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN
--                  01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN
-- ============================================================================

CREATE TABLE payment (
    id                BIGSERIAL    PRIMARY KEY,
    -- Identificação
    legacy_sequence   BIGINT       UNIQUE,       -- AA NUM-PAGAMENTO (sequencial histórico)
    cpf_beneficiary   VARCHAR(11)  NOT NULL,     -- AB NUM-CPF (nunca expor sem mascaramento)
    program_code      VARCHAR(4)   NOT NULL,     -- AD COD-PROGRAMA
    period            INTEGER      NOT NULL,     -- AE ANO-MES-REF (AAAAMM)
    cycle_number      INTEGER,                   -- AF NUM-CICLO
    -- Valores (TRUNCADOS, não arredondados — BATCHPGT.NSN#L284)
    gross_amount      NUMERIC(9,2) NOT NULL,     -- BA VLR-BRUTO
    net_amount        NUMERIC(9,2) NOT NULL,     -- BB VLR-LIQUIDO (nunca negativo)
    total_deduction   NUMERIC(7,2) NOT NULL DEFAULT 0, -- BC VLR-DESCONTO-TOTAL
    -- Status
    status            VARCHAR(1)   NOT NULL DEFAULT 'P' -- DA SIT-PAGAMENTO
                          CONSTRAINT payment_status_check
                          CHECK (status IN ('P','G','E','C','D','X','R')),
    generated_date    DATE,                      -- DB DT-GERACAO
    issued_date       DATE,                      -- DD DT-EMISSAO
    confirmed_date    DATE,                      -- DE DT-CONFIRMACAO
    cancelled_date    DATE,                      -- DF DT-CANCELAMENTO
    cancel_reason     VARCHAR(3),                -- DG MOT-CANCELAMENTO
    -- Dados bancários
    bank_code         VARCHAR(3),                -- EA COD-BANCO
    bank_return_code  VARCHAR(2),                -- GD COD-RETORNO-BANCO (BATCHCON)
    -- Controle
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ,
    -- Idempotência: um pagamento por CPF por competência (REQ-BATCH-004)
    CONSTRAINT uq_payment_cpf_period UNIQUE (cpf_beneficiary, period)
);

-- GRP-DESCONTO (PE/8) → tabela filha
CREATE TABLE payment_deduction (
    id                  BIGSERIAL    PRIMARY KEY,
    payment_id          BIGINT       NOT NULL REFERENCES payment(id),
    type                VARCHAR(3)   NOT NULL   -- CB TIPO-DESCONTO
                            CONSTRAINT ded_type_check
                            CHECK (type IN ('IR','JD','CS','PA','EM','TX','OU','EX')),
    amount              NUMERIC(7,2) NOT NULL,  -- CC VLR-DESCONTO
    percentage          NUMERIC(3,2),           -- CD PCT-DESCONTO
    legal_process_number VARCHAR(20),           -- CE NUM-PROCESSO (apenas JD)
    start_date          DATE,                   -- CF DT-INICIO-DSCT
    end_date            DATE                    -- CG DT-FIM-DSCT (NULL=indefinido)
);

-- Índices (superdescriptors do DDM)
CREATE INDEX idx_payment_cpf_period    ON payment (cpf_beneficiary, period);
CREATE INDEX idx_payment_status        ON payment (status);
CREATE INDEX idx_payment_program_period ON payment (program_code, period, status);
CREATE INDEX idx_deduction_payment_id  ON payment_deduction (payment_id);

COMMENT ON TABLE payment IS
    'Pagamentos processados pelo SIFAP — DDM PAGAMENTO arquivo 152. '
    '~180M registros (2018). Crescimento: ~3.8M/mês. '
    'CPF mascarado nos DTOs. Valores TRUNCADOS (não arredondados).';
COMMENT ON COLUMN payment.net_amount IS
    'Valor líquido — nunca negativo (BATCHPGT.NSN#L316). '
    'Teto não-judicial: 30%% do bruto (CALCDSCT.NSN#L142, REQ-PAY-001).';
