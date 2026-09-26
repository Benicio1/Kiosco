using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Windows.Media.Imaging; // Soporte nativo para WebP, JPG, PNG, GIF, BMP, TIFF

namespace AuraPaint.MVP
{
    // =========================================================================
    // 1. ESTRUCTURAS WIN32 PARA ENTRADA NATIVA DE LÁPIZ Y TECLADO
    // =========================================================================
    public static class NativePen
    {
        public const int WM_POINTERDOWN   = 0x0246;
        public const int WM_POINTERUPDATE = 0x0245;
        public const int WM_POINTERUP     = 0x0247;

        public const uint PEN_MASK_PRESSURE = 0x00000001;
        public const uint PEN_MASK_ROTATION = 0x00000002;
        public const uint PEN_MASK_TILT_X   = 0x00000004;
        public const uint PEN_MASK_TILT_Y   = 0x00000008;

        public const int VK_SPACE   = 0x20;
        public const int VK_MBUTTON = 0x04;

        [StructLayout(LayoutKind.Sequential)]
        public struct POINT
        {
            public int X;
            public int Y;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct POINTER_INFO
        {
            public uint pointerType;
            public uint pointerId;
            public uint frameId;
            public uint pointerFlags;
            public IntPtr sourceDevice;
            public IntPtr hwndTarget;
            public POINT ptPixelLocation;
            public POINT ptHimetricLocation;
            public POINT ptPixelLocationRaw;
            public POINT ptHimetricLocationRaw;
            public uint dwTime;
            public uint historyCount;
            public int InputData;
            public uint KeyStates;
            public ulong PerformanceCount;
            public int ButtonChangeType;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct POINTER_PEN_INFO
        {
            public POINTER_INFO pointerInfo;
            public uint penFlags;
            public uint penMask;
            public uint pressure;
            public uint rotation;
            public int tiltX;
            public int tiltY;
        }

        [DllImport("user32.dll")]
        public static extern bool GetPointerPenInfo(uint pointerId, ref POINTER_PEN_INFO penInfo);

        [DllImport("user32.dll")]
        public static extern short GetAsyncKeyState(int vKey);
    }

    // =========================================================================
    // 2. PUNTO DE TRAZO CON METADATOS DE HARDWARE
    // =========================================================================
    public struct StrokePoint
    {
        public float X;
        public float Y;
        public float Pressure;
        public float TiltX;
        public float TiltY;
        public long Timestamp;

        public StrokePoint(float x, float y, float pressure, float tiltX, float tiltY, long timestamp)
        {
            X = x;
            Y = y;
            Pressure = pressure;
            TiltX = tiltX;
            TiltY = tiltY;
            Timestamp = timestamp;
        }
    }

    // =========================================================================
    // 3. CAPA DE DIBUJO CONTINUA (CERO LÍNEAS BLANCAS, CERO CUADRÍCULAS BUG)
    // =========================================================================
    public class DrawingLayer
    {
        public string Name { get; set; }
        public float Opacity { get; set; }
        public bool Visible { get; set; }
        public Bitmap Bitmap { get; private set; }
        public Graphics Graphics { get; private set; }

        public DrawingLayer(string name, int width, int height, Color initialColor)
        {
            Name = name;
            Opacity = 1.0f;
            Visible = true;
            InitBitmap(width, height, initialColor);
        }

        public void InitBitmap(int width, int height, Color initialColor)
        {
            if (Graphics != null) { Graphics.Dispose(); Graphics = null; }
            if (Bitmap != null) { Bitmap.Dispose(); Bitmap = null; }

            Bitmap = new Bitmap(width, height, PixelFormat.Format32bppPArgb);
            Graphics = Graphics.FromImage(Bitmap);
            Graphics.SmoothingMode = SmoothingMode.AntiAlias;
            Graphics.InterpolationMode = InterpolationMode.Bilinear;
            Graphics.PixelOffsetMode = PixelOffsetMode.HighSpeed;
            Graphics.CompositingQuality = CompositingQuality.HighSpeed;

            using (var brush = new SolidBrush(initialColor))
            {
                Graphics.FillRectangle(brush, 0, 0, width, height);
            }
        }

        public void Clear(Color col)
        {
            Graphics.CompositingMode = CompositingMode.SourceCopy;
            using (var brush = new SolidBrush(col))
            {
                Graphics.FillRectangle(brush, 0, 0, Bitmap.Width, Bitmap.Height);
            }
            Graphics.CompositingMode = CompositingMode.SourceOver;
        }

        public void Dispose()
        {
            if (Graphics != null) { Graphics.Dispose(); Graphics = null; }
            if (Bitmap != null) { Bitmap.Dispose(); Bitmap = null; }
        }
    }

    // =========================================================================
    // 4. HISTORIAL OPTIMIZADO POR ÁREA SUCIA (DIRTY BOUNDING CROP)
    // =========================================================================
    public class UndoStrokeAction
    {
        public DrawingLayer Layer;
        public Rectangle DirtyBounds;
        public Bitmap BeforeCrop;
        public Bitmap AfterCrop;

        public void Undo()
        {
            if (BeforeCrop != null && Layer != null && Layer.Graphics != null)
            {
                Layer.Graphics.CompositingMode = CompositingMode.SourceCopy;
                Layer.Graphics.DrawImage(BeforeCrop, DirtyBounds.X, DirtyBounds.Y);
                Layer.Graphics.CompositingMode = CompositingMode.SourceOver;
            }
        }

        public void Redo()
        {
            if (AfterCrop != null && Layer != null && Layer.Graphics != null)
            {
                Layer.Graphics.CompositingMode = CompositingMode.SourceCopy;
                Layer.Graphics.DrawImage(AfterCrop, DirtyBounds.X, DirtyBounds.Y);
                Layer.Graphics.CompositingMode = CompositingMode.SourceOver;
            }
        }

        public void Dispose()
        {
            if (BeforeCrop != null) { BeforeCrop.Dispose(); BeforeCrop = null; }
            if (AfterCrop != null) { AfterCrop.Dispose(); AfterCrop = null; }
        }
    }

    // =========================================================================
    // 5. SISTEMA DE PINCELES CON ESTILO IBIS PAINT X
    // =========================================================================
    public enum BrushCategory
    {
        All,            // Todos
        Basic,          // Básico / Simple
        Sketch,         // Bosquejo
        Comic,          // Cómic
        Inking,         // Tinta
        Vector,         // Vector
        Airbrush,       // Aerógrafo
        WatercolorFlat, // Acuarela (plana)
        WatercolorTex,  // Acuarela (textura)
        Paint,          // Pintar
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

        public static List<BrushPreset> GetAllPresets()
        {
            var list = new List<BrushPreset>();

            // --- BÁSICO / SIMPLE ---
            list.Add(new BrushPreset {
                Id = "pen_soft", Name = "Pluma (Suave)", Category = BrushCategory.Basic, Size = 17.5f,
                Description = "Pluma cónica estándar con extremos suaves y flujo continuo.",
                Hardness = 0.85f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "pen_hard", Name = "Pluma (Fuerte)", Category = BrushCategory.Basic, Size = 30.0f,
                Description = "Pluma de entintado nítida con 100% de opacidad y contorno definido.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.10f, Gamma = 1.3f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "pen_blur", Name = "Pluma (Difuminado)", Category = BrushCategory.Basic, Size = 8.0f,
                Description = "Plumilla fina con bordes antialiased suaves.",
                Hardness = 0.70f, DefaultFlow = 0.90f, Spacing = 0.08f, MinSizeFactor = 0.25f, Gamma = 1.1f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "marker_soft", Name = "Rotulador (Suave)", Category = BrushCategory.Basic, Size = 12.0f,
                Description = "Rotulador redondeado de flujo medio para boceto y color rápido.",
                Hardness = 0.80f, DefaultFlow = 0.70f, Spacing = 0.08f, MinSizeFactor = 0.40f, Gamma = 1.1f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "marker_hard", Name = "Rotulador (Fuerte)", Category = BrushCategory.Basic, Size = 12.0f,
                Description = "Rotulador de alta cobertura con borde cortante.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.07f, MinSizeFactor = 0.35f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "pencil_blur", Name = "Lápiz (Difuminado)", Category = BrushCategory.Basic, Size = 48.0f,
                Description = "Mina suave y ancha para sombreados envolventes.",
                Hardness = 0.40f, DefaultFlow = 0.60f, Spacing = 0.10f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "impasto_flat", Name = "Pincel de impasto - Plano", Category = BrushCategory.Basic, Size = 48.0f,
                Description = "Pincelada plana con cuerpo y textura visible.",
                Hardness = 0.85f, DefaultFlow = 0.85f, Spacing = 0.09f, MinSizeFactor = 0.25f, Gamma = 1.2f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "magnetic_line", Name = "Línea magnética", Category = BrushCategory.Basic, Size = 20.0f,
                Description = "Plumilla biselada con ángulo constante estilo rotulador caligráfico.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.3f, PressureAffectsOpacity = false, IsChisel = true
            });
            list.Add(new BrushPreset {
                Id = "digital_pencil", Name = "Lápiz digital", Category = BrushCategory.Basic, Size = 8.0f,
                Description = "Línea fina nítida y uniforme para dibujo de precisión.",
                Hardness = 0.92f, DefaultFlow = 1.0f, Spacing = 0.07f, MinSizeFactor = 0.50f, Gamma = 1.0f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "airbrush_normal", Name = "Aerógrafo (Normal)", Category = BrushCategory.Basic, Size = 80.0f,
                Description = "Aerógrafo difuso concéntrico para degradados y coloreado suave.",
                Hardness = 0.04f, DefaultFlow = 0.35f, Spacing = 0.08f, MinSizeFactor = 0.30f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "airbrush_triangle", Name = "Aerógrafo (Triángulo)", Category = BrushCategory.Basic, Size = 12.0f,
                Description = "Aerógrafo focalizado de alta precisión para brillos puntuales.",
                Hardness = 0.25f, DefaultFlow = 0.55f, Spacing = 0.07f, MinSizeFactor = 0.40f, Gamma = 1.0f, PressureAffectsOpacity = true
            });

            // --- BOSQUEJO ---
            list.Add(new BrushPreset {
                Id = "sketch_hb", Name = "Lápiz de Grafito HB", Category = BrushCategory.Sketch, Size = 12.0f,
                Description = "Sensación clásica de mina de grafito con grano de papel.",
                Hardness = 0.65f, DefaultFlow = 0.75f, Spacing = 0.12f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "sketch_4b", Name = "Lápiz Blando 4B", Category = BrushCategory.Sketch, Size = 24.0f,
                Description = "Mina oscura para bocetos dinámicos y sombreados expresivos.",
                Hardness = 0.55f, DefaultFlow = 0.85f, Spacing = 0.11f, MinSizeFactor = 0.20f, Gamma = 1.1f, PressureAffectsOpacity = true, HasGrain = true
            });
            list.Add(new BrushPreset {
                Id = "charcoal_rough", Name = "Carboncillo Orgánico", Category = BrushCategory.Sketch, Size = 36.0f,
                Description = "Textura terrosa y porosa para bocetos anatómicos rápidos.",
                Hardness = 0.45f, DefaultFlow = 0.65f, Spacing = 0.14f, MinSizeFactor = 0.15f, Gamma = 1.0f, PressureAffectsOpacity = true, HasGrain = true
            });

            // --- CÓMIC & TINTA ---
            list.Add(new BrushPreset {
                Id = "gpen_manga", Name = "Pluma G Manga", Category = BrushCategory.Comic, Size = 25.0f,
                Description = "Lineart tradicional japonés de alto contraste y 100% opacidad.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.06f, MinSizeFactor = 0.08f, Gamma = 1.3f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "maru_manga", Name = "Pluma Maru Fina", Category = BrushCategory.Inking, Size = 6.0f,
                Description = "Punta ultra afilada para ojos, pestañas y tramas finas de manga.",
                Hardness = 1.0f, DefaultFlow = 1.0f, Spacing = 0.05f, MinSizeFactor = 0.05f, Gamma = 1.5f, PressureAffectsOpacity = false
            });
            list.Add(new BrushPreset {
                Id = "kabura_manga", Name = "Pluma Kabura / Cuchara", Category = BrushCategory.Inking, Size = 18.0f,
                Description = "Trazo firme y controlado con mínima variación de grosor.",
                Hardness = 0.95f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.30f, Gamma = 1.1f, PressureAffectsOpacity = false
            });

            // --- ACUARELA & PINTURA ---
            list.Add(new BrushPreset {
                Id = "watercolor_wet_edge", Name = "Acuarela (Borde Húmedo)", Category = BrushCategory.WatercolorTex, Size = 35.0f,
                Description = "Efecto de secado acuoso con concentración de pigmento en los bordes.",
                Hardness = 0.35f, DefaultFlow = 0.65f, Spacing = 0.09f, MinSizeFactor = 0.25f, Gamma = 1.2f, PressureAffectsOpacity = true, HasWetEdge = true
            });
            list.Add(new BrushPreset {
                Id = "watercolor_wash", Name = "Acuarela (Lavado)", Category = BrushCategory.WatercolorFlat, Size = 60.0f,
                Description = "Transparencias acuosas suaves para fondos uniformes.",
                Hardness = 0.20f, DefaultFlow = 0.40f, Spacing = 0.08f, MinSizeFactor = 0.35f, Gamma = 1.0f, PressureAffectsOpacity = true
            });
            list.Add(new BrushPreset {
                Id = "gouache_rich", Name = "Gouache Opaco", Category = BrushCategory.Paint, Size = 32.0f,
                Description = "Pintura cubriente con cuerpo sólido.",
                Hardness = 0.88f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.2f, PressureAffectsOpacity = false
            });

            // --- AERÓGRAFO & FX ---
            list.Add(new BrushPreset {
                Id = "spray_splatter", Name = "Aerógrafo (Salpicadura)", Category = BrushCategory.Airbrush, Size = 65.0f,
                Description = "Gotas dispersas para efectos de estrellas, salpicaduras y nieve.",
                Hardness = 0.90f, DefaultFlow = 0.70f, Spacing = 0.25f, MinSizeFactor = 0.10f, Gamma = 1.0f, PressureAffectsOpacity = true, IsSplatter = true
            });

            // --- BORRADORES ---
            list.Add(new BrushPreset {
                Id = "eraser_precision", Name = "Goma de Borrar (Dura)", Category = BrushCategory.Erasers, Size = 28.0f,
                Description = "Borrado total de precisión con corte nítido.",
                Hardness = 0.98f, DefaultFlow = 1.0f, Spacing = 0.08f, MinSizeFactor = 0.20f, Gamma = 1.1f, PressureAffectsOpacity = false, IsEraser = true
            });
            list.Add(new BrushPreset {
                Id = "eraser_soft_blur", Name = "Goma de Borrar (Suave)", Category = BrushCategory.Erasers, Size = 50.0f,
                Description = "Borrado difuminado con desvanecimiento sutil en los bordes.",
                Hardness = 0.08f, DefaultFlow = 0.50f, Spacing = 0.09f, MinSizeFactor = 0.25f, Gamma = 1.0f, PressureAffectsOpacity = true, IsEraser = true
            });

            return list;
        }
    }

