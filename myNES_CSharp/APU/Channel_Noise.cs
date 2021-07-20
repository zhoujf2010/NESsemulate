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
using System.IO;
namespace AHD.MyNes.Nes
{
    public class Channel_Noise : APUChannel
    {
        // Fields
        private double _frequency;
        private static byte[] _lengthValues = new byte[] {
 0x5*2,0x7f*2,0xA*2,0x1*2,0x14*2,0x2*2,0x28*2,0x3*2,0x50*2,0x4*2,0x1E*2,0x5*2,0x7*2,0x6*2,0x0E*2,0x7*2,
 0x6*2,0x08*2,0xC*2,0x9*2,0x18*2,0xa*2,0x30*2,0xb*2,0x60*2,0xc*2,0x24*2,0xd*2,0x8*2,0xe*2,0x10*2,0xf*2
};
        private bool _noiseMode;
        private int _noiseShiftData;
        private static int[] _noiseWavelengths = new int[] { 0x2, 0x4, 0x8, 0x10,
            0x20, 0x30, 0x40, 0x50, 0x65, 0x7f, 0xbe, 0xfe, 0x17D, 0x1fc,
            0x3f9, 0x7f2 };
        private double _renderedWavelength;

        // Methods
        public Channel_Noise(double samplingRate)
            : base(samplingRate)
        {
            this._noiseShiftData = 1;
        }

        public override ushort RenderSample()
        {
            if (base.LengthCounter > 0 & this.Enabled)
            {
                base.SampleCount++;
                if (base.SampleCount >= this._renderedWavelength)
                {
                    int num2;
                    base.SampleCount -= this._renderedWavelength;
                    int num = (this.NoiseShiftData & 0x4000) >> 14;//Bit14
                    if (this.NoiseMode)
                    {
                        num2 = (this.NoiseShiftData & 0x0100) >> 8;//Bit8
                    }
                    else
                    {
                        num2 = (this.NoiseShiftData & 0x2000) >> 13;//Bit13
                    }
                    this.NoiseShiftData = this.NoiseShiftData << 1;
                    this.NoiseShiftData |= (num ^ num2) & 1;
                }
                int num3 = (this.NoiseShiftData & 1) * 0x20;
                return (ushort)(num3 * (base.EnvelopeDecayDisable ? base.Volume : base.Envelope));
            }
            return 0;
            //int num3 = (this.NoiseShiftData & 1) * 0x20;
            //return (ushort)(num3 * (base.EnvelopeDecayDisable ? base.Volume : base.Envelope ));
        }
        public override void UpdateEnvelope()
        {
            base.EnvelopeCounter--;
            if ((!base.EnvelopeDecayDisable && (base.Envelope > 0)) && (base.EnvelopeCounter == 0))
            {
                base.Envelope--;
                base.EnvelopeCounter = base.Volume;
            }
            if (base.LengthCounterDisable && (base.Envelope == 0))
            {
                base.Envelope = 15;
            }
         
        }
        private void UpdateFrequency()
        {
            this._frequency = base.ClockSpeed / ((base.Wavelength + 1) * 0x10);
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
            base.Volume = base.EnvelopeCounter = b & 0xF;
            base.EnvelopeDecayDisable = (b & 0x10) != 0;
            base.LengthCounterDisable = (b & 0x20) != 0;
        }
        public override void WriteReg2(byte b)
        {
            base.Wavelength = _noiseWavelengths[b & 15];
            this.UpdateFrequency();
            this.NoiseMode = (b & 0x80) != 0;
        }
        public override void WriteReg3(byte b)
        {
            base.LengthCounter = _lengthValues[(b & 0xF8) >> 3];
        }
        public override void WriteReg4(byte b)
        {
        }
        // Properties
        public bool NoiseMode
        {
            get
            {
                return this._noiseMode;
            }
            set
            {
                this._noiseMode = value;
            }
        }
        public int NoiseShiftData
        {
            get
            {
                return this._noiseShiftData;
            }
            set
            {
                if (value >  100)
                    System.Console.WriteLine();
                this._noiseShiftData = value;
            }
        }
    }
}
