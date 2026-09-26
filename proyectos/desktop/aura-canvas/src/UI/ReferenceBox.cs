using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Windows.Forms;
using System.Windows.Media.Imaging;

namespace AuraPaint.UI
{
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
            Height = 380;
            BackColor = Color.FromArgb(24, 24, 30);
            DoubleBuffered = true;
            Visible = false;
            AllowDrop = true;

            DragEnter += (s, e) => {
                if (e.Data.GetDataPresent(DataFormats.FileDrop)) e.Effect = DragDropEffects.Copy;
            };

            DragDrop += (s, e) => {
                string[] files = (string[])e.Data.GetData(DataFormats.FileDrop);
                if (files != null && files.Length > 0)
                {
                    LoadUniversalImage(files[0]);
                }
            };

            // Encabezado
            var pnlHeader = new Panel { Dock = DockStyle.Top, Height = 28, BackColor = Color.FromArgb(36, 36, 44) };
            var lblTitle = new Label { Text = "🖼️ Sub-Vista / Referencia", Left = 8, Top = 5, ForeColor = Color.LightGray, Font = new Font("Segoe UI", 8.2f, FontStyle.Bold), AutoSize = true };
            
            var btnOpen = new Button { Text = "📂 Abrir", Left = 160, Top = 2, Width = 64, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 65), ForeColor = Color.White, Font = new Font("Segoe UI", 7.5f), TabStop = false };
            btnOpen.FlatAppearance.BorderSize = 0;
            btnOpen.Click += (s, e) => ShowOpenDialog();

            var btnFit = new Button { Text = "⛶", Left = 230, Top = 2, Width = 28, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(50, 50, 65), ForeColor = Color.White, TabStop = false };
            btnFit.FlatAppearance.BorderSize = 0;
            btnFit.Click += (s, e) => FitImage();

            var btnClose = new Button { Text = "✕", Left = 300, Top = 2, Width = 28, Height = 24, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(70, 30, 35), ForeColor = Color.White, TabStop = false };
            btnClose.FlatAppearance.BorderSize = 0;
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

                // Intento 1: Decodificador Universal de Windows (WPF/WIC con Stream para evitar bugs de URI)
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
                MessageBox.Show(string.Format("No se pudo cargar la imagen ({0}): {1}", Path.GetFileName(filePath), ex.Message), "Error de Formato", MessageBoxButtons.OK, MessageBoxIcon.Error);
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

            if (e.Button == MouseButtons.Left && _referenceImage != null)
            {
                // Cuentagotas en la referencia
                float imgX = (e.X - _refPanX) / _refZoom;
                float imgY = (e.Y - _refPanY) / _refZoom;
                int ix = (int)Math.Floor(imgX);
                int iy = (int)Math.Floor(imgY);

                if (ix >= 0 && ix < _referenceImage.Width && iy >= 0 && iy < _referenceImage.Height)
                {
                    Color picked = _referenceImage.GetPixel(ix, iy);
                    if (OnColorPicked != null) OnColorPicked(picked);
                    return;
                }
            }

            if (e.Button == MouseButtons.Right || e.Button == MouseButtons.Middle)
            {
                _isDragging = true;
                _dragStart = e.Location;
            }
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
            _isDragging = false;
        }

        protected override void OnMouseWheel(MouseEventArgs e)
        {
            base.OnMouseWheel(e);
            if (_referenceImage == null) return;

            float zoomFactor = e.Delta > 0 ? 1.15f : 0.85f;
            float newZoom = Math.Max(0.05f, Math.Min(20.0f, _refZoom * zoomFactor));

            _refPanX = e.X - (e.X - _refPanX) * (newZoom / _refZoom);
            _refPanY = e.Y - (e.Y - _refPanY) * (newZoom / _refZoom);
            _refZoom = newZoom;
            Invalidate();
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e);
            var g = e.Graphics;
            g.PixelOffsetMode = PixelOffsetMode.Half;
            g.InterpolationMode = _refZoom < 1.0f ? InterpolationMode.Bilinear : InterpolationMode.NearestNeighbor;

            if (_referenceImage != null)
            {
                g.DrawImage(_referenceImage, _refPanX, _refPanY, _referenceImage.Width * _refZoom, _referenceImage.Height * _refZoom);
            }
            else
            {
                using (var font = new Font("Segoe UI", 9f))
                using (var brush = new SolidBrush(Color.FromArgb(140, 140, 155)))
                {
                    var sf = new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Center };
                    g.DrawString("Arrastra una imagen aquí\n(WebP, JPG, PNG, GIF, BMP)\no haz clic en 'Abrir'", font, brush, new RectangleF(0, 28, Width, Height - 28), sf);
                }
            }

            using (var borderPen = new Pen(Color.FromArgb(50, 50, 65)))
            {
                g.DrawRectangle(borderPen, 0, 0, Width - 1, Height - 1);
            }
        }
    }
}
