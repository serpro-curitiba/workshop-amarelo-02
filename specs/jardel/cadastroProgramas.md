# Entrada para /speckit.specify - CADPROG

Use o texto abaixo como entrada no comando /speckit.specify.

Titulo da feature: cadastro-modernizado-programas-sociais

Descricao da feature:
Modernizar o fluxo de cadastro e consulta de programas sociais do programa legado CADPROG.NSN, preservando regras criticas de integridade cadastral e elegibilidade, e evoluindo calculos/filtros para governanca parametrizavel.

Contexto legado obrigatorio:
- Programa fonte: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
- Dominio: tabela de programas sociais (ARQ 155)
- Operacoes legadas: inclusao (I) e consulta (C)

Diretrizes para a especificacao:
1. Produzir requisitos em notacao EARS.
2. Gerar REQ-IDs unicos no formato REQ-PROG-001 em diante.
3. Todo requisito deve conter source_legacy apontando para CADPROG.NSN com linha aproximada.
4. Todo criterio de aceitacao deve estar em Given/When/Then.
5. Separar claramente regras para MIGRAR, EVOLUIR e DESCARTAR.
6. Incluir justificativa explicita para cada regra classificada como EVOLUIR ou DESCARTAR.
7. Incluir requisitos de validacao de entradas (operacao, datas, faixas, tipo de programa).
8. Preservar linguagem de negocio do SIFAP (programa social, codigo de elegibilidade, status do programa).

Regras legadas para transformar em requisitos:
- R1: Operacao valida apenas para I (inclusao) ou C (consulta).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L44-L53
  destino sugerido: MIGRAR

- R2: Em operacao C, executar somente consulta por codigo e encerrar fluxo.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L55-L58
  destino sugerido: MIGRAR

- R3: Nao permitir inclusao quando codigo de programa ja existir.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L75-L83
  destino sugerido: MIGRAR

- R4: Calcular valor base ajustado com fator K antes de persistir.
  formula_legado: fator_k = 1.00 + (fator_reajuste * 0.347215)
  formula_legado_2: vlr_ajustado = vlr_base * fator_k
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L85-L88
  destino sugerido: EVOLUIR (politica de reajuste versionada, auditavel e parametrizavel)

- R5: Incluir novo programa com status inicial A (ativo).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L99
  destino sugerido: MIGRAR

- R6: Consulta deve retornar campos essenciais (codigo, nome, tipo, valor base, elegibilidade, status).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L113-L119
  destino sugerido: MIGRAR

- R7: Em consulta sem registro, retornar programa nao encontrado.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L121-L123
  destino sugerido: MIGRAR

Regras legadas para evoluir com validacoes de negocio:
- E1: Validar tipo de programa contra dominio autorizado (A/P/T no legado).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L63-L66
  destino sugerido: EVOLUIR (dominio extensivel e versionado)

- E2: Validar consistencia de vigencia e faixa etaria (dt_inicio <= dt_fim quando dt_fim informado; idade_min <= idade_max).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L67-L71
  destino sugerido: EVOLUIR (regra explicita em vez de implicita)

Comportamentos legados candidatos a descarte:
- D1: Acoplamento da formula de reajuste a constante fixa hardcoded 0.347215 sem trilha de governanca.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86
  destino sugerido: DESCARTAR
  justificativa esperada: risco de opacidade regulatoria e manutencao fragil de politica de reajuste.

Saida esperada do /speckit.specify:
- Secao de escopo e fora de escopo
- Lista de requisitos EARS (REQ-PROG-001...)
- Criterios Given/When/Then para cada requisito
- Matriz de rastreabilidade regra_legado -> REQ-ID
- Resumo final com classificacao:
  - MIGRAR
  - EVOLUIR
  - DESCARTAR

Observacao sobre constitution:
- O arquivo .specify/memory/constitution.md do workspace esta no template e sem principios preenchidos.
- Enquanto isso nao for completado, considerar como baseline de governanca deste repositorio:
  - EARS obrigatorio
  - source_legacy obrigatorio
  - REQ-ID unico
  - rastreabilidade e criterios testaveis
