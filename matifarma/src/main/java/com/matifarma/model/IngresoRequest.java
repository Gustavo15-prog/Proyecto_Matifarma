package com.matifarma.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record IngresoRequest(
        @NotNull(message = "Seleccione un proveedor") Integer idProveedor,
        @NotBlank(message = "El número de guía o factura es obligatorio")
        @Size(max = 30, message = "El número de documento admite hasta 30 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9\\-/ ]*$", message = "El documento solo admite letras, números, guiones y barras")
        String numeroDocumento,
        @NotBlank(message = "Token de operación ausente") @Size(max = 40) String token,
        @NotEmpty(message = "Agregue al menos un producto al ingreso") @Valid List<IngresoDetalle> detalles) {}
