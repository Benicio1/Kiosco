import { EstadoStock, InsumoDTO } from "./types";

export function getEstadoStock(stockActual: number, stockMinimo: number): EstadoStock {
  if (stockActual <= 0) return "AGOTADO";
  if (stockActual <= stockMinimo) return "BAJO_STOCK";
  return "OPTIMO";
}

export function getEstadoVencimiento(fechaVencimiento: string | Date | null | undefined): {
  isExpiring: boolean;
  isExpired: boolean;
  daysLeft: number | null;
  text: string;
} {
  if (!fechaVencimiento) {
    return { isExpiring: false, isExpired: false, daysLeft: null, text: "" };
  }

  const vencimiento = new Date(fechaVencimiento);
  const hoy = new Date();
  
  // Normalizar a media noche
  vencimiento.setHours(0, 0, 0, 0);
  hoy.setHours(0, 0, 0, 0);

  const diffTime = vencimiento.getTime() - hoy.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return {
      isExpiring: true,
      isExpired: true,
      daysLeft: diffDays,
      text: `Venció hace ${Math.abs(diffDays)}d`,
    };
  }

  if (diffDays === 0) {
    return {
      isExpiring: true,
      isExpired: false,
      daysLeft: 0,
      text: "Vence hoy",
    };
  }

  if (diffDays <= 3) {
    return {
      isExpiring: true,
      isExpired: false,
      daysLeft: diffDays,
      text: `Vence en ${diffDays}d`,
    };
  }

  return {
    isExpiring: false,
    isExpired: false,
    daysLeft: diffDays,
    text: `Vence en ${diffDays}d`,
  };
}

export function calcularCantidadSugerida(insumo: {
  stockActual: number;
  stockMinimo: number;
  stockIdeal?: number;
}): number {
  const ideal = insumo.stockIdeal && insumo.stockIdeal > 0 
    ? insumo.stockIdeal 
    : insumo.stockMinimo * 2;
  const sugerida = ideal - insumo.stockActual;
  return Number(Math.max(0, sugerida).toFixed(2));
}
