package com.matifarma.config;

import com.matifarma.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SesionActual {
    private SesionActual() {}

    public static UsuarioPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal p) return p;
        throw new ReglaNegocioException("Sesión no válida", HttpStatus.UNAUTHORIZED, null);
    }

    public static int idEmpleado() { return principal().getIdEmpleado(); }
    public static String rol() { return principal().getRol(); }
    public static boolean esAdministrador() { return "ADMINISTRADOR".equals(rol()); }
}