    // =========================================================================
    // 6. MOTOR DE PINCELES CON VELOCIDAD Y TRAZADO ULTRA FLUIDO
    // =========================================================================
    public class BrushEngine
    {
        public float BaseSize = 30.0f;
        public float Flow = 1.0f;
        public Color Color = Color.FromArgb(235, 45, 80);
        public BrushPreset ActivePreset;

        private readonly List<StrokePoint> _rawPoints = new List<StrokePoint>();
        private float _carryDistance = 0.0f;
        private readonly Random _rand = new Random();

        public BrushEngine()
        {
            ActivePreset = BrushPreset.GetAllPresets()[1]; // Pluma (Fuerte) por defecto
            BaseSize = ActivePreset.Size;
            Flow = ActivePreset.DefaultFlow;
        }

        public void SetPreset(BrushPreset preset)
        {
            ActivePreset = preset;
            BaseSize = preset.Size;
            Flow = preset.DefaultFlow;
        }

        public void BeginStroke(StrokePoint start)
        {
            _rawPoints.Clear();
            _rawPoints.Add(start);
            _carryDistance = 0.0f;
        }

        public List<StrokePoint> ProcessPoint(StrokePoint current)
        {
            _rawPoints.Add(current);
            var interpolatedDabs = new List<StrokePoint>();

            if (_rawPoints.Count < 4)
            {
                if (_rawPoints.Count == 1)
                {
                    interpolatedDabs.Add(current);
                }
                return interpolatedDabs;
            }

            int i = _rawPoints.Count - 2;
            StrokePoint p0 = _rawPoints[i - 1];
            StrokePoint p1 = _rawPoints[i];
            StrokePoint p2 = _rawPoints[i + 1];
            StrokePoint p3 = (i + 2 < _rawPoints.Count) ? _rawPoints[i + 2] : p2;

            float segmentLength = (float)Math.Sqrt(Math.Pow(p2.X - p1.X, 2) + Math.Pow(p2.Y - p1.Y, 2));
            float currentDabRadius = Math.Max(1.0f, (BaseSize * Math.Max(ActivePreset.MinSizeFactor, p1.Pressure)) * 0.5f);
            
            // Garantizar que stepDistance nunca sea microscópico para evitar trabas al mover rápido el ratón
            float stepDistance = Math.Max(1.5f, currentDabRadius * 2.0f * ActivePreset.Spacing);

            float distance = _carryDistance;
            while (distance < segmentLength)
            {
                float t = distance / Math.Max(0.001f, segmentLength);
                StrokePoint dab = EvaluateCatmullRom(p0, p1, p2, p3, t);
                interpolatedDabs.Add(dab);
                distance += stepDistance;
            }
            _carryDistance = distance - segmentLength;

            return interpolatedDabs;
        }

        public void EndStroke()
        {
            _rawPoints.Clear();
            _carryDistance = 0.0f;
        }

        private StrokePoint EvaluateCatmullRom(StrokePoint p0, StrokePoint p1, StrokePoint p2, StrokePoint p3, float t)
        {
            float t2 = t * t;
            float t3 = t2 * t;

            float x = 0.5f * ((2 * p1.X) + (-p0.X + p2.X) * t + (2 * p0.X - 5 * p1.X + 4 * p2.X - p3.X) * t2 + (-p0.X + 3 * p1.X - 3 * p2.X + p3.X) * t3);
            float y = 0.5f * ((2 * p1.Y) + (-p0.Y + p2.Y) * t + (2 * p0.Y - 5 * p1.Y + 4 * p2.Y - p3.Y) * t2 + (-p0.Y + 3 * p1.Y - 3 * p2.Y + p3.Y) * t3);
            float pr = 0.5f * ((2 * p1.Pressure) + (-p0.Pressure + p2.Pressure) * t + (2 * p0.Pressure - 5 * p1.Pressure + 4 * p2.Pressure - p3.Pressure) * t2 + (-p0.Pressure + 3 * p1.Pressure - 3 * p2.Pressure + p3.Pressure) * t3);
            pr = Math.Max(0.0f, Math.Min(1.0f, pr));

            return new StrokePoint(x, y, pr, p1.TiltX, p1.TiltY, p1.Timestamp);
        }

        public void StampDabDirect(Graphics g, float canvasX, float canvasY, float pressure)
        {
            float dynamicPressure = (float)Math.Pow(pressure, ActivePreset.Gamma);
            float radius = Math.Max(1.0f, (BaseSize * Math.Max(ActivePreset.MinSizeFactor, dynamicPressure)) * 0.5f);

            int baseAlpha;
            if (ActivePreset.PressureAffectsOpacity)
            {
                baseAlpha = (int)(Math.Max(5, Math.Min(255, 255 * Flow * (0.25f + 0.75f * dynamicPressure))));
            }
            else
            {
                baseAlpha = (int)Math.Max(5, Math.Min(255, 255 * Flow));
            }

            // GOMA DE BORRAR
            if (ActivePreset.IsEraser)
            {
                g.CompositingMode = CompositingMode.SourceCopy;
                RectangleF bounds = new RectangleF(canvasX - radius, canvasY - radius, radius * 2, radius * 2);
                if (ActivePreset.Hardness >= 0.8f)
                {
                    using (var brush = new SolidBrush(Color.FromArgb(0, 0, 0, 0)))
                    {
                        g.FillEllipse(brush, bounds);
                    }
                }
                else
                {
                    int eraseAlpha = Math.Max(0, 255 - baseAlpha);
                    using (var brush = new SolidBrush(Color.FromArgb(eraseAlpha, 0, 0, 0)))
                    {
                        g.FillEllipse(brush, bounds);
                    }
                }
                g.CompositingMode = CompositingMode.SourceOver;
                return;
            }

            // SALPICADURA / SPRAY
            if (ActivePreset.IsSplatter)
            {
                int dropCount = 5;
                for (int d = 0; d < dropCount; d++)
                {
                    float angle = (float)(_rand.NextDouble() * Math.PI * 2);
                    float dist = (float)(_rand.NextDouble() * radius * 1.5f);
                    float dx = canvasX + (float)Math.Cos(angle) * dist;
                    float dy = canvasY + (float)Math.Sin(angle) * dist;
                    float dropRadius = Math.Max(0.75f, (float)(_rand.NextDouble() * radius * 0.25f));
                    using (var brush = new SolidBrush(Color.FromArgb(baseAlpha, Color)))
                    {
                        g.FillEllipse(brush, dx - dropRadius, dy - dropRadius, dropRadius * 2, dropRadius * 2);
                    }
                }
                return;
            }

            // PINCEL CALIGRÁFICO
            if (ActivePreset.IsChisel)
            {
                var state = g.Save();
                g.TranslateTransform(canvasX, canvasY);
                g.RotateTransform(-35.0f);
                RectangleF bounds = new RectangleF(-radius * 1.5f, -radius * 0.45f, radius * 3.0f, radius * 0.9f);
                using (var brush = new SolidBrush(Color.FromArgb(baseAlpha, Color)))
                {
                    g.FillEllipse(brush, bounds);
                }
                g.Restore(state);
                return;
            }

            // LÁPIZ CON GRANO
            if (ActivePreset.HasGrain)
            {
                float jitter = (radius * 0.30f);
                canvasX += (float)(_rand.NextDouble() * 2.0 - 1.0) * jitter;
                canvasY += (float)(_rand.NextDouble() * 2.0 - 1.0) * jitter;
            }

            // AERÓGRAFO SUAVE
            if (ActivePreset.Hardness <= 0.2f)
            {
                RectangleF b1 = new RectangleF(canvasX - radius, canvasY - radius, radius * 2, radius * 2);
                RectangleF b2 = new RectangleF(canvasX - radius * 0.65f, canvasY - radius * 0.65f, radius * 1.3f, radius * 1.3f);
                RectangleF b3 = new RectangleF(canvasX - radius * 0.35f, canvasY - radius * 0.35f, radius * 0.7f, radius * 0.7f);

                int a1 = Math.Max(1, (int)(baseAlpha * 0.18f));
                int a2 = Math.Max(2, (int)(baseAlpha * 0.32f));
                int a3 = Math.Max(3, (int)(baseAlpha * 0.50f));

                using (var br1 = new SolidBrush(Color.FromArgb(a1, Color))) g.FillEllipse(br1, b1);
                using (var br2 = new SolidBrush(Color.FromArgb(a2, Color))) g.FillEllipse(br2, b2);
                using (var br3 = new SolidBrush(Color.FromArgb(a3, Color))) g.FillEllipse(br3, b3);
                return;
            }

            // ACUARELA CON BORDE HÚMEDO
            if (ActivePreset.HasWetEdge)
            {
                RectangleF bounds = new RectangleF(canvasX - radius, canvasY - radius, radius * 2, radius * 2);
                using (var brFill = new SolidBrush(Color.FromArgb((int)(baseAlpha * 0.45f), Color)))
                {
                    g.FillEllipse(brFill, bounds);
                }
                using (var penEdge = new Pen(Color.FromArgb(Math.Min(255, (int)(baseAlpha * 1.15f)), Color), Math.Max(1.0f, radius * 0.18f)))
                {
                    g.DrawEllipse(penEdge, bounds);
                }
                return;
            }

            // PLUMAS Y PINCELES ESTÁNDAR
            {
                RectangleF bounds = new RectangleF(canvasX - radius, canvasY - radius, radius * 2, radius * 2);
                using (var brush = new SolidBrush(Color.FromArgb(baseAlpha, Color)))
                {
                    g.FillEllipse(brush, bounds);
                }
            }
        }

