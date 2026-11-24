(function () {
  'use strict';
  const form = document.getElementById('formLogin');
  const btn = document.getElementById('btnIngresar');
  const pass = document.getElementById('password');
  let enviado = false;

  document.getElementById('btnVerPassword').addEventListener('click', function () {
    const visible = pass.type === 'text';
    pass.type = visible ? 'password' : 'text';
    this.innerHTML = '<i class="bi ' + (visible ? 'bi-eye' : 'bi-eye-slash') + '"></i>';
    this.setAttribute('aria-label', visible ? 'Mostrar contraseña' : 'Ocultar contraseña');
  });

  form.addEventListener('submit', function (e) {
    if (enviado) { e.preventDefault(); return; }
    const r = Validar.formulario(form, {
      username: { requerido: true, etiqueta: 'El usuario', min: 4, max: 30, patron: /^[A-Za-z0-9._]+$/, msgPatron: 'El usuario solo admite letras, números, punto y guion bajo' },
      password: { requerido: true, etiqueta: 'La contraseña', sinRecorte: true, min: 1, max: 64 }
    });
    if (!r.valido) { e.preventDefault(); return; }
    enviado = true;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Ingresando…';
  });
})();
