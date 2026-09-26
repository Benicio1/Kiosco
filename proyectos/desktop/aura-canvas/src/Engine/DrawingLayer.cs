using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;

namespace AuraPaint.Engine
{
    public class DrawingLayer : IDisposable
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public float Opacity { get; set; }
        public bool Visible { get; set; }
        public bool IsAlphaLocked { get; set; }
        public string BlendMode { get; set; }

        public Bitmap Bitmap { get; private set; }
        public Graphics Graphics { get; private set; }
        
        private Bitmap _thumbnail = null;
        private bool _thumbDirty = true;

        public DrawingLayer(int id, string name, int width, int height, Color initialColor)
        {
            Id = id;
            Name = name;
            Opacity = 1.0f;
            Visible = true;
            IsAlphaLocked = false;
            BlendMode = "Normal";
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

            if (initialColor.A > 0)
            {
                using (var brush = new SolidBrush(initialColor))
                {
                    Graphics.FillRectangle(brush, 0, 0, width, height);
                }
            }
            InvalidateThumbnail();
        }

        public void Clear(Color col)
        {
            Graphics.CompositingMode = CompositingMode.SourceCopy;
            using (var brush = new SolidBrush(col))
            {
                Graphics.FillRectangle(brush, 0, 0, Bitmap.Width, Bitmap.Height);
            }
            Graphics.CompositingMode = CompositingMode.SourceOver;
            InvalidateThumbnail();
        }

        public void InvalidateThumbnail()
        {
            _thumbDirty = true;
        }

        public Bitmap GetThumbnail(int thumbWidth = 54, int thumbHeight = 54)
        {
            if (!_thumbDirty && _thumbnail != null && _thumbnail.Width == thumbWidth && _thumbnail.Height == thumbHeight)
            {
                return _thumbnail;
            }

            if (_thumbnail != null) { _thumbnail.Dispose(); _thumbnail = null; }
            _thumbnail = new Bitmap(thumbWidth, thumbHeight, PixelFormat.Format32bppPArgb);

            using (var g = Graphics.FromImage(_thumbnail))
            {
                g.Clear(Color.Transparent);
                g.CompositingMode = CompositingMode.SourceCopy;
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                g.PixelOffsetMode = PixelOffsetMode.Half;
                g.CompositingQuality = CompositingQuality.HighQuality;
                g.SmoothingMode = SmoothingMode.AntiAlias;

                g.DrawImage(Bitmap, new Rectangle(0, 0, thumbWidth, thumbHeight), 0, 0, Bitmap.Width, Bitmap.Height, GraphicsUnit.Pixel);
            }

            _thumbDirty = false;
            return _thumbnail;
        }

        public void FlipHorizontal()
        {
            Bitmap.RotateFlip(RotateFlipType.RotateNoneFlipX);
            InvalidateThumbnail();
        }

        public void FlipVertical()
        {
            Bitmap.RotateFlip(RotateFlipType.RotateNoneFlipY);
            InvalidateThumbnail();
        }

        public void InvertColors()
        {
            BitmapData data = Bitmap.LockBits(new Rectangle(0, 0, Bitmap.Width, Bitmap.Height), ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
            int bytes = Math.Abs(data.Stride) * Bitmap.Height;
            byte[] rgb = new byte[bytes];
            System.Runtime.InteropServices.Marshal.Copy(data.Scan0, rgb, 0, bytes);

            for (int i = 0; i < bytes; i += 4)
            {
                byte a = rgb[i + 3];
                if (a > 0)
                {
                    rgb[i] = (byte)(255 - rgb[i]);         // B
                    rgb[i + 1] = (byte)(255 - rgb[i + 1]); // G
                    rgb[i + 2] = (byte)(255 - rgb[i + 2]); // R
                }
            }

            System.Runtime.InteropServices.Marshal.Copy(rgb, 0, data.Scan0, bytes);
            Bitmap.UnlockBits(data);
            InvalidateThumbnail();
        }

        public void Dispose()
        {
            if (Graphics != null) { Graphics.Dispose(); Graphics = null; }
            if (Bitmap != null) { Bitmap.Dispose(); Bitmap = null; }
            if (_thumbnail != null) { _thumbnail.Dispose(); _thumbnail = null; }
        }
    }
}
