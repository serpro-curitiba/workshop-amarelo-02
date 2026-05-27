// ============================================================================
// AgreementNotFoundException.java — agreement · application
// ============================================================================

package br.gov.client.sifap.agreement.application;

public class AgreementNotFoundException extends RuntimeException {
    public AgreementNotFoundException(String code) {
        super("Agreement not found: code=" + code);
    }
    public AgreementNotFoundException(Long id) {
        super("Agreement not found: id=" + id);
    }
}
