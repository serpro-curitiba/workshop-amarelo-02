// ============================================================================
// NotificationService.java — bounded context: notification · application
// ============================================================================
// Camada de aplicação: orquestra os casos de uso de notificação.
// @Transactional somente aqui — nunca em Repository ou Controller.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD] — sem equivalente no legado Natural.
//   Eventos de origem rastreiam para:
//     - BATCHCON.NSN#L166-L168 (divergência bancária)
//     - BATCHPGT.NSN#L88-L142  (geração de ciclo de pagamento)
//
// Casos de uso implementados:
//   - send(command)      → cria Notification, simula envio, persiste status
//   - retry(id)          → reprocessa notificação FAILED
//   - findById(id)       → consulta por ID
//   - findFailed()       → lista notificações com status FAILED para retry em batch
// ============================================================================

package br.gov.client.sifap.notification.application;

import br.gov.client.sifap.notification.domain.Notification;
import br.gov.client.sifap.notification.domain.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = Objects.requireNonNull(notifications);
    }

    /**
     * Envia uma notificação: cria o registro, despacha pelo canal e atualiza status.
     * Em uma implementação real, o despacho seria via adaptador de infraestrutura
     * (SES, Twilio etc.) injetado como interface — aqui simulado para o workshop.
     *
     * @param command dados da notificação a enviar
     * @return DTO com o registro criado e status atualizado
     */
    @Transactional
    public NotificationDto send(SendNotificationCommand command) {
        String safeBody = maskSensitiveData(command.body());

        Notification notification = new Notification(
                command.channel(),
                command.recipient(),
                command.subject(),
                safeBody,
                command.referenceId(),
                command.referenceTable()
        );

        try {
            dispatch(notification);        // porta de saída — troque por adaptador real
            notification.markSent();
        } catch (Exception ex) {
            notification.markFailed(ex.getMessage());
        }

        return NotificationDto.from(notifications.save(notification));
    }

    /**
     * Reprocessa uma notificação FAILED.
     * REQ-NOT-002 (adicione ao SPECIFICATION.md da sua equipe).
     */
    @Transactional
    public NotificationDto retry(Long id) {
        Notification notification = findOrThrow(id);

        if (!"FAILED".equals(notification.getStatus())) {
            throw new IllegalStateException(
                    "Only FAILED notifications can be retried; current status: " + notification.getStatus());
        }

        notification.resetForRetry();

        try {
            dispatch(notification);
            notification.markSent();
        } catch (Exception ex) {
            notification.markFailed(ex.getMessage());
        }

        return NotificationDto.from(notifications.save(notification));
    }

    /** Consulta por ID — nunca retorna null; lança NotificationNotFoundException se ausente. */
    @Transactional(readOnly = true)
    public NotificationDto findById(Long id) {
        return NotificationDto.from(findOrThrow(id));
    }

    /** Lista todas as notificações com status FAILED — útil para job de retry. */
    @Transactional(readOnly = true)
    public List<NotificationDto> findFailed() {
        return notifications.findByStatus("FAILED").stream()
                .map(NotificationDto::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Métodos privados de suporte
    // -------------------------------------------------------------------------

    private Notification findOrThrow(Long id) {
        return notifications.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    /**
     * Porta de saída para despacho real (EMAIL via SES, SMS via Twilio etc.).
     * Substitua por uma interface injetável para testes e produção.
     * Workshop: log simula envio.
     */
    private void dispatch(Notification notification) {
        // TODO: injetar NotificationGateway (interface) e chamar gateway.send(notification)
        // Simulação para o workshop:
        System.out.printf("[NOTIFICATION] Dispatching %s to %s via %s%n",
                notification.getId(), notification.getRecipient(), notification.getChannel());
    }

    /**
     * Mascara CPF no formato XXX.XXX.NNN-NN antes de persistir no body.
     * Requisito LGPD — sem dados sensíveis em logs ou banco sem mascaramento.
     * Padrão: mantém apenas os 3 dígitos centrais e os verificadores.
     */
    static String maskSensitiveData(String text) {
        if (text == null) return null;
        // CPF formatado: 000.000.000-00 → XXX.XXX.NNN-NN
        return text.replaceAll("(\\d{3})\\.(\\d{3})\\.(\\d{3})-(\\d{2})",
                "XXX.XXX.$3-$4");
    }
}
