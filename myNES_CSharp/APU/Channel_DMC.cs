using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
namespace AHD.MyNes.Nes
{
    public class Channel_DMC : APUChannel
    {
        // Fields
        private byte _dac;
        private int _dacCounter;
        /*private static int[] _dmcWavelengths = new int[] 
        { 0x1ac, 380, 340, 320, 0x11e, 0xfe, 0xe2, 0xd6, 190,
            160, 0x8e, 0x80, 0x6a, 0x54, 0x48, 0x36 };*/
        private static int[] _dmcWavelengths = new int[] 
        { 0xD60, 0xBE0, 0xAA0, 0xA00, 0x8F0, 0x7F0, 0x710, 0x6B0, 0x5F0,
            0x500, 0x470, 0x400, 0x350, 0x2A8, 0x240, 0x1B0 };
        private double _frequency;
        private ushort _initialAddress;
        private int _initialLength;
        private bool _irqEnable;
        private bool _loop;
        private double _renderedWavelength;
        private ushort _sampleAddress;
        private int _sampleLength;
        private int _shift;
        NesEmulator _Nes;
        // Methods
        public Channel_DMC(double samplingRate, NesEmulator NesEmu)
            : base(samplingRate)
        {
            _Nes = NesEmu;
        }
        public override ushort RenderSample()
        {
            if (base.Enabled)
            {
                base.SampleCount++;
                if (base.SampleCount > this._renderedWavelength)
                {
                    base.SampleCount -= this._renderedWavelength;
                    if ((this.SampleLength > 0) && (this._shift == 0))
                    {
                        ushort num2;
                        this.SampleAddress = (ushort)((num2 = this.SampleAddress) + 1);
                        this.DAC = _Nes.ReadMemory8(num2);
                        this.SampleLength--;
                        this._shift = 8;
                        if (this.Loop && (this.SampleLength <= 0))
                        {
                            this.SampleLength = this._initialLength;
                            this.SampleAddress = this._initialAddress;
                        }
                    }
                    if (this.SampleLength > 0)
                    {
                        if (this.DAC != 0)
                        {
                            int num = this.DAC & 1;
                            if ((num == 0) && (this._dacCounter > 1))
                            {
                                this._dacCounter -= 2;
                            }
                            else if ((num != 0) && (this._dacCounter < 0x7e))
                            {
                                this._dacCounter += 2;
                            }
                        }
                        this._dacCounter--;
                        if (this._dacCounter <= 0)
                        {
                            this._dacCounter = 8;
                        }
                        this.DAC = (byte)(this.DAC >> 1);
                        this._shift--;
                    }
                }
            }
            return (ushort)(this._dacCounter * 0x30);
        }
        public override void UpdateEnvelope()
        {
        }
        private void UpdateFrequency()
        {
            this._frequency = base.ClockSpeed / (base.Wavelength + 1);
            this._renderedWavelength = base._samplingRate / this._frequency;
        }
        public override void UpdateLinearCounter()
        {
        }
        public override void UpdateSweep()
        {
        }
        public override void WriteReg1(byte b)
        {
            this.IRQEnable = (b & 0x80) != 0;
            this.Loop = (b & 0x40) != 0;
            base.Wavelength = _dmcWavelengths[b & 15];
            this.UpdateFrequency();
        }
        public override void WriteReg2(byte b)
        {
            this.DAC = (byte)(b & 0x7f);
            this._shift = 8;
        }
        public override void WriteReg3(byte b)
        {
            this.SampleAddress = (ushort)((b * 0x40) + 0xc000);
            this._initialAddress = this.SampleAddress;
        }
        public override void WriteReg4(byte b)
        {
            this.SampleLength = (b * 0x10) + 1;
            this._initialLength = this.SampleLength;
        }
        // Properties
        public byte DAC
        {
            get
            {
                return this._dac;
            }
            set
            {
                this._dac = value;
            }
        }
        public bool IRQEnable
        {
            get
            {
                return this._irqEnable;
            }
            set
            {
                this._irqEnable = value;
            }
        }
        public bool Loop
        {
            get
            {
                return this._loop;
            }
            set
            {
                this._loop = value;
            }
        }
        public ushort SampleAddress
        {
            get
            {
                return this._sampleAddress;
            }
            set
            {
                this._sampleAddress = value;
            }
        }
        public int SampleLength
        {
            get
            {
                return this._sampleLength;
            }
            set
            {
                this._sampleLength = value;
            }
        }
    }
}
