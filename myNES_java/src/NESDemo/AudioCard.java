package NESDemo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import NESDemo.Channel.APUChannel;
import NESDemo.Channel.Channel_DMC;
import NESDemo.Channel.Channel_Noise;
import NESDemo.Channel.Channel_Square;
import NESDemo.Channel.Channel_Triangle;

public class AudioCard
{
    MotherBoard mb;
    // Fields
    private int _bitsPerSample = 0x10;
    // private SecondarySoundBuffer _buffer;
    private int _bufferLength = 5;
    private int _bufferSize;
    private boolean[] _channelEnabled;
    private int _dataPosition;
    private boolean _firstRender = true;
    private int _frameCounter;
    public long _frameCycles = 29780;
    public long _frameElapsedCycles;
    private int _lastPosition;
    public boolean _palTiming;
    public double SampleRate = 44100.0;
    private APUChannel[] _soundChannels;
    private byte[] _soundData;
    // private DirectSound _soundDevice;
    boolean IsPaused = false;
    int _MasterVolume = 7;
    long k = 0;
    // Methods

    public AudioCard(MotherBoard motherBoard) {
        this.mb = motherBoard;


        this.InitDirectSound();
        this.Reset();
        
        SetClockSpeed(1789772);
        _frameCycles = 29606;//262 * 113
        _palTiming = false;
    }
    private void ClearBuffer() {
        for (int i = 0; i < this._bufferSize; i++) {
            this._soundData[i] = 0;
        }
        // this._buffer.Write(this._soundData, 0, LockFlags.EntireBuffer);
    }

    private void InitDirectSound() {
        // this._soundDevice = new DirectSound();
        // this._soundDevice.SetCooperativeLevel(parent.Parent.Handle,
        // CooperativeLevel.Priority);
        float rate = 44100;
        int sampleSize = 16;
        boolean bigEndian = true;
        int channels = 1;
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
         AudioFormat  wfx = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
        // wfx.FormatTag = WaveFormatTag.Pcm;
        // wfx.BitsPerSample = 0x10; //16
        // wfx.Channels = 1;
        // wfx.SamplesPerSecond = 0xac44;//44100
        // wfx.AverageBytesPerSecond = ((wfx.BitsPerSample / 8) * wfx.Channels)
        // * wfx.SamplesPerSecond;
        // wfx.BlockAlignment = 2;
        this._bufferSize = (int) (rate * sampleSize /8 * channels * 5);//wfx.AverageBytesPerSecond * 5;
        // SoundBufferDescription desc = new SoundBufferDescription();
        // desc.Format = wfx;
        // desc.SizeInBytes = this._bufferSize = wfx.AverageBytesPerSecond * 5;
        // desc.Flags = BufferFlags.GlobalFocus | BufferFlags.Software;
        // this._buffer = new SecondarySoundBuffer(this._soundDevice, desc);
         this._soundData = new byte[this._bufferSize];
         this.ClearBuffer();
        // this._buffer.Play(0, PlayFlags.Looping);
         
      // 设置数据输入
         DataLine.Info info = new DataLine.Info(SourceDataLine.class, wfx);

         try {
             auline = (SourceDataLine) AudioSystem.getLine(info);
             auline.open(wfx);
         } catch (Exception e) {
             e.printStackTrace();
             return;
         }
         auline.start();
//         // 清空数据缓冲,并关闭输入
//         auline.drain();
//         auline.close();
         new Thread() {                             //1.创建一个Thread的匿名类
             public void run() {                     //2.重写run方法 
                     PlayBuffer();
             }
         }.start();  
         
    }
    SourceDataLine auline = null;

    public void Pause() {
        // if ((this._buffer != null) && !this._buffer.Disposed)
        // {
        // this._buffer.Stop(); IsPaused = true;
        // }
    }

