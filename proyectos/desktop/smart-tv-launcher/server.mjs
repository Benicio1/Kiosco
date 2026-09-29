import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { fileURLToPath } from 'node:url';
import { exec } from 'node:child_process';
import {
  initInputBridge, closeInputBridge, sendBridgeCommand,
  adjustWindowsVolume, sendWindowsKey, typeWindowsText, getSystemStats
} from './bridge.mjs';

export { closeInputBridge, getSystemStats };

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const tvClients = new Set();

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8', '.css': 'text/css; charset=UTF-8',
  '.js': 'application/javascript; charset=UTF-8', '.json': 'application/json; charset=UTF-8',
  '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.ico': 'image/x-icon'
};

export function getLocalIp() {
  const nets = os.networkInterfaces();
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) return net.address;
    }
  }
  return 'localhost';
}

export async function cdpCommand(fn) {
  try {
    const res = await fetch('http://127.0.0.1:9222/json', { signal: AbortSignal.timeout(600) });
    const list = await res.json();
    const page = list.find(p => p.type === 'page');
    if (!page?.webSocketDebuggerUrl) return false;
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    await new Promise((resolve, reject) => {
      ws.onopen = resolve;
      ws.onerror = reject;
      setTimeout(reject, 600);
    });
    await fn(ws, page);
    setTimeout(() => { try { ws.close(); } catch {} }, 300);
    return true;
  } catch {
    return false;
  }
}

// User-Agent PlayStation 4 Leanback: Desbloquea 1080p y 60fps en YouTube TV sin el límite 720p de dispositivos móviles
const TV_USER_AGENT = 'Mozilla/5.0 (PS4; Leanback Shell) Gecko/20100101 Firefox/65.0 LeanbackShell/01.00.01.75 Sony PS4/ (PS4, , no, CH)';

let isExternalAppActive = false;

export async function openYouTubeTvMode() {
  isExternalAppActive = true;
  return await cdpCommand((ws) => {
    ws.send(JSON.stringify({ id: 10, method: 'Network.setUserAgentOverride', params: { userAgent: TV_USER_AGENT } }));
    ws.send(JSON.stringify({ id: 11, method: 'Page.navigate', params: { url: 'https://www.youtube.com/tv' } }));
  });
}

export async function openCrunchyrollMode() {
  isExternalAppActive = true;
  const nav = await cdpCommand((ws) => {
    ws.send(JSON.stringify({ id: 12, method: 'Network.setUserAgentOverride', params: { userAgent: '' } }));
    ws.send(JSON.stringify({ id: 13, method: 'Page.navigate', params: { url: 'https://www.crunchyroll.com/es/' } }));
  });
  if (!nav && process.platform === 'win32') {
    exec('start "" "https://www.crunchyroll.com/es/"', () => {});
  }
  return true;
}

export async function navigateHome() {
  isExternalAppActive = false;
  const navigated = await cdpCommand((ws, page) => {
    if (!page.url.includes(`localhost:${PORT}`) && !page.url.includes(`127.0.0.1:${PORT}`)) {
      ws.send(JSON.stringify({ id: 9, method: 'Network.setUserAgentOverride', params: { userAgent: '' } }));
      ws.send(JSON.stringify({ id: 1, method: 'Page.navigate', params: { url: `http://localhost:${PORT}/` } }));
    }
  });
  if (!navigated && (isExternalAppActive || tvClients.size === 0)) sendBridgeCommand('home');
}

export async function handleBackAction() {
  const handled = await cdpCommand((ws, page) => {
    if (page.url.includes('youtube.com/tv')) {
      sendBridgeCommand('key esc');
      return;
    }
    if (!page.url.includes(`localhost:${PORT}`) && !page.url.includes(`127.0.0.1:${PORT}`)) {
      ws.send(JSON.stringify({ id: 1, method: 'Runtime.evaluate', params: { expression: 'window.history.back()' } }));
      setTimeout(async () => {
        try {
          const res = await fetch('http://127.0.0.1:9222/json', { signal: AbortSignal.timeout(500) });
          const list = await res.json();
          const p = list.find(x => x.type === 'page');
          if (p && !p.url.includes(`localhost:${PORT}`) && !p.url.includes(`127.0.0.1:${PORT}`)) {
            ws.send(JSON.stringify({ id: 2, method: 'Page.navigate', params: { url: `http://localhost:${PORT}/` } }));
          }
        } catch {}
      }, 500);
    }
  });
  if (!handled && (isExternalAppActive || tvClients.size === 0)) sendBridgeCommand('key back');
}

