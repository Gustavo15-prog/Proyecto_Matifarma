package com.matifarma.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controlador MVC: devuelve las vistas Thymeleaf. El acceso por rol se define en SecurityConfig. */
@Controller
public class PaginaController {

    @GetMapping("/")
    public String raiz() { return "redirect:/dashboard"; }

    @GetMapping("/login")
    public String login(Authentication auth) {
        boolean autenticado = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        return autenticado ? "redirect:/dashboard" : "login";
    }

    @GetMapping("/dashboard") public String dashboard() { return "dashboard"; }
    @GetMapping("/productos") public String productos() { return "productos"; }
    @GetMapping("/inventario") public String inventario() { return "inventario"; }
    @GetMapping("/ventas") public String ventas() { return "ventas"; }
    @GetMapping("/consultar-stock") public String consultarStock() { return "consultar-stock"; }
    @GetMapping("/alertas") public String alertas() { return "alertas"; }
    @GetMapping("/proveedores") public String proveedores() { return "proveedores"; }
    @GetMapping("/reportes") public String reportes() { return "reportes"; }
    @GetMapping("/usuarios") public String usuarios() { return "usuarios"; }
}
