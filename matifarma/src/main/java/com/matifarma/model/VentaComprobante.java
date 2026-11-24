package com.matifarma.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaComprobante(int id, String numero, LocalDateTime fecha, String cliente, String documento,
                               String tipoPago, String vendedor, BigDecimal total, List<LineaComprobante> lineas) {}
