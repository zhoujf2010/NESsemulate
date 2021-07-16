package NESDemo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class JoyPad implements KeyListener
{

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

    interface ICalc
    {
        public int CalcNum(int num1, int num2);
    }

    private static int ReadKey11(int key, int num, ICalc calc) {
        // 手柄1
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
        // 手柄2
        else if (key == 59) // A (健盘;)
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
        // TODO Auto-generated method stub

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
}
