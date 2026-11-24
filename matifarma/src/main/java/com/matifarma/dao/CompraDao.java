package com.matifarma.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public class CompraDao extends BaseDao {
    public CompraDao(JdbcTemplate jdbc) { super(jdbc); }

    public boolean existeToken(String token) { return existe("SELECT 1 AS x FROM compra WHERE token = ?", token); }

    public boolean existeDocumento(int idProveedor, String numero) {
        return existe("SELECT 1 AS x FROM compra WHERE id_proveedor = ? AND numero_documento = ?", idProveedor, numero);
    }

    public int insertar(int idProveedor, int idEmpleado, String numero, LocalDateTime fecha, BigDecimal total, String token) {
        return insertar("INSERT INTO compra(id_proveedor, id_empleado, numero_documento, fecha, total, token) VALUES (?,?,?,?,?,?)",
                idProveedor, idEmpleado, numero, Db.ts(fecha), total, token);
    }

    public void insertarDetalle(int idCompra, int idProducto, int cantidad, BigDecimal costo, BigDecimal subtotal,
                                LocalDate vencimiento, boolean conforme, String observacion) {
        jdbc.update("INSERT INTO detalle_compra(id_compra, id_producto, cantidad, costo_unitario, subtotal, "
                        + "fecha_vencimiento, conforme, observacion) VALUES (?,?,?,?,?,?,?,?)",
                idCompra, idProducto, cantidad, costo, subtotal, Db.d(vencimiento), conforme, observacion);
    }
}
