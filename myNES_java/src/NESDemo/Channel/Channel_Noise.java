package NESDemo.Channel;

public class Channel_Noise extends APUChannel
{
    // Fields
    private double _frequency;
    private static byte[] _lengthValues = new byte[] {0x5 * 2, (byte) (0x7f * 2), 0xA * 2, 0x1 * 2, 0x14 * 2, 0x2 * 2,
            0x28 * 2, 0x3 * 2, (byte) (0x50 * 2), 0x4 * 2, 0x1E * 2, 0x5 * 2, 0x7 * 2, 0x6 * 2, 0x0E * 2, 0x7 * 2,
            0x6 * 2, 0x08 * 2, 0xC * 2, 0x9 * 2, 0x18 * 2, 0xa * 2, 0x30 * 2, 0xb * 2, (byte) (0x60 * 2), 0xc * 2,
            0x24 * 2, 0xd * 2, 0x8 * 2, 0xe * 2, 0x10 * 2, 0xf * 2 };
    private boolean _noiseMode;
    private int _noiseShiftData;
    private static int[] _noiseWavelengths = new int[] {0x2, 0x4, 0x8, 0x10, 0x20, 0x30, 0x40, 0x50, 0x65, 0x7f, 0xbe,
            0xfe, 0x17D, 0x1fc, 0x3f9, 0x7f2 };
    private double _renderedWavelength;

    // Methods
    public Channel_Noise(double samplingRate) {
        super(samplingRate);

        this._noiseShiftData = 1;
    }

    public long RenderSample() {
        if (super.getLengthCounter() > 0 & this.getEnabled()) {
            super.setSampleCount(super.getSampleCount() + 1);
            if (super.getSampleCount() >= this._renderedWavelength) {
                int num2;
                super.setSampleCount(super.getSampleCount() - this._renderedWavelength);
                int num = (this.getNoiseShiftData() & 0x4000) >> 14;// Bit14
                if (this.getNoiseMode()) {
                    num2 = (this.getNoiseShiftData() & 0x0100) >> 8;// Bit8
                }
                else {
                    num2 = (this.getNoiseShiftData() & 0x2000) >> 13;// Bit13
                }
                this.setNoiseShiftData(this.getNoiseShiftData() << 1);
                this.setNoiseShiftData(this.getNoiseShiftData() | (num ^ num2) & 1);
            }
            int num3 = (this.getNoiseShiftData() & 1) * 0x20;
            return (long) (num3 * (super.getEnvelopeDecayDisable() ? super.getVolume() : super.getEnvelope()));
        }
        return 0;
        // int num3 = (this.NoiseShiftData & 1) * 0x20;
        // return (ushort)(num3 * (super.getEnvelopeDecayDisable ?
        // super.getVolume :
        // super.getEnvelope ));
    }

    public void UpdateEnvelope() {
        super.setEnvelopeCounter(super.getEnvelopeCounter() - 1);
        if ((!super.getEnvelopeDecayDisable() && (super.getEnvelope() > 0)) && (super.getEnvelopeCounter() == 0)) {
            super.setEnvelope(super.getEnvelope() - 1);
            super.setEnvelopeCounter(super.getVolume());
        }
        if (super.getLengthCounterDisable() && (super.getEnvelope() == 0)) {
            super.setEnvelope(15);
        }

    }

    private void UpdateFrequency() {
        this._frequency = super.getClockSpeed() / ((super.getWavelength() + 1) * 0x10);
        this._renderedWavelength = super._samplingRate / this._frequency;
    }

    public void UpdateLinearCounter() {
    }

    public void UpdateSweep() {
    }

    public void WriteReg1(byte b) {
        super.setEnvelopeCounter(b & 0xF);
        super.setVolume(b & 0xF);
        super.setEnvelopeDecayDisable((b & 0x10) != 0);
        super.setLengthCounterDisable((b & 0x20) != 0);
    }

    public void WriteReg2(byte b) {
        super.setWavelength(_noiseWavelengths[b & 15]);
        this.UpdateFrequency();
        this.setNoiseMode((b & 0x80) != 0);
    }

    public void WriteReg3(byte b) {
        super.setLengthCounter(_lengthValues[(b & 0xF8) >> 3]);
    }

    public void WriteReg4(byte b) {
    }

    public boolean getNoiseMode() {
        return this._noiseMode;
    }

    public void setNoiseMode(boolean value) {
        this._noiseMode = value;
    }

    public int getNoiseShiftData() {
        return this._noiseShiftData;
    }

    public void setNoiseShiftData(int value) {
        this._noiseShiftData = value;
    }
}
