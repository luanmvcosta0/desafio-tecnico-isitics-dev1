package com.dev1_isitics.ligahub_be.modules.clubs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.support.PlayerRequests;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class ClubApiIntegrationTest extends PostgresIntegrationTest {

  private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTU";

  @Autowired private MockMvc mockMvc;

  private ResultActions create(String name, String acronym) throws Exception {
    return mockMvc.perform(
        post("/api/v1/clubs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"name\": \"%s\", \"acronym\": \"%s\", \"cash\": 9999}".formatted(name, acronym)));
  }

  private int clubCount() {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM clubes", Integer.class);
  }

  @Test
  void createsClubWithInitialCashAndLocation() throws Exception {
    create("  Águias   FC ", "AGU")
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/clubs/1"))
        .andExpect(jsonPath("$.name").value("Águias FC"))
        .andExpect(jsonPath("$.acronym").value("AGU"))
        .andExpect(jsonPath("$.cash").value(300.00))
        .andExpect(jsonPath("$.squad_value").value(0.00))
        .andExpect(jsonPath("$.net_worth").value(300.00));

    mockMvc
        .perform(get("/api/v1/clubs/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.players").isEmpty());
  }

  @Test
  void refusesDuplicatedNameIgnoringCaseButConsideringAccents() throws Exception {
    create("Águias FC", "AGU").andExpect(status().isCreated());

    create("  águias   fc ", "AFC")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").isNotEmpty());
    create("Aguias FC", "AFC").andExpect(status().isCreated());
    create("Aguias Clube", "AGU").andExpect(status().isConflict());
    assertThat(clubCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"name\": \"Águias FC\"}", "{\"acronym\": \"AGU\"}", "[]", "{\"name\": 10}"})
  void refusesInvalidBodyWithoutSaving(String body) throws Exception {
    mockMvc
        .perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());
    assertThat(clubCount()).isZero();
  }

  @Test
  void refusesTheTwentyFirstClub() throws Exception {
    for (char letter : LETTERS.substring(0, 20).toCharArray()) {
      create("Time " + letter + " Clube", "T" + letter + "C").andExpect(status().isCreated());
    }

    create("Clube Extra", "CEX").andExpect(status().isConflict());
    assertThat(clubCount()).isEqualTo(20);
  }

  @Test
  void listsByNameOrNetWorthWithSquadValues() throws Exception {
    create("Gama SC", "GSC");
    create("Épsilon EC", "EEC");
    create("Delta FC", "DFC");
    int player = PlayerRequests.register(mockMvc, "Meia Lua", "MEI", 25);
    jdbcTemplate.update("UPDATE jogadores SET clube_id = 1 WHERE id = ?", player);
    jdbcTemplate.update("UPDATE clubes SET caixa = 295 WHERE id = 1");

    mockMvc
        .perform(get("/api/v1/clubs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].name", contains("Delta FC", "Épsilon EC", "Gama SC")));
    mockMvc
        .perform(get("/api/v1/clubs?sort=net_worth_desc"))
        .andExpect(jsonPath("$[*].name", contains("Gama SC", "Delta FC", "Épsilon EC")))
        .andExpect(jsonPath("$[0].squad_value").value(10.00))
        .andExpect(jsonPath("$[0].net_worth").value(305.00));
    mockMvc
        .perform(get("/api/v1/clubs?sort=net_worth_asc"))
        .andExpect(jsonPath("$[*].name", contains("Delta FC", "Épsilon EC", "Gama SC")));
  }

  @ParameterizedTest
  @ValueSource(strings = {"sort=", "sort=NAME", "sort=net_worth", "sort=name,net_worth_desc"})
  void refusesInvalidSort(String query) throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?" + query))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void showsTheSquadInSquadOrder() throws Exception {
    create("Águias FC", "AGU");
    PlayerRequests.register(mockMvc, "Otto Rangel", "ATA", 25);
    PlayerRequests.register(mockMvc, "Rui Prado", "GOL", 25);
    PlayerRequests.register(mockMvc, "Davi Lins", "LAT", 25);
    PlayerRequests.register(mockMvc, "Ana Lima", "LAT", 25);
    jdbcTemplate.update("UPDATE jogadores SET clube_id = 1");

    mockMvc
        .perform(get("/api/v1/clubs/1"))
        .andExpect(
            jsonPath("$.players[*].name", contains("Rui Prado", "Ana Lima", "Davi Lins", "Otto Rangel")))
        .andExpect(jsonPath("$.squad_value").value(40.00))
        .andExpect(jsonPath("$.net_worth").value(340.00));
  }

  @Test
  void answersNotFoundForUnknownClub() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }
}
