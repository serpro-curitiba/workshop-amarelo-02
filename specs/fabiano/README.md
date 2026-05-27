# 📋 Especificações SIFAP 2.0 — Workspace

**Data**: 2026-05-27  
**Status**: Stage 2 (Specification)  
**Versão Constitution**: 1.0.0

---

## 📁 Arquivos Neste Workspace

### 1. **BATCHPGT-specification.md** (20 KB)
   - **Objetivo**: Geração mensal de pagamentos em lote
   - **Requisitos**: 16 EARS (8 críticos, 5 altos, 3 médios)
   - **Rastreabilidade**: 100% ao BATCHPGT.NSN
   - **Destaques**:
     - 4 fatores de cálculo (regional, familiar, renda, idade)
     - Truncagem (não arredondar) — contábil
     - 13º + abono em dezembro
     - Output: INSERT PAGAMENTO

### 2. **BATCHREL-specification.md** (20 KB)
   - **Objetivo**: Consolidação e relatórios mensais
   - **Requisitos**: 13 EARS (5 críticos, 5 altos, 2 médios)
   - **Rastreabilidade**: 100% ao BATCHREL.NSN
   - **Destaques**:
     - 5 macro-regiões (N/NE/SE/S/CO)
     - 5 status (G/P/C/D/E)
     - Arredondamento (+0.005) — diferente de BATCHPGT
     - Output: Flat-file Mainframe

### 3. **README.md** (este arquivo)
   - Instruções de uso e próximas etapas

---

## 🎯 Como Usar Estas Especificações

### Para Product Owner
```
1. Abra BATCHPGT-specification.md
2. Leia "Resumo Executivo" + "Regras de Negócio Críticas"
3. Aprove os 16 requisitos (assine)
4. Repita para BATCHREL (13 requisitos)
```

### Para Arquiteto Enterprise/Software
```
1. Abra BATCHPGT-specification.md
2. Valide "Mapeamento de Dados" + "ADRs"
3. Revise "Próximas Fases" e confira alinhamento
4. Repita para BATCHREL
5. Aprove ADRs (assinando)
```

### Para Developer
```
1. Abra BATCHPGT-specification.md
2. Vá para "Critérios de Aceitação"
3. Implemente cada REQ-BATCH-### em Estágio 3
4. Use source_legacy para verificar código Natural
5. Escreva testes baseados em cada regra crítica
```

### Para QA/Tester
```
1. Abra BATCHPGT-specification.md → "Critérios de Aceitação"
2. Crie casos de teste para:
   - Cada fator de cálculo (regional, familiar, renda, idade)
   - Arredondamento/truncagem
   - 13º + abono em dezembro
   - Deduplicação CPF
3. Repita para BATCHREL com testes de mapeamento
```

---

## 🔄 Fluxo de Aprovação (Stage 2)

```
┌─────────────────────┐
│  Specs Geradas ✓    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────┐
│ 1. Revisar com @architect   │
│    (validar req + ADRs)     │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│ 2. Clarificar Pontos Abertos│
│    (5 por spec)             │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│ 3. Aprovar com Assinatura   │
│    (PO + EA + SA)           │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│ Estágio 3 (Implementação)   │
│ /speckit.tasks              │
└─────────────────────────────┘
```

---

## 📊 Pontos Abertos para Clarificação

### BATCHPGT
1. **Revalidação CPF/NIS**: Usar APIs externas ou confiar no cadastro?
2. **Tratamento de erros**: Falha o batch se programa não encontrado, ou logging + continue?
3. **Parametrização**: Quando mudam os fatores (13º, descontos)?
4. **Rollback**: Estratégia de reversão se falhar no meio?
5. **Performance**: Qual é aceitável? 60 min é limite ou alvo?

### BATCHREL
1. **Período de execução**: Roda junto com BATCHPGT (1º dia) ou on-demand?
2. **Validação competência**: Validar se competência existe antes de ler?
3. **Consolidação multi-período**: Suportar relatório de 3 meses acumulado?
4. **Exportação**: Apenas flat-file ou também PDF/Excel?
5. **Parametrização regiões**: User-defined grouping de regiões?

---

## 🚀 Próximas Ações (Ordem Recomendada)

### ✅ **AGORA (Stage 2 - Hoje)**
- [ ] Revisar ambas specs com @architect
- [ ] Validar rastreabilidade (source_legacy)
- [ ] Clarificar 5 pontos abertos cada
- [ ] Aprovar ADRs com assinatura
- [ ] Versionar specs em Git (commit + tag)

### 📋 **PRÓXIMO (Stage 2 - Amanhã)**
- [ ] Executar `/speckit.clarify` para ambas
- [ ] Executar `/speckit.tasks` para gerar task breakdown
- [ ] Executar `/speckit.taskstoissues` para criar GitHub issues
- [ ] Revisar tasks com @technical-lead

### 🔨 **ESTÁGIO 3 (Implementação - 4-5 dias)**
- [ ] Criar domain classes (Beneficiario, Pagamento, etc)
- [ ] Implementar BPagamentoCalculator (fatores)
- [ ] Testes parametrizados (JUnit 5)
- [ ] BATCHPGT repository + service
- [ ] BATCHREL aggregation + formatter
- [ ] Testcontainers + CI/CD

### 🚀 **ESTÁGIO 4 (Evolução - 2-3 dias)**
- [ ] Scheduler (Quartz para 1º dia útil)
- [ ] Reconciliação automática
- [ ] Monitoring + SLA
- [ ] Deploy em produção

---

## 🔗 Referências

- **Constitution**: `.specify/memory/constitution.md`
- **Legado BATCHPGT**: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **Legado BATCHREL**: `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN`
- **Stack**: Java 21 + Spring Boot 3.3 + PostgreSQL 16 + Next.js 15
- **Team Flow**: `00-TEAM-FLOW.md` (passagens entre estágios)

---

## 🎓 Diferença Crítica: BATCHPGT vs BATCHREL

| Aspecto | BATCHPGT | BATCHREL |
|---------|----------|----------|
| **Arredondamento** | 🔴 TRUNCA | 🟠 ARREDONDA +0.005 |
| **Impacto** | Contábil preciso | Consultivo (pode não fechar) |
| **Reconciliação** | Com banco (SELECT SUM) | Com volume (QTD) |
| **Reversão** | ⚠️ Complexa (DELETE + reprocessar) | ✓ Simples (re-run) |

**Implicação**: Relatórios NUNCA fecham 100% com BATCHPGT → design intencional.

---

## 💬 Dúvidas Frequentes

**P: Onde estão os testes?**  
R: Testes são criados em Estágio 3. As specs possuem "Critérios de Aceitação" que guiam a escrita dos testes.

**P: Por que arredondar em BATCHREL e truncar em BATCHPGT?**  
R: BATCHPGT é executivo (valores reais); BATCHREL é consultivo (legibilidade). Comentário no código (L136) valida isso.

**P: Posso mudar o mapeamento de regiões?**  
R: ADR-002 recomenda parametrizar (criar tabela `tab_regiao_grupo`). Fale com @architect antes.

**P: E se houver erro em BATCHPGT?**  
R: RNF-BATCH-005 recomenda rollback completo. Strategy de retry depende de requerimento de Estágio 2.

---

## 📝 Assinatura de Aprovação

```
Especificações Aprovadas por:

Product Owner: ________________   Data: _______
Enterprise Architect: ________________   Data: _______
Software Architect: ________________   Data: _______
Technical Lead: ________________   Data: _______
```

---

**Próximo**: `/speckit.clarify` para ambas as specs.
