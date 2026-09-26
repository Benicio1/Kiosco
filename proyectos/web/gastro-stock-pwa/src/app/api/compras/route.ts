import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { calcularCantidadSugerida } from "@/lib/stock-utils";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const estado = searchParams.get("estado"); // 'pendientes', 'comprados', 'todos'

    let where: any = {};
    if (estado === "pendientes") {
      where.comprado = false;
    } else if (estado === "comprados") {
      where.comprado = true;
    }

    const items = await prisma.itemCompra.findMany({
      where,
      include: {
        insumo: true,
      },
      orderBy: [
        { comprado: "asc" },
        { createdAt: "desc" },
      ],
    });

    // Calcular totales
    const pendientes = items.filter((i) => !i.comprado);
    const totalPresupuestoEstimado = pendientes.reduce(
      (sum, item) => sum + item.cantidadSugerida * item.costoEstimado,
      0
    );

    return NextResponse.json({
      items,
      totalPresupuestoEstimado,
      totalPendientes: pendientes.length,
    });
  } catch (error: any) {
    console.error("Error al obtener lista de compras:", error);
    return NextResponse.json(
      { error: "Error al obtener lista de compras", details: error.message },
      { status: 500 }
    );
  }
}

// Crear ítem manual o sincronización automática desde faltantes
export async function POST(request: Request) {
  try {
    const body = await request.json();

    // Sincronización automática de faltantes (Bajo Stock y Agotados)
    if (body.action === "SYNC_FALTANTES") {
      const insumos = await prisma.insumo.findMany();
      
      // Filtrar los que están en bajo stock o agotados
      const faltantes = insumos.filter(
        (i) => i.stockActual <= i.stockMinimo
      );

      // Obtener los que ya están en la lista como pendientes
      const itemsExistentes = await prisma.itemCompra.findMany({
        where: { comprado: false },
      });
      const insumosEnLista = new Set(
        itemsExistentes.map((item) => item.insumoId).filter(Boolean)
      );

      let agregadosCount = 0;
      for (const insumo of faltantes) {
        if (!insumosEnLista.has(insumo.id)) {
          const cantidadSugerida = calcularCantidadSugerida(insumo);
          if (cantidadSugerida > 0) {
            await prisma.itemCompra.create({
              data: {
                insumoId: insumo.id,
                nombre: insumo.nombre,
                categoria: insumo.categoria,
                cantidadSugerida,
                unidadMedida: insumo.unidadMedida,
                costoEstimado: insumo.costoUnitario,
                prioridad: insumo.stockActual === 0 ? "ALTA" : "MEDIA",
                comprado: false,
              },
            });
            agregadosCount++;
          }
        }
      }

      return NextResponse.json({
        message: `Se agregaron ${agregadosCount} faltantes a la lista`,
        agregadosCount,
      });
    }

    // Agregar compra manual puntual
    const {
      insumoId,
      nombre,
      categoria,
      cantidadSugerida,
      unidadMedida,
      costoEstimado,
      prioridad,
    } = body;

    if (!nombre || !cantidadSugerida) {
      return NextResponse.json(
        { error: "Nombre y cantidad son requeridos" },
        { status: 400 }
      );
    }

    const nuevoItem = await prisma.itemCompra.create({
      data: {
        insumoId: insumoId || null,
        nombre,
        categoria: categoria || "General",
        cantidadSugerida: parseFloat(cantidadSugerida),
        unidadMedida: unidadMedida || "u",
        costoEstimado: parseFloat(costoEstimado) || 0,
        prioridad: prioridad || "MEDIA",
        comprado: false,
      },
      include: {
        insumo: true,
      },
    });

    return NextResponse.json(nuevoItem, { status: 201 });
  } catch (error: any) {
    console.error("Error al crear ítem de compra:", error);
    return NextResponse.json(
      { error: "Error al crear ítem de compra", details: error.message },
      { status: 500 }
    );
  }
}

// Confirmar compra e ingresar stock automáticamente
export async function PUT(request: Request) {
  try {
    const body = await request.json();
    const { id, cantidadComprada, costoReal, comprado } = body;

    if (!id) {
      return NextResponse.json({ error: "ID requerido" }, { status: 400 });
    }

    const item = await prisma.itemCompra.findUnique({
      where: { id },
      include: { insumo: true },
    });

    if (!item) {
      return NextResponse.json({ error: "Ítem no encontrado" }, { status: 404 });
    }

    const cantCompradaNum = parseFloat(cantidadComprada);
    const costoRealNum = parseFloat(costoReal);

    // Transacción atómica
    const resultado = await prisma.$transaction(async (tx) => {
      // 1. Actualizar el ItemCompra
      const itemActualizado = await tx.itemCompra.update({
        where: { id },
        data: {
          comprado: comprado ?? true,
          cantidadComprada: !isNaN(cantCompradaNum) ? cantCompradaNum : item.cantidadSugerida,
          costoReal: !isNaN(costoRealNum) ? costoRealNum : item.costoEstimado,
          fechaCompra: comprado ? new Date() : null,
        },
      });

      // 2. Si tiene insumo vinculado y se marcó como comprado, incrementar stock en inventario
      let insumoActualizado = null;
      if (item.insumoId && comprado) {
        const insumo = await tx.insumo.findUnique({
          where: { id: item.insumoId },
        });

        if (insumo) {
          const cantidadSumar = !isNaN(cantCompradaNum)
            ? cantCompradaNum
            : item.cantidadSugerida;
          
          const nuevoStock = Number((insumo.stockActual + cantidadSumar).toFixed(2));
          
          // Actualizar costo unitario estimado si se cargó el costo real pagado
          const dataToUpdate: any = { stockActual: nuevoStock };
          if (!isNaN(costoRealNum) && costoRealNum > 0) {
            dataToUpdate.costoUnitario = costoRealNum;
          }

          insumoActualizado = await tx.insumo.update({
            where: { id: item.insumoId },
            data: dataToUpdate,
          });
        }
      }

      return { itemActualizado, insumoActualizado };
    });

    return NextResponse.json(resultado);
  } catch (error: any) {
    console.error("Error al actualizar compra:", error);
    return NextResponse.json(
      { error: "Error al actualizar compra", details: error.message },
      { status: 500 }
    );
  }
}

export async function DELETE(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const id = searchParams.get("id");

    if (!id) {
      return NextResponse.json({ error: "ID requerido" }, { status: 400 });
    }

    await prisma.itemCompra.delete({
      where: { id },
    });

    return NextResponse.json({ success: true });
  } catch (error: any) {
    console.error("Error al eliminar compra:", error);
    return NextResponse.json(
      { error: "Error al eliminar compra", details: error.message },
      { status: 500 }
    );
  }
}
