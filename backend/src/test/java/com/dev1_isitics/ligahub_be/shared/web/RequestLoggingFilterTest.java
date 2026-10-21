package com.dev1_isitics.ligahub_be.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {

  private final RequestLoggingFilter filter = new RequestLoggingFilter();

  @Test
  void logsMethodPathAndStatusInOneLine(CapturedOutput output) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/players");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain =
        (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_NOT_FOUND);

    filter.doFilter(request, response, chain);

    assertThat(output.getOut())
        .containsOnlyOnce("RequestLoggingFilter")
        .contains("GET /api/v1/players 404");
  }
}
