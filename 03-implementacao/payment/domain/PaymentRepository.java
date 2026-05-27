// ============================================================================
// PaymentRepository.java — payment · domain
// ============================================================================

package br.gov.client.sifap.payment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Idempotência: verifica se pagamento já foi gerado para CPF nesta competência. */
    boolean existsByCpfBeneficiaryAndPeriod(String cpf, Integer period);

    /** Lista pagamentos de um beneficiário. */
    List<Payment> findByCpfBeneficiaryOrderByPeriodDesc(String cpf);

    /** Paginação por status (ex.: buscar todos 'G' para emissão). */
    Page<Payment> findByStatus(String status, Pageable pageable);

    /** Totais do ciclo — para relatório de resumo (equivalente ao BATCHPGT resumo). */
    @Query("SELECT COUNT(p), SUM(p.grossAmount), SUM(p.totalDeduction), SUM(p.netAmount) " +
           "FROM Payment p WHERE p.period = :period AND p.status != 'X'")
    Object[] summarizeByPeriod(@Param("period") Integer period);

    /** Superdescriptor S1: CPF + COMPETÊNCIA. */
    Optional<Payment> findByCpfBeneficiaryAndPeriod(String cpf, Integer period);
}
