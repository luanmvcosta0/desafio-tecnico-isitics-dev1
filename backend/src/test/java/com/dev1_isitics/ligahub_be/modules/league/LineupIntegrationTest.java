package com.dev1_isitics.ligahub_be.modules.league;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.support.PlayerRequests;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class LineupIntegrationTest extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private int goalkeeper;
  private int defender;
  private int fullBack;
  private int midfielder;
  private int attacker;
  private int otherFullBack;
  private int sick;
  private int outsider;

  private ResultActions saveLineup(int clubId, String body) throws Exception {
    return mockMvc.perform(
        put("/api/v1/clubs/" + clubId + "/lineup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  private String lineup(String formation, int... ids) {
    return "{\"formation\": \"%s\", \"player_ids\": %s}"
        .formatted(formation, Arrays.toString(ids));
  }

  private int starters() {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM escalacoes", Integer.class);
  }

  private void joinClub(int clubId, int... ids) {
    for (int id : ids) {
      jdbcTemplate.update("UPDATE jogadores SET clube_id = ? WHERE id = ?", clubId, id);
    }
  }

  @BeforeEach
  void prepareSquad() throws Exception {
    for (String club : new String[] {"Águias FC|AGU", "Delta FC|DFC"}) {
      String[] parts = club.split("\\|");
      mockMvc
          .perform(
              post("/api/v1/clubs")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"%s\", \"acronym\": \"%s\"}".formatted(parts[0], parts[1])))
          .andExpect(status().isCreated());
    }
    attacker = PlayerRequests.register(mockMvc, "Otto Rangel", "ATA", 25);
    midfielder = PlayerRequests.register(mockMvc, "Lauro Neves", "MEI", 25);
    fullBack = PlayerRequests.register(mockMvc, "Davi Lins", "LAT", 25);
    defender = PlayerRequests.register(mockMvc, "Téo Brandão", "ZAG", 25);
    goalkeeper = PlayerRequests.register(mockMvc, "Rui Prado", "GOL", 25);
    otherFullBack = PlayerRequests.register(mockMvc, "Ana Lima", "LAT", 25);
    sick = PlayerRequests.register(mockMvc, "Zeca Doente", "ATA", 25);
    outsider = PlayerRequests.register(mockMvc, "Ivo Teles", "GOL", 25);
    joinClub(1, attacker, midfielder, fullBack, defender, goalkeeper, otherFullBack, sick);
    joinClub(2, outsider);
    jdbcTemplate.update("UPDATE jogadores SET condicao = 'DOENTE' WHERE id = ?", sick);
  }

  @Test
  void startsWithoutLineup() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/1/lineup"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.club_id").value(1))
        .andExpect(jsonPath("$.formation").isEmpty())
        .andExpect(jsonPath("$.complete").value(false))
        .andExpect(jsonPath("$.players").isEmpty());
  }

  @Test
  void savesTheStartersInSquadOrderAndReplacesTheWholeLineup() throws Exception {
    saveLineup(1, lineup("2-1-1", attacker, midfielder, goalkeeper, fullBack, defender))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.formation").value("2-1-1"))
        .andExpect(jsonPath("$.complete").value(true))
        .andExpect(jsonPath("$.players[*].position", contains("GOL", "ZAG", "LAT", "MEI", "ATA")))
        .andExpect(jsonPath("$.players[0].overall").value(60));
    mockMvc
        .perform(get("/api/v1/clubs/1/lineup"))
        .andExpect(
            jsonPath(
                "$.players[*].name",
                contains("Rui Prado", "Téo Brandão", "Davi Lins", "Lauro Neves", "Otto Rangel")));

    saveLineup(1, lineup("3-1", goalkeeper, defender, fullBack, otherFullBack, attacker))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$.players[*].name",
                contains("Rui Prado", "Téo Brandão", "Ana Lima", "Davi Lins", "Otto Rangel")));
    assertThat(starters()).isEqualTo(5);
    assertThat(
            jdbcTemplate.queryForObject("SELECT caixa FROM clubes WHERE id = 1", BigDecimal.class))
        .isEqualByComparingTo("300.00");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"player_ids\": [1, 2, 3, 4, 5]}",
        "{\"formation\": \"4-0\", \"player_ids\": [1, 2, 3, 4, 5]}",
        "{\"formation\": \"2-1-1 \", \"player_ids\": [1, 2, 3, 4, 5]}",
        "{\"formation\": 211, \"player_ids\": [1, 2, 3, 4, 5]}",
        "{\"formation\": \"2-1-1\"}",
        "{\"formation\": \"2-1-1\", \"player_ids\": [1, 2, 3, 4]}",
        "{\"formation\": \"2-1-1\", \"player_ids\": [1, 2, 3, 4, 5, 6]}",
        "{\"formation\": \"2-1-1\", \"player_ids\": [1, 2, 3, 4, 4]}",
        "{\"formation\": \"2-1-1\", \"player_ids\": [0, 2, 3, 4, 5]}",
        "{\"formation\": \"2-1-1\", \"player_ids\": [\"1\", 2, 3, 4, 5]}",
        "{\"formation\": \"2-1-1\", \"player_ids\": [null, 2, 3, 4, 5]}",
        "{\"formation\": \"2-1-1\", \"player_ids\": 5}"
      })
  void refusesInvalidBodyWithoutSaving(String body) throws Exception {
    saveLineup(1, body)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());

    assertThat(starters()).isZero();
  }

  @Test
  void answersNotFoundForUnknownClubOrPlayer() throws Exception {
    saveLineup(9, lineup("2-1-1", attacker, midfielder, goalkeeper, fullBack, defender))
        .andExpect(status().isNotFound());
    saveLineup(1, lineup("2-1-1", attacker, midfielder, goalkeeper, fullBack, 999))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/api/v1/clubs/9/lineup")).andExpect(status().isNotFound());
  }

  @Test
  void refusesStartersOutOfTheSquadSickOrNotClosingTheFormation() throws Exception {
    saveLineup(1, lineup("2-1-1", attacker, midfielder, goalkeeper, fullBack, defender))
        .andExpect(status().isOk());

    saveLineup(1, lineup("2-1-1", attacker, midfielder, outsider, fullBack, defender))
        .andExpect(status().isConflict());
    saveLineup(1, lineup("2-1-1", sick, midfielder, goalkeeper, fullBack, defender))
        .andExpect(status().isConflict());
    saveLineup(1, lineup("2-2", attacker, midfielder, goalkeeper, fullBack, defender))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").isNotEmpty());
    mockMvc
        .perform(get("/api/v1/clubs/1/lineup"))
        .andExpect(jsonPath("$.formation").value("2-1-1"))
        .andExpect(jsonPath("$.complete").value(true));
  }

  @Test
  void acceptsUnfitStarters() throws Exception {
    jdbcTemplate.update("UPDATE jogadores SET condicao = 'MAL_CONDICIONADO' WHERE id = ?", fullBack);

    saveLineup(1, lineup("2-1-1", attacker, midfielder, goalkeeper, fullBack, defender))
        .andExpect(status().isOk());
  }
}
