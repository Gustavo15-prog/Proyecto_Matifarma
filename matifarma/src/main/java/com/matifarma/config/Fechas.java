package com.matifarma.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Fecha y hora del negocio (zona horaria de Perú). */
public final class Fechas {
    public static final ZoneId ZONA = ZoneId.of("America/Lima");
    private Fechas() {}

    public static LocalDate hoy() { return LocalDate.now(ZONA); }
    public static LocalDateTime ahora() { return LocalDateTime.now(ZONA); }
}
