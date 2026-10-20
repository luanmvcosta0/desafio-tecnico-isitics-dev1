package com.dev1_isitics.ligahub_be.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String INVALID_BODY_MESSAGE =
      "O corpo da requisição deve ser um objeto JSON válido, com cada campo no tipo esperado.";
  private static final String NOT_FOUND_MESSAGE = "Recurso não encontrado.";
  private static final String INVALID_PARAMETER_MESSAGE = "Parâmetro inválido: %s.";
  private static final String METHOD_NOT_ALLOWED_MESSAGE = "Método não permitido neste recurso.";
  private static final String UNEXPECTED_ERROR_MESSAGE =
      "Erro inesperado. Nenhuma alteração foi gravada.";

  @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
  public ResponseEntity<ErrorResponse> handleUnreadableBody(Exception exception) {
    return respond(HttpStatus.BAD_REQUEST, INVALID_BODY_MESSAGE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse(INVALID_BODY_MESSAGE);
    return respond(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException exception) {
    if (exception.getParameter().hasParameterAnnotation(PathVariable.class)) {
      return respond(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE);
    }
    return respond(
        HttpStatus.BAD_REQUEST, INVALID_PARAMETER_MESSAGE.formatted(exception.getName()));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException exception) {
    return respond(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException exception) {
    return respond(HttpStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED_MESSAGE);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
    log.error("Unexpected error while handling request", exception);
    return respond(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_MESSAGE);
  }

  private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(message));
  }
}
