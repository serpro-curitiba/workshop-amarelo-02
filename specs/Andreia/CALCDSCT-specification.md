# Input para /speckit.specify - CALCDSCT

Use o texto abaixo como entrada do comando `/speckit.specify` para modernizar o programa legado CALCDSCT.NSN.

## Observacao sobre a constitution

A constitution atual em `.specify/memory/constitution.md` esta apenas com placeholders. Portanto, gere a spec usando:

- o template padrao do Spec-Kit;
- as regras globais do repositorio para requisitos;
- rastreabilidade obrigatoria com `source_legacy:`;
- requisitos formais em EARS;
- criterios de aceitacao em Given/When/Then;
- foco em comportamento de negocio, sem detalhes de implementacao.

## Texto sugerido para /speckit.specify

Criar uma especificacao funcional para a modernizacao do programa legado `CALCDSCT.NSN`, responsavel pelo calculo de descontos e deducoes de pagamento no SIFAP. A feature deve cobrir localizacao de pagamento e beneficiario, aplicacao de descontos obrigatorios e cadastrados, regras de teto e persistencia do valor final de desconto, preservando rastreabilidade com o legado Natural/Adabas.

Contexto de negocio:

- O programa calcula descontos sobre o valor bruto de um pagamento especifico.
- O processo considera descontos obrigatorios (contribuicao social) e descontos cadastrados por beneficiario.
- Parte dos descontos respeita teto de 30% do bruto, com excecao de desconto judicial.
- O resultado final atualiza o pagamento com o valor total de desconto calculado.
- O objetivo da spec e descrever o comportamento moderno esperado do motor de descontos, deixando explicitos os comportamentos que devem ser migrados, evoluidos ou descartados.

Escopo prioritario:

1. Localizacao do pagamento por numero e CPF.
2. Validacao de existencia de beneficiario.
3. Calculo obrigatorio de contribuicao social por faixa.
4. Calculo de teto maximo de desconto (30% do bruto).
5. Processamento de descontos cadastrados com vigencia.
6. Regras por tipo de desconto (J, P, I, S, A).
7. Aplicacao de teto para descontos nao judiciais.
8. Persistencia do valor total de desconto no pagamento.

Fora de escopo desta feature:

- calculo do valor bruto do beneficio;
- geracao do pagamento mensal;
- manutencao cadastral de descontos no beneficiario;
- processamento batch de toda a carteira de pagamentos;
- integracoes externas nao presentes no CALCDSCT.

Principais atores:

- operador financeiro que calcula descontos para um pagamento especifico;
- area de negocio responsavel pelas politicas de deducao;
- auditoria que valida teto, excecoes e valor final aplicado.

Regras e comportamentos legados que devem orientar a spec:

1. O sistema deve localizar pagamento pelo numero informado e validar aderencia ao CPF informado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L72-L84

2. O sistema deve interromper o fluxo quando pagamento nao for encontrado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L83-L84

3. O sistema deve interromper o fluxo quando beneficiario nao for encontrado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L88-L93

4. O sistema deve sempre aplicar desconto de contribuicao social por faixa de valor bruto.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L99-L99
   source_legacy_detail: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L192-L200

5. O sistema deve calcular teto maximo de desconto como 30% do valor bruto, com truncamento para duas casas decimais.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L102-L105

6. O sistema deve processar apenas descontos vigentes, ignorando descontos expirados ou ainda nao iniciados.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L112-L117

7. O sistema deve calcular desconto judicial (`J`) por valor fixo ou percentual e nao aplicar teto sobre esse tipo.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L123-L132

8. O sistema deve calcular pensao (`P`) por valor fixo ou percentual.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L133-L141

9. O sistema deve calcular imposto (`I`) por percentual sobre o valor bruto.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L146

10. O sistema deve calcular desconto sindical (`S`) como 1% do valor bruto.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L147-L150

11. O sistema deve calcular desconto administrativo (`A`) por valor fixo ou percentual.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L151-L160

12. O sistema deve ignorar tipos de desconto desconhecidos.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L161-L163

13. O sistema deve aplicar teto de 30% para descontos nao judiciais quando total ultrapassar o limite.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L164-L168

14. O sistema deve truncar o desconto total para duas casas decimais antes de persistir.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L174-L175

15. O sistema deve atualizar o pagamento com o valor total de desconto e confirmar transacao.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L179-L182

16. O sistema deve apresentar o resumo do calculo com valor bruto, desconto total e teto aplicado.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L185-L188

Pontos de modernizacao que a spec deve explicitar:

- quais regras do legado devem ser migradas sem alteracao;
- quais regras precisam de evolucao por risco regulatorio ou de politica financeira;
- se o teto de 30% para descontos nao judiciais permanece como regra geral;
- como tratar excecoes judiciais com rastreabilidade e auditabilidade;
- como tornar governavel a politica de aliquotas e tipos de desconto.

User stories esperadas:

- P1: calcular descontos de um pagamento de forma consistente e auditavel;
- P1: impedir processamento quando pagamento ou beneficiario nao existirem;
- P1: aplicar teto de desconto corretamente com excecao judicial;
- P2: preservar diferencas por tipo de desconto com memoria de calculo;
- P2: explicitar o que do legado sera preservado e o que sera evoluido nas regras de deducao.

Edge cases que a spec deve cobrir:

- pagamento inexistente para o numero/CPF informado;
- beneficiario inexistente;
- desconto com vigencia encerrada;
- desconto com inicio futuro;
- desconto judicial por valor fixo versus percentual;
- acumulacao de multiplos descontos nao judiciais acima do teto;
- coexistencia de desconto judicial com descontos nao judiciais;
- tipo de desconto desconhecido;
- truncamento nas bordas monetarias;
- pagamento com valor bruto muito baixo e impacto no desconto compulsorio.

Entidades principais:

- Pagamento: numero, CPF do beneficiario, valor bruto, valor de desconto, competencia.
- Beneficiario: CPF, status, UF e lista de descontos cadastrados com tipo, valor, percentual e vigencia.
- Desconto Cadastrado: tipo, valor fixo, percentual, data de inicio, data de fim, identificador de processo.
- Resultado de Desconto: valor total de desconto, teto aplicado, composicao por tipo.

Criticos para a qualidade da spec:

- usar linguagem de negocio e nao de implementacao;
- escrever requisitos formais testaveis;
- incluir `source_legacy:` em todos os requisitos funcionais derivados do legado;
- limitar clarificacoes abertas ao minimo necessario;
- destacar como assuncao que a constitution atual nao esta ratificada e que as regras de requisitos do repositorio foram usadas como baseline;
- manter transparencia da regra de teto e da excecao judicial para auditoria.

Resultados esperados da spec:

- user scenarios independentes e priorizados;
- requisitos funcionais completos e testaveis;
- criterios de sucesso mensuraveis e agnosticos de tecnologia;
- edge cases claros;
- assuncoes explicitas sobre governanca e modernizacao do processo de descontos do legado.