    public void Play() {
        // if (!IsPaused)
        // { return; }
        // if ((this._buffer != null) && !this._buffer.Disposed)
        // {
        // IsPaused = false;
        // this._buffer.Play(0, PlayFlags.Looping);
        // }
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
    
    int CurrentWritePosition = 0;
    
    public void PlayBuffer() {

        int rate = 44100;
        int sampleSize = 16;
        boolean bigEndian = true;
        int channels = 1;
        int second = rate * sampleSize /8 * channels;
        while(true) {
            
            if (CurrentWritePosition + second >_soundData.length)
                CurrentWritePosition = 0;
            auline.write(_soundData, CurrentWritePosition, second);  //播放
        }
    }

    public void Render(long cycles) {
        if (IsPaused)
        { return; }
        int writePosition = 0;
        this.UpdateRegisters(cycles);
        //try
        //{
        writePosition = CurrentWritePosition;
        //writePosition = this._buffer.CurrentWritePosition;//
        //}
        //catch { return; }
        if (this._firstRender)
        {
            this._firstRender = false;
            this._dataPosition = CurrentWritePosition + 0x1000;
            this._lastPosition = CurrentWritePosition;
        }
        int num2 = writePosition - this._lastPosition;
        if (num2 < 0)
        {
            num2 = (this._bufferSize - this._lastPosition) + writePosition;
        }
        int rate = 44100;
        int sampleSize = 16;
        boolean bigEndian = true;
        int second = rate * sampleSize /8 * 1;
        num2 = second / 60;
        if (num2 != 0)
        {
            for (int i = 0; i < num2; i += 2)
            {
                int num4 = 0;
                for (int j = 0; j < 5; j++)
                {
                    if (this._channelEnabled[j])
                    {
                        num4 = (int)(num4 + this._soundChannels[j].RenderSample());
                    }
                }
                num4 = (int)(num4 * _MasterVolume);
                this._soundData[this._dataPosition + 1] = (byte)((int)((num4 & 0xff00) >> 8));
                this._soundData[this._dataPosition] = (byte)(num4 & 0xff);
                this._dataPosition += 2;
                this._dataPosition = this._dataPosition % this._bufferSize;
            }
            //this._buffer.Write(this._soundData, 0, LockFlags.None);
            this._lastPosition = writePosition;
        }
//        auline.write(_soundData, 0, _soundData.length);  //播放
//        System.out.print(1);
    }
    

    private void UpdateRegisters(long cycles)
    {
        if ((cycles % _frameCycles) > mb.tick_count)
        {
            if (_palTiming)
                k = 4;
            else
                k = 3;
        }
        else
        {
            if (_palTiming)
                k = 3;
            else
                k = 2;
        }
        //Channel_Noise TR = (Channel_Noise)_soundChannels[3];
        //_Nes.myVideo.DrawText(TR.LengthCounter.ToString(), 1);
        while (k > 0)
        {
            this._frameCounter++;
            int num = this._palTiming ? (this._frameCounter % 5) : (this._frameCounter % 4);
            for (int i = 0; i < 5; i++)
            {
                APUChannel base2 = this._soundChannels[i];
                base2.UpdateEnvelope();
                base2.UpdateLinearCounter();
                switch (num)
                {
                    case 1:
                        base2.DecrementLengthCounter();
                        base2.UpdateSweep();
                        break;
                    case 3:
                        base2.DecrementLengthCounter();
                        base2.UpdateSweep();
                        break;
                }
            } k--;
        }
        mb.total_cycles = 0;
    }
    public void Reset()
    {
        //this._soundChannels = new APUChannel[] { new Channel_Square(44100.0, true), new Channel_Square(44100.0, false), new Channel_Triangle(44100.0), new Channel_Noise(44100.0), new Channel_DMC(44100.0, _Nes) };
        this._soundChannels = new APUChannel[] 
        { 
            new Channel_Square(SampleRate, true), //0
            new Channel_Square(SampleRate, false),//1
            new Channel_Triangle(SampleRate),     //2
            new Channel_Noise(SampleRate),        //3
            new Channel_DMC(SampleRate, mb)     //4
        };
        this._channelEnabled = new boolean[5];
        for (int i = 0; i < 5; i++)
        {
            this._channelEnabled[i] = true;
        }
    }
    public void Shutdown()
    {
        //if ((this._buffer != null) && !this._buffer.Disposed)
        //{
        //    this._buffer.Stop(); IsPaused = true;
        //}
        //this._buffer.Dispose();
        //this._soundDevice.Dispose();
    }
    public void WriteDMCReg1(byte b)
    {
        this._soundChannels[4].WriteReg1(b);
    }
    public void WriteDMCReg2(byte b)
    {
        this._soundChannels[4].WriteReg2(b);
    }
    public void WriteDMCReg3(byte b)
    {
        this._soundChannels[4].WriteReg3(b);
    }
    public void WriteDMCReg4(byte b)
    {
        this._soundChannels[4].WriteReg4(b);
    }
    public void WriteNoiseReg1(byte b)
    {
        this._soundChannels[3].WriteReg1(b);
    }
    public void WriteNoiseReg2(byte b)
    {
        this._soundChannels[3].WriteReg2(b);
    }
    public void WriteNoiseReg3(byte b)
    {
        this._soundChannels[3].WriteReg3(b);
    }
    public void WriteRectReg1(int c, byte b)
    {
        this._soundChannels[c].WriteReg1(b);
    }
    public void WriteRectReg2(int c, byte b)
    {
        this._soundChannels[c].WriteReg2(b);
    }
    public void WriteRectReg3(int c, byte b)
    {
        this._soundChannels[c].WriteReg3(b);
    }
    public void WriteRectReg4(int c, byte b)
    {
        this._soundChannels[c].WriteReg4(b);
    }
    public void WriteStatusReg(byte b)
    {
        this._soundChannels[0].setEnabled( (b & 1) != 0);
        this._soundChannels[1].setEnabled(  (b & 2) != 0);
        this._soundChannels[2].setEnabled(  (b & 4) != 0);
        this._soundChannels[3].setEnabled(  (b & 8) != 0);
        this._soundChannels[4].setEnabled(  (b & 0x10) != 0);

    }
    public void WriteTriReg1(byte b)
    {
        this._soundChannels[2].WriteReg1(b);
    }
    public void WriteTriReg2(byte b)
    {
        this._soundChannels[2].WriteReg2(b);
    }
    public void WriteTriReg3(byte b)
    {
        this._soundChannels[2].WriteReg3(b);
    }
    public void WriteTriReg4(byte b)
    {
        this._soundChannels[2].WriteReg4(b);
    }
    public void Write4017(byte b)
    {
        //IRQ = ((b & 0x40) == 0);
        //_frameCounter = 0;
        //_palTiming = ((b & 0x80) != 0);
    }
    public void SetClockSpeed(int ClockSpeed)
    {
        for (int j = 0; j < 5; j++)
        {
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
