using System;
using System.Collections.Generic;

namespace AuraPaint.Models
{
    public enum BrushCategory
    {
        All,            // Todos
        Basic,          // Básico / Simple
        Sketch,         // Bosquejo
        Comic,          // Cómic & Manga
        Inking,         // Tinta
        Airbrush,       // Aerógrafo
        Watercolor,     // Acuarela
        Paint,          // Pintar & Óleo
        Special,        // Efectos Especiales & Tramas
        Erasers         // Borradores
    }

    public class BrushPreset
    {
        public string Id;
        public string Name;
        public BrushCategory Category;
        public string Description;
        public float Size;
        public float Hardness;
        public float DefaultFlow;
        public float Spacing;
        public float MinSizeFactor;
        public float Gamma;
        public bool PressureAffectsOpacity;
        public bool HasGrain;
        public bool IsEraser;
        public bool IsChisel;
        public bool HasWetEdge;
        public bool IsSplatter;
        public bool IsHalftone;

        public static List<BrushPreset> GetAllPresets()
        {
            var list = new List<BrushPreset>();

            // =================================================================
            // 1. BÁSICO / PLUMAS & ROTULADORES (12)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "pen_soft", Name = "Pluma (Suave)", Category = BrushCategory.Basic, Size = 16.0f,
                Description = "Pluma cónica estándar con extremos suaves y flujo continuo.",
                Hardness = 0.85f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "pen_hard", Name = "Pluma (Fuerte)", Category = BrushCategory.Basic, Size = 28.0f,
                Description = "Pluma de entintado nítida con 100% de opacidad y contorno definido.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.10f, Gamma = 1.3f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "pen_blur", Name = "Pluma (Difuminado)", Category = BrushCategory.Basic, Size = 10.0f,
                Description = "Plumilla fina con bordes antialiased suaves.",
                Hardness = 0.70f, DefaultFlow = 0.90f, Spacing = 0.08f, MinSizeFactor = 0.25f, Gamma = 1.1f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "digital_pen", Name = "Pluma Digital", Category = BrushCategory.Basic, Size = 6.0f,
                Description = "Línea fina ultra nítida y uniforme para dibujo de precisión.",
                Hardness = 1.0f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "mapping_pen", Name = "Pluma de Mapeo", Category = BrushCategory.Basic, Size = 4.5f,
                Description = "Punta de extrema finura para detalles y líneas técnicas.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.05f, MinSizeFactor = 0.15f, Gamma = 1.4f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "caligraphy_pen", Name = "Pluma Caligráfica", Category = BrushCategory.Basic, Size = 22.0f,
                Description = "Plumilla biselada con ángulo constante estilo rotulador caligráfico.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.3f, PressureAffectsOpacity = false, IsChisel = true
            });
            list.Add(new BrushPreset {
                Id = "marker_soft", Name = "Rotulador (Suave)", Category = BrushCategory.Basic, Size = 14.0f,
                Description = "Rotulador redondeado de flujo medio para boceto y color rápido.",
                Hardness = 0.80f, DefaultFlow = 0.70f, Spacing = 0.08f, MinSizeFactor = 0.40f, Gamma = 1.1f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "marker_hard", Name = "Rotulador (Fuerte)", Category = BrushCategory.Basic, Size = 16.0f,
                Description = "Rotulador de alta cobertura con borde cortante.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.07f, MinSizeFactor = 0.35f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "fine_marker", Name = "Rotulador Punta Fina", Category = BrushCategory.Basic, Size = 3.5f,
                Description = "Delineador estilo estilógrafo 0.3mm con espesor parejo.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.70f, Gamma = 1.0f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "highlighter", Name = "Marcador Fluorescente", Category = BrushCategory.Basic, Size = 35.0f,
                Description = "Marcador semitransparente con punta plana biselada.",
                Hardness = 0.85f, DefaultFlow = 0.45f, Spacing = 0.07f, MinSizeFactor = 0.70f, Gamma = 1.0f, PressureAffectsOpacity = false, IsChisel = true
            });
            list.Add(new BrushPreset {
                Id = "flat_brush", Name = "Pincel Plano Básico", Category = BrushCategory.Basic, Size = 40.0f,
                Description = "Pincel ancho para fondos y rellenos directos.",
                Hardness = 0.90f, DefaultFlow = 0.90f, Spacing = 0.09f, MinSizeFactor = 0.30f, Gamma = 1.1f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "round_soft", Name = "Pincel Redondo Suave", Category = BrushCategory.Basic, Size = 32.0f,
                Description = "Puntas suaves y transiciones progresivas.",
                Hardness = 0.60f, DefaultFlow = 0.80f, Spacing = 0.09f, MinSizeFactor = 0.20f, Gamma = 1.1f, PressureAffectsOpacity = true
            });

