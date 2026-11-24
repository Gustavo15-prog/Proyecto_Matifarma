package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.UsuarioPrincipal;
import com.matifarma.dao.EmpleadoDao;
import com.matifarma.model.EmpleadoAuth;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoUserDetailsService implements UserDetailsService {
    private final EmpleadoDao empleadoDao;

    public EmpleadoUserDetailsService(EmpleadoDao empleadoDao) { this.empleadoDao = empleadoDao; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String usuario = username == null ? "" : username.trim();
        EmpleadoAuth e = empleadoDao.buscarAuthPorUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        boolean bloqueado = e.bloqueadoHasta() != null && e.bloqueadoHasta().isAfter(Fechas.ahora());
        return new UsuarioPrincipal(e.id(), e.nombres(), e.rol(), e.usuario(), e.passwordHash(), e.activo(), !bloqueado);
    }
}
