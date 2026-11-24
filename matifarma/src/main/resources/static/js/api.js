/* Utilidades compartidas: cliente HTTP con CSRF (Api) y formato de datos (Fmt) */
(function (w) {
  'use strict';

  const meta = (n) => document.querySelector('meta[name="' + n + '"]')?.content;

  async function pedir(metodo, url, cuerpo) {
    const opciones = { method: metodo, headers: { Accept: 'application/json' }, credentials: 'same-origin' };
    if (cuerpo !== undefined) {
      opciones.headers['Content-Type'] = 'application/json';
      opciones.body = JSON.stringify(cuerpo);
    }
    if (metodo !== 'GET') opciones.headers[meta('_csrf_header') || 'X-CSRF-TOKEN'] = meta('_csrf');

    let res;
    try {
      res = await fetch(url, opciones);
    } catch (e) {
      throw { status: 0, mensaje: 'No se pudo conectar con el servidor. Verifique su conexión.', errores: {} };
    }
    if (res.status === 401 || (res.redirected && res.url.includes('/login'))) {
      window.location.href = '/login';
      throw { status: 401, mensaje: 'Su sesión expiró. Ingrese nuevamente.', errores: {} };
    }
    let datos = null;
    const texto = await res.text();
    if (texto) { try { datos = JSON.parse(texto); } catch (e) { datos = null; } }
    if (!res.ok) {
      throw {
        status: res.status,
        mensaje: (datos && datos.mensaje) || (res.status === 403 ? 'No tiene permisos para esta operación' : 'Ocurrió un error inesperado'),
        errores: (datos && datos.errores) || {}
      };
    }
    return datos;
  }

  w.Api = {
    get: (u) => pedir('GET', u),
    post: (u, c) => pedir('POST', u, c === undefined ? {} : c),
    put: (u, c) => pedir('PUT', u, c),
    patch: (u, c) => pedir('PATCH', u, c)
  };

  const p2 = (n) => String(n).padStart(2, '0');

  w.Fmt = {
    esc(s) {
      return String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    },
    moneda(n) {
      return 'S/ ' + Number(n == null ? 0 : n).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
    fecha(iso) {
      if (!iso) return '—';
      const [y, m, d] = String(iso).substring(0, 10).split('-');
      return d + '/' + m + '/' + y;
    },
    fechaHora(iso) { return iso ? Fmt.fecha(iso) + ' ' + String(iso).substring(11, 16) : '—'; },
    hoyISO() { const d = new Date(); return d.getFullYear() + '-' + p2(d.getMonth() + 1) + '-' + p2(d.getDate()); },
    diasHasta(iso) {
      if (!iso) return null;
      const a = new Date(Fmt.hoyISO() + 'T00:00:00'), b = new Date(String(iso).substring(0, 10) + 'T00:00:00');
      return Math.round((b - a) / 86400000);
    },
    uuid() {
      if (w.crypto && typeof w.crypto.randomUUID === 'function') return w.crypto.randomUUID();
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0;
        return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
      });
    },
    debounce(fn, ms) {
      let t;
      return function () { const a = arguments; clearTimeout(t); t = setTimeout(() => fn.apply(null, a), ms || 300); };
    },
    /** Estado de un producto para mostrar como etiqueta. */
    estadoProducto(p) {
      if (!p.activo) return { texto: 'Inactivo', clase: 'chip-neutral' };
      const dias = Fmt.diasHasta(p.fechaVencimiento);
      if (dias !== null && dias < 0 && p.stock > 0) return { texto: 'Vencido', clase: 'chip-danger' };
      if (p.stock === 0) return { texto: 'Agotado', clase: 'chip-danger' };
      if (p.stock <= p.stockMinimo) return { texto: 'Stock bajo', clase: 'chip-warn' };
      if (dias !== null && dias <= 30) return { texto: 'Por vencer', clase: 'chip-warn' };
      return { texto: 'Normal', clase: 'chip-ok' };
    },
    chipEstado(p) { const e = Fmt.estadoProducto(p); return '<span class="chip ' + e.clase + '">' + e.texto + '</span>'; }
  };
})(window);
