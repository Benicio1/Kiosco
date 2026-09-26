// sw.js - Service Worker offline para el Organizador de Kiosco
const CACHE_NAME = 'kiosco-faltantes-v1';
const ASSETS_TO_CACHE = [
  './index.html',
  './styles.css',
  './components.css',
  './modals.css',
  './app.js',
  './uiRenderer.js',
  './qr.js',
  './manifest.json',
  '../core/Item.mjs',
  '../core/KioscoList.mjs',
  '../core/InitialData.mjs',
  '../adapters/StorageAdapter.mjs',
  '../adapters/ShareAdapter.mjs'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE).catch(() => {});
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      return cachedResponse || fetch(event.request).catch(() => cachedResponse);
    })
  );
});
