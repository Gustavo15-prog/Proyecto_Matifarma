package com.matifarma.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record VentaRequest(
        @NotBlank(message = "Seleccione el tipo de pago") String tipoPago,
        @Size(max = 15, message = "El documento del cliente admite hasta 15 caracteres") String clienteDocumento,
        @Size(max = 120, message = "El nombre del cliente admite hasta 120 caracteres") String clienteNombre,
        @NotBlank(message = "Token de operación ausente") @Size(max = 40) String token,
        @NotEmpty(message = "La venta no tiene productos") @Valid List<VentaDetalleRequest> detalles) {}
