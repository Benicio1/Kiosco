import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { fileURLToPath } from 'node:url';
import { exec, spawn } from 'node:child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const tvClients = new Set();
const bridgeExe = path.join(__dirname, 'tools', 'InputBridge.exe');
let bridgeProcess = null;

// Inicializar el Puente de Entrada Nativo de Windows (0ms latencia)
export function initInputBridge() {
  if (process.platform === 'win32' && fs.existsSync(bridgeExe) && !bridgeProcess) {
    try {
      bridgeProcess = spawn(bridgeExe, [], { stdio: ['pipe', 'ignore', 'ignore'] });
      bridgeProcess.unref();
      bridgeProcess.on('error', () => { bridgeProcess = null; });
      bridgeProcess.on('exit', () => { bridgeProcess = null; });
    } catch {
      bridgeProcess = null;
    }
  }
}

export function closeInputBridge() {
  if (bridgeProcess) {
    try { bridgeProcess.kill(); } catch {}
    bridgeProcess = null;
  }
}

export function sendBridgeCommand(cmd) {
  if (bridgeProcess && bridgeProcess.stdin && bridgeProcess.stdin.writable) {
    try {
      bridgeProcess.stdin.write(cmd + '\n');
      return true;
    } catch {
      return false;
    }
  }
  return false;
}

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.css': 'text/css; charset=UTF-8',
  '.js': 'application/javascript; charset=UTF-8',
  '.json': 'application/json; charset=UTF-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.ico': 'image/x-icon'
};

export function getLocalIp() {
  const nets = os.networkInterfaces();
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        return net.address;
      }
    }
  }
  return 'localhost';
}

export function adjustWindowsVolume(action) {
  if (sendBridgeCommand(`vol ${action}`)) return;
  let charCode = action === 'up' ? 175 : action === 'down' ? 174 : 173;
  if (process.platform === 'win32') {
    exec(`powershell -NoProfile -Command "(New-Object -ComObject Wscript.Shell).SendKeys([char]${charCode})"`, { timeout: 1000 }, () => {});
  }
}

export function sendWindowsKey(key) {
  if (sendBridgeCommand(`key ${key}`)) return;
  if (process.platform !== 'win32') return;
  let code = key === 'space' || key === 'play_pause' || key === 'ok' ? '[char]32' :
             key === 'left' ? '{LEFT}' : key === 'right' ? '{RIGHT}' :
             key === 'up' ? '{UP}' : key === 'down' ? '{DOWN}' :
             key === 'back' ? '%{LEFT}' : '{ENTER}';
  exec(`powershell -NoProfile -Command "(New-Object -ComObject Wscript.Shell).SendKeys('${code}')"`, { timeout: 1000 }, () => {});
}

export function typeWindowsText(text) {
  if (!text) return;
  if (sendBridgeCommand(`type ${text}`)) return;
  if (process.platform !== 'win32') return;
  const safe = text.replace(/([+^%~{}()[\]])/g, '{$1}').replace(/'/g, "''");
  exec(`powershell -NoProfile -Command "(New-Object -ComObject Wscript.Shell).SendKeys('${safe}')"`, { timeout: 2000 }, () => {});
}

export function navigateHome() {
  sendBridgeCommand('home');
}

export function handleBackAction() {
  sendBridgeCommand('key back');
}

export function getSystemStats() {
  const total = os.totalmem();
  const free = os.freemem();
  const used = total - free;
  return {
    totalMB: Math.round(total / (1024 * 1024)),
    freeMB: Math.round(free / (1024 * 1024)),
    usedMB: Math.round(used / (1024 * 1024)),
    usedPercent: Math.round((used / total) * 100),
    platform: process.platform,
    hostname: os.hostname()
  };
}

export async function searchYouTube(query) {
  try {
    const targetUrl = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
    const res = await fetch(targetUrl, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept-Language': 'es-419,es;q=0.9,en;q=0.8'
      }
    });
    const html = await res.text();
    const match = html.match(/var ytInitialData = ({.*?});<\/script>/);
    if (!match) return [];
    const data = JSON.parse(match[1]);
    const contents = data.contents?.twoColumnSearchResultsRenderer?.primaryContents?.sectionListRenderer?.contents?.[0]?.itemSectionRenderer?.contents || [];
    const results = [];
    for (const item of contents) {
      if (item.videoRenderer) {
        const v = item.videoRenderer;
        const id = v.videoId;
        const title = v.title?.runs?.[0]?.text || '';
        const channel = v.ownerText?.runs?.[0]?.text || '';
        const duration = v.lengthText?.simpleText || (v.badges ? 'EN VIVO' : '');
        const thumbnail = `https://i.ytimg.com/vi/${id}/hqdefault.jpg`;
        if (id && title) results.push({ id, title, channel, duration, thumbnail });
      }
    }
    return results.slice(0, 24);
  } catch {
    return [];
  }
}

function serveStaticFile(reqPath, res) {
  let safePath = reqPath === '/' ? '/index.html' : reqPath;
  if (safePath === '/remote') safePath = '/remote.html';

  const fullPath = path.join(PUBLIC_DIR, safePath);
  if (!fullPath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Acceso denegado');
    return;
  }

  fs.readFile(fullPath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('Archivo no encontrado');
      return;
    }
    const ext = path.extname(fullPath).toLowerCase();
    const mime = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': mime });
    res.end(data);
  });
}

export function createTvServer() {
  initInputBridge();
  return http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
      res.writeHead(204);
      res.end();
      return;
    }

    const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = parsedUrl.pathname;

    if (pathname === '/api/remote/events') {
      res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive'
      });
      res.write(': connected\n\n');
      tvClients.add(res);
      req.on('close', () => { tvClients.delete(res); });
      return;
    }

    if (pathname === '/api/remote/action' && req.method === 'POST') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          const actionData = JSON.parse(body || '{}');

          // Comandos de Windows directos para reproductores web (Crunchyroll, etc.)
          if (actionData.type === 'volume' || actionData.type === 'volume_hardware') {
            adjustWindowsVolume(actionData.action);
          }
          if (actionData.type === 'type_text') {
            typeWindowsText(actionData.text);
          }
          if (actionData.type === 'playback') {
            sendWindowsKey('space');
          }
          if (actionData.type === 'mouse_move') {
            sendBridgeCommand(`mouse move ${Math.round(actionData.dx)} ${Math.round(actionData.dy)}`);
          }
          if (actionData.type === 'mouse_click') {
            sendBridgeCommand('mouse click');
          }
          if (actionData.type === 'mouse_scroll') {
            sendBridgeCommand(`mouse scroll ${Math.round(actionData.dy)}`);
          }
          if (actionData.type === 'open_url' && actionData.url) {
            if (process.platform === 'win32') {
              exec(`start "" "${actionData.url}"`, { timeout: 2000 }, () => {});
            }
          }
          if (actionData.type === 'dpad') {
            if (actionData.key === 'home') navigateHome();
            else if (actionData.key === 'back') handleBackAction();
            // No enviar flechas a Windows aquí para evitar que salte de dos en dos en el launcher
          }

          // Transmitir a la pantalla de TV si está en la app local
          const payload = `data: ${JSON.stringify(actionData)}\n\n`;
          for (const client of tvClients) client.write(payload);

          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ ok: true, receivers: tvClients.size }));
        } catch {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Payload JSON inválido' }));
        }
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
