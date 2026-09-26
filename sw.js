// sw.js - Service Worker PWA para Kiosco Faltantes
const CACHE_NAME = 'kiosco-pwa-v2';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './manifest.json',
  './src/gui/styles.css',
  './src/gui/components.css',
  './src/gui/modals.css',
  './src/gui/app.js',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/apple-touch-icon.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE).catch((err) => {
        console.warn('Error precaching assets:', err);
      });
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k))
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  event.respondWith(
    caches.match(event.request).then((cached) => {
      return cached || fetch(event.request).then((networkRes) => {
        return caches.open(CACHE_NAME).then((cache) => {
          // Cache successful responses for offline use
          if (networkRes.status === 200) {
            cache.put(event.request, networkRes.clone());
          }
          return networkRes;
        });
      }).catch(() => cached);
    })
  );
});
