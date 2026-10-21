package com.dev1_isitics.ligahub_be.modules.players;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.support.PlayerRequests;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class PlayerManagementIntegrationTest extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private int playerId;

  @BeforeEach
  void registerPlayer() throws Exception {
    playerId = PlayerRequests.register(mockMvc, "Meia Lua", "MEI", 25);
  }

  private ResultActions edit(int id, String body) throws Exception {
    return mockMvc.perform(
        put("/api/v1/players/" + id).contentType(MediaType.APPLICATION_JSON).content(body));
  }

  private ResultActions changeCondition(String condition) throws Exception {
    return mockMvc.perform(
        patch("/api/v1/players/" + playerId + "/condition")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"condition\": \"" + condition + "\", \"name\": \"Outro Nome\"}"));
  }

  private void joinClub() {
    jdbcTemplate.update(
        "INSERT INTO clubes (nome, sigla, caixa, criado_em) VALUES ('Águias FC', 'AGU', 300, now())");
    jdbcTemplate.update("UPDATE jogadores SET clube_id = 1 WHERE id = ?", playerId);
  }

  @Test
  void editsOnlyTheFourFieldsAndKeepsTheBaseValue() throws Exception {
    edit(
            playerId,
            """
            {"name": " Meia   Zeta ", "position": "ATA", "agility": 90, "age": 33,
             "pace": 99, "potential": 99, "base_value": 500, "overall": 99}
            """)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Meia Zeta"))
        .andExpect(jsonPath("$.position").value("ATA"))
        .andExpect(jsonPath("$.age").value(33))
        .andExpect(jsonPath("$.pace").value(60))
        .andExpect(jsonPath("$.potential").value(60))
        .andExpect(jsonPath("$.base_value").value(10.00))
        .andExpect(jsonPath("$.market_value").value(9.00));
  }

  @Test
  void refusesLowerAgeAndKeepsThePlayer() throws Exception {
    edit(playerId, "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 50, \"age\": 24}")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").isNotEmpty());

    mockMvc.perform(get("/api/v1/players/" + playerId)).andExpect(jsonPath("$.age").value(25));
  }

  @Test
  void refusesPositionChangeOfPlayerWithClub() throws Exception {
    joinClub();

    edit(playerId, "{\"name\": \"Meia Lua\", \"position\": \"ATA\", \"agility\": 50, \"age\": 25}")
        .andExpect(status().isConflict());
    edit(playerId, "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 60, \"age\": 26}")
        .andExpect(status().isOk());
  }

  @Test
  void checksFormatBeforeExistence() throws Exception {
    edit(999, "{\"name\": \"Meia Lua\"}").andExpect(status().isBadRequest());
    edit(999, "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 50, \"age\": 25}")
        .andExpect(status().isNotFound());
  }

  @Test
  void changesConditionButNeverFromSickStraightToFit() throws Exception {
    changeCondition("DOENTE")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.condition").value("DOENTE"))
        .andExpect(jsonPath("$.name").value("Meia Lua"));
    changeCondition("DOENTE").andExpect(status().isOk());
    changeCondition("APTO").andExpect(status().isConflict());
    changeCondition("MAL_CONDICIONADO").andExpect(status().isOk());
    changeCondition("APTO").andExpect(status().isOk());
  }

  @Test
  void deletesLogicallyAndAnswersNotFoundEverywhereAfterwards() throws Exception {
    mockMvc
        .perform(delete("/api/v1/players/" + playerId))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM jogadores WHERE excluido_em IS NOT NULL", Integer.class))
        .isEqualTo(1);
    mockMvc.perform(get("/api/v1/players/" + playerId)).andExpect(status().isNotFound());
    mockMvc.perform(delete("/api/v1/players/" + playerId)).andExpect(status().isNotFound());
    changeCondition("DOENTE").andExpect(status().isNotFound());
    edit(playerId, "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 50, \"age\": 25}")
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/v1/players"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.meta.total_items").value(0));
  }

  @Test
  void refusesToDeletePlayerWithClub() throws Exception {
    joinClub();

    mockMvc.perform(delete("/api/v1/players/" + playerId)).andExpect(status().isConflict());
    mockMvc.perform(get("/api/v1/players/" + playerId)).andExpect(status().isOk());
  }
}
