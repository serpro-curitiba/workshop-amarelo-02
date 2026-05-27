// ============================================================================
// CreateAgreementCommand.java — agreement · application
// ============================================================================

package br.gov.client.sifap.agreement.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAgreementCommand(
        @NotBlank @Size(max = 4) String code,
        @NotBlank @Size(max = 60) String name,
        String acronym,
        @NotBlank @Size(max = 1) String type,
        BigDecimal baseValueIndividual,
        BigDecimal baseValueFamily,
        BigDecimal benefitCeiling,
        BigDecimal benefitFloor,
        BigDecimal annualAdjustmentPct,
        BigDecimal maxIncomePerCapita,
        Integer minAge,
        Integer maxAge,
        String legalBasis
) {}
