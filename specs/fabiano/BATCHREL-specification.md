# Especificação: BATCHREL - Geração de Relatórios Consolidados Mensais

**Status**: Draft (Stage 2 - Specification)  
**Data**: 2026-05-27  
**Fonte**: Workshop Amarelo 02 - SIFAP 2.0 Modernization  
**Constitution**: v1.0.0

---

## Resumo Executivo

O **BATCHREL** é o programa de relatórios do SIFAP: gera consolidações mensais de pagamentos por região (5 regiões brasileiras) e por status de processamento (gerado/pago/cancelado/devolvido/estornado). Recebe competência via INPUT, lê todos os pagamentos daquele mês da tabela PAGAMENTO, busca a região de cada beneficiário, acumula totalizações por área geográfica e status, e imprime relatório flat-file para impressora Mainframe (66 linhas por página).

---

## Requisitos de Negócio (EARS)

### REQ-REL-001: Solicitar Competência ao Operador
```
GIVEN sistema iniciado
WHEN programa aguarda INPUT
THEN exibe prompt "COMPETENCIA RELATORIO:"
AND aguarda entrada numérica no formato AAAAMM
AND armazena em #COMPETENCIA
```
**Tipo**: Interação  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L102`  
**Notas**: 
- INPUT é operação síncrona — bloqueia até operador digitar
- Validação de formato (AAAAMM) é responsabilidade do operador (não verificada no código)
- Permite relatório retroativo de qualquer mês anterior

---

### REQ-REL-002: Inicializar Estruturas de Acumulação
```
GIVEN programa iniciado
WHEN inicia acumuladores
THEN cria 5 tabelas regionais e 5 tabelas de status com:
  - Nomes de regiões: 'NORTE', 'NORDESTE', 'SUDESTE', 'SUL', 'CENTRO-OESTE'
  - Nomes de status: 'GERADO', 'PAGO', 'CANCELADO', 'DEVOLVIDO', 'ESTORNADO'
  - Zeros para todos os acumuladores (bruto, desconto, líquido, quantidade)
AND seta total geral = 0
```
**Tipo**: Inicialização  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L74-L100`  
**Mapeamento Regional**:
```
Índice 1: COD-REGIAO 1-5 (regiões 1-5)   → NORTE
Índice 2: COD-REGIAO 6-10 (regiões 6-10) → NORDESTE
Índice 3: COD-REGIAO 11-15 (regiões 11-15) → SUDESTE
Índice 4: COD-REGIAO 16-20 (regiões 16-20) → SUL
Índice 5: COD-REGIAO 21-27 (regiões 21-27) → CENTRO-OESTE
```

---

### REQ-REL-003: Leitura de Pagamentos por Competência
```
GIVEN #COMPETENCIA preenchida
WHEN inicia leitura
THEN READ PAGAMENTO-V BY COMPETENCIA = #COMPETENCIA
AND para cada pagamento:
  - Valida que PAGAMENTO-V.COMPETENCIA = #COMPETENCIA (escape se não)
  - Busca região do beneficiário na tabela BENEFICIARIO pelo CPF
  - Mapeia COD-REGIAO para índice de região (1-5)
```
**Tipo**: Processamento  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L105-L114`  
**Notas**: 
- FIND busca por CPF (chave primária) — deve ser rápido
- Se região não encontrada, #COD-REG = 0 → mapeia para índice 5 (padrão CENTRO-OESTE)
- Leitura é sequencial — não há ordenação específica mencionada

---

### REQ-REL-004: Mapear Código de Região para Índice (1-5)
```
GIVEN #COD-REGIAO obtido do BENEFICIARIO
WHEN calcula índice de região
THEN:
  IF COD-REGIAO IN [1..5]
    THEN #IDX-REG = 1 (NORTE)
  ELSE IF COD-REGIAO IN [6..10]
    THEN #IDX-REG = 2 (NORDESTE)
  ELSE IF COD-REGIAO IN [11..15]
    THEN #IDX-REG = 3 (SUDESTE)
  ELSE IF COD-REGIAO IN [16..20]
    THEN #IDX-REG = 4 (SUL)
  ELSE
    THEN #IDX-REG = 5 (CENTRO-OESTE, padrão para 21-27 ou inválido)
