// ============================================================================
// NotificationEvent.java — bounded context: notification
// ============================================================================
// Record imutável que representa um evento de domínio que dispara notificação.
// Publicado via ApplicationEventPublisher por outros bounded contexts.
//
// Rastreabilidade:
//   source_legacy: BATCHCON.NSN#L166-L168 (divergência bancária → auditoria)
//                  BATCHPGT.NSN#L88-L142  (geração de ciclo → notificação)
//                  RELAUDIT.NSN#L45-L72   (trilha de auditoria → evento)
// ============================================================================

package br.gov.client.sifap.notification.domain;

/**
 * Evento de domínio publicado por outros módulos (payment, audit etc.).
 * O NotificationEventListener consome este record e dispara notificações.
 *
 * @param eventType      tipo do evento (ex.: "PAYMENT_APPROVED", "BATCH_DIVERGENCE")
 * @param referenceId    ID do registro que originou o evento
 * @param referenceTable tabela/entidade de origem (ex.: "payment", "audit")
 * @param recipientCpf   CPF do destinatário (será mascarado antes de persistir)
 */
public record NotificationEvent(
        String eventType,
        Long referenceId,
        String referenceTable,
        String recipientCpf
) {}
