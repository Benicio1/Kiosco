"use client";

import React, { useState, useEffect } from "react";
import { ItemCompraDTO } from "@/lib/types";
import { formatCurrency } from "@/lib/formatters";
import { X, CheckCircle2, ShoppingBag } from "lucide-react";

interface BuyItemModalProps {
  item: ItemCompraDTO | null;
  currencySymbol?: string;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (id: string, cantidadComprada: number, costoReal: number) => Promise<void>;
}

export const BuyItemModal: React.FC<BuyItemModalProps> = ({
  item,
  currencySymbol = "$",
  isOpen,
  onClose,
  onConfirm,
}) => {
  const [cantidad, setCantidad] = useState<string>("1");
  const [precio, setPrecio] = useState<string>("0");
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    if (item) {
      setCantidad(item.cantidadSugerida.toString());
      setPrecio(item.costoEstimado.toString());
    }
  }, [item]);

  if (!isOpen || !item) return null;

  const cantNum = parseFloat(cantidad) || 0;
  const precioNum = parseFloat(precio) || 0;
  const totalGasto = cantNum * precioNum;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (cantNum <= 0) return;

    try {
      setLoading(true);
      await onConfirm(item.id, cantNum, precioNum);
      onClose();
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-neutral-900 border-t sm:border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl">
        {/* Encabezado */}
        <div className="flex items-center justify-between pb-3 border-b border-neutral-800">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-emerald-500/20 text-emerald-400">
              <ShoppingBag className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white leading-tight">
                Confirmar Compra
              </h2>
              <p className="text-xs text-neutral-400 font-medium">
                {item.nombre}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-2 text-neutral-400 hover:text-white rounded-full bg-neutral-800/80 active:scale-95"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div className="bg-blue-950/40 border border-blue-800/40 rounded-xl p-3 text-xs text-blue-300">
            ℹ️ Al confirmar, esta cantidad se ingresará inmediatamente al inventario y actualizará el stock disponible.
          </div>

          {/* Cantidad Real Comprada */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Cantidad Real Comprada ({item.unidadMedida}):
            </label>
            <input
              type="number"
              step="any"
              min="0.01"
              required
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
              className="w-full h-12 bg-neutral-800 border border-neutral-700 rounded-xl px-4 text-xl font-bold text-white focus:outline-none focus:border-emerald-500"
            />
            <p className="text-[11px] text-neutral-500 mt-1">
              Sugerido original: {item.cantidadSugerida} {item.unidadMedida}
            </p>
          </div>

          {/* Precio Unitario Real Pagado */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Precio Unitario Real Pagado ({currencySymbol}):
            </label>
            <input
              type="number"
              step="any"
              min="0"
              required
              value={precio}
              onChange={(e) => setPrecio(e.target.value)}
              className="w-full h-12 bg-neutral-800 border border-neutral-700 rounded-xl px-4 text-xl font-bold text-white focus:outline-none focus:border-emerald-500"
            />
            <p className="text-[11px] text-neutral-500 mt-1">
              Estimado inicial: {formatCurrency(item.costoEstimado, currencySymbol)}
            </p>
          </div>

          {/* Total Abonado */}
          <div className="p-3 bg-neutral-950 rounded-xl border border-neutral-800 flex items-center justify-between text-xs">
            <span className="text-neutral-400">Total Abonado en Caja:</span>
            <span className="text-base font-black text-emerald-400">
              {formatCurrency(totalGasto, currencySymbol)}
            </span>
          </div>

          {/* Botón Táctil de Aprobación */}
          <button
            type="submit"
            disabled={loading || cantNum <= 0}
            className="w-full h-14 rounded-2xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-base shadow-lg shadow-emerald-600/30 active:scale-95 disabled:opacity-50 disabled:pointer-events-none transition-all flex items-center justify-center gap-2 mt-2"
          >
            <CheckCircle2 className="w-5 h-5" />
            <span>{loading ? "Actualizando stock..." : "Ingresar a Inventario"}</span>
          </button>
        </form>
      </div>
    </div>
  );
};
