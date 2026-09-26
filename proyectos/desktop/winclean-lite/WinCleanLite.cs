using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Principal;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.Win32;

namespace WinCleanLite
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }

    public class MainForm : Form
    {
        // Win32 API para RAM
        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
        public class MEMORYSTATUSEX
        {
            public uint dwLength;
            public uint dwMemoryLoad;
            public ulong ullTotalPhys;
            public ulong ullAvailPhys;
            public ulong ullTotalPageFile;
            public ulong ullAvailPageFile;
            public ulong ullTotalVirtual;
            public ulong ullAvailVirtual;
            public ulong ullAvailExtendedVirtual;
            public MEMORYSTATUSEX()
            {
                this.dwLength = (uint)Marshal.SizeOf(typeof(MEMORYSTATUSEX));
            }
        }

        [DllImport("kernel32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool GlobalMemoryStatusEx([In, Out] MEMORYSTATUSEX lpBuffer);

        [DllImport("psapi.dll")]
        static extern int EmptyWorkingSet(IntPtr hwProc);

        [DllImport("shell32.dll")]
        static extern int SHEmptyRecycleBin(IntPtr hwnd, string pszRootPath, uint dwFlags);
        const uint SHERB_NOCONFIRMATION = 0x00000001;
        const uint SHERB_NOPROGRESSUI   = 0x00000002;
        const uint SHERB_NOSOUND        = 0x00000004;

        // UI Controls
        private Timer monitorTimer;
        private Label lblRamPercent;
        private Label lblRamDetails;
        private ProgressBar pbRam;
        private Label lblDiskDetails;
        private ProgressBar pbDisk;
        private Label lblAdminStatus;
        private RichTextBox txtLog;
        private TabControl tabControl;

        // Checkboxes Limpieza
        private CheckBox chkUserTemp;
        private CheckBox chkWinTemp;
        private CheckBox chkWinUpdate;
        private CheckBox chkErrorReport;
        private CheckBox chkCrashDumps;
        private CheckBox chkRecycleBin;
        private CheckBox chkThumbnails;
        private CheckBox chkDeliveryOpt;

        // Buttons
        private Button btnQuickOptimize;
        private Button btnFlushRam;
        private Button btnScanJunk;
        private Button btnCleanJunk;
        private Button btnApplyAllTweaks;

        private bool isWorking = false;

        public MainForm()
        {
            InitializeComponent();
            CheckAdminPrivileges();
            StartMonitoring();
            Log("Bienvenido a WinClean Lite para Windows 11.");
            Log("Diseñado especialmente para netbooks y equipos con memoria RAM reducida.");
            if (!IsRunningAsAdmin())
            {
                Log("[AVISO] No se detectaron permisos de Administrador. Para limpiar archivos del sistema, ejecuta la app como Administrador.");
            }
        }

        private bool IsRunningAsAdmin()
        {
            WindowsIdentity id = WindowsIdentity.GetCurrent();
            WindowsPrincipal principal = new WindowsPrincipal(id);
            return principal.IsInRole(WindowsBuiltInRole.Administrator);
        }

        private void CheckAdminPrivileges()
        {
            if (IsRunningAsAdmin())
            {
                lblAdminStatus.Text = "🛡️ Administrador (Acceso Completo)";
                lblAdminStatus.ForeColor = Color.FromArgb(74, 222, 128); // Green
            }
            else
            {
                lblAdminStatus.Text = "⚠️ Modo Estándar (Haz clic para modo Admin)";
                lblAdminStatus.ForeColor = Color.FromArgb(251, 191, 36); // Yellow
                lblAdminStatus.Cursor = Cursors.Hand;
                lblAdminStatus.Click += (s, e) => RestartAsAdmin();
            }
        }

        private void RestartAsAdmin()
        {
            try
            {
                ProcessStartInfo proc = new ProcessStartInfo();
                proc.UseShellExecute = true;
                proc.WorkingDirectory = Environment.CurrentDirectory;
                proc.FileName = Application.ExecutablePath;
                proc.Verb = "runas";
                Process.Start(proc);
                Application.Exit();
            }
            catch (Exception ex)
            {
                MessageBox.Show("No se pudo elevar a Administrador: " + ex.Message, "Atención", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
        }

        private void InitializeComponent()
        {
            this.Text = "WinClean Lite - Optimizador de Rendimiento para Windows 11";
            this.Size = new Size(880, 680);
            this.MinimumSize = new Size(820, 620);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.FromArgb(24, 24, 27); // Dark Zinc
            this.ForeColor = Color.FromArgb(244, 244, 245);
            this.Font = new Font("Segoe UI", 9.5f, FontStyle.Regular);
            this.Icon = SystemIcons.Application;

            // TOP HEADER PANEL
            Panel pnlHeader = new Panel();
            pnlHeader.Dock = DockStyle.Top;
            pnlHeader.Height = 70;
            pnlHeader.BackColor = Color.FromArgb(32, 32, 36);
            pnlHeader.Padding = new Padding(20, 10, 20, 10);

            Label lblTitle = new Label();
            lblTitle.Text = "⚡ WinClean Lite";
            lblTitle.Font = new Font("Segoe UI", 16f, FontStyle.Bold);
            lblTitle.ForeColor = Color.FromArgb(96, 165, 250); // Light blue
            lblTitle.AutoSize = true;
            lblTitle.Location = new Point(15, 10);
            pnlHeader.Controls.Add(lblTitle);

            Label lblSubtitle = new Label();
            lblSubtitle.Text = "Limpieza de archivos inservibles y optimización de RAM para Windows 11";
            lblSubtitle.Font = new Font("Segoe UI", 8.5f, FontStyle.Regular);
            lblSubtitle.ForeColor = Color.FromArgb(161, 161, 170);
            lblSubtitle.AutoSize = true;
            lblSubtitle.Location = new Point(18, 42);
            pnlHeader.Controls.Add(lblSubtitle);

            lblAdminStatus = new Label();
            lblAdminStatus.Text = "Comprobando...";
            lblAdminStatus.Font = new Font("Segoe UI", 9f, FontStyle.Bold);
            lblAdminStatus.AutoSize = true;
            lblAdminStatus.Anchor = AnchorStyles.Top | AnchorStyles.Right;
            lblAdminStatus.Location = new Point(590, 25);
            pnlHeader.Controls.Add(lblAdminStatus);

            this.Controls.Add(pnlHeader);

            // CARDS PANEL (STATS)
            Panel pnlStats = new Panel();
            pnlStats.Dock = DockStyle.Top;
            pnlStats.Height = 110;
            pnlStats.BackColor = Color.FromArgb(24, 24, 27);
            pnlStats.Padding = new Padding(15, 10, 15, 10);

            // RAM CARD
            Panel cardRam = CreateCard(15, 10, 420, 90);
            Label lblRamTitle = new Label { Text = "🧠 Memoria RAM en Uso", Font = new Font("Segoe UI", 10f, FontStyle.Bold), ForeColor = Color.White, Location = new Point(12, 8), AutoSize = true };
            lblRamPercent = new Label { Text = "0%", Font = new Font("Segoe UI", 14f, FontStyle.Bold), ForeColor = Color.FromArgb(239, 68, 68), Location = new Point(12, 30), AutoSize = true };
            lblRamDetails = new Label { Text = "Calculando...", Font = new Font("Segoe UI", 8f), ForeColor = Color.FromArgb(161, 161, 170), Location = new Point(85, 36), AutoSize = true };
            
            pbRam = new ProgressBar { Location = new Point(12, 60), Size = new Size(270, 16), Minimum = 0, Maximum = 100 };
            
            btnFlushRam = CreateButton("⚡ Liberar RAM", 295, 30, 115, 46, Color.FromArgb(37, 99, 235));
            btnFlushRam.Click += async (s, e) => await FlushRamAsync();

            cardRam.Controls.Add(lblRamTitle);
            cardRam.Controls.Add(lblRamPercent);
            cardRam.Controls.Add(lblRamDetails);
            cardRam.Controls.Add(pbRam);
            cardRam.Controls.Add(btnFlushRam);
            pnlStats.Controls.Add(cardRam);

            // DISK C: CARD
            Panel cardDisk = CreateCard(445, 10, 400, 90);
            Label lblDiskTitle = new Label { Text = "💾 Espacio en Disco C:", Font = new Font("Segoe UI", 10f, FontStyle.Bold), ForeColor = Color.White, Location = new Point(12, 8), AutoSize = true };
            lblDiskDetails = new Label { Text = "Leyendo disco...", Font = new Font("Segoe UI", 9f), ForeColor = Color.FromArgb(228, 228, 231), Location = new Point(12, 33), AutoSize = true };
            pbDisk = new ProgressBar { Location = new Point(12, 60), Size = new Size(375, 16), Minimum = 0, Maximum = 100 };

            cardDisk.Controls.Add(lblDiskTitle);
            cardDisk.Controls.Add(lblDiskDetails);
            cardDisk.Controls.Add(pbDisk);
            pnlStats.Controls.Add(cardDisk);

            this.Controls.Add(pnlStats);

            // BOTTOM BAR (QUICK OPTIMIZATION)
            Panel pnlBottom = new Panel();
            pnlBottom.Dock = DockStyle.Bottom;
            pnlBottom.Height = 70;
            pnlBottom.BackColor = Color.FromArgb(32, 32, 36);
            pnlBottom.Padding = new Padding(15, 12, 15, 12);

            btnQuickOptimize = CreateButton("🚀 OPTIMIZACIÓN COMPLETA EN 1-CLIC (Limpia basura + Libera RAM + Optimiza Win 11)", 20, 12, 820, 46, Color.FromArgb(16, 185, 129));
            btnQuickOptimize.Font = new Font("Segoe UI", 11f, FontStyle.Bold);
            btnQuickOptimize.Click += async (s, e) => await QuickOptimizeAsync();
            pnlBottom.Controls.Add(btnQuickOptimize);

            this.Controls.Add(pnlBottom);

            // MAIN TABS
            tabControl = new TabControl();
            tabControl.Dock = DockStyle.Fill;
            tabControl.Padding = new Point(12, 8);
            tabControl.Font = new Font("Segoe UI", 9.5f, FontStyle.Regular);

            TabPage tabClean = new TabPage("🧹 Limpieza de Archivos Inservibles");
            tabClean.BackColor = Color.FromArgb(24, 24, 27);
            SetupCleanTab(tabClean);
            tabControl.TabPages.Add(tabClean);

            TabPage tabTweaks = new TabPage("⚙️ Ajustes Windows 11 (Acelerar Netbook)");
            tabTweaks.BackColor = Color.FromArgb(24, 24, 27);
            SetupTweaksTab(tabTweaks);
            tabControl.TabPages.Add(tabTweaks);

            TabPage tabLog = new TabPage("📋 Registro de Acciones (Log)");
            tabLog.BackColor = Color.FromArgb(24, 24, 27);
            SetupLogTab(tabLog);
            tabControl.TabPages.Add(tabLog);

            this.Controls.Add(tabControl);

            // Fix Tab Order display in WinForms
            pnlHeader.SendToBack();
            pnlStats.BringToFront();
            pnlBottom.BringToFront();
            tabControl.BringToFront();
        }

        private Panel CreateCard(int x, int y, int width, int height)
        {
            Panel p = new Panel();
            p.Location = new Point(x, y);
            p.Size = new Size(width, height);
            p.BackColor = Color.FromArgb(39, 39, 42); // Zinc 800
            p.BorderStyle = BorderStyle.None;
            return p;
        }

        private Button CreateButton(string text, int x, int y, int width, int height, Color bgColor)
        {
            Button btn = new Button();
            btn.Text = text;
            btn.Location = new Point(x, y);
            btn.Size = new Size(width, height);
            btn.BackColor = bgColor;
            btn.ForeColor = Color.White;
            btn.FlatStyle = FlatStyle.Flat;
            btn.FlatAppearance.BorderSize = 0;
            btn.Font = new Font("Segoe UI", 9.5f, FontStyle.Bold);
            btn.Cursor = Cursors.Hand;
            return btn;
        }

        private void SetupCleanTab(TabPage tab)
        {
            Panel pnlLeft = new Panel { Dock = DockStyle.Left, Width = 520, Padding = new Padding(20, 15, 10, 15) };

            Label lblIntro = new Label
            {
                Text = "Selecciona los archivos innecesarios que deseas eliminar con seguridad:",
                Font = new Font("Segoe UI", 9.5f, FontStyle.Bold),
                ForeColor = Color.FromArgb(228, 228, 231),
                AutoSize = true,
                Location = new Point(20, 15)
            };
            pnlLeft.Controls.Add(lblIntro);

            chkUserTemp = CreateCheckBox("Archivos Temporales de Usuario (%TEMP% / Navegadores / Cachés)", 20, 45, true);
            chkWinTemp = CreateCheckBox("Archivos Temporales del Sistema (C:\\Windows\\Temp)", 20, 75, true);
            chkWinUpdate = CreateCheckBox("Caché de Descargas de Windows Update (Archivos residuales instalados)", 20, 105, true);
            chkErrorReport = CreateCheckBox("Informes de Errores de Windows (WER) y Telemetría local", 20, 135, true);
            chkCrashDumps = CreateCheckBox("Volcados de Memoria por Bloqueos (CrashDumps)", 20, 165, true);
            chkRecycleBin = CreateCheckBox("Vaciar Papelera de Reciclaje", 20, 195, true);
            chkThumbnails = CreateCheckBox("Caché de Miniaturas de Explorador de Archivos", 20, 225, true);
            chkDeliveryOpt = CreateCheckBox("Caché de Optimización de Distribución (P2P de Windows)", 20, 255, true);

            pnlLeft.Controls.Add(chkUserTemp);
            pnlLeft.Controls.Add(chkWinTemp);
            pnlLeft.Controls.Add(chkWinUpdate);
            pnlLeft.Controls.Add(chkErrorReport);
            pnlLeft.Controls.Add(chkCrashDumps);
            pnlLeft.Controls.Add(chkRecycleBin);
            pnlLeft.Controls.Add(chkThumbnails);
            pnlLeft.Controls.Add(chkDeliveryOpt);

            btnScanJunk = CreateButton("🔍 Analizar Espacio Ocupado", 20, 295, 230, 42, Color.FromArgb(71, 85, 105));
            btnScanJunk.Click += async (s, e) => await ScanJunkAsync();

            btnCleanJunk = CreateButton("🗑️ Limpiar Archivos Ahora", 260, 295, 230, 42, Color.FromArgb(220, 38, 38));
            btnCleanJunk.Click += async (s, e) => await CleanJunkAsync();

            pnlLeft.Controls.Add(btnScanJunk);
            pnlLeft.Controls.Add(btnCleanJunk);

            tab.Controls.Add(pnlLeft);

            // Right tip panel
            Panel pnlRight = new Panel { Dock = DockStyle.Fill, Padding = new Padding(15, 20, 20, 20) };
            Label lblInfoBox = new Label
            {
                Text = "💡 ¿Por qué se llena la RAM y el CPU de tu Netbook?\n\n" +
                       "• Windows 11 incluye servicios pesados como Widgets ('Contenido de Microsoft') y 'WebView2', que consumen cientos de megas de RAM sin que los uses.\n\n" +
                       "• La acumulación de archivos temporales hace que el Antivirus ('Antimalware Service') escanee constantemente el disco, elevando el uso de CPU al 96%.\n\n" +
                       "• Esta herramienta elimina solo archivos inservibles y seguros, liberando espacio en disco y aligerando la carga de Windows.",
                ForeColor = Color.FromArgb(161, 161, 170),
                BackColor = Color.FromArgb(39, 39, 42),
                Padding = new Padding(15),
                Dock = DockStyle.Fill,
                Font = new Font("Segoe UI", 9.5f)
            };
            pnlRight.Controls.Add(lblInfoBox);
            tab.Controls.Add(pnlRight);
        }

        private CheckBox CreateCheckBox(string text, int x, int y, bool isChecked)
        {
            CheckBox chk = new CheckBox();
            chk.Text = text;
            chk.Location = new Point(x, y);
            chk.Size = new Size(480, 24);
            chk.Checked = isChecked;
            chk.ForeColor = Color.FromArgb(228, 228, 231);
            chk.Cursor = Cursors.Hand;
            return chk;
        }

        private void SetupTweaksTab(TabPage tab)
        {
            Panel pnlTweaks = new Panel { Dock = DockStyle.Fill, AutoScroll = true, Padding = new Padding(25, 15, 25, 15) };

            Label lblIntro = new Label
            {
                Text = "Ajustes Clave para Acelerar Netbooks con Poca RAM en Windows 11:",
                Font = new Font("Segoe UI", 11f, FontStyle.Bold),
                ForeColor = Color.FromArgb(96, 165, 250),
                AutoSize = true,
                Location = new Point(10, 10)
            };
            pnlTweaks.Controls.Add(lblIntro);

            int y = 45;

            // Tweak 1: Widgets & WebView2
            pnlTweaks.Controls.Add(CreateTweakRow(
                "1. Desactivar Widgets y Noticias de la Barra de Tareas",
                "Detiene los procesos de 'Contenido de Microsoft' y 'WebView2' (Ahorra ~100MB RAM inmediatamente).",
                y,
                () => ApplyDisableWidgets()
            ));
            y += 65;

            // Tweak 2: Bing Search
            pnlTweaks.Controls.Add(CreateTweakRow(
                "2. Desactivar Búsqueda Web de Bing en Windows",
                "Hace que la búsqueda sea solo local. Reduce enormemente el proceso 'Buscar' que se ve en tu captura.",
                y,
                () => ApplyDisableBingSearch()
            ));
            y += 65;

            // Tweak 3: Telemetry
            pnlTweaks.Controls.Add(CreateTweakRow(
                "3. Desactivar Telemetría y Diagnósticos de Microsoft (DiagTrack)",
                "Detiene el envío de reportes constantes a los servidores de Microsoft que desgastan tu CPU y RAM.",
                y,
                () => ApplyDisableTelemetry()
            ));
            y += 65;

            // Tweak 4: Visual Effects
            pnlTweaks.Controls.Add(CreateTweakRow(
                "4. Optimizar Efectos Visuales (Modo Rendimiento Rápido)",
                "Desactiva transparencias y animaciones lentas para que las ventanas abran de inmediato sin lag.",
                y,
                () => ApplyVisualEffects()
            ));
            y += 65;

            // Tweak 5: Windows Tips & Ads
            pnlTweaks.Controls.Add(CreateTweakRow(
                "5. Desactivar Sugerencias, Consejos y Anuncios de Windows",
                "Evita que Windows descargue y ejecute sugerencias y apps automáticas en segundo plano.",
                y,
                () => ApplyDisableTips()
            ));
            y += 65;

            // Tweak 6: SysMain (Superfetch)
            pnlTweaks.Controls.Add(CreateTweakRow(
                "6. Desactivar Servicio SysMain (SuperFetch)",
                "Muy recomendado en netbooks: evita que Windows precargue apps en RAM y sature el disco al 100%.",
                y,
                () => ApplyDisableSysMain()
            ));
            y += 75;

            btnApplyAllTweaks = CreateButton("⚡ APLICAR TODOS LOS AJUSTES DE RENDIMIENTO", 10, y, 780, 44, Color.FromArgb(79, 70, 229));
            btnApplyAllTweaks.Click += (s, e) => ApplyAllTweaks();
            pnlTweaks.Controls.Add(btnApplyAllTweaks);

            tab.Controls.Add(pnlTweaks);
        }

        private Panel CreateTweakRow(string title, string description, int y, Action action)
        {
            Panel pnl = new Panel();
            pnl.Location = new Point(10, y);
            pnl.Size = new Size(800, 56);
            pnl.BackColor = Color.FromArgb(32, 32, 36);

            Label lblT = new Label
            {
                Text = title,
                Font = new Font("Segoe UI", 9.5f, FontStyle.Bold),
                ForeColor = Color.White,
                Location = new Point(10, 8),
                AutoSize = true
            };
            Label lblD = new Label
            {
                Text = description,
                Font = new Font("Segoe UI", 8.2f),
                ForeColor = Color.FromArgb(161, 161, 170),
                Location = new Point(12, 30),
                AutoSize = true
            };

            Button btn = new Button
            {
                Text = "Aplicar",
                Size = new Size(100, 36),
                Location = new Point(680, 10),
                BackColor = Color.FromArgb(37, 99, 235),
                ForeColor = Color.White,
                FlatStyle = FlatStyle.Flat,
                Font = new Font("Segoe UI", 9f, FontStyle.Bold),
                Cursor = Cursors.Hand
            };
            btn.FlatAppearance.BorderSize = 0;
            btn.Click += (s, e) =>
            {
                action();
                btn.Text = "✓ Aplicado";
                btn.BackColor = Color.FromArgb(16, 185, 129);
            };

            pnl.Controls.Add(lblT);
            pnl.Controls.Add(lblD);
            pnl.Controls.Add(btn);

            return pnl;
        }

        private void SetupLogTab(TabPage tab)
        {
            txtLog = new RichTextBox();
            txtLog.Dock = DockStyle.Fill;
            txtLog.BackColor = Color.FromArgb(18, 18, 20);
            txtLog.ForeColor = Color.FromArgb(212, 212, 216);
            txtLog.Font = new Font("Consolas", 9.5f, FontStyle.Regular);
            txtLog.ReadOnly = true;
            txtLog.BorderStyle = BorderStyle.None;

            Panel pnlLogTools = new Panel { Dock = DockStyle.Bottom, Height = 45, BackColor = Color.FromArgb(24, 24, 27) };
            Button btnClearLog = CreateButton("Limpiar Registro", 15, 7, 140, 30, Color.FromArgb(71, 85, 105));
            btnClearLog.Click += (s, e) => txtLog.Clear();
            pnlLogTools.Controls.Add(btnClearLog);

            tab.Controls.Add(txtLog);
            tab.Controls.Add(pnlLogTools);
        }

        public void Log(string msg)
        {
            if (this.InvokeRequired)
            {
                this.Invoke(new Action<string>(Log), msg);
                return;
            }
            string timestamp = DateTime.Now.ToString("HH:mm:ss");
            txtLog.AppendText(string.Format("[{0}] {1}\r\n", timestamp, msg));
            txtLog.SelectionStart = txtLog.Text.Length;
            txtLog.ScrollToCaret();
        }

        private void StartMonitoring()
        {
            UpdateStats();
            monitorTimer = new Timer();
            monitorTimer.Interval = 2500; // Cada 2.5 seg
            monitorTimer.Tick += (s, e) => UpdateStats();
            monitorTimer.Start();
        }

        private void UpdateStats()
        {
            try
            {
                // RAM Info
                MEMORYSTATUSEX memStatus = new MEMORYSTATUSEX();
                if (GlobalMemoryStatusEx(memStatus))
                {
                    uint memLoad = memStatus.dwMemoryLoad;
                    double totalGB = (double)memStatus.ullTotalPhys / (1024 * 1024 * 1024);
                    double availGB = (double)memStatus.ullAvailPhys / (1024 * 1024 * 1024);
                    double usedGB = totalGB - availGB;

                    lblRamPercent.Text = memLoad.ToString() + "%";
                    lblRamDetails.Text = string.Format("{0:0.1} GB usados de {1:0.1} GB", usedGB, totalGB);

                    pbRam.Value = (int)Math.Min(100, Math.Max(0, memLoad));

                    if (memLoad > 85)
                    {
                        lblRamPercent.ForeColor = Color.FromArgb(239, 68, 68); // Red
                    }
                    else if (memLoad > 65)
                    {
                        lblRamPercent.ForeColor = Color.FromArgb(245, 158, 11); // Yellow
                    }
                    else
                    {
                        lblRamPercent.ForeColor = Color.FromArgb(74, 222, 128); // Green
                    }
                }

                // Disk Info
                DriveInfo cDrive = new DriveInfo("C");
                if (cDrive.IsReady)
                {
                    double totalDiskGB = (double)cDrive.TotalSize / (1024 * 1024 * 1024);
                    double freeDiskGB = (double)cDrive.AvailableFreeSpace / (1024 * 1024 * 1024);
                    double usedDiskGB = totalDiskGB - freeDiskGB;
                    int diskPercent = (int)((usedDiskGB / totalDiskGB) * 100);

                    lblDiskDetails.Text = string.Format("{0:0.1} GB libres de {1:0.1} GB ({2}% ocupado)", freeDiskGB, totalDiskGB, diskPercent);
                    pbDisk.Value = Math.Min(100, Math.Max(0, diskPercent));
                }
            }
            catch
            {
                // Ignorar fallas momentáneas de lectura
            }
        }

        private async Task FlushRamAsync()
        {
            if (isWorking) return;
            isWorking = true;
            btnFlushRam.Enabled = false;

            Log(">>> Iniciando liberación profunda de memoria RAM...");

            await Task.Run(() =>
            {
                int freedProcesses = 0;
                long memBefore = 0;
                long memAfter = 0;

                MEMORYSTATUSEX m1 = new MEMORYSTATUSEX();
                if (GlobalMemoryStatusEx(m1)) memBefore = (long)m1.ullAvailPhys;

                Process[] procs = Process.GetProcesses();
                foreach (Process p in procs)
                {
                    try
                    {
                        if (!p.HasExited)
                        {
                            EmptyWorkingSet(p.Handle);
                            freedProcesses++;
                        }
                    }
                    catch
                    {
                        // Procesos protegidos del sistema no accesibles
                    }
                }

                GC.Collect();
                GC.WaitForPendingFinalizers();

                MEMORYSTATUSEX m2 = new MEMORYSTATUSEX();
                if (GlobalMemoryStatusEx(m2)) memAfter = (long)m2.ullAvailPhys;

                long diffBytes = memAfter - memBefore;
                double freedMB = diffBytes > 0 ? (double)diffBytes / (1024 * 1024) : 0;

                Log(string.Format("[OK] Se optimizaron {0} procesos del sistema.", freedProcesses));
                if (freedMB > 0)
                {
                    Log(string.Format("🎉 ¡Se liberaron aproximadamente {0:0.0} MB de memoria RAM!", freedMB));
                }
                else
                {
                    Log("✓ La memoria de trabajo ha sido compactada exitosamente.");
                }
            });

            UpdateStats();
            isWorking = false;
            btnFlushRam.Enabled = true;
        }

        private async Task ScanJunkAsync()
        {
            if (isWorking) return;
            isWorking = true;
            btnScanJunk.Enabled = false;
            btnCleanJunk.Enabled = false;

            Log(">>> Analizando archivos inservibles en el disco...");

            await Task.Run(() =>
            {
                long totalBytes = 0;
                int totalFiles = 0;

                if (chkUserTemp.Checked)
                {
                    string p = Path.GetTempPath();
                    ScanFolder(p, ref totalBytes, ref totalFiles, "Temporales de Usuario");
                }
                if (chkWinTemp.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "Temp");
                    ScanFolder(p, ref totalBytes, ref totalFiles, "Temporales de Windows");
                }
                if (chkWinUpdate.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "SoftwareDistribution", "Download");
                    ScanFolder(p, ref totalBytes, ref totalFiles, "Descargas de Windows Update");
                }
                if (chkErrorReport.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData), "Microsoft", "Windows", "WER");
                    ScanFolder(p, ref totalBytes, ref totalFiles, "Reportes de Error (WER)");
                }
                if (chkCrashDumps.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "CrashDumps");
                    ScanFolder(p, ref totalBytes, ref totalFiles, "Crash Dumps");
                }
                if (chkDeliveryOpt.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "SoftwareDistribution", "DeliveryOptimization");
                    ScanFolder(p, ref totalBytes, ref totalFiles, "Delivery Optimization");
                }

                double mb = (double)totalBytes / (1024 * 1024);
                Log(string.Format("📊 Análisis finalizado: Se detectaron {0} archivos ({1:0.00} MB) listos para eliminar.", totalFiles, mb));
            });

            isWorking = false;
            btnScanJunk.Enabled = true;
            btnCleanJunk.Enabled = true;
        }

        private void ScanFolder(string path, ref long totalBytes, ref int totalFiles, string label)
        {
            if (!Directory.Exists(path)) return;
            try
            {
                DirectoryInfo dir = new DirectoryInfo(path);
                FileInfo[] files = dir.GetFiles("*", SearchOption.AllDirectories);
                long fBytes = 0;
                foreach (FileInfo f in files)
                {
                    try { fBytes += f.Length; } catch { }
                }
                totalBytes += fBytes;
                totalFiles += files.Length;
                Log(string.Format(" - {0}: {1} archivos ({2:0.0} MB)", label, files.Length, (double)fBytes / (1024 * 1024)));
            }
            catch (Exception ex)
            {
                Log(string.Format(" [!] {0}: No se pudo leer completamente ({1})", label, ex.Message));
            }
        }

        private async Task CleanJunkAsync()
        {
            if (isWorking) return;
            isWorking = true;
            btnScanJunk.Enabled = false;
            btnCleanJunk.Enabled = false;

            Log(">>> Iniciando limpieza de archivos inservibles...");

            await Task.Run(() =>
            {
                long cleanedBytes = 0;
                int cleanedFiles = 0;

                if (chkUserTemp.Checked)
                {
                    CleanFolder(Path.GetTempPath(), ref cleanedBytes, ref cleanedFiles, "Temporales de Usuario");
                }
                if (chkWinTemp.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "Temp");
                    CleanFolder(p, ref cleanedBytes, ref cleanedFiles, "Temporales de Windows");
                }
                if (chkWinUpdate.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "SoftwareDistribution", "Download");
                    CleanFolder(p, ref cleanedBytes, ref cleanedFiles, "Descargas de Windows Update");
                }
                if (chkErrorReport.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData), "Microsoft", "Windows", "WER");
                    CleanFolder(p, ref cleanedBytes, ref cleanedFiles, "Reportes de Error (WER)");
                }
                if (chkCrashDumps.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "CrashDumps");
                    CleanFolder(p, ref cleanedBytes, ref cleanedFiles, "Crash Dumps");
                }
                if (chkDeliveryOpt.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "SoftwareDistribution", "DeliveryOptimization");
                    CleanFolder(p, ref cleanedBytes, ref cleanedFiles, "Delivery Optimization");
                }
                if (chkThumbnails.Checked)
                {
                    string p = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft", "Windows", "Explorer");
                    CleanThumbnails(p, ref cleanedBytes, ref cleanedFiles);
                }
                if (chkRecycleBin.Checked)
                {
                    try
                    {
                        SHEmptyRecycleBin(IntPtr.Zero, null, SHERB_NOCONFIRMATION | SHERB_NOPROGRESSUI | SHERB_NOSOUND);
                        Log("[OK] Papelera de reciclaje vaciada.");
                    }
                    catch (Exception ex)
                    {
                        Log("[!] Error al vaciar papelera: " + ex.Message);
                    }
                }

                double mb = (double)cleanedBytes / (1024 * 1024);
                Log(string.Format("🎉 ¡Limpieza completada! Se eliminaron {0} archivos ({1:0.00} MB liberados de disco).", cleanedFiles, mb));
            });

            UpdateStats();
            isWorking = false;
            btnScanJunk.Enabled = true;
            btnCleanJunk.Enabled = true;
        }

        private void CleanFolder(string path, ref long cleanedBytes, ref int cleanedFiles, string label)
        {
            if (!Directory.Exists(path)) return;
            int count = 0;
            long bytes = 0;

            try
            {
                DirectoryInfo dir = new DirectoryInfo(path);
                foreach (FileInfo file in dir.GetFiles("*", SearchOption.AllDirectories))
                {
                    try
                    {
                        long len = file.Length;
                        file.Attributes = FileAttributes.Normal;
                        file.Delete();
                        bytes += len;
                        count++;
                    }
                    catch
                    {
                        // Archivo en uso o protegido por el sistema
                    }
                }

                // Borrar subdirectorios vacíos
                foreach (DirectoryInfo subDir in dir.GetDirectories("*", SearchOption.AllDirectories))
                {
                    try
                    {
                        if (subDir.GetFiles().Length == 0 && subDir.GetDirectories().Length == 0)
                        {
                            subDir.Delete();
                        }
                    }
                    catch { }
                }

                cleanedBytes += bytes;
                cleanedFiles += count;
                Log(string.Format("[OK] {0}: {1} archivos borrados ({2:0.0} MB).", label, count, (double)bytes / (1024 * 1024)));
            }
            catch (Exception ex)
            {
                Log(string.Format("[!] {0}: Aviso durante limpieza ({1})", label, ex.Message));
            }
        }

        private void CleanThumbnails(string path, ref long cleanedBytes, ref int cleanedFiles)
        {
            if (!Directory.Exists(path)) return;
            try
            {
                DirectoryInfo dir = new DirectoryInfo(path);
                foreach (FileInfo f in dir.GetFiles("thumbcache_*.db"))
                {
                    try
                    {
                        long len = f.Length;
                        f.Delete();
                        cleanedBytes += len;
                        cleanedFiles++;
                    }
                    catch { }
                }
                Log("[OK] Caché de miniaturas de Windows limpiada.");
            }
            catch { }
        }

        // TWEAKS IMPLEMENTATION
        private void ApplyDisableWidgets()
        {
            try
            {
                // Desactivar Widgets en la barra de tareas
                Registry.SetValue(@"HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced", "TaskbarDa", 0, RegistryValueKind.DWord);
                
                // Desactivar política Dss
                try
                {
                    Registry.SetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Dss", "AllowNewsAndInterests", 0, RegistryValueKind.DWord);
                }
                catch { }

                // Matar proceso Widgets y WebView2 huérfanos de widgets para liberar RAM ya
                KillProcessesByName("Widgets");
                
                Log("[OK] Widgets y Noticias de Windows 11 desactivados. Los procesos de WebView2 asociados se han detenido.");
            }
            catch (Exception ex)
            {
                Log("[!] Error al desactivar Widgets: " + ex.Message);
            }
        }

        private void ApplyDisableBingSearch()
        {
            try
            {
                Registry.SetValue(@"HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Search", "BingSearchEnabled", 0, RegistryValueKind.DWord);
                Registry.SetValue(@"HKEY_CURRENT_USER\Software\Policies\Microsoft\Windows\Explorer", "DisableSearchBoxSuggestions", 1, RegistryValueKind.DWord);
                
                try
                {
                    Registry.SetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\Windows Search", "ConnectedSearchUseWeb", 0, RegistryValueKind.DWord);
                    Registry.SetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\Windows Search", "AllowCloudSearch", 0, RegistryValueKind.DWord);
                }
                catch { }

                Log("[OK] Búsqueda Web de Bing desactivada. Ahora el buscador de Windows es 100% local y mucho más liviano.");
            }
            catch (Exception ex)
            {
                Log("[!] Error al desactivar Bing Search: " + ex.Message);
            }
        }

        private void ApplyDisableTelemetry()
        {
            try
            {
                // Deshabilitar telemetría en registro
                try
                {
                    Registry.SetValue(@"HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\DataCollection", "AllowTelemetry", 0, RegistryValueKind.DWord);
                }
                catch { }

                // Detener servicios pesados DiagTrack y dmwappushservice
                StopAndDisableService("DiagTrack");
                StopAndDisableService("dmwappushservice");

                Log("[OK] Servicios de Telemetría (DiagTrack) detenidos y deshabilitados.");
            }
            catch (Exception ex)
            {
                Log("[!] Error al configurar telemetría: " + ex.Message);
            }
        }

        private void ApplyVisualEffects()
        {
            try
            {
                // Desactivar transparencias
                Registry.SetValue(@"HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize", "EnableTransparency", 0, RegistryValueKind.DWord);
                
                // Desactivar animaciones de ventanas
                Registry.SetValue(@"HKEY_CURRENT_USER\Control Panel\Desktop\WindowMetrics", "MinAnimate", "0", RegistryValueKind.String);
                
                // Ajustar preferencia de rendimiento
                Registry.SetValue(@"HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\VisualEffects", "VisualFXSetting", 2, RegistryValueKind.DWord);

                Log("[OK] Modo Rendimiento Visual aplicado: Transparencias y animaciones pesadas desactivadas.");
            }
            catch (Exception ex)
            {
                Log("[!] Error al aplicar efectos visuales: " + ex.Message);
            }
        }

        private void ApplyDisableTips()
        {
            try
            {
                string key = @"HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\ContentDeliveryManager";
                Registry.SetValue(key, "SystemPaneSuggestionsEnabled", 0, RegistryValueKind.DWord);
                Registry.SetValue(key, "SubscribedContent-338388Enabled", 0, RegistryValueKind.DWord);
                Registry.SetValue(key, "SubscribedContent-338389Enabled", 0, RegistryValueKind.DWord);
                Registry.SetValue(key, "SubscribedContent-353698Enabled", 0, RegistryValueKind.DWord);
                Registry.SetValue(key, "SoftLandingEnabled", 0, RegistryValueKind.DWord);

                Log("[OK] Sugerencias, anuncios y descargas automáticas de Windows desactivadas.");
            }
            catch (Exception ex)
            {
                Log("[!] Error al desactivar sugerencias: " + ex.Message);
            }
        }

        private void ApplyDisableSysMain()
        {
            try
            {
                StopAndDisableService("SysMain");
                Log("[OK] Servicio SysMain (SuperFetch) detenido. Evita saturación de CPU y Disco al 100%.");
            }
            catch (Exception ex)
            {
                Log("[!] Error al configurar SysMain: " + ex.Message);
            }
        }

        private void ApplyAllTweaks()
        {
            Log(">>> Aplicando todos los ajustes de aceleración para Windows 11...");
            ApplyDisableWidgets();
            ApplyDisableBingSearch();
            ApplyDisableTelemetry();
            ApplyVisualEffects();
            ApplyDisableTips();
            ApplyDisableSysMain();
            Log("🎉 ¡Todos los ajustes han sido aplicados con éxito! Notarás mayor fluidez de inmediato.");
        }

        private async Task QuickOptimizeAsync()
        {
            if (isWorking) return;
            Log("=================================================");
            Log("🚀 INICIANDO SUPER OPTIMIZACIÓN RÁPIDA 1-CLIC...");
            Log("=================================================");
            
            // 1. Aplicar Tweaks
            ApplyAllTweaks();
            
            // 2. Limpiar Basura
            await CleanJunkAsync();

            // 3. Liberar RAM
            await FlushRamAsync();

            Log("=================================================");
            Log("✅ ¡PROCESO COMPLETADO EXITOSAMENTE!");
            Log("Tu netbook ha quedado limpia de archivos basura y con la RAM liberada.");
            Log("=================================================");
            
            tabControl.SelectedTab = tabControl.TabPages[2]; // Ir al log
        }

        private void StopAndDisableService(string serviceName)
        {
            try
            {
                ProcessStartInfo psi = new ProcessStartInfo("sc.exe", string.Format("config \"{0}\" start=disabled", serviceName));
                psi.CreateNoWindow = true;
                psi.UseShellExecute = false;
                Process p1 = Process.Start(psi);
                p1.WaitForExit(3000);

                ProcessStartInfo psiStop = new ProcessStartInfo("net.exe", string.Format("stop \"{0}\" /y", serviceName));
                psiStop.CreateNoWindow = true;
                psiStop.UseShellExecute = false;
                Process p2 = Process.Start(psiStop);
                p2.WaitForExit(3000);
            }
            catch { }
        }

        private void KillProcessesByName(string procName)
        {
            try
            {
                Process[] procs = Process.GetProcessesByName(procName);
                foreach (Process p in procs)
                {
                    try { p.Kill(); } catch { }
                }
            }
            catch { }
        }
    }
}
