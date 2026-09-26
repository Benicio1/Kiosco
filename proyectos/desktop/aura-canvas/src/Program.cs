using System;
using System.IO;
using System.Windows.Forms;
using AuraPaint.UI;

namespace AuraPaint
{
    public static class Program
    {
        [STAThread]
        public static void Main()
        {
            try
            {
                Application.EnableVisualStyles();
                Application.SetCompatibleTextRenderingDefault(false);
                Application.Run(new CanvasForm());
            }
            catch (Exception ex)
            {
                File.WriteAllText("crash_log.txt", ex.ToString());
                MessageBox.Show(ex.ToString(), "Error inesperado en AuraPaint", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
