(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const form = $('formUsuario');
  const ROLES = { ADMINISTRADOR: 'Administrador', VENDEDOR: 'Vendedor', ALMACENERO: 'Almacenero' };
  let lista = [], editandoId = null;

  function esquema() {
    const crear = editandoId === null;
    return {
      usuario: { requerido: true, etiqueta: 'El usuario', min: 4, max: 30, patron: /^[A-Za-z0-9._]+$/, msgPatron: 'El usuario solo admite letras, números, punto y guion bajo' },
      nombres: { requerido: true, etiqueta: 'Los nombres', min: 3, max: 120 },
      correo: { requerido: true, etiqueta: 'El correo', email: true, max: 120 },
      rol: { requerido: true, msgRequerido: 'Seleccione un rol' },
      password: {
        requerido: crear, etiqueta: 'La contraseña', sinRecorte: true, max: 64,
        fn: (v) => {
          if (!v) return null;
          if (v.length < 8) return 'La contraseña debe tener al menos 8 caracteres';
          if (/\s/.test(v)) return 'La contraseña no puede tener espacios';
          if (!/[a-z]/.test(v) || !/[A-Z]/.test(v) || !/\d/.test(v)) return 'Debe incluir mayúscula, minúscula y número';
          return null;
        }
      },
      confirmarPassword: { sinRecorte: true, evaluarVacio: true, fn: (v, d) => (d.password || v) && v !== d.password ? 'La confirmación no coincide con la contraseña' : null }
    };
  }

  function estado(u) {
    if (!u.activo) return '<span class="chip chip-neutral">Inactivo</span>';
    if (u.bloqueado) return '<span class="chip chip-danger"><i class="bi bi-lock-fill"></i> Bloqueado</span>';
    return '<span class="chip chip-ok">Activo</span>';
  }

  function pintar() {
    $('tbody').innerHTML = lista.length ? lista.map((u) =>
      '<tr class="' + (u.activo ? '' : 'inactivo') + '"><td><div class="usuario-cell"><span class="avatar" aria-hidden="true">' + Fmt.esc(u.nombres.charAt(0).toUpperCase()) + '</span><strong>' + Fmt.esc(u.usuario) + '</strong></div></td>' +
      '<td>' + Fmt.esc(u.nombres) + '</td><td>' + Fmt.esc(u.correo) + '</td>' +
      '<td><span class="chip ' + (u.rol === 'ADMINISTRADOR' ? 'rol-admin' : 'chip-purple') + '">' + ROLES[u.rol] + '</span></td><td>' + estado(u) + '</td>' +
      '<td class="text-end text-nowrap">' +
      (u.bloqueado ? '<button class="btn btn-icon btn-outline-primary" data-accion="desbloquear" data-id="' + u.id + '" title="Desbloquear" aria-label="Desbloquear ' + Fmt.esc(u.usuario) + '"><i class="bi bi-unlock"></i></button> ' : '') +
      '<button class="btn btn-icon btn-outline-primary" data-accion="editar" data-id="' + u.id + '" title="Editar" aria-label="Editar ' + Fmt.esc(u.usuario) + '"><i class="bi bi-pencil"></i></button> ' +
      '<button class="btn btn-icon ' + (u.activo ? 'btn-outline-danger' : 'btn-outline-primary') + '" data-accion="estado" data-id="' + u.id + '" title="' + (u.activo ? 'Desactivar' : 'Activar') + '" aria-label="' + (u.activo ? 'Desactivar' : 'Activar') + ' ' + Fmt.esc(u.usuario) + '"><i class="bi ' + (u.activo ? 'bi-slash-circle' : 'bi-check-circle') + '"></i></button></td></tr>'
    ).join('') : '<tr><td colspan="6" class="vacio">No hay usuarios.</td></tr>';
  }

  async function cargar() {
    try { lista = await Api.get('/api/usuarios'); pintar(); } catch (e) { Notificar.error(e.mensaje); }
  }

  function abrir(u) {
    editandoId = u ? u.id : null;
    Validar.limpiar(form);
    form.reset();
    $('tituloUsuario').textContent = u ? 'Editar usuario' : 'Nuevo usuario';
    $('lblPassword').className = 'form-label' + (u ? '' : ' req');
    $('ayudaPassword').innerHTML = '<i class="bi bi-shield-lock me-1"></i>' + (u ? 'Deje la contraseña en blanco para conservar la actual. ' : '') + 'Mínimo 8 caracteres, con mayúscula, minúscula y número. Sin espacios.';
    if (u) ['usuario', 'nombres', 'correo', 'rol'].forEach((k) => { form.elements[k].value = u[k]; });
    Modal.abrir('modalUsuario');
  }

  $('btnGuardar').addEventListener('click', function () {
    const r = Validar.formulario(form, esquema());
    if (!r.valido) return;
    const d = r.datos;
    const cuerpo = { usuario: d.usuario, nombres: d.nombres, correo: d.correo, rol: d.rol, password: d.password || null, confirmarPassword: d.confirmarPassword || null };
    Validar.enviarUnaVez(this, async () => {
      try {
        if (editandoId === null) await Api.post('/api/usuarios', cuerpo); else await Api.put('/api/usuarios/' + editandoId, cuerpo);
        Modal.cerrar('modalUsuario');
        Notificar.exito(editandoId === null ? 'Usuario creado correctamente.' : 'Usuario actualizado correctamente.');
        cargar();
      } catch (e) { if (!Validar.errorServidor(form, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje); }
    });
  });

  $('tbody').addEventListener('click', async (ev) => {
    const btn = ev.target.closest('button[data-accion]');
    if (!btn) return;
    const u = lista.find((x) => x.id === Number(btn.dataset.id));
    if (!u) return;
    try {
      if (btn.dataset.accion === 'editar') return abrir(u);
      if (btn.dataset.accion === 'desbloquear') {
        await Api.post('/api/usuarios/' + u.id + '/desbloquear');
        Notificar.exito('Usuario desbloqueado.');
        return cargar();
      }
      const ok = await Modal.confirmar({
        titulo: u.activo ? 'Desactivar usuario' : 'Activar usuario',
        mensaje: u.activo ? '«' + u.usuario + '» no podrá iniciar sesión hasta que se reactive.' : '«' + u.usuario + '» podrá volver a iniciar sesión.',
        textoConfirmar: u.activo ? 'Desactivar' : 'Activar', peligro: u.activo
      });
      if (!ok) return;
      await Api.patch('/api/usuarios/' + u.id + '/estado', { activo: !u.activo });
      Notificar.exito('Estado actualizado.');
      cargar();
    } catch (e) { Notificar.error(e.mensaje); }
  });

  $('btnNuevo').addEventListener('click', () => abrir(null));
  cargar();
})();
