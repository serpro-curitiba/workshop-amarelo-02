# SPECIFICATION - Validacao Cadastral, Documental e Elegibilidade

## Metadados

- versao: 0.1.0
- data: 2026-05-27
- origem_legacy:
  - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN
  - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN
  - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN
- objetivo: consolidar regras de validacao de beneficiario antes de concessao em programa social

## Alinhamento com Constitution e Regras do Projeto

Como a constitution em .specify/memory/constitution.md esta em formato template (placeholders), esta especificacao aplica as definicoes normativas vigentes no repositorio:

- requisitos em notacao EARS
- obrigatoriedade de source_legacy em todos os requisitos
- rastreabilidade explicita para regras legadas e para descarte/evolucao
- priorizacao de seguranca e consistencia de dados em regras de identidade e elegibilidade

## Inconsistencias detectadas e ajuste aplicado

1. CPF com bypass no legado
- conflito: VALBENEF aceita excecao para CPFs iniciados em 000 (comentario de teste) e VALDOCS invalida CPF=0 mas pode sobrescrever erro por prefixo especial.
- ajuste: unificar validacao CPF por modulo 11 sem bypass de prefixo e sem reset de erros.

2. Prefixos especiais em VALDOCS
- conflito: CHECK-DOC-ESPECIAL pode marcar documento valido mesmo quando CPF falha e ainda limpar erros acumulados.
- ajuste: remover efeito de override; prefixo especial nao substitui validacao obrigatoria de identidade.

3. Regiao 99 em VALELEG
- conflito: regra retorna elegivel imediatamente e ignora validacoes posteriores de status/documentacao/restricoes do programa.
- ajuste: regiao 99 apenas dispensa filtros socioeconomicos configurados, mas nao dispensa status ativo, identidade valida e documentacao obrigatoria.

4. Calculo de idade simplificado
- conflito: idade calculada apenas por ano (ano atual - ano nascimento), gerando falso positivo/negativo em fronteiras de aniversario.
- ajuste: idade deve ser calculada por data completa (AAAAMMDD).

## Requisitos EARS consolidados

### REQ-BEN-VAL-001 - CPF obrigatorio e valido

```yaml
REQ-BEN-VAL-001:
  pattern: event-driven
  text: "Quando os dados de beneficiario forem informados para cadastro, o sistema deve validar CPF com algoritmo modulo 11 e rejeitar cadastro com CPF invalido."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L125-L131
    - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L198-L261
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L72-L78
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L105-L147
  modernization_decision: EVOLUIR
  rationale: "Harmoniza validacao de CPF em um unico criterio tecnico, eliminando bypass legado."
  acceptance:
    - "CPF com DV invalido e rejeitado."
    - "CPF 00000000000 e rejeitado."
    - "CPF com formato numerico valido e DV correto e aceito."
  priority: P0
  risk: CRITICO
```

### REQ-BEN-VAL-002 - Nome completo obrigatorio

```yaml
REQ-BEN-VAL-002:
  pattern: unwanted
  text: "O sistema nao deve aceitar cadastro de beneficiario sem nome e sobrenome."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L143-L148
  modernization_decision: MIGRAR
  rationale: "Regra estrutural de qualidade cadastral ja implementada no legado."
  acceptance:
    - "Nome vazio e rejeitado."
    - "Nome sem separacao de pelo menos dois termos e rejeitado."
    - "Nome com pelo menos nome e sobrenome e aceito."
  priority: P0
  risk: ALTO
```

### REQ-BEN-VAL-003 - Data de nascimento valida

```yaml
REQ-BEN-VAL-003:
  pattern: event-driven
  text: "Quando a data de nascimento for informada, o sistema deve validar faixa de ano, mes e dia calendario."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L134-L141
  modernization_decision: EVOLUIR
  rationale: "Mantem validacao basica e permite aprimorar tratamento de ano bissexto com calendario completo."
  acceptance:
    - "Mes fora de 1..12 e rejeitado."
    - "Dia fora do limite do mes e rejeitado."
    - "Ano futuro e rejeitado."
  priority: P0
  risk: ALTO
```

