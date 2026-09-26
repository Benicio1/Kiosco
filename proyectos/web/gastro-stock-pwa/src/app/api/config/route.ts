import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

export async function GET() {
  try {
    let config = await prisma.configuracion.findUnique({
      where: { id: "global" },
    });

    if (!config) {
      config = await prisma.configuracion.create({
        data: {
          id: "global",
          moneda: "$",
          nombreNegocio: "Mi Cocina",
        },
      });
    }

    return NextResponse.json(config);
  } catch (error: any) {
    console.error("Error al obtener configuración:", error);
    return NextResponse.json(
      { error: "Error al obtener configuración", details: error.message },
      { status: 500 }
    );
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { moneda, nombreNegocio } = body;

    const config = await prisma.configuracion.upsert({
      where: { id: "global" },
      update: {
        ...(moneda && { moneda }),
        ...(nombreNegocio && { nombreNegocio }),
      },
      create: {
        id: "global",
        moneda: moneda || "$",
        nombreNegocio: nombreNegocio || "Mi Cocina",
      },
    });

    return NextResponse.json(config);
  } catch (error: any) {
    console.error("Error al guardar configuración:", error);
    return NextResponse.json(
      { error: "Error al guardar configuración", details: error.message },
      { status: 500 }
    );
  }
}
