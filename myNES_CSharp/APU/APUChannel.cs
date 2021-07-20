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
    public abstract class APUChannel
    {
        // Fields
        private bool _enabled;
        private int _envelope;
        private int _envelopeCounter;
        private bool _envelopeDecay;
        private int _lengthCounter;
        private bool _lengthCounterDisable;
        private double _sampleCount;
        protected double _samplingRate;
        private int _volume;
        private int _wavelength;
        private bool _waveStatus;
        private int _ClockSpeed = 1790000;
        // Methods
        public APUChannel(double samplingRate)
        {
            this.Volume = 6;
            this._samplingRate = samplingRate;
        }
        public void DecrementLengthCounter()
        {
            if (!this.LengthCounterDisable)
            {
                this.LengthCounter--;
            }
            if ((this.LengthCounter <= 0) || !this.Enabled)
            {
                this.LengthCounter = 0;
                //LengthCounterDisable = true;
            }
        }
        public abstract ushort RenderSample();
        public abstract void UpdateEnvelope();
        public abstract void UpdateLinearCounter();
        public abstract void UpdateSweep();
        public abstract void WriteReg1(byte b);
        public abstract void WriteReg2(byte b);
        public abstract void WriteReg3(byte b);
        public abstract void WriteReg4(byte b);
        // Properties
        public bool Enabled
        {
            get
            {
                return this._enabled;
            }
            set
            {
                this._enabled = value;
            }
        }
        public int Envelope
        {
            get
            {
                return this._envelope;
            }
            set
            {
                this._envelope = value;
            }
        }
        public int EnvelopeCounter
        {
            get
            {
                return this._envelopeCounter;
            }
            set
            {
                this._envelopeCounter = value;
            }
        }
        public bool EnvelopeDecayDisable
        {
            get
            {
                return this._envelopeDecay;
            }
            set
            {
                this._envelopeDecay = value;
            }
        }
        public int LengthCounter
        {
            get
            {
                return this._lengthCounter;
            }
            set
            {
                if (value < 0)
                    System.Console.WriteLine();
                this._lengthCounter = value;
            }
        }
        public bool LengthCounterDisable
        {
            get
            {
                return this._lengthCounterDisable;
            }
            set
            {
                this._lengthCounterDisable = value;
            }
        }
        public double SampleCount
        {
            get
            {
                return this._sampleCount;
            }
            set
            {
                this._sampleCount = value;
            }
        }
        public int Volume
        {
            get
            {
                return this._volume;
            }
            set
            {
                this._volume = value;
            }
        }
        public int Wavelength
        {
            get
            {
                return this._wavelength;
            }
            set
            {
                this._wavelength = value;
            }
        }
        public bool WaveStatus
        {
            get
            {
                return this._waveStatus;
            }
            set
            {
                this._waveStatus = value;
            }
        }
        public int ClockSpeed
        { get { return _ClockSpeed; } set { _ClockSpeed = value; } }
    }
}
