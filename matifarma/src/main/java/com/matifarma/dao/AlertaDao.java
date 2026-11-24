package com.matifarma.dao;

import com.matifarma.model.AlertaVista;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AlertaDao extends BaseDao {
    private static final String PARTE =
            "SELECT p.id_producto, p.codigo, p.nombre, '%1$s' AS tipo, i.cantidad AS stock, p.stock_minimo, "
          + "p.fecha_vencimiento, pr.razon_social AS proveedor FROM producto p "
          + "JOIN inventario i ON i.id_producto = p.id_producto "
          + "LEFT JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor "
          + "WHERE p.activo = 1 AND %2$s AND NOT EXISTS (SELECT 1 FROM alerta_atendida a WHERE a.id_producto = p.id_producto "
          + "AND a.tipo = '%1$s' AND a.fecha_atencion >= i.fecha_actualizacion)";

    public AlertaDao(JdbcTemplate jdbc) { super(jdbc); }

    /** Alertas vigentes: stock bajo, productos vencidos con stock y productos próximos a vencer. */
    public List<AlertaVista> listar(LocalDate hoy, LocalDate limiteVencimiento) {
        String sql = "SELECT * FROM ("
                + String.format(PARTE, "STOCK_BAJO", "i.cantidad <= p.stock_minimo")
                + " UNION ALL "
                + String.format(PARTE, "VENCIDO", "p.fecha_vencimiento IS NOT NULL AND p.fecha_vencimiento < ? AND i.cantidad > 0")
                + " UNION ALL "
                + String.format(PARTE, "POR_VENCER", "p.fecha_vencimiento IS NOT NULL AND p.fecha_vencimiento >= ? AND p.fecha_vencimiento <= ? AND i.cantidad > 0")
                + ") t ORDER BY CASE tipo WHEN 'VENCIDO' THEN 0 WHEN 'STOCK_BAJO' THEN 1 ELSE 2 END, nombre";
        return jdbc.query(sql, (rs, i) -> new AlertaVista(rs.getInt("id_producto"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getString("tipo"), rs.getInt("stock"), rs.getInt("stock_minimo"),
                Db.date(rs, "fecha_vencimiento"), rs.getString("proveedor")),
                Db.d(hoy), Db.d(hoy), Db.d(limiteVencimiento));
    }

    public void atender(int idProducto, String tipo, int idEmpleado, LocalDateTime ahora) {
        jdbc.update("INSERT INTO alerta_atendida(id_producto, tipo, id_empleado, fecha_atencion) VALUES (?,?,?,?)",
                idProducto, tipo, idEmpleado, Db.ts(ahora));
    }
}
