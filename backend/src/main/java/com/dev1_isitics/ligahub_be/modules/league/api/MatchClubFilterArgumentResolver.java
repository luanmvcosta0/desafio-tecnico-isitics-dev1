package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.shared.web.QueryParams;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Reads {@code club_id} with the integer rule of the query params (invalid or empty: 400). */
public class MatchClubFilterArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String CLUB_ID = "club_id";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(MatchClubFilter.class);
  }

  @Override
  public MatchClubFilter resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    return new MatchClubFilter(QueryParams.of(webRequest).positiveInteger(CLUB_ID).orElse(null));
  }
}
