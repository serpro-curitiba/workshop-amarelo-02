<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Scripts

![PASTA 11 Scripts](https://img.shields.io/badge/PASTA-11%20Scripts-1A1A1A?style=for-the-badge) ![CONTEÚDO setup.sh + check.sh](https://img.shields.io/badge/CONTEÚDO-setup.sh%20+%20check.sh-737373?style=for-the-badge)



> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → **Scripts**

> **Para quem é isto?** Quem precisa rodar `setup.sh` ou `check.sh` no terminal.
>
> **O que você terá ao final desta leitura:**
>
> 1. O que `setup.sh` faz (clona materiais, cria symlinks)
> 2. O que `check.sh` faz (mirror do CI localmente)
> 3. Variáveis de ambiente opcionais para customizar

| Script                 | O que ele faz                                                                                                           |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| [`setup.sh`](setup.sh) | Faz o bootstrap do repositório da equipe — verifica pré-requisitos, clona materiais de referência, inicializa `specs/` |
| [`check.sh`](check.sh) | Executa todos os gates de CI localmente (testes do backend, lint/teste do frontend, Terraform fmt)                      |
| [`plan-each.sh`](plan-each.sh) | Percorre features em `specs/`, atualiza `.specify/feature.json` e prepara a execução sequencial de `/speckit.plan` |

## Uso

```bash
chmod +x scripts/*.sh

# Configuração inicial
./11-scripts/setup.sh

# Antes de cada push
./11-scripts/check.sh

# Listar features pendentes de planejamento
bash ./11-scripts/plan-each.sh --list

# Ativar a próxima feature pendente para rodar /speckit.plan no chat
bash ./11-scripts/plan-each.sh --next

# Percorrer interativamente todas as features pendentes
bash ./11-scripts/plan-each.sh
```

## Observações

- `setup.sh` clona os materiais de referência em `reference/` e cria symlinks para `prototype/` e `infra/`. A pasta `01-arqueologia/legado-sifap/` já vem incluída no kit. Sobrescreva o repositório de origem com `WORKSHOP_REPO=...`.
- Os symlinks estão no gitignore — eles servem apenas para conveniência local.
- `check.sh` ignora qualquer verificação cuja pasta ainda não exista (assim ele funciona durante os estágios iniciais).
- `plan-each.sh` não executa `/speckit.plan` diretamente no shell, porque esse comando pertence ao fluxo de chat do Copilot. Ele apenas prepara a feature ativa e organiza a execução uma por vez.
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Kit PT-BR</strong></a><br/>
<sub>Hub deste folder: comece aqui se nunca abriu o kit.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../00-SETUP.md"><strong>SETUP</strong></a><br/>
<sub>Setup do laptop: Git, VS Code, Copilot, Spec-Kit, branch protection.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
