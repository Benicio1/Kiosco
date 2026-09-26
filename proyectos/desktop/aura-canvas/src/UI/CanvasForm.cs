using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Windows.Forms;
using AuraPaint.Engine;
using AuraPaint.Models;

namespace AuraPaint.UI
{
    public class CanvasForm : Form
    {
        public int CanvasWidth { get { return _layerManager.CanvasWidth; } }
        public int CanvasHeight { get { return _layerManager.CanvasHeight; } }

        public float Zoom = 0.35f;
        public float PanX = 180.0f;
        public float PanY = 40.0f;
        public float CanvasRotation = 0.0f;

        public bool ShowGrid = false;

        private readonly LayerManager _layerManager;
        private readonly BrushEngine _brush = new BrushEngine();
        private readonly Stack<IUndoableAction> _undoStack = new Stack<IUndoableAction>();
        private readonly Stack<IUndoableAction> _redoStack = new Stack<IUndoableAction>();

        // Estado del trazo
        private bool _isPanning = false;
        private Point _lastMousePos;
        private bool _isStroking = false;
        private bool _isSpaceDown = false;
        private float _strokeMinX, _strokeMinY, _strokeMaxX, _strokeMaxY;
        private Bitmap _preStrokeLayerSnapshot = null;

        // UI Controls
        private IbisToolbar _toolbar;
        private Panel _bottomDock;
        private Label _lblSizeValue;
        private Label _lblFlowValue;
        private Label _lblCanvasDimensions;
        private Button _btnCurrentColor;
        private Button _btnActiveBrush;
        private Button _btnLayers;
        private TextBox _txtHex;
        private bool _suppressHexUpdate = false;
        private ReferenceBox _refBox;
        private IbisLayerDialog _layerDialog = null;

        public CanvasForm()
        {
            Text = "AuraPaint Pro Studio — High-Performance Digital Illustration Engine";
            Width = 1440;
            Height = 900;
            KeyPreview = true;
            DoubleBuffered = true;
            SetStyle(ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint | ControlStyles.OptimizedDoubleBuffer, true);
            BackColor = Color.FromArgb(20, 20, 24);

            _layerManager = new LayerManager(3840, 2160);
            _layerManager.OnLayersChanged += () => {
                UpdateLayerButtonText();
                Invalidate();
            };
            _layerManager.OnUndoActionGenerated += (action) => {
                _undoStack.Push(action);
                foreach (var redo in _redoStack) redo.Dispose();
                _redoStack.Clear();
            };

            InitializeUserInterface();
            CenterCanvasView();
        }

        private void InitializeUserInterface()
        {
            SuspendLayout();

            // 1. Menú superior
            InitMenuBar();

            // 2. Barra lateral vertical estilo ibis Paint X (media_1789869663873.png)
            _toolbar = new IbisToolbar();
            _toolbar.OnToolSelected += OnToolChanged;
            Controls.Add(_toolbar);

            // 3. Barra inferior de control rápido
            InitBottomDock();

            // 4. Ventana de referencia flotante
            _refBox = new ReferenceBox { Left = Width - 365, Top = 50 };
            _refBox.OnColorPicked += (c) => SetActiveColor(c);
            Controls.Add(_refBox);
            _refBox.BringToFront();

            ResumeLayout(true);
        }

        private void InitMenuBar()
        {
            var menu = new MenuStrip { BackColor = Color.FromArgb(32, 32, 38), ForeColor = Color.White };

            var fileMenu = new ToolStripMenuItem("Archivo (&A)");
            fileMenu.DropDownItems.Add("Nuevo Lienzo / Redimensionar (Ctrl+N)", null, (s, e) => ShowCanvasSizeDialog());
            fileMenu.DropDownItems.Add("Exportar Ilustración a PNG (Ctrl+S)", null, (s, e) => ExportCanvasPng());
            fileMenu.DropDownItems.Add(new ToolStripSeparator());
            fileMenu.DropDownItems.Add("Salir", null, (s, e) => Close());

            var editMenu = new ToolStripMenuItem("Edición (&E)");
            editMenu.DropDownItems.Add("Deshacer (Ctrl+Z)", null, (s, e) => Undo());
            editMenu.DropDownItems.Add("Rehacer (Ctrl+Y)", null, (s, e) => Redo());
            editMenu.DropDownItems.Add(new ToolStripSeparator());
            editMenu.DropDownItems.Add("Limpiar Capa Activa", null, (s, e) => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.Clear(Color.FromArgb(0, 0, 0, 0));
                    Invalidate();
                }
            });

