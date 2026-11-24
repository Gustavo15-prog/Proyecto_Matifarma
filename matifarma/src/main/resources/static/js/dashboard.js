(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const TIPOS = {
    STOCK_BAJO: { texto: 'Stock bajo', clase: 'chip-warn' },
    POR_VENCER: { texto: 'Por vencer', clase: 'chip-warn' },
    VENCIDO: { texto: 'Vencido', clase: 'chip-danger' }
  };

  function detalle(a) {
    if (a.tipo === 'STOCK_BAJO') return 'Stock ' + a.stock + ' (mín. ' + a.stockMinimo + ')';
    return 'Vence ' + Fmt.fecha(a.fechaVencimiento) + ' · stock ' + a.stock;
  }

  function dibujarVentas(v) {
    $('vtCantidad').textContent = v.cantidadHoy;
    $('vtHoy').textContent = Fmt.moneda(v.totalHoy);
    $('vtMes').textContent = Fmt.moneda(v.totalMes);
    const max = Math.max.apply(null, v.ultimosDias.map((d) => Number(d.total)).concat([1]));
    const hoy = Fmt.hoyISO();
    const dias = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'];
    $('barras').innerHTML = v.ultimosDias.map((d) => {
      const alto = Math.max(2, Math.round((Number(d.total) / max) * 100));
      return '<div class="barra' + (d.fecha === hoy ? ' hoy' : '') + '"><span class="barra-val">' +
        (Number(d.total) > 0 ? Number(d.total).toFixed(0) : '') + '</span><div class="barra-col" style="height:' + alto + '%"></div></div>';
    }).join('');
    const et = document.createElement('div');
    et.className = 'etiquetas-dias';
    et.innerHTML = v.ultimosDias.map((d) => '<span>' + dias[new Date(d.fecha + 'T00:00:00').getDay()] + ' ' + d.fecha.substring(8) + '</span>').join('');
    $('barras').after(et);
  }

  async function cargar() {
    try {
      const r = await Api.get('/api/dashboard');
      $('stTotal').textContent = r.totalProductos;
      $('stBajo').textContent = r.stockBajo;
      $('stPorVencer').textContent = r.porVencer;
      $('stVencidos').textContent = r.vencidos;
      if (r.ventas) dibujarVentas(r.ventas);
      else { $('colVentas').remove(); $('colAlertas').className = 'col-12'; }
      $('listaAlertas').innerHTML = r.alertas.length
        ? r.alertas.map((a) => {
            const t = TIPOS[a.tipo];
            return '<li><span class="chip ' + t.clase + '">' + t.texto + '</span><div><div class="nombre">' + Fmt.esc(a.nombre) +
              '</div><small>' + Fmt.esc(detalle(a)) + '</small></div></li>';
          }).join('')
        : '<li class="vacio w-100 d-block"><i class="bi bi-check2-circle"></i>No hay alertas pendientes.</li>';
    } catch (e) { Notificar.error(e.mensaje); }
  }

  if (new URLSearchParams(location.search).has('denegado')) Notificar.aviso('No tiene permisos para acceder a esa sección.');
  cargar();
})();
