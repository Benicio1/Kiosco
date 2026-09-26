// Item.mjs - Modelo de Producto para el Organizador de Faltantes del Kiosco
// Regla: No incluye precios por requerimiento explícito del usuario.

export class Item {
  /**
   * Crea una instancia de Item.
   * @param {Object} params
   * @param {string} [params.id]
   * @param {string} params.nombre
   * @param {number} [params.cantidad=1]
   * @param {string} [params.unidad='unid']
   * @param {boolean} [params.falta=true]
   * @param {string} [params.categoria='General']
   * @param {string} [params.notas='']
   * @param {number} [params.createdAt]
   * @param {number} [params.updatedAt]
   */
  constructor({
    id,
    nombre,
    cantidad = 1,
    unidad = 'unid',
    falta = true,
    categoria = 'General',
    notas = '',
    createdAt = Date.now(),
    updatedAt = Date.now()
  }) {
    if (!nombre || typeof nombre !== 'string' || !nombre.trim()) {
      throw new Error('El nombre del producto no puede estar vacío');
    }

    this.id = id || `item-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
    this.nombre = nombre.trim();
    this.cantidad = Math.max(0, Number(cantidad) || 0);
    this.unidad = (unidad || 'unid').trim();
    this.falta = Boolean(falta);
    this.categoria = (categoria || 'General').trim();
    this.notas = (notas || '').trim();
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Alterna el estado entre falta / no falta (tachar / destachar)
   * @returns {boolean} Nuevo estado de falta
   */
  toggleFalta() {
    this.falta = !this.falta;
    this.updatedAt = Date.now();
    return this.falta;
  }

  /**
   * Marca explícitamente si falta o no
   * @param {boolean} valor
   */
  setFalta(valor) {
    this.falta = Boolean(valor);
    this.updatedAt = Date.now();
  }

  /**
   * Incrementa la cantidad a reponer
   * @param {number} [delta=1]
   */
  incrementarCantidad(delta = 1) {
    this.cantidad = Math.max(0, this.cantidad + delta);
    this.updatedAt = Date.now();
    return this.cantidad;
  }

  /**
   * Decrementa la cantidad a reponer (mínimo 0)
   * @param {number} [delta=1]
   */
  decrementarCantidad(delta = 1) {
    this.cantidad = Math.max(0, this.cantidad - delta);
    this.updatedAt = Date.now();
    return this.cantidad;
  }

  /**
   * Actualiza los datos del producto
   * @param {Object} cambios
   */
  actualizar({ nombre, cantidad, unidad, falta, categoria, notas }) {
    if (nombre !== undefined) {
      if (!nombre.trim()) throw new Error('El nombre no puede estar vacío');
      this.nombre = nombre.trim();
    }
    if (cantidad !== undefined) {
      this.cantidad = Math.max(0, Number(cantidad) || 0);
    }
    if (unidad !== undefined) this.unidad = unidad.trim();
    if (falta !== undefined) this.falta = Boolean(falta);
    if (categoria !== undefined) this.categoria = categoria.trim();
    if (notas !== undefined) this.notas = notas.trim();
    this.updatedAt = Date.now();
  }

  /**
   * Serializa a JSON plano
   */
  toJSON() {
    return {
      id: this.id,
      nombre: this.nombre,
      cantidad: this.cantidad,
      unidad: this.unidad,
      falta: this.falta,
      categoria: this.categoria,
      notas: this.notas,
      createdAt: this.createdAt,
      updatedAt: this.updatedAt
    };
  }

  /**
   * Crea una instancia desde un objeto JSON
   * @param {Object} data
   */
  static fromJSON(data) {
    return new Item(data);
  }
}
