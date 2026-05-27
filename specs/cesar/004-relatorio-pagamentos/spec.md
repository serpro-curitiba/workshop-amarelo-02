# Feature Specification: Relatorio Analitico de Pagamentos Modernizado

**Feature Branch**: `004-modernize-relpgt-report`

**Created**: 2026-05-27

**Status**: Draft

**Input**: User description: "Modernizar a funcionalidade legada RELPGT (relatorio analitico de pagamentos por periodo) para a stack alvo do projeto SIFAP 2.0, preservando comportamento observavel, rastreabilidade legada e requisitos de seguranca/operabilidade definidos na constituicao do repositorio."

## Clarifications

### Session 2026-05-27

- Q: Como tratar pagamento sem beneficiario correspondente em BENEFICIARIO? -> A: Incluir o registro orfao no relatorio com nome/UF vazios (ou marcador "NAO ENCONTRADO") e contabilizar nos totais.
- Q: Como tratar os limites do intervalo de competencia (inicial e final)? -> A: Intervalo inclusivo nas duas pontas (`COMP-INI <= competencia <= COMP-FIM`).
- Q: Como tratar filtro por programa inexistente? -> A: Retornar relatorio vazio (sem linhas), com totais zerados e indicacao clara de "programa sem dados no periodo".
- Q: Qual ordenacao aplicar na listagem detalhada do relatorio? -> A: Ordenar por codigo de programa ascendente e, dentro do programa, por competencia ascendente e numero do pagamento ascendente.
- Q: Como tratar competencia inicial maior que competencia final? -> A: Retornar erro de validacao.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Emitir relatorio por periodo (Priority: P1)

Como operador, quero gerar um relatorio analitico de pagamentos por intervalo de competencia para acompanhar valores e situacao de pagamentos.

**Why this priority**: Esta e a finalidade central do RELPGT no legado e entrega valor imediato para operacao mensal.

**Independent Test**: Deve ser possivel gerar o relatorio para um periodo conhecido e validar listagem detalhada, subtotais por programa e total geral.

**Acceptance Scenarios**:

1. **Given** um periodo valido com pagamentos, **When** o operador solicita o relatorio, **Then** o sistema retorna listagem detalhada por pagamento com os campos esperados.
2. **Given** um periodo valido com pagamentos de multiplos programas, **When** o relatorio e gerado, **Then** o sistema apresenta subtotal por programa e total geral consolidado.
3. **Given** um periodo sem pagamentos, **When** o relatorio e solicitado, **Then** o sistema retorna resposta vazia com indicacao clara de ausencia de dados.
4. **Given** competencia inicial e final validas, **When** o relatorio e executado, **Then** o sistema inclui registros com competencia igual a inicial e igual a final.
5. **Given** competencia inicial maior que competencia final, **When** o relatorio e solicitado, **Then** o sistema retorna erro de validacao e nao processa a consulta.

---

### User Story 2 - Aplicar filtros e mapeamentos de dominio (Priority: P2)

Como operador, quero filtrar por codigo de programa e visualizar status/tipo com descricao legivel para facilitar a analise.

**Why this priority**: Preserva regras observaveis do legado e reduz erros de interpretacao operacional.

**Independent Test**: Deve ser possivel comparar execucao com filtro por programa especifico e sem filtro (0=TODOS), alem de verificar traducao de status e tipo.

**Acceptance Scenarios**:

1. **Given** codigo de programa informado como 0, **When** o relatorio e executado, **Then** o sistema inclui pagamentos de todos os programas no periodo.
2. **Given** codigo de programa especifico valido, **When** o relatorio e executado, **Then** o sistema inclui somente registros daquele programa.
3. **Given** pagamentos com codigos de status e tipo conhecidos, **When** o relatorio e apresentado, **Then** o sistema exibe codigo e descricao correspondente para cada item.
4. **Given** pagamento sem beneficiario correspondente em BENEFICIARIO, **When** o relatorio e executado, **Then** o sistema inclui o registro no detalhamento com nome/UF vazios (ou marcador padrao) e contabiliza o item nos totais.
5. **Given** codigo de programa inexistente para o periodo consultado, **When** o relatorio e executado, **Then** o sistema retorna relatorio vazio com totais zerados e indicacao clara de ausencia de dados para o programa.
6. **Given** um conjunto de pagamentos no periodo, **When** o relatorio e gerado, **Then** a listagem detalhada e ordenada por codigo de programa ascendente e, dentro de cada programa, por competencia ascendente e numero do pagamento ascendente.

