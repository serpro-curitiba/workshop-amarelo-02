# Feature Specification: Relatorio de Trilha de Auditoria Modernizado

**Feature Branch**: `005-modernize-relaudit-report`

**Created**: 2026-05-27

**Status**: Draft

**Input**: User description: "Modernizar a funcionalidade legada RELAUDIT (relatorio de trilha de auditoria do sistema) para a stack alvo do projeto SIFAP 2.0, preservando comportamento observavel, rastreabilidade legada e requisitos de seguranca/operabilidade definidos na constituicao do repositorio."

## Clarifications

### Session 2026-05-27

- Q: Como deve ser o comportamento quando data inicial e/ou data final nao forem informadas? -> A: Preservar legado: data inicial = 19970101 quando ausente e data final = data atual quando ausente.
- Q: Como tratar acoes validas no AUDITORIA.ddm que nao estao no mapeamento principal do relatorio (ex.: LG, LO, BT, ER, AU, RE)? -> A: Exibir na listagem e contabilizar em OUTROS, mantendo codigo original e descricao generica.
- Q: Se o usuario informar explicitamente filtro de acao EX, qual deve ser o comportamento? -> A: Manter regra global; eventos EX nao sao exibidos e devem ser contabilizados como filtrados.
- Q: Como tratar o campo de descricao detalhada do evento na saida padrao? -> A: Exibir com mascaramento de padroes sensiveis na saida padrao; liberar versao completa apenas em contexto autorizado de auditoria forense.
- Q: Qual ordenacao deve ser usada na listagem detalhada de eventos na saida padrao? -> A: Ordenar por data do evento ascendente, hora ascendente e sequencial de auditoria ascendente.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar trilha por periodo (Priority: P1)

Como operador de controle, quero consultar a trilha de auditoria por intervalo de datas para analisar eventos do sistema em um periodo definido.

**Why this priority**: A consulta por periodo e o fluxo principal do relatorio RELAUDIT e entrega valor imediato para operacao e auditoria.

**Independent Test**: Pode ser testada de forma independente executando consulta com periodo valido e validando listagem de eventos e resumo final.

**Acceptance Scenarios**:

1. **Given** um intervalo de datas valido com eventos registrados, **When** o relatorio e solicitado, **Then** o sistema retorna a listagem de eventos do periodo com resumo de totais.
2. **Given** um intervalo valido sem eventos, **When** o relatorio e solicitado, **Then** o sistema retorna resposta vazia com indicacao clara de ausencia de dados.
3. **Given** data inicial e final validas, **When** a consulta e executada, **Then** eventos nas datas-limite sao incluidos no resultado.
4. **Given** data inicial maior que data final, **When** a consulta e solicitada, **Then** o sistema retorna erro de validacao e nao processa a busca.
5. **Given** data inicial e/ou final ausentes, **When** a consulta e solicitada, **Then** o sistema aplica os defaults legados (inicial=19970101, final=data atual) antes de executar o relatorio.

---

### User Story 2 - Filtrar e classificar eventos (Priority: P2)

Como operador, quero aplicar filtros por acao, usuario e tabela, com classificacao legivel da acao, para localizar rapidamente eventos relevantes.

**Why this priority**: Preserva o comportamento observavel do legado e reduz esforco manual de analise.

**Independent Test**: Pode ser testada executando consultas com e sem filtros e conferindo exclusoes, mapeamentos e contadores por tipo de acao.

**Acceptance Scenarios**:

1. **Given** filtro de acao, usuario ou tabela informado, **When** o relatorio e executado, **Then** o sistema retorna apenas eventos que atendem aos filtros informados.
2. **Given** evento com acao EX na base, **When** o relatorio e executado, **Then** o sistema nao exibe esse evento na listagem padrao e contabiliza o item como filtrado.
3. **Given** eventos com codigos de acao mapeados, **When** o relatorio e exibido, **Then** o sistema apresenta codigo e descricao correspondente da acao.
4. **Given** evento com codigo de acao valido no DDM, mas fora do mapeamento principal do relatorio, **When** o relatorio e exibido, **Then** o sistema mantem o codigo original e classifica a descricao como OUTRA, contabilizando o item em OUTROS.
5. **Given** combinacao de filtros sem correspondencia, **When** a consulta e executada, **Then** o sistema retorna relatorio vazio com resumo consistente e indicacao clara de ausencia de dados para o filtro.
6. **Given** filtro de acao EX informado, **When** a consulta e executada, **Then** o sistema nao exibe eventos EX e contabiliza os registros correspondentes como filtrados no resumo.
7. **Given** um conjunto de eventos no periodo, **When** a listagem detalhada e gerada, **Then** os eventos sao ordenados por data do evento ascendente, hora ascendente e sequencial de auditoria ascendente.

