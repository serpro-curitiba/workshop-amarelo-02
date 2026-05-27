-- ============================================================================
-- V2__init_notification_module.sql — Migração Flyway
-- Módulo: notification | Bounded context: SIFAP 2.0
-- ============================================================================
-- Rastreabilidade:
--   source_legacy: [GREENFIELD] sem tabela equivalente no legado Adabas.
--   Eventos de origem: BATCHCON.NSN (divergência bancária), BATCHPGT.NSN (ciclo).
-- ============================================================================

CREATE TABLE notification (
    id            BIGSERIAL    PRIMARY KEY,
    channel       VARCHAR(10)  NOT NULL
                    CONSTRAINT notification_channel_check
                    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    recipient     VARCHAR(255) NOT NULL,
    subject       VARCHAR(255),
    body          TEXT         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CONSTRAINT notification_status_check
                    CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    reference_id  BIGINT,
    reference_tbl VARCHAR(50),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at       TIMESTAMPTZ
);

-- Índice para busca por status (job de retry usa findByStatus)
CREATE INDEX idx_notification_status ON notification (status);

-- Índice para rastreabilidade inversa (qual notificação originou de qual entidade)
CREATE INDEX idx_notification_reference ON notification (reference_tbl, reference_id);

COMMENT ON TABLE notification IS
    'Registro de notificações enviadas ou pendentes. CPF/dados sensíveis '
    'devem ser mascarados antes de inserir em ''body''. '
    'GREENFIELD — sem equivalente no legado Natural/Adabas.';