        public static void DrawIbisStrokePreview(Graphics g, BrushPreset preset, Color color, Rectangle bounds, float displaySize)
        {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            g.InterpolationMode = InterpolationMode.HighQualityBilinear;

            float startX = bounds.Left + 10;
            float endX = bounds.Right - 10;
            float midY = bounds.Top + bounds.Height / 2.0f;
            float amp = bounds.Height * 0.28f;

            PointF p0 = new PointF(startX, midY + amp * 0.4f);
            PointF p1 = new PointF(startX + (endX - startX) * 0.28f, midY - amp);
            PointF p2 = new PointF(startX + (endX - startX) * 0.72f, midY + amp);
            PointF p3 = new PointF(endX, midY - amp * 0.4f);

            int steps = 70;
            float maxThickness = Math.Max(2.5f, Math.Min(bounds.Height * 0.55f, displaySize * 0.55f));

            PointF prevPt = p0;
            for (int i = 1; i <= steps; i++)
            {
                float t = i / (float)steps;
                float t2 = t * t;
                float t3 = t2 * t;
                float u = 1.0f - t;
                float u2 = u * u;
                float u3 = u2 * u;

                float x = u3 * p0.X + 3 * u2 * t * p1.X + 3 * u * t2 * p2.X + t3 * p3.X;
                float y = u3 * p0.Y + 3 * u2 * t * p1.Y + 3 * u * t2 * p2.Y + t3 * p3.Y;
                PointF currPt = new PointF(x, y);

                float taper = (float)Math.Sin(t * Math.PI);
                float thickness = Math.Max(1.0f, maxThickness * taper);

                int alpha = preset.PressureAffectsOpacity ? (int)(180 * taper) : 255;
                if (preset.IsEraser) alpha = 140;

                using (var pen = new Pen(Color.FromArgb(Math.Max(10, Math.Min(255, alpha)), color), thickness))
                {
                    pen.StartCap = LineCap.Round;
                    pen.EndCap = LineCap.Round;
                    g.DrawLine(pen, prevPt, currPt);
                }
                prevPt = currPt;
            }
        }
    }

    // =========================================================================
    // 7. SELECTOR DE PINCELES CON LA INTERFAZ EXACTA DE IBIS PAINT X
    // =========================================================================
    public class IbisBrushDialog : Form
    {
        public BrushPreset SelectedPreset { get; private set; }
        public float SelectedSize { get; private set; }
        public float SelectedFlow { get; private set; }

        private Color _activeColor;
        private BrushCategory _activeCategory = BrushCategory.All;
        private List<BrushPreset> _allPresets;

        private FlowLayoutPanel _categoryPanel;
        private Panel _brushListPanel;
        private Panel _previewBox;
        private Label _lblBrushTitle;
        private TrackBar _tbGrosor;
        private Label _lblGrosorVal;
        private TrackBar _tbOpacidad;
        private Label _lblOpacidadVal;

        private List<Panel> _brushRowControls = new List<Panel>();

        public IbisBrushDialog(BrushPreset current, float currentSize, float currentFlow, Color activeColor)
        {
            SelectedPreset = current;
            SelectedSize = currentSize;
            SelectedFlow = currentFlow;
            _activeColor = activeColor;
            _allPresets = BrushPreset.GetAllPresets();

            Text = "Pincel (" + _allPresets.Count + ")";
            Width = 860;
            Height = 600;
            FormBorderStyle = FormBorderStyle.FixedDialog;
            MaximizeBox = false;
            MinimizeBox = false;
            StartPosition = FormStartPosition.CenterParent;
            BackColor = Color.FromArgb(40, 40, 46);
            ForeColor = Color.WhiteSmoke;

            CreateTopBar();
            CreateLayout();
            PopulateCategories();
            PopulateBrushList();
            UpdateRightInspector();
        }

        private void CreateTopBar()
        {
            var pnlTop = new Panel { Dock = DockStyle.Top, Height = 40, BackColor = Color.FromArgb(50, 50, 58) };
            var lblPincelTitle = new Label
            {
                Text = "Pincel (" + _allPresets.Count + ")",
                Left = 14, Top = 8, Width = 140,
                Font = new Font("Segoe UI", 11f, FontStyle.Bold),
                ForeColor = Color.White
            };

            var btnBasico = new Button { Text = "Básico", Left = 160, Top = 6, Width = 80, Height = 28, FlatStyle = FlatStyle.Flat, BackColor = Color.White, ForeColor = Color.Black, Font = new Font("Segoe UI", 8.5f, FontStyle.Bold), TabStop = false };
            var btnPersonalizado = new Button { Text = "Personalizado", Left = 245, Top = 6, Width = 100, Height = 28, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(60, 60, 70), ForeColor = Color.LightGray, Font = new Font("Segoe UI", 8.5f), TabStop = false };
            var btnEnLinea = new Button { Text = "En línea", Left = 350, Top = 6, Width = 80, Height = 28, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(60, 60, 70), ForeColor = Color.LightGray, Font = new Font("Segoe UI", 8.5f), TabStop = false };

            pnlTop.Controls.Add(lblPincelTitle);
            pnlTop.Controls.Add(btnBasico);
            pnlTop.Controls.Add(btnPersonalizado);
            pnlTop.Controls.Add(btnEnLinea);
            Controls.Add(pnlTop);
        }

