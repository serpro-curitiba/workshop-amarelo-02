# SPECIFICATION - VALDOCS (Validacao Documental do Beneficiario)

## Metadados

- versao: 0.1.0
- data: 2026-05-27
- origem_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN
- objetivo: validar consistencia de documentos obrigatorios do beneficiario

## Requisitos EARS

### REQ-VALDOCS-001 - CPF documental obrigatorio e valido

```yaml
REQ-VALDOCS-001:
  pattern: event-driven
  text: "Quando a validacao documental for executada, o sistema deve validar CPF por modulo 11 e rejeitar fluxo documental com CPF invalido."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L72-L78
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L105-L147
  modernization_decision: EVOLUIR
  rationale: "Alinha validacao documental ao mesmo criterio de identidade cadastral."
  acceptance:
    - "CPF igual a zero e rejeitado."
    - "CPF com DV invalido e rejeitado."
    - "CPF valido permite continuidade da validacao documental."
  priority: P0
  risk: CRITICO
```

### REQ-VALDOCS-002 - RG obrigatorio com tamanho minimo

```yaml
REQ-VALDOCS-002:
  pattern: event-driven
  text: "Quando RG for informado, o sistema deve exigir conteudo nao vazio e no minimo 5 caracteres uteis."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L80-L86
  modernization_decision: MIGRAR
  rationale: "Regra minima de qualidade documental ja presente no legado."
  acceptance:
    - "RG vazio e rejeitado."
    - "RG menor que 5 caracteres uteis e rejeitado."
    - "RG valido atende ao criterio minimo e e aceito."
  priority: P1
  risk: MEDIO
```

### REQ-VALDOCS-003 - Documento especial nao sobrescreve falhas

```yaml
REQ-VALDOCS-003:
  pattern: unwanted
  text: "O sistema nao deve sobrescrever resultado invalido de CPF nem limpar erros acumulados por conta de prefixo especial de documento."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L169-L188
  modernization_decision: EVOLUIR
  rationale: "Elimina bypass de controle e preserva rastreabilidade de erro."
  acceptance:
    - "Prefixo especial nao transforma CPF invalido em valido."
    - "Quantidade de erros acumulados nao e zerada por regra especial."
    - "Resultado final respeita validacoes obrigatorias anteriores."
  priority: P0
  risk: CRITICO
```

### REQ-VALDOCS-004 - Flag DOCUMENTOS-OK confiavel para elegibilidade

```yaml
REQ-VALDOCS-004:
  pattern: state-driven
  text: "Enquanto a concessao depender de documentacao, o sistema deve expor DOCUMENTOS-OK confiavel e coerente com as validacoes de CPF e RG."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L88-L98
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L100-L147
  modernization_decision: EVOLUIR
  rationale: "Evita concessao indevida em VALELEG por estado documental inconsistente."
  acceptance:
    - "Falha em CPF implica DOCUMENTOS-OK negativo."
    - "Falha em RG implica DOCUMENTOS-OK negativo."
    - "Somente validacao documental integral define DOCUMENTOS-OK positivo."
  priority: P0
  risk: ALTO
```

## Regra legada candidata a descarte

```yaml
LEGACY-VALDOCS-DROP-001:
  text: "Usar lista de prefixos especiais para aprovar documento e limpar erros de validacao."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L169-L188
  modernization_decision: DESCARTAR
  rationale: "Bypass de seguranca e perda de rastreabilidade de inconsistencias."
```

## Observacao de integracao

- Este modulo deve consumir CPF ja validado por VALBENEF.
- A saida documental alimenta VALELEG e nao pode ser alterada por excecoes nao auditaveis.
