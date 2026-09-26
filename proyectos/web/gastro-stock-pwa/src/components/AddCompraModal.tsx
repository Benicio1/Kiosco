"use client";

import React, { useState } from "react";
import { PrioridadCompra } from "@/lib/types";
import { X, Plus, ShoppingCart } from "lucide-react";

interface AddCompraModalProps {
  isOpen: boolean;
  currencySymbol?: string;
  onClose: () => void;
  onSuccess: () => void;
}

export const AddCompraModal: React.FC<AddCompraModalProps> = ({
  isOpen,
  currencySymbol = "$",
  onClose,
  onSuccess,
}) => {
  const [nombre, setNombre] = useState("");
  const [categoria, setCategoria] = useState("General");
  const [cantidadSugerida, setCantidadSugerida] = useState("1");
  const [unidadMedida, setUnidadMedida] = useState("u");
  const [costoEstimado, setCostoEstimado] = useState("0");
  const [prioridad, setPrioridad] = useState<PrioridadCompra>("MEDIA");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const prioridades: { id: PrioridadCompra; label: string; color: string }[] = [
    { id: "ALTA", label: "Alta", color: "border-rose-500 text-rose-400 bg-rose-950/40" },
    { id: "MEDIA", label: "Media", color: "border-amber-500 text-amber-400 bg-amber-950/40" },
    { id: "BAJA", label: "Baja", color: "border-blue-500 text-blue-400 bg-blue-950/40" },
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombre.trim()) {
      setError("El nombre es requerido");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const res = await fetch("/api/compras", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nombre: nombre.trim(),
          categoria,
          cantidadSugerida: parseFloat(cantidadSugerida) || 1,
          unidadMedida,
          costoEstimado: parseFloat(costoEstimado) || 0,
          prioridad,
        }),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Error al agregar compra");
      }

      onSuccess();
      onClose();
      setNombre("");
      setCantidadSugerida("1");
      setCostoEstimado("0");
    } catch (err: any) {
      setError(err.message || "Error al conectar");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-neutral-900 border-t sm:border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl">
        <div className="flex items-center justify-between pb-3 border-b border-neutral-800">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-blue-500/20 text-blue-400">
              <ShoppingCart className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white leading-tight">
                Agregar a Compras
              </h2>
              <p className="text-xs text-neutral-400">
                Compra puntual de mercado
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

          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Artículo a comprar:
            </label>
            <input
              type="text"
              required
              placeholder="Ej. Servilletas, Lavandina, Pimentón..."
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-xs font-semibold text-neutral-300 mb-1">
                Cantidad:
              </label>
              <input
                type="number"
                step="any"
                min="0.1"
                required
                value={cantidadSugerida}
                onChange={(e) => setCantidadSugerida(e.target.value)}
                className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-neutral-300 mb-1">
                Unidad:
              </label>
              <select
                value={unidadMedida}
                onChange={(e) => setUnidadMedida(e.target.value)}
                className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
              >
                <option value="u">Unidades (u)</option>
                <option value="kg">Kilos (kg)</option>
                <option value="gr">Gramos (gr)</option>
                <option value="l">Litros (l)</option>
                <option value="pack">Pack / Caja</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1">
              Costo Estimado Unitario ({currencySymbol}):
            </label>
            <input
              type="number"
              step="any"
              min="0"
              value={costoEstimado}
              onChange={(e) => setCostoEstimado(e.target.value)}
              className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Prioridad */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1.5">
              Prioridad de Compra:
            </label>
            <div className="grid grid-cols-3 gap-2">
              {prioridades.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => setPrioridad(p.id)}
                  className={`py-2 rounded-xl text-xs font-bold border transition-all active:scale-95 ${
                    prioridad === p.id
                      ? `${p.color} ring-1`
                      : "bg-neutral-800 text-neutral-400 border-neutral-700 hover:bg-neutral-750"
                  }`}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full h-13 py-3.5 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-sm shadow-lg shadow-blue-600/30 active:scale-95 disabled:opacity-50 transition-all flex items-center justify-center gap-2 mt-3"
          >
            <Plus className="w-4 h-4" />
            <span>{loading ? "Agregando..." : "Agregar a la Lista"}</span>
          </button>
        </form>
      </div>
    </div>
  );
};
