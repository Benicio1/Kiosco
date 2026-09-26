"use client";

import React, { useState, useEffect } from "react";
import { Header } from "@/components/Header";
import { InventoryCard } from "@/components/InventoryCard";
import { MermaModal } from "@/components/MermaModal";
import { AddInsumoModal } from "@/components/AddInsumoModal";
import { InsumoDTO } from "@/lib/types";
import { getEstadoStock, getEstadoVencimiento } from "@/lib/stock-utils";
import { Search, Plus, Filter, AlertTriangle } from "lucide-react";

export default function InventoryPage() {
  const [insumos, setInsumos] = useState<InsumoDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>("");
  const [selectedCategoria, setSelectedCategoria] = useState<string>("TODOS");
  const [filterMode, setFilterMode] = useState<"TODOS" | "CRITICOS" | "POR_VENCER">("TODOS");
  const [selectedMermaInsumo, setSelectedMermaInsumo] = useState<InsumoDTO | null>(null);
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [currencySymbol, setCurrencySymbol] = useState<string>("$");

  const categorias = [
    "TODOS",
    "Carnes",
    "Verduras",
    "Secos",
    "Lácteos",
    "Bebidas",
    "Descartables",
  ];

  const fetchInsumos = async () => {
    try {
      setLoading(true);
      const res = await fetch("/api/insumos");
      if (res.ok) {
        const data = await res.json();
        setInsumos(data);
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
    fetchInsumos();
  }, []);

  // Manejadores táctiles de incremento/decremento inmediato
  const handleIncrement = async (id: string, delta: number) => {
    // Optimistic UI update
    setInsumos((prev) =>
      prev.map((i) =>
        i.id === id
          ? { ...i, stockActual: Number((i.stockActual + delta).toFixed(2)) }
          : i
      )
    );

    try {
      await fetch("/api/insumos", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id, deltaStock: delta }),
      });
    } catch (err) {
      console.error("Error al actualizar stock", err);
      fetchInsumos();
    }
  };

  const handleDecrement = async (id: string, delta: number) => {
    setInsumos((prev) =>
      prev.map((i) =>
        i.id === id
          ? {
              ...i,
              stockActual: Math.max(0, Number((i.stockActual - delta).toFixed(2))),
            }
          : i
      )
    );

    try {
      await fetch("/api/insumos", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id, deltaStock: -delta }),
      });
    } catch (err) {
      console.error("Error al descontar stock", err);
      fetchInsumos();
    }
  };

  // Filtrado reactivo
  const filteredInsumos = insumos.filter((item) => {
    const matchSearch =
      item.nombre.toLowerCase().includes(search.toLowerCase()) ||
      item.categoria.toLowerCase().includes(search.toLowerCase());

    const matchCategoria =
      selectedCategoria === "TODOS" || item.categoria === selectedCategoria;

    let matchFilterMode = true;
    if (filterMode === "CRITICOS") {
      matchFilterMode = item.stockActual <= item.stockMinimo;
    } else if (filterMode === "POR_VENCER") {
      const v = getEstadoVencimiento(item.fechaVencimiento);
      matchFilterMode = v.isExpiring;
    }

    return matchSearch && matchCategoria && matchFilterMode;
  });

  return (
    <div className="min-h-screen bg-[#0b0f19] px-4 pt-3 pb-24">
      <Header
        title="Control de Stock"
        subtitle={`${insumos.length} artículos en inventario`}
      />

      {/* Buscador táctil */}
      <div className="mt-4 relative">
        <input
          type="text"
          placeholder="Buscar insumo (ej: Tomate, Carne)..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full h-12 bg-neutral-900 border border-neutral-800 rounded-2xl pl-11 pr-4 text-sm text-white placeholder-neutral-500 focus:outline-none focus:border-blue-500"
        />
        <Search className="w-5 h-5 text-neutral-500 absolute left-3.5 top-3.5" />
      </div>

      {/* Selector de Modos de Vista: Todos / Críticos / Por Vencer */}
      <div className="grid grid-cols-3 gap-1.5 mt-3">
        <button
          type="button"
          onClick={() => setFilterMode("TODOS")}
          className={`py-2 rounded-xl text-xs font-bold border transition-all ${
            filterMode === "TODOS"
              ? "bg-blue-600 text-white border-blue-500 shadow-md shadow-blue-600/30"
              : "bg-neutral-900 text-neutral-400 border-neutral-800 hover:text-white"
          }`}
        >
          Todos ({insumos.length})
        </button>

        <button
          type="button"
          onClick={() => setFilterMode("CRITICOS")}
          className={`py-2 rounded-xl text-xs font-bold border transition-all ${
            filterMode === "CRITICOS"
              ? "bg-rose-600 text-white border-rose-500 shadow-md shadow-rose-600/30"
              : "bg-neutral-900 text-neutral-400 border-neutral-800 hover:text-rose-400"
          }`}
        >
          Críticos ({insumos.filter((i) => i.stockActual <= i.stockMinimo).length})
        </button>

        <button
          type="button"
          onClick={() => setFilterMode("POR_VENCER")}
          className={`py-2 rounded-xl text-xs font-bold border transition-all ${
            filterMode === "POR_VENCER"
              ? "bg-purple-600 text-white border-purple-500 shadow-md shadow-purple-600/30"
              : "bg-neutral-900 text-neutral-400 border-neutral-800 hover:text-purple-400"
          }`}
        >
          Por Vencer
        </button>
      </div>

      {/* Filtros horizontales por categoría con scroll táctil suave */}
      <div className="flex items-center gap-1.5 overflow-x-auto py-3 no-scrollbar select-none">
        {categorias.map((cat) => (
          <button
            key={cat}
            type="button"
            onClick={() => setSelectedCategoria(cat)}
            className={`px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap border transition-all active:scale-95 ${
              selectedCategoria === cat
                ? "bg-white text-neutral-950 border-white"
                : "bg-neutral-900 text-neutral-400 border-neutral-800 hover:border-neutral-700"
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Lista de Tarjetas de Inventario */}
      <div className="space-y-3 mt-1">
        {loading ? (
          <div className="space-y-3">
            {[1, 2, 3, 4].map((n) => (
              <div
                key={n}
                className="h-36 rounded-2xl bg-neutral-900/60 animate-pulse border border-neutral-800"
              />
            ))}
          </div>
        ) : filteredInsumos.length > 0 ? (
          filteredInsumos.map((item) => (
            <InventoryCard
              key={item.id}
              insumo={item}
              currencySymbol={currencySymbol}
              onIncrement={handleIncrement}
              onDecrement={handleDecrement}
              onOpenMerma={(ins) => setSelectedMermaInsumo(ins)}
            />
          ))
        ) : (
          <div className="p-8 text-center rounded-2xl bg-neutral-900/50 border border-neutral-800">
            <p className="text-neutral-400 text-sm font-semibold">
              No se encontraron insumos
            </p>
            <p className="text-neutral-500 text-xs mt-1">
              Prueba cambiando los filtros o agrega un nuevo producto.
            </p>
          </div>
        )}
      </div>

      {/* Botón Flotante para Agregar Insumo (Fácil con pulgar) */}
      <button
        type="button"
        onClick={() => setShowAddModal(true)}
        aria-label="Agregar insumo"
        className="fixed bottom-20 right-4 z-30 w-14 h-14 rounded-full bg-blue-600 hover:bg-blue-500 text-white flex items-center justify-center shadow-xl shadow-blue-600/40 active:scale-90 transition-all"
      >
        <Plus className="w-7 h-7 font-bold" />
      </button>

      {/* Modales */}
      <MermaModal
        isOpen={!!selectedMermaInsumo}
        insumo={selectedMermaInsumo}
        currencySymbol={currencySymbol}
        onClose={() => setSelectedMermaInsumo(null)}
        onSuccess={fetchInsumos}
      />

      <AddInsumoModal
        isOpen={showAddModal}
        currencySymbol={currencySymbol}
        onClose={() => setShowAddModal(false)}
        onSuccess={fetchInsumos}
      />
    </div>
  );
}
