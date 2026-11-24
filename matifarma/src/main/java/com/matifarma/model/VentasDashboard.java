package com.matifarma.model;

import java.math.BigDecimal;
import java.util.List;

public record VentasDashboard(int cantidadHoy, BigDecimal totalHoy, BigDecimal totalMes, List<VentaDia> ultimosDias) {}
