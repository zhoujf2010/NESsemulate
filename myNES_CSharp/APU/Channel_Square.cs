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
    public class Channel_Square : APUChannel
    {
        // Fields
        private int _duty;
        private double _dutyPercentage;
        private double _frequency;
        private bool _isFirstChannel;
        /*private static byte[] _lengthValues = new byte[] { 
        5, 0x7f, 10, 1, 20, 2, 40, 3, 80, 4, 30, 5, 7, 6, 13, 7, 
        6, 8, 12, 9, 0x18, 10, 0x30, 11, 0x60, 12, 0x24, 13, 8, 14, 0x10, 15
     };*/
        private static byte[] _lengthValues = new byte[] {
 0x5*2,0x7f*2,0xA*2,0x1*2,0x14*2,0x2*2,0x28*2,0x3*2,0x50*2,0x4*2,0x1E*2,0x5*2,0x7*2,0x6*2,0x0E*2,0x7*2,
 0x6*2,0x08*2,0xC*2,0x9*2,0x18*2,0xa*2,0x30*2,0xb*2,0x60*2,0xc*2,0x24*2,0xd*2,0x8*2,0xe*2,0x10*2,0xf*2
};
        private double _renderedWavelength;
        private int _rightShift;
        private int _sweepCounter;
        private bool _sweepEnable;
        private bool _sweepNegate;
        private int _sweepRate;
        // Methods
        public Channel_Square(double samplingRate, bool firstChannel)
            : base(samplingRate)
        {
            this._isFirstChannel = firstChannel;
        }
        public override ushort RenderSample()
        {
            if (base.LengthCounter > 0)
            {
                base.SampleCount++;
                if (base.WaveStatus && (base.SampleCount > (this._renderedWavelength * this.DutyPercentage)))
                {
                    base.SampleCount -= this._renderedWavelength * this.DutyPercentage;
                    base.WaveStatus = !base.WaveStatus;
                }
                else if (!base.WaveStatus && (base.SampleCount > (this._renderedWavelength * (1.0 - this.DutyPercentage))))
                {
                    base.SampleCount -= this._renderedWavelength * (1.0 - this.DutyPercentage);
                    base.WaveStatus = !base.WaveStatus;
                }
                if (base.WaveStatus)
                {
                    return 0;
                }
                if (!base.EnvelopeDecayDisable)
                {
                    return (ushort)(0x40 * base.Envelope);
                }
                return (ushort)(0x40 * base.Volume);
            }
            return 0;
            /*if (base.WaveStatus)
            {
                return 0;
            }
            if (!base.EnvelopeDecayDisable)
            {
                return (ushort)(0x40 * base.Envelope);
            }
            return (ushort)(0x40 * base.Volume);*/
        }
        public override void UpdateEnvelope()
        {
            base.EnvelopeCounter--;
            if (base.EnvelopeCounter < 0)
            {
                base.EnvelopeCounter = 0;
            }
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
            this._frequency = base.ClockSpeed / ((base.Wavelength + 1) * 0x10); //0x1b5030 / ((base.Wavelength + 1) * 0x10);
            this._renderedWavelength = base._samplingRate / this._frequency;
        }
        public override void UpdateLinearCounter()
        {
        }
        public override void UpdateSweep()
        {
            int num = 0;
            this.SweepCounter--;
            if (this.SweepCounter < 0)
            {
                this.SweepCounter = 0;
            }
            if ((this.SweepEnable && (this.SweepCounter <= 0)) && ((this.RightShift != 0) && (base.LengthCounter > 0)))
            {
                this.SweepCounter = this.SweepRate;
                if (base.Wavelength >= 8)
                {
                    num = base.Wavelength >> this.RightShift;
                    if (this.SweepNegate)
                    {
                        num = -num;
                        if (!this._isFirstChannel)
                        {
                            num--;
                        }
                    }
                    num += base.Wavelength;
                    if ((num < 0x800) && (num > 8))
                    {
                        base.Wavelength = num;
                        this.UpdateFrequency();
                    }
                    else
                    {
                        this.SweepEnable = false;
                    }
                }
            }
        }
        public override void WriteReg1(byte b)
        {
            base.Volume = base.EnvelopeCounter = b & 15;
            base.EnvelopeCounter++;
            base.EnvelopeDecayDisable = (b & 0x10) != 0;
            base.LengthCounterDisable = (b & 0x20) != 0;
            this.Duty = (b & 0xc0) >> 6;
            switch (this.Duty)
            {
                case 0:
                    this.DutyPercentage = 0.125;
                    return;

                case 1:
                    this.DutyPercentage = 0.25;
                    return;

                case 2:
                    this.DutyPercentage = 0.5;
                    return;

                case 3:
                    this.DutyPercentage = 0.75;
                    return;
            }
        }
        public override void WriteReg2(byte b)
        {
            this.RightShift = b & 7;
            this.SweepNegate = (b & 8) != 0;
            this.SweepRate = this.SweepCounter = ((b & 0x70) >> 4) + 1;
            this.SweepEnable = (b & 0x80) != 0;
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
            if (!base.EnvelopeDecayDisable)
            {
                base.Envelope = 15;
            }
        }
        // Properties
        public int Duty
        {
            get
            {
                return this._duty;
            }
            set
            {
                this._duty = value;
            }
        }
        public double DutyPercentage
        {
            get
            {
                return this._dutyPercentage;
            }
            set
            {
                this._dutyPercentage = value;
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
        public int SweepCounter
        {
            get
            {
                return this._sweepCounter;
            }
            set
            {
                this._sweepCounter = value;
            }
        }
        public bool SweepEnable
        {
            get
            {
                return this._sweepEnable;
            }
            set
            {
                this._sweepEnable = value;
            }
        }
        public bool SweepNegate
        {
            get
            {
                return this._sweepNegate;
            }
            set
            {
                this._sweepNegate = value;
            }
        }
        public int SweepRate
        {
            get
            {
                return this._sweepRate;
            }
            set
            {
                this._sweepRate = value;
            }
        }
    }
}