package com.matifarma.model;

import java.time.LocalDateTime;

public record EmpleadoAuth(Integer id, String usuario, String nombres, String passwordHash, String rol,
                           boolean activo, int intentos, LocalDateTime bloqueadoHasta) {}
