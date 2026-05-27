// ============================================================================
// BeneficiaryService.java — bounded context: beneficiary · application
// ============================================================================
// Casos de uso: registrar beneficiário, consultar, atualizar status.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN
//                  01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
// ============================================================================

package br.gov.client.sifap.beneficiary.application;

import br.gov.client.sifap.beneficiary.domain.Beneficiary;
import br.gov.client.sifap.beneficiary.domain.BeneficiaryRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaries;
    private final ApplicationEventPublisher eventPublisher;

    public BeneficiaryService(BeneficiaryRepository beneficiaries,
                              ApplicationEventPublisher eventPublisher) {
        this.beneficiaries  = Objects.requireNonNull(beneficiaries);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    /**
     * Registra novo beneficiário.
     * Valida CPF único antes de persistir (equivalente à validação de duplicata
     * do CADBENEF.NSN).
     */
    @Transactional
    public BeneficiaryDto register(RegisterBeneficiaryCommand cmd) {
        if (beneficiaries.existsByCpf(cmd.cpf())) {
            throw new IllegalStateException("CPF already registered: " + cmd.cpf());
        }
        Beneficiary b = new Beneficiary(
                cmd.cpf(), cmd.fullName(), cmd.motherName(),
                cmd.birthDate(), cmd.programCode());
        b.setFamilyIncome(cmd.familyIncome());
        b.setFamilySize(cmd.familySize());
        b.setEmail(cmd.email());
        b.setPhone(cmd.phone());
        return BeneficiaryDto.from(beneficiaries.save(b));
    }

    /** Consulta por CPF — principal chave de acesso do sistema. */
    @Transactional(readOnly = true)
    public BeneficiaryDto findByCpf(String cpf) {
        return beneficiaries.findByCpf(cpf)
                .map(BeneficiaryDto::from)
                .orElseThrow(() -> new BeneficiaryNotFoundException(cpf));
    }

    @Transactional(readOnly = true)
    public BeneficiaryDto findById(Long id) {
        return beneficiaries.findById(id)
                .map(BeneficiaryDto::from)
                .orElseThrow(() -> new BeneficiaryNotFoundException(id));
    }

    /**
     * Lista ativos paginados em ordem de CPF.
     * Mesma ordenação do BATCHPGT.NSN#L182 (READ BENEFICIARIO-V BY CPF).
     */
    @Transactional(readOnly = true)
    public Page<BeneficiaryDto> findAllActive(Pageable pageable) {
        return beneficiaries.findAllActiveOrderByCpf(pageable)
                .map(BeneficiaryDto::from);
    }

    /**
     * Altera status do beneficiário (ex: suspender, cancelar).
     * Publica evento para que o módulo notification possa notificar o beneficiário.
     */
    @Transactional
    public BeneficiaryDto changeStatus(Long id, String newStatus, String reason) {
        Beneficiary b = beneficiaries.findById(id)
                .orElseThrow(() -> new BeneficiaryNotFoundException(id));
        String previousStatus = b.getStatus();
        b.changeStatus(newStatus, reason);
        Beneficiary saved = beneficiaries.save(b);
        // Publica evento para notification sem acoplamento direto
        eventPublisher.publishEvent(new BeneficiaryStatusChangedEvent(
                saved.getId(), saved.getCpf(), previousStatus, newStatus));
        return BeneficiaryDto.from(saved);
    }
}
