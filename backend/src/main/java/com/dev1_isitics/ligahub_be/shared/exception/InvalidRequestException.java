package com.dev1_isitics.ligahub_be.shared.exception;

import org.springframework.http.HttpStatus;

/** Invalid data, format or parameter not covered by Bean Validation (V9): 400. */
public class InvalidRequestException extends ApiException {

  public InvalidRequestException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
