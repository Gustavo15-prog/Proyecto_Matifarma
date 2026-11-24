package com.matifarma.model;

import jakarta.validation.constraints.*;

public record Proveedor(
        Integer id,
        @NotBlank(message = "La razón social es obligatoria")
        @Size(min = 3, max = 150, message = "La razón social debe tener entre 3 y 150 caracteres")
        String razonSocial,
        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
        String ruc,
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[+]?[0-9 \\-]{7,20}$", message = "Teléfono inválido (7 a 20 dígitos; se permiten +, espacios y guiones)")
        String telefono,
        @Size(max = 120, message = "El correo admite hasta 120 caracteres")
        @Email(message = "Ingrese un correo electrónico válido")
        String correo,
        @Size(max = 200, message = "La dirección admite hasta 200 caracteres")
        String direccion,
        Boolean activo) {}
