# Input para /speckit.specify - CALCCORR

Use o texto abaixo como entrada do comando `/speckit.specify` para modernizar o programa legado CALCCORR.NSN.

## Observacao sobre a constitution

A constitution atual em `.specify/memory/constitution.md` esta apenas com placeholders. Portanto, gere a spec usando:

- o template padrao do Spec-Kit;
- as regras globais do repositorio para requisitos;
- rastreabilidade obrigatoria com `source_legacy:`;
- requisitos formais em EARS;
- criterios de aceitacao em Given/When/Then;
- foco em comportamento de negocio, sem detalhes de implementacao.

## Texto sugerido para /speckit.specify

Criar uma especificacao funcional para a modernizacao do programa legado `CALCCORR.NSN`, responsavel pelo calculo de correcao retroativa de pagamentos no SIFAP. A feature deve cobrir validacao de periodo, selecao de pagamentos elegiveis para correcao, calculo de indice acumulado e persistencia da correcao, preservando rastreabilidade com o legado Natural/Adabas.

Contexto de negocio:

- O programa recalcula pagamentos retroativos por variacao de indice IPCA em periodo informado.
- O fluxo processa pagamentos de um CPF dentro de uma faixa de competencia inicial/final.
- Apenas pagamentos ainda nao corrigidos e com diferenca positiva sao atualizados.
- O resultado final informa quantidade de registros corrigidos e valor total da correcao aplicada.
- O objetivo da spec e descrever o comportamento moderno esperado do processo de correcao retroativa, deixando explicitos os comportamentos que devem ser migrados, evoluidos ou descartados.

Escopo prioritario:

1. Validacao de periodo de competencia inicial/final.
2. Selecao de pagamentos por CPF no intervalo informado.
3. Exclusao de registros ja corrigidos.
4. Calculo do indice acumulado por competencia com base em tabela de indices.
5. Aplicacao da correcao sobre valor bruto original.
6. Truncamento para duas casas decimais.
7. Persistencia da correcao apenas quando houver diferenca positiva.
8. Fechamento com consolidado de quantidade e valor total corrigido.

Fora de escopo desta feature:

- geracao do pagamento original;
- calculo de beneficio mensal;
- manutencao cadastral de beneficiario;
- manutencao de parametros de programa social;
- tratamento de indices historicos especiais fora da regra corrente (ex.: bloco comentado Plano Verao).

Principais atores:

- operador financeiro que executa correcao retroativa;
- area de negocio responsavel por politica de indices;
- auditoria que valida os registros corrigidos e os valores consolidados.

Regras e comportamentos legados que devem orientar a spec:

1. O sistema nao deve aceitar periodo com competencia inicial maior que a competencia final.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L119-L121

2. O sistema deve processar pagamentos do CPF informado, filtrando apenas registros dentro do periodo solicitado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L128-L137

3. O sistema nao deve corrigir pagamentos que ja estejam marcados como corrigidos (`IND-CORRIGIDO = 'S'`).
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L140-L142

4. O sistema deve iniciar o indice acumulado em 1.000000 para cada pagamento processado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L146-L146

5. O sistema deve calcular o indice acumulado a partir da competencia do pagamento, decompondo ano/mes e aplicando fator do mes correspondente.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L176-L185

6. O sistema deve calcular valor corrigido como valor original multiplicado pelo indice acumulado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L152-L156

7. O sistema deve truncar o valor corrigido para duas casas decimais no padrao legado.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L154-L155

8. O sistema deve calcular a diferenca entre valor corrigido e valor original, aplicando correcao somente quando a diferenca for positiva.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L156-L158

9. Quando houver diferenca positiva, o sistema deve atualizar valor de correcao, data de correcao e indicador de corrigido no pagamento.
   source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L159-L162

10. O sistema deve confirmar transacao apos update de pagamento corrigido.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L163-L163

11. O sistema deve acumular valor total corrigido e quantidade de registros efetivamente corrigidos.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L164-L165

12. Ao final, o sistema deve apresentar resumo com quantidade de registros corrigidos e valor total de correcao.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L170-L173

13. O bloco historico de "Plano Verao" esta comentado e nao deve ser assumido como regra ativa sem decisao explicita de negocio.
    source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L100-L110

Pontos de modernizacao que a spec deve explicitar:

- quais regras do legado devem ser migradas sem alteracao;
- quais regras precisam de evolucao por risco regulatorio ou de auditoria;
- se a tabela de IPCA interna hardcoded deve virar fonte parametrizavel e versionada;
- como garantir rastreabilidade da memoria de calculo do indice acumulado;
- como tratar correcao com diferenca nao positiva (sem update) de forma auditavel.

User stories esperadas:

- P1: executar correcao retroativa valida para um CPF e periodo;
- P1: impedir processamento com periodo invalido;
- P1: atualizar apenas pagamentos elegiveis e ainda nao corrigidos;
- P1: fornecer consolidado final com total corrigido e quantidade;
- P2: explicitar o que do legado sera preservado e o que sera evoluido no tratamento de indices.

Edge cases que a spec deve cobrir:

- competencia inicial maior que final;
- CPF sem pagamentos no periodo;
- pagamento fora do periodo informado;
- pagamento ja marcado como corrigido;
- indice nao encontrado para determinado ano/mes;
- diferenca de correcao igual a zero;
- diferenca de correcao negativa;
- truncamento de valores monetarios em fronteiras;
- falha em update de parte dos registros (comportamento transacional esperado);
- bloco historico comentado que nao deve ser executado sem reativacao formal.

Entidades principais:

- Pagamento: numero, CPF do beneficiario, competencia, valor bruto, desconto, liquido, valor de correcao, data de correcao, indicador de corrigido.
- Periodo de Correcao: competencia inicial e final, CPF alvo, data de processamento.
- Indice Mensal: ano, mes, valor do indice, fator acumulado aplicavel.
- Resultado de Correcao: quantidade de registros corrigidos, valor total de correcao, detalhes por registro.

Criticos para a qualidade da spec:

- usar linguagem de negocio e nao de implementacao;
- escrever requisitos formais testaveis;
- incluir `source_legacy:` em todos os requisitos funcionais derivados do legado;
- limitar clarificacoes abertas ao minimo necessario;
- destacar como assuncao que a constitution atual nao esta ratificada e que as regras de requisitos do repositorio foram usadas como baseline;
- manter explicabilidade e auditabilidade do calculo como requisito central.

Resultados esperados da spec:

- user scenarios independentes e priorizados;
- requisitos funcionais completos e testaveis;
- criterios de sucesso mensuraveis e agnosticos de tecnologia;
- edge cases claros;
- assuncoes explicitas sobre governanca e modernizacao do processo de correcao retroativa do legado.