// ============================================================================
// PaymentStatus.java — bounded context: payment · domain
// ============================================================================
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
//                  Campo DA SIT-PAGAMENTO (A1)
//                  P=PEND G=GERADO E=EMITIDO C=CONFIRMADO D=DEVOLVIDO
//                  X=CANCELADO R=REPROCESSADO
// ============================================================================

package br.gov.client.sifap.payment.domain;

public sealed interface PaymentStatus permits
        PaymentStatus.Pending,
        PaymentStatus.Generated,
        PaymentStatus.Issued,
        PaymentStatus.Confirmed,
        PaymentStatus.Returned,
        PaymentStatus.Cancelled,
        PaymentStatus.Reprocessed {

    record Pending()                      implements PaymentStatus {} // P
    record Generated()                    implements PaymentStatus {} // G — saída do BATCHPGT
    record Issued()                       implements PaymentStatus {} // E — enviado ao banco
    record Confirmed()                    implements PaymentStatus {} // C — retorno CNAB 00
    record Returned(String bankCode)      implements PaymentStatus {} // D — devolvido
    record Cancelled(String reason)       implements PaymentStatus {} // X
    record Reprocessed()                  implements PaymentStatus {} // R

    default String legacyCode() {
        return switch (this) {
            case Pending ignored      -> "P";
            case Generated ignored    -> "G";
            case Issued ignored       -> "E";
            case Confirmed ignored    -> "C";
            case Returned ignored     -> "D";
            case Cancelled ignored    -> "X";
            case Reprocessed ignored  -> "R";
        };
    }
}