            var viewMenu = new ToolStripMenuItem("Ver (&V)");
            var gridItem = new ToolStripMenuItem("Mostrar Cuadrícula de Guía", null, (s, e) => {
                ShowGrid = !ShowGrid;
                ((ToolStripMenuItem)s).Checked = ShowGrid;
                Invalidate();
            });
            gridItem.Checked = ShowGrid;

            var refItem = new ToolStripMenuItem("Ventana de Referencia Sub-View (F4)", null, (s, e) => {
                _refBox.Visible = !_refBox.Visible;
                if (_refBox.Visible) _refBox.BringToFront();
            });

            viewMenu.DropDownItems.Add(gridItem);
            viewMenu.DropDownItems.Add(refItem);
            viewMenu.DropDownItems.Add("Centrar Vista (Ctrl+0)", null, (s, e) => CenterCanvasView());

            var layerMenu = new ToolStripMenuItem("Capas (&C)");
            layerMenu.DropDownItems.Add("Abrir Panel de Capas (F2)", null, (s, e) => ToggleLayerDialog());
            layerMenu.DropDownItems.Add("Nueva Capa", null, (s, e) => _layerManager.AddLayer());
            layerMenu.DropDownItems.Add("Duplicar Capa Activa", null, (s, e) => _layerManager.DuplicateLayer());
            layerMenu.DropDownItems.Add("Combinar Hacia Abajo", null, (s, e) => _layerManager.MergeDown());

            menu.Items.Add(fileMenu);
            menu.Items.Add(editMenu);
            menu.Items.Add(viewMenu);
            menu.Items.Add(layerMenu);

