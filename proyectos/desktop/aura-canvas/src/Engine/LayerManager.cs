using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Windows.Media.Imaging;
using AuraPaint.Models;

namespace AuraPaint.Engine
{
    public enum CanvasBackgroundType
    {
        White,
        LightChecker,
        DarkChecker,
        Transparent
    }

    public class LayerManager : IDisposable
    {
        public int CanvasWidth { get; private set; }
        public int CanvasHeight { get; private set; }
        public CanvasBackgroundType BackgroundType { get; set; }

        public List<DrawingLayer> Layers { get; private set; }
        public int ActiveLayerIndex { get; set; }
        private int _idCounter = 1;

        public event Action OnLayersChanged;
        public event Action<IUndoableAction> OnUndoActionGenerated;

        public DrawingLayer ActiveLayer
        {
            get
            {
                if (Layers.Count == 0) return null;
                if (ActiveLayerIndex < 0) ActiveLayerIndex = 0;
                if (ActiveLayerIndex >= Layers.Count) ActiveLayerIndex = Layers.Count - 1;
                return Layers[ActiveLayerIndex];
            }
        }

        public LayerManager(int width, int height)
        {
            CanvasWidth = width;
            CanvasHeight = height;
            BackgroundType = CanvasBackgroundType.White;
            Layers = new List<DrawingLayer>();

            // Capa 1 inicial
            _idCounter = 1;
            var initialLayer = new DrawingLayer(_idCounter, "Capa 1", width, height, Color.FromArgb(0, 0, 0, 0));
            Layers.Add(initialLayer);
            ActiveLayerIndex = 0;
        }

        public void ResizeCanvas(int newWidth, int newHeight, bool clear)
        {
            CanvasWidth = newWidth;
            CanvasHeight = newHeight;

            foreach (var l in Layers)
            {
                if (clear)
                {
                    l.InitBitmap(newWidth, newHeight, Color.FromArgb(0, 0, 0, 0));
                }
                else
                {
                    var oldBmp = l.Bitmap;
                    l.InitBitmap(newWidth, newHeight, Color.FromArgb(0, 0, 0, 0));
                    using (Graphics g = Graphics.FromImage(l.Bitmap))
                    {
                        g.DrawImage(oldBmp, 0, 0);
                    }
                    oldBmp.Dispose();
                }
            }
            TriggerLayersChanged();
        }

        public DrawingLayer AddLayer(string name = null, bool aboveActive = true)
        {
            _idCounter++;
            string layerName = name ?? ("Capa " + _idCounter);
            var newLayer = new DrawingLayer(_idCounter, layerName, CanvasWidth, CanvasHeight, Color.FromArgb(0, 0, 0, 0));

            int insertIdx;
            if (aboveActive && Layers.Count > 0)
            {
                insertIdx = ActiveLayerIndex + 1;
                if (insertIdx > Layers.Count) insertIdx = Layers.Count;
            }
            else
            {
                insertIdx = Math.Max(0, ActiveLayerIndex);
            }

            Layers.Insert(insertIdx, newLayer);
            ActiveLayerIndex = insertIdx;

            if (OnUndoActionGenerated != null)
            {
                OnUndoActionGenerated(new UndoAddLayerAction { Manager = this, Layer = newLayer, Index = insertIdx });
            }

            TriggerLayersChanged();
            return newLayer;
        }

        public DrawingLayer DuplicateLayer()
        {
            if (ActiveLayer == null) return null;

            var current = ActiveLayer;
            _idCounter++;
            string layerName = current.Name + " (copia)";
            var dup = new DrawingLayer(_idCounter, layerName, CanvasWidth, CanvasHeight, Color.FromArgb(0, 0, 0, 0));
            dup.Opacity = current.Opacity;
            dup.Visible = current.Visible;
            dup.BlendMode = current.BlendMode;

            using (Graphics g = Graphics.FromImage(dup.Bitmap))
            {
                g.CompositingMode = CompositingMode.SourceCopy;
                g.DrawImage(current.Bitmap, 0, 0);
            }
            dup.InvalidateThumbnail();

            int insertIdx = ActiveLayerIndex + 1;
            Layers.Insert(insertIdx, dup);
            ActiveLayerIndex = insertIdx;

            if (OnUndoActionGenerated != null)
            {
                OnUndoActionGenerated(new UndoAddLayerAction { Manager = this, Layer = dup, Index = insertIdx });
            }

            TriggerLayersChanged();
            return dup;
        }

