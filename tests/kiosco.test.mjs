// tests/kiosco.test.mjs - Suite de Pruebas Automatizadas para el Organizador de Faltantes de Kiosco
import assert from 'node:assert/strict';
import { test, describe } from 'node:test';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

import { Item } from '../src/core/Item.mjs';
import { KioscoList } from '../src/core/KioscoList.mjs';
import { PRODUCTOS_INICIALES } from '../src/core/InitialData.mjs';
import { StorageAdapter } from '../src/adapters/StorageAdapter.mjs';
import { ShareAdapter } from '../src/adapters/ShareAdapter.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const SRC_DIR = path.resolve(__dirname, '../src');

describe('📦 1. Catálogo Inicial y Validación de Requerimientos', () => {
  test('Debe contener exactamente los 9 productos solicitados por el usuario sin extras genéricos', () => {
    assert.strictEqual(PRODUCTOS_INICIALES.length, 9, 'Debe haber exactamente 9 ítems iniciales');

    const nombresEsperados = [
      'Promo de panchos',
      'Sanchuchitos de miga',
      'Alfajores Guaymallén',
      'Alfajores Fulbito',
      'Juguitos Baggio multifruta',
      'Picodulces',
      'Chupetín con chicle',
      'Flynn Paff',
      'Palitos de la selva'
    ];

    const nombresEnCatalogo = PRODUCTOS_INICIALES.map(p => p.nombre);
    for (const esperado of nombresEsperados) {
      assert.ok(
        nombresEnCatalogo.includes(esperado),
        `Falta el producto solicitado: ${esperado}`
      );
    }
  });

  test('Ningún producto debe tener campos de precio', () => {
    for (const p of PRODUCTOS_INICIALES) {
      assert.strictEqual(p.precio, undefined, 'No debe existir propiedad de precio');
      assert.strictEqual(p.costo, undefined, 'No debe existir propiedad de costo');
    }
  });
});

describe('🔧 2. Entidad Item y Control de Estados', () => {
  test('Creación de un Item con validaciones', () => {
    const item = new Item({
      nombre: 'Alfajores Jorgito',
      cantidad: 3,
      unidad: 'cajas',
      categoria: 'Alfajores'
    });

    assert.strictEqual(item.nombre, 'Alfajores Jorgito');
    assert.strictEqual(item.cantidad, 3);
    assert.strictEqual(item.unidad, 'cajas');
    assert.strictEqual(item.falta, true, 'Por defecto un nuevo ítem falta');
  });

  test('Rechaza items con nombre vacío', () => {
    assert.throws(() => {
      new Item({ nombre: '   ' });
    }, /El nombre del producto no puede estar vacío/);
  });

  test('Tachar y destachar alterna el estado de falta', () => {
    const item = new Item({ nombre: 'Picodulces', falta: true });
    assert.strictEqual(item.falta, true);
    
    // Tachar (comprado)
    item.toggleFalta();
    assert.strictEqual(item.falta, false);

    // Destachar (vuelve a faltar)
    item.toggleFalta();
    assert.strictEqual(item.falta, true);
  });

  test('Incremento y decremento seguro de cantidad', () => {
    const item = new Item({ nombre: 'Panes', cantidad: 2 });
    item.incrementarCantidad(3);
    assert.strictEqual(item.cantidad, 5);

    item.decrementarCantidad(2);
    assert.strictEqual(item.cantidad, 3);

    // No debe permitir cantidades negativas
    item.decrementarCantidad(10);
    assert.strictEqual(item.cantidad, 0);
  });
});

