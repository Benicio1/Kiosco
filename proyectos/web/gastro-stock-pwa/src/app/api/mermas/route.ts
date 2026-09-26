import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const month = searchParams.get("month"); // opcional: 'YYYY-MM'

    let dateFilter: any = {};
    if (month) {
      const [yearStr, monthStr] = month.split("-");
      const year = parseInt(yearStr, 10);
      const monthIndex = parseInt(monthStr, 10) - 1;
      const startDate = new Date(year, monthIndex, 1);
      const endDate = new Date(year, monthIndex + 1, 0, 23, 59, 59, 999);
      dateFilter = {
        fecha: {
          gte: startDate,
          lte: endDate,
        },
      };
    } else {
      // Por defecto: mes corriente
      const now = new Date();
      const startDate = new Date(now.getFullYear(), now.getMonth(), 1);
      dateFilter = {
        fecha: {
          gte: startDate,
        },
      };
    }

    const mermas = await prisma.registroMerma.findMany({
      where: dateFilter,
      include: {
        insumo: true,
      },
      orderBy: {
        fecha: "desc",
      },
    });

    const totalCostoMes = mermas.reduce((acc, m) => acc + m.costoPerdido, 0);

    // Agrupación por motivo
    const porMotivo = mermas.reduce((acc: Record<string, number>, m) => {
      acc[m.motivo] = (acc[m.motivo] || 0) + m.costoPerdido;
      return acc;
    }, {});

    return NextResponse.json({
      mermas,
      totalCostoMes,
      porMotivo,
    });
  } catch (error: any) {
    console.error("Error al obtener mermas:", error);
    return NextResponse.json(
      { error: "Error al obtener mermas", details: error.message },
      { status: 500 }
    );
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { insumoId, cantidad, motivo, notas } = body;

    const cantNum = parseFloat(cantidad);
    if (!insumoId || isNaN(cantNum) || cantNum <= 0 || !motivo) {
      return NextResponse.json(
        { error: "Datos de merma inválidos o incompletos" },
        { status: 400 }
      );
    }

    // Transacción atómica: descontar stock y registrar merma
    const resultado = await prisma.$transaction(async (tx) => {
      const insumo = await tx.insumo.findUnique({
        where: { id: insumoId },
      });

      if (!insumo) {
        throw new Error("Insumo no encontrado");
      }

      const nuevoStock = Math.max(0, Number((insumo.stockActual - cantNum).toFixed(2)));
      const costoPerdido = Number((cantNum * insumo.costoUnitario).toFixed(2));

      const insumoActualizado = await tx.insumo.update({
        where: { id: insumoId },
        data: {
          stockActual: nuevoStock,
        },
      });

      const merma = await tx.registroMerma.create({
        data: {
          insumoId,
          cantidad: cantNum,
          unidadMedida: insumo.unidadMedida,
          costoPerdido,
          motivo,
          notas: notas || null,
        },
        include: {
          insumo: true,
        },
      });

      return { merma, insumoActualizado };
    });

    return NextResponse.json(resultado, { status: 201 });
  } catch (error: any) {
    console.error("Error al registrar merma:", error);
    return NextResponse.json(
      { error: "Error al registrar merma", details: error.message },
      { status: 500 }
    );
  }
}
