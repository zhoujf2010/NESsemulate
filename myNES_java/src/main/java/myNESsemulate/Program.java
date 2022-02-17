package myNESsemulate;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
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
            rompath = "src\\main\\resources\\rom.nes";
        if (!new File(rompath).exists())
            rompath = "/opt/NES/rom.nes";

        final MotherBoard pc = new MotherBoard(); // 定义PC设备

        // 加载ROM文件
        byte[] rom = getContentFromSystem(rompath);
        pc.card = rom;
        pc.Init();
        System.out.println("rom:" + rom.length);

        JFrame frame = new JFrame("NES 模拟器");
        frame.setLayout(null);
        frame.setResizable(false); //大小固定不能调整
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 设置窗体属性
        
        //windows上尺寸
//        frame.setSize(640+10, 600 + 15);
        
        //raspberry尺寸
      frame.setSize(480 , 320);
      frame.setUndecorated(true);//无边框
        
        
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
                double dx = mainPanel.getSize().getWidth()/256;
                double dy = mainPanel.getSize().getHeight() / 240;
                double dm = Math.min(dx, dy);
                if (img != null)
                    ((Graphics2D) g).drawRenderedImage(img, AffineTransform.getScaleInstance(dm,dm));
            }
        };
        
        mainPanel.setLocation(0, 0);
        Dimension sz = frame.getSize();
        int height = sz.height;
        if (!frame.isUndecorated())
            height -= 10; //减去顶上的标题栏高度
        int width = (int)(height / 240.0 * 256);
        mainPanel.setSize(width, height);
        frame.add(mainPanel);
        
        // 监听按健，把信息送出模拟PC机中
        pc.joypad.Init(frame);

        // 刷新一帖画页，更新显示图片
        pc.SetFrameRefresh((e) -> {
            mainPanel.repaint();
        });

        // 刷新title，计算出每秒帧数
        new Timer(1000, (e) -> {
            frame.setTitle("NES 模拟器  " + pc.getFrameCount() + " fps");
        }).start();
        

        //全屏控制
//      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//      GraphicsDevice vc = env.getDefaultScreenDevice();
//      vc.setFullScreenWindow(frame);
        
        frame.setVisible(true);// 显示窗体
        
        System.out.println(frame.getSize());
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
