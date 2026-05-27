# Especificacao da Funcionalidade: Consulta Modernizada de Beneficiario

**Branch da Funcionalidade**: `003-modernize-consbenf-query`

**Criado em**: 2026-05-27

**Status**: Rascunho

**Entrada**: Descricao do usuario: "Modernizar a funcionalidade legada CONSBENF (consulta de beneficiario) para a stack alvo do projeto SIFAP 2.0, preservando comportamento observavel e respeitando a constituicao do repositorio."

## Cenarios de Usuario e Testes *(obrigatorio)*

### Historia de Usuario 1 - Consultar por CPF ou NIS (Prioridade: P1)

Como operador, quero consultar um beneficiario por CPF ou NIS e visualizar os dados cadastrais com historico de pagamentos para tomar decisoes de atendimento rapidamente.

**Por que esta prioridade**: Este e o fluxo principal do legado CONSBENF e entrega valor imediato de negocio ao substituir a tela 3270 de consulta.

**Teste Independente**: Pode ser totalmente testada consultando um beneficiario existente por CPF e outro por NIS e validando o retorno de cadastro e historico de no maximo 12 pagamentos.

**Cenarios de Aceitacao**:

1. **Dado** um beneficiario existente e tipo de busca CPF, **Quando** o usuario consulta informando CPF valido, **Entao** o sistema retorna dados cadastrais e historico de pagamentos limitado aos 12 registros mais recentes.
2. **Dado** um beneficiario existente e tipo de busca NIS, **Quando** o usuario consulta informando NIS valido, **Entao** o sistema retorna os mesmos dados de dominio esperados para consulta por CPF.
3. **Dado** que o tipo de busca nao foi informado, **Quando** o usuario envia consulta com CPF, **Entao** o sistema assume tipo CPF como padrao.

---

### Historia de Usuario 2 - Tratar erros e status de forma consistente (Prioridade: P2)

Como operador, quero mensagens e codigos de erro consistentes para tipo de busca invalido e beneficiario nao encontrado, e quero descricoes de status padronizadas para leitura humana.

**Por que esta prioridade**: Reduz ambiguidade operacional e evita divergencia de comportamento em relacao ao legado.

**Teste Independente**: Pode ser testada enviando tipo invalido, busca sem resultado e verificando mapeamento de status de beneficiario para descricao textual.

**Cenarios de Aceitacao**:

1. **Dado** tipo de busca invalido, **Quando** a consulta e submetida, **Entao** o sistema retorna erro de validacao explicito para tipo de busca.
2. **Dado** CPF ou NIS sem correspondencia, **Quando** a consulta e processada, **Entao** o sistema retorna resposta de nao encontrado sem dados cadastrais.
3. **Dado** um beneficiario com status valido no cadastro, **Quando** os dados sao retornados, **Entao** o sistema exibe descricao de status conforme tabela de mapeamento definida.

---

### Historia de Usuario 3 - Proteger dados sensiveis e garantir operabilidade (Prioridade: P3)

Como time de operacoes e seguranca, quero que CPF fique mascarado em exibicoes e logs, e que a funcionalidade tenha requisitos de readiness e monitoracao para operacao continua.

**Por que esta prioridade**: Atende exigencias de seguranca e de operabilidade da constituicao, reduzindo risco de exposicao indevida de dados.

**Teste Independente**: Pode ser testada com inspeção de respostas, logs de aplicacao e checklist de prontidao para confirmar mascaramento e sinais operacionais minimos.

**Cenarios de Aceitacao**:

1. **Dado** uma consulta bem-sucedida, **Quando** o sistema retorna identificadores de beneficiario, **Entao** CPF aparece apenas mascarado na visualizacao ao usuario.
2. **Dado** uma execucao de consulta, **Quando** eventos sao registrados, **Entao** logs nao contem CPF em formato integral.
3. **Dado** a funcionalidade implantada, **Quando** operacoes executa checklist de prontidao, **Entao** os itens minimos de monitoracao, alerta e procedimento de incidente estao disponiveis.

---

### Casos de Borda

- O que acontece quando o tipo de busca e invalido (diferente de C ou N)?
- Como o sistema trata requisicao com tipo C sem CPF ou tipo N sem NIS?
- Como o sistema trata beneficiario inexistente?
- Como o sistema trata beneficiario existente sem historico de pagamentos?
- Como o sistema trata historico com mais de 12 pagamentos (corte de retorno)?

## Requisitos *(obrigatorio)*

### Requisitos Funcionais

