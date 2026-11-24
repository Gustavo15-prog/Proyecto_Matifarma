package com.matifarma.model;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull(message = "Estado requerido") Boolean activo) {}
