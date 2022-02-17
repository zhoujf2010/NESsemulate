package myNESsemulate.computer;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;

public class JoyPad2 implements IJoyPad
{
    @Override
    public void Init(JFrame frame) {

        Button btn0 = new Button();
        btn0.setLabel("Exit");
        btn0.setLocation(341 + 20, 20);
        btn0.setSize(80, 40);
        btn0.setBackground(Color.blue);
        btn0.setForeground(Color.black);

        btn0.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Exited from button");
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });

        frame.add(btn0);

        Button btn = new Button();
        btn.setLabel("Select");
        btn.setLocation(341 + 20, 80);
        btn.setSize(80, 40);
        btn.setBackground(Color.blue);
        btn.addMouseListener(new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                joypaddata1 = joypaddata1 | 0x04;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                joypaddata1 = joypaddata1 ^ 0x04;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        frame.add(btn);

        Button btn2 = new Button();
        btn2.setLabel("Start");
        btn2.setLocation(341 + 20, 140);
        btn2.setSize(80, 40);
        btn2.setBackground(Color.blue);
        btn2.addMouseListener(new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                joypaddata1 = joypaddata1 | 0x08;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                joypaddata1 = joypaddata1 ^ 0x08;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        frame.add(btn2);

        Thread thread = new Thread()
        {
            public void run() {
                try {
                    ScanKey2();
                    //                    while(true) {
                    //                        Thread.sleep(2);
                    //                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    private static final int PIN_BUTTON = 26; // PIN 18 = BCM 24

    public void ScanKey() {
        Context pi4j = Pi4J.newAutoContext();

        DigitalInputConfigBuilder buttonConfig = DigitalInput.newConfigBuilder(pi4j).id("button").name("Press button")
                .address(PIN_BUTTON).pull(PullResistance.PULL_UP).debounce(3000L).provider("pigpio-digital-input");
        DigitalInput button = pi4j.create(buttonConfig);

        DigitalState oldstate = button.state();
        while (true) {
            try {
                if (oldstate != button.state()) {
                    System.out.println(button.state());
                    oldstate = button.state();
                    if (button.state() == DigitalState.LOW) {
                        joypaddata1 = joypaddata1 ^ 0x01;
                    }

                    if (button.state() == DigitalState.HIGH) {
                        joypaddata1 = joypaddata1 | 0x01;
                    }
                }
                Thread.sleep(1);
            }
            catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void ScanKey2() throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"python3", "getKey.py" };
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        boolean up = false;
        boolean down = false;
        boolean right = false;
        boolean left = false;
        boolean A = false;
        boolean B = false;
        while ((s = stdInput.readLine()) != null) {
            String[] segs = s.split(" ");
            if (segs.length == 6) {
                //                System.out.println(s);
                int x = Integer.parseInt(segs[0]);
                int y = Integer.parseInt(segs[1]);

                if (y == 255) { //up
                    joypaddata1 = joypaddata1 | 0x10;
                    up = true;
                }
                else if (y == 0) { //down 
                    joypaddata1 = joypaddata1 | 0x20;
                    down = true;
                }
                else {
                    if (up) {
                        joypaddata1 = joypaddata1 ^ 0x10;
                        up = false;
                    }
                    if (down) {
                        joypaddata1 = joypaddata1 ^ 0x20;
                        down = false;
                    }
                }

                if (x == 255) { //right
                    joypaddata1 = joypaddata1 | 0x80;
                    right = true;
                }
                else if (x == 0) { //left 
                    joypaddata1 = joypaddata1 | 0x40;
                    left = true;
                }
                else {
                    if (right) {
                        joypaddata1 = joypaddata1 ^ 0x80;
                        right = false;
                    }
                    if (left) {
                        joypaddata1 = joypaddata1 ^ 0x40;
                        left = false;
                    }
                }

                if (segs[2].equals("0")) {
                    joypaddata1 = joypaddata1 | 0x01;
                    A = true;
                }
                else if (segs[2].equals("1")) {
                    if (A) {
                        joypaddata1 = joypaddata1 ^ 0x01;
                        A = false;
                    }
                }

                if (segs[3].equals("0")) {
                    joypaddata1 = joypaddata1 | 0x02;
                    B = true;
                }
                else if (segs[3].equals("1")) {
                    if (B) {
                        joypaddata1 = joypaddata1 ^ 0x02;
                        B = false;
                    }
                }

                //                if (segs[4].equals("1"))
                //                    joypaddata1 = joypaddata1 | 0x04;
                //                else if (segs[4].equals("0"))
                //                    joypaddata1 = joypaddata1 ^ 0x04;
                //
                //                if (segs[5].equals("1"))
                //                    joypaddata1 = joypaddata1 | 0x08;
                //                else if (segs[5].equals("0"))
                //                    joypaddata1 = joypaddata1 ^ 0x08;

                //                                System.out.println(joypaddata1 + "  " + joypaddata2);
            }
        }

        // Read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    private int JoyData1 = 0;
    public int joypaddata1 = 0;
    private int JoyData2 = 0;
    public int joypaddata2 = 0;
    private byte JoyStrobe = 0;

    @Override
    public byte Read(int address) {
        if (address == 0x4016) {
            byte num2 = (byte) (JoyData1 & 1);
            JoyData1 = JoyData1 >> 1;
            return num2;
        }
        else if (address == 0x4017) {
            byte num2 = (byte) (JoyData2 & 1);
            JoyData2 = JoyData2 >> 1;
            return num2;
        }
        return 0;
    }

    @Override
    public void Write(int address, byte data) {
        if (address == 0x4016) {
            if ((this.JoyStrobe == 1) && ((data & 1) == 0)) {
                this.JoyData1 = joypaddata1 | 0x100;
                this.JoyData2 = joypaddata2 | 0x200;
            }
            this.JoyStrobe = (byte) (data & 1);
        }
    }

}