- **FR-001 (REQ-CONSBENF-001)**: O sistema DEVE permitir consulta de beneficiario por CPF quando o tipo de busca for `C`.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-002 (REQ-CONSBENF-002)**: O sistema DEVE permitir consulta de beneficiario por NIS quando o tipo de busca for `N`.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-003 (REQ-CONSBENF-003)**: O sistema DEVE assumir CPF como tipo de busca padrao quando o tipo estiver em branco.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-004 (REQ-CONSBENF-004)**: O sistema DEVE rejeitar valores de tipo de busca nao suportados com erro de validacao explicito.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-005 (REQ-CONSBENF-005)**: O sistema DEVE retornar resposta clara de nao encontrado quando nenhum beneficiario corresponder ao identificador informado.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-006 (REQ-CONSBENF-006)**: O sistema DEVE retornar atributos cadastrais essenciais do beneficiario (identificacao, dados pessoais e dados de cadastro) em consultas bem-sucedidas.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm`
- **FR-007 (REQ-CONSBENF-007)**: O sistema DEVE retornar historico de pagamentos limitado a 12 registros em consultas bem-sucedidas.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
  - **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm`
- **FR-008 (REQ-CONSBENF-008)**: O sistema DEVE retornar mensagem/estado claro de historico vazio quando o beneficiario nao possuir pagamentos.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-009 (REQ-CONSBENF-009)**: O sistema DEVE mapear codigos de status do beneficiario para descricoes legiveis usando a tabela: `A=ATIVO`, `S=SUSPENSO`, `C=CANCELADO`, `I=INATIVO`, `D=DESLIGADO`, fallback=`DESCONHECIDO`.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-010 (REQ-CONSBENF-010)**: O sistema DEVE mascarar CPF nas saidas voltadas ao usuario.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-011 (REQ-CONSBENF-011)**: O sistema DEVE manter comportamento de mascaramento de CPF compativel com o legado e NAO DEVE alterar a inconsistencia historica conhecida sem aprovacao formal de auditoria.
  - **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`
- **FR-012 (REQ-CONSBENF-012)**: O sistema DEVE garantir que logs nao exponham valores integrais de CPF.
  - **source_legacy**: `[GREENFIELD] Requisito introduzido para atender a politica constitucional de seguranca em observabilidade moderna.`
- **FR-013 (REQ-CONSBENF-013)**: O sistema DEVE definir verificacoes de readiness e operabilidade de incidente para o fluxo de consulta (disponibilidade de dependencias de dados, comportamento degradado e caminho de resposta operacional).
  - **source_legacy**: `[GREENFIELD] Requisito operacional exigido pela constituicao como entregavel para operacao moderna em producao.`

### Entidades-Chave *(incluir se a funcionalidade envolver dados)*

- **BeneficiaryQueryInput**: Representa os criterios de consulta com tipo de busca e um identificador (CPF ou NIS).
- **BeneficiaryProfile**: Representa os dados cadastrais essenciais do beneficiario retornados pela consulta.
- **PaymentHistoryEntry**: Representa um item de pagamento associado ao beneficiario, incluindo competencia, valores bruto/desconto/liquido e status/tipo.
- **BeneficiaryConsultationResult**: Representa o envelope final de resposta com perfil, descricao de status, historico de pagamentos e estado de vazio/nao encontrado.

## Criterios de Sucesso *(obrigatorio)*

### Resultados Mensuraveis

- **SC-001**: 95% das consultas validas por CPF ou NIS retornam resultado (ou nao encontrado) em ate 2 segundos em horario comercial.
- **SC-002**: 100% das respostas de consulta exibem CPF mascarado para o usuario final.
- **SC-003**: 100% dos cenarios de erro de entrada obrigatoria (tipo invalido, identificador ausente) retornam mensagem clara e consistente.
- **SC-004**: Para beneficiarios com historico acima de 12 pagamentos, 100% das respostas respeitam o limite maximo de 12 itens.
- **SC-005**: Em auditoria de logs de consulta, 0 ocorrencias de CPF integral exposto apos a implantacao.

## Premissas

- A base de dados de beneficiario e pagamento ja existe e esta acessivel no ambiente alvo.
- O mapeamento de status definido no legado CONSBENF continua valido para o escopo desta funcionalidade.
- A consulta moderna mantera equivalencia funcional com o legado sem redesenhar regras de outros programas (como CALCBENF e VALBENEF).
- O time de negocio/auditoria fornecera aprovacao formal caso seja proposta alteracao da regra historica de mascara de CPF.
