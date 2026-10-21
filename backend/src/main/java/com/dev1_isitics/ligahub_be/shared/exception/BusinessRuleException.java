package com.dev1_isitics.ligahub_be.shared.exception;

import org.springframework.http.HttpStatus;

/** Business rule that depends on the current state and was violated (V9): 409. */
public class BusinessRuleException extends ApiException {

  public BusinessRuleException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
