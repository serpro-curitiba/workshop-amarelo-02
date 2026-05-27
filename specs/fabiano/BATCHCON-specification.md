# Especificação: BATCHCON - Conciliação de Pagamentos x Retorno Bancário

**Status**: Draft (Stage 2 - Specification)  
**Data**: 2026-05-27  
**Fonte**: Workshop Amarelo 02 - SIFAP 2.0 Modernization  
**Constitution**: v1.0.0

---

## Resumo Executivo

O **BATCHCON** é o programa de conciliação bancária do SIFAP: processa o arquivo de retorno CNAB 240 do Banco do Brasil, compara cada registro retornado com os pagamentos gerados por BATCHPGT, reconcilia valores (tolerância de R$ 0,01), atualiza o status dos pagamentos ('G' → 'P'/'D'/'E') e grava registros de auditoria para cada evento conciliado ou divergente. É o programa que "fecha o ciclo" do pagamento: BATCHPGT **gera**, BATCHCON **confirma**. Código comentado de integração com Banco Real (descontinuada em 2007 após aquisição pelo Santander) deve ser preservado como referência histórica.

---

## Requisitos de Negócio (EARS)

### REQ-CON-001: Solicitar Parâmetros de Execução
```
GIVEN sistema iniciado
WHEN aguarda INPUT do operador
THEN exibe tela:
  "SIFAP - CONCILIACAO BANCARIA"
  "============================="
  "COMPETENCIA.....: {#COMPETENCIA}"
  "ARQUIVO RETORNO.: {#ARQ-RETORNO}"
AND aguarda competência (AAAAMM) e caminho do arquivo de retorno
```
**Tipo**: Interação  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L93-L102`  
**Notas**:
- #ARQ-RETORNO é caminho completo do arquivo flat CNAB 240 gerado pelo banco
- Não valida se arquivo existe antes de tentar abrir
- Não valida formato de competência

---

### REQ-CON-002: Abrir Arquivo CNAB 240 de Retorno
```
GIVEN #ARQ-RETORNO preenchido
WHEN abre arquivo
THEN DEFINE WORK FILE 1 #ARQ-RETORNO TYPE 'ASCII'
AND inicia leitura sequencial (READ WORK FILE 1 #REG-CNAB)
AND cada linha tem exatamente 240 bytes (#REG-CNAB = A240)
```
**Tipo**: I/O  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L105-L106`  
**Notas**:
- CNAB 240 = padrão FEBRABAN, layout Banco do Brasil
- Arquivo ASCII com registros de largura fixa (240 chars/linha)
- Pode conter registros de header, trailer e detalhe — apenas detalhe é processado

---

### REQ-CON-003: Filtrar Registros CNAB (Apenas Tipo '3' — Detalhe)
```
GIVEN registro CNAB 240 lido
WHEN verifica tipo de registro
THEN:
  #CNAB-TIPO-REG = SUBSTR(#REG-CNAB, 8, 1)
  IF #CNAB-TIPO-REG ≠ '3'
    THEN ignorar registro (ESCAPE TOP)
  ELSE
    THEN processar como detalhe
AND incrementa #QTD-LIDOS para TODOS os registros
```
**Tipo**: Filtro  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L113-L118`  
**Tipos de Registro CNAB 240**:
```
Tipo '0' = Header de arquivo
Tipo '1' = Header de lote
Tipo '3' = Detalhe (ÚNICO a processar)
Tipo '5' = Trailer de lote
Tipo '9' = Trailer de arquivo
```
**Notas**:
- Apenas tipo '3' contém dados de pagamento
- Headers/trailers carregam metadados do lote (banco, data, totais)

---

### REQ-CON-004: Parsear Layout CNAB 240 (Banco do Brasil)
```
GIVEN registro de detalhe (TIPO = '3')
WHEN parseia campos por posição fixa
THEN extrai:
  #CNAB-BANCO    = SUBSTR(#REG-CNAB,  1,  3)   → código banco
  #CNAB-LOTE     = SUBSTR(#REG-CNAB,  4,  4)   → número do lote
  #CNAB-TIPO-REG = SUBSTR(#REG-CNAB,  8,  1)   → tipo registro
  #CNAB-CPF      = SUBSTR(#REG-CNAB, 44, 11)   → CPF do beneficiário
  #CNAB-NUM-DOC  = SUBSTR(#REG-CNAB, 74, 10)   → número do documento/pagamento
  #CNAB-VLR      = SUBSTR(#REG-CNAB,120, 15)   → valor pago (em centavos)
  #CNAB-DT-PGTO  = SUBSTR(#REG-CNAB,140,  8)   → data do pagamento (AAAAMMDD)
  #CNAB-COD-RET  = SUBSTR(#REG-CNAB,231,  2)   → código de retorno
```
**Tipo**: Parsing  
**Prioridade**: CRÍTICA  
**Complexidade**: Alta  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L111-L135`  
**Mapa de Posições CNAB 240 (Banco do Brasil)**:
```
Pos  1-3   (3 bytes):  Código do banco (001 = Banco do Brasil)
Pos  4-7   (4 bytes):  Número do lote
Pos  8     (1 byte):   Tipo de registro (3 = detalhe)
Pos 44-54 (11 bytes):  CPF do beneficiário (sem pontuação)
Pos 74-83 (10 bytes):  Número do documento (= NUM-PAGTO do SIFAP)
Pos 120-134 (15 bytes): Valor em centavos (inteiro, sem ponto)
Pos 140-147 (8 bytes):  Data pagamento (DDMMAAAA — atenção ao formato!)
Pos 231-232 (2 bytes):  Código de retorno bancário
```
**Notas**:
- Mapeamento é hardcoded para layout CNAB 240 Banco do Brasil (código 001)
- #CNAB-NUM-DOC = NUM-PAGTO do SIFAP → chave de busca na tabela PAGAMENTO
- Valor em **centavos** — necessário dividir por 100

---

### REQ-CON-005: Converter Valor CNAB (Centavos → Reais)
```
GIVEN #CNAB-VLR string com valor em centavos
WHEN converte para reais
THEN:
  #VLR-STR = #CNAB-VLR (string → numérico)
  #VLR-RETORNO = #VLR-STR / 100 (centavos → reais)
```
**Tipo**: Transformação  
**Prioridade**: CRÍTICA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L130-L132`  
**Notas**:
- CNAB 240: valor sempre em centavos (sem ponto decimal)
- Ex: "000000001500" = R$ 15,00
- Divisão por 100 é a única transformação — sem truncagem ou arredondamento

---

### REQ-CON-006: Buscar Pagamento Correspondente
```
GIVEN #NUM-PGTO (número do documento) e #CPF-NUM e #COMPETENCIA
WHEN busca pagamento no SIFAP
THEN FIND PAGAMENTO-V WHERE NUM-PAGTO = #NUM-PGTO
  AND valida: CPF-BENEF = #CPF-NUM
  AND valida: COMPETENCIA = #COMPETENCIA
  THEN #FOUND = TRUE
IF NOT #FOUND
  THEN:
    #QTD-NAO-ENCONTRADOS += 1
    log: "NAO ENCONTRADO: CPF={cpf} DOC={doc}"
    ESCAPE TOP (ignora este registro)
```
**Tipo**: Validação  
**Prioridade**: CRÍTICA  
**Complexidade**: Média  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L138-L152`  
**Notas**:
- Dupla validação: NUM-PAGTO + CPF + COMPETENCIA
- Não encontrado é anomalia — pode indicar pagamento duplicado ou falha em BATCHPGT
- Log imediato para investigação

---

### REQ-CON-007: Conciliar Valor SIFAP x Banco (Tolerância R$ 0,01)
```
GIVEN pagamento encontrado com VLR-LIQUIDO (SIFAP) e #VLR-RETORNO (banco)
WHEN calcula diferença
THEN:
  #DIFF = |VLR-LIQUIDO - #VLR-RETORNO|  (valor absoluto)
  IF #DIFF > 0.01 (divergência)
    THEN:
      #QTD-DIVERGENTES += 1
      log: "DIVERGENCIA: CPF={cpf} SIFAP={vlr} BANCO={vlr}"
      PERFORM GRAVA-AUDITORIA-DIVERG
  ELSE (conciliado: diferença ≤ R$ 0,01)
    THEN:
      #QTD-CONCILIADOS += 1
      processar código de retorno
      PERFORM GRAVA-AUDITORIA-CONC
```
**Tipo**: Negócio (Conciliação)  
**Prioridade**: CRÍTICA  
**Complexidade**: Alta  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L155-L202`  
**Notas**:
- Tolerância de R$ 0,01 absorve diferenças de arredondamento
- Essa tolerância compensatória é consistente com a diferença de arredondamento BATCHPGT (trunca) vs BATCHREL (arredonda)
- Divergência **não** cancela o pagamento — apenas registra auditoria
- Divergência NÃO atualiza STATUS-PGTO (pagamento fica como 'G')

---

### REQ-CON-008: Atualizar Status por Código de Retorno Bancário
```
GIVEN pagamento conciliado (#DIFF ≤ 0.01)
WHEN processa código de retorno (#COD-RET)
THEN DECIDE ON FIRST VALUE:
  '00' (sucesso) →
    STATUS-PGTO = 'P' (pago)
    DT-PAGAMENTO = #DT-PGTO
    COD-BANCO = 1
    COD-RETORNO = '00'
    UPDATE PAGAMENTO + END TRANSACTION
  '01' (devolvido) →
    STATUS-PGTO = 'D' (devolvido)
    COD-RETORNO = '01'
    UPDATE PAGAMENTO + END TRANSACTION
  '02' (estornado) →
    STATUS-PGTO = 'E' (estornado)
    COD-RETORNO = '02'
    UPDATE PAGAMENTO + END TRANSACTION
  NONE (desconhecido) →
    log: "COD RETORNO DESCONHECIDO: {cod} CPF={cpf}"
    (não atualiza status)
```
**Tipo**: Negócio (Atualização de Status)  
**Prioridade**: CRÍTICA  
**Complexidade**: Alta  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L171-L199`  
**Ciclo de Status do Pagamento**:
```
'G' (GERADO por BATCHPGT)
  ↓ após conciliação '00'
'P' (PAGO — transmitido com sucesso)
  OU
  ↓ após conciliação '01'
'D' (DEVOLVIDO — banco retornou sem crédito)
  OU
  ↓ após conciliação '02'
'E' (ESTORNADO — reversão após crédito indevido)
```
**Notas**:
- COD-BANCO hardcoded = 1 (Banco do Brasil) — não parametrizado
- Código desconhecido deixa pagamento em 'G' sem update (possível falha silenciosa)
- Cada update é transacional (END TRANSACTION individual)

---

### REQ-CON-009: Gravar Auditoria de Conciliação
```
GIVEN pagamento conciliado (DIFF ≤ 0.01) e status atualizado
WHEN grava registro de auditoria
THEN INSERT AUDITORIA com:
  SEQ-AUDIT: sequencial (último + 1)
  DT-EVENTO: data de hoje
  HR-EVENTO: hora atual
  USUARIO:   'BATCH'
  ACAO:      'CO' (conciliado)
  TABELA-REF: 'PAGAMENTO'
  CHAVE-REF:  NUM-PAGTO
  VLR-ANTERIOR: (vazio — status anterior implícito)
  VLR-NOVO:    (vazio — novo status implícito)
  DESCRICAO:  'CONCILIADO COD RET={cod}'
AND END TRANSACTION
AND #QTD-AUDIT += 1
```
**Tipo**: Auditoria  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L238-L251`  
**Notas**:
- ACAO 'CO' = conciliação bem-sucedida
- Auditoria é SEMPRE gravada para conciliações (obrigatória por lei)
- SEQ-AUDIT sequencial preserva ordem cronológica de eventos

---

### REQ-CON-010: Gravar Auditoria de Divergência
```
GIVEN pagamento com DIFF > 0.01
WHEN grava registro de auditoria de divergência
THEN INSERT AUDITORIA com:
  SEQ-AUDIT: sequencial (último + 1)
  DT-EVENTO: data de hoje
  HR-EVENTO: hora atual
  USUARIO:   'BATCH'
  ACAO:      'DV' (divergente)
  TABELA-REF: 'PAGAMENTO'
  CHAVE-REF:  NUM-PAGTO
  VLR-ANTERIOR: VLR-LIQUIDO do SIFAP (string)
  VLR-NOVO:    #VLR-RETORNO do banco (string)
  DESCRICAO:  'DIVERGENCIA VALOR SIFAP X BANCO'
AND END TRANSACTION
AND #QTD-AUDIT += 1
```
**Tipo**: Auditoria  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L253-L270`  
**Notas**:
- ACAO 'DV' = divergência detectada
- Guarda ambos os valores (SIFAP e banco) em VLR-ANTERIOR/VLR-NOVO
- Divergências requerem investigação manual — relatório deve listar todas

---

### REQ-CON-011: Emitir Resumo de Conciliação
```
GIVEN processamento completo
WHEN imprime resumo
THEN:
  WRITE "BATCHCON - RESUMO CONCILIACAO"
  WRITE "REGISTROS LIDOS........: {#QTD-LIDOS}"
  WRITE "CONCILIADOS............: {#QTD-CONCILIADOS}"
  WRITE "DIVERGENTES............: {#QTD-DIVERGENTES}"
  WRITE "NAO ENCONTRADOS........: {#QTD-NAO-ENCONTRADOS}"
  WRITE "REGISTROS AUDITORIA....: {#QTD-AUDIT}"
```
**Tipo**: Relatório  
**Prioridade**: ALTA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L227-L235`  
**Reconciliação**:
```
#QTD-LIDOS ≥ #QTD-CONCILIADOS + #QTD-DIVERGENTES + #QTD-NAO-ENCONTRADOS
(≥ por causa de registros não-detalhe que só incrementam #QTD-LIDOS)

#QTD-AUDIT = #QTD-CONCILIADOS + #QTD-DIVERGENTES
(cada conciliado E cada divergente gera auditoria)
```
**Notas**:
- #QTD-DIVERGENTES > 0 exige investigação urgente
- #QTD-NAO-ENCONTRADOS > 0 indica desincronização SIFAP/banco
- #QTD-AUDIT deve bater com SELECT COUNT(*) FROM AUDITORIA WHERE DT-EVENTO = hoje

---

### REQ-CON-012: Preservar Código Histórico Banco Real (Dead Code)
```
GIVEN código comentado de integração Banco Real (linhas 207-224)
WHEN revisar ou manter código
THEN MUST NOT reativar ou remover sem análise histórica
AND MUST documentar: integração descontinuada em 2007 (Santander adquiriu Banco Real)
AND Diferenças do layout Banco Real vs BB:
  - CPF: posição 30-43 (vs 44-54 no BB)
  - Valor: posição 100-112 (vs 120-134 no BB)
  - Código banco: '356' (vs '001' no BB)
```
**Tipo**: Arqueológico / Documentação  
**Prioridade**: MÉDIA  
**Complexidade**: Baixa  
**Rastreabilidade**: `source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L206-L224`  
**Notas**:
- Referência histórica: Banco Real foi adquirido pelo Santander em 2007
- Layout CNAB Real difere do BB — útil se futura integração Santander for necessária
- Manter como comentário no código modernizado com link para este documento

---

## Requisitos Não-Funcionais

### RNF-CON-001: Atomicidade por Registro
```
GIVEN cada atualização de STATUS-PGTO
WHEN executa UPDATE
THEN usa transação individual (UPDATE + END TRANSACTION)
  E NÃO agrupa atualizações (commit por registro)
```
**Enforcement**: END TRANSACTION após cada UPDATE (linhas 179, 186, 193)  
**Rastreabilidade**: `source_legacy: BATCHCON.NSN#L178-L179, #L185-L186, #L192-L193`

---

### RNF-CON-002: Auditoria Obrigatória
```
GIVEN qualquer conciliação ou divergência
WHEN processa registro
THEN MUST gravar registro na tabela AUDITORIA
  - Conciliação: ACAO = 'CO'
  - Divergência: ACAO = 'DV'
```
**Enforcement**: PERFORM GRAVA-AUDITORIA-* em todos os caminhos  
**Rastreabilidade**: `source_legacy: BATCHCON.NSN#L167, #L201`

---

### RNF-CON-003: Tolerância de Divergência R$ 0,01
```
GIVEN diferença de valor SIFAP vs banco
WHEN |SIFAP - BANCO| ≤ 0.01
THEN considera conciliado (não divergente)
```
**Enforcement**: IF #DIFF > 0.01 (linha 160)  
**Rastreabilidade**: `source_legacy: BATCHCON.NSN#L160`  
**Notas**: Tolerância absorve diferenças de arredondamento entre BATCHPGT (trunca) e banco (arredonda).

---

### RNF-CON-004: Rastreabilidade de Hora de Evento
```
GIVEN execução do batch
WHEN grava auditoria
THEN registra também HORA de evento (HR-EVENTO = #HR-ATUAL = *TIMN)
  (além da data)
```
**Enforcement**: MOVE *TIMN TO #HR-ATUAL (linha 80)  
**Rastreabilidade**: `source_legacy: BATCHCON.NSN#L80, #L243`

---

### RNF-CON-005: Idempotência (Execução Segura Múltipla)
```
GIVEN arquivo de retorno já processado
WHEN executa novamente
THEN pode atualizar STATUS-PGTO múltiplas vezes
  (sem verificação de idempotência — RISCO)
```
**Enforcement**: NÃO IMPLEMENTADO no legado — risco latente  
**Rastreabilidade**: `source_legacy: BATCHCON.NSN#L173-L193`  
**Notas**: ⚠️ Ausência de idempotência é risco — deve ser adicionada no Estágio 3.

---

## Mapeamento de Dados

### Entrada: Arquivo CNAB 240 (flat-file ASCII)
```
Registro de 240 bytes, posições fixas:
Pos  1-3   (A3):   Código do banco
Pos  4-7   (A4):   Número do lote
Pos  8     (A1):   Tipo registro ('3' = detalhe)
Pos 44-54 (A11):   CPF beneficiário
Pos 74-83 (A10):   Número documento (= NUM-PAGTO)
Pos 120-134 (A15): Valor em centavos (inteiro, sem ponto)
Pos 140-147 (A8):  Data pagamento (DDMMAAAA)
Pos 231-232 (A2):  Código de retorno ('00'/'01'/'02')
```

### Entrada: PAGAMENTO (FIND para leitura + UPDATE)
```
NUM-PAGTO          (N10)      ← Chave de busca (= #CNAB-NUM-DOC)
CPF-BENEF          (N11)      ← Validação cruzada
COMPETENCIA        (N6)       ← Validação cruzada
VLR-LIQUIDO        (N9.2)     ← Comparado com #VLR-RETORNO
STATUS-PGTO        (A1)       ← Atualizado ('G' → 'P'/'D'/'E')
DT-PAGAMENTO       (N8)       ← Atualizado se '00'
COD-BANCO          (N3)       ← Atualizado se '00' (hardcoded=1)
COD-RETORNO        (A2)       ← Atualizado com código do banco
```

### Saída: AUDITORIA (INSERT)
```
SEQ-AUDIT          (N10)      ← Sequencial (PK)
DT-EVENTO          (N8)       ← Data de execução do batch
HR-EVENTO          (N6)       ← Hora de execução (HHMMSS)
USUARIO            (A8)       ← 'BATCH' (fixo)
ACAO               (A2)       ← 'CO' (conciliado) ou 'DV' (divergente)
TABELA-REF         (A15)      ← 'PAGAMENTO'
CHAVE-REF          (A20)      ← NUM-PAGTO
VLR-ANTERIOR       (A60)      ← VLR-LIQUIDO SIFAP (apenas em DV)
VLR-NOVO           (A60)      ← VLR-RETORNO banco (apenas em DV)
DESCRICAO          (A80)      ← Texto descritivo do evento
```

---

## Regras de Negócio Críticas

| ID | Regra | Risco | Origem |
|----|-------|-------|--------|
| BR-001 | Processar APENAS tipo='3' (detalhe CNAB) | **CRÍTICO** | L116-118, filtro de detalhe |
| BR-002 | Chave de busca: NUM-PAGTO + CPF + COMPETENCIA | **CRÍTICO** | L139-144, tripla validação |
| BR-003 | Valor CNAB em centavos ÷ 100 = reais | **CRÍTICO** | L132, conversão obrigatória |
| BR-004 | Tolerância de divergência = R$ 0,01 | **CRÍTICO** | L160, absorve arredondamento |
| BR-005 | COD-RET '00' → 'P', '01' → 'D', '02' → 'E' | **CRÍTICO** | L171-199, ciclo de status |
| BR-006 | Auditoria obrigatória para CO e DV | **ALTO** | L167,201, compliance |
| BR-007 | COD-BANCO hardcoded = 1 (BB) | **ALTO** | L176, limitação atual |
| BR-008 | Dead code Banco Real — não reativar sem spec | **MÉDIO** | L206-224, histórico 2007 |

---

## Decisões de Design (ADRs)

### ADR-001: Tolerância de R$ 0,01
**Decisão**: Aceitar diferença de até R$ 0,01 como conciliado  
**Racional**: Diferença de arredondamento sistemática entre BATCHPGT (trunca) e banco (arredonda). Sem tolerância, todos os pagamentos seriam divergentes.  
**Impacto**: Em Java 21, usar `BigDecimal.abs().compareTo(new BigDecimal("0.01")) <= 0`.

### ADR-002: Posições CNAB 240 Hardcoded
**Decisão**: Posições de parsing hardcoded para layout Banco do Brasil  
**Racional**: Código foi escrito em 2000, alterado em 2008 para CNAB 240 BB.  
**Impacto**: Em Estágio 3, criar `CnabLayoutDefinition` parametrizável por banco. Suportar pelo menos: BB (001), CEF (104), Santander (033).

### ADR-003: Auditoria com AÇÃO 'CO'/'DV'
**Decisão**: Usar dois tipos de ação de auditoria (CO e DV)  
**Racional**: Distinção para relatórios analíticos: conciliados vs divergentes.  
**Impacto**: Enum `AcaoAuditoria { CONCILIADO("CO"), DIVERGENTE("DV") }` no domain.

### ADR-004: Idempotência Ausente (Risco)
**Decisão**: Legado não previne re-processamento do mesmo arquivo  
**Racional**: Não havia controle. Em Estágio 3, adicionar: verificar se NUM-PAGTO já tem STATUS='P'/'D'/'E' antes de atualizar.  
**Impacto**: Adicionar coluna `dt_conciliacao` e check de idempotência no service.

---

## Critérios de Aceitação (DoD)

- [ ] INPUT recebe competência + caminho do arquivo CNAB 240
- [ ] Abre arquivo flat-file ASCII (WORK FILE)
- [ ] Filtra apenas registros tipo='3' (detalhe)
- [ ] Parseia 8 campos por posição fixa conforme layout BB
- [ ] Converte valor CNAB (centavos) para reais (÷ 100)
- [ ] Busca PAGAMENTO por NUM-PAGTO + CPF + COMPETENCIA
- [ ] Calcula diferença absoluta SIFAP vs banco
- [ ] Tolerância de R$ 0,01 corretamente aplicada
- [ ] Atualiza STATUS para 'P'/'D'/'E' conforme código retorno '00'/'01'/'02'
- [ ] Grava auditoria 'CO' para cada conciliado
- [ ] Grava auditoria 'DV' para cada divergente (com ambos os valores)
- [ ] Log de "não encontrados" com CPF e número de documento
- [ ] Relatório final com todos os contadores
- [ ] Reconciliação: #QTD-AUDIT = #QTD-CONCILIADOS + #QTD-DIVERGENTES
- [ ] Testes com arquivo CNAB 240 mock (5+ registros)
- [ ] Teste de idempotência (re-processamento do mesmo arquivo)
- [ ] Teste de tolerância R$ 0,01 (valor exato + valor ligeiramente diferente)

---

## Pontos de Atenção / Riscos

### ⚠️ RISCO 1: Idempotência Ausente
O legado não verifica se um pagamento já foi conciliado antes de atualizar.  
**Mitigação**: Adicionar verificação em Estágio 3:
```java
if (pagamento.getStatus() != StatusPagamento.GERADO) {
    log.warn("Pagamento {} já processado: {}", numPagto, pagamento.getStatus());
    return;
}
```

### ⚠️ RISCO 2: COD-BANCO Hardcoded = 1
Se houver integração com outro banco (ex: CEF, Santander), COD-BANCO sempre será 1.  
**Mitigação**: Extrair código do banco do próprio arquivo CNAB (posição 1-3).

### ⚠️ RISCO 3: Código Retorno Desconhecido — Falha Silenciosa
Código de retorno não mapeado ('00'/'01'/'02' são os únicos) loga mensagem mas não atualiza status.  
**Mitigação**: Lançar exceção verificada ou enviar alerta operacional.

### ⚠️ RISCO 4: Data CNAB em Formato DDMMAAAA
Formato da data no arquivo CNAB (DDMMAAAA) difere do formato SIFAP (AAAAMMDD — YYYYMMDD).  
**Mitigação**: Converter explicitamente no parser CNAB.

### ⚠️ RISCO 5: Dead Code Banco Real
Código comentado (linhas 207-224) para Banco Real (CNAB com layout diferente).  
**Decisão**: Preservar como comentário com referência documental. Não reativar sem spec.

---

## Próximas Fases

### Estágio 2 → 3 (Clarification & Planning)
1. **Idempotência**: Como garantir que o mesmo arquivo não seja processado duas vezes?
2. **Múltiplos bancos**: Suportar outros bancos além do BB no futuro próximo?
3. **Divergências**: Quem investiga? Existe SLA? Workflow de resolução?
4. **Notificação**: Alertar automaticamente se #QTD-DIVERGENTES > 0?
5. **Formato de data CNAB**: Confirmar DDMMAAAA vs AAAAMMDD na especificação com banco?

### Estágio 3 (Implementation)
- [ ] Criar `CnabParser` (CNAB 240 layout BB)
- [ ] Criar `ConciliacaoService` com tolerância configurável
- [ ] Criar `AuditoriaRepository` (INSERT com sequencial)
- [ ] Implementar `PagamentoRepository.updateStatus()`
- [ ] Testes com fixture CNAB 240 (arquivo mock)
- [ ] Testes parametrizados: COD-RET '00'/'01'/'02'/desconhecido
- [ ] Teste de tolerância (diff=0.00, 0.01, 0.011)
- [ ] Teste de idempotência (re-run mesmo arquivo)
- [ ] Integração com Testcontainers (PostgreSQL)

---

## Rastreabilidade Completa

| Estágio | Artefato | Linha | Descrição |
|---------|----------|-------|-----------|
| 1 (Arqueologia) | BATCHCON.NSN | L1-11 | Cabeçalho + histórico |
| 1 (Arqueologia) | BATCHCON.NSN | L13-77 | DEFINE DATA |
| 1 (Arqueologia) | BATCHCON.NSN | L206-224 | Dead code Banco Real (histórico 2007) |
| 2 (Spec) | Este documento | - | Requisitos EARS + ADRs |
| 2 (Spec) | DDM PAGAMENTO | - | Estrutura input/output |
| 2 (Spec) | DDM AUDITORIA | - | Estrutura output auditoria |
| 3 (Implementação) | src/main/java/domain/conciliacao/ | - | Classes Domain |
| 3 (Implementação) | src/main/java/service/ | - | ConciliacaoService |
| 3 (Implementação) | src/main/java/cnab/ | - | CnabParser + layouts |
| 3 (Implementação) | src/test/resources/ | - | Fixtures CNAB 240 mock |
| 3 (Implementação) | src/test/java/ | - | ConciliacaoServiceTest |
| 4 (Evolução) | GitHub Actions | - | Workflow de conciliação |
| 4 (Evolução) | Alerting | - | Notificação divergências |

---

## Comparação: Os Três Batches SIFAP

| Aspecto | BATCHPGT | BATCHREL | BATCHCON |
|---------|----------|----------|----------|
| **Objetivo** | Gerar pagamentos | Relatar pagamentos | Conciliar pagamentos |
| **Entrada** | BENEFICIARIO | PAGAMENTO | Arquivo CNAB 240 |
| **Processamento** | Cálculo com 4 fatores | Somatórios regionais | Parsing + reconciliação |
| **Saída principal** | INSERT PAGAMENTO | Flat-file relatório | UPDATE PAGAMENTO + INSERT AUDITORIA |
| **Status produzido** | 'G' (gerado) | Leitura apenas | 'P'/'D'/'E' (pós-banco) |
| **Arredondamento** | TRUNCAR | ARREDONDAR +0.005 | NENHUM (centavos÷100) |
| **Tolerância** | N/A | N/A | R$ 0,01 |
| **Auditoria** | Não | Não | **SIM** ('CO'/'DV') |
| **Frequência** | Mensal (automático) | On-demand | Após retorno bancário |
| **Complexidade** | Alta (fórmulas) | Baixa (somas) | Alta (parsing + regras) |
| **Risco** | Financeiro CRÍTICO | Consultivo | Financeiro + Compliance |

---

**Versão da Especificação**: 1.0.0 Draft  
**Data de Ratificação**: [Pendente revisão Estágio 2]  
**Última Alteração**: 2026-05-27  
**Ratificado por**: [Arquiteto Enterprise + Software Architect]
