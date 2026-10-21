package com.dev1_isitics.ligahub_be.modules.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class SaleIntegrationTest extends PostgresIntegrationTest {

  private static final DateTimeFormatter REQUEST_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mmXXX");
  private static final int PLAYER_ID = 1;

  @Autowired private MockMvc mockMvc;

  private ResultActions perform(MockHttpServletRequestBuilder request, String body)
      throws Exception {
    return mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
  }

  private ResultActions sell() throws Exception {
    return mockMvc.perform(post("/api/v1/players/" + PLAYER_ID + "/sell"));
  }

  private ResultActions buy(int clubId, Duration loyalty) throws Exception {
    String loyaltyUntil =
        Instant.now()
            .truncatedTo(ChronoUnit.MINUTES)
            .plus(loyalty)
            .atOffset(BrasiliaDateTime.OFFSET)
            .format(REQUEST_FORMAT);
    return perform(
        post("/api/v1/players/" + PLAYER_ID + "/buy"),
        "{\"club_id\": %d, \"loyalty_until\": \"%s\"}".formatted(clubId, loyaltyUntil));
  }

  private void changeCondition(String condition) throws Exception {
    perform(
            patch("/api/v1/players/" + PLAYER_ID + "/condition"),
            "{\"condition\": \"" + condition + "\"}")
        .andExpect(status().isOk());
  }

  private BigDecimal cashOf(int clubId) {
    return jdbcTemplate.queryForObject(
        "SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, clubId);
  }

  @BeforeEach
  void registerPlayerAndClubs() throws Exception {
    perform(post("/api/v1/clubs"), "{\"name\": \"Águias FC\", \"acronym\": \"AGU\"}")
        .andExpect(status().isCreated());
    perform(post("/api/v1/clubs"), "{\"name\": \"Delta FC\", \"acronym\": \"DFC\"}")
        .andExpect(status().isCreated());
    perform(
            post("/api/v1/players"),
            """
            {"name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25, "pace": 70,
             "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55, "physical": 64,
             "potential": 86}
            """)
        .andExpect(status().isCreated());
    changeCondition("MAL_CONDICIONADO");
  }

  @Test
  void sellsWithPenaltyCommissionAndCreditLikeTheContractExample() throws Exception {
    buy(1, Duration.ofDays(120))
        .andExpect(jsonPath("$.value").value(37.00))
        .andExpect(jsonPath("$.club_cash").value(261.15));
    changeCondition("APTO");

    sell()
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("VENDA"))
        .andExpect(jsonPath("$.club_id").value(1))
        .andExpect(jsonPath("$.player_name").value("Meia Lua"))
        .andExpect(jsonPath("$.value").value(40.00))
        .andExpect(jsonPath("$.fee").value(0.30))
        .andExpect(jsonPath("$.penalty").value(4.00))
        .andExpect(jsonPath("$.profit").value(3.00))
        .andExpect(jsonPath("$.cash_change").value(35.70))
        .andExpect(jsonPath("$.purchase_value").value(37.00))
        .andExpect(jsonPath("$.remaining_days").value(120))
        .andExpect(jsonPath("$.penalty_rate").value(10))
        .andExpect(jsonPath("$.contract_status").value("RESCINDIDO"))
        .andExpect(jsonPath("$.club_cash").value(296.85));

    mockMvc
        .perform(get("/api/v1/players/" + PLAYER_ID))
        .andExpect(jsonPath("$.club_id").isEmpty());
    mockMvc.perform(get("/api/v1/clubs/1")).andExpect(jsonPath("$.players").isEmpty());
    BigDecimal cashChanges =
        jdbcTemplate.queryForObject(
            "SELECT sum(variacao_caixa) FROM transacoes WHERE clube_id = 1", BigDecimal.class);
    assertThat(cashOf(1)).isEqualByComparingTo(new BigDecimal("300.00").add(cashChanges));
  }

  @Test
  void endsTheContractWithoutPenaltyWhenTheLoyaltyIsOver() throws Exception {
    buy(1, Duration.ofDays(30)).andExpect(status().isOk());
    jdbcTemplate.update("UPDATE contratos SET fidelidade_ate = now() - interval '1 day'");

    sell()
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.remaining_days").value(0))
        .andExpect(jsonPath("$.penalty_rate").value(0))
        .andExpect(jsonPath("$.penalty").value(0.00))
        .andExpect(jsonPath("$.profit").value(0.00))
        .andExpect(jsonPath("$.fee").value(0.00))
        .andExpect(jsonPath("$.cash_change").value(37.00))
        .andExpect(jsonPath("$.contract_status").value("ENCERRADO"));
  }

  @Test
  void refusesToSellAFreeOrDeletedPlayer() throws Exception {
    sell().andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());

    mockMvc.perform(post("/api/v1/players/999/sell")).andExpect(status().isNotFound());
    mockMvc.perform(post("/api/v1/players/abc/sell")).andExpect(status().isNotFound());
    jdbcTemplate.update("UPDATE jogadores SET excluido_em = now()");
    sell().andExpect(status().isNotFound());
  }

  @Test
  void allowsAnotherClubToBuyButNotTheSeller() throws Exception {
    buy(1, Duration.ofDays(200)).andExpect(status().isOk());
    perform(post("/api/v1/players/" + PLAYER_ID + "/sell"), "{\"club_id\": 2}")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.club_id").value(1))
        .andExpect(jsonPath("$.penalty_rate").value(20));

    buy(1, Duration.ofDays(30)).andExpect(status().isConflict());
    buy(2, Duration.ofDays(30)).andExpect(status().isOk());
  }
}
