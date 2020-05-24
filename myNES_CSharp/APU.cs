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
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Media;
namespace AHD.MyNes.Nes
{
    public class APU
    {
        // Fields
        private const int _bitsPerSample = 0x10;
        //private SecondarySoundBuffer _buffer;
        private const int _bufferLength = 5;
        private int _bufferSize;
        private bool[] _channelEnabled;
        private int _dataPosition;
        private bool _firstRender = true;
        private int _frameCounter;
        public uint _frameCycles = 29780;
        public uint _frameElapsedCycles;
        private int _lastPosition;
        public bool _palTiming;
        public double SampleRate = 44100.0;
        private APUChannel[] _soundChannels;
        private byte[] _soundData;
        //private DirectSound _soundDevice;
        NesEmulator _Nes;
        bool IsPaused = false;
        int _MasterVolume = 7;
        uint k = 0;
        // Methods
        public APU(Control parent, NesEmulator NesEmu)
        {
            _Nes = NesEmu;
            this.InitDirectSound(parent);
            this.Reset();
        }
        private void ClearBuffer()
        {
            for (int i = 0; i < this._bufferSize; i++)
            {
                this._soundData[i] = 0;
            }
            //this._buffer.Write(this._soundData, 0, LockFlags.EntireBuffer);
        }
        private void InitDirectSound(Control parent)
        {
            //this._soundDevice = new DirectSound();
            //this._soundDevice.SetCooperativeLevel(parent.Parent.Handle, CooperativeLevel.Priority);
            //WaveFormat wfx = new WaveFormat();
            //wfx.FormatTag = WaveFormatTag.Pcm;
            //wfx.BitsPerSample = 0x10;
            //wfx.Channels = 1;
            //wfx.SamplesPerSecond = 0xac44;
            //wfx.AverageBytesPerSecond = ((wfx.BitsPerSample / 8) * wfx.Channels) * wfx.SamplesPerSecond;
            //wfx.BlockAlignment = 2;
            //SoundBufferDescription desc = new SoundBufferDescription();
            //desc.Format = wfx;
            //desc.SizeInBytes = this._bufferSize = wfx.AverageBytesPerSecond * 5;
            //desc.Flags = BufferFlags.GlobalFocus | BufferFlags.Software;
            //this._buffer = new SecondarySoundBuffer(this._soundDevice, desc);
            //this._soundData = new byte[this._bufferSize];
            //this.ClearBuffer();
            //this._buffer.Play(0, PlayFlags.Looping);
        }
        public void Pause()
        {
            //if ((this._buffer != null) && !this._buffer.Disposed)
            //{
            //    this._buffer.Stop(); IsPaused = true;
            //}
        }
        public void Play()
        {
            //if (!IsPaused)
            //{ return; }
            //if ((this._buffer != null) && !this._buffer.Disposed)
            //{
            //    IsPaused = false;
            //    this._buffer.Play(0, PlayFlags.Looping);
            //}
        }
        public byte ReadStatusReg()
        {
            int num = 0;
            num |= this._soundChannels[0].Enabled ? 1 : 0;
            num |= this._soundChannels[1].Enabled ? 2 : 0;
            num |= this._soundChannels[2].Enabled ? 4 : 0;
            num |= this._soundChannels[3].Enabled ? 8 : 0;
            num |= this._soundChannels[4].Enabled ? 0x10 : 0;
            return (byte)num;
        }
        public void Render(uint cycles)
        {
            if (IsPaused)
            { return; }
            int writePosition = 0;
            this.UpdateRegisters(cycles);
            //try
            //{
            //writePosition = this._buffer.CurrentWritePosition;//
            //}
            //catch { return; }
            //if (this._firstRender)
            //{
            //    this._firstRender = false;
            //    this._dataPosition = this._buffer.CurrentWritePosition + 0x1000;
            //    this._lastPosition = this._buffer.CurrentWritePosition;
            //}
            int num2 = writePosition - this._lastPosition;
            if (num2 < 0)
            {
                num2 = (this._bufferSize - this._lastPosition) + writePosition;
            }
            if (num2 != 0)
            {
                for (int i = 0; i < num2; i += 2)
                {
                    ushort num4 = 0;
                    for (int j = 0; j < 5; j++)
                    {
                        if (this._channelEnabled[j])
                        {
                            num4 = (ushort)(num4 + this._soundChannels[j].RenderSample());
                        }
                    }
                    num4 = (ushort)(num4 * _MasterVolume);
                    this._soundData[this._dataPosition + 1] = (byte)((ushort)((num4 & 0xff00) >> 8));
                    this._soundData[this._dataPosition] = (byte)(num4 & 0xff);
                    this._dataPosition += 2;
                    this._dataPosition = this._dataPosition % this._bufferSize;
                }
                //this._buffer.Write(this._soundData, 0, LockFlags.None);
                this._lastPosition = writePosition;
            }
        }
        private void UpdateRegisters(uint cycles)
        {
            if ((cycles % _frameCycles) > _Nes.my6502.tick_count)
            {
                if (_palTiming)
                    k = 4;
                else
                    k = 3;
            }
            else
            {
                if (_palTiming)
                    k = 3;
                else
                    k = 2;
            }
            //Channel_Noise TR = (Channel_Noise)_soundChannels[3];
            //_Nes.myVideo.DrawText(TR.LengthCounter.ToString(), 1);
            while (k > 0)
            {
                this._frameCounter++;
                int num = this._palTiming ? (this._frameCounter % 5) : (this._frameCounter % 4);
                for (int i = 0; i < 5; i++)
                {
                    APUChannel base2 = this._soundChannels[i];
                    base2.UpdateEnvelope();
                    base2.UpdateLinearCounter();
                    switch (num)
                    {
                        case 1:
                            base2.DecrementLengthCounter();
                            base2.UpdateSweep();
                            break;
                        case 3:
                            base2.DecrementLengthCounter();
                            base2.UpdateSweep();
                            break;
                    }
                } k--;
            }
            _Nes.my6502.total_cycles = 0;
        }
        public void Reset()
        {
            //this._soundChannels = new APUChannel[] { new Channel_Square(44100.0, true), new Channel_Square(44100.0, false), new Channel_Triangle(44100.0), new Channel_Noise(44100.0), new Channel_DMC(44100.0, _Nes) };
            this._soundChannels = new APUChannel[] 
            { 
                new Channel_Square(SampleRate, true), //0
                new Channel_Square(SampleRate, false),//1
                new Channel_Triangle(SampleRate),     //2
                new Channel_Noise(SampleRate),        //3
                new Channel_DMC(SampleRate, _Nes)     //4
            };
            this._channelEnabled = new bool[5];
            for (int i = 0; i < 5; i++)
            {
                this._channelEnabled[i] = true;
            }
        }
        public void Shutdown()
        {
            //if ((this._buffer != null) && !this._buffer.Disposed)
            //{
            //    this._buffer.Stop(); IsPaused = true;
            //}
            //this._buffer.Dispose();
            //this._soundDevice.Dispose();
        }
        public void WriteDMCReg1(byte b)
        {
            this._soundChannels[4].WriteReg1(b);
        }
        public void WriteDMCReg2(byte b)
        {
            this._soundChannels[4].WriteReg2(b);
        }
        public void WriteDMCReg3(byte b)
        {
            this._soundChannels[4].WriteReg3(b);
        }
        public void WriteDMCReg4(byte b)
        {
            this._soundChannels[4].WriteReg4(b);
        }
        public void WriteNoiseReg1(byte b)
        {
            this._soundChannels[3].WriteReg1(b);
        }
        public void WriteNoiseReg2(byte b)
        {
            this._soundChannels[3].WriteReg2(b);
        }
        public void WriteNoiseReg3(byte b)
        {
            this._soundChannels[3].WriteReg3(b);
        }
        public void WriteRectReg1(int c, byte b)
        {
            this._soundChannels[c].WriteReg1(b);
        }
        public void WriteRectReg2(int c, byte b)
        {
            this._soundChannels[c].WriteReg2(b);
        }
        public void WriteRectReg3(int c, byte b)
        {
            this._soundChannels[c].WriteReg3(b);
        }
        public void WriteRectReg4(int c, byte b)
        {
            this._soundChannels[c].WriteReg4(b);
        }
        public void WriteStatusReg(byte b)
        {
            this._soundChannels[0].Enabled = (b & 1) != 0;
            this._soundChannels[1].Enabled = (b & 2) != 0;
            this._soundChannels[2].Enabled = (b & 4) != 0;
            this._soundChannels[3].Enabled = (b & 8) != 0;
            this._soundChannels[4].Enabled = (b & 0x10) != 0;

        }
        public void WriteTriReg1(byte b)
        {
            this._soundChannels[2].WriteReg1(b);
        }
        public void WriteTriReg2(byte b)
        {
            this._soundChannels[2].WriteReg2(b);
        }
        public void WriteTriReg3(byte b)
        {
            this._soundChannels[2].WriteReg3(b);
        }
        public void WriteTriReg4(byte b)
        {
            this._soundChannels[2].WriteReg4(b);
        }
        public void Write4017(byte b)
        {
            //IRQ = ((b & 0x40) == 0);
            //_frameCounter = 0;
            //_palTiming = ((b & 0x80) != 0);
        }
        public void SetClockSpeed(int ClockSpeed)
        {
            for (int j = 0; j < 5; j++)
            {
                _soundChannels[j].ClockSpeed = ClockSpeed;
            }
        }
        // Properties
        public bool DMCEnabled
        {
            get
            {
                return this._channelEnabled[4];
            }
            set
            {
                this._channelEnabled[4] = value;
            }
        }
        public bool NoiseEnabled
        {
            get
            {
                return this._channelEnabled[3];
            }
            set
            {
                this._channelEnabled[3] = value;
            }
        }
        public bool SquareWave1Enabled
        {
            get
            {
                return this._channelEnabled[0];
            }
            set
            {
                this._channelEnabled[0] = value;
            }
        }
        public bool SquareWave2Enabled
        {
            get
            {
                return this._channelEnabled[1];
            }
            set
            {
                this._channelEnabled[1] = value;
            }
        }
        public bool TriangleEnabled
        {
            get
            {
                return this._channelEnabled[2];
            }
            set
            {
                this._channelEnabled[2] = value;
            }
        }
        public int MasterVolume
        { get { return _MasterVolume; } set { _MasterVolume = value; } }
    }
}