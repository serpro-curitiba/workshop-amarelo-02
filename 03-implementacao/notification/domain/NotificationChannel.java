// ============================================================================
// NotificationChannel.java — bounded context: notification
// ============================================================================
// Canais de envio de notificação disponíveis no SIFAP 2.0.
//
// Rastreabilidade:
//   source_legacy: [GREENFIELD] sem equivalente direto no legado Natural/Adabas.
//   Justificativa: o legado imprimia relatórios (RELPGT.NSN, RELAUDIT.NSN);
//   comunicação eletrônica com beneficiários é requisito LGPD/modernização.
// ============================================================================

package br.gov.client.sifap.notification.domain;

public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH
}
