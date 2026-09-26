"use client";

import React, { useState } from "react";
import { InsumoDTO, MotivoMerma } from "@/lib/types";
import { formatCurrency } from "@/lib/formatters";
import { X, Trash2, AlertTriangle } from "lucide-react";

interface MermaModalProps {
  insumo: InsumoDTO | null;
  currencySymbol?: string;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const MermaModal: React.FC<MermaModalProps> = ({
  insumo,
  currencySymbol = "$",
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [cantidad, setCantidad] = useState<string>("1");
  const [motivo, setMotivo] = useState<MotivoMerma>("MAL_ESTADO");
  const [notas, setNotas] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !insumo) return null;

  const cantNum = parseFloat(cantidad) || 0;
  const costoTotalPerdido = cantNum * insumo.costoUnitario;

  const motivosDisponibles: { id: MotivoMerma; label: string; icon: string }[] = [
    { id: "MAL_ESTADO", label: "Mal Estado", icon: "🤢" },
    { id: "VENCIDO", label: "Vencido", icon: "⏰" },
    { id: "ROTO", label: "Roto / Dañado", icon: "💥" },
    { id: "OTRO", label: "Otro Descarte", icon: "⚠️" },
  ];

  const handleQuickAdd = (delta: number) => {
    const current = parseFloat(cantidad) || 0;
    const nextVal = Math.max(0.1, Number((current + delta).toFixed(2)));
    setCantidad(nextVal.toString());
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (cantNum <= 0) {
      setError("Ingresa una cantidad mayor a 0");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const res = await fetch("/api/mermas", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          insumoId: insumo.id,
          cantidad: cantNum,
          motivo,
          notas,
        }),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Error al registrar merma");
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.message || "Error al conectar con el servidor");
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
            <div className="p-2 rounded-xl bg-rose-500/20 text-rose-400">
              <Trash2 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white leading-tight">
                Registrar Merma / Desperdicio
              </h2>
              <p className="text-xs text-neutral-400 font-medium">
                {insumo.nombre} (Disp: {insumo.stockActual} {insumo.unidadMedida})
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
          {error && (
            <div className="p-3 bg-rose-950/80 border border-rose-600/50 rounded-xl text-xs text-rose-300 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Cantidad a descartar */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1.5">
              Cantidad a descartar ({insumo.unidadMedida}):
            </label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                step="any"
                min="0.01"
                required
                value={cantidad}
                onChange={(e) => setCantidad(e.target.value)}
                className="w-full h-12 bg-neutral-800 border border-neutral-700 rounded-xl px-4 text-xl font-bold text-white focus:outline-none focus:border-rose-500"
              />
            </div>

            {/* Presets rápidos para carga veloz en cocina */}
            <div className="grid grid-cols-4 gap-1.5 mt-2">
              {[0.5, 1, 2, 5].map((val) => (
                <button
                  key={val}
                  type="button"
                  onClick={() => handleQuickAdd(val)}
                  className="py-2 bg-neutral-800/90 hover:bg-neutral-700 text-neutral-300 rounded-lg text-xs font-bold border border-neutral-700/80 active:scale-95 transition-all"
                >
                  +{val}
                </button>
              ))}
            </div>
          </div>

          {/* Motivo de descarte (Botones grandes táctiles) */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 mb-1.5">
              Motivo del Descarte:
            </label>
            <div className="grid grid-cols-2 gap-2">
              {motivosDisponibles.map((m) => (
                <button
                  key={m.id}
                  type="button"
                  onClick={() => setMotivo(m.id)}
                  className={`flex items-center gap-2 p-3 rounded-xl border text-xs font-bold transition-all active:scale-95 ${
                    motivo === m.id
                      ? "bg-rose-950/70 border-rose-500 text-rose-300 ring-1 ring-rose-500"
                      : "bg-neutral-800/60 border-neutral-700 text-neutral-300 hover:bg-neutral-800"
                  }`}
                >
                  <span className="text-base">{m.icon}</span>
                  <span>{m.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Impacto económico en vivo */}
          <div className="p-3 bg-neutral-950 rounded-xl border border-neutral-800/80 flex items-center justify-between text-xs">
            <span className="text-neutral-400">Pérdida Económica:</span>
            <span className="text-sm font-black text-rose-400">
              - {formatCurrency(costoTotalPerdido, currencySymbol)}
            </span>
          </div>

          {/* Notas opcionales */}
          <div>
            <input
              type="text"
              placeholder="Nota u observación (opcional)..."
              value={notas}
              onChange={(e) => setNotas(e.target.value)}
              className="w-full h-10 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-xs text-white focus:outline-none focus:border-neutral-500"
            />
          </div>

          {/* Botón táctil de confirmación */}
          <button
            type="submit"
            disabled={loading || cantNum <= 0}
            className="w-full h-14 rounded-2xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-base shadow-lg shadow-rose-600/30 active:scale-95 disabled:opacity-50 disabled:pointer-events-none transition-all flex items-center justify-center gap-2 mt-2"
          >
            {loading ? "Registrando..." : "Confirmar y Descontar Stock"}
          </button>
        </form>
      </div>
    </div>
  );
};
