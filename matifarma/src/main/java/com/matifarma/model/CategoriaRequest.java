package com.matifarma.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
        String nombre) {}
