import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        kitchen: {
          dark: "#0b0f19",
          card: "#111827",
          border: "#1f2937",
          hover: "#1e293b",
          optimo: "#10b981",
          bajo: "#f59e0b",
          agotado: "#ef4444",
          vence: "#a855f7",
          accent: "#3b82f6",
        },
      },
      minHeight: {
        touch: "48px",
      },
      minWidth: {
        touch: "48px",
      },
    },
  },
  plugins: [],
};
export default config;
