package com.matifarma.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/** Usuario autenticado con los datos adicionales del empleado. */
public class UsuarioPrincipal extends User {
    private final Integer idEmpleado;
    private final String nombres;
    private final String rol;

    public UsuarioPrincipal(Integer idEmpleado, String nombres, String rol, String usuario, String passwordHash,
                            boolean activo, boolean cuentaNoBloqueada) {
        super(usuario, passwordHash, activo, true, true, cuentaNoBloqueada,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        this.idEmpleado = idEmpleado;
        this.nombres = nombres;
        this.rol = rol;
    }

    public Integer getIdEmpleado() { return idEmpleado; }
    public String getNombres() { return nombres; }
    public String getRol() { return rol; }
}
