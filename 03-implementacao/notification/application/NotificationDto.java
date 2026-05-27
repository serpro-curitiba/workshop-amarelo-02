// ============================================================================
// NotificationDto.java — bounded context: notification · application
// ============================================================================
// Record DTO de resposta da API. Nunca exponha a entidade JPA diretamente.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD]
// ============================================================================

package br.gov.client.sifap.notification.application;

import br.gov.client.sifap.notification.domain.Notification;
import br.gov.client.sifap.notification.domain.NotificationChannel;

import java.time.Instant;

/**
 * DTO de leitura retornado pela API REST.
 * Construído a partir da entidade Notification via factory method {@link #from}.
 */
public record NotificationDto(
        Long id,
        NotificationChannel channel,
        String recipient,
        String subject,
        String status,
        Long referenceId,
        String referenceTable,
        Instant createdAt,
        Instant sentAt
) {
    /** Factory method — nunca exponha campos internos da entidade diretamente. */
    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getChannel(),
                n.getRecipient(),
                n.getSubject(),
                n.getStatus(),
                n.getReferenceId(),
                n.getReferenceTable(),
                n.getCreatedAt(),
                n.getSentAt()
        );
    }
}