```
**Tipo**: Transformação  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L116-L133`  
**Notas**: 
- Lógica aninhada com IF/ELSE IF (padrão escalada)
- Índice é 1-based (não 0-based)
- CENTRO-OESTE é "catch-all" para qualquer código inválido

---

### REQ-REL-005: Acumular Valor Bruto Regional (com Arredondamento)
```
GIVEN pagamento lido e #IDX-REG calculado
WHEN acumula valor bruto regional
THEN:
  VLR-ARR = VLR-BRUTO + 0.005  (arredonda para cima, diferente de BATCHPGT)
  VLR-TEMP = VLR-ARR × 100
  VLR-ARR = VLR-TEMP / 100
  #TOT-REG-BRUTO(#IDX-REG) += VLR-ARR
AND incrementa #QTD-REG(#IDX-REG) += 1
```
**Tipo**: Agregação  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L136-L143`  
**Notas**: 
- ⚠️ **DIFERENÇA CRÍTICA**: ARREDONDA (+0.005) vs BATCHPGT que TRUNCA
- Comentário no código: "ARREDONDAMENTO DIFERE DO CALCBENF" (L136)
- Implicação: relatório pode não fechar 100% com BATCHPGT se houver muitos decimais
- Técnica +0.005 é standard para arredondamento comercial (banker's rounding aproximado)

---

### REQ-REL-006: Acumular Desconto e Líquido Regional
```
GIVEN pagamento lido e #IDX-REG calculado
WHEN acumula desconto e líquido regionais
THEN:
  #TOT-REG-DESC(#IDX-REG) += VLR-DESCONTO (sem arredondamento)
  #TOT-REG-LIQ(#IDX-REG) += VLR-LIQUIDO (sem arredondamento)
```
**Tipo**: Agregação  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L141-L142`  
**Notas**: 
- Desconto e líquido usam valores DIRETOS (não arredondam)
- Apenas BRUTO recebe arredondamento por +0.005
- Isso pode causar desbalanceamento: (BRUTO-ARREDONDADO) - DESC ≠ LIQ original

---

### REQ-REL-007: Mapear Status de Pagamento para Índice (1-5)
```
GIVEN STATUS-PGTO do pagamento lido
WHEN calcula índice de status
THEN usa DECIDE ON FIRST VALUE:
  'G' (gerado)  → #IDX-STS = 1
  'P' (pago)    → #IDX-STS = 2
  'C' (cancelado) → #IDX-STS = 3
  'D' (devolvido) → #IDX-STS = 4
  'E' (estornado) → #IDX-STS = 5
  NONE          → #IDX-STS = 1 (padrão = GERADO)
```
**Tipo**: Transformação  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L146-L159`  
**Status Codes**:
```
'G' = GERADO     (saída de BATCHPGT, ainda não processado)
'P' = PAGO       (transmitido ao banco/sistema pagador)
'C' = CANCELADO  (cancelamento administrativo)
'D' = DEVOLVIDO  (retorno do banco — motivo a investigar)
'E' = ESTORNADO  (cancelamento com restituição)
```
**Notas**: 
- NONE case mapeia para 'G' (gerado) — nunca deve ocorrer em produção

---

### REQ-REL-008: Acumular por Status
```
GIVEN pagamento lido, #IDX-STS calculado
WHEN acumula por status
THEN:
  #TOT-STS-BRUTO(#IDX-STS) += VLR-BRUTO (sem arredondamento aqui)
  #QTD-STS(#IDX-STS) += 1
