package com.matifarma.dao;

import com.matifarma.model.ReporteResultado;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReporteDao extends BaseDao {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReporteDao(JdbcTemplate jdbc) { super(jdbc); }

    private static Object celda(Object o) {
        if (o == null) return "";
        if (o instanceof Timestamp t) return t.toLocalDateTime().format(FECHA_HORA);
        if (o instanceof java.sql.Date d) return d.toLocalDate().format(FECHA);
        if (o instanceof BigDecimal b) return b.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return o;
    }

    private List<List<Object>> filas(String sql, Object... args) {
        return jdbc.query(sql, (ResultSetExtractor<List<List<Object>>>) rs -> {
            List<List<Object>> out = new ArrayList<>();
            int n = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                List<Object> fila = new ArrayList<>(n);
                for (int c = 1; c <= n; c++) fila.add(celda(rs.getObject(c)));
                out.add(fila);
            }
            return out;
        }, args);
    }

    public ReporteResultado stock(LocalDate hoy) {
        List<List<Object>> filas = filas(
                "SELECT p.codigo, p.nombre, c.nombre AS categoria, i.cantidad AS stock, p.stock_minimo, p.precio_compra, "
              + "p.precio_venta, p.fecha_vencimiento, CASE WHEN p.activo = 0 THEN 'Inactivo' "
              + "WHEN p.fecha_vencimiento < ? AND i.cantidad > 0 THEN 'Vencido' WHEN i.cantidad = 0 THEN 'Agotado' "
              + "WHEN i.cantidad <= p.stock_minimo THEN 'Stock bajo' ELSE 'Normal' END AS estado "
              + "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria "
              + "JOIN inventario i ON i.id_producto = p.id_producto ORDER BY p.nombre", Db.d(hoy));
        Map<String, Object> resumen = new LinkedHashMap<>();
        Map<String, Object> tot = jdbc.queryForMap("SELECT COUNT(*) AS productos, ISNULL(SUM(i.cantidad), 0) AS unidades, "
                + "ISNULL(SUM(i.cantidad * p.precio_compra), 0) AS valor FROM producto p JOIN inventario i ON i.id_producto = p.id_producto "
                + "WHERE p.activo = 1");
        resumen.put("Productos activos", tot.get("productos"));
        resumen.put("Unidades en stock", tot.get("unidades"));
        resumen.put("Valor del inventario (S/)", celda(tot.get("valor")));
        return new ReporteResultado("Reporte de stock actual",
                List.of("Código", "Producto", "Categoría", "Stock", "Stock mínimo", "P. compra", "P. venta", "Vencimiento", "Estado"),
                filas, resumen);
    }

    public ReporteResultado ventas(LocalDate desde, LocalDate hasta) {
        Timestamp d = Db.ts(desde.atStartOfDay());
        Timestamp h = Db.ts(hasta.plusDays(1).atStartOfDay());
        List<List<Object>> filas = filas(
                "SELECT 'V-' + RIGHT('000000' + CAST(v.id_venta AS VARCHAR(10)), 6) AS numero, v.fecha, "
              + "ISNULL(c.nombre, N'Cliente general') AS cliente, v.tipo_pago, e.nombres AS vendedor, v.total "
              + "FROM venta v LEFT JOIN cliente c ON c.id_cliente = v.id_cliente "
              + "JOIN empleado e ON e.id_empleado = v.id_empleado WHERE v.fecha >= ? AND v.fecha < ? ORDER BY v.fecha DESC", d, h);
        Map<String, Object> tot = jdbc.queryForMap("SELECT COUNT(*) AS cantidad, ISNULL(SUM(total), 0) AS monto FROM venta "
                + "WHERE fecha >= ? AND fecha < ?", d, h);
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("Ventas registradas", tot.get("cantidad"));
        resumen.put("Monto total (S/)", celda(tot.get("monto")));
        return new ReporteResultado("Reporte de ventas", List.of("N.º", "Fecha", "Cliente", "Tipo de pago", "Vendedor", "Total (S/)"), filas, resumen);
    }

    public ReporteResultado movimientos(LocalDate desde, LocalDate hasta) {
        Timestamp d = Db.ts(desde.atStartOfDay());
        Timestamp h = Db.ts(hasta.plusDays(1).atStartOfDay());
        List<List<Object>> filas = filas(
                "SELECT m.fecha, p.codigo, p.nombre, m.tipo, m.cantidad, m.stock_resultante, m.motivo, "
              + "ISNULL(m.referencia, '') AS referencia, e.nombres AS usuario FROM movimiento_inventario m "
              + "JOIN producto p ON p.id_producto = m.id_producto JOIN empleado e ON e.id_empleado = m.id_empleado "
              + "WHERE m.fecha >= ? AND m.fecha < ? ORDER BY m.fecha DESC, m.id_movimiento DESC", d, h);
        Map<String, Object> tot = jdbc.queryForMap("SELECT ISNULL(SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE 0 END), 0) AS entradas, "
                + "ISNULL(SUM(CASE WHEN tipo = 'SALIDA' THEN cantidad ELSE 0 END), 0) AS salidas, COUNT(*) AS movimientos "
                + "FROM movimiento_inventario WHERE fecha >= ? AND fecha < ?", d, h);
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("Movimientos", tot.get("movimientos"));
        resumen.put("Unidades que entraron", tot.get("entradas"));
        resumen.put("Unidades que salieron", tot.get("salidas"));
        return new ReporteResultado("Reporte de movimientos de inventario",
                List.of("Fecha", "Código", "Producto", "Tipo", "Cantidad", "Stock resultante", "Motivo", "Referencia", "Usuario"), filas, resumen);
    }
}
