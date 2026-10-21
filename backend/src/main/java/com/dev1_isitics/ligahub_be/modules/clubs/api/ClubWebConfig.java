package com.dev1_isitics.ligahub_be.modules.clubs.api;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ClubWebConfig implements WebMvcConfigurer {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new ClubSortArgumentResolver());
  }
}
