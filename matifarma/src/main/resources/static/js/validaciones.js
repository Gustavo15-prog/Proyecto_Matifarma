/* Validaciones de formularios en el navegador. El servidor vuelve a validar todo. */
(function (w) {
  'use strict';
  const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

  function iso(d) { return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }

  function fechaValida(v) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(v)) return false;
    const d = new Date(v + 'T00:00:00');
    return !isNaN(d) && iso(d) === v;
  }

  function validarValor(v, r, datos) {
    const et = r.etiqueta || 'Este campo';
    if (v === '' || v == null || v === false) {
      if (r.requerido) return r.msgRequerido || et + ' es obligatorio';
      return r.evaluarVacio && r.fn ? r.fn(v, datos) || null : null;
    }
    if (typeof v === 'string') {
      if (r.min != null && v.length < r.min) return et + ' debe tener al menos ' + r.min + ' caracteres';
      if (r.max != null && v.length > r.max) return et + ' admite máximo ' + r.max + ' caracteres';
      if (r.patron && !r.patron.test(v)) return r.msgPatron || et + ' tiene un formato inválido';
      if (r.email && !EMAIL.test(v)) return 'Ingrese un correo electrónico válido';
      if (r.numero || r.entero || r.minimo != null || r.mayorQue != null || r.maximo != null || r.decimales != null) {
        const n = Number(v);
        if (!Number.isFinite(n)) return et + ' debe ser un número válido';
        if (r.entero && !Number.isInteger(n)) return et + ' debe ser un número entero';
        if (r.mayorQue != null && !(n > r.mayorQue)) return et + ' debe ser mayor que ' + r.mayorQue;
        if (r.minimo != null && n < r.minimo) return et + ' no puede ser menor que ' + r.minimo;
        if (r.maximo != null && n > r.maximo) return et + ' no puede ser mayor que ' + r.maximo;
        if (r.decimales != null && (v.split('.')[1] || '').length > r.decimales) return et + ' admite máximo ' + r.decimales + ' decimales';
      }
      if (r.fecha) {
        if (!fechaValida(v)) return 'Ingrese una fecha válida';
        const hoy = iso(new Date());
        if (r.noPasado && v < hoy) return et + ' no puede ser anterior a hoy';
        if (r.noFuturo && v > hoy) return et + ' no puede ser futura';
      }
    }
    return r.fn ? r.fn(v, datos) || null : null;
  }

  function feedback(campo) {
    const padre = campo.closest('.input-group') ? campo.closest('.input-group').parentElement : campo.parentElement;
    let fb = padre.querySelector(':scope > .invalid-feedback[data-gen]');
    if (!fb) {
      fb = document.createElement('div');
      fb.className = 'invalid-feedback';
      fb.setAttribute('data-gen', '1');
      padre.appendChild(fb);
    }
    return fb;
  }

  function marcar(campo, mensaje) {
    campo.classList.add('is-invalid');
    campo.setAttribute('aria-invalid', 'true');
    const fb = feedback(campo);
    fb.textContent = mensaje;
    fb.style.display = 'block';
  }

  function limpiar(form) {
    form.querySelectorAll('.is-invalid').forEach((c) => { c.classList.remove('is-invalid'); c.removeAttribute('aria-invalid'); });
    form.querySelectorAll('.invalid-feedback[data-gen]').forEach((f) => { f.textContent = ''; f.style.display = 'none'; });
  }

  /** Valida el formulario según el esquema y devuelve {valido, datos}. */
  function formulario(form, esquema) {
    limpiar(form);
    const datos = {};
    for (const nombre of Object.keys(esquema)) {
      const campo = form.elements[nombre];
      if (!campo) continue;
      datos[nombre] = campo.type === 'checkbox' ? campo.checked
        : esquema[nombre].sinRecorte ? String(campo.value) : String(campo.value).trim().replace(/\s+/g, ' ');
    }
    let valido = true, primero = null;
    for (const nombre of Object.keys(esquema)) {
      const campo = form.elements[nombre];
      if (!campo) continue;
      const msg = validarValor(datos[nombre], esquema[nombre], datos);
      if (msg) { marcar(campo, msg); valido = false; primero = primero || campo; }
    }
    if (primero) primero.focus();
    return { valido, datos };
  }

  /** Muestra en el formulario los errores por campo devueltos por el servidor. */
  function errorServidor(form, errores) {
    let n = 0, primero = null;
    Object.keys(errores || {}).forEach((nombre) => {
      const campo = form.elements[nombre];
      if (campo) { marcar(campo, errores[nombre]); n++; primero = primero || campo; }
    });
    if (primero) primero.focus();
    return n;
  }

  /** Evita envíos dobles: deshabilita el botón mientras se ejecuta la operación. */
  async function enviarUnaVez(boton, fn) {
    if (boton.dataset.enviando === '1') return undefined;
    boton.dataset.enviando = '1';
    boton.disabled = true;
    const html = boton.innerHTML;
    boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Procesando…';
    try { return await fn(); }
    finally { boton.dataset.enviando = '0'; boton.disabled = false; boton.innerHTML = html; }
  }

  /** Solo permite dígitos (RUC, DNI, teléfono...). */
  function soloDigitos(input) {
    input.addEventListener('input', () => { input.value = input.value.replace(/\D+/g, ''); });
  }

  w.Validar = { formulario, errorServidor, limpiar, marcar, enviarUnaVez, soloDigitos, fechaValida, hoy: () => iso(new Date()) };
})(window);
