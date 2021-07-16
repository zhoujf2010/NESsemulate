package NESDemo.Channel;

//////////////////////////////////////////////////////////////////////////////
//This file is part of My Nes                                               //
//A Nintendo Entertainment System Emulator.                                 //
////
//Copyright © 2009 Ala Hadid (AHD)                                          //
////
//My Nes is free software; you can redistribute it and/or modify            //
//it under the terms of the GNU General Public License as published by      //
//the Free Software Foundation; either version 2 of the License, or         //
//(at your option) any later version.                                       //
////
//My Nes is distributed in the hope that it will be useful,                 //
//but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//GNU General Public License for more details.                              //
////
//You should have received a copy of the GNU General Public License         //
//along with this program; if not, write to the Free Software               //
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA//
//////////////////////////////////////////////////////////////////////////////
public class Channel_Square extends APUChannel
{
    // Fields
    private int _duty;
    private double _dutyPercentage;
    private double _frequency;
    private boolean _isFirstChannel;
    /*
     * private static byte[] _lengthValues = new byte[] { 5, 0x7f, 10, 1, 20, 2,
     * 40, 3, 80, 4, 30, 5, 7, 6, 13, 7, 6, 8, 12, 9, 0x18, 10, 0x30, 11, 0x60,
     * 12, 0x24, 13, 8, 14, 0x10, 15 };
     */
    private static byte[] _lengthValues = new byte[] {0x5 * 2, (byte) (0x7f * 2), 0xA * 2, 0x1 * 2, 0x14 * 2, 0x2 * 2,
            0x28 * 2, 0x3 * 2, (byte) (0x50 * 2), 0x4 * 2, 0x1E * 2, 0x5 * 2, 0x7 * 2, 0x6 * 2, 0x0E * 2, 0x7 * 2,
            0x6 * 2, 0x08 * 2, 0xC * 2, 0x9 * 2, 0x18 * 2, 0xa * 2, 0x30 * 2, 0xb * 2, (byte) (0x60 * 2), 0xc * 2,
            0x24 * 2, 0xd * 2, 0x8 * 2, 0xe * 2, 0x10 * 2, 0xf * 2 };
    private double _renderedWavelength;
    private int _rightShift;
    private int _sweepCounter;
    private boolean _sweepEnable;
    private boolean _sweepNegate;
    private int _sweepRate;

    // Methods
    public Channel_Square(double samplingRate, boolean firstChannel) {
        super(samplingRate);
        this._isFirstChannel = firstChannel;
    }

    public long RenderSample() {
        if (super.getLengthCounter() > 0) {
            super.setSampleCount(super.getSampleCount() + 1);// .getSampleCount++;
            if (super.getWaveStatus()
                    && (super.getSampleCount() > (this._renderedWavelength * this.getDutyPercentage()))) {
                super.setSampleCount(super.getSampleCount() - this._renderedWavelength * this.getDutyPercentage());
                super.setWaveStatus(!super.getWaveStatus());
            }
            else if (!super.getWaveStatus()
                    && (super.getSampleCount() > (this._renderedWavelength * (1.0 - this.getDutyPercentage())))) {
                super.setSampleCount(
                        super.getSampleCount() - this._renderedWavelength * (1.0 - this.getDutyPercentage()));
                super.setWaveStatus(!super.getWaveStatus());
            }
            if (super.getWaveStatus()) {
                return 0;
            }
            if (!super.getEnvelopeDecayDisable()) {
                return (long) (0x40 * super.getEnvelope());
            }
            return (long) (0x40 * super.getVolume());
        }
        return 0;
        /*
         * if (super.getWaveStatus) { return 0; } if
         * (!super.getEnvelopeDecayDisable) { return (ushort)(0x40 *
         * super.getEnvelope); } return (ushort)(0x40 * super.getVolume);
         */
    }

    public void UpdateEnvelope() {
        super.setEnvelopeCounter(super.getEnvelopeCounter() - 1);
        if (super.getEnvelopeCounter() < 0) {
            super.setEnvelopeCounter(0);
        }
        if ((!super.getEnvelopeDecayDisable() && (super.getEnvelope() > 0)) && (super.getEnvelopeCounter() == 0)) {
            super.setEnvelope(super.getEnvelope() - 1);
            super.setEnvelopeCounter(super.getVolume());
        }
        if (super.getLengthCounterDisable() && (super.getEnvelope() == 0)) {
            super.setEnvelope(15);
        }
    }

