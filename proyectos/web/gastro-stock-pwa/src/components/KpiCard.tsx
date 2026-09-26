"use strict";
import React from "react";
import { LucideIcon } from "lucide-react";

interface KpiCardProps {
  title: string;
  value: string;
  subtitle?: string;
  icon: LucideIcon;
  variant?: "default" | "warning" | "danger" | "success";
  onClick?: () => void;
}

export const KpiCard: React.FC<KpiCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon,
  variant = "default",
  onClick,
}) => {
  const variantStyles = {
    default: "border-neutral-800 bg-neutral-900/90 text-neutral-100",
    warning:
      "border-amber-500/40 bg-amber-950/20 text-amber-300 ring-1 ring-amber-500/20",
    danger:
      "border-rose-500/40 bg-rose-950/20 text-rose-300 ring-1 ring-rose-500/20",
    success:
      "border-emerald-500/40 bg-emerald-950/20 text-emerald-300 ring-1 ring-emerald-500/20",
  };

  const iconColors = {
    default: "text-blue-400 bg-blue-500/10",
    warning: "text-amber-400 bg-amber-500/20",
    danger: "text-rose-400 bg-rose-500/20",
    success: "text-emerald-400 bg-emerald-500/20",
  };

  return (
    <div
      onClick={onClick}
      className={`p-4 rounded-2xl border shadow-lg transition-all ${
        onClick ? "cursor-pointer active:scale-98 hover:border-neutral-700" : ""
      } ${variantStyles[variant]}`}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold text-neutral-400 uppercase tracking-wider">
            {title}
          </p>
          <p className="text-2xl font-black mt-1 tracking-tight text-white">
            {value}
          </p>
          {subtitle && (
            <p className="text-xs text-neutral-400 mt-1 font-medium">
              {subtitle}
            </p>
          )}
        </div>
        <div className={`p-2.5 rounded-xl ${iconColors[variant]}`}>
          <Icon className="w-5 h-5" />
        </div>
      </div>
    </div>
  );
};
