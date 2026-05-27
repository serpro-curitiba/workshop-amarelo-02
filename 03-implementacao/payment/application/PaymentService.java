// ============================================================================
// PaymentService.java — payment · application
// ============================================================================
// Casos de uso: gerar pagamento, consultar, cancelar, confirmar.
//
// Regras críticas:
//   - Teto 30% descontos não-judiciais → delegado a Payment.applyDeductions()
//   - Idempotência por CPF+período (REQ-BATCH-004 / BATCHPGT.NSN#L200)
//   - Publicação de evento ao mudar status (audit trail via notification)
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN
//                  01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN
//                  01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
// ============================================================================

package br.gov.client.sifap.payment.application;

import br.gov.client.sifap.payment.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository payments, ApplicationEventPublisher eventPublisher) {
        this.payments       = Objects.requireNonNull(payments);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    /**
     * Gera um pagamento para um beneficiário na competência.
     * Idempotente: retorna existente se já gerado (REQ-BATCH-004).
     * Rastreabilidade: BATCHPGT.NSN#L200-L210 (verificação #JA-GERADO).
     */
    @Transactional
    public PaymentDto generate(String cpf, String programCode,
                               Integer period, BigDecimal grossAmount) {
        if (payments.existsByCpfBeneficiaryAndPeriod(cpf, period)) {
            return payments.findByCpfBeneficiaryAndPeriod(cpf, period)
                    .map(PaymentDto::from)
                    .orElseThrow();
        }
        Payment p = new Payment(cpf, programCode, period, grossAmount);
        return PaymentDto.from(payments.save(p));
    }

    /**
     * Consulta por ID.
     */
    @Transactional(readOnly = true)
    public PaymentDto findById(Long id) {
        return payments.findById(id)
                .map(PaymentDto::from)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    /**
     * Lista pagamentos de um beneficiário.
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> findByCpf(String cpf) {
        return payments.findByCpfBeneficiaryOrderByPeriodDesc(cpf).stream()
                .map(PaymentDto::from)
                .toList();
    }

    /**
     * Lista pagamentos por status (paginado).
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto> findByStatus(String status, Pageable pageable) {
        return payments.findByStatus(status, pageable).map(PaymentDto::from);
    }

    /**
     * Cancela pagamento.
     * Rastreabilidade: PAGAMENTO.ddm campo DG MOT-CANCELAMENTO.
     */
    @Transactional
    public PaymentDto cancel(Long id, String reason) {
        Payment p = findOrThrow(id);
        String prev = p.getStatus();
        if ("X".equals(prev)) {
            throw new IllegalStateException("Payment already cancelled: id=" + id);
        }
        p.cancel(reason);
        Payment saved = payments.save(p);
        publishStatusEvent(saved, prev);
        return PaymentDto.from(saved);
    }

    /**
     * Marca pagamento como emitido (enviado ao banco).
     * Rastreabilidade: PAGAMENTO.ddm campo DD DT-EMISSAO.
     */
    @Transactional
    public PaymentDto markIssued(Long id, LocalDate issuedDate) {
        Payment p = findOrThrow(id);
        String prev = p.getStatus();
        p.markIssued(issuedDate);
        Payment saved = payments.save(p);
        publishStatusEvent(saved, prev);
        return PaymentDto.from(saved);
    }

    /**
     * Confirma pagamento (retorno bancário código 00).
     * Rastreabilidade: BATCHCON.NSN#L171-L179 (COD-RET = '00').
     */
    @Transactional
    public PaymentDto confirm(Long id, LocalDate confirmedDate) {
        Payment p = findOrThrow(id);
        String prev = p.getStatus();
        p.markConfirmed(confirmedDate);
        Payment saved = payments.save(p);
        publishStatusEvent(saved, prev);
        return PaymentDto.from(saved);
    }

    // -------------------------------------------------------------------------

    private Payment findOrThrow(Long id) {
        return payments.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private void publishStatusEvent(Payment p, String previousStatus) {
        eventPublisher.publishEvent(new PaymentStatusChangedEvent(
                p.getId(), p.getCpfBeneficiary(), previousStatus, p.getStatus(), p.getPeriod()));
    }
}