        public bool DeleteActiveLayer()
        {
            return DeleteLayerAt(ActiveLayerIndex);
        }

        public bool DeleteLayerAt(int index)
        {
            if (Layers.Count <= 1) return false; // Mantener al menos una capa
            if (index < 0 || index >= Layers.Count) return false;

            var layerToRemove = Layers[index];
            int oldIdx = index;
            Layers.RemoveAt(index);

            if (ActiveLayerIndex >= Layers.Count)
            {
                ActiveLayerIndex = Layers.Count - 1;
            }

            if (OnUndoActionGenerated != null)
            {
                OnUndoActionGenerated(new UndoDeleteLayerAction { Manager = this, Layer = layerToRemove, Index = oldIdx });
            }

            TriggerLayersChanged();
            return true;
        }

        public void MoveLayerUp(int index)
        {
            if (index < Layers.Count - 1)
            {
                MoveLayerInternal(index, index + 1);
                ActiveLayerIndex = index + 1;
                if (OnUndoActionGenerated != null)
                {
                    OnUndoActionGenerated(new UndoMoveLayerAction { Manager = this, FromIndex = index, ToIndex = index + 1 });
                }
                TriggerLayersChanged();
            }
        }

        public void MoveLayerDown(int index)
        {
            if (index > 0)
            {
                MoveLayerInternal(index, index - 1);
                ActiveLayerIndex = index - 1;
                if (OnUndoActionGenerated != null)
                {
                    OnUndoActionGenerated(new UndoMoveLayerAction { Manager = this, FromIndex = index, ToIndex = index - 1 });
                }
                TriggerLayersChanged();
            }
        }

        public void MoveLayerInternal(int fromIdx, int toIdx)
        {
            if (fromIdx >= 0 && fromIdx < Layers.Count && toIdx >= 0 && toIdx < Layers.Count)
            {
                var layer = Layers[fromIdx];
                Layers.RemoveAt(fromIdx);
                Layers.Insert(toIdx, layer);
                ActiveLayerIndex = toIdx;
                TriggerLayersChanged();
            }
        }

        public void InsertLayerWithoutUndo(int index, DrawingLayer layer)
        {
            if (layer != null)
            {
                if (index < 0) index = 0;
                if (index > Layers.Count) index = Layers.Count;
                Layers.Insert(index, layer);
                ActiveLayerIndex = index;
                TriggerLayersChanged();
            }
        }

        public void RemoveLayerWithoutUndo(DrawingLayer layer)
        {
            if (layer != null && Layers.Contains(layer))
            {
                if (Layers.Count > 1)
                {
                    Layers.Remove(layer);
                    if (ActiveLayerIndex >= Layers.Count) ActiveLayerIndex = Layers.Count - 1;
                    TriggerLayersChanged();
                }
            }
        }

        public bool MergeDown()
        {
            if (ActiveLayerIndex <= 0 || Layers.Count <= 1) return false;

            var upper = Layers[ActiveLayerIndex];
            var lower = Layers[ActiveLayerIndex - 1];

            using (Graphics g = Graphics.FromImage(lower.Bitmap))
            {
                if (upper.Opacity < 1.0f)
                {
                    ColorMatrix matrix = new ColorMatrix { Matrix33 = upper.Opacity };
                    using (ImageAttributes attr = new ImageAttributes())
                    {
                        attr.SetColorMatrix(matrix, ColorMatrixFlag.Default, ColorAdjustType.Bitmap);
                        g.DrawImage(upper.Bitmap, new Rectangle(0, 0, CanvasWidth, CanvasHeight), 0, 0, CanvasWidth, CanvasHeight, GraphicsUnit.Pixel, attr);
                    }
                }
                else
                {
                    g.DrawImage(upper.Bitmap, 0, 0);
                }
            }
            lower.InvalidateThumbnail();

            Layers.RemoveAt(ActiveLayerIndex);
            upper.Dispose();
            ActiveLayerIndex--;

            TriggerLayersChanged();
            return true;
        }

