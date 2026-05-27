// ============================================================================
// Deduction.java — bounded context: payment · domain
// ============================================================================
// @Entity mapeando GRP-DESCONTO (PE/8) do DDM PAGAMENTO.
// Cada pagamento pode ter até 8 descontos de tipos distintos.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
//                  Campos CA-CG (GRP-DESCONTO PE/8)
//                  01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148
// ============================================================================

package br.gov.client.sifap.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_deduction")
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 3)
    private DeductionType type; // CB TIPO-DESCONTO

    @Column(name = "amount", nullable = false, precision = 7, scale = 2)
    private BigDecimal amount; // CC VLR-DESCONTO

    @Column(name = "percentage", precision = 3, scale = 2)
    private BigDecimal percentage; // CD PCT-DESCONTO

    /** Número do processo judicial — preenchido apenas para JD. */
    @Column(name = "legal_process_number", length = 20)
    private String legalProcessNumber; // CE NUM-PROCESSO

    @Column(name = "start_date")
    private LocalDate startDate; // CF DT-INICIO-DSCT

    /** Null = vigência indefinida. */
    @Column(name = "end_date")
    private LocalDate endDate; // CG DT-FIM-DSCT

    protected Deduction() {}

    public Deduction(Payment payment, DeductionType type, BigDecimal amount) {
        this.payment = payment;
        this.type    = type;
        this.amount  = amount;
    }

    public Long getId()                    { return id; }
    public DeductionType getType()         { return type; }
    public BigDecimal getAmount()          { return amount; }
    public BigDecimal getPercentage()      { return percentage; }
    public String getLegalProcessNumber()  { return legalProcessNumber; }
    public LocalDate getStartDate()        { return startDate; }
    public LocalDate getEndDate()          { return endDate; }

    public void setLegalProcessNumber(String n) { this.legalProcessNumber = n; }
    public void setStartDate(LocalDate d)        { this.startDate = d; }
    public void setEndDate(LocalDate d)          { this.endDate = d; }
    public void setPercentage(BigDecimal p)      { this.percentage = p; }
}
