# Runbook Operacional - Semente

## Objetivo

Fornecer um procedimento curto e executavel para subir, validar, recuperar e escalar operacao do SIFAP 2.0.

## Escopo

- Ambiente local com Docker Compose
- Qualidade pre-push e CI
- Readiness para demo/release
- Resposta inicial a incidente
- Segredos e boas praticas

## Donos

| Tema | Dono primario | Backup |
|---|---|---|
| Ambiente local e CI | DevOps Engineer | Technical Lead |
| Smoke tests | QA Engineer | Developer |
| Documentacao operacional | Tech Writer | DevOps Engineer |
| Priorizacao em incidente | Technical Lead | Product Owner |

## Subida inicial

```bash
./11-scripts/setup.sh
docker compose up -d
```

Validacao minima:

- Backend: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:3001

## Rotina diaria

```bash
docker compose up -d
./11-scripts/check.sh
git status
```

Checklist diario:

- [ ] containers criticos em execucao
- [ ] check local verde
- [ ] sem segredo no diff
- [ ] PR com descricao e rastreabilidade

## CI/CD - gates minimos

- [ ] build backend
- [ ] build frontend
- [ ] testes automatizados
- [ ] validacao Terraform (`fmt` e `validate`)
- [ ] rastreabilidade de requisitos quando houver mudanca de spec/docs

## Terraform no workshop

```bash
cd infra
terraform init
terraform plan -var-file=envs/dev/terraform.tfvars
```

Regra: usar plan no workshop e evitar apply sem autorizacao explicita.

## Readiness de demo/release

- [ ] ambiente sobe com comando documentado
- [ ] health check retorna UP
- [ ] fluxo principal validado por smoke test
- [ ] CI verde na branch alvo
- [ ] riscos conhecidos registrados com workaround

## Incidente - primeiros 10 minutos

1. Confirmar escopo e impacto.
2. Coletar evidencia minima (log, comando, tela, PR recente).
3. Mitigar com alternativa mais segura (rollback/restart controlado).
4. Comunicar status e proxima atualizacao.
5. Registrar causa provavel e acao seguinte.

Template de comunicacao:

```text
Incidente: <titulo curto>
Impacto: <o que parou/degradou>
Acao em curso: <mitigacao>
Proxima atualizacao: <hora>
```

## Segredos

- Proibido commit de token/senha/chave.
- CI usando GitHub Secrets/Environment Secrets.
- Cloud usando Key Vault (ou equivalente).
- Em caso de vazamento: revogar, rotacionar e registrar.

## Definicao de pronto

- [ ] pessoa nova sobe ambiente sem ajuda
- [ ] checklist de readiness executado em menos de 10 minutos
- [ ] top 5 erros possuem procedimento de resposta
- [ ] donos e escalonamento claros
