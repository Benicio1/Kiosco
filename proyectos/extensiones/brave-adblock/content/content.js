(() => {
  const currentHost = window.location.hostname;
  let isEnabled = true;
  let isWhitelisted = false;
  let blockedCount = 0;

  // Query background for settings
  chrome.storage.local.get(['adblock_enabled', 'whitelist'], (result) => {
    isEnabled = result.adblock_enabled !== false;
    const whitelist = result.whitelist || [];
    isWhitelisted = whitelist.some(domain => currentHost === domain || currentHost.endsWith('.' + domain));

    if (isEnabled && !isWhitelisted) {
      initAdBlocker();
    }
  });

  // Listen for dynamic toggle messages
  chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'GET_PAGE_STATS') {
      sendResponse({ blockedCount, host: currentHost });
    }
  });

  function initAdBlocker() {
    scanAndRemoveAds();
    setupMutationObserver();
    setupPlayerGuard();
    setupYouTubeAdBypass();
  }

  function reportBlocked(count) {
    if (count <= 0) return;
    blockedCount += count;
    try {
      chrome.runtime.sendMessage({
        type: 'INCREMENT_BLOCKED',
        count: count,
        url: window.location.href
      });
    } catch (e) {
      // Context might be invalidated on tab close
    }
  }

  // Selectors for dynamic removal
  const AD_SELECTORS = [
    'ins.adsbygoogle',
    'iframe[src*="doubleclick.net"]',
    'iframe[src*="googlesyndication.com"]',
    'iframe[src*="adnxs.com"]',
    'iframe[src*="criteo.com"]',
    'iframe[src*="amazon-adsystem.com"]',
    'iframe[src*="taboola.com"]',
    'iframe[src*="outbrain.com"]',
    'iframe[src*="highperformancegate"]',
    'iframe[src*="monetag"]',
    'iframe[src*="adsterra"]',
    'iframe[src*="exoclick"]',
    'iframe[src*="hilltopads"]',
    'iframe[src*="popcash"]',
    'iframe[id*="google_ads"]',
    'div[id*="div-gpt-ad"]',
    'div[id*="google_ads"]',
    'div[data-ad-unit]',
    'div[class*="popunder"]',
    'div[id*="popunder"]',
    'div[class*="click-layer"]',
    'div[id*="click-layer"]',
    'div[class*="player-overlay-ad"]',
    'div[class*="video-ad-overlay"]',
    'a[href*="highperformancegate"]',
    'a[href*="adsterra"]',
    'a[href*="effectivecpmgate"]',
    'a[href*="profitablegatecpm"]',
    'a[href*="1xbet"]',
    'a[href*="bet365"]',
    'a[href*="pin-up"]',
    '.trc_rbox_div',
    '.ytd-ad-slot-renderer',
    '.ytp-ad-overlay-container'
  ];

  function scanAndRemoveAds() {
    let found = 0;
    const combinedSelector = AD_SELECTORS.join(', ');
    const elements = document.querySelectorAll(combinedSelector);

    elements.forEach(el => {
      if (!el.dataset.aegisBlocked) {
        el.dataset.aegisBlocked = 'true';
        el.style.setProperty('display', 'none', 'important');
        el.style.setProperty('visibility', 'hidden', 'important');
        el.remove();
        found++;
      }
    });

    if (found > 0) {
      reportBlocked(found);
    }
  }

  // Player Guard: Silently remove invisible overlays over the player WITHOUT touching iframe attributes
  function setupPlayerGuard() {
    function cleanPlayerOverlays() {
      const playerWraps = document.querySelectorAll('#main-player-wrap, #player-section, .player-wrap');
      playerWraps.forEach(wrap => {
        const potentialOverlays = wrap.querySelectorAll('div, a, span');
        potentialOverlays.forEach(el => {
          if (el.id === 'player-frame' || el.tagName === 'IFRAME' || el.tagName === 'VIDEO') return;
          const style = window.getComputedStyle(el);
          const zIndex = parseInt(style.zIndex, 10);
          if (zIndex >= 10 && (style.position === 'absolute' || style.position === 'fixed')) {
            const hasControls = el.querySelectorAll('button, input, select').length > 0;
            if (!hasControls) {
              el.remove();
            }
          }
        });
      });
    }

    cleanPlayerOverlays();
    setInterval(cleanPlayerOverlays, 600);
  }

  function setupMutationObserver() {
    let scheduled = false;

    const observer = new MutationObserver(() => {
      if (scheduled) return;
      scheduled = true;

      setTimeout(() => {
        scheduled = false;
        scanAndRemoveAds();
        checkYouTubeSkip();
      }, 250);
    });

    if (document.body) {
      observer.observe(document.body, { childList: true, subtree: true });
    } else {
      document.addEventListener('DOMContentLoaded', () => {
        if (document.body) {
          observer.observe(document.body, { childList: true, subtree: true });
        }
      });
    }
  }

  // YouTube Specific Video Ad Handlers
  function checkYouTubeSkip() {
    if (!window.location.hostname.includes('youtube.com')) return;

    const skipButtons = document.querySelectorAll(
      '.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button, .ytp-ad-skip-button-container button'
    );
    skipButtons.forEach(btn => {
      btn.click();
      reportBlocked(1);
    });

    const video = document.querySelector('video');
    const adShowing = document.querySelector('.ad-showing, .ad-interrupting');
    if (adShowing && video && !isNaN(video.duration) && video.duration > 0) {
      video.muted = true;
      video.playbackRate = 16.0;
      video.currentTime = video.duration;
      reportBlocked(1);
    }
  }

  function setupYouTubeAdBypass() {
    if (!window.location.hostname.includes('youtube.com')) return;
    setInterval(checkYouTubeSkip, 500);
  }
})();
