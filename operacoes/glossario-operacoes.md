# Glossario de Operacoes - Semente

## Objetivo

Padronizar termos de operacao, CI/CD, incidentes e rastreabilidade para reduzir ambiguidade entre DevOps, QA, TL e Tech Writer.

## Termos iniciais

| # | Termo | Expansao | Contexto |
|---|---|---|---|
| 1 | SLO | Service Level Objective | Meta interna de confiabilidade (ex.: latencia, disponibilidade). |
| 2 | SLA | Service Level Agreement | Compromisso formal de nivel de servico. |
| 3 | SLI | Service Level Indicator | Medida observavel usada para avaliar o SLO. |
| 4 | MTTR | Mean Time To Recovery | Tempo medio para restaurar o servico apos incidente. |
| 5 | MTTD | Mean Time To Detect | Tempo medio para detectar incidente. |
| 6 | SEV-1 | Severidade 1 | Incidente critico com indisponibilidade total ou alto risco para demo/release. |
| 7 | SEV-2 | Severidade 2 | Degradacao relevante com workaround parcial. |
| 8 | SEV-3 | Severidade 3 | Impacto baixo, sem bloquear fluxo principal. |
| 9 | Smoke Test | Teste de fumaca | Verificacao rapida do fluxo principal apos deploy/start. |
| 10 | Health Check | Verificacao de saude | Endpoint de status tecnico da aplicacao. |
| 11 | Readiness | Prontidao | Condicoes minimas para liberar demo/release. |
| 12 | Rollback | Reversao | Retorno para versao estavel anterior apos falha. |
| 13 | Post-mortem | Analise pos-incidente | Registro de causa raiz, impacto e acoes preventivas. |
| 14 | Drift | Desvio de configuracao | Diferenca entre estado esperado (IaC/spec) e estado real. |
| 15 | IaC | Infrastructure as Code | Definicao de infraestrutura por codigo versionado. |
| 16 | Secret Rotation | Rotacao de segredo | Troca programada de credenciais/tokens. |
| 17 | Branch Protection | Protecao de branch | Regras para impedir merge sem checks/review. |
| 18 | Quality Gate | Gate de qualidade | Conjunto de validacoes obrigatorias para merge/release. |
| 19 | REQ-ID | Identificador de requisito | Chave de rastreabilidade requisito-codigo-teste. |
| 20 | source_legacy | Fonte legada do requisito | Referencia obrigatoria a NSN/DDM ou GREENFIELD justificado. |

## Pendencias de validacao

- Definir SLO alvo para demo (latencia e taxa de erro).
- Confirmar criterio de SEV com todo o time.
- Confirmar donos oficiais de escalonamento.
