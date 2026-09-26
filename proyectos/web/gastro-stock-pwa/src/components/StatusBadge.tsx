"use strict";
import React from "react";
import { EstadoStock } from "@/lib/types";
import { AlertCircle, AlertTriangle, CheckCircle, Clock } from "lucide-react";

interface StatusBadgeProps {
  estado: EstadoStock;
  vencimiento?: {
    isExpiring: boolean;
    isExpired: boolean;
    text: string;
  };
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  estado,
  vencimiento,
}) => {
  return (
    <div className="flex flex-wrap items-center gap-1.5">
      {/* Badge de Stock */}
      {estado === "OPTIMO" && (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-950/80 text-emerald-400 border border-emerald-500/40">
          <CheckCircle className="w-3.5 h-3.5" />
          Óptimo
        </span>
      )}

      {estado === "BAJO_STOCK" && (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-950/80 text-amber-400 border border-amber-500/50 animate-pulse">
          <AlertTriangle className="w-3.5 h-3.5" />
          Bajo Stock
        </span>
      )}

      {estado === "AGOTADO" && (
        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-950/90 text-rose-300 border border-rose-500/60 animate-pulse">
          <AlertCircle className="w-3.5 h-3.5" />
          Agotado
        </span>
      )}

      {/* Badge de Vencimiento */}
      {vencimiento && vencimiento.isExpiring && (
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold border ${
            vencimiento.isExpired
              ? "bg-red-950 text-red-300 border-red-500"
              : "bg-purple-950/80 text-purple-300 border-purple-500/50"
          }`}
        >
          <Clock className="w-3 h-3" />
          {vencimiento.text}
        </span>
      )}
    </div>
  );
};
