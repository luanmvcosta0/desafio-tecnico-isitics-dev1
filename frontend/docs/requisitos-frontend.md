# Requisitos do frontend — LigaHub

Fonte: seção "🖥️ Frontend" do enunciado e barema F1–F5 (https://github.com/isi-tics/desafio-isi-dev-1).
A banca usa a interface no **Google Chrome, janela 1366 × 768**, seguindo um roteiro fixo, gravando a tela e o
tráfego de rede (HAR). Cada item do roteiro vale tudo ou nada.

## 1. Princípios que valem para todas as telas

- **A tela não recalcula nada que a API devolve**: valor base, valor de mercado, overall, taxa, multa,
  comissão, lucro, crédito, valor do elenco, patrimônio, dias restantes, % de multa, totais do `summary`,
  placar, duelos e classificação vêm **sempre da resposta da API**. Única exceção permitida: somar valor +
  taxa para exibir o custo total da compra.
- **Ordem das listas = ordem devolvida pela API** (elenco, clubes, histórico, partidas, classificação). Não
  reordenar no cliente.
- **Depois de cada ação**, mostrar os dados atualizados pela API **sem recarregar a página** (refazer a
  consulta ou usar a resposta da ação).
- **Toda ação** mostra mensagem de sucesso ou o **`message` devolvido pela API** (mensagem genérica no lugar
  do `message` conta como erro).
- **Durante o envio**, o botão da ação fica desabilitado e a mesma ação **não é enviada duas vezes** (clique
  duplo em "Jogar partida" não pode criar duas partidas — a banca confere no HAR).
- **Recusas de regra de negócio (409) e de inexistência (404) não são impedidas na tela**: envia-se o pedido e
  mostra-se o `message` (nome/sigla repetidos, limite de 20 clubes, compra acima de 50% do caixa, elenco
  cheio, recompra, clube sem escalação completa, dados que mudaram depois de a tela abrir). Exceções que a tela
  **pode** impedir mostrando o motivo (ou enviar e mostrar o `message`):
  - "só livres" junto com o filtro por clube;
  - condição de `DOENTE` direto para `APTO`;
  - mudança de posição de jogador com clube;
  - idade menor que a atual;
  - as escolhas da escalação (só elenco, sem `DOENTE`, vagas da formação).
- **Formulários validam antes de enviar** os campos obrigatórios e os limites de um campo só, com a
  **mensagem junto ao campo**. Regras que cruzam campos, formato de nomes/sigla e o que depende do banco podem
  ficar só na API.
- **Toda tela de lista** mostra os estados de **carregamento**, **lista vazia** e **erro**.
- **Moedas** com 2 casas e vírgula decimal (`1.234,50`); **datas** no horário de Brasília; datas digitadas
  também são interpretadas no horário de Brasília e enviadas como `AAAA-MM-DDTHH:MM-03:00`.

## 2. Telas e funcionalidades obrigatórias

### Jogadores (`/players`)

- Tabela com **nome, posição, overall, agilidade, idade, condição, valor base, valor de mercado e clube**
  (nome ou sigla; **"Livre"** quando `club_id` é nulo).
- **Paginação de 10 por página** (`limit=10`), navegação entre páginas e **total de jogadores encontrados**
  (`meta.total_items`).
- **Busca por nome** (`search`) e filtros combináveis: posição, condição, só livres (`free=true`), clube
  (`club_id`), faixa de idade (`min_age`/`max_age`) e faixa de valor de mercado (`min_value`/`max_value`).
- Ordenação: nome; valor de mercado (↑ e ↓); idade (↑ e ↓); posição (`sort`).
- "Só livres" + clube: a tela impede a combinação **ou** mostra o `message` (a API responde 400).
- Botão **Excluir** só nos jogadores livres, com **confirmação**; o jogador sai da lista.
- Formulário de **cadastro** com os 4 campos + **sete indicadores** (`pace`, `shooting`, `passing`,
  `dribbling`, `defending`, `physical`, `potential`); valor base e overall aparecem **depois de salvar**.
- Formulário de **edição** (nome, posição, agilidade, idade) e **alteração de condição**.
- **Compra** a partir dos jogadores livres: escolher o clube, informar o **fim da fidelidade** (data e hora,
  Brasília), confirmar. Resultado: **valor, taxa, custo total (valor + taxa), fim da fidelidade e novo
  caixa**. Depois, a tabela mostra o clube do jogador, o filtro por clube o encontra e "só livres" não o
  mostra.

### Clubes (`/clubs`)

- Listagem com **nome, sigla, caixa, valor do elenco e patrimônio**, ordenável por nome e por patrimônio (pelo
  menos do maior para o menor), **na ordem devolvida por `GET /clubs`**.
- Formulário de **criação** (nome e sigla).

### Página do clube (`/clubs/:clubId`)

- **Caixa, valor do elenco e patrimônio**.
- **Elenco** na ordem da API, com valor de mercado de cada jogador e as colunas do contrato: **fim da
  fidelidade, dias restantes e % de multa**.
- Botão **Vender** em cada jogador do elenco, com **confirmação**. Resultado: **valor de compra, valor de venda,
  dias restantes, multa, lucro, comissão, crédito e novo caixa**.
- **Histórico de transações**: tabela com data e hora (Brasília), tipo (compra/venda), jogador, valor, taxa ou
  comissão, multa, lucro e **variação do caixa com sinal** ("+" entrada, "-" saída).
  - Filtro por **um ou mais tipos** e por **jogador** (escolhido entre os jogadores que aparecem no histórico
    do clube); se a API recusar (ex.: jogador excluído → 404), mostrar o `message`.
  - **Abaixo da tabela**, os totais do `summary` respeitando o filtro: total pago em compras, total recebido em
    vendas, total de taxas e comissões, total de multas, lucro total.
  - Botão **Exportar CSV**, que baixa o arquivo da API **com os filtros aplicados**.
- Aba **Escalação**: escolher a formação (`3-1`, `2-2`, `2-1-1`, `1-2-1`) e **um titular para cada vaga**
  (1 goleiro + jogadores de linha de cada setor) entre os jogadores do elenco, **sem `DOENTE`**, e salvar.
  Depois de salvar, mostrar a escalação devolvida pela API. Com a escalação **incompleta** (`complete: false`),
  avisar que o clube não pode jogar.

### Partidas (`/matches`)

- Escolher **mandante e visitante** e jogar. Resultado: **placar e os 6 duelos** devolvidos pela API
  (atacante, defensor, atributos, valores e gol), **sem recalcular**. Clube sem escalação completa → `message`.
- **Histórico de partidas**: data e hora, mandante, placar e visitante, da mais recente para a mais antiga, com
  **filtro por clube**; ao abrir uma partida (`/matches/:matchId`), mostrar os duelos.

### Classificação (`/standings`)

- Tabela com **posição, clube, jogos, vitórias, empates, derrotas, gols marcados, gols sofridos, saldo e
  pontos**, na ordem devolvida por `GET /standings`.

### Opcional (mockup)

Tela inicial, contadores do menu e últimas partidas na classificação são opcionais. A fidelidade visual ao
mockup não conta ponto; ele orienta fluxo e disposição. Figma v1.0:
https://www.figma.com/design/oevjrS0eaqL3VHSvKYB8GS/Passe

## 3. Os 7 formulários e o que validam antes de enviar (F2)

| Formulário            | Obrigatórios                                  | Limites de um campo só                                               |
| --------------------- | --------------------------------------------- | -------------------------------------------------------------------- |
| Cadastro de jogador   | todos os 11 campos                            | nome 3–100; agilidade 1–100; idade 16–45; atributos e potencial 1–99 |
| Edição de jogador     | nome, posição, agilidade, idade               | nome 3–100; agilidade 1–100; idade 16–45                             |
| Alteração de condição | condição                                      | —                                                                    |
| Criação de clube      | nome, sigla                                   | nome 3–60; sigla 3 letras                                            |
| Compra                | clube, fim da fidelidade                      | data enviada com `-03:00`                                            |
| Escalação             | formação e um titular em cada uma das 5 vagas | —                                                                    |
| Nova partida          | mandante e visitante                          | —                                                                    |

## 4. As 9 ações (mensagem de sucesso/erro, sem duplo envio, dados atualizados)

Cadastrar jogador; editar jogador; alterar condição; excluir jogador; criar clube; comprar; vender; escalar;
jogar partida.

## 5. As 6 telas de lista (carregamento, vazio, erro)

Jogadores; clubes; elenco do clube; histórico de transações; histórico de partidas; classificação.
A banca vê o "vazio" com o banco recém-recriado, o "carregamento" com a rede desacelerada e o "erro" com o
backend parado.

## 6. Apresentação (moedas e datas) — 6 telas conferidas

Lista de jogadores; clubes; página do clube (com histórico); resultado da compra; resultado da venda;
histórico de partidas.
