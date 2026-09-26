using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;
using AuraPaint.Engine;

namespace AuraPaint.UI
{
    public class IbisLayerDialog : Form
    {
        private readonly LayerManager _layerManager;
        private readonly Action _onCanvasRepaintNeeded;

        // UI Components
        private Panel _leftPreviewPanel;
        private PictureBox _pbPreview;
        private Panel _centerListPanel;
        private Panel _layersScrollPanel;
        private Panel _rightActionPanel;
        private Panel _bottomControlsPanel;

        private Label _lblOpacityVal;
        private TrackBar _tbOpacity;
        private ComboBox _cboBlendMode;
        private Button _btnAlphaLock;

        public IbisLayerDialog(LayerManager layerManager, Action onCanvasRepaintNeeded)
        {
            _layerManager = layerManager;
            _onCanvasRepaintNeeded = onCanvasRepaintNeeded;

            Text = "Capas — ibis Paint X Studio";
            FormBorderStyle = FormBorderStyle.None;
            StartPosition = FormStartPosition.Manual;
            Width = 650;
            Height = 500;
            BackColor = Color.FromArgb(36, 36, 40);
            ShowInTaskbar = false;
            DoubleBuffered = true;

            InitializeLayout();
            _layerManager.OnLayersChanged += RefreshLayerList;
            RefreshLayerList();
        }

        private void InitializeLayout()
        {
            // 1. Barra de Título / Encabezado
            var header = new Panel { Dock = DockStyle.Top, Height = 36, BackColor = Color.FromArgb(48, 48, 54) };
            var lblTitle = new Label { Text = "📑 Capas", ForeColor = Color.White, Font = new Font("Segoe UI", 10.5f, FontStyle.Bold), Left = 12, Top = 7, AutoSize = true };
            var btnClose = new Button { Text = "✕", ForeColor = Color.LightGray, BackColor = Color.Transparent, FlatStyle = FlatStyle.Flat, Width = 32, Height = 30, Left = Width - 38, Top = 3, Font = new Font("Segoe UI", 10f) };
            btnClose.FlatAppearance.BorderSize = 0;
            btnClose.Click += (s, e) => this.Hide();

            // Handle de arrastre superior
            var dragHandle = new Panel { Width = 60, Height = 5, BackColor = Color.FromArgb(120, 120, 130), Left = (Width - 60) / 2, Top = 8 };
            dragHandle.MouseMove += (s, e) => {
                if (e.Button == MouseButtons.Left)
                {
                    this.Left += e.X - 30;
                    this.Top += e.Y - 2;
                }
            };

            header.Controls.Add(lblTitle);
            header.Controls.Add(dragHandle);
            header.Controls.Add(btnClose);
            Controls.Add(header);

            // 2. Barra Inferior de Ajustes (Opacidad, Modo de Fusión, Alpha Lock)
            _bottomControlsPanel = new Panel { Dock = DockStyle.Bottom, Height = 75, BackColor = Color.FromArgb(24, 24, 28) };
            InitBottomBar();
            Controls.Add(_bottomControlsPanel);

            // 3. Columna Derecha de Acciones Rápidas
            _rightActionPanel = new Panel { Dock = DockStyle.Right, Width = 42, BackColor = Color.FromArgb(30, 30, 34) };
            InitRightActions();
            Controls.Add(_rightActionPanel);

            // 4. Columna Izquierda de Vista Previa y Botones de Importación
            _leftPreviewPanel = new Panel { Dock = DockStyle.Left, Width = 220, BackColor = Color.FromArgb(32, 32, 36) };
            InitLeftPreview();
            Controls.Add(_leftPreviewPanel);

            // 5. Columna Central (Lista de Capas & Selector de Fondo)
            _centerListPanel = new Panel { Dock = DockStyle.Fill, BackColor = Color.FromArgb(40, 40, 46) };
            InitCenterList();
            Controls.Add(_centerListPanel);
        }

