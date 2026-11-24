/* Modales reutilizables: confirmación de operaciones importantes y helpers de apertura */
(function (w) {
  'use strict';

  function confirmar(opciones) {
    const o = Object.assign({ titulo: 'Confirmar', mensaje: '', textoConfirmar: 'Confirmar', peligro: false }, opciones);
    return new Promise((resolve) => {
      document.getElementById('modalConfirmar')?.remove();
      const el = document.createElement('div');
      el.className = 'modal fade' + (o.peligro ? ' modal-danger' : '');
      el.id = 'modalConfirmar';
      el.tabIndex = -1;
      el.innerHTML =
        '<div class="modal-dialog modal-dialog-centered"><div class="modal-content">' +
        '<div class="modal-header"><h2 class="modal-title"></h2><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button></div>' +
        '<div class="modal-body"><p class="mb-0" data-msg></p></div>' +
        '<div class="modal-footer"><button type="button" class="btn btn-outline-primary" data-bs-dismiss="modal">Cancelar</button>' +
        '<button type="button" class="btn ' + (o.peligro ? 'btn-danger' : 'btn-accent') + '" data-ok></button></div></div></div>';
      el.querySelector('.modal-title').textContent = o.titulo;
      el.querySelector('[data-msg]').textContent = o.mensaje;
      el.querySelector('[data-ok]').textContent = o.textoConfirmar;
      document.body.appendChild(el);
      const m = new bootstrap.Modal(el);
      let aceptado = false;
      el.querySelector('[data-ok]').addEventListener('click', () => { aceptado = true; m.hide(); });
      el.addEventListener('hidden.bs.modal', () => { el.remove(); resolve(aceptado); });
      m.show();
    });
  }

  w.Modal = {
    confirmar,
    abrir: (id) => bootstrap.Modal.getOrCreateInstance(document.getElementById(id)).show(),
    cerrar: (id) => bootstrap.Modal.getOrCreateInstance(document.getElementById(id)).hide()
  };
})(window);
