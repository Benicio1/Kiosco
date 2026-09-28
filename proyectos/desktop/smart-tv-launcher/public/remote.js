// remote.js - Lógica del Control Remoto Móvil para Celular

// Función de vibración háptica
function vibrate(ms = 25) {
  if (navigator.vibrate) {
    try { navigator.vibrate(ms); } catch { /* ignorar */ }
  }
}

// Enviar acción al servidor de la TV
async function sendAction(actionData) {
  vibrate(25);
  try {
    const res = await fetch('/api/remote/action', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(actionData)
    });
    const statusElem = document.getElementById('connection-status');
    if (res.ok) {
      statusElem.textContent = 'Conectado a la TV';
      statusElem.parentElement.style.color = '#10b981';
    } else {
      statusElem.textContent = 'Error de conexión';
      statusElem.parentElement.style.color = '#ef4444';
    }
  } catch {
    const statusElem = document.getElementById('connection-status');
    statusElem.textContent = 'Sin conexión con la TV';
    statusElem.parentElement.style.color = '#ef4444';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  setupTabs();
  setupDpad();
  setupNavigation();
  setupVolume();
  setupShortcuts();
  setupSearch();
  setupTouchpad();
  setupTypeSender();
});

// Pestañas (D-Pad vs Touchpad)
function setupTabs() {
  const tabDpad = document.getElementById('tab-dpad');
  const tabTouchpad = document.getElementById('tab-touchpad');
  const panelDpad = document.getElementById('panel-dpad');
  const panelTouchpad = document.getElementById('panel-touchpad');

  tabDpad.addEventListener('click', () => {
    vibrate(15);
    tabDpad.classList.add('active');
    tabTouchpad.classList.remove('active');
    panelDpad.classList.add('active');
    panelTouchpad.classList.remove('active');
  });

  tabTouchpad.addEventListener('click', () => {
    vibrate(15);
    tabTouchpad.classList.add('active');
    tabDpad.classList.remove('active');
    panelTouchpad.classList.add('active');
    panelDpad.classList.remove('active');
  });
}

// D-Pad
function setupDpad() {
  document.querySelectorAll('.dpad-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const key = btn.dataset.key;
      sendAction({ type: 'dpad', key });
    });
  });
}

// Fila de Navegación
function setupNavigation() {
  document.getElementById('btn-back').addEventListener('click', () => {
    sendAction({ type: 'dpad', key: 'back' });
  });

  document.getElementById('btn-home').addEventListener('click', () => {
    sendAction({ type: 'dpad', key: 'home' });
  });

  document.getElementById('btn-power').addEventListener('click', () => {
    sendAction({ type: 'dpad', key: 'home' });
  });

  document.getElementById('btn-playpause').addEventListener('click', () => {
    sendAction({ type: 'playback', action: 'play_pause' });
  });
}

// Volumen (Afecta tanto a la app como al volumen maestro de Windows)
function setupVolume() {
  document.getElementById('btn-vol-up').addEventListener('click', () => {
    sendAction({ type: 'volume', action: 'up' });
    sendAction({ type: 'volume_hardware', action: 'up' });
  });

  document.getElementById('btn-vol-down').addEventListener('click', () => {
    sendAction({ type: 'volume', action: 'down' });
    sendAction({ type: 'volume_hardware', action: 'down' });
  });

  document.getElementById('btn-vol-mute').addEventListener('click', () => {
    sendAction({ type: 'volume', action: 'mute' });
    sendAction({ type: 'volume_hardware', action: 'mute' });
  });
}

// Accesos Directos a Apps
function setupShortcuts() {
  document.querySelectorAll('.btn-app').forEach(btn => {
    btn.addEventListener('click', () => {
      const appId = btn.dataset.app;
      sendAction({ type: 'open_app', appId });
    });
  });
}

// Búsqueda de Texto en TV
function setupSearch() {
  const input = document.getElementById('tv-search-input');
  const btnSearch = document.getElementById('btn-tv-search');

  function doSearch() {
    const query = input.value.trim();
    if (query) {
      sendAction({ type: 'search', query });
      input.value = '';
      input.blur();
    }
  }

  btnSearch.addEventListener('click', doSearch);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') doSearch();
  });
}

// Touchpad Virtual con soporte para 2 dedos (Scroll de página)
function setupTouchpad() {
  const surface = document.getElementById('touchpad-surface');
  const btnClick = document.getElementById('btn-touch-click');
  const btnScrollUp = document.getElementById('btn-scroll-up');
  const btnScrollDown = document.getElementById('btn-scroll-down');

  let lastX = 0;
  let lastY = 0;
  let isMoving = false;
  let isTwoFingers = false;
  let startTimestamp = 0;

  surface.addEventListener('touchstart', (e) => {
    if (e.touches.length === 1) {
      isMoving = true;
      isTwoFingers = false;
      lastX = e.touches[0].clientX;
      lastY = e.touches[0].clientY;
      startTimestamp = Date.now();
    } else if (e.touches.length >= 2) {
      isTwoFingers = true;
      isMoving = false;
      lastY = (e.touches[0].clientY + e.touches[1].clientY) / 2;
    }
  }, { passive: true });

  surface.addEventListener('touchmove', (e) => {
    // 2 dedos: deslizar página arriba/abajo como en la netbook
    if (e.touches.length >= 2) {
      const currentY = (e.touches[0].clientY + e.touches[1].clientY) / 2;
      const dy = currentY - lastY;
      lastY = currentY;

      // dy > 0 es deslizar hacia abajo -> scroll hacia abajo (rueda negativa en Windows)
      const scrollAmount = Math.round(dy * 5);
      sendAction({ type: 'mouse_scroll', dy: -scrollAmount });
      return;
    }

    if (!isMoving || e.touches.length !== 1) return;

    const currentX = e.touches[0].clientX;
    const currentY = e.touches[0].clientY;
    const dx = (currentX - lastX) * 2.2;
    const dy = (currentY - lastY) * 2.2;

    lastX = currentX;
    lastY = currentY;

    sendAction({ type: 'mouse_move', dx, dy });
  }, { passive: true });

  surface.addEventListener('touchend', (e) => {
    if (isMoving && !isTwoFingers) {
      isMoving = false;
      const duration = Date.now() - startTimestamp;
      if (duration < 220) {
        sendAction({ type: 'mouse_click' });
      }
    }
    if (e.touches.length === 0) {
      isTwoFingers = false;
      isMoving = false;
    }
  });

  btnClick.addEventListener('click', () => {
    sendAction({ type: 'mouse_click' });
  });

  if (btnScrollUp) {
    btnScrollUp.addEventListener('click', () => {
      sendAction({ type: 'mouse_scroll', dy: 240 });
    });
  }

  if (btnScrollDown) {
    btnScrollDown.addEventListener('click', () => {
      sendAction({ type: 'mouse_scroll', dy: -240 });
    });
  }
}

function setupTypeSender() {
  const inputType = document.getElementById('type-text-input');
  const btnSendText = document.getElementById('btn-send-text');
  if (btnSendText && inputType) {
    function doSendText() {
      const text = inputType.value;
      if (text) {
        sendAction({ type: 'type_text', text });
        inputType.value = '';
        inputType.blur();
      }
    }
    btnSendText.addEventListener('click', doSendText);
    inputType.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') doSendText();
    });
  }
}
