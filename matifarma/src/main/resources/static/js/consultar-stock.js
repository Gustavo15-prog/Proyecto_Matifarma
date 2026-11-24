(function () {
  'use strict';
  const $ = (id) => document.getElementById(id);
  let datos = [];

  function pintar() {
    const estado = $('selEstado').value;
    const lista = datos.filter((p) => !estado || Fmt.estadoProducto(p).texto === estado);
    $('contador').textContent = lista.length + ' producto(s)';
    $('tbody').innerHTML = lista.length ? lista.map((p) => {
      const clase = p.stock === 0 ? 'cero' : (p.stock <= p.stockMinimo ? 'bajo' : '');
      return '<tr><td><code>' + Fmt.esc(p.codigo) + '</code></td><td><strong>' + Fmt.esc(p.nombre) + '</strong><div class="small text-muted">' + Fmt.esc(p.descripcion || '') + '</div></td>' +
        '<td>' + Fmt.esc(p.categoria) + '</td><td class="num"><span class="stock-num ' + clase + '">' + p.stock + '</span></td>' +
        '<td class="num precio-venta">' + Fmt.moneda(p.precioVenta) + '</td><td>' + Fmt.fecha(p.fechaVencimiento) + '</td>' +
        '<td>' + Fmt.esc(p.ubicacion || '—') + '</td><td>' + Fmt.chipEstado(p) + '</td></tr>';
    }).join('') : '<tr><td colspan="8" class="vacio"><i class="bi bi-search"></i>No se encontraron productos con esos criterios.</td></tr>';
  }

  async function buscar() {
    const params = new URLSearchParams();
    if ($('txtBuscar').value.trim()) params.set('q', $('txtBuscar').value.trim());
    if ($('selCategoria').value) params.set('idCategoria', $('selCategoria').value);
    try { datos = await Api.get('/api/stock?' + params); pintar(); }
    catch (e) { Notificar.error(e.mensaje); }
  }

  $('txtBuscar').addEventListener('input', Fmt.debounce(buscar, 300));
  $('selCategoria').addEventListener('change', buscar);
  $('selEstado').addEventListener('change', pintar);

  Api.get('/api/categorias').then((cats) => {
    $('selCategoria').insertAdjacentHTML('beforeend', cats.map((c) => '<option value="' + c.id + '">' + Fmt.esc(c.nombre) + '</option>').join(''));
  }).catch((e) => Notificar.error(e.mensaje));
  buscar();
})();
