using System;
using System.Drawing;
using System.Windows.Forms;

namespace AuraPaint.UI
{
    public class CanvasSizeDialog : Form
    {
        public int CanvasWidth { get; private set; }
        public int CanvasHeight { get; private set; }
        public bool ClearCanvas { get; private set; }

        private NumericUpDown numWidth;
        private NumericUpDown numHeight;
        private CheckBox chkClear;
        private ComboBox cboPresets;

        public CanvasSizeDialog(int currentWidth, int currentHeight)
        {
            CanvasWidth = currentWidth;
            CanvasHeight = currentHeight;

            Text = "Tamaño del Lienzo — AuraPaint";
            StartPosition = FormStartPosition.CenterParent;
            Width = 420;
            Height = 320;
            FormBorderStyle = FormBorderStyle.FixedDialog;
            MaximizeBox = false;
            MinimizeBox = false;
            BackColor = Color.FromArgb(32, 32, 38);
            ForeColor = Color.White;

            InitializeComponents();
        }

        private void InitializeComponents()
        {
            var lblPreset = new Label { Text = "Preajustes estándar:", Left = 20, Top = 20, Width = 150, AutoSize = true };
            cboPresets = new ComboBox { Left = 20, Top = 42, Width = 360, DropDownStyle = ComboBoxStyle.DropDownList, BackColor = Color.FromArgb(45, 45, 55), ForeColor = Color.White, FlatStyle = FlatStyle.Flat };
            cboPresets.Items.AddRange(new object[] {
                "Personalizado",
                "A4 Ilustración Manga (300 DPI) - 2480 x 3508 px",
                "A4 Impresión Alta Definición (600 DPI) - 4960 x 7016 px",
                "4K Ultra HD (16:9) - 3840 x 2160 px",
                "Full HD (16:9) - 1920 x 1080 px",
                "Cuadrado Redes Sociales (1:1) - 2048 x 2048 px",
                "Póster Ilustrado (3:2) - 3000 x 2000 px"
            });
            cboPresets.SelectedIndex = 0;
            cboPresets.SelectedIndexChanged += OnPresetChanged;

            var lblWidth = new Label { Text = "Ancho (px):", Left = 20, Top = 85, Width = 120 };
            numWidth = new NumericUpDown { Left = 20, Top = 110, Width = 160, Minimum = 100, Maximum = 16000, Value = CanvasWidth, BackColor = Color.FromArgb(45, 45, 55), ForeColor = Color.White };

            var lblHeight = new Label { Text = "Alto (px):", Left = 220, Top = 85, Width = 120 };
            numHeight = new NumericUpDown { Left = 220, Top = 110, Width = 160, Minimum = 100, Maximum = 16000, Value = CanvasHeight, BackColor = Color.FromArgb(45, 45, 55), ForeColor = Color.White };

            chkClear = new CheckBox { Text = "Limpiar lienzo al redimensionar (crear nuevo lienzo en blanco)", Left = 20, Top = 160, Width = 360, Checked = false, ForeColor = Color.FromArgb(200, 200, 210) };

            var btnOk = new Button { Text = "Aplicar", Left = 180, Top = 225, Width = 95, Height = 34, DialogResult = DialogResult.OK, BackColor = Color.FromArgb(0, 150, 100), FlatStyle = FlatStyle.Flat };
            var btnCancel = new Button { Text = "Cancelar", Left = 285, Top = 225, Width = 95, Height = 34, DialogResult = DialogResult.Cancel, BackColor = Color.FromArgb(60, 60, 70), FlatStyle = FlatStyle.Flat };

            btnOk.Click += (s, e) => {
                CanvasWidth = (int)numWidth.Value;
                CanvasHeight = (int)numHeight.Value;
                ClearCanvas = chkClear.Checked;
            };

            Controls.Add(lblPreset);
            Controls.Add(cboPresets);
            Controls.Add(lblWidth);
            Controls.Add(numWidth);
            Controls.Add(lblHeight);
            Controls.Add(numHeight);
            Controls.Add(chkClear);
            Controls.Add(btnOk);
            Controls.Add(btnCancel);

            AcceptButton = btnOk;
            CancelButton = btnCancel;
        }

        private void OnPresetChanged(object sender, EventArgs e)
        {
            switch (cboPresets.SelectedIndex)
            {
                case 1: numWidth.Value = 2480; numHeight.Value = 3508; break;
                case 2: numWidth.Value = 4960; numHeight.Value = 7016; break;
                case 3: numWidth.Value = 3840; numHeight.Value = 2160; break;
                case 4: numWidth.Value = 1920; numHeight.Value = 1080; break;
                case 5: numWidth.Value = 2048; numHeight.Value = 2048; break;
                case 6: numWidth.Value = 3000; numHeight.Value = 2000; break;
            }
        }
    }
}