        private void InitLeftPreview()
        {
            _pbPreview = new PictureBox
            {
                Dock = DockStyle.Fill,
                BackColor = Color.White,
                SizeMode = PictureBoxSizeMode.Zoom,
                Margin = new Padding(8)
            };
            _pbPreview.Paint += (s, e) => {
                var g = e.Graphics;
                g.InterpolationMode = InterpolationMode.HighQualityBilinear;
                // Fondo
                using (var brush = new SolidBrush(Color.White))
                {
                    g.FillRectangle(brush, _pbPreview.ClientRectangle);
                }
                // Capas compuestas
                foreach (var l in _layerManager.Layers)
                {
                    if (l.Visible && l.Bitmap != null)
                    {
                        g.DrawImage(l.Bitmap, _pbPreview.ClientRectangle, new Rectangle(0, 0, l.Bitmap.Width, l.Bitmap.Height), GraphicsUnit.Pixel);
                    }
                }
            };

            // Barra inferior bajo la vista previa: [+], [📋+], [📷], [▶|◀], [▲|▼]
            var previewBottomBar = new Panel { Dock = DockStyle.Bottom, Height = 36, BackColor = Color.FromArgb(22, 22, 26) };

            var btnAdd = CreateToolButton("+", 6, "Añadir Capa");
            btnAdd.Click += (s, e) => {
                _layerManager.AddLayer();
                _onCanvasRepaintNeeded();
            };

            var btnDup = CreateToolButton("📋+", 48, "Duplicar Capa");
            btnDup.Click += (s, e) => {
                _layerManager.DuplicateLayer();
                _onCanvasRepaintNeeded();
            };

            var btnImport = CreateToolButton("📷", 90, "Importar Imagen como Capa");
            btnImport.Click += (s, e) => {
                ShowImportImageDialog();
            };

            var btnFlipH = CreateToolButton("▶|◀", 132, "Voltear Horizontalmente");
            btnFlipH.Click += (s, e) => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.FlipHorizontal();
                    _onCanvasRepaintNeeded();
                    _pbPreview.Invalidate();
                    RefreshLayerList();
                }
            };

