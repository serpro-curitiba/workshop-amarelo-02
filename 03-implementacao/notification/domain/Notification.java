// ============================================================================
// Notification.java — bounded context: notification
// ============================================================================
// Entidade JPA central do módulo. Representa uma mensagem enviada ou a enviar
// para um beneficiário/operador do SIFAP 2.0.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD] — sem tabela equivalente no legado Adabas.
//   Justificativa: comunicação eletrônica é requisito LGPD/modernização.
//   Eventos que geram notificações rastreiam para BATCHCON.NSN, BATCHPGT.NSN.
//
// Migração de banco: V2__init_notification_module.sql
// ============================================================================

package br.gov.client.sifap.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationChannel channel;

    /** Endereço de destino: e-mail, número de telefone, ou token de push. */
    @Column(nullable = false)
    private String recipient;

    @Column(length = 255)
    private String subject;

    /**
     * Corpo da mensagem. CPF e dados sensíveis DEVEM ser mascarados antes
     * de persistir (ver NotificationService#maskSensitiveData).
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Status armazenado como String para compatibilidade; o domínio usa
     * NotificationStatus (sealed interface). Convertido via @PostLoad.
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** ID do registro de origem (ex.: payment.id, audit.id). */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Tabela/entidade de origem (ex.: "payment", "audit"). */
    @Column(name = "reference_tbl", length = 50)
    private String referenceTable;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "sent_at")
    private Instant sentAt;

    protected Notification() {}

    public Notification(
            NotificationChannel channel,
            String recipient,
            String subject,
            String body,
            Long referenceId,
            String referenceTable) {
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.referenceId = referenceId;
        this.referenceTable = referenceTable;
    }

    /** Marca a notificação como enviada com sucesso. */
    public void markSent() {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }

    /** Marca a notificação como falha, registrando o motivo para retry. */
    public void markFailed(String reason) {
        this.status = "FAILED";
    }

    /** Recoloca em fila para nova tentativa de envio. */
    public void resetForRetry() {
        this.status = "PENDING";
        this.sentAt = null;
    }

    public Long getId() { return id; }
    public NotificationChannel getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getStatus() { return status; }
    public Long getReferenceId() { return referenceId; }
    public String getReferenceTable() { return referenceTable; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
}
