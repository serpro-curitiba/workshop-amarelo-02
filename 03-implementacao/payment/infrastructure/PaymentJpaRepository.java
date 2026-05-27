// ============================================================================
// PaymentJpaRepository.java — payment · infrastructure
// ============================================================================

package br.gov.client.sifap.payment.infrastructure;

import br.gov.client.sifap.payment.domain.Payment;
import br.gov.client.sifap.payment.domain.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository
        extends PaymentRepository, JpaRepository<Payment, Long> {
}
