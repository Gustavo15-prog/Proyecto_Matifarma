package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.SesionActual;
import com.matifarma.dao.CategoriaDao;
import com.matifarma.dao.InventarioDao;
import com.matifarma.dao.ProductoDao;
import com.matifarma.dao.ProveedorDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.ProductoRequest;
import com.matifarma.model.ProductoVista;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoDao productoDao;
    private final CategoriaDao categoriaDao;
    private final ProveedorDao proveedorDao;
    private final InventarioDao inventarioDao;

    public ProductoService(ProductoDao productoDao, CategoriaDao categoriaDao, ProveedorDao proveedorDao, InventarioDao inventarioDao) {
        this.productoDao = productoDao;
        this.categoriaDao = categoriaDao;
        this.proveedorDao = proveedorDao;
        this.inventarioDao = inventarioDao;
    }

    /** El precio de compra solo lo ve el administrador. */
    private List<ProductoVista> filtrar(List<ProductoVista> lista) {
        return SesionActual.esAdministrador() ? lista : lista.stream().map(ProductoVista::sinCosto).toList();
    }

    public List<ProductoVista> listar(String q, Integer idCategoria, boolean soloActivos) {
        return filtrar(productoDao.listar(q, idCategoria, soloActivos, null));
    }

    public List<ProductoVista> buscarParaOperar(String q) { return filtrar(productoDao.listar(q, null, true, 15)); }

    public ProductoVista obtener(int id) {
        return productoDao.buscarPorId(id).orElseThrow(() -> ReglaNegocioException.noEncontrado("El producto no existe"));
    }

    private ProductoRequest normalizar(ProductoRequest r) {
        return new ProductoRequest(r.codigo().trim().toUpperCase(), Texto.limpiar(r.nombre()), Texto.limpiar(r.descripcion()),
                r.idCategoria(), r.idProveedor(), r.precioCompra(), r.precioVenta(), r.stockMinimo(),
                r.fechaVencimiento(), Texto.limpiar(r.ubicacion()));
    }

    private void validar(ProductoRequest r, Integer excluirId, ProductoVista anterior) {
        if (!categoriaDao.existe(r.idCategoria()))
            throw ReglaNegocioException.invalido("idCategoria", "La categoría seleccionada no existe");
        if (r.idProveedor() != null && proveedorDao.buscarPorId(r.idProveedor()).isEmpty())
            throw ReglaNegocioException.invalido("idProveedor", "El proveedor seleccionado no existe");
        if (productoDao.existeCodigo(r.codigo(), excluirId))
            throw ReglaNegocioException.conflicto("codigo", "Ya existe un producto con el código " + r.codigo());
        if (productoDao.existeDuplicado(r.nombre(), r.idCategoria(), excluirId))
            throw ReglaNegocioException.conflicto("nombre", "Producto ya registrado en esa categoría. Modifique el existente en lugar de crear otro.");
        LocalDate fv = r.fechaVencimiento();
        if (fv != null) {
            LocalDate hoy = Fechas.hoy();
            boolean sinCambio = anterior != null && fv.equals(anterior.fechaVencimiento());
            if (fv.isBefore(hoy) && !sinCambio)
                throw ReglaNegocioException.invalido("fechaVencimiento", "La fecha de vencimiento no puede ser anterior a hoy");
            if (fv.isAfter(hoy.plusYears(15)))
                throw ReglaNegocioException.invalido("fechaVencimiento", "La fecha de vencimiento es demasiado lejana");
        }
    }

    @Transactional
    public ProductoVista crear(ProductoRequest datos) {
        ProductoRequest r = normalizar(datos);
        validar(r, null, null);
        int id = productoDao.insertar(r);
        inventarioDao.crearRegistro(id, r.ubicacion(), Fechas.ahora());
        return obtener(id);
    }

    @Transactional
    public ProductoVista actualizar(int id, ProductoRequest datos) {
        ProductoVista anterior = obtener(id);
        ProductoRequest r = normalizar(datos);
        validar(r, id, anterior);
        productoDao.actualizar(id, r);
        inventarioDao.actualizarUbicacion(id, r.ubicacion());
        return obtener(id);
    }

    @Transactional
    public ProductoVista cambiarEstado(int id, boolean activo) {
        obtener(id);
        productoDao.cambiarEstado(id, activo);
        return obtener(id);
    }
}
