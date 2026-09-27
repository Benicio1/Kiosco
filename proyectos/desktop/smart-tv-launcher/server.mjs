import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { fileURLToPath } from 'node:url';
import { exec } from 'node:child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

// Lista de clientes SSE activos (Pantalla de TV)
const tvClients = new Set();

// Mapa de tipos MIME
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

// Obtener la IP local de la red Wi-Fi / Ethernet
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

// Control de volumen nativo de Windows (usando teclas multimedia de bajo consumo)
export function adjustWindowsVolume(action) {
  let charCode = null;
  if (action === 'up') charCode = 175;       // VK_VOLUME_UP
  else if (action === 'down') charCode = 174; // VK_VOLUME_DOWN
  else if (action === 'mute') charCode = 173; // VK_VOLUME_MUTE

  if (charCode && process.platform === 'win32') {
    const cmd = `powershell -NoProfile -Command "(New-Object -ComObject Wscript.Shell).SendKeys([char]${charCode})"`;
    exec(cmd, { timeout: 1000 }, () => {});
  }
}

// Estadísticas de memoria RAM del sistema
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

// Servir archivos estáticos
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

// Servidor HTTP Principal
export function createTvServer() {
  return http.createServer((req, res) => {
    // Configurar cabeceras CORS para red local
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

    // 1. SSE - Conexión de escucha en vivo de la TV
    if (pathname === '/api/remote/events') {
      res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive'
      });
      res.write(': connected\n\n');
      tvClients.add(res);

      req.on('close', () => {
        tvClients.delete(res);
      });
      return;
    }

    // 2. Acción enviada desde el Control Remoto (Celular)
    if (pathname === '/api/remote/action' && req.method === 'POST') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          const actionData = JSON.parse(body || '{}');
          
          // Si es control de volumen nativo de Windows
          if (actionData.type === 'volume_hardware') {
            adjustWindowsVolume(actionData.action);
          }

          // Transmitir inmediatamente a todas las pantallas de TV conectadas
          const payload = `data: ${JSON.stringify(actionData)}\n\n`;
          for (const client of tvClients) {
            client.write(payload);
          }

          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ ok: true, receivers: tvClients.size }));
        } catch {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Payload JSON inválido' }));
        }
      });
      return;
    }

    // 3. Info de IP y Configuración para el QR
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

    // 4. Estadísticas del sistema (RAM y Hardware)
    if (pathname === '/api/system/stats') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(getSystemStats()));
      return;
    }

    // 5. Archivos estáticos de interfaz TV y Control Remoto
    serveStaticFile(pathname, res);
  });
}

// Iniciar servidor si se ejecuta directamente
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
