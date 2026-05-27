# README de Operacoes - Esqueleto

## Objetivo

Explicar como operar o sistema com seguranca e previsibilidade durante workshop e continuidade.

## Escopo

- Execucao local
- CI/CD
- Readiness
- Incidente
- Segredos

## Acesso rapido

- Runbook: `operacoes/runbook-semente.md`
- Glossario: `operacoes/glossario-operacoes.md`
- Troubleshooting: `docs/troubleshooting.md`
- Status: `docs/STATUS.md`

## Checklist diario

- [ ] Subir ambiente
- [ ] Validar health
- [ ] Rodar checks locais
- [ ] Confirmar CI da branch

## Resposta inicial a incidente

- Classificar severidade (SEV-1, SEV-2, SEV-3)
- Mitigar com menor risco
- Comunicar impacto e ETA
- Registrar acao e pendencia

## Donos

- DevOps Engineer: operacao e pipeline
- Tech Writer: clareza e manutencao da documentacao
- QA Engineer: smoke tests e validacao
- Technical Lead: priorizacao tecnica em incidente

## Definicao de pronto

- [ ] Procedimentos executaveis e sem placeholders
- [ ] Links internos validos
- [ ] Dono por secao definido
- [ ] Checklists objetivos e curtos
