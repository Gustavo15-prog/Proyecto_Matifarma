package com.matifarma.dao;

import com.matifarma.model.EmpleadoAuth;
import com.matifarma.model.UsuarioVista;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EmpleadoDao extends BaseDao {
    private static final String AUTH = "SELECT id_empleado, usuario, nombres, password_hash, rol, activo, intentos_fallidos, bloqueado_hasta FROM empleado";
    private static final RowMapper<EmpleadoAuth> AUTH_MAPPER = (rs, i) -> new EmpleadoAuth(rs.getInt("id_empleado"),
            rs.getString("usuario"), rs.getString("nombres"), rs.getString("password_hash"), rs.getString("rol"),
            rs.getBoolean("activo"), rs.getInt("intentos_fallidos"), Db.dateTime(rs, "bloqueado_hasta"));

    private static final String VISTA = "SELECT id_empleado, usuario, correo, nombres, rol, activo, intentos_fallidos, "
            + "CASE WHEN bloqueado_hasta IS NOT NULL AND bloqueado_hasta > ? THEN 1 ELSE 0 END AS bloqueado FROM empleado";

    private static RowMapper<UsuarioVista> vistaMapper() {
        return (rs, i) -> new UsuarioVista(rs.getInt("id_empleado"), rs.getString("usuario"), rs.getString("correo"),
                rs.getString("nombres"), rs.getString("rol"), rs.getBoolean("activo"), rs.getInt("bloqueado") == 1,
                rs.getInt("intentos_fallidos"));
    }

    public EmpleadoDao(JdbcTemplate jdbc) { super(jdbc); }

    public Optional<EmpleadoAuth> buscarAuthPorUsuario(String usuario) {
        return jdbc.query(AUTH + " WHERE usuario = ?", AUTH_MAPPER, usuario).stream().findFirst();
    }

    public Optional<EmpleadoAuth> buscarAuthPorId(int id) {
        return jdbc.query(AUTH + " WHERE id_empleado = ?", AUTH_MAPPER, id).stream().findFirst();
    }

    public List<UsuarioVista> listar(LocalDateTime ahora) {
        return jdbc.query(VISTA + " ORDER BY nombres", vistaMapper(), Db.ts(ahora));
    }

    public Optional<UsuarioVista> buscarVista(int id, LocalDateTime ahora) {
        return jdbc.query(VISTA + " WHERE id_empleado = ?", vistaMapper(), Db.ts(ahora), id).stream().findFirst();
    }

    public boolean existeUsuario(String usuario, Integer excluirId) {
        return existe("SELECT 1 AS x FROM empleado WHERE usuario = ? AND id_empleado <> ?", usuario, excluir(excluirId));
    }

    public boolean existeCorreo(String correo, Integer excluirId) {
        return existe("SELECT 1 AS x FROM empleado WHERE correo = ? AND id_empleado <> ?", correo, excluir(excluirId));
    }

    public int insertar(String usuario, String correo, String nombres, String hash, String rol, LocalDateTime ahora) {
        return insertar("INSERT INTO empleado(usuario, correo, nombres, password_hash, rol, activo, intentos_fallidos, fecha_creacion) "
                + "VALUES (?,?,?,?,?,1,0,?)", usuario, correo, nombres, hash, rol, Db.ts(ahora));
    }

    public void actualizar(int id, String usuario, String correo, String nombres, String rol) {
        jdbc.update("UPDATE empleado SET usuario=?, correo=?, nombres=?, rol=? WHERE id_empleado=?", usuario, correo, nombres, rol, id);
    }

    public void actualizarPassword(int id, String hash) {
        jdbc.update("UPDATE empleado SET password_hash = ?, intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_empleado = ?", hash, id);
    }

    public void cambiarEstado(int id, boolean activo) {
        jdbc.update("UPDATE empleado SET activo = ? WHERE id_empleado = ?", activo, id);
    }

    public void actualizarIntentos(int id, int intentos, LocalDateTime bloqueadoHasta) {
        jdbc.update("UPDATE empleado SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id_empleado = ?",
                intentos, Db.ts(bloqueadoHasta), id);
    }

    public int contar() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM empleado", Integer.class);
        return n == null ? 0 : n;
    }

    public int contarAdministradoresActivos() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM empleado WHERE rol = 'ADMINISTRADOR' AND activo = 1", Integer.class);
        return n == null ? 0 : n;
    }
}
