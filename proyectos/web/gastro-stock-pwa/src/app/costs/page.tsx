"use client";

import React, { useState, useEffect } from "react";
import { Header } from "@/components/Header";
import { KpiCard } from "@/components/KpiCard";
import { formatCurrency } from "@/lib/formatters";
import {
  DollarSign,
  TrendingDown,
  Boxes,
  ShoppingCart,
  PieChart,
  Settings,
  Calendar,
  AlertCircle,
  Save,
} from "lucide-react";

export default function CostsPage() {
  const [loading, setLoading] = useState<boolean>(true);
  const [valorInventario, setValorInventario] = useState<number>(0);
  const [presupuestoPendiente, setPresupuestoPendiente] = useState<number>(0);
  const [mermas, setMermas] = useState<any[]>([]);
  const [totalMermasMes, setTotalMermasMes] = useState<number>(0);
  const [porMotivo, setPorMotivo] = useState<Record<string, number>>({});
  const [moneda, setMoneda] = useState<string>("$");
  const [nombreNegocio, setNombreNegocio] = useState<string>("Mi Cocina");
  const [showConfigModal, setShowConfigModal] = useState<boolean>(false);
  const [tempMoneda, setTempMoneda] = useState<string>("$");
  const [tempNombre, setTempNombre] = useState<string>("");

  const fetchData = async () => {
    try {
      setLoading(true);

      // 1. Dashboard data for inventory value and budget
      const dashRes = await fetch("/api/dashboard");
      if (dashRes.ok) {
        const d = await dashRes.json();
        setValorInventario(d.kpis.valorInventario);
        setPresupuestoPendiente(d.kpis.presupuestoPendiente);
        if (d.config) {
          setMoneda(d.config.moneda || "$");
          setNombreNegocio(d.config.nombreNegocio || "Mi Cocina");
          setTempMoneda(d.config.moneda || "$");
          setTempNombre(d.config.nombreNegocio || "Mi Cocina");
        }
      }

      // 2. Waste records for this month
      const mermaRes = await fetch("/api/mermas");
      if (mermaRes.ok) {
        const m = await mermaRes.json();
        setMermas(m.mermas || []);
        setTotalMermasMes(m.totalCostoMes || 0);
        setPorMotivo(m.porMotivo || {});
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSaveConfig = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch("/api/config", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          moneda: tempMoneda,
          nombreNegocio: tempNombre,
        }),
      });
      if (res.ok) {
        setMoneda(tempMoneda);
        setNombreNegocio(tempNombre);
        setShowConfigModal(false);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const getMotivoLabel = (motivo: string) => {
    switch (motivo) {
      case "MAL_ESTADO":
        return "Mal Estado";
      case "VENCIDO":
        return "Vencido";
      case "ROTO":
        return "Roto / Dañado";
      default:
        return "Otro";
    }
  };

  return (
    <div className="min-h-screen bg-[#0b0f19] px-4 pt-3 pb-24">
      <Header
        title="Finanzas y Costos"
        subtitle="Valorización y Balance Operativo"
      />

      {/* Botón de configuración de moneda y negocio */}
      <div className="flex items-center justify-between mt-3 px-1">
        <span className="text-xs font-bold text-neutral-400 uppercase tracking-wider">
          Moneda Activa: <strong className="text-blue-400">{moneda}</strong>
        </span>
        <button
          type="button"
          onClick={() => setShowConfigModal(true)}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-neutral-900 border border-neutral-800 text-xs font-semibold text-neutral-300 hover:text-white active:scale-95 transition-all"
        >
          <Settings className="w-3.5 h-3.5" />
          <span>Configurar</span>
        </button>
      </div>

      {/* Tarjetas Principales de Costos */}
      <div className="space-y-3 mt-3">
        {/* 1. Valorización de Inventario Inmovilizado */}
        <div className="p-5 rounded-3xl bg-neutral-900 border border-neutral-800 shadow-xl">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-neutral-400 uppercase tracking-wider">
                Valorización de Inventario
              </p>
              <p className="text-3xl font-black text-white mt-1">
                {formatCurrency(valorInventario, moneda)}
              </p>
              <p className="text-xs text-neutral-500 mt-1">
                Capital total inmovilizado en insumos y materias primas
              </p>
            </div>
            <div className="p-3 rounded-2xl bg-blue-500/10 text-blue-400">
              <Boxes className="w-6 h-6" />
            </div>
          </div>
        </div>

        {/* 2. Presupuesto Antes de Salir */}
        <div className="p-5 rounded-3xl bg-neutral-900 border border-neutral-800 shadow-xl">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-neutral-400 uppercase tracking-wider">
                Presupuesto de Salida
              </p>
              <p className="text-3xl font-black text-amber-400 mt-1">
                {formatCurrency(presupuestoPendiente, moneda)}
              </p>
              <p className="text-xs text-neutral-500 mt-1">
                Total estimado para comprar los faltantes de la lista
              </p>
            </div>
            <div className="p-3 rounded-2xl bg-amber-500/10 text-amber-400">
              <ShoppingCart className="w-6 h-6" />
            </div>
          </div>
        </div>

        {/* 3. Pérdidas por Merma del Mes */}
        <div className="p-5 rounded-3xl bg-rose-950/20 border border-rose-900/40 shadow-xl">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-bold text-rose-400 uppercase tracking-wider">
                Pérdidas por Merma (Mes)
              </p>
              <p className="text-3xl font-black text-rose-300 mt-1">
                - {formatCurrency(totalMermasMes, moneda)}
              </p>
              <p className="text-xs text-rose-400/80 mt-1">
                Insumos descartados por mal estado, vencimiento o rotura
              </p>
            </div>
            <div className="p-3 rounded-2xl bg-rose-500/20 text-rose-400">
              <TrendingDown className="w-6 h-6" />
            </div>
          </div>

          {/* Desglose visual por motivo */}
          {totalMermasMes > 0 && (
            <div className="mt-4 pt-3 border-t border-rose-900/40 space-y-2">
              <p className="text-xs font-bold text-neutral-300">
                Distribución de Pérdidas:
              </p>
              {Object.entries(porMotivo).map(([motivo, monto]) => {
                const porcentaje = Math.round((monto / totalMermasMes) * 100);
                return (
                  <div key={motivo}>
                    <div className="flex justify-between text-xs font-medium mb-1">
                      <span className="text-neutral-400">{getMotivoLabel(motivo)}</span>
                      <span className="text-white">
                        {formatCurrency(monto, moneda)} ({porcentaje}%)
                      </span>
                    </div>
                    <div className="w-full h-2 rounded-full bg-neutral-800 overflow-hidden">
                      <div
                        className="h-full bg-rose-500 rounded-full"
                        style={{ width: `${porcentaje}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Historial Detallado de Mermas */}
      <div className="mt-6">
        <h2 className="text-sm font-bold text-white tracking-wide flex items-center gap-2 mb-3">
          <Calendar className="w-4 h-4 text-neutral-400" />
          Últimos Descartes Registrados
        </h2>

        <div className="space-y-2.5">
          {loading ? (
            <div className="space-y-2">
              {[1, 2].map((n) => (
                <div
                  key={n}
                  className="h-16 rounded-2xl bg-neutral-900/60 border border-neutral-800 animate-pulse"
                />
              ))}
            </div>
          ) : mermas.length > 0 ? (
            mermas.slice(0, 8).map((m) => (
              <div
                key={m.id}
                className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 flex items-center justify-between text-xs"
              >
                <div>
                  <h4 className="font-bold text-white text-sm">
                    {m.insumo?.nombre || "Insumo"}
                  </h4>
                  <p className="text-neutral-400 mt-0.5">
                    {m.cantidad} {m.unidadMedida} • {getMotivoLabel(m.motivo)}
                    {m.notas ? ` (${m.notas})` : ""}
                  </p>
                  <p className="text-[10px] text-neutral-500">
                    {new Date(m.fecha).toLocaleDateString("es-AR", {
                      day: "2-digit",
                      month: "short",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </p>
                </div>
                <span className="font-black text-rose-400 text-sm">
                  - {formatCurrency(m.costoPerdido, moneda)}
                </span>
              </div>
            ))
          ) : (
            <div className="p-6 text-center rounded-2xl bg-neutral-900/50 border border-neutral-800">
              <p className="text-xs text-neutral-400">
                No hay mermas registradas este mes. ¡Excelente control!
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Modal de Configuración */}
      {showConfigModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-sm bg-neutral-900 border border-neutral-800 rounded-3xl p-5 shadow-2xl">
            <h3 className="text-base font-bold text-white mb-1">
              Configuración de Moneda y Negocio
            </h3>
            <p className="text-xs text-neutral-400 mb-4">
              Personaliza el símbolo monetario y nombre de tu cocina
            </p>

            <form onSubmit={handleSaveConfig} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-neutral-300 mb-1">
                  Nombre del Restaurante / Cocina:
                </label>
                <input
                  type="text"
                  value={tempNombre}
                  onChange={(e) => setTempNombre(e.target.value)}
                  className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-neutral-300 mb-1">
                  Símbolo de Moneda:
                </label>
                <div className="grid grid-cols-4 gap-2 mb-2">
                  {["$", "USD", "€", "MXN"].map((sym) => (
                    <button
                      key={sym}
                      type="button"
                      onClick={() => setTempMoneda(sym)}
                      className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                        tempMoneda === sym
                          ? "bg-blue-600 text-white border-blue-500"
                          : "bg-neutral-800 text-neutral-300 border-neutral-700"
                      }`}
                    >
                      {sym}
                    </button>
                  ))}
                </div>
                <input
                  type="text"
                  placeholder="Otro símbolo..."
                  value={tempMoneda}
                  onChange={(e) => setTempMoneda(e.target.value)}
                  className="w-full h-11 bg-neutral-800 border border-neutral-700 rounded-xl px-3 text-sm text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowConfigModal(false)}
                  className="flex-1 py-3 rounded-xl bg-neutral-800 hover:bg-neutral-750 text-neutral-300 text-xs font-bold"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="flex-1 py-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold shadow-md shadow-blue-600/30 flex items-center justify-center gap-1.5"
                >
                  <Save className="w-4 h-4" />
                  <span>Guardar</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
