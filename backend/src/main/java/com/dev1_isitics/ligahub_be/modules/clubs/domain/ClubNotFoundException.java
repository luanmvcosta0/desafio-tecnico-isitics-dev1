package com.dev1_isitics.ligahub_be.modules.clubs.domain;

import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;

/** Club of the URL, of the body or of a filter that does not exist (V8): 404. */
public class ClubNotFoundException extends ResourceNotFoundException {

  private static final String MESSAGE = "Clube %d não encontrado.";

  public ClubNotFoundException(Integer clubId) {
    super(MESSAGE.formatted(clubId));
  }
}
