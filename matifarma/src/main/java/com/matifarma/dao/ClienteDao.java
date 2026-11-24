package com.matifarma.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ClienteDao extends BaseDao {
    public ClienteDao(JdbcTemplate jdbc) { super(jdbc); }

    public Optional<Integer> buscarId(String documento) {
        return jdbc.query("SELECT id_cliente FROM cliente WHERE documento = ?", (rs, i) -> rs.getInt(1), documento)
                .stream().findFirst();
    }

    public int insertar(String documento, String nombre) {
        return insertar("INSERT INTO cliente(documento, nombre) VALUES (?, ?)", documento, nombre);
    }
}
