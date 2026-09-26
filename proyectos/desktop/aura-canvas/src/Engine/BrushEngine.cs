using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using AuraPaint.Models;

namespace AuraPaint.Engine
{
    public class BrushEngine
    {
        public BrushPreset ActivePreset { get; private set; }
        public float BaseSize { get; set; }
        public float Flow { get; set; }
        public Color Color { get; set; }

        private readonly List<StrokePoint> _rawPoints = new List<StrokePoint>();
        private float _carryDistance = 0.0f;
        private readonly Random _rand = new Random();

        public BrushEngine()
        {
            var presets = BrushPreset.GetAllPresets();
            ActivePreset = presets[0]; // Pluma suave por defecto
            BaseSize = ActivePreset.Size;
            Flow = ActivePreset.DefaultFlow;
            Color = Color.FromArgb(220, 20, 45); // Rojo elegante por defecto
        }

        public void SetPreset(BrushPreset preset)
        {
            if (preset == null) return;
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

            // SALPICADURA / PARTICULAS
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

            // TRAMA SEMITONO CÓMIC
            if (ActivePreset.IsHalftone)
            {
                int dotSpacing = 8;
                int gridX = ((int)Math.Floor(canvasX / dotSpacing)) * dotSpacing;
                int gridY = ((int)Math.Floor(canvasY / dotSpacing)) * dotSpacing;
                float dotRadius = Math.Max(1.0f, radius * 0.35f);
                using (var brush = new SolidBrush(Color.FromArgb(baseAlpha, Color)))
                {
                    g.FillEllipse(brush, gridX - dotRadius, gridY - dotRadius, dotRadius * 2, dotRadius * 2);
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
                float jitter = (radius * 0.25f);
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

            // ESTÁNDAR
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

                float x = 0.5f * ((2 * p1.X) + (-p0.X + p2.X) * t + (2 * p0.X - 5 * p1.X + 4 * p2.X - p3.X) * t2 + (-p0.X + 3 * p1.X - 3 * p2.X + p3.X) * t3);
                float y = 0.5f * ((2 * p1.Y) + (-p0.Y + p2.Y) * t + (2 * p0.Y - 5 * p1.Y + 4 * p2.Y - p3.Y) * t2 + (-p0.Y + 3 * p1.Y - 3 * p2.Y + p3.Y) * t3);

                float pressure = (float)Math.Sin(t * Math.PI);
                float thickness = Math.Max(1.2f, maxThickness * (preset.MinSizeFactor + (1.0f - preset.MinSizeFactor) * pressure));

                int alpha = preset.IsEraser ? 120 : (int)(255 * preset.DefaultFlow * (0.4f + 0.6f * pressure));
                Color strokeColor = preset.IsEraser ? Color.FromArgb(140, 150, 165) : color;

                using (var pen = new Pen(Color.FromArgb(Math.Max(10, Math.Min(255, alpha)), strokeColor), thickness))
                {
                    pen.StartCap = LineCap.Round;
                    pen.EndCap = LineCap.Round;
                    g.DrawLine(pen, prevPt, new PointF(x, y));
                }

                prevPt = new PointF(x, y);
            }
        }
    }
}
