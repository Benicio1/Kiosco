using System;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Diagnostics;

namespace SmartTvLauncher
{
    class InputBridge
    {
        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        public static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint cButtons, uint dwExtraInfo);

        [DllImport("user32.dll")]
        public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, int dwExtraInfo);

        private const uint MOUSEEVENTF_LEFTDOWN = 0x02;
        private const uint MOUSEEVENTF_LEFTUP = 0x04;
        private const uint MOUSEEVENTF_WHEEL = 0x0800;

        private const uint KEYEVENTF_EXTENDEDKEY = 0x0001;
        private const uint KEYEVENTF_KEYUP = 0x0002;

        static void Main(string[] args)
        {
            Console.WriteLine("READY");
            string line;
            while ((line = Console.ReadLine()) != null)
            {
                line = line.Trim();
                if (string.IsNullOrEmpty(line)) continue;
                if (line == "exit" || line == "quit") break;

                try
                {
                    HandleCommand(line);
                }
                catch (Exception ex)
                {
                    Console.WriteLine("ERR: " + ex.Message);
                }
            }
        }

        static void PressKey(byte vk, uint flags = 0)
        {
            keybd_event(vk, 0, flags, 0);
            System.Threading.Thread.Sleep(15);
            keybd_event(vk, 0, flags | KEYEVENTF_KEYUP, 0);
        }

        static void SendBrowserBack()
        {
            // 1. Tecla nativa multimedia Browser Back (0xA6 con ExtendedKey)
            keybd_event(0xA6, 0x6A, KEYEVENTF_EXTENDEDKEY, 0);
            System.Threading.Thread.Sleep(20);
            keybd_event(0xA6, 0x6A, KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP, 0);
            System.Threading.Thread.Sleep(20);

            // 2. Alt + Flecha Izquierda extendida (0x12 + 0x25 Extended)
            keybd_event(0x12, 0x38, 0, 0); // Alt down
            System.Threading.Thread.Sleep(20);
            keybd_event(0x25, 0x4B, KEYEVENTF_EXTENDEDKEY, 0); // Left Arrow down
            System.Threading.Thread.Sleep(20);
            keybd_event(0x25, 0x4B, KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP, 0); // Left Arrow up
            System.Threading.Thread.Sleep(20);
            keybd_event(0x12, 0x38, KEYEVENTF_KEYUP, 0); // Alt up
        }

        static void HandleCommand(string cmd)
        {
            if (cmd.StartsWith("mouse move "))
            {
                string[] parts = cmd.Substring(11).Split(' ');
                if (parts.Length >= 2)
                {
                    int dx = (int)float.Parse(parts[0]);
                    int dy = (int)float.Parse(parts[1]);
                    Point current = Cursor.Position;
                    Cursor.Position = new Point(current.X + dx, current.Y + dy);
                }
            }
            else if (cmd.StartsWith("mouse scroll "))
            {
                int amount = (int)float.Parse(cmd.Substring(13));
                mouse_event(MOUSEEVENTF_WHEEL, 0, 0, (uint)amount, 0);
            }
            else if (cmd == "mouse click")
            {
                uint x = (uint)Cursor.Position.X;
                uint y = (uint)Cursor.Position.Y;
                mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, x, y, 0, 0);
            }
            else if (cmd.StartsWith("key "))
            {
                string k = cmd.Substring(4).ToLower();
                if (k == "space" || k == "play_pause") PressKey(0x20); // VK_SPACE
                else if (k == "ok" || k == "enter") PressKey(0x0D); // VK_RETURN
                else if (k == "left") PressKey(0x25, KEYEVENTF_EXTENDEDKEY); // VK_LEFT
                else if (k == "right") PressKey(0x27, KEYEVENTF_EXTENDEDKEY); // VK_RIGHT
                else if (k == "up") PressKey(0x26, KEYEVENTF_EXTENDEDKEY); // VK_UP
                else if (k == "down") PressKey(0x28, KEYEVENTF_EXTENDEDKEY); // VK_DOWN
                else if (k == "back")
                {
                    SendBrowserBack();
                }
                else if (k == "esc") PressKey(0x1B); // VK_ESCAPE
                else if (k == "f" || k == "fullscreen") PressKey(0x46); // VK_F
            }
            else if (cmd.StartsWith("vol "))
            {
                string v = cmd.Substring(4).ToLower();
                if (v == "up") PressKey(0xAF, KEYEVENTF_EXTENDEDKEY); // VK_VOLUME_UP
                else if (v == "down") PressKey(0xAE, KEYEVENTF_EXTENDEDKEY); // VK_VOLUME_DOWN
                else if (v == "mute") PressKey(0xAD, KEYEVENTF_EXTENDEDKEY); // VK_VOLUME_MUTE
            }
            else if (cmd.StartsWith("type "))
            {
                string text = cmd.Substring(5);
                SendKeys.SendWait(text);
            }
            else if (cmd == "home" || cmd.StartsWith("home "))
            {
                // Regresar al Launcher navegando hacia atrás en la misma ventana
                for (int i = 0; i < 4; i++)
                {
                    SendBrowserBack();
                    System.Threading.Thread.Sleep(50);
                }
            }
        }
    }
}
