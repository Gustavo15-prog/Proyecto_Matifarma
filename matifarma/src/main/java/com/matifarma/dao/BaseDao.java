package com.matifarma.dao;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;

public abstract class BaseDao {
    protected final JdbcTemplate jdbc;

    protected BaseDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Ejecuta un INSERT y devuelve el ID autogenerado. */
    protected int insertar(String sql, Object... args) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            new ArgumentPreparedStatementSetter(args).setValues(ps);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) throw new IllegalStateException("No se obtuvo el ID generado");
        return key.intValue();
    }

    /** Verifica si la consulta (SELECT 1 AS x FROM ...) devuelve al menos una fila. */
    protected boolean existe(String sql, Object... args) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM (" + sql + ") t", Integer.class, args);
        return n != null && n > 0;
    }

    protected static int excluir(Integer id) { return id == null ? 0 : id; }
}
