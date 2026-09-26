"use client";

import React, { useState, useEffect } from "react";
import { Header } from "@/components/Header";
import { KpiCard } from "@/components/KpiCard";
import { StatusBadge } from "@/components/StatusBadge";
import { MermaModal } from "@/components/MermaModal";
import { formatCurrency, formatStockQuantity } from "@/lib/formatters";
import { getEstadoStock, getEstadoVencimiento } from "@/lib/stock-utils";
import { InsumoDTO, DashboardKPIs } from "@/lib/types";
import {
  Boxes,
  ShoppingCart,
  AlertTriangle,
  Clock,
  ArrowRight,
  Plus,
  RefreshCw,
  Search,
  Sparkles,
} from "lucide-react";
import Link from "next/link";

export default function DashboardPage() {
  const [data, setData] = useState<{
    kpis: DashboardKPIs;
    criticos: InsumoDTO[];
    porVencer: InsumoDTO[];
    config: { moneda: string; nombreNegocio: string };
  } | null>(null);

  const [loading, setLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [selectedMermaInsumo, setSelectedMermaInsumo] = useState<InsumoDTO | null>(null);
  const [syncing, setSyncing] = useState<boolean>(false);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const res = await fetch("/api/dashboard");
      if (res.ok) {
        const json = await res.json();
        setData(json);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const handleSyncFaltantes = async () => {
    try {
      setSyncing(true);
      const res = await fetch("/api/compras", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ action: "SYNC_FALTANTES" }),
      });
      if (res.ok) {
        await loadDashboard();
      }
    } catch (e) {
      console.error(e);
    } finally {
      setSyncing(false);
    }
  };

  const moneda = data?.config?.moneda || "$";

  return (
    <div className="min-h-screen bg-[#0b0f19] px-4 pt-3 pb-8">
      <Header
        title={data?.config?.nombreNegocio || "Cocina & Stock"}
        subtitle="Panel Operativo en Vivo"
      />

      {/* Buscador de acceso rápido */}
      <div className="mt-4 relative">
        <input
          type="text"
          placeholder="Buscar insumo o ingrediente..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full h-12 bg-neutral-900 border border-neutral-800 rounded-2xl pl-11 pr-4 text-sm text-white placeholder-neutral-500 focus:outline-none focus:border-blue-500 shadow-inner"
        />
        <Search className="w-5 h-5 text-neutral-500 absolute left-3.5 top-3.5" />
      </div>

      {/* Tarjetas KPI */}
      <div className="grid grid-cols-2 gap-3 mt-4">
        <KpiCard
          title="Stock Inmovilizado"
          value={formatCurrency(data?.kpis?.valorInventario || 0, moneda)}
          subtitle={`${data?.kpis?.totalInsumos || 0} insumos`}
          icon={Boxes}
          variant="default"
        />

        <Link href="/shopping-list">
          <KpiCard
            title="Presupuesto Compras"
            value={formatCurrency(data?.kpis?.presupuestoPendiente || 0, moneda)}
            subtitle="Pendiente de compra"
            icon={ShoppingCart}
            variant="warning"
          />
        </Link>

        <Link href="/inventory?filtro=criticos">
          <KpiCard
            title="Stock Crítico"
            value={(data?.kpis?.articulosCriticos || 0).toString()}
            subtitle="Agotados o bajo mín."
            icon={AlertTriangle}
            variant={
              (data?.kpis?.articulosCriticos || 0) > 0 ? "danger" : "success"
            }
          />
        </Link>

        <KpiCard
          title="Por Vencer"
          value={(data?.kpis?.articulosPorVencer || 0).toString()}
          subtitle="En menos de 3 días"
          icon={Clock}
          variant={
            (data?.kpis?.articulosPorVencer || 0) > 0 ? "warning" : "default"
          }
        />
      </div>

      {/* Acciones Rápidas con un toque */}
      <div className="mt-5 p-4 rounded-2xl bg-neutral-900 border border-neutral-800 flex items-center justify-between gap-3">
        <div>
          <p className="text-xs font-bold text-white flex items-center gap-1.5">
            <Sparkles className="w-4 h-4 text-amber-400" />
            Reposición Inteligente
          </p>
          <p className="text-[11px] text-neutral-400 mt-0.5">
            Agrega faltantes a la lista de compras
          </p>
        </div>

        <button
          type="button"
          onClick={handleSyncFaltantes}
          disabled={syncing}
          className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold shadow-md shadow-blue-600/30 active:scale-95 disabled:opacity-50 transition-all shrink-0"
        >
          <RefreshCw
            className={`w-3.5 h-3.5 ${syncing ? "animate-spin" : ""}`}
          />
          <span>{syncing ? "Sincronizando..." : "Sincronizar"}</span>
        </button>
      </div>

      {/* Sección 1: Insumos Críticos que requieren atención */}
      <div className="mt-6">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-sm font-bold text-white tracking-wide flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-rose-500" />
            Insumos Críticos
          </h2>
          <Link
            href="/inventory"
            className="text-xs text-blue-400 hover:text-blue-300 font-semibold flex items-center gap-1"
          >
            Ver todos
            <ArrowRight className="w-3 h-3" />
          </Link>
        </div>

        {loading ? (
          <div className="space-y-2">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="h-16 rounded-2xl bg-neutral-900/60 animate-pulse"
              />
            ))}
          </div>
        ) : data?.criticos && data.criticos.length > 0 ? (
          <div className="space-y-2.5">
            {data.criticos
              .filter((item) =>
                item.nombre.toLowerCase().includes(searchQuery.toLowerCase())
              )
              .map((item) => {
                const estado = getEstadoStock(
                  item.stockActual,
                  item.stockMinimo
                );
                return (
                  <div
                    key={item.id}
                    className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 flex items-center justify-between gap-3 shadow-md"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] uppercase font-bold text-neutral-400 bg-neutral-800 px-1.5 py-0.5 rounded">
                          {item.categoria}
                        </span>
                        <h4 className="text-sm font-bold text-white truncate">
                          {item.nombre}
                        </h4>
                      </div>
                      <p className="text-xs text-neutral-400 mt-1">
                        Stock:{" "}
                        <span className="font-bold text-white">
                          {formatStockQuantity(
                            item.stockActual,
                            item.unidadMedida
                          )}
                        </span>{" "}
                        (Mín: {item.stockMinimo} {item.unidadMedida})
                      </p>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <StatusBadge estado={estado} />
                      <Link
                        href={`/shopping-list`}
                        className="p-2.5 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-200 active:scale-95 transition-all"
                        title="Ir a lista de compras"
                      >
                        <ShoppingCart className="w-4 h-4 text-blue-400" />
                      </Link>
                    </div>
                  </div>
                );
              })}
          </div>
        ) : (
          <div className="p-6 rounded-2xl bg-neutral-900/60 border border-neutral-800 text-center">
            <p className="text-sm font-semibold text-emerald-400">
              ✓ No hay insumos en estado crítico
            </p>
            <p className="text-xs text-neutral-400 mt-1">
              Todos los stocks se encuentran sobre el nivel mínimo.
            </p>
          </div>
        )}
      </div>

      {/* Sección 2: Próximos a Vencer */}
      <div className="mt-6">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-sm font-bold text-white tracking-wide flex items-center gap-2">
            <Clock className="w-4 h-4 text-purple-400" />
            Alerta de Vencimiento (&lt; 3 días)
          </h2>
        </div>

        {data?.porVencer && data.porVencer.length > 0 ? (
          <div className="space-y-2.5">
            {data.porVencer.map((item) => {
              const venc = getEstadoVencimiento(item.fechaVencimiento);
              return (
                <div
                  key={item.id}
                  className="p-3.5 rounded-2xl bg-purple-950/20 border border-purple-800/40 flex items-center justify-between gap-3"
                >
                  <div>
                    <h4 className="text-sm font-bold text-white">
                      {item.nombre}
                    </h4>
                    <p className="text-xs text-purple-300 font-semibold mt-0.5">
                      {venc.text} • Disponible: {item.stockActual}{" "}
                      {item.unidadMedida}
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={() => setSelectedMermaInsumo(item)}
                    className="px-3 py-1.5 rounded-xl bg-purple-900/50 hover:bg-purple-800 text-purple-200 text-xs font-bold border border-purple-700/50 active:scale-95 transition-all shrink-0"
                  >
                    Descartar Merma
                  </button>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="p-4 rounded-2xl bg-neutral-900/50 border border-neutral-800 text-center">
            <p className="text-xs text-neutral-400">
              No hay productos con vencimiento en los próximos 3 días.
            </p>
          </div>
        )}
      </div>

      {/* Modal de merma rápido */}
      <MermaModal
        isOpen={!!selectedMermaInsumo}
        insumo={selectedMermaInsumo}
        currencySymbol={moneda}
        onClose={() => setSelectedMermaInsumo(null)}
        onSuccess={loadDashboard}
      />
    </div>
  );
}
