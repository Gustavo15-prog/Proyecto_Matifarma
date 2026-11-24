package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.dao.EmpleadoDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Protección ante intentos repetidos de acceso. */
@Service
public class SeguridadService {
    private final EmpleadoDao empleadoDao;
    @Value("${matifarma.seguridad.max-intentos:5}") private int maxIntentos;
    @Value("${matifarma.seguridad.bloqueo-minutos:15}") private int bloqueoMinutos;

    public SeguridadService(EmpleadoDao empleadoDao) { this.empleadoDao = empleadoDao; }

    public void registrarFallo(String usuario) {
        if (usuario == null) return;
        empleadoDao.buscarAuthPorUsuario(usuario.trim()).ifPresent(e -> {
            LocalDateTime ahora = Fechas.ahora();
            int intentos = e.intentos();
            if (e.bloqueadoHasta() != null && e.bloqueadoHasta().isBefore(ahora)) intentos = 0;
            intentos++;
            if (intentos >= maxIntentos) empleadoDao.actualizarIntentos(e.id(), 0, ahora.plusMinutes(bloqueoMinutos));
            else empleadoDao.actualizarIntentos(e.id(), intentos, null);
        });
    }

    public void registrarExito(int idEmpleado) { empleadoDao.actualizarIntentos(idEmpleado, 0, null); }
}
