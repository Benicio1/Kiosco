"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, Boxes, ShoppingCart, DollarSign } from "lucide-react";

export const BottomNav: React.FC = () => {
  const pathname = usePathname();

  const navItems = [
    {
      label: "Inicio",
      href: "/",
      icon: LayoutDashboard,
      active: pathname === "/",
    },
    {
      label: "Stock",
      href: "/inventory",
      icon: Boxes,
      active: pathname.startsWith("/inventory"),
    },
    {
      label: "Compras",
      href: "/shopping-list",
      icon: ShoppingCart,
      active: pathname.startsWith("/shopping-list"),
    },
    {
      label: "Costos",
      href: "/costs",
      icon: DollarSign,
      active: pathname.startsWith("/costs"),
    },
  ];

  return (
    <nav
      aria-label="Navegación principal inferior"
      className="fixed bottom-0 left-0 right-0 z-40 bg-neutral-900/95 backdrop-blur-md border-t border-neutral-800 pb-safe"
    >
      <div className="max-w-md mx-auto grid grid-cols-4 h-16 items-center px-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex flex-col items-center justify-center h-full min-h-[48px] rounded-xl transition-all duration-150 active:scale-95 select-none ${
                item.active
                  ? "text-blue-400 font-bold"
                  : "text-neutral-400 hover:text-neutral-200"
              }`}
            >
              <div
                className={`p-1.5 rounded-xl transition-colors ${
                  item.active
                    ? "bg-blue-500/20 text-blue-400"
                    : "bg-transparent text-neutral-400"
                }`}
              >
                <Icon className="w-5 h-5" strokeWidth={item.active ? 2.5 : 2} />
              </div>
              <span className="text-[11px] mt-0.5 tracking-tight font-medium">
                {item.label}
              </span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
};
