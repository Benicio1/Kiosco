// Aegis AdBlock - Background Service Worker (Manifest V3)
// 100% Stealth Mode: Invisible to anti-adblock detectors, eliminates rogue popup tabs natively

const tabCounts = new Map();

const CASINO_AND_POPUNDER_PATTERNS = [
  // Sololatino Ad Networks
  'lianoidmachar.com',
  'constbezzant.com',
  'saltcatthebaic.com',
  'kishkeenocyte.com',
  'primasphobist.com',
  'densenbl.com',
  '149138',
  '149139',
  '149160',
  '149161',

  // Popunder & Redirect Networks
  'highperformancegate.com',
  'effectivecpmgate.com',
  'profitablegatecpm.com',
  'alwingulla.com',
  'deloton.com',
  'coalsoupservant.com',
  'grotesquemirror.com',
  'monetag.com',
  'popads.net',
  'popcash.net',
  'adsterra.com',
  'clickadu.com',
  'clckd.net',
  'exoclick.com',
  'exosrv.com',
  'realsrv.com',
  'tsyndicate.com',
  'hilltopads.com',
  'galaksion.com',
  'evadav.com',
  'rollerads.com',
  'richads.com',
  'ad-maven.com',
  'admaven.com',
  'onclkds.com',
  'onclickalgo.com',
  'poptm.com',
  'propu.sh',
  'deliveryengine',

  // Casino / Gambling brands & keywords
  '1xbet',
  'bet365',
  'betfair',
  'pin-up',
  'melbet',
  'mostbet',
  'betwinner',
  'spinamba',
  'vulkanvegas',
  'vulkan.',
  'icecasino',
  'verde-casino',
  '1win',
  'stake.com',
  'bc.game',
  'roobet',
  'aviator',
  'apuestas',
  'casino'
];

function isPopunderOrCasino(url) {
  if (!url) return false;
  const lower = url.toLowerCase();
  return CASINO_AND_POPUNDER_PATTERNS.some(pat => lower.includes(pat));
}

// Check if a spawned popup should be terminated
async function shouldTerminateTab(tab) {
  if (!tab || !tab.id) return false;
  const targetUrl = tab.pendingUrl || tab.url || '';

  // 1. Direct match with casino or popunder
  if (isPopunderOrCasino(targetUrl)) return true;

  // 2. If opened from an existing tab (popup/popunder)
  if (tab.openerTabId) {
    try {
      const openerTab = await chrome.tabs.get(tab.openerTabId);
      const openerUrl = openerTab.url || openerTab.pendingUrl || '';

      // If opener is streaming site Sololatino
      if (openerUrl.includes('sololatino.net')) {
        // If popup points to something other than sololatino or google
        if (targetUrl && targetUrl !== 'about:blank' && !targetUrl.startsWith('chrome://')) {
          const parsed = new URL(targetUrl);
          if (!parsed.hostname.includes('sololatino.net') && !parsed.hostname.includes('google.com')) {
            console.warn('[Aegis Stealth] Auto-terminating cross-origin popup from Sololatino:', targetUrl);
            return true;
          }
        }
      }
    } catch (e) {}
  }

  return false;
}

// Initialize extension defaults
chrome.runtime.onInstalled.addListener(async () => {
  const data = await chrome.storage.local.get(['adblock_enabled', 'total_blocked', 'whitelist']);
  if (data.adblock_enabled === undefined) {
    await chrome.storage.local.set({ adblock_enabled: true });
  }
  if (data.total_blocked === undefined) {
    await chrome.storage.local.set({ total_blocked: 0 });
  }
  if (!data.whitelist) {
    await chrome.storage.local.set({ whitelist: [] });
  }

  chrome.action.setBadgeBackgroundColor({ color: '#FF5500' });
});

// Auto-close popunders or casino pages opened in new tabs
chrome.tabs.onCreated.addListener(async (tab) => {
  if (await shouldTerminateTab(tab)) {
    console.warn('[Aegis Stealth] Killed ad tab on creation:', tab.id);
    chrome.tabs.remove(tab.id).catch(() => {});
  }
});