describe('📋 3. Operaciones de KioscoList', () => {
  test('Inicialización carga los productos por defecto', () => {
    const lista = new KioscoList();
    assert.strictEqual(lista.items.length, 9);
    assert.strictEqual(lista.obtenerMetricas().total, 9);
    assert.strictEqual(lista.obtenerMetricas().faltan, 9);
  });

  test('Añadir un nuevo producto a la lista', () => {
    const lista = new KioscoList();
    const nuevo = lista.agregarItem({
      nombre: 'Caramelos Sugus',
      cantidad: 5,
      unidad: 'bolsa',
      categoria: 'Golosinas'
    });

    assert.strictEqual(lista.items.length, 10);
    assert.strictEqual(lista.items[0].nombre, 'Caramelos Sugus', 'Debe agregarse al inicio');
    assert.strictEqual(nuevo.cantidad, 5);
  });

  test('Tachar un producto por su ID actualiza métricas', () => {
    const lista = new KioscoList();
    const primerItem = lista.items[0];

    lista.toggleTachar(primerItem.id);
    const metricas = lista.obtenerMetricas();
    assert.strictEqual(metricas.faltan, 8);
    assert.strictEqual(metricas.listos, 1);
  });

  test('Filtrar por estado: faltan vs listos', () => {
    const lista = new KioscoList();
    // Tachar los primeros 2
    lista.toggleTachar(lista.items[0].id);
    lista.toggleTachar(lista.items[1].id);

    const soloFaltan = lista.filtrar({ filtro: 'faltan' });
    assert.strictEqual(soloFaltan.length, 7);

    const soloListos = lista.filtrar({ filtro: 'listos' });
    assert.strictEqual(soloListos.length, 2);

    const todos = lista.filtrar({ filtro: 'todos' });
    assert.strictEqual(todos.length, 9);
  });

  test('Búsqueda por texto insensible a mayúsculas/minúsculas', () => {
    const lista = new KioscoList();
    const resultado = lista.filtrar({ busqueda: 'guaymallen' });
    assert.strictEqual(resultado.length, 1);
    assert.strictEqual(resultado[0].nombre, 'Alfajores Guaymallén');
  });

  test('Eliminar un producto de la lista', () => {
    const lista = new KioscoList();
    const idAEliminar = lista.items[0].id;
    const eliminado = lista.eliminarItem(idAEliminar);
    assert.strictEqual(eliminado, true);
    assert.strictEqual(lista.items.length, 8);
    assert.strictEqual(lista.obtenerItem(idAEliminar), undefined);
  });

  test('Generación de reporte para WhatsApp', () => {
    const lista = new KioscoList();
    const texto = lista.generarMensajeWhatsApp();
    assert.ok(texto.includes('FALTANTES DEL KIOSCO'));
    assert.ok(texto.includes('Promo de panchos'));
    assert.ok(texto.includes('Alfajores Fulbito'));
    assert.ok(!texto.includes('$'), 'El reporte no debe contener signo $ ni precios');
  });

  test('Serialización y restauración completa a JSON', () => {
    const listaOriginal = new KioscoList();
    listaOriginal.cambiarCantidad({ id: listaOriginal.items[0].id, delta: 5 });
    listaOriginal.toggleTachar(listaOriginal.items[1].id);

    const json = listaOriginal.toJSON();
    const listaRecuperada = KioscoList.fromJSON(json);

    assert.strictEqual(listaRecuperada.items.length, listaOriginal.items.length);
    assert.strictEqual(listaRecuperada.items[0].cantidad, listaOriginal.items[0].cantidad);
    assert.strictEqual(listaRecuperada.items[1].falta, listaOriginal.items[1].falta);
  });
});

describe('💾 4. Adaptadores de Almacenamiento y Compartir', () => {
  test('StorageAdapter funciona en memoria sin LocalStorage disponible', () => {
    const storage = new StorageAdapter({ clave: 'test_kiosco' });
    storage.guardar([{ nombre: 'Prueba', falta: true }]);
    const recuperado = storage.cargar();
    assert.strictEqual(recuperado.length, 1);
    assert.strictEqual(recuperado[0].nombre, 'Prueba');
  });

  test('ShareAdapter no lanza excepciones ante llamadas vacías', async () => {
    const resultado = await ShareAdapter.compartirNativoOClipboard({ texto: '' });
    assert.strictEqual(resultado.compartido, false);
  });
});

describe('📏 5. Verificación de Regla de Modularidad (<400 líneas por archivo)', async () => {
  async function escanearLineas(dir) {
    const entries = await fs.readdir(dir, { withFileTypes: true });
    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        await escanearLineas(fullPath);
      } else if (/\.(m?js|css|html)$/.test(entry.name)) {
        const content = await fs.readFile(fullPath, 'utf-8');
        const lineCount = content.split('\n').length;
        assert.ok(
          lineCount <= 400,
          `El archivo ${entry.name} supera las 400 líneas (tiene ${lineCount} líneas)`
        );
      }
    }
  }

  test('Ningún archivo de código en src/ supera el límite estricto de 400 líneas', async () => {
    await escanearLineas(SRC_DIR);
  });
});