            var btnFlipV = CreateToolButton("▲|▼", 174, "Voltear Verticalmente");
            btnFlipV.Click += (s, e) => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.FlipVertical();
                    _onCanvasRepaintNeeded();
                    _pbPreview.Invalidate();
                    RefreshLayerList();
                }
            };

            previewBottomBar.Controls.Add(btnAdd);
            previewBottomBar.Controls.Add(btnDup);
            previewBottomBar.Controls.Add(btnImport);
            previewBottomBar.Controls.Add(btnFlipH);
            previewBottomBar.Controls.Add(btnFlipV);

            _leftPreviewPanel.Controls.Add(_pbPreview);
            _leftPreviewPanel.Controls.Add(previewBottomBar);
        }

        private void InitCenterList()
        {
            _layersScrollPanel = new Panel
            {
                Dock = DockStyle.Fill,
                AutoScroll = true,
                BackColor = Color.FromArgb(32, 32, 36)
            };

            // Selector de Fondo Inferior
            var pnlFondo = new Panel { Dock = DockStyle.Bottom, Height = 44, BackColor = Color.FromArgb(28, 28, 32) };
            var lblFondo = new Label { Text = "Fondo:", ForeColor = Color.FromArgb(200, 200, 210), Left = 12, Top = 12, AutoSize = true, Font = new Font("Segoe UI", 9f, FontStyle.Bold) };
            pnlFondo.Controls.Add(lblFondo);

            int sx = 80;
            AddBackgroundSwatch(pnlFondo, sx, Color.White, CanvasBackgroundType.White, "Fondo Blanco");
            AddBackgroundSwatch(pnlFondo, sx + 36, Color.FromArgb(220, 220, 225), CanvasBackgroundType.LightChecker, "Fondo Ajedrez Claro");
            AddBackgroundSwatch(pnlFondo, sx + 72, Color.FromArgb(80, 80, 85), CanvasBackgroundType.DarkChecker, "Fondo Ajedrez Oscuro");
            AddBackgroundSwatch(pnlFondo, sx + 108, Color.FromArgb(240, 240, 245), CanvasBackgroundType.Transparent, "Transparente");

            _centerListPanel.Controls.Add(_layersScrollPanel);
            _centerListPanel.Controls.Add(pnlFondo);
        }

        private void AddBackgroundSwatch(Panel parent, int left, Color col, CanvasBackgroundType bgType, string tooltip)
        {
            var btn = new Button
            {
                Left = left,
                Top = 8,
                Width = 30,
                Height = 28,
                BackColor = col,
                FlatStyle = FlatStyle.Flat,
                Cursor = Cursors.Hand
            };
            btn.FlatAppearance.BorderColor = (_layerManager.BackgroundType == bgType) ? Color.FromArgb(0, 150, 255) : Color.FromArgb(90, 90, 100);
            btn.FlatAppearance.BorderSize = (_layerManager.BackgroundType == bgType) ? 2 : 1;
            btn.Click += (s, e) => {
                _layerManager.BackgroundType = bgType;
                _onCanvasRepaintNeeded();
                _pbPreview.Invalidate();
                foreach (Control c in parent.Controls)
                {
                    Button b = c as Button;
                    if (b != null)
                    {
                        b.FlatAppearance.BorderColor = Color.FromArgb(90, 90, 100);
                        b.FlatAppearance.BorderSize = 1;
                    }
                }
                btn.FlatAppearance.BorderColor = Color.FromArgb(0, 150, 255);
                btn.FlatAppearance.BorderSize = 2;
            };
            parent.Controls.Add(btn);
        }

        private void InitRightActions()
        {
            int y = 6;
            int btnH = 34;

            AddActionButton("▨", y, "Limpiar Capa", () => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.Clear(Color.FromArgb(0, 0, 0, 0));
                    _onCanvasRepaintNeeded();
                    RefreshLayerList();
                }
            });
            y += btnH;

            AddActionButton("◑", y, "Invertir Color", () => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.InvertColors();
                    _onCanvasRepaintNeeded();
                    RefreshLayerList();
                }
            });
            y += btnH;

            AddActionButton("⬇", y, "Combinar Hacia Abajo", () => {
                if (_layerManager.MergeDown())
                {
                    _onCanvasRepaintNeeded();
                }
            });
            y += btnH;

            AddActionButton("🗑️", y, "Eliminar Capa Activa", () => {
                if (_layerManager.DeleteActiveLayer())
                {
                    _onCanvasRepaintNeeded();
                }
                else
                {
                    MessageBox.Show("Debe existir al menos una capa.", "Aviso", MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
            });
        }

        private void AddActionButton(string text, int top, string tooltip, Action action)
        {
            var btn = new Button
            {
                Text = text,
                Left = 5,
                Top = top,
                Width = 32,
                Height = 30,
                BackColor = Color.FromArgb(42, 42, 48),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI Emoji", 10f),
                Cursor = Cursors.Hand
            };
            btn.FlatAppearance.BorderSize = 0;
            btn.Click += (s, e) => action();
            _rightActionPanel.Controls.Add(btn);
        }

        private void InitBottomBar()
        {
            _btnAlphaLock = new Button
            {
                Text = "🔒α Bloqueo Alfa",
                Left = 14,
                Top = 8,
                Width = 110,
                Height = 26,
                BackColor = Color.FromArgb(42, 42, 48),
                ForeColor = Color.LightGray,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 8.5f),
                Cursor = Cursors.Hand
            };
            _btnAlphaLock.FlatAppearance.BorderSize = 0;
            _btnAlphaLock.Click += (s, e) => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.IsAlphaLocked = !_layerManager.ActiveLayer.IsAlphaLocked;
                    _btnAlphaLock.BackColor = _layerManager.ActiveLayer.IsAlphaLocked ? Color.FromArgb(0, 140, 230) : Color.FromArgb(42, 42, 48);
                    _btnAlphaLock.ForeColor = _layerManager.ActiveLayer.IsAlphaLocked ? Color.White : Color.LightGray;
                }
            };

            _cboBlendMode = new ComboBox
            {
                Left = 135,
                Top = 9,
                Width = 120,
                DropDownStyle = ComboBoxStyle.DropDownList,
                BackColor = Color.FromArgb(42, 42, 48),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 8.5f)
            };
            _cboBlendMode.Items.AddRange(new object[] { "Normal", "Multiplicar", "Pantalla", "Superponer", "Aclarar", "Oscurecer" });
            _cboBlendMode.SelectedIndex = 0;
            _cboBlendMode.SelectedIndexChanged += (s, e) => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.BlendMode = _cboBlendMode.SelectedItem.ToString();
                    _onCanvasRepaintNeeded();
                    RefreshLayerList();
                }
            };

            _lblOpacityVal = new Label { Text = "100%", ForeColor = Color.White, Left = 14, Top = 44, Width = 45, Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };
            
            var btnMinus = new Button { Text = "−", Left = 65, Top = 40, Width = 26, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(42, 42, 48), ForeColor = Color.White };
            btnMinus.FlatAppearance.BorderSize = 0;

            _tbOpacity = new TrackBar
            {
                Left = 95,
                Top = 40,
                Width = 490,
                Minimum = 0,
                Maximum = 100,
                Value = 100,
                TickStyle = TickStyle.None
            };

            var btnPlus = new Button { Text = "+", Left = 590, Top = 40, Width = 26, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(42, 42, 48), ForeColor = Color.White };
            btnPlus.FlatAppearance.BorderSize = 0;

            _tbOpacity.ValueChanged += (s, e) => {
                if (_layerManager.ActiveLayer != null)
                {
                    _layerManager.ActiveLayer.Opacity = _tbOpacity.Value / 100.0f;
                    _lblOpacityVal.Text = string.Format("{0}%", _tbOpacity.Value);
                    _onCanvasRepaintNeeded();
                    _pbPreview.Invalidate();
                }
            };

            btnMinus.Click += (s, e) => _tbOpacity.Value = Math.Max(0, _tbOpacity.Value - 5);
            btnPlus.Click += (s, e) => _tbOpacity.Value = Math.Min(100, _tbOpacity.Value + 5);

            _bottomControlsPanel.Controls.Add(_btnAlphaLock);
            _bottomControlsPanel.Controls.Add(_cboBlendMode);
            _bottomControlsPanel.Controls.Add(_lblOpacityVal);
            _bottomControlsPanel.Controls.Add(btnMinus);
            _bottomControlsPanel.Controls.Add(_tbOpacity);
            _bottomControlsPanel.Controls.Add(btnPlus);
        }

        public void RefreshLayerList()
        {
            _layersScrollPanel.SuspendLayout();
            _layersScrollPanel.Controls.Clear();

            int y = 6;
            int rowHeight = 62;
            int totalLayers = _layerManager.Layers.Count;

            // Renderizar desde la capa superior hacia la inferior (como en ibis Paint X)
            for (int i = totalLayers - 1; i >= 0; i--)
            {
                int layerIdx = i;
                var layer = _layerManager.Layers[layerIdx];
                bool isActive = (layerIdx == _layerManager.ActiveLayerIndex);

                var row = new Panel
                {
                    Left = 6,
                    Top = y,
                    Width = _layersScrollPanel.ClientSize.Width - 14,
                    Height = rowHeight,
                    BackColor = isActive ? Color.FromArgb(36, 56, 82) : Color.FromArgb(48, 48, 54),
                    Cursor = Cursors.Hand
                };

                // Barra indicadora azul lateral si está activa
                if (isActive)
                {
                    var activeBar = new Panel
                    {
                        Left = 0,
                        Top = 0,
                        Width = 5,
                        Height = rowHeight,
                        BackColor = Color.FromArgb(0, 160, 255)
                    };
                    row.Controls.Add(activeBar);
                }

                // Miniatura con fondo de ajedrez
                var pbThumb = new PictureBox
                {
                    Left = 10,
                    Top = 5,
                    Width = 52,
                    Height = 52,
                    BackColor = Color.White,
                    SizeMode = PictureBoxSizeMode.Zoom
                };
                pbThumb.Paint += (s, e) => {
                    var g = e.Graphics;
                    DrawCheckerboard(g, pbThumb.ClientRectangle, 6);
                    var thumb = layer.GetThumbnail(52, 52);
                    if (thumb != null)
                    {
                        g.DrawImage(thumb, 0, 0);
                    }
                    using (var pen = new Pen(isActive ? Color.FromArgb(0, 160, 255) : Color.FromArgb(100, 100, 110), isActive ? 2f : 1f))
                    {
                        g.DrawRectangle(pen, 0, 0, pbThumb.Width - 1, pbThumb.Height - 1);
                    }
                };

                // Botón Ojo Visibilidad 👁️
                var btnEye = new Button
                {
                    Text = layer.Visible ? "👁️" : "⎯",
                    Left = 66,
                    Top = 16,
                    Width = 30,
                    Height = 30,
                    FlatStyle = FlatStyle.Flat,
                    BackColor = Color.Transparent,
                    ForeColor = layer.Visible ? Color.White : Color.Gray,
                    Font = new Font("Segoe UI Emoji", 11f),
                    Cursor = Cursors.Hand
                };
                btnEye.FlatAppearance.BorderSize = 0;
                btnEye.Click += (s, e) => {
                    layer.Visible = !layer.Visible;
                    btnEye.Text = layer.Visible ? "👁️" : "⎯";
                    _onCanvasRepaintNeeded();
                    _pbPreview.Invalidate();
                };

                // Nombre e ID de la Capa (con etiqueta CLARA de [ACTIVA])
                string titleText = isActive ? string.Format("{0} [ACTIVA]", layer.Name) : layer.Name;
                var lblName = new Label
                {
                    Text = titleText,
                    Left = 102,
                    Top = 10,
                    Font = new Font("Segoe UI", 10f, FontStyle.Bold),
                    ForeColor = isActive ? Color.FromArgb(80, 200, 255) : Color.White,
                    AutoSize = true
                };

                // Info: ID único, opacidad y modo de fusión
                var lblInfo = new Label
                {
                    Text = string.Format("ID: {0} • {1:P0} {2}", layer.Id, layer.Opacity, layer.BlendMode),
                    Left = 102,
                    Top = 32,
                    Font = new Font("Segoe UI", 8.2f),
                    ForeColor = isActive ? Color.FromArgb(180, 220, 250) : Color.FromArgb(150, 150, 160),
                    AutoSize = true
                };

                // Botón SUBIR capa (▲)
                var btnUp = new Button
                {
                    Text = "▲",
                    Left = row.Width - 92,
                    Top = 15,
                    Width = 26,
                    Height = 30,
                    BackColor = Color.FromArgb(60, 60, 68),
                    ForeColor = (layerIdx < totalLayers - 1) ? Color.White : Color.Gray,
                    FlatStyle = FlatStyle.Flat,
                    Font = new Font("Segoe UI", 8.5f, FontStyle.Bold),
                    Cursor = Cursors.Hand
                };
                btnUp.FlatAppearance.BorderSize = 0;
                btnUp.Enabled = (layerIdx < totalLayers - 1);
                btnUp.Click += (s, e) => {
                    _layerManager.MoveLayerUp(layerIdx);
                    _onCanvasRepaintNeeded();
                };

                // Botón BAJAR capa (▼)
                var btnDown = new Button
                {
                    Text = "▼",
                    Left = row.Width - 63,
                    Top = 15,
                    Width = 26,
                    Height = 30,
                    BackColor = Color.FromArgb(60, 60, 68),
                    ForeColor = (layerIdx > 0) ? Color.White : Color.Gray,
                    FlatStyle = FlatStyle.Flat,
                    Font = new Font("Segoe UI", 8.5f, FontStyle.Bold),
                    Cursor = Cursors.Hand
                };
                btnDown.FlatAppearance.BorderSize = 0;
                btnDown.Enabled = (layerIdx > 0);
                btnDown.Click += (s, e) => {
                    _layerManager.MoveLayerDown(layerIdx);
                    _onCanvasRepaintNeeded();
                };

                // Botón BORRAR esta capa (🗑️)
                var btnDeleteThis = new Button
                {
                    Text = "🗑️",
                    Left = row.Width - 34,
                    Top = 15,
                    Width = 28,
                    Height = 30,
                    BackColor = Color.FromArgb(75, 35, 40),
                    ForeColor = Color.White,
                    FlatStyle = FlatStyle.Flat,
                    Font = new Font("Segoe UI Emoji", 9f),
                    Cursor = Cursors.Hand
                };
                btnDeleteThis.FlatAppearance.BorderSize = 0;
                btnDeleteThis.Click += (s, e) => {
                    if (!_layerManager.DeleteLayerAt(layerIdx))
                    {
                        MessageBox.Show("Debe existir al menos una capa.", "Aviso", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    }
                    else
                    {
                        _onCanvasRepaintNeeded();
                    }
                };

                // Clic para activar capa
                Action activateAction = () => {
                    _layerManager.ActiveLayerIndex = layerIdx;
                    UpdateActiveLayerControls();
                    RefreshLayerList();
                };

                row.Click += (s, e) => activateAction();
                pbThumb.Click += (s, e) => activateAction();
                lblName.Click += (s, e) => activateAction();
                lblInfo.Click += (s, e) => activateAction();

                row.Controls.Add(pbThumb);
                row.Controls.Add(btnEye);
                row.Controls.Add(lblName);
                row.Controls.Add(lblInfo);
                row.Controls.Add(btnUp);
                row.Controls.Add(btnDown);
                row.Controls.Add(btnDeleteThis);

                _layersScrollPanel.Controls.Add(row);
                y += rowHeight + 5;
            }

            _layersScrollPanel.ResumeLayout(true);
            UpdateActiveLayerControls();
            _pbPreview.Invalidate();
        }

        private void UpdateActiveLayerControls()
        {
            var active = _layerManager.ActiveLayer;
            if (active != null)
            {
                _tbOpacity.Value = Math.Max(0, Math.Min(100, (int)(active.Opacity * 100)));
                _lblOpacityVal.Text = string.Format("{0}%", _tbOpacity.Value);
                _btnAlphaLock.BackColor = active.IsAlphaLocked ? Color.FromArgb(0, 140, 230) : Color.FromArgb(42, 42, 48);
                _btnAlphaLock.ForeColor = active.IsAlphaLocked ? Color.White : Color.LightGray;

                for (int i = 0; i < _cboBlendMode.Items.Count; i++)
                {
                    if (_cboBlendMode.Items[i].ToString() == active.BlendMode)
                    {
                        _cboBlendMode.SelectedIndex = i;
                        break;
                    }
                }
            }
        }

        private void ShowImportImageDialog()
        {
            using (var ofd = new OpenFileDialog())
            {
                ofd.Title = "Importar Imagen como Capa (WebP, JPG, PNG, GIF, BMP...)";
                ofd.Filter = "Imágenes (*.webp;*.jpg;*.jpeg;*.png;*.gif;*.bmp)|*.webp;*.jpg;*.jpeg;*.png;*.gif;*.bmp|Todos los Archivos (*.*)|*.*";
                if (ofd.ShowDialog() == DialogResult.OK)
                {
                    var result = MessageBox.Show(
                        "¿Deseas importar la imagen SOBRE la capa actual?\n\n(Elige 'Sí' para colocarla encima, o 'No' para colocarla debajo).",
                        "Posición de la Nueva Capa",
                        MessageBoxButtons.YesNoCancel,
                        MessageBoxIcon.Question
                    );

                    if (result == DialogResult.Cancel) return;

                    bool above = (result == DialogResult.Yes);
                    var importedLayer = _layerManager.ImportImageAsLayer(ofd.FileName, above);
                    if (importedLayer != null)
                    {
                        _onCanvasRepaintNeeded();
                        RefreshLayerList();
                    }
                }
            }
        }

        private Button CreateToolButton(string text, int left, string tooltip)
        {
            var btn = new Button
            {
                Text = text,
                Left = left,
                Top = 4,
                Width = 38,
                Height = 28,
                BackColor = Color.FromArgb(48, 48, 54),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI Emoji", 9.5f),
                Cursor = Cursors.Hand
            };
            btn.FlatAppearance.BorderSize = 0;
            return btn;
        }

        private void DrawCheckerboard(Graphics g, Rectangle bounds, int size)
        {
            for (int x = bounds.Left; x < bounds.Right; x += size)
            {
                for (int y = bounds.Top; y < bounds.Bottom; y += size)
                {
                    bool isEven = ((x / size) + (y / size)) % 2 == 0;
                    using (var b = new SolidBrush(isEven ? Color.FromArgb(245, 245, 250) : Color.FromArgb(215, 215, 222)))
                    {
                        g.FillRectangle(b, x, y, size, size);
                    }
                }
            }
        }
    }
}