---

### User Story 3 - Proteger dados sensiveis e garantir operabilidade (Priority: P3)

Como time de operacoes e seguranca, quero CPF mascarado em saidas e logs, e verificacoes de prontidao para operacao segura e rastreavel.

**Why this priority**: Atende requisitos constitucionais de seguranca e operabilidade como parte do entregavel.

**Independent Test**: Deve ser possivel auditar uma execucao do relatorio e comprovar ausencia de CPF integral em saidas e logs, com checklist de prontidao preenchivel.

**Acceptance Scenarios**:

1. **Given** relatorio gerado com dados de beneficiarios, **When** a saida e exibida ao usuario, **Then** CPF deve aparecer apenas mascarado.
2. **Given** execucao do relatorio em ambiente monitorado, **When** logs sao coletados, **Then** nao deve haver CPF integral em eventos de aplicacao.
3. **Given** fluxo implantado, **When** equipe operacional executa checklist de prontidao, **Then** dependencia de dados, sinais de monitoracao e procedimento de incidente estao documentados.

---

### Edge Cases

- Competencia inicial maior que competencia final deve retornar erro de validacao e interromper o processamento.
- Registros com competencia igual a inicial e igual a final DEVEM ser incluidos no relatorio.
- Filtro por programa inexistente deve retornar relatorio vazio com totais zerados e indicacao clara de ausencia de dados para o programa no periodo.
- Pagamento sem beneficiario correspondente em BENEFICIARIO deve permanecer no relatorio com nome/UF vazios (ou marcador padrao) e ser contabilizado nos totais.
- A ordenacao do detalhamento deve ser deterministica para evitar divergencia em testes e reconciliacao de totais.
- Como o sistema trata codigos de status/tipo fora da tabela conhecida?
- Como o sistema trata volume elevado sem degradacao severa?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (REQ-RELPGT-001)**: O sistema DEVE gerar relatorio analitico de pagamentos por intervalo de competencia.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-002 (REQ-RELPGT-002)**: O sistema DEVE aceitar filtro opcional por codigo de programa, onde `0` significa todos os programas.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-002A (REQ-RELPGT-002A)**: O sistema DEVE tratar o intervalo de competencia de forma inclusiva nas duas pontas (`COMP-INI <= competencia <= COMP-FIM`).
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-003 (REQ-RELPGT-003)**: O sistema DEVE listar por pagamento os campos de competencia, CPF mascarado, nome, UF, valor bruto, valor desconto, valor liquido, status e tipo.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm`
- **FR-004 (REQ-RELPGT-004)**: O sistema DEVE calcular e exibir subtotal por codigo de programa contendo quantidade, total bruto e total liquido.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-005 (REQ-RELPGT-005)**: O sistema DEVE calcular e exibir total geral com quantidade total de registros, total bruto, total desconto, total liquido e total abono.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-006 (REQ-RELPGT-006)**: O sistema DEVE mapear tipo de pagamento para descricao com a tabela `N=NORMAL`, `D=DECIMO`, `T=TERCEIRO`, fallback `OUTRO`.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-007 (REQ-RELPGT-007)**: O sistema DEVE mapear status de pagamento para descricao com a tabela `G=GERADO`, `P=PAGO`, `C=CANCELAD`, `D=DEVOLVID`, `E=ESTORNAD`, fallback `OUTRO`.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-008 (REQ-RELPGT-008)**: O sistema DEVE retornar estado claro para periodo sem pagamentos, incluindo metadados de execucao.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-009 (REQ-RELPGT-009)**: O sistema DEVE mascarar CPF nas saidas de usuario do relatorio.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
- **FR-010 (REQ-RELPGT-010)**: O sistema DEVE garantir que logs operacionais nao exponham CPF integral.
  - **source_legacy**: `[GREENFIELD] Requisito de seguranca para observabilidade moderna conforme constituicao.`
- **FR-011 (REQ-RELPGT-011)**: O sistema DEVE fornecer dois formatos de saida: estruturado para consumo de API e tabular para leitura humana equivalente funcional ao legado.
  - **source_legacy**: `[GREENFIELD] Requisito para compatibilidade de canais modernos mantendo equivalencia funcional.`
- **FR-012 (REQ-RELPGT-012)**: O sistema DEVE registrar parametros de execucao do relatorio para auditabilidade sem vazar dados sensiveis.
  - **source_legacy**: `[GREENFIELD] Requisito de auditabilidade da plataforma moderna.`
- **FR-013 (REQ-RELPGT-013)**: O sistema DEVE definir verificacoes de prontidao e operabilidade para dependencias de PAGAMENTO e BENEFICIARIO e procedimento de incidente associado.
  - **source_legacy**: `[GREENFIELD] Requisito operacional exigido pela constituicao.`
- **FR-014 (REQ-RELPGT-014)**: O sistema DEVE incluir pagamentos sem correspondencia em BENEFICIARIO no relatorio, mantendo o item nos totais e preenchendo nome/UF como vazio ou marcador padrao configurado.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de representacao de nome/UF para registros orfaos na saida moderna.`
- **FR-015 (REQ-RELPGT-015)**: O sistema DEVE retornar relatorio vazio com totais zerados e mensagem clara de "programa sem dados no periodo" quando o filtro de programa nao possuir registros no intervalo informado.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de mensagem e totais para ausencia de dados por filtro no canal moderno.`
- **FR-016 (REQ-RELPGT-016)**: O sistema DEVE ordenar o detalhamento por codigo de programa ascendente e, dentro de cada programa, por competencia ascendente e numero do pagamento ascendente.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de ordenacao deterministica para canais modernos e testes automatizados.`
- **FR-017 (REQ-RELPGT-017)**: O sistema DEVE validar o intervalo informado e retornar erro de validacao quando `competencia inicial > competencia final`.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de semantica de erro para API moderna.`

### Key Entities *(include if feature involves data)*

- **FiltroRelatorioPagamento**: criterio de consulta contendo competencia inicial, competencia final e codigo de programa.
- **LinhaRelatorioPagamento**: item detalhado do relatorio com identificacao do pagamento, beneficiario e valores.
- **SubtotalPrograma**: agregacao por programa com quantidade e totais financeiros.
- **ResumoGeralRelatorio**: totalizador consolidado de quantidade e valores do periodo.
- **ResultadoRelatorioPagamentos**: envelope de saida contendo linhas detalhadas, subtotais, total geral e metadados de execucao.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 95% das execucoes para periodos com ate 1 milhao de registros retornam resultado em ate 10 segundos em horario comercial.
- **SC-002**: 100% das saidas de usuario exibem CPF mascarado.
- **SC-003**: 100% dos totais gerais batem com a soma das linhas detalhadas do periodo.
- **SC-004**: 100% dos subtotais por programa batem com a soma das linhas de cada programa.
- **SC-005**: 0 ocorrencias de CPF integral em logs de execucao apos implantacao.

## Assumptions

- As estruturas de dados de PAGAMENTO e BENEFICIARIO permanecem semanticamente compatveis com o legado para esta funcionalidade.
- O codigo de programa `0` continuara representando consulta sem filtro de programa.
- A equivalencia funcional com o relatorio legado e priorizada sobre reproduzir restricoes fisicas de impressora mainframe.
- Regras de negocio de calculo de beneficio fora do escopo (por exemplo, CALCBENF) nao serao alteradas nesta feature.
