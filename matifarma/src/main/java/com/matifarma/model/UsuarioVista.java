package com.matifarma.model;

public record UsuarioVista(Integer id, String usuario, String correo, String nombres, String rol,
                           boolean activo, boolean bloqueado, int intentosFallidos) {}
