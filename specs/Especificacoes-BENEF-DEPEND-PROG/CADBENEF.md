# SPECIFICATION - SIFAP 2.0 (Rascunho Consolidado)

## Metadados
- Versão: 0.1.0
- Base: levantamento legado + leitura de código Natural
- Status: draft para validação funcional
- Observação: itens marcados como evoluir dependem de decisão de produto e compliance

---

## Requisitos - Beneficiary

REQ-BEN-001:
  pattern: unwanted
  text: O SIFAP não deve permitir inclusão ou alteração de beneficiário com CPF inválido pelo algoritmo módulo 11.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L111-L116
  business_rule: RN-001
  acceptance:
  - Dado CPF inválido, a operação retorna erro de validação e não persiste dados.
  - Dado CPF válido, a validação não bloqueia inclusão nem alteração.
  priority: P0
  risk: CRITICO
  modernization_decision: migrar

REQ-BEN-002:
  pattern: unwanted
  text: O SIFAP não deve permitir inclusão de beneficiário com CPF já cadastrado.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L143-L147
  business_rule: RN-002
  acceptance:
  - Dado CPF já existente, inclusão retorna conflito.
  - Dado CPF inexistente, inclusão é permitida.
  priority: P0
  risk: CRITICO
  modernization_decision: migrar

REQ-BEN-003:
  pattern: state-driven
  text: Enquanto a operação for alteração, o SIFAP deve exigir que o beneficiário exista previamente no cadastro.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L149-L153
  business_rule: RN-008
  acceptance:
  - Alteração de CPF inexistente retorna não encontrado.
  - Alteração de CPF existente é processada.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-BEN-004:
  pattern: ubiquitous
  text: O SIFAP deve exigir os campos mínimos obrigatórios para cadastro: CPF, nome, data de nascimento e sexo válido.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L105-L134
  business_rule: RN-001
  acceptance:
  - CPF ausente ou zero bloqueia operação.
  - Nome vazio bloqueia operação.
  - Data de nascimento ausente bloqueia operação.
  - Sexo diferente de M ou F bloqueia operação.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-BEN-005:
  pattern: event-driven
  text: Quando um novo beneficiário for incluído, o SIFAP deve definir status inicial ACTIVE.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L162-L164
  business_rule: RN-002
  acceptance:
  - Inclusão válida grava status ACTIVE.
  - Data de cadastro e atualização são preenchidas na inclusão.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-BEN-006:
  pattern: state-driven
  text: Enquanto o beneficiário tiver idade acima de 75 anos, o SIFAP deve aplicar revisão cadastral obrigatória antes de eventual suspensão.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L167-L169
  business_rule: RN-006
  acceptance:
  - Idade acima de 75 não gera suspensão silenciosa.
  - Suspensão por regra etária exige justificativa auditável.
  - Deve existir mecanismo de revisão manual.
  priority: P1
  risk: CRITICO
  modernization_decision: evoluir

REQ-BEN-007:
  pattern: unwanted
  text: O SIFAP não deve restringir indevidamente atualização de atributos cadastrais críticos sem política explícita de governança.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L201-L214
  business_rule: RN-008
  acceptance:
  - Campos críticos têm política explícita de mutabilidade por perfil.
  - Atualização autorizada de campo crítico gera auditoria.
  - Atualização não autorizada retorna erro de negócio.
  priority: P1
  risk: ALTO
  modernization_decision: evoluir

REQ-BEN-008:
  pattern: unwanted
  text: O SIFAP não deve aceitar código de região fora da tabela oficial definida para o domínio de beneficiário.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-005
  acceptance:
  - Região fora do conjunto permitido retorna erro de validação.
  - Região reservada deve seguir política explícita e auditável.
  priority: P1
  risk: ALTO
  modernization_decision: evoluir

REQ-BEN-009:
  pattern: unwanted
  text: O SIFAP não deve tratar limitação de interface terminal I/A como regra de domínio de negócio.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L99-L103
  business_rule: RN-SUPP-LEGACY-001
  acceptance:
  - O domínio moderno suporta comandos por caso de uso, não por restrição de tela legada.
  - Compatibilidade com terminal, quando necessária, fica na camada de interface.
  priority: P2
  risk: MEDIO
  modernization_decision: descartar

---

## Requisitos - Payment, Descontos e Elegibilidade

