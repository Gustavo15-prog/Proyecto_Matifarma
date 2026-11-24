package com.matifarma.config;

import com.matifarma.service.SeguridadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final SeguridadService seguridad;

    public LoginSuccessHandler(SeguridadService seguridad) { this.seguridad = seguridad; }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        if (auth.getPrincipal() instanceof UsuarioPrincipal p) seguridad.registrarExito(p.getIdEmpleado());
        res.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
