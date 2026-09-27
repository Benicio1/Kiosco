import test from 'node:test';
import assert from 'node:assert/strict';
import { createTvServer, getLocalIp, getSystemStats } from '../server.mjs';

test('Smart TV Server - Diagnóstico y Endpoints', async (t) => {
  const server = createTvServer();
  const testPort = 3999;

  await new Promise((resolve) => server.listen(testPort, '127.0.0.1', resolve));

  t.after(() => {
    return new Promise((resolve) => server.close(resolve));
  });

  await t.test('getLocalIp() retorna una direccion IP valida', () => {
    const ip = getLocalIp();
    assert.ok(typeof ip === 'string');
    assert.ok(ip.length > 0);
  });

  await t.test('getSystemStats() provee métricas de RAM en tiempo real', () => {
    const stats = getSystemStats();
    assert.ok(stats.totalMB > 0, 'totalMB debe ser mayor a cero');
    assert.ok(stats.freeMB >= 0, 'freeMB debe ser no negativo');
    assert.ok(stats.usedPercent >= 0 && stats.usedPercent <= 100, 'usedPercent debe estar entre 0 y 100');
  });

  await t.test('GET / debe servir index.html con status 200', async () => {
    const res = await fetch(`http://127.0.0.1:${testPort}/`);
    assert.equal(res.status, 200);
    const text = await res.text();
    assert.ok(text.includes('Smart TV Launcher'));
  });

  await t.test('GET /remote debe servir remote.html con status 200', async () => {
    const res = await fetch(`http://127.0.0.1:${testPort}/remote`);
    assert.equal(res.status, 200);
    const text = await res.text();
    assert.ok(text.includes('Control Remoto TV'));
  });

  await t.test('GET /api/info retorna URLs y configuración de red', async () => {
    const res = await fetch(`http://127.0.0.1:${testPort}/api/info`);
    assert.equal(res.status, 200);
    const data = await res.json();
    assert.ok(data.remoteUrl.includes('/remote'));
    assert.ok(data.tvUrl);
  });

  await t.test('POST /api/remote/action procesa comandos del celular', async () => {
    const res = await fetch(`http://127.0.0.1:${testPort}/api/remote/action`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: 'dpad', key: 'ok' })
    });
    assert.equal(res.status, 200);
    const data = await res.json();
    assert.equal(data.ok, true);
  });
});