// Detect and close popunders/casino when tab navigates or updates
chrome.tabs.onUpdated.addListener(async (tabId, changeInfo, tab) => {
  if (await shouldTerminateTab(tab)) {
    console.warn('[Aegis Stealth] Killed ad tab on navigation update:', tabId);
    chrome.tabs.remove(tabId).catch(() => {});
    return;
  }

  if (changeInfo.status === 'loading') {
    tabCounts.set(tabId, 0);
    updateBadge(tabId, 0);
  }
});

// WebNavigation Interceptor: Block before navigation even starts
if (chrome.webNavigation && chrome.webNavigation.onBeforeNavigate) {
  chrome.webNavigation.onBeforeNavigate.addListener(async (details) => {
    if (details.frameId === 0) {
      if (isPopunderOrCasino(details.url)) {
        console.warn('[Aegis WebNav] Aborting casino navigation:', details.tabId, details.url);
        chrome.tabs.remove(details.tabId).catch(() => {});
        return;
      }

      try {
        const tab = await chrome.tabs.get(details.tabId);
        if (tab && (await shouldTerminateTab(tab))) {
          chrome.tabs.remove(details.tabId).catch(() => {});
        }
      } catch (e) {}
    }
  });
}

chrome.tabs.onRemoved.addListener((tabId) => {
  tabCounts.delete(tabId);
});

// Update extension icon badge
function updateBadge(tabId, count) {
  if (!tabId || tabId < 0) return;
  const text = count > 0 ? (count > 999 ? '999+' : String(count)) : '';
  chrome.action.setBadgeText({ tabId, text }).catch(() => {});
}

// Sync declarativeNetRequest rules with whitelist
async function syncWhitelistRules() {
  const { whitelist = [], adblock_enabled = true } = await chrome.storage.local.get(['whitelist', 'adblock_enabled']);

  if (!adblock_enabled) {
    await chrome.declarativeNetRequest.updateEnabledRulesets({
      disableRulesetIds: ['ruleset_1']
    });
  } else {
    await chrome.declarativeNetRequest.updateEnabledRulesets({
      enableRulesetIds: ['ruleset_1']
    });
  }

  const existingRules = await chrome.declarativeNetRequest.getDynamicRules();
  const ruleIdsToRemove = existingRules.map(r => r.id);

  const newRules = [];
  let dynamicRuleId = 10000;

  for (const domain of whitelist) {
    newRules.push({
      id: dynamicRuleId++,
      priority: 2,
      action: { type: 'allowAllRequests' },
      condition: {
        initiatorDomains: [domain],
        resourceTypes: ['main_frame', 'sub_frame', 'stylesheet', 'script', 'image', 'font', 'object', 'xmlhttprequest', 'ping', 'media', 'websocket', 'other']
      }
    });
  }

  await chrome.declarativeNetRequest.updateDynamicRules({
    removeRuleIds: ruleIdsToRemove,
    addRules: newRules
  });
}

// Listen to messages from Content Scripts and Popup
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'INCREMENT_BLOCKED') {
    const tabId = sender?.tab?.id;
    const add = message.count || 1;

    if (tabId) {
      const current = (tabCounts.get(tabId) || 0) + add;
      tabCounts.set(tabId, current);
      updateBadge(tabId, current);
    }

    chrome.storage.local.get(['total_blocked'], (res) => {
      const total = (res.total_blocked || 0) + add;
      chrome.storage.local.set({ total_blocked: total });
    });

    sendResponse({ success: true });
    return true;
  }

  if (message.type === 'GET_TAB_COUNT') {
    const count = tabCounts.get(message.tabId) || 0;
    sendResponse({ count });
    return true;
  }

  if (message.type === 'SETTINGS_CHANGED') {
    syncWhitelistRules().then(() => {
      sendResponse({ success: true });
    });
    return true;
  }
});

// Track network rule matches in debug mode if supported
if (chrome.declarativeNetRequest.onRuleMatchedDebug) {
  chrome.declarativeNetRequest.onRuleMatchedDebug.addListener((info) => {
    const tabId = info?.request?.tabId;
    if (tabId && tabId > 0) {
      const current = (tabCounts.get(tabId) || 0) + 1;
      tabCounts.set(tabId, current);
      updateBadge(tabId, current);

      chrome.storage.local.get(['total_blocked'], (res) => {
        const total = (res.total_blocked || 0) + 1;
        chrome.storage.local.set({ total_blocked: total });
      });
    }
  });
}
