package com.dev1_isitics.ligahub_be.shared.exception;

/** Body of every refused or failed request: always a non-empty message and never a stack trace. */
public record ErrorResponse(String message) {}
