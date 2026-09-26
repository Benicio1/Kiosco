// uiRenderer.js - Funciones puras de renderizado visual y marcado HTML
export function escapeHTML(str) {
  return String(str || '').replace(/[&<>'"]/g, tag => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
  }[tag] || tag));
}

/**
 * Renderiza el HTML de una tarjeta de producto
 * @param {Object} item
 * @param {boolean} esRecienAgregado
 * @returns {string}
 */
export function renderizarTarjetaItem(item, esRecienAgregado = false) {
  return `
    <article class="item-card ${item.falta ? 'falta' : 'tachado'} ${esRecienAgregado ? 'just-added' : ''}" data-id="${item.id}">
      <button class="btn-toggle-tachar" aria-label="${item.falta ? 'Tachar comprado' : 'Marcar que falta'}">
        ${item.falta ? '⭕' : '✔️'}
      </button>

      <div class="item-details">
        <div class="item-title-row">
          <span class="item-title">${escapeHTML(item.nombre)}</span>
        </div>
        <div class="item-meta">
          <span class="badge-status ${item.falta ? 'falta' : 'stock'}">
            ${item.falta ? 'Falta Reponer' : 'En Stock'}
          </span>
          <span class="badge-tag">${escapeHTML(item.categoria || 'General')}</span>
          ${item.notas ? `<span style="font-style:italic; font-size:11px;">(${escapeHTML(item.notas)})</span>` : ''}
        </div>
      </div>

      <div class="quantity-control">
        <button class="qty-btn btn-qty-minus" aria-label="Disminuir">−</button>
        <span class="qty-display">${item.cantidad} <small style="font-size:10px; font-weight:normal; color:#94a3b8;">${escapeHTML(item.unidad)}</small></span>
        <button class="qty-btn btn-qty-plus" aria-label="Aumentar">+</button>
      </div>

      <button class="item-actions-btn btn-edit" title="Editar" aria-label="Editar">✏️</button>
    </article>
  `;
}

/**
 * Renderiza el estado vacío según el filtro actual y la búsqueda
 * @param {Object} params
 * @returns {string}
 */
export function renderizarEstadoVacio({ filtroActual, busquedaActual }) {
  if (busquedaActual && busquedaActual.trim()) {
    const termino = escapeHTML(busquedaActual.trim());
    return `
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3>No se encontró "${termino}"</h3>
        <p style="margin-bottom:12px;">¿Querés añadirlo ahora mismo a los faltantes?</p>
        <button id="btn-add-search-prompt" class="btn-add-search-prompt">
          ➕ Añadir "${termino}" a la lista
        </button>
      </div>
    `;
  }

  let tituloVacio = 'No hay productos en esta sección';
  let subtituloVacio = 'Escribí lo que falta en la caja de arriba para añadirlo.';

  if (filtroActual === 'faltan') {
    tituloVacio = '🎉 ¡Excelente! No hay nada marcado como faltante';
    subtituloVacio = 'Si se acabó algo en el kiosco, escribilo arriba para agregarlo.';
  } else if (filtroActual === 'listos') {
    tituloVacio = '📦 Todavía no compraste nada';
    subtituloVacio = 'Tocá el círculo de cualquier faltante para tacharlo cuando lo compres.';
  }

  return `
    <div class="empty-state">
      <div class="empty-icon">${filtroActual === 'faltan' ? '🎉' : '📋'}</div>
      <h3>${tituloVacio}</h3>
      <p>${subtituloVacio}</p>
    </div>
  `;
}
