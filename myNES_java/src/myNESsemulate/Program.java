package myNESsemulate;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
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
        pc.card = rom;
//        pc.loadRom(rom);
        pc.Init();

        JFrame frame = new JFrame("NES 模拟器");

        // 设置窗体属性
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050 + 16, 1000 + 16 + 25);
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
                RenderedImage img = pc.bi;
                if (img != null)
                    ((Graphics2D) g).drawRenderedImage(img, AffineTransform.getScaleInstance(4.0, 4.0));
            }
        };
        mainPanel.setLocation(10, 10);
        mainPanel.setSize(frame.getWidth() - 10 - 25, frame.getHeight() - 25 - 35);
        frame.add(mainPanel);

        // 监听按健，把信息送出模拟PC机中
        frame.addKeyListener(pc.joypad);


        // 刷新一帖画页，更新显示图片
        pc.SetFrameRefresh((e) -> {
            mainPanel.repaint();
        });

        // 刷新title，计算出每秒帧数
        new Timer(1000, (e) -> {
            frame.setTitle("NES 模拟器  " + pc.getFrameCount() + " fps");
        }).start();

        frame.setVisible(true);// 显示窗体
        pc.start(); // 开机
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
