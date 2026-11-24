/* Buscador de productos con autocompletado (usa /api/productos/buscar) */
(function (w) {
  'use strict';

  function crear(opts) {
    const input = opts.input, lista = opts.lista;
    let items = [], pos = -1;

    function cerrar() { lista.hidden = true; pos = -1; }

    function resaltar() {
      lista.querySelectorAll('.resultado').forEach((b, i) => b.classList.toggle('activo', i === pos));
    }

    function pintar() {
      lista.innerHTML = items.length ? items.map((p, i) =>
        '<button type="button" class="resultado" role="option" data-i="' + i + '"' + (opts.deshabilitar && opts.deshabilitar(p) ? ' disabled' : '') + '>' +
        '<span><strong>' + Fmt.esc(p.nombre) + '</strong><small>' + Fmt.esc(p.codigo) + ' · ' + Fmt.esc(p.categoria) + '</small></span>' +
        '<span class="text-end"><b>' + (opts.precio ? opts.precio(p) : Fmt.moneda(p.precioVenta)) + '</b><small>Stock ' + p.stock + '</small></span></button>'
      ).join('') : '<div class="resultado-vacio">No se encontraron productos.</div>';
      lista.hidden = false;
      pos = -1;
    }

    function elegir(p) {
      if (!p || (opts.deshabilitar && opts.deshabilitar(p))) return;
      cerrar();
      input.value = '';
      opts.onSelect(p);
    }

    const buscar = Fmt.debounce(async () => {
      const q = input.value.trim();
      if (q.length < 2) { cerrar(); return; }
      try { items = await Api.get('/api/productos/buscar?q=' + encodeURIComponent(q)); pintar(); }
      catch (e) { Notificar.error(e.mensaje); }
    }, 250);

    input.addEventListener('input', buscar);
    input.addEventListener('keydown', (e) => {
      if (lista.hidden) return;
      if (e.key === 'ArrowDown') { e.preventDefault(); pos = Math.min(pos + 1, items.length - 1); resaltar(); }
      else if (e.key === 'ArrowUp') { e.preventDefault(); pos = Math.max(pos - 1, 0); resaltar(); }
      else if (e.key === 'Enter') { e.preventDefault(); elegir(items[pos >= 0 ? pos : 0]); }
      else if (e.key === 'Escape') cerrar();
    });
    lista.addEventListener('click', (e) => {
      const b = e.target.closest('[data-i]');
      if (b) elegir(items[Number(b.dataset.i)]);
    });
    document.addEventListener('click', (e) => { if (e.target !== input && !lista.contains(e.target)) cerrar(); });
    return { cerrar };
  }

  w.BuscadorProductos = { crear };
})(window);
