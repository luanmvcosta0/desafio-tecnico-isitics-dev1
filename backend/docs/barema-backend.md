# Barema do backend — checklist de avaliação

Backend vale **50 pontos (5,0 na nota)**. Aprovação: nota final ≥ 7,0 com **≥ 2,5 no backend** e ≥ 2,5 no
frontend. Itens do roteiro valem tudo ou nada.

## B1. Roteiro da API (30 pontos) — script automatizado, na ordem

| Item | O que é verificado | Regras | Pts |
| --- | --- | --- | --- |
| **Etapa 1: Jogadores** | | | **9** |
| B01 | Validação do cadastro: corpo não-objeto, tipos, nomes (espaços, vazio), listas, obrigatórios ausentes/nulos, campos fora do snake_case, limites e regras cruzadas (idade×agilidade, soma, potencial) | V1–V6; Limites | 1,5 |
| B02 | JSON inválido (400), caminho/recurso inexistente (404, inclusive id fora do formato), regra (409), sempre com `message` e sem gravar; `Content-Type` JSON; listas vazias 200 | V1; V8; V9; Convenções | 1 |
| B03 | Campos controlados ignorados (inclusive `base_value`); `201` + `Location`; consulta por id; valor base e indicadores imutáveis no `PUT` | V7; RN08 | 1 |
| B04 | Validação do `PUT` e do `PATCH`; idade não diminui; `DOENTE`→`APTO` (409) | V1–V6; RN08 | 1 |
| B05 | Valor de mercado por condição, agilidade, idade, teto, piso, arredondamento | RN02–RN05 | 2 |
| B06 | Overall por pesos; valor base (faixas, jovem promessa, piso, arredondamento) | RN06 | 1 |
| B07 | Busca sem acentos, filtros combinados, ordenação (dois critérios, desempate), paginação `meta`, params inválidos/contraditórios | RN07 | 1 |
| B08 | Exclusão lógica: 204 sem corpo; excluído = 404 em consulta, edição, condição, nova exclusão; fora de `GET /players` | RN09; V8 | 0,5 |
| **Etapa 2: Clubes** | | | **1,5** |
| B09 | `201` + `Location`, caixa 300,00, formato e unicidade do nome, sigla, limite 20, `GET /clubs` por nome | RN10; RN11 | 1,5 |
| **Etapa 3: Compra** | | | **5** |
| B10 | Preço = valor de mercado, taxa, limite 50% sem arredondar, contrato `ATIVO`, transação `COMPRA`, jogador no elenco | RN12; RN13 | 1,5 |
| B11 | Recusas: jogador com clube, excluído (404), elenco 12, `club_id` inválido (400), clube inexistente (404) | RN09; RN12; RN14; V8 | 1 |
| B12 | Fidelidade: `-03:00`, outro formato 400, futura e ≤ 1.095 dias | RN01 | 1 |
| B13 | Sequência de compras conferida por `GET /clubs/{id}`: valores, caixa, elenco na ordem, contrato (fim, dias restantes, % multa), valor do elenco, patrimônio | RN02–RN05; RN11; RN13; RN15 | 1,5 |
| **Etapa 4: Venda** | | | **5** |
| B14 | Dias restantes (teto), faixas, teto 10,00, `RESCINDIDO`/`ENCERRADO` | RN15 | 2 |
| B15 | Lucro, comissão progressiva (inclusive prejuízo), crédito, jogador livre de novo, transação `VENDA` | RN16 | 2 |
| B16 | Venda recusada (livre, excluído); recompra proibida ao mesmo clube, permitida a outro | RN12; RN16; V8 | 1 |
| **Etapa 5: Clubes com elenco** | | | **1** |
| B17 | `GET /clubs` por patrimônio + desempate, `sort` inválido 400; `GET /players` com `club_id` e `free=false`; excluir com clube 409; mudar posição com clube 409 | RN07; RN09; RN11 | 1 |
| **Etapa 6: Histórico e exportação** | | | **4** |
| B18 | Ordem, nome/valores do momento (após editar jogador), transações de excluído mantidas, conciliação do caixa, filtros, `summary` filtrado, erros (inclusive `player_id` excluído 404) | RN09; RN17 | 2 |
| B19 | CSV: UTF-8 sem BOM, LF, cabeçalho, ordem, formato dos valores, nome com `;`/aspas, filtros, erros em JSON | RN18 | 2 |
| **Etapa 7: Liga** | | | **4,5** |
| B20 | Escalação: formações, 1 GOL + setores; 400/404/409 sem gravar; substituição integral sem mudar caixa; titular sai ao ser vendido ou ficar `DOENTE`; `GET lineup` com `formation`, `complete`, ordem do elenco | RN19 | 1,5 |
| B21 | Partida: `201` + `Location`; 6 duelos na ordem; atacante/defensor pela ordem do elenco; atributo pela posição; gol só se ataque > defesa; ×0,85 com arredondamento; `result`; sem efeitos colaterais; 400/404/409; gravação com valores do momento | RN20 | 2 |
| B22 | `GET /matches` ordem + filtro `club_id` + erros; `GET /matches/{id}` com 6 duelos; `GET /standings` com todos os clubes e desempates | RN21 | 1 |