```
**Tipo**: Agregação  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L160-L161`  
**Notas**: 
- Apenas VLR-BRUTO (não desconto/líquido) — foco em análise de processamento
- Acumulação por status é INDEPENDENTE da acumulação por região
- Permite auditar % de pagos vs cancelados

---

### REQ-REL-009: Acumular Totalizações Gerais
```
GIVEN pagamento lido
WHEN acumula totais gerais
THEN:
  #TOT-GERAL-BRUTO += VLR-ARR (valor arredondado regional)
  #TOT-GERAL-DESC += VLR-DESCONTO
  #TOT-GERAL-LIQ += VLR-LIQUIDO
  #QTD-GERAL += 1
```
**Tipo**: Agregação  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L164-L167`  
**Notas**: 
- Totais gerais devem reconciliar com SELECT SUM do banco
- BRUTO-GERAL usa valor arredondado (VLR-ARR) — cuidado!

---

### REQ-REL-010: Imprimir Cabeçalho de Página
```
GIVEN #PAG (contador de páginas), #COMPETENCIA, #DT-HOJE
WHEN chama IMPRIME-CABECALHO
THEN:
  - Incrementa #PAG += 1
  - Escreve FORM FEED (/) para mudança de página (padrão Mainframe)
  - Escreve título: "SIFAP - RELATORIO CONSOLIDADO MENSAL"
  - Alinha à direita "PAG: {#PAG}"
  - Nova linha: "COMPETENCIA: {#COMPETENCIA}     DATA: {#DT-HOJE}"
  - Escreve linha de dashes (60 caracteres)
  - Seta #LINHA = 5 (contador de linhas impressas)
