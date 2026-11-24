package com.matifarma.dao;

import com.matifarma.model.ProductoRequest;
import com.matifarma.model.ProductoVista;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoDao extends BaseDao {
    private static final String BASE =
            "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, p.id_categoria, c.nombre AS categoria, "
          + "p.id_proveedor, pr.razon_social AS proveedor, p.precio_compra, p.precio_venta, "
          + "ISNULL(i.cantidad, 0) AS stock, p.stock_minimo, p.fecha_vencimiento, i.ubicacion, p.activo "
          + "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria "
          + "LEFT JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor "
          + "LEFT JOIN inventario i ON i.id_producto = p.id_producto";

    private static final RowMapper<ProductoVista> MAPPER = (rs, i) -> new ProductoVista(
            rs.getInt("id_producto"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"),
            rs.getInt("id_categoria"), rs.getString("categoria"), Db.integer(rs, "id_proveedor"), rs.getString("proveedor"),
            rs.getBigDecimal("precio_compra"), rs.getBigDecimal("precio_venta"), rs.getInt("stock"),
            rs.getInt("stock_minimo"), Db.date(rs, "fecha_vencimiento"), rs.getString("ubicacion"), rs.getBoolean("activo"));

    public ProductoDao(JdbcTemplate jdbc) { super(jdbc); }

    public List<ProductoVista> listar(String q, Integer idCategoria, boolean soloActivos, Integer limite) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (soloActivos) sql.append(" AND p.activo = 1");
        if (q != null && !q.isBlank()) {
            sql.append(" AND (p.nombre LIKE ? OR p.codigo LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (idCategoria != null) {
            sql.append(" AND p.id_categoria = ?");
            args.add(idCategoria);
        }
        sql.append(" ORDER BY p.nombre");
        String finalSql = sql.toString();
        if (limite != null) finalSql = finalSql.replaceFirst("SELECT", "SELECT TOP " + limite.intValue());
        return jdbc.query(finalSql, MAPPER, args.toArray());
    }

    public Optional<ProductoVista> buscarPorId(int id) {
        return jdbc.query(BASE + " WHERE p.id_producto = ?", MAPPER, id).stream().findFirst();
    }

    public boolean existeCodigo(String codigo, Integer excluirId) {
        return existe("SELECT 1 AS x FROM producto WHERE codigo = ? AND id_producto <> ?", codigo, excluir(excluirId));
    }

    public boolean existeDuplicado(String nombre, int idCategoria, Integer excluirId) {
        return existe("SELECT 1 AS x FROM producto WHERE nombre = ? AND id_categoria = ? AND id_producto <> ?",
                nombre, idCategoria, excluir(excluirId));
    }

    public int insertar(ProductoRequest r) {
        return insertar("INSERT INTO producto(codigo, nombre, descripcion, id_categoria, id_proveedor, precio_compra, "
                        + "precio_venta, stock_minimo, fecha_vencimiento, activo) VALUES (?,?,?,?,?,?,?,?,?,1)",
                r.codigo(), r.nombre(), r.descripcion(), r.idCategoria(), r.idProveedor(), r.precioCompra(),
                r.precioVenta(), r.stockMinimo(), Db.d(r.fechaVencimiento()));
    }

    public void actualizar(int id, ProductoRequest r) {
        jdbc.update("UPDATE producto SET codigo=?, nombre=?, descripcion=?, id_categoria=?, id_proveedor=?, "
                        + "precio_compra=?, precio_venta=?, stock_minimo=?, fecha_vencimiento=? WHERE id_producto=?",
                r.codigo(), r.nombre(), r.descripcion(), r.idCategoria(), r.idProveedor(), r.precioCompra(),
                r.precioVenta(), r.stockMinimo(), Db.d(r.fechaVencimiento()), id);
    }

    public void cambiarEstado(int id, boolean activo) {
        jdbc.update("UPDATE producto SET activo = ? WHERE id_producto = ?", activo, id);
    }

    public void actualizarFechaVencimiento(int id, LocalDate fecha) {
        jdbc.update("UPDATE producto SET fecha_vencimiento = ? WHERE id_producto = ?", Db.d(fecha), id);
    }
}
