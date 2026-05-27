// ============================================================================
// Payment.java — bounded context: payment · domain
// ============================================================================
// Entidade transacional central. Mapeia DDM PAGAMENTO (arquivo 152, ~180M registros).
// Grupo periódico GRP-DESCONTO (PE/8) → @OneToMany(Deduction).
//
// Regras de negócio críticas capturadas:
//   - Teto de descontos não-judiciais: 30% do bruto (REQ-PAY-001 / CALCDSCT.NSN#L142)
//   - Descontos judiciais: sem teto (REQ-PAY-002)
//   - Valor líquido nunca negativo (BATCHPGT.NSN#L316)
//   - Truncar (não arredondar) para 2 casas decimais (BATCHPGT.NSN#L284)
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
//                  01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN
//                  01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN
// ============================================================================

package br.gov.client.sifap.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_cpf_period",  columnList = "cpf_beneficiary, period"),
        @Index(name = "idx_payment_status",      columnList = "status"),
        @Index(name = "idx_payment_program",     columnList = "program_code, period, status")
})
public class Payment {

    /**
     * Teto de descontos NÃO judiciais: 30% do valor bruto.
     * Rastreabilidade: CALCDSCT.NSN#L142-L148, REQ-PAY-001.
     */
    private static final BigDecimal NON_JUDICIAL_CAP = new BigDecimal("0.30");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** AA NUM-PAGAMENTO — sequencial histórico preservado para rastreabilidade. */
    @Column(name = "legacy_sequence", unique = true)
    private Long legacySequence;

    /** AB NUM-CPF — nunca expor sem mascaramento (LGPD). */
    @Column(name = "cpf_beneficiary", nullable = false, length = 11)
    private String cpfBeneficiary;

    /** AD COD-PROGRAMA */
    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    /** AE ANO-MES-REF — competência AAAAMM (ex.: 202605). */
    @Column(name = "period", nullable = false)
    private Integer period;

    /** AF NUM-CICLO */
    @Column(name = "cycle_number")
    private Integer cycleNumber;

    // ---- Valores ----
    @Column(name = "gross_amount", nullable = false, precision = 9, scale = 2)
    private BigDecimal grossAmount;  // BA VLR-BRUTO

    @Column(name = "net_amount", nullable = false, precision = 9, scale = 2)
    private BigDecimal netAmount;    // BB VLR-LIQUIDO

    @Column(name = "total_deduction", nullable = false, precision = 7, scale = 2)
    private BigDecimal totalDeduction = BigDecimal.ZERO; // BC VLR-DESCONTO-TOTAL

    // ---- Descontos (GRP-DESCONTO PE/8) ----
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deduction> deductions = new ArrayList<>();

    // ---- Status ----
    @Column(name = "status", nullable = false, length = 1)
    private String status = "P"; // DA SIT-PAGAMENTO

    @Column(name = "generated_date")
    private LocalDate generatedDate; // DB DT-GERACAO

    @Column(name = "issued_date")
    private LocalDate issuedDate;    // DD DT-EMISSAO

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate; // DE DT-CONFIRMACAO

    @Column(name = "cancelled_date")
    private LocalDate cancelledDate; // DF DT-CANCELAMENTO

    @Column(name = "cancel_reason", length = 3)
    private String cancelReason;    // DG MOT-CANCELAMENTO

    // ---- Dados bancários ----
    @Column(name = "bank_code", length = 3)
    private String bankCode;   // EA COD-BANCO

    @Column(name = "bank_return_code", length = 2)
    private String bankReturnCode; // GD COD-RETORNO-BANCO (BATCHCON)

    // ---- Controle ----
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Payment() {}

    public Payment(String cpfBeneficiary, String programCode,
                   Integer period, BigDecimal grossAmount) {
        this.cpfBeneficiary = cpfBeneficiary;
        this.programCode    = programCode;
        this.period         = period;
        this.grossAmount    = grossAmount;
        this.netAmount      = grossAmount; // ajustado ao aplicar descontos
        this.generatedDate  = LocalDate.now();
        this.status         = "G"; // G=Gerado pelo batch
    }

    /**
     * Aplica lista de deduções respeitando o teto de 30% para não-judiciais.
     * Rastreabilidade: CALCDSCT.NSN#L142-L148, REQ-PAY-001 e REQ-PAY-002.
     * Trunca (não arredonda) — padrão contábil do legado (BATCHPGT.NSN#L284).
     */
    public void applyDeductions(List<Deduction> newDeductions) {
        this.deductions.clear();
        newDeductions.forEach(d -> {
            d = new Deduction(this, d.getType(), d.getAmount());
            this.deductions.add(d);
        });

        BigDecimal judicialTotal = newDeductions.stream()
                .filter(d -> d.getType() == DeductionType.JD)
                .map(Deduction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal otherTotal = newDeductions.stream()
                .filter(d -> d.getType() != DeductionType.JD)
                .map(Deduction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cap = truncate(grossAmount.multiply(NON_JUDICIAL_CAP));
        BigDecimal cappedOther = otherTotal.min(cap); // REQ-PAY-001

        this.totalDeduction = cappedOther.add(judicialTotal); // REQ-PAY-002
        BigDecimal liquid = grossAmount.subtract(totalDeduction);
        this.netAmount = truncate(liquid.max(BigDecimal.ZERO)); // nunca negativo
        this.updatedAt = Instant.now();
    }

    /** Trunca para 2 casas decimais (sem arredondar) — padrão BATCHPGT.NSN#L284. */
    private static BigDecimal truncate(BigDecimal value) {
        return value.setScale(2, RoundingMode.DOWN);
    }

    public void markIssued(LocalDate date)          { this.status = "E"; this.issuedDate = date; this.updatedAt = Instant.now(); }
    public void markConfirmed(LocalDate date)        { this.status = "C"; this.confirmedDate = date; this.updatedAt = Instant.now(); }
    public void markReturned(String bankCode)        { this.status = "D"; this.bankReturnCode = bankCode; this.updatedAt = Instant.now(); }
    public void cancel(String reason)               { this.status = "X"; this.cancelReason = reason; this.cancelledDate = LocalDate.now(); this.updatedAt = Instant.now(); }

    public Long getId()                    { return id; }
    public Long getLegacySequence()        { return legacySequence; }
    public String getCpfBeneficiary()      { return cpfBeneficiary; }
    public String getProgramCode()         { return programCode; }
    public Integer getPeriod()             { return period; }
    public Integer getCycleNumber()        { return cycleNumber; }
    public BigDecimal getGrossAmount()     { return grossAmount; }
    public BigDecimal getNetAmount()       { return netAmount; }
    public BigDecimal getTotalDeduction()  { return totalDeduction; }
    public List<Deduction> getDeductions() { return List.copyOf(deductions); }
    public String getStatus()              { return status; }
    public LocalDate getGeneratedDate()    { return generatedDate; }
    public LocalDate getIssuedDate()       { return issuedDate; }
    public LocalDate getConfirmedDate()    { return confirmedDate; }
    public String getBankReturnCode()      { return bankReturnCode; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }

    public void setLegacySequence(Long seq) { this.legacySequence = seq; }
    public void setCycleNumber(Integer c)   { this.cycleNumber = c; }
    public void setBankCode(String bc)      { this.bankCode = bc; }
}