export function exitApplication() {
  closeInputBridge();
  if (process.platform === 'win32') {
    exec('powershell -NoProfile -Command "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like \'*smart_tv_profile*\' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"', () => {
      setTimeout(() => process.exit(0), 300);
    });
  } else {
    setTimeout(() => process.exit(0), 300);
  }
}

export async function getAppStatus() {
  try {
    const res = await fetch('http://127.0.0.1:9222/json', { signal: AbortSignal.timeout(350) });
    const list = await res.json();
    const page = list.find(p => p.type === 'page');
    if (!page) return { mode: 'windows', title: 'Computadora / Windows' };
    if (page.url.includes('youtube.com/tv')) return { mode: 'youtube_tv', title: 'YouTube en TV' };
    if (page.url.includes('youtube.com')) return { mode: 'youtube_web', title: 'YouTube Web' };
    if (page.url.includes('crunchyroll.com')) return { mode: 'crunchyroll', title: 'Crunchyroll Web' };
    if (page.url.includes(`localhost:${PORT}`) || page.url.includes(`127.0.0.1:${PORT}`)) return { mode: 'tv_app', title: 'Smart TV Launcher' };
    return { mode: 'other_web', title: page.title || 'Navegador Web' };
  } catch {
    return { mode: 'windows', title: 'Computadora / Windows' };
  }
}

export async function searchYouTube(query) {
  try {
    const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
    const res = await fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36' } });
    const html = await res.text();
    const match = html.match(/var ytInitialData = ({.*?});<\/script>/);
    if (!match) return [];
    const contents = JSON.parse(match[1]).contents?.twoColumnSearchResultsRenderer?.primaryContents?.sectionListRenderer?.contents?.[0]?.itemSectionRenderer?.contents || [];
    return contents.filter(i => i.videoRenderer).map(i => {
      const v = i.videoRenderer;
      return { id: v.videoId, title: v.title?.runs?.[0]?.text || '', channel: v.ownerText?.runs?.[0]?.text || '', duration: v.lengthText?.simpleText || 'EN VIVO', thumbnail: `https://i.ytimg.com/vi/${v.videoId}/hqdefault.jpg` };
    }).slice(0, 24);
  } catch { return []; }
}

function serveStaticFile(reqPath, res) {
  let safePath = reqPath === '/' ? '/index.html' : (reqPath === '/remote' ? '/remote.html' : reqPath);
  const fullPath = path.join(PUBLIC_DIR, safePath);
  if (!fullPath.startsWith(PUBLIC_DIR)) { res.writeHead(403); res.end('Acceso denegado'); return; }
  fs.readFile(fullPath, (err, data) => {
    if (err) { res.writeHead(404); res.end('No encontrado'); return; }
    const ext = path.extname(fullPath).toLowerCase();
    res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
    res.end(data);
  });
}

export async function handleSearchAction(query, target = 'auto') {
  const status = await getAppStatus();
  const dest = target !== 'auto' ? target : (status.mode === 'crunchyroll' ? 'crunchyroll' : (status.mode.startsWith('youtube') ? 'youtube' : 'auto'));

  if (dest === 'crunchyroll' || status.mode === 'crunchyroll') {
    const url = `https://www.crunchyroll.com/es/search?q=${encodeURIComponent(query)}`;
    isExternalAppActive = true;
    const nav = await cdpCommand((ws) => {
      ws.send(JSON.stringify({ id: 14, method: 'Network.setUserAgentOverride', params: { userAgent: '' } }));
      ws.send(JSON.stringify({ id: 15, method: 'Page.navigate', params: { url } }));
    });
    if (!nav && process.platform === 'win32') exec(`start "" "${url}"`, () => {});
    return;
  }
  if (dest === 'youtube' || status.mode === 'youtube_web') {
    const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
    const nav = await cdpCommand((ws) => { ws.send(JSON.stringify({ id: 1, method: 'Page.navigate', params: { url } })); });
    if (!nav && process.platform === 'win32') exec(`start "" "${url}"`, () => {});
    return;
  }
  if (status.mode === 'youtube_tv') {
    const url = `https://www.youtube.com/tv#/search?resume&q=${encodeURIComponent(query)}`;
    await cdpCommand((ws) => { ws.send(JSON.stringify({ id: 1, method: 'Page.navigate', params: { url } })); });
  }
}

