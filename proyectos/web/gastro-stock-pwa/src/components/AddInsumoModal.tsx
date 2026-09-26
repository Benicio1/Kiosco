"use client";

import React, { useState } from "react";
import { CategoriaInsumo, UnidadMedida } from "@/lib/types";
import { X, Plus, PackagePlus } from "lucide-react";

interface AddInsumoModalProps {
  isOpen: boolean;
  currencySymbol?: string;
  onClose: () => void;
  onSuccess: () => void;
}

export const AddInsumoModal: React.FC<AddInsumoModalProps> = ({
  isOpen,
  currencySymbol = "$",
  onClose,
  onSuccess,
}) => {
  const [nombre, setNombre] = useState("");
  const [categoria, setCategoria] = useState<CategoriaInsumo>("Carnes");
  const [stockActual, setStockActual] = useState("0");
  const [stockMinimo, setStockMinimo] = useState("2");
  const [stockIdeal, setStockIdeal] = useState("5");
  const [unidadMedida, setUnidadMedida] = useState<UnidadMedida>("kg");
  const [costoUnitario, setCostoUnitario] = useState("0");
  const [fechaVencimiento, setFechaVencimiento] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const categorias = [
    "Carnes",
    "Verduras",
    "Secos",
    "Lácteos",
    "Bebidas",
    "Descartables",
  ];

  const unidades = ["kg", "gr", "l", "u"];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombre.trim()) {
      setError("El nombre es requerido");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const res = await fetch("/api/insumos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nombre: nombre.trim(),
          categoria,
          stockActual: parseFloat(stockActual) || 0,
          stockMinimo: parseFloat(stockMinimo) || 0,
          stockIdeal: parseFloat(stockIdeal) || 0,
          unidadMedida,
          costoUnitario: parseFloat(costoUnitario) || 0,
          fechaVencimiento: fechaVencimiento || null,
        }),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Error al crear insumo");
      }

      onSuccess();
      onClose();
      // Reset form
      setNombre("");
      setStockActual("0");
      setCostoUnitario("0");
      setFechaVencimiento("");
    } catch (err: any) {
      setError(err.message || "Error al conectar");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/80 backdrop-blur-sm animate-fade-in overflow-y-auto">
      <div className="w-full max-w-md bg-neutral-900 border-t sm:border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl my-auto">
        <div className="flex items-center justify-between pb-3 border-b border-neutral-800">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-blue-500/20 text-blue-400">
              <PackagePlus className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white leading-tight">
                Nuevo Insumo
              </h2>
              <p className="text-xs text-neutral-400">
                Alta de artículo al inventario
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

        <form onSubmit={handleSubmit} className="mt-4 space-y-3.5">
          {error && (
            <div className="p-2.5 bg-rose-950/80 border border-rose-600/50 rounded-xl text-xs text-rose-300">
              {error}
            </div>
          )}

          {/* Nombre */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Nombre del Insumo:
            </label>
            <input
              type="text"
              required
              placeholder="Ej. Salmón Rosado, Harina 000..."
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Categoría */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Categoría:
            </label>
            <div className="grid grid-cols-3 gap-1.5">
              {categorias.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setCategoria(cat)}
                  className={`py-2 px-1 text-center rounded-xl text-xs font-bold border transition-all ${
                    categoria === cat
                      ? "bg-blue-600 text-white border-blue-500"
                      : "bg-neutral-800 text-neutral-300 border-neutral-700 hover:bg-neutral-750"
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          {/* Unidad de medida */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Unidad de Medida:
            </label>
            <div className="grid grid-cols-4 gap-2">
              {unidades.map((u) => (
                <button
                  key={u}
                  type="button"
                  onClick={() => setUnidadMedida(u)}
                  className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                    unidadMedida === u
                      ? "bg-blue-600 text-white border-blue-500"
                      : "bg-neutral-800 text-neutral-300 border-neutral-700 hover:bg-neutral-750"
                  }`}
                >
                  {u}
                </button>
              ))}
            </div>
          </div>

          {/* Stock Actual y Mínimo */}
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-xs font-semibold text-neutral-300 mb-1">
                Stock Inicial:
              </label>
              <input
                type="number"
                step="any"
                min="0"
                value={stockActual}
                onChange={(e) => setStockActual(e.target.value)}
                className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-neutral-300 mb-1">
                Stock Mínimo (Alerta):
              </label>
              <input
                type="number"
                step="any"
                min="0"
                value={stockMinimo}
                onChange={(e) => setStockMinimo(e.target.value)}
                className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>
          </div>

          {/* Costo Unitario Estimado */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Costo Unitario Estimado ({currencySymbol}):
            </label>
            <input
              type="number"
              step="any"
              min="0"
              placeholder="0.00"
              value={costoUnitario}
              onChange={(e) => setCostoUnitario(e.target.value)}
              className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Fecha de vencimiento opcional */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Fecha de Vencimiento (Opcional):
            </label>
            <input
              type="date"
              value={fechaVencimiento}
              onChange={(e) => setFechaVencimiento(e.target.value)}
              className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full h-13 py-3.5 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-sm shadow-lg shadow-blue-600/30 active:scale-95 disabled:opacity-50 transition-all flex items-center justify-center gap-2 mt-3"
          >
            <Plus className="w-4 h-4" />
            <span>{loading ? "Guardando..." : "Guardar Insumo"}</span>
          </button>
        </form>
      </div>
    </div>
  );
};
