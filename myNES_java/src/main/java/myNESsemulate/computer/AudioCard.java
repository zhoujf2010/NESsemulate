package myNESsemulate.computer;

import myNESsemulate.computer.Channel.APUChannel;
import myNESsemulate.computer.Channel.Channel_DMC;
import myNESsemulate.computer.Channel.Channel_Noise;
import myNESsemulate.computer.Channel.Channel_Square;
import myNESsemulate.computer.Channel.Channel_Triangle;

public class AudioCard
{
    MotherBoard mb;
    private boolean[] _channelEnabled;
    private int _frameCounter;
    public long _frameCycles = 29780;
    public long _frameElapsedCycles;
    public boolean _palTiming;
    public double SampleRate = 44100.0;
    private APUChannel[] _soundChannels;
    boolean IsPaused = false;
    int _MasterVolume = 7;
    long k = 0;

    public AudioCard(MotherBoard motherBoard) {
        this.mb = motherBoard;

        this.Reset();

        SetClockSpeed(1789772);
        _frameCycles = 29606;// 262 * 113
        _palTiming = false;
    }

    public byte ReadStatusReg() {
        int num = 0;
        num |= this._soundChannels[0].getEnabled() ? 1 : 0;
        num |= this._soundChannels[1].getEnabled() ? 2 : 0;
        num |= this._soundChannels[2].getEnabled() ? 4 : 0;
        num |= this._soundChannels[3].getEnabled() ? 8 : 0;
        num |= this._soundChannels[4].getEnabled() ? 0x10 : 0;
        return (byte) num;
    }

    public byte[] Render(long cycles) {
        this.UpdateRegisters(cycles);
        
        int rate = 44100;
        int sampleSize = 16;
        int second = rate * sampleSize / 8;
        int len = second / 60;
        byte[] dt = new byte[len];
        for (int i = 0; i < len / 2; i += 2) {
            int tmp= 0;
            for (int j = 0; j < 5; j++) {
                if (this._channelEnabled[j]) {
                    tmp = (int) (tmp+ this._soundChannels[j].RenderSample());
                }
            }
            tmp= tmp* _MasterVolume;
            dt[i * 2] = (byte) (tmp & 0xff);
            dt[i * 2 + 1] = (byte) ((tmp & 0xff00) >> 8);
        }
        return dt;
    }

    private void UpdateRegisters(long cycles) {
        if ((cycles % _frameCycles) > mb.tick_count) {
            if (_palTiming)
                k = 4;
            else
                k = 3;
        }
        else {
            if (_palTiming)
                k = 3;
            else
                k = 2;
        }
        while (k > 0) {
            this._frameCounter++;
            int num = this._palTiming ? (this._frameCounter % 5) : (this._frameCounter % 4);
            for (int i = 0; i < 5; i++) {
                APUChannel base2 = this._soundChannels[i];
                base2.UpdateEnvelope();
                base2.UpdateLinearCounter();
                switch (num) {
                    case 1:
                        base2.DecrementLengthCounter();
                        base2.UpdateSweep();
                        break;
                    case 3:
                        base2.DecrementLengthCounter();
                        base2.UpdateSweep();
                        break;
                }
            }
            k--;
        }
        mb.total_cycles = 0;
    }

    public void Reset() {
        this._soundChannels = new APUChannel[] {new Channel_Square(SampleRate, true), // 0
                new Channel_Square(SampleRate, false), // 1
                new Channel_Triangle(SampleRate), // 2
                new Channel_Noise(SampleRate), // 3
                new Channel_DMC(SampleRate, mb) // 4
        };
        this._channelEnabled = new boolean[5];
        for (int i = 0; i < 5; i++) {
            this._channelEnabled[i] = true;
        }
    }

    public void WriteDMCReg1(byte b) {
        this._soundChannels[4].WriteReg1(b);
    }

    public void WriteDMCReg2(byte b) {
        this._soundChannels[4].WriteReg2(b);
    }

    public void WriteDMCReg3(byte b) {
        this._soundChannels[4].WriteReg3(b);
    }

    public void WriteDMCReg4(byte b) {
        this._soundChannels[4].WriteReg4(b);
    }

    public void WriteNoiseReg1(byte b) {
        this._soundChannels[3].WriteReg1(b);
    }

    public void WriteNoiseReg2(byte b) {
        this._soundChannels[3].WriteReg2(b);
    }