```
**Tipo**: Formatação/IO  
**Prioridade**: MÉDIA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L200-L209`  
**Notas**: 
- FORM FEED (/) é instrução Mainframe para pular página
- Página tem máximo 66 linhas (#MAX-LINHAS)
- Inicialização força cabeçalho na primeira página (#LINHA = 99)

---

### REQ-REL-011: Imprimir Resumo por Região
```
GIVEN acumuladores regionais preenchidos
WHEN imprime resumo regional
THEN:
  WRITE 'RESUMO POR REGIAO'
  WRITE '=========================================='
  FOR #I = 1 TO 5:
    WRITE {#NOME-REG(#I)} '  QTD:' {#QTD-REG(#I)}
          '  BRUTO:' {#TOT-REG-BRUTO(#I)}
          '  DESC:' {#TOT-REG-DESC(#I)}
          '  LIQ:' {#TOT-REG-LIQ(#I)}
```
**Tipo**: Relatório  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L174-L181`  
**Formato**:
```
RESUMO POR REGIAO
==========================================
NORTE     QTD: 123456  BRUTO: 12345678.90  DESC: 1234567.89  LIQ: 11111111.01
NORDESTE  QTD: 234567  BRUTO: 23456789.01  DESC: 2345678.90  LIQ: 21111110.11
... etc
```
**Notas**: 
- Exibe as 5 regiões consolidadas
- Formato é flat-file (não alinhado em coluna — para Mainframe)

---

### REQ-REL-012: Imprimir Resumo por Status
```
GIVEN acumuladores de status preenchidos
WHEN imprime resumo de status
THEN:
  WRITE 'RESUMO POR STATUS'
  WRITE '=========================================='
  FOR #I = 1 TO 5:
    WRITE {#NOME-STS(#I)} '  QTD:' {#QTD-STS(#I)}
          '  BRUTO:' {#TOT-STS-BRUTO(#I)}
```
**Tipo**: Relatório  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L184-L189`  
**Formato**:
```
RESUMO POR STATUS
==========================================
GERADO      QTD: 100000  BRUTO: 50000000.00
PAGO        QTD: 850000  BRUTO: 425000000.00
CANCELADO   QTD: 20000   BRUTO: 10000000.00
DEVOLVIDO   QTD: 15000   BRUTO: 7500000.00
ESTORNADO   QTD: 15000   BRUTO: 7500000.00
```
**Notas**: 
- Apenas bruto (não desc/líq) — foco em tracking de status
- Auditoria: QTD total deve = #QTD-GERAL

---

### REQ-REL-013: Imprimir Total Geral
```
GIVEN todos os acumuladores finalizados
WHEN imprime total geral
THEN:
  WRITE '=========================================='
  WRITE 'TOTAL GERAL  QTD:' {#QTD-GERAL}
        '  BRUTO:' {#TOT-GERAL-BRUTO}
        '  DESC:' {#TOT-GERAL-DESC}
        '  LIQ:' {#TOT-GERAL-LIQ}
  WRITE '=========================================='
```
**Tipo**: Relatório  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L192-L197`  
**Reconciliação**:
```
Validação de fechamento:
  #QTD-GERAL = SUM(#QTD-REG) = SUM(#QTD-STS)
  #TOT-GERAL-BRUTO ≈ SUM(#TOT-REG-BRUTO) [pode diferir por arredondamento]
  #TOT-GERAL-LIQ ≈ SUM(#TOT-REG-LIQ) [pode diferir por arredondamento]
```
**Notas**: 
- Total geral é a linha MAIS importante — auditoria depende
- Desbalanceamento por arredondamento (BRUTO vs DESC/LIQ) é aceitável
- Deve ser enviado para controlaria para reconciliação com BATCHPGT

---

## Requisitos Não-Funcionais

### RNF-REL-001: Formato para Impressora Mainframe
```
GIVEN saída de relatório
WHEN formata para impressão
THEN:
  - Máximo 66 linhas por página (padrão Mainframe 132 caracteres/linha)
  - FORM FEED (/) entre páginas
  - Sem formatação especial (flat-file)
```
**Enforcement**: Constante #MAX-LINHAS = 66  
**Rastreabilidade**: `source_legacy: BATCHREL.NSN#L70`

---

### RNF-REL-002: Flexibilidade de Competência
```
GIVEN operador solicita competência
WHEN executa relatório
THEN permite qualquer competência anterior ou futura
  (não restringe a mês atual)
```
**Enforcement**: INPUT sem validação de data  
**Rastreabilidade**: `source_legacy: BATCHREL.NSN#L102`

---

### RNF-REL-003: Rastreabilidade de Execução
```
GIVEN programa em execução
WHEN printa cabeçalho
THEN registra:
  - Número da página
  - Data de execução
  - Competência do relatório
```
**Enforcement**: IMPRIME-CABECALHO em cada página  
**Rastreabilidade**: `source_legacy: BATCHREL.NSN#L200-L209`

---

### RNF-REL-004: Determinístico (Sem Lógica de Paginação Automática)
```
GIVEN leitura de pagamentos
WHEN imprime linhas
THEN não implementa quebra automática de página
  (responsabilidade do operador ou sistema de impressão)
```
**Enforcement**: #LINHA é inicializado mas não verificado  
**Rastreabilidade**: `source_legacy: BATCHREL.NSN#L61-L72`

---

## Mapeamento de Dados

### Entrada: PAGAMENTO (READ)
```
NUM-PAGTO          (N10)
CPF-BENEF          (N11)      ← Chave para buscar região
COD-PROGRAMA       (N4)
COMPETENCIA        (N6)       ← Filtro de leitura
VLR-BRUTO          (N9.2)     ← Acumulado (com arredondamento)
VLR-DESCONTO       (N9.2)     ← Acumulado direto
VLR-LIQUIDO        (N9.2)     ← Acumulado direto
STATUS-PGTO        (A1)       ← Mapeado para índice (1-5)
TIPO-PGTO          (A1)       ← Usado em filtragens futuras
VLR-ABONO          (N9.2)
```

### Entrada: BENEFICIARIO (FIND)
```
CPF                (N11)      ← Chave de busca
COD-REGIAO         (N2)       ← Mapeado para índice regional (1-5)
UF                 (A2)       ← Informativo apenas
```

### Saída: Relatório Flat-File (WRITE)
```
Linha 1-5:    Cabeçalho de página (titulo, competência, data, pag)
Linha 6:      Vazio
Linha 7-11:   Resumo por região (5 linhas + headers)
Linha 12:     Vazio
Linha 13-17:  Resumo por status (5 linhas + headers)
Linha 18-20:  Total geral + separadores
```

---

## Regras de Negócio Críticas

| ID | Regra | Risco | Origem |
|----|-------|-------|--------|
| BR-001 | Arredondar bruto com +0.005 (não truncar) | **CRÍTICO** | L137, comentário L136: difere de CALCBENF |
| BR-002 | Mapeamento regional: 5 faixas de COD-REGIAO | **ALTO** | L116-133, suporta futura redivisão geográfica |
| BR-003 | Status mapeado em 5 categorias (G/P/C/D/E) | **ALTO** | L146-159, audit trail de processamento |
| BR-004 | DESC e LIQ não são arredondados | **MÉDIO** | L141-142, pode desbalancear com BRUTO |
| BR-005 | Competência via INPUT (sem validação) | **MÉDIO** | L102, permite relatório retroativo |
| BR-006 | Máximo 66 linhas/página (Mainframe) | **MÉDIO** | L70, padrão de impressora |
| BR-007 | Acumuladores separados por região e status | **ALTO** | L35-50, cruzamento de dimensões |
| BR-008 | Busca BENEFICIARIO para cada pagamento | **MÉDIO** | L112-114, impacto de performance |

---

## Decisões de Design (ADRs)

### ADR-001: Arredondar vs Truncar (Diferença de BATCHPGT)
**Decisão**: Usar arredondamento comercial (+0.005) em relatórios, diferente de BATCHPGT que trunca  
**Racional**: Relatórios são consultivos (não executivos). Arredondamento é mais legível. Comentário no código valida essa escolha (L136).  
**Impacto**: 
- Relatório de BRUTO não fecha 100% com BATCHPGT se muitos decimais
- Recomendação: sempre reconciliar com SELECT SUM do banco
- No Java 21: usar `Math.round()` ou `BigDecimal.setScale(2, ROUND_HALF_UP)`

### ADR-002: 5 Regiões vs 27 COD-REGIAO
**Decisão**: Agrupar 27 regiões em 5 macro-regiões brasileiras (N/NE/SE/S/CO)  
**Racional**: Relatório executivo exige consolidação. Detalhe fica para queries ad-hoc.  
**Impacto**: 
- Perda de granularidade regional (trade-off aceitável)
- Em Estágio 3, considerar parametrizar mapeamento (tabela `tab_regiao_grupo`)
- Futuro: permitir "relatório por COD-REGIAO" com filtro

### ADR-003: Acumuladores Separados por Região E Status
**Decisão**: Manter dois eixos de agregação (região + status) independentes  
**Racional**: Requisitos diferentes (auditoria regional + auditoria processamento)  
**Impacto**: 
- Maior consumo de memória (2 × 5 arrays)
- Permite análise cruzada: "Quantos pagos por região?"
- Mantém simplicidade (loops separados, não matriz 5×5)

---

## Critérios de Aceitação (DoD)

- [ ] Solicita competência via INPUT (sem validação)
- [ ] Inicializa 5 acumuladores regionais + 5 acumuladores de status
- [ ] Lê todos os PAGAMENTO da competência
- [ ] Busca BENEFICIARIO para cada pagamento
- [ ] Mapeia COD-REGIAO (1-27) para índice regional (1-5) corretamente
- [ ] Arredonda BRUTO com +0.005 (diferente de BATCHPGT)
- [ ] Acumula DESC e LIQ SEM arredondamento
- [ ] Mapeia STATUS-PGTO ('G'/'P'/'C'/'D'/'E') para índice (1-5)
- [ ] Acumula totalizações gerais
- [ ] Imprime cabeçalho com FORM FEED, título, competência, data, página
- [ ] Imprime resumo regional (5 linhas com QTD/BRUTO/DESC/LIQ)
- [ ] Imprime resumo status (5 linhas com QTD/BRUTO)
- [ ] Imprime total geral (QTD/BRUTO/DESC/LIQ)
- [ ] Validação: #QTD-GERAL = SUM(#QTD-REG) = SUM(#QTD-STS)
- [ ] Testes parametrizados com dados de mock (15-20 pagamentos)
- [ ] Validação de arredondamento vs reconciliação banco

---

## Próximas Fases

### Estágio 2 → 3 (Clarification & Planning)
1. **Período de execução**: Relatório roda junto com BATCHPGT no 1º dia útil ou separadamente?
2. **Validação de competência**: Deve validar se competência existe antes de ler? Ou leave fail-safe ao operador?
3. **Consolidação de múltiplas regiões**: Suportar período (ex: relatório acumulado de 3 meses)?
4. **Exportação**: Apenas flat-file para Mainframe ou também PDF/Excel?
5. **Parametrização de macro-regiões**: Permitir user-defined grouping de regiões?

### Estágio 3 (Implementation)
- [ ] Criar `RelatorioService` com `generateMonthlyReport(competencia)`
- [ ] Criar `RelatorioCalculator` com agregação por região + status
- [ ] Implementar `BeneficiarioRepository.findByCpf()` (FIND)
- [ ] Mapear 5 macro-regiões (tabela `tab_regiao_grupo`)
- [ ] Testes parametrizados com múltiplos status
- [ ] Geração de PDF/Excel (futuro) em paralelo com flat-file
- [ ] Reconciliação automática: validar #QTD-GERAL vs SELECT COUNT

---

## Rastreabilidade Completa

| Estágio | Artefato | Linha | Descrição |
|---------|----------|-------|-----------|
| 1 (Arqueologia) | BATCHREL.NSN | L1-11 | Cabeçalho + objetivo |
| 1 (Arqueologia) | BATCHREL.NSN | L12-67 | DEFINE DATA |
| 2 (Spec) | Este documento | - | Requisitos EARS + ADRs |
| 2 (Spec) | DDM PAGAMENTO | - | Estrutura entrada |
| 2 (Spec) | DDM BENEFICIARIO | - | Estrutura entrada |
| 3 (Implementação) | src/main/java/domain/relatorio/ | - | Classes Domain |
| 3 (Implementação) | src/main/java/service/ | - | RelatorioService |
| 3 (Implementação) | src/main/java/repository/ | - | PagamentoRepository (findByCompetencia) |
| 3 (Implementação) | src/test/java/ | - | Testes + fixtures |
| 4 (Evolução) | GitHub Actions | - | Scheduling relatorio |
| 4 (Evolução) | Reconciliation Job | - | Validação vs BATCHPGT |

---

## Comparação: BATCHPGT vs BATCHREL

| Aspecto | BATCHPGT | BATCHREL |
|---------|----------|----------|
| **Objetivo** | Gerar pagamentos | Consolidar/reportar pagamentos |
| **Entrada** | BENEFICIARIO | PAGAMENTO |
| **Cálculo** | Múltiplos fatores (regional, familiar, renda, idade) | Somatorios apenas |
| **Arredondamento** | TRUNCAR (contábil) | ARREDONDAR +0.005 (legível) |
| **Status saída** | 'G' (gerado) | 'G'/'P'/'C'/'D'/'E' (auditoria) |
| **Frequência** | Mensal (1º dia útil) | On-demand (operador) |
| **Performance** | Crítica (500k registros) | Moderada (consolidação) |
| **Auditoria** | Reconciliação de valores | Reconciliação de volume |

---

**Versão da Especificação**: 1.0.0 Draft  
**Data de Ratificação**: [Pendente revisão Estágio 2]  
**Última Alteração**: 2026-05-27  
**Ratificado por**: [Arquiteto Enterprise + Software Architect]
