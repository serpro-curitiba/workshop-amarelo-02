// ============================================================================
// Beneficiary.java — bounded context: beneficiary · domain
// ============================================================================
// Entidade raiz do contexto. Mapeia DDM BENEFICIARIO (arquivo 150, ~4.2M registros).
// Grupo periódico GRP-DEPENDENTE → @OneToMany(Dependent).
// Grupo de endereço GRP-ENDERECO → @Embedded(Address).
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
//                  01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN
//
// Migração de banco: V3__init_beneficiary_module.sql
// ============================================================================

package br.gov.client.sifap.beneficiary.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "beneficiary", indexes = {
        @Index(name = "idx_beneficiary_cpf",    columnList = "cpf",    unique = true),
        @Index(name = "idx_beneficiary_status",  columnList = "status"),
        @Index(name = "idx_beneficiary_program", columnList = "program_code, status")
})
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CPF sem formatação — 11 dígitos.
     * Nunca expor cru em logs — mascarar antes (LGPD).
     * Rastreabilidade: BENEFICIARIO.ddm campo AB NUM-CPF (DE).
     */
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "registration_number", length = 11)
    private String registrationNumber; // NUM-INSCRICAO / NIS

    @Column(name = "full_name", nullable = false, length = 60)
    private String fullName;

    @Column(name = "mother_name", nullable = false, length = 60)
    private String motherName;

    @Column(name = "father_name", length = 60)
    private String fatherName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** M/F/I (I=Indefinido). */
    @Column(name = "gender", length = 1)
    private String gender;

    /** S=Solteiro C=Casado D=Divorciado V=Viúvo U=União Estável. */
    @Column(name = "marital_status", length = 1)
    private String maritalStatus;

    // ---- Endereço (GRP-ENDERECO → @Embedded) ----
    @Embedded
    private Address address;

    // ---- Dados do Benefício ----
    /** Código do programa social (FK → agreement). */
    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Column(name = "benefit_start_date")
    private LocalDate benefitStartDate;

    /** Null = sem prazo. */
    @Column(name = "benefit_end_date")
    private LocalDate benefitEndDate;

    /**
     * Status: A=Ativo S=Suspenso C=Cancelado I=Inativo D=Desligado.
     * Domain usa BeneficiaryStatus (sealed); persistência usa código legado.
     */
    @Column(name = "status", nullable = false, length = 1)
    private String status = "A";

    @Column(name = "status_reason", length = 3)
    private String statusReason;

    @Column(name = "status_changed_date")
    private LocalDate statusChangedDate;

    /** Renda familiar declarada — N9.2 no Adabas. */
    @Column(name = "family_income", precision = 9, scale = 2)
    private BigDecimal familyIncome;

    @Column(name = "family_size")
    private Integer familySize;

    /** Renda per capita calculada — N7.2. */
    @Column(name = "income_per_capita", precision = 7, scale = 2)
    private BigDecimal incomePerCapita;

    // ---- Contato (adicionado 2015) ----
    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", length = 80)
    private String email;

    // ---- Dependentes (PE group → @OneToMany) ----
    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dependent> dependents = new ArrayList<>();

    // ---- Controle ----
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Beneficiary() {}

    public Beneficiary(String cpf, String fullName, String motherName,
                       LocalDate birthDate, String programCode) {
        this.cpf = cpf;
        this.fullName = fullName;
        this.motherName = motherName;
        this.birthDate = birthDate;
        this.programCode = programCode;
    }

    /** Altera status e registra data da mudança. */
    public void changeStatus(String newStatus, String reason) {
        this.status = newStatus;
        this.statusReason = reason;
        this.statusChangedDate = LocalDate.now();
        this.updatedAt = Instant.now();
    }

    public void addDependent(Dependent dependent) {
        dependents.add(dependent);
    }

    // ---- Getters ----
    public Long getId()                 { return id; }
    public String getCpf()              { return cpf; }
    public String getRegistrationNumber(){ return registrationNumber; }
    public String getFullName()         { return fullName; }
    public String getMotherName()       { return motherName; }
    public LocalDate getBirthDate()     { return birthDate; }
    public String getGender()           { return gender; }
    public String getMaritalStatus()    { return maritalStatus; }
    public Address getAddress()         { return address; }
    public String getProgramCode()      { return programCode; }
    public LocalDate getBenefitStartDate() { return benefitStartDate; }
    public LocalDate getBenefitEndDate()   { return benefitEndDate; }
    public String getStatus()           { return status; }
    public String getStatusReason()     { return statusReason; }
    public BigDecimal getFamilyIncome() { return familyIncome; }
    public Integer getFamilySize()      { return familySize; }
    public String getPhone()            { return phone; }
    public String getEmail()            { return email; }
    public List<Dependent> getDependents() { return List.copyOf(dependents); }
    public Instant getCreatedAt()       { return createdAt; }
    public Instant getUpdatedAt()       { return updatedAt; }

    // ---- Setters necessários para atualização ----
    public void setAddress(Address address)          { this.address = address; this.updatedAt = Instant.now(); }
    public void setFamilyIncome(BigDecimal income)   { this.familyIncome = income; this.updatedAt = Instant.now(); }
    public void setFamilySize(Integer size)          { this.familySize = size; this.updatedAt = Instant.now(); }
    public void setEmail(String email)               { this.email = email; this.updatedAt = Instant.now(); }
    public void setPhone(String phone)               { this.phone = phone; this.updatedAt = Instant.now(); }
    public void setRegistrationNumber(String nis)    { this.registrationNumber = nis; }
    public void setBenefitStartDate(LocalDate d)     { this.benefitStartDate = d; }
    public void setBenefitEndDate(LocalDate d)       { this.benefitEndDate = d; }
}
