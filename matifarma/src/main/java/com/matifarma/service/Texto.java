package com.matifarma.service;

public final class Texto {
    private Texto() {}

    /** Recorta espacios; devuelve null si queda vacío. */
    public static String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? null : t;
    }
}
