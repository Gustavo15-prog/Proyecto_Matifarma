package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.SesionActual;
import com.matifarma.dao.CompraDao;
import com.matifarma.dao.InventarioDao;
import com.matifarma.dao.ProductoDao;
import com.matifarma.dao.ProveedorDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InventarioService {
    private final InventarioDao inventarioDao;
    private final ProductoDao productoDao;
    private final ProveedorDao proveedorDao;
    private final CompraDao compraDao;

    private record Linea(ProductoVista producto, IngresoDetalle detalle, BigDecimal subtotal, boolean conforme) {}

    public InventarioService(InventarioDao inventarioDao, ProductoDao productoDao, ProveedorDao proveedorDao, CompraDao compraDao) {
        this.inventarioDao = inventarioDao;
        this.productoDao = productoDao;
        this.proveedorDao = proveedorDao;
        this.compraDao = compraDao;
    }

    public List<MovimientoVista> movimientos(String q) { return inventarioDao.movimientos(q, 200); }

    @Transactional
    public IngresoResultado registrarIngreso(IngresoRequest r) {
        if (compraDao.existeToken(r.token()))
            throw ReglaNegocioException.conflicto("Este ingreso ya fue registrado. Evite enviar el formulario dos veces.");
        Proveedor prov = proveedorDao.buscarPorId(r.idProveedor())
                .orElseThrow(() -> ReglaNegocioException.invalido("idProveedor", "El proveedor seleccionado no existe"));
        if (!Boolean.TRUE.equals(prov.activo()))
            throw ReglaNegocioException.invalido("idProveedor", "El proveedor seleccionado está inactivo");
        String documento = Texto.limpiar(r.numeroDocumento());
        if (documento == null) throw ReglaNegocioException.invalido("numeroDocumento", "El número de guía o factura es obligatorio");
        documento = documento.toUpperCase();
        if (compraDao.existeDocumento(prov.id(), documento))
            throw ReglaNegocioException.conflicto("numeroDocumento", "Ya se registró un ingreso con esa guía/factura para este proveedor");

        LocalDate hoy = Fechas.hoy();
        LocalDateTime ahora = Fechas.ahora();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Set<Integer> vistos = new HashSet<>();
        List<Linea> lineas = new ArrayList<>();
        List<String> noConformes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (IngresoDetalle d : r.detalles()) {
            if (!vistos.add(d.idProducto()))
                throw ReglaNegocioException.invalido("Hay un producto repetido en el ingreso. Unifique las cantidades en una sola línea.");
            ProductoVista p = productoDao.buscarPorId(d.idProducto())
                    .orElseThrow(() -> ReglaNegocioException.invalido("El producto con ID " + d.idProducto() + " no existe"));
            if (!p.activo()) throw ReglaNegocioException.invalido("El producto «" + p.nombre() + "» está inactivo");
            if (d.fechaVencimiento() != null && d.fechaVencimiento().isAfter(hoy.plusYears(15)))
                throw ReglaNegocioException.invalido("La fecha de vencimiento de «" + p.nombre() + "» es demasiado lejana");
            boolean conforme = d.fechaVencimiento() == null || !d.fechaVencimiento().isBefore(hoy);
            BigDecimal subtotal = d.costoUnitario().multiply(BigDecimal.valueOf(d.cantidad())).setScale(2, RoundingMode.HALF_UP);
            if (conforme) total = total.add(subtotal);
            else noConformes.add(p.nombre() + " (vencido el " + d.fechaVencimiento().format(fmt) + ")");
            lineas.add(new Linea(p, d, subtotal, conforme));
        }

        int idEmpleado = SesionActual.idEmpleado();
        int idCompra = compraDao.insertar(prov.id(), idEmpleado, documento, ahora, total, r.token());
        int ingresadas = 0;
        for (Linea l : lineas) {
            IngresoDetalle d = l.detalle();
            ProductoVista p = l.producto();
            compraDao.insertarDetalle(idCompra, p.id(), d.cantidad(), d.costoUnitario(), l.subtotal(), d.fechaVencimiento(),
                    l.conforme(), l.conforme() ? null : "Producto vencido: no ingresa al stock");
            if (!l.conforme()) continue;
            int nuevo = inventarioDao.incrementar(p.id(), d.cantidad(), ahora)
                    .orElseThrow(() -> new IllegalStateException("El producto no tiene registro de inventario"));
            inventarioDao.insertarMovimiento(p.id(), "ENTRADA", d.cantidad(), nuevo, "Ingreso de proveedor",
                    "COMP-" + idCompra, idEmpleado, ahora, null);
            if (d.fechaVencimiento() != null) {
                LocalDate actual = p.fechaVencimiento();
                boolean sinStockPrevio = nuevo - d.cantidad() <= 0;
                LocalDate fijar = (sinStockPrevio || actual == null || d.fechaVencimiento().isBefore(actual))
                        ? d.fechaVencimiento() : actual;
                if (!fijar.equals(actual)) productoDao.actualizarFechaVencimiento(p.id(), fijar);
            }
            ingresadas++;
        }
        return new IngresoResultado(idCompra, ingresadas, total, noConformes);
    }

    @Transactional
    public int registrarSalida(SalidaRequest r) {
        if (inventarioDao.existeTokenMovimiento(r.token()))
            throw ReglaNegocioException.conflicto("Esta salida ya fue registrada. Evite enviar el formulario dos veces.");
        ProductoVista p = productoDao.buscarPorId(r.idProducto())
                .orElseThrow(() -> ReglaNegocioException.invalido("idProducto", "El producto no existe"));
        if (!p.activo()) throw ReglaNegocioException.invalido("idProducto", "El producto «" + p.nombre() + "» está inactivo");
        String motivo = Texto.limpiar(r.motivo());
        if (motivo == null) throw ReglaNegocioException.invalido("motivo", "El motivo es obligatorio");
        LocalDateTime ahora = Fechas.ahora();
        int nuevo = inventarioDao.descontar(p.id(), r.cantidad(), ahora).orElseThrow(() -> ReglaNegocioException.conflicto(
                "cantidad", "Stock insuficiente: solo hay " + p.stock() + " unidades. Una salida no puede dejar el stock en negativo."));
        inventarioDao.insertarMovimiento(p.id(), "SALIDA", r.cantidad(), nuevo, motivo, null, SesionActual.idEmpleado(), ahora, r.token());
        return nuevo;
    }
}