            // =================================================================
            // 2. BOSQUEJO & GRAFITO (10)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "pencil_hb", Name = "Lápiz de Grafito HB", Category = BrushCategory.Sketch, Size = 10.0f,
                Description = "Sensación clásica de mina de grafito equilibrada con grano de papel.",
                Hardness = 0.65f, DefaultFlow = 0.75f, Spacing = 0.12f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "pencil_2b", Name = "Lápiz Grafito 2B", Category = BrushCategory.Sketch, Size = 15.0f,
                Description = "Mina suave y versátil para bocetos anatómicos rápidos.",
                Hardness = 0.60f, DefaultFlow = 0.80f, Spacing = 0.11f, MinSizeFactor = 0.22f, Gamma = 1.05f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "pencil_4b", Name = "Lápiz Blando 4B", Category = BrushCategory.Sketch, Size = 22.0f,
                Description = "Mina oscura y aterciopelada para contrastes potentes.",
                Hardness = 0.55f, DefaultFlow = 0.85f, Spacing = 0.11f, MinSizeFactor = 0.20f, Gamma = 1.1f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "pencil_6b", Name = "Lápiz Extra Blando 6B", Category = BrushCategory.Sketch, Size = 30.0f,
                Description = "Sombreado denso y textura rica para valores oscuros.",
                Hardness = 0.50f, DefaultFlow = 0.90f, Spacing = 0.10f, MinSizeFactor = 0.18f, Gamma = 1.15f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "mechanical_pencil", Name = "Portaminas 0.5mm", Category = BrushCategory.Sketch, Size = 4.0f,
                Description = "Líneas de boceto finas y limpias sin variación de ángulo.",
                Hardness = 0.85f, DefaultFlow = 0.80f, Spacing = 0.08f, MinSizeFactor = 0.60f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "charcoal_fine", Name = "Carboncillo Fino", Category = BrushCategory.Sketch, Size = 18.0f,
                Description = "Punta de sauce comprimido para encaje y líneas gestuales.",
                Hardness = 0.50f, DefaultFlow = 0.70f, Spacing = 0.13f, MinSizeFactor = 0.20f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "charcoal_rough", Name = "Carboncillo Orgánico", Category = BrushCategory.Sketch, Size = 42.0f,
                Description = "Textura terrosa y porosa para manchas de gran escala.",
                Hardness = 0.40f, DefaultFlow = 0.65f, Spacing = 0.15f, MinSizeFactor = 0.15f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "soft_pastel", Name = "Tiza Pastel Suave", Category = BrushCategory.Sketch, Size = 35.0f,
                Description = "Pigmento seco y pulverulento con mezcla visual de capas.",
                Hardness = 0.45f, DefaultFlow = 0.70f, Spacing = 0.12f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "sanguine", Name = "Sanguina Tradicional", Category = BrushCategory.Sketch, Size = 14.0f,
                Description = "Sensación clásica renacentista en tonos arcillosos.",
                Hardness = 0.65f, DefaultFlow = 0.75f, Spacing = 0.11f, MinSizeFactor = 0.25f, Gamma = 1.05f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "color_pencil", Name = "Lápiz de Color Céreo", Category = BrushCategory.Sketch, Size = 16.0f,
                Description = "Mezcla translúcida de capas con textura granular.",
                Hardness = 0.70f, DefaultFlow = 0.60f, Spacing = 0.10f, MinSizeFactor = 0.35f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });

            // =================================================================
            // 3. CÓMIC & MANGA (8)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "gpen_manga", Name = "Pluma G Manga (G-Pen)", Category = BrushCategory.Comic, Size = 26.0f,
                Description = "Lineart tradicional japonés de alto contraste y 100% opacidad.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.08f, Gamma = 1.35f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "maru_manga", Name = "Pluma Maru Fina", Category = BrushCategory.Comic, Size = 6.0f,
                Description = "Punta ultra afilada para ojos, pestañas y tramas finas de manga.",
                Hardness = 1.0f, DefaultFlow = 1.0f, Spacing = 0.05f, MinSizeFactor = 0.05f, Gamma = 1.5f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "kabura_manga", Name = "Pluma Kabura / Cuchara", Category = BrushCategory.Comic, Size = 18.0f,
                Description = "Trazo firme y controlado con mínima variación de grosor.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.07f, MinSizeFactor = 0.30f, Gamma = 1.1f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "sumie_brush", Name = "Pincel Tinta China (Sumi-e)", Category = BrushCategory.Comic, Size = 36.0f,
                Description = "Trazos orientales fluidos de pincel de pelo natural.",
                Hardness = 0.85f, DefaultFlow = 0.95f, Spacing = 0.08f, MinSizeFactor = 0.06f, Gamma = 1.4f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "oriental_inking", Name = "Pincel de Entintado Oriental", Category = BrushCategory.Comic, Size = 28.0f,
                Description = "Curvas elásticas con puntas afiladas para entintado dinámico.",
                Hardness = 0.92f, DefaultFlow = 1.0f, Spacing = 0.07f, MinSizeFactor = 0.10f, Gamma = 1.3f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "shonen_brush", Name = "Línea Modulada Shonen", Category = BrushCategory.Comic, Size = 34.0f,
                Description = "Líneas de impacto vigorosas con variación extrema de grosor.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.04f, Gamma = 1.45f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "dry_japanese_ink", Name = "Tinta Seca Japonesa", Category = BrushCategory.Comic, Size = 24.0f,
                Description = "Bordes ligeramente rotos y deshilachados estilo entintado tradicional.",
                Hardness = 0.80f, DefaultFlow = 0.90f, Spacing = 0.10f, MinSizeFactor = 0.15f, Gamma = 1.2f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "fountain_pen", Name = "Pluma Estilográfica", Category = BrushCategory.Comic, Size = 12.0f,
                Description = "Flujo uniforme y elegante de tinta líquida.",
                Hardness = 0.92f, DefaultFlow = 0.95f, Spacing = 0.07f, MinSizeFactor = 0.25f, Gamma = 1.15f, PressureAffectsOpacity = false
            });

            // =================================================================
            // 4. AERÓGRAFO & RESPLANDOR (7)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "airbrush_20", Name = "Aerógrafo (Normal 20%)", Category = BrushCategory.Airbrush, Size = 75.0f,
                Description = "Degradados súper sutiles y transiciones de luz atmosférica.",
                Hardness = 0.03f, DefaultFlow = 0.20f, Spacing = 0.08f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "airbrush_40", Name = "Aerógrafo (Normal 40%)", Category = BrushCategory.Airbrush, Size = 75.0f,
                Description = "Aerógrafo estándar equilibrado para luces y sombras corporales.",
                Hardness = 0.04f, DefaultFlow = 0.40f, Spacing = 0.08f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "airbrush_60", Name = "Aerógrafo (Normal 60%)", Category = BrushCategory.Airbrush, Size = 75.0f,
                Description = "Mayor densidad de color conservando contornos difusos.",
                Hardness = 0.05f, DefaultFlow = 0.60f, Spacing = 0.08f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "airbrush_triangle", Name = "Aerógrafo (Triángulo)", Category = BrushCategory.Airbrush, Size = 15.0f,
                Description = "Aerógrafo focalizado de alta precisión para brillos puntuales.",
                Hardness = 0.25f, DefaultFlow = 0.55f, Spacing = 0.07f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "airbrush_trapezoid", Name = "Aerógrafo (Trapezoide)", Category = BrushCategory.Airbrush, Size = 45.0f,
                Description = "Apertura angular amplia para degradados direccionales.",
                Hardness = 0.10f, DefaultFlow = 0.45f, Spacing = 0.08f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "airbrush_particle", Name = "Aerógrafo de Partículas", Category = BrushCategory.Airbrush, Size = 60.0f,
                Description = "Pulverizado atomizado con micro-gotas de color.",
                Hardness = 0.30f, DefaultFlow = 0.50f, Spacing = 0.12f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true, IsSplatter = true
            });
            list.Add(new BrushPreset {
                Id = "glow_neon", Name = "Resplandor Neón (Glow)", Category = BrushCategory.Airbrush, Size = 65.0f,
                Description = "Núcleo brillante con halo translúcido para efectos luminiscentes.",
                Hardness = 0.08f, DefaultFlow = 0.65f, Spacing = 0.08f, MinSizeFactor = 0.35f, Gamma = 1.1f, PressureAffectsOpacity = true
            });

            // =================================================================
            // 5. ACUARELA & GOUACHE (6)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "watercolor_soft", Name = "Acuarela Suave", Category = BrushCategory.Watercolor, Size = 40.0f,
                Description = "Veladuras transparentes y lavados ligeros.",
                Hardness = 0.50f, DefaultFlow = 0.45f, Spacing = 0.09f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true, HasWetEdge = true
            });
            list.Add(new BrushPreset {
                Id = "watercolor_wet_edge", Name = "Acuarela Borde Húmedo (Wet Edge)", Category = BrushCategory.Watercolor, Size = 38.0f,
                Description = "Característico borde oscuro donde el pigmento se acumula al secar.",
                Hardness = 0.70f, DefaultFlow = 0.60f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.1f, PressureAffectsOpacity = true, HasWetEdge = true
            });
            list.Add(new BrushPreset {
                Id = "watercolor_bleed", Name = "Acuarela Sangrado Húmedo", Category = BrushCategory.Watercolor, Size = 55.0f,
                Description = "Fusión de manchas amplias simulando papel mojado.",
                Hardness = 0.35f, DefaultFlow = 0.40f, Spacing = 0.10f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true, HasWetEdge = true
            });
            list.Add(new BrushPreset {
                Id = "oriental_wash", Name = "Aguada Oriental", Category = BrushCategory.Watercolor, Size = 48.0f,
                Description = "Pinceladas aguadas para caligrafía y paisajes zen.",
                Hardness = 0.55f, DefaultFlow = 0.50f, Spacing = 0.09f, MinSizeFactor = 0.15f, Gamma = 1.2f, PressureAffectsOpacity = true, HasWetEdge = true
            });
            list.Add(new BrushPreset {
                Id = "gouache_opaque", Name = "Gouache Opaco", Category = BrushCategory.Watercolor, Size = 34.0f,
                Description = "Acuarela densa de alta cobertura para luces y detalles finales.",
                Hardness = 0.85f, DefaultFlow = 0.85f, Spacing = 0.08f, MinSizeFactor = 0.25f, Gamma = 1.15f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "wash_flat", Name = "Pincel de Lavado Plano", Category = BrushCategory.Watercolor, Size = 60.0f,
                Description = "Pluma ancha plana para fondos atmosféricos de acuarela.",
                Hardness = 0.60f, DefaultFlow = 0.40f, Spacing = 0.10f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = true
            });

            // =================================================================
            // 6. PINTURA & ÓLEO (6)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "oil_thick", Name = "Óleo Espeso", Category = BrushCategory.Paint, Size = 36.0f,
                Description = "Pintura al óleo untuosa con gran presencia de materia.",
                Hardness = 0.85f, DefaultFlow = 0.90f, Spacing = 0.08f, MinSizeFactor = 0.30f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "oil_flat", Name = "Óleo Plano con Cuerpo", Category = BrushCategory.Paint, Size = 45.0f,
                Description = "Pincel plano para definir planos facetados y luces directas.",
                Hardness = 0.90f, DefaultFlow = 0.85f, Spacing = 0.08f, MinSizeFactor = 0.25f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "dry_bristle", Name = "Cerdas Secas", Category = BrushCategory.Paint, Size = 28.0f,
                Description = "Pincel áspero descargado que deja vetas de textura.",
                Hardness = 0.70f, DefaultFlow = 0.75f, Spacing = 0.12f, MinSizeFactor = 0.20f, Gamma = 1.1f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "palette_knife", Name = "Espátula / Cuchillo de Paleta", Category = BrushCategory.Paint, Size = 38.0f,
                Description = "Bordes rectos cortantes para aplicar pintura en bloque.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.35f, Gamma = 1.3f, PressureAffectsOpacity = false, IsChisel = true
            });
            list.Add(new BrushPreset {
                Id = "acrylic_solid", Name = "Acrílico Intenso", Category = BrushCategory.Paint, Size = 32.0f,
                Description = "Color saturado de secado rápido con borde definido.",
                Hardness = 0.92f, DefaultFlow = 0.95f, Spacing = 0.07f, MinSizeFactor = 0.25f, Gamma = 1.15f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "impasto_texture", Name = "Impasto Texturado", Category = BrushCategory.Paint, Size = 50.0f,
                Description = "Pincelada con relieve y textura táctil visible.",
                Hardness = 0.85f, DefaultFlow = 0.85f, Spacing = 0.09f, MinSizeFactor = 0.25f, Gamma = 1.2f, PressureAffectsOpacity = false
            });

            // =================================================================
            // 7. EFECTOS ESPECIALES & TRAMAS (6)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "splatter_ink", Name = "Salpicaduras de Tinta (Splatter)", Category = BrushCategory.Special, Size = 55.0f,
                Description = "Gotas aleatorias de tinta arrojada para dinamismo y acción.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.15f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = false, IsSplatter = true
            });
            list.Add(new BrushPreset {
                Id = "spray_dots", Name = "Spray de Puntos", Category = BrushCategory.Special, Size = 45.0f,
                Description = "Nube concentrada de micro-puntos para texturas.",
                Hardness = 0.80f, DefaultFlow = 0.70f, Spacing = 0.14f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = true, IsSplatter = true
            });
            list.Add(new BrushPreset {
                Id = "halftone_comic", Name = "Trama Semitono Cómic", Category = BrushCategory.Special, Size = 60.0f,
                Description = "Patrón de puntos regulares estilo imprenta manga clásico.",
                Hardness = 0.85f, DefaultFlow = 0.65f, Spacing = 0.14f, MinSizeFactor = 0.50f, Gamma = 1.0f, PressureAffectsOpacity = false, IsHalftone = true
            });
            list.Add(new BrushPreset {
                Id = "stardust_glitter", Name = "Polvo de Estrellas / Glitter", Category = BrushCategory.Special, Size = 40.0f,
                Description = "Destellos y partículas brillantes para fantasía y magia.",
                Hardness = 0.75f, DefaultFlow = 0.80f, Spacing = 0.14f, MinSizeFactor = 0.30f, Gamma = 1.1f, PressureAffectsOpacity = true, IsSplatter = true
            });
            list.Add(new BrushPreset {
                Id = "sparkles", Name = "Puntos Mágicos (Sparkles)", Category = BrushCategory.Special, Size = 35.0f,
                Description = "Estrellitas y destellos para joyas y reflejos.",
                Hardness = 0.85f, DefaultFlow = 0.90f, Spacing = 0.16f, MinSizeFactor = 0.35f, Gamma = 1.1f, PressureAffectsOpacity = false, IsSplatter = true
            });
            list.Add(new BrushPreset {
                Id = "grain_texture", Name = "Textura Cuero / Grano", Category = BrushCategory.Special, Size = 50.0f,
                Description = "Grano uniforme para aportar riqueza táctil a superficies.",
                Hardness = 0.60f, DefaultFlow = 0.55f, Spacing = 0.12f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });

            // =================================================================
            // 8. BORRADORES (5)
            // =================================================================
            list.Add(new BrushPreset {
                Id = "eraser_hard", Name = "Borrador Duro (100% Opaco)", Category = BrushCategory.Erasers, Size = 28.0f,
                Description = "Eliminación limpia y contundente de trazos.",
                Hardness = 1.0f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = false, IsEraser = true
            });
            list.Add(new BrushPreset {
                Id = "eraser_soft", Name = "Borrador Suave (Degradado)", Category = BrushCategory.Erasers, Size = 45.0f,
                Description = "Borrador con transición suave para difuminar bordes.",
                Hardness = 0.20f, DefaultFlow = 0.50f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.0f, PressureAffectsOpacity = true, IsEraser = true
            });
            list.Add(new BrushPreset {
                Id = "eraser_precision", Name = "Borrador de Precisión (2px)", Category = BrushCategory.Erasers, Size = 4.0f,
                Description = "Punta microscópica para limpiar esquinas de lineart.",
                Hardness = 1.0f, DefaultFlow = 1.0f, Spacing = 0.05f, MinSizeFactor = 0.80f, Gamma = 1.0f, PressureAffectsOpacity = false, IsEraser = true
            });
            list.Add(new BrushPreset {
                Id = "eraser_kneaded", Name = "Borrador Amasado (Miga de Pan)", Category = BrushCategory.Erasers, Size = 38.0f,
                Description = "Aclara tonos y rebaja grafito sin borrar completamente.",
                Hardness = 0.40f, DefaultFlow = 0.35f, Spacing = 0.10f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true, IsEraser = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "eraser_airbrush", Name = "Borrador Aerógrafo", Category = BrushCategory.Erasers, Size = 70.0f,
                Description = "Borrador ultra sutil concéntrico para veladuras y correcciones tonales.",
                Hardness = 0.03f, DefaultFlow = 0.30f, Spacing = 0.08f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true, IsEraser = true
            });

            return list;
        }
    }
}
