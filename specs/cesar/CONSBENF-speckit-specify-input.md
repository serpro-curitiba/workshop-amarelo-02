# Prompt para /speckit.specify - CONSBENF.NSN

Use o texto abaixo como entrada do comando /speckit.specify.

## Texto de entrada (copiar e colar)

Modernizar a funcionalidade legada CONSBENF (consulta de beneficiario) para a stack alvo do projeto SIFAP 2.0, preservando comportamento observavel e respeitando a constituicao do repositorio.

Contexto funcional observado no legado:
- Programa legado: 01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN
- Objetivo legado: consulta online 3270 de dados cadastrais do beneficiario e historico de pagamentos.
- Busca por tipo:
  - C = CPF
  - N = NIS
  - Em branco deve assumir C por padrao
  - Tipo invalido deve retornar erro de tipo de busca invalido
- Se nao encontrar beneficiario: retornar mensagem de nao encontrado.
- Exibir dados cadastrais do beneficiario encontrado.
- Exibir historico de pagamentos com limite de 12 registros.
- Se nao houver historico: exibir mensagem de nenhum pagamento encontrado.
- Aplicar mascara de CPF para ocultacao de dado sensivel.
- Regra legada conhecida: existe inconsistencia historica da mascara em alguns cenarios; nao alterar essa regra sem aprovacao formal de auditoria.

Requisitos obrigatorios da especificacao (Constituicao):
- Escrever especificacao em EARS.
- Toda regra deve ter REQ-ID unico.
- Todo REQ-ID deve incluir source_legacy apontando para arquivo .NSN/.ddm ou [GREENFIELD] com justificativa.
- Incluir criterios de aceitacao Given/When/Then testaveis e mensuraveis.
- Incluir requisitos de seguranca para nao exposicao de dados sensiveis em logs e saidas.
- Incluir requisitos de operabilidade (runbook/readiness/incidente) para a funcionalidade.

Escopo funcional esperado para a feature moderna:
1) Consulta de beneficiario por CPF ou NIS via endpoint e/ou fluxo de interface equivalente.
2) Comportamento padrao do tipo de busca quando nao informado (CPF).
3) Retorno estruturado de dados cadastrais essenciais do beneficiario.
4) Retorno de historico de pagamentos limitado aos ultimos 12 registros.
5) Traducao de status do beneficiario para descricao legivel:
   - A = ATIVO
   - S = SUSPENSO
   - C = CANCELADO
   - I = INATIVO
   - D = DESLIGADO
   - Demais = DESCONHECIDO
6) Tratamento de erros de entrada e de nao encontrado com codigos/semantica consistentes.
7) Mascaramento de CPF em exibicoes e logs conforme politica de seguranca.

Fontes de rastreabilidade minima que DEVEM aparecer na spec:
- source_legacy: 01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN
- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm
- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm
- source_legacy: [GREENFIELD] para requisitos novos de API moderna, observabilidade e readiness, com justificativa explicita.

Regras de qualidade para a saida do /speckit.specify:
- Priorizar historias por valor (P1, P2, P3), cada uma independente e testavel.
- Explicitar casos de borda:
  - tipo de busca invalido
  - CPF/NIS ausente conforme tipo
  - beneficiario inexistente
  - beneficiario sem historico
  - mais de 12 pagamentos historicos
- Definir criterios de sucesso mensuraveis.
- Declarar premissas e itens NEEDS CLARIFICATION quando faltar informacao.

Nao objetivos desta feature:
- Alterar regra historica de mascara de CPF sem aprovacao de auditoria.
- Redesenhar regras de negocio de outros programas (ex.: CALCBENF, VALBENEF).

Saida esperada:
- spec.md completo, consistente com a constituicao do projeto e pronto para fase de plan/tasks.

## Sugestao de uso

1. Abra o chat do Copilot com o agente Speckit.
2. Execute /speckit.specify.
3. Cole integralmente o texto da secao "Texto de entrada (copiar e colar)".
4. Revise a spec gerada conferindo REQ-ID + source_legacy em todos os requisitos.
