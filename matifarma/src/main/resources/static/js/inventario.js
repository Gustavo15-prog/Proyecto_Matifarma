(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  let lineas = [];                 // líneas del ingreso en curso
  let tokenIngreso = Fmt.uuid();
  let tokenSalida = Fmt.uuid();
  let productoSalida = null;

  /* ---------- Stock actual ---------- */
  async function cargarStock() {
    const q = $('txtStock').value.trim();
    try {
      const datos = await Api.get('/api/stock' + (q ? '?q=' + encodeURIComponent(q) : ''));
      $('contStock').textContent = datos.length + ' producto(s)';
      $('tbStock').innerHTML = datos.length ? datos.map((p) =>
        '<tr><td><code>' + Fmt.esc(p.codigo) + '</code></td><td><strong>' + Fmt.esc(p.nombre) + '</strong></td><td>' + Fmt.esc(p.categoria) + '</td>' +
        '<td class="num"><strong>' + p.stock + '</strong></td><td class="num">' + p.stockMinimo + '</td><td>' + Fmt.esc(p.ubicacion || '—') + '</td>' +
        '<td>' + Fmt.fecha(p.fechaVencimiento) + '</td><td>' + Fmt.chipEstado(p) + '</td></tr>'
      ).join('') : '<tr><td colspan="8" class="vacio"><i class="bi bi-search"></i>No se encontraron productos.</td></tr>';
    } catch (e) { Notificar.error(e.mensaje); }
  }

  /* ---------- Movimientos ---------- */
  async function cargarMovimientos() {
    const q = $('txtMov').value.trim();
    try {
      const datos = await Api.get('/api/inventario/movimientos' + (q ? '?q=' + encodeURIComponent(q) : ''));
      $('tbMov').innerHTML = datos.length ? datos.map((m) =>
        '<tr><td>' + Fmt.fechaHora(m.fecha) + '</td><td><strong>' + Fmt.esc(m.producto) + '</strong><div class="small text-muted">' + Fmt.esc(m.codigo) + '</div></td>' +
        '<td><span class="chip ' + (m.tipo === 'ENTRADA' ? 'tipo-entrada' : 'tipo-salida') + '">' + (m.tipo === 'ENTRADA' ? 'Entrada' : 'Salida') + '</span></td>' +
        '<td class="num">' + m.cantidad + '</td><td class="num">' + m.stockResultante + '</td><td>' + Fmt.esc(m.motivo) + '</td><td>' + Fmt.esc(m.referencia || '—') + '</td><td>' + Fmt.esc(m.usuario) + '</td></tr>'
      ).join('') : '<tr><td colspan="8" class="vacio"><i class="bi bi-clock-history"></i>Aún no hay movimientos registrados.</td></tr>';
    } catch (e) { Notificar.error(e.mensaje); }
  }

  /* ---------- Ingreso ---------- */
  const subtotal = (l) => (Number(l.cantidad) || 0) * (Number(l.costo) || 0);

  function pintarIngreso() {
    $('tbIngreso').innerHTML = lineas.length ? lineas.map((l, i) => {
      const vencido = l.vencimiento && l.vencimiento < Fmt.hoyISO();
      return '<tr data-i="' + i + '"><td><strong>' + Fmt.esc(l.nombre) + '</strong><div class="small text-muted">' + Fmt.esc(l.codigo) + '</div></td>' +
        '<td><input type="number" min="1" step="1" class="form-control form-control-sm" data-campo="cantidad" value="' + Fmt.esc(l.cantidad) + '" aria-label="Cantidad de ' + Fmt.esc(l.nombre) + '"></td>' +
        '<td><input type="number" min="0.01" step="0.01" class="form-control form-control-sm" data-campo="costo" value="' + Fmt.esc(l.costo) + '" aria-label="Costo de ' + Fmt.esc(l.nombre) + '"></td>' +
        '<td><input type="date" class="form-control form-control-sm" data-campo="vencimiento" value="' + Fmt.esc(l.vencimiento) + '" aria-label="Vencimiento de ' + Fmt.esc(l.nombre) + '">' +
        (vencido ? '<span class="venc-aviso">Vencido: no se sumará al stock</span>' : '') + '</td>' +
        '<td class="num" data-sub>' + Fmt.moneda(subtotal(l)) + '</td>' +
        '<td class="text-end"><button type="button" class="btn btn-icon btn-outline-danger" data-quitar="' + i + '" aria-label="Quitar ' + Fmt.esc(l.nombre) + '"><i class="bi bi-trash"></i></button></td></tr>';
    }).join('') : '<tr><td colspan="6" class="vacio"><i class="bi bi-box-seam"></i>Busque y agregue los productos recibidos.</td></tr>';
    actualizarResumen();
  }

  function actualizarResumen() {
    const validas = lineas.filter((l) => !(l.vencimiento && l.vencimiento < Fmt.hoyISO()));
    $('resLineas').textContent = lineas.length;
    $('resUnidades').textContent = lineas.reduce((a, l) => a + (Number(l.cantidad) || 0), 0);
    $('resTotal').textContent = Fmt.moneda(validas.reduce((a, l) => a + subtotal(l), 0));
  }

  BuscadorProductos.crear({
    input: $('buscarIngreso'), lista: $('listaIngreso'),
    precio: (p) => (p.precioCompra != null ? Fmt.moneda(p.precioCompra) : Fmt.moneda(p.precioVenta)),
    onSelect: (p) => {
      if (lineas.some((l) => l.idProducto === p.id)) { Notificar.aviso('«' + p.nombre + '» ya está en la lista. Modifique su cantidad.'); return; }
      lineas.push({ idProducto: p.id, nombre: p.nombre, codigo: p.codigo, cantidad: 1, costo: p.precioCompra != null ? Number(p.precioCompra).toFixed(2) : '', vencimiento: '' });
      pintarIngreso();
      $('tbIngreso').querySelector('tr:last-child input')?.focus();
    }
  });

  $('tbIngreso').addEventListener('input', (ev) => {
    const campo = ev.target.dataset.campo;
    if (!campo) return;
    const fila = ev.target.closest('tr');
    const l = lineas[Number(fila.dataset.i)];
    l[campo] = ev.target.value;
    fila.querySelector('[data-sub]').textContent = Fmt.moneda(subtotal(l));
    actualizarResumen();
  });
  $('tbIngreso').addEventListener('change', (ev) => { if (ev.target.dataset.campo === 'vencimiento') pintarIngreso(); });
  $('tbIngreso').addEventListener('click', (ev) => {
    const b = ev.target.closest('[data-quitar]');
    if (b) { lineas.splice(Number(b.dataset.quitar), 1); pintarIngreso(); }
  });

  function validarLineas() {
    for (const l of lineas) {
      const cant = Number(l.cantidad), costo = Number(l.costo);
      if (!Number.isInteger(cant) || cant <= 0) return 'La cantidad de «' + l.nombre + '» debe ser un entero mayor que cero.';
      if (!(costo > 0) || (String(l.costo).split('.')[1] || '').length > 2) return 'El costo de «' + l.nombre + '» debe ser mayor que cero (máx. 2 decimales).';
      if (l.vencimiento && !Validar.fechaValida(l.vencimiento)) return 'La fecha de vencimiento de «' + l.nombre + '» no es válida.';
    }
    return null;
  }

  $('btnRegistrarIngreso').addEventListener('click', async function () {
    const f = $('formIngresoCab');
    const r = Validar.formulario(f, {
      idProveedor: { requerido: true, msgRequerido: 'Seleccione un proveedor' },
      numeroDocumento: { requerido: true, etiqueta: 'El número de documento', max: 30, patron: /^[A-Za-z0-9\-/ ]+$/, msgPatron: 'Solo letras, números, guiones y barras' }
    });
    if (!r.valido) return;
    if (!lineas.length) return Notificar.aviso('Agregue al menos un producto al ingreso.');
    const err = validarLineas();
    if (err) return Notificar.aviso(err);
    const ok = await Modal.confirmar({ titulo: 'Registrar ingreso', mensaje: 'Se registrará el ingreso de ' + lineas.length + ' producto(s) por ' + $('resTotal').textContent + ' y se actualizará el stock. ¿Continuar?', textoConfirmar: 'Registrar ingreso' });
    if (!ok) return;
    const cuerpo = {
      idProveedor: Number(r.datos.idProveedor), numeroDocumento: r.datos.numeroDocumento.toUpperCase(), token: tokenIngreso,
      detalles: lineas.map((l) => ({ idProducto: l.idProducto, cantidad: Number(l.cantidad), costoUnitario: Number(l.costo), fechaVencimiento: l.vencimiento || null }))
    };
    Validar.enviarUnaVez(this, async () => {
      try {
        const res = await Api.post('/api/inventario/ingresos', cuerpo);
        Notificar.exito('Ingreso registrado: ' + res.lineasIngresadas + ' producto(s) sumados al stock.');
        if (res.noConformes.length) Notificar.aviso('No conformes (no ingresaron al stock): ' + res.noConformes.join('; '));
        lineas = []; tokenIngreso = Fmt.uuid(); f.reset(); Validar.limpiar(f); pintarIngreso();
        cargarStock();
      } catch (e) {
        if (!Validar.errorServidor(f, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje);
      }
    });
  });

  /* ---------- Salida ---------- */
  function mostrarSeleccionSalida() {
    const box = $('selSalida');
    if (!productoSalida) { box.hidden = true; $('idProductoSalida').value = ''; return; }
    box.hidden = false;
    box.innerHTML = '<div><strong>' + Fmt.esc(productoSalida.nombre) + '</strong><small>' + Fmt.esc(productoSalida.codigo) + '</small></div>' +
      '<div class="text-end"><small>Stock disponible</small><strong class="fs-5">' + productoSalida.stock + '</strong></div>';
    $('idProductoSalida').value = productoSalida.id;
  }

  BuscadorProductos.crear({
    input: $('buscarSalida'), lista: $('listaSalida'),
    onSelect: (p) => { productoSalida = p; mostrarSeleccionSalida(); $('cantidadSalida').focus(); }
  });

  $('btnRegistrarSalida').addEventListener('click', async function () {
    const f = $('formSalida');
    if (!productoSalida) return Notificar.aviso('Busque y seleccione el producto de la salida.');
    const r = Validar.formulario(f, {
      cantidad: { requerido: true, etiqueta: 'La cantidad', entero: true, mayorQue: 0, maximo: productoSalida.stock, numero: true,
        fn: (v) => Number(v) > productoSalida.stock ? 'La salida no puede superar el stock disponible (' + productoSalida.stock + ')' : null },
      motivo: { requerido: true, msgRequerido: 'Seleccione el motivo' },
      detalle: { max: 70, etiqueta: 'El detalle' }
    });
    if (!r.valido) return;
    const motivo = r.datos.motivo + (r.datos.detalle ? ' - ' + r.datos.detalle : '');
    const ok = await Modal.confirmar({ titulo: 'Registrar salida', mensaje: 'Se descontarán ' + r.datos.cantidad + ' unidad(es) de «' + productoSalida.nombre + '». ¿Continuar?', textoConfirmar: 'Registrar salida', peligro: true });
    if (!ok) return;
    Validar.enviarUnaVez(this, async () => {
      try {
        const res = await Api.post('/api/inventario/salidas', { idProducto: productoSalida.id, cantidad: Number(r.datos.cantidad), motivo, token: tokenSalida });
        Notificar.exito('Salida registrada. Stock actual: ' + res.stockResultante + '.');
        productoSalida = null; tokenSalida = Fmt.uuid(); f.reset(); Validar.limpiar(f); mostrarSeleccionSalida();
        cargarStock();
      } catch (e) {
        if (!Validar.errorServidor(f, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje);
      }
    });
  });

  /* ---------- Inicio ---------- */
  document.querySelector('[data-bs-target="#tabMov"]').addEventListener('shown.bs.tab', cargarMovimientos);
  document.querySelector('[data-bs-target="#tabStock"]').addEventListener('shown.bs.tab', cargarStock);
  $('txtStock').addEventListener('input', Fmt.debounce(cargarStock, 300));
  $('txtMov').addEventListener('input', Fmt.debounce(cargarMovimientos, 300));

  Api.get('/api/proveedores/activos').then((provs) => {
    $('idProveedor').innerHTML = '<option value="">Seleccione…</option>' + provs.map((p) => '<option value="' + p.id + '">' + Fmt.esc(p.razonSocial) + '</option>').join('');
  }).catch((e) => Notificar.error(e.mensaje));
  pintarIngreso();
  cargarStock();
})();