        public DrawingLayer ImportImageAsLayer(string filePath, bool aboveActive = true)
        {
            Bitmap img = LoadUniversalBitmap(filePath);
            if (img == null) return null;

            _idCounter++;
            string fileName = Path.GetFileNameWithoutExtension(filePath);
            string layerName = string.IsNullOrEmpty(fileName) ? ("Capa " + _idCounter) : ("Capa " + _idCounter + ": " + fileName);

            var newLayer = new DrawingLayer(_idCounter, layerName, CanvasWidth, CanvasHeight, Color.FromArgb(0, 0, 0, 0));
            using (Graphics g = Graphics.FromImage(newLayer.Bitmap))
            {
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                float scale = Math.Min((float)CanvasWidth / img.Width, (float)CanvasHeight / img.Height);
                int destW = (int)(img.Width * scale);
                int destH = (int)(img.Height * scale);
                int destX = (CanvasWidth - destW) / 2;
                int destY = (CanvasHeight - destH) / 2;

                g.DrawImage(img, new Rectangle(destX, destY, destW, destH), 0, 0, img.Width, img.Height, GraphicsUnit.Pixel);
            }
            img.Dispose();
            newLayer.InvalidateThumbnail();

            int insertIdx = aboveActive ? (ActiveLayerIndex + 1) : ActiveLayerIndex;
            if (insertIdx > Layers.Count) insertIdx = Layers.Count;
            if (insertIdx < 0) insertIdx = 0;

            Layers.Insert(insertIdx, newLayer);
            ActiveLayerIndex = insertIdx;

            if (OnUndoActionGenerated != null)
            {
                OnUndoActionGenerated(new UndoAddLayerAction { Manager = this, Layer = newLayer, Index = insertIdx });
            }

            TriggerLayersChanged();
            return newLayer;
        }

        private Bitmap LoadUniversalBitmap(string filePath)
        {
            byte[] fileBytes = null;
            try { fileBytes = File.ReadAllBytes(filePath); } catch { return null; }

            // WIC / WPF
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
                        var bmp = new Bitmap(w, h, PixelFormat.Format32bppArgb);
                        BitmapData bmpData = bmp.LockBits(new Rectangle(0, 0, w, h), ImageLockMode.WriteOnly, PixelFormat.Format32bppArgb);
                        var converted = new FormatConvertedBitmap(frame, System.Windows.Media.PixelFormats.Bgra32, null, 0);
                        converted.CopyPixels(System.Windows.Int32Rect.Empty, bmpData.Scan0, h * bmpData.Stride, bmpData.Stride);
                        bmp.UnlockBits(bmpData);
                        return bmp;
                    }
                }
            }
            catch { }

            // GDI+ Fallback
            try
            {
                using (var ms = new MemoryStream(fileBytes))
                {
                    using (var temp = Image.FromStream(ms, true, false))
                    {
                        var bmp = new Bitmap(temp.Width, temp.Height, PixelFormat.Format32bppArgb);
                        using (Graphics g = Graphics.FromImage(bmp))
                        {
                            g.DrawImage(temp, 0, 0);
                        }
                        return bmp;
                    }
                }
            }
            catch { }

            return null;
        }

        public void TriggerLayersChanged()
        {
            if (OnLayersChanged != null) OnLayersChanged();
        }

        public void Dispose()
        {
            foreach (var l in Layers) l.Dispose();
            Layers.Clear();
        }
    }
}
