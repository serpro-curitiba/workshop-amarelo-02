// ============================================================================
// BeneficiaryStatus.java — bounded context: beneficiary · domain
// ============================================================================
// Sealed interface representando o ciclo de vida do beneficiário.
// Códigos legados: A=ATV S=SUSP C=CANC I=INAT D=DESL
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
//                  Campo CE SIT-BENEFICIARIO (A1)
// ============================================================================

package br.gov.client.sifap.beneficiary.domain;

public sealed interface BeneficiaryStatus permits
        BeneficiaryStatus.Active,
        BeneficiaryStatus.Suspended,
        BeneficiaryStatus.Cancelled,
        BeneficiaryStatus.Inactive,
        BeneficiaryStatus.Dismissed {

    /** A=ATV — beneficiário ativo, apto a receber pagamentos. */
    record Active() implements BeneficiaryStatus {}

    /** S=SUSP — benefício suspenso temporariamente. */
    record Suspended(String reason) implements BeneficiaryStatus {}

    /** C=CANC — benefício cancelado definitivamente. */
    record Cancelled(String reason) implements BeneficiaryStatus {}

    /** I=INAT — inativo (ex.: óbito não confirmado). */
    record Inactive() implements BeneficiaryStatus {}

    /** D=DESL — desligado do programa. */
    record Dismissed(String reason) implements BeneficiaryStatus {}

    /** Código de 1 caractere usado na persistência e integração legada. */
    default String legacyCode() {
        return switch (this) {
            case Active ignored         -> "A";
            case Suspended ignored      -> "S";
            case Cancelled ignored      -> "C";
            case Inactive ignored       -> "I";
            case Dismissed ignored      -> "D";
        };
    }
}
