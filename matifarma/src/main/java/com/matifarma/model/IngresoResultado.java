package com.matifarma.model;

import java.math.BigDecimal;
import java.util.List;

public record IngresoResultado(int idCompra, int lineasIngresadas, BigDecimal total, List<String> noConformes) {}
