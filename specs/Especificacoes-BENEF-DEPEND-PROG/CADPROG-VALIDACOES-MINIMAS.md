# SPECIFICATION - CADPROG Validacoes Minimas (EARS)

## Metadados
- Versao: 0.1.0
- Escopo: cadastro de programas sociais (operacao de inclusao/manutencao)
- Fonte: leitura de codigo Natural e DDM do legado SIFAP
- Status: draft para validacao funcional

---

## Requisitos - Cadastro de Programas (EARS)

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

## Observacoes de modernizacao
- O legado atual de CADPROG esta focado em inclusao/consulta e nao implementa todas as validacoes descritas acima.
- As regras acima representam o conjunto minimo para evitar parametrizacao invalida com impacto em elegibilidade e calculo.
- Recomenda-se converter estes requisitos em testes automatizados de contrato e de validacao de dominio antes da implementacao.
