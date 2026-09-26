import type { Metadata, Viewport } from "next";
import "./globals.css";
import { BottomNav } from "@/components/BottomNav";

export const metadata: Metadata = {
  title: "GastroStock - Control Gastronómico",
  description: "PWA táctil para gestión operativa, stock, compras y costos",
  manifest: "/manifest.json",
  appleWebApp: {
    capable: true,
    statusBarStyle: "black-translucent",
    title: "GastroStock",
  },
};

export const viewport: Viewport = {
  themeColor: "#0b0f19",
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es" className="dark">
      <head>
        <link rel="icon" href="/favicon.ico" sizes="any" />
      </head>
      <body className="bg-[#0b0f19] text-neutral-100 min-h-screen antialiased flex flex-col selection:bg-blue-600 selection:text-white">
        <main className="flex-1 pb-20 max-w-md mx-auto w-full relative">
          {children}
        </main>
        <BottomNav />
      </body>
    </html>
  );
}
