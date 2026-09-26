document.addEventListener('DOMContentLoaded', async () => {
  const globalToggle = document.getElementById('global-toggle');
  const statusHero = document.getElementById('status-hero');
  const statusText = document.getElementById('status-text');
  const statusDesc = document.getElementById('status-desc');
  const pageCountEl = document.getElementById('page-count');
  const totalCountEl = document.getElementById('total-count');
  const siteDomainEl = document.getElementById('site-domain');
  const siteToggleBtn = document.getElementById('site-toggle-btn');
  const siteBtnText = document.getElementById('site-btn-text');

  let currentTab = null;
  let currentDomain = '';
  let isGlobalEnabled = true;
  let whitelist = [];

  // 1. Get active tab
  try {
    const tabs = await chrome.tabs.query({ active: true, currentWindow: true });
    if (tabs && tabs.length > 0) {
      currentTab = tabs[0];
      if (currentTab.url && (currentTab.url.startsWith('http://') || currentTab.url.startsWith('https://'))) {
        try {
          const urlObj = new URL(currentTab.url);
          currentDomain = urlObj.hostname;
          siteDomainEl.textContent = currentDomain;
        } catch (e) {
          siteDomainEl.textContent = 'Página local / interna';
          siteToggleBtn.disabled = true;
        }
      } else {
        siteDomainEl.textContent = 'Página interna del navegador';
        siteToggleBtn.disabled = true;
      }
    }
  } catch (err) {
    console.error('Error fetching tab:', err);
  }

  // 2. Load stored settings & stats
  const data = await chrome.storage.local.get(['adblock_enabled', 'total_blocked', 'whitelist']);
  isGlobalEnabled = data.adblock_enabled !== false;
  whitelist = data.whitelist || [];
  const totalBlocked = data.total_blocked || 0;

  globalToggle.checked = isGlobalEnabled;
  totalCountEl.textContent = formatNumber(totalBlocked);

  // 3. Get page blocked count from background worker
  if (currentTab && currentTab.id) {
    chrome.runtime.sendMessage({ type: 'GET_TAB_COUNT', tabId: currentTab.id }, (response) => {
      if (response && typeof response.count === 'number') {
        pageCountEl.textContent = formatNumber(response.count);
      }
    });
  }

  // 4. Update UI state
  function updateUIState() {
    const isWhitelisted = currentDomain && whitelist.includes(currentDomain);

    if (!isGlobalEnabled) {
      statusHero.classList.add('disabled');
      statusText.textContent = 'Protección Desactivada';
      statusDesc.textContent = 'El bloqueador está apagado';
      siteToggleBtn.style.opacity = '0.5';
      siteToggleBtn.disabled = true;
    } else if (isWhitelisted) {
      statusHero.classList.add('disabled');
      statusText.textContent = 'Sitio en Lista Blanca';
      statusDesc.textContent = 'Los anuncios se muestran en este sitio';
      siteToggleBtn.disabled = false;
      siteToggleBtn.style.opacity = '1';
      siteToggleBtn.classList.add('whitelisted');
      siteBtnText.textContent = 'Reanudar en este sitio';
    } else {
      statusHero.classList.remove('disabled');
      statusText.textContent = 'Protección Activa';
      statusDesc.textContent = 'Navegación limpia y sin rastreadores';
      siteToggleBtn.disabled = !currentDomain;
      siteToggleBtn.style.opacity = '1';
      siteToggleBtn.classList.remove('whitelisted');
      siteBtnText.textContent = 'Pausar en este sitio';
    }
  }

  updateUIState();

  // 5. Handle Global Toggle
  globalToggle.addEventListener('change', async () => {
    isGlobalEnabled = globalToggle.checked;
    await chrome.storage.local.set({ adblock_enabled: isGlobalEnabled });
    chrome.runtime.sendMessage({ type: 'SETTINGS_CHANGED' });
    updateUIState();

    // Reload tab to apply
    if (currentTab && currentTab.id) {
      chrome.tabs.reload(currentTab.id);
    }
  });

  // 6. Handle Site Whitelist Toggle
  siteToggleBtn.addEventListener('click', async () => {
    if (!currentDomain || !isGlobalEnabled) return;

    const index = whitelist.indexOf(currentDomain);
    if (index > -1) {
      whitelist.splice(index, 1);
    } else {
      whitelist.push(currentDomain);
    }

    await chrome.storage.local.set({ whitelist });
    chrome.runtime.sendMessage({ type: 'SETTINGS_CHANGED' });
    updateUIState();

    // Reload tab to apply changes
    if (currentTab && currentTab.id) {
      chrome.tabs.reload(currentTab.id);
    }
  });

  function formatNumber(num) {
    if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
    if (num >= 1000) return (num / 1000).toFixed(1) + 'k';
    return String(num);
  }
});
