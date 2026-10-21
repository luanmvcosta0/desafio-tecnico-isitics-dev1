package com.dev1_isitics.ligahub_be.modules.players.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerQueryService;
import com.dev1_isitics.ligahub_be.modules.players.application.PlayerService;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.modules.players.domain.TechnicalAttributes;
import com.dev1_isitics.ligahub_be.shared.config.JacksonConfig;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(PlayerController.class)
@Import(JacksonConfig.class)
class PlayerControllerTest {

  private static final String VALID_BODY =
      """
      {"name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25, "pace": 70,
       "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55, "physical": 64,
       "potential": 86}
      """;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PlayerService playerService;

  @MockitoBean private PlayerQueryService playerQueryService;

  private static Player savedPlayer() {
    Player player =
        Player.register(
            "Meia Lua", Position.MEI, 85, 25, new TechnicalAttributes(70, 68, 82, 79, 55, 64), 86);
    ReflectionTestUtils.setField(player, "id", 2);
    return player;
  }

  private ResultActions postPlayer(String body) throws Exception {
    return mockMvc.perform(
        post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content(body));
  }

  private static String withField(String field, String value) {
    return VALID_BODY.replaceFirst(
        "\"" + field + "\": [^,}]+", Matcher.quoteReplacement("\"" + field + "\": " + value));
  }

  @Test
  void registersPlayerWithLocationAndComputedValues() throws Exception {
    when(playerService.register(any())).thenReturn(savedPlayer());

    postPlayer(VALID_BODY)
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/players/2"))
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.condition").value("APTO"))
        .andExpect(jsonPath("$.overall").value(75))
        .andExpect(jsonPath("$.base_value").value(40.00))
        .andExpect(jsonPath("$.market_value").value(40.00))
        .andExpect(jsonPath("$.club_id").doesNotExist());
  }

  @Test
  void normalizesNameAndIgnoresSystemFields() throws Exception {
    when(playerService.register(any())).thenReturn(savedPlayer());
    String body =
        withField("name", "\"  Meia   Lua \"")
            .replace("{", "{\"id\": 99, \"base_value\": 1.0, \"condition\": \"DOENTE\",");

    postPlayer(body).andExpect(status().isCreated());

    ArgumentCaptor<PlayerCreateRequest> captor = ArgumentCaptor.forClass(PlayerCreateRequest.class);
    verify(playerService).register(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Meia Lua");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "name|null",
        "name|\"   \"",
        "name|\"Ab\"",
        "name|\"Meia\"",
        "name|\"Meia Lua 2\"",
        "name|\"Meia\\tLua\"",
        "position|\"mei\"",
        "position|null",
        "agility|0",
        "agility|101",
        "agility|\"85\"",
        "agility|85.5",
        "age|15",
        "age|46",
        "pace|0",
        "pace|100",
        "potential|null",
        "potential|81"
      })
  void rejectsInvalidFieldWithoutRegistering(String fieldAndValue) throws Exception {
    String[] parts = fieldAndValue.split("\\|");

    postPlayer(withField(parts[0], parts[1]))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(playerService, never()).register(any());
  }

  @Test
  void rejectsAttributesAboveTheMaximumSum() throws Exception {
    String body =
        VALID_BODY
            .replace("\"pace\": 70", "\"pace\": 99")
            .replace("\"shooting\": 68", "\"shooting\": 99")
            .replace("\"physical\": 64", "\"physical\": 99")
            .replace("\"potential\": 86", "\"potential\": 99");

    postPlayer(body).andExpect(status().isBadRequest());
  }

  @Test
  void requiresPotentialEqualToTheHighestAttributeFromAge33() throws Exception {
    when(playerService.register(any())).thenReturn(savedPlayer());
    String veteran = withField("age", "33");

    postPlayer(veteran).andExpect(status().isBadRequest());
    postPlayer(veteran.replace("\"potential\": 86", "\"potential\": 82"))
        .andExpect(status().isCreated());
  }

  @Test
  void limitsAgilityTo85FromAge35() throws Exception {
    when(playerService.register(any())).thenReturn(savedPlayer());
    String veteran = withField("age", "35").replace("\"potential\": 86", "\"potential\": 82");

    postPlayer(veteran).andExpect(status().isCreated());
    postPlayer(veteran.replace("\"agility\": 85", "\"agility\": 86"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void acceptsIntegerWithZeroFraction() throws Exception {
    when(playerService.register(any())).thenReturn(savedPlayer());

    postPlayer(withField("agility", "85.0")).andExpect(status().isCreated());
  }

  @Test
  void findsActivePlayer() throws Exception {
    when(playerService.findActive(2)).thenReturn(savedPlayer());

    mockMvc
        .perform(get("/api/v1/players/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Meia Lua"))
        .andExpect(jsonPath("$.potential").value(86));
  }

  @Test
  void answersNotFoundForMissingPlayer() throws Exception {
    when(playerService.findActive(7))
        .thenThrow(new ResourceNotFoundException("Jogador 7 não encontrado."));

    mockMvc
        .perform(get("/api/v1/players/7"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Jogador 7 não encontrado."));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"position\": \"MEI\", \"agility\": 70, \"age\": 23}",
        "{\"name\": \"Davi Melo\", \"position\": \"mei\", \"agility\": 70, \"age\": 23}",
        "{\"name\": \"Davi Melo\", \"position\": \"MEI\", \"agility\": 90, \"age\": 36}",
        "{\"name\": \"Davi\", \"position\": \"MEI\", \"agility\": 70, \"age\": 23}",
        "[]"
      })
  void rejectsInvalidEditionWithoutCallingTheService(String body) throws Exception {
    mockMvc
        .perform(put("/api/v1/players/2").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(playerService, never()).edit(anyInt(), any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{}", "{\"condition\": null}", "{\"condition\": \"apto\"}", "10"})
  void rejectsInvalidConditionWithoutCallingTheService(String body) throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/players/2/condition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());

    verify(playerService, never()).changeCondition(anyInt(), any());
  }

  @Test
  void deletesWithNoContent() throws Exception {
    mockMvc
        .perform(delete("/api/v1/players/2"))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    verify(playerService).delete(2);
  }

  @Test
  void rejectsInvalidQueryParamWithoutListing() throws Exception {
    mockMvc
        .perform(get("/api/v1/players").param("sort", "POSITION"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(playerQueryService, never()).list(any());
  }
}
