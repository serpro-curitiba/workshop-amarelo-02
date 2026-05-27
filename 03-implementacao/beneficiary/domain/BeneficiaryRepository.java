// ============================================================================
// BeneficiaryRepository.java — bounded context: beneficiary · domain
// ============================================================================
// Porta de saída (interface Spring Data). Implementação gerada pelo framework.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
//                  Superdescriptors S1(CPF), S2(UF+SIT), S3(PROGRAMA+SIT)
// ============================================================================

package br.gov.client.sifap.beneficiary.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    /** Busca por CPF — superdescriptor S1. */
    Optional<Beneficiary> findByCpf(String cpf);

    /** Lista ativos paginados, ordenados por CPF (mesma ordem do BATCHPGT.NSN). */
    @Query("SELECT b FROM Beneficiary b WHERE b.status = 'A' ORDER BY b.cpf ASC")
    Page<Beneficiary> findAllActiveOrderByCpf(Pageable pageable);

    /** Conta ativos por programa — superdescriptor S3. */
    long countByProgramCodeAndStatus(String programCode, String status);

    /** Verifica existência por CPF — usado em validação de dependentes. */
    boolean existsByCpf(String cpf);
}
