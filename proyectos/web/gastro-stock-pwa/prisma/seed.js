const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function main() {
  console.log('--- Limpiando datos existentes ---');
  await prisma.registroMerma.deleteMany({});
  await prisma.itemCompra.deleteMany({});
  await prisma.insumo.deleteMany({});
  await prisma.configuracion.deleteMany({});

  console.log('--- Creando Configuración inicial ---');
  await prisma.configuracion.create({
    data: {
      id: 'global',
      moneda: '$',
      nombreNegocio: 'Bistró & Cocina Pro',
    },
  });

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

  // Fechas de vencimiento para testing de alertas
  const expiraManana = new Date(today.getTime() + 1 * 24 * 60 * 60 * 1000);
  const expiraEn2Dias = new Date(today.getTime() + 2 * 24 * 60 * 60 * 1000);
  const expiraEn10Dias = new Date(today.getTime() + 10 * 24 * 60 * 60 * 1000);

  console.log('--- Creando Insumos de prueba ---');
  const insumos = [
    // Carnes
    {
      nombre: 'Lomo de Ternera',
      categoria: 'Carnes',
      stockActual: 1.5,
      stockMinimo: 5.0,
      stockIdeal: 12.0,
      unidadMedida: 'kg',
      costoUnitario: 14500,
      fechaVencimiento: expiraEn2Dias, // Alerta: por vencer
    },
    {
      nombre: 'Pechuga de Pollo',
      categoria: 'Carnes',
      stockActual: 8.0,
      stockMinimo: 4.0,
      stockIdeal: 10.0,
      unidadMedida: 'kg',
      costoUnitario: 6200,
      fechaVencimiento: expiraEn10Dias,
    },
    {
      nombre: 'Salmón Rosado',
      categoria: 'Carnes',
      stockActual: 0.0, // Agotado
      stockMinimo: 3.0,
      stockIdeal: 6.0,
      unidadMedida: 'kg',
      costoUnitario: 22000,
      fechaVencimiento: null,
    },

    // Verduras
    {
      nombre: 'Cebolla Morada',
      categoria: 'Verduras',
      stockActual: 15.0,
      stockMinimo: 5.0,
      stockIdeal: 15.0,
      unidadMedida: 'kg',
      costoUnitario: 1200,
      fechaVencimiento: null,
    },
    {
      nombre: 'Tomate Redondo',
      categoria: 'Verduras',
      stockActual: 2.0,
      stockMinimo: 6.0,
      stockIdeal: 10.0,
      unidadMedida: 'kg',
      costoUnitario: 2100,
      fechaVencimiento: expiraManana, // Alerta: por vencer y bajo stock
    },
    {
      nombre: 'Rúcula Fresca',
      categoria: 'Verduras',
      stockActual: 0.0, // Agotado
      stockMinimo: 1.0,
      stockIdeal: 3.0,
      unidadMedida: 'kg',
      costoUnitario: 3500,
      fechaVencimiento: null,
    },

    // Lácteos
    {
      nombre: 'Queso Mozzarella',
      categoria: 'Lácteos',
      stockActual: 4.5,
      stockMinimo: 4.0,
      stockIdeal: 10.0,
      unidadMedida: 'kg',
      costoUnitario: 7800,
      fechaVencimiento: expiraEn10Dias,
    },
    {
      nombre: 'Crema de Leche',
      categoria: 'Lácteos',
      stockActual: 1.0,
      stockMinimo: 3.0,
      stockIdeal: 8.0,
      unidadMedida: 'l',
      costoUnitario: 3900,
      fechaVencimiento: expiraEn2Dias, // Por vencer
    },

    // Secos
    {
      nombre: 'Arroz Carnaroli',
      categoria: 'Secos',
      stockActual: 10.0,
      stockMinimo: 3.0,
      stockIdeal: 10.0,
      unidadMedida: 'kg',
      costoUnitario: 4500,
      fechaVencimiento: null,
    },
    {
      nombre: 'Aceite de Oliva Extra Virgen',
      categoria: 'Secos',
      stockActual: 1.5,
      stockMinimo: 5.0,
      stockIdeal: 10.0,
      unidadMedida: 'l',
      costoUnitario: 11000,
      fechaVencimiento: null,
    },

    // Bebidas
    {
      nombre: 'Vino Malbec Reserva',
      categoria: 'Bebidas',
      stockActual: 12,
      stockMinimo: 6,
      stockIdeal: 18,
      unidadMedida: 'u',
      costoUnitario: 6500,
      fechaVencimiento: null,
    },
    {
      nombre: 'Agua Mineral 500ml',
      categoria: 'Bebidas',
      stockActual: 0, // Agotado
      stockMinimo: 24,
      stockIdeal: 48,
      unidadMedida: 'u',
      costoUnitario: 800,
      fechaVencimiento: null,
    },

    // Descartables
    {
      nombre: 'Cajas de Pizza 33x33',
      categoria: 'Descartables',
      stockActual: 80,
      stockMinimo: 50,
      stockIdeal: 200,
      unidadMedida: 'u',
      costoUnitario: 450,
      fechaVencimiento: null,
    },
    {
      nombre: 'Papel Film 30cm',
      categoria: 'Descartables',
      stockActual: 1,
      stockMinimo: 2,
      stockIdeal: 5,
      unidadMedida: 'u',
      costoUnitario: 3200,
      fechaVencimiento: null,
    },
  ];

  const createdInsumos = [];
  for (const item of insumos) {
    const created = await prisma.insumo.create({ data: item });
    createdInsumos.push(created);
  }

  console.log(`Creados ${createdInsumos.length} insumos.`);

  // Crear algunas compras de prueba pre-cargadas
  console.log('--- Creando Lista de Compras inicial ---');
  const salmon = createdInsumos.find(i => i.nombre === 'Salmón Rosado');
  const aceite = createdInsumos.find(i => i.nombre === 'Aceite de Oliva Extra Virgen');
  const tomate = createdInsumos.find(i => i.nombre === 'Tomate Redondo');

  if (salmon) {
    await prisma.itemCompra.create({
      data: {
        insumoId: salmon.id,
        nombre: salmon.nombre,
        categoria: salmon.categoria,
        cantidadSugerida: salmon.stockIdeal - salmon.stockActual,
        unidadMedida: salmon.unidadMedida,
        costoEstimado: salmon.costoUnitario,
        prioridad: 'ALTA',
        comprado: false,
      },
    });
  }

  if (aceite) {
    await prisma.itemCompra.create({
      data: {
        insumoId: aceite.id,
        nombre: aceite.nombre,
        categoria: aceite.categoria,
        cantidadSugerida: aceite.stockIdeal - aceite.stockActual,
        unidadMedida: aceite.unidadMedida,
        costoEstimado: aceite.costoUnitario,
        prioridad: 'MEDIA',
        comprado: false,
      },
    });
  }

  // Compra manual puntual (sin insumo en stock aún)
  await prisma.itemCompra.create({
    data: {
      nombre: 'Esponjas de Acero Inox',
      categoria: 'Descartables',
      cantidadSugerida: 6,
      unidadMedida: 'u',
      costoEstimado: 950,
      prioridad: 'BAJA',
      comprado: false,
    },
  });

  // Crear registros de mermas pasadas para el módulo de costos
  console.log('--- Creando registros de Merma iniciales ---');
  if (tomate) {
    await prisma.registroMerma.create({
      data: {
        insumoId: tomate.id,
        cantidad: 1.5,
        unidadMedida: 'kg',
        costoPerdido: 1.5 * tomate.costoUnitario,
        motivo: 'MAL_ESTADO',
        notas: 'Cajón recibido con humedad',
        fecha: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
      },
    });
  }

  const crema = createdInsumos.find(i => i.nombre === 'Crema de Leche');
  if (crema) {
    await prisma.registroMerma.create({
      data: {
        insumoId: crema.id,
        cantidad: 1.0,
        unidadMedida: 'l',
        costoPerdido: 1.0 * crema.costoUnitario,
        motivo: 'VENCIDO',
        notas: 'Expiró en heladera de estación',
        fecha: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
      },
    });
  }

  console.log('--- Seed completado con éxito! ---');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
