// ============================================================================
// DiscountType.java — bounded context: agreement · domain
// ============================================================================
// Tipos de desconto aplicáveis definidos por programa (campo MU TIPO-DSCT-APLIC).
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm
//                  Campo EA TIPO-DSCT-APLIC (MU/8)
//                  01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
//                  Campo CB TIPO-DESCONTO (A3)
// ============================================================================

package br.gov.client.sifap.agreement.domain;

public enum DiscountType {
    IR,   // Imposto de Renda Retido na Fonte
    JD,   // Judicial
    CS,   // Consignado
    PA,   // Pensão Alimentícia
    EM,   // Empréstimo
    TX,   // Taxa
    OU,   // Outros
    EX    // Extraordinário
}
