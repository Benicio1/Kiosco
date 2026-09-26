// pwa.js - Gestor de Instalación PWA y Service Worker para Kiosco Faltantes
(function () {
  'use strict';

  let deferredPrompt = null;

  // 1. Registro seguro del Service Worker
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      // Determinamos ruta relativa segura según si está en GitHub Pages o local
      const isGhPages = window.location.pathname.startsWith('/Kiosco');
      const swUrl = isGhPages ? '/Kiosco/sw.js' : './sw.js';

      navigator.serviceWorker.register(swUrl)
        .then((reg) => {
          console.log('[PWA] Service Worker registrado correctamente:', reg.scope);
        })
        .catch((err) => {
          console.warn('[PWA] Error registrando Service Worker:', err);
        });
    });
  }

  // 2. Comprobar si ya se está ejecutando como app instalada (standalone)
  function esAppInstalada() {
    return window.matchMedia('(display-mode: standalone)').matches ||
           window.navigator.standalone === true;
  }

  // 3. Captura del evento de instalación nativo (Chrome Android / Edge / Desktop)
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;

    if (!esAppInstalada() && !sessionStorage.getItem('kiosco_pwa_dismissed')) {
      mostrarBannerInstalacion();
    }
  });

  function mostrarBannerInstalacion() {
    const banner = document.getElementById('pwa-install-banner');
    if (banner) {
      banner.style.display = 'flex';
    }
  }

  // 4. Acción de Instalar al tocar el botón del cartel
  window.instalarPWA = async function () {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const choice = await deferredPrompt.userChoice;
      console.log('[PWA] Respuesta del usuario:', choice.outcome);
      if (choice.outcome === 'accepted') {
        const banner = document.getElementById('pwa-install-banner');
        if (banner) banner.style.display = 'none';
      }
      deferredPrompt = null;
    } else {
      // Si el navegador no disparó el prompt nativo aún, abrir modal guiado
      window.abrirModalGuiaInstalacion();
    }
  };

  // 5. Descartar el cartel
  window.descartarPWA = function () {
    const banner = document.getElementById('pwa-install-banner');
    if (banner) banner.style.display = 'none';
    sessionStorage.setItem('kiosco_pwa_dismissed', 'true');
  };

  // 6. Evento de instalación completada
  window.addEventListener('appinstalled', () => {
    console.log('[PWA] App instalada con éxito');
    const banner = document.getElementById('pwa-install-banner');
    if (banner) banner.style.display = 'none';
    if (typeof window.mostrarAviso === 'function') {
      window.mostrarAviso('🎉 ¡App instalada en tu pantalla principal!', 'success');
    }
  });

  // 7. Modal de ayuda para instalación manual
  window.abrirModalGuiaInstalacion = function () {
    const modal = document.getElementById('modal-instalar-pwa');
    if (modal) modal.classList.add('active');
  };

  window.cerrarModalGuiaInstalacion = function () {
    const modal = document.getElementById('modal-instalar-pwa');
    if (modal) modal.classList.remove('active');
  };

  // 8. En móviles no-standalone, mostrar el cartel tras 1 segundo si no fue descartado
  document.addEventListener('DOMContentLoaded', () => {
    if (!esAppInstalada() && !sessionStorage.getItem('kiosco_pwa_dismissed')) {
      setTimeout(() => {
        mostrarBannerInstalacion();
      }, 1000);
    }
  });
})();
