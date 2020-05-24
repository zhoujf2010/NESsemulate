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
/* **********************************************
 * Some Code taken from : 
 * SharpNes, Copyright (c) 2005, Jonathan Turner
 * All rights reserved.
 * **********************************************
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading;
using System.Windows.Forms;
using System.Drawing;
using System.Drawing.Imaging;

namespace AHD.MyNes.Nes
{
    public class NesEmulator
    {

        private string saveFilename;
        private int JoyData1 = 0;
        private int JoyData2 = 0;
        private byte JoyStrobe = 0;
        private NesSystem _CurrentSystem = NesSystem.PAL;
        //private InputManager _inputManager;
        //private Joypad _joypad1;
        //private Joypad _joypad2;


        public uint Ticks_Per_Scanline = 113;//113 for NTSC, 106 for PAL

        //Devices in the nes
        public Cart myCartridge;// Nes cart
        public CPU6502 my6502;// CPU
        public Mappers myMapper;//Current mapper
        public PPU myPPU;//PPU
        //  public Video myVideo;//Video renderer
        public APU myAPU;//sound

        //Fields
        public bool isQuitting;
        public bool hasQuit;
        public bool isDebugging;
        public bool isSaveRAMReadonly;
        public bool isPaused;

        //public bool fix_bgchange;
        //public bool fix_spritehit;
        public bool fix_scrolloffset1;
        public bool fix_scrolloffset2;
        public bool fix_scrolloffset3;
        public bool LimitTo50 = false;
        public bool LimitTo60 = false;
        public byte[][] scratchRam;
        public byte[] saveRam;
        public bool SoundEnabled = true;
        public string FileName = "";
        public uint numOfInstructions;
        public bool SpeedThrottling = false;

        //public Joypad Joypad1 { get { return _joypad1; } }
        //public Joypad Joypad2 { get { return _joypad2; } }

        public NesSystem CurrentSystem
        {
            get
            {
                return _CurrentSystem;
            }
            set
            {
                _CurrentSystem = value;
                // myVideo.CurrentDrawer.TVMode = value;
                switch (value)
                {
                    case NesSystem.NTSC:
                        Ticks_Per_Scanline = 113;
                        myAPU.SetClockSpeed(1789772);
                        myAPU._frameCycles = 29606;//262 * 113
                        myAPU._palTiming = false;
                        myPPU.FramePeriod = 0.01667;//60 FPS
                        myPPU.Scanlinesperframe = 262;
                        //////////////////////////////
                        //This value is important, in doc's, they say
                        //that VBLANK is happens in SL (scan line) # 240
                        //but in real test (with some roms like Castlequest),
                        //this No will make a bad scroll in the game ...
                        //however i test it and 248 (with NTSC) is verygood
                        //for this game and others (Castlequest, Castlevania ..)
                        //remember that the PPU returns VBLANK in SL # 240
                        //in the status register
                        //while the real one happens after that lil' bit.
                        myPPU.ScanlinesOfVBLANK = 248;
                        //////////////////////////////
                        break;
                    case NesSystem.PAL:
                        Ticks_Per_Scanline = 106;
                        myAPU.SetClockSpeed(1773447);
                        myAPU._frameCycles = 33072;//312 * 106
                        myAPU._palTiming = true;
                        myPPU.FramePeriod = 0.020;//50 FPS
                        myPPU.Scanlinesperframe = 312;
                        myPPU.ScanlinesOfVBLANK = 290;
                        break;
                }
            }
        }

        public Control Surface
        {
            get
            {
                return _Surface;
            }
            set
            {
                _Surface = value;
                GR = Surface.CreateGraphics();
                GR.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.NearestNeighbor;
            }
        }

        public DrawSize SizeOfDraw
        {
            get
            {
                return _DrawSize;
            }
            set
            {
                _DrawSize = value;
                UpdateSize();
            }
        }
        public void UpdateSize()
        {
            _CanRender = false;
            while (_IsRendering)
            { }
            switch (_DrawSize)
            {
                case DrawSize.Stretch:
                    Screen_X = 0;
                    Screen_Y = 0;
                    Screen_W = Surface.Size.Width;
                    Screen_H = Surface.Size.Height;
                    break;
                case DrawSize.X1:
                    Screen_X = (Surface.Size.Width / 2) - 128;
                    Screen_Y = (Surface.Size.Height / 2) - (_ScanLines / 2);
                    Screen_W = 256;
                    Screen_H = _ScanLines;
                    break;
                case DrawSize.X2:
                    Screen_X = (Surface.Size.Width / 2) - 256;
                    Screen_Y = (Surface.Size.Height / 2) - _ScanLines;
                    Screen_W = 512;
                    Screen_H = _ScanLines * 2;
                    break;
            }
            GR = Surface.CreateGraphics();
            GR.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.NearestNeighbor;
            GR.Clear(Color.Black);
            CanRender = true;
        }
        public enum DrawSize
        {
            Stretch, X1, X2
        }

        public bool CanRender
        { get { return _CanRender; } set { _CanRender = value; } }
        public NesSystem TVMode
        {
            get
            {
                return _TVMode;
            }
            set
            {
                _TVMode = value;
                switch (value)
                {
                    case NesSystem.NTSC:
                        _ScanLines = 224;
                        _FirstLinesTCut = 2048;
                        bmp = new Bitmap(256, 224);
                        break;
                    case NesSystem.PAL:
                        _ScanLines = 240;
                        _FirstLinesTCut = 0;
                        bmp = new Bitmap(256, 240);
                        break;
                }
            }
        }
        Bitmap bmp = new Bitmap(256, 240);
        NesSystem _TVMode = NesSystem.NTSC;
        DrawSize _DrawSize = DrawSize.Stretch;
        bool _IsRendering = false;
        bool _CanRender = true;
        int _ScanLines = 240;
        int _FirstLinesTCut = 0;
        Control _Surface;
        Graphics GR;
        int Screen_X = 0;
        int Screen_Y = 0;
        int Screen_W = 0;
        int Screen_H = 0;
        string TextToRender = "";
        int TextApperance = 200;
        int p = 0;
        public unsafe void RenderFrame(short[] ScreenBuffer)
        {
            if (Surface != null & !_IsRendering & _CanRender)
            {
                _IsRendering = true;
                //Render the bitmap
                BitmapData bmpData = bmp.LockBits(new Rectangle(0, 0, 256, _ScanLines), ImageLockMode.WriteOnly, PixelFormat.Format16bppRgb565);
                short* numPtr = (short*)bmpData.Scan0;
                for (int i = _FirstLinesTCut; i < ScreenBuffer.Length - _FirstLinesTCut; i++)
                {
                    numPtr[i - _FirstLinesTCut] = ScreenBuffer[i];
                }
                bmp.UnlockBits(bmpData);
                bmp.Save("E:\\image2.png");
                p++;
                //Draw it !!
                GR.DrawImage(bmp, Screen_X, Screen_Y, Screen_W, Screen_H);
                //Draw the text if we have to
                if (TextApperance > 0)
                {
                    GR.DrawString(TextToRender, new System.Drawing.Font("Tohama", 16, FontStyle.Bold),
                       new SolidBrush(Color.White), new PointF(30, Surface.Height - 50));
                    TextApperance--;
                }
                _IsRendering = false;
            }


            //Bitmap bmpt = new Bitmap(256, 224, PixelFormat.Format16bppRgb565);
            //for (int i = 0; i < 256; i++)
            //{
            //    for (int j = 0; j < 224; j++)
            //    {
            //        short s = ScreenBuffer[i + j * 256 + _FirstLinesTCut];
            //        int r = (s & 0xF800)>>11;
            //        int g = (s & 0x07E0) >> 5;
            //        int b = (s & 0x001F);
            //        bmpt.SetPixel(i, j, Color.FromArgb(255,r,g,b ));
            //    }
            //}
            //File.Delete("E:\\img2.png");
            //bmpt.Save("E:\\img2.png");
        }

        //Vid_GDI_16Bit _CurrentDrawer;
        //Control _TV;

        //public Vid_GDI_16Bit CurrentDrawer
        //{
        //    get
        //    {
        //        if (_CurrentDrawer == null)
        //        {
        //            _CurrentDrawer = new Vid_GDI_16Bit();
        //            _CurrentDrawer.Surface = _TV;
        //        }
        //        return _CurrentDrawer;
        //    }
        //}

        /// <summary>
        /// Nes emulater main !!
        /// </summary>
        /// <param name="TV">The control that will be a draw surface</param>
        /// <param name="SoundDevice">The sound device (WARNING !! : don't make TV = SoundDevice)</param>
        public NesEmulator(Control TV, Control SoundDevice)
        {
            //_inputManager = new InputManager(TV.Parent.Handle, CooperativeLevel.Nonexclusive | CooperativeLevel.Foreground);

            //_joypad1 = new Joypad(_inputManager);
            //_joypad2 = new Joypad(_inputManager);

            myAPU = new APU(SoundDevice, this);
            //myVideo = new Video(TV);
            this.Surface = TV;
            InitializeEngine();
        }

        //Memory
        public byte ReadMemory8(ushort address)
        {
            byte returnvalue = 0;
            if (address < 0x2000)
            {
                if (address < 0x800)
                {
                    returnvalue = scratchRam[0][address];
                }
                else if (address < 0x1000)
                {
                    returnvalue = scratchRam[1][address - 0x800];
                }

                else if (address < 0x1800)
                {
                    returnvalue = scratchRam[2][address - 0x1000];
                }
                else
                {
                    returnvalue = scratchRam[3][address - 0x1800];
                }
            }
            else if (address < 0x6000)
            {
                switch (address)
                {
                    case (0x2002): returnvalue = myPPU.Status_Register_Read(); break;
                    case (0x2004): returnvalue = myPPU.SpriteRam_IO_Register_Read(); break;
                    case (0x2007): returnvalue = myPPU.VRAM_IO_Register_Read(); break;
                    case (0x4015): returnvalue = myAPU.ReadStatusReg(); break;
                    case (0x4016):
                        byte num2 = (byte)(JoyData1 & 1);
                        JoyData1 = JoyData1 >> 1;
                        returnvalue = num2;
                        break;
                    case (0x4017):
                        byte num3 = (byte)(JoyData2 & 1);
                        JoyData2 = JoyData2 >> 1;
                        returnvalue = num3;
                        break;
                }
            }
            else if (address < 0x8000)
            {
                returnvalue = saveRam[address - 0x6000];
                if (myCartridge.mapper == 5)
                    returnvalue = 1;
            }
            else
            {
                returnvalue = myMapper.ReadPrgRom(address);
            }
            return returnvalue;
        }
        public byte WriteMemory8(ushort address, byte data)
        {
            if (address == 532 && data == 192)
            {

            }
            if (address == 532 && data == 248)
            {
            }
            if (address < 0x800)
            {
                scratchRam[0][address] = data;
            }
            else if (address < 0x1000)
            {
                scratchRam[1][address - 0x800] = data;
            }
            else if (address < 0x1800)
            {
                scratchRam[2][address - 0x1000] = data;
            }
            else if (address < 0x2000)
            {
                scratchRam[3][address - 0x1800] = data;
            }
            else if (address < 0x4000)
            {
                switch (address)
                {
                    case (0x2000): myPPU.Control_Register_1_Write(data); break;
                    case (0x2001): myPPU.Control_Register_2_Write(data); break;
                    case (0x2003): myPPU.SpriteRam_Address_Register_Write(data); break;
                    case (0x2004): myPPU.SpriteRam_IO_Register_Write(data); break;
                    case (0x2005): myPPU.VRAM_Address_Register_1_Write(data); break;
                    case (0x2006): myPPU.VRAM_Address_Register_2_Write(data); break;
                    case (0x2007): myPPU.VRAM_IO_Register_Write(data); break;
                }
            }
            else if (address < 0x6000)
            {
                switch (address)
                {
                    case (0x4000): myAPU.WriteRectReg1(0, data); break;
                    case (0x4001): myAPU.WriteRectReg2(0, data); break;
                    case (0x4002): myAPU.WriteRectReg3(0, data); break;
                    case (0x4003): myAPU.WriteRectReg4(0, data); break;
                    case (0x4004): myAPU.WriteRectReg1(1, data); break;
                    case (0x4005): myAPU.WriteRectReg2(1, data); break;
                    case (0x4006): myAPU.WriteRectReg3(1, data); break;
                    case (0x4007): myAPU.WriteRectReg4(1, data); break;
                    case (0x4008): myAPU.WriteTriReg1(data); break;
                    case (0x4009): myAPU.WriteTriReg2(data); break;
                    case (0x400A): myAPU.WriteTriReg3(data); break;
                    case (0x400B): myAPU.WriteTriReg4(data); break;
                    case (0x400C): myAPU.WriteNoiseReg1(data); break;
                    case (0x400E): myAPU.WriteNoiseReg2(data); break;
                    case (0x400F): myAPU.WriteNoiseReg3(data); break;
                    case (0x4010): myAPU.WriteDMCReg1(data); break;
                    case (0x4011): myAPU.WriteDMCReg2(data); break;
                    case (0x4012): myAPU.WriteDMCReg3(data); break;
                    case (0x4013): myAPU.WriteDMCReg4(data); break;
                    case (0x4014): myPPU.SpriteRam_DMA_Begin(data); break;
                    case (0x4015): myAPU.WriteStatusReg(data); break;
                    case (0x4016):
                        if ((this.JoyStrobe == 1) && ((data & 1) == 0))
                        {
                            //_inputManager.Update();
                            // this.JoyData1 = _joypad1.GetJoyData() | 0x100;
                            this.JoyData1 = joypaddata1 | 0x100;
                            // this.JoyData2 = _joypad2.GetJoyData() | 0x200;
                        }
                        this.JoyStrobe = (byte)(data & 1);
                        break;
                }

                if (myCartridge.mapper == 5)
                    myMapper.WritePrgRom(address, data);
            }
            else if (address < 0x8000)
            {
                if (!isSaveRAMReadonly)
                    saveRam[address - 0x6000] = data;

                if (myCartridge.mapper == 34 & myCartridge.mapper == 16)
                    myMapper.WritePrgRom(address, data);

            }
            else
            {
                myMapper.WritePrgRom(address, data);
            }
            return 1;
        }

        public int joypaddata1 { get; set; }

        public ushort ReadMemory16(ushort address)
        {
            byte data_1 = 0;
            byte data_2 = 0;
            if (address < 0x2000)
            {
                if (address < 0x800)
                {
                    data_1 = scratchRam[0][address];
                    data_2 = scratchRam[0][address + 1];
                }
                else if (address < 0x1000)
                {
                    data_1 = scratchRam[1][address - 0x800];
                    data_2 = scratchRam[1][address - 0x800 + 1];
                }

                else if (address < 0x1800)
                {
                    data_1 = scratchRam[2][address - 0x1000];
                    data_2 = scratchRam[2][address - 0x1000 + 1];
                }
                else
                {
                    data_1 = scratchRam[3][address - 0x1800];
                    data_2 = scratchRam[3][address - 0x1800 + 1];
                }
            }
            else if (address < 0x8000)
            {
                data_1 = saveRam[address - 0x6000];
                data_2 = saveRam[address - 0x6000 + 1];
            }
            else
            {
                data_1 = myMapper.ReadPrgRom(address);
                data_2 = myMapper.ReadPrgRom((ushort)(address + 1));
            }
            ushort data = (ushort)((data_2 << 8) + data_1);
            return data;
        }

        /// <summary>
        /// Start the nes machine
        /// </summary>
        public void InitializeEngine()
        {
            myCartridge = new Cart();
            my6502 = new CPU6502(this);
            myMapper = new Mappers(this, ref myCartridge);
            myPPU = new PPU(this);//, myVideo);

            myAPU.Reset();

            scratchRam = new byte[4][];
            scratchRam[0] = new byte[0x800];
            scratchRam[1] = new byte[0x800];
            scratchRam[2] = new byte[0x800];
            scratchRam[3] = new byte[0x800];
            saveRam = new byte[0x2000];

            isSaveRAMReadonly = false;
            isDebugging = false;
            isQuitting = false;
            isPaused = false;
            hasQuit = false;
            //fix_bgchange = false;
            //fix_spritehit = false;
            fix_scrolloffset1 = false;
            fix_scrolloffset2 = false;
            fix_scrolloffset3 = false;
        }
        /// <summary>
        /// Restart !!
        /// </summary>
        public void RestartEngine()
        {
            isSaveRAMReadonly = false;
            isDebugging = false;
            isQuitting = false;
            isPaused = false;
            hasQuit = false;
            //fix_bgchange = false;
            //fix_spritehit = false;
            fix_scrolloffset1 = false;
            fix_scrolloffset2 = false;
            fix_scrolloffset3 = false;
            myPPU.RestartPPU();
        }
        /// <summary>
        /// Quit (turn off)
        /// </summary>
        public void QuitEngine()
        {
            myAPU.Pause();
            myAPU.Shutdown();
            isQuitting = true;
        }
        public void TogglePause()
        {
            if (isPaused)
            {
                isPaused = false;
            }
            else
            {
                isPaused = true;
            }
        }
        public void DoFrame()
        {
            //* Try & Catch slow down the emulator i think
            my6502.pc_register = ReadMemory16(0xFFFC);
            my6502.RunProcessor();
            hasQuit = true;
        }

        //Cart ...
        public bool LoadCart(string filename)
        {
            FileName = filename;
            byte[] nesHeader = new byte[16];
            int i;
            try
            {
                using (FileStream reader = File.OpenRead(filename))
                {
                    reader.Read(nesHeader, 0, 16);
                    int prg_roms = nesHeader[4] * 4;
                    myCartridge.prg_rom_pages = nesHeader[4];
                    myCartridge.prg_rom = new byte[prg_roms][];
                    for (i = 0; i < (prg_roms); i++)
                    {
                        myCartridge.prg_rom[i] = new byte[4096];
                        reader.Read(myCartridge.prg_rom[i], 0, 4096);
                    }
                    int chr_roms = nesHeader[5] * 8;
                    myCartridge.chr_rom_pages = nesHeader[5];
                    if (myCartridge.chr_rom_pages != 0)
                    {
                        myCartridge.chr_rom = new byte[chr_roms][];
                        for (i = 0; i < (chr_roms); i++)
                        {
                            myCartridge.chr_rom[i] = new byte[1024];
                            reader.Read(myCartridge.chr_rom[i], 0, 1024);
                        }
                        myCartridge.is_vram = false;
                    }
                    else
                    {
                        myCartridge.chr_rom = new byte[(prg_roms * 16)][];
                        for (i = 0; i < (prg_roms * 16); i++)
                        {
                            myCartridge.chr_rom[i] = new byte[1024];
                        }
                        myCartridge.is_vram = true;
                    }
                    if ((nesHeader[6] & 0x1) == 0x0)
                    {
                        myCartridge.mirroring = MIRRORING.HORIZONTAL;
                    }
                    else
                    {
                        myCartridge.mirroring = MIRRORING.VERTICAL;
                    }

                    if ((nesHeader[6] & 0x2) == 0x0)
                    {
                        myCartridge.save_ram_present = false;
                    }
                    else
                    {
                        myCartridge.save_ram_present = true;
                    }

                    if ((nesHeader[6] & 0x4) == 0x0)
                    {
                        myCartridge.trainer_present = false;
                    }
                    else
                    {
                        myCartridge.trainer_present = true;
                    }

                    if ((nesHeader[6] & 0x8) != 0x0)
                    {
                        myCartridge.mirroring = MIRRORING.FOUR_SCREEN;
                    }

                    if (nesHeader[7] == 0x44)
                    {
                        myCartridge.mapper = (byte)(nesHeader[6] >> 4);
                    }
                    else
                    {
                        myCartridge.mapper = (byte)((nesHeader[6] >> 4) + (nesHeader[7] & 0xf0));
                    }
                    if ((nesHeader[6] == 0x23) && (nesHeader[7] == 0x64))
                        myCartridge.mapper = 2;
                    if ((myCartridge.prg_rom[prg_roms - 1][0xfeb] == 'Z') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfec] == 'E') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfed] == 'L') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfee] == 'D') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfef] == 'A'))
                    {
                        //fix_bgchange = true;
                    }
                    if ((myCartridge.prg_rom[prg_roms - 1][0xfe0] == 'B') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe1] == 'B') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe2] == '4') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe3] == '7') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe4] == '9') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe5] == '5') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe6] == '6') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe7] == '-') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe8] == '1') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfe9] == '5') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfea] == '4') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfeb] == '4') &&
                        (myCartridge.prg_rom[prg_roms - 1][0xfec] == '0'))
                    {
                        fix_scrolloffset1 = true;
                    }
                    if ((myCartridge.prg_rom[0][0x9] == 0xfc) &&
                        (myCartridge.prg_rom[0][0xa] == 0xfc) &&
                        (myCartridge.prg_rom[0][0xb] == 0xfc) &&
                        (myCartridge.prg_rom[0][0xc] == 0x40) &&
                        (myCartridge.prg_rom[0][0xd] == 0x40) &&
                        (myCartridge.prg_rom[0][0xe] == 0x40) &&
                        (myCartridge.prg_rom[0][0xf] == 0x40))
                    {
                        fix_scrolloffset2 = true;
                    }
                    if ((myCartridge.prg_rom[0][0x75] == 0x11) &&
                        (myCartridge.prg_rom[0][0x76] == 0x12) &&
                        (myCartridge.prg_rom[0][0x77] == 0x13) &&
                        (myCartridge.prg_rom[0][0x78] == 0x14) &&
                        (myCartridge.prg_rom[0][0x79] == 0x07) &&
                        (myCartridge.prg_rom[0][0x7a] == 0x03) &&
                        (myCartridge.prg_rom[0][0x7b] == 0x03) &&
                        (myCartridge.prg_rom[0][0x7c] == 0x03) &&
                        (myCartridge.prg_rom[0][0x7d] == 0x03)
                        )
                    {
                        fix_scrolloffset3 = true;
                    }
                    myMapper.SetUpMapperDefaults();
                }
            }
            catch
            {
                return false;
            }

            if (myCartridge.save_ram_present)
            {
                saveFilename = filename.Remove(filename.Length - 3, 3);
                saveFilename = saveFilename.Insert(saveFilename.Length, "sav");
                try
                {
                    using (FileStream reader = File.OpenRead(saveFilename))
                    {
                        reader.Read(saveRam, 0, 0x2000);
                    }
                }
                catch
                {
                    //Ignore it, we'll make our own.
                }
            }
            return true;
        }
    }
    public enum NesSystem
    {
        NTSC, PAL
    }
}