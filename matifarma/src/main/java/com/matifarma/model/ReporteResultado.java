package com.matifarma.model;

import java.util.List;
import java.util.Map;

public record ReporteResultado(String titulo, List<String> columnas, List<List<Object>> filas, Map<String, Object> resumen) {}
