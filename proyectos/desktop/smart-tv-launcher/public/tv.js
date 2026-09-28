// tv.js - Controlador Principal de la Pantalla 10-Foot UI de Smart TV
import { 
  initYouTubeTV, 
  openYouTubeTV, 
  closeYouTubeTV, 
  handlePlayerAction, 
  isYouTubePlayerActive, 
  isYouTubeViewActive 
} from './youtube-tv.js';

const APPS = [
  { id: 'youtube', name: 'YouTube TV', category: 'streaming', icon: '▶️', color: '#ff0000', action: 'youtube_tv' },
  { id: 'plutotv', name: 'Pluto TV', category: 'tv', icon: '📺', color: '#ffd100', url: 'https://pluto.tv' },
  { id: 'netflix', name: 'Netflix', category: 'streaming', icon: '🍿', color: '#e50914', url: 'https://www.netflix.com' },
  { id: 'disney', name: 'Disney+', category: 'streaming', icon: '✨', color: '#113ccf', url: 'https://www.disneyplus.com' },
  { id: 'prime', name: 'Prime Video', category: 'streaming', icon: '📦', color: '#00a8e1', url: 'https://www.primevideo.com' },
  { id: 'tn_vivo', name: 'TN en Vivo', category: 'tv', icon: '📡', color: '#dc2626', action: 'search_tn' },
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

window.showTvToast = showToast;
window.refreshTvFocusables = refreshFocusables;

document.addEventListener('DOMContentLoaded', () => {
  initYouTubeTV();
  renderApps();
  setupClock();
  setupRamMonitor();
  setupKeyboardNavigation();
  setupSSEConnection();
  setupModals();
  fetchServerInfo();
});

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
  if (focusableElements.length > 0) focusElement(0);
}

function launchApp(app) {
  if (app.action === 'youtube_tv') {
    showToast('Iniciando YouTube TV...', '▶️');
    openYouTubeTV('tendencias musica argentina');
    return;
  }
  if (app.action === 'search_tn') {
    showToast('Sintonizando TN en Vivo...', '📡');
    openYouTubeTV('tn en vivo argentina');
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
    return;
  }
  if (app.url) {
    showToast(`Abriendo ${app.name}...`, app.icon);
    window.location.href = app.url;
  }
}

export function refreshFocusables() {
  const activeView = isYouTubeViewActive() 
    ? document.getElementById('view-youtube-tv') 
    : document.getElementById('view-launcher');
  focusableElements = Array.from(activeView.querySelectorAll('.focusable:not([tabindex="-1"])'));
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
      case 'ArrowRight': e.preventDefault(); navigateGrid(1); break;
      case 'ArrowLeft': e.preventDefault(); navigateGrid(-1); break;
      case 'ArrowDown': e.preventDefault(); navigateGrid(4); break;
      case 'ArrowUp': e.preventDefault(); navigateGrid(-4); break;
      case 'Enter':
        e.preventDefault();
        if (focusableElements[currentFocusIndex]) focusableElements[currentFocusIndex].click();
        break;
      case 'Escape':
      case 'Backspace':
        e.preventDefault();
        if (isYouTubeViewActive()) closeYouTubeTV();
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
    if (data.key === 'up') navigateGrid(-4);
    if (data.key === 'down') navigateGrid(4);
    if (data.key === 'left') navigateGrid(-1);
    if (data.key === 'right') navigateGrid(1);
    if (data.key === 'ok') {
      if (focusableElements[currentFocusIndex]) focusableElements[currentFocusIndex].click();
    }
    if (data.key === 'home') {
      closeModal();
      if (isYouTubeViewActive()) closeYouTubeTV();
      setCategory('all');
      focusElement(0);
    }
    if (data.key === 'back') {
      if (isYouTubeViewActive()) closeYouTubeTV();
      else closeModal();
    }
  } else if (data.type === 'volume') {
    showToast(`Volumen: ${data.action.toUpperCase()}`, '🔊');
  } else if (data.type === 'open_app') {
    if (data.appId === 'youtube') openYouTubeTV('tendencias musica argentina');
    else {
      const target = APPS.find(a => a.id === data.appId);
      if (target) launchApp(target);
    }
  } else if (data.type === 'search') {
    showToast(`Buscando en YouTube: "${data.query}"`, '🔍');
    openYouTubeTV(data.query);
  } else if (data.type === 'playback') {
    handlePlayerAction('play_pause');
  } else if (data.type === 'mouse_move') {
    updateVirtualCursor(data.dx, data.dy);
  } else if (data.type === 'mouse_click') {
    clickVirtualCursor();
  }
}

function updateVirtualCursor(dx, dy) {
  const cursor = document.getElementById('virtual-cursor');
  cursor.classList.remove('hidden');
  cursorX = Math.max(0, Math.min(window.innerWidth - 10, cursorX + dx));
  cursorY = Math.max(0, Math.min(window.innerHeight - 10, cursorY + dy));
  cursor.style.transform = `translate(${cursorX}px, ${cursorY}px)`;
}

function clickVirtualCursor() {
  const elem = document.elementFromPoint(cursorX, cursorY);
  if (elem) { elem.click(); showToast('Clic táctil', '👆'); }
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
