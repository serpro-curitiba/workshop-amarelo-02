# Especificação: BATCHPGT - Geração Mensal de Pagamentos em Lote

**Status**: Draft (Stage 2 - Specification)  
**Data**: 2026-05-27  
**Fonte**: Workshop Amarelo 02 - SIFAP 2.0 Modernization  
**Constitution**: v1.0.0

---

## Resumo Executivo

O **BATCHPGT** é o coração do SIFAP: um batch crítico que executa mensalmente no 1º dia útil para gerar pagamentos de todos os beneficiários ativos. Processa ordem por CPF (otimização de 1999), calcula benefício com múltiplos fatores (regional, familiar, renda, idade), aplica 13º/abono em dezembro e descontos simplificados, gravando um registro de pagamento por beneficiário por competência.

---

## Requisitos de Negócio (EARS)

### REQ-BATCH-001: Inicializar Competência Mensal
```
GIVEN o batch é executado em data de execução (DT-HOJE)
WHEN o sistema inicia
THEN a competência é calculada como AAAAMM (ex: 202605 para maio/2026)
```
**Tipo**: Configuração  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L105-L110`  
**Notas**: Cálculo determinístico de competência garante sincronização com sistemas downstream (RELPGT, relatórios).

---

### REQ-BATCH-002: Filtrar Beneficiários Válidos
```
GIVEN está em leitura do arquivo BENEFICIARIO
WHEN processa um beneficiário
THEN deve ignorar o registro IF:
  - STATUS ≠ 'A' (ativo)
  - OR CPF é duplicado (mesmo CPF já processado nesta execução)
  - OR já existe pagamento para este CPF nesta competência
AND deve contar como IGNORADO
```
**Tipo**: Validação  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L182-L210`  
**Notas**: 
- Deduplicação por CPF (linha 188-192) é essencial — sistemas legados podem ter CPFs duplicados
- Verifica duplicata no mesmo lote, não duplicatas históricas
- Incrementa contador #QTD-IGNORADOS

---

