import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { fileURLToPath } from 'node:url';
import { exec, spawn } from 'node:child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
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