        private void CreateLayout()
        {
            _categoryPanel = new FlowLayoutPanel
            {
                Dock = DockStyle.Left,
                Width = 110,
                BackColor = Color.FromArgb(20, 20, 24),
                AutoScroll = true,
                Padding = new Padding(2)
            };

            var pnlRight = new Panel
            {
                Dock = DockStyle.Right,
                Width = 360,
                BackColor = Color.FromArgb(32, 32, 38),
                Padding = new Padding(12)
            };

            _previewBox = new Panel
            {
                Dock = DockStyle.Top,
                Height = 140,
                BackColor = Color.White,
                BorderStyle = BorderStyle.FixedSingle
            };
            _previewBox.Paint += DrawCheckerboardAndPreview;
            pnlRight.Controls.Add(_previewBox);

            _lblBrushTitle = new Label
            {
                Dock = DockStyle.Top,
                Height = 36,
                Font = new Font("Segoe UI", 12f, FontStyle.Bold),
                ForeColor = Color.White,
                TextAlign = ContentAlignment.MiddleLeft,
                Padding = new Padding(0, 8, 0, 0)
            };
            pnlRight.Controls.Add(_lblBrushTitle);

            var pnlSliders = new Panel { Dock = DockStyle.Fill, Padding = new Padding(0, 10, 0, 0) };

            var lblGrosorTitle = new Label { Text = "Grosor", Left = 0, Top = 10, Width = 80, Font = new Font("Segoe UI", 9.5f, FontStyle.Bold), ForeColor = Color.White };
            _lblGrosorVal = new Label { Text = string.Format("{0:F1}px", SelectedSize), Left = 260, Top = 10, Width = 80, TextAlign = ContentAlignment.MiddleRight, ForeColor = Color.LightSkyBlue, Font = new Font("Segoe UI", 9.5f, FontStyle.Bold) };
            _tbGrosor = new TrackBar { Left = 0, Top = 34, Width = 340, Minimum = 1, Maximum = 250, Value = Math.Max(1, Math.Min(250, (int)SelectedSize)), TickStyle = TickStyle.None, TabStop = false };
            _tbGrosor.ValueChanged += (s, e) => {
                SelectedSize = _tbGrosor.Value;
                _lblGrosorVal.Text = string.Format("{0:F1}px", SelectedSize);
                _previewBox.Invalidate();
            };

            var lblOpacidadTitle = new Label { Text = "Opacidad", Left = 0, Top = 74, Width = 80, Font = new Font("Segoe UI", 9.5f, FontStyle.Bold), ForeColor = Color.White };
            _lblOpacidadVal = new Label { Text = string.Format("{0:P0}", SelectedFlow), Left = 260, Top = 74, Width = 80, TextAlign = ContentAlignment.MiddleRight, ForeColor = Color.Yellow, Font = new Font("Segoe UI", 9.5f, FontStyle.Bold) };
            _tbOpacidad = new TrackBar { Left = 0, Top = 98, Width = 340, Minimum = 5, Maximum = 100, Value = Math.Max(5, Math.Min(100, (int)(SelectedFlow * 100))), TickStyle = TickStyle.None, TabStop = false };
            _tbOpacidad.ValueChanged += (s, e) => {
                SelectedFlow = _tbOpacidad.Value / 100.0f;
                _lblOpacidadVal.Text = string.Format("{0:P0}", SelectedFlow);
                _previewBox.Invalidate();
            };

            var btnSelect = new Button
            {
                Text = "✔ Seleccionar este Pincel",
                Dock = DockStyle.Bottom,
                Height = 44,
                BackColor = Color.FromArgb(0, 150, 100),
                ForeColor = Color.White,
                Font = new Font("Segoe UI", 10.5f, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat,
                DialogResult = DialogResult.OK,
                TabStop = false
            };

            pnlSliders.Controls.Add(lblGrosorTitle);
            pnlSliders.Controls.Add(_lblGrosorVal);
            pnlSliders.Controls.Add(_tbGrosor);
            pnlSliders.Controls.Add(lblOpacidadTitle);
            pnlSliders.Controls.Add(_lblOpacidadVal);
            pnlSliders.Controls.Add(_tbOpacidad);
            pnlRight.Controls.Add(pnlSliders);
            pnlRight.Controls.Add(btnSelect);

            _brushListPanel = new Panel
            {
                Dock = DockStyle.Fill,
                BackColor = Color.White,
                AutoScroll = true
            };

            Controls.Add(_brushListPanel);
            Controls.Add(pnlRight);
            Controls.Add(_categoryPanel);

            AcceptButton = btnSelect;
        }

        private void PopulateCategories()
        {
            var categories = new[] {
                new { Name = "📊 Todos", Cat = BrushCategory.All },
                new { Name = "📈 Básico", Cat = BrushCategory.Basic },
                new { Name = "✏️ Bosquejo", Cat = BrushCategory.Sketch },
                new { Name = "📖 Cómic", Cat = BrushCategory.Comic },
                new { Name = "🖋️ Tinta", Cat = BrushCategory.Inking },
                new { Name = "💨 Aerógrafo", Cat = BrushCategory.Airbrush },
                new { Name = "💧 Acuarela", Cat = BrushCategory.WatercolorFlat },
                new { Name = "🎨 Textura", Cat = BrushCategory.WatercolorTex },
                new { Name = "🖌️ Pintar", Cat = BrushCategory.Paint },
                new { Name = "🧹 Gomas", Cat = BrushCategory.Erasers }
            };

            foreach (var item in categories)
            {
                var btn = new Button
                {
                    Text = item.Name,
                    Width = 100,
                    Height = 40,
                    FlatStyle = FlatStyle.Flat,
                    BackColor = (item.Cat == _activeCategory) ? Color.FromArgb(45, 90, 140) : Color.FromArgb(28, 28, 34),
                    ForeColor = (item.Cat == _activeCategory) ? Color.White : Color.FromArgb(180, 180, 190),
                    Font = new Font("Segoe UI", 8f, FontStyle.Bold),
                    Margin = new Padding(2),
                    TabStop = false
                };
                BrushCategory cat = item.Cat;
                btn.Click += (s, e) => {
                    _activeCategory = cat;
                    foreach (Control c in _categoryPanel.Controls)
                    {
                        c.BackColor = Color.FromArgb(28, 28, 34);
                        c.ForeColor = Color.FromArgb(180, 180, 190);
                    }
                    btn.BackColor = Color.FromArgb(45, 90, 140);
                    btn.ForeColor = Color.White;
                    PopulateBrushList();
                };
                _categoryPanel.Controls.Add(btn);
            }
        }

        private void PopulateBrushList()
        {
            _brushListPanel.SuspendLayout();
            _brushListPanel.Controls.Clear();
            _brushRowControls.Clear();

            int y = 0;
            int rowHeight = 52;
            int panelWidth = _brushListPanel.ClientSize.Width > 0 ? _brushListPanel.ClientSize.Width : 370;

            foreach (var preset in _allPresets)
            {
                if (_activeCategory != BrushCategory.All && preset.Category != _activeCategory)
                    continue;

                var row = new Panel
                {
                    Left = 0,
                    Top = y,
                    Width = panelWidth,
                    Height = rowHeight,
                    BackColor = (preset.Id == SelectedPreset.Id) ? Color.FromArgb(215, 235, 252) : Color.White,
                    Cursor = Cursors.Hand
                };

                BrushPreset targetPreset = preset;

                row.Paint += (s, e) => {
                    var g = e.Graphics;
                    using (var linePen = new Pen(Color.FromArgb(235, 235, 240)))
                    {
                        g.DrawLine(linePen, 0, rowHeight - 1, row.Width, rowHeight - 1);
                    }

                    Rectangle strokeRect = new Rectangle(12, 6, 170, 40);
                    BrushEngine.DrawIbisStrokePreview(g, targetPreset, Color.FromArgb(25, 25, 30), strokeRect, targetPreset.Size);

                    using (var font = new Font("Segoe UI", 9.5f, FontStyle.Regular))
                    using (var textBrush = new SolidBrush(Color.FromArgb(20, 20, 25)))
                    {
                        g.DrawString(targetPreset.Name, font, textBrush, 190, 16);
                    }

                    using (var font = new Font("Segoe UI", 9f, FontStyle.Regular))
                    using (var textBrush = new SolidBrush(Color.FromArgb(120, 120, 130)))
                    {
                        string sizeStr = string.Format("{0:F1}", targetPreset.Size);
                        g.DrawString(sizeStr, font, textBrush, row.Width - 65, 16);
                    }

                    using (var circlePen = new Pen(Color.FromArgb(180, 180, 190), 1.2f))
                    using (var plusFont = new Font("Segoe UI", 10f, FontStyle.Bold))
                    using (var plusBrush = new SolidBrush(Color.FromArgb(140, 140, 150)))
                    {
                        int cx = row.Width - 30;
                        int cy = 16;
                        g.DrawEllipse(circlePen, cx, cy, 18, 18);
                        g.DrawString("+", plusFont, plusBrush, cx + 3, cy - 2);
                    }
                };

                Action selectAction = () => {
                    SelectedPreset = targetPreset;
                    SelectedSize = targetPreset.Size;
                    SelectedFlow = targetPreset.DefaultFlow;

                    foreach (var r in _brushRowControls)
                    {
                        r.BackColor = Color.White;
                    }
                    row.BackColor = Color.FromArgb(215, 235, 252);

                    UpdateRightInspector();
                };

                row.Click += (s, e) => selectAction();
                _brushRowControls.Add(row);
                _brushListPanel.Controls.Add(row);
                y += rowHeight;
            }

            _brushListPanel.ResumeLayout(true);
        }

        private void UpdateRightInspector()
        {
            _lblBrushTitle.Text = SelectedPreset.Name;
            _tbGrosor.Value = Math.Max(1, Math.Min(250, (int)SelectedSize));
            _lblGrosorVal.Text = string.Format("{0:F1}px", SelectedSize);
            _tbOpacidad.Value = Math.Max(5, Math.Min(100, (int)(SelectedFlow * 100)));
            _lblOpacidadVal.Text = string.Format("{0:P0}", SelectedFlow);
            _previewBox.Invalidate();
        }

        private void DrawCheckerboardAndPreview(object sender, PaintEventArgs e)
        {
            var g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;

            int tileSize = 12;
            for (int x = 0; x < _previewBox.Width; x += tileSize)
            {
                for (int y = 0; y < _previewBox.Height; y += tileSize)
                {
                    bool isEven = ((x / tileSize) + (y / tileSize)) % 2 == 0;
                    using (var b = new SolidBrush(isEven ? Color.White : Color.FromArgb(225, 225, 230)))
                    {
                        g.FillRectangle(b, x, y, tileSize, tileSize);
                    }
                }
            }

            Rectangle previewRect = new Rectangle(14, 10, _previewBox.Width - 28, _previewBox.Height - 20);
            Color strokeColor = Color.FromArgb((int)(SelectedFlow * 255), _activeColor);
            BrushEngine.DrawIbisStrokePreview(g, SelectedPreset, strokeColor, previewRect, SelectedSize);
        }
    }

    // =========================================================================
    // 8. VENTANA DE REFERENCIA UNIVERSAL (CARGA WEBP, JPG, PNG, GIF, BMP, TIFF)
    // =========================================================================
    public class ReferenceBox : Panel
    {
        private Bitmap _referenceImage = null;
        private float _refZoom = 1.0f;
        private float _refPanX = 0.0f;
        private float _refPanY = 0.0f;
        private bool _isDragging = false;
        private Point _dragStart;

        public event Action<Color> OnColorPicked;

        public ReferenceBox()
        {
            Width = 340;
            Height = 280;
            BackColor = Color.FromArgb(24, 24, 30);
            BorderStyle = BorderStyle.FixedSingle;
            DoubleBuffered = true;
            AllowDrop = true;

            DragEnter += (s, e) => {
                if (e.Data.GetDataPresent(DataFormats.FileDrop))
                    e.Effect = DragDropEffects.Copy;
            };
            DragDrop += (s, e) => {
                string[] files = (string[])e.Data.GetData(DataFormats.FileDrop);
                if (files != null && files.Length > 0)
                {
                    LoadUniversalImage(files[0]);
                }
            };

            var pnlHeader = new Panel { Dock = DockStyle.Top, Height = 28, BackColor = Color.FromArgb(36, 36, 44) };
            var lblTitle = new Label { Text = "🖼️ Referencia", Left = 6, Top = 5, Width = 110, ForeColor = Color.FromArgb(0, 220, 160), Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };

            var btnOpen = new Button { Text = "📂 Abrir", Left = 120, Top = 2, Width = 64, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 65), ForeColor = Color.White, Font = new Font("Segoe UI", 7.5f), TabStop = false };
            btnOpen.Click += (s, e) => ShowOpenDialog();

            var btnFit = new Button { Text = "🔄", Left = 190, Top = 2, Width = 28, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 65), ForeColor = Color.White, TabStop = false };
            btnFit.Click += (s, e) => FitImage();

            var btnClose = new Button { Text = "✖", Left = 304, Top = 2, Width = 28, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(70, 30, 35), ForeColor = Color.White, TabStop = false };
            btnClose.Click += (s, e) => this.Visible = false;

            pnlHeader.Controls.Add(lblTitle);
            pnlHeader.Controls.Add(btnOpen);
            pnlHeader.Controls.Add(btnFit);
            pnlHeader.Controls.Add(btnClose);

            Controls.Add(pnlHeader);
        }

        public void ShowOpenDialog()
        {
            using (var ofd = new OpenFileDialog())
            {
                ofd.Title = "Seleccionar Imagen de Referencia (WebP, JPG, PNG, GIF, BMP, TIFF...)";
                ofd.Filter = "Todos los Formatos de Imagen (*.webp;*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.jfif;*.tiff)|*.webp;*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.jfif;*.tiff|Archivos WebP (*.webp)|*.webp|Archivos JPG (*.jpg;*.jpeg;*.jfif)|*.jpg;*.jpeg;*.jfif|Archivos PNG (*.png)|*.png|Archivos GIF (*.gif)|*.gif|Todos los Archivos (*.*)|*.*";
                if (ofd.ShowDialog() == DialogResult.OK)
                {
                    LoadUniversalImage(ofd.FileName);
                }
            }
        }

