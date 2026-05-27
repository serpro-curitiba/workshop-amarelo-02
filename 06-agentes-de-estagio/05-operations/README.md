<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# @operations — Etapa 5: Operações

![ETAPA 5](https://img.shields.io/badge/ETAPA-5%20Opera%C3%A7%C3%B5es-2F855A?style=for-the-badge) ![AGENTE @operations](https://img.shields.io/badge/AGENTE-@operations-1A1A1A?style=for-the-badge) ![PÓS-ESTÁGIO](https://img.shields.io/badge/P%C3%93S%20EST%C3%81GIO-continuidade-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Agentes](../README.md) → **@operations**

> Use este agente quando a equipe precisar consolidar operação contínua após a evolução inicial: runbook, readiness, incidentes, observabilidade e hardening de CI/CD.

## Objetivo da etapa

Transformar o que já foi construído em operação sustentável: procedimentos claros, checklists de prontidão, resposta a incidentes e governança de segredos.

## Quando usar

- **Momento recomendado:** após Estágio 4, preparação de demo final ou continuidade pós-workshop.
- **Protagonistas:** DevOps Engineer e Tech Writer.
- **Suporte forte:** QA Engineer e Technical Lead.

## Passo a passo com o agente

1. Selecione o agente `@operations` no Copilot Chat.
2. Cole o prompt de abertura abaixo.
3. Gere ou revise runbook com comandos executáveis.
4. Feche checklist de prontidão operacional.
5. Valide playbook de incidente e responsáveis.
6. Atualize links de operação no README/docs.

```text
Estou iniciando a Etapa 5 — Operações.
Quero consolidar runbook, readiness de release/demo,
playbook de incidentes, observabilidade mínima e hardening de CI/CD.
Priorize instruções curtas, verificáveis e sem placeholders.
```

## O que perguntar

| Situação | Prompt útil |
| --- | --- |
| Runbook inicial | "Gere a semente de runbook para subir, validar, recuperar e escalar." |
| Readiness | "Crie checklist de prontidão operacional para demo/release." |
| Incidente | "Monte playbook de incidente com severidade, acionamento e pós-mortem." |
| Segredos | "Mapeie segredos necessários e estratégia segura de armazenamento." |
| CI/CD | "Liste os gates mínimos para impedir merge de código não operável." |

## Definição de Pronto

- [ ] Runbook v1 atualizado em `docs/runbook.md`.
- [ ] Checklist de readiness documentado e marcado.
- [ ] Playbook de incidente mínimo definido.
- [ ] Regras de segredos documentadas e sem hardcode.
- [ ] Links principais de operação visíveis em `docs/README.md`.

## Anti-padrões

| Não faça | Faça |
| --- | --- |
| Confiar em conhecimento oral | Versionar procedimento no runbook |
| Deixar segredo em arquivo | Usar secrets manager e variáveis seguras |
| Esconder risco operacional | Documentar risco + mitigação + owner |
| Pipeline permissivo | Definir gates de qualidade e infra |

## Navegação

| Anterior | Início | Próximo |
| --- | --- | --- |
| [@evolution](../04-evolution/README.md) | [Kit PT-BR](../../README.md) | [Docs](../../docs/README.md) |

— Paula
