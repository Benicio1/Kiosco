$csharpCode = @"
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Drawing.Text;

public class IconGenerator
{
    public static void Generate(int size, string path, bool maskable)
    {
        using (Bitmap bmp = new Bitmap(size, size))
        using (Graphics g = Graphics.FromImage(bmp))
        {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            g.InterpolationMode = InterpolationMode.HighQualityBicubic;
            g.PixelOffsetMode = PixelOffsetMode.HighQuality;
            g.TextRenderingHint = TextRenderingHint.AntiAliasGridFit;

            Rectangle rect = new Rectangle(0, 0, size, size);

            if (maskable)
            {
                using (LinearGradientBrush brush = new LinearGradientBrush(rect, ColorTranslator.FromHtml("#1e3a8a"), ColorTranslator.FromHtml("#0f172a"), 45f))
                {
                    g.FillRectangle(brush, rect);
                }
            }
            else
            {
                using (LinearGradientBrush bgBrush = new LinearGradientBrush(rect, ColorTranslator.FromHtml("#090d16"), ColorTranslator.FromHtml("#0f172a"), 90f))
                {
                    g.FillRectangle(bgBrush, rect);
                }

                int pad = (int)(size * 0.08);
                Rectangle inner = new Rectangle(pad, pad, size - 2 * pad, size - 2 * pad);
                int r = (int)(size * 0.22);
                int d = r * 2;
                using (GraphicsPath pathShape = new GraphicsPath())
                {
                    pathShape.AddArc(inner.X, inner.Y, d, d, 180, 90);
                    pathShape.AddArc(inner.Right - d, inner.Y, d, d, 270, 90);
                    pathShape.AddArc(inner.Right - d, inner.Bottom - d, d, d, 0, 90);
                    pathShape.AddArc(inner.X, inner.Bottom - d, d, d, 90, 90);
                    pathShape.CloseFigure();

                    using (LinearGradientBrush cardBrush = new LinearGradientBrush(inner, ColorTranslator.FromHtml("#2563eb"), ColorTranslator.FromHtml("#4f46e5"), 45f))
                    {
                        g.FillPath(cardBrush, pathShape);
                    }
                    using (Pen borderPen = new Pen(ColorTranslator.FromHtml("#60a5fa"), Math.Max(2f, size * 0.02f)))
                    {
                        g.DrawPath(borderPen, pathShape);
                    }
                }
            }

            // Draw cart emoji
            int fontSize = (int)(size * 0.44);
            using (Font font = new Font("Segoe UI Emoji", fontSize, FontStyle.Bold, GraphicsUnit.Pixel))
            using (StringFormat sf = new StringFormat())
            {
                sf.Alignment = StringAlignment.Center;
                sf.LineAlignment = StringAlignment.Center;
                RectangleF textRect = new RectangleF(0, size * 0.04f, size, size);
                g.DrawString(Char.ConvertFromUtf32(0x1F6D2), font, Brushes.White, textRect, sf);
            }

            bmp.Save(path, ImageFormat.Png);
        }
    }
}
"@

Add-Type -TypeDefinition $csharpCode -ReferencedAssemblies "System.Drawing"

$iconsDir = Join-Path $PSScriptRoot "..\icons"
if (-not (Test-Path $iconsDir)) {
    New-Item -ItemType Directory -Path $iconsDir -Force | Out-Null
}

[IconGenerator]::Generate(192, (Join-Path $iconsDir "icon-192.png"), $false)
[IconGenerator]::Generate(512, (Join-Path $iconsDir "icon-512.png"), $false)
[IconGenerator]::Generate(180, (Join-Path $iconsDir "apple-touch-icon.png"), $false)
[IconGenerator]::Generate(192, (Join-Path $iconsDir "icon-maskable-192.png"), $true)
[IconGenerator]::Generate(512, (Join-Path $iconsDir "icon-maskable-512.png"), $true)

Write-Output "Iconos generados exitosamente en $iconsDir"
