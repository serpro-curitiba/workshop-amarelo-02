// ============================================================================
// AgreementStatus.java — bounded context: agreement · domain
// ============================================================================
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm
//                  Campo AI SIT-PROGRAMA (A1): A=ATV I=INAT E=ENCERR
// ============================================================================

package br.gov.client.sifap.agreement.domain;

public enum AgreementStatus {
    /** A=ATV — programa em vigência, aceita novos beneficiários. */
    ACTIVE,
    /** I=INAT — inativo temporariamente. */
    INACTIVE,
    /** E=ENCERR — encerrado definitivamente. */
    CLOSED;

    public String legacyCode() {
        return switch (this) {
            case ACTIVE   -> "A";
            case INACTIVE -> "I";
            case CLOSED   -> "E";
        };
    }

    public static AgreementStatus fromLegacyCode(String code) {
        return switch (code) {
            case "A" -> ACTIVE;
            case "I" -> INACTIVE;
            case "E" -> CLOSED;
            default  -> throw new IllegalArgumentException("Unknown status code: " + code);
        };
    }
}
