package NESDemo.Channel;

import NESDemo.MotherBoard;

public class Channel_DMC extends APUChannel
{
    // Fields
    private byte _dac;
    private int _dacCounter;
    /*
     * private static int[] _dmcWavelengths = new int[] { 0x1ac, 380, 340, 320,
     * 0x11e, 0xfe, 0xe2, 0xd6, 190, 160, 0x8e, 0x80, 0x6a, 0x54, 0x48, 0x36 };
     */
    private static int[] _dmcWavelengths = new int[] {0xD60, 0xBE0, 0xAA0, 0xA00, 0x8F0, 0x7F0, 0x710, 0x6B0, 0x5F0,
            0x500, 0x470, 0x400, 0x350, 0x2A8, 0x240, 0x1B0 };
    private double _frequency;
    private int _initialAddress;
    private int _initialLength;
    private boolean _irqEnable;
    private boolean _loop;
    private double _renderedWavelength;
    private int _sampleAddress;
    private int _sampleLength;
    private int _shift;
    MotherBoard _Nes;

    // Methods
    public Channel_DMC(double samplingRate, MotherBoard NesEmu) {
        super(samplingRate);

        _Nes = NesEmu;
    }

    public long RenderSample() {
        if (super.getEnabled()) {
            super.setSampleCount(super.getSampleCount()+1);
            if (super.getSampleCount() > this._renderedWavelength) {
                super.setSampleCount(super.getSampleCount()- this._renderedWavelength);
                if ((this.getSampleLength() > 0) && (this._shift == 0)) {
                    int num2 = this.getSampleAddress();
                    this.setSampleAddress((int) (num2  + 1));
                    this.setDAC(_Nes.ReadBus8(num2));  //ReadMemory8
                    this.setSampleLength(this.getSampleLength()-1);
                    this._shift = 8;
                    if (this.getLoop() && (this.getSampleLength() <= 0)) {
                        this.setSampleLength( this._initialLength);
                        this.setSampleAddress (this._initialAddress);
                    }
                }
                if (this.getSampleLength() > 0) {
                    if (this.getDAC() != 0) {
                        int num = this.getDAC() & 1;
                        if ((num == 0) && (this._dacCounter > 1)) {
                            this._dacCounter -= 2;
                        }
                        else if ((num != 0) && (this._dacCounter < 0x7e)) {
                            this._dacCounter += 2;
                        }
                    }
                    this._dacCounter--;
                    if (this._dacCounter <= 0) {
                        this._dacCounter = 8;
                    }
                    this.setDAC((byte) (this.getDAC() >> 1));
                    this._shift--;
                }
            }
        }
        return (int) (this._dacCounter * 0x30);
    }

    public void UpdateEnvelope() {
    }

    private void UpdateFrequency() {
        this._frequency = super.getClockSpeed() / (super.getWavelength() + 1);
        this._renderedWavelength = super._samplingRate / this._frequency;
    }

    public void UpdateLinearCounter() {
    }

    public void UpdateSweep() {
    }

    public void WriteReg1(byte b) {
        this.setIRQEnable((b & 0x80) != 0);
        this.setLoop((b & 0x40) != 0);
        super.setWavelength(_dmcWavelengths[b & 15]);
        this.UpdateFrequency();
    }

    public void WriteReg2(byte b) {
        this.setDAC((byte) (b & 0x7f));
        this._shift = 8;
    }

    public void WriteReg3(byte b) {
        this.setSampleAddress((int) ((b * 0x40) + 0xc000));
        this._initialAddress = this.getSampleAddress();
    }

    public void WriteReg4(byte b) {
        this.setSampleLength((b * 0x10) + 1);
        this._initialLength = this.getSampleLength();
    }

    public byte getDAC() {
        return this._dac;
    }

    public void setDAC(byte value) {
        this._dac = value;
    }

    public boolean getIRQEnable() {
        return this._irqEnable;
    }

    public void setIRQEnable(boolean value) {
        this._irqEnable = value;
    }

    public boolean getLoop() {
        return this._loop;
    }

    public void setLoop(boolean value) {
        this._loop = value;
    }

    public int getSampleAddress() {
        return this._sampleAddress;
    }

    public void setSampleAddress(int value) {
        this._sampleAddress = value;
    }

    public int getSampleLength() {
        return this._sampleLength;
    }

    public void setSampleLength(int value) {
        this._sampleLength = value;
    }
}
