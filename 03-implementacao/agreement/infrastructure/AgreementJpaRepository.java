// ============================================================================
// AgreementJpaRepository.java — agreement · infrastructure
// ============================================================================

package br.gov.client.sifap.agreement.infrastructure;

import br.gov.client.sifap.agreement.domain.Agreement;
import br.gov.client.sifap.agreement.domain.AgreementRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementJpaRepository
        extends AgreementRepository, JpaRepository<Agreement, Long> {
}
