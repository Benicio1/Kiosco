export function formatCurrency(
  amount: number,
  currencySymbol: string = "$"
): string {
  const formatted = new Intl.NumberFormat("es-AR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount || 0);

  return `${currencySymbol} ${formatted}`;
}

export function formatStockQuantity(
  amount: number,
  unit: string = "u"
): string {
  const num = Number(amount || 0);
  const formatted = Number.isInteger(num) ? num.toString() : num.toFixed(2);
  return `${formatted} ${unit}`;
}

export function formatDate(dateString?: string | Date | null): string {
  if (!dateString) return "-";
  const date = new Date(dateString);
  return date.toLocaleDateString("es-AR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}
