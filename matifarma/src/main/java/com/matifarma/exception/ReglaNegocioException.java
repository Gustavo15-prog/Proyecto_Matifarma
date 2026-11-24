package com.matifarma.exception;

import org.springframework.http.HttpStatus;

/** Error de regla de negocio o validación del servidor. */
public class ReglaNegocioException extends RuntimeException {
    private final HttpStatus estado;
    private final String campo;

    public ReglaNegocioException(String mensaje, HttpStatus estado, String campo) {
        super(mensaje);
        this.estado = estado;
        this.campo = campo;
    }

    public static ReglaNegocioException invalido(String mensaje) { return new ReglaNegocioException(mensaje, HttpStatus.BAD_REQUEST, null); }
    public static ReglaNegocioException invalido(String campo, String mensaje) { return new ReglaNegocioException(mensaje, HttpStatus.BAD_REQUEST, campo); }
    public static ReglaNegocioException conflicto(String mensaje) { return new ReglaNegocioException(mensaje, HttpStatus.CONFLICT, null); }
    public static ReglaNegocioException conflicto(String campo, String mensaje) { return new ReglaNegocioException(mensaje, HttpStatus.CONFLICT, campo); }
    public static ReglaNegocioException noEncontrado(String mensaje) { return new ReglaNegocioException(mensaje, HttpStatus.NOT_FOUND, null); }

    public HttpStatus getEstado() { return estado; }
    public String getCampo() { return campo; }
}
