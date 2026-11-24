package com.matifarma.model;

import java.time.LocalDate;

public record AlertaVista(Integer idProducto, String codigo, String nombre, String tipo, int stock,
                          int stockMinimo, LocalDate fechaVencimiento, String proveedor) {}
