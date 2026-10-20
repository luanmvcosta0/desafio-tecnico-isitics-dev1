# Barema do frontend — checklist de avaliação

Frontend vale **50 pontos (5,0 na nota)**; é preciso **≥ 2,5 no frontend** (e nota final ≥ 7,0).

## F1. Roteiro da interface (30 pontos) — executado na tela, Chrome 1366 × 768

| Item                        | O que é verificado                                                                                                                                                                                                                                               | Pts   |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **Etapa 1: Jogadores**      |                                                                                                                                                                                                                                                                  | **7** |
| F01                         | Tabela com nome, posição, overall, agilidade, idade, condição, valor base, valor de mercado e clube ("Livre"); paginação de 10, navegação e total encontrado                                                                                                     | 2     |
| F02                         | Busca por nome; filtros (posição, condição, só livres, idade, valor) combináveis; ordenação por nome, valor (↑↓), idade (↑↓) e posição; só livres + clube impedido ou com `message`                                                                              | 2     |
| F03                         | Excluir nos livres, com confirmação; o jogador sai da lista                                                                                                                                                                                                      | 1     |
| F04                         | Cadastro com os 7 indicadores (valor base e overall aparecem depois de salvar), edição e alteração de condição                                                                                                                                                   | 2     |
| **Etapa 2: Clubes**         |                                                                                                                                                                                                                                                                  | **2** |
| F05                         | Listagem (nome, sigla, caixa, valor do elenco, patrimônio) por nome, na ordem da API; formulário de criação                                                                                                                                                      | 2     |
| **Etapa 3: Compra e venda** |                                                                                                                                                                                                                                                                  | **8** |
| F06                         | Compra a partir dos livres: clube, fim da fidelidade em Brasília enviado com `-03:00`, confirmação; resultado com valor, taxa, custo total, fim da fidelidade e novo caixa; depois a tabela mostra o clube, o filtro por clube encontra e "só livres" não mostra | 3     |
| F07                         | Página do clube: caixa, valor do elenco, patrimônio, elenco na ordem da API com valor de mercado e contrato (fim, dias restantes, % multa); lista de clubes por patrimônio (maior → menor)                                                                       | 2     |
| F08                         | Venda com confirmação; resultado: valor de compra, valor de venda, dias restantes, multa, lucro, comissão, crédito e novo caixa                                                                                                                                  | 3     |
| **Etapa 4: Histórico**      |                                                                                                                                                                                                                                                                  | **6** |
| F09                         | Histórico: data/hora, tipo, jogador, valor, taxa/comissão, multa, lucro, variação com sinal; filtros por tipos e jogador; `message` quando a API recusa                                                                                                          | 3     |
| F10                         | Totais do `summary` abaixo da tabela, respeitando o filtro                                                                                                                                                                                                       | 2     |
| F11                         | Exportar CSV com os filtros aplicados                                                                                                                                                                                                                            | 1     |
| **Etapa 5: Liga**           |                                                                                                                                                                                                                                                                  | **7** |
| F12                         | Escalação: formação e um titular por vaga (sem `DOENTE`); mostra a escalação devolvida; aviso de escalação incompleta                                                                                                                                            | 2     |
| F13                         | Partida: mandante e visitante; placar e 6 duelos da API, sem recalcular; `message` de clube sem escalação                                                                                                                                                        | 3     |
| F14                         | Histórico de partidas (data/hora, mandante, placar, visitante, filtro por clube, duelos ao abrir) e classificação na ordem da API                                                                                                                                | 2     |

Um item vale zero se a tela não envia o pedido previsto, não mostra a resposta ou não trata o código recebido.
Erro de valor vindo da API não tira ponto da tela. Tempo máximo por ação: 10 s.

## F2. Qualidade do código (10 pontos)

| Critério              | Atende (2)                                                                                                   |
| --------------------- | ------------------------------------------------------------------------------------------------------------ |
| Componentes           | Tabela, formulário (campo + rótulo + erro) e mensagens **não duplicados** entre telas                        |
| Acesso à API          | Todas as chamadas numa camada própria; **nenhum** componente com `fetch`/Axios                               |
| Atualização e regras  | Após as 9 ações, dados atualizados pela API sem recarregar; **nenhuma regra recalculada**                    |
| Formulários           | Os 7 formulários validam obrigatórios e limites antes de enviar, mensagem junto ao campo, datas com `-03:00` |
| Código morto e padrão | Nenhum arquivo com código comentado, sem uso ou nomes fora do padrão                                         |

## F3. Usabilidade e feedback (6 pontos)

| Critério          | Atende (2)                                                                                   |
| ----------------- | -------------------------------------------------------------------------------------------- |
| Mensagens         | As 9 ações mostram sucesso e o `message` da API nas recusas; clique duplo envia um só pedido |
| Estados das telas | Carregamento, vazio e erro nas 6 telas de lista                                              |
| Apresentação      | Moedas com 2 casas e vírgula, datas em Brasília nas 6 telas conferidas                       |

## F4. README e Git (2 pontos)

- README do frontend (1): itens 1, 3, 4 e 5 dos requisitos do README (pré-requisitos com versão; instalar e
  executar no Ubuntu 24.04 com URL do frontend e lockfile versionado; variáveis de ambiente com
  `.env.example`; estrutura de pastas e bibliotecas com motivo).
- Git (1): **≥ 10 commits que alteram `frontend/`**, push em **≥ 3 dias diferentes**, no máximo metade
  genéricas.

## F5. Diferenciais (até 2 pontos)

- **Responsividade (1):** 375 px sem rolagem horizontal da página em jogadores e página do clube (com
  histórico); tabelas rolam no próprio quadro; todas as ações acessíveis.
- **Testes (1):** `npm test` termina sozinho em ≤ 10 min, sem falhas, com ≥ 1 teste de componente ou E2E.
- **Lint e formatação (0,5):** `npm run lint` e `npm run format:check` sem erros (o de formatação só verifica).
