package com.matifarma.model;

import java.time.LocalDateTime;

public record MovimientoVista(Integer id, LocalDateTime fecha, String codigo, String producto, String tipo,
                              int cantidad, int stockResultante, String motivo, String referencia, String usuario) {}
