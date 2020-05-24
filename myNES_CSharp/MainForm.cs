//////////////////////////////////////////////////////////////////////////////
//This file is part of My Nes                                               //
//A Nintendo Entertainment System Emulator.                                 //
//                                                                          //
//Copyright © 2009 Ala Hadid (AHD)                                          //
//                                                                          //
//My Nes is free software; you can redistribute it and/or modify            //
//it under the terms of the GNU General Public License as published by      //
//the Free Software Foundation; either version 2 of the License, or         //
//(at your option) any later version.                                       //
//                                                                          //
//My Nes is distributed in the hope that it will be useful,                 //
//but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//GNU General Public License for more details.                              //
//                                                                          //
//You should have received a copy of the GNU General Public License         //
//along with this program; if not, write to the Free Software               //
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA//
//////////////////////////////////////////////////////////////////////////////
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing.Imaging;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using AHD.MyNes.Nes;
using System.Threading;
using System.Reflection;
namespace AHD.MyNes.Core
{
    /// <summary>
    /// The main form of the application
    /// </summary>
    public partial class Form_Main : Form
    {
        public NesEmulator _Nes;
        private Thread gameThread;

        /// <summary>
        /// The main form of the application
        /// </summary>
        public Form_Main()
        {
            InitializeComponent();

            //this.DoubleBuffered = true;
            PropertyInfo pinfo = panel1.GetType().GetProperty("DoubleBuffered", BindingFlags.NonPublic | BindingFlags.Instance);
            pinfo.SetValue(panel1, true,null);

            _Nes = new NesEmulator(panel1, panel1);
            // _Nes.CurrentDrawer = new Vid_GDI_16Bit();
            _Nes.TVMode = NesSystem.NTSC;

            OpenRom(Application.StartupPath + "\\rom.nes");
        }

        public void OpenRom(string FileName)
        {
            //CartHeaderReader rom = new CartHeaderReader(FileName);
            ////To know if archive file includes
            ////more than one rom

            //if (!rom.validRom)
            //{
            //    MessageBox.Show("Not a INes rom !!");
            //    _Nes.QuitEngine();
            //    return;
            //}
            //if (!rom.SupportedMapper())
            //{
            //    MessageBox.Show("Unsupported mapper # " + rom.MemoryMapper.ToString());
            //    _Nes.QuitEngine();
            //    return;
            //}
            _Nes.LoadCart(FileName);
            _Nes.SizeOfDraw = AHD.MyNes.Nes.NesEmulator.DrawSize.Stretch;
            //sound
            _Nes.SoundEnabled = true;
            _Nes.myAPU.MasterVolume = 7;
            _Nes.myAPU.DMCEnabled = true;
            _Nes.myAPU.NoiseEnabled = true;
            _Nes.myAPU.SquareWave1Enabled = true;
            _Nes.myAPU.SquareWave2Enabled = true;
            _Nes.myAPU.TriangleEnabled = true;
            _Nes.SpeedThrottling = false;

            //Launch ...
            gameThread = new Thread(new ThreadStart(_Nes.DoFrame));
            gameThread.Priority = ThreadPriority.Highest;
            gameThread.Start();
            _Nes.isPaused = false;
        }

        private void Form_Main_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (gameThread != null)
            { gameThread.Abort(); }
            _Nes.QuitEngine();
        }

        //This timer will count Fps each second
        private void timerFPSCounter_Tick(object sender, EventArgs e)
        {
            this.Text = "My NES Simulate Fps : " + _Nes.myPPU.frameCounter;
            _Nes.myPPU.frameCounter = 0;
        }

        const int WM_KEYDOWN = 0x0100;
        const int WM_KEYUP = 0x0101;
        delegate int Fun(int old, int num);

        protected override void WndProc(ref Message m)
        {
            base.WndProc(ref m);

            bool iskey = false;
            Fun fun = null;
           // Console.WriteLine(m.Msg.ToString());



            if (m.Msg == WM_KEYDOWN)
            {
                iskey = true;
                fun = (x, y) => x | y;
            }
            else if (m.Msg == WM_KEYUP)
            {
                iskey = true;
                fun = (x, y) => x ^ y;
            }

            if (iskey)
            {
                int num = _Nes.joypaddata1;
                string key = m.WParam.ToString();

                if (key == "75") //A  (健盘K)
                    num = fun(num, 0x01);
                else if (key == "74") //B  (健盘J)
                    num = fun(num, 0x02);
                else if (key == "86") //Select   (健盘V)
                    num = fun(num, 0x04);
                else if (key == "66") //Select   (健盘B)
                    num = fun(num, 0x08);
                else if (key == "87") //Up   (健盘W)
                    num = fun(num, 0x10);
                else if (key == "83") //Down   (健盘S)
                    num = fun(num, 0x20);
                else if (key == "65") //Left   (健盘A)
                    num = fun(num, 0x40);
                else if (key == "68") //Right   (健盘D)
                    num = fun(num, 0x80);

                _Nes.joypaddata1 = num;
                System.Console.WriteLine(num);
            }
        }
    }
}
