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
    public class Channel_Triangle : APUChannel
    {
        // Fields
        ushort dat;
        private double _frequency;
        private static byte[] _lengthValues = new byte[] {
 0x5*2,0x7f*2,0xA*2,0x1*2,0x14*2,0x2*2,0x28*2,0x3*2,0x50*2,0x4*2,0x1E*2,0x5*2,0x7*2,0x6*2,0x0E*2,0x7*2,
 0x6*2,0x08*2,0xC*2,0x9*2,0x18*2,0xa*2,0x30*2,0xb*2,0x60*2,0xc*2,0x24*2,0xd*2,0x8*2,0xe*2,0x10*2,0xf*2
};
        private int _linearCounter;
        private int _linearCounterLoad;
        private double _renderedWavelength;
        private int _rightShift;
        private uint _sequence;
        public static byte[] _sequenceData = new byte[] { 
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0
     };
        private bool _triangleHalt;
        // Methods
        public Channel_Triangle(double samplingRate)
            : base(samplingRate)
        {
        }
        public override ushort RenderSample()
        {
            if (((base.LengthCounter > 0) && (this.LinearCounter > 0)) && (base.Wavelength > 0))
            {
                base.SampleCount++;
                if (base.SampleCount >= this._renderedWavelength)
                {
                    base.SampleCount -= this._renderedWavelength;
                    this.Sequence++;
                }
                return (ushort)(_sequenceData[this.Sequence & 0x1f] * 0x40);
            }
            return 0;
            //return (ushort)(_sequenceData[this.Sequence & 0x1f] * 0x40);
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
            if (!base.LengthCounterDisable)
            {
                this.LinearCounter--;
                if (this.LinearCounter < 0)
                {
                    this.LinearCounter = 0;
                }
            }
        }
        public override void UpdateSweep()
        {
        }
        public override void WriteReg1(byte b)
        {
            base.LengthCounterDisable = (b & 0x80) != 0;
            this.LinearCounterLoad = b & 0x7f;
        }
        public override void WriteReg2(byte b)
        {
            this.RightShift = b & 3;
        }
        public override void WriteReg3(byte b)
        {
            base.Wavelength = (base.Wavelength & 0x700) | b;
            this.UpdateFrequency();
        }
        public override void WriteReg4(byte b)
        {
            base.Wavelength = (base.Wavelength & 0xff) | ((b & 7) << 8);
            this.UpdateFrequency();
            base.LengthCounter = _lengthValues[(b & 0xf8) >> 3]; 
            this.TriangleHalt = true;
            this.LinearCounter = this.LinearCounterLoad;
        }
        // Properties
        public int LinearCounter
        {
            get
            {
                return this._linearCounter;
            }
            set
            {
                this._linearCounter = value;
            }
        }
        public int LinearCounterLoad
        {
            get
            {
                return this._linearCounterLoad;
            }
            set
            {
                this._linearCounterLoad = value;
            }
        }
        public int RightShift
        {
            get
            {
                return this._rightShift;
            }
            set
            {
                this._rightShift = value;
            }
        }
        public uint Sequence
        {
            get
            {
                return this._sequence;
            }
            set
            {
                this._sequence = value;
            }
        }
        public bool TriangleHalt
        {
            get
            {
                return this._triangleHalt;
            }
            set
            {
                this._triangleHalt = value;
            }
        }
    }
}
