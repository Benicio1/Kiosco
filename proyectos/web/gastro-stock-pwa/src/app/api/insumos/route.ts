import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const search = searchParams.get("search") || "";
    const categoria = searchParams.get("categoria") || "";

    const where: any = {};

    if (search) {
      where.nombre = {
        contains: search,
      };
    }

    if (categoria && categoria !== "TODOS") {
      where.categoria = categoria;
    }

    const insumos = await prisma.insumo.findMany({
      where,
      orderBy: [
        { categoria: "asc" },
        { nombre: "asc" },
      ],
    });

    return NextResponse.json(insumos);
  } catch (error: any) {
    console.error("Error al obtener insumos:", error);
    return NextResponse.json(
      { error: "Error al obtener insumos", details: error.message },
      { status: 500 }
    );
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const {
      nombre,
      categoria,
      stockActual,
      stockMinimo,
      stockIdeal,
      unidadMedida,
      costoUnitario,
      fechaVencimiento,
    } = body;

    if (!nombre || !categoria || !unidadMedida) {
      return NextResponse.json(
        { error: "Campos requeridos faltantes" },
        { status: 400 }
      );
    }

    const nuevo = await prisma.insumo.create({
      data: {
        nombre,
        categoria,
        stockActual: parseFloat(stockActual) || 0,
        stockMinimo: parseFloat(stockMinimo) || 0,
        stockIdeal: parseFloat(stockIdeal) || (parseFloat(stockMinimo) || 0) * 2,
        unidadMedida,
        costoUnitario: parseFloat(costoUnitario) || 0,
        fechaVencimiento: fechaVencimiento ? new Date(fechaVencimiento) : null,
      },
    });

    return NextResponse.json(nuevo, { status: 201 });
  } catch (error: any) {
    console.error("Error al crear insumo:", error);
    return NextResponse.json(
      { error: "Error al crear insumo", details: error.message },
      { status: 500 }
    );
  }
}

// Actualización rápida de stock (+ / -) o edición de insumo
export async function PATCH(request: Request) {
  try {
    const body = await request.json();
    const { id, deltaStock, nuevoStock, ...otrosDatos } = body;

    if (!id) {
      return NextResponse.json(
        { error: "Se requiere el ID del insumo" },
        { status: 400 }
      );
    }

    const insumoActual = await prisma.insumo.findUnique({
      where: { id },
    });

    if (!insumoActual) {
      return NextResponse.json(
        { error: "Insumo no encontrado" },
        { status: 404 }
      );
    }

    let stockFinal = insumoActual.stockActual;
    if (typeof deltaStock === "number") {
      stockFinal = Math.max(0, Number((insumoActual.stockActual + deltaStock).toFixed(2)));
    } else if (typeof nuevoStock === "number") {
      stockFinal = Math.max(0, Number(nuevoStock.toFixed(2)));
    }

    const updated = await prisma.insumo.update({
      where: { id },
      data: {
        stockActual: stockFinal,
        ...(otrosDatos.costoUnitario !== undefined && {
          costoUnitario: parseFloat(otrosDatos.costoUnitario),
        }),
        ...(otrosDatos.stockMinimo !== undefined && {
          stockMinimo: parseFloat(otrosDatos.stockMinimo),
        }),
        ...(otrosDatos.stockIdeal !== undefined && {
          stockIdeal: parseFloat(otrosDatos.stockIdeal),
        }),
        ...(otrosDatos.fechaVencimiento !== undefined && {
          fechaVencimiento: otrosDatos.fechaVencimiento
            ? new Date(otrosDatos.fechaVencimiento)
            : null,
        }),
      },
    });

    return NextResponse.json(updated);
  } catch (error: any) {
    console.error("Error al actualizar stock:", error);
    return NextResponse.json(
      { error: "Error al actualizar stock", details: error.message },
      { status: 500 }
    );
  }
}

export async function DELETE(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const id = searchParams.get("id");

    if (!id) {
      return NextResponse.json({ error: "ID no provisto" }, { status: 400 });
    }

    await prisma.insumo.delete({
      where: { id },
    });

    return NextResponse.json({ success: true });
  } catch (error: any) {
    console.error("Error al eliminar insumo:", error);
    return NextResponse.json(
      { error: "Error al eliminar insumo", details: error.message },
      { status: 500 }
    );
  }
}
