package com.dev1_isitics.ligahub_be.shared.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
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
  private static final String INVALID_FIELD_MESSAGE =
      "O campo %s está em formato inválido ou fora do tipo esperado.";
  private static final String NOT_FOUND_MESSAGE = "Recurso não encontrado.";
  private static final String INVALID_PARAMETER_MESSAGE = "Parâmetro inválido: %s.";
  private static final String METHOD_NOT_ALLOWED_MESSAGE = "Método não permitido neste recurso.";
  private static final String UNEXPECTED_ERROR_MESSAGE =
      "Erro inesperado. Nenhuma alteração foi gravada.";

  private static final PropertyNamingStrategies.NamingBase JSON_NAMES =
      new PropertyNamingStrategies.SnakeCaseStrategy();

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
    return respond(exception.getStatus(), exception.getMessage());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException exception) {
    return respond(HttpStatus.BAD_REQUEST, INVALID_BODY_MESSAGE);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(
      HttpMessageNotReadableException exception) {
    if (exception.getCause() instanceof JsonMappingException mappingException
        && !mappingException.getPath().isEmpty()) {
      return respond(
          HttpStatus.BAD_REQUEST, INVALID_FIELD_MESSAGE.formatted(fieldPath(mappingException)));
    }
    return respond(HttpStatus.BAD_REQUEST, INVALID_BODY_MESSAGE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(GlobalExceptionHandler::describe)
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

  private static String fieldPath(JsonMappingException exception) {
    return exception.getPath().stream()
        .map(
            reference ->
                reference.getFieldName() != null
                    ? reference.getFieldName()
                    : String.valueOf(reference.getIndex()))
        .collect(Collectors.joining("."));
  }

  private static String describe(ObjectError error) {
    if (error instanceof FieldError fieldError) {
      return JSON_NAMES.translate(fieldError.getField()) + ": " + fieldError.getDefaultMessage();
    }
    return error.getDefaultMessage();
  }

  private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ErrorResponse(message));
  }
}
