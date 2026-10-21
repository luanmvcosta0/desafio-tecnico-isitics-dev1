package com.dev1_isitics.ligahub_be.modules.league;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class MatchIntegrationTest extends PostgresIntegrationTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String PLAYER =
      """
      {"name": "%s", "position": "%s", "agility": 50, "age": 25, "pace": 50, "shooting": %d,
       "passing": %d, "dribbling": %d, "defending": %d, "physical": 50, "potential": 90}
      """;

  @Autowired private MockMvc mockMvc;

  private ResultActions send(MockHttpServletRequestBuilder request, String body)
      throws Exception {
    return mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
  }

  private ResultActions play(String body) throws Exception {
    return send(post("/api/v1/matches"), body);
  }

  private int createClub(String name, String acronym) throws Exception {
    String body =
        send(
                post("/api/v1/clubs"),
                "{\"name\": \"%s\", \"acronym\": \"%s\"}".formatted(name, acronym))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return MAPPER.readTree(body).get("id").asInt();
  }

  private int starter(
      int clubId,
      String name,
      String position,
      int shooting,
      int passing,
      int dribbling,
      int defending)
      throws Exception {
    String body =
        send(
                post("/api/v1/players"),
                PLAYER.formatted(name, position, shooting, passing, dribbling, defending))
            .andReturn()
            .getResponse()
            .getContentAsString();
    int id = MAPPER.readTree(body).get("id").asInt();
    jdbcTemplate.update("UPDATE jogadores SET clube_id = ? WHERE id = ?", clubId, id);
    return id;
  }

  private void lineup(int clubId, List<Integer> ids) throws Exception {
    send(
            put("/api/v1/clubs/" + clubId + "/lineup"),
            "{\"formation\": \"2-1-1\", \"player_ids\": " + ids + "}")
        .andExpect(status().isOk());
  }

  private int count(String table) {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
  }

  @BeforeEach
  void prepareTheExampleClubs() throws Exception {
    int aguias = createClub("Águias FC", "AGU");
    int delta = createClub("Delta FC", "DFC");
    createClub("Mangue FC", "MFC");
    List<Integer> aguiasStarters = new ArrayList<>();
    aguiasStarters.add(starter(aguias, "Rui Prado", "GOL", 50, 50, 50, 77));
    aguiasStarters.add(starter(aguias, "Téo Brandão", "ZAG", 50, 50, 50, 84));
    int unfit = starter(aguias, "Davi Lins", "LAT", 50, 50, 74, 66);
    aguiasStarters.add(unfit);
    aguiasStarters.add(starter(aguias, "Lauro Neves", "MEI", 50, 82, 50, 50));
    aguiasStarters.add(starter(aguias, "Otto Rangel", "ATA", 75, 50, 50, 50));
    jdbcTemplate.update("UPDATE jogadores SET condicao = 'MAL_CONDICIONADO' WHERE id = ?", unfit);
    lineup(aguias, aguiasStarters);
    lineup(
        delta,
        List.of(
            starter(delta, "Ivo Teles", "GOL", 50, 50, 50, 72),
            starter(delta, "Hugo Vaz", "ZAG", 50, 50, 50, 84),
            starter(delta, "Lino Matos", "LAT", 50, 50, 79, 63),
            starter(delta, "Nei Soares", "MEI", 50, 70, 50, 50),
            starter(delta, "Ugo Falcão", "ATA", 81, 50, 50, 50)));
  }

  @Test
  void playsAndRecordsTheMatchWithItsSixDuels() throws Exception {
    play("{\"home_club_id\": 1, \"away_club_id\": 2}")
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/matches/1"))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.played_at").value(endsWith("-03:00")))
        .andExpect(jsonPath("$.home.club_name").value("Águias FC"))
        .andExpect(jsonPath("$.home.formation").value("2-1-1"))
        .andExpect(jsonPath("$.home.goals").value(1))
        .andExpect(jsonPath("$.away.goals").value(2))
        .andExpect(jsonPath("$.result").value("VISITANTE"))
        .andExpect(jsonPath("$.duels[*].order", contains(1, 2, 3, 4, 5, 6)))
        .andExpect(jsonPath("$.duels[*].goal", contains(true, false, false, true, false, true)))
        .andExpect(jsonPath("$.duels[0].attacking_club_id").value(1))
        .andExpect(jsonPath("$.duels[0].attacker.player_name").value("Otto Rangel"))
        .andExpect(jsonPath("$.duels[0].attacker.attribute").value("shooting"))
        .andExpect(jsonPath("$.duels[0].defender.position").value("GOL"))
        .andExpect(jsonPath("$.duels[0].defender.attribute").value("defending"))
        .andExpect(jsonPath("$.duels[2].attacker.condition").value("MAL_CONDICIONADO"))
        .andExpect(jsonPath("$.duels[2].attacker.value").value(63))
        .andExpect(jsonPath("$.duels[5].defender.value").value(56));

    assertThat(count("duelos_partida")).isEqualTo(6);
    mockMvc
        .perform(get("/api/v1/matches/1"))
        .andExpect(jsonPath("$.duels.length()").value(6))
        .andExpect(jsonPath("$.duels[3].attacker.player_name").value("Ugo Falcão"));
  }

  @Test
  void changesNeitherCashNorLineupsNorPlayers() throws Exception {
    play("{\"home_club_id\": 1, \"away_club_id\": 2}").andExpect(status().isCreated());

    assertThat(jdbcTemplate.queryForObject("SELECT sum(caixa) FROM clubes", BigDecimal.class))
        .isEqualByComparingTo("900.00");
    assertThat(count("escalacoes")).isEqualTo(10);
    mockMvc
        .perform(get("/api/v1/clubs/1/lineup"))
        .andExpect(jsonPath("$.complete").value(true));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"home_club_id\": 1, \"away_club_id\": 1}",
        "{\"home_club_id\": 1}",
        "{\"away_club_id\": 2}",
        "{\"home_club_id\": 0, \"away_club_id\": 2}",
        "{\"home_club_id\": \"1\", \"away_club_id\": 2}",
        "[]"
      })
  void refusesInvalidBodyWithoutRecording(String body) throws Exception {
    play(body).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty());

    assertThat(count("partidas")).isZero();
  }

  @Test
  void answersNotFoundAndConflictWithoutRecording() throws Exception {
    play("{\"home_club_id\": 1, \"away_club_id\": 9}").andExpect(status().isNotFound());
    play("{\"home_club_id\": 3, \"away_club_id\": 1}")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").isNotEmpty());
    jdbcTemplate.update("DELETE FROM escalacoes WHERE jogador_id = 1");
    play("{\"home_club_id\": 1, \"away_club_id\": 2}").andExpect(status().isConflict());

    assertThat(count("partidas")).isZero();
    mockMvc.perform(get("/api/v1/matches/1")).andExpect(status().isNotFound());
  }

  @Test
  void listsMostRecentFirstWithClubFilterAndWithoutDuels() throws Exception {
    play("{\"home_club_id\": 1, \"away_club_id\": 2}");
    play("{\"home_club_id\": 2, \"away_club_id\": 1}");

    mockMvc
        .perform(get("/api/v1/matches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].id", contains(2, 1)))
        .andExpect(jsonPath("$[0].duels").doesNotExist())
        .andExpect(jsonPath("$[0].home.club_name").value("Delta FC"));
    mockMvc.perform(get("/api/v1/matches?club_id=3")).andExpect(jsonPath("$").isEmpty());
    mockMvc.perform(get("/api/v1/matches?club_id=1")).andExpect(jsonPath("$.length()").value(2));
    mockMvc.perform(get("/api/v1/matches?club_id=9")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/v1/matches?club_id=abc")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/v1/matches?club_id=")).andExpect(status().isBadRequest());
  }

}
