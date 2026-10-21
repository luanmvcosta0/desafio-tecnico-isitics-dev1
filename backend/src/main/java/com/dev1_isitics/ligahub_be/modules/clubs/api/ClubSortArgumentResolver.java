package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.application.ClubSort;
import com.dev1_isitics.ligahub_be.shared.web.QueryParams;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Reads the single {@code sort} of {@code GET /clubs}: absent is "name", anything else is 400. */
public class ClubSortArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String SORT = "sort";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(ClubSort.class);
  }

  @Override
  public ClubSort resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    return QueryParams.of(webRequest)
        .text(SORT)
        .map(
            sort -> ClubSort.fromParameter(sort).orElseThrow(() -> QueryParams.invalid(SORT, sort)))
        .orElse(ClubSort.NAME);
  }
}
