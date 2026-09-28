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

        private const uint MOUSEEVENTF_LEFTDOWN = 0x02;
        private const uint MOUSEEVENTF_LEFTUP = 0x04;

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
            else if (cmd == "mouse click")
            {
                uint x = (uint)Cursor.Position.X;
                uint y = (uint)Cursor.Position.Y;
                mouse_event(MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP, x, y, 0, 0);
            }
            else if (cmd.StartsWith("key "))
            {
                string k = cmd.Substring(4).ToLower();
                if (k == "space" || k == "play_pause" || k == "ok") SendKeys.SendWait(" ");
                else if (k == "enter") SendKeys.SendWait("{ENTER}");
                else if (k == "left") SendKeys.SendWait("{LEFT}");
                else if (k == "right") SendKeys.SendWait("{RIGHT}");
                else if (k == "up") SendKeys.SendWait("{UP}");
                else if (k == "down") SendKeys.SendWait("{DOWN}");
                else if (k == "back" || k == "esc") SendKeys.SendWait("%{LEFT}");
                else if (k == "f" || k == "fullscreen") SendKeys.SendWait("f");
            }
            else if (cmd.StartsWith("vol "))
            {
                string v = cmd.Substring(4).ToLower();
                if (v == "up") SendKeys.SendWait(((char)175).ToString());
                else if (v == "down") SendKeys.SendWait(((char)174).ToString());
                else if (v == "mute") SendKeys.SendWait(((char)173).ToString());
            }
            else if (cmd.StartsWith("type "))
            {
                string text = cmd.Substring(5);
                SendKeys.SendWait(text);
            }
            else if (cmd.StartsWith("home "))
            {
                string port = cmd.Substring(5).Trim();
                Process.Start("http://localhost:" + port);
            }
        }
    }
}
