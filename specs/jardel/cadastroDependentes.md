# Entrada para /speckit.specify - CADDEPEND

Use o texto abaixo como entrada no comando /speckit.specify.

Titulo da feature: cadastro-modernizado-dependentes

Descricao da feature:
Modernizar o fluxo de cadastro de dependentes do programa legado CADDEPEND.NSN, mantendo regras criticas de elegibilidade, removendo comportamentos inseguros e evoluindo regras rigidas para configuracao por politica.

Contexto legado obrigatorio:
- Programa fonte: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN
- Dominio: beneficiario titular e seus dependentes (PE group DEPENDENTES)
- Campo de status do titular: C e D bloqueiam inclusao

Diretrizes para a especificacao:
1. Produzir requisitos em notacao EARS.
2. Gerar REQ-IDs unicos no formato REQ-DEP-001 em diante.
3. Todo requisito deve conter source_legacy apontando para CADDEPEND.NSN com linha aproximada.
4. Todo criterio de aceitacao deve estar em Given/When/Then.
5. Separar claramente regras para MIGRAR, EVOLUIR e DESCARTAR.
6. Incluir justificativa explicita para cada regra classificada como EVOLUIR ou DESCARTAR.
7. Incluir requisitos de validacao de entrada e integridade de dados para evitar duplicidade de dependente.
8. Preservar linguagem de negocio do SIFAP (titular, dependente, parentesco, situacao cadastral).

Regras legadas para transformar em requisitos:
- R1: Inclusao de dependente somente se CPF do titular existir.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L40-L52
  destino sugerido: MIGRAR

- R2: Bloquear inclusao se titular estiver cancelado ou desligado (status C/D).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L54-L58
  destino sugerido: MIGRAR

- R3: Limite maximo de dependentes por titular (legado: valor fixo 5).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L62-L65
  destino sugerido: EVOLUIR para parametro de politica

- R4: Validacoes de cadastro (nome obrigatorio e parentesco valido).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L79-L91
  destino sugerido: EVOLUIR para dominio de parentesco configuravel

- R5: Bloquear CPF de dependente duplicado dentro do mesmo titular.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L98-L107
  destino sugerido: EVOLUIR com regra explicita para CPF ausente

Comportamento legado para descarte:
- D1: Tratar CPF do dependente igual a zero como fluxo normal de cadastro.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L100
  destino sugerido: DESCARTAR
  justificativa esperada: risco de identidade ambigua e inconsistencias cadastrais

Saida esperada do /speckit.specify:
- Secao de escopo e fora de escopo
- Lista de requisitos EARS (REQ-DEP-001...)
- Criterios Given/When/Then para cada requisito
- Matriz de rastreabilidade regra_legado -> REQ-ID
- Resumo final com classificacao:
  - MIGRAR
  - EVOLUIR
  - DESCARTAR

Observacao sobre constitution:
- O arquivo .specify/memory/constitution.md do workspace esta no template e sem principios preenchidos.
- Enquanto isso nao for completado, considerar como baseline de governanca deste repositiorio:
  - EARS obrigatorio
  - source_legacy obrigatorio
  - REQ-ID unico
  - rastreabilidade e criterios testaveis
