// ============================================================================
// AgreementDto.java — agreement · application
// ============================================================================

package br.gov.client.sifap.agreement.application;

import br.gov.client.sifap.agreement.domain.Agreement;
import br.gov.client.sifap.agreement.domain.AgreementStatus;

import java.math.BigDecimal;

public record AgreementDto(
        Long id,
        String code,
        String name,
        String acronym,
        String type,
        AgreementStatus status,
        BigDecimal baseValueIndividual,
        BigDecimal baseValueFamily,
        BigDecimal benefitCeiling,
        BigDecimal benefitFloor,
        BigDecimal annualAdjustmentPct,
        BigDecimal maxIncomePerCapita,
        Integer minAge,
        Integer maxAge
) {
    public static AgreementDto from(Agreement a) {
        return new AgreementDto(
                a.getId(), a.getCode(), a.getName(), a.getAcronym(), a.getType(),
                a.getStatus(), a.getBaseValueIndividual(), a.getBaseValueFamily(),
                a.getBenefitCeiling(), a.getBenefitFloor(), a.getAnnualAdjustmentPct(),
                a.getMaxIncomePerCapita(), a.getMinAge(), a.getMaxAge()
        );
    }
}