        // Decodificador Universal a través de WPF BitmapDecoder (soporta WebP y todos los códecs de Windows)
        public void LoadUniversalImage(string filePath)
        {
            try
            {
                if (!File.Exists(filePath))
                {
                    MessageBox.Show("El archivo no existe.", "Error", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                Bitmap newBmp = null;

                byte[] fileBytes = null;
                try { fileBytes = File.ReadAllBytes(filePath); } catch { }

                // Intento 1: Decodificador Universal de Windows (WPF/WIC en Stream para evitar bugs de URI en nombres con # o caracteres especiales)
                if (fileBytes != null)
                {
                    try
                    {
                        using (var ms = new MemoryStream(fileBytes))
                        {
                            var decoder = BitmapDecoder.Create(ms, BitmapCreateOptions.None, BitmapCacheOption.OnLoad);
                            if (decoder.Frames.Count > 0)
                            {
                                var frame = decoder.Frames[0];
                                int w = frame.PixelWidth;
                                int h = frame.PixelHeight;

                                newBmp = new Bitmap(w, h, PixelFormat.Format32bppArgb);
                                BitmapData bmpData = newBmp.LockBits(new Rectangle(0, 0, w, h), ImageLockMode.WriteOnly, PixelFormat.Format32bppArgb);
                                
                                var converted = new FormatConvertedBitmap(frame, System.Windows.Media.PixelFormats.Bgra32, null, 0);
                                converted.CopyPixels(System.Windows.Int32Rect.Empty, bmpData.Scan0, h * bmpData.Stride, bmpData.Stride);
                                
                                newBmp.UnlockBits(bmpData);
                            }
                        }
                    }
                    catch { }
                }

                // Intento 2: GDI+ con MemoryStream
                if (newBmp == null && fileBytes != null)
                {
                    try
                    {
                        using (var ms = new MemoryStream(fileBytes))
                        {
                            using (var temp = Image.FromStream(ms, true, false))
                            {
                                newBmp = new Bitmap(temp.Width, temp.Height, PixelFormat.Format32bppArgb);
                                using (Graphics g = Graphics.FromImage(newBmp))
                                {
                                    g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                                    g.DrawImage(temp, new Rectangle(0, 0, temp.Width, temp.Height), 0, 0, temp.Width, temp.Height, GraphicsUnit.Pixel);
                                }
                            }
                        }
                    }
                    catch { }
                }

                // Intento 3: Image.FromFile
                if (newBmp == null)
                {
                    using (var temp = Image.FromFile(filePath))
                    {
                        newBmp = new Bitmap(temp.Width, temp.Height, PixelFormat.Format32bppArgb);
                        using (Graphics g = Graphics.FromImage(newBmp))
                        {
                            g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                            g.DrawImage(temp, new Rectangle(0, 0, temp.Width, temp.Height), 0, 0, temp.Width, temp.Height, GraphicsUnit.Pixel);
                        }
                    }
                }

                if (newBmp != null)
                {
                    if (_referenceImage != null) _referenceImage.Dispose();
                    _referenceImage = newBmp;
                    FitImage();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("No se pudo cargar la imagen (" + Path.GetFileName(filePath) + "): " + ex.Message, "Error al abrir imagen", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void FitImage()
        {
            if (_referenceImage == null) return;
            float availW = Width;
            float availH = Height - 28;
            _refZoom = Math.Min(availW / (float)_referenceImage.Width, availH / (float)_referenceImage.Height) * 0.95f;
            _refPanX = (availW - _referenceImage.Width * _refZoom) / 2.0f;
            _refPanY = 28 + (availH - _referenceImage.Height * _refZoom) / 2.0f;
            Invalidate();
        }

        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e);
            if (e.Y < 28) return;

            if (e.Button == MouseButtons.Left && (ModifierKeys == Keys.Shift || e.Button == MouseButtons.Middle))
            {
                PickColorFromRef(e.X, e.Y);
                return;
            }

            _isDragging = true;
            _dragStart = e.Location;
            Cursor = Cursors.SizeAll;
        }

        protected override void OnMouseMove(MouseEventArgs e)
        {
            base.OnMouseMove(e);
            if (_isDragging && _referenceImage != null)
            {
                _refPanX += (e.X - _dragStart.X);
                _refPanY += (e.Y - _dragStart.Y);
                _dragStart = e.Location;
                Invalidate();
            }
        }

        protected override void OnMouseUp(MouseEventArgs e)
        {
            base.OnMouseUp(e);
            if (_isDragging)
            {
                _isDragging = false;
                Cursor = Cursors.Default;
            }
        }

        protected override void OnMouseWheel(MouseEventArgs e)
        {
            base.OnMouseWheel(e);
            if (_referenceImage == null) return;

            float zoomFactor = e.Delta > 0 ? 1.15f : (1.0f / 1.15f);
            float newZoom = Math.Max(0.05f, Math.Min(30.0f, _refZoom * zoomFactor));

            float imgX = (e.X - _refPanX) / _refZoom;
            float imgY = (e.Y - _refPanY) / _refZoom;

            _refZoom = newZoom;
            _refPanX = e.X - (imgX * _refZoom);
            _refPanY = e.Y - (imgY * _refZoom);

            Invalidate();
        }

        private void PickColorFromRef(int screenX, int screenY)
        {
            if (_referenceImage == null) return;
            float imgX = (screenX - _refPanX) / _refZoom;
            float imgY = (screenY - _refPanY) / _refZoom;

            if (imgX >= 0 && imgX < _referenceImage.Width && imgY >= 0 && imgY < _referenceImage.Height)
            {
                Color c = _referenceImage.GetPixel((int)imgX, (int)imgY);
                if (OnColorPicked != null) OnColorPicked(c);
            }
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e);
            var g = e.Graphics;
            g.InterpolationMode = InterpolationMode.HighQualityBilinear;
            g.PixelOffsetMode = PixelOffsetMode.Half;

            if (_referenceImage == null)
            {
                using (var brush = new SolidBrush(Color.FromArgb(140, 140, 160)))
                using (var font = new Font("Segoe UI", 8.5f))
                {
                    string hint = "Haz clic en 📂 Abrir o arrastra cualquier\nimagen (WebP, JPG, PNG, GIF, BMP).\n\n• Rueda: Zoom | Arrastrar: Paneo\n• Shift + Clic: Cuentagotas de color";
                    var sf = new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Center };
                    g.DrawString(hint, font, brush, new RectangleF(0, 28, Width, Height - 28), sf);
                }
                return;
            }

            var state = g.Save();
            g.SetClip(new Rectangle(0, 28, Width, Height - 28));
            g.TranslateTransform(_refPanX, _refPanY);
            g.ScaleTransform(_refZoom, _refZoom);
            g.DrawImage(_referenceImage, 0, 0);
            g.Restore(state);
        }
    }

    // =========================================================================
    // 9. DIÁLOGO MODAL PARA TAMAÑO DEL LIENZO
    // =========================================================================
    public class CanvasSizeDialog : Form
    {
        public int SelectedWidth { get; private set; }
        public int SelectedHeight { get; private set; }
        public bool ClearCanvas { get; private set; }

        private NumericUpDown numWidth;
        private NumericUpDown numHeight;
        private ComboBox cboPresets;
        private CheckBox chkClear;

        public CanvasSizeDialog(int currentWidth, int currentHeight)
        {
            Text = "Configurar Tamaño del Lienzo";
            Width = 440;
            Height = 340;
            FormBorderStyle = FormBorderStyle.FixedDialog;
            MaximizeBox = false;
            MinimizeBox = false;
            StartPosition = FormStartPosition.CenterParent;
            BackColor = Color.FromArgb(32, 32, 38);
            ForeColor = Color.WhiteSmoke;

            var lblPreset = new Label { Text = "Presets Estándar:", Left = 24, Top = 24, Width = 140, ForeColor = Color.LightGray };
            cboPresets = new ComboBox { Left = 170, Top = 20, Width = 220, DropDownStyle = ComboBoxStyle.DropDownList };
            cboPresets.Items.AddRange(new object[] {
                "Personalizado",
                "A4 - 300 DPI (2480 × 3508 px)",
                "A4 Manga - 600 DPI (4960 × 7016 px)",
                "4K Ultra HD (3840 × 2160 px)",
                "Full HD 1080p (1920 × 1080 px)",
                "Cuadrado Redes (2048 × 2048 px)",
                "Banner Web / Twitter (3000 × 2000 px)"
            });
            cboPresets.SelectedIndex = 0;
            cboPresets.SelectedIndexChanged += OnPresetChanged;

            var lblWidth = new Label { Text = "Ancho (Píxeles):", Left = 24, Top = 70, Width = 140, ForeColor = Color.LightGray };
            numWidth = new NumericUpDown { Left = 170, Top = 68, Width = 130, Minimum = 256, Maximum = 16384, Value = currentWidth };

            var lblHeight = new Label { Text = "Alto (Píxeles):", Left = 24, Top = 114, Width = 140, ForeColor = Color.LightGray };
            numHeight = new NumericUpDown { Left = 170, Top = 112, Width = 130, Minimum = 256, Maximum = 16384, Value = currentHeight };

            chkClear = new CheckBox { Text = "Crear como lienzo nuevo en blanco (limpiar capas)", Left = 24, Top = 165, Width = 370, Checked = false, ForeColor = Color.LemonChiffon };

            var btnOk = new Button { Text = "Aplicar", Left = 180, Top = 230, Width = 100, Height = 34, DialogResult = DialogResult.OK, BackColor = Color.FromArgb(0, 150, 100), FlatStyle = FlatStyle.Flat };
            var btnCancel = new Button { Text = "Cancelar", Left = 290, Top = 230, Width = 100, Height = 34, DialogResult = DialogResult.Cancel, BackColor = Color.FromArgb(60, 60, 70), FlatStyle = FlatStyle.Flat };

            btnOk.Click += (s, e) => {
                SelectedWidth = (int)numWidth.Value;
                SelectedHeight = (int)numHeight.Value;
                ClearCanvas = chkClear.Checked;
            };

            Controls.Add(lblPreset);
            Controls.Add(cboPresets);
            Controls.Add(lblWidth);
            Controls.Add(numWidth);
            Controls.Add(lblHeight);
            Controls.Add(numHeight);
            Controls.Add(chkClear);
            Controls.Add(btnOk);
            Controls.Add(btnCancel);

            AcceptButton = btnOk;
            CancelButton = btnCancel;
        }

        private void OnPresetChanged(object sender, EventArgs e)
        {
            switch (cboPresets.SelectedIndex)
            {
                case 1: numWidth.Value = 2480; numHeight.Value = 3508; break;
                case 2: numWidth.Value = 4960; numHeight.Value = 7016; break;
                case 3: numWidth.Value = 3840; numHeight.Value = 2160; break;
                case 4: numWidth.Value = 1920; numHeight.Value = 1080; break;
                case 5: numWidth.Value = 2048; numHeight.Value = 2048; break;
                case 6: numWidth.Value = 3000; numHeight.Value = 2000; break;
            }
        }
    }

    // =========================================================================
    // 10. VENTANA PRINCIPAL (SUPERFICIE CONTINUA SIN SEPARACIONES NI LÍNEAS)
    // =========================================================================
    public class CanvasForm : Form
    {
        public int CanvasWidth = 3840;
        public int CanvasHeight = 2160;

        public float Zoom = 0.35f;
        public float PanX = 260.0f;
        public float PanY = 60.0f;
        public float CanvasRotation = 0.0f;

        public bool ShowGrid = false;

        private DrawingLayer _bgLayer;
        private DrawingLayer _drawLayer;
        private readonly BrushEngine _brush = new BrushEngine();
        private readonly Stack<UndoStrokeAction> _undoStack = new Stack<UndoStrokeAction>();
        private readonly Stack<UndoStrokeAction> _redoStack = new Stack<UndoStrokeAction>();
        
        // Estado del trazo
        private bool _isPanning = false;
        private Point _lastMousePos;
        private bool _isStroking = false;
        private bool _isSpaceDown = false;
        private float _strokeMinX, _strokeMinY, _strokeMaxX, _strokeMaxY;
        private Bitmap _preStrokeLayerSnapshot = null;

        // UI Controls
        private Panel _leftDock;
        private Label _lblSizeValue;
        private Label _lblFlowValue;
        private Label _lblCanvasDimensions;
        private Button _btnCurrentColor;
        private Button _btnActiveBrush;
        private TextBox _txtHex;
        private bool _suppressHexUpdate = false;
        private ReferenceBox _refBox;

        public CanvasForm()
        {
            Text = "AuraPaint Pro Studio — High-Performance Digital Illustration Engine";
            Width = 1440;
            Height = 900;
            KeyPreview = true;
            DoubleBuffered = true;
            SetStyle(ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint | ControlStyles.OptimizedDoubleBuffer, true);
            BackColor = Color.FromArgb(20, 20, 24);

            _bgLayer = new DrawingLayer("Fondo Blanco", CanvasWidth, CanvasHeight, Color.White);
            _drawLayer = new DrawingLayer("Capa de Dibujo 1", CanvasWidth, CanvasHeight, Color.Transparent);

            CreateQuickToolbar();
            CreateLeftDock();
            CreateReferenceBox();
            CenterCanvasView();
        }

        private void CreateReferenceBox()
        {
            _refBox = new ReferenceBox
            {
                Left = ClientSize.Width - 360,
                Top = 38,
                Anchor = AnchorStyles.Top | AnchorStyles.Right,
                Visible = true
            };
            _refBox.OnColorPicked += (c) => SetCurrentColor(c, true);
            Controls.Add(_refBox);
            _refBox.BringToFront();
        }

        private void CenterCanvasView()
        {
            float availableWidth = ClientSize.Width - 230;
            float availableHeight = ClientSize.Height - 40;
            Zoom = Math.Min(availableWidth / (float)CanvasWidth, availableHeight / (float)CanvasHeight) * 0.88f;
            PanX = 230 + (availableWidth - CanvasWidth * Zoom) / 2.0f;
            PanY = 35 + (availableHeight - CanvasHeight * Zoom) / 2.0f;
            CanvasRotation = 0.0f;
            Invalidate();
        }

        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            Keys key = keyData & Keys.KeyCode;

            if (key == Keys.Space)
            {
                if (ActiveControl is TextBox)
                {
                    return base.ProcessCmdKey(ref msg, keyData);
                }

                if (!_isSpaceDown)
                {
                    _isSpaceDown = true;
                    if (!_isStroking) Cursor = Cursors.Hand;
                }
                return true;
            }

            return base.ProcessCmdKey(ref msg, keyData);
        }

        private void CreateLeftDock()
        {
            _leftDock = new Panel
            {
                Dock = DockStyle.Left,
                Width = 230,
                BackColor = Color.FromArgb(28, 28, 34),
                Padding = new Padding(8)
            };

            var lblTitle = new Label
            {
                Text = "⚡ AURAPAINT",
                Dock = DockStyle.Top,
                Height = 32,
                ForeColor = Color.FromArgb(0, 220, 160),
                Font = new Font("Segoe UI", 11f, FontStyle.Bold),
                TextAlign = ContentAlignment.MiddleCenter
            };
            _leftDock.Controls.Add(lblTitle);

            _lblCanvasDimensions = new Label
            {
                Text = string.Format("Lienzo: {0}x{1} px", CanvasWidth, CanvasHeight),
                Dock = DockStyle.Top,
                Height = 20,
                ForeColor = Color.FromArgb(170, 170, 190),
                Font = new Font("Consolas", 8.5f),
                TextAlign = ContentAlignment.MiddleCenter
            };
            var btnResizeCanvas = new Button
            {
                Text = "📐 Tamaño de Lienzo...",
                Dock = DockStyle.Top,
                Height = 28,
                BackColor = Color.FromArgb(45, 45, 55),
                ForeColor = Color.WhiteSmoke,
                FlatStyle = FlatStyle.Flat,
                TabStop = false
            };
            btnResizeCanvas.Click += (s, e) => {
                this.ActiveControl = null;
                ShowCanvasSizeDialog();
            };

            _leftDock.Controls.Add(btnResizeCanvas);
            _leftDock.Controls.Add(_lblCanvasDimensions);

            AddDockSeparator("HERRAMIENTA ACTIVA");

            _btnActiveBrush = new Button
            {
                Text = "🖌️ " + _brush.ActivePreset.Name + " ▼",
                Dock = DockStyle.Top,
                Height = 40,
                BackColor = Color.FromArgb(38, 55, 75),
                ForeColor = Color.Cyan,
                Font = new Font("Segoe UI", 9.5f, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat,
                TabStop = false
            };
            _btnActiveBrush.Click += (s, e) => {
                this.ActiveControl = null;
                using (var dlg = new IbisBrushDialog(_brush.ActivePreset, _brush.BaseSize, _brush.Flow, _brush.Color))
                {
                    if (dlg.ShowDialog(this) == DialogResult.OK)
                    {
                        _brush.SetPreset(dlg.SelectedPreset);
                        _brush.BaseSize = dlg.SelectedSize;
                        _brush.Flow = dlg.SelectedFlow;
                        _btnActiveBrush.Text = "🖌️ " + _brush.ActivePreset.Name + " ▼";
                        _lblSizeValue.Text = string.Format("{0:F0} px", _brush.BaseSize);
                        _lblFlowValue.Text = string.Format("{0:P0}", _brush.Flow);
                    }
                }
                this.ActiveControl = null;
            };
            _leftDock.Controls.Add(_btnActiveBrush);

            var btnOpenRef = new Button
            {
                Text = "🖼️ Mostrar/Ocultar Referencia",
                Dock = DockStyle.Top,
                Height = 26,
                BackColor = Color.FromArgb(40, 40, 50),
                ForeColor = Color.LightSkyBlue,
                Font = new Font("Segoe UI", 8f),
                FlatStyle = FlatStyle.Flat,
                TabStop = false,
                Margin = new Padding(0, 4, 0, 4)
            };
            btnOpenRef.Click += (s, e) => {
                this.ActiveControl = null;
                _refBox.Visible = !_refBox.Visible;
            };
            _leftDock.Controls.Add(btnOpenRef);

            AddDockSeparator("PARÁMETROS");

            var pnlSize = new Panel { Dock = DockStyle.Top, Height = 44 };
            var lblSizeTitle = new Label { Text = "Tamaño:", Top = 2, Left = 4, Width = 60, ForeColor = Color.LightGray };
            _lblSizeValue = new Label { Text = string.Format("{0:F0} px", _brush.BaseSize), Top = 2, Left = 130, Width = 70, ForeColor = Color.Cyan, TextAlign = ContentAlignment.MiddleRight };
            var tbSize = new TrackBar { Dock = DockStyle.Bottom, Minimum = 1, Maximum = 250, Value = Math.Max(1, Math.Min(250, (int)_brush.BaseSize)), TickStyle = TickStyle.None, TabStop = false };
            tbSize.ValueChanged += (s, e) => {
                _brush.BaseSize = tbSize.Value;
                _lblSizeValue.Text = string.Format("{0} px", tbSize.Value);
            };
            pnlSize.Controls.Add(lblSizeTitle);
            pnlSize.Controls.Add(_lblSizeValue);
            pnlSize.Controls.Add(tbSize);
            _leftDock.Controls.Add(pnlSize);

            var pnlFlow = new Panel { Dock = DockStyle.Top, Height = 44 };
            var lblFlowTitle = new Label { Text = "Opacidad:", Top = 2, Left = 4, Width = 70, ForeColor = Color.LightGray };
            _lblFlowValue = new Label { Text = string.Format("{0:P0}", _brush.Flow), Top = 2, Left = 130, Width = 70, ForeColor = Color.Yellow, TextAlign = ContentAlignment.MiddleRight };
            var tbFlow = new TrackBar { Dock = DockStyle.Bottom, Minimum = 5, Maximum = 100, Value = Math.Max(5, Math.Min(100, (int)(_brush.Flow * 100))), TickStyle = TickStyle.None, TabStop = false };
            tbFlow.ValueChanged += (s, e) => {
                _brush.Flow = tbFlow.Value / 100.0f;
                _lblFlowValue.Text = string.Format("{0}%", tbFlow.Value);
            };
            pnlFlow.Controls.Add(lblFlowTitle);
            pnlFlow.Controls.Add(_lblFlowValue);
            pnlFlow.Controls.Add(tbFlow);
            _leftDock.Controls.Add(pnlFlow);

            AddDockSeparator("COLOR ACTIVO Y CÓDIGO HEX");

            var pnlColorBtn = new Panel { Dock = DockStyle.Top, Height = 34 };
            _btnCurrentColor = new Button
            {
                Dock = DockStyle.Fill,
                BackColor = _brush.Color,
                Text = "Abrir Selector de Color...",
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                TabStop = false
            };
            _btnCurrentColor.Click += (s, e) => {
                this.ActiveControl = null;
                using (var cd = new ColorDialog { Color = _brush.Color, FullOpen = true })
                {
                    if (cd.ShowDialog(this) == DialogResult.OK)
                    {
                        SetCurrentColor(cd.Color, true);
                    }
                }
                this.ActiveControl = null;
            };
            pnlColorBtn.Controls.Add(_btnCurrentColor);
            _leftDock.Controls.Add(pnlColorBtn);

            var pnlHex = new Panel { Dock = DockStyle.Top, Height = 34, Padding = new Padding(0, 4, 0, 4) };
            var lblHexTag = new Label { Text = "HEX:", Left = 2, Top = 8, Width = 34, ForeColor = Color.Cyan, Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };

            _txtHex = new TextBox
            {
                Left = 38,
                Top = 6,
                Width = 74,
                Text = string.Format("#{0:X2}{1:X2}{2:X2}", _brush.Color.R, _brush.Color.G, _brush.Color.B),
                BackColor = Color.FromArgb(18, 18, 24),
                ForeColor = Color.LimeGreen,
                Font = new Font("Consolas", 9f, FontStyle.Bold),
                BorderStyle = BorderStyle.FixedSingle
            };
            _txtHex.TextChanged += (s, e) => {
                if (_suppressHexUpdate) return;
                TryParseHexColor(_txtHex.Text);
            };
            _txtHex.KeyDown += (s, e) => {
                if (e.KeyCode == Keys.Enter)
                {
                    TryParseHexColor(_txtHex.Text);
                    e.Handled = true;
                    e.SuppressKeyPress = true;
                    this.ActiveControl = null;
                }
            };

            var btnCopyHex = new Button
            {
                Text = "📋",
                Left = 116,
                Top = 5,
                Width = 32,
                Height = 24,
                BackColor = Color.FromArgb(45, 45, 55),
                ForeColor = Color.WhiteSmoke,
                FlatStyle = FlatStyle.Flat,
                TabStop = false
            };
            btnCopyHex.Click += (s, e) => {
                this.ActiveControl = null;
                if (!string.IsNullOrEmpty(_txtHex.Text))
                {
                    Clipboard.SetText(_txtHex.Text);
                }
            };

            var btnPasteHex = new Button
            {
                Text = "📥 Pegar",
                Left = 152,
                Top = 5,
                Width = 64,
                Height = 24,
                BackColor = Color.FromArgb(45, 45, 55),
                ForeColor = Color.WhiteSmoke,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 7.5f),
                TabStop = false
            };
            btnPasteHex.Click += (s, e) => {
                this.ActiveControl = null;
                if (Clipboard.ContainsText())
                {
                    string clip = Clipboard.GetText().Trim();
                    _txtHex.Text = clip;
                    TryParseHexColor(clip);
                }
            };

            pnlHex.Controls.Add(lblHexTag);
            pnlHex.Controls.Add(_txtHex);
            pnlHex.Controls.Add(btnCopyHex);
            pnlHex.Controls.Add(btnPasteHex);
            _leftDock.Controls.Add(pnlHex);

            var pnlSwatches = new FlowLayoutPanel { Dock = DockStyle.Top, Height = 58, Padding = new Padding(2) };
            Color[] quickColors = new Color[] {
                Color.FromArgb(20, 20, 25),
                Color.White,
                Color.FromArgb(120, 120, 130),
                Color.FromArgb(255, 85, 85),
                Color.FromArgb(235, 45, 80),
                Color.FromArgb(255, 140, 0),
                Color.FromArgb(0, 160, 255),
                Color.FromArgb(40, 200, 100),
                Color.FromArgb(150, 80, 220),
                Color.FromArgb(255, 215, 120)
            };
            foreach (var c in quickColors)
            {
                var swatch = new Button { Width = 38, Height = 24, BackColor = c, FlatStyle = FlatStyle.Flat, Margin = new Padding(2), TabStop = false };
                Color captureColor = c;
                swatch.Click += (s, e) => {
                    this.ActiveControl = null;
                    SetCurrentColor(captureColor, true);
                };
                pnlSwatches.Controls.Add(swatch);
            }
            _leftDock.Controls.Add(pnlSwatches);

            Controls.Add(_leftDock);
        }

        private void SetCurrentColor(Color c, bool updateHexText)
        {
            _brush.Color = c;
            _btnCurrentColor.BackColor = c;
            _btnCurrentColor.ForeColor = (c.R * 0.299 + c.G * 0.587 + c.B * 0.114) > 140 ? Color.Black : Color.White;
            if (updateHexText && _txtHex != null)
            {
                _suppressHexUpdate = true;
                _txtHex.Text = string.Format("#{0:X2}{1:X2}{2:X2}", c.R, c.G, c.B);
                _suppressHexUpdate = false;
            }
        }

        private void TryParseHexColor(string hex)
        {
            if (string.IsNullOrEmpty(hex)) return;
            hex = hex.Trim().TrimStart('#');
            if (hex.Length == 6)
            {
                int r, g, b;
                if (int.TryParse(hex.Substring(0, 2), System.Globalization.NumberStyles.HexNumber, null, out r) &&
                    int.TryParse(hex.Substring(2, 2), System.Globalization.NumberStyles.HexNumber, null, out g) &&
                    int.TryParse(hex.Substring(4, 2), System.Globalization.NumberStyles.HexNumber, null, out b))
                {
                    SetCurrentColor(Color.FromArgb(255, r, g, b), false);
                }
            }
            else if (hex.Length == 3)
            {
                int r, g, b;
                if (int.TryParse(new string(hex[0], 2), System.Globalization.NumberStyles.HexNumber, null, out r) &&
                    int.TryParse(new string(hex[1], 2), System.Globalization.NumberStyles.HexNumber, null, out g) &&
                    int.TryParse(new string(hex[2], 2), System.Globalization.NumberStyles.HexNumber, null, out b))
                {
                    SetCurrentColor(Color.FromArgb(255, r, g, b), false);
                }
            }
        }

        private void AddDockSeparator(string title)
        {
            var lbl = new Label
            {
                Text = title,
                Dock = DockStyle.Top,
                Height = 22,
                ForeColor = Color.FromArgb(120, 140, 160),
                Font = new Font("Segoe UI", 7.5f, FontStyle.Bold),
                TextAlign = ContentAlignment.BottomLeft
            };
            _leftDock.Controls.Add(lbl);
        }

        private void ShowCanvasSizeDialog()
        {
            using (var dlg = new CanvasSizeDialog(CanvasWidth, CanvasHeight))
            {
                if (dlg.ShowDialog(this) == DialogResult.OK)
                {
                    SetCanvasSize(dlg.SelectedWidth, dlg.SelectedHeight, dlg.ClearCanvas);
                }
            }
        }

        public void SetCanvasSize(int newWidth, int newHeight, bool clear)
        {
            CanvasWidth = newWidth;
            CanvasHeight = newHeight;
            _lblCanvasDimensions.Text = string.Format("Lienzo: {0}x{1} px", CanvasWidth, CanvasHeight);

            _bgLayer.InitBitmap(CanvasWidth, CanvasHeight, Color.White);
            _drawLayer.InitBitmap(CanvasWidth, CanvasHeight, Color.Transparent);

            foreach (var action in _undoStack) action.Dispose();
            foreach (var action in _redoStack) action.Dispose();
            _undoStack.Clear();
            _redoStack.Clear();

            CenterCanvasView();
            Invalidate();
        }

        private void CreateQuickToolbar()
        {
            var menu = new MenuStrip();
            menu.BackColor = Color.FromArgb(32, 32, 38);
            menu.ForeColor = Color.WhiteSmoke;

            var fileMenu = new ToolStripMenuItem("Archivo (&A)");
            var newCanvas = new ToolStripMenuItem("Nuevo Lienzo / Tamaño... (Ctrl+N)", null, (s, e) => ShowCanvasSizeDialog());
            var exportPng = new ToolStripMenuItem("Exportar a PNG...", null, (s, e) => ExportCanvasPng());
            var resetCanvas = new ToolStripMenuItem("Limpiar Capa de Dibujo", null, (s, e) => {
                _drawLayer.Clear(Color.Transparent);
                Invalidate();
            });
            fileMenu.DropDownItems.Add(newCanvas);
            fileMenu.DropDownItems.Add(exportPng);
            fileMenu.DropDownItems.Add(resetCanvas);

            var editMenu = new ToolStripMenuItem("Edición (&E)");
            var undoItem = new ToolStripMenuItem("Deshacer (Ctrl+Z)", null, (s, e) => Undo());
            var redoItem = new ToolStripMenuItem("Rehacer (Ctrl+Y)", null, (s, e) => Redo());
            editMenu.DropDownItems.Add(undoItem);
            editMenu.DropDownItems.Add(redoItem);

            var canvasMenu = new ToolStripMenuItem("Lienzo (&L)");
            var sizeItem = new ToolStripMenuItem("Cambiar Tamaño del Lienzo...", null, (s, e) => ShowCanvasSizeDialog());
            var fitView = new ToolStripMenuItem("Ajustar a Pantalla (Ctrl+0)", null, (s, e) => CenterCanvasView());
            var rotate90 = new ToolStripMenuItem("Rotar 90° (R)", null, (s, e) => {
                CanvasRotation = (CanvasRotation + 90.0f) % 360.0f;
                Invalidate();
            });
            canvasMenu.DropDownItems.Add(sizeItem);
            canvasMenu.DropDownItems.Add(fitView);
            canvasMenu.DropDownItems.Add(rotate90);

            var viewMenu = new ToolStripMenuItem("Ver (&V)");
            var gridItem = new ToolStripMenuItem("Mostrar Cuadrícula de Guía", null, (s, e) => {
                ShowGrid = !ShowGrid;
                ((ToolStripMenuItem)s).Checked = ShowGrid;
                Invalidate();
            });
            gridItem.Checked = ShowGrid;

            var refItem = new ToolStripMenuItem("Ventana de Referencia (F4)", null, (s, e) => {
                _refBox.Visible = !_refBox.Visible;
                ((ToolStripMenuItem)s).Checked = _refBox.Visible;
            });
            refItem.Checked = _refBox != null && _refBox.Visible;

            viewMenu.DropDownItems.Add(gridItem);
            viewMenu.DropDownItems.Add(refItem);

            menu.Items.Add(fileMenu);
            menu.Items.Add(editMenu);
            menu.Items.Add(canvasMenu);
            menu.Items.Add(viewMenu);

            MainMenuStrip = menu;
            Controls.Add(menu);
        }

        // =====================================================================
        // CAPTURA NATIVA DE LÁPIZ VIA WNDPROC (WM_POINTER)
        // =====================================================================
        protected override void WndProc(ref Message m)
        {
            switch (m.Msg)
            {
                case NativePen.WM_POINTERDOWN:
                {
                    uint pointerId = (uint)(m.WParam.ToInt64() & 0xFFFF);
                    var penInfo = new NativePen.POINTER_PEN_INFO();
                    if (NativePen.GetPointerPenInfo(pointerId, ref penInfo))
                    {
                        Point clientPt = PointToClient(new Point(penInfo.pointerInfo.ptPixelLocation.X, penInfo.pointerInfo.ptPixelLocation.Y));
                        
                        if (_isSpaceDown || (penInfo.penFlags & 0x01) != 0)
                        {
                            _isPanning = true;
                            _lastMousePos = clientPt;
                            Cursor = Cursors.SizeAll;
                            m.Result = IntPtr.Zero;
                            return;
                        }

                        float pressure = (penInfo.penMask & NativePen.PEN_MASK_PRESSURE) != 0 
                            ? (float)penInfo.pressure / 1024.0f 
                            : 0.8f;
                        if (pressure > 1.0f) pressure /= 4.0f;

                        StartStroke(clientPt.X, clientPt.Y, pressure, penInfo.tiltX, penInfo.tiltY);
                        m.Result = IntPtr.Zero;
                        return;
                    }
                    break;
                }
                case NativePen.WM_POINTERUPDATE:
                {
                    uint pointerId = (uint)(m.WParam.ToInt64() & 0xFFFF);
                    var penInfo = new NativePen.POINTER_PEN_INFO();
                    if (NativePen.GetPointerPenInfo(pointerId, ref penInfo))
                    {
                        Point clientPt = PointToClient(new Point(penInfo.pointerInfo.ptPixelLocation.X, penInfo.pointerInfo.ptPixelLocation.Y));

                        if (_isPanning)
                        {
                            PanX += (clientPt.X - _lastMousePos.X);
                            PanY += (clientPt.Y - _lastMousePos.Y);
                            _lastMousePos = clientPt;
                            Invalidate();
                            m.Result = IntPtr.Zero;
                            return;
                        }

                        float pressure = (penInfo.penMask & NativePen.PEN_MASK_PRESSURE) != 0 
                            ? (float)penInfo.pressure / 1024.0f 
                            : 0.8f;
                        if (pressure > 1.0f) pressure /= 4.0f;

                        UpdateStroke(clientPt.X, clientPt.Y, pressure, penInfo.tiltX, penInfo.tiltY);
                        m.Result = IntPtr.Zero;
                        return;
                    }
                    break;
                }
                case NativePen.WM_POINTERUP:
                {
                    if (_isPanning)
                    {
                        _isPanning = false;
                        Cursor = _isSpaceDown ? Cursors.Hand : Cursors.Default;
                    }
                    EndStroke();
                    m.Result = IntPtr.Zero;
                    return;
                }
            }

            base.WndProc(ref m);
        }

        public PointF ScreenToCanvas(float screenX, float screenY)
        {
            float dx = screenX - PanX;
            float dy = screenY - PanY;

            float rad = -CanvasRotation * (float)Math.PI / 180.0f;
            float cos = (float)Math.Cos(rad);
            float sin = (float)Math.Sin(rad);

            float rx = dx * cos - dy * sin;
            float ry = dx * sin + dy * cos;

            return new PointF(rx / Zoom, ry / Zoom);
        }

        public PointF CanvasToScreen(float canvasX, float canvasY)
        {
            float zx = canvasX * Zoom;
            float zy = canvasY * Zoom;

            float rad = CanvasRotation * (float)Math.PI / 180.0f;
            float cos = (float)Math.Cos(rad);
            float sin = (float)Math.Sin(rad);

            float rx = zx * cos - zy * sin;
            float ry = zx * sin + zy * cos;

            return new PointF(rx + PanX, ry + PanY);
        }

        private void StartStroke(float screenX, float screenY, float pressure, float tiltX = 0, float tiltY = 0)
        {
            _isStroking = true;
            _strokeMinX = float.MaxValue;
            _strokeMinY = float.MaxValue;
            _strokeMaxX = float.MinValue;
            _strokeMaxY = float.MinValue;

            // Clonar copia de respaldo completa para capturar el área antes del trazo
            if (_preStrokeLayerSnapshot != null) _preStrokeLayerSnapshot.Dispose();
            _preStrokeLayerSnapshot = (Bitmap)_drawLayer.Bitmap.Clone();

            PointF canvasPt = ScreenToCanvas(screenX, screenY);
            var strokePt = new StrokePoint(canvasPt.X, canvasPt.Y, pressure, tiltX, tiltY, DateTime.UtcNow.Ticks);
            _brush.BeginStroke(strokePt);

            var dabs = _brush.ProcessPoint(strokePt);
            RenderDabsDirect(dabs);
        }

        private void UpdateStroke(float screenX, float screenY, float pressure, float tiltX = 0, float tiltY = 0)
        {
            if (!_isStroking) return;

            PointF canvasPt = ScreenToCanvas(screenX, screenY);
            var strokePt = new StrokePoint(canvasPt.X, canvasPt.Y, pressure, tiltX, tiltY, DateTime.UtcNow.Ticks);
            var dabs = _brush.ProcessPoint(strokePt);
            RenderDabsDirect(dabs);
        }

        private void EndStroke()
        {
            if (!_isStroking) return;
            _isStroking = false;
            _brush.EndStroke();

            // Registrar acción de Undo con el bounding box exacto del trazo
            if (_strokeMaxX >= _strokeMinX && _strokeMaxY >= _strokeMinY && _preStrokeLayerSnapshot != null)
            {
                int margin = (int)(_brush.BaseSize + 10);
                int bx = Math.Max(0, (int)Math.Floor(_strokeMinX) - margin);
                int by = Math.Max(0, (int)Math.Floor(_strokeMinY) - margin);
                int bw = Math.Min(CanvasWidth - bx, (int)Math.Ceiling(_strokeMaxX - _strokeMinX) + margin * 2);
                int bh = Math.Min(CanvasHeight - by, (int)Math.Ceiling(_strokeMaxY - _strokeMinY) + margin * 2);

                if (bw > 0 && bh > 0)
                {
                    Rectangle cropRect = new Rectangle(bx, by, bw, bh);
                    var action = new UndoStrokeAction
                    {
                        Layer = _drawLayer,
                        DirtyBounds = cropRect,
                        BeforeCrop = _preStrokeLayerSnapshot.Clone(cropRect, PixelFormat.Format32bppPArgb),
                        AfterCrop = _drawLayer.Bitmap.Clone(cropRect, PixelFormat.Format32bppPArgb)
                    };

                    _undoStack.Push(action);
                    foreach (var redo in _redoStack) redo.Dispose();
                    _redoStack.Clear();
                }
            }

            if (_preStrokeLayerSnapshot != null)
            {
                _preStrokeLayerSnapshot.Dispose();
                _preStrokeLayerSnapshot = null;
            }

            Invalidate();
        }

        private void RenderDabsDirect(List<StrokePoint> dabs)
        {
            if (dabs == null || dabs.Count == 0) return;

            float minCanvX = float.MaxValue, minCanvY = float.MaxValue;
            float maxCanvX = float.MinValue, maxCanvY = float.MinValue;

            foreach (var dab in dabs)
            {
                float radius = (_brush.BaseSize * Math.Max(_brush.ActivePreset.MinSizeFactor, dab.Pressure)) * 0.5f;

                float rX0 = dab.X - radius;
                float rY0 = dab.Y - radius;
                float rX1 = dab.X + radius;
                float rY1 = dab.Y + radius;

                if (rX0 < _strokeMinX) _strokeMinX = rX0;
                if (rY0 < _strokeMinY) _strokeMinY = rY0;
                if (rX1 > _strokeMaxX) _strokeMaxX = rX1;
                if (rY1 > _strokeMaxY) _strokeMaxY = rY1;

                if (rX0 < minCanvX) minCanvX = rX0;
                if (rY0 < minCanvY) minCanvY = rY0;
                if (rX1 > maxCanvX) maxCanvX = rX1;
                if (rY1 > maxCanvY) maxCanvY = rY1;

                // Estampar directamente sobre el Graphics continuo de la capa
                _brush.StampDabDirect(_drawLayer.Graphics, dab.X, dab.Y, dab.Pressure);
            }

            // Transformar solo las 4 esquinas del bounding box una sola vez para Invalidate
            PointF c1 = CanvasToScreen(minCanvX, minCanvY);
            PointF c2 = CanvasToScreen(maxCanvX, minCanvY);
            PointF c3 = CanvasToScreen(minCanvX, maxCanvY);
            PointF c4 = CanvasToScreen(maxCanvX, maxCanvY);

            float minScrX = Math.Min(Math.Min(c1.X, c2.X), Math.Min(c3.X, c4.X));
            float minScrY = Math.Min(Math.Min(c1.Y, c2.Y), Math.Min(c3.Y, c4.Y));
            float maxScrX = Math.Max(Math.Max(c1.X, c2.X), Math.Max(c3.X, c4.X));
            float maxScrY = Math.Max(Math.Max(c1.Y, c2.Y), Math.Max(c3.Y, c4.Y));

            int margin = 8;
            int ix = Math.Max(_leftDock.Width, (int)Math.Floor(minScrX) - margin);
            int iy = Math.Max(0, (int)Math.Floor(minScrY) - margin);
            int iw = Math.Min(ClientSize.Width - ix, (int)Math.Ceiling(maxScrX - minScrX) + margin * 2);
            int ih = Math.Min(ClientSize.Height - iy, (int)Math.Ceiling(maxScrY - minScrY) + margin * 2);

            if (iw > 0 && ih > 0)
            {
                Invalidate(new Rectangle(ix, iy, iw, ih));
            }
        }

        protected override void OnKeyDown(KeyEventArgs e)
        {
            base.OnKeyDown(e);

            if (e.Control && e.KeyCode == Keys.Z)
            {
                Undo();
                e.Handled = true;
            }
            else if (e.Control && e.KeyCode == Keys.Y)
            {
                Redo();
                e.Handled = true;
            }
            else if (e.Control && e.KeyCode == Keys.N)
            {
                ShowCanvasSizeDialog();
                e.Handled = true;
            }
            else if (e.Control && e.KeyCode == Keys.D0)
            {
                CenterCanvasView();
                e.Handled = true;
            }
            else if (e.KeyCode == Keys.R)
            {
                CanvasRotation = (CanvasRotation + 15.0f) % 360.0f;
                Invalidate();
            }
            else if (e.KeyCode == Keys.F4)
            {
                _refBox.Visible = !_refBox.Visible;
                e.Handled = true;
            }
            else if (e.KeyCode == Keys.OemOpenBrackets || e.KeyCode == Keys.D1)
            {
                _brush.BaseSize = Math.Max(1.0f, _brush.BaseSize - 3.0f);
                _lblSizeValue.Text = string.Format("{0:F0} px", _brush.BaseSize);
                Invalidate();
            }
            else if (e.KeyCode == Keys.OemCloseBrackets || e.KeyCode == Keys.D2)
            {
                _brush.BaseSize = Math.Min(250.0f, _brush.BaseSize + 3.0f);
                _lblSizeValue.Text = string.Format("{0:F0} px", _brush.BaseSize);
                Invalidate();
            }
            else if (e.KeyCode == Keys.E)
            {
                if (_brush.ActivePreset.IsEraser)
                {
                    _brush.SetPreset(BrushPreset.GetAllPresets()[1]); // Pluma (Fuerte)
                }
                else
                {
                    _brush.SetPreset(BrushPreset.GetAllPresets().Find(p => p.Id == "eraser_precision"));
                }
                _btnActiveBrush.Text = "🖌️ " + _brush.ActivePreset.Name + " ▼";
                Invalidate();
            }
        }

        protected override void OnKeyUp(KeyEventArgs e)
        {
            base.OnKeyUp(e);

            if (e.KeyCode == Keys.Space)
            {
                _isSpaceDown = false;
                if (!_isPanning) Cursor = Cursors.Default;
            }
        }

        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e);

