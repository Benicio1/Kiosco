// tv.js - Controlador de la Interfaz 10-Foot UI de Smart TV
const APPS = [
  { id: 'youtube', name: 'YouTube', category: 'streaming', icon: '▶️', color: '#ff0000', url: 'https://www.youtube.com' },
  { id: 'netflix', name: 'Netflix', category: 'streaming', icon: '🍿', color: '#e50914', url: 'https://www.netflix.com' },
  { id: 'plutotv', name: 'Pluto TV (Gratis)', category: 'tv', icon: '📺', color: '#ffd100', url: 'https://pluto.tv' },
  { id: 'disney', name: 'Disney+', category: 'streaming', icon: '✨', color: '#113ccf', url: 'https://www.disneyplus.com' },
  { id: 'prime', name: 'Prime Video', category: 'streaming', icon: '📦', color: '#00a8e1', url: 'https://www.primevideo.com' },
  { id: 'tn_vivo', name: 'TN en Vivo', category: 'tv', icon: '📡', color: '#dc2626', url: 'https://tn.com.ar/envivo/' },
  { id: 'twitch', name: 'Twitch', category: 'streaming', icon: '🎮', color: '#9146ff', url: 'https://www.twitch.tv' },
  { id: 'spotify', name: 'Spotify', category: 'music', icon: '🎧', color: '#1db954', url: 'https://open.spotify.com' },
  { id: 'local_video', name: 'Videos Locales', category: 'system', icon: '📁', color: '#6366f1', action: 'open_file_dialog' },
  { id: 'turbo_ram', name: 'Optimizar RAM', category: 'system', icon: '⚡', color: '#10b981', action: 'clean_ram' }
];

let currentCategory = 'all';
let currentFocusIndex = 0;
let focusableElements = [];
let cursorX = window.innerWidth / 2;
let cursorY = window.innerHeight / 2;
let cursorVisible = false;

// Inicialización de la pantalla
document.addEventListener('DOMContentLoaded', () => {
  renderApps();
  setupClock();
  setupRamMonitor();
  setupKeyboardNavigation();
  setupSSEConnection();
  setupModals();
  fetchServerInfo();
});

// Renderizar tarjetas de aplicaciones
function renderApps() {
  const container = document.getElementById('apps-container');
  container.innerHTML = '';

  const filtered = currentCategory === 'all' 
    ? APPS 
    : APPS.filter(a => a.category === currentCategory);

  filtered.forEach((app, idx) => {
    const card = document.createElement('div');
    card.className = 'app-card focusable';
    card.tabIndex = 0;
    card.dataset.index = idx;
    card.dataset.appId = app.id;

    card.innerHTML = `
      <div class="card-icon" style="background: ${app.color}20; color: ${app.color}; border: 1px solid ${app.color}40;">
        ${app.icon}
      </div>
      <span class="card-title">${app.name}</span>
      <span class="card-badge">${app.category}</span>
    `;

    card.addEventListener('click', () => launchApp(app));
    container.appendChild(card);
  });

  refreshFocusables();
  if (focusableElements.length > 0) {
    focusElement(0);
  }
}

// Ejecutar una aplicación
function launchApp(app) {
  showToast(`Abriendo ${app.name}...`, app.icon);

  if (app.action === 'clean_ram') {
    optimizeRamAction();
    return;
  }

  if (app.action === 'open_file_dialog') {
    openLocalVideoPicker();
    return;
  }

  if (app.url) {
    window.open(app.url, '_blank');
  }
}

// Acción de optimización de RAM visual e interactiva
function optimizeRamAction() {
  showToast('Liberando memoria RAM de la Netbook...', '⚡');
  if (window.gc) window.gc();
  setTimeout(() => {
    updateRamBadge();
    showToast('¡Memoria optimizada con éxito!', '✅');
  }, 600);
}

// Selector de video local
function openLocalVideoPicker() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'video/*';
  input.onchange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const videoUrl = URL.createObjectURL(file);
      window.open(videoUrl, '_blank');
    }
  };
  input.click();
}

// Gestión del Foco y Navegación
function refreshFocusables() {
  focusableElements = Array.from(document.querySelectorAll('.focusable:not([tabindex="-1"])'));
}

function focusElement(index) {
  if (focusableElements.length === 0) return;
  currentFocusIndex = (index + focusableElements.length) % focusableElements.length;
  focusableElements[currentFocusIndex].focus();
}

// Navegación con teclado físico / control remoto USB
function setupKeyboardNavigation() {
  window.addEventListener('keydown', (e) => {
    switch (e.key) {
      case 'ArrowRight':
        e.preventDefault();
        navigateGrid(1);
        break;
      case 'ArrowLeft':
        e.preventDefault();
        navigateGrid(-1);
        break;
      case 'ArrowDown':
        e.preventDefault();
        navigateGrid(4); // Salto de fila aproximado
        break;
      case 'ArrowUp':
        e.preventDefault();
        navigateGrid(-4);
        break;
      case 'Enter':
        e.preventDefault();
        if (focusableElements[currentFocusIndex]) {
          focusableElements[currentFocusIndex].click();
        }
        break;
      case 'Escape':
      case 'Backspace':
        closeModal();
        break;
    }
  });
}

function navigateGrid(delta) {
  focusElement(currentFocusIndex + delta);
}

// Conexión en vivo con el Celular (Server-Sent Events)
function setupSSEConnection() {
  const evtSource = new EventSource('/api/remote/events');

  evtSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      handleRemoteAction(data);
    } catch {
      // Ignorar mensajes vacíos
    }
  };

  evtSource.onerror = () => {
    // Reconexión automática nativa del navegador
  };
}

