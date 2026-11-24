package com.matifarma.config;

import com.matifarma.service.SeguridadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    private final SeguridadService seguridad;

    public LoginFailureHandler(SeguridadService seguridad) { this.seguridad = seguridad; }

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
       
        if (ex instanceof BadCredentialsException) seguridad.registrarFallo(req.getParameter("username"));
        res.sendRedirect(req.getContextPath() + "/login?error");
    }
}
