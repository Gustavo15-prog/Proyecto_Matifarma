package com.matifarma.model;

import java.math.BigDecimal;

public record LineaComprobante(String codigo, String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
