package com.matifarma.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Conversiones entre tipos java.time y tipos JDBC. */
public final class Db {
    private Db() {}

    public static Date d(LocalDate v) { return v == null ? null : Date.valueOf(v); }
    public static Timestamp ts(LocalDateTime v) { return v == null ? null : Timestamp.valueOf(v); }

    public static LocalDate date(ResultSet rs, String col) throws SQLException {
        Date v = rs.getDate(col);
        return v == null ? null : v.toLocalDate();
    }

    public static LocalDateTime dateTime(ResultSet rs, String col) throws SQLException {
        Timestamp v = rs.getTimestamp(col);
        return v == null ? null : v.toLocalDateTime();
    }

    public static Integer integer(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
