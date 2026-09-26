# Servidor Local de Vista Previa

> `SERVER.bat` levanta un servidor HTTP en tu máquina para ver el avance del proyecto en el navegador — sin instalar nada, sin depender de ningún framework.

---

## Uso
1. Doble clic en **`SERVER.bat`** (o `node herramientas/servidor_local.mjs` desde terminal).
2. Se abre automáticamente `http://127.0.0.1:8080` en tu navegador.
3. Elige qué mostrar automáticamente: si existe `dist/`, `build/`, `public/` u `out/` (carpetas típicas de build de producción), sirve esa; si no, sirve la raíz del proyecto tal cual.

## Cuándo usarlo
Cada vez que quieras mostrar el estado actual de un prototipo, un sitio estático, o un build — sin levantar un servidor de desarrollo pesado. Es el mismo espíritu de "un clic para ver el avance" que ya usás con el Cotizador y con el propio Dashboard de MÖLDEA, ahora disponible en **cualquier proyecto**, sea cual sea su stack.

## Si el puerto 8080 está ocupado
El servidor prueba automáticamente el siguiente puerto libre (8081, 8082...) — mirá la consola para ver en qué puerto quedó.
