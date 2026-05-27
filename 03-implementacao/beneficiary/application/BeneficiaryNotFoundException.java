// ============================================================================
// BeneficiaryNotFoundException.java — bounded context: beneficiary · application
// ============================================================================

package br.gov.client.sifap.beneficiary.application;

public class BeneficiaryNotFoundException extends RuntimeException {
    public BeneficiaryNotFoundException(String cpf) {
        super("Beneficiary not found: cpf=" + cpf);
    }
    public BeneficiaryNotFoundException(Long id) {
        super("Beneficiary not found: id=" + id);
    }
}
