package myNESsemulate.computer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class JoyPad implements KeyListener
{
    public JoyPad() {
        try {
            if ("Linux".equals(System.getProperty("os.name")))
                ReadFormJoypadLinux(this);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int JoyData1 = 0;
    public int joypaddata1 = 0;
    private int JoyData2 = 0;
    public int joypaddata2 = 0;
    private byte JoyStrobe = 0;

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

    public void Write(int address, byte data) {
        if (address == 0x4016) {
            if ((this.JoyStrobe == 1) && ((data & 1) == 0)) {
                this.JoyData1 = joypaddata1 | 0x100;
                this.JoyData2 = joypaddata2 | 0x200;
            }
            this.JoyStrobe = (byte) (data & 1);
        }
    }

    // 手柄1
    private static int ReadKey1(int key) {
        if (key == 75) // A (健盘K)
            return 0x01;
        else if (key == 74) // B (健盘J)
            return 0x02;
        else if (key == 86) // Select (健盘V)
            return 0x04;
        else if (key == 66) // Select (健盘B)
            return 0x08;
        else if (key == 87) // Up (健盘W)
            return 0x10;
        else if (key == 83) // Down (健盘S)
            return 0x20;
        else if (key == 65) // Left (健盘A)
            return 0x40;
        else if (key == 68) // Right (健盘D)
            return 0x80;
        return -1;
    }

    private static int ReadKey2(int key) {
        // 手柄2
        if (key == 59) // A (健盘;)
            return 0x01;
        else if (key == 222) // B (健盘')
            return 0x02;
        else if (key == 86) // Select (健盘V)
            return 0x04;
        else if (key == 66) // Select (健盘B)
            return 0x08;
        else if (key == 38) // Up (健盘上)
            return 0x10;
        else if (key == 40) // Down (健盘下)
            return 0x20;
        else if (key == 37) // Left (健盘左)
            return 0x40;
        else if (key == 39) // Right (健盘右)
            return 0x80;
        return -1;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int dt1 = ReadKey1(e.getKeyCode());
        if (dt1 > 0)
            joypaddata1 = joypaddata1 | dt1;
        int dt2 = ReadKey2(e.getKeyCode());
        if (dt2 > 0)
            joypaddata2 = joypaddata2 | dt2;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int dt1 = ReadKey1(e.getKeyCode());
        if (dt1 > 0)
            joypaddata1 = joypaddata1 ^ dt1;
        int dt2 = ReadKey2(e.getKeyCode());
        if (dt2 > 0)
            joypaddata2 = joypaddata2 ^ dt2;
    }

    private static int ReadPad(int controlType, int control, int amount) {
        if (controlType == 1 && control == 0) // A (健盘K)
            return 0x01;
        else if (controlType == 1 && control == 3) // B (健盘J)
            return 0x02;
        else if (controlType == 1 && control == 8) // Select (健盘V)
            return 0x04;
        else if (controlType == 1 && control == 9) // Select (健盘B)
            return 0x08;
        else if (controlType == 2 && control == 1 && amount < 0) // Up (健盘W)
            return 0x10;
        else if (controlType == 2 && control == 1 && amount > 0) // Down (健盘S)
            return 0x20;
        else if (controlType == 2 && control == 0 && amount < 0) // Left (健盘A)
            return 0x40;
        else if (controlType == 2 && control == 0 && amount > 0) // Right (健盘D)
            return 0x80;
        else if (controlType == 2 && amount == 0) { // 方向归0
//            num = calc.CalcNum(num, 0x10);
//          num = calc.CalcNum(num, 0x20);
//          num = calc.CalcNum(num, 0x40);
//          num = calc.CalcNum(num, 0x80);
            return 0x10 | 0x20 | 0x40 |0x80;
        }
        return 0;
    }

//    private static int ReadKey1_joypad(short controlType, short control, short amount, int num, ICalc calc) {
//        if (controlType == 1 && control == 0) // A (健盘K)
//            num = calc.CalcNum(num, 0x01);
//        else if (controlType == 1 && control == 3) // B (健盘J)
//            num = calc.CalcNum(num, 0x02);
//        else if (controlType == 1 && control == 8) // Select (健盘V)
//            num = calc.CalcNum(num, 0x04);
//        else if (controlType == 1 && control == 9) // Select (健盘B)
//            num = calc.CalcNum(num, 0x08);
//        else if (controlType == 2 && control == 1 && amount < 0) // Up (健盘W)
//            num = calc.CalcNum(num, 0x10);
//        else if (controlType == 2 && control == 1 && amount > 0) // Down (健盘S)
//            num = calc.CalcNum(num, 0x20);
//        else if (controlType == 2 && control == 0 && amount < 0) // Left (健盘A)
//            num = calc.CalcNum(num, 0x40);
//        else if (controlType == 2 && control == 0 && amount > 0) // Right (健盘D)
//            num = calc.CalcNum(num, 0x80);
//        else if (controlType == 2 && amount == 0) { // 方向归0
//            num = calc.CalcNum(num, 0x10);
//            num = calc.CalcNum(num, 0x20);
//            num = calc.CalcNum(num, 0x40);
//            num = calc.CalcNum(num, 0x80);
//        }
//        return num;
//    }
//
//    interface ICalc
//    {
//        public int CalcNum(int num1, int num2);
//    }

    private static void ReadFormJoypadLinux(JoyPad pad) throws FileNotFoundException {

        FileInputStream inputStream = new FileInputStream("/dev/input/js0");

        new Thread()
        {
            public void run() {
                try {
                    byte[] buffer = new byte[8];
                    int b;
                    while ((b = inputStream.read(buffer)) > 0) {
                        if (b < 8)
                            System.out.println("Only read " + b + " bytes from F710. Ignoring and continuing.");
                        else {
                            long time = ((((((buffer[3] & 0x00000000000000ff) << 8) | (buffer[2] & 0x00ff)) << 8)
                                    | (buffer[1] & 0x00ff)) << 8) | (buffer[0] & 0x00ff);
                            short amount = (short) (((buffer[5] & 0x00ff) << 8) | (buffer[4] & 0x00ff));
                            short controlType = buffer[6]; // buffer[6]==1 =>
                                                           // button,
                                                           // buffer[6]==2 =>
                                                           // joystick
                            short control = buffer[7];
//                            System.out
//                                    .println("got HID event time " + time + " controlType " + controlType + " control "
//                                            + control + " amount " + amount + " b5=" + buffer[5] + "b4=" + buffer[4]);

                            if (amount != 0) {
                                int v = ReadPad(controlType, control, amount);
                                pad.joypaddata1 = pad.joypaddata1 | v;
                            }
                            else {
                                int v = ReadPad(controlType, control, amount);
                                pad.joypaddata1 = pad.joypaddata1 & ~v;
                            }
//                          System.out.println("====2>" + pc.joypaddata1);
                        }
                    }
                    inputStream.close();
                }
                catch (Exception e) {
                    System.out.println("F710.run failure. Exiting read thread." + e);
                }
            }
        }.start();
    }
}
