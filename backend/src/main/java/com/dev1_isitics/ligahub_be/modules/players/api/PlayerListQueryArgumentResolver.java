package com.dev1_isitics.ligahub_be.modules.players.api;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerListQuery;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Hands a validated {@link PlayerListQuery} to the controller, parsed from the raw query params.
 */
public class PlayerListQueryArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(PlayerListQuery.class);
  }

  @Override
  public PlayerListQuery resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    Map<String, String> firstValues =
        webRequest.getParameterMap().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
    return PlayerListQueryParser.parse(firstValues);
  }
}
