package com.citasalud.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte las excepciones en respuestas HTTP con mensaje en español (RNF_006).
 * Así ningún controller necesita try/catch.
 */
@RestControllerAdvice
public class ManejadorErrores {

    public record ErrorResponse(LocalDateTime fecha, int estado, String error, String mensaje, List<String> detalles) {
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    @ExceptionHandler(PagoRechazadoException.class)
    public ResponseEntity<ErrorResponse> pagoRechazado(PagoRechazadoException e) {
        return responder(HttpStatus.PAYMENT_REQUIRED, e.getMessage(), List.of());
    }

    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> accesoDenegado(RuntimeException e) {
        String mensaje = e instanceof AccesoDenegadoException ? e.getMessage() : "No tienes permiso para esta acción";
        return responder(HttpStatus.FORBIDDEN, mensaje, List.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la petición", detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> peticionMalFormada(Exception e) {
        return responder(HttpStatus.BAD_REQUEST, "La petición está mal formada: revisa los campos, las fechas (AAAA-MM-DD) y los valores permitidos", List.of());
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje, List<String> detalles) {
        ErrorResponse cuerpo = new ErrorResponse(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje, detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
