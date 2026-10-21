package com.dev1_isitics.ligahub_be.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Registers players through the API, all attributes 60 (overall 60, base value 10,00). */
public final class PlayerRequests {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String BODY =
      """
      {"name": "%s", "position": "%s", "agility": 50, "age": %d, "pace": 60, "shooting": 60,
       "passing": 60, "dribbling": 60, "defending": 60, "physical": 60, "potential": 60}
      """;

  private PlayerRequests() {}

  public static int register(MockMvc mockMvc, String name, String position, int age)
      throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(BODY.formatted(name, position, age)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return MAPPER.readTree(response).get("id").asInt();
  }
}
