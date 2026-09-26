using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;
using AuraPaint.Engine;
using AuraPaint.Models;

namespace AuraPaint.UI
{
    public class IbisBrushDialog : Form
    {
        public BrushPreset SelectedPreset { get; private set; }
        public float SelectedSize { get; private set; }
        public float SelectedFlow { get; private set; }

        private readonly Color _activeColor;
        private BrushCategory _activeCategory = BrushCategory.All;
        private readonly List<BrushPreset> _allPresets;

        // UI Controls
        private Panel _categoryPanel;
        private Panel _brushListPanel;
        private Panel _inspectorPanel;

        private Label _lblBrushTitle;
        private PictureBox _previewBox;
        private TrackBar _tbGrosor;
        private Label _lblGrosorVal;
        private TrackBar _tbOpacidad;
        private Label _lblOpacidadVal;
        private readonly List<Panel> _brushRowControls = new List<Panel>();

        public IbisBrushDialog(BrushPreset currentPreset, float currentSize, float currentFlow, Color activeColor)
        {
            SelectedPreset = currentPreset;
            SelectedSize = currentSize;
            SelectedFlow = currentFlow;
            _activeColor = activeColor;
            _allPresets = BrushPreset.GetAllPresets();

            Text = "Pincel — ibis Paint X Studio";
            StartPosition = FormStartPosition.CenterParent;
            Width = 920;
            Height = 620;
            BackColor = Color.FromArgb(40, 40, 46);
            FormBorderStyle = FormBorderStyle.FixedDialog;
            MaximizeBox = false;
            MinimizeBox = false;
            ShowInTaskbar = false;
            DoubleBuffered = true;

            InitializeLayout();
        }

        private void InitializeLayout()
        {
            // 1. Barra Superior con pestañas
            var pnlTop = new Panel { Dock = DockStyle.Top, Height = 42, BackColor = Color.FromArgb(50, 50, 58) };
            var lblTitle = new Label { Text = "Pincel", Left = 16, Top = 10, Font = new Font("Segoe UI", 11.5f, FontStyle.Bold), ForeColor = Color.White, AutoSize = true };
            pnlTop.Controls.Add(lblTitle);

            var btnBasico = new Button { Text = "Básico", Left = 150, Top = 7, Width = 85, Height = 28, FlatStyle = FlatStyle.Flat, BackColor = Color.White, ForeColor = Color.Black, Font = new Font("Segoe UI", 8.5f, FontStyle.Bold), TabStop = false };
            var btnPersonalizado = new Button { Text = "Personalizado", Left = 240, Top = 7, Width = 105, Height = 28, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(60, 60, 70), ForeColor = Color.LightGray, Font = new Font("Segoe UI", 8.5f), TabStop = false };
            var btnEnLinea = new Button { Text = "En línea", Left = 350, Top = 7, Width = 85, Height = 28, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(60, 60, 70), ForeColor = Color.LightGray, Font = new Font("Segoe UI", 8.5f), TabStop = false };

            pnlTop.Controls.Add(btnBasico);
            pnlTop.Controls.Add(btnPersonalizado);
            pnlTop.Controls.Add(btnEnLinea);
            Controls.Add(pnlTop);

            // 2. Columna Izquierda: Categorías
            _categoryPanel = new Panel
            {
                Dock = DockStyle.Left,
                Width = 140,
                BackColor = Color.FromArgb(20, 20, 24),
                AutoScroll = true
            };
            PopulateCategories();
            Controls.Add(_categoryPanel);

            // 3. Columna Derecha: Inspector & Previsualización
            _inspectorPanel = new Panel
            {
                Dock = DockStyle.Right,
                Width = 290,
                BackColor = Color.FromArgb(32, 32, 38)
            };
            BuildRightInspector();
            Controls.Add(_inspectorPanel);

            // 4. Columna Central: Lista de Pinceles
            _brushListPanel = new Panel
            {
                Dock = DockStyle.Fill,
                BackColor = Color.White,
                AutoScroll = true
            };
            Controls.Add(_brushListPanel);

            PopulateBrushList();
            UpdateRightInspector();
        }

