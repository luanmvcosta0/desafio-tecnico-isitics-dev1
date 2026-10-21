package com.dev1_isitics.ligahub_be.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Refusal raised by the services; the message is returned as is in {@link ErrorResponse}. */
@Getter
public abstract class ApiException extends RuntimeException {

  private final HttpStatus status;

  protected ApiException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