### REQ-BEN-VAL-004 - Dominio de UF e status cadastral

```yaml
REQ-BEN-VAL-004:
  pattern: state-driven
  text: "Enquanto o beneficiario estiver em manutencao cadastral, o sistema deve aceitar apenas UF do dominio oficial e status no conjunto A/S/C/I/D."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L150-L174
    - 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L176-L184
  modernization_decision: MIGRAR
  rationale: "Regra de dominio evita codigos invalidos e inconsistencias de estado."
  acceptance:
    - "UF fora da tabela oficial e rejeitada."
    - "Status fora do dominio A/S/C/I/D e rejeitado."
    - "UF e status validos permitem continuidade."
  priority: P0
  risk: MEDIO
```

### REQ-BEN-DOC-005 - RG com validacao minima

```yaml
REQ-BEN-DOC-005:
  pattern: event-driven
  text: "Quando RG for informado, o sistema deve exigir conteudo nao vazio e tamanho minimo de 5 caracteres uteis."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L80-L86
  modernization_decision: MIGRAR
  rationale: "Regra minima ja existente para reduzir documentos evidentemente invalidos."
  acceptance:
    - "RG vazio e rejeitado."
    - "RG com menos de 5 caracteres uteis e rejeitado."
    - "RG com 5 ou mais caracteres uteis e aceito para as validacoes seguintes."
  priority: P1
  risk: MEDIO
```

### REQ-BEN-DOC-006 - Documentacao consistente para elegibilidade

```yaml
REQ-BEN-DOC-006:
  pattern: complex
  text: "Enquanto a solicitacao de elegibilidade estiver em processamento, quando o programa exigir documentacao, o sistema deve bloquear concessao para beneficiario com DOCUMENTOS-OK diferente de S."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L88-L98
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L180-L188
  modernization_decision: EVOLUIR
  rationale: "Evita override de validacao documental por regras excepcionais de prefixo."
  acceptance:
    - "Para programa que exige documentacao, DOCUMENTOS-OK != S bloqueia elegibilidade."
    - "Nao deve haver limpeza de erros anteriores por regra de excecao documental."
  priority: P0
  risk: CRITICO
```

### REQ-BEN-ELEG-007 - Beneficiario e programa devem existir e estar ativos

```yaml
REQ-BEN-ELEG-007:
  pattern: unwanted
  text: "O sistema nao deve avaliar elegibilidade quando beneficiario ou programa nao existir, nem quando o programa estiver inativo."
  source_legacy:
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L72-L90
    - 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L92-L107
  modernization_decision: MIGRAR
  rationale: "Garante pre-condicoes basicas do fluxo de concessao."
  acceptance:
    - "Beneficiario inexistente encerra fluxo com motivo explicito."
    - "Programa inexistente encerra fluxo com motivo explicito."
    - "Programa inativo encerra fluxo com motivo explicito."
  priority: P0
  risk: CRITICO
```

### REQ-BEN-ELEG-008 - Status do beneficiario para concessao

```yaml
REQ-BEN-ELEG-008:
  pattern: state-driven
  text: "Enquanto o beneficiario nao estiver com status A, o sistema deve marcar a solicitacao como nao elegivel e registrar motivo de bloqueio."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L118-L136
  modernization_decision: MIGRAR
  rationale: "Consolida politica de bloqueio por estados S/C/D/I com rastreio de causa."
  acceptance:
    - "Status S gera nao elegivel com motivo BENEFICIARIO SUSPENSO."
    - "Status C ou D gera nao elegivel com motivo BENEFICIARIO CANCELADO/DESLIGADO."
    - "Status I gera nao elegivel com motivo BENEFICIARIO INATIVO."
  priority: P0
  risk: CRITICO
```

### REQ-BEN-ELEG-009 - Faixa etaria e renda por programa

