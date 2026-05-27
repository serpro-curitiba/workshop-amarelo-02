# SPECIFICATION — Cadastro de Dependentes (CADDEPEND)

## Metadados

- Versao da spec: 0.1.0
- Origem principal: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN
- Escopo: inclusao de dependentes vinculados ao beneficiario titular

## Requisitos EARS (Top 5 impacto no beneficiario)

### REQ-DEP-001 · Vinculo obrigatorio a titular existente

```yaml
REQ-DEP-001:
  pattern: ubiquitous
  text: "O SIFAP deve permitir inclusao de dependente somente quando o CPF do titular existir na base de beneficiarios."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L40-L52
  modernization_decision: MIGRAR
  rationale: "Regra de elegibilidade estrutural para evitar vinculos orfaos."
  acceptance:
    - "CPF titular existente permite seguir para a captura de dados do dependente."
    - "CPF titular inexistente encerra fluxo com mensagem de beneficiario nao encontrado."
  priority: P0
  risk: CRITICO
```

### REQ-DEP-002 · Bloqueio por status do titular

```yaml
REQ-DEP-002:
  pattern: state-driven
  text: "Enquanto o titular estiver com status CANCELADO ou DESLIGADO, o SIFAP nao deve permitir inclusao de dependentes."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L54-L58
  modernization_decision: MIGRAR
  rationale: "Regra central de concessao e manutencao de direitos por status do titular."
  acceptance:
    - "Titular com status C ou D recebe bloqueio de inclusao."
    - "Titular com status ativo pode iniciar inclusao."
  priority: P0
  risk: CRITICO
```

### REQ-DEP-003 · Limite de quantidade de dependentes

```yaml
REQ-DEP-003:
  pattern: unwanted
  text: "O SIFAP nao deve permitir inclusao quando a quantidade de dependentes do titular atingir o limite configurado pela politica vigente."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L62-L65
  modernization_decision: EVOLUIR
  rationale: "No legado o limite esta fixo em 5; no moderno deve ser parametrizavel por regra de negocio, produto ou periodo."
  acceptance:
    - "Ao atingir o limite configurado, nova inclusao e bloqueada."
    - "Alteracao de parametro de limite passa a valer sem alteracao de codigo."
  priority: P0
  risk: ALTO
```

### REQ-DEP-004 · Validacao minima de dados e parentesco

```yaml
REQ-DEP-004:
  pattern: event-driven
  text: "Quando dados de dependente forem informados, o SIFAP deve exigir nome preenchido e validar parentesco contra tabela de dominio autorizada."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L79-L91
  modernization_decision: EVOLUIR
  rationale: "Valida a qualidade de cadastro, mas codigos fixos (FI/CO/IR/OU) devem migrar para dominio governado e extensivel."
  acceptance:
    - "Nome em branco impede inclusao."
    - "Parentesco fora da tabela autorizada impede inclusao."
    - "Inclusao com parentesco valido e nome preenchido prossegue."
  priority: P0
  risk: ALTO
```

### REQ-DEP-005 · Duplicidade de dependente por CPF

```yaml
REQ-DEP-005:
  pattern: unwanted
  text: "O SIFAP nao deve permitir o cadastro duplicado de dependente para o mesmo titular quando houver CPF informado."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L98-L107
  modernization_decision: EVOLUIR
  rationale: "A regra atual evita duplicidade, mas a excecao de CPF=0 e fragil e deve ser substituida por politica clara para identificacao incompleta."
  acceptance:
    - "Mesmo CPF para o mesmo titular e rejeitado na segunda tentativa."
    - "Ausencia de CPF segue politica explicita de identificacao alternativa e auditoria."
  priority: P0
  risk: CRITICO
```

## Regra legada candidata a descarte

```yaml
LEGACY-DEP-DROP-001:
  text: "Aceitar CPF do dependente igual a 0 como caminho normal de cadastro."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L100
  modernization_decision: DESCARTAR
  rationale: "Gera ambiguidade de identidade e risco de inconsistencias/duplicidades no ciclo de beneficios."
```

## Resumo de decisao

- MIGRAR: REQ-DEP-001, REQ-DEP-002
- EVOLUIR: REQ-DEP-003, REQ-DEP-004, REQ-DEP-005
- DESCARTAR: LEGACY-DEP-DROP-001
