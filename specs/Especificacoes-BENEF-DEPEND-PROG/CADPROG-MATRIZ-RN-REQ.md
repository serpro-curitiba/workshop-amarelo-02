# Matriz de Rastreabilidade - CADPROG (RN -> REQ)

## Escopo
- Modulo: cadastro de programas sociais
- Fonte principal: CADPROG.NSN, PROGRAMA-SOCIAL.ddm, VALELEG.NSN, MANUAL-TECNICO-SIFAP-2008.md
- Objetivo: garantir rastreabilidade entre regra de negocio e requisito moderno

---

## Matriz RN -> REQ

| RN | Descricao resumida | REQ | Fonte legado | Cobertura |
|---|---|---|---|---|
| RN-PRG-001 | Validar dominio do tipo do programa (A/P/T) | REQ-PRG-001 | CADPROG.NSN#L16, CADPROG.NSN#L65 | Completa |
| RN-PRG-002 | Garantir unicidade na inclusao e existencia para alteracao | REQ-PRG-002 | CADPROG.NSN#L77-L83 | Parcial (legado sem fluxo explicito de alteracao no programa atual) |
| RN-PRG-003 | Exigir data de inicio valida | REQ-PRG-003 | CADPROG.NSN#L68, MANUAL-TECNICO-SIFAP-2008.md#L280 | Parcial |
| RN-PRG-004 | Exigir coerencia de vigencia (fim >= inicio; fim=0 indeterminado) | REQ-PRG-004 | CADPROG.NSN#L69, MANUAL-TECNICO-SIFAP-2008.md#L280 | Parcial |
| RN-PRG-005 | Manter faixas parametrizadas com limite maximo por programa | REQ-PRG-005 | PROGRAMA-SOCIAL.ddm#L59-L62, MANUAL-TECNICO-SIFAP-2008.md#L278 | Completa |
| RN-PRG-006 | Impedir sobreposicao/inconsistencia de faixas | REQ-PRG-006 | PROGRAMA-SOCIAL.ddm#L60-L61, CALCBENF.NSN#L303-L307 | Parcial |
| RN-PRG-007 | Impedir renda maxima negativa | REQ-PRG-007 | CADPROG.NSN#L70, VALELEG.NSN#L157-L158 | Parcial |
| RN-PRG-008 | Impedir faixa etaria inconsistente | REQ-PRG-008 | CADPROG.NSN#L71-L72, VALELEG.NSN#L139-L147 | Parcial |
| RN-PRG-009 | Restringir operacao a inclusao e consulta (I/C) | REQ-PRG-009 | CADPROG.NSN#L51-L54 | Completa |
| RN-PRG-010 | Encerrar em fluxo exclusivo de consulta sem persistencia | REQ-PRG-010 | CADPROG.NSN#L56-L58, CADPROG.NSN#L109-L118 | Completa |
| RN-PRG-011 | Calcular valor base ajustado via FATOR-K antes de persistir | REQ-PRG-011 | CADPROG.NSN#L87-L88 | Completa |
| RN-PRG-012 | Definir status inicial A na inclusao | REQ-PRG-012 | CADPROG.NSN#L97 | Completa |
| RN-PRG-013 | Confirmar transacao apos persistencia de inclusao | REQ-PRG-013 | CADPROG.NSN#L102-L103 | Completa |
| RN-PRG-014 | Retornar mensagem explicita para programa nao encontrado em consulta | REQ-PRG-014 | CADPROG.NSN#L117-L119 | Completa |

---

## Gaps identificados
- O CADPROG legado atual nao valida explicitamente todas as regras minimas de integridade de cadastro.
- Parte das regras aparece implicitamente no consumo (VALELEG/CALCBENF), nao na inclusao/manutencao.
- Recomenda-se tratar REQ-PRG-002 a REQ-PRG-008 como backlog P0/P1 para fechar lacunas de governanca de dados mestres.