export function createTvServer() {
  initInputBridge();
  return http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') { res.writeHead(204); res.end(); return; }

    const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = parsedUrl.pathname;

    if (pathname === '/api/remote/events') {
      res.writeHead(200, { 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache', 'Connection': 'keep-alive' });
      res.write(': connected\n\n');
      tvClients.add(res);
      isExternalAppActive = false;
      req.on('close', () => {
        tvClients.delete(res);
        if (tvClients.size === 0) isExternalAppActive = true;
      });
      return;
    }

    if (pathname === '/api/remote/action' && req.method === 'POST') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', async () => {
        try {
          const actionData = JSON.parse(body || '{}');

          if (actionData.type === 'volume' || actionData.type === 'volume_hardware') adjustWindowsVolume(actionData.action);
          if (actionData.type === 'type_text') typeWindowsText(actionData.text);
          if (actionData.type === 'playback') sendWindowsKey('space');
          if (actionData.type === 'mouse_move') sendBridgeCommand(`mouse move ${Math.round(actionData.dx)} ${Math.round(actionData.dy)}`);
          if (actionData.type === 'mouse_click') sendBridgeCommand('mouse click');
          if (actionData.type === 'mouse_scroll') sendBridgeCommand(`mouse scroll ${Math.round(actionData.dy)}`);
          if (actionData.type === 'open_url' && actionData.url && process.platform === 'win32') {
            exec(`start "" "${actionData.url}"`, { timeout: 2000 }, () => {});
          }
          if (actionData.type === 'open_app') {
            if (actionData.appId === 'crunchyroll') {
              if (isExternalAppActive || tvClients.size === 0) await openCrunchyrollMode();
            } else if (actionData.appId === 'youtube') {
              await openYouTubeTvMode();
            } else if (actionData.appId === 'home') {
              await navigateHome();
            }
          }
          if (actionData.type === 'search' && actionData.query) {
            await handleSearchAction(actionData.query, actionData.target);
          }
          if (actionData.type === 'dpad') {
            if (actionData.key === 'home') await navigateHome();
            else if (actionData.key === 'back') await handleBackAction();
            else if (isExternalAppActive || tvClients.size === 0) {
              sendWindowsKey(actionData.key);
            }
          }
          if (actionData.type === 'exit_app' || actionData.type === 'exit_tv') exitApplication();

          if (!actionData.type?.startsWith('mouse_')) {
            const payload = `data: ${JSON.stringify(actionData)}\n\n`;
            for (const client of tvClients) client.write(payload);
          }

          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ ok: true }));
        } catch {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Payload JSON inválido' }));
        }
      });
      return;
    }

    if (pathname === '/api/youtube/open-tv' && req.method === 'POST') {
      openYouTubeTvMode().then(ok => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok }));
      }).catch(() => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: false }));
      });
      return;
    }

    if (pathname === '/api/status') {
      getAppStatus().then(status => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(status));
      }).catch(() => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ mode: 'windows', title: 'Computadora / Windows' }));
      });
      return;
    }

    if (pathname === '/api/info') {
      const localIp = getLocalIp();
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        ip: localIp,
        port: PORT,
        remoteUrl: `http://${localIp}:${PORT}/remote`,
        tvUrl: `http://localhost:${PORT}`
      }));
      return;
    }

    if (pathname === '/api/system/stats') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(getSystemStats()));
      return;
    }

    if (pathname === '/api/system/exit' && req.method === 'POST') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ ok: true, message: 'Cerrando Smart TV...' }));
      exitApplication();
      return;
    }

    if (pathname === '/api/youtube/search') {
      const q = parsedUrl.searchParams.get('q') || 'musica argentina';
      searchYouTube(q).then(results => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(results));
      }).catch(() => {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
      });
      return;
    }

    serveStaticFile(pathname, res);
  });
}

const isDirectRun = process.argv[1] && process.argv[1].endsWith('server.mjs');
if (isDirectRun) {
  const server = createTvServer();
  server.listen(PORT, '0.0.0.0', () => {
    const ip = getLocalIp();
    console.log('====================================================');
    console.log('  📺 SMART TV LAUNCHER — MÖLDEA LIGHTWEIGHT TV OS   ');
    console.log('====================================================');
    console.log(`  🖥️  Pantalla TV:     http://localhost:${PORT}`);
    console.log(`  📱 Control Celular: http://${ip}:${PORT}/remote`);
    console.log('====================================================');
    console.log('  Presiona Ctrl + C para detener el servidor.\n');
  });
}