        private void PopulateCategories()
        {
            var categories = new[]
            {
                new { Cat = BrushCategory.All,         Name = "Todos" },
                new { Cat = BrushCategory.Basic,       Name = "Básico / Pluma" },
                new { Cat = BrushCategory.Sketch,      Name = "Bosquejo" },
                new { Cat = BrushCategory.Comic,       Name = "Cómic & Manga" },
                new { Cat = BrushCategory.Airbrush,    Name = "Aerógrafo" },
                new { Cat = BrushCategory.Watercolor,  Name = "Acuarela" },
                new { Cat = BrushCategory.Paint,       Name = "Pintura & Óleo" },
                new { Cat = BrushCategory.Special,     Name = "Efectos & Trama" },
                new { Cat = BrushCategory.Erasers,     Name = "Borradores" }
            };

            int y = 6;
            foreach (var item in categories)
            {
                var btn = new Button
                {
                    Text = item.Name,
                    Left = 4,
                    Top = y,
                    Width = 130,
                    Height = 34,
                    FlatStyle = FlatStyle.Flat,
                    BackColor = (item.Cat == _activeCategory) ? Color.FromArgb(45, 90, 140) : Color.FromArgb(28, 28, 34),
                    ForeColor = (item.Cat == _activeCategory) ? Color.White : Color.FromArgb(180, 180, 190),
                    Font = new Font("Segoe UI", 8.5f, FontStyle.Bold),
                    TextAlign = ContentAlignment.MiddleLeft,
                    Margin = new Padding(2),
                    TabStop = false,
                    Cursor = Cursors.Hand
                };
                btn.FlatAppearance.BorderSize = 0;
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
                y += 38;
            }
        }

        private void PopulateBrushList()
        {
            _brushListPanel.SuspendLayout();
            _brushListPanel.Controls.Clear();
            _brushRowControls.Clear();

            int y = 0;
            int rowHeight = 52;
            int panelWidth = _brushListPanel.ClientSize.Width > 0 ? _brushListPanel.ClientSize.Width : 480;

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

                    // S-Curve Preview
                    Rectangle strokeRect = new Rectangle(12, 6, 170, 40);
                    BrushEngine.DrawIbisStrokePreview(g, targetPreset, Color.FromArgb(25, 25, 30), strokeRect, targetPreset.Size);

                    // Nombre
                    using (var font = new Font("Segoe UI", 9.5f, FontStyle.Regular))
                    using (var textBrush = new SolidBrush(Color.FromArgb(20, 20, 25)))
                    {
                        g.DrawString(targetPreset.Name, font, textBrush, 190, 16);
                    }

                    // Tamaño
                    using (var font = new Font("Segoe UI", 9f, FontStyle.Regular))
                    using (var textBrush = new SolidBrush(Color.FromArgb(120, 120, 130)))
                    {
                        string sizeStr = string.Format("{0:F1}", targetPreset.Size);
                        g.DrawString(sizeStr, font, textBrush, row.Width - 65, 16);
                    }

                    // Icono +
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

                // SELECCIÓN INMEDIATA (SIN CONFIRMACIÓN): Un solo clic selecciona y cierra para dibujar directamente
                Action selectAndCloseAction = () => {
                    SelectedPreset = targetPreset;
                    SelectedSize = targetPreset.Size;
                    SelectedFlow = targetPreset.DefaultFlow;
                    this.DialogResult = DialogResult.OK;
                    this.Close();
                };

                row.Click += (s, e) => selectAndCloseAction();
                _brushRowControls.Add(row);
                _brushListPanel.Controls.Add(row);
                y += rowHeight;
            }

            _brushListPanel.ResumeLayout(true);
        }