    private void UpdateFrequency() {
        this._frequency = super.getClockSpeed() / ((super.getWavelength() + 1) * 0x10); // 0x1b5030
                                                                                        // /
                                                                                        // ((super.getWavelength
                                                                                        // +
                                                                                        // 1)
                                                                                        // *
                                                                                        // 0x10);
        this._renderedWavelength = super._samplingRate / this._frequency;
    }

    public void UpdateLinearCounter() {
    }

    public void UpdateSweep() {
        int num = 0;
        this.setSweepCounter(this.getSweepCounter() - 1);
        if (this.getSweepCounter() < 0) {
            this.setSweepCounter(0);
        }
        if ((this.getSweepEnable() && (this.getSweepCounter() <= 0))
                && ((this.getRightShift() != 0) && (super.getLengthCounter() > 0))) {
            this.setSweepCounter(this.getSweepRate());
            if (super.getWavelength() >= 8) {
                num = super.getWavelength() >> this.getRightShift();
                if (this.getSweepNegate()) {
                    num = -num;
                    if (!this._isFirstChannel) {
                        num--;
                    }
                }
                num += super.getWavelength();
                if ((num < 0x800) && (num > 8)) {
                    super.setWavelength(num);
                    this.UpdateFrequency();
                }
                else {
                    this.setSweepEnable(false);
                }
            }
        }
    }

    public void WriteReg1(byte b) {
        super.setEnvelopeCounter(b & 15);
        super.setVolume(b & 15);
        super.setEnvelopeCounter(super.getEnvelopeCounter() + 1);
        super.setEnvelopeDecayDisable((b & 0x10) != 0);
        super.setLengthCounterDisable((b & 0x20) != 0);
        this.setDuty((b & 0xc0) >> 6);
        switch (this.getDuty()) {
            case 0:
                this.setDutyPercentage(0.125);
                return;

            case 1:
                this.setDutyPercentage(0.25);
                return;

            case 2:
                this.setDutyPercentage(0.5);
                return;

            case 3:
                this.setDutyPercentage(0.75);
                return;
        }
    }

    public void WriteReg2(byte b) {
        this.setRightShift(b & 7);
        this.setSweepNegate((b & 8) != 0);
        this.setSweepCounter(((b & 0x70) >> 4) + 1);
        this.setSweepRate(((b & 0x70) >> 4) + 1);
        this.setSweepEnable((b & 0x80) != 0);
    }

    public void WriteReg3(byte b) {
        super.setWavelength((super.getWavelength() & 0x700) | b);
        this.UpdateFrequency();
    }

    public void WriteReg4(byte b) {
        super.setWavelength((super.getWavelength() & 0xff) | ((b & 7) << 8));
        this.UpdateFrequency();
        super.setLengthCounter((_lengthValues[(b & 0xf8) >> 3])&0xff);
        if (!super.getEnvelopeDecayDisable()) {
            super.setEnvelope(15);
        }
    }

    public int getDuty() {
        return this._duty;
    }

    public void setDuty(int value) {
        this._duty = value;
    }

    public double getDutyPercentage() {
        return this._dutyPercentage;
    }

    public void setDutyPercentage(double value) {
        this._dutyPercentage = value;
    }

    public int getRightShift() {
        return this._rightShift;
    }

    public void setRightShift(int value) {
        this._rightShift = value;
    }

    public int getSweepCounter() {
        return this._sweepCounter;
    }

    public void setSweepCounter(int value) {
        this._sweepCounter = value;
    }

    public boolean getSweepEnable() {
        return this._sweepEnable;
    }

    public void setSweepEnable(boolean value) {
        this._sweepEnable = value;
    }

    public boolean getSweepNegate() {
        return this._sweepNegate;
    }

    public void setSweepNegate(boolean value) {
        this._sweepNegate = value;
    }

    public int getSweepRate() {
        return this._sweepRate;
    }

    public void setSweepRate(int value) {
        this._sweepRate = value;
    }
}
