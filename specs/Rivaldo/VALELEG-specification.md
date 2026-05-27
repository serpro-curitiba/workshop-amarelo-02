# SPECIFICATION - VALELEG (Validacao de Elegibilidade em Programa Social)

## Metadados

- versao: 0.1.0
- data: 2026-05-27
- origem_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN
- objetivo: validar elegibilidade do beneficiario conforme regras de programa social

## Requisitos EARS

### REQ-VALELEG-001 - Existencia de beneficiario e programa ativo

```yaml
REQ-VALELEG-001:
  pattern: unwanted
  text: "O sistema nao deve avaliar elegibilidade quando beneficiario ou programa nao existir, nem quando programa estiver inativo."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L72-L90
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L92-L107
  modernization_decision: MIGRAR
  rationale: "Pre-condicao obrigatoria para qualquer decisao de concessao."
  acceptance:
    - "Beneficiario nao encontrado encerra fluxo."
    - "Programa nao encontrado encerra fluxo."
    - "Programa inativo encerra fluxo."
  priority: P0
  risk: CRITICO
```

### REQ-VALELEG-002 - Status do beneficiario deve ser ativo

```yaml
REQ-VALELEG-002:
  pattern: state-driven
  text: "Enquanto o beneficiario estiver com status diferente de A, o sistema deve marcar como nao elegivel e registrar motivo."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L118-L136
  modernization_decision: MIGRAR
  rationale: "Preserva governanca de concessao por estado cadastral."
  acceptance:
    - "Status S bloqueia elegibilidade com motivo."
    - "Status C ou D bloqueia elegibilidade com motivo."
    - "Status I bloqueia elegibilidade com motivo."
  priority: P0
  risk: CRITICO
```

### REQ-VALELEG-003 - Faixa etaria e renda parametrizadas

```yaml
REQ-VALELEG-003:
  pattern: complex
  text: "Enquanto os parametros do programa estiverem vigentes, quando idade e renda forem avaliadas, o sistema deve aplicar IDADE-MIN, IDADE-MAX e RENDA-MAX para decisao de elegibilidade."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L139-L158
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L160-L168
  modernization_decision: EVOLUIR
  rationale: "Mantem regra parametrica e corrige calculo de idade para data completa."
  acceptance:
    - "Idade abaixo do minimo bloqueia elegibilidade."
    - "Idade acima do maximo bloqueia elegibilidade."
    - "Renda acima do teto bloqueia elegibilidade."
  priority: P0
  risk: ALTO
```

### REQ-VALELEG-004 - Regras por tipo de programa

```yaml
REQ-VALELEG-004:
  pattern: event-driven
  text: "Quando o tipo de programa for A, P ou T, o sistema deve aplicar regra especifica de elegibilidade e rejeitar tipo desconhecido."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L170-L206
  modernization_decision: MIGRAR
  rationale: "Mantem semantica de politicas por categoria de programa."
  acceptance:
    - "Tipo A aplica regras assistenciais e documentais."
    - "Tipo P exige idade minima previdenciaria."
    - "Tipo T exige faixa etaria de trabalho."
    - "Tipo desconhecido bloqueia elegibilidade."
  priority: P0
  risk: ALTO
```

### REQ-VALELEG-005 - Elegibilidade especifica por codigo

```yaml
REQ-VALELEG-005:
  pattern: state-driven
  text: "Enquanto codigo de elegibilidade especifica estiver ativo, o sistema deve exigir NIS para marcador R e dependentes para marcador D."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L208-L247
  modernization_decision: MIGRAR
  rationale: "Preserva regras especificas de negocio por programa."
  acceptance:
    - "Marcador R com NIS ausente bloqueia elegibilidade."
    - "Marcador D sem dependentes bloqueia elegibilidade."
    - "Com exigencias atendidas, regra especifica nao bloqueia."
  priority: P1
  risk: ALTO
```

### REQ-VALELEG-006 - Regiao 99 sem bypass de controles basicos

```yaml
REQ-VALELEG-006:
  pattern: optional
  text: "Onde o beneficiario pertencer a regiao 99, o sistema deve aplicar flexibilizacao apenas nos criterios socioeconomicos previstos em politica, sem dispensar identidade valida, status ativo e documentacao obrigatoria."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L109-L116
  modernization_decision: EVOLUIR
  rationale: "Remove aprovacao automatica sem controles essenciais de concessao."
  acceptance:
    - "Regiao 99 nao aprova beneficiario com status diferente de A."
    - "Regiao 99 nao aprova beneficiario sem documentacao obrigatoria."
    - "Flexibilizacao ocorre apenas onde politica explicita permitir."
  priority: P0
  risk: CRITICO
```

## Regra legada candidata a descarte

```yaml
LEGACY-VALELEG-DROP-001:
  text: "Aprovar automaticamente elegibilidade para COD-REGIAO=99 com retorno imediato."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L109-L116
  modernization_decision: DESCARTAR
  rationale: "Bypass de validacoes obrigatorias e risco de concessao indevida."
```

## Observacao de integracao

- Este modulo depende de identidade valida de VALBENEF e consistencia documental de VALDOCS.
- Nao deve existir fluxo de elegibilidade que bypassa falhas desses modulos anteriores.
