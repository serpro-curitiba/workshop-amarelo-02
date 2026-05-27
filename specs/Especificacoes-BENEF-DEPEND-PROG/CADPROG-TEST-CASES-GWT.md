# Casos de Teste - CADPROG (Given/When/Then)

## Escopo
- Requisitos alvo: REQ-PRG-001 a REQ-PRG-014
- Objetivo: transformar acceptance criteria em cenarios executaveis

---

## REQ-PRG-001 - Tipo do programa

### CT-PRG-001.1
Given operacao de cadastro de programa
And tipo informado igual a A
When o usuario envia o formulario
Then o sistema aceita a validacao de tipo

### CT-PRG-001.2
Given operacao de cadastro de programa
And tipo informado igual a X
When o usuario envia o formulario
Then o sistema retorna erro de validacao de tipo
And o cadastro nao e persistido

## REQ-PRG-002 - Unicidade/existencia por operacao

### CT-PRG-002.1
Given um codigo de programa ja cadastrado
When o usuario tenta incluir novo programa com o mesmo codigo
Then o sistema retorna conflito

### CT-PRG-002.2
Given um codigo de programa inexistente
When o usuario tenta alterar esse programa
Then o sistema retorna nao encontrado

## REQ-PRG-003 - Data de inicio valida

### CT-PRG-003.1
Given data de inicio igual a 20260115
When o usuario envia o cadastro
Then o sistema aceita a data de inicio

### CT-PRG-003.2
Given data de inicio igual a 0
When o usuario envia o cadastro
Then o sistema retorna erro de validacao de vigencia

## REQ-PRG-004 - Coerencia de vigencia

### CT-PRG-004.1
Given data de inicio 20260101
And data fim igual a 0
When o usuario envia o cadastro
Then o sistema interpreta vigencia indeterminada

### CT-PRG-004.2
Given data de inicio 20260101
And data fim 20251231
When o usuario envia o cadastro
Then o sistema retorna erro por data fim menor que data inicio

## REQ-PRG-005 - Limite e estrutura de faixas

### CT-PRG-005.1
Given um programa com 5 faixas validas cadastradas
When o usuario tenta incluir a sexta faixa
Then o sistema retorna erro de limite de faixas

### CT-PRG-005.2
Given uma faixa sem fator multiplicador
When o usuario tenta salvar a faixa
Then o sistema retorna erro de campo obrigatorio

## REQ-PRG-006 - Faixas sem sobreposicao

### CT-PRG-006.1
Given faixa 1 de 0 a 300
And faixa 2 de 250 a 600
When o usuario salva as faixas
Then o sistema retorna erro de sobreposicao

### CT-PRG-006.2
Given faixa 1 de 0 a 300
And faixa 2 de 301 a 600
When o usuario salva as faixas
Then o sistema aceita as faixas

## REQ-PRG-007 - Renda maxima nao negativa

### CT-PRG-007.1
Given renda maxima igual a 0
When o usuario salva o cadastro
Then o sistema aceita o valor

### CT-PRG-007.2
Given renda maxima igual a -1
When o usuario salva o cadastro
Then o sistema retorna erro de validacao de renda maxima

## REQ-PRG-008 - Faixa etaria consistente

### CT-PRG-008.1
Given idade minima igual a 18
And idade maxima igual a 65
When o usuario salva o cadastro
Then o sistema aceita a faixa etaria

### CT-PRG-008.2
Given idade minima igual a 70
And idade maxima igual a 60
When o usuario salva o cadastro
Then o sistema retorna erro de faixa etaria invalida

### CT-PRG-008.3
Given idade minima igual a -1
And idade maxima igual a 10
When o usuario salva o cadastro
Then o sistema retorna erro por idade fora do intervalo permitido

## REQ-PRG-009 - Operacao permitida I/C

### CT-PRG-009.1
Given operacao informada igual a I
When o usuario inicia o processamento de cadastro
Then o sistema aceita a operacao

### CT-PRG-009.2
Given operacao informada igual a X
When o usuario inicia o processamento de cadastro
Then o sistema retorna erro de operacao invalida

## REQ-PRG-010 - Fluxo exclusivo de consulta

### CT-PRG-010.1
Given operacao igual a C
And codigo de programa existente
When o usuario executa a consulta
Then o sistema exibe os dados do programa
And nao persiste alteracoes

### CT-PRG-010.2
Given operacao igual a C
And codigo de programa inexistente
When o usuario executa a consulta
Then o sistema retorna programa nao encontrado

## REQ-PRG-011 - Calculo de valor ajustado com FATOR-K

### CT-PRG-011.1
Given valor base informado 100.00
And fator reajuste informado 0.1000
When o usuario confirma a inclusao
Then o sistema calcula FATOR-K como 1.00 + (0.1000 * 0.347215)
And persiste valor base ajustado igual a valor base informado multiplicado pelo FATOR-K

## REQ-PRG-012 - Status inicial do programa

### CT-PRG-012.1
Given inclusao de programa valida
When o sistema conclui a persistencia
Then o status do programa e gravado como A

## REQ-PRG-013 - Confirmacao transacional

### CT-PRG-013.1
Given inclusao de programa valida
When o sistema executa o store do registro
Then o sistema confirma a transacao
And o registro fica consolidado para consulta subsequente

## REQ-PRG-014 - Mensagem para nao encontrado em consulta

### CT-PRG-014.1
Given operacao de consulta ativa
And codigo de programa sem correspondencia
When o usuario executa a busca
Then o sistema apresenta mensagem explicita de programa nao encontrado
