package com.matifarma.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtenderAlertaRequest(
        @NotNull(message = "Producto inválido") Integer idProducto,
        @NotBlank(message = "Tipo de alerta inválido") String tipo) {}
