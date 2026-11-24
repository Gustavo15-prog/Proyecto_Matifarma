package com.matifarma.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9\\-]*$", message = "El código solo admite letras, números y guiones")
        String codigo,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,
        @Size(max = 255, message = "La descripción admite hasta 255 caracteres")
        String descripcion,
        @NotNull(message = "Seleccione una categoría")
        Integer idCategoria,
        Integer idProveedor,
        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 2 decimales")
        BigDecimal precioCompra,
        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 2 decimales")
        BigDecimal precioVenta,
        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        @Max(value = 1000000, message = "El stock mínimo es demasiado alto")
        Integer stockMinimo,
        LocalDate fechaVencimiento,
        @Size(max = 60, message = "La ubicación admite hasta 60 caracteres")
        String ubicacion) {}
