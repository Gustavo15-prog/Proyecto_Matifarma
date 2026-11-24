package com.matifarma.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones de la API en respuestas JSON {mensaje, errores}. */
@RestControllerAdvice(basePackages = "com.matifarma.controller")
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> negocio(ReglaNegocioException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (ex.getCampo() != null) errores.put(ex.getCampo(), ex.getMessage());
        return respuesta(ex.getEstado(), ex.getMessage(), errores);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        String primero = errores.values().stream().findFirst().orElse("Datos inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, "Revise los datos ingresados: " + primero, errores);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> formato(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Formato de datos inválido (revise fechas y números).", Map.of());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, Object>> duplicado(DuplicateKeyException ex) {
        log.warn("Registro duplicado: {}", ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "El registro ya existe o la operación ya fue procesada.", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generico(Exception ex) throws Exception {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) throw ex;
        log.error("Error no controlado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intente nuevamente.", Map.of());
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String mensaje, Map<String, String> errores) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("errores", errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
