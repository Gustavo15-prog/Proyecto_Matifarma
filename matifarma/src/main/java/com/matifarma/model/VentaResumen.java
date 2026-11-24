package com.matifarma.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResumen(int id, String numero, LocalDateTime fecha, String cliente, String tipoPago,
                           String vendedor, BigDecimal total) {}