REQ-PAY-013:
  pattern: ubiquitous
  text: O SIFAP deve calcular o benefício mensal com base na combinação de valor base do programa e componentes vinculados aos dependentes e faixa de renda.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-013
  acceptance:
  - Dado programa e faixa válidos, o cálculo retorna valor consistente com os parâmetros vigentes.
  - Dado aumento de dependentes elegíveis, o valor final reflete o acréscimo definido pela política do programa.
  priority: P0
  risk: CRITICO
  modernization_decision: evoluir

REQ-PAY-014:
  pattern: ubiquitous
  text: O SIFAP deve aplicar truncamento para duas casas decimais no cálculo de benefício, sem arredondamento matemático.
  source_legacy: REGRAS-NEGOCIO-2012.md
  source_legacy_confirmed_code: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L231-L234
  business_rule: RN-014
  acceptance:
  - Valor 125,567 resulta em 125,56.
  - Valor 125,561 resulta em 125,56.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PAY-017:
  pattern: ubiquitous
  text: O SIFAP deve manter faixas de valores parametrizadas por programa e exercício fiscal.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-017
  acceptance:
  - Cada programa permite múltiplas faixas por exercício dentro do limite de configuração.
  - Alteração de faixa em novo exercício não retroage cálculos já consolidados.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PAY-018:
  pattern: state-driven
  text: Enquanto houver renda per capita declarada, o SIFAP deve selecionar a primeira faixa cujo limite superior seja maior ou igual à renda.
  source_legacy: REGRAS-NEGOCIO-2012.md
  source_legacy_confirmed_code: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L303-L307
  business_rule: RN-018
  acceptance:
  - Dada renda exatamente no limite da faixa, aplica-se a própria faixa.
  - Dada renda entre limites, aplica-se a primeira faixa elegível em ordem crescente.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PAY-019:
  pattern: event-driven
  text: Quando iniciar novo exercício em janeiro com índice oficial publicado, o SIFAP deve aplicar reajuste anual de benefícios.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-019
  acceptance:
  - Sem índice oficial vigente, reajuste não é aplicado.
  - Com índice oficial vigente, cálculo usa o fator do exercício correto.
  priority: P1
  risk: ALTO
  modernization_decision: migrar

REQ-PAY-020:
  pattern: ubiquitous
  text: O SIFAP deve aplicar reajuste sobre o valor base, e não sobre o valor total acrescido de dependentes.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-020
  acceptance:
  - Com mesmo valor base e mesmo índice, o reajuste é idêntico independentemente de componentes não-base.
  - A memória de cálculo explicita base reajustada e componentes adicionais separadamente.
  priority: P1
  risk: ALTO
  modernization_decision: evoluir

REQ-PAY-021:
  pattern: unwanted
  text: O SIFAP não deve permitir descontos totais acima de 30 por cento do valor bruto, exceto quando houver exceção judicial formalmente definida.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-021
  acceptance:
  - Sem exceção judicial, descontos acima de 30 por cento são bloqueados.
  - Com exceção judicial válida, o motor aplica política especial e registra auditoria.
  priority: P0
  risk: CRITICO
  modernization_decision: evoluir

REQ-PAY-022:
  pattern: ubiquitous
  text: O SIFAP deve manter catálogo versionado de tipos de desconto com código, prioridade e regra de elegibilidade.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-022
  acceptance:
  - Tipos oficiais possuem código único e descrição normativa.
  - Inclusão de novo tipo exige parametrização sem alteração de código-fonte.
  priority: P1
  risk: MEDIO
  modernization_decision: evoluir

REQ-PAY-023:
  pattern: state-driven
  text: Enquanto houver limite disponível de desconto, o SIFAP deve aplicar descontos por ordem de prioridade numérica.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-023
  acceptance:
  - Com limite parcial remanescente, descontos de menor prioridade são descartados primeiro.
  - Resultado final respeita ordem determinística e reproduzível.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-ELG-015:
  pattern: ubiquitous
  text: O SIFAP deve manter regras de elegibilidade específicas por programa social, com governança explícita e rastreável.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-015
  acceptance:
  - Cada programa possui política de elegibilidade própria publicada.
  - Alterações de política são versionadas e auditáveis.
  priority: P0
  risk: CRITICO
  modernization_decision: evoluir

