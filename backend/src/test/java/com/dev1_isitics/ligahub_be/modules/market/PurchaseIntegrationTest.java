package com.dev1_isitics.ligahub_be.modules.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import com.dev1_isitics.ligahub_be.support.PlayerRequests;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class PurchaseIntegrationTest extends PostgresIntegrationTest {

  private static final DateTimeFormatter REQUEST_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mmXXX");
  private static final String STAR_PLAYER =
      """
      {"name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25, "pace": 70,
       "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55, "physical": 64,
       "potential": 86}
      """;

  @Autowired private MockMvc mockMvc;

  private int starId;

  private static String loyaltyIn(Duration duration) {
    return Instant.now()
        .truncatedTo(ChronoUnit.MINUTES)
        .plus(duration)
        .atOffset(BrasiliaDateTime.OFFSET)
        .format(REQUEST_FORMAT);
  }

  private ResultActions buy(int playerId, String body) throws Exception {
    return mockMvc.perform(
        post("/api/v1/players/" + playerId + "/buy")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  private ResultActions buy(int playerId, int clubId) throws Exception {
    return buy(
        playerId,
        "{\"club_id\": %d, \"loyalty_until\": \"%s\"}"
            .formatted(clubId, loyaltyIn(Duration.ofDays(120))));
  }

  private void createClub(String name, String acronym) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/clubs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"%s\", \"acronym\": \"%s\"}".formatted(name, acronym)))
        .andExpect(status().isCreated());
  }

  private BigDecimal cashOf(int clubId) {
    return jdbcTemplate.queryForObject(
        "SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, clubId);
  }

  private int countOf(String table) {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
  }

  @BeforeEach
  void prepare() throws Exception {
    createClub("Águias FC", "AGU");
    createClub("Delta FC", "DFC");
    String response =
        mockMvc
            .perform(
                post("/api/v1/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(STAR_PLAYER))
            .andReturn()
            .getResponse()
            .getContentAsString();
    starId = Integer.parseInt(response.replaceAll(".*\"id\":(\\d+).*", "$1"));
  }

  @Test
  void buysAtMarketValuePlusFeeAndSignsTheContract() throws Exception {
    String loyalty = loyaltyIn(Duration.ofDays(120));

    buy(starId, "{\"club_id\": 1, \"loyalty_until\": \"" + loyalty + "\"}")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.club_id").value(1))
        .andExpect(jsonPath("$.player_id").value(starId))
        .andExpect(jsonPath("$.player_name").value("Meia Lua"))
        .andExpect(jsonPath("$.type").value("COMPRA"))
        .andExpect(jsonPath("$.value").value(40.00))
        .andExpect(jsonPath("$.fee").value(2.00))
        .andExpect(jsonPath("$.penalty").value(0.00))
        .andExpect(jsonPath("$.profit").isEmpty())
        .andExpect(jsonPath("$.cash_change").value(-42.00))
        .andExpect(jsonPath("$.created_at").value(endsWith("-03:00")))
        .andExpect(jsonPath("$.contract.purchase_value").value(40.00))
        .andExpect(
            jsonPath("$.contract.loyalty_until").value(loyalty.replace("-03:00", ":00-03:00")))
        .andExpect(jsonPath("$.contract.status").value("ATIVO"))
        .andExpect(jsonPath("$.club_cash").value(258.00));

    mockMvc
        .perform(get("/api/v1/clubs/1"))
        .andExpect(jsonPath("$.cash").value(258.00))
        .andExpect(jsonPath("$.squad_value").value(40.00))
        .andExpect(jsonPath("$.net_worth").value(298.00))
        .andExpect(jsonPath("$.players[0].name").value("Meia Lua"))
        .andExpect(jsonPath("$.players[0].club_id").value(1))
        .andExpect(jsonPath("$.players[0].contract.purchase_value").value(40.00))
        .andExpect(jsonPath("$.players[0].contract.remaining_days").value(120))
        .andExpect(jsonPath("$.players[0].contract.penalty_rate").value(10));
    mockMvc
        .perform(get("/api/v1/players?club_id=1"))
        .andExpect(jsonPath("$.data[*].name", contains("Meia Lua")));
    mockMvc.perform(get("/api/v1/players?free=true")).andExpect(jsonPath("$.data").isEmpty());
    BigDecimal cashChanges =
        jdbcTemplate.queryForObject("SELECT sum(variacao_caixa) FROM transacoes", BigDecimal.class);
    assertThat(cashOf(1)).isEqualByComparingTo(new BigDecimal("300.00").add(cashChanges));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
        "{\"club_id\": 0, \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
        "{\"club_id\": \"1\", \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
        "{\"club_id\": 1}",
        "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T10:15\"}",
        "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T10:15:00-03:00\"}",
        "{\"club_id\": 1, \"loyalty_until\": \"2027-02-30T10:15-03:00\"}",
        "{\"club_id\": 1, \"loyalty_until\": \"2020-01-01T10:15-03:00\"}",
        "{\"club_id\": 1, \"loyalty_until\": \"2099-01-01T10:15-03:00\"}"
      })
  void refusesInvalidBodyWithoutSaving(String body) throws Exception {
    buy(starId, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());

    assertThat(countOf("transacoes")).isZero();
    assertThat(cashOf(1)).isEqualByComparingTo("300.00");
  }

  @Test
  void acceptsLoyaltyUpToExactly1095Days() throws Exception {
    buy(
            starId,
            "{\"club_id\": 1, \"loyalty_until\": \"%s\"}"
                .formatted(loyaltyIn(Duration.ofDays(1095))))
        .andExpect(status().isOk());
  }

  @Test
  void answersNotFoundForUnknownOrDeletedPlayerAndUnknownClub() throws Exception {
    buy(999, 1).andExpect(status().isNotFound());
    buy(starId, 9).andExpect(status().isNotFound());
    jdbcTemplate.update("UPDATE jogadores SET excluido_em = now() WHERE id = ?", starId);
    buy(starId, 1).andExpect(status().isNotFound());
  }

  @Test
  void refusesPlayerThatAlreadyHasAClub() throws Exception {
    buy(starId, 1).andExpect(status().isOk());

    buy(starId, 2).andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());
    assertThat(cashOf(2)).isEqualByComparingTo("300.00");
  }

  @Test
  void refusesRebuyByTheClubThatSoldThePlayer() throws Exception {
    jdbcTemplate.update(
        "INSERT INTO transacoes (clube_id, jogador_id, nome_jogador, tipo, valor, taxa, multa,"
            + " lucro, variacao_caixa, criado_em) VALUES (1, ?, 'Meia Lua', 'VENDA', 40, 0, 0, 0,"
            + " 40, now())",
        starId);

    buy(starId, 1).andExpect(status().isConflict());
    buy(starId, 2).andExpect(status().isOk());
  }

  @Test
  void refusesThirteenthPlayerOfTheSquad() throws Exception {
    for (int index = 0; index < 12; index++) {
      int id = PlayerRequests.register(mockMvc, "Atleta " + (char) ('A' + index), "MEI", 25);
      jdbcTemplate.update("UPDATE jogadores SET clube_id = 1 WHERE id = ?", id);
    }

    buy(starId, 1).andExpect(status().isConflict());
  }

  @Test
  void limitsTheCostToHalfOfTheCash() throws Exception {
    jdbcTemplate.update("UPDATE clubes SET caixa = 83.99 WHERE id = 1");
    buy(starId, 1)
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message")
                .value("O custo da compra (42,00) passa do limite de 50% do caixa (41,995)."));

    jdbcTemplate.update("UPDATE clubes SET caixa = 84.00 WHERE id = 1");
    buy(starId, 1).andExpect(status().isOk()).andExpect(jsonPath("$.club_cash").value(42.00));
  }
}
