// ============================================================================
// BeneficiaryDto.java — bounded context: beneficiary · application
// ============================================================================

package br.gov.client.sifap.beneficiary.application;

import br.gov.client.sifap.beneficiary.domain.Beneficiary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

public record BeneficiaryDto(
        Long id,
        String cpf,          // mascarado antes de retornar ao cliente
        String fullName,
        LocalDate birthDate,
        String programCode,
        String status,
        BigDecimal familyIncome,
        Integer familySize,
        String email,
        Instant createdAt,
        Instant updatedAt
) {
    public static BeneficiaryDto from(Beneficiary b) {
        return new BeneficiaryDto(
                b.getId(),
                maskCpf(b.getCpf()),
                b.getFullName(),
                b.getBirthDate(),
                b.getProgramCode(),
                b.getStatus(),
                b.getFamilyIncome(),
                b.getFamilySize(),
                b.getEmail(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    /** Mascara CPF: XXX.XXX.NNN-NN (mantém 3 centrais + verificadores). */
    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return "***.***.***-**";
        return "XXX.XXX." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