        private void BuildRightInspector()
        {
            _lblBrushTitle = new Label
            {
                Text = SelectedPreset.Name,
                Left = 16,
                Top = 16,
                Width = 250,
                Font = new Font("Segoe UI", 12f, FontStyle.Bold),
                ForeColor = Color.White
            };
            _inspectorPanel.Controls.Add(_lblBrushTitle);

            // Preview Cuadrícula Ajedrez
            _previewBox = new PictureBox
            {
                Left = 16,
                Top = 50,
                Width = 258,
                Height = 130,
                BackColor = Color.White
            };
            _previewBox.Paint += DrawCheckerboardAndPreview;
            _inspectorPanel.Controls.Add(_previewBox);

            // Grosor
            int cy = 195;
            var lblGrosor = new Label { Text = "Grosor", Left = 16, Top = cy, ForeColor = Color.LightGray, Font = new Font("Segoe UI", 9f), AutoSize = true };
            _lblGrosorVal = new Label { Text = string.Format("{0:F1}px", SelectedSize), Left = 210, Top = cy, ForeColor = Color.White, Font = new Font("Segoe UI", 9f, FontStyle.Bold), AutoSize = true };
            _inspectorPanel.Controls.Add(lblGrosor);
            _inspectorPanel.Controls.Add(_lblGrosorVal);

            cy += 24;
            var btnMinusW = new Button { Text = "−", Left = 16, Top = cy, Width = 28, Height = 26, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 60), ForeColor = Color.White };
            btnMinusW.FlatAppearance.BorderSize = 0;
            _tbGrosor = new TrackBar { Left = 48, Top = cy, Width = 195, Minimum = 1, Maximum = 250, Value = (int)SelectedSize, TickStyle = TickStyle.None };
            var btnPlusW = new Button { Text = "+", Left = 246, Top = cy, Width = 28, Height = 26, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 60), ForeColor = Color.White };
            btnPlusW.FlatAppearance.BorderSize = 0;

            _tbGrosor.ValueChanged += (s, e) => {
                SelectedSize = _tbGrosor.Value;
                _lblGrosorVal.Text = string.Format("{0:F1}px", SelectedSize);
                _previewBox.Invalidate();
            };
            btnMinusW.Click += (s, e) => _tbGrosor.Value = Math.Max(1, _tbGrosor.Value - 1);
            btnPlusW.Click += (s, e) => _tbGrosor.Value = Math.Min(250, _tbGrosor.Value + 1);

            _inspectorPanel.Controls.Add(btnMinusW);
            _inspectorPanel.Controls.Add(_tbGrosor);
            _inspectorPanel.Controls.Add(btnPlusW);

            // Opacidad
            cy += 40;
            var lblOpacidad = new Label { Text = "Opacidad", Left = 16, Top = cy, ForeColor = Color.LightGray, Font = new Font("Segoe UI", 9f), AutoSize = true };
            _lblOpacidadVal = new Label { Text = string.Format("{0:P0}", SelectedFlow), Left = 210, Top = cy, ForeColor = Color.White, Font = new Font("Segoe UI", 9f, FontStyle.Bold), AutoSize = true };
            _inspectorPanel.Controls.Add(lblOpacidad);
            _inspectorPanel.Controls.Add(_lblOpacidadVal);

            cy += 24;
            var btnMinusO = new Button { Text = "−", Left = 16, Top = cy, Width = 28, Height = 26, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 60), ForeColor = Color.White };
            btnMinusO.FlatAppearance.BorderSize = 0;
            _tbOpacidad = new TrackBar { Left = 48, Top = cy, Width = 195, Minimum = 5, Maximum = 100, Value = (int)(SelectedFlow * 100), TickStyle = TickStyle.None };
            var btnPlusO = new Button { Text = "+", Left = 246, Top = cy, Width = 28, Height = 26, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 60), ForeColor = Color.White };
            btnPlusO.FlatAppearance.BorderSize = 0;

            _tbOpacidad.ValueChanged += (s, e) => {
                SelectedFlow = _tbOpacidad.Value / 100.0f;
                _lblOpacidadVal.Text = string.Format("{0:P0}", SelectedFlow);
                _previewBox.Invalidate();
            };
            btnMinusO.Click += (s, e) => _tbOpacidad.Value = Math.Max(5, _tbOpacidad.Value - 5);
            btnPlusO.Click += (s, e) => _tbOpacidad.Value = Math.Min(100, _tbOpacidad.Value + 5);

            _inspectorPanel.Controls.Add(btnMinusO);
            _inspectorPanel.Controls.Add(_tbOpacidad);
            _inspectorPanel.Controls.Add(btnPlusO);
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

            Rectangle strokeRect = new Rectangle(14, 15, _previewBox.Width - 28, _previewBox.Height - 30);
            BrushEngine.DrawIbisStrokePreview(g, SelectedPreset, _activeColor, strokeRect, SelectedSize);
        }
    }
}
