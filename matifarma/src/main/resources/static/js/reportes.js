(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  const form = $('formReporte');
  let actual = null;

  const p2 = (n) => String(n).padStart(2, '0');
  const hoy = new Date();
  $('hasta').value = Fmt.hoyISO();
  $('desde').value = hoy.getFullYear() + '-' + p2(hoy.getMonth() + 1) + '-01';

  function alternarFechas() {
    const necesita = $('tipo').value !== 'stock';
    document.querySelectorAll('.campo-fecha').forEach((c) => { c.style.display = necesita ? '' : 'none'; });
  }
  $('tipo').addEventListener('change', alternarFechas);
  alternarFechas();

  function esquema() {
    const conFechas = $('tipo').value !== 'stock';
    return {
      tipo: { requerido: true, msgRequerido: 'Seleccione el tipo de reporte' },
      desde: { requerido: conFechas, fecha: true, etiqueta: 'La fecha inicial', msgRequerido: 'Seleccione la fecha inicial' },
      hasta: { requerido: conFechas, fecha: true, noFuturo: true, etiqueta: 'La fecha final', msgRequerido: 'Seleccione la fecha final',
        fn: (v, d) => (conFechas && d.desde && v && d.desde > v) ? 'La fecha final debe ser igual o posterior a la inicial' : null }
    };
  }

  function pintar(r, tipo) {
    actual = r;
    $('inicial').hidden = true;
    $('resultado').hidden = false;
    $('tituloReporte').textContent = r.titulo;
    $('subtituloReporte').textContent = tipo === 'stock' ? 'Al ' + Fmt.fecha(Fmt.hoyISO()) : 'Del ' + Fmt.fecha($('desde').value) + ' al ' + Fmt.fecha($('hasta').value);
    $('resumen').innerHTML = Object.keys(r.resumen).map((k) =>
      '<div class="col-6 col-lg-3"><div class="kpi"><small>' + Fmt.esc(k) + '</small><strong>' + Fmt.esc(r.resumen[k]) + '</strong></div></div>').join('');
    $('thead').innerHTML = '<tr>' + r.columnas.map((c) => '<th>' + Fmt.esc(c) + '</th>').join('') + '</tr>';
    $('tbody').innerHTML = r.filas.length
      ? r.filas.map((f) => '<tr>' + f.map((v) => '<td' + (/^-?\d+(\.\d+)?$/.test(String(v)) ? ' class="num"' : '') + '>' + Fmt.esc(v) + '</td>').join('') + '</tr>').join('')
      : '<tr><td colspan="' + r.columnas.length + '" class="vacio"><i class="bi bi-inbox"></i>No se encontraron resultados para los filtros seleccionados.</td></tr>';
    $('btnCsv').disabled = r.filas.length === 0;
    $('btnImprimir').disabled = false;
  }

  $('btnGenerar').addEventListener('click', function () {
    const v = Validar.formulario(form, esquema());
    if (!v.valido) return;
    const d = v.datos;
    const url = '/api/reportes/' + d.tipo + (d.tipo === 'stock' ? '' : '?desde=' + d.desde + '&hasta=' + d.hasta);
    Validar.enviarUnaVez(this, async () => {
      try {
        const r = await Api.get(url);
        pintar(r, d.tipo);
        if (!r.filas.length) Notificar.info('No hay resultados para los filtros seleccionados.');
      } catch (e) { if (!Validar.errorServidor(form, e.errores)) Notificar.error(e.mensaje); else Notificar.aviso(e.mensaje); }
    });
  });

  function celdaCsv(v) { return '"' + String(v == null ? '' : v).replace(/"/g, '""') + '"'; }

  $('btnCsv').addEventListener('click', () => {
    if (!actual || !actual.filas.length) return;
    const lineas = [actual.columnas.map(celdaCsv).join(',')].concat(actual.filas.map((f) => f.map(celdaCsv).join(',')));
    const blob = new Blob(['\uFEFF' + lineas.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'reporte-' + $('tipo').value + '-' + Fmt.hoyISO() + '.csv';
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(a.href), 1000);
  });
  $('btnImprimir').addEventListener('click', () => window.print());
})();
