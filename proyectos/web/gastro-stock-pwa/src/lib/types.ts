export type CategoriaInsumo =
  | "Carnes"
  | "Verduras"
  | "Secos"
  | "Lácteos"
  | "Bebidas"
  | "Descartables"
  | string;

export type UnidadMedida = "kg" | "gr" | "l" | "u" | string;

export type EstadoStock = "OPTIMO" | "BAJO_STOCK" | "AGOTADO";

export type PrioridadCompra = "ALTA" | "MEDIA" | "BAJA";

export type MotivoMerma = "VENCIDO" | "ROTO" | "MAL_ESTADO" | "OTRO";

export interface InsumoDTO {
  id: string;
  nombre: string;
  categoria: string;
  stockActual: number;
  stockMinimo: number;
  stockIdeal: number;
  unidadMedida: string;
  costoUnitario: number;
  fechaVencimiento: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ItemCompraDTO {
  id: string;
  insumoId?: string | null;
  insumo?: InsumoDTO | null;
  nombre: string;
  categoria: string;
  cantidadSugerida: number;
  cantidadComprada?: number | null;
  unidadMedida: string;
  costoEstimado: number;
  costoReal?: number | null;
  prioridad: PrioridadCompra;
  comprado: boolean;
  fechaCompra?: string | null;
}

export interface RegistroMermaDTO {
  id: string;
  insumoId: string;
  insumo?: InsumoDTO;
  cantidad: number;
  unidadMedida: string;
  costoPerdido: number;
  motivo: MotivoMerma;
  notas?: string | null;
  fecha: string;
}

export interface DashboardKPIs {
  valorInventario: number;
  presupuestoPendiente: number;
  articulosCriticos: number; // Bajo stock o Agotado
  articulosPorVencer: number; // <= 3 días
  totalInsumos: number;
}
