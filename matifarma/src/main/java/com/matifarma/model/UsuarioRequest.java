package com.matifarma.model;

import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(min = 4, max = 30, message = "El usuario debe tener entre 4 y 30 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._]*$", message = "El usuario solo admite letras, números, punto y guion bajo")
        String usuario,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Ingrese un correo electrónico válido")
        @Size(max = 120, message = "El correo admite hasta 120 caracteres")
        String correo,
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 3, max = 120, message = "Los nombres deben tener entre 3 y 120 caracteres")
        String nombres,
        @NotBlank(message = "Seleccione un rol") String rol,
        @Size(max = 64, message = "La contraseña admite hasta 64 caracteres") String password,
        String confirmarPassword) {}
