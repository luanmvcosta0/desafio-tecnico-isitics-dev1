package com.dev1_isitics.ligahub_be.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS only for the frontend origin informed by {@code CORS_ALLOWED_ORIGIN} ("*" is forbidden). */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final String allowedOrigin;

  public CorsConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
    this.allowedOrigin = allowedOrigin;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigin)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .exposedHeaders(HttpHeaders.LOCATION, HttpHeaders.CONTENT_DISPOSITION);
  }
}
