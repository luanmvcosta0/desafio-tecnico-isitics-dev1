# Regras de negócio — LigaHub (backend)

Fonte: enunciado do estudo de caso SENAI/PE – CÓD. 237 (https://github.com/isi-tics/desafio-isi-dev-1).
Este arquivo é a transcrição fiel das regras que o backend precisa cumprir. **As tabelas de exemplo foram
conferidas pela banca e devem virar casos de teste.** Em caso de dúvida, o enunciado original prevalece.

---

## 1. Premissas

- **Sem login:** nenhum endpoint exige autenticação. Cada operação diz o clube envolvido (`club_id` na compra,
  clube atual do jogador na venda, clube da URL em `/clubs/{id}`).
- **Moeda:** "moedas", sempre com 2 casas decimais. Tipo em Java: `BigDecimal`.
- **Arredondamento:** uma única vez, **no resultado final** de cada cálculo em moedas, para 2 casas, com meio
  **para longe do zero** (`RoundingMode.HALF_UP` do Java): 14,775 → 14,78; 0,385 → 0,39. Valor que é resultado
  de outra regra (valor base, valor de mercado) entra no cálculo seguinte **já arredondado**.
- **Ordem alfabética** (sempre que ordenar por nome): converter para minúsculas, remover acentos (NFD e retirar
  a categoria Unicode Mn, o que inclui a cedilha), comparar caractere a caractere pela ordem Unicode
  (code point). Espaço vem antes do hífen, hífen antes das letras ("Épsilon" entre "Delta" e "Gama";
  "Ana Lima" antes de "Ana-Maria"). Nomes iguais nessa comparação ("Águias FC" e "Aguias FC") empatam, e o
  empate é desfeito pelo **id crescente**.
- **Números nas respostas:** números JSON; `40`, `40.0` e `40.00` são o mesmo valor.
- **Detalhes não definidos:** decidir e registrar na seção "Decisões e premissas" do README.

## 2. Validação dos dados (V1–V9)

Toda regra é validada no backend.

- **V1:** corpo é um **objeto** JSON. JSON inválido, array ou valor simples (`[]`, `"x"`, `10`) → 400. Nos
  pedidos sem corpo (consultas, exclusão, venda), corpo enviado é ignorado.
- **V2:** cada campo chega no tipo indicado. Número é número JSON: `15` válido, `"15"` → 400. Campo inteiro
  não aceita parte fracionária: `50.5` → 400; `50.0` vale 50 e é aceito.
- **V3:** nenhum valor em moedas é enviado no corpo; todos são calculados pela API. Campo de moeda enviado
  (`base_value`, `cash`…) é ignorado (V7). Únicas moedas enviadas: filtros `min_value`/`max_value`.
- **V4:** nos campos de nome e no `search`: remover espaços do início e do fim e colapsar espaços repetidos em
  um só **antes** de validar tamanho e gravar. Caracteres de controle (tab, quebra de linha…) → 400. Texto que
  fica vazio conta como ausente; no `search`, → 400. Ex.: `"  Meia   Zeta "` grava `"Meia Zeta"`.
  - **caractere** = ponto de código Unicode; **espaço** = U+0020; **letra** = categoria Unicode L;
    **dígito** = 0–9; **palavra** = sequência sem espaços com pelo menos uma letra; **ignorar acentos** = NFD e
    retirar marcas Mn (ç ≡ c).
- **V5:** valores de lista são *case-sensitive*, exatamente como escritos (`ATA`, `MAL_CONDICIONADO`,
  `value_desc`, `true`). `"ata"` e `"TRUE"` → 400, nunca convertidos.
- **V6:** campo obrigatório ausente ou `null` → 400.
- **V7:** campos calculados/controlados pelo sistema (`id`, `cash`, `market_value`; no cadastro e na edição de
  jogador também `club_id` e `condition`; no `PUT` também os indicadores; em qualquer pedido `overall` e
  `base_value`) e campos que não pertencem ao endpoint (inclusive inexistentes ou de outros endpoints, como
  `name` no `PATCH` de condição, ou `loyaltyUntil` em camelCase) são **ignorados, sem erro**.
- **V8:** recurso da URL inexistente → 404; `club_id` inexistente na compra → 404.
  - Id na URL que não é inteiro positivo escrito só com dígitos, sem zeros à esquerda (`abc`, `0`, `-1`,
    `1.5`, `01`), ou maior que 2.147.483.647 → **404** (conta como inexistente).
  - Ids no corpo (`club_id`, `player_ids`, `home_club_id`, `away_club_id`) nunca virão acima de
    2.147.483.647.
  - Jogador excluído (RN09) conta como inexistente em todo endpoint que recebe o id dele (URL, corpo ou
    filtro). Transações e partidas antigas continuam mostrando o jogador como foi gravado.
- **V9:** dado/formato/parâmetro inválido → `400`; recurso inexistente → `404`; regra de negócio → `409`. A
  correção nunca combina dois erros no mesmo pedido. **Ordem de verificação: 400 → 404 → 409.**

## 3. Limites de cada campo

Limites **inclusivos**. Fora do limite → **400**, nunca ajustado. Os marcados com 409 dependem do estado atual.

| Campo | Onde | Tipo | Aceito |
| --- | --- | --- | --- |
| `name` | Jogador | texto | 3 a 100 caracteres (após V4), pelo menos **duas palavras** e **nenhum dígito** |
| `position` | Jogador | lista | `GOL`, `ZAG`, `LAT`, `MEI`, `ATA`; no `PUT`, jogador com clube não muda de posição (**409**) |
| `agility` | Jogador | inteiro | 1 a 100; com 35 anos ou mais, no máximo 85 |
| `age` | Jogador | inteiro | 16 a 45; no `PUT`, não pode ser menor que a idade atual (**409**) |
| `pace`, `shooting`, `passing`, `dribbling`, `defending`, `physical` | Jogador (só cadastro) | inteiro | 1 a 99 cada; soma dos seis ≤ 480 |
| `potential` | Jogador (só cadastro) | inteiro | 1 a 99 **e** ≥ maior dos seis atributos; no cadastro com 33 anos ou mais, **igual** ao maior dos seis (um `PUT` que leve a idade a 33+ é aceito, sem mudar `potential`) |
| `condition` | Alteração de condição | lista | `APTO`, `MAL_CONDICIONADO`, `DOENTE`; de `DOENTE` direto para `APTO` → **409**; enviar a condição atual → 200 sem mudança |
| `name` | Clube | texto | 3 a 60 caracteres (após V4), 2 a 5 palavras; só letras (com/sem acento), espaços, hífen e ponto |
| `acronym` | Clube | texto | exatamente 3 letras A–Z maiúsculas sem acento, que aparecem no nome do clube **nessa ordem** (sem diferenciar maiúsculas/acentos), a primeira delas é a **primeira letra do nome**; as três não podem ser todas iguais |
| `club_id` | Compra | inteiro | positivo; 0 ou negativo → 400; inexistente → 404 |
| `loyalty_until` | Compra | data/hora RN01 | depois do momento da compra e até 1.095 dias depois dele (fora → 400) |
| `formation` | Escalação | lista | `3-1`, `2-2`, `2-1-1`, `1-2-1` |
| `player_ids` | Escalação | lista de inteiros | exatamente 5 ids, distintos, positivos (400); inexistente → 404; fora do elenco, `DOENTE` ou posições que não fecham a formação → 409 |
| `home_club_id`, `away_club_id` | Partida | inteiro | positivo; 0/negativo → 400; iguais → 400; inexistente → 404 |

## 4. Datas e horas

- **RN01:** toda data/hora **enviada** usa horário de Brasília no formato estrito `AAAA-MM-DDTHH:MM-03:00`
  (ex.: `2027-03-02T10:15-03:00`). Outro formato (inclusive sem `-03:00`, ou com segundos) → 400. Data/hora
  inexistente no formato certo (`2027-02-30T10:15-03:00`, `2027-03-02T24:00-03:00`) → 400, nunca ajustada
  (usar `ResolverStyle.STRICT` com `uuuu`).
- As **respostas** usam `AAAA-MM-DDTHH:MM:SS-03:00` (ex.: `2026-11-02T10:15:00-03:00`).
- Nas regras de data (fim da fidelidade, dias restantes) vale o **instante exato do servidor** no momento da
  operação. Brasília é sempre `-03:00` (sem horário de verão). Injetar um `Clock` para tornar testável.

## 5. Convenções da API

- Base URL **`/api/v1`**. Sem barra final (`/api/v1/players`, nunca `/api/v1/players/`); sem redirecionamentos.
- Corpo, respostas e query params em **snake_case** (`base_value`, `club_id`, `loyalty_until`, `min_value`).
- Métodos, caminhos, nomes de campos, cabeçalhos e códigos de sucesso são **obrigatórios** como no enunciado.
  Endpoints e campos adicionais são permitidos.
- Respostas JSON (inclusive erros) com `Content-Type: application/json`; exportação com `text/csv`.
- **CORS** habilitado só para a origem do frontend, configurável (variável de ambiente). `*` não é aceito.
- `POST /players`, `POST /clubs` e `POST /matches` → `201` com cabeçalho **`Location`** (ex.:
  `/api/v1/players/2`).
- `GET /clubs`, `GET /matches`, `GET /standings` → array JSON. `GET /players` → `{data, meta}`.
  `GET /clubs/{id}/transactions` → `{data, summary}`. Sem resultados: `200` com lista vazia.
- Em **toda recusa**: nada é gravado; corpo é objeto JSON com `message` (texto não vazio), **sem stack trace**.
- Única exclusão: lógica, de jogadores (RN09). Não há endpoints para editar/excluir clubes, contratos,
  transações e partidas. Caixa e status do contrato mudam só por compra e venda.
- Caminho fora do enunciado → 404. Método não previsto em caminho existente → 404 ou 405 (não conferido).
- 500 em falha inesperada, com a operação desfeita por inteiro (e ainda `{message}` sem stack trace).

---

## 6. Módulo 1 — Jogadores

### Campos

- `name` (obrigatório; homônimos permitidos), `position`, `agility`, `age` (obrigatórios).
- `pace`, `shooting`, `passing`, `dribbling`, `defending`, `physical`, `potential`: obrigatórios **só no
  cadastro**; nunca mudam depois.
- `base_value`: calculado e gravado no cadastro (RN06); não muda.
- `overall`: calculado a cada consulta (RN06); **nunca gravado**.
- `market_value`: calculado a cada consulta (RN05); **nunca gravado**.
- `condition`: `APTO` no cadastro; muda só por `PATCH /players/{id}/condition`.
- `club_id`: nulo no cadastro; muda só por compra e venda.
- O `PUT` exige `name`, `position`, `agility` e `age`, mesmo que só um mude.

### RN02 – Desconto por condição física
APTO 0%; MAL_CONDICIONADO 15%; DOENTE 30%.

### RN03 – Efeito da agilidade
Altera **somente o desconto por condição** e **somente quando o jogador não está APTO**:
- agilidade 80–100: desconto cai pela metade;
- agilidade 1–30: desconto aumenta 10 pontos percentuais;
- agilidade 31–79: sem efeito.

### RN04 – Ajuste por idade (idade atual)
Até 21 anos: **+10%**; 22–31: 0%; 32 ou mais: **−10%**. Vale para qualquer condição, inclusive APTO.

### RN05 – Valor de mercado (a cada consulta, nesta ordem)
1. desconto por condição = RN02 ajustado por RN03;
2. percentual total = ajuste por idade (RN04) − desconto por condição;
3. teto do desconto: se percentual total < **−40%**, usa −40%;
4. valor de mercado = valor base × (100 + percentual total) ÷ 100, arredondado (2 casas, HALF_UP);
5. piso: se < **1,00**, vale 1,00.

| Valor base | Condição | Agilidade | Idade | Desconto (RN02+RN03) | Idade (RN04) | Total | Valor de mercado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 82,00 | APTO | 20 | 25 | 0% | 0% | 0% | 82,00 |
| 82,00 | APTO | 50 | 19 | 0% | +10% | +10% | 90,20 |
| 82,00 | MAL_CONDICIONADO | 50 | 25 | 15% | 0% | −15% | 69,70 |
| 82,00 | MAL_CONDICIONADO | 80 | 21 | 7,5% | +10% | +2,5% | 84,05 |
| 82,00 | MAL_CONDICIONADO | 30 | 33 | 25% | −10% | −35% | 53,30 |
| 82,00 | DOENTE | 90 | 25 | 15% | 0% | −15% | 69,70 |
| 82,00 | DOENTE | 30 | 25 | 40% | 0% | −40% (no teto) | 49,20 |
| 82,00 | DOENTE | 10 | 40 | 40% | −10% | −50% → −40% | 49,20 |
| 9,50 | MAL_CONDICIONADO | 85 | 25 | 7,5% | 0% | −7,5% | 8,79 (8,7875) |
| 1,00 | MAL_CONDICIONADO | 50 | 35 | 15% | −10% | −25% | 1,00 (0,75 → piso) |

### RN06 – Indicadores, overall e valor base
1. Os seis atributos e o `potential` são informados no cadastro e não mudam. `potential` ≥ maior atributo.
2. **Overall** (a cada consulta, nunca gravado, porque a posição pode mudar no `PUT`):
   1. multiplicar cada atributo pelo peso da posição (tabela abaixo; pesos somam 100);
   2. somar os seis produtos e dividir por 100;
   3. arredondar para o **inteiro** mais próximo, meio para cima (72,5 → 73; 72,49 → 72).
3. **Valor base** (uma única vez, no cadastro, com overall pela posição/atributos do cadastro e idade do
   cadastro; gravado; não muda nem quando o `PUT` muda posição ou idade):
   1. parte do overall, progressiva: **0,50** por ponto de 41 a 60, **+2,00** por ponto de 61 a 75,
      **+6,00** por ponto de 76 a 99; overall ≤ 40 não soma nada;
   2. jovem promessa: idade no cadastro ≤ 21 → parte do overall × (100 + potencial − overall) ÷ 100;
   3. arredondar uma única vez (2 casas); se < 1,00, vale 1,00.
4. Depois do cadastro, indicadores e overall **não alteram o valor de mercado**.

Pesos por posição (%):

| Posição | `pace` | `shooting` | `passing` | `dribbling` | `defending` | `physical` |
| --- | --- | --- | --- | --- | --- | --- |
| GOL | 10 | 0 | 20 | 0 | 40 | 30 |
| ZAG | 10 | 0 | 15 | 0 | 45 | 30 |
| LAT | 25 | 0 | 20 | 15 | 25 | 15 |
| MEI | 10 | 15 | 35 | 25 | 5 | 10 |
| ATA | 20 | 40 | 10 | 20 | 0 | 10 |

Exemplos de overall:

| Posição | pace | shooting | passing | dribbling | defending | physical | Soma | ÷100 | Overall |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| MEI | 68 | 65 | 80 | 78 | 52 | 60 | 7.265 | 72,65 | 73 |
| ATA | 78 | 75 | 73 | 82 | 48 | 68 | 7.610 | 76,10 | 76 |
| LAT | 72 | 70 | 78 | 76 | 70 | 66 | 7.240 | 72,40 | 72 |
| ZAG | 50 | 90 | 60 | 40 | 80 | 75 | 7.250 | 72,50 | 73 |
| GOL | 55 | 30 | 65 | 40 | 72 | 68 | 6.770 | 67,70 | 68 |

Exemplos de valor base:

| Overall | Potencial | Idade no cadastro | Parte do overall | Jovem promessa | Valor base |
| --- | --- | --- | --- | --- | --- |
| 75 | 86 | 25 | 20×0,50 + 15×2,00 = 40,00 | não | 40,00 |
| 60 | 70 | 30 | 20×0,50 = 10,00 | não | 10,00 |
| 61 | 70 | 30 | 10,00 + 1×2,00 = 12,00 | não | 12,00 |
| 76 | 80 | 22 | 10,00 + 30,00 + 1×6,00 = 46,00 | não | 46,00 |
| 85 | 92 | 27 | 10,00 + 30,00 + 10×6,00 = 100,00 | não | 100,00 |
| 70 | 84 | 21 | 10,00 + 10×2,00 = 30,00 | 30,00 × 114 ÷ 100 | 34,20 |
| 70 | 84 | 22 | 30,00 | não | 30,00 |
| 45 | 90 | 18 | 5×0,50 = 2,50 | 2,50 × 145 ÷ 100 = 3,625 | 3,63 |
| 40 | 66 | 36 | 0,00 | não | 1,00 (piso) |

### RN07 – Listagem paginada, busca e filtros
1. Filtros e ordenação por valor usam o **valor de mercado** (nunca o valor base). Em qualquer ordenação, o
   empate é desfeito pelo **nome** (ordem alfabética das Premissas) e depois pelo **id** crescente.
2. `sort=position` ordena **GOL, ZAG, LAT, MEI, ATA** (não alfabética). `age_asc`/`age_desc` pela idade.
3. `search` procura o texto **dentro do nome**, ignorando maiúsculas/minúsculas e acentos (`joao` acha
   "João Silva"; `LUA` acha "Meia Lua").
4. Paginação aplicada **depois** de buscar, filtrar e ordenar.
5. `meta.total_items` conta todos os jogadores que atendem aos filtros; `meta.total_pages` =
   ceil(total_items ÷ limit) (0 quando não há itens).
6. Página além da última → `200` com `data: []` e `meta` correto.

| Atendem | `limit` | `page` | Itens em `data` | `total_items` | `total_pages` |
| --- | --- | --- | --- | --- | --- |
| 23 | 10 | 1 | 10 (1º–10º) | 23 | 3 |
| 23 | 10 | 3 | 3 (21º–23º) | 23 | 3 |
| 23 | 10 | 4 | 0 | 23 | 3 |
| 23 | 50 | 1 | 23 | 23 | 1 |
| 0 | 10 | 1 | 0 | 0 | 0 |

### RN08 – Edição do jogador
`PUT /players/{id}` muda só nome, posição, agilidade e idade. Indicadores, potencial e valor base continuam os
do cadastro (V7). Jogador com clube não muda de posição (409); idade não pode diminuir (409).

### RN09 – Exclusão lógica
1. `DELETE /players/{id}` grava o momento em `excluido_em` e responde **`204`** sem corpo. Nada é apagado.
2. Só jogador **livre** pode ser excluído; jogador com clube → **409**.
3. Jogador excluído conta como **inexistente (404)** em todos os endpoints (consulta, edição, condição,
   compra, nova exclusão, filtro `player_id` do histórico) e não aparece em `GET /players`.
4. Transações, contratos e duelos antigos continuam como gravados, com o nome do momento.

---

## 7. Módulo 2 — Clubes

### RN10 – Criação, nome único e sigla
- O clube nasce com **300,00 em caixa**, sem jogadores e sem escalação. O caixa só muda por compra e venda.
- Nome único **após V4**, **ignorando maiúsculas/minúsculas mas considerando acentos**: com "Águias FC"
  cadastrado, `"  águias   fc "` → 409, e "Aguias FC" é aceito. A unicidade **também no banco**, com a mesma
  regra (índice único funcional em `lower(nome)`).
- Sigla única: repetida → 409.
- Máximo de **20 clubes**: o 21º → 409.

### RN11 – Elenco e valores do clube
- Elenco ordenado por posição **GOL, ZAG, LAT, MEI, ATA**, depois nome (ordem alfabética), depois id.
- Cada jogador do elenco mostra o contrato: `purchase_value`, `loyalty_until`, `remaining_days` e
  `penalty_rate` (percentual de multa se fosse vendido agora, RN15).
- `squad_value` = soma dos valores de mercado do elenco; `net_worth` = caixa + valor do elenco.
- Valor do elenco, patrimônio, dias restantes e percentual de multa: calculados a cada consulta, nunca gravados.
- `GET /clubs` ordena por nome (padrão) ou patrimônio (`sort`); empate → nome, depois id crescente.

---

## 8. Módulo 3 — Mercado

### RN12 – Compra e contrato
- O jogador precisa estar **livre**. O preço é o **valor de mercado no momento** (nunca o valor base).
- Um clube **não pode comprar um jogador que ele mesmo já vendeu** (outros podem) → 409.
- Numa **única transação do banco**: desconta o custo do caixa, coloca o jogador no elenco, cria o contrato
  `ATIVO` (valor de compra, fim da fidelidade) e grava a transação `COMPRA`.

### RN13 – Taxa de compra e limite de 50% do caixa
- taxa = 5% do valor de mercado, arredondada, cobrada **somente quando o valor de mercado > 30,00**;
- custo total = valor de mercado + taxa;
- custo total **≤ 50% do caixa antes da compra** (pode ser igual). O limite é exatamente a metade, **sem
  arredondamento** (caixa 261,15 → limite 130,575; custo 130,58 é recusado). O caixa nunca fica negativo.

| Caixa | Valor de mercado | Taxa | Custo | Limite | Resultado |
| --- | --- | --- | --- | --- | --- |
| 300,00 | 28,00 | 0,00 | 28,00 | 150,00 | Aceita |
| 300,00 | 30,00 | 0,00 | 30,00 | 150,00 | Aceita |
| 300,00 | 30,01 | 1,50 (1,5005) | 31,51 | 150,00 | Aceita |
| 100,00 | 47,62 | 2,38 | 50,00 | 50,00 | Aceita (igual) |
| 100,00 | 48,00 | 2,40 | 50,40 | 50,00 | 409 |

### RN14 – Limite de elenco
No máximo **12 jogadores** por clube → 409.

### RN15 – Multa por fidelidade
1. dias restantes = (fim da fidelidade − momento), em dias **com fração**, **arredondado para cima**; se o fim
   já passou ou é o próprio momento, 0. Ex.: 116 dias, 17 h e 35 min → 117.
2. percentual: **0 dia → 0%**; **1–30 → 5%**; **31–180 → 10%**; **181+ → 20%**.
3. multa = valor de mercado na venda × percentual, arredondada, **limitada a 10,00**.
4. com multa → contrato `RESCINDIDO`; sem multa (fidelidade cumprida) → `ENCERRADO`.

| Fim da fidelidade, a partir da venda | Dias restantes | Multa |
| --- | --- | --- |
| Já passou | 0 | 0% (ENCERRADO) |
| 10 dias e 2 horas | 11 | 5% |
| Exatamente 30 dias | 30 | 5% |
| 30 dias e 1 minuto | 31 | 10% |
| Exatamente 180 dias | 180 | 10% |
| 180 dias e 1 minuto | 181 | 20% |
| 200 dias | 200 | 20% |

### RN16 – Venda, comissão e crédito
1. valor de compra = valor gravado no contrato `ATIVO` (sem a taxa);
2. lucro = valor de mercado na venda − valor de compra;
3. comissão **progressiva**, só se lucro > 0 (senão 0,00): **10%** sobre a parte até 10,00 + **20%** sobre a
   parte acima de 10,00, somadas e **arredondadas uma única vez** no final;
4. crédito = valor de mercado − multa − comissão;
5. o jogador volta a ficar livre, sai da escalação (RN19) e a transação `VENDA` é gravada na mesma transação;
6. jogador livre não pode ser vendido → 409.

| Lucro | Cálculo | Comissão |
| --- | --- | --- |
| 3,00 | 10% × 3,00 | 0,30 |
| 3,85 | 10% × 3,85 = 0,385 | 0,39 |
| 10,00 | 10% × 10,00 | 1,00 |
| 10,01 | 1,00 + 20% × 0,01 = 1,002 | 1,00 |
| 15,00 | 1,00 + 20% × 5,00 | 2,00 |
| −7,50 | sem lucro | 0,00 |

| Compra | Venda | Multa | Lucro | Comissão | Crédito |
| --- | --- | --- | --- | --- | --- |
| 37,00 | 40,00 | 10% = 4,00 | 3,00 | 0,30 | 35,70 |
| 50,00 | 42,50 | 20% = 8,50 | −7,50 | 0,00 | 34,00 |
| 26,15 | 30,00 | 5% = 1,50 | 3,85 | 0,39 | 28,11 |
| 25,00 | 25,00 | 0% (ENCERRADO) | 0,00 | 0,00 | 25,00 |
| 60,00 | 75,00 | 20% = 15,00 → 10,00 | 15,00 | 2,00 | 63,00 |

### RN17 – Histórico de transações
Campos gravados:

| Campo | Na compra | Na venda |
| --- | --- | --- |
| `id`, `club_id`, `player_id` | ids | ids |
| `player_name` | nome no momento | nome no momento |
| `type` | `COMPRA` | `VENDA` |
| `value` | valor de mercado no momento | valor de mercado no momento |
| `fee` | taxa (RN13) | comissão (RN16) |
| `penalty` | 0,00 | multa (RN15) |
| `profit` | `null` | lucro (pode ser negativo) |
| `cash_change` | −(`value` + `fee`) | `value` − `penalty` − `fee` |
| `created_at` | data/hora da operação | idem |

- Toda compra/venda grava uma transação na mesma operação; operação recusada não grava.
- A transação guarda valores e nome **do momento**; nunca é recalculada nem editada.
- Invariante: **300,00 + Σ `cash_change` = caixa atual** (bom teste de integração).
- Ordem: mais recente primeiro; empate de data/hora → id decrescente.
- Filtros opcionais `type` e `player_id`; jogador sem transações no clube → `200` com `data` vazio e
  `summary` zerado.
- `summary` (respeita os filtros, 2 casas):

| Campo | Soma |
| --- | --- |
| `total_spent` | `value` + `fee` das `COMPRA` |
| `total_received` | `cash_change` das `VENDA` |
| `total_fees` | `fee` de todas |
| `total_penalties` | `penalty` das `VENDA` |
| `total_profit` | `profit` das `VENDA`, com sinal |

### RN18 – Exportação CSV
`GET /clubs/{id}/transactions/export` aceita os mesmos filtros (mesmas validações) e responde `200`,
`text/csv`:
1. cabeçalho: `data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa`;
2. uma linha por transação, na **mesma ordem** do histórico;
3. separador `;`; `data_hora` no formato das respostas (RN01); valores com **2 casas e vírgula decimal**,
   negativo com `-`; `lucro` vazio quando nulo;
4. sem transações: só o cabeçalho;
5. filtro inválido → 400; clube/jogador inexistente → 404 (**erros continuam em JSON**);
6. `jogador` com `;` ou aspas vai entre aspas duplas, aspas internas duplicadas (`"Zé ""Foguete"" Lima"`);
7. UTF-8 **sem BOM**, cada linha terminada em `\n` (LF), **inclusive a última**. `Content-Disposition`
   opcional.

```text
data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa
2026-11-05T16:40:00-03:00;VENDA;Meia Lua;40,00;0,30;4,00;3,00;35,70
2026-11-02T10:15:00-03:00;COMPRA;Meia Lua;37,00;1,85;0,00;;-38,85
```

---

## 9. Módulo 4 — Liga

### RN19 – Escalação
1. Cada clube tem uma escalação com **5 titulares**: exatamente **1 GOL** e **4 jogadores de linha** numa
   formação. O clube nasce sem escalação.
2. Formação conta os 4 de linha por setor, de trás para frente: **defesa** (ZAG ou LAT), **meio** (MEI),
   **ataque** (ATA).
3. `PUT /clubs/{id}/lineup` recebe `formation` e `player_ids` (5 ids, qualquer ordem) e **substitui** a
   escalação inteira numa única transação.
4. Cada titular precisa ser do elenco do clube e não estar `DOENTE`, e as posições precisam fechar a formação;
   senão → 409 sem gravar nada (formato inválido → 400; clube/jogador inexistente → 404).
   `MAL_CONDICIONADO` pode ser escalado (joga com o efeito da RN20).
5. O titular **sai da escalação** quando é vendido ou passa a `DOENTE`, na mesma transação da venda/alteração.
   A escalação fica incompleta (mesma formação) e o clube não joga até escalar de novo. O jogador não volta
   sozinho.
6. `GET /clubs/{id}/lineup` → `club_id`, `formation`, `complete` (`true` com os 5) e `players` na ordem do
   elenco (RN11), cada um com os campos de `GET /players/{id}`. Sem escalação: `formation: null`,
   `players: []`, `complete: false`. Incompleta: `formation` salva, `players` com os que sobraram,
   `complete: false`.
7. Escalar não muda o caixa.

| `formation` | Defesa (ZAG/LAT) | Meio (MEI) | Ataque (ATA) | Exemplo na ordem do elenco |
| --- | --- | --- | --- | --- |
| `3-1` | 3 | 0 | 1 | GOL, ZAG, LAT, LAT, ATA |
| `2-2` | 2 | 0 | 2 | GOL, ZAG, LAT, ATA, ATA |
| `2-1-1` | 2 | 1 | 1 | GOL, ZAG, LAT, MEI, ATA |
| `1-2-1` | 1 | 2 | 1 | GOL, ZAG, MEI, MEI, ATA |

### RN20 – Partida
`POST /matches` joga na hora e grava tudo numa única transação:
1. **Conferências:** corpo/id inválido ou mandante = visitante → 400; clube inexistente → 404; algum dos dois
   sem escalação completa → 409.
2. **Titulares:** só os 5 de cada clube, com os dados do momento, na ordem do elenco (RN11): goleiro e depois os
   4 de linha numerados do **1º** (mais recuado) ao **4º** (mais avançado). Mesma posição → nome, depois id.
3. **Condição:** `MAL_CONDICIONADO` joga com cada atributo × 0,85, arredondado ao inteiro, meio para cima
   (74 × 0,85 = 62,9 → 63; 50 × 0,85 = 42,5 → 43). `APTO` usa os atributos do cadastro. O efeito vale só na
   partida.
4. **Duelos:** cada time faz 3 ataques. O 1º de linha só defende; o 2º ataca e defende; o 3º e o 4º só atacam.
   Os 6 duelos são sempre disputados.
5. **Valores:** atacante usa o atributo da posição: ATA → `shooting`, MEI → `passing`, ZAG/LAT → `dribbling`.
   Defensor usa sempre `defending` (inclusive o goleiro). Nada mais entra no duelo.
6. **Placar:** gol quando ataque **>** defesa (empate não é gol). Cada time 0–3 gols. `result`: `MANDANTE`,
   `VISITANTE` ou `EMPATE`.
7. **Gravação:** `played_at` (momento do servidor), placar, formações dos dois clubes e os 6 duelos (1–3:
   ataques do mandante; 4–6: do visitante) com nomes, posições, condições, atributos e valores usados. Nada é
   recalculado depois.
8. **Sem outros efeitos:** não muda caixa, escalação nem jogadores. Sem limite de partidas; confronto pode
   repetir.

| Duelo (mandante ataca) | Duelo (visitante ataca) | Atacante | Defensor (adversário) |
| --- | --- | --- | --- |
| 1 | 4 | 4º de linha | Goleiro |
| 2 | 5 | 3º de linha | 1º de linha |
| 3 | 6 | 2º de linha | 2º de linha |

Exemplo completo (caso de teste): Águias FC (mandante) × Delta FC (visitante), ambos `2-1-1`; o LAT do Águias
está `MAL_CONDICIONADO`.

| Ordem | Águias FC | Valores | Delta FC | Valores |
| --- | --- | --- | --- | --- |
| Goleiro | Rui Prado (GOL, 30) | defesa 77 | Ivo Teles (GOL, 40) | defesa 72 |
| 1º | Téo Brandão (ZAG, 32) | defesa 84 | Hugo Vaz (ZAG, 41) | defesa 84 |
| 2º | Davi Lins (LAT, 31, MAL_COND.) | drible 74→63; defesa 66→56 | Lino Matos (LAT, 42) | drible 79; defesa 63 |
| 3º | Lauro Neves (MEI, 33) | passe 82 | Nei Soares (MEI, 43) | passe 70 |
| 4º | Otto Rangel (ATA, 34) | finalização 75 | Ugo Falcão (ATA, 44) | finalização 81 |

| Duelo | Atacante | Ataque | Defensor | Defesa | Gol |
| --- | --- | --- | --- | --- | --- |
| 1 | Otto Rangel | 75 | Ivo Teles | 72 | sim |
| 2 | Lauro Neves | 82 | Hugo Vaz | 84 | não |
| 3 | Davi Lins | 63 | Lino Matos | 63 | não (empate) |
| 4 | Ugo Falcão | 81 | Rui Prado | 77 | sim |
| 5 | Nei Soares | 70 | Téo Brandão | 84 | não |
| 6 | Lino Matos | 79 | Davi Lins | 56 | sim |

Placar: Águias FC 1 × 2 Delta FC → `VISITANTE`.

| Formação | 1º ao 4º de linha | Atacam (4º, 3º, 2º) | Defendem (GOL, 1º, 2º) |
| --- | --- | --- | --- |
| `3-1` | ZAG, LAT, LAT, ATA | ATA (shooting), LAT (dribbling), LAT (dribbling) | GOL, ZAG, LAT |
| `2-2` | ZAG, LAT, ATA, ATA | ATA, ATA (shooting), LAT (dribbling) | GOL, ZAG, LAT |
| `2-1-1` | ZAG, LAT, MEI, ATA | ATA (shooting), MEI (passing), LAT (dribbling) | GOL, ZAG, LAT |
| `1-2-1` | ZAG, MEI, MEI, ATA | ATA (shooting), MEI (passing), MEI (passing) | GOL, ZAG, MEI |

### RN21 – Histórico de partidas e classificação
1. `GET /matches`: mais recente primeiro (mesmo instante → maior id primeiro), com placar e `result`;
   `club_id` opcional filtra partidas em que o clube foi mandante ou visitante.
2. `GET /matches/{id}`: partida com os 6 duelos na ordem da RN20.
3. `GET /standings`: **todos** os clubes (inclusive os que não jogaram, zerados) com `position`, `club_id`,
   `club_name`, `played`, `wins`, `draws`, `losses`, `goals_for`, `goals_against`, `goal_difference`,
   `points` (vitória 3, empate 1, derrota 0).
4. Ordem: pontos ↓; saldo ↓; gols marcados ↓; nome do clube (ordem alfabética das Premissas). `position` de
   1 ao total, sem repetir (o id desempata nomes equivalentes).
5. Calculada a cada consulta, nunca gravada.

Exemplo (6 clubes, 4 partidas: Águias 1×2 Delta; Gama 2×1 Épsilon; Épsilon 3×2 Águias; Delta 0×1 Gama):

| Pos. | Clube | Pts | Saldo | GM |
| --- | --- | --- | --- | --- |
| 1 | Gama SC | 6 | +2 | 3 |
| 2 | Épsilon EC | 3 | 0 | 4 |
| 3 | Delta FC | 3 | 0 | 2 |
| 4 | Mangue FC | 0 | 0 | 0 |
| 5 | Tubarões EC | 0 | 0 | 0 |
| 6 | Águias FC | 0 | −2 | 3 |
