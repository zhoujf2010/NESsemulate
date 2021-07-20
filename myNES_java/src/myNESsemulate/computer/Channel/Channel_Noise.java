package myNESsemulate.computer.Channel;

public class Channel_Noise extends APUChannel
{
    // Fields
    private double _frequency;
    private static byte[] _lengthValues = new byte[] {0x5 * 2, (byte) (0x7f * 2), 0xA * 2, 0x1 * 2, 0x14 * 2, 0x2 * 2,
            0x28 * 2, 0x3 * 2, (byte) (0x50 * 2), 0x4 * 2, 0x1E * 2, 0x5 * 2, 0x7 * 2, 0x6 * 2, 0x0E * 2, 0x7 * 2,
            0x6 * 2, 0x08 * 2, 0xC * 2, 0x9 * 2, 0x18 * 2, 0xa * 2, 0x30 * 2, 0xb * 2, (byte) (0x60 * 2), 0xc * 2,
            0x24 * 2, 0xd * 2, 0x8 * 2, 0xe * 2, 0x10 * 2, 0xf * 2 };
    private boolean NoiseMode;
    private int NoiseShiftData;
    private static int[] _noiseWavelengths = new int[] {0x2, 0x4, 0x8, 0x10, 0x20, 0x30, 0x40, 0x50, 0x65, 0x7f, 0xbe,
            0xfe, 0x17D, 0x1fc, 0x3f9, 0x7f2 };
    private double _renderedWavelength;

    // Methods
    public Channel_Noise(double samplingRate) {
        super(samplingRate);

        this.NoiseShiftData = 1;
    }

    public int RenderSample() {
//        if (super.LengthCounter > 0 & this.Enabled)
//        {
//            super.SampleCount++;
//            if (super.SampleCount >= this._renderedWavelength)
//            {
//                int num2;
//                super.SampleCount -= this._renderedWavelength;
//                int num = (this.NoiseShiftData & 0x4000) >> 14;//Bit14
//                if (this.NoiseMode)
//                {
//                    num2 = (this.NoiseShiftData & 0x0100) >> 8;//Bit8
//                }
//                else
//                {
//                    num2 = (this.NoiseShiftData & 0x2000) >> 13;//Bit13
//                }
//                this.NoiseShiftData = this.NoiseShiftData << 1;
//                this.NoiseShiftData |= (num ^ num2) & 1;
//            }
//            int num3 = (this.NoiseShiftData & 1) * 0x20;
//            return (int)(num3 * (super.EnvelopeDecayDisable ? super.Volume : super.Envelope));
//        }
        return 0;
        // int num3 = (this.NoiseShiftData & 1) * 0x20;
        // return (ushort)(num3 * (super.getEnvelopeDecayDisable ?
        // super.getVolume :
        // super.getEnvelope ));
    }

    public void UpdateEnvelope() {
        super.EnvelopeCounter--;
        if ((!super.EnvelopeDecayDisable && (super.Envelope > 0)) && (super.EnvelopeCounter == 0))
        {
            super.Envelope--;
            super.EnvelopeCounter = super.Volume;
        }
        if (super.LengthCounterDisable && (super.Envelope == 0))
        {
            super.Envelope = 15;
        }
    }

    private void UpdateFrequency() {
        this._frequency = super.ClockSpeed / ((super.Wavelength + 1) * 0x10);
        this._renderedWavelength = super._samplingRate / this._frequency;
    }

    public void UpdateLinearCounter() {
    }

    public void UpdateSweep() {
    }

    public void WriteReg1(byte b)
    {
        super.Volume = super.EnvelopeCounter = b & 0xF;
        super.EnvelopeDecayDisable = (b & 0x10) != 0;
        super.LengthCounterDisable = (b & 0x20) != 0;
    }
    public void WriteReg2(byte b)
    {
        super.Wavelength = _noiseWavelengths[b & 15];
        this.UpdateFrequency();
        this.NoiseMode = (b & 0x80) != 0;
    }
    public void WriteReg3(byte b)
    {
        super.LengthCounter = _lengthValues[(b & 0xF8) >> 3];
    }
    public void WriteReg4(byte b)
    {
    }
    
    public boolean getNoiseMode() {
        return this.NoiseMode;
    }

    public void setNoiseMode(boolean value) {
        this.NoiseMode = value;
    }

    public int getNoiseShiftData() {
        return this.NoiseShiftData;
    }

    public void setNoiseShiftData(int value) {
        if (value < -1)
            System.out.print("");
        this.NoiseShiftData = value;
    }
}