REQ-ELG-016:
  pattern: event-driven
  text: Quando houver integração ativa com CadUnico, o SIFAP deve validar elegibilidade com base nas evidências cadastrais sincronizadas.
  source_legacy: REGRAS-NEGOCIO-2012.md
  business_rule: RN-016
  acceptance:
  - Falha de integração não pode aprovar automaticamente casos sem dados mínimos.
  - Divergências cadastrais geram status de revisão e não aprovação silenciosa.
  priority: P0
  risk: CRITICO
  modernization_decision: evoluir

---

## Requisitos - Programas Sociais (CADPROG)

REQ-PRG-001:
  pattern: unwanted
  text: O SIFAP nao deve aceitar cadastro de programa com tipo diferente de A, P ou T.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L16-L16
  source_legacy_confirmed_code: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L65-L65
  business_rule: RN-PRG-001
  acceptance:
  - Dado tipo A, o cadastro prossegue.
  - Dado tipo P, o cadastro prossegue.
  - Dado tipo T, o cadastro prossegue.
  - Dado tipo X ou vazio, o cadastro retorna erro de validacao e nao persiste.
  priority: P0
  risk: CRITICO
  modernization_decision: migrar

REQ-PRG-002:
  pattern: state-driven
  text: Enquanto a operacao de cadastro estiver em modo de manutencao de programa, o SIFAP deve exigir codigo de programa unico para inclusao e existente para alteracao.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L77-L83
  business_rule: RN-PRG-002
  acceptance:
  - Inclusao com codigo ja cadastrado retorna conflito.
  - Alteracao com codigo inexistente retorna nao encontrado.
  - Inclusao com codigo novo persiste normalmente.
  priority: P0
  risk: CRITICO
  modernization_decision: evoluir

REQ-PRG-003:
  pattern: unwanted
  text: O SIFAP nao deve aceitar vigencia com data de inicio ausente ou invalida.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L68-L68
  source_legacy_context: 01-arqueologia/legado-sifap/legacy-docs/MANUAL-TECNICO-SIFAP-2008.md#L280-L280
  business_rule: RN-PRG-003
  acceptance:
  - Data inicio valida no formato AAAAMMDD e aceita.
  - Data inicio zero, vazia ou nao calendario retorna erro de validacao.
  priority: P0
  risk: ALTO
  modernization_decision: evoluir

REQ-PRG-004:
  pattern: unwanted
  text: O SIFAP nao deve aceitar vigencia com data fim menor que a data inicio; data fim zero deve representar vigencia indeterminada.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L69-L69
  source_legacy_context: 01-arqueologia/legado-sifap/legacy-docs/MANUAL-TECNICO-SIFAP-2008.md#L280-L280
  business_rule: RN-PRG-004
  acceptance:
  - Data fim 0 e aceita como indeterminada.
  - Data fim maior ou igual a data inicio e aceita.
  - Data fim menor que data inicio retorna erro de validacao.
  priority: P0
  risk: ALTO
  modernization_decision: evoluir

REQ-PRG-005:
  pattern: ubiquitous
  text: O SIFAP deve manter parametros de faixa de calculo por programa com no maximo 5 faixas, contendo renda inicio, renda fim e fator multiplicador.
  source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm#L59-L62
  source_legacy_context: 01-arqueologia/legado-sifap/legacy-docs/MANUAL-TECNICO-SIFAP-2008.md#L278-L278
  business_rule: RN-PRG-005
  acceptance:
  - Programa permite cadastrar de 1 a 5 faixas.
  - Cada faixa exige renda inicio, renda fim e fator multiplicador.
  - Tentativa de cadastrar 6a faixa retorna erro de validacao.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PRG-006:
  pattern: unwanted
  text: O SIFAP nao deve aceitar faixas de renda sobrepostas, com lacunas logicas ou com limite inicial maior que o limite final.
  source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm#L60-L61
  source_legacy_confirmed_code: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L303-L307
  business_rule: RN-PRG-006
  acceptance:
  - Faixa com inicio maior que fim retorna erro de validacao.
  - Faixa que sobrepoe faixa existente retorna erro de validacao.
  - Conjunto de faixas sem sobreposicao e aceito.
  priority: P0
  risk: CRITICO
  modernization_decision: evoluir