---

### User Story 3 - Operar com seguranca e rastreabilidade (Priority: P3)

Como equipe de operacoes e conformidade, quero executar o relatorio com controles de seguranca, auditabilidade e prontidao para atendimento de incidente e governanca.

**Why this priority**: Garante aderencia aos requisitos constitucionais de seguranca, operabilidade e evidencia operacional.

**Independent Test**: Pode ser testada auditando logs e artefatos operacionais apos execucao do relatorio, verificando ausencia de vazamento e presenca de metadados operacionais.

**Acceptance Scenarios**:

1. **Given** execucao do relatorio em ambiente monitorado, **When** logs sao analisados, **Then** nao ha exposicao de dados sensiveis em texto integral.
2. **Given** execucao com filtros variados, **When** o evento operacional e registrado, **Then** os parametros da consulta sao auditaveis sem vazar dados sensiveis.
3. **Given** servico em operacao, **When** a equipe executa checklist de prontidao, **Then** dependencias de dados, sinais de monitoracao e procedimento de incidente estao documentados e verificaveis.
4. **Given** evento com descricao detalhada contendo padrao sensivel, **When** o relatorio padrao e gerado, **Then** a descricao e retornada com mascaramento; e apenas contexto autorizado de auditoria forense pode acessar versao completa.

---

### Edge Cases

- Data inicial maior que data final deve retornar erro de validacao.
- Intervalo de datas deve ser inclusivo nas duas pontas.
- Filtros combinados sem correspondencia devem retornar resultado vazio com resumo consistente.
- Eventos com acao EX devem ser removidos da exibicao padrao.
- Filtro explicito por acao EX nao altera a regra global de exclusao de EX na exibicao.
- Evento com acao nao mapeada deve usar fallback de descricao OUTRA.
- Acoes validas no DDM e fora do mapeamento principal devem permanecer visiveis, mantendo codigo original e sendo contabilizadas em OUTROS.
- Descricao detalhada deve aplicar mascaramento na saida padrao e permitir acesso completo somente em contexto autorizado de auditoria forense.
- A ordenacao da listagem detalhada deve ser deterministica por data, hora e sequencial de auditoria para evitar divergencia entre execucoes.
- Tipo de saida em branco deve assumir valor padrao de exibicao em tela.
- Parametro de tipo de saida invalido deve ser rejeitado com erro de validacao.
- Data inicial ausente deve assumir 19970101 e data final ausente deve assumir data atual.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (REQ-RELAUDIT-001)**: O sistema DEVE gerar relatorio de trilha de auditoria por intervalo de datas informado.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-002 (REQ-RELAUDIT-002)**: O sistema DEVE aplicar filtro opcional por acao, usuario e tabela, tratando valor em branco como sem restricao para o filtro correspondente.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-003 (REQ-RELAUDIT-003)**: O sistema DEVE considerar o intervalo de datas de forma inclusiva nas duas pontas.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-004 (REQ-RELAUDIT-004)**: O sistema DEVE excluir da exibicao padrao eventos com codigo de acao EX e contabilizar tais eventos como filtrados.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-005 (REQ-RELAUDIT-005)**: O sistema DEVE exibir, para cada evento listado, data do evento, hora formatada, usuario, acao (codigo e descricao), tabela de referencia e chave de referencia.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
- **FR-006 (REQ-RELAUDIT-006)**: O sistema DEVE incluir descricao detalhada do evento quando o formato de saida selecionado exigir detalhe expandido.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-007 (REQ-RELAUDIT-007)**: O sistema DEVE mapear codigos de acao para descricao legivel com tabela minima IN=INCLUSAO, AL=ALTERACAO, CO=CONCILIACAO, CN=CONSULTA, DV=DIVERGENCIA e fallback OUTRA.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
- **FR-008 (REQ-RELAUDIT-008)**: O sistema DEVE apresentar resumo final com total lido, total exibido, total filtrado e totais por tipo de acao (IN, AL, CO, CN, DV, OUTROS).
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-009 (REQ-RELAUDIT-009)**: O sistema DEVE retornar resposta deterministica para consultas sem dados, incluindo indicacao clara de ausencia de eventos e resumo consistente.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de contrato de retorno sem dados para canais modernos.`
- **FR-010 (REQ-RELAUDIT-010)**: O sistema DEVE suportar formato estruturado para consumo de API e formato tabular para consumo humano, mantendo equivalencia funcional do relatorio legado.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `[GREENFIELD] Requisito de compatibilidade entre canais modernos e legado.`
- **FR-011 (REQ-RELAUDIT-011)**: O sistema DEVE validar parametros de entrada e retornar erro de validacao quando data inicial for maior que data final.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de semantica de erro para canais modernos.`
- **FR-012 (REQ-RELAUDIT-012)**: O sistema DEVE assumir saida em tela quando tipo de saida for omitido.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
- **FR-013 (REQ-RELAUDIT-013)**: O sistema DEVE rejeitar tipo de saida invalido com mensagem de erro clara e sem processar o relatorio.
  - **source_legacy**: `[GREENFIELD] Regra de validacao explicita para robustez do contrato moderno.`
