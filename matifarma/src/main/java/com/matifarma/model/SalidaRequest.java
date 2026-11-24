package com.matifarma.model;

import jakarta.validation.constraints.*;

public record SalidaRequest(
        @NotNull(message = "Seleccione un producto") Integer idProducto,
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        @Max(value = 1000000, message = "La cantidad es demasiado alta")
        Integer cantidad,
        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 100, message = "El motivo admite hasta 100 caracteres")
        String motivo,
        @NotBlank(message = "Token de operación ausente") @Size(max = 40) String token) {}
