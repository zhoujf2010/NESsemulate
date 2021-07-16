package NESDemo.Channel;

public class APUChannel
{

    // Fields
    private boolean _enabled;
    private int _envelope;
    private int _envelopeCounter;
    private boolean _envelopeDecay;
    private int _lengthCounter;
    private boolean _lengthCounterDisable;
    private double _sampleCount;
    protected double _samplingRate;
    private int _volume;
    private int _wavelength;
    private boolean _waveStatus;
    private int _ClockSpeed = 1790000;

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

    public long RenderSample() {
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
        return this._enabled;
    }

    public void setEnabled(boolean value) {
        this._enabled = value;
    }

    public int getEnvelope() {
        return this._envelope;
    }

    public void setEnvelope(int value) {
        this._envelope = value;
    }

    public int getEnvelopeCounter() {
        return this._envelopeCounter;
    }

    public void setEnvelopeCounter(int value) {
        this._envelopeCounter = value;
    }

    public boolean getEnvelopeDecayDisable() {
        return this._envelopeDecay;
    }

    public void setEnvelopeDecayDisable(boolean value) {
        this._envelopeDecay = value;
    }

    public int getLengthCounter() {
        return this._lengthCounter;
    }

    public void setLengthCounter(int value) {
        this._lengthCounter = value;
    }

    public boolean getLengthCounterDisable() {
        return this._lengthCounterDisable;
    }

    public void setLengthCounterDisable(boolean value) {
        this._lengthCounterDisable = value;
    }

    public double getSampleCount() {
        return this._sampleCount;
    }

    public void setSampleCount(double value) {
        this._sampleCount = value;
    }

    public int getVolume() {
        return this._volume;
    }

    public void setVolume(int value) {
        this._volume = value;
    }

    public int getWavelength() {
        return this._wavelength;
    }

    public void setWavelength(int value) {
        this._wavelength = value;
    }

    public boolean getWaveStatus() {
        return this._waveStatus;
    }

    public void setWaveStatus(boolean value) {
        this._waveStatus = value;
    }

    public int getClockSpeed() {
        return this._ClockSpeed;
    }

    public void setClockSpeed(int value) {
        this._ClockSpeed = value;
    }
}