REQ-PRG-007:
  pattern: unwanted
  text: O SIFAP nao deve aceitar renda maxima negativa para elegibilidade do programa.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L70-L70
  source_legacy_context: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L157-L158
  business_rule: RN-PRG-007
  acceptance:
  - Renda maxima maior ou igual a zero e aceita.
  - Renda maxima negativa retorna erro de validacao.
  priority: P0
  risk: ALTO
  modernization_decision: evoluir

REQ-PRG-008:
  pattern: unwanted
  text: O SIFAP nao deve aceitar faixa etaria inconsistente, com idade minima ou maxima negativa, acima de 130, ou com minima maior que maxima quando ambas informadas.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L71-L72
  source_legacy_context: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L139-L147
  business_rule: RN-PRG-008
  acceptance:
  - Idade minima e maxima iguais a 0 sao aceitas como sem restricao.
  - Idade minima 18 e maxima 65 e aceita.
  - Idade minima maior que maxima retorna erro de validacao.
  - Idades fora do intervalo 0..130 retornam erro de validacao.
  priority: P0
  risk: ALTO
  modernization_decision: evoluir

REQ-PRG-009:
  pattern: unwanted
  text: O SIFAP nao deve aceitar operacao diferente de I ou C no cadastro de programas sociais.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L51-L54
  business_rule: RN-PRG-009
  acceptance:
  - Operacao I e aceita.
  - Operacao C e aceita.
  - Operacao diferente de I/C retorna erro de operacao invalida.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PRG-010:
  pattern: event-driven
  text: Quando a operacao for C, o SIFAP deve executar apenas o fluxo de consulta por codigo e encerrar a rotina sem persistir alteracoes.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L56-L58
  source_legacy_confirmed_code: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L109-L118
  business_rule: RN-PRG-010
  acceptance:
  - Operacao C com codigo existente exibe dados do programa.
  - Operacao C com codigo inexistente retorna programa nao encontrado.
  - Operacao C nao executa store de registro.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PRG-011:
  pattern: ubiquitous
  text: O SIFAP deve calcular o valor base ajustado do programa com FATOR-K derivado do fator de reajuste antes da persistencia.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L87-L88
  business_rule: RN-PRG-011
  acceptance:
  - O sistema calcula FATOR-K pela formula 1.00 + (fator-reajuste * 0.347215).
  - O valor base persistido corresponde ao valor base informado multiplicado pelo FATOR-K.
  priority: P1
  risk: ALTO
  modernization_decision: evoluir

REQ-PRG-012:
  pattern: event-driven
  text: Quando um novo programa for incluido com sucesso, o SIFAP deve definir status inicial A (ativo).
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L97-L97
  business_rule: RN-PRG-012
  acceptance:
  - Inclusao valida grava status A no programa.
  - Consulta apos inclusao exibe status A.
  priority: P0
  risk: ALTO
  modernization_decision: migrar

REQ-PRG-013:
  pattern: event-driven
  text: Quando a inclusao de programa for realizada, o SIFAP deve persistir o registro e confirmar a transacao para consolidar os dados.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L102-L103
  business_rule: RN-PRG-013
  acceptance:
  - Inclusao valida executa store do programa.
  - Inclusao valida executa confirmacao transacional apos store.
  priority: P0
  risk: CRITICO
  modernization_decision: migrar

REQ-PRG-014:
  pattern: state-driven
  text: Enquanto a operacao de consulta estiver ativa, o SIFAP deve retornar mensagem explicita quando o codigo de programa pesquisado nao existir.
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L117-L119
  business_rule: RN-PRG-014
  acceptance:
  - Consulta sem retorno apresenta mensagem de programa nao encontrado.
  - Consulta com retorno nao apresenta mensagem de ausencia.
  priority: P1
  risk: MEDIO
  modernization_decision: migrar

---

## Matriz de Confiança de Fonte

- Alta confiança: requisitos confirmados diretamente em código Natural analisado.
- Média confiança: requisitos vindos de documento 2012 com coerência de domínio, mas sem validação técnica formal.
- Baixa confiança: requisitos explicitamente pendentes no documento legado.

### Recomendação de uso
- Usar itens de alta confiança como base imediata da implementação.
- Tratar itens médios como candidato a validação com negócio.
- Transformar itens de baixa confiança em tarefas de arqueologia antes de codificar.
