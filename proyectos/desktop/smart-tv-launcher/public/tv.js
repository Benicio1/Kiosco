// tv.js - Controlador Principal de la Pantalla 10-Foot UI de Smart TV
import { 
  initYouTubeTV, 
  openYouTubeTV, 
  closeYouTubeTV, 
  handlePlayerAction, 
  isYouTubePlayerActive, 
  isYouTubeViewActive 
} from './youtube-tv.js';
import { 
  initCrunchyrollTV, 
  openCrunchyrollTV, 
  closeCrunchyrollTV, 
  isCrunchyrollViewActive,
  isCrunchyrollModalActive,
  closeAnimeModal
} from './crunchyroll-tv.js';

const YOUTUBE_SVG = `<svg viewBox="0 0 24 24" class="app-svg" fill="#ff0000"><path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/></svg>`;

const CRUNCHYROLL_IMG = `<img src="/crunchyroll-icon.png" alt="Crunchyroll" class="app-img-icon" />`;

const APPS = [
  { id: 'youtube', name: 'YouTube TV', category: 'streaming', badge: 'Streaming & Música', iconSvg: YOUTUBE_SVG, color: '#ff0000', action: 'youtube_tv' },
  { id: 'crunchyroll', name: 'Crunchyroll', category: 'streaming', badge: 'Anime Oficial', iconSvg: CRUNCHYROLL_IMG, color: '#f47521', action: 'crunchyroll_direct' },
  { id: 'local_video', name: 'Videos Locales', category: 'system', badge: 'Archivos PC', icon: '📁', color: '#6366f1', action: 'open_file_dialog' },
  { id: 'turbo_ram', name: 'Optimizar RAM', category: 'system', badge: 'Liberar Memoria', icon: '⚡', color: '#10b981', action: 'clean_ram' },
  { id: 'exit_tv', name: 'Salir de la TV', category: 'system', badge: 'Apagar', icon: '🛑', color: '#ef4444', action: 'exit_app' }
];

let currentCategory = 'all';
let currentFocusIndex = 0;
let focusableElements = [];

window.showTvToast = showToast;
window.refreshTvFocusables = refreshFocusables;

document.addEventListener('DOMContentLoaded', () => {
  initYouTubeTV();
  initCrunchyrollTV();
  renderApps();
  setupClock();
  setupRamMonitor();
  setupKeyboardNavigation();
  setupSSEConnection();
  setupModals();
  fetchServerInfo();
  setupExitButton();
});

function setupExitButton() {
  const btnExit = document.getElementById('btn-header-exit');
  if (btnExit) {
    btnExit.addEventListener('click', () => {
      showToast('Cerrando Smart TV...', '🛑');
      fetch('/api/system/exit', { method: 'POST' }).catch(() => {});
    });
  }
}

function renderApps() {
  const container = document.getElementById('apps-container');
  container.innerHTML = '';
  const filtered = currentCategory === 'all' ? APPS : APPS.filter(a => a.category === currentCategory);

  filtered.forEach((app, idx) => {
    const card = document.createElement('div');
    card.className = 'app-card focusable';
    card.tabIndex = 0;
    card.dataset.index = idx;
    card.dataset.appId = app.id;
    card.innerHTML = `
      <div class="card-icon" style="background: ${app.color}15; border: 1px solid ${app.color}35;">
        ${app.iconSvg || `<span style="font-size: 38px;">${app.icon}</span>`}
      </div>
      <span class="card-title">${app.name}</span>
      <span class="card-badge" style="background: ${app.color}25; color: ${app.color};">${app.badge || app.category}</span>
    `;
    card.addEventListener('click', () => launchApp(app));
    container.appendChild(card);
  });

  refreshFocusables();
  if (focusableElements.length > 0) focusElement(0);
}

