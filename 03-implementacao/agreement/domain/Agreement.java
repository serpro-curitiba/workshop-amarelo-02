// ============================================================================
// Agreement.java — bounded context: agreement · domain
// ============================================================================
// Entidade raiz: representa um Programa Social do SIFAP.
// "agreement" = nome moderno para PROGRAMA-SOCIAL (contrato de benefício).
//
// Notas de mapeamento:
//   - GRP-FAIXA-CALCULO (PE/5) → @OneToMany(IncomeRange)
//   - TIPO-DSCT-APLIC (MU/8)   → @ElementCollection com enum DiscountType
//   - FATOR-K (BG) → campo undocumented! Preservado como bigDecimal com comentário.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm
//                  01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
// ============================================================================

package br.gov.client.sifap.agreement.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agreement", indexes = {
        @Index(name = "idx_agreement_code",   columnList = "code",   unique = true),
        @Index(name = "idx_agreement_status", columnList = "status")
})
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COD-PROGRAMA — chave natural de 4 caracteres (ex.: "1001"). */
    @Column(name = "code", nullable = false, unique = true, length = 4)
    private String code;

    @Column(name = "name", nullable = false, length = 60)
    private String name; // AB NOME-PROGRAMA

    @Column(name = "acronym", length = 10)
    private String acronym; // AC SIGLA-PROGRAMA (ex: PBF, BPC, PETI)

    /**
     * Tipo: A=Assistencial T=Trabalho P=Previdência.
     * Rastreabilidade: campo AD TIPO-PROGRAMA.
     */
    @Column(name = "type", nullable = false, length = 1)
    private String type;

    @Column(name = "governing_body", length = 10)
    private String governingBody; // AE ORGAO-RESPONSAVEL

    @Column(name = "legal_basis", length = 20)
    private String legalBasis; // AF LEI-CRIACAO

    @Column(name = "created_date")
    private LocalDate createdDate; // AG DT-CRIACAO

    @Column(name = "closed_date")
    private LocalDate closedDate;  // AH DT-ENCERRAMENTO (null = vigente)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private AgreementStatus status = AgreementStatus.ACTIVE;

    // ---- Valores base ----
    @Column(name = "base_value_individual", precision = 7, scale = 2)
    private BigDecimal baseValueIndividual; // BA VLR-BASE-INDIVIDUAL

    @Column(name = "base_value_family", precision = 7, scale = 2)
    private BigDecimal baseValueFamily; // BB VLR-BASE-FAMILIAR

    @Column(name = "benefit_ceiling", precision = 9, scale = 2)
    private BigDecimal benefitCeiling;  // BC VLR-TETO-BENEF

    @Column(name = "benefit_floor", precision = 7, scale = 2)
    private BigDecimal benefitFloor;    // BD VLR-PISO-BENEF

    @Column(name = "annual_adjustment_pct", precision = 3, scale = 2)
    private BigDecimal annualAdjustmentPct; // BE PCT-REAJUSTE-ANUAL

    @Column(name = "last_adjustment_date")
    private LocalDate lastAdjustmentDate; // BF DT-ULT-REAJUSTE

    /**
     * FATOR-K — campo não documentado inserido em ago/2008 por Adilson Batista.
     * "Atende solicitação SENARC" — sem mais detalhes.
     * Mystery preservado: não aplicar sem entender. Ver mysteries-found.md.
     * Rastreabilidade: PROGRAMA-SOCIAL.ddm campo BG FATOR-K (N5.4).
     */
    @Column(name = "factor_k", precision = 5, scale = 4)
    private BigDecimal factorK;

    // ---- Elegibilidade ----
    @Column(name = "max_income_per_capita", precision = 7, scale = 2)
    private BigDecimal maxIncomePerCapita; // CA RENDA-MAX-PERCAP

    @Column(name = "min_age")
    private Integer minAge; // CB IDADE-MIN (0=sem)

    @Column(name = "max_age")
    private Integer maxAge; // CC IDADE-MAX (0=sem)

    @Column(name = "requires_children", length = 1)
    private String requiresChildren = "N"; // CD IND-EXIGE-FILHOS

    @Column(name = "min_children")
    private Integer minChildren; // CE QTD-MIN-FILHOS

    @Column(name = "requires_school", length = 1)
    private String requiresSchool = "N"; // CF IND-EXIGE-ESCOLA

    // ---- Faixas de cálculo (GRP-FAIXA-CALCULO PE/5) ----
    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("incomeFrom ASC")
    private List<IncomeRange> incomeRanges = new ArrayList<>();

    // ---- Tipos de desconto aplicáveis (MU/8) ----
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "agreement_discount_type",
            joinColumns = @JoinColumn(name = "agreement_id"))
    @Column(name = "discount_type")
    private List<DiscountType> applicableDiscountTypes = new ArrayList<>();

    // ---- Controle ----
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Agreement() {}

    public Agreement(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public void addIncomeRange(IncomeRange range)         { incomeRanges.add(range); }
    public void addDiscountType(DiscountType dt)          { applicableDiscountTypes.add(dt); }
    public void deactivate()                              { this.status = AgreementStatus.INACTIVE; updatedAt = Instant.now(); }
    public void close(LocalDate closeDate)                { this.status = AgreementStatus.CLOSED; this.closedDate = closeDate; updatedAt = Instant.now(); }

    public Long getId()                       { return id; }
    public String getCode()                   { return code; }
    public String getName()                   { return name; }
    public String getAcronym()                { return acronym; }
    public String getType()                   { return type; }
    public AgreementStatus getStatus()        { return status; }
    public BigDecimal getBaseValueIndividual(){ return baseValueIndividual; }
    public BigDecimal getBaseValueFamily()    { return baseValueFamily; }
    public BigDecimal getBenefitCeiling()     { return benefitCeiling; }
    public BigDecimal getBenefitFloor()       { return benefitFloor; }
    public BigDecimal getAnnualAdjustmentPct(){ return annualAdjustmentPct; }
    public BigDecimal getFactorK()            { return factorK; }
    public BigDecimal getMaxIncomePerCapita() { return maxIncomePerCapita; }
    public Integer getMinAge()                { return minAge; }
    public Integer getMaxAge()                { return maxAge; }
    public List<IncomeRange> getIncomeRanges(){ return List.copyOf(incomeRanges); }
    public List<DiscountType> getApplicableDiscountTypes() { return List.copyOf(applicableDiscountTypes); }
    public Instant getCreatedAt()             { return createdAt; }
    public Instant getUpdatedAt()             { return updatedAt; }

    public void setAcronym(String a)                       { this.acronym = a; }
    public void setBaseValueIndividual(BigDecimal v)        { this.baseValueIndividual = v; updatedAt = Instant.now(); }
    public void setBaseValueFamily(BigDecimal v)            { this.baseValueFamily = v; updatedAt = Instant.now(); }
    public void setBenefitCeiling(BigDecimal v)             { this.benefitCeiling = v; updatedAt = Instant.now(); }
    public void setBenefitFloor(BigDecimal v)               { this.benefitFloor = v; updatedAt = Instant.now(); }
    public void setAnnualAdjustmentPct(BigDecimal v)        { this.annualAdjustmentPct = v; updatedAt = Instant.now(); }
    public void setMaxIncomePerCapita(BigDecimal v)         { this.maxIncomePerCapita = v; }
    public void setMinAge(Integer v)                        { this.minAge = v; }
    public void setMaxAge(Integer v)                        { this.maxAge = v; }
    public void setLegalBasis(String v)                     { this.legalBasis = v; }
    public void setGoverningBody(String v)                  { this.governingBody = v; }
    public void setCreatedDate(LocalDate v)                 { this.createdDate = v; }
}
