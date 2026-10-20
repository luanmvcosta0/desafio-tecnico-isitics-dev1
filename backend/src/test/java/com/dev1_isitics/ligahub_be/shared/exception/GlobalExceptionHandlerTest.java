package com.dev1_isitics.ligahub_be.shared.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.shared.config.JacksonConfig;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ProbeController.class)
@Import({JacksonConfig.class, GlobalExceptionHandlerTest.ProbeController.class})
class GlobalExceptionHandlerTest {

  @RestController
  static class ProbeController {

    record ProbeRequest(Integer age) {}

    @GetMapping("/api/v1/probes/" + PathIds.ID)
    Integer find(@PathVariable Integer id) {
      return id;
    }

    @PostMapping("/api/v1/probes")
    ProbeRequest create(@RequestBody ProbeRequest request) {
      return request;
    }
  }

  @Autowired private MockMvc mockMvc;

  private static void expectError(ResultActions result) throws Exception {
    result
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void acceptsValidPathId() throws Exception {
    mockMvc.perform(get("/api/v1/probes/2147483647")).andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "0", "-1", "1.5", "01", "2147483648", "99999999999"})
  void answersNotFoundForMalformedPathId(String id) throws Exception {
    ResultActions result = mockMvc.perform(get("/api/v1/probes/" + id));

    result.andExpect(status().isNotFound());
    expectError(result);
  }

  @Test
  void answersNotFoundForUnknownPath() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/v1/inexistente"));

    result.andExpect(status().isNotFound());
    expectError(result);
  }

  @Test
  void doesNotMatchTrailingSlash() throws Exception {
    mockMvc.perform(get("/api/v1/probes/1/")).andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{invalid", "[]", "10", "{\"age\": \"15\"}", "{\"age\": 50.5}"})
  void answersBadRequestForInvalidBody(String body) throws Exception {
    ResultActions result =
        mockMvc.perform(
            post("/api/v1/probes").contentType(MediaType.APPLICATION_JSON).content(body));

    result.andExpect(status().isBadRequest());
    expectError(result);
  }
}