function launchApp(app) {
  if (app.action === 'exit_app' || app.id === 'exit_tv') {
    showToast('Cerrando Smart TV...', '🛑');
    fetch('/api/system/exit', { method: 'POST' }).catch(() => {});
    return;
  }
  if (app.action === 'youtube_tv') {
    showToast('Iniciando YouTube TV...', '▶️');
    openYouTubeTV('tendencias musica argentina');
    return;
  }
  if (app.action === 'crunchyroll_direct' || app.id === 'crunchyroll') {
    showToast('Iniciando Crunchyroll...', '🟠');
    openCrunchyrollTV();
    return;
  }
  if (app.action === 'clean_ram') {
    showToast('Liberando memoria RAM...', '⚡');
    setTimeout(() => { updateRamBadge(); showToast('¡Memoria optimizada!', '✅'); }, 500);
    return;
  }
  if (app.action === 'open_file_dialog') {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'video/*';
    input.onchange = (e) => {
      const file = e.target.files[0];
      if (file) window.location.href = URL.createObjectURL(file);
    };
    input.click();
  }
}

export function refreshFocusables() {
  let activeContainer = document.getElementById('view-launcher');
  if (isCrunchyrollModalActive()) {
    activeContainer = document.getElementById('cr-anime-modal');
  } else if (isCrunchyrollViewActive()) {
    activeContainer = document.getElementById('view-crunchyroll-tv');
  } else if (isYouTubeViewActive()) {
    activeContainer = document.getElementById('view-youtube-tv');
  }
  if (!activeContainer) return;
  focusableElements = Array.from(activeContainer.querySelectorAll('.focusable:not([tabindex="-1"])'));
}

function focusElement(index) {
  if (focusableElements.length === 0) return;
  currentFocusIndex = (index + focusableElements.length) % focusableElements.length;
  focusableElements[currentFocusIndex].focus();
}

function navigateGrid(delta) {
  focusElement(currentFocusIndex + delta);
}

function setupKeyboardNavigation() {
  window.addEventListener('keydown', (e) => {
    if (isYouTubePlayerActive()) {
      if (e.key === ' ' || e.key === 'Enter') { e.preventDefault(); handlePlayerAction('play_pause'); return; }
      if (e.key === 'ArrowLeft') { e.preventDefault(); handlePlayerAction('seek_left'); return; }
      if (e.key === 'ArrowRight') { e.preventDefault(); handlePlayerAction('seek_right'); return; }
      if (e.key === 'Escape' || e.key === 'Backspace') { e.preventDefault(); handlePlayerAction('back'); return; }
    }
    switch (e.key) {
      case 'ArrowRight':
      case 'ArrowDown':
        e.preventDefault();
        navigateGrid(1);
        break;
      case 'ArrowLeft':
      case 'ArrowUp':
        e.preventDefault();
        navigateGrid(-1);
        break;
      case 'Enter':
        e.preventDefault();
        if (focusableElements[currentFocusIndex]) focusableElements[currentFocusIndex].click();
        break;
      case 'Escape':
      case 'Backspace':
        e.preventDefault();
        if (isCrunchyrollModalActive()) closeAnimeModal();
        else if (isCrunchyrollViewActive()) closeCrunchyrollTV();
        else if (isYouTubeViewActive()) closeYouTubeTV();
        else closeModal();
        break;
    }
  });
}

function setupSSEConnection() {
  const evtSource = new EventSource('/api/remote/events');
  evtSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      handleRemoteAction(data);
    } catch { /* ignorar */ }
  };
}