            MainMenuStrip = menu;
            Controls.Add(menu);
        }

        private void InitBottomDock()
        {
            _bottomDock = new Panel
            {
                Dock = DockStyle.Bottom,
                Height = 56,
                BackColor = Color.FromArgb(28, 28, 34)
            };

            int x = 70;

            // Color actual y selector
            _btnCurrentColor = new Button
            {
                Left = x,
                Top = 10,
                Width = 36,
                Height = 36,
                BackColor = _brush.Color,
                FlatStyle = FlatStyle.Flat,
                Cursor = Cursors.Hand,
                TabStop = false
            };
            _btnCurrentColor.FlatAppearance.BorderColor = Color.White;
            _btnCurrentColor.FlatAppearance.BorderSize = 2;
            _btnCurrentColor.Click += (s, e) => ShowColorDialog();
            _bottomDock.Controls.Add(_btnCurrentColor);
            x += 44;

            // Hex Input (#RRGGBB)
            _txtHex = new TextBox
            {
                Left = x,
                Top = 16,
                Width = 75,
                Height = 26,
                BackColor = Color.FromArgb(18, 18, 24),
                ForeColor = Color.White,
                Font = new Font("Consolas", 10f, FontStyle.Bold),
                BorderStyle = BorderStyle.FixedSingle,
                MaxLength = 9,
                TabStop = false
            };
            UpdateHexTextFromColor(_brush.Color);
            _txtHex.TextChanged += (s, e) => {
                if (_suppressHexUpdate) return;
                string text = _txtHex.Text.Trim();
                if (text.StartsWith("#")) text = text.Substring(1);
                if (text.Length == 6)
                {
                    try
                    {
                        int r = Convert.ToInt32(text.Substring(0, 2), 16);
                        int g = Convert.ToInt32(text.Substring(2, 2), 16);
                        int b = Convert.ToInt32(text.Substring(4, 2), 16);
                        Color c = Color.FromArgb(r, g, b);
                        _brush.Color = c;
                        _btnCurrentColor.BackColor = c;
                        Invalidate();
                    }
                    catch { }
                }
            };
            _bottomDock.Controls.Add(_txtHex);
            x += 82;

            // Pincel Activo (Botón que abre el IbisBrushDialog)
            _btnActiveBrush = new Button
            {
                Text = "🖌️ " + _brush.ActivePreset.Name + " ▼",
                Left = x,
                Top = 12,
                Width = 180,
                Height = 32,
                BackColor = Color.FromArgb(45, 45, 55),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 9f, FontStyle.Bold),
                Cursor = Cursors.Hand,
                TabStop = false
            };
            _btnActiveBrush.FlatAppearance.BorderSize = 0;
            _btnActiveBrush.Click += (s, e) => ShowBrushDialog();
            _bottomDock.Controls.Add(_btnActiveBrush);
            x += 188;

            // Grosor Slider
            var lblSize = new Label { Text = "Grosor:", Left = x, Top = 18, ForeColor = Color.LightGray, AutoSize = true, Font = new Font("Segoe UI", 8.5f) };
            _bottomDock.Controls.Add(lblSize);
            x += 50;

            var tbSize = new TrackBar
            {
                Left = x,
                Top = 14,
                Width = 120,
                Height = 30,
                Minimum = 1,
                Maximum = 250,
                Value = (int)_brush.BaseSize,
                TickStyle = TickStyle.None,
                TabStop = false
            };
            _lblSizeValue = new Label { Text = string.Format("{0:F0} px", _brush.BaseSize), Left = x + 124, Top = 18, ForeColor = Color.White, AutoSize = true, Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };
            tbSize.ValueChanged += (s, e) => {
                _brush.BaseSize = tbSize.Value;
                _lblSizeValue.Text = string.Format("{0:F0} px", _brush.BaseSize);
            };
            _bottomDock.Controls.Add(tbSize);
            _bottomDock.Controls.Add(_lblSizeValue);
            x += 175;

            // Opacidad Slider
            var lblFlow = new Label { Text = "Opacidad:", Left = x, Top = 18, ForeColor = Color.LightGray, AutoSize = true, Font = new Font("Segoe UI", 8.5f) };
            _bottomDock.Controls.Add(lblFlow);
            x += 60;

            var tbFlow = new TrackBar
            {
                Left = x,
                Top = 14,
                Width = 100,
                Height = 30,
                Minimum = 5,
                Maximum = 100,
                Value = (int)(_brush.Flow * 100),
                TickStyle = TickStyle.None,
                TabStop = false
            };
            _lblFlowValue = new Label { Text = string.Format("{0:P0}", _brush.Flow), Left = x + 104, Top = 18, ForeColor = Color.White, AutoSize = true, Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };
            tbFlow.ValueChanged += (s, e) => {
                _brush.Flow = tbFlow.Value / 100.0f;
                _lblFlowValue.Text = string.Format("{0:P0}", _brush.Flow);
            };
            _bottomDock.Controls.Add(tbFlow);
            _bottomDock.Controls.Add(_lblFlowValue);
            x += 150;

            // Botón de Capas (Abre la ventana de capas estilo ibis Paint X)
            _btnLayers = new Button
            {
                Text = "📑 Capas (1)",
                Left = x,
                Top = 12,
                Width = 120,
                Height = 32,
                BackColor = Color.FromArgb(45, 75, 110),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 9f, FontStyle.Bold),
                Cursor = Cursors.Hand,
                TabStop = false
            };
            _btnLayers.FlatAppearance.BorderSize = 0;
            _btnLayers.Click += (s, e) => ToggleLayerDialog();
            _bottomDock.Controls.Add(_btnLayers);
            x += 128;

            // Botón Sub-Vista / Referencia
            var btnRef = new Button
            {
                Text = "🖼️ Referencia",
                Left = x,
                Top = 12,
                Width = 105,
                Height = 32,
                BackColor = Color.FromArgb(50, 50, 60),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 8.5f),
                Cursor = Cursors.Hand,
                TabStop = false
            };
            btnRef.FlatAppearance.BorderSize = 0;
            btnRef.Click += (s, e) => {
                _refBox.Visible = !_refBox.Visible;
                if (_refBox.Visible) _refBox.BringToFront();
            };
            _bottomDock.Controls.Add(btnRef);
            x += 115;

            // Dimensiones Lienzo
            _lblCanvasDimensions = new Label
            {
                Text = string.Format("{0}x{1} px", CanvasWidth, CanvasHeight),
                Left = x,
                Top = 18,
                ForeColor = Color.FromArgb(140, 140, 150),
                AutoSize = true,
                Font = new Font("Segoe UI", 8.5f)
            };
            _bottomDock.Controls.Add(_lblCanvasDimensions);

            Controls.Add(_bottomDock);
        }

        private void UpdateLayerButtonText()
        {
            if (_btnLayers != null)
            {
                _btnLayers.Text = string.Format("📑 Capas ({0})", _layerManager.Layers.Count);
            }
        }

        private void ToggleLayerDialog()
        {
            if (_layerDialog == null || _layerDialog.IsDisposed)
            {
                _layerDialog = new IbisLayerDialog(_layerManager, () => Invalidate());
                _layerDialog.Location = new Point(this.Right - _layerDialog.Width - 30, this.Bottom - _layerDialog.Height - 80);
                _layerDialog.Show(this);
            }
            else
            {
                if (_layerDialog.Visible)
                {
                    _layerDialog.Hide();
                }
                else
                {
                    _layerDialog.Location = new Point(this.Right - _layerDialog.Width - 30, this.Bottom - _layerDialog.Height - 80);
                    _layerDialog.RefreshLayerList();
                    _layerDialog.Show(this);
                    _layerDialog.BringToFront();
                }
            }
        }

        private void OnToolChanged(IbisToolMode tool)
        {
            switch (tool)
            {
                case IbisToolMode.Brush:
                    if (_brush.ActivePreset.IsEraser)
                    {
                        _brush.SetPreset(BrushPreset.GetAllPresets()[0]);
                        _btnActiveBrush.Text = "🖌️ " + _brush.ActivePreset.Name + " ▼";
                    }
                    break;

                case IbisToolMode.Eraser:
                    _brush.SetPreset(BrushPreset.GetAllPresets().Find(p => p.Id == "eraser_hard"));
                    _btnActiveBrush.Text = "🧹 " + _brush.ActivePreset.Name + " ▼";
                    break;

                case IbisToolMode.SpecialPen:
                    _brush.SetPreset(BrushPreset.GetAllPresets().Find(p => p.Id == "splatter_ink"));
                    _btnActiveBrush.Text = "✨ " + _brush.ActivePreset.Name + " ▼";
                    break;

                case IbisToolMode.Eyedropper:
                    Cursor = Cursors.Cross;
                    break;

                case IbisToolMode.Fill:
                    Cursor = Cursors.Hand;
                    break;

                case IbisToolMode.CanvasSettings:
                    ShowCanvasSizeDialog();
                    _toolbar.SetActiveTool(IbisToolMode.Brush);
                    break;

                case IbisToolMode.Filter:
                    if (_layerManager.ActiveLayer != null)
                    {
                        _layerManager.ActiveLayer.InvertColors();
                        Invalidate();
                    }
                    _toolbar.SetActiveTool(IbisToolMode.Brush);
                    break;
            }
        }

        public void SetActiveColor(Color c)
        {
            _brush.Color = c;
            _btnCurrentColor.BackColor = c;
            UpdateHexTextFromColor(c);
            Invalidate();
        }

        private void UpdateHexTextFromColor(Color c)
        {
            _suppressHexUpdate = true;
            _txtHex.Text = string.Format("#{0:X2}{1:X2}{2:X2}", c.R, c.G, c.B);
            _suppressHexUpdate = false;
        }

        private void ShowColorDialog()
        {
            using (var cd = new ColorDialog())
            {
                cd.FullOpen = true;
                cd.Color = _brush.Color;
                if (cd.ShowDialog() == DialogResult.OK)
                {
                    SetActiveColor(cd.Color);
                }
            }
        }

        private void ShowBrushDialog()
        {
            using (var dlg = new IbisBrushDialog(_brush.ActivePreset, _brush.BaseSize, _brush.Flow, _brush.Color))
            {
                if (dlg.ShowDialog(this) == DialogResult.OK)
                {
                    _brush.SetPreset(dlg.SelectedPreset);
                    _brush.BaseSize = dlg.SelectedSize;
                    _brush.Flow = dlg.SelectedFlow;
                    _lblSizeValue.Text = string.Format("{0:F0} px", _brush.BaseSize);
                    _lblFlowValue.Text = string.Format("{0:P0}", _brush.Flow);
                    _btnActiveBrush.Text = "🖌️ " + _brush.ActivePreset.Name + " ▼";
                    Invalidate();
                }
            }
        }

        private void ShowCanvasSizeDialog()
        {
            using (var dlg = new CanvasSizeDialog(CanvasWidth, CanvasHeight))
            {
                if (dlg.ShowDialog(this) == DialogResult.OK)
                {
                    _layerManager.ResizeCanvas(dlg.CanvasWidth, dlg.CanvasHeight, dlg.ClearCanvas);
                    _lblCanvasDimensions.Text = string.Format("{0}x{1} px", CanvasWidth, CanvasHeight);
                    CenterCanvasView();
                }
            }
        }

        private void CenterCanvasView()
        {
            int availW = ClientSize.Width - _toolbar.Width;
            int availH = ClientSize.Height - _bottomDock.Height - 30;

            float zx = (availW * 0.82f) / CanvasWidth;
            float zy = (availH * 0.82f) / CanvasHeight;
            Zoom = Math.Max(0.05f, Math.Min(zx, zy));

            PanX = _toolbar.Width + (availW - CanvasWidth * Zoom) / 2.0f;
            PanY = 30 + (availH - CanvasHeight * Zoom) / 2.0f;
            CanvasRotation = 0.0f;
            Invalidate();
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
            var activeLayer = _layerManager.ActiveLayer;
            if (activeLayer == null || !activeLayer.Visible) return;

            _isStroking = true;
            _strokeMinX = float.MaxValue;
            _strokeMinY = float.MaxValue;
            _strokeMaxX = float.MinValue;
            _strokeMaxY = float.MinValue;

            if (_preStrokeLayerSnapshot != null) _preStrokeLayerSnapshot.Dispose();
            _preStrokeLayerSnapshot = (Bitmap)activeLayer.Bitmap.Clone();

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

            var activeLayer = _layerManager.ActiveLayer;
            if (activeLayer != null && _strokeMaxX >= _strokeMinX && _strokeMaxY >= _strokeMinY && _preStrokeLayerSnapshot != null)
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
                        Layer = activeLayer,
                        DirtyBounds = cropRect,
                        BeforeCrop = _preStrokeLayerSnapshot.Clone(cropRect, PixelFormat.Format32bppPArgb),
                        AfterCrop = activeLayer.Bitmap.Clone(cropRect, PixelFormat.Format32bppPArgb)
                    };

                    _undoStack.Push(action);
                    foreach (var redo in _redoStack) redo.Dispose();
                    _redoStack.Clear();
                }
                activeLayer.InvalidateThumbnail();
                _layerManager.TriggerLayersChanged();
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
            var activeLayer = _layerManager.ActiveLayer;
            if (dabs == null || dabs.Count == 0 || activeLayer == null) return;

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

                _brush.StampDabDirect(activeLayer.Graphics, dab.X, dab.Y, dab.Pressure);
            }

            PointF c1 = CanvasToScreen(minCanvX, minCanvY);
            PointF c2 = CanvasToScreen(maxCanvX, minCanvY);
            PointF c3 = CanvasToScreen(minCanvX, maxCanvY);
            PointF c4 = CanvasToScreen(maxCanvX, maxCanvY);

            float minScrX = Math.Min(Math.Min(c1.X, c2.X), Math.Min(c3.X, c4.X));
            float minScrY = Math.Min(Math.Min(c1.Y, c2.Y), Math.Min(c3.Y, c4.Y));
            float maxScrX = Math.Max(Math.Max(c1.X, c2.X), Math.Max(c3.X, c4.X));
            float maxScrY = Math.Max(Math.Max(c1.Y, c2.Y), Math.Max(c3.Y, c4.Y));

            int margin = 8;
            int ix = Math.Max(_toolbar.Width, (int)Math.Floor(minScrX) - margin);
            int iy = Math.Max(0, (int)Math.Floor(minScrY) - margin);
            int iw = Math.Min(ClientSize.Width - ix, (int)Math.Ceiling(maxScrX - minScrX) + margin * 2);
            int ih = Math.Min(ClientSize.Height - iy, (int)Math.Ceiling(maxScrY - minScrY) + margin * 2);

            if (iw > 0 && ih > 0)
            {
                Invalidate(new Rectangle(ix, iy, iw, ih));
            }
        }

        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e);
            if (e.X < _toolbar.Width || e.Y > ClientSize.Height - _bottomDock.Height) return;

            bool isMiddleButton = (e.Button == MouseButtons.Middle) || (NativePen.GetAsyncKeyState(NativePen.VK_MBUTTON) < 0);
            bool isSpaceDrag = (e.Button == MouseButtons.Left) && (_isSpaceDown || (NativePen.GetAsyncKeyState(NativePen.VK_SPACE) < 0) || (_toolbar.ActiveTool == IbisToolMode.Transform));
            bool isRightDrag = (e.Button == MouseButtons.Right);

            if (isMiddleButton || isSpaceDrag || isRightDrag)
            {
                _isPanning = true;
                _lastMousePos = e.Location;
                Cursor = Cursors.SizeAll;
                return;
            }

            // Cuentagotas
            if (_toolbar.ActiveTool == IbisToolMode.Eyedropper && e.Button == MouseButtons.Left)
            {
                PointF pt = ScreenToCanvas(e.X, e.Y);
                int cx = (int)pt.X;
                int cy = (int)pt.Y;
                if (cx >= 0 && cx < CanvasWidth && cy >= 0 && cy < CanvasHeight)
                {
                    if (_layerManager.ActiveLayer != null)
                    {
                        Color c = _layerManager.ActiveLayer.Bitmap.GetPixel(cx, cy);
                        if (c.A > 0) SetActiveColor(c);
                    }
                }
                return;
            }

            // Trazo con ratón (Presión constante 1.0f para 100% de grosor sin afinarse)
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
            float zoomFactor = e.Delta > 0 ? 1.15f : 0.85f;
            float newZoom = Math.Max(0.02f, Math.Min(40.0f, Zoom * zoomFactor));

            PanX = e.X - (e.X - PanX) * (newZoom / Zoom);
            PanY = e.Y - (e.Y - PanY) * (newZoom / Zoom);
            Zoom = newZoom;
            Invalidate();
        }

        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            if (keyData == Keys.Space)
            {
                if (!_isSpaceDown)
                {
                    _isSpaceDown = true;
                    Cursor = Cursors.Hand;
                }
                return true;
            }
            return base.ProcessCmdKey(ref msg, keyData);
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

        protected override void OnKeyDown(KeyEventArgs e)
        {
            base.OnKeyDown(e);

            if (e.Control && e.KeyCode == Keys.Z) { Undo(); e.Handled = true; }
            else if (e.Control && e.KeyCode == Keys.Y) { Redo(); e.Handled = true; }
            else if (e.Control && e.KeyCode == Keys.N) { ShowCanvasSizeDialog(); e.Handled = true; }
            else if (e.Control && e.KeyCode == Keys.D0) { CenterCanvasView(); e.Handled = true; }
            else if (e.KeyCode == Keys.F2) { ToggleLayerDialog(); e.Handled = true; }
            else if (e.KeyCode == Keys.F4) { _refBox.Visible = !_refBox.Visible; e.Handled = true; }
            else if (e.KeyCode == Keys.B) { _toolbar.SetActiveTool(IbisToolMode.Brush); }
            else if (e.KeyCode == Keys.E) { _toolbar.SetActiveTool(IbisToolMode.Eraser); }
        }

        public void Undo()
        {
            if (_undoStack.Count > 0)
            {
                var action = _undoStack.Pop();
                action.Undo();
                _redoStack.Push(action);
                Invalidate();
                _layerManager.TriggerLayersChanged();
            }
        }

        public void Redo()
        {
            if (_redoStack.Count > 0)
            {
                var action = _redoStack.Pop();
                action.Redo();
                _undoStack.Push(action);
                Invalidate();
                _layerManager.TriggerLayersChanged();
            }
        }

        protected override void WndProc(ref Message m)
        {
            switch (m.Msg)
            {
                case NativePen.WM_POINTERDOWN:
                case NativePen.WM_POINTERUPDATE:
                case NativePen.WM_POINTERUP:
                    uint pointerId = (uint)(m.WParam.ToInt64() & 0xFFFF);
                    var penInfo = new NativePen.POINTER_PEN_INFO();
                    if (NativePen.GetPointerPenInfo(pointerId, ref penInfo))
                    {
                        Point clientPt = PointToClient(new Point(penInfo.pointerInfo.ptPixelLocation.X, penInfo.pointerInfo.ptPixelLocation.Y));
                        if (clientPt.X >= _toolbar.Width && clientPt.Y <= ClientSize.Height - _bottomDock.Height)
                        {
                            float pressure = (penInfo.penMask & NativePen.PEN_MASK_PRESSURE) != 0 ? (penInfo.pressure / 1024.0f) : 1.0f;
                            if (m.Msg == NativePen.WM_POINTERDOWN) StartStroke(clientPt.X, clientPt.Y, pressure, penInfo.tiltX, penInfo.tiltY);
                            else if (m.Msg == NativePen.WM_POINTERUPDATE) UpdateStroke(clientPt.X, clientPt.Y, pressure, penInfo.tiltX, penInfo.tiltY);
                            else if (m.Msg == NativePen.WM_POINTERUP) EndStroke();
                            m.Result = IntPtr.Zero;
                            return;
                        }
                    }
                    break;
            }
            base.WndProc(ref m);
        }

        // =====================================================================
        // RENDERIZADO DEL VIEWPORT CONTINUO MULTICAPA (COMPOSITING PIPELINE)
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

            // Sombra del lienzo
            using (var shadowBrush = new SolidBrush(Color.FromArgb(10, 10, 14)))
            {
                g.FillRectangle(shadowBrush, 6, 6, CanvasWidth, CanvasHeight);
            }

            // 1. Fondo seleccionado (Blanco, Ajedrez claro, Ajedrez oscuro, Transparente)
            DrawCanvasBackground(g);

            // 2. Renderizado ordenado de todas las capas
            foreach (var layer in _layerManager.Layers)
            {
                if (layer.Visible && layer.Bitmap != null)
                {
                    if (layer.Opacity < 1.0f)
                    {
                        ColorMatrix matrix = new ColorMatrix { Matrix33 = layer.Opacity };
                        using (ImageAttributes attr = new ImageAttributes())
                        {
                            attr.SetColorMatrix(matrix, ColorMatrixFlag.Default, ColorAdjustType.Bitmap);
                            g.DrawImage(layer.Bitmap, new Rectangle(0, 0, CanvasWidth, CanvasHeight), 0, 0, CanvasWidth, CanvasHeight, GraphicsUnit.Pixel, attr);
                        }
                    }
                    else
                    {
                        g.DrawImage(layer.Bitmap, new Rectangle(0, 0, CanvasWidth, CanvasHeight), 0, 0, CanvasWidth, CanvasHeight, GraphicsUnit.Pixel);
                    }
                }
            }

            // 3. Cuadrícula opcional de guía
            if (ShowGrid)
            {
                int gridStep = 100;
                using (var gridPen = new Pen(Color.FromArgb(40, 0, 120, 255), 1.0f / Zoom))
                {
                    for (int x = 0; x <= CanvasWidth; x += gridStep) g.DrawLine(gridPen, x, 0, x, CanvasHeight);
                    for (int y = 0; y <= CanvasHeight; y += gridStep) g.DrawLine(gridPen, 0, y, CanvasWidth, y);
                }
            }

            // Marco del lienzo
            using (var borderPen = new Pen(Color.FromArgb(70, 70, 85), 1.5f / Zoom))
            {
                g.DrawRectangle(borderPen, 0, 0, CanvasWidth, CanvasHeight);
            }

            g.Restore(state);
        }

        private void DrawCanvasBackground(Graphics g)
        {
            Rectangle rect = new Rectangle(0, 0, CanvasWidth, CanvasHeight);
            switch (_layerManager.BackgroundType)
            {
                case CanvasBackgroundType.White:
                    using (var b = new SolidBrush(Color.White)) g.FillRectangle(b, rect);
                    break;
                case CanvasBackgroundType.LightChecker:
                    DrawTiledChecker(g, rect, 24, Color.White, Color.FromArgb(235, 235, 240));
                    break;
                case CanvasBackgroundType.DarkChecker:
                    DrawTiledChecker(g, rect, 24, Color.FromArgb(45, 45, 50), Color.FromArgb(65, 65, 72));
                    break;
                case CanvasBackgroundType.Transparent:
                    DrawTiledChecker(g, rect, 20, Color.FromArgb(250, 250, 250), Color.FromArgb(220, 220, 225));
                    break;
            }
        }

        private void DrawTiledChecker(Graphics g, Rectangle bounds, int size, Color c1, Color c2)
        {
            using (var b1 = new SolidBrush(c1))
            using (var b2 = new SolidBrush(c2))
            {
                for (int x = bounds.Left; x < bounds.Right; x += size)
                {
                    for (int y = bounds.Top; y < bounds.Bottom; y += size)
                    {
                        bool isEven = ((x / size) + (y / size)) % 2 == 0;
                        g.FillRectangle(isEven ? b1 : b2, x, y, Math.Min(size, bounds.Right - x), Math.Min(size, bounds.Bottom - y));
                    }
                }
            }
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
                        foreach (var layer in _layerManager.Layers)
                        {
                            if (layer.Visible && layer.Bitmap != null)
                            {
                                if (layer.Opacity < 1.0f)
                                {
                                    ColorMatrix matrix = new ColorMatrix { Matrix33 = layer.Opacity };
                                    using (ImageAttributes attr = new ImageAttributes())
                                    {
                                        attr.SetColorMatrix(matrix, ColorMatrixFlag.Default, ColorAdjustType.Bitmap);
                                        g.DrawImage(layer.Bitmap, new Rectangle(0, 0, CanvasWidth, CanvasHeight), 0, 0, CanvasWidth, CanvasHeight, GraphicsUnit.Pixel, attr);
                                    }
                                }
                                else
                                {
                                    g.DrawImage(layer.Bitmap, 0, 0);
                                }
                            }
                        }
                        fullCanvas.Save(sfd.FileName, ImageFormat.Png);
                    }
                    MessageBox.Show("¡Lienzo exportado exitosamente a PNG!", "Exportación Completada", MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
            }
        }
    }
}
