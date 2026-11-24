/* Menú lateral: contraer/expandir (escritorio) y menú deslizable (móvil) */
(function () {
  'use strict';
  const KEY = 'matifarma.sidebar';
  const body = document.body;
  const btn = document.getElementById('btnColapsar');

  function aplicarAria() {
    if (btn) {
      const contraido = body.classList.contains('sidebar-collapsed');
      btn.setAttribute('aria-label', contraido ? 'Expandir menú' : 'Contraer menú');
      btn.setAttribute('aria-expanded', String(!contraido));
    }
  }

  try { if (localStorage.getItem(KEY) === '1') body.classList.add('sidebar-collapsed'); } catch (e) { /* almacenamiento no disponible */ }
  aplicarAria();

  btn?.addEventListener('click', () => {
    const contraido = body.classList.toggle('sidebar-collapsed');
    try { localStorage.setItem(KEY, contraido ? '1' : '0'); } catch (e) { /* ignorar */ }
    aplicarAria();
  });

  document.getElementById('btnMenuMovil')?.addEventListener('click', () => body.classList.toggle('sidebar-open'));
  document.getElementById('sidebarBackdrop')?.addEventListener('click', () => body.classList.remove('sidebar-open'));
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') body.classList.remove('sidebar-open'); });
  document.querySelectorAll('.sidebar .nav-item').forEach((a) => a.addEventListener('click', () => body.classList.remove('sidebar-open')));
})();
