package com.matifarma.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoDetalle(
        @NotNull(message = "Seleccione un producto") Integer idProducto,
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        @Max(value = 1000000, message = "La cantidad es demasiado alta")
        Integer cantidad,
        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.01", message = "El costo debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El costo admite máximo 2 decimales")
        BigDecimal costoUnitario,
        LocalDate fechaVencimiento) {}
