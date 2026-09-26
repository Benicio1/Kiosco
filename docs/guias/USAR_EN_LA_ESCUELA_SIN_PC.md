# Cómo Usar la App en la Escuela sin Depender de la Computadora

> **PROPÓSITO:**  
> Esta guía te explica cómo usar el **Organizador de Faltantes del Kiosco** en tu teléfono móvil desde la escuela, en el recreo o en cualquier lugar, **con la computadora de tu casa apagada y sin depender de la misma red Wi-Fi**.

---

## 🚀 OPCIÓN 1: La más rápida (Archivo Portable Directo al Celular)
Ya generamos un archivo único llamado **`kiosco_app_celular.html`** dentro de la carpeta `dist/`. Este archivo tiene todo el sistema integrado adentro (no necesita servidores, ni descargas adicionales, ni internet).

### Paso a paso:
1. En tu computadora, abrí la carpeta [`dist/`](../../dist/).
2. Enviate el archivo **`kiosco_app_celular.html`** a tu celular:
   * Te lo podés mandar por **WhatsApp Web** a tu propio chat (mensaje a vos mismo).
   * O por email / Telegram / Google Drive.
3. En tu celular:
   * Descargá el archivo y tocalo para abrirlo con **Google Chrome** (en Android) o con tu navegador favorito.
   * ¡Listo! Ya tenés el organizador funcionando en la mano.
4. **Para tenerlo como App en la pantalla de inicio:**
   * Tocá los 3 puntitos de Chrome arriba a la derecha y elegí **"Agregar a la pantalla principal"**.
   * Te queda el icono de app en tu celular y funciona siempre, incluso sin señal ni internet en la escuela.

---

## 🌐 OPCIÓN 2: Tener tu propia Web en la Nube (Gratis y con Link Permanente)
Si preferís entrar con un enlace web tipo `https://tu-kiosco.netlify.app` desde cualquier celular sin descargar archivos:

### Método 1: Netlify Drop (En 10 segundos y sin instalar nada)
1. Abrí en tu navegador: [https://app.netlify.com/drop](https://app.netlify.com/drop) (es 100% gratuito).
2. Arrastrá la carpeta completa **`dist/`** de este proyecto y soltala en la pantalla.
3. En 5 segundos te da un enlace permanente HTTPS (ej: `https://mi-kiosco.netlify.app`).
4. Abrís ese enlace en tu celular desde la escuela ¡y listo! Podés instalarlo en tu pantalla de inicio.

### Método 2: GitHub Pages
1. Subí tu repositorio a GitHub.
2. En GitHub andá a **Settings ➔ Pages**.
3. En *Source*, seleccioná la rama `master` o `main` y la carpeta `/` o `/dist`.
4. Te generará un link permanente gratuito: `https://tu-usuario.github.io/tu-repo/`.

---

## 💾 ¿Dónde se guardan las cosas que anoto o tacho?
Tanto en la Opción 1 como en la Opción 2, **todos los datos se guardan directamente en la memoria de tu propio teléfono móvil** (`localStorage`).  
No se borra nada al cerrar el navegador ni al apagar el teléfono.