function handleRemoteAction(data) {
  if (isYouTubePlayerActive()) {
    if (data.type === 'dpad') {
      if (data.key === 'ok') { handlePlayerAction('ok'); return; }
      if (data.key === 'left') { handlePlayerAction('seek_left'); return; }
      if (data.key === 'right') { handlePlayerAction('seek_right'); return; }
      if (data.key === 'back') { handlePlayerAction('back'); return; }
      if (data.key === 'home') { handlePlayerAction('home'); return; }
    }
    if (data.type === 'playback') { handlePlayerAction('play_pause'); return; }
  }

  if (data.type === 'dpad') {
    if (data.key === 'right' || data.key === 'down') navigateGrid(1);
    if (data.key === 'left' || data.key === 'up') navigateGrid(-1);
    if (data.key === 'ok') {
      if (focusableElements[currentFocusIndex]) focusableElements[currentFocusIndex].click();
    }
    if (data.key === 'home') {
      closeModal();
      if (isCrunchyrollModalActive()) closeAnimeModal();
      if (isCrunchyrollViewActive()) closeCrunchyrollTV();
      if (isYouTubeViewActive()) closeYouTubeTV();
      setCategory('all');
      focusElement(0);
    }
    if (data.key === 'back') {
      if (isCrunchyrollModalActive()) closeAnimeModal();
      else if (isCrunchyrollViewActive()) closeCrunchyrollTV();
      else if (isYouTubeViewActive()) closeYouTubeTV();
      else closeModal();
    }
  } else if (data.type === 'open_url') {
    showToast('Cargando página...', '🌐');
    window.location.href = data.url;
  } else if (data.type === 'volume') {
    showToast(`Volumen: ${data.action.toUpperCase()}`, '🔊');
  } else if (data.type === 'open_app') {
    if (data.appId === 'youtube') openYouTubeTV('tendencias musica argentina');
    else if (data.appId === 'crunchyroll') {
      showToast('Iniciando Crunchyroll...', '🟠');
      openCrunchyrollTV();
    } else {
      const target = APPS.find(a => a.id === data.appId);
      if (target) launchApp(target);
    }
  } else if (data.type === 'search') {
    showToast(`Buscando en YouTube: "${data.query}"`, '🔍');
    openYouTubeTV(data.query);
  } else if (data.type === 'playback') {
    handlePlayerAction('play_pause');
  } else if (data.type === 'exit_app' || data.type === 'exit_tv') {
    showToast('Cerrando Smart TV...', '🛑');
    fetch('/api/system/exit', { method: 'POST' }).catch(() => {});
  } else if (data.type === 'app_mouse_move') {
    handleAppMouseMove(data.dx, data.dy);
  } else if (data.type === 'app_mouse_click') {
    handleAppMouseClick();
  } else if (data.type === 'app_mouse_scroll') {
    handleAppMouseScroll(data.dy);
  }
}

let appCursorX = window.innerWidth / 2;
let appCursorY = window.innerHeight / 2;

function handleAppMouseMove(dx, dy) {
  const cursor = document.getElementById('tv-app-cursor');
  if (!cursor) return;
  cursor.classList.remove('hidden');
  document.body.classList.add('app-cursor-active');

  appCursorX = Math.max(0, Math.min(window.innerWidth - 12, appCursorX + dx));
  appCursorY = Math.max(0, Math.min(window.innerHeight - 12, appCursorY + dy));

  cursor.style.transform = `translate3d(${appCursorX}px, ${appCursorY}px, 0)`;
}

function handleAppMouseClick() {
  const cursor = document.getElementById('tv-app-cursor');
  if (cursor) {
    cursor.classList.add('clicking');
    setTimeout(() => cursor.classList.remove('clicking'), 150);
  }
  const target = document.elementFromPoint(appCursorX, appCursorY);
  if (target) {
    target.click();
    showToast('Clic táctil', '👆');
  }
}

function handleAppMouseScroll(dy) {
  const activeContainer = document.querySelector('.tv-main, .cr-main');
  if (activeContainer) {
    activeContainer.scrollBy({ top: dy, behavior: 'smooth' });
  } else {
    window.scrollBy({ top: dy, behavior: 'smooth' });
  }
}

function setupClock() {
  const timeElem = document.getElementById('clock-time');
  const dateElem = document.getElementById('clock-date');
  function update() {
    const now = new Date();
    timeElem.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    const opts = { weekday: 'long', day: 'numeric', month: 'long' };
    const dateStr = now.toLocaleDateString('es-AR', opts);
    dateElem.textContent = dateStr.charAt(0).toUpperCase() + dateStr.slice(1);
  }
  update();
  setInterval(update, 1000);
}

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
    badge.style.color = stats.usedPercent > 85 ? '#ef4444' : '#34d399';
  } catch { /* ignorar */ }
}

function setCategory(cat) {
  currentCategory = cat;
  document.querySelectorAll('.cat-pill').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.cat === cat);
  });
  renderApps();
}

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
  const btnCrBack = document.getElementById('btn-cr-back');
  if (btnCrBack) {
    btnCrBack.addEventListener('click', closeCrunchyrollTV);
  }
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
  } catch { /* ignorar */ }
}

let toastTimer = null;
function showToast(message, icon = '📺') {
  const toast = document.getElementById('remote-toast');
  document.getElementById('toast-icon').textContent = icon;
  document.getElementById('toast-message').textContent = message;
  toast.classList.remove('hidden');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.classList.add('hidden'); }, 2500);
}
