package myNESsemulate.computer.Channel;

public class APUChannel
{

    // Fields
    protected boolean Enabled;
    protected int Envelope;
    protected int EnvelopeCounter;
    protected boolean EnvelopeDecayDisable;
    protected int LengthCounter;
    protected boolean LengthCounterDisable;
    protected double SampleCount;
    protected double _samplingRate;
    protected int Volume;
    protected int Wavelength;
    protected boolean _waveStatus;
    protected int ClockSpeed = 1790000;

    // Methods
    public APUChannel(double samplingRate) {
        this.setVolume(6);
        this._samplingRate = samplingRate;
    }

    public void DecrementLengthCounter() {
        if (!this.getLengthCounterDisable()) {
            this.setLengthCounter(this.getLengthCounter() - 1);
        }
        if ((this.getLengthCounter() <= 0) || !this.getEnabled()) {
            this.setLengthCounter(0);
            // LengthCounterDisable = true;
        }
    }

    public int RenderSample() {
        return 0;
    };

    public void UpdateEnvelope() {
    };

    public void UpdateLinearCounter() {
    };

    public void UpdateSweep() {
    };

    public void WriteReg1(byte b) {
    };

    public void WriteReg2(byte b) {
    };

    public void WriteReg3(byte b) {
    };

    public void WriteReg4(byte b) {
    };

    // Properties
    public boolean getEnabled() {
        return this.Enabled;
    }

    public void setEnabled(boolean value) {
        this.Enabled = value;
    }

    public int getEnvelope() {
        return this.Envelope;
    }

    public void setEnvelope(int value) {
        if (value < -1)
            System.out.print("");
        this.Envelope = value;
    }

    public int getEnvelopeCounter() {
        return this.EnvelopeCounter;
    }

    public void setEnvelopeCounter(int value) {
        if (value < -1)
            System.out.print("");
        this.EnvelopeCounter = value;
    }

    public boolean getEnvelopeDecayDisable() {
        return this.EnvelopeDecayDisable;
    }

    public void setEnvelopeDecayDisable(boolean value) {
        this.EnvelopeDecayDisable = value;
    }

    public int getLengthCounter() {
        return this.LengthCounter;
    }

    public void setLengthCounter(int value) {
        if (value < -1)
            System.out.print("");
        this.LengthCounter = value;
    }

    public boolean getLengthCounterDisable() {
        return this.LengthCounterDisable;
    }

    public void setLengthCounterDisable(boolean value) {
        this.LengthCounterDisable = value;
    }

    public double getSampleCount() {
        return this.SampleCount;
    }

    public void setSampleCount(double value) {
        this.SampleCount = value;
    }

    public int getVolume() {
        return this.Volume;
    }

    public void setVolume(int value) {
        if (value < -1)
            System.out.print("");
        this.Volume = value;
    }

    public int getWavelength() {
        return this.Wavelength;
    }

    public void setWavelength(int value) {
        if (value < -1)
            System.out.print("");
        this.Wavelength = value;
    }

    public boolean getWaveStatus() {
        return this._waveStatus;
    }

    public void setWaveStatus(boolean value) {
        this._waveStatus = value;
    }

    public int getClockSpeed() {
        return this.ClockSpeed;
    }

    public void setClockSpeed(int value) {
        if (value < -1)
            System.out.print("");
        this.ClockSpeed = value;
    }
}
