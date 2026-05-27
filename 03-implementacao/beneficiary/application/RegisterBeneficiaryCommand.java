// ============================================================================
// RegisterBeneficiaryCommand.java — bounded context: beneficiary · application
// ============================================================================

package br.gov.client.sifap.beneficiary.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterBeneficiaryCommand(
        @NotBlank @Size(min = 11, max = 11) String cpf,
        @NotBlank @Size(max = 60)           String fullName,
        @NotBlank @Size(max = 60)           String motherName,
        @NotNull  @Past                     LocalDate birthDate,
        @NotBlank @Size(max = 4)            String programCode,
        BigDecimal familyIncome,
        Integer familySize,
        String email,
        String phone
) {}
