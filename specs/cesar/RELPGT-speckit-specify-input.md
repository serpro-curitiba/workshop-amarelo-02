# Prompt para /speckit.specify - RELPGT.NSN

Use o texto abaixo como entrada do comando /speckit.specify.

## Texto de entrada (copiar e colar)

Modernizar a funcionalidade legada RELPGT (relatorio analitico de pagamentos por periodo) para a stack alvo do projeto SIFAP 2.0, preservando comportamento observavel, rastreabilidade legada e requisitos de seguranca/operabilidade definidos na constituicao do repositorio.

Contexto funcional observado no legado:
- Programa legado: 01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN
- Objetivo legado: relatorio detalhado de pagamentos por periodo, com totalizadores e subtotais por programa.
- Entrada operacional no legado:
  - competencia inicial (#COMP-INI)
  - competencia final (#COMP-FIM)
  - codigo de programa (0 = todos)
- Regras de filtro no legado:
  - processar registros de PAGAMENTO dentro do intervalo de competencia
  - quando codigo programa != 0, filtrar por cod-programa exato
- Quebra e agregacao:
  - subtotal por COD-PROGRAMA
  - total geral ao final
  - acumuladores de bruto, desconto, liquido, abono e quantidade
- Enriquecimento de dados:
  - buscar nome/UF do beneficiario por CPF em BENEFICIARIO
- Mascaramento de CPF:
  - exibir CPF mascarado no detalhe do relatorio
- Traducoes de dominio no legado:
  - TIPO-PGTO: N=NORMAL, D=DECIMO, T=TERCEIRO, outros=OUTRO
  - STATUS-PGTO: G=GERADO, P=PAGO, C=CANCELAD, D=DEVOLVID, E=ESTORNAD, outros=OUTRO
- Caracteristica de impressao legada:
  - paginacao em layout de relatorio (cabecalho por pagina, 66 linhas no mainframe)

Requisitos obrigatorios da especificacao (Constituicao):
- Escrever especificacao em EARS.
- Toda regra deve ter REQ-ID unico.
- Todo REQ-ID deve incluir source_legacy apontando para arquivo .NSN/.ddm ou [GREENFIELD] com justificativa.
- Incluir criterios de aceitacao Given/When/Then testaveis e mensuraveis.
- Incluir requisitos de seguranca para nao exposicao de dados sensiveis em logs e saidas.
- Incluir requisitos de operabilidade (runbook/readiness/incidente) para a funcionalidade.

Escopo funcional esperado para a feature moderna:
1) Geracao de relatorio analitico de pagamentos por intervalo de competencia.
2) Suporte a filtro opcional por codigo de programa (0 = todos).
3) Listagem detalhada por pagamento com dados essenciais:
   - competencia
   - CPF mascarado
   - nome do beneficiario
   - UF
   - valor bruto
   - valor desconto
   - valor liquido
   - status (codigo + descricao)
   - tipo (codigo + descricao)
4) Calculo e exibicao de subtotal por programa:
   - quantidade
   - total bruto
   - total liquido
5) Calculo e exibicao de total geral:
   - quantidade total de registros
   - total bruto
   - total desconto
   - total liquido
   - total abono
6) Comportamento deterministico para sem dados no periodo (retorno vazio com metadados/indicacao clara).
7) Compatibilidade operacional com formatos de saida:
   - formato estruturado para API (JSON)
   - formato tabular para consumo humano (equivalente funcional ao relatorio legado)

Fontes de rastreabilidade minima que DEVEM aparecer na spec:
- source_legacy: 01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN
- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
- source_legacy: [GREENFIELD] para requisitos novos de API moderna, observabilidade, exportacao e readiness, com justificativa explicita.

Regras de qualidade para a saida do /speckit.specify:
- Priorizar historias por valor (P1, P2, P3), cada uma independente e testavel.
- Explicitar casos de borda:
  - competencia inicial maior que competencia final
  - filtro por programa inexistente
  - pagamento sem beneficiario correspondente
  - periodo sem pagamentos
  - status/tipo fora da tabela conhecida (fallback para OUTRO)
- Definir criterios de sucesso mensuraveis.
- Declarar premissas e itens NEEDS CLARIFICATION quando faltar informacao.

Requisitos nao funcionais minimos a incluir:
- Performance: consulta deve suportar volume alto sem degradacao severa (definir alvo mensuravel na spec).
- Seguranca: CPF nunca deve aparecer sem mascara em resposta de usuario e em logs.
- Auditabilidade: registrar parametros de execucao do relatorio sem vazar dados sensiveis.
- Operabilidade: incluir verificacoes de readiness para dependencia de dados de PAGAMENTO/BENEFICIARIO.

Nao objetivos desta feature:
- Reescrever regras de calculo de beneficio de programas de calculo (ex.: CALCBENF).
- Alterar sem aprovacao de negocio os codigos de status/tipo herdados do legado.
- Reproduzir restricoes fisicas de impressora mainframe (66 linhas) como requisito tecnico obrigatorio da API moderna.

Saida esperada:
- spec.md completo, consistente com a constituicao do projeto e pronto para fase de plan/tasks.

## Sugestao de uso

1. Abra o chat do Copilot com o agente Speckit.
2. Execute /speckit.specify.
3. Cole integralmente o texto da secao "Texto de entrada (copiar e colar)".
4. Revise a spec gerada conferindo REQ-ID + source_legacy em todos os requisitos.
