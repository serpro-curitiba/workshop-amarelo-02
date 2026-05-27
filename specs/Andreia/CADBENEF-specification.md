# Input para /speckit.specify - CADBENEF

Use o texto abaixo como entrada do comando `/speckit.specify` para modernizar o programa legado CADBENEF.NSN.

## Observacao sobre a constitution

A constitution atual em `.specify/memory/constitution.md` esta apenas com placeholders. Portanto, gere a spec usando:

- o template padrao do Spec-Kit;
- as regras globais do repositorio para requisitos;
- rastreabilidade obrigatoria com `source_legacy:`;
- requisitos formais em EARS;
- criterios de aceitacao em Given/When/Then;
- foco em comportamento de negocio, sem detalhes de implementacao.

## Texto sugerido para /speckit.specify

Criar uma especificacao funcional para a modernizacao do programa legado `CADBENEF.NSN`, responsavel pelo cadastro de beneficiarios no SIFAP. A feature deve cobrir inclusao e alteracao cadastral de beneficiario, preservando rastreabilidade com o legado Natural/Adabas.

Contexto de negocio:

- O programa legado realiza manutencao de dados cadastrais de beneficiarios no arquivo 150.
- A operacao aceita apenas inclusao (`I`) e alteracao (`A`).
- O fluxo exige validacoes obrigatorias antes de qualquer persistencia.
- O objetivo da spec e descrever o comportamento moderno esperado do modulo de beneficiarios, deixando explicitos os comportamentos que devem ser migrados, evoluidos ou descartados.

Escopo prioritario:

1. Cadastro de beneficiario com validacoes obrigatorias de entrada.
2. Alteracao de beneficiario existente.
3. Validacao de CPF pelo algoritmo modulo 11.
4. Regras de existencia previa para inclusao e alteracao.
5. Definicao de status inicial e tratamento para beneficiarios com idade acima de 75 anos.
6. Persistencia de datas de cadastro e atualizacao.
7. Restricoes e lacunas do legado que exigem decisao explicita de modernizacao.

Fora de escopo desta feature:

- calculo de beneficio;
- validacao de elegibilidade por programa;
- cadastro de dependentes;
- processamento batch de pagamentos;
- integracoes externas nao presentes no CADBENEF.

Principais atores:

- operador de cadastro que inclui beneficiarios;
- operador de cadastro que altera beneficiarios existentes;
- area de negocio responsavel por governanca cadastral.

Regras e comportamentos legados que devem orientar a spec:

1. O sistema nao deve aceitar operacao diferente de `I` ou `A`.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L99-L103

2. O sistema deve exigir CPF obrigatorio.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L105-L109

3. O sistema nao deve permitir inclusao ou alteracao com CPF invalido pelo algoritmo modulo 11.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L111-L116
   source_legacy_detail: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L221-L259

4. O sistema deve exigir nome obrigatorio.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L118-L122

5. O sistema deve exigir data de nascimento obrigatoria.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L124-L128

6. O sistema nao deve aceitar sexo diferente de `M` ou `F`.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L130-L134

7. O sistema nao deve permitir inclusao de beneficiario com CPF ja cadastrado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L136-L147

8. Enquanto a operacao for alteracao, o sistema deve exigir que o beneficiario exista previamente.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L149-L153

9. Quando a operacao for inclusao valida, o sistema deve definir status inicial `A`.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L162-L164

10. Quando a idade calculada do beneficiario for maior que 75 anos, o legado move o status para `S`.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L156-L169
    observacao: a spec moderna deve tratar esse comportamento como regra a confirmar/evoluir, pois ha risco de suspensao automatica sem revisao.

11. Quando a inclusao for concluida com sucesso, o sistema deve persistir todos os dados cadastrais e registrar data de cadastro e atualizacao com a data corrente.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L178-L197

12. Quando a alteracao for concluida com sucesso, o sistema deve atualizar apenas os atributos permitidos pelo legado, preservando CPF e data de cadastro, e atualizar a data de atualizacao.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L198-L214

13. O sistema deve confirmar a transacao apos store ou update bem-sucedido.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L196-L197
    source_legacy_detail: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L212-L214

Pontos de modernizacao que a spec deve explicitar:

- quais regras do legado devem ser migradas sem alteracao;
- quais regras precisam de evolucao por risco de negocio ou compliance;
- quais restricoes de interface terminal nao devem virar regra de dominio;
- quais campos alteraveis precisam de politica de governanca mais explicita no sistema moderno.

User stories esperadas:

- P1: incluir novo beneficiario valido com sucesso;
- P1: impedir inclusao/alteracao com dados obrigatorios invalidos;
- P1: alterar beneficiario existente mantendo integridade cadastral;
- P2: tratar beneficiario acima de 75 anos com politica explicita e auditavel;
- P2: explicitar o que do legado sera preservado e o que sera evoluido.

Edge cases que a spec deve cobrir:

- CPF igual a zero;
- CPF com digito verificador invalido;
- nome vazio;
- data de nascimento ausente;
- sexo invalido;
- tentativa de incluir CPF duplicado;
- tentativa de alterar CPF inexistente;
- beneficiario com idade acima de 75 anos;
- diferenca entre campos permitidos em inclusao e alteracao;
- comportamento quando a operacao informada e invalida.

Entidades principais:

- Beneficiario: CPF, nome, data de nascimento, sexo, endereco, municipio, UF, CEP, telefone, RG, status, codigo do programa, renda familiar, numero de dependentes, data de cadastro, data de atualizacao, codigo de regiao, NIS.
- Operacao de Cadastro: tipo de operacao, validacoes executadas, resultado da persistencia.

Criticos para a qualidade da spec:

- usar linguagem de negocio e nao de implementacao;
- escrever requisitos formais testaveis;
- incluir `source_legacy:` em todos os requisitos funcionais derivados do legado;
- limitar clarificacoes abertas ao minimo necessario;
- destacar como assuncao que a constitution atual nao esta ratificada e que as regras de requisitos do repositorio foram usadas como baseline.

Resultados esperados da spec:

- user scenarios independentes e priorizados;
- requisitos funcionais completos e testaveis;
- criterios de sucesso mensuraveis e agnosticos de tecnologia;
- edge cases claros;
- assuncoes explicitas sobre governanca e modernizacao do comportamento legado.