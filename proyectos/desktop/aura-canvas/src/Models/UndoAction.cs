using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using AuraPaint.Engine;

namespace AuraPaint.Models
{
    public interface IUndoableAction : IDisposable
    {
        void Undo();
        void Redo();
    }

    public class UndoStrokeAction : IUndoableAction
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
                Layer.Graphics.DrawImage(BeforeCrop, new Rectangle(DirtyBounds.X, DirtyBounds.Y, DirtyBounds.Width, DirtyBounds.Height), 0, 0, DirtyBounds.Width, DirtyBounds.Height, GraphicsUnit.Pixel);
                Layer.Graphics.CompositingMode = CompositingMode.SourceOver;
                Layer.InvalidateThumbnail();
            }
        }

        public void Redo()
        {
            if (AfterCrop != null && Layer != null && Layer.Graphics != null)
            {
                Layer.Graphics.CompositingMode = CompositingMode.SourceCopy;
                Layer.Graphics.DrawImage(AfterCrop, new Rectangle(DirtyBounds.X, DirtyBounds.Y, DirtyBounds.Width, DirtyBounds.Height), 0, 0, DirtyBounds.Width, DirtyBounds.Height, GraphicsUnit.Pixel);
                Layer.Graphics.CompositingMode = CompositingMode.SourceOver;
                Layer.InvalidateThumbnail();
            }
        }

        public void Dispose()
        {
            if (BeforeCrop != null) { BeforeCrop.Dispose(); BeforeCrop = null; }
            if (AfterCrop != null) { AfterCrop.Dispose(); AfterCrop = null; }
        }
    }

    public class UndoAddLayerAction : IUndoableAction
    {
        public LayerManager Manager;
        public DrawingLayer Layer;
        public int Index;

        public void Undo()
        {
            if (Manager != null && Layer != null)
            {
                Manager.RemoveLayerWithoutUndo(Layer);
            }
        }

        public void Redo()
        {
            if (Manager != null && Layer != null)
            {
                Manager.InsertLayerWithoutUndo(Index, Layer);
            }
        }

        public void Dispose() { }
    }

    public class UndoDeleteLayerAction : IUndoableAction
    {
        public LayerManager Manager;
        public DrawingLayer Layer;
        public int Index;

        public void Undo()
        {
            if (Manager != null && Layer != null)
            {
                Manager.InsertLayerWithoutUndo(Index, Layer);
            }
        }

        public void Redo()
        {
            if (Manager != null && Layer != null)
            {
                Manager.RemoveLayerWithoutUndo(Layer);
            }
        }

        public void Dispose() { }
    }

    public class UndoMoveLayerAction : IUndoableAction
    {
        public LayerManager Manager;
        public int FromIndex;
        public int ToIndex;

        public void Undo()
        {
            if (Manager != null)
            {
                Manager.MoveLayerInternal(ToIndex, FromIndex);
            }
        }

        public void Redo()
        {
            if (Manager != null)
            {
                Manager.MoveLayerInternal(FromIndex, ToIndex);
            }
        }

        public void Dispose() { }
    }
}
