package com.matifarma.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaDia(LocalDate fecha, BigDecimal total) {}
