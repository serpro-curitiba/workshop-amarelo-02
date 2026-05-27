// ============================================================================
// BeneficiaryJpaRepository.java — beneficiary · infrastructure
// ============================================================================

package br.gov.client.sifap.beneficiary.infrastructure;

import br.gov.client.sifap.beneficiary.domain.Beneficiary;
import br.gov.client.sifap.beneficiary.domain.BeneficiaryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryJpaRepository
        extends BeneficiaryRepository, JpaRepository<Beneficiary, Long> {
}
