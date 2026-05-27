// ============================================================================
// PaymentDto.java — payment · application
// ============================================================================

package br.gov.client.sifap.payment.application;

import br.gov.client.sifap.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

public record PaymentDto(
        Long id,
        String cpfBeneficiary,  // mascarado
        String programCode,
        Integer period,
        BigDecimal grossAmount,
        BigDecimal totalDeduction,
        BigDecimal netAmount,
        String status,
        LocalDate generatedDate,
        LocalDate issuedDate,
        LocalDate confirmedDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentDto from(Payment p) {
        return new PaymentDto(
                p.getId(),
                maskCpf(p.getCpfBeneficiary()),
                p.getProgramCode(),
                p.getPeriod(),
                p.getGrossAmount(),
                p.getTotalDeduction(),
                p.getNetAmount(),
                p.getStatus(),
                p.getGeneratedDate(),
                p.getIssuedDate(),
                p.getConfirmedDate(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return "***.***.***-**";
        return "XXX.XXX." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