Detalhes do script: HTTP/1.1, `Accept: */*`, caminhos sem barra final, `Content-Type: application/json` nos
pedidos com corpo, sem seguir redirecionamentos, timeout de 10 s por requisição. `Content-Type` da resposta
conferido em B02 e B19. Recusa = código certo + JSON com `message` + nada gravado.

## B2. Qualidade do código (10 pontos)

| Critério | Pts | Atende |
| --- | --- | --- |
| Camadas | 2 | Nenhum controller calcula regra ou acessa banco (1–2 ocorrências = metade) |
| Regras de negócio | 2 | Valor de mercado, overall, taxa, multa, comissão e placar, cada um em **um único lugar** |
| Validação e erros | 2 | Todo endpoint valida por mecanismo declarado; recusas por tratamento central; nunca stack trace |
| Consistência dos dados | 2 | Compra, venda, partida (+6 duelos), escalação e condição→DOENTE cada uma numa transação |
| Segurança e configuração | 1 | ORM/consultas parametrizadas; segredos por env; CORS restrito à origem do front; sem credencial real |
| Código morto e padrão | 1 | Nenhum arquivo com código comentado, sem uso ou nomes fora do padrão (mistura de idiomas conta) |

## B3. Banco de dados (6 pontos)

| Critério | Atende (2) |
| --- | --- |
| Criação reprodutível | Comandos do README criam **e recriam** o banco do zero por migrations/script versionado |
| Estrutura | Nenhum desvio das 7 tabelas (nomes, colunas, PKs, FKs, moeda em DECIMAL; PK composta em `escalacoes`) |
| Restrições | Unicidade no banco: nome do clube (RN10), sigla, `escalacoes.jogador_id`; nenhuma coluna calculada |

## B4. README e Git (2 pontos)

- README do backend (1): itens 1 e 3 a 8 dos requisitos presentes e corretos.
- Histórico Git (1): **≥ 10 commits que alteram `backend/`**, enviados em **≥ 3 dias diferentes**, no máximo
  metade com mensagem genérica (vazia ou só palavras como: ajuste(s), alteração(ões), atualização(ões),
  change(s), commit, correção(ões), final, fix(es), misc, teste(s), test(s), update(s), versão, wip).

## B5. Diferenciais (2 pontos, 0,5 cada)

Testes automatizados; Docker + Compose; Swagger/OpenAPI; logs de requisição; lint + formatação. Detalhes em
[infraestrutura.md](infraestrutura.md).

## Eliminação (independe da nota)

- Tecnologia fora da stack (ex.: H2/SQLite como banco da aplicação — nos testes, qualquer banco é permitido).
- Commit entregue sem as pastas `frontend/` e `backend/` na raiz.
- Repositório inacessível, hash ausente/errado, entrega fora do prazo, cópia comprovada.