            if (e.X < _leftDock.Width) return;
            this.ActiveControl = null;

            bool isMiddleButton = (e.Button == MouseButtons.Middle) || (NativePen.GetAsyncKeyState(NativePen.VK_MBUTTON) < 0);
            bool isSpaceDrag = (e.Button == MouseButtons.Left) && (_isSpaceDown || (NativePen.GetAsyncKeyState(NativePen.VK_SPACE) < 0));
            bool isRightDrag = (e.Button == MouseButtons.Right);

            if (isMiddleButton || isSpaceDrag || isRightDrag)
            {
                _isPanning = true;
                _lastMousePos = e.Location;
                Cursor = Cursors.SizeAll;
                return;
            }

            // Para ratón: Presión constante 1.0f para mantener el trazo con su grosor completo y sólido
            if (e.Button == MouseButtons.Left && !_isStroking)
            {
                StartStroke(e.X, e.Y, 1.0f);
            }
        }

        protected override void OnMouseMove(MouseEventArgs e)
        {
            base.OnMouseMove(e);

            if (_isPanning)
            {
                PanX += (e.X - _lastMousePos.X);
                PanY += (e.Y - _lastMousePos.Y);
                _lastMousePos = e.Location;
                Invalidate();
                return;
            }

            // Ratón: Presión siempre al 100% (1.0f) para que jamás se adelgace al mover rápido
            if (_isStroking && e.Button == MouseButtons.Left)
            {
                _lastMousePos = e.Location;
                UpdateStroke(e.X, e.Y, 1.0f);
            }
        }

