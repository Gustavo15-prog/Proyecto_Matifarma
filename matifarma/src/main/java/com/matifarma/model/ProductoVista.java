package com.matifarma.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoVista(Integer id, String codigo, String nombre, String descripcion,
                            Integer idCategoria, String categoria, Integer idProveedor, String proveedor,
                            BigDecimal precioCompra, BigDecimal precioVenta, int stock, int stockMinimo,
                            LocalDate fechaVencimiento, String ubicacion, boolean activo) {

    /** Oculta el precio de compra para roles que no son administrador. */
    public ProductoVista sinCosto() {
        return new ProductoVista(id, codigo, nombre, descripcion, idCategoria, categoria, idProveedor, proveedor,
                null, precioVenta, stock, stockMinimo, fechaVencimiento, ubicacion, activo);
    }
}
