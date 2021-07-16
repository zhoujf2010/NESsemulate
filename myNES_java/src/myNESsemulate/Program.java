package myNESsemulate;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import myNESsemulate.computer.MotherBoard;

/**
 * NES模拟器程序
 * 
 * @作者 Jeffrey Zhou
 * @version [V1.0, 2017年2月27日]
 */
public class Program
{
    static JPanel mainPanel = null;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        
        
        String rompath = "rom.nes";
        if (!new File(rompath).exists())
            rompath = "src\\rom.nes";
        if (!new File(rompath).exists())
            rompath = "/opt/NES/rom.nes";

        final MotherBoard pc = new MotherBoard(); // 定义PC设备

        // 加载ROM文件
        byte[] rom = getContentFromSystem(rompath);
        pc.loadRom(rom);

        JFrame frame = new JFrame("NES 模拟器");

        // 设置窗体属性
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(512 + 16, 448 + 16 + 25);
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = ((int) screensize.getWidth() - frame.getWidth()) / 2;
        int y = ((int) screensize.getHeight() - frame.getHeight()) / 2;
        frame.setLocation(x, y);

        // 主Panel，用于显示图形画页
        mainPanel = new JPanel()
        {
            private static final long serialVersionUID = 8017932155640452457L;

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                RenderedImage img = pc.getRenderedImage();
                if (img != null)
                    ((Graphics2D) g).drawRenderedImage(img, AffineTransform.getScaleInstance(2.0, 2.0));
            }
        };
        mainPanel.setLocation(10, 10);
        mainPanel.setSize(frame.getWidth() - 10 - 25, frame.getHeight() - 25 - 35);
        frame.add(mainPanel);

        // 监听按健，把信息送出模拟PC机中
        frame.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                pc.joypaddata1 = ReadKey1(e.getKeyCode(), pc.joypaddata1, (x, y) -> {
                    return x | y;
                });
                pc.joypaddata2 = ReadKey2(e.getKeyCode(), pc.joypaddata2, (x, y) -> {
                    return x | y;
                });
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pc.joypaddata1 = ReadKey1(e.getKeyCode(), pc.joypaddata1, (x, y) -> {
                    return x ^ y;
                });
                pc.joypaddata2 = ReadKey2(e.getKeyCode(), pc.joypaddata2, (x, y) -> {
                    return x ^ y;
                });
            }
        });

        if ("Linux".equals(System.getProperty("os.name")))
            ReadFormJoypadLinux(pc);

        // 刷新一帖画页，更新显示图片
        pc.SetFrameRefresh((e) -> {
            mainPanel.repaint();
        });

        // 刷新title，计算出每秒帧数
        new Timer(1000, (e) -> {
            frame.setTitle("NES 模拟器  " + pc.getFrameCount() + " fps");
        }).start();

        frame.setVisible(true);// 显示窗体
        pc.run(); // 开机
    }

    private static void ReadFormJoypadLinux(MotherBoard pc) throws FileNotFoundException {

        FileInputStream inputStream = new FileInputStream("/dev/input/js0");

        new Thread()
        {
            public void run() {
                try {
                    byte[] buffer = new byte[8];
                    int b;
                    String result = "";
                    System.out.println("Read from device ");
                    short oldmount = 0;
                    while ((b = inputStream.read(buffer)) > 0) {
                        if (b < 8)
                            System.out.println("Only read " + b + " bytes from F710. Ignoring and continuing.");
                        else {
                            long time = ((((((buffer[3] & 0x00000000000000ff) << 8) | (buffer[2] & 0x00ff)) << 8)
                                    | (buffer[1] & 0x00ff)) << 8) | (buffer[0] & 0x00ff);
                            short amount = (short) (((buffer[5] & 0x00ff) << 8) | (buffer[4] & 0x00ff));
                            short controlType = buffer[6]; // buffer[6]==1 => button, buffer[6]==2 => joystick
                            short control = buffer[7];
//                            System.err
//                                    .println("got HID event time " + time + " controlType " + controlType + " control "
//                                            + control + " amount " + amount + " b5=" + buffer[5] + "b4=" + buffer[4]);

//                            System.out.println("====1>" + pc.joypaddata1);
                            if (amount != 0) {
                                pc.joypaddata1 = ReadKey1_joypad(controlType, control, amount, pc.joypaddata1,
                                        (x, y) -> {
                                            return x | y;
                                        });
                                oldmount = amount;
                            }
                            else {
                                pc.joypaddata1 = ReadKey1_joypad(controlType, control, amount, pc.joypaddata1,
                                        (x, y) -> {
                                            return x & ~y;//x ^ y;
                                        });
                            }
//                            System.out.println("====2>" + pc.joypaddata1);
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("F710.run failure. Exiting read thread." + e);
                }
            }
        }.start();
    }

    private static int ReadKey1_joypad(short controlType, short control, short amount, int num, ICalc calc) {
        if (controlType == 1 && control == 0) // A (健盘K)
            num = calc.CalcNum(num, 0x01);
        else if (controlType == 1 && control == 3) // B (健盘J)
            num = calc.CalcNum(num, 0x02);
        else if (controlType == 1 && control == 8) // Select (健盘V)
            num = calc.CalcNum(num, 0x04);
        else if (controlType == 1 && control == 9) // Select (健盘B)
            num = calc.CalcNum(num, 0x08);
        else if (controlType == 2 && control == 1 && amount < 0) // Up (健盘W)
            num = calc.CalcNum(num, 0x10);
        else if (controlType == 2 && control == 1 && amount > 0) // Down (健盘S)
            num = calc.CalcNum(num, 0x20);
        else if (controlType == 2 && control == 0 && amount < 0) // Left (健盘A)
            num = calc.CalcNum(num, 0x40);
        else if (controlType == 2 && control == 0 && amount > 0) // Right (健盘D)
            num = calc.CalcNum(num, 0x80);
        else if (controlType == 2 && amount == 0) { // 方向归0
            num = calc.CalcNum(num, 0x10);
            num = calc.CalcNum(num, 0x20);
            num = calc.CalcNum(num, 0x40);
            num = calc.CalcNum(num, 0x80);
        }
        return num;
    }

    interface ICalc
    {
        public int CalcNum(int num1, int num2);
    }

    private static int ReadKey1(int key, int num, ICalc calc) {
        if (key == 75) // A (健盘K)
            num = calc.CalcNum(num, 0x01);
        else if (key == 74) // B (健盘J)
            num = calc.CalcNum(num, 0x02);
        else if (key == 86) // Select (健盘V)
            num = calc.CalcNum(num, 0x04);
        else if (key == 66) // Select (健盘B)
            num = calc.CalcNum(num, 0x08);
        else if (key == 87) // Up (健盘W)
            num = calc.CalcNum(num, 0x10);
        else if (key == 83) // Down (健盘S)
            num = calc.CalcNum(num, 0x20);
        else if (key == 65) // Left (健盘A)
            num = calc.CalcNum(num, 0x40);
        else if (key == 68) // Right (健盘D)
            num = calc.CalcNum(num, 0x80);
        return num;
    }

    private static int ReadKey2(int key, int num, ICalc calc) {
        if (key == 59) // A (健盘;)
            num = calc.CalcNum(num, 0x01);
        else if (key == 222) // B (健盘')
            num = calc.CalcNum(num, 0x02);
        else if (key == 86) // Select (健盘V)
            num = calc.CalcNum(num, 0x04);
        else if (key == 66) // Select (健盘B)
            num = calc.CalcNum(num, 0x08);
        else if (key == 38) // Up (健盘上)
            num = calc.CalcNum(num, 0x10);
        else if (key == 40) // Down (健盘下)
            num = calc.CalcNum(num, 0x20);
        else if (key == 37) // Left (健盘左)
            num = calc.CalcNum(num, 0x40);
        else if (key == 39) // Right (健盘右)
            num = calc.CalcNum(num, 0x80);
        return num;
    }

    private static byte[] getContentFromSystem(String filePath) {
        byte[] content = null;
        File newfile = new File(filePath);
        if (newfile.exists()) {
            // 打开一个输入流
            InputStream fis = null;
            try {
                fis = new FileInputStream(newfile);
                content = getContentFromInputStream(fis);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    private static byte[] getContentFromInputStream(InputStream fis) {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            byte[] buffer = new byte[1024 * 4];
            // 打开一个输出流
            baos = new ByteArrayOutputStream();
            // 记录读到缓冲buffer中的字节长度
            int ch = 0;
            while ((ch = fis.read(buffer)) != -1) {
                // 因为有可能出现ch与buffer的length不一致的问题,所以用下面的写法
                baos.write(buffer, 0, ch);
            }
            content = baos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fis != null)
                    fis.close();
                if (baos != null)
                    baos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }
}
