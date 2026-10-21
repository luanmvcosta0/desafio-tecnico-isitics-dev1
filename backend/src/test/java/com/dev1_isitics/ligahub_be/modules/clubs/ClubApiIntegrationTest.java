package com.dev1_isitics.ligahub_be.modules.clubs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
