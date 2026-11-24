package com.matifarma.dao;

import com.matifarma.model.MovimientoVista;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InventarioDao extends BaseDao {
    public InventarioDao(JdbcTemplate jdbc) { super(jdbc); }

    public void crearRegistro(int idProducto, String ubicacion, LocalDateTime ahora) {
        jdbc.update("INSERT INTO inventario(id_producto, cantidad, ubicacion, fecha_actualizacion) VALUES (?, 0, ?, ?)",
                idProducto, ubicacion, Db.ts(ahora));
    }

    public void actualizarUbicacion(int idProducto, String ubicacion) {
        jdbc.update("UPDATE inventario SET ubicacion = ? WHERE id_producto = ?", ubicacion, idProducto);
    }

    /** Suma stock de forma atómica y devuelve el nuevo stock. */
    public Optional<Integer> incrementar(int idProducto, int cantidad, LocalDateTime ahora) {
        return jdbc.query("UPDATE inventario SET cantidad = cantidad + ?, fecha_actualizacion = ? "
                        + "OUTPUT INSERTED.cantidad WHERE id_producto = ?",
                (rs, i) -> rs.getInt(1), cantidad, Db.ts(ahora), idProducto).stream().findFirst();
    }

    /** Descuenta stock solo si alcanza; vacío = stock insuficiente. Nunca deja stock negativo. */
    public Optional<Integer> descontar(int idProducto, int cantidad, LocalDateTime ahora) {
        return jdbc.query("UPDATE inventario SET cantidad = cantidad - ?, fecha_actualizacion = ? "
                        + "OUTPUT INSERTED.cantidad WHERE id_producto = ? AND cantidad >= ?",
                (rs, i) -> rs.getInt(1), cantidad, Db.ts(ahora), idProducto, cantidad).stream().findFirst();
    }

    public void insertarMovimiento(int idProducto, String tipo, int cantidad, int stockResultante, String motivo,
                                   String referencia, int idEmpleado, LocalDateTime fecha, String token) {
        jdbc.update("INSERT INTO movimiento_inventario(id_producto, tipo, cantidad, stock_resultante, motivo, referencia, "
                        + "id_empleado, fecha, token) VALUES (?,?,?,?,?,?,?,?,?)",
                idProducto, tipo, cantidad, stockResultante, motivo, referencia, idEmpleado, Db.ts(fecha), token);
    }

    public boolean existeTokenMovimiento(String token) {
        return existe("SELECT 1 AS x FROM movimiento_inventario WHERE token = ?", token);
    }

    public List<MovimientoVista> movimientos(String q, int limite) {
        StringBuilder sql = new StringBuilder("SELECT TOP " + limite + " m.id_movimiento, m.fecha, p.codigo, p.nombre, m.tipo, "
                + "m.cantidad, m.stock_resultante, m.motivo, m.referencia, e.nombres AS usuario "
                + "FROM movimiento_inventario m JOIN producto p ON p.id_producto = m.id_producto "
                + "JOIN empleado e ON e.id_empleado = m.id_empleado WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (p.nombre LIKE ? OR p.codigo LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY m.fecha DESC, m.id_movimiento DESC");
        return jdbc.query(sql.toString(), (rs, i) -> new MovimientoVista(rs.getInt("id_movimiento"),
                Db.dateTime(rs, "fecha"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("tipo"),
                rs.getInt("cantidad"), rs.getInt("stock_resultante"), rs.getString("motivo"),
                rs.getString("referencia"), rs.getString("usuario")), args.toArray());
    }
}
