package com.matifarma.dao;

import com.matifarma.model.LineaComprobante;
import com.matifarma.model.VentaComprobante;
import com.matifarma.model.VentaResumen;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class VentaDao extends BaseDao {
    public VentaDao(JdbcTemplate jdbc) { super(jdbc); }

    public static String numero(int id) { return String.format("V-%06d", id); }

    public boolean existeToken(String token) { return existe("SELECT 1 AS x FROM venta WHERE token = ?", token); }

    public int insertar(LocalDateTime fecha, Integer idCliente, int idEmpleado, String tipoPago, BigDecimal total, String token) {
        return insertar("INSERT INTO venta(fecha, id_cliente, id_empleado, tipo_pago, total, estado, token) VALUES (?,?,?,?,?,'COMPLETADA',?)",
                Db.ts(fecha), idCliente, idEmpleado, tipoPago, total, token);
    }

    public void insertarDetalle(int idVenta, int idProducto, int cantidad, BigDecimal precio, BigDecimal subtotal) {
        jdbc.update("INSERT INTO detalle_venta(id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)",
                idVenta, idProducto, cantidad, precio, subtotal);
    }

    public Optional<Integer> idEmpleadoDeVenta(int idVenta) {
        return jdbc.query("SELECT id_empleado FROM venta WHERE id_venta = ?", (rs, i) -> rs.getInt(1), idVenta)
                .stream().findFirst();
    }

    public Optional<VentaComprobante> comprobante(int id) {
        List<LineaComprobante> lineas = jdbc.query(
                "SELECT p.codigo, p.nombre, d.cantidad, d.precio_unitario, d.subtotal FROM detalle_venta d "
              + "JOIN producto p ON p.id_producto = d.id_producto WHERE d.id_venta = ? ORDER BY d.id_detalle",
                (rs, i) -> new LineaComprobante(rs.getString("codigo"), rs.getString("nombre"), rs.getInt("cantidad"),
                        rs.getBigDecimal("precio_unitario"), rs.getBigDecimal("subtotal")), id);
        return jdbc.query("SELECT v.id_venta, v.fecha, v.tipo_pago, v.total, c.nombre AS cliente, c.documento, e.nombres AS vendedor "
                        + "FROM venta v LEFT JOIN cliente c ON c.id_cliente = v.id_cliente "
                        + "JOIN empleado e ON e.id_empleado = v.id_empleado WHERE v.id_venta = ?",
                (rs, i) -> new VentaComprobante(rs.getInt("id_venta"), numero(rs.getInt("id_venta")),
                        Db.dateTime(rs, "fecha"), rs.getString("cliente") == null ? "Cliente general" : rs.getString("cliente"),
                        rs.getString("documento"), rs.getString("tipo_pago"), rs.getString("vendedor"),
                        rs.getBigDecimal("total"), lineas), id).stream().findFirst();
    }

    /** Ventas recientes; si idEmpleado no es nulo, solo las de ese empleado. */
    public List<VentaResumen> recientes(int limite, Integer idEmpleado) {
        String filtro = idEmpleado == null ? "" : " WHERE v.id_empleado = ?";
        String sql = "SELECT TOP " + limite + " v.id_venta, v.fecha, v.tipo_pago, v.total, c.nombre AS cliente, e.nombres AS vendedor "
                + "FROM venta v LEFT JOIN cliente c ON c.id_cliente = v.id_cliente "
                + "JOIN empleado e ON e.id_empleado = v.id_empleado" + filtro + " ORDER BY v.fecha DESC, v.id_venta DESC";
        Object[] args = idEmpleado == null ? new Object[0] : new Object[]{idEmpleado};
        return jdbc.query(sql, (rs, i) -> new VentaResumen(rs.getInt("id_venta"), numero(rs.getInt("id_venta")),
                Db.dateTime(rs, "fecha"), rs.getString("cliente") == null ? "Cliente general" : rs.getString("cliente"),
                rs.getString("tipo_pago"), rs.getString("vendedor"), rs.getBigDecimal("total")), args);
    }
}
