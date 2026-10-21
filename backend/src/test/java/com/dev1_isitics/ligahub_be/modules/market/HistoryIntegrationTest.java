package com.dev1_isitics.ligahub_be.modules.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dev1_isitics.ligahub_be.support.PlayerRequests;
import com.dev1_isitics.ligahub_be.support.PostgresIntegrationTest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class HistoryIntegrationTest extends PostgresIntegrationTest {

  private static final String INSERT =
      "INSERT INTO transacoes (clube_id, jogador_id, nome_jogador, tipo, valor, taxa, multa, lucro,"
          + " variacao_caixa, criado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::timestamptz)";

  @Autowired private MockMvc mockMvc;

  private int meiaLua;
  private int zeFoguete;

  @BeforeEach
  void recordTransactions() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/clubs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Águias FC\", \"acronym\": \"AGU\"}"))
        .andExpect(status().isCreated());
    meiaLua = PlayerRequests.register(mockMvc, "Meia Lua", "MEI", 25);
    zeFoguete = PlayerRequests.register(mockMvc, "Zé Foguete Lima", "ATA", 25);
    jdbcTemplate.update(
        INSERT,
        1,
        meiaLua,
        "Meia Lua",
        "COMPRA",
        37,
        1.85,
        0,
        null,
        -38.85,
        "2026-11-02T10:15:00-03:00");
    jdbcTemplate.update(
        INSERT,
        1,
        meiaLua,
        "Meia Lua",
        "VENDA",
        40,
        0.30,
        4,
        3,
        35.70,
        "2026-11-05T16:40:00-03:00");
    jdbcTemplate.update(
        INSERT,
        1,
        zeFoguete,
        "Zé \"Foguete\"; Lima",
        "COMPRA",
        20,
        0,
        0,
        null,
        -20,
        "2026-11-05T16:40:00-03:00");
  }

  @Test
  void listsMostRecentFirstWithTheSummary() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].id", contains(3, 2, 1)))
        .andExpect(jsonPath("$.data[1].created_at").value("2026-11-05T16:40:00-03:00"))
        .andExpect(jsonPath("$.data[1].profit").value(3.00))
        .andExpect(jsonPath("$.data[2].profit").isEmpty())
        .andExpect(jsonPath("$.summary.total_spent").value(58.85))
        .andExpect(jsonPath("$.summary.total_received").value(35.70))
        .andExpect(jsonPath("$.summary.total_fees").value(2.15))
        .andExpect(jsonPath("$.summary.total_penalties").value(4.00))
        .andExpect(jsonPath("$.summary.total_profit").value(3.00));
  }

  @Test
  void appliesTheFiltersToDataAndSummary() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions?type=COMPRA&player_id=" + meiaLua))
        .andExpect(jsonPath("$.data[*].id", contains(1)))
        .andExpect(jsonPath("$.summary.total_spent").value(38.85))
        .andExpect(jsonPath("$.summary.total_received").value(0.00))
        .andExpect(jsonPath("$.summary.total_profit").value(0.00));
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions?type=VENDA,COMPRA"))
        .andExpect(jsonPath("$.data.length()").value(3));
  }

  @Test
  void answersEmptyForAPlayerWithoutTransactionsInTheClub() throws Exception {
    int other = PlayerRequests.register(mockMvc, "Outro Jogador", "GOL", 25);

    mockMvc
        .perform(get("/api/v1/clubs/1/transactions?player_id=" + other))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.summary.total_spent").value(0.00));
  }

  @Test
  void keepsTransactionsOfDeletedPlayersButRefusesThemAsFilter() throws Exception {
    jdbcTemplate.update("UPDATE jogadores SET excluido_em = now() WHERE id = ?", meiaLua);

    mockMvc
        .perform(get("/api/v1/clubs/1/transactions"))
        .andExpect(jsonPath("$.data.length()").value(3));
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions?player_id=" + meiaLua))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"type=", "type=venda", "type=COMPRA,COMPRA", "player_id=0"})
  void refusesInvalidFiltersInBothEndpoints(String query) throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions?" + query))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions/export?" + query))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  void answersNotFoundForUnknownClubInJson() throws Exception {
    mockMvc.perform(get("/api/v1/clubs/9/transactions")).andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/v1/clubs/9/transactions/export"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  void exportsTheCsvByteByByte() throws Exception {
    byte[] csv =
        mockMvc
            .perform(get("/api/v1/clubs/1/transactions/export"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    assertThat(new String(csv, StandardCharsets.UTF_8))
        .isEqualTo(
            "data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa\n"
                + "2026-11-05T16:40:00-03:00;COMPRA;\"Zé \"\"Foguete\"\"; Lima\";"
                + "20,00;0,00;0,00;;-20,00\n"
                + "2026-11-05T16:40:00-03:00;VENDA;Meia Lua;40,00;0,30;4,00;3,00;35,70\n"
                + "2026-11-02T10:15:00-03:00;COMPRA;Meia Lua;37,00;1,85;0,00;;-38,85\n");
    assertThat(csv[0]).isEqualTo((byte) 'd');
  }

  @Test
  void exportsOnlyTheHeaderWhenTheFilterMatchesNothing() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/1/transactions/export?type=VENDA&player_id=" + zeFoguete))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .bytes(
                    "data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa\n"
                        .getBytes(StandardCharsets.UTF_8)));
  }
}
