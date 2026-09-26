// ShareAdapter.mjs - Adaptador para compartir lista de faltantes por WhatsApp o portapapeles

export class ShareAdapter {
  /**
   * Abre WhatsApp con el texto prediseñado de faltantes
   * @param {string} texto
   */
  static compartirWhatsApp(texto) {
    if (!texto) return false;
    const url = `https://wa.me/?text=${encodeURIComponent(texto)}`;
    if (typeof window !== 'undefined') {
      window.open(url, '_blank');
      return true;
    }
    return false;
  }

  /**
   * Intenta usar la Web Share API nativa de Android/iOS o copia al portapapeles
   * @param {Object} params
   * @param {string} params.titulo
   * @param {string} params.texto
   * @returns {Promise<{ compartido: boolean, metodo: 'share'|'clipboard'|'ninguno' }>}
   */
  static async compartirNativoOClipboard({ titulo = 'Faltantes de Kiosco', texto }) {
    if (!texto || typeof window === 'undefined') {
      return { compartido: false, metodo: 'ninguno' };
    }

    // 1. Probar Web Share API nativa (Móvil)
    if (navigator.share && typeof navigator.canShare === 'function') {
      try {
        if (navigator.canShare({ text: texto })) {
          await navigator.share({ title: titulo, text: texto });
          return { compartido: true, metodo: 'share' };
        }
      } catch (err) {
        // Si el usuario canceló el share sheet nativo, no forzamos error
        if (err.name === 'AbortError') {
          return { compartido: false, metodo: 'share' };
        }
        console.warn('[ShareAdapter] Error en Web Share API, reintentando con portapapeles:', err);
      }
    }

    // 2. Fallback a Clipboard API
    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(texto);
        return { compartido: true, metodo: 'clipboard' };
      }
    } catch (err) {
      console.warn('[ShareAdapter] Error en Clipboard API, usando textarea temporal:', err);
    }

    // 3. Fallback tradicional con textarea
    try {
      const textarea = document.createElement('textarea');
      textarea.value = texto;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      return { compartido: true, metodo: 'clipboard' };
    } catch {
      return { compartido: false, metodo: 'ninguno' };
    }
  }
}