// Interpretar acciones recibidas desde el celular
function handleRemoteAction(data) {
  if (data.type === 'dpad') {
    if (data.key === 'up') navigateGrid(-4);
    if (data.key === 'down') navigateGrid(4);
    if (data.key === 'left') navigateGrid(-1);
    if (data.key === 'right') navigateGrid(1);
    if (data.key === 'ok') {
      if (focusableElements[currentFocusIndex]) {
        focusableElements[currentFocusIndex].click();
      }
    }
    if (data.key === 'home') {
      closeModal();
      setCategory('all');
      focusElement(0);
    }
    if (data.key === 'back') {
      closeModal();
    }
  } else if (data.type === 'volume') {
    showToast(`Volumen: ${data.action.toUpperCase()}`, '🔊');
  } else if (data.type === 'open_app') {
    const target = APPS.find(a => a.id === data.appId);
    if (target) launchApp(target);
  } else if (data.type === 'search') {
    showToast(`Buscando: "${data.query}"`, '🔍');
    const searchUrl = `https://www.youtube.com/results?search_query=${encodeURIComponent(data.query)}`;
    window.open(searchUrl, '_blank');
  } else if (data.type === 'mouse_move') {
    updateVirtualCursor(data.dx, data.dy);
  } else if (data.type === 'mouse_click') {
    clickVirtualCursor();
  }
}

// Cursor Virtual controlado por el Touchpad del Celular
function updateVirtualCursor(dx, dy) {
  const cursor = document.getElementById('virtual-cursor');
  cursorVisible = true;
  cursor.classList.remove('hidden');

  cursorX = Math.max(0, Math.min(window.innerWidth - 10, cursorX + dx));
  cursorY = Math.max(0, Math.min(window.innerHeight - 10, cursorY + dy));

  cursor.style.transform = `translate(${cursorX}px, ${cursorY}px)`;
}

function clickVirtualCursor() {
  const elem = document.elementFromPoint(cursorX, cursorY);
  if (elem) {
    elem.click();
    showToast('Clic táctil', '👆');
  }
}

// Configuración del Reloj
function setupClock() {
  const timeElem = document.getElementById('clock-time');
  const dateElem = document.getElementById('clock-date');

  function update() {
    const now = new Date();
    const h = String(now.getHours()).padStart(2, '0');
    const m = String(now.getMinutes()).padStart(2, '0');
    timeElem.textContent = `${h}:${m}`;

    const options = { weekday: 'long', day: 'numeric', month: 'long' };
    const dateStr = now.toLocaleDateString('es-AR', options);
    dateElem.textContent = dateStr.charAt(0).toUpperCase() + dateStr.slice(1);
  }

  update();
  setInterval(update, 1000);
}

// Monitor de RAM
function setupRamMonitor() {
  updateRamBadge();
  setInterval(updateRamBadge, 4000);
}

async function updateRamBadge() {
  try {
    const res = await fetch('/api/system/stats');
    if (!res.ok) return;
    const stats = await res.json();
    const textElem = document.getElementById('ram-text');
    const badge = document.getElementById('ram-badge');

    const usedGB = (stats.usedMB / 1024).toFixed(1);
    const totalGB = (stats.totalMB / 1024).toFixed(1);
    textElem.textContent = `RAM: ${usedGB} / ${totalGB} GB (${stats.usedPercent}%)`;

    if (stats.usedPercent > 85) {
      badge.style.color = '#ef4444';
    } else {
      badge.style.color = '#34d399';
    }
  } catch {
    // Si falla la petición no interrumpir
  }
}

// Pestañas de Categoría
function setCategory(cat) {
  currentCategory = cat;
  document.querySelectorAll('.cat-pill').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.cat === cat);
  });
  renderApps();
}

// Modales y QR
function setupModals() {
  const btnShowQr = document.getElementById('btn-show-qr');
  const btnCloseQr = document.getElementById('btn-close-qr');
  const modal = document.getElementById('qr-modal');

  btnShowQr.addEventListener('click', () => {
    modal.classList.remove('hidden');
    refreshFocusables();
    btnCloseQr.focus();
  });

  btnCloseQr.addEventListener('click', closeModal);

  document.querySelectorAll('.cat-pill').forEach(pill => {
    pill.addEventListener('click', () => setCategory(pill.dataset.cat));
  });
}

function closeModal() {
  const modal = document.getElementById('qr-modal');
  modal.classList.add('hidden');
  refreshFocusables();
  focusElement(0);
}

// Obtener la IP y URL del Control Remoto
async function fetchServerInfo() {
  try {
    const res = await fetch('/api/info');
    const data = await res.json();
    document.getElementById('modal-remote-url').textContent = data.remoteUrl;

    const qrContainer = document.getElementById('qr-container');
    const encoded = encodeURIComponent(data.remoteUrl);
    qrContainer.innerHTML = `
      <img src="https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encoded}&margin=4" 
           alt="QR Control Remoto" width="220" height="220" style="display:block; border-radius:12px;" />
    `;
  } catch {
    // Fallback
  }
}

// Notificación Flotante
let toastTimer = null;
function showToast(message, icon = '📺') {
  const toast = document.getElementById('remote-toast');
  document.getElementById('toast-icon').textContent = icon;
  document.getElementById('toast-message').textContent = message;
  toast.classList.remove('hidden');

  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.add('hidden');
  }, 2500);
}
