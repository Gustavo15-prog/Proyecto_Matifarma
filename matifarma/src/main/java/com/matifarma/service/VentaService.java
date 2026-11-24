package com.matifarma.service;

import com.matifarma.config.Fechas;
import com.matifarma.config.SesionActual;
import com.matifarma.dao.ClienteDao;
import com.matifarma.dao.InventarioDao;
import com.matifarma.dao.ProductoDao;
import com.matifarma.dao.VentaDao;
import com.matifarma.exception.ReglaNegocioException;
import com.matifarma.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VentaService {
    private static final Set<String> TIPOS_PAGO = Set.of("EFECTIVO", "YAPE", "PLIN", "TARJETA");

    private final VentaDao ventaDao;
    private final ProductoDao productoDao;
    private final InventarioDao inventarioDao;
    private final ClienteDao clienteDao;

    private record Linea(ProductoVista producto, int cantidad, BigDecimal precio, BigDecimal subtotal) {}

    public VentaService(VentaDao ventaDao, ProductoDao productoDao, InventarioDao inventarioDao, ClienteDao clienteDao) {
        this.ventaDao = ventaDao;
        this.productoDao = productoDao;
        this.inventarioDao = inventarioDao;
        this.clienteDao = clienteDao;
    }

    public List<VentaResumen> recientes() {
        return ventaDao.recientes(30, SesionActual.esAdministrador() ? null : SesionActual.idEmpleado());
    }

    public VentaComprobante comprobante(int id) {
        Integer dueno = ventaDao.idEmpleadoDeVenta(id).orElseThrow(() -> ReglaNegocioException.noEncontrado("La venta no existe"));
        if (!SesionActual.esAdministrador() && dueno != SesionActual.idEmpleado())
            throw new ReglaNegocioException("No puede consultar ventas de otros usuarios", HttpStatus.FORBIDDEN, null);
        return ventaDao.comprobante(id).orElseThrow(() -> ReglaNegocioException.noEncontrado("La venta no existe"));
    }

    @Transactional
    public VentaComprobante registrar(VentaRequest r) {
        if (ventaDao.existeToken(r.token()))
            throw ReglaNegocioException.conflicto("Esta venta ya fue registrada. Evite enviar el formulario dos veces.");
        String tipoPago = r.tipoPago().trim().toUpperCase();
        if (!TIPOS_PAGO.contains(tipoPago)) throw ReglaNegocioException.invalido("tipoPago", "Tipo de pago no permitido");

        // Cliente (opcional)
        final String documento = Texto.limpiar(r.clienteDocumento());
        final String nombre = Texto.limpiar(r.clienteNombre());
        Integer idCliente = null;
        if (documento == null && nombre != null)
            throw ReglaNegocioException.invalido("clienteDocumento", "Ingrese el documento del cliente o deje ambos campos vacíos");
        if (documento != null) {
            if (!documento.matches("^(\\d{8}|\\d{11}|[A-Za-z0-9]{9,12})$"))
                throw ReglaNegocioException.invalido("clienteDocumento", "Documento inválido: DNI (8 dígitos), RUC (11) o carnet de extranjería");
            idCliente = clienteDao.buscarId(documento).orElseGet(() -> {
                if (nombre == null) throw ReglaNegocioException.invalido("clienteNombre", "Ingrese el nombre del cliente");
                return clienteDao.insertar(documento, nombre);
            });
        }

        // Validar líneas y calcular importes con los precios del servidor
        LocalDate hoy = Fechas.hoy();
        List<VentaDetalleRequest> ordenadas = r.detalles().stream()
                .sorted(Comparator.comparing(VentaDetalleRequest::idProducto)).toList();
        Set<Integer> vistos = new HashSet<>();
        List<Linea> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (VentaDetalleRequest d : ordenadas) {
            if (!vistos.add(d.idProducto()))
                throw ReglaNegocioException.invalido("Hay un producto repetido en la venta. Unifique las cantidades.");
            ProductoVista p = productoDao.buscarPorId(d.idProducto())
                    .orElseThrow(() -> ReglaNegocioException.invalido("El producto con ID " + d.idProducto() + " no existe"));
            if (!p.activo()) throw ReglaNegocioException.invalido("El producto «" + p.nombre() + "» está inactivo");
            if (p.fechaVencimiento() != null && p.fechaVencimiento().isBefore(hoy))
                throw ReglaNegocioException.invalido("El producto «" + p.nombre() + "» está vencido y no puede venderse");
            if (p.stock() < d.cantidad())
                throw ReglaNegocioException.conflicto("Stock insuficiente de «" + p.nombre() + "»: disponible " + p.stock() + ", solicitado " + d.cantidad());
            BigDecimal subtotal = p.precioVenta().multiply(BigDecimal.valueOf(d.cantidad())).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);
            lineas.add(new Linea(p, d.cantidad(), p.precioVenta(), subtotal));
        }

        // Registrar venta, detalle, stock y movimientos (misma transacción)
        LocalDateTime ahora = Fechas.ahora();
        int idEmpleado = SesionActual.idEmpleado();
        int idVenta = ventaDao.insertar(ahora, idCliente, idEmpleado, tipoPago, total, r.token());
        String referencia = VentaDao.numero(idVenta);
        for (Linea l : lineas) {
            ventaDao.insertarDetalle(idVenta, l.producto().id(), l.cantidad(), l.precio(), l.subtotal());
            Optional<Integer> nuevo = inventarioDao.descontar(l.producto().id(), l.cantidad(), ahora);
            if (nuevo.isEmpty()) {
                int disponible = productoDao.buscarPorId(l.producto().id()).map(ProductoVista::stock).orElse(0);
                throw ReglaNegocioException.conflicto("Stock insuficiente de «" + l.producto().nombre() + "»: disponible " + disponible);
            }
            inventarioDao.insertarMovimiento(l.producto().id(), "SALIDA", l.cantidad(), nuevo.get(), "Venta", referencia, idEmpleado, ahora, null);
        }
        return ventaDao.comprobante(idVenta).orElseThrow(() -> new IllegalStateException("No se pudo generar el comprobante"));
    }
}
