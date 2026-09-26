"use client";

import React from "react";
import { InsumoDTO } from "@/lib/types";
import { getEstadoStock, getEstadoVencimiento } from "@/lib/stock-utils";
import { formatCurrency, formatStockQuantity } from "@/lib/formatters";
import { StatusBadge } from "./StatusBadge";
import { Minus, Plus, Trash2, Calendar } from "lucide-react";

interface InventoryCardProps {
  insumo: InsumoDTO;
  currencySymbol?: string;
  onIncrement: (id: string, delta: number) => void;
  onDecrement: (id: string, delta: number) => void;
  onOpenMerma: (insumo: InsumoDTO) => void;
  onEdit?: (insumo: InsumoDTO) => void;
}

export const InventoryCard: React.FC<InventoryCardProps> = ({
  insumo,
  currencySymbol = "$",
  onIncrement,
  onDecrement,
  onOpenMerma,
  onEdit,
}) => {
  const estado = getEstadoStock(insumo.stockActual, insumo.stockMinimo);
  const vencimiento = getEstadoVencimiento(insumo.fechaVencimiento);

  // Determinar paso de incremento según unidad de medida
  const step = insumo.unidadMedida === "kg" || insumo.unidadMedida === "l" ? 0.5 : 1;

  // Resaltado de borde según severidad
  const borderHighlight =
    estado === "AGOTADO"
      ? "border-rose-500/50 bg-rose-950/10"
      : estado === "BAJO_STOCK"
      ? "border-amber-500/40 bg-amber-950/10"
      : "border-neutral-800 bg-neutral-900/90";

  return (
    <div
      className={`rounded-2xl border p-4 shadow-md transition-all select-none ${borderHighlight}`}
    >
      {/* Cabecera de la tarjeta: Categoría y Badges */}
      <div className="flex items-start justify-between gap-2 mb-2">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md bg-neutral-800 text-neutral-300">
            {insumo.categoria}
          </span>
          <h3
            onClick={() => onEdit && onEdit(insumo)}
            className="text-base font-bold text-white mt-1 cursor-pointer hover:text-blue-400 transition-colors"
          >
            {insumo.nombre}
          </h3>
        </div>
        <StatusBadge estado={estado} vencimiento={vencimiento} />
      </div>

      {/* Información de stock y costo */}
      <div className="grid grid-cols-2 gap-2 py-2 my-1 border-y border-neutral-800/60 text-xs">
        <div>
          <span className="text-neutral-400">Stock Actual:</span>
          <p className="text-lg font-black text-white">
            {formatStockQuantity(insumo.stockActual, insumo.unidadMedida)}
          </p>
          <span className="text-[11px] text-neutral-500">
            Mín: {formatStockQuantity(insumo.stockMinimo, insumo.unidadMedida)}
          </span>
        </div>
        <div className="text-right">
          <span className="text-neutral-400">Costo Unit.:</span>
          <p className="text-sm font-bold text-neutral-200">
            {formatCurrency(insumo.costoUnitario, currencySymbol)}
          </p>
          <span className="text-[11px] text-neutral-400 font-medium">
            Total: {formatCurrency(insumo.stockActual * insumo.costoUnitario, currencySymbol)}
          </span>
        </div>
      </div>

      {/* Vencimiento si existe */}
      {insumo.fechaVencimiento && (
        <div className="flex items-center gap-1 text-[11px] text-neutral-400 mb-3">
          <Calendar className="w-3 h-3 text-neutral-500" />
          <span>Vence: {new Date(insumo.fechaVencimiento).toLocaleDateString("es-AR")}</span>
        </div>
      )}

      {/* Acciones Rápidas Táctiles: Botón Merma + Stepper +/- */}
      <div className="flex items-center justify-between gap-2 pt-1">
        {/* Botón rápido Merma */}
        <button
          type="button"
          onClick={() => onOpenMerma(insumo)}
          aria-label="Registrar merma"
          className="flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-neutral-800 hover:bg-rose-950/40 text-rose-400 border border-neutral-700 hover:border-rose-700/50 text-xs font-semibold active:scale-95 transition-all min-h-[44px]"
        >
          <Trash2 className="w-4 h-4" />
          <span>Merma</span>
        </button>

        {/* Controles de incremento táctiles con una sola mano */}
        <div className="flex items-center gap-2">
          <button
            type="button"
            disabled={insumo.stockActual <= 0}
            onClick={() => onDecrement(insumo.id, step)}
            aria-label="Disminuir stock"
            className="w-12 h-11 flex items-center justify-center rounded-xl bg-neutral-800 text-neutral-200 hover:bg-neutral-700 border border-neutral-700 active:scale-90 disabled:opacity-30 disabled:pointer-events-none transition-all"
          >
            <Minus className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={() => onIncrement(insumo.id, step)}
            aria-label="Incrementar stock"
            className="w-12 h-11 flex items-center justify-center rounded-xl bg-blue-600 text-white hover:bg-blue-500 active:scale-90 shadow-lg shadow-blue-600/30 transition-all"
          >
            <Plus className="w-5 h-5 font-bold" />
          </button>
        </div>
      </div>
    </div>
  );
};
