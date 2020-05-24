package myNESsemulate.computer;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

/**
 * 模拟器主板
 * 
 * @作者 Jeffrey Zhou
 */
public class MotherBoard
{
    public MotherBoard() {
        fix_scrolloffset1 = false;
        fix_scrolloffset2 = false;
        fix_scrolloffset3 = false;

        cpu = new CPU(this);
        ppu = new VideoCard(this);
        apu = new AudioCard();
        memory = new Memory();
    }

    private CPU cpu;
    private VideoCard ppu;
    private Memory memory;
    public AudioCard apu;

    private int JoyData1 = 0;
    public int joypaddata1 = 0;
    private int JoyData2 = 0;
    public int joypaddata2 = 0;
    
    private byte JoyStrobe = 0;
   // public byte[] saveRam;
    public boolean fix_scrolloffset1;
    public boolean fix_scrolloffset2;
    public boolean fix_scrolloffset3;

    public int Ticks_Per_Scanline = 113;// 113 for NTSC, 106 for PAL
    public boolean isQuitting = false;
    public boolean isPaused = false;

    public MIRRORING mirroring;
    public int mirroringBase; // For one screen mirroring

    public enum MIRRORING
    {
        HORIZONTAL, VERTICAL, FOUR_SCREEN, ONE_SCREEN
    };

    // 加载ROM文件
    public void loadRom(byte[] rom) {

        // 将程序段读入内存中
        int prg_roms = rom[4];// 16kB ROM的数目
        for (int i = 0; i < 1024 * 16 * prg_roms; i++)
            memory.WriteMemory8(i + 0x8000, rom[i + 16]);

        // 将显示数据读入显存中
        int chr_roms = rom[5];// 8kB VROM的数目
        ppu.chr_roms = new byte[1024 * 8 * chr_roms];
        for (int i = 0; i < 1024 * 8 * chr_roms; i++) {
            ppu.chr_roms[i] = rom[i + 1024 * 16 * prg_roms + 16];
        }

        if ((rom[6] & 0x1) == 0x0)
            mirroring = MIRRORING.HORIZONTAL;
        else
            mirroring = MIRRORING.VERTICAL;

        int offset = 0x8000 + 4096 * 7;
        if ((memory.ReadMemory8(0xfe0 + offset) == 'B') && (memory.ReadMemory8(0xfe1 + offset) == 'B')
                && (memory.ReadMemory8(0xfe2 + offset) == '4') && (memory.ReadMemory8(0xfe3 + offset) == '7')
                && (memory.ReadMemory8(0xfe4 + offset) == '9') && (memory.ReadMemory8(0xfe5 + offset) == '5')
                && (memory.ReadMemory8(0xfe6 + offset) == '6') && (memory.ReadMemory8(0xfe7 + offset) == '-')
                && (memory.ReadMemory8(0xfe8 + offset) == '1') && (memory.ReadMemory8(0xfe9 + offset) == '5')
                && (memory.ReadMemory8(0xfea + offset) == '4') && (memory.ReadMemory8(0xfeb + offset) == '4')
                && (memory.ReadMemory8(0xfec + offset) == '0')) {
            fix_scrolloffset1 = true;
        }
        offset = 0x8000;
        if ((memory.ReadMemory8(0x9 + offset) == 0xfc) && (memory.ReadMemory8(0xa + offset) == 0xfc)
                && (memory.ReadMemory8(0xb + offset) == 0xfc) && (memory.ReadMemory8(0xc + offset) == 0x40)
                && (memory.ReadMemory8(0xd + offset) == 0x40) && (memory.ReadMemory8(0xe + offset) == 0x40)
                && (memory.ReadMemory8(0xf + offset) == 0x40)) {
            fix_scrolloffset2 = true;
        }
        if ((memory.ReadMemory8(0x75 + offset) == 0x11) && (memory.ReadMemory8(0x76 + offset) == 0x12)
                && (memory.ReadMemory8(0x77 + offset) == 0x13) && (memory.ReadMemory8(0x78 + offset) == 0x14)
                && (memory.ReadMemory8(0x79 + offset) == 0x07) && (memory.ReadMemory8(0x7a + offset) == 0x03)
                && (memory.ReadMemory8(0x7b + offset) == 0x03) && (memory.ReadMemory8(0x7c + offset) == 0x03)
                && (memory.ReadMemory8(0x7d + offset) == 0x03)) {
            fix_scrolloffset3 = true;
        }
    }

    // 开机运行
    public void run() throws InterruptedException {
        cpu.pc_register = memory.ReadMemory16(0xFFFC);//读取程序启动点
        cpu.RunProcessor();
    }

