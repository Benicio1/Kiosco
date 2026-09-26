using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace AuraPaint.UI
{
    public enum IbisToolMode
    {
        Transform,      // Transformar
        MagicWand,      // Varita mágica
        Lasso,          // Lazo
        Filter,         // Filtro (FX)
        Brush,          // Pincel
        Eraser,         // Borrador
        Smudge,         // Difuminar
        Blur,           // Borrosidad
        SpecialPen,     // Pluma Especial
        Fill,           // Relleno
        Vector,         // Vector
        Text,           // Texto
        FrameDivider,   // Divisor de viñetas
        Eyedropper,     // Cuentagotas
        CanvasSettings  // Lienzo
    }

    public class IbisToolbar : Panel
    {
        public IbisToolMode ActiveTool { get; private set; }
        public event Action<IbisToolMode> OnToolSelected;

        private class ToolItem
        {
            public IbisToolMode Mode;
            public string Label;
            public string IconText;
            public Rectangle Bounds;
        }

        private readonly ToolItem[] _tools;
        private int _hoveredIndex = -1;

        public IbisToolbar()
        {
            _tools = new ToolItem[]
            {
                new ToolItem { Mode = IbisToolMode.Transform,      Label = "Transformar", IconText = "✥" },
                new ToolItem { Mode = IbisToolMode.MagicWand,      Label = "Varita mágica", IconText = "🪄" },
                new ToolItem { Mode = IbisToolMode.Lasso,          Label = "Lazo",        IconText = "➿" },
                new ToolItem { Mode = IbisToolMode.Filter,         Label = "Filtro",      IconText = "FX" },
                new ToolItem { Mode = IbisToolMode.Brush,          Label = "Pincel",      IconText = "🖌️" },
                new ToolItem { Mode = IbisToolMode.Eraser,         Label = "Borrador",    IconText = "🧹" },
                new ToolItem { Mode = IbisToolMode.Smudge,         Label = "Difuminar",   IconText = "👆" },
                new ToolItem { Mode = IbisToolMode.Blur,           Label = "Borrosidad",  IconText = "💧" },
                new ToolItem { Mode = IbisToolMode.SpecialPen,     Label = "Pluma Especial", IconText = "✨" },
                new ToolItem { Mode = IbisToolMode.Fill,           Label = "Relleno",     IconText = "🪣" },
                new ToolItem { Mode = IbisToolMode.Vector,         Label = "Vector",      IconText = "↗️" },
                new ToolItem { Mode = IbisToolMode.Text,           Label = "Texto",       IconText = "𝐓" },
                new ToolItem { Mode = IbisToolMode.FrameDivider,   Label = "Viñetas",     IconText = "⊞" },
                new ToolItem { Mode = IbisToolMode.Eyedropper,     Label = "Cuentagotas", IconText = "🧪" },
                new ToolItem { Mode = IbisToolMode.CanvasSettings, Label = "Lienzo",      IconText = "📐" }
            };

            Width = 62;
            Dock = DockStyle.Left;
            BackColor = Color.FromArgb(60, 60, 64); // Gris característico de ibis Paint X
            DoubleBuffered = true;
            ActiveTool = IbisToolMode.Brush;
            Cursor = Cursors.Hand;
        }

        public void SetActiveTool(IbisToolMode tool)
        {
            ActiveTool = tool;
            Invalidate();
        }

        protected override void OnResize(EventArgs eventargs)
        {
            base.OnResize(eventargs);
            if (_tools == null || _tools.Length == 0) return;
            int itemH = Math.Max(42, (Height - 10) / _tools.Length);
            for (int i = 0; i < _tools.Length; i++)
            {
                _tools[i].Bounds = new Rectangle(0, i * itemH + 2, Width, itemH);
            }
            Invalidate();
        }

        protected override void OnMouseMove(MouseEventArgs e)
        {
            base.OnMouseMove(e);
            int prev = _hoveredIndex;
            _hoveredIndex = -1;
            for (int i = 0; i < _tools.Length; i++)
            {
                if (_tools[i].Bounds.Contains(e.Location))
                {
                    _hoveredIndex = i;
                    break;
                }
            }
            if (prev != _hoveredIndex) Invalidate();
        }

        protected override void OnMouseLeave(EventArgs e)
        {
            base.OnMouseLeave(e);
            _hoveredIndex = -1;
            Invalidate();
        }

        protected override void OnMouseClick(MouseEventArgs e)
        {
            base.OnMouseClick(e);
            for (int i = 0; i < _tools.Length; i++)
            {
                if (_tools[i].Bounds.Contains(e.Location))
                {
                    ActiveTool = _tools[i].Mode;
                    Invalidate();
                    if (OnToolSelected != null) OnToolSelected(ActiveTool);
                    break;
                }
            }
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e);
            var g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;
            g.TextRenderingHint = System.Drawing.Text.TextRenderingHint.ClearTypeGridFit;

            // Borde derecho divisorio
            using (var pen = new Pen(Color.FromArgb(45, 45, 50)))
            {
                g.DrawLine(pen, Width - 1, 0, Width - 1, Height);
            }

            for (int i = 0; i < _tools.Length; i++)
            {
                var item = _tools[i];
                bool isActive = (item.Mode == ActiveTool);
                bool isHover = (i == _hoveredIndex);

                if (isActive)
                {
                    // Resaltado azul ibis Paint X
                    using (var bgBrush = new SolidBrush(Color.FromArgb(40, 48, 62)))
                    {
                        g.FillRectangle(bgBrush, item.Bounds);
                    }
                    using (var barBrush = new SolidBrush(Color.FromArgb(0, 160, 255)))
                    {
                        g.FillRectangle(barBrush, 0, item.Bounds.Top, 4, item.Bounds.Height);
                    }
                }
                else if (isHover)
                {
                    using (var hoverBrush = new SolidBrush(Color.FromArgb(75, 75, 82)))
                    {
                        g.FillRectangle(hoverBrush, item.Bounds);
                    }
                }

                // Icono
                Color iconColor = isActive ? Color.FromArgb(70, 175, 255) : Color.White;
                using (var iconFont = new Font("Segoe UI Emoji", 13.5f, FontStyle.Regular))
                using (var brush = new SolidBrush(iconColor))
                {
                    var sf = new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Center };
                    var iconRect = new Rectangle(item.Bounds.Left, item.Bounds.Top + 2, item.Bounds.Width, item.Bounds.Height - 16);
                    g.DrawString(item.IconText, iconFont, brush, iconRect, sf);
                }

                // Etiqueta de texto inferior
                Color labelColor = isActive ? Color.FromArgb(100, 190, 255) : Color.FromArgb(215, 215, 225);
                using (var labelFont = new Font("Segoe UI", 6.8f, FontStyle.Regular))
                using (var brush = new SolidBrush(labelColor))
                {
                    var sf = new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Far };
                    var labelRect = new Rectangle(item.Bounds.Left, item.Bounds.Top, item.Bounds.Width, item.Bounds.Height - 3);
                    g.DrawString(item.Label, labelFont, brush, labelRect, sf);
                }
            }
        }
    }
}
