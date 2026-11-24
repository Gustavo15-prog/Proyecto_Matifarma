package com.matifarma.dao;

import com.matifarma.model.VentaDia;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class DashboardDao extends BaseDao {
    public DashboardDao(JdbcTemplate jdbc) { super(jdbc); }

    public int totalProductosActivos() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE activo = 1", Integer.class);
        return n == null ? 0 : n;
    }

    public int cantidadVentas(LocalDate desde, LocalDate hastaExcluido) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM venta WHERE fecha >= ? AND fecha < ?", Integer.class,
                Db.ts(desde.atStartOfDay()), Db.ts(hastaExcluido.atStartOfDay()));
        return n == null ? 0 : n;
    }

    public BigDecimal totalVentas(LocalDate desde, LocalDate hastaExcluido) {
        BigDecimal t = jdbc.queryForObject("SELECT ISNULL(SUM(total), 0) FROM venta WHERE fecha >= ? AND fecha < ?",
                BigDecimal.class, Db.ts(desde.atStartOfDay()), Db.ts(hastaExcluido.atStartOfDay()));
        return t == null ? BigDecimal.ZERO : t;
    }

    public List<VentaDia> totalesPorDia(LocalDate desde, LocalDate hastaExcluido) {
        return jdbc.query("SELECT CAST(fecha AS DATE) AS dia, SUM(total) AS total FROM venta WHERE fecha >= ? AND fecha < ? "
                        + "GROUP BY CAST(fecha AS DATE)",
                (rs, i) -> new VentaDia(Db.date(rs, "dia"), rs.getBigDecimal("total")),
                Db.ts(desde.atStartOfDay()), Db.ts(hastaExcluido.atStartOfDay()));
    }
}
