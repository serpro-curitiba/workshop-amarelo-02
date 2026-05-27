// ============================================================================
// PaymentNotFoundException.java — payment · application
// ============================================================================

package br.gov.client.sifap.payment.application;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Payment not found: id=" + id);
    }
}
