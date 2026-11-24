package com.matifarma.config;

import com.matifarma.dao.EmpleadoDao;
import com.matifarma.model.EmpleadoAuth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Revalida en cada petición que el empleado siga activo y conserve el mismo rol.
 * Así, desactivar a un usuario o cambiarle el rol surte efecto de inmediato,
 * sin esperar a que caduque su sesión.
 */
public class EstadoSesionFilter extends OncePerRequestFilter {
    private final EmpleadoDao empleadoDao;

    public EstadoSesionFilter(EmpleadoDao empleadoDao) { this.empleadoDao = empleadoDao; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/img/") || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal p) {
            Optional<EmpleadoAuth> actual = empleadoDao.buscarAuthPorId(p.getIdEmpleado());
            boolean valido = actual.isPresent() && actual.get().activo() && actual.get().rol().equals(p.getRol());
            if (!valido) {
                SecurityContextHolder.clearContext();
                HttpSession sesion = req.getSession(false);
                if (sesion != null) sesion.invalidate();
                if (req.getRequestURI().startsWith("/api/")) res.setStatus(HttpStatus.UNAUTHORIZED.value());
                else res.sendRedirect(req.getContextPath() + "/login?sesion");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
