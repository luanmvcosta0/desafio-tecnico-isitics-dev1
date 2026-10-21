package com.dev1_isitics.ligahub_be.modules.players;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class PlayerApiIntegrationTest extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PlayerRepository playerRepository;

  @Test
  void registersYoungProspectAndReadsItBack() throws Exception {
    String location =
        mockMvc
            .perform(
                post("/api/v1/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name": "  Davi   Lins ", "position": "LAT", "agility": 60, "age": 19,
                         "pace": 72, "shooting": 70, "passing": 78, "dribbling": 76,
                         "defending": 70, "physical": 66, "potential": 84,
                         "club_id": 3, "condition": "DOENTE", "base_value": 999}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Davi Lins"))
            .andExpect(jsonPath("$.overall").value(72))
            .andExpect(jsonPath("$.base_value").value(38.08))
            .andExpect(jsonPath("$.market_value").value(41.89))
            .andExpect(jsonPath("$.condition").value("APTO"))
            .andExpect(jsonPath("$.club_id").isEmpty())
            .andReturn()
            .getResponse()
            .getHeader("Location");

    mockMvc
        .perform(get(location))
        .andExpect(status().isOk())
        .andExpect(header().doesNotExist("Location"))
        .andExpect(jsonPath("$.name").value("Davi Lins"))
        .andExpect(jsonPath("$.base_value").value(38.08));
  }

  @Test
  void savesNothingWhenTheBodyIsRefused() throws Exception {
    long before = playerRepository.count();

    mockMvc
        .perform(
            post("/api/v1/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Meia Lua\", \"position\": \"MEI\"}"))
        .andExpect(status().isBadRequest());

    assertThat(playerRepository.count()).isEqualTo(before);
  }

  @Test
  void answersNotFoundForUnknownPlayer() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/2147483647"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }
}
