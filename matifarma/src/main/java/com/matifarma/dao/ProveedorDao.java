package com.matifarma.dao;

import com.matifarma.model.Proveedor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProveedorDao extends BaseDao {
    private static final String BASE = "SELECT id_proveedor, razon_social, ruc, telefono, correo, direccion, activo FROM proveedor";
    private static final RowMapper<Proveedor> MAPPER = (rs, i) -> new Proveedor(
            rs.getInt("id_proveedor"), rs.getString("razon_social"), rs.getString("ruc").trim(),
            rs.getString("telefono"), rs.getString("correo"), rs.getString("direccion"), rs.getBoolean("activo"));

    public ProveedorDao(JdbcTemplate jdbc) { super(jdbc); }

    public List<Proveedor> listar(String q, boolean soloActivos) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (soloActivos) sql.append(" AND activo = 1");
        if (q != null && !q.isBlank()) {
            sql.append(" AND (razon_social LIKE ? OR ruc LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY razon_social");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    public Optional<Proveedor> buscarPorId(int id) {
        return jdbc.query(BASE + " WHERE id_proveedor = ?", MAPPER, id).stream().findFirst();
    }

    public boolean existeRuc(String ruc, Integer excluirId) {
        return existe("SELECT 1 AS x FROM proveedor WHERE ruc = ? AND id_proveedor <> ?", ruc, excluir(excluirId));
    }

    public int insertar(Proveedor p) {
        return insertar("INSERT INTO proveedor(razon_social, ruc, telefono, correo, direccion, activo) VALUES (?,?,?,?,?,1)",
                p.razonSocial(), p.ruc(), p.telefono(), p.correo(), p.direccion());
    }

    public void actualizar(int id, Proveedor p) {
        jdbc.update("UPDATE proveedor SET razon_social=?, ruc=?, telefono=?, correo=?, direccion=? WHERE id_proveedor=?",
                p.razonSocial(), p.ruc(), p.telefono(), p.correo(), p.direccion(), id);
    }

    public void cambiarEstado(int id, boolean activo) {
        jdbc.update("UPDATE proveedor SET activo = ? WHERE id_proveedor = ?", activo, id);
    }
}
