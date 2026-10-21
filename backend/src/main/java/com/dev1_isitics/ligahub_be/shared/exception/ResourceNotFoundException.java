package com.dev1_isitics.ligahub_be.shared.exception;

import org.springframework.http.HttpStatus;

/** Resource of the URL or of the body that does not exist or was deleted (V8): 404. */
public class ResourceNotFoundException extends ApiException {

  public ResourceNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
