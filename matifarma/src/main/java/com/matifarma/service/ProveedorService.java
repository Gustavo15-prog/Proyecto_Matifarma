package com.matifarma.service;

import com.matifarma.dao.ProveedorDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.Proveedor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {
    private final ProveedorDao proveedorDao;

    public ProveedorService(ProveedorDao proveedorDao) { this.proveedorDao = proveedorDao; }

    public List<Proveedor> listar(String q, boolean soloActivos) { return proveedorDao.listar(q, soloActivos); }

    public Proveedor obtener(int id) {
        return proveedorDao.buscarPorId(id).orElseThrow(() -> ReglaNegocioException.noEncontrado("El proveedor no existe"));
    }

    private Proveedor normalizar(Proveedor p) {
        String correo = Texto.limpiar(p.correo());
        return new Proveedor(p.id(), Texto.limpiar(p.razonSocial()), p.ruc().trim(), Texto.limpiar(p.telefono()),
                correo == null ? null : correo.toLowerCase(), Texto.limpiar(p.direccion()), p.activo());
    }

    @Transactional
    public Proveedor crear(Proveedor datos) {
        Proveedor p = normalizar(datos);
        if (proveedorDao.existeRuc(p.ruc(), null)) throw ReglaNegocioException.conflicto("ruc", "Ya existe un proveedor con ese RUC");
        return obtener(proveedorDao.insertar(p));
    }

    @Transactional
    public Proveedor actualizar(int id, Proveedor datos) {
        obtener(id);
        Proveedor p = normalizar(datos);
        if (proveedorDao.existeRuc(p.ruc(), id)) throw ReglaNegocioException.conflicto("ruc", "Ya existe otro proveedor con ese RUC");
        proveedorDao.actualizar(id, p);
        return obtener(id);
    }

    @Transactional
    public Proveedor cambiarEstado(int id, boolean activo) {
        obtener(id);
        proveedorDao.cambiarEstado(id, activo);
        return obtener(id);
    }
}
