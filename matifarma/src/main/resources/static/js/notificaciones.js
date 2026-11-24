/* Notificaciones tipo toast (Bootstrap) */
(function (w) {
  'use strict';
  const ICONOS = { ok: 'bi-check-circle-fill', error: 'bi-x-octagon-fill', aviso: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };

  function contenedor() {
    let c = document.getElementById('toastContainer');
    if (!c) {
      c = document.createElement('div');
      c.id = 'toastContainer';
      c.className = 'toast-container position-fixed bottom-0 end-0 p-3';
      c.style.zIndex = '2000';
      document.body.appendChild(c);
    }
    return c;
  }

  function mostrar(tipo, mensaje, ms) {
    const el = document.createElement('div');
    el.className = 'toast toast-mf toast-' + tipo + ' border-0';
    el.setAttribute('role', tipo === 'error' ? 'alert' : 'status');
    el.innerHTML = '<div class="d-flex"><div class="toast-body"><i class="bi ' + ICONOS[tipo] + ' me-2"></i><span></span></div>' +
      '<button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Cerrar"></button></div>';
    el.querySelector('span').textContent = mensaje;
    contenedor().appendChild(el);
    el.addEventListener('hidden.bs.toast', () => el.remove());
    new bootstrap.Toast(el, { delay: ms }).show();
  }

  w.Notificar = {
    exito: (m) => mostrar('ok', m, 4500),
    error: (m) => mostrar('error', m, 8000),
    aviso: (m) => mostrar('aviso', m, 7000),
    info: (m) => mostrar('info', m, 4500)
  };
})(window);
