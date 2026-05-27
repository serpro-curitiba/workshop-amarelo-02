// ============================================================================
// IncomeRange.java — bounded context: agreement · domain
// ============================================================================
// @Entity mapeando GRP-FAIXA-CALCULO (PE/5) do DDM PROGRAMA-SOCIAL.
// Faixas de cálculo que determinam o multiplicador sobre o valor base.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm
//                  Campos DA-DF (GRP-FAIXA-CALCULO PE/5)
//                  01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L153-L162
// ============================================================================

package br.gov.client.sifap.agreement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "agreement_income_range")
public class IncomeRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_id", nullable = false)
    private Agreement agreement;

    @Column(name = "income_from", nullable = false, precision = 7, scale = 2)
    private BigDecimal incomeFrom; // DB RENDA-INICIO

    @Column(name = "income_to", nullable = false, precision = 7, scale = 2)
    private BigDecimal incomeTo;   // DC RENDA-FIM

    /** Multiplicador sobre VLR-BASE (ex.: 0.85 = 85%). */
    @Column(name = "multiplier", nullable = false, precision = 3, scale = 4)
    private BigDecimal multiplier; // DD FATOR-MULTIPLICADOR

    @Column(name = "additional_amount", precision = 7, scale = 2)
    private BigDecimal additionalAmount; // DE VLR-ADICIONAL

    /** S=acumula com faixa anterior. */
    @Column(name = "cumulative", length = 1)
    private String cumulative = "N"; // DF IND-ACUMULATIVO

    protected IncomeRange() {}

    public IncomeRange(Agreement agreement, BigDecimal incomeFrom, BigDecimal incomeTo,
                       BigDecimal multiplier) {
        this.agreement    = agreement;
        this.incomeFrom   = incomeFrom;
        this.incomeTo     = incomeTo;
        this.multiplier   = multiplier;
    }

    public Long getId()                   { return id; }
    public BigDecimal getIncomeFrom()     { return incomeFrom; }
    public BigDecimal getIncomeTo()       { return incomeTo; }
    public BigDecimal getMultiplier()     { return multiplier; }
    public BigDecimal getAdditionalAmount(){ return additionalAmount; }
    public String getCumulative()         { return cumulative; }
}
