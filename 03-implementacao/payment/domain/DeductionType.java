// ============================================================================
// DeductionType.java — bounded context: payment · domain
// ============================================================================
// Compartilhado com agreement.domain.DiscountType — mantido como cópia local
// para evitar acoplamento entre contextos (cada módulo é autônomo).
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
//                  Campo CB TIPO-DESCONTO (A3)
//                  01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN
// ============================================================================

package br.gov.client.sifap.payment.domain;

public enum DeductionType {
    IR,   // Imposto de Renda Retido na Fonte
    JD,   // Judicial (sem teto — REQ-PAY-002)
    CS,   // Consignado
    PA,   // Pensão Alimentícia
    EM,   // Empréstimo
    TX,   // Taxa administrativa
    OU,   // Outros
    EX    // Extraordinário
}
