// ============================================================================
// NotificationEventListener.java — bounded context: notification · infrastructure
// ============================================================================
// Adaptador de entrada via Spring Application Events.
// Escuta eventos publicados por outros bounded contexts SEM importar suas classes.
// Isso mantém a fronteira entre módulos — sem acoplamento direto.
//
// Como publicar um evento do módulo payment:
//   applicationEventPublisher.publishEvent(new NotificationEvent(
//       "PAYMENT_APPROVED", payment.getId(), "payment", payment.getCpfBenef()));
//
// Rastreabilidade:
//   source_legacy: BATCHCON.NSN#L166-L168 (divergência bancária)
//                  BATCHPGT.NSN#L88-L142  (geração de ciclo mensal)
// ============================================================================

package br.gov.client.sifap.notification.infrastructure;

import br.gov.client.sifap.notification.application.NotificationService;
import br.gov.client.sifap.notification.application.SendNotificationCommand;
import br.gov.client.sifap.notification.domain.NotificationChannel;
import br.gov.client.sifap.notification.domain.NotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Consome NotificationEvent de qualquer módulo e dispara notificação.
     * O canal padrão é EMAIL — adapte conforme regras de negócio.
     *
     * Rastreabilidade: BATCHCON.NSN#L166 (divergência detectada → notifica operador).
     */
    @EventListener
    public void onNotificationEvent(NotificationEvent event) {
        String body = buildBody(event);

        SendNotificationCommand command = new SendNotificationCommand(
                NotificationChannel.EMAIL,
                resolveRecipient(event),
                "SIFAP — " + event.eventType(),
                body,
                event.referenceId(),
                event.referenceTable()
        );

        notificationService.send(command);
    }

    private String buildBody(NotificationEvent event) {
        // CPF é mascarado pelo NotificationService#maskSensitiveData antes de persistir
        return String.format(
                "Evento: %s%nEntidade: %s (id=%d)%nDestinatário: %s",
                event.eventType(),
                event.referenceTable(),
                event.referenceId(),
                event.recipientCpf()   // mascaramento ocorre no service
        );
    }

    private String resolveRecipient(NotificationEvent event) {
        // Produção: buscar e-mail do beneficiário no módulo beneficiary via porta de saída.
        // Workshop: retorna placeholder para simplificar.
        return "notifications@sifap.gov.br";
    }
}
