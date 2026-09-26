// Aegis AdBlock - Stealth Mode Defuser
// Completely invisible to anti-adblock and sandbox detectors

(() => {
  if (window.__aegis_stealth_installed) return;
  window.__aegis_stealth_installed = true;

  const currentHost = window.location.hostname;

  // Helper to make hooked functions completely undetectable (returns "[native code]")
  function makeNative(fn, name) {
    try {
      Object.defineProperty(fn, 'name', { value: name, configurable: true });
      Object.defineProperty(fn, 'toString', {
        value: function() { return `function ${name}() { [native code] }`; },
        configurable: true
      });
    } catch (e) {}
    return fn;
  }

  // 1. Anti-AdBlock Bait Shims (Tells scripts that ads are running normally)
  try {
    window.canRunAds = true;
    window.isAdBlockActive = false;
    window.adblock = false;
    window.adblocker = false;
    window.google_ad_status = 1;
  } catch (e) {}

  // 2. SOLOLATINO NATIVE VIP STATE (Their own code skips ads when VIP is active)
  try {
    localStorage.setItem("sl_vip_active", "1");
  } catch (e) {}

  try {
    Object.defineProperty(window, 'SL_USER', {
      value: true,
      writable: true,
      configurable: true
    });
    Object.defineProperty(window, 'SL_VIP_DATA', {
      value: { vip: true, days: 99999, exp: "2099-01-01" },
      writable: true,
      configurable: true
    });
    window.SL_STATE = { user: { vip: true } };
  } catch (e) {}

  // 3. Stealthy Eradication of Sololatino Ad Elements (__sl_ads and __sl_sp)
  const originalGetElementById = document.getElementById;
  document.getElementById = makeNative(function(id) {
    if (id === '__sl_ads' || id === '__sl_sp') {
      return null;
    }
    return originalGetElementById.apply(this, arguments);
  }, 'getElementById');

  const originalQuerySelector = document.querySelector;
  document.querySelector = makeNative(function(selector) {
    if (typeof selector === 'string' && (selector.includes('__sl_ads') || selector.includes('__sl_sp'))) {
      return null;
    }
    return originalQuerySelector.apply(this, arguments);
  }, 'querySelector');

  function purgeAdTags() {
    try {
      const elAds = originalGetElementById.call(document, '__sl_ads');
      if (elAds) elAds.remove();
      const elSp = originalGetElementById.call(document, '__sl_sp');
      if (elSp) elSp.remove();
    } catch (e) {}
  }
  purgeAdTags();
  document.addEventListener('DOMContentLoaded', purgeAdTags);

  // 4. Block dynamic injection of Sololatino rotating ad scripts
  const CASINO_AND_AD_KEYWORDS = [
    'casino', 'bet365', '1xbet', 'betfair', 'pin-up', 'betwinner',
    'melbet', 'mostbet', 'spinamba', 'vulkan', 'slot', 'jackpot',
    'roulette', 'gamble', 'aviator', 'apuesta', 'jugador', '1win',
    'cpmgate', 'performancegate', 'adsterra', 'popads', 'popcash',
    'monetag', 'propeller', 'deliveryengine', 'densenbl', 'lianoidmachar',
    'constbezzant', 'saltcatthebaic', 'kishkeenocyte', 'primasphobist',
    '149138', '149139', '149160', '149161', 'onclkds', 'onclickalgo'
  ];

  function isAdOrCasinoUrl(url) {
    if (!url) return false;
    const lower = String(url).toLowerCase();
    return CASINO_AND_AD_KEYWORDS.some(kw => lower.includes(kw));
  }

  const originalAppendChild = Node.prototype.appendChild;
  Node.prototype.appendChild = makeNative(function(node) {
    if (node && node.tagName === 'SCRIPT') {
      const src = node.src || node.getAttribute('src') || '';
      if (isAdOrCasinoUrl(src)) {
        console.warn('[Aegis] Suppressed dynamic ad script:', src);
        return node;
      }
    }
    return originalAppendChild.apply(this, arguments);
  }, 'appendChild');

  const originalInsertBefore = Node.prototype.insertBefore;
  Node.prototype.insertBefore = makeNative(function(node, ref) {
    if (node && node.tagName === 'SCRIPT') {
      const src = node.src || node.getAttribute('src') || '';
      if (isAdOrCasinoUrl(src)) {
        console.warn('[Aegis] Suppressed dynamic ad script insertion:', src);
        return node;
      }
    }
    return originalInsertBefore.apply(this, arguments);
  }, 'insertBefore');

  // 5. Track genuine user clicks vs automated ad clicks
  let lastTrustedClickTime = 0;
  window.addEventListener('click', (e) => {
    if (e.isTrusted) {
      lastTrustedClickTime = Date.now();
    }
  }, true);

  // 6. Stealthy window.open filter (Preserves native signature so detectors pass)
  const originalWindowOpen = window.open;
  window.open = makeNative(function(url, target, features) {
    // If URL is explicitly an ad or casino, block it cleanly
    if (url && isAdOrCasinoUrl(url)) {
      console.warn('[Aegis] Silently blocked casino/ad window.open:', url);
      // Return a working Window proxy so the script never suspects anything
      return {
        closed: false,
        focus: () => {},
        blur: () => {},
        close: () => {},
        postMessage: () => {},
        location: { href: 'about:blank', replace: () => {}, assign: () => {} },
        document: { write: () => {}, open: () => {}, close: () => {} }
      };
    }

    // Normal window.open passes through to allow legitimate features;
    // Any rogue ad popups are instantly terminated by background.js at the browser level!
    return originalWindowOpen.apply(this, arguments);
  }, 'open');

  // 7. Click Capture Filter: Remove transparent overlays over the page
  window.addEventListener('click', (e) => {
    const anchor = e.target.closest ? e.target.closest('a') : null;
    if (anchor && isAdOrCasinoUrl(anchor.href)) {
      e.preventDefault();
      e.stopPropagation();
      e.stopImmediatePropagation();
      anchor.remove();
      console.warn('[Aegis] Neutralized click on casino/ad link:', anchor.href);
      return;
    }

    // Remove full-screen clickjack overlays
    if (e.target && e.target !== document.body && e.target !== document.documentElement) {
      const style = window.getComputedStyle(e.target);
      const zIndex = parseInt(style.zIndex, 10);
      if (zIndex >= 100) {
        const rect = e.target.getBoundingClientRect();
        if (rect.width >= window.innerWidth * 0.7 && rect.height >= window.innerHeight * 0.7) {
          if ((e.target.innerText || '').trim().length < 5) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            e.target.remove();
            console.warn('[Aegis] Destroyed clickjack overlay');
            return;
          }
        }
      }
    }
  }, true);

})();
