# Input para /speckit.specify - CALCBENF

Use o texto abaixo como entrada do comando `/speckit.specify` para modernizar o programa legado CALCBENF.NSN.

## Observacao sobre a constitution

A constitution atual em `.specify/memory/constitution.md` esta apenas com placeholders. Portanto, gere a spec usando:

- o template padrao do Spec-Kit;
- as regras globais do repositorio para requisitos;
- rastreabilidade obrigatoria com `source_legacy:`;
- requisitos formais em EARS;
- criterios de aceitacao em Given/When/Then;
- foco em comportamento de negocio, sem detalhes de implementacao.

## Texto sugerido para /speckit.specify

Criar uma especificacao funcional para a modernizacao do programa legado `CALCBENF.NSN`, responsavel pelo calculo do valor de beneficio mensal no SIFAP. A feature deve cobrir validacao de entrada, calculo de componentes do beneficio, composicao de bruto/liquido e gravacao do pagamento, preservando rastreabilidade com o legado Natural/Adabas.

Contexto de negocio:

- O programa calcula o beneficio a partir de dados do beneficiario, parametros do programa e tabelas internas de fator.
- O fluxo aplica fatores regionais, familiares, renda e idade para compor o valor.
- Em dezembro, ha regras adicionais para 13o e abono natalino.
- O resultado final grava um registro de pagamento com bruto, desconto e liquido.
- O objetivo da spec e descrever o comportamento moderno esperado do motor de calculo, deixando explicitos os comportamentos que devem ser migrados, evoluidos ou descartados.

Escopo prioritario:

1. Validacao de competencia (AAAAMM) e beneficiario ativo.
2. Leitura de beneficiario e programa para base de calculo.
3. Determinacao dos fatores (regional, familiar, renda, idade).
4. Calculo principal do valor de beneficio e aplicacao de reajuste.
5. Truncamento de valores monetarios para duas casas decimais.
6. Regra de dezembro para 13o e abono natalino.
7. Calculo simplificado de descontos e valor liquido minimo zero.
8. Persistencia do pagamento gerado com status e tipo de pagamento.

Fora de escopo desta feature:

- manutencao de cadastro de beneficiario;
- manutencao de programas sociais;
- motor completo de descontos (coberto em CALCDSCT);
- processamento batch de toda a base (coberto em BATCHPGT);
- integracoes externas nao presentes no CALCBENF.

Principais atores:

- operador que executa calculo pontual de beneficio;
- area de negocio responsavel por regras de valor;
- controle operacional que precisa rastrear o pagamento gerado.

Regras e comportamentos legados que devem orientar a spec:

1. O sistema deve exigir competencia valida, com mes entre 1 e 12.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L154-L162

2. O sistema deve interromper o fluxo quando o beneficiario nao for encontrado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L165-L176

3. O sistema nao deve calcular beneficio para beneficiario com status diferente de `A`.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L178-L181

4. O sistema deve interromper o fluxo quando o programa do beneficiario nao for encontrado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L184-L196

5. O sistema deve determinar fator regional pela tabela interna para regioes 1..25 e usar 1.0000 para demais codigos.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L199-L204
   source_legacy_detail: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L93-L127

6. O sistema deve calcular fator familiar conforme faixas de numero de dependentes.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L207-L218

7. O sistema deve determinar fator de renda pela primeira faixa cujo teto seja maior ou igual a renda informada.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L301-L307
   source_legacy_detail: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L130-L139

8. O sistema deve calcular fator de idade por faixas etarias (<18, 60-64, >=65, demais).
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L223-L236

9. O sistema deve calcular o valor base do beneficio pela multiplicacao de base e fatores (regional, familiar, renda, idade).
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L243-L245

10. O sistema deve aplicar reajuste do programa sobre o valor calculado.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L248-L248

11. O sistema deve truncar valores monetarios para duas casas decimais no padrao legado.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L251-L252

12. Em dezembro, o sistema deve calcular 13o e somar ao bruto.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L261-L268

13. Em dezembro e para programa tipo `A`, o sistema deve calcular abono natalino de 15% sobre o valor de beneficio.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L270-L278

14. O sistema deve calcular desconto simplificado de 3% apenas quando o bruto ultrapassar 500.00.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L316-L321

15. O sistema deve calcular valor liquido como bruto menos desconto e aplicar piso zero para resultado negativo.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L285-L288

16. O sistema deve persistir pagamento com bruto, desconto, liquido, competencia, data de geracao, tipo de pagamento e status `G`.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L293-L304

17. O sistema deve confirmar a transacao apos gravar pagamento.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L305-L306

Pontos de modernizacao que a spec deve explicitar:

- quais regras do legado devem ser migradas sem alteracao;
- quais regras precisam de evolucao por risco de negocio ou compliance;
- se as tabelas internas fixas (fator regional e faixas de renda) devem virar parametrizacao externa governavel;
- como tratar explicabilidade da composicao do valor calculado para auditoria;
- como tratar os arredondamentos/truncamentos de forma reproduzivel no sistema moderno.

User stories esperadas:

- P1: calcular beneficio mensal valido para beneficiario ativo e programa existente;
- P1: impedir calculo quando competencia, beneficiario ou programa forem invalidos;
- P1: gerar pagamento com bruto, desconto e liquido de forma rastreavel;
- P2: aplicar corretamente regras de dezembro (13o e abono);
- P2: explicitar o que do legado sera preservado e o que sera evoluido no calculo.

Edge cases que a spec deve cobrir:

- competencia com mes invalido;
- beneficiario nao encontrado;
- beneficiario nao ativo;
- programa nao encontrado;
- codigo de regiao fora do intervalo esperado;
- renda exatamente no limite de faixa;
- renda acima da ultima faixa parametrizada;
- beneficiario sem dependentes e com muitos dependentes;
- idade nas bordas 17, 18, 59, 60, 64, 65;
- calculo em dezembro para tipo `A` e nao `A`;
- valor liquido negativo apos descontos;
- truncamento de valores em etapas intermediarias e finais.

Entidades principais:

- Beneficiario: CPF, status, codigo do programa, renda familiar, numero de dependentes, codigo de regiao, data de nascimento.
- Programa Social: codigo, tipo, valor base, fator de reajuste.
- Pagamento: CPF beneficiario, codigo do programa, competencia, valor bruto, desconto, valor liquido, data de geracao, status, tipo de pagamento, valor de abono.
- Avaliacao de Calculo: fatores aplicados, memoria de calculo, resultado final.

Criticos para a qualidade da spec:

- usar linguagem de negocio e nao de implementacao;
- escrever requisitos formais testaveis;
- incluir `source_legacy:` em todos os requisitos funcionais derivados do legado;
- limitar clarificacoes abertas ao minimo necessario;
- destacar como assuncao que a constitution atual nao esta ratificada e que as regras de requisitos do repositorio foram usadas como baseline;
- manter rastreabilidade da formula e dos fatores para auditoria de calculo.

Resultados esperados da spec:

- user scenarios independentes e priorizados;
- requisitos funcionais completos e testaveis;
- criterios de sucesso mensuraveis e agnosticos de tecnologia;
- edge cases claros;
- assuncoes explicitas sobre governanca e modernizacao do calculo do legado.