        protected override void OnMouseUp(MouseEventArgs e)
        {
            base.OnMouseUp(e);

            if (_isPanning)
            {
                _isPanning = false;
                Cursor = _isSpaceDown ? Cursors.Hand : Cursors.Default;
                return;
            }

            if (_isStroking && e.Button == MouseButtons.Left)
            {
                EndStroke();
            }
        }

        protected override void OnMouseWheel(MouseEventArgs e)
        {
            base.OnMouseWheel(e);

            float oldZoom = Zoom;
            float zoomFactor = e.Delta > 0 ? 1.15f : (1.0f / 1.15f);
            float newZoom = Math.Max(0.01f, Math.Min(64.0f, oldZoom * zoomFactor));

            PointF canvasBefore = ScreenToCanvas(e.X, e.Y);
            Zoom = newZoom;
            PointF screenAfter = CanvasToScreen(canvasBefore.X, canvasBefore.Y);

            PanX += (e.X - screenAfter.X);
            PanY += (e.Y - screenAfter.Y);

            Invalidate();
        }

        private void Undo()
        {
            if (_undoStack.Count > 0)
            {
                var action = _undoStack.Pop();
                action.Undo();
                _redoStack.Push(action);
                Invalidate();
            }
        }

        private void Redo()
        {
            if (_redoStack.Count > 0)
            {
                var action = _redoStack.Pop();
                action.Redo();
                _undoStack.Push(action);
                Invalidate();
            }
        }

