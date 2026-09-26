"use client";

import React, { useState, useEffect } from "react";
import { Header } from "@/components/Header";
import { BuyItemModal } from "@/components/BuyItemModal";
import { AddCompraModal } from "@/components/AddCompraModal";
import { ItemCompraDTO, PrioridadCompra } from "@/lib/types";
import { formatCurrency, formatStockQuantity } from "@/lib/formatters";
import {
  ShoppingCart,
  Plus,
  RefreshCw,
  Check,
  CheckCircle2,
  Trash2,
  AlertCircle,
  Tag,
  Clock,
} from "lucide-react";

export default function ShoppingListPage() {
  const [items, setItems] = useState<ItemCompraDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [syncing, setSyncing] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<"pendientes" | "comprados">("pendientes");
  const [selectedBuyItem, setSelectedBuyItem] = useState<ItemCompraDTO | null>(null);
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [currencySymbol, setCurrencySymbol] = useState<string>("$");

  const fetchCompras = async () => {
    try {
      setLoading(true);
      const res = await fetch("/api/compras");
      if (res.ok) {
        const data = await res.json();
        setItems(data.items);
      }

      const confRes = await fetch("/api/config");
      if (confRes.ok) {
        const conf = await confRes.json();
        if (conf.moneda) setCurrencySymbol(conf.moneda);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCompras();
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
        await fetchCompras();
      }
    } catch (e) {
      console.error(e);
    } finally {
      setSyncing(false);
    }
  };

  const handleConfirmBuy = async (
    id: string,
    cantidadComprada: number,
    costoReal: number
  ) => {
    const res = await fetch("/api/compras", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id,
        comprado: true,
        cantidadComprada,
        costoReal,
      }),
    });

    if (res.ok) {
      await fetchCompras();
    }
  };

  const handleDeleteItem = async (id: string) => {
    if (!confirm("¿Eliminar este ítem de la lista de compras?")) return;
    try {
      const res = await fetch(`/api/compras?id=${id}`, {
        method: "DELETE",
      });
      if (res.ok) {
        setItems((prev) => prev.filter((i) => i.id !== id));
      }
    } catch (e) {
      console.error(e);
    }
  };

  const pendientes = items.filter((i) => !i.comprado);
  const comprados = items.filter((i) => i.comprado);

  const totalPresupuestoPendiente = pendientes.reduce(
    (acc, i) => acc + i.cantidadSugerida * i.costoEstimado,
    0
  );

  const totalGastadoComprados = comprados.reduce(
    (acc, i) => acc + (i.cantidadComprada || i.cantidadSugerida) * (i.costoReal || i.costoEstimado),
    0
  );

  const getPriorityBadge = (prioridad: PrioridadCompra) => {
    switch (prioridad) {
      case "ALTA":
        return (
          <span className="px-2 py-0.5 rounded text-[10px] font-black bg-rose-950/80 text-rose-300 border border-rose-600/40">
            ALTA
          </span>
        );
      case "MEDIA":
        return (
          <span className="px-2 py-0.5 rounded text-[10px] font-black bg-amber-950/80 text-amber-300 border border-amber-600/40">
            MEDIA
          </span>
        );
      case "BAJA":
        return (
          <span className="px-2 py-0.5 rounded text-[10px] font-black bg-blue-950/80 text-blue-300 border border-blue-600/40">
            BAJA
          </span>
        );
    }
  };

  return (
    <div className="min-h-screen bg-[#0b0f19] px-4 pt-3 pb-24">
      <Header
        title="Lista de Compras"
        subtitle="Abastecimiento para Cocina"
      />

      {/* Banner de Presupuesto Proyectado Antes de Salir */}
      <div className="mt-4 p-4 rounded-2xl bg-gradient-to-br from-neutral-900 to-neutral-950 border border-neutral-800 shadow-xl">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-neutral-400 uppercase tracking-wider">
              {activeTab === "pendientes" ? "Presupuesto Proyectado" : "Total Comprado"}
            </p>
            <p className="text-2xl font-black text-white mt-1">
              {formatCurrency(
                activeTab === "pendientes" ? totalPresupuestoPendiente : totalGastadoComprados,
                currencySymbol
              )}
            </p>
            <p className="text-[11px] text-neutral-400 mt-0.5">
              {activeTab === "pendientes"
                ? `${pendientes.length} artículos por comprar`
                : `${comprados.length} compras completadas`}
            </p>
          </div>

          <button
            type="button"
            onClick={handleSyncFaltantes}
            disabled={syncing}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-lg shadow-blue-600/30 active:scale-95 disabled:opacity-50 transition-all"
          >
            <RefreshCw className={`w-5 h-5 ${syncing ? "animate-spin" : ""}`} />
            <span className="text-[10px] mt-1">Sugerir</span>
          </button>
        </div>
      </div>

      {/* Selector de Pestañas Pendientes / Comprados */}
      <div className="grid grid-cols-2 gap-2 mt-4">
        <button
          type="button"
          onClick={() => setActiveTab("pendientes")}
          className={`py-2.5 rounded-xl text-xs font-bold border transition-all ${
            activeTab === "pendientes"
              ? "bg-blue-600 text-white border-blue-500 shadow-md shadow-blue-600/30"
              : "bg-neutral-900 text-neutral-400 border-neutral-800"
          }`}
        >
          Pendientes ({pendientes.length})
        </button>

        <button
          type="button"
          onClick={() => setActiveTab("comprados")}
          className={`py-2.5 rounded-xl text-xs font-bold border transition-all ${
            activeTab === "comprados"
              ? "bg-emerald-600 text-white border-emerald-500 shadow-md shadow-emerald-600/30"
              : "bg-neutral-900 text-neutral-400 border-neutral-800"
          }`}
        >
          Comprados ({comprados.length})
        </button>
      </div>

      {/* Lista de Ítems de Compra */}
      <div className="mt-4 space-y-2.5">
        {loading ? (
          <div className="space-y-2.5">
            {[1, 2, 3].map((n) => (
              <div
                key={n}
                className="h-24 rounded-2xl bg-neutral-900/60 border border-neutral-800 animate-pulse"
              />
            ))}
          </div>
        ) : (activeTab === "pendientes" ? pendientes : comprados).length > 0 ? (
          (activeTab === "pendientes" ? pendientes : comprados).map((item) => (
            <div
              key={item.id}
              className={`p-3.5 rounded-2xl border transition-all ${
                item.comprado
                  ? "bg-neutral-900/60 border-neutral-800/80 opacity-80"
                  : "bg-neutral-900 border-neutral-800 hover:border-neutral-700 shadow-md"
              }`}
            >
              <div className="flex items-center justify-between gap-3">
                {/* Botón táctil gigante para check de compra dinámico */}
                {!item.comprado ? (
                  <button
                    type="button"
                    onClick={() => setSelectedBuyItem(item)}
                    aria-label="Marcar como comprado"
                    className="w-12 h-12 rounded-2xl bg-neutral-800 hover:bg-emerald-950/60 border border-neutral-700 hover:border-emerald-500 flex items-center justify-center text-neutral-400 hover:text-emerald-400 active:scale-90 transition-all shrink-0"
                  >
                    <Check className="w-6 h-6 stroke-[3]" />
                  </button>
                ) : (
                  <div className="w-10 h-10 rounded-xl bg-emerald-950/80 border border-emerald-500/40 flex items-center justify-center text-emerald-400 shrink-0">
                    <CheckCircle2 className="w-5 h-5" />
                  </div>
                )}

                {/* Contenido del Ítem */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5 flex-wrap">
                    <span className="text-[10px] uppercase font-bold text-neutral-400 bg-neutral-800 px-1.5 py-0.5 rounded">
                      {item.categoria}
                    </span>
                    {getPriorityBadge(item.prioridad)}
                  </div>

                  <h3
                    className={`text-sm font-bold mt-1 truncate ${
                      item.comprado ? "text-neutral-400 line-through" : "text-white"
                    }`}
                  >
                    {item.nombre}
                  </h3>

                  <div className="flex items-center justify-between mt-1 text-xs text-neutral-400">
                    <span>
                      Cant:{" "}
                      <strong className="text-white font-bold">
                        {item.comprado
                          ? formatStockQuantity(item.cantidadComprada || item.cantidadSugerida, item.unidadMedida)
                          : formatStockQuantity(item.cantidadSugerida, item.unidadMedida)}
                      </strong>
                    </span>
                    <span>
                      Est:{" "}
                      <strong className="text-neutral-200">
                        {formatCurrency(
                          item.comprado
                            ? (item.cantidadComprada || item.cantidadSugerida) * (item.costoReal || item.costoEstimado)
                            : item.cantidadSugerida * item.costoEstimado,
                          currencySymbol
                        )}
                      </strong>
                    </span>
                  </div>
                </div>

                {/* Botón de eliminar */}
                <button
                  type="button"
                  onClick={() => handleDeleteItem(item.id)}
                  aria-label="Eliminar ítem"
                  className="p-2 text-neutral-500 hover:text-rose-400 active:scale-90 transition-all"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="p-8 text-center rounded-2xl bg-neutral-900/50 border border-neutral-800">
            <p className="text-sm font-bold text-neutral-300">
              {activeTab === "pendientes"
                ? "No hay compras pendientes"
                : "No hay compras registradas"}
            </p>
            <p className="text-xs text-neutral-500 mt-1">
              Usa el botón &quot;Sugerir&quot; para sincronizar faltantes o agrega ítems manualmente.
            </p>
          </div>
        )}
      </div>

      {/* Botón Flotante para Carga Manual */}
      <button
        type="button"
        onClick={() => setShowAddModal(true)}
        aria-label="Agregar compra manual"
        className="fixed bottom-20 right-4 z-30 w-14 h-14 rounded-full bg-blue-600 hover:bg-blue-500 text-white flex items-center justify-center shadow-xl shadow-blue-600/40 active:scale-90 transition-all"
      >
        <Plus className="w-7 h-7 font-bold" />
      </button>

      {/* Modal de confirmación de compra y sync a inventario */}
      <BuyItemModal
        isOpen={!!selectedBuyItem}
        item={selectedBuyItem}
        currencySymbol={currencySymbol}
        onClose={() => setSelectedBuyItem(null)}
        onConfirm={handleConfirmBuy}
      />

      {/* Modal de alta manual de compras */}
      <AddCompraModal
        isOpen={showAddModal}
        currencySymbol={currencySymbol}
        onClose={() => setShowAddModal(false)}
        onSuccess={fetchCompras}
      />
    </div>
  );
}
