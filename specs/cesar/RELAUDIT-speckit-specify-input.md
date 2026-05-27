# Prompt para /speckit.specify - RELAUDIT.NSN

Use o texto abaixo como entrada do comando /speckit.specify.

## Texto de entrada (copiar e colar)

Modernizar a funcionalidade legada RELAUDIT (relatorio de trilha de auditoria do sistema) para a stack alvo do projeto SIFAP 2.0, preservando comportamento observavel, rastreabilidade legada e requisitos de seguranca/operabilidade definidos na constituicao do repositorio.

Contexto funcional observado no legado:
- Programa legado: 01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN
- Objetivo legado: listagem da trilha de auditoria com filtros por periodo, acao, usuario e tabela.
- Entrada operacional no legado:
  - data inicial (#DT-INI)
  - data final (#DT-FIM)
  - acao (branco = todas)
  - usuario (branco = todos)
  - tabela (branco = todas)
  - tipo de saida (T=tela, I=impressora)
- Defaults observados no legado:
  - #TIPO-SAIDA em branco assume T
  - #DT-INI = 0 assume 19970101
  - #DT-FIM = 0 assume data atual
- Regras de filtro no legado:
  - processar registros de AUDITORIA por DT-EVENTO no intervalo informado
  - intervalo inclusivo por data (>= inicial e <= final)
  - acao EX e sempre excluida da exibicao
  - quando filtro de acao e informado, exibir apenas acao exata
  - quando filtro de usuario e informado, exibir apenas usuario exato
  - quando filtro de tabela e informado, exibir apenas tabela exata
- Contadores no legado:
  - total lido
  - total exibido
  - total filtrado
  - totais por tipo de acao (IN, AL, CO, CN, DV, OUTROS)
- Mapeamento de acao observado no programa:
  - IN=INCLUSAO
  - AL=ALTERACAO
  - CO=CONCILIACAO
  - CN=CONSULTA
  - DV=DIVERGENCIA
  - outros=OUTRA
- Formato de saida observado:
  - TELA: data, hora formatada, usuario, acao, tabela, chave
  - IMPRESSORA: mesmos campos + descricao
- Caracteristica de impressao legada:
  - paginacao com cabecalho por pagina
  - controle de 66 linhas por pagina

Requisitos obrigatorios da especificacao (Constituicao):
- Escrever especificacao em EARS.
- Toda regra deve ter REQ-ID unico.
- Todo REQ-ID deve incluir source_legacy apontando para arquivo .NSN/.ddm ou [GREENFIELD] com justificativa.
- Incluir criterios de aceitacao Given/When/Then testaveis e mensuraveis.
- Incluir requisitos de seguranca para nao exposicao de dados sensiveis em logs e saidas.
- Incluir requisitos de operabilidade (runbook/readiness/incidente) para a funcionalidade.

Escopo funcional esperado para a feature moderna:
1) Geracao de relatorio de trilha de auditoria por intervalo de datas.
2) Suporte a filtros opcionais por acao, usuario e tabela.
3) Exclusao obrigatoria de eventos de acao EX na exibicao padrao.
4) Listagem detalhada de eventos com campos essenciais:
   - data do evento
   - hora formatada
   - usuario
   - acao (codigo + descricao)
   - tabela de referencia
   - chave de referencia
   - descricao do evento (quando aplicavel ao formato escolhido)
5) Exibicao de resumo final com:
   - total lido
   - total exibido
   - total filtrado
   - totais por tipo de acao (IN, AL, CO, CN, DV, OUTROS)
6) Comportamento deterministico para consultas sem dados no periodo ou sem correspondencia de filtro (retorno vazio com indicacao clara).
7) Compatibilidade operacional com formatos de saida:
   - formato estruturado para API (JSON)
   - formato tabular para consumo humano (equivalente funcional ao relatorio legado)

Fontes de rastreabilidade minima que DEVEM aparecer na spec:
- source_legacy: 01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN
- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm
- source_legacy: [GREENFIELD] para requisitos novos de API moderna, observabilidade, exportacao e readiness, com justificativa explicita.

Regras de qualidade para a saida do /speckit.specify:
- Priorizar historias por valor (P1, P2, P3), cada uma independente e testavel.
- Explicitar casos de borda:
  - data inicial maior que data final
  - filtros combinados sem resultado
  - tipo de saida invalido
  - eventos com acao nao mapeada (fallback para OUTRA)
  - acao EX presente na base e removida da exibicao
  - periodo sem eventos
- Definir criterios de sucesso mensuraveis.
- Declarar premissas e itens NEEDS CLARIFICATION quando faltar informacao.

Requisitos nao funcionais minimos a incluir:
- Performance: consulta deve suportar alto volume historico de auditoria sem degradacao severa (definir alvo mensuravel na spec).
- Seguranca: nao expor dados sensiveis em logs e em respostas de usuario; aplicar mascaramento quando houver dado pessoal.
- Auditabilidade: registrar parametros de execucao e metadados da consulta sem vazar dados sensiveis.
- Operabilidade: incluir verificacoes de readiness para dependencia de dados de AUDITORIA e procedimentos de incidente.

Nao objetivos desta feature:
- Alterar politicas de retencao legal de auditoria.
- Alterar sem aprovacao de negocio o significado dos codigos de acao herdados.
- Reproduzir restricoes fisicas de impressora mainframe como requisito tecnico obrigatorio da API moderna.

Saida esperada:
- spec.md completo, consistente com a constituicao do projeto e pronto para fase de plan/tasks.

## Sugestao de uso

1. Abra o chat do Copilot com o agente Speckit.
2. Execute /speckit.specify.
3. Cole integralmente o texto da secao Texto de entrada (copiar e colar).
4. Revise a spec gerada conferindo REQ-ID + source_legacy em todos os requisitos.
