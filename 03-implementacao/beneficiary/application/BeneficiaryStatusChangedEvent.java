// ============================================================================
// BeneficiaryStatusChangedEvent.java — bounded context: beneficiary · application
// ============================================================================
// Evento publicado via ApplicationEventPublisher quando o status muda.
// Consumido por NotificationEventListener sem import cruzado de classes internas.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN
// ============================================================================

package br.gov.client.sifap.beneficiary.application;

public record BeneficiaryStatusChangedEvent(
        Long beneficiaryId,
        String cpf,
        String previousStatus,
        String newStatus
) {}
