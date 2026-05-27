// ============================================================================
// SendNotificationCommand.java — bounded context: notification · application
// ============================================================================
// Record imutável que serve como "command" de entrada para NotificationService.
// Usar records do Java 21 para garantir imutabilidade e equals/hashCode grátis.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD]
// ============================================================================

package br.gov.client.sifap.notification.application;

import br.gov.client.sifap.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command para envio de uma notificação.
 *
 * @param channel      canal de envio (EMAIL, SMS, PUSH)
 * @param recipient    endereço de destino (validado pelo canal no service)
 * @param subject      assunto — obrigatório para EMAIL, ignorado para SMS/PUSH
 * @param body         corpo da mensagem (não deve conter CPF cru — mascare antes)
 * @param referenceId  ID da entidade que originou a notificação (opcional)
 * @param referenceTable tabela/entidade de origem (opcional)
 */
public record SendNotificationCommand(
        @NotNull NotificationChannel channel,
        @NotBlank String recipient,
        String subject,
        @NotBlank String body,
        Long referenceId,
        String referenceTable
) {}
