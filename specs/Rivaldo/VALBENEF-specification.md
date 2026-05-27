# SPECIFICATION - VALBENEF (Validacao Cadastral do Beneficiario)

## Metadados

- versao: 0.1.0
- data: 2026-05-27
- origem_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN
- objetivo: validar dados cadastrais do beneficiario antes de persistencia

## Requisitos EARS

### REQ-VALBENEF-001 - CPF obrigatorio e valido

```yaml
REQ-VALBENEF-001:
  pattern: event-driven
  text: "Quando os dados cadastrais do beneficiario forem informados, o sistema deve validar CPF com algoritmo modulo 11 e rejeitar cadastro com CPF invalido."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L125-L131
    - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L198-L261
  modernization_decision: EVOLUIR
  rationale: "Mantem regra principal e remove excecoes legadas de homologacao."
  acceptance:
    - "CPF com DV invalido e rejeitado."
    - "CPF com todos os digitos iguais e rejeitado."
    - "CPF valido com DV correto e aceito."
  priority: P0
  risk: CRITICO
```

### REQ-VALBENEF-002 - Data de nascimento valida

```yaml
REQ-VALBENEF-002:
  pattern: event-driven
  text: "Quando a data de nascimento for informada, o sistema deve validar ano, mes e dia em calendario valido."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L134-L141
  modernization_decision: EVOLUIR
  rationale: "Preserva validacao e permite evolucao para calendario completo com ano bissexto."
  acceptance:
    - "Ano futuro e rejeitado."
    - "Mes fora de 1..12 e rejeitado."
    - "Dia invalido para o mes e rejeitado."
  priority: P0
  risk: ALTO
```

### REQ-VALBENEF-003 - Nome e sobrenome obrigatorios

```yaml
REQ-VALBENEF-003:
  pattern: unwanted
  text: "O sistema nao deve aceitar cadastro sem nome e sobrenome do beneficiario."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L143-L148
  modernization_decision: MIGRAR
  rationale: "Regra de qualidade cadastral consolidada no legado."
  acceptance:
    - "Nome vazio e rejeitado."
    - "Nome sem separacao de dois termos e rejeitado."
    - "Nome completo e aceito."
  priority: P0
  risk: ALTO
```

### REQ-VALBENEF-004 - Dominio de UF

```yaml
REQ-VALBENEF-004:
  pattern: state-driven
  text: "Enquanto o cadastro estiver em manutencao, o sistema deve aceitar apenas UF pertencente ao dominio federativo oficial."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L150-L174
  modernization_decision: MIGRAR
  rationale: "Evita codigos de UF invalidos e inconsistencias geograficas."
  acceptance:
    - "UF fora da tabela oficial e rejeitada."
    - "UF valida e aceita."
  priority: P1
  risk: MEDIO
```

### REQ-VALBENEF-005 - Dominio de status cadastral

```yaml
REQ-VALBENEF-005:
  pattern: unwanted
  text: "O sistema nao deve aceitar status fora do conjunto A, S, C, I e D no cadastro de beneficiario."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L176-L184
  modernization_decision: MIGRAR
  rationale: "Garante consistencia de estados para modulos de elegibilidade e pagamento."
  acceptance:
    - "Status fora do dominio e rejeitado."
    - "Status no dominio e aceito."
  priority: P0
  risk: MEDIO
```

## Regra legada candidata a descarte

```yaml
LEGACY-VALBENEF-DROP-001:
  text: "Aceitar CPF iniciado com 000 como excecao de teste."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L220-L224
  modernization_decision: DESCARTAR
  rationale: "Compromete integridade de identidade civil em producao."
```

## Observacao de integracao

- A validacao de CPF deste modulo e pre-condicao para VALDOCS e VALELEG.
- Nao e permitido bypass de CPF por regra documental ou regional em modulos seguintes.