### REQ-BATCH-003: Validar Programa Social
```
GIVEN está processando um beneficiário com COD-PROGRAMA
WHEN busca o programa na tabela PROGRAMA-SOCIAL
THEN must fail IF:
  - Programa não encontrado → log "ERRO: PROG NAO ENCONTRADO CPF=... PROG=..."
  - Status do programa ≠ 'A' (ativo) → ignorar
AND incrementa #QTD-ERROS se não encontrado
```
**Tipo**: Validação  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L213-L230`  
**Notas**: Programa é obrigatório para cada beneficiário. Erros de programa não encontrado são anormais e exigem investigação.

---

### REQ-BATCH-004: Calcular Fator Regional
```
GIVEN beneficiário com COD-REGIAO (1-27)
WHEN calcula fator regional
THEN:
  IF COD-REGIAO IN [1..25]
    THEN aplica tabela regional (#TAB-REG)
  ELSE
    THEN fator = 1.0000 (neutro)
```
**Tipo**: Cálculo  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L124-L150, #L240-L244`  
**Tabela de Fatores**:
```
Região 1-10: 1.3500, 1.3200, 1.3000, 1.2800, 1.3100, 1.4000, 1.3800, 1.3500, 1.3200, 1.3600
Região 11-20: 1.1000, 1.1200, 1.0800, 1.0500, 1.0000, 1.0500, 1.0700, 1.0300, 1.1500, 1.2000
Região 21-25: 1.1800, 1.2500, 1.1000, 1.2200, 1.3300
Região 26-27: 1.0000 (padrão)
```
**Notas**: Tabela é hardcoded — necessário extrair para config/banco de dados no Estágio 3.

---

### REQ-BATCH-005: Calcular Fator Familiar
```
GIVEN beneficiário com NUM-DEPENDENTES (0-N)
WHEN calcula fator familiar
THEN:
  IF NUM-DEP = 0
    THEN fator = 1.0000
  ELSE IF NUM-DEP <= 2
    THEN fator = 1.0000 + (NUM-DEP × 0.0500)
       Ex: 1 dependente = 1.0500; 2 = 1.1000
  ELSE IF NUM-DEP <= 4
    THEN fator = 1.1000 + ((NUM-DEP - 2) × 0.0300)
       Ex: 3 deps = 1.1300; 4 = 1.1600
  ELSE
    THEN fator = 1.1600 + ((NUM-DEP - 4) × 0.0200)
       Ex: 5 deps = 1.1800; 6 = 1.2000
```
**Tipo**: Cálculo  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L246-L259`  
**Notas**: 
- Fator escalado com diminuição de incremento (0.05 → 0.03 → 0.02)
- Progressão não-linear reflete política de redução gradual de subsídio

---

### REQ-BATCH-006: Calcular Fator Renda Familiar
```
GIVEN beneficiário com RENDA-FAMILIAR
WHEN calcula fator renda
THEN busca primeira faixa onde RENDA <= FAIXA-RENDA e aplica FATOR-FAIXA:
  Faixa 1: RENDA <= R$ 300.00   → fator = 1.0000
  Faixa 2: RENDA <= R$ 600.00   → fator = 0.8500
  Faixa 3: RENDA <= R$ 1.000    → fator = 0.7000
  Faixa 4: RENDA <= R$ 1.500    → fator = 0.5500
  Faixa 5: RENDA > R$ 1.500     → fator = 0.4000
```
**Tipo**: Cálculo  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L153-L162, #L262, #L367-L375 (subrrotina DET-FAIXA-RENDA-BATCH)`  
**Notas**: 
- Implementado em subrrotina PERFORM DET-FAIXA-RENDA-BATCH
- Fator decresce com aumento de renda (progressividade regressiva)
- Última faixa (9999.99) é sempre atingida

---

### REQ-BATCH-007: Calcular Fator Idade
```
GIVEN beneficiário com DT-NASCIMENTO
WHEN calcula fator idade (IDADE = ANO-HOJE - ANO-NASC)
THEN:
  IF IDADE >= 65
    THEN fator = 1.1500 (idoso avançado)
  ELSE IF IDADE >= 60
    THEN fator = 1.1000 (idoso)
  ELSE IF IDADE < 18
    THEN fator = 1.0500 (jovem)
  ELSE
    THEN fator = 1.0000 (adulto)
```
**Tipo**: Cálculo  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L264-L277`  
**Notas**: 
- Cálculo de idade simplificado: ignora mês/dia
- Favorece idosos e jovens
- Fator neutro (1.0000) para população em idade produtiva (18-59)

---

### REQ-BATCH-008: Calcular Valor Bruto Mensal
```
GIVEN beneficiário com todos os fatores calculados (regional, familiar, renda, idade)
  AND VLR-BASE (do programa social)
  AND FATOR-REAJUSTE (do programa)
WHEN calcula valor bruto
THEN:
  VLR-BENF = VLR-BASE × FATOR-REG × FATOR-FAM × FATOR-RENDA × FATOR-IDADE
  VLR-BRUTO = VLR-BENF × (1 + FATOR-REAJUSTE)
  TRUNCAR para 2 casas decimais (não arredondar)
```
**Tipo**: Cálculo  
**Prioridade**: CRÍTICA  
**Complexidade**: Alta  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L280-L286`  
**Notas**: 
- Multiplicação de 4 fatores — sensível a arredondamentos
- TRUNCAR (não arredondar) é regra explícita no código (linhas 284-285)
- Reajuste é percentual aditivo (1 + FATOR) — guarda inflação do período

---

### REQ-BATCH-009: Calcular 13º Salário (Dezembro Apenas)
```
GIVEN mês de execução = 12 (dezembro)
  AND VLR-BASE, FATOR-REG, FATOR-IDADE já calculados
WHEN tipo de pagamento = 'D' (décimo terceiro)
THEN:
  VLR-13 = VLR-BASE × FATOR-REG × FATOR-IDADE (sem fator renda/familiar)
  TRUNCAR para 2 casas decimais
  VLR-BRUTO += VLR-13 (acumula ao bruto mensal)
```
**Tipo**: Cálculo Condicional  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L291-L296`  
**Notas**: 
- Apenas em dezembro
- 13º não sofre desconto familiar — visa cobrir meses sem benefício
- Código marca TIPO-PGTO = 'D' para auditoria

---

### REQ-BATCH-010: Calcular Abono Dezembro (Programa Tipo 'A')
```
GIVEN mês = 12 (dezembro)
  AND TIPO-PROG = 'A' (programa com abono, ex: auxílio-complementar)
WHEN calcula abono
THEN:
  VLR-ABONO = VLR-BENF × 0.15 (15% do benefício mensal)
  TRUNCAR para 2 casas decimais
  VLR-BRUTO += VLR-ABONO
```
**Tipo**: Cálculo Condicional  
**Prioridade**: ALTA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L298-L303`  
**Notas**: 
- Apenas para programas tipo 'A' (exemplos: código 1001-1005 = auxílio-educação)
- Abono é 15% do benefício mensal (não do 13º)
- Acumulável com 13º

---

### REQ-BATCH-011: Calcular Desconto Simplificado
```
GIVEN VLR-BRUTO (após 13º/abono)
WHEN calcula desconto
THEN:
  IF VLR-BRUTO > R$ 500.00
    THEN VLR-DESC = VLR-BRUTO × 0.03 (3%)
         TRUNCAR para 2 casas decimais
  ELSE
    THEN VLR-DESC = 0 (sem desconto até R$ 500)
```
**Tipo**: Cálculo  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L306-L312`  
**Notas**: 
- Desconto simplificado: apenas 3% flat se > R$ 500
- Sem faixas progressivas (diferente do legado CALCDSCT)
- Piso de R$ 500 isenta baixas renda

---

### REQ-BATCH-012: Calcular Valor Líquido
```
GIVEN VLR-BRUTO e VLR-DESC
WHEN calcula valor líquido
THEN:
  VLR-LIQ = VLR-BRUTO - VLR-DESC
  IF VLR-LIQ < 0
    THEN VLR-LIQ = 0 (nunca negativo)
  TRUNCAR para 2 casas decimais
```
**Tipo**: Cálculo  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L314-L320`  
**Notas**: 
- Garantia de não-negatividade
- Truncar (não arredondar) mantém precisão contábil

---

### REQ-BATCH-013: Gerar Registro de Pagamento
```
GIVEN todos os cálculos completados
WHEN insere novo pagamento no banco
THEN cria registro PAGAMENTO com:
  - NUM-PAGTO: sequencial incremental (último + 1)
  - CPF-BENEF: CPF do beneficiário
  - COD-PROGRAMA: código do programa
  - COMPETENCIA: AAAAMM
  - VLR-BRUTO: calculado
  - VLR-DESCONTO: calculado
  - VLR-LIQUIDO: calculado
  - DT-GERACAO: data de execução do batch
  - STATUS-PGTO: 'G' (gerado)
  - TIPO-PGTO: 'N' (normal), 'D' (décimo), 'A' (com abono)
  - VLR-ABONO: valor do abono ou 0
AND commita a transação
```
**Tipo**: Persistência  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L322-L336`  
**Notas**: 
- NUM-PAGTO é PK — busca do último garante continuidade
- STATUS-PGTO 'G' significa pronto para processamento de transmissão
- TIPO-PGTO é audit trail (qual tipo de pagamento: normal/13º/abono)

---

### REQ-BATCH-014: Acumular Estatísticas
```
GIVEN cada pagamento gerado
WHEN acumula contadores
THEN incrementa:
  - #QTD-GERADOS: +1
  - #VLR-TOTAL-BRUTO += VLR-BRUTO
  - #VLR-TOTAL-DESC += VLR-DESCONTO
  - #VLR-TOTAL-LIQ += VLR-LIQUIDO
  - #VLR-TOTAL-ABONO += VLR-ABONO
```
**Tipo**: Agregação  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L338-L342`  
**Notas**: 
- Acumuladores são para auditoria e reconciliação
- Devem bater com SELECT SUM do banco após execução

---

### REQ-BATCH-015: Log de Progresso
```
GIVEN batch em processamento
WHEN #QTD-GERADOS MOD 1000 = 0
THEN escreve linha de log:
  "PROCESSADOS: {qtd} ULTIMO CPF: {cpf}"
```
**Tipo**: Observabilidade  
**Prioridade**: MÉDIA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L344-L347`  
**Notas**: 
- Checkpoint a cada 1000 registros para lotes grandes
- Rastreabilidade do CPF permite reinício em caso de falha

---

### REQ-BATCH-016: Relatório Final de Execução
```
GIVEN batch finalizado
WHEN escreve resumo
THEN imprime:
  - COMPETENCIA: {valor}
  - TOTAL PROCESSADOS: {#QTD-PROCESSADOS}
  - PAGTOS GERADOS: {#QTD-GERADOS}
  - IGNORADOS: {#QTD-IGNORADOS}
  - ERROS: {#QTD-ERROS}
  - VLR TOTAL BRUTO: {valor}
  - VLR TOTAL DESC: {valor}
  - VLR TOTAL LIQUIDO: {valor}
  - VLR TOTAL ABONO: {valor}
```
**Tipo**: Relatório  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L351-L364`  
**Notas**: 
- Auditoria crítica: #QTD-GERADOS deve ser > 0
- ERROS > 0 exige investigação
- Totalizações devem ser auditadas externamente

---

## Requisitos Não-Funcionais

### RNF-BATCH-001: Performance
```
GIVEN lote de até 500.000 beneficiários
WHEN executa BATCHPGT
THEN deve completar em < 60 minutos (1 hora)
```
**Enforcement**: Timestamp de início/fim no log  
**Rastreabilidade**: `source_legacy: BATCHPGT.NSN#L1-L15 (crítico - execução 1º dia útil)`

---

### RNF-BATCH-002: Ordem de Processamento
```
GIVEN leitura de BENEFICIARIO
WHEN busca registros
THEN MUST estar ordenado por CPF (ASC)
  (otimização de 1999 — sistemas downstream dependem)
```
**Enforcement**: `READ BENEFICIARIO-V BY CPF` (não quebrar!)  
**Rastreabilidade**: `source_legacy: BATCHPGT.NSN#L178-L182`

---

### RNF-BATCH-003: Atomicidade
```
GIVEN cada pagamento
WHEN insere PAGAMENTO-V
THEN usa transação (STORE … END TRANSACTION)
  para garantir consistência
```
**Enforcement**: Sem STORE, nenhum commit  
**Rastreabilidade**: `source_legacy: BATCHPGT.NSN#L335-L336`

---

### RNF-BATCH-004: Idempotência
```
GIVEN beneficiário já tem pagamento na competência
WHEN tenta gerar novo pagamento
THEN deve ignorar (#QTD-IGNORADOS += 1)
```
**Enforcement**: Validação `FIND PAGAMENTO-V WHERE CPF + COMPETENCIA`  
**Rastreabilidade**: `source_legacy: BATCHPGT.NSN#L200-L210`

---

### RNF-BATCH-005: Auditoria
```
GIVEN batch em execução
WHEN gera pagamento
THEN MUST registrar:
  - Quem executou (usuário/sistema)
  - Quando (timestamp DT-HOJE)
  - Que competência (COMPETENCIA)
  - Quantos registros (counters)
```
**Enforcement**: Relatório final obrigatório  
**Rastreabilidade**: `source_legacy: BATCHPGT.NSN#L10 (alteração 2015 — INC AUDITORIA)`

---

## Mapeamento de Dados

### Entrada: BENEFICIARIO (VIEW)
```
CPF                (N11)      ← PK, ordenado
NOME               (A60)
DT-NASCIMENTO      (N8)       ← Calcula idade
STATUS             (A1)       ← Filtro 'A'
COD-PROGRAMA       (N4)       ← FK → PROGRAMA-SOCIAL
RENDA-FAMILIAR     (N9.2)     ← Fator renda
NUM-DEPENDENTES    (N2)       ← Fator familiar
COD-REGIAO         (N2)       ← Fator regional (1-27)
UF                 (A2)
NIS                (N11)      ← Identificador social
```

### Entrada: PROGRAMA-SOCIAL (VIEW)
```
COD-PROGRAMA       (N4)       ← PK
TIPO               (A1)       ← 'A' (abono), 'N' (normal)
VLR-BASE           (N9.2)     ← Base para cálculo
FATOR-REAJUSTE     (N3.4)     ← Percentual reajuste
STATUS-PROG        (A1)       ← Filtro 'A'
RENDA-MAX          (N9.2)     ← Teto de renda (futuro)
```

### Saída: PAGAMENTO (INSERT)
```
NUM-PAGTO          (N10)      ← Sequencial (PK)
CPF-BENEF          (N11)      ← FK → BENEFICIARIO
COD-PROGRAMA       (N4)       ← FK → PROGRAMA-SOCIAL
COMPETENCIA        (N6)       ← AAAAMM
VLR-BRUTO          (N9.2)     ← Calculado
VLR-DESCONTO       (N9.2)     ← Calculado
VLR-LIQUIDO        (N9.2)     ← Calculado
DT-GERACAO         (N8)       ← Data execução
STATUS-PGTO        (A1)       ← 'G' (gerado)
TIPO-PGTO          (A1)       ← 'N'/'D'/'A'
VLR-ABONO          (N9.2)     ← Calculado (0 se N/D)
```

---

## Regras de Negócio Críticas

| ID | Regra | Risco | Origem |
|----|-------|-------|--------|
| BR-001 | Processamento ORDENADO por CPF | **CRÍTICO** | L178, comentário: sistemas downstream dependem |
| BR-002 | Deduplicação de CPF no lote | **CRÍTICO** | L188-192, 1999 (otimização) |
| BR-003 | Fator familiar decresce com incrementos | **ALTO** | L250-257, 2012 (novas faixas) |
| BR-004 | Truncar (não arredondar) valores | **ALTO** | L284-285, implicação contábil |
| BR-005 | 13º + abono apenas em dezembro | **MÉDIO** | L292-303, 2009 (ajuste PGTO) |
| BR-006 | Desconto 3% flat (não progressivo) | **MÉDIO** | L308-312, simplificado no batch |
| BR-007 | Status 'G' = pronto p/ transmissão | **ALTO** | L332, audit trail |
| BR-008 | Renda máxima = gating (futuro) | **MÉDIO** | L49, campo ainda não aplicado |

---

## Decisões de Design (ADRs)

### ADR-001: Truncar vs Arredondar
**Decisão**: Sempre TRUNCAR valores (não arredondar)  
**Racional**: Regra explícita no código (COMPUTE + divisão por 100). Atende a auditoria contábil (evita acúmulo de centavos).  
**Impacto**: No Java 21, usar `Math.floor()` ou BigDecimal com `ROUND_DOWN`.

### ADR-002: Tabelas Hardcoded
**Decisão**: Extrair tabelas de fatores regionais e faixas de renda para banco/config  
**Racional**: Tabelas mudam a cada reforma (2012 = novas faixas). Hardcode em código = recompilação.  
**Impacto**: Criar tabelas `tab_fator_regional`, `tab_faixa_renda` em PostgreSQL; parametrizar por `dtv_inicio`, `dtv_fim`.

### ADR-003: Ordem de Processamento
**Decisão**: PRESERVAR ordem por CPF (não quebrar)  
**Racional**: Comentário em código: "sistemas downstream dependem desta ordenação" (L179). Deduplicação usa #CPF-ANT.  
**Impacto**: Em JPA, usar `Sort.by("cpf")` obrigatoriamente.

---

## Critérios de Aceitação (DoD)

- [ ] Batch lê BENEFICIARIO ordenado por CPF
- [ ] Filtra status='A' e programa ativo
- [ ] Deduplica CPF duplicado no lote
- [ ] Calcula 4 fatores (regional, familiar, renda, idade)
- [ ] Aplica truncagem a 2 casas decimais
- [ ] Gera 13º em dezembro (TIPO-PGTO='D')
- [ ] Gera abono se TIPO-PROG='A' em dezembro
- [ ] Calcula desconto 3% se bruto > R$500
- [ ] Insere pagamento com sequencial NUM-PAGTO
- [ ] Acumula counters e relatório final
- [ ] Logs a cada 1000 registros
- [ ] Testes cobrindo cada fator + combinações críticas (ex: 13º + abono + desconto)
- [ ] Auditoria externa valida totalizações vs SELECT SUM

---

## Próximas Fases

### Estágio 2 → 3 (Clarification & Planning)
1. **Definir janelas de competência**: Quando rodar? Sempre 1º dia útil ou permite outros dias?
2. **Tratamento de erros de programa não encontrado**: Falha o batch ou logging + continue?
3. **Revalidação de CPF/NIS**: Usar APIs externas ou confiar no cadastro?
4. **Backup/rollback**: Se falhar no meio, como recuperar?
5. **Parametrização de tabelas**: Quando mudam os fatores?

### Estágio 3 (Implementation)
- [ ] Criar `BeneficiarioRepository` com `findAllActiveOrderByCpf()`
- [ ] Criar `PagamentoService` com `generateMonthlyBatch(competencia)`
- [ ] Implementar `PagamentoCalculator` com 4 fatores + truncagem
- [ ] Criar tabelas `tab_fator_regional`, `tab_faixa_renda`
- [ ] Testes parametrizados (JUnit 5 @ParameterizedTest) para fatores
- [ ] Teste de integração com Testcontainers (PostgreSQL)

---

## Rastreabilidade Completa

| Estágio | Artefato | Linha | Descrição |
|---------|----------|-------|-----------|
| 1 (Arqueologia) | BATCHPGT.NSN | L1-15 | Cabeçalho + histórico |
| 1 (Arqueologia) | BATCHPGT.NSN | L16-103 | DEFINE DATA (estrutura) |
| 2 (Spec) | Este documento | - | Requisitos EARS + ADRs |
| 2 (Spec) | DDM BENEFICIARIO | - | Estrutura entrada |
| 2 (Spec) | DDM PROGRAMA-SOCIAL | - | Estrutura entrada |
| 2 (Spec) | DDM PAGAMENTO | - | Estrutura saída |
| 3 (Implementação) | src/main/java/domain/pagamento/ | - | Classes Domain |
| 3 (Implementação) | src/main/java/service/ | - | BPagamentoService |
| 3 (Implementação) | src/main/java/repository/ | - | Repositories JPA |
| 3 (Implementação) | src/test/java/ | - | Testes + Testcontainers |
| 4 (Evolução) | GitHub Actions | - | Workflow de batch (scheduler) |

---

**Versão da Especificação**: 1.0.0 Draft  
**Data de Ratificação**: [Pendente revisão Estágio 2]  
**Última Alteração**: 2026-05-27  
**Ratificado por**: [Arquiteto Enterprise + Software Architect]
