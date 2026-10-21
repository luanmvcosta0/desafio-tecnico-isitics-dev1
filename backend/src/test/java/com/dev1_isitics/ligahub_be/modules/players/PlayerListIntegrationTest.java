package com.dev1_isitics.ligahub_be.modules.players;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.support.PlayerRequests;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class PlayerListIntegrationTest extends PostgresIntegrationTest {

  private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVW";

  @Autowired private MockMvc mockMvc;

  private ResultActions list(String query) throws Exception {
    return mockMvc.perform(get("/api/v1/players?" + query)).andExpect(status().isOk());
  }

  private void registerSamples() throws Exception {
    PlayerRequests.register(mockMvc, "João Silva", "ATA", 19);
    PlayerRequests.register(mockMvc, "Meia Lua", "MEI", 25);
    PlayerRequests.register(mockMvc, "Épsilon Dias", "GOL", 33);
    PlayerRequests.register(mockMvc, "Delta Lima", "ZAG", 25);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "search=joao|João Silva",
        "search=LUA|Meia Lua",
        "position=ZAG|Delta Lima",
        "min_value=10.5|João Silva",
        "max_value=9|Épsilon Dias",
        "min_age=20&max_age=30&sort=name|Delta Lima,Meia Lua"
      })
  void filtersAndSearchesIgnoringAccents(String query, String expectedNames) throws Exception {
    registerSamples();

    list(query).andExpect(jsonPath("$.data[*].name", contains(expectedNames.split(","))));
  }

  @Test
  void normalizesSpacesOfTheSearch() throws Exception {
    registerSamples();

    mockMvc
        .perform(get("/api/v1/players").param("search", "  ep  "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].name", contains("Épsilon Dias")));
  }

  @Test
  void ordersByNameIgnoringAccentsByDefault() throws Exception {
    registerSamples();

    list("")
        .andExpect(
            jsonPath(
                "$.data[*].name", contains("Delta Lima", "Épsilon Dias", "João Silva", "Meia Lua")));
  }

  @Test
  void breaksSortTiesByNameAndThenById() throws Exception {
    registerSamples();
    PlayerRequests.register(mockMvc, "Delta Lima", "MEI", 25);

    list("sort=value_desc")
        .andExpect(
            jsonPath(
                "$.data[*].name",
                contains("João Silva", "Delta Lima", "Delta Lima", "Meia Lua", "Épsilon Dias")))
        .andExpect(jsonPath("$.data[1].position").value("ZAG"));
    list("sort=position,age_desc")
        .andExpect(
            jsonPath(
                "$.data[*].position", contains("GOL", "ZAG", "MEI", "MEI", "ATA")));
  }

  @Test
  void paginatesAfterFilteringAndSorting() throws Exception {
    for (char letter : LETTERS.toCharArray()) {
      PlayerRequests.register(mockMvc, "Atleta " + letter, "MEI", 25);
    }

    list("page=1&limit=10")
        .andExpect(jsonPath("$.data.length()").value(10))
        .andExpect(jsonPath("$.data[0].name").value("Atleta A"))
        .andExpect(jsonPath("$.meta.total_items").value(23))
        .andExpect(jsonPath("$.meta.total_pages").value(3));
    list("page=3&limit=10")
        .andExpect(jsonPath("$.data[*].name", contains("Atleta U", "Atleta V", "Atleta W")));
    list("page=4&limit=10")
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.meta.page").value(4))
        .andExpect(jsonPath("$.meta.total_pages").value(3));
    list("limit=50").andExpect(jsonPath("$.data.length()").value(23));
  }

  @Test
  void answersEmptyMetaWithoutPlayers() throws Exception {
    list("")
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.meta.total_items").value(0))
        .andExpect(jsonPath("$.meta.total_pages").value(0));
  }

  @Test
  void filtersByClubAndAnswersNotFoundForUnknownClub() throws Exception {
    registerSamples();
    jdbcTemplate.update(
        "INSERT INTO clubes (nome, sigla, caixa, criado_em) VALUES ('Águias FC', 'AGU', 300, now())");
    jdbcTemplate.update("UPDATE jogadores SET clube_id = 1 WHERE nome = 'Meia Lua'");

    list("club_id=1&free=false").andExpect(jsonPath("$.data[*].name", contains("Meia Lua")));
    list("free=true").andExpect(jsonPath("$.meta.total_items").value(3));
    mockMvc
        .perform(get("/api/v1/players?club_id=2"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").isNotEmpty());
    mockMvc.perform(get("/api/v1/players?club_id=2&free=true")).andExpect(status().isBadRequest());
  }
}
