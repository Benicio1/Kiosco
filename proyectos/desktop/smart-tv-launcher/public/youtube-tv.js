// youtube-tv.js - Simulación Integral de YouTube Smart TV (Leanback UI)
let ytPlayer = null;
let isPlayerActive = false;
let isVideoPlaying = true;
let videoCurrentSec = 0;
let currentVideos = [];
let hudTimeout = null;

// Escuchar eventos postMessage desde el reproductor de YouTube
window.addEventListener('message', (event) => {
  try {
    const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
    if (data && data.event === 'infoDelivery' && data.info) {
      if (typeof data.info.currentTime === 'number') {
        videoCurrentSec = data.info.currentTime;
      }
      if (typeof data.info.playerState === 'number') {
        if (data.info.playerState === 1) isVideoPlaying = true;
        if (data.info.playerState === 2) isVideoPlaying = false;
        if (data.info.playerState === 0) closeVideoPlayer();
      }
    }
  } catch { /* ignorar */ }
});

// Enviar comandos al iframe embebido de YouTube
function sendIframeCommand(func, args = []) {
  const iframe = document.getElementById('yt-player-iframe');
  if (iframe && iframe.contentWindow) {
    iframe.contentWindow.postMessage(JSON.stringify({
      event: 'command',
      func: func,
      args: args
    }), '*');
  }
}

export function initYouTubeTV() {
  const btnBack = document.getElementById('btn-yt-back');
  if (btnBack) btnBack.addEventListener('click', closeYouTubeTV);

  document.querySelectorAll('.yt-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.yt-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      loadYouTubeVideos(chip.dataset.query, chip.textContent);
    });
  });
}

export function openYouTubeTV(initialQuery = 'tendencias musica argentina') {
  document.getElementById('view-launcher').classList.add('hidden');
  document.getElementById('view-launcher').classList.remove('active');

  const ytView = document.getElementById('view-youtube-tv');
  ytView.classList.remove('hidden');
  ytView.classList.add('active');

  loadYouTubeVideos(initialQuery, 'Tendencias');
}

export function closeYouTubeTV() {
  if (isPlayerActive) {
    closeVideoPlayer();
    return;
  }
  const ytView = document.getElementById('view-youtube-tv');
  ytView.classList.add('hidden');
  ytView.classList.remove('active');

  const launcher = document.getElementById('view-launcher');
  launcher.classList.remove('hidden');
  launcher.classList.add('active');

  if (window.refreshTvFocusables) window.refreshTvFocusables();
}

export async function loadYouTubeVideos(query, label = 'Búsqueda') {
  const container = document.getElementById('yt-videos-container');
  const searchText = document.getElementById('yt-search-text');
  if (searchText) searchText.textContent = label;

  container.innerHTML = `
    <div class="yt-loading">
      <span class="yt-spinner"></span>
      <p>Cargando videos de YouTube TV...</p>
    </div>
  `;

  try {
    const res = await fetch(`/api/youtube/search?q=${encodeURIComponent(query)}`);
    const videos = await res.json();
    currentVideos = videos;

    if (!videos || videos.length === 0) {
      container.innerHTML = `<div class="yt-loading"><p>No se encontraron videos.</p></div>`;
      return;
    }
    renderYouTubeCards(videos);
  } catch {
    container.innerHTML = `<div class="yt-loading"><p>Error al conectar con YouTube.</p></div>`;
  }
}

function renderYouTubeCards(videos) {
  const container = document.getElementById('yt-videos-container');
  container.innerHTML = '';

  videos.forEach((vid, idx) => {
    const card = document.createElement('div');
    card.className = 'yt-video-card focusable';
    card.tabIndex = 0;
    card.dataset.videoId = vid.id;
    card.dataset.index = idx;

    card.innerHTML = `
      <div class="yt-thumb-box">
        <img class="yt-thumb" src="${vid.thumbnail}" alt="${vid.title}" loading="lazy" />
        ${vid.duration ? `<span class="yt-duration">${vid.duration}</span>` : ''}
      </div>
      <div class="yt-card-info">
        <h4 class="yt-card-title">${vid.title}</h4>
        <p class="yt-card-channel">${vid.channel}</p>
      </div>
    `;

    card.addEventListener('click', () => {
      playVideo(vid.id, vid.title, vid.channel);
    });

    container.appendChild(card);
  });

  if (window.refreshTvFocusables) window.refreshTvFocusables();
  const firstVideo = container.querySelector('.yt-video-card');
  if (firstVideo) firstVideo.focus();
}

export function playVideo(videoId, title, channel) {
  const overlay = document.getElementById('yt-player-overlay');
  overlay.classList.remove('hidden');
  isPlayerActive = true;
  isVideoPlaying = true;
  videoCurrentSec = 0;

  document.getElementById('player-video-title').textContent = title;
  document.getElementById('player-video-channel').textContent = channel;
  showPlayerHud(3500);

  const target = document.getElementById('yt-player-target');
  target.innerHTML = `
    <iframe id="yt-player-iframe" width="100%" height="100%" 
      src="https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1&enablejsapi=1&controls=0&rel=0&iv_load_policy=3" 
      frameborder="0" allow="autoplay; encrypted-media; fullscreen" allowfullscreen>
    </iframe>
  `;
}

function showPlayerHud(durationMs = 3500) {
  const hud = document.getElementById('yt-player-hud');
  if (!hud) return;
  hud.classList.remove('fade-out');

  clearTimeout(hudTimeout);
  hudTimeout = setTimeout(() => {
    hud.classList.add('fade-out');
  }, durationMs);
}

export function closeVideoPlayer() {
  const overlay = document.getElementById('yt-player-overlay');
  if (overlay) overlay.classList.add('hidden');
  isPlayerActive = false;

  const target = document.getElementById('yt-player-target');
  if (target) target.innerHTML = '';

  if (window.refreshTvFocusables) window.refreshTvFocusables();
  const firstCard = document.querySelector('.yt-video-card');
  if (firstCard) firstCard.focus();
}

export function handlePlayerAction(action) {
  if (!isPlayerActive) return false;
  showPlayerHud(3000);

  if (action === 'play_pause' || action === 'ok') {
    if (isVideoPlaying) {
      sendIframeCommand('pauseVideo');
      isVideoPlaying = false;
      if (window.showTvToast) window.showTvToast('Pausa', '⏸️');
    } else {
      sendIframeCommand('playVideo');
      isVideoPlaying = true;
      if (window.showTvToast) window.showTvToast('Reproduciendo', '▶️');
    }
    return true;
  }

  if (action === 'seek_left' || action === 'left') {
    videoCurrentSec = Math.max(0, videoCurrentSec - 10);
    sendIframeCommand('seekTo', [videoCurrentSec, true]);
    if (window.showTvToast) window.showTvToast('⏪ -10 seg', '⏪');
    return true;
  }

  if (action === 'seek_right' || action === 'right') {
    videoCurrentSec += 10;
    sendIframeCommand('seekTo', [videoCurrentSec, true]);
    if (window.showTvToast) window.showTvToast('⏩ +10 seg', '⏩');
    return true;
  }

  if (action === 'back') {
    closeVideoPlayer();
    return true;
  }

  if (action === 'home') {
    closeVideoPlayer();
    closeYouTubeTV();
    return true;
  }

  return false;
}

export function isYouTubePlayerActive() {
  return isPlayerActive;
}

export function isYouTubeViewActive() {
  const ytView = document.getElementById('view-youtube-tv');
  return ytView && !ytView.classList.contains('hidden');
}
