// ============================================================================
// Address.java — bounded context: beneficiary · domain
// ============================================================================
// @Embeddable mapeando o grupo GRP-ENDERECO do DDM BENEFICIARIO.
// Persistido inline na tabela beneficiary (sem tabela própria).
//
// Rastreabilidade:
//   source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
//                  Campos BA-BJ (GRP-ENDERECO)
// ============================================================================

package br.gov.client.sifap.beneficiary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    @Column(name = "logradouro", length = 60)
    private String street;

    @Column(name = "numero", length = 10)
    private String number;

    @Column(name = "complemento", length = 30)
    private String complement;

    @Column(name = "bairro", length = 40)
    private String neighborhood;

    @Column(name = "municipio", length = 40)
    private String city;

    @Column(name = "uf", length = 2)
    private String state;

    /** CEP sem hífen — 8 dígitos. */
    @Column(name = "cep", length = 8)
    private String zipCode;

    @Column(name = "cod_ibge")
    private Integer ibgeCode;

    /** Código de região 01-05 ou 99 (especial). */
    @Column(name = "cod_regiao", length = 2)
    private String regionCode;

    protected Address() {}

    public Address(String street, String number, String complement, String neighborhood,
                   String city, String state, String zipCode, Integer ibgeCode, String regionCode) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.ibgeCode = ibgeCode;
        this.regionCode = regionCode;
    }

    public String getStreet()       { return street; }
    public String getNumber()       { return number; }
    public String getComplement()   { return complement; }
    public String getNeighborhood() { return neighborhood; }
    public String getCity()         { return city; }
    public String getState()        { return state; }
    public String getZipCode()      { return zipCode; }
    public Integer getIbgeCode()    { return ibgeCode; }
    public String getRegionCode()   { return regionCode; }
}
