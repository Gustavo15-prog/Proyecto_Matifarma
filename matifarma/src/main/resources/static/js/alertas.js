(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const TIPOS = {
    STOCK_BAJO: { texto: 'Stock bajo', clase: 'chip-warn' },
    POR_VENCER: { texto: 'Por vencer', clase: 'chip-warn' },
    VENCIDO: { texto: 'Vencido', clase: 'chip-danger' }
  };
  let alertas = [], filtro = '';

  function pintar() {
    const lista = alertas.filter((a) => !filtro || a.tipo === filtro);
    $('nTodas').textContent = alertas.length;
    $('nVencido').textContent = alertas.filter((a) => a.tipo === 'VENCIDO').length;
    $('nStock').textContent = alertas.filter((a) => a.tipo === 'STOCK_BAJO').length;
    $('nPorVencer').textContent = alertas.filter((a) => a.tipo === 'POR_VENCER').length;
    $('tbody').innerHTML = lista.length ? lista.map((a) => {
      const t = TIPOS[a.tipo];
      let vence = Fmt.fecha(a.fechaVencimiento);
      const d = Fmt.diasHasta(a.fechaVencimiento);
      if (d !== null && a.tipo !== 'STOCK_BAJO') vence += d < 0 ? ' (hace ' + Math.abs(d) + ' d)' : ' (en ' + d + ' d)';
      return '<tr><td><span class="chip ' + t.clase + '">' + t.texto + '</span></td><td><strong>' + Fmt.esc(a.nombre) + '</strong><div class="small text-muted">' + Fmt.esc(a.codigo) + '</div></td>' +
        '<td class="num"><strong>' + a.stock + '</strong></td><td class="num">' + a.stockMinimo + '</td><td>' + vence + '</td><td>' + Fmt.esc(a.proveedor || '—') + '</td>' +
        '<td class="text-end"><button class="btn btn-sm btn-outline-primary" data-id="' + a.idProducto + '" data-tipo="' + a.tipo + '"><i class="bi bi-check2"></i> Atendida</button></td></tr>';
    }).join('') : '<tr><td colspan="7" class="vacio"><i class="bi bi-check2-circle"></i>No hay alertas pendientes en esta categoría.</td></tr>';
  }

  async function cargar() {
    try {
      const r = await Api.get('/api/alertas');
      alertas = r.alertas;
      $('infoDias').textContent = 'Se avisa con ' + r.diasVencimiento + ' días de anticipación.';
      pintar();
    } catch (e) { Notificar.error(e.mensaje); }
  }

  document.querySelectorAll('.filtro').forEach((b) => b.addEventListener('click', () => {
    document.querySelectorAll('.filtro').forEach((x) => x.classList.remove('active'));
    b.classList.add('active');
    filtro = b.dataset.tipo;
    pintar();
  }));

  $('tbody').addEventListener('click', async (ev) => {
    const btn = ev.target.closest('button[data-id]');
    if (!btn) return;
    const ok = await Modal.confirmar({ titulo: 'Marcar alerta como atendida', mensaje: 'La alerta dejará de mostrarse hasta que cambie el stock del producto. ¿Desea continuar?', textoConfirmar: 'Marcar atendida' });
    if (!ok) return;
    try {
      btn.disabled = true;
      await Api.post('/api/alertas/atender', { idProducto: Number(btn.dataset.id), tipo: btn.dataset.tipo });
      Notificar.exito('Alerta marcada como atendida.');
      cargar();
    } catch (e) { Notificar.error(e.mensaje); cargar(); }
  });

  cargar();
})();
