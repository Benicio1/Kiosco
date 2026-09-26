import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { getEstadoVencimiento } from "@/lib/stock-utils";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    const [insumos, itemsCompra, mermasRecientes, config] = await Promise.all([
      prisma.insumo.findMany({
        orderBy: { nombre: "asc" },
      }),
      prisma.itemCompra.findMany({
        where: { comprado: false },
      }),
      prisma.registroMerma.findMany({
        take: 5,
        orderBy: { fecha: "desc" },
        include: { insumo: true },
      }),
      prisma.configuracion.findUnique({
        where: { id: "global" },
      }),
    ]);

    // 1. Valor del inventario inmovilizado
    const valorInventario = insumos.reduce(
      (sum, item) => sum + item.stockActual * item.costoUnitario,
      0
    );

    // 2. Presupuesto de compras pendientes
    const presupuestoPendiente = itemsCompra.reduce(
      (sum, item) => sum + item.cantidadSugerida * item.costoEstimado,
      0
    );

    // 3. Artículos críticos (Agotados o bajo stock)
    const criticos = insumos.filter(
      (item) => item.stockActual <= item.stockMinimo
    );

    // 4. Artículos por vencer (<= 3 días o ya vencidos)
    const porVencer = insumos.filter((item) => {
      const v = getEstadoVencimiento(item.fechaVencimiento);
      return v.isExpiring;
    });

    return NextResponse.json({
      kpis: {
        valorInventario,
        presupuestoPendiente,
        articulosCriticos: criticos.length,
        articulosPorVencer: porVencer.length,
        totalInsumos: insumos.length,
      },
      criticos: criticos.slice(0, 6),
      porVencer: porVencer.slice(0, 6),
      mermasRecientes,
      config: config || { moneda: "$", nombreNegocio: "Mi Cocina" },
    });
  } catch (error: any) {
    console.error("Error al obtener datos del dashboard:", error);
    return NextResponse.json(
      { error: "Error al obtener datos del dashboard", details: error.message },
      { status: 500 }
    );
  }
}
