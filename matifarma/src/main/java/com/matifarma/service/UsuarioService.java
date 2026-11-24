package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.SesionActual;
import com.matifarma.dao.EmpleadoDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.EmpleadoAuth;
import com.matifarma.model.Rol;
import com.matifarma.model.UsuarioRequest;
import com.matifarma.model.UsuarioVista;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private final EmpleadoDao empleadoDao;
    private final PasswordEncoder encoder;

    public UsuarioService(EmpleadoDao empleadoDao, PasswordEncoder encoder) {
        this.empleadoDao = empleadoDao;
        this.encoder = encoder;
    }

    public List<UsuarioVista> listar() { return empleadoDao.listar(Fechas.ahora()); }

    public UsuarioVista obtener(int id) {
        return empleadoDao.buscarVista(id, Fechas.ahora()).orElseThrow(() -> ReglaNegocioException.noEncontrado("El usuario no existe"));
    }

    private void validarPassword(String password, String confirmar) {
        if (password == null || password.length() < 8)
            throw ReglaNegocioException.invalido("password", "La contraseña debe tener al menos 8 caracteres");
        if (password.length() > 64 || password.contains(" "))
            throw ReglaNegocioException.invalido("password", "La contraseña no puede tener espacios ni superar 64 caracteres");
        if (!password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*"))
            throw ReglaNegocioException.invalido("password", "La contraseña debe incluir mayúscula, minúscula y número");
        if (!password.equals(confirmar))
            throw ReglaNegocioException.invalido("confirmarPassword", "La confirmación no coincide con la contraseña");
    }

    private void validarDatos(UsuarioRequest r, Integer excluirId) {
        if (!Rol.esValido(r.rol())) throw ReglaNegocioException.invalido("rol", "Rol no permitido");
        if (empleadoDao.existeUsuario(r.usuario().trim().toLowerCase(), excluirId))
            throw ReglaNegocioException.conflicto("usuario", "El nombre de usuario ya está en uso");
        if (empleadoDao.existeCorreo(r.correo().trim().toLowerCase(), excluirId))
            throw ReglaNegocioException.conflicto("correo", "El correo ya está registrado");
    }

    @Transactional
    public UsuarioVista crear(UsuarioRequest r) {
        validarDatos(r, null);
        validarPassword(r.password(), r.confirmarPassword());
        int id = empleadoDao.insertar(r.usuario().trim().toLowerCase(), r.correo().trim().toLowerCase(),
                Texto.limpiar(r.nombres()), encoder.encode(r.password()), r.rol(), Fechas.ahora());
        return obtener(id);
    }

    @Transactional
    public UsuarioVista actualizar(int id, UsuarioRequest r) {
        EmpleadoAuth actual = empleadoDao.buscarAuthPorId(id).orElseThrow(() -> ReglaNegocioException.noEncontrado("El usuario no existe"));
        validarDatos(r, id);
        boolean quitaAdmin = actual.rol().equals("ADMINISTRADOR") && !r.rol().equals("ADMINISTRADOR");
        if (quitaAdmin && id == SesionActual.idEmpleado())
            throw ReglaNegocioException.conflicto("rol", "No puede quitarse a sí mismo el rol de administrador");
        if (quitaAdmin && actual.activo() && empleadoDao.contarAdministradoresActivos() <= 1)
            throw ReglaNegocioException.conflicto("rol", "Debe existir al menos un administrador activo");
        empleadoDao.actualizar(id, r.usuario().trim().toLowerCase(), r.correo().trim().toLowerCase(), Texto.limpiar(r.nombres()), r.rol());
        if (r.password() != null && !r.password().isEmpty()) {
            validarPassword(r.password(), r.confirmarPassword());
            empleadoDao.actualizarPassword(id, encoder.encode(r.password()));
        }
        return obtener(id);
    }

    @Transactional
    public UsuarioVista cambiarEstado(int id, boolean activo) {
        EmpleadoAuth actual = empleadoDao.buscarAuthPorId(id).orElseThrow(() -> ReglaNegocioException.noEncontrado("El usuario no existe"));
        if (!activo) {
            if (id == SesionActual.idEmpleado()) throw ReglaNegocioException.conflicto("No puede desactivar su propia cuenta");
            if (actual.rol().equals("ADMINISTRADOR") && actual.activo() && empleadoDao.contarAdministradoresActivos() <= 1)
                throw ReglaNegocioException.conflicto("Debe existir al menos un administrador activo");
        }
        empleadoDao.cambiarEstado(id, activo);
        return obtener(id);
    }

    @Transactional
    public UsuarioVista desbloquear(int id) {
        obtener(id);
        empleadoDao.actualizarIntentos(id, 0, null);
        return obtener(id);
    }
}
