// ============================================================================
// AgreementRepository.java — agreement · domain
// ============================================================================

package br.gov.client.sifap.agreement.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {

    Optional<Agreement> findByCode(String code);

    List<Agreement> findByStatus(AgreementStatus status);

    boolean existsByCode(String code);
}
