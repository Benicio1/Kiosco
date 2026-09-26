// KioscoList.mjs - Gestor de la lista de faltantes y stock del kiosco
import { Item } from './Item.mjs';
import { PRODUCTOS_INICIALES } from './InitialData.mjs';

export class KioscoList {
  /**
   * @param {Object} [params]
   * @param {Item[]} [params.items=[]]
   */
  constructor({ items = [] } = {}) {
    this.items = items.map(item => (item instanceof Item ? item : new Item(item)));
    if (this.items.length === 0) {
      this.cargarIniciales();
    }
  }

  /**
   * Carga los productos iniciales configurados para el kiosco
   */
  cargarIniciales() {
    this.items = PRODUCTOS_INICIALES.map(data => new Item(data));
  }

  /**
   * Agrega un nuevo ítem a la lista
   * @param {Object} params
   * @returns {Item}
   */
  agregarItem(params) {
    const item = new Item(params);
    this.items.unshift(item); // Agregar al principio para visibilidad inmediata
    return item;
  }

  /**
   * Obtiene un ítem por su ID
   * @param {string} id
   * @returns {Item|undefined}
   */
  obtenerItem(id) {
    return this.items.find(i => i.id === id);
  }

  /**
   * Tacha o destacha un producto (falta / no falta)
   * @param {string} id
   * @returns {Item|null}
   */
  toggleTachar(id) {
    const item = this.obtenerItem(id);
    if (!item) return null;
    item.toggleFalta();
    return item;
  }

  /**
   * Modifica la cantidad de un ítem
   * @param {Object} params
   * @param {string} params.id
   * @param {number} params.delta
   * @returns {Item|null}
   */
  cambiarCantidad({ id, delta }) {
    const item = this.obtenerItem(id);
    if (!item) return null;
    if (delta > 0) {
      item.incrementarCantidad(delta);
    } else {
      item.decrementarCantidad(Math.abs(delta));
    }
    return item;
  }

  /**
   * Actualiza los datos de un ítem
   * @param {Object} params
   * @param {string} params.id
   * @param {Object} params.cambios
   */
  editarItem({ id, cambios }) {
    const item = this.obtenerItem(id);
    if (!item) return null;
    item.actualizar(cambios);
    return item;
  }

  /**
   * Elimina un ítem de la lista
   * @param {string} id
   * @returns {boolean}
   */
  eliminarItem(id) {
    const initialLen = this.items.length;
    this.items = this.items.filter(i => i.id !== id);
    return this.items.length < initialLen;
  }

  /**
   * Marca todos los ítems como comprados / en stock (todos tachados)
   */
  marcarTodosComprados() {
    this.items.forEach(i => i.setFalta(false));
  }

  /**
   * Marca todos los ítems como faltantes
   */
  marcarTodosFaltan() {
    this.items.forEach(i => i.setFalta(true));
  }

  /**
   * Filtra los elementos según el estado y un término de búsqueda
   * @param {Object} params
   * @param {'todos'|'faltan'|'listos'} [params.filtro='todos']
   * @param {string} [params.busqueda='']
   * @param {string} [params.categoria='']
   * @returns {Item[]}
   */
  filtrar({ filtro = 'todos', busqueda = '', categoria = '' } = {}) {
    const normalizar = (s) => (s || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');

    const termino = normalizar(busqueda.trim());
    const cat = normalizar(categoria.trim());

    return this.items.filter(item => {
      // Filtro de estado
      if (filtro === 'faltan' && !item.falta) return false;
      if (filtro === 'listos' && item.falta) return false;

      // Filtro de categoría
      if (cat && cat !== 'todas' && normalizar(item.categoria) !== cat) return false;

      // Filtro de búsqueda textual insensible a tildes
      if (termino) {
        const enNombre = normalizar(item.nombre).includes(termino);
        const enNotas = normalizar(item.notas).includes(termino);
        const enCat = normalizar(item.categoria).includes(termino);
        if (!enNombre && !enNotas && !enCat) return false;
      }

      return true;
    });
  }

  /**
   * Obtiene métricas rápidas del kiosco
   * @returns {{ total: number, faltan: number, listos: number, porcentajeFaltante: number }}
   */
  obtenerMetricas() {
    const total = this.items.length;
    const faltan = this.items.filter(i => i.falta).length;
    const listos = total - faltan;
    const porcentajeFaltante = total > 0 ? Math.round((faltan / total) * 100) : 0;
    return { total, faltan, listos, porcentajeFaltante };
  }

  /**
   * Obtiene la lista de categorías únicas presentes
   * @returns {string[]}
   */
  obtenerCategorias() {
    const set = new Set(this.items.map(i => i.categoria).filter(Boolean));
    return Array.from(set).sort();
  }

  /**
   * Genera un texto limpio para enviar por WhatsApp o guardar en notas
   * @returns {string}
   */
  generarMensajeWhatsApp() {
    const faltantes = this.items.filter(i => i.falta);
    if (faltantes.length === 0) {
      return '✅ ¡No hay faltantes en el kiosco por ahora! Todo en orden.';
    }

    const fecha = new Date().toLocaleDateString('es-AR', {
      weekday: 'long',
      day: 'numeric',
      month: 'short'
    });

    let texto = `🛒 *FALTANTES DEL KIOSCO* (${fecha})\n`;
    texto += `_Total de productos a reponer: ${faltantes.length}_\n\n`;

    // Agrupar por categoría
    const porCat = {};
    faltantes.forEach(item => {
      const cat = item.categoria || 'Varios';
      if (!porCat[cat]) porCat[cat] = [];
      porCat[cat].push(item);
    });

    for (const [categoria, items] of Object.entries(porCat)) {
      texto += `📌 *${categoria.toUpperCase()}*:\n`;
      items.forEach(it => {
        const detalleUnidad = it.unidad ? ` ${it.unidad}` : '';
        const notas = it.notas ? ` _(${it.notas})_` : '';
        texto += ` • [ ] ${it.nombre} — Cant: *${it.cantidad}*${detalleUnidad}${notas}\n`;
      });
      texto += '\n';
    }

    texto += '📋 _Generado con Organizador de Kiosco_';
    return texto.trim();
  }

  /**
   * Serializa todos los ítems a JSON plano
   */
  toJSON() {
    return this.items.map(i => i.toJSON());
  }

  /**
   * Reconstituye la lista desde un array JSON
   * @param {Object[]} arr
   */
  static fromJSON(arr) {
    if (!Array.isArray(arr)) return new KioscoList();
    const items = arr.map(data => Item.fromJSON(data));
    return new KioscoList({ items });
  }
}
