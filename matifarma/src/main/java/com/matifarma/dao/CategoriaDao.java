package com.matifarma.dao;

import com.matifarma.model.Categoria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoriaDao extends BaseDao {
    public CategoriaDao(JdbcTemplate jdbc) { super(jdbc); }

    public List<Categoria> listar() {
        return jdbc.query("SELECT id_categoria, nombre FROM categoria ORDER BY nombre",
                (rs, i) -> new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
    }

    public boolean existe(int id) { return existe("SELECT 1 AS x FROM categoria WHERE id_categoria = ?", id); }

    public boolean existeNombre(String nombre) { return existe("SELECT 1 AS x FROM categoria WHERE nombre = ?", nombre); }

    public Categoria insertar(String nombre) {
        int id = insertar("INSERT INTO categoria(nombre) VALUES (?)", nombre);
        return new Categoria(id, nombre);
    }
}