    // #region 总线数据读写
    public byte ReadBus8(int address) {
        if (address == 0x2002)
            return ppu.Status_Register_Read();
        else if (address == 0x2004)
            return ppu.SpriteRam_IO_Register_Read();
        else if (address == 0x2007)
            return ppu.VRAM_IO_Register_Read();
        else if (address == 0x4015)
            return apu.ReadStatusReg();
        else if (address == 0x4016) {
            byte num2 = (byte) (JoyData1 & 1);
            JoyData1 = JoyData1 >> 1;
            return num2;
        }
        else if (address == 0x4017) {
            byte num2 = (byte) (JoyData2 & 1);
            JoyData2 = JoyData2 >> 1;
            return num2;
        }
        else
            return memory.ReadMemory8(address);
    }

    public int ReadBus16(int address) {
        return memory.ReadMemory16(address);
    }

    public byte WriteBus8(int address, byte data) {
        if (address == 0x2000)
            ppu.Control_Register_1_Write(data);
        else if (address == 0x2001)
            ppu.Control_Register_2_Write(data);
        else if (address == 0x2003)
            ppu.SpriteRam_Address_Register_Write(data);
        else if (address == 0x2004)
            ppu.SpriteRam_IO_Register_Write(data);
        else if (address == 0x2005)
            ppu.VRAM_Address_Register_1_Write(data);
        else if (address == 0x2006)
            ppu.VRAM_Address_Register_2_Write(data);
        else if (address == 0x2007)
            ppu.VRAM_IO_Register_Write(data);
        else if (address == 0x4000)
            apu.WriteRectReg1(0, data);
        else if (address == 0x4001)
            apu.WriteRectReg2(0, data);
        else if (address == 0x4002)
            apu.WriteRectReg3(0, data);
        else if (address == 0x4003)
            apu.WriteRectReg4(0, data);
        else if (address == 0x4004)
            apu.WriteRectReg1(1, data);
        else if (address == 0x4005)
            apu.WriteRectReg2(1, data);
        else if (address == 0x4006)
            apu.WriteRectReg3(1, data);
        else if (address == 0x4007)
            apu.WriteRectReg4(1, data);
        else if (address == 0x4008)
            apu.WriteTriReg1(data);
        else if (address == 0x4009)
            apu.WriteTriReg2(data);
        else if (address == 0x400A)
            apu.WriteTriReg3(data);
        else if (address == 0x400B)
            apu.WriteTriReg4(data);
        else if (address == 0x400C)
            apu.WriteNoiseReg1(data);
        else if (address == 0x400E)
            apu.WriteNoiseReg2(data);
        else if (address == 0x400F)
            apu.WriteNoiseReg3(data);
        else if (address == 0x4010)
            apu.WriteDMCReg1(data);
        else if (address == 0x4011)
            apu.WriteDMCReg2(data);
        else if (address == 0x4012)
            apu.WriteDMCReg3(data);
        else if (address == 0x4013)
            apu.WriteDMCReg4(data);
        else if (address == 0x4014)
            ppu.SpriteRam_DMA_Begin(data);
        else if (address == 0x4015)
            apu.WriteStatusReg(data);
        else if (address == 0x4016) {
            if ((this.JoyStrobe == 1) && ((data & 1) == 0)) {
                // _inputManager.Update();
                this.JoyData1 = joypaddata1 | 0x100;
                this.JoyData2 = joypaddata2 | 0x200;
            }
            this.JoyStrobe = (byte) (data & 1);
        }
        else
            memory.WriteMemory8(address, data);
        return 1;
    }
    // #endregion

    // #region 视频展示

    private short[] newa = null;
    private int framecount = 0;

    // 输出视频扫描线
    public boolean RenderNextScanline() {
        return this.ppu.RenderNextScanline();
    }

    // 输出一帧视频
    public void RenderFrame(short[] offscreenBuffer) {
        newa = new short[offscreenBuffer.length - 2048];
        for (int i = 0; i < newa.length; i++)
            newa[i] = offscreenBuffer[i + 2048];
        if (frameRefresh != null) // 通知外部取出该帧数据
            frameRefresh.actionPerformed(null);
        framecount++;
    }

    private ActionListener frameRefresh;

    // 设置帧数据输出监听者
    public void SetFrameRefresh(ActionListener frameRefresh) {
        this.frameRefresh = frameRefresh;
    }

    // 获取每秒的帧数
    public int getFrameCount() {
        int ret = framecount;
        framecount = 0;
        return ret;
    }

    // 取得一帧显示图象对象
    public RenderedImage getRenderedImage() {
        if (newa != null) {
            int width = 256;
            int height = 224;
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
            WritableRaster raster = bi.getRaster();
            raster.setDataElements(0, 0, width, height, newa);
            return bi;
        }
        return null;
    }
    // #endregion

    public void RendSound(){
        //if (_NesEmu.SoundEnabled) {
        this.apu.Render(cpu.total_cycles);
    }
}
