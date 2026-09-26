// StorageAdapter.mjs - Adaptador de persistencia segura en LocalStorage con fallback
const CLAVE_STORAGE = 'moldea_kiosco_faltantes_v1';

export class StorageAdapter {
  constructor({ clave = CLAVE_STORAGE } = {}) {
    this.clave = clave;
    this.memoria = null;
  }

  /**
   * Determina si localStorage está disponible en el entorno
   * @returns {boolean}
   */
  estaDisponible() {
    try {
      if (typeof window === 'undefined' || !window.localStorage) return false;
      const testKey = '__storage_test__';
      window.localStorage.setItem(testKey, '1');
      window.localStorage.removeItem(testKey);
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Carga los datos almacenados
   * @returns {Object[]|null}
   */
  cargar() {
    if (this.estaDisponible()) {
      try {
        const raw = window.localStorage.getItem(this.clave);
        if (!raw) return null;
        return JSON.parse(raw);
      } catch (err) {
        console.warn('[StorageAdapter] Error al leer LocalStorage, usando respaldo:', err);
      }
    }
    return this.memoria;
  }

  /**
   * Guarda los datos en almacenamiento
   * @param {Object[]} items
   * @returns {boolean}
   */
  guardar(items) {
    this.memoria = items;
    if (this.estaDisponible()) {
      try {
        window.localStorage.setItem(this.clave, JSON.stringify(items));
        return true;
      } catch (err) {
        console.error('[StorageAdapter] No se pudo guardar en LocalStorage:', err);
        return false;
      }
    }
    return true;
  }

  /**
   * Exporta los datos como cadena JSON descargable
   * @returns {string}
   */
  exportar() {
    const datos = this.cargar() || [];
    return JSON.stringify({
      version: '1.0.0',
      timestamp: Date.now(),
      items: datos
    }, null, 2);
  }

  /**
   * Restablece el almacenamiento a los datos de fábrica
   */
  restablecer() {
    this.memoria = null;
    if (this.estaDisponible()) {
      try {
        window.localStorage.removeItem(this.clave);
      } catch {}
    }
  }
}
