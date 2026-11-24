package com.matifarma.model;

import java.util.Arrays;

public enum Rol {
    ADMINISTRADOR, VENDEDOR, ALMACENERO;

    public static boolean esValido(String valor) {
        return valor != null && Arrays.stream(values()).anyMatch(r -> r.name().equals(valor));
    }
}
