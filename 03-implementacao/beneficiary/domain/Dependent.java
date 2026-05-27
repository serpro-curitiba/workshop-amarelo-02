// ============================================================================
// Dependent.java — bounded context: beneficiary · domain
// ============================================================================
// @Entity mapeando o grupo periódico GRP-DEPENDENTE (PE, max 10 ocorrências).
// Em Adabas era PE group; em JPA vira @OneToMany com tabela filha.
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
//                  Campos DA-DG (GRP-DEPENDENTE, PE/10)
//                  01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN
// ============================================================================

package br.gov.client.sifap.beneficiary.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "beneficiary_dependent")
public class Dependent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK para o beneficiário titular. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    /**
     * CPF do dependente — pode ser nulo para menores sem CPF.
     * GREENFIELD: legado aceitava "00000000000" (LEGACY-DEP-DROP-001 — descartado).
     */
    @Column(name = "cpf", length = 11)
    private String cpf;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Parentesco: FI=Filho, CJ=Cônjuge, NT=Neto, TU=Tutelado.
     * Rastreabilidade: BENEFICIARIO.ddm campo DE PARENTESCO (A2).
     */
    @Column(name = "relationship", nullable = false, length = 2)
    private String relationship;

    @Column(name = "status", nullable = false, length = 1)
    private String status = "A"; // A=ATV I=INAT D=DESL

    /** S/N — indica necessidades especiais. */
    @Column(name = "has_disability", nullable = false, length = 1)
    private String hasDisability = "N";

    protected Dependent() {}

    public Dependent(Beneficiary beneficiary, String cpf, String name,
                     LocalDate birthDate, String relationship) {
        this.beneficiary = beneficiary;
        this.cpf = cpf;
        this.name = name;
        this.birthDate = birthDate;
        this.relationship = relationship;
    }

    public Long getId()             { return id; }
    public String getCpf()          { return cpf; }
    public String getName()         { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getRelationship() { return relationship; }
    public String getStatus()       { return status; }
    public String getHasDisability(){ return hasDisability; }

    public void deactivate() { this.status = "I"; }
}
