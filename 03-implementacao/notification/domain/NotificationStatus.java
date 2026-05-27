// ============================================================================
// NotificationStatus.java — bounded context: notification
// ============================================================================
// Sealed interface representando o ciclo de vida de uma notificação.
// Usa sealed + permits do Java 21 para garantir exaustividade no switch.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD] — transições de status inspiradas no padrão
//   de STATUS-PGTO do BATCHPGT.NSN (campo A1: P/A/R/D/E).
// ============================================================================

package br.gov.client.sifap.notification.domain;

public sealed interface NotificationStatus permits
        NotificationStatus.Pending,
        NotificationStatus.Sent,
        NotificationStatus.Failed {

    record Pending() implements NotificationStatus {}

    record Sent() implements NotificationStatus {}

    /** @param reason motivo da falha para facilitar retry e diagnóstico */
    record Failed(String reason) implements NotificationStatus {}
}
