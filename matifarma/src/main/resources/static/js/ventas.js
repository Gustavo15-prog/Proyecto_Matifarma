(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const form = $('formVenta');
  let carrito = [];                 // {id, codigo, nombre, precio, stock, cantidad}
  let token = Fmt.uuid();

  Validar.soloDigitos(form.elements.clienteDocumento);

  const total = () => carrito.reduce((a, l) => a + l.precio * l.cantidad, 0);

  function pintarCarrito() {
    $('tbCarrito').innerHTML = carrito.length ? carrito.map((l, i) =>
      '<tr data-i="' + i + '"><td class="linea-producto"><strong>' + Fmt.esc(l.nombre) + '</strong><small>' + Fmt.esc(l.codigo) + '</small></td>' +
      '<td class="num">' + Fmt.moneda(l.precio) + '</td>' +
      '<td><div class="cant"><button type="button" class="btn btn-outline-primary btn-sm" data-delta="-1" aria-label="Disminuir">−</button>' +
      '<input type="number" class="form-control form-control-sm" min="1" max="' + l.stock + '" step="1" value="' + l.cantidad + '" aria-label="Cantidad de ' + Fmt.esc(l.nombre) + '">' +
      '<button type="button" class="btn btn-outline-primary btn-sm" data-delta="1" aria-label="Aumentar">+</button></div><span class="stock-tope">Disponible: ' + l.stock + '</span></td>' +
      '<td class="num" data-sub><strong>' + Fmt.moneda(l.precio * l.cantidad) + '</strong></td>' +
      '<td class="text-end"><button type="button" class="btn btn-icon btn-outline-danger" data-quitar aria-label="Quitar ' + Fmt.esc(l.nombre) + '"><i class="bi bi-x-lg"></i></button></td></tr>'
    ).join('') : '<tr><td colspan="5" class="vacio"><i class="bi bi-cart"></i>El carrito está vacío. Busque un producto para comenzar.</td></tr>';
    actualizarTotales();
  }

  function actualizarTotales() {
    $('totItems').textContent = carrito.reduce((a, l) => a + l.cantidad, 0);
    $('totMonto').textContent = Fmt.moneda(total());
  }

  function fijarCantidad(i, valor) {
    const l = carrito[i];
    let n = Math.floor(Number(valor));
    if (!Number.isFinite(n) || n < 1) n = 1;
    if (n > l.stock) { n = l.stock; Notificar.aviso('Solo hay ' + l.stock + ' unidad(es) de «' + l.nombre + '».'); }
    l.cantidad = n;
  }

  BuscadorProductos.crear({
    input: $('buscarVenta'), lista: $('listaVenta'),
    deshabilitar: (p) => p.stock <= 0 || (p.fechaVencimiento && Fmt.diasHasta(p.fechaVencimiento) < 0),
    onSelect: (p) => {
      const ex = carrito.find((l) => l.id === p.id);
      if (ex) { if (ex.cantidad < ex.stock) ex.cantidad++; else Notificar.aviso('No hay más stock de «' + p.nombre + '».'); }
      else carrito.push({ id: p.id, codigo: p.codigo, nombre: p.nombre, precio: Number(p.precioVenta), stock: p.stock, cantidad: 1 });
      pintarCarrito();
      $('buscarVenta').focus();
    }
  });

  $('tbCarrito').addEventListener('click', (ev) => {
    const fila = ev.target.closest('tr[data-i]');
    if (!fila) return;
    const i = Number(fila.dataset.i);
    if (ev.target.closest('[data-quitar]')) { carrito.splice(i, 1); return pintarCarrito(); }
    const d = ev.target.closest('[data-delta]');
    if (d) { fijarCantidad(i, carrito[i].cantidad + Number(d.dataset.delta)); pintarCarrito(); }
  });
  $('tbCarrito').addEventListener('change', (ev) => {
    const fila = ev.target.closest('tr[data-i]');
    if (fila && ev.target.matches('input[type=number]')) { fijarCantidad(Number(fila.dataset.i), ev.target.value); pintarCarrito(); }
  });

  $('btnLimpiar').addEventListener('click', async () => {
    if (!carrito.length) return;
    if (await Modal.confirmar({ titulo: 'Vaciar carrito', mensaje: '¿Desea quitar todos los productos de la venta?', textoConfirmar: 'Vaciar', peligro: true })) { carrito = []; pintarCarrito(); }
  });

  function lineaComprobante(c) {
    return '<div class="cmp-cab"><h3>Botica Matifarma</h3><div>Comprobante de venta ' + Fmt.esc(c.numero) + '</div><small>' + Fmt.fechaHora(c.fecha) + '</small></div>' +
      '<div class="mb-2 small"><div><b>Cliente:</b> ' + Fmt.esc(c.cliente) + (c.documento ? ' (' + Fmt.esc(c.documento) + ')' : '') + '</div>' +
      '<div><b>Atendió:</b> ' + Fmt.esc(c.vendedor) + ' · <b>Pago:</b> ' + Fmt.esc(c.tipoPago) + '</div></div>' +
      '<table><thead><tr><th>Producto</th><th class="text-end">Cant.</th><th class="text-end">P. unit.</th><th class="text-end">Importe</th></tr></thead><tbody>' +
      c.lineas.map((l) => '<tr><td>' + Fmt.esc(l.producto) + '</td><td class="text-end">' + l.cantidad + '</td><td class="text-end">' + Fmt.moneda(l.precioUnitario) + '</td><td class="text-end">' + Fmt.moneda(l.subtotal) + '</td></tr>').join('') +
      '</tbody></table><div class="cmp-total"><span>TOTAL</span><span>' + Fmt.moneda(c.total) + '</span></div><p class="text-center small text-muted mt-3 mb-0">¡Gracias por su compra!</p>';
  }

  function verComprobante(c) { $('comprobante').innerHTML = lineaComprobante(c); Modal.abrir('modalComprobante'); }

  $('btnConfirmar').addEventListener('click', async function () {
    if (!carrito.length) return Notificar.aviso('Agregue al menos un producto para registrar la venta.');
    const r = Validar.formulario(form, {
      tipoPago: { requerido: true, msgRequerido: 'Seleccione el tipo de pago' },
      clienteDocumento: { evaluarVacio: true, patron: /^(\d{8}|\d{11}|[A-Za-z0-9]{9,12})$/, msgPatron: 'Ingrese un DNI (8 dígitos), RUC (11) o carnet de extranjería válido',
        fn: (v, d) => (!v && d.clienteNombre) ? 'Ingrese el documento o deje ambos campos vacíos' : null },
      clienteNombre: { max: 120, etiqueta: 'El nombre del cliente' }
    });
    if (!r.valido) return;
    const ok = await Modal.confirmar({ titulo: 'Confirmar venta', mensaje: 'Se registrará la venta por ' + Fmt.moneda(total()) + ' (' + r.datos.tipoPago + ') y se descontará el stock. ¿Continuar?', textoConfirmar: 'Registrar venta' });
    if (!ok) return;
    const cuerpo = {
      tipoPago: r.datos.tipoPago, clienteDocumento: r.datos.clienteDocumento || null, clienteNombre: r.datos.clienteNombre || null, token,
      detalles: carrito.map((l) => ({ idProducto: l.id, cantidad: l.cantidad }))
    };
    Validar.enviarUnaVez(this, async () => {
      try {
        const comprobante = await Api.post('/api/ventas', cuerpo);
        Notificar.exito('Venta ' + comprobante.numero + ' registrada correctamente.');
        carrito = []; token = Fmt.uuid(); form.reset(); Validar.limpiar(form); pintarCarrito();
        verComprobante(comprobante);
        cargarRecientes();
      } catch (e) {
        if (!Validar.errorServidor(form, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje);
      }
    });
  });

  async function cargarRecientes() {
    try {
      const v = await Api.get('/api/ventas/recientes');
      $('tbRecientes').innerHTML = v.length ? v.map((x) =>
        '<tr><td><strong>' + Fmt.esc(x.numero) + '</strong></td><td>' + Fmt.fechaHora(x.fecha) + '</td><td>' + Fmt.esc(x.cliente) + '</td>' +
        '<td><span class="chip chip-purple">' + Fmt.esc(x.tipoPago) + '</span></td><td>' + Fmt.esc(x.vendedor) + '</td><td class="num"><strong>' + Fmt.moneda(x.total) + '</strong></td>' +
        '<td class="text-end"><button class="btn btn-sm btn-outline-primary" data-ver="' + x.id + '"><i class="bi bi-receipt"></i> Ver</button></td></tr>'
      ).join('') : '<tr><td colspan="7" class="vacio"><i class="bi bi-receipt"></i>Todavía no hay ventas registradas.</td></tr>';
    } catch (e) { Notificar.error(e.mensaje); }
  }

  $('tbRecientes').addEventListener('click', async (ev) => {
    const b = ev.target.closest('[data-ver]');
    if (!b) return;
    try { verComprobante(await Api.get('/api/ventas/' + b.dataset.ver + '/comprobante')); } catch (e) { Notificar.error(e.mensaje); }
  });
  $('btnImprimir').addEventListener('click', () => window.print());

  pintarCarrito();
  cargarRecientes();
})();
