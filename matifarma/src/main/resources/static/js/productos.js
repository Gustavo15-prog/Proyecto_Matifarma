(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const form = $('formProducto');
  let productos = [], editandoId = null;

  const esquema = {
    codigo: { requerido: true, etiqueta: 'El código', min: 3, max: 20, patron: /^[A-Za-z0-9-]+$/, msgPatron: 'El código solo admite letras, números y guiones' },
    nombre: { requerido: true, etiqueta: 'El nombre', min: 3, max: 120 },
    descripcion: { max: 255, etiqueta: 'La descripción' },
    idCategoria: { requerido: true, msgRequerido: 'Seleccione una categoría' },
    precioCompra: { requerido: true, etiqueta: 'El precio de compra', mayorQue: 0, decimales: 2, numero: true },
    precioVenta: { requerido: true, etiqueta: 'El precio de venta', mayorQue: 0, decimales: 2, numero: true },
    stockMinimo: { requerido: true, etiqueta: 'El stock mínimo', entero: true, minimo: 0, maximo: 1000000 },
    fechaVencimiento: { fecha: true, etiqueta: 'La fecha de vencimiento', fn: (v) => (editandoId === null && v < Validar.hoy()) ? 'La fecha de vencimiento no puede ser anterior a hoy' : null },
    ubicacion: { max: 60, etiqueta: 'La ubicación' }
  };

  function pintar() {
    $('contador').textContent = productos.length + ' producto(s)';
    $('tbody').innerHTML = productos.length ? productos.map((p) => {
      const clase = p.stock <= p.stockMinimo ? 'text-danger' : '';
      return '<tr class="' + (p.activo ? '' : 'inactivo') + '"><td><code>' + Fmt.esc(p.codigo) + '</code></td>' +
        '<td><strong>' + Fmt.esc(p.nombre) + '</strong></td><td>' + Fmt.esc(p.categoria) + '</td>' +
        '<td class="num">' + Fmt.moneda(p.precioCompra) + '</td><td class="num">' + Fmt.moneda(p.precioVenta) + '</td>' +
        '<td class="num stock-col ' + clase + '">' + p.stock + '</td><td class="num">' + p.stockMinimo + '</td>' +
        '<td>' + Fmt.fecha(p.fechaVencimiento) + '</td><td>' + Fmt.chipEstado(p) + '</td>' +
        '<td class="text-end text-nowrap"><button class="btn btn-icon btn-outline-primary" data-accion="editar" data-id="' + p.id + '" title="Editar" aria-label="Editar ' + Fmt.esc(p.nombre) + '"><i class="bi bi-pencil"></i></button> ' +
        '<button class="btn btn-icon ' + (p.activo ? 'btn-outline-danger' : 'btn-outline-primary') + '" data-accion="estado" data-id="' + p.id + '" title="' + (p.activo ? 'Desactivar' : 'Activar') + '" aria-label="' + (p.activo ? 'Desactivar' : 'Activar') + ' ' + Fmt.esc(p.nombre) + '"><i class="bi ' + (p.activo ? 'bi-slash-circle' : 'bi-check-circle') + '"></i></button></td></tr>';
    }).join('') : '<tr><td colspan="10" class="vacio"><i class="bi bi-capsule"></i>No hay productos que coincidan con la búsqueda.</td></tr>';
  }

  async function cargar() {
    const params = new URLSearchParams();
    if ($('txtBuscar').value.trim()) params.set('q', $('txtBuscar').value.trim());
    if ($('selCategoriaFiltro').value) params.set('idCategoria', $('selCategoriaFiltro').value);
    if ($('chkInactivos').checked) params.set('todos', 'true');
    try { productos = await Api.get('/api/productos?' + params); pintar(); }
    catch (e) { Notificar.error(e.mensaje); }
  }

  async function cargarCombos(seleccionCategoria) {
    const [cats, provs] = await Promise.all([Api.get('/api/categorias'), Api.get('/api/proveedores/activos')]);
    const opts = cats.map((c) => '<option value="' + c.id + '">' + Fmt.esc(c.nombre) + '</option>').join('');
    $('idCategoria').innerHTML = '<option value="">Seleccione…</option>' + opts;
    const filtro = $('selCategoriaFiltro').value;
    $('selCategoriaFiltro').innerHTML = '<option value="">Todas las categorías</option>' + opts;
    $('selCategoriaFiltro').value = filtro;
    $('idProveedor').innerHTML = '<option value="">Sin proveedor</option>' + provs.map((p) => '<option value="' + p.id + '">' + Fmt.esc(p.razonSocial) + '</option>').join('');
    if (seleccionCategoria) $('idCategoria').value = seleccionCategoria;
  }

  function abrirFormulario(p) {
    editandoId = p ? p.id : null;
    Validar.limpiar(form);
    form.reset();
    $('tituloProducto').textContent = p ? 'Editar producto' : 'Nuevo producto';
    if (p) {
      ['codigo', 'nombre', 'descripcion', 'idCategoria', 'precioCompra', 'precioVenta', 'stockMinimo', 'fechaVencimiento', 'ubicacion']
        .forEach((k) => { form.elements[k].value = p[k] == null ? '' : p[k]; });
      form.elements.idProveedor.value = p.idProveedor == null ? '' : p.idProveedor;
    }
    Modal.abrir('modalProducto');
  }

  $('btnGuardar').addEventListener('click', function () {
    const r = Validar.formulario(form, esquema);
    if (!r.valido) return;
    const d = r.datos;
    const cuerpo = {
      codigo: d.codigo.toUpperCase(), nombre: d.nombre, descripcion: d.descripcion || null,
      idCategoria: Number(d.idCategoria), idProveedor: d.idProveedor ? Number(d.idProveedor) : null,
      precioCompra: Number(d.precioCompra), precioVenta: Number(d.precioVenta), stockMinimo: Number(d.stockMinimo),
      fechaVencimiento: d.fechaVencimiento || null, ubicacion: d.ubicacion || null
    };
    Validar.enviarUnaVez(this, async () => {
      try {
        if (editandoId === null) await Api.post('/api/productos', cuerpo); else await Api.put('/api/productos/' + editandoId, cuerpo);
        Modal.cerrar('modalProducto');
        Notificar.exito(editandoId === null ? 'Producto registrado correctamente.' : 'Producto actualizado correctamente.');
        cargar();
      } catch (e) {
        if (!Validar.errorServidor(form, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje);
      }
    });
  });

  $('tbody').addEventListener('click', async (ev) => {
    const btn = ev.target.closest('button[data-accion]');
    if (!btn) return;
    const p = productos.find((x) => x.id === Number(btn.dataset.id));
    if (!p) return;
    if (btn.dataset.accion === 'editar') return abrirFormulario(p);
    const desactivar = p.activo;
    const ok = await Modal.confirmar({
      titulo: desactivar ? 'Desactivar producto' : 'Activar producto',
      mensaje: desactivar ? '«' + p.nombre + '» dejará de aparecer en ventas e ingresos. Su historial se conserva.' : '«' + p.nombre + '» volverá a estar disponible.',
      textoConfirmar: desactivar ? 'Desactivar' : 'Activar', peligro: desactivar
    });
    if (!ok) return;
    try { await Api.patch('/api/productos/' + p.id + '/estado', { activo: !p.activo }); Notificar.exito('Estado actualizado.'); cargar(); }
    catch (e) { Notificar.error(e.mensaje); }
  });

  $('btnNuevo').addEventListener('click', () => abrirFormulario(null));
  $('btnNuevaCategoria').addEventListener('click', () => { Validar.limpiar($('formCategoria')); $('formCategoria').reset(); Modal.abrir('modalCategoria'); });
  $('btnGuardarCategoria').addEventListener('click', function () {
    const f = $('formCategoria');
    const r = Validar.formulario(f, { nombre: { requerido: true, etiqueta: 'El nombre', min: 3, max: 60 } });
    if (!r.valido) return;
    Validar.enviarUnaVez(this, async () => {
      try {
        const c = await Api.post('/api/categorias', { nombre: r.datos.nombre });
        Modal.cerrar('modalCategoria');
        Notificar.exito('Categoría creada.');
        await cargarCombos();
        void c;
      } catch (e) { if (!Validar.errorServidor(f, e.errores)) Notificar.error(e.mensaje); }
    });
  });

  $('txtBuscar').addEventListener('input', Fmt.debounce(cargar, 300));
  $('selCategoriaFiltro').addEventListener('change', cargar);
  $('chkInactivos').addEventListener('change', cargar);
  cargarCombos().then(cargar).catch((e) => Notificar.error(e.mensaje));
})();
