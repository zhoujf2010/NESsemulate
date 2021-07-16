package NESDemo;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MotherBoard
{
    private CPU cpu = null;
    private VideoCard ppu;
    public Memory memory;
    private AudioCard apu;
    public JoyPad joypad;
    public byte[] card; //游戏卡带

    public void Init() {
        cpu = new CPU(this);
        ppu = new VideoCard(this);
        apu = new AudioCard(this);
        memory = new Memory();
        joypad = new JoyPad();
    }

    public void start() {
        cpu.pc_register = cpu.ReadBus16(0xFFFC);//读取程序启动点
        cpu.RunProcessor();
    }

    public byte ReadBus8(int address) {
        //从硬盘、内存、游戏手柄、显卡、声卡读
        if (address >= 0x2000 && address <= 0x2007)
            return ppu.Read(address);
        else if (address >= 0x4000 && address <= 0x4015)
            return apu.Read(address);
        else if (address >= 0x4016 && address <= 0x4017)
            return joypad.Read(address);
        else if (address >= 0x8000)
            return card[address - 0x8000 + 16];
        else
            return memory.ReadMemory8(address);
    }

    public void WriteBus8(int address, byte data) {
        //写入硬盘、内存、游戏手柄、显卡、声卡
        if (address >= 0x2000 && address <= 0x2007)
            ppu.Write(address, data);
        else if (address == 0x4014)
            ppu.Write(address, data);
        else if (address >= 0x4000 && address <= 0x4013)
            apu.Write(address, data);
        else if (address == 0x4015)
            apu.Write(address, data);
        else if (address >= 0x4016 && address <= 0x4017)
            joypad.Write(address, data);
        else
            memory.WriteMemory8(address, data);
    }

    public int Ticks_Per_Scanline = 113;// 113 for NTSC, 106 for PAL
    
    public long tick_count=0;
    public long total_cycles = 0;

    public long CPUTick(long tick_count) {
        this.tick_count = tick_count;
        total_cycles += tick_count;
        apu.Play();
        if (tick_count >= 113) {// 113 for NTSC, 106 for PAL
            if (ppu.RenderNextScanline()) {
                cpu.Push16(cpu.pc_register);
                cpu.PushStatus();
                cpu.pc_register = cpu.ReadBus16(0xFFFA);

                RenderFrame(ppu.offscreenBuffer);
                //                mb.RendSound();
                apu.Render(tick_count);
            }
            tick_count = tick_count - 113;
        }
        return tick_count;
    }

    private ActionListener frameRefresh;

    // 设置帧数据输出监听者
    public void SetFrameRefresh(ActionListener frameRefresh) {
        this.frameRefresh = frameRefresh;
    }

    public BufferedImage bi = null;// 取得一帧显示图象对象
    private int framecount = 0;
    private double _lastFrameTime = 0;
    private double FramePeriod = 0.01667 * 1000;// 60 FPS
    
    public void RenderFrame(short[] offscreenBuffer) {
        framecount++;
        
      // 卡住，让快速的CPU慢下来，保证60 fps
      while (true) {
          if ((System.currentTimeMillis() - _lastFrameTime) >= FramePeriod)
              break;
      }
      _lastFrameTime = System.currentTimeMillis();

        int width = 256;
        int height = 240;
        if (bi == null)
            bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
        WritableRaster raster = bi.getRaster();
        raster.setDataElements(0, 0, width, height, offscreenBuffer);

        if (frameRefresh != null) // 通知外部取出该帧数据
            frameRefresh.actionPerformed(null);
        

//        File outputfile = new File("image.jpg");
//        try {
//            ImageIO.write(bi, "jpg", outputfile);
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    // 获取每秒的帧数
    public int getFrameCount() {
        int ret = framecount;
        framecount = 0;
        return ret;
    }
}
