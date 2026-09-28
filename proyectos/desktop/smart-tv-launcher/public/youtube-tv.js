// youtube-tv.js - Simulación Integral de YouTube Smart TV (Leanback UI)
let ytPlayer = null;
let isPlayerReady = false;
let isPlayerActive = false;
let currentVideos = [];
let hudTimeout = null;

// Inicialización de la API de YouTube Iframe
window.onYouTubeIframeAPIReady = function() {
  ytPlayer = new YT.Player('yt-player-target', {
    height: '100%',
    width: '100%',
    playerVars: {
      autoplay: 1,
      controls: 0,        // Ocultar controles nativos de mouse para control 100% remoto
      disablekb: 1,
      enablejsapi: 1,
      fs: 0,
      modestbranding: 1,
      rel: 0,
      iv_load_policy: 3
    },
    events: {
      onReady: () => { isPlayerReady = true; },
      onStateChange: onPlayerStateChange
    }
  });
};

function onPlayerStateChange(event) {
  // 1 = Reproduciendo, 2 = Pausado, 0 = Terminado
  if (event.data === YT.PlayerState.ENDED) {
    closeVideoPlayer();
  }
}

// Inicializar eventos de la interfaz de YouTube TV
export function initYouTubeTV() {
  const btnBack = document.getElementById('btn-yt-back');
  if (btnBack) {
    btnBack.addEventListener('click', closeYouTubeTV);
  }

  // Chips de categorías
  document.querySelectorAll('.yt-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.yt-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const query = chip.dataset.query;
      loadYouTubeVideos(query, chip.textContent);
    });
  });
}

// Abrir vista de YouTube TV
export function openYouTubeTV(initialQuery = 'tendencias musica argentina') {
  document.getElementById('view-launcher').classList.add('hidden');
  document.getElementById('view-launcher').classList.remove('active');

  const ytView = document.getElementById('view-youtube-tv');
  ytView.classList.remove('hidden');
  ytView.classList.add('active');

  loadYouTubeVideos(initialQuery, 'Tendencias');
}

// Cerrar vista de YouTube TV y volver al Launcher
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

// Cargar videos desde el backend
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
      container.innerHTML = `<div class="yt-loading"><p>No se encontraron videos. Probá otra búsqueda.</p></div>`;
      return;
    }

    renderYouTubeCards(videos);
  } catch {
    container.innerHTML = `<div class="yt-loading"><p>Error al conectar con YouTube. Revisá la red.</p></div>`;
  }
}

// Renderizar las tarjetas de video en la grilla TV
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
  // Enfocar el primer video para control remoto
  const firstVideo = container.querySelector('.yt-video-card');
  if (firstVideo) firstVideo.focus();
}

// Reproducir video a pantalla completa
export function playVideo(videoId, title, channel) {
  const overlay = document.getElementById('yt-player-overlay');
  overlay.classList.remove('hidden');
  isPlayerActive = true;

  document.getElementById('player-video-title').textContent = title;
  document.getElementById('player-video-channel').textContent = channel;

  showPlayerHud();

  if (ytPlayer && isPlayerReady && ytPlayer.loadVideoById) {
    ytPlayer.loadVideoById(videoId);
    ytPlayer.playVideo();
  } else {
    // Fallback directo por si la API aún no cargó
    const target = document.getElementById('yt-player-target');
    target.innerHTML = `
      <iframe width="100%" height="100%" 
        src="https://www.youtube-nocookie.com/embed/${videoId}?autoplay=1&enablejsapi=1&controls=0&rel=0" 
        frameborder="0" allow="autoplay; encrypted-media; fullscreen" allowfullscreen>
      </iframe>
    `;
  }
}

// Mostrar HUD del reproductor con auto-ocultamiento
function showPlayerHud(durationMs = 3500) {
  const hud = document.getElementById('yt-player-hud');
  if (!hud) return;
  hud.classList.remove('fade-out');

  clearTimeout(hudTimeout);
  hudTimeout = setTimeout(() => {
    hud.classList.add('fade-out');
  }, durationMs);
}

// Cerrar el reproductor y volver a la grilla de videos
export function closeVideoPlayer() {
  const overlay = document.getElementById('yt-player-overlay');
  if (overlay) overlay.classList.add('hidden');
  isPlayerActive = false;

  if (ytPlayer && ytPlayer.stopVideo) {
    try { ytPlayer.stopVideo(); } catch { /* ignorar */ }
  }

  // Refrescar foco en la grilla de videos
  if (window.refreshTvFocusables) window.refreshTvFocusables();
  const firstCard = document.querySelector('.yt-video-card');
  if (firstCard) firstCard.focus();
}

// Manejar comandos del control remoto en el reproductor
export function handlePlayerAction(action) {
  if (!isPlayerActive) return false;

  showPlayerHud(3000);

  if (action === 'play_pause' || action === 'ok') {
    if (ytPlayer && ytPlayer.getPlayerState) {
      const state = ytPlayer.getPlayerState();
      if (state === YT.PlayerState.PLAYING) {
        ytPlayer.pauseVideo();
        if (window.showTvToast) window.showTvToast('Pausa', '⏸️');
      } else {
        ytPlayer.playVideo();
        if (window.showTvToast) window.showTvToast('Reproduciendo', '▶️');
      }
    }
    return true;
  }

  if (action === 'seek_left' || action === 'left') {
    if (ytPlayer && ytPlayer.getCurrentTime) {
      const curr = ytPlayer.getCurrentTime();
      ytPlayer.seekTo(Math.max(0, curr - 10), true);
      if (window.showTvToast) window.showTvToast('⏪ -10 seg', '⏪');
    }
    return true;
  }

  if (action === 'seek_right' || action === 'right') {
    if (ytPlayer && ytPlayer.getCurrentTime) {
      const curr = ytPlayer.getCurrentTime();
      ytPlayer.seekTo(curr + 10, true);
      if (window.showTvToast) window.showTvToast('⏩ +10 seg', '⏩');
    }
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

// Determinar si el reproductor está activo
export function isYouTubePlayerActive() {
  return isPlayerActive;
}

// Determinar si la vista de YouTube TV está activa
export function isYouTubeViewActive() {
  const ytView = document.getElementById('view-youtube-tv');
  return ytView && !ytView.classList.contains('hidden');
}
