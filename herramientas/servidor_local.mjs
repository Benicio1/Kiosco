#!/usr/bin/env node
// Servidor HTTP estático zero-dependency para previsualizar el avance del proyecto
// en el navegador. No asume ningún framework: sirve `dist/` o `public/` si
// existen (build de producción), y si no, sirve la raíz del proyecto tal cual.
//
// Uso: node herramientas/servidor_local.mjs   (o doble clic en SERVER.bat)

import http from 'node:http';
import fs from 'node:fs/promises';
import { createReadStream, existsSync } from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { exec } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

// Preferir una carpeta de build/salida si existe; si no, la interfaz gui o la raíz.
const CANDIDATOS_RAIZ = ['dist', 'build', 'public', 'out'];
const rootDir = CANDIDATOS_RAIZ.map(c => path.join(PROJECT_ROOT, c)).find(existsSync) || PROJECT_ROOT;
const guiIndex = path.join(PROJECT_ROOT, 'src', 'gui', 'index.html');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8', '.mjs': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8', '.png': 'image/png', '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg', '.gif': 'image/gif', '.svg': 'image/svg+xml', '.ico': 'image/x-icon',
  '.webp': 'image/webp', '.pdf': 'application/pdf', '.txt': 'text/plain; charset=utf-8',
  '.md': 'text/markdown; charset=utf-8', '.woff': 'font/woff', '.woff2': 'font/woff2'
};

function escapeHtml(str) {
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function startServer(port) {
  const server = http.createServer(async (req, res) => {
    try {
      const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
      let filePath = path.join(rootDir, pathname);
      const guiPath = path.join(PROJECT_ROOT, 'src', 'gui', pathname);

      // Si no existe en rootDir pero existe en src/gui, servir desde src/gui
      if (!existsSync(filePath) && existsSync(guiPath)) {
        filePath = guiPath;
      }

      // Confinamiento: nunca servir nada fuera de PROJECT_ROOT.
      const relative = path.relative(PROJECT_ROOT, filePath);
      if (relative.startsWith('..') || path.isAbsolute(relative)) {
        res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
        return res.end('403 Prohibido');
      }

      let stat;
      try {
        stat = await fs.stat(filePath);
      } catch {
        res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
        return res.end(`404 No encontrado: ${pathname}`);
      }

      if (stat.isDirectory()) {
        if (pathname === '/' && existsSync(guiIndex)) {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          return createReadStream(guiIndex).pipe(res);
        }
        const indexPath = path.join(filePath, 'index.html');
        if (existsSync(indexPath)) {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          return createReadStream(indexPath).pipe(res);
        }
        const entries = await fs.readdir(filePath);
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        return res.end(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>Índice: ${escapeHtml(pathname)}</title>
          <style>body{font-family:system-ui,sans-serif;max-width:700px;margin:40px auto;padding:0 20px;background:#0f172a;color:#f1f5f9}
          a{color:#38bdf8;text-decoration:none}li{padding:6px 0}</style></head><body>
          <h2>📁 ${escapeHtml(pathname)}</h2><ul>
          ${pathname !== '/' ? '<li><a href="..">⬅ Volver</a></li>' : ''}
          ${entries.map(e => `<li><a href="${escapeHtml(path.posix.join(pathname, e))}">${escapeHtml(e)}</a></li>`).join('')}
          </ul></body></html>`);
      }

      const ext = path.extname(filePath).toLowerCase();
      res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
      createReadStream(filePath).pipe(res);
    } catch (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end(`500 Error interno: ${err.message}`);
    }
  });

  server.on('error', (e) => {
    if (e.code === 'EADDRINUSE') {
      console.log(`[i] Puerto ${port} ocupado, probando con ${port + 1}...`);
      startServer(port + 1);
    } else {
      console.error('[X] Error en el servidor:', e);
    }
  });

  server.listen(port, '0.0.0.0', () => {
    const urlLocal = `http://127.0.0.1:${port}`;
    let ipMóvil = null;
    const ifaces = os.networkInterfaces();
    for (const dev in ifaces) {
      for (const details of ifaces[dev]) {
        if (details.family === 'IPv4' && !details.internal) {
          ipMóvil = details.address;
          break;
        }
      }
      if (ipMóvil) break;
    }

    console.log(`\n======================================================`);
    console.log(`📦 ORGANIZADOR DE FALTANTES DEL KIOSCO EN EJECUCIÓN`);
    console.log(`======================================================`);
    console.log(`[💻] En esta PC:     ${urlLocal}`);
    if (ipMóvil) {
      console.log(`[📱] En tu CELULAR:  http://${ipMóvil}:${port}`);
      console.log(`     (Conectá el celular al mismo Wi-Fi y abrí ese enlace)`);
    }
    console.log(`------------------------------------------------------`);
    console.log('[i] Presioná Ctrl+C para detener el servidor.\n');
    exec(`start ${urlLocal}`); // Windows. En Mac/Linux reemplazar por 'open'/'xdg-open'.
  });
}

startServer(parseInt(process.env.PORT || '8080', 10));