- **FR-017 (REQ-RELAUDIT-017)**: O sistema DEVE aplicar defaults legados para datas ausentes (`data inicial = 19970101` e `data final = data atual`) antes da validacao e da execucao da consulta.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de contrato de entrada para canal moderno.`
- **FR-014 (REQ-RELAUDIT-014)**: O sistema DEVE impedir exposicao de dados sensiveis em logs e saidas de usuario.
  - **source_legacy**: `[GREENFIELD] Requisito de seguranca da constituicao para observabilidade moderna.`
- **FR-015 (REQ-RELAUDIT-015)**: O sistema DEVE registrar parametros de execucao e metadados de consulta para auditoria operacional sem vazar dados sensiveis.
  - **source_legacy**: `[GREENFIELD] Requisito de auditabilidade da plataforma moderna.`
- **FR-016 (REQ-RELAUDIT-016)**: O sistema DEVE definir verificacoes de prontidao e procedimento de incidente para dependencia de dados de AUDITORIA.
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
  - **source_legacy**: `[GREENFIELD] Requisito operacional exigido pela constituicao.`
- **FR-018 (REQ-RELAUDIT-018)**: O sistema DEVE exibir eventos com codigos de acao validos no `AUDITORIA.ddm` e fora do mapeamento principal, mantendo o codigo original e classificando a descricao como OUTRA, com contabilizacao em OUTROS no resumo.
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de comportamento para compatibilidade entre codificacao legada e contrato moderno.`
- **FR-019 (REQ-RELAUDIT-019)**: O sistema DEVE manter a exclusao de eventos com acao EX da listagem em todos os cenarios, inclusive quando o filtro de acao for explicitamente EX, contabilizando esses registros como filtrados.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de precedencia de regras de filtro para contrato moderno.`
- **FR-020 (REQ-RELAUDIT-020)**: O sistema DEVE aplicar mascaramento de padroes sensiveis no campo de descricao detalhada na saida padrao e DEVE permitir acesso a descricao completa apenas em contexto autorizado de auditoria forense.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de controle de exposicao para texto livre em canais modernos.`
- **FR-021 (REQ-RELAUDIT-021)**: O sistema DEVE ordenar a listagem detalhada por data do evento ascendente, hora ascendente e sequencial de auditoria ascendente.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
  - **source_legacy**: `[GREENFIELD] Definicao explicita de ordenacao deterministica para canais modernos e testes automatizados.`

### Key Entities *(include if feature involves data)*

- **FiltroRelatorioAuditoria**: criterio de consulta com data inicial, data final, acao, usuario, tabela e tipo de saida.
- **EventoAuditoriaRelatorio**: item da listagem contendo dados do evento, classificacao da acao e referencias de rastreabilidade.
- **ResumoRelatorioAuditoria**: agregacao de totais gerais, exibidos, filtrados e contadores por tipo de acao.
- **ResultadoRelatorioAuditoria**: envelope de saida com lista de eventos, resumo final e metadados da execucao.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 95% das consultas de auditoria para periodos com ate 5 milhoes de eventos retornam resultado em ate 12 segundos em horario comercial.
- **SC-002**: 100% das consultas com intervalo de datas invalido sao bloqueadas com mensagem clara de validacao.
- **SC-003**: 100% dos eventos com acao EX permanecem fora da exibicao padrao e sao refletidos no total filtrado.
- **SC-004**: 100% dos resumos finais apresentam consistencia entre total lido, total exibido, total filtrado e distribuicao por tipo de acao.
- **SC-005**: 0 ocorrencias de exposicao de dados sensiveis em logs de execucao apos implantacao.

## Assumptions

- O arquivo AUDITORIA permanece como fonte oficial de eventos para esta funcionalidade.
- A listagem moderna deve preservar equivalencia funcional do comportamento legado, sem reproduzir limitacoes fisicas da impressao mainframe.
- A semantica de acao EX como evento nao exibivel continua valida na exibicao padrao.
- Consultas sem filtros especificos (acao, usuario, tabela) devem considerar todos os valores possiveis no periodo.
- A definicao de dados sensiveis para mascaramento e logging segue a politica corporativa vigente do projeto.