    public void WriteNoiseReg3(byte b) {
        this._soundChannels[3].WriteReg3(b);
    }

    public void WriteRectReg1(int c, byte b) {
        this._soundChannels[c].WriteReg1(b);
    }

    public void WriteRectReg2(int c, byte b) {
        this._soundChannels[c].WriteReg2(b);
    }

    public void WriteRectReg3(int c, byte b) {
        this._soundChannels[c].WriteReg3(b);
    }

    public void WriteRectReg4(int c, byte b) {
        this._soundChannels[c].WriteReg4(b);
    }

    public void WriteStatusReg(byte b) {
        this._soundChannels[0].setEnabled((b & 1) != 0);
        this._soundChannels[1].setEnabled((b & 2) != 0);
        this._soundChannels[2].setEnabled((b & 4) != 0);
        this._soundChannels[3].setEnabled((b & 8) != 0);
        this._soundChannels[4].setEnabled((b & 0x10) != 0);

    }

    public void WriteTriReg1(byte b) {
        this._soundChannels[2].WriteReg1(b);
    }

    public void WriteTriReg2(byte b) {
        this._soundChannels[2].WriteReg2(b);
    }

    public void WriteTriReg3(byte b) {
        this._soundChannels[2].WriteReg3(b);
    }

    public void WriteTriReg4(byte b) {
        this._soundChannels[2].WriteReg4(b);
    }

    public void Write4017(byte b) {
        // IRQ = ((b & 0x40) == 0);
        // _frameCounter = 0;
        // _palTiming = ((b & 0x80) != 0);
    }

    public void SetClockSpeed(int ClockSpeed) {
        for (int j = 0; j < 5; j++) {
            _soundChannels[j].setClockSpeed(ClockSpeed);
        }
    }

    // Properties
    public boolean getDMCEnabled() {
        return this._channelEnabled[4];
    }

    public void setDMCEnabled(boolean value) {
        this._channelEnabled[4] = value;
    }

    public boolean getNoiseEnabled() {
        return this._channelEnabled[3];
    }

    public void setNoiseEnabled(boolean value) {
        this._channelEnabled[3] = value;
    }

    public boolean getSquareWave1Enabled() {
        return this._channelEnabled[0];
    }

    public void setSquareWave1Enabled(boolean value) {
        this._channelEnabled[0] = value;
    }

    public boolean getSquareWave2Enabled() {
        return this._channelEnabled[1];
    }

    public void setSquareWave2Enabled(boolean value) {
        this._channelEnabled[1] = value;
    }

    public boolean getTriangleEnabled() {
        return this._channelEnabled[2];
    }

    public void setTriangleEnabled(boolean value) {
        this._channelEnabled[2] = value;
    }

    public int getMasterVolume() {
        return this._MasterVolume;
    }

    public void setMasterVolume(int value) {
        this._MasterVolume = value;
    }

    public byte Read(int address) {
        if (address == 0x4015)
            return ReadStatusReg();
        return 0;
    }

    public void Write(int address, byte data) {
        if (address == 0x4000)
            WriteRectReg1(0, data);
        else if (address == 0x4001)
            WriteRectReg2(0, data);
        else if (address == 0x4002)
            WriteRectReg3(0, data);
        else if (address == 0x4003)
            WriteRectReg4(0, data);
        else if (address == 0x4004)
            WriteRectReg1(1, data);
        else if (address == 0x4005)
            WriteRectReg2(1, data);
        else if (address == 0x4006)
            WriteRectReg3(1, data);
        else if (address == 0x4007)
            WriteRectReg4(1, data);
        else if (address == 0x4008)
            WriteTriReg1(data);
        else if (address == 0x4009)
            WriteTriReg2(data);
        else if (address == 0x400A)
            WriteTriReg3(data);
        else if (address == 0x400B)
            WriteTriReg4(data);
        else if (address == 0x400C)
            WriteNoiseReg1(data);
        else if (address == 0x400E)
            WriteNoiseReg2(data);
        else if (address == 0x400F)
            WriteNoiseReg3(data);
        else if (address == 0x4010)
            WriteDMCReg1(data);
        else if (address == 0x4011)
            WriteDMCReg2(data);
        else if (address == 0x4012)
            WriteDMCReg3(data);
        else if (address == 0x4013)
            WriteDMCReg4(data);
        else if (address == 0x4015)
            WriteStatusReg(data);
    }
}