```yaml
REQ-BEN-ELEG-009:
  pattern: complex
  text: "Enquanto os parametros de elegibilidade do programa estiverem vigentes, quando idade e renda forem avaliadas, o sistema deve aplicar limites de IDADE-MIN, IDADE-MAX e RENDA-MAX para decidir elegibilidade."
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

### REQ-BEN-ELEG-010 - Regras por tipo de programa A/P/T

```yaml
REQ-BEN-ELEG-010:
  pattern: event-driven
  text: "Quando o tipo de programa for identificado, o sistema deve aplicar regra especifica de elegibilidade para tipos A, P e T e rejeitar tipo desconhecido."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L170-L206
  modernization_decision: MIGRAR
  rationale: "Preserva semantica de politicas por categoria de programa."
  acceptance:
    - "Tipo A aplica regras assistenciais com validacao documental."
    - "Tipo P exige idade minima previdenciaria."
    - "Tipo T exige faixa etaria de trabalho."
    - "Tipo desconhecido gera nao elegivel com motivo explicito."
  priority: P0
  risk: ALTO
```

### REQ-BEN-ELEG-011 - Elegibilidade especifica por codigo (NIS e dependentes)

```yaml
REQ-BEN-ELEG-011:
  pattern: state-driven
  text: "Enquanto o codigo de elegibilidade especifica estiver configurado no programa, o sistema deve exigir NIS quando houver marcador R e exigir dependentes quando houver marcador D."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L208-L247
  modernization_decision: MIGRAR
  rationale: "Regra de negocio especifica por programa com dependencia de dados sociais."
  acceptance:
    - "Marcador R com NIS=0 bloqueia elegibilidade."
    - "Marcador D com zero dependentes bloqueia elegibilidade."
    - "Com exigencias satisfeitas, regra especifica nao bloqueia."
  priority: P1
  risk: ALTO
```

### REQ-BEN-ELEG-012 - Regiao especial sem bypass de identidade

```yaml
REQ-BEN-ELEG-012:
  pattern: optional
  text: "Onde o beneficiario pertencer a regiao especial 99, o sistema deve aplicar flexibilizacao apenas nos filtros socioeconomicos definidos em politica, sem dispensar validacao de identidade, status e documentacao obrigatoria."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L109-L116
  modernization_decision: EVOLUIR
  rationale: "Remove retorno imediato que burlava controles essenciais de concessao."
  acceptance:
    - "Regiao 99 nao pode aprovar beneficiario com status diferente de A."
    - "Regiao 99 nao pode aprovar beneficiario sem documentacao obrigatoria."
    - "Flexibilizacao aplicada apenas aos criterios previstos na politica ativa."
  priority: P0
  risk: CRITICO
```

## Regras legadas candidatas a descarte

```yaml
LEGACY-DROP-001:
  text: "Aceitar CPF iniciado por 000 como excecao valida de homologacao."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L220-L224
  modernization_decision: DESCARTAR
  rationale: "Fere integridade de identidade civil em ambiente de producao."

LEGACY-DROP-002:
  text: "Permitir prefixo especial de CPF sobrescrever falhas de validacao e limpar erros acumulados."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L180-L188
  modernization_decision: DESCARTAR
  rationale: "Cria bypass de seguranca e apaga evidencia de falhas."

LEGACY-DROP-003:
  text: "Aprovar automaticamente elegibilidade por COD-REGIAO=99 com escape imediato."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L109-L116
  modernization_decision: DESCARTAR
  rationale: "Viola principio de validacao minima obrigatoria antes da concessao."
```

## Resumo de decisao

- MIGRAR: REQ-BEN-VAL-002, REQ-BEN-VAL-004, REQ-BEN-DOC-005, REQ-BEN-ELEG-007, REQ-BEN-ELEG-008, REQ-BEN-ELEG-010, REQ-BEN-ELEG-011
- EVOLUIR: REQ-BEN-VAL-001, REQ-BEN-VAL-003, REQ-BEN-DOC-006, REQ-BEN-ELEG-009, REQ-BEN-ELEG-012
- DESCARTAR: LEGACY-DROP-001, LEGACY-DROP-002, LEGACY-DROP-003
