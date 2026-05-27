// ============================================================================
// PaymentStatusChangedEvent.java — payment · application
// ============================================================================
// Evento publicado quando o status de um pagamento muda.
// Consumido por NotificationEventListener sem acoplamento direto.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L166-L201
//                  (alterações de status após conciliação bancária)
// ============================================================================

package br.gov.client.sifap.payment.application;

public record PaymentStatusChangedEvent(
        Long paymentId,
        String cpfBeneficiary,
        String previousStatus,
        String newStatus,
        Integer period
) {}