        // =====================================================================
        // RENDERIZADO DEL VIEWPORT CONTINUO (SIN CUADRÍCULAS NI LÍNEAS RESIDUALES)
        // =====================================================================
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics;
            g.PixelOffsetMode = PixelOffsetMode.Half;
            g.InterpolationMode = Zoom < 1.0f ? InterpolationMode.Bilinear : InterpolationMode.NearestNeighbor;

            var state = g.Save();
            g.TranslateTransform(PanX, PanY);
            g.RotateTransform(CanvasRotation);
            g.ScaleTransform(Zoom, Zoom);

            // Sombra suave limpia
            using (var shadowBrush = new SolidBrush(Color.FromArgb(10, 10, 14)))
            {
                g.FillRectangle(shadowBrush, 6, 6, CanvasWidth, CanvasHeight);
            }

            // Fondo blanco continuo
            if (_bgLayer != null && _bgLayer.Visible)
            {
                g.DrawImage(_bgLayer.Bitmap, new Rectangle(0, 0, CanvasWidth, CanvasHeight), 0, 0, CanvasWidth, CanvasHeight, GraphicsUnit.Pixel);
            }

            // Capa de dibujo continua (¡CERO líneas blancas entrecortadas ni costuras!)
            if (_drawLayer != null && _drawLayer.Visible)
            {
                g.DrawImage(_drawLayer.Bitmap, new Rectangle(0, 0, CanvasWidth, CanvasHeight), 0, 0, CanvasWidth, CanvasHeight, GraphicsUnit.Pixel);
            }

            // Cuadrícula opcional si el usuario la activa en el menú
            if (ShowGrid)
            {
                int gridStep = 100;
                using (var gridPen = new Pen(Color.FromArgb(40, 0, 120, 255), 1.0f / Zoom))
                {
                    for (int x = 0; x <= CanvasWidth; x += gridStep)
                    {
                        g.DrawLine(gridPen, x, 0, x, CanvasHeight);
                    }
                    for (int y = 0; y <= CanvasHeight; y += gridStep)
                    {
                        g.DrawLine(gridPen, 0, y, CanvasWidth, y);
                    }
                }
            }

            // Marco del lienzo
            using (var borderPen = new Pen(Color.FromArgb(70, 70, 85), 1.5f / Zoom))
            {
                g.DrawRectangle(borderPen, 0, 0, CanvasWidth, CanvasHeight);
            }

            g.Restore(state);
        }

        private void ExportCanvasPng()
        {
            using (var sfd = new SaveFileDialog())
            {
                sfd.Filter = "PNG Image (*.png)|*.png";
                sfd.FileName = "Ilustracion_AuraPaint.png";
                if (sfd.ShowDialog() == DialogResult.OK)
                {
                    using (var fullCanvas = new Bitmap(CanvasWidth, CanvasHeight, PixelFormat.Format32bppArgb))
                    using (var g = Graphics.FromImage(fullCanvas))
                    {
                        g.Clear(Color.White);
                        if (_bgLayer != null && _bgLayer.Visible) g.DrawImage(_bgLayer.Bitmap, 0, 0);
                        if (_drawLayer != null && _drawLayer.Visible) g.DrawImage(_drawLayer.Bitmap, 0, 0);
                        fullCanvas.Save(sfd.FileName, ImageFormat.Png);
                    }
                    MessageBox.Show("¡Lienzo exportado exitosamente a PNG!", "Exportación Completada", MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
            }
        }

        [STAThread]
        public static void Main()
        {
            try
            {
                Application.EnableVisualStyles();
                Application.SetCompatibleTextRenderingDefault(false);
                Application.Run(new CanvasForm());
            }
            catch (Exception ex)
            {
                File.WriteAllText("crash_log.txt", ex.ToString());
                MessageBox.Show(ex.ToString(), "Error al iniciar AuraPaint", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
