(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const form = $('formProveedor');
  let lista = [], editandoId = null;

  Validar.soloDigitos(form.elements.ruc);

  const esquema = {
    razonSocial: { requerido: true, etiqueta: 'La razón social', min: 3, max: 150 },
    ruc: { requerido: true, etiqueta: 'El RUC', patron: /^\d{11}$/, msgPatron: 'El RUC debe tener exactamente 11 dígitos numéricos' },
    telefono: { requerido: true, etiqueta: 'El teléfono', patron: /^\+?[0-9 -]{7,20}$/, msgPatron: 'Teléfono inválido (7 a 20 dígitos; se permiten +, espacios y guiones)' },
    correo: { email: true, max: 120, etiqueta: 'El correo' },
    direccion: { max: 200, etiqueta: 'La dirección' }
  };

  function pintar() {
    $('contador').textContent = lista.length + ' proveedor(es)';
    $('tbody').innerHTML = lista.length ? lista.map((p) =>
      '<tr class="' + (p.activo ? '' : 'inactivo') + '"><td><strong>' + Fmt.esc(p.razonSocial) + '</strong></td><td class="ruc">' + Fmt.esc(p.ruc) + '</td>' +
      '<td>' + Fmt.esc(p.telefono) + '</td><td>' + Fmt.esc(p.correo || '—') + '</td><td>' + Fmt.esc(p.direccion || '—') + '</td>' +
      '<td><span class="chip ' + (p.activo ? 'chip-ok' : 'chip-neutral') + '">' + (p.activo ? 'Activo' : 'Inactivo') + '</span></td>' +
      '<td class="text-end text-nowrap"><button class="btn btn-icon btn-outline-primary" data-accion="editar" data-id="' + p.id + '" title="Editar" aria-label="Editar ' + Fmt.esc(p.razonSocial) + '"><i class="bi bi-pencil"></i></button> ' +
      '<button class="btn btn-icon ' + (p.activo ? 'btn-outline-danger' : 'btn-outline-primary') + '" data-accion="estado" data-id="' + p.id + '" title="' + (p.activo ? 'Desactivar' : 'Activar') + '" aria-label="' + (p.activo ? 'Desactivar' : 'Activar') + ' ' + Fmt.esc(p.razonSocial) + '"><i class="bi ' + (p.activo ? 'bi-slash-circle' : 'bi-check-circle') + '"></i></button></td></tr>'
    ).join('') : '<tr><td colspan="7" class="vacio"><i class="bi bi-truck"></i>No se encontraron proveedores.</td></tr>';
  }

  async function cargar() {
    const q = $('txtBuscar').value.trim();
    try { lista = await Api.get('/api/proveedores?todos=true' + (q ? '&q=' + encodeURIComponent(q) : '')); pintar(); }
    catch (e) { Notificar.error(e.mensaje); }
  }

  function abrir(p) {
    editandoId = p ? p.id : null;
    Validar.limpiar(form);
    form.reset();
    $('tituloProveedor').textContent = p ? 'Editar proveedor' : 'Nuevo proveedor';
    if (p) ['razonSocial', 'ruc', 'telefono', 'correo', 'direccion'].forEach((k) => { form.elements[k].value = p[k] == null ? '' : p[k]; });
    Modal.abrir('modalProveedor');
  }

  $('btnGuardar').addEventListener('click', function () {
    const r = Validar.formulario(form, esquema);
    if (!r.valido) return;
    const d = r.datos;
    const cuerpo = { razonSocial: d.razonSocial, ruc: d.ruc, telefono: d.telefono, correo: d.correo || null, direccion: d.direccion || null };
    Validar.enviarUnaVez(this, async () => {
      try {
        if (editandoId === null) await Api.post('/api/proveedores', cuerpo); else await Api.put('/api/proveedores/' + editandoId, cuerpo);
        Modal.cerrar('modalProveedor');
        Notificar.exito(editandoId === null ? 'Proveedor registrado correctamente.' : 'Proveedor actualizado correctamente.');
        cargar();
      } catch (e) { if (!Validar.errorServidor(form, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje); }
    });
  });

  $('tbody').addEventListener('click', async (ev) => {
    const btn = ev.target.closest('button[data-accion]');
    if (!btn) return;
    const p = lista.find((x) => x.id === Number(btn.dataset.id));
    if (!p) return;
    if (btn.dataset.accion === 'editar') return abrir(p);
    const ok = await Modal.confirmar({
      titulo: p.activo ? 'Desactivar proveedor' : 'Activar proveedor',
      mensaje: p.activo ? '«' + p.razonSocial + '» ya no podrá usarse en nuevos ingresos.' : '«' + p.razonSocial + '» volverá a estar disponible.',
      textoConfirmar: p.activo ? 'Desactivar' : 'Activar', peligro: p.activo
    });
    if (!ok) return;
    try { await Api.patch('/api/proveedores/' + p.id + '/estado', { activo: !p.activo }); Notificar.exito('Estado actualizado.'); cargar(); }
    catch (e) { Notificar.error(e.mensaje); }
  });

  $('btnNuevo').addEventListener('click', () => abrir(null));
  $('txtBuscar').addEventListener('input', Fmt.debounce(cargar, 300));
  cargar();
})();
