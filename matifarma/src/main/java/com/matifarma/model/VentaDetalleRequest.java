package com.matifarma.model;

import jakarta.validation.constraints.*;

public record VentaDetalleRequest(
        @NotNull(message = "Producto inválido") Integer idProducto,
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        @Max(value = 10000, message = "La cantidad es demasiado alta")
        Integer cantidad) {}
