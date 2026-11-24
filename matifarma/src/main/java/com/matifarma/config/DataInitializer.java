package com.matifarma.config;

import com.matifarma.dao.EmpleadoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Crea el administrador inicial la primera vez que arranca el sistema (tabla empleado vacía). */
@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmpleadoDao empleadoDao;
    private final PasswordEncoder encoder;
    @Value("${matifarma.admin.usuario}") private String usuario;
    @Value("${matifarma.admin.correo}") private String correo;
    @Value("${matifarma.admin.nombres}") private String nombres;
    @Value("${matifarma.admin.password:}") private String password;

    public DataInitializer(EmpleadoDao empleadoDao, PasswordEncoder encoder) {
        this.empleadoDao = empleadoDao;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (empleadoDao.contar() == 0) {
            boolean generada = password == null || password.isBlank();
            String clave = generada ? generarClave() : password;
            empleadoDao.insertar(usuario, correo, nombres, encoder.encode(clave), "ADMINISTRADOR", Fechas.ahora());
            if (generada)
                log.warn("Administrador inicial creado. Usuario: '{}'. Contraseña temporal (se muestra solo esta vez): {}  -- cámbiela desde Usuarios.", usuario, clave);
            else
                log.info("Administrador inicial creado. Usuario: '{}'. Cambie la contraseña después del primer ingreso.", usuario);
        }
    }

    /** Contraseña aleatoria de 16 caracteres que cumple la política (mayúscula, minúscula y número). */
    private static String generarClave() {
        final String may = "ABCDEFGHJKLMNPQRSTUVWXYZ", min = "abcdefghijkmnopqrstuvwxyz", num = "23456789";
        final String todo = may + min + num;
        SecureRandom r = new SecureRandom();
        List<Character> c = new ArrayList<>();
        c.add(may.charAt(r.nextInt(may.length())));
        c.add(min.charAt(r.nextInt(min.length())));
        c.add(num.charAt(r.nextInt(num.length())));
        for (int i = 0; i < 13; i++) c.add(todo.charAt(r.nextInt(todo.length())));
        Collections.shuffle(c, r);
        StringBuilder sb = new StringBuilder();
        c.forEach(sb::append);
        return sb.toString();
    }
}
