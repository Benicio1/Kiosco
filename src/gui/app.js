// app.js - Organizador Móvil de Faltantes del Kiosco (Universal HTTP + file://)
(function () {
  'use strict';

  const CLAVE_STORAGE = 'moldea_kiosco_faltantes_v1';
  const INICIALES = [
    { id: 'p1', nombre: 'Promo de panchos', cantidad: 1, unidad: 'promo', falta: true, categoria: 'Comidas', notas: 'Salchichas y panes' },
    { id: 'p2', nombre: 'Sanchuchitos de miga', cantidad: 1, unidad: 'docena', falta: true, categoria: 'Comidas', notas: 'Jamón y queso' },
    { id: 'p3', nombre: 'Alfajores Guaymallén', cantidad: 1, unidad: 'caja', falta: true, categoria: 'Alfajores', notas: 'Blanco / Negro' },
    { id: 'p4', nombre: 'Alfajores Fulbito', cantidad: 1, unidad: 'caja', falta: true, categoria: 'Alfajores', notas: 'De maní' },
    { id: 'p5', nombre: 'Juguitos Baggio multifruta', cantidad: 1, unidad: 'pack', falta: true, categoria: 'Bebidas', notas: '200ml' },
    { id: 'p6', nombre: 'Picodulces', cantidad: 1, unidad: 'bolsa', falta: true, categoria: 'Golosinas', notas: 'Chupetines rayas' },
    { id: 'p7', nombre: 'Chupetín con chicle', cantidad: 1, unidad: 'bolsa', falta: true, categoria: 'Golosinas', notas: 'Tipo Pop' },
    { id: 'p8', nombre: 'Flynn Paff', cantidad: 1, unidad: 'bolsa', falta: true, categoria: 'Golosinas', notas: 'Caramelos' },
    { id: 'p9', nombre: 'Palitos de la selva', cantidad: 1, unidad: 'bolsa', falta: true, categoria: 'Golosinas', notas: 'Caramelos' }
  ];

  let items = [];
  let filtroActual = 'faltan';
  let busquedaActual = '';
  let idRecien = null;

  function cargar() {
    try {
      const raw = localStorage.getItem(CLAVE_STORAGE);
      if (raw) {
        const arr = JSON.parse(raw);
        if (Array.isArray(arr) && arr.length > 0) { items = arr; return; }
      }
    } catch (e) {}
    items = JSON.parse(JSON.stringify(INICIALES));
  }

  function guardar() {
    try { localStorage.setItem(CLAVE_STORAGE, JSON.stringify(items)); } catch (e) {}
    actualizarMetricas();
  }

  function mostrarAviso(msg, tipo = 'normal') {
    const t = document.getElementById('toast-notice');
    const tm = document.getElementById('toast-message');
    if (!t || !tm) return;
    tm.textContent = msg;
    t.className = 'toast-notice ' + tipo + ' show';
    try { if (navigator.vibrate) navigator.vibrate([25, 30, 25]); } catch (e) {}
    clearTimeout(t._timer);
    t._timer = setTimeout(() => t.classList.remove('show'), 2600);
  }

  function actualizarMetricas() {
    const total = items.length;
    const faltan = items.filter(i => i.falta).length;
    const listos = total - faltan;
    const setT = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
    setT('metric-faltan', faltan);
    setT('metric-listos', listos);
    setT('metric-total', total);

    const setB = (id, act, cls) => {
      const b = document.getElementById(id);
      if (b) b.className = 'metric-btn' + (act ? ' ' + cls : '');
    };
    setB('btn-filtro-faltan', filtroActual === 'faltan', 'active-danger');
    setB('btn-filtro-listos', filtroActual === 'listos', 'active-success');
    setB('btn-filtro-todos', filtroActual === 'todos', 'active-total');
  }

  function norm(s) {
    return String(s || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
  }

  function esc(s) {
    return String(s || '').replace(/[&<>'"]/g, t => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[t] || t));
  }

  function renderizar() {
    const c = document.getElementById('items-list');
    if (!c) return;
    const q = norm(busquedaActual);

    const filtrados = items.filter(i => {
      if (filtroActual === 'faltan' && !i.falta) return false;
      if (filtroActual === 'listos' && i.falta) return false;
      if (q) {
        const t = norm(i.nombre) + ' ' + norm(i.categoria) + ' ' + norm(i.notas);
        if (!t.includes(q)) return false;
      }
      return true;
    });

    actualizarMetricas();

    if (filtrados.length === 0) {
      if (q) {
        c.innerHTML = `
          <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h3>No se encontró "${esc(busquedaActual)}"</h3>
            <p>¿Querés añadirlo ahora?</p>
            <button type="button" class="btn-add-search-prompt" onclick="window.agregarDirecto('${esc(busquedaActual)}')">
              ➕ Añadir "${esc(busquedaActual)}" a la lista
            </button>
          </div>`;
        return;
      }
      const esFalta = filtroActual === 'faltan';
      c.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">${esFalta ? '🎉' : '📦'}</div>
          <h3>${esFalta ? '¡Excelente! No falta nada por reponer' : 'Todavía no compraste nada'}</h3>
          <p>${esFalta ? 'Todo está en stock. Si se acaba algo, anotalo arriba.' : 'Tocá "FALTA" en cualquier producto para marcarlo comprado.'}</p>
        </div>`;
      return;
    }

    c.innerHTML = filtrados.map(i => `
      <article class="item-card ${i.falta ? 'falta' : 'tachado'} ${i.id === idRecien ? 'just-added' : ''}" data-id="${i.id}">
        <button type="button" class="btn-toggle-tachar" onclick="window.toggleTachar('${i.id}')" title="${i.falta ? 'Marcar comprado' : 'Marcar que falta'}">
          ${i.falta ? '<span class="status-btn-text red">❌ FALTA</span>' : '<span class="status-btn-text green">✔️ LISTO</span>'}
        </button>
        <div class="item-details">
          <div class="item-title-row"><span class="item-title">${esc(i.nombre)}</span></div>
          <div class="item-meta">
            <span class="badge-status ${i.falta ? 'falta' : 'stock'}">${i.falta ? 'Falta' : 'En stock'}</span>
            <span class="badge-tag">${esc(i.categoria || 'Kiosco')}</span>
            ${i.notas ? `<span class="item-note">(${esc(i.notas)})</span>` : ''}
          </div>
        </div>
        <div class="quantity-control">
          <button type="button" class="qty-btn" onclick="window.cambiarCantidad('${i.id}', -1)" aria-label="Menos">−</button>
          <span class="qty-display">${i.cantidad} <small>${esc(i.unidad || 'unid')}</small></span>
          <button type="button" class="qty-btn" onclick="window.cambiarCantidad('${i.id}', 1)" aria-label="Más">+</button>
        </div>
        <div class="item-actions">
          <button type="button" class="item-action-btn" onclick="window.abrirModalEdicion('${i.id}')" title="Editar">✏️</button>
          <button type="button" class="item-action-btn delete" onclick="window.eliminarItem('${i.id}')" title="Eliminar">🗑️</button>
        </div>
      </article>
    `).join('');

    if (idRecien) setTimeout(() => { idRecien = null; }, 2800);
  }

  window.agregarProducto = function () {
    const inNom = document.getElementById('quick-input-nombre');
    const inCant = document.getElementById('quick-input-cant');
    const inUni = document.getElementById('quick-input-unidad');
    if (!inNom) return;
    const nombre = inNom.value.trim();
    if (!nombre) {
      mostrarAviso('⚠️ Escribí el nombre del producto que falta.', 'danger');
      inNom.focus();
      return;
    }
    const cantidad = Math.max(1, parseInt(inCant?.value, 10) || 1);
    const nuevo = {
      id: 'i-' + Date.now() + '-' + Math.random().toString(36).substring(2, 5),
      nombre, cantidad, unidad: inUni?.value || 'unid', falta: true, categoria: 'Kiosco', notas: ''
    };
    items.unshift(nuevo);
    idRecien = nuevo.id;
    guardar();
    if (filtroActual === 'listos') filtroActual = 'faltan';
    busquedaActual = '';
    const sinp = document.getElementById('search-input');
    if (sinp) sinp.value = '';
    renderizar();
    mostrarAviso(`✅ ¡Añadido: "${nuevo.nombre}" (${nuevo.cantidad} ${nuevo.unidad})!`, 'success');
    inNom.value = '';
    if (inCant) inCant.value = '1';
    inNom.focus();
    setTimeout(() => {
      document.querySelector(`[data-id="${nuevo.id}"]`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 60);
  };

  window.agregarDirecto = function (n) {
    const inNom = document.getElementById('quick-input-nombre');
    if (inNom) inNom.value = n;
    window.agregarProducto();
  };

  window.toggleTachar = function (id) {
    const item = items.find(i => i.id === id);
    if (!item) return;
    item.falta = !item.falta;
    guardar();
    renderizar();
    mostrarAviso(item.falta ? `🔴 "${item.nombre}" FALTANTE` : `✔️ "${item.nombre}" COMPRADO`, item.falta ? 'danger' : 'success');
  };

  window.cambiarCantidad = function (id, d) {
    const item = items.find(i => i.id === id);
    if (!item) return;
    item.cantidad = Math.max(1, (item.cantidad || 1) + d);
    guardar();
    renderizar();
  };

  window.eliminarItem = function (id) {
    const item = items.find(i => i.id === id);
    if (!item) return;
    if (confirm(`¿Eliminar "${item.nombre}" de la lista?`)) {
      items = items.filter(i => i.id !== id);
      guardar();
      renderizar();
      mostrarAviso(`🗑️ Eliminado: "${item.nombre}"`, 'danger');
    }
  };

  window.setFiltro = function (f) { filtroActual = f; renderizar(); };

  window.abrirModalEdicion = function (id) {
    const item = items.find(i => i.id === id);
    if (!item) return;
    document.getElementById('item-id').value = item.id;
    document.getElementById('item-nombre').value = item.nombre;
    document.getElementById('item-cantidad').value = item.cantidad;
    document.getElementById('item-unidad').value = item.unidad || 'unid';
    document.getElementById('item-falta').checked = item.falta;
    document.getElementById('item-notas').value = item.notas || '';
    document.getElementById('modal-item')?.classList.add('active');
  };

  window.cerrarModalEdicion = function () {
    document.getElementById('modal-item')?.classList.remove('active');
  };

  window.guardarEdicion = function () {
    const id = document.getElementById('item-id').value;
    const item = items.find(i => i.id === id);
    if (!item) return;
    const n = document.getElementById('item-nombre').value.trim();
    if (!n) return;
    item.nombre = n;
    item.cantidad = Math.max(1, parseInt(document.getElementById('item-cantidad').value, 10) || 1);
    item.unidad = document.getElementById('item-unidad').value;
    item.falta = document.getElementById('item-falta').checked;
    item.notas = document.getElementById('item-notas').value.trim();
    guardar();
    window.cerrarModalEdicion();
    renderizar();
    mostrarAviso(`Guardado: "${item.nombre}"`, 'success');
  };

  window.compartirWhatsApp = function () {
    const f = items.filter(i => i.falta);
    if (f.length === 0) { alert('¡No hay faltantes por ahora en el kiosco!'); return; }
    let txt = `🛒 *FALTANTES DEL KIOSCO*\n_Cosas a reponer (${f.length}):_\n\n`;
    f.forEach(i => {
      txt += `• [ ] ${i.nombre} — Cant: *${i.cantidad}* ${i.unidad || ''}${i.notas ? ' (' + i.notas + ')' : ''}\n`;
    });
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(txt).then(() => mostrarAviso('📋 ¡Lista copiada para WhatsApp!', 'success')).catch(() => {});
    }
    window.open('https://wa.me/?text=' + encodeURIComponent(txt), '_blank');
  };

  window.abrirModalQr = function () {
    const h = window.location.hostname;
    const p = window.location.port ? ':' + window.location.port : '';
    const url = (h === 'localhost' || h === '127.0.0.1' || !h) ? 'http://192.168.1.55' + (p || ':8080') : window.location.href;
    const qc = document.getElementById('qr-container');
    if (qc) {
      qc.innerHTML = `<div style="background:#fff; padding:12px; border-radius:10px; display:inline-block;"><img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(url)}" alt="QR" width="180" height="180" style="display:block;border-radius:4px;"></div><p style="margin-top:8px;"><a href="${url}" target="_blank" style="color:#38bdf8;font-weight:bold;font-size:14px;text-decoration:none;">🔗 ${url}</a></p>`;
    }
    const tu = document.getElementById('qr-url-text');
    if (tu) tu.textContent = url;
    document.getElementById('modal-qr')?.classList.add('active');
  };

  window.cerrarModalQr = function () {
    document.getElementById('modal-qr')?.classList.remove('active');
  };

  document.addEventListener('DOMContentLoaded', () => {
    cargar();
    const s = document.getElementById('search-input');
    s?.addEventListener('input', (e) => { busquedaActual = e.target.value; renderizar(); });
    ['modal-item', 'modal-qr'].forEach(id => {
      const m = document.getElementById(id);
      m?.addEventListener('click', (e) => { if (e.target === m) m.classList.remove('active'); });
    });
    renderizar();
  });

  if (document.readyState === 'interactive' || document.readyState === 'complete') {
    cargar();
    renderizar();
  }
})();
