// ============================================================================
// AgreementService.java — agreement · application
// ============================================================================
// Casos de uso: consultar, listar ativos, criar, encerrar programa.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
//                  01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm
// ============================================================================

package br.gov.client.sifap.agreement.application;

import br.gov.client.sifap.agreement.domain.Agreement;
import br.gov.client.sifap.agreement.domain.AgreementRepository;
import br.gov.client.sifap.agreement.domain.AgreementStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class AgreementService {

    private final AgreementRepository agreements;

    public AgreementService(AgreementRepository agreements) {
        this.agreements = Objects.requireNonNull(agreements);
    }

    @Transactional(readOnly = true)
    public AgreementDto findByCode(String code) {
        return agreements.findByCode(code)
                .map(AgreementDto::from)
                .orElseThrow(() -> new AgreementNotFoundException(code));
    }

    @Transactional(readOnly = true)
    public AgreementDto findById(Long id) {
        return agreements.findById(id)
                .map(AgreementDto::from)
                .orElseThrow(() -> new AgreementNotFoundException(id));
    }

    /** Lista apenas programas ativos — usado pelo BATCHPGT para calcular benefícios. */
    @Transactional(readOnly = true)
    public List<AgreementDto> listActive() {
        return agreements.findByStatus(AgreementStatus.ACTIVE).stream()
                .map(AgreementDto::from)
                .toList();
    }

    @Transactional
    public AgreementDto create(CreateAgreementCommand cmd) {
        if (agreements.existsByCode(cmd.code())) {
            throw new IllegalStateException("Agreement already exists: code=" + cmd.code());
        }
        Agreement a = new Agreement(cmd.code(), cmd.name(), cmd.type());
        a.setAcronym(cmd.acronym());
        a.setBaseValueIndividual(cmd.baseValueIndividual());
        a.setBaseValueFamily(cmd.baseValueFamily());
        a.setBenefitCeiling(cmd.benefitCeiling());
        a.setBenefitFloor(cmd.benefitFloor());
        a.setAnnualAdjustmentPct(cmd.annualAdjustmentPct());
        a.setMaxIncomePerCapita(cmd.maxIncomePerCapita());
        a.setMinAge(cmd.minAge());
        a.setMaxAge(cmd.maxAge());
        a.setLegalBasis(cmd.legalBasis());
        return AgreementDto.from(agreements.save(a));
    }

    @Transactional
    public AgreementDto close(String code, LocalDate closeDate) {
        Agreement a = agreements.findByCode(code)
                .orElseThrow(() -> new AgreementNotFoundException(code));
        a.close(closeDate);
        return AgreementDto.from(agreements.save(a));
    }
}
