package NESDemo;

public class CPU
{
    private MotherBoard mb;
    public boolean isQuitting = false;

    public CPU(MotherBoard mb) {
        this.mb = mb;
    }

    // 寄存器
    private byte a_register = 0; // 它是一个8位寄存器，一般称为累加器或A寄存器
    private byte x_index_register = 0; //变址寄存器X
    private byte y_index_register = 0; //变址寄存器Y
    private byte sp_register = (byte) 0xff; // 专用于指示系统堆栈栈顶位置的8位寄存器
    public int pc_register = 0; // ushort PC是6502CPU中的16位寄存器，它主要用于存放下一条指令的首字节地址
    private byte p_register = 0; // 状态寄存器（C — 进位标志，Z — 零标志， I — 中断禁止（屏蔽）标志，
                                 //D — 十进制运算标志， B — 软件中断指令标志， V — 溢出标志，N — 负数标志
                                 // Flags
    private byte carry_flag;
    private byte zero_flag;
    private byte interrupt_flag;
    private byte decimal_flag;
    private byte brk_flag;
    private byte overflow_flag;
    private byte sign_flag;

    private long tick_count;// CPU时钟

    private byte currentOpcode;//临时变量，记录当前操作

    public byte ReadBus8(int address) {
        return mb.ReadBus8(address);
    }

    public void WriteBus8(int address, byte data) {
        mb.WriteBus8(address, data);
    }

    public int ReadBus16(int address) {
        byte dt1 = ReadBus8(address);
        byte dt2 = ReadBus8(address + 1);
        int data = ((dt2 & 0xFF) << 8) + (dt1 & 0xff);// java中的byte是补码形式，计算时需要与0xff才正确
        return data;
    }

    public void RunProcessor() {
        while (!isQuitting) {
            currentOpcode = ReadBus8(pc_register);//读指令
            switch (currentOpcode) {
                case ((byte) 0x00):
                    OpcodeBRK();
                    break;
                case ((byte) 0x01):
                    OpcodeORA();
                    break;
                case ((byte) 0x05):
                    OpcodeORA();
                    break;
                case ((byte) 0x06):
                    OpcodeASL();
                    break;
                case ((byte) 0x08):
                    OpcodePHP();
                    break;
                case ((byte) 0x09):
                    OpcodeORA();
                    break;
                case ((byte) 0x0a):
                    OpcodeASL();
                    break;
                case ((byte) 0x0d):
                    OpcodeORA();
                    break;
                case ((byte) 0x0e):
                    OpcodeASL();
                    break;
                case ((byte) 0x10):
                    OpcodeBPL();
                    break;
                case ((byte) 0x11):
                    OpcodeORA();
                    break;
                case ((byte) 0x15):
                    OpcodeORA();
                    break;
                case ((byte) 0x16):
                    OpcodeASL();
                    break;
                case ((byte) 0x18):
                    OpcodeCLC();
                    break;
                case ((byte) 0x19):
                    OpcodeORA();
                    break;
                case ((byte) 0x1d):
                    OpcodeORA();
                    break;
                case ((byte) 0x1e):
                    OpcodeASL();
                    break;
                case ((byte) 0x20):
                    OpcodeJSR();
                    break;
                case ((byte) 0x21):
                    OpcodeAND();
                    break;
                case ((byte) 0x24):
                    OpcodeBIT();
                    break;
                case ((byte) 0x25):
                    OpcodeAND();
                    break;
                case ((byte) 0x26):
                    OpcodeROL();
                    break;
                case ((byte) 0x28):
                    OpcodePLP();
                    break;
                case ((byte) 0x29):
                    OpcodeAND();
                    break;
                case ((byte) 0x2a):
                    OpcodeROL();
                    break;
                case ((byte) 0x2c):
                    OpcodeBIT();
                    break;
                case ((byte) 0x2d):
                    OpcodeAND();
                    break;
                case ((byte) 0x2e):
                    OpcodeROL();
                    break;
                case ((byte) 0x30):
                    OpcodeBMI();
                    break;
                case ((byte) 0x31):
                    OpcodeAND();
                    break;
                case ((byte) 0x32):
                    OpcodeNOP();
                    break;
                case ((byte) 0x33):
                    OpcodeNOP();
                    break;
                case ((byte) 0x34):
                    OpcodeNOP();
                    break;
                case ((byte) 0x35):
                    OpcodeAND();
                    break;
                case ((byte) 0x36):
                    OpcodeROL();
                    break;
                case ((byte) 0x38):
                    OpcodeSEC();
                    break;
                case ((byte) 0x39):
                    OpcodeAND();
                    break;
                case ((byte) 0x3d):
                    OpcodeAND();
                    break;
                case ((byte) 0x3e):
                    OpcodeROL();
                    break;
                case ((byte) 0x40):
                    OpcodeRTI();
                    break;
                case ((byte) 0x41):
                    OpcodeEOR();
                    break;
                case ((byte) 0x45):
                    OpcodeEOR();
                    break;
                case ((byte) 0x46):
                    OpcodeLSR();
                    break;
                case ((byte) 0x48):
                    OpcodePHA();
                    break;
                case ((byte) 0x49):
                    OpcodeEOR();
                    break;
                case ((byte) 0x4a):
                    OpcodeLSR();
                    break;
                case ((byte) 0x4c):
                    OpcodeJMP();
                    break;
                case ((byte) 0x4d):
                    OpcodeEOR();
                    break;
                case ((byte) 0x4e):
                    OpcodeLSR();
                    break;
                case ((byte) 0x50):
                    OpcodeBVC();
                    break;
                case ((byte) 0x51):
                    OpcodeEOR();
                    break;
                case ((byte) 0x55):
                    OpcodeEOR();
                    break;
                case ((byte) 0x56):
                    OpcodeLSR();
                    break;
                case ((byte) 0x58):
                    OpcodeCLI();
                    break;
                case ((byte) 0x59):
                    OpcodeEOR();
                    break;
                case ((byte) 0x5d):
                    OpcodeEOR();
                    break;
                case ((byte) 0x5e):
                    OpcodeLSR();
                    break;
                case ((byte) 0x60):
                    OpcodeRTS();
                    break;
                case ((byte) 0x61):
                    OpcodeADC();
                    break;
                case ((byte) 0x65):
                    OpcodeADC();
                    break;
                case ((byte) 0x66):
                    OpcodeROR();
                    break;
                case ((byte) 0x68):
                    OpcodePLA();
                    break;
                case ((byte) 0x69):
                    OpcodeADC();
                    break;
                case ((byte) 0x6a):
                    OpcodeROR();
                    break;
                case ((byte) 0x6c):
                    OpcodeJMP();
                    break;
                case ((byte) 0x6d):
                    OpcodeADC();
                    break;
                case ((byte) 0x6e):
                    OpcodeROR();
                    break;
                case ((byte) 0x70):
                    OpcodeBVS();
                    break;
                case ((byte) 0x71):
                    OpcodeADC();
                    break;
                case ((byte) 0x75):
                    OpcodeADC();
                    break;
                case ((byte) 0x76):
                    OpcodeROR();
                    break;
                case ((byte) 0x78):
                    OpcodeSEI();
                    break;
                case ((byte) 0x79):
                    OpcodeADC();
                    break;
                case ((byte) 0x7d):
                    OpcodeADC();
                    break;
                case ((byte) 0x7e):
                    OpcodeROR();
                    break;
                case ((byte) 0x81):
                    OpcodeSTA();
                    break;
                case ((byte) 0x84):
                    OpcodeSTY();
                    break;
                case ((byte) 0x85):
                    OpcodeSTA();
                    break;
                case ((byte) 0x86):
                    OpcodeSTX();
                    break;
                case ((byte) 0x88):
                    OpcodeDEY();
                    break;
                case ((byte) 0x8a):
                    OpcodeTXA();
                    break;
                case ((byte) 0x8c):
                    OpcodeSTY();
                    break;
                case ((byte) 0x8d):
                    OpcodeSTA();
                    break;
                case ((byte) 0x8e):
                    OpcodeSTX();
                    break;
                case ((byte) 0x90):
                    OpcodeBCC();
                    break;
                case ((byte) 0x91):
                    OpcodeSTA();
                    break;
                case ((byte) 0x94):
                    OpcodeSTY();
                    break;
                case ((byte) 0x95):
                    OpcodeSTA();
                    break;
                case ((byte) 0x96):
                    OpcodeSTX();
                    break;
                case ((byte) 0x98):
                    OpcodeTYA();
                    break;
                case ((byte) 0x99):
                    OpcodeSTA();
                    break;
                case ((byte) 0x9a):
                    OpcodeTXS();
                    break;
                case ((byte) 0x9d):
                    OpcodeSTA();
                    break;
                case ((byte) 0xa0):
                    OpcodeLDY();
                    break;
                case ((byte) 0xa1):
                    OpcodeLDA();
                    break;
                case ((byte) 0xa2):
                    OpcodeLDX();
                    break;
                case ((byte) 0xa4):
                    OpcodeLDY();
                    break;
                case ((byte) 0xa5):
                    OpcodeLDA();
                    break;
                case ((byte) 0xa6):
                    OpcodeLDX();
                    break;
                case ((byte) 0xa8):
                    OpcodeTAY();
                    break;
                case ((byte) 0xa9):
                    OpcodeLDA();
                    break;
                case ((byte) 0xaa):
                    OpcodeTAX();
                    break;
                case ((byte) 0xac):
                    OpcodeLDY();
                    break;
                case ((byte) 0xad):
                    OpcodeLDA();
                    break;
                case ((byte) 0xae):
                    OpcodeLDX();
                    break;
                case ((byte) 0xb0):
                    OpcodeBCS();
                    break;
                case ((byte) 0xb1):
                    OpcodeLDA();
                    break;
                case ((byte) 0xb4):
                    OpcodeLDY();
                    break;
                case ((byte) 0xb5):
                    OpcodeLDA();
                    break;
                case ((byte) 0xb6):
                    OpcodeLDX();
                    break;
                case ((byte) 0xb8):
                    OpcodeCLV();
                    break;
                case ((byte) 0xb9):
                    OpcodeLDA();
                    break;
                case ((byte) 0xba):
                    OpcodeTSX();
                    break;
                case ((byte) 0xbc):
                    OpcodeLDY();
                    break;
                case ((byte) 0xbd):
                    OpcodeLDA();
                    break;
                case ((byte) 0xbe):
                    OpcodeLDX();
                    break;
                case ((byte) 0xc0):
                    OpcodeCPY();
                    break;
                case ((byte) 0xc1):
                    OpcodeCMP();
                    break;
                case ((byte) 0xc4):
                    OpcodeCPY();
                    break;
                case ((byte) 0xc5):
                    OpcodeCMP();
                    break;
                case ((byte) 0xc6):
                    OpcodeDEC();
                    break;
                case ((byte) 0xc8):
                    OpcodeINY();
                    break;
                case ((byte) 0xc9):
                    OpcodeCMP();
                    break;
                case ((byte) 0xca):
                    OpcodeDEX();
                    break;
                case ((byte) 0xcc):
                    OpcodeCPY();
                    break;
                case ((byte) 0xcd):
                    OpcodeCMP();
                    break;
                case ((byte) 0xce):
                    OpcodeDEC();
                    break;
                case ((byte) 0xd0):
                    OpcodeBNE();
                    break;
                case ((byte) 0xd1):
                    OpcodeCMP();
                    break;
                case ((byte) 0xd5):
                    OpcodeCMP();
                    break;
                case ((byte) 0xd6):
                    OpcodeDEC();
                    break;
                case ((byte) 0xd8):
                    OpcodeCLD();
                    break;
                case ((byte) 0xd9):
                    OpcodeCMP();
                    break;
                case ((byte) 0xdd):
                    OpcodeCMP();
                    break;
                case ((byte) 0xde):
                    OpcodeDEC();
                    break;
                case ((byte) 0xe0):
                    OpcodeCPX();
                    break;
                case ((byte) 0xe1):
                    OpcodeSBC();
                    break;
                case ((byte) 0xe4):
                    OpcodeCPX();
                    break;
                case ((byte) 0xe5):
                    OpcodeSBC();
                    break;
                case ((byte) 0xe6):
                    OpcodeINC();
                    break;
                case ((byte) 0xe8):
                    OpcodeINX();
                    break;
                case ((byte) 0xe9):
                    OpcodeSBC();
                    break;
                case ((byte) 0xec):
                    OpcodeCPX();
                    break;
                case ((byte) 0xed):
                    OpcodeSBC();
                    break;
                case ((byte) 0xee):
                    OpcodeINC();
                    break;
                case ((byte) 0xf0):
                    OpcodeBEQ();
                    break;
                case ((byte) 0xf1):
                    OpcodeSBC();
                    break;
                case ((byte) 0xf5):
                    OpcodeSBC();
                    break;
                case ((byte) 0xf6):
                    OpcodeINC();
                    break;
                case ((byte) 0xf8):
                    OpcodeSED();
                    break;
                case ((byte) 0xf9):
                    OpcodeSBC();
                    break;
                case ((byte) 0xfd):
                    OpcodeSBC();
                    break;
                case ((byte) 0xfe):
                    OpcodeINC();
                    break;
                default:
                    OpcodeNOP();
                    break;
            }
            tick_count = mb.CPUTick(tick_count);
        }
    }

    //2个8位地址拼成一个16位地址
    private int MakeAddress(byte c, byte d) {
        int newAddress = (int) (d & 0xFF);
        newAddress = newAddress << 8;
        newAddress += (int) (c & 0xFF);
        return (int) newAddress;
    }

    //#region 堆栈操作
    private void Push8(byte data) {
        WriteBus8((0x100 + (sp_register & 0xff)), data);
        sp_register = (byte) ((sp_register & 0xff) - 1);
    }

    public void Push16(int data) {
        Push8((byte) (data >> 8));
        Push8((byte) (data & 0xff));
    }

    private byte Pull8() {
        sp_register = (byte) ((sp_register & 0xFF) + 1);
        return ReadBus8((0x100 + (sp_register & 0xFF)));
    }

    private int Pull16() {
        byte data1, data2;
        int fulldata;

        data1 = Pull8();
        data2 = Pull8();

        // We use MakeAddress because it's easier
        fulldata = MakeAddress(data1, data2);

        return fulldata;
    }

    public void PushStatus() {
        p_register = 0;

        if (sign_flag == 1)
            p_register = (byte) (p_register + 0x80);

        if (overflow_flag == 1)
            p_register = (byte) (p_register + 0x40);

        // statusdata = (byte)(statusdata + 0x20);

        if (brk_flag == 1)
            p_register = (byte) (p_register + 0x10);

        if (decimal_flag == 1)
            p_register = (byte) (p_register + 0x8);

        if (interrupt_flag == 1)
            p_register = (byte) (p_register + 0x4);

        if (zero_flag == 1)
            p_register = (byte) (p_register + 0x2);

        if (carry_flag == 1)
            p_register = (byte) (p_register + 0x1);

        Push8(p_register);
    }

    public void PullStatus() {
        p_register = Pull8();

        if ((p_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        if ((p_register & 0x40) == 0x40)
            overflow_flag = 1;
        else
            overflow_flag = 0;

        if ((p_register & 0x10) == 0x10)
            brk_flag = 1;
        else
            brk_flag = 0;

        if ((p_register & 0x8) == 0x8)
            decimal_flag = 1;
        else
            decimal_flag = 0;

        if ((p_register & 0x4) == 0x4)
            interrupt_flag = 1;
        else
            interrupt_flag = 0;

        if ((p_register & 0x2) == 0x2)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((p_register & 0x1) == 0x1)
            carry_flag = 1;
        else
            carry_flag = 0;
    }
    //#endregion

    // #region 运算器
    // Opcodes
    public void OpcodeADC() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        // Decode
        switch (currentOpcode) {
            case ((byte) 0x69):
                valueholder = arg1;
                break;
            case ((byte) 0x65):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x75):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x6D):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x7D):
                valueholder = AbsoluteX(arg1, arg2, true);
                break;
            case ((byte) 0x79):
                valueholder = AbsoluteY(arg1, arg2, true);
                break;
            case ((byte) 0x61):
                valueholder = IndirectX(arg1);
                break;
            case ((byte) 0x71):
                valueholder = IndirectY(arg1, true);
                break;
            default:
                isQuitting = true;
                break;
        }

        // Execute
        int valueholder32;
        valueholder32 = ((a_register & 0xff) + (valueholder & 0xff) + carry_flag);
        // valueholder32 = (uint)(a_register + valueholder);
        if (valueholder32 > 255) {
            carry_flag = 1;
            overflow_flag = 1;
        }
        else {
            carry_flag = 0;
            overflow_flag = 0;
        }
        if ((valueholder32 & 0xff) == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder32 & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        a_register = (byte) (valueholder32 & 0xff);

        // Advance PC and tick count
        // FIXME: X and Y index overflow tick
        switch (currentOpcode) {
            case ((byte) 0x69):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0x65):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x75):
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x6D):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x7D):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x79):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x61):
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x71):
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                break;
        }
    }

    public void OpcodeAND() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0x29):
                valueholder = arg1;
                break;
            case ((byte) 0x25):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x35):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x2D):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x3D):
                valueholder = AbsoluteX(arg1, arg2, true);
                break;
            case ((byte) 0x39):
                valueholder = AbsoluteY(arg1, arg2, true);
                break;
            case ((byte) 0x21):
                valueholder = IndirectX(arg1);
                break;
            case ((byte) 0x31):
                valueholder = IndirectY(arg1, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken AND");
                break;
        }

        a_register = (byte) (a_register & valueholder);
        if ((a_register & 0xff) == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        switch (currentOpcode) {
            case ((byte) 0x29):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0x25):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x35):
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x2D):
                tick_count += 3;
                pc_register += 3;
                break;
            case ((byte) 0x3D):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x39):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x21):
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x31):
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken AND");
                break;
        }
    }

    public void OpcodeASL() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0x0a):
                valueholder = a_register;
                break;
            case ((byte) 0x06):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x16):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x0E):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x1E):
                valueholder = AbsoluteX(arg1, arg2, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ASL");
                break;
        }
        if ((valueholder & 0x80) == 0x80)
            carry_flag = 1;
        else
            carry_flag = 0;

        valueholder = (byte) (valueholder << 1);

        if ((valueholder & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // This one is a little different because we actually need
        // to do more than incrementing in the last step
        switch (currentOpcode) {
            case ((byte) 0x0a):
                a_register = valueholder;
                tick_count += 2;
                pc_register += 1;
                break;
            case ((byte) 0x06):
                ZeroPageWrite(arg1, valueholder);
                tick_count += 5;
                pc_register += 2;
                break;
            case ((byte) 0x16):
                ZeroPageXWrite(arg1, valueholder);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x0E):
                AbsoluteWrite(arg1, arg2, valueholder);
                tick_count += 6;
                pc_register += 3;
                break;
            case ((byte) 0x1E):
                AbsoluteXWrite(arg1, arg2, valueholder);
                tick_count += 7;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ASL");
                break;
        }
    }

    public void OpcodeBCC() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (carry_flag == 0) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBCS() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (carry_flag == 1) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBEQ() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (zero_flag == 1) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBIT() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0x24):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x2c):
                valueholder = Absolute(arg1, arg2);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken BIT");
                break;
        }

        if ((a_register & valueholder) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        if ((valueholder & 0x40) == 0x40)
            overflow_flag = 1;
        else
            overflow_flag = 0;

        switch (currentOpcode) {
            case ((byte) 0x24):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x2c):
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken BIT");
                break;
        }
    }

    public void OpcodeBMI() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (sign_flag == 1) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBNE() {
        byte arg1;

        // FIX ME: All these are set "wrong" to match the old emulator
        // FIXME: They should probably all be corrected when debugging is finished
        if (zero_flag == 0) {
            arg1 = ReadBus8((pc_register + 1));
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBPL() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (sign_flag == 0) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBRK() {
        pc_register = (pc_register + 2);
        Push16(pc_register);
        brk_flag = 1;
        PushStatus();
        interrupt_flag = 1;
        pc_register = ReadBus16(0xfffe);
        tick_count += 7;
    }

    public void OpcodeBVC() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (overflow_flag == 0) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeBVS() {
        byte arg1 = ReadBus8((pc_register + 1));

        // FIX ME: Branching to a new page takes a 1 tick penalty
        if (overflow_flag == 1) {
            pc_register += 2;
            if ((pc_register & 0xFF00) != ((pc_register + arg1 + 2) & 0xFF00)) {
                tick_count += 1;
            }
            pc_register = (pc_register + arg1);
            tick_count += 1;
        }
        else {
            pc_register += 2;
        }
        tick_count += 2;
    }

    public void OpcodeCLC() {
        carry_flag = 0;
        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeCLD() {
        decimal_flag = 0;
        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeCLI() {
        interrupt_flag = 0;
        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeCLV() {
        overflow_flag = 0;
        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeCMP() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0xC9):
                valueholder = arg1;
                break;
            case ((byte) 0xC5):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0xD5):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0xCD):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0xDD):
                valueholder = AbsoluteX(arg1, arg2, true);
                break;
            case ((byte) 0xD9):
                valueholder = AbsoluteY(arg1, arg2, true);
                break;
            case ((byte) 0xC1):
                valueholder = IndirectX(arg1);
                break;
            case ((byte) 0xD1):
                valueholder = IndirectY(arg1, true);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken CMP");
                break;
        }
        if ((a_register & 0xff) >= (valueholder & 0xff))
            carry_flag = 1;
        else
            carry_flag = 0;

        valueholder = (byte) (a_register - valueholder);

        if (valueholder == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // FIXME: X and Y index overflow tick
        switch (currentOpcode) {
            case ((byte) 0xC9):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xC5):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xD5):
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0xCD):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xDD):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xD9):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xC1):
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0xD1):
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken CMP");
                break;
        }
    }

    public void OpcodeCPX() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0xE0):
                valueholder = arg1;
                break;
            case ((byte) 0xE4):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0xEC):
                valueholder = Absolute(arg1, arg2);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken CPX");
                break;
        }

        if (x_index_register >= valueholder)
            carry_flag = 1;
        else
            carry_flag = 0;

        valueholder = (byte) (x_index_register - valueholder);

        if (valueholder == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        switch (currentOpcode) {
            case ((byte) 0xE0):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xE4):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xEC):
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken CPX");
                break;
        }
    }

    public void OpcodeCPY() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0xC0):
                valueholder = arg1;
                break;
            case ((byte) 0xC4):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0xCC):
                valueholder = Absolute(arg1, arg2);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken CPY");
                break;
        }

        if ((y_index_register & 0xff) >= (valueholder & 0xff))
            carry_flag = 1;
        else
            carry_flag = 0;

        valueholder = (byte) (y_index_register - valueholder);

        if (valueholder == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        switch (currentOpcode) {
            case ((byte) 0xC0):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xC4):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xCC):
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken CPY");
                break;
        }
    }

    public void OpcodeDEC() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            // case ((byte)0xCA): valueholder = a_register; break;
            case ((byte) 0xC6):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0xD6):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0xCE):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0xDE):
                valueholder = AbsoluteX(arg1, arg2, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken DEC");
                break;
        }

        valueholder--;

        if ((valueholder & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // This one is a little different because we actually need
        // to do more than incrementing in the last step
        switch (currentOpcode) {
            // case ((byte)0xCA): a_register = valueholder;
            // tick_count += 2; pc_register += 1; break;
            case ((byte) 0xC6):
                ZeroPageWrite(arg1, valueholder);
                tick_count += 5;
                pc_register += 2;
                break;
            case ((byte) 0xD6):
                ZeroPageXWrite(arg1, valueholder);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0xCE):
                AbsoluteWrite(arg1, arg2, valueholder);
                tick_count += 6;
                pc_register += 3;
                break;
            case ((byte) 0xDE):
                AbsoluteXWrite(arg1, arg2, valueholder);
                tick_count += 7;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken DEC");
                break;
        }
    }

    public void OpcodeDEX() {
        x_index_register--;

        if ((x_index_register & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((x_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register++;
        tick_count += 2;
    }

    public void OpcodeDEY() {
        y_index_register--;

        if ((y_index_register & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((y_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register++;
        tick_count += 2;
    }

    public void OpcodeEOR() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0x49):
                valueholder = arg1;
                break;
            case ((byte) 0x45):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x55):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x4D):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x5D):
                valueholder = AbsoluteX(arg1, arg2, true);
                break;
            case ((byte) 0x59):
                valueholder = AbsoluteY(arg1, arg2, true);
                break;
            case ((byte) 0x41):
                valueholder = IndirectX(arg1);
                break;
            case ((byte) 0x51):
                valueholder = IndirectY(arg1, true);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken EOR");
                break;
        }

        a_register = (byte) (a_register ^ valueholder);
        if ((a_register & 0xff) == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        switch (currentOpcode) {
            case ((byte) 0x49):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0x45):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x55):
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x4D):
                tick_count += 3;
                pc_register += 3;
                break;
            case ((byte) 0x5D):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x59):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x41):
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x51):
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken EOR");
                break;
        }
    }

    public void OpcodeINC() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            // case ((byte)0xCA): valueholder = a_register; break;
            case ((byte) 0xE6):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0xF6):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0xEE):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0xFE):
                valueholder = AbsoluteX(arg1, arg2, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken INC");
                break;
        }
        valueholder++;

        if ((valueholder & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // This one is a little different because we actually need
        // to do more than incrementing in the last step
        switch (currentOpcode) {
            // case ((byte)0xCA): a_register = valueholder;
            // tick_count += 2; pc_register += 1; break;
            case ((byte) 0xE6):
                ZeroPageWrite(arg1, valueholder);
                tick_count += 5;
                pc_register += 2;
                break;
            case ((byte) 0xF6):
                ZeroPageXWrite(arg1, valueholder);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0xEE):
                AbsoluteWrite(arg1, arg2, valueholder);
                tick_count += 6;
                pc_register += 3;
                break;
            case ((byte) 0xFE):
                AbsoluteXWrite(arg1, arg2, valueholder);
                tick_count += 7;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken INC");
                break;
        }
    }

    public void OpcodeINX() {
        x_index_register++;

        if ((x_index_register & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((x_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register++;
        tick_count += 2;
    }

    public void OpcodeINY() {
        y_index_register++;

        if ((y_index_register & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((y_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register++;
        tick_count += 2;
    }

    public void OpcodeJMP() {
        // byte arg1 = myEngine.ReadMemory8((pc_register + 1));
        // byte arg2 = myEngine.ReadMemory8((pc_register + 2));
        int myAddress = ReadBus16((pc_register + 1));

        switch (currentOpcode) {
            case ((byte) 0x4c): // pc_register = MakeAddress(arg1, arg2);
                pc_register = myAddress;
                // System.out.println("Jumping to: {0:x}", pc_register);
                tick_count += 3;
                break;
            case ((byte) 0x6c): // pc_register = myEngine.ReadBus16(MakeAddress(arg1, arg2));
                pc_register = ReadBus16(myAddress);
                // System.out.println("Jumping to: {0:x}", pc_register);
                tick_count += 5;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken JMP");
                break;
        }
    }

    public void OpcodeJSR() {
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        Push16((pc_register + 2));
        pc_register = MakeAddress(arg1, arg2);
        tick_count += 6;
    }

    public void OpcodeLDA() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2;
        switch (currentOpcode) {
            case ((byte) 0xA9):
                a_register = arg1;
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xA5):
                a_register = ZeroPage(arg1 & 0xff);
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xB5):
                a_register = ZeroPageX(arg1 & 0xff);
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0xAD):
                arg2 = ReadBus8((pc_register + 2));
                a_register = Absolute(arg1, arg2);
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xBD):
                arg2 = ReadBus8((pc_register + 2));
                a_register = AbsoluteX(arg1, arg2, true); // CHECK FOR PAGE BOUNDARIES

                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xB9):
                arg2 = ReadBus8((pc_register + 2));
                a_register = AbsoluteY(arg1, arg2, true); // CHECK FOR PAGE BOUNDARIES
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xA1):
                a_register = IndirectX(arg1);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0xB1):
                a_register = IndirectY(arg1, true); // CHECK FOR PAGE BOUNDARIES
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken LDA");
                break;
        }

        if (a_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;
    }

    public void OpcodeLDX() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        switch (currentOpcode) {
            case ((byte) 0xA2):
                x_index_register = arg1;
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xA6):
                x_index_register = ZeroPage(arg1);
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xB6):
                x_index_register = ZeroPageY(arg1);
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0xAE):
                x_index_register = Absolute(arg1, arg2);
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xBE):
                x_index_register = AbsoluteY(arg1, arg2, true);
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken LDX");
                break;
        }

        if (x_index_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((x_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;
    }

    public void OpcodeLDY() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        // byte valueholder = 0xff;
        switch (currentOpcode) {
            case ((byte) 0xA0):
                y_index_register = arg1;
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xA4):
                y_index_register = ZeroPage(arg1);
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xB4):
                y_index_register = ZeroPageX(arg1);
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0xAC):
                y_index_register = Absolute(arg1, arg2);
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xBC):
                y_index_register = AbsoluteX(arg1, arg2, true);
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken LDY");
                break;
        }

        if (y_index_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((y_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;
    }

    public void OpcodeLSR() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0x4a):
                valueholder = a_register;
                break;
            case ((byte) 0x46):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x56):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x4E):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x5E):
                valueholder = AbsoluteX(arg1, arg2, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken LSR");
                break;
        }
        if ((valueholder & 0x1) == 0x1)
            carry_flag = 1;
        else
            carry_flag = 0;

        valueholder = (byte) ((valueholder & 0xff) >> 1);

        if ((valueholder & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // This one is a little different because we actually need
        // to do more than incrementing in the last step
        switch (currentOpcode) {
            case ((byte) 0x4a):
                a_register = valueholder;
                tick_count += 2;
                pc_register += 1;
                break;
            case ((byte) 0x46):
                ZeroPageWrite(arg1, valueholder);
                tick_count += 5;
                pc_register += 2;
                break;
            case ((byte) 0x56):
                ZeroPageXWrite(arg1, valueholder);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x4E):
                AbsoluteWrite(arg1, arg2, valueholder);
                tick_count += 6;
                pc_register += 3;
                break;
            case ((byte) 0x5E):
                AbsoluteXWrite(arg1, arg2, valueholder);
                tick_count += 7;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken LSR");
                break;
        }
    }

    public void OpcodeNOP() {
        if (currentOpcode != 0xEA) {
            // System.out.println("Illegal Instruction");
            // myEngine.isQuitting = true;
        }
        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeORA() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        switch (currentOpcode) {
            case ((byte) 0x09):
                valueholder = arg1;
                break;
            case ((byte) 0x05):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x15):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x0D):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x1D):
                valueholder = AbsoluteX(arg1, arg2, true);
                break;
            case ((byte) 0x19):
                valueholder = AbsoluteY(arg1, arg2, true);
                break;
            case ((byte) 0x01):
                valueholder = IndirectX(arg1);
                break;
            case ((byte) 0x11):
                valueholder = IndirectY(arg1, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ORA");
                break;
        }

        a_register = (byte) (a_register | valueholder);
        if ((a_register & 0xff) == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // FIXME: X and Y index overflow tick
        switch (currentOpcode) {
            case ((byte) 0x09):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0x05):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x15):
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x0D):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x1D):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x19):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x01):
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x11):
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ORA");
                break;
        }
    }

    public void OpcodePHA() {
        Push8(a_register);
        pc_register += 1;
        tick_count += 3;
    }

    public void OpcodePHP() {
        PushStatus();
        pc_register += 1;
        tick_count += 3;
    }

    public void OpcodePLA() {
        a_register = Pull8();
        if ((a_register & 0xff) == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;
        pc_register += 1;
        tick_count += 4;
    }

    public void OpcodePLP() {
        PullStatus();
        pc_register += 1;
        tick_count += 4;
    }

    public void OpcodeROL() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;
        byte bitholder = 0;

        switch (currentOpcode) {
            case ((byte) 0x2a):
                valueholder = a_register;
                break;
            case ((byte) 0x26):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x36):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x2E):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x3E):
                valueholder = AbsoluteX(arg1, arg2, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ROL");
                break;
        }
        if ((valueholder & 0x80) == 0x80)
            bitholder = 1;
        else
            bitholder = 0;

        valueholder = (byte) (valueholder << 1);
        valueholder = (byte) (valueholder | carry_flag);

        carry_flag = bitholder;

        if ((valueholder & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // This one is a little different because we actually need
        // to do more than incrementing in the last step
        switch (currentOpcode) {
            case ((byte) 0x2a):
                a_register = valueholder;
                tick_count += 2;
                pc_register += 1;
                break;
            case ((byte) 0x26):
                ZeroPageWrite(arg1, valueholder);
                tick_count += 5;
                pc_register += 2;
                break;
            case ((byte) 0x36):
                ZeroPageXWrite(arg1, valueholder);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x2E):
                AbsoluteWrite(arg1, arg2, valueholder);
                tick_count += 6;
                pc_register += 3;
                break;
            case ((byte) 0x3E):
                AbsoluteXWrite(arg1, arg2, valueholder);
                tick_count += 7;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ROL");
                break;
        }
    }

    public void OpcodeROR() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;
        byte bitholder = 0;

        switch (currentOpcode) {
            case ((byte) 0x6a):
                valueholder = a_register;
                break;
            case ((byte) 0x66):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0x76):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0x6E):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0x7E):
                valueholder = AbsoluteX(arg1, arg2, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ROR");
                break;
        }

        if ((valueholder & 0x1) == 0x1)
            bitholder = 1;
        else
            bitholder = 0;

        valueholder = (byte) ((valueholder & 0xff) >> 1);

        if (carry_flag == 1)
            valueholder = (byte) (valueholder | 0x80);

        carry_flag = bitholder;

        if ((valueholder & 0xff) == 0x0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        // This one is a little different because we actually need
        // to do more than incrementing in the last step
        switch (currentOpcode) {
            case ((byte) 0x6a):
                a_register = valueholder;
                tick_count += 2;
                pc_register += 1;
                break;
            case ((byte) 0x66):
                ZeroPageWrite(arg1, valueholder);
                tick_count += 5;
                pc_register += 2;
                break;
            case ((byte) 0x76):
                ZeroPageXWrite(arg1, valueholder);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x6E):
                AbsoluteWrite(arg1, arg2, valueholder);
                tick_count += 6;
                pc_register += 3;
                break;
            case ((byte) 0x7E):
                AbsoluteXWrite(arg1, arg2, valueholder);
                tick_count += 7;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken ROR");
                break;
        }
    }

    public void OpcodeRTI() {
        PullStatus();
        pc_register = Pull16();
        tick_count += 6;
    }

    public void OpcodeRTS() {
        pc_register = Pull16();
        tick_count += 6;
        pc_register += 1;
    }

    public void OpcodeSBC() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        byte valueholder = (byte) 0xff;

        // Decode
        switch (currentOpcode) {
            case ((byte) 0xE9):
                valueholder = arg1;
                break;
            case ((byte) 0xE5):
                valueholder = ZeroPage(arg1);
                break;
            case ((byte) 0xF5):
                valueholder = ZeroPageX(arg1);
                break;
            case ((byte) 0xED):
                valueholder = Absolute(arg1, arg2);
                break;
            case ((byte) 0xFD):
                valueholder = AbsoluteX(arg1, arg2, true);
                break;
            case ((byte) 0xF9):
                valueholder = AbsoluteY(arg1, arg2, true);
                break;
            case ((byte) 0xE1):
                valueholder = IndirectX(arg1);
                break;
            case ((byte) 0xF1):
                valueholder = IndirectY(arg1, false);
                break;
            default:
                isQuitting = true;
                System.out.println("Broken SBC");
                break;
        }

        // Execute
        long valueholder32;
        valueholder32 = ((a_register & 0xff) - (valueholder & 0xff)) & 0xFFFFFF;
        if (carry_flag == 0)
            valueholder32 = (valueholder32 - 1) & 0xFFFFFF;

        if (valueholder32 > 255) {
            carry_flag = 0;
            overflow_flag = 1;
        }
        else {
            carry_flag = 1;
            overflow_flag = 0;
        }
        if ((valueholder32 & 0xff) == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((valueholder32 & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        a_register = (byte) (valueholder32 & 0xff);

        // Advance PC and tick count
        // FIXME: X and Y index overflow tick
        switch (currentOpcode) {
            case ((byte) 0xE9):
                tick_count += 2;
                pc_register += 2;
                break;
            case ((byte) 0xE5):
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0xF5):
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0xED):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xFD):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xF9):
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0xE1):
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0xF1):
                tick_count += 5;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken SBC");
                break;
        }
    }

    public void OpcodeSEC() {
        carry_flag = 1;
        tick_count += 2;
        pc_register += 1;
    }

    public void OpcodeSED() {
        decimal_flag = 1;
        tick_count += 2;
        pc_register += 1;
    }

    public void OpcodeSEI() {
        interrupt_flag = 1;
        tick_count += 2;
        pc_register += 1;
    }

    public void OpcodeSTA() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));

        // Decode
        switch (currentOpcode) {
            case ((byte) 0x85):
                ZeroPageWrite(arg1 & 0xff, a_register);
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x95):
                ZeroPageXWrite(arg1 & 0xff, a_register);
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x8D):
                AbsoluteWrite(arg1, arg2, a_register);
                tick_count += 4;
                pc_register += 3;
                break;
            case ((byte) 0x9D):
                AbsoluteXWrite(arg1, arg2, a_register);
                tick_count += 5;
                pc_register += 3;
                break;
            case ((byte) 0x99):
                AbsoluteYWrite(arg1, arg2, a_register);
                tick_count += 5;
                pc_register += 3;
                break;
            case ((byte) 0x81):
                IndirectXWrite(arg1, a_register);
                tick_count += 6;
                pc_register += 2;
                break;
            case ((byte) 0x91):
                IndirectYWrite(arg1, a_register);
                tick_count += 6;
                pc_register += 2;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken STA");
                break;
        }
    }

    public void OpcodeSTX() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        // Decode
        switch (currentOpcode) {
            case ((byte) 0x86):
                ZeroPageWrite(arg1, x_index_register);
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x96):
                ZeroPageYWrite(arg1, x_index_register);
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x8E):
                AbsoluteWrite(arg1, arg2, x_index_register);
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken STX");
                break;
        }
    }

    public void OpcodeSTY() {
        // We may not use both, but it's easier to grab them now
        byte arg1 = ReadBus8((pc_register + 1));
        byte arg2 = ReadBus8((pc_register + 2));
        // Decode
        switch (currentOpcode) {
            case ((byte) 0x84):
                ZeroPageWrite(arg1, y_index_register);
                tick_count += 3;
                pc_register += 2;
                break;
            case ((byte) 0x94):
                ZeroPageXWrite(arg1, y_index_register);
                tick_count += 4;
                pc_register += 2;
                break;
            case ((byte) 0x8C):
                AbsoluteWrite(arg1, arg2, y_index_register);
                tick_count += 4;
                pc_register += 3;
                break;
            default:
                isQuitting = true;
                System.out.println("Broken STY");
                break;
        }
    }

    public void OpcodeTAX() {
        x_index_register = a_register;

        if (x_index_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((x_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeTAY() {
        y_index_register = a_register;

        if (y_index_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((y_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeTSX() {
        x_index_register = sp_register;

        if (x_index_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((x_index_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeTXA() {
        a_register = x_index_register;

        if (a_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeTXS() {
        sp_register = x_index_register;

        pc_register += 1;
        tick_count += 2;
    }

    public void OpcodeTYA() {
        a_register = y_index_register;

        if (a_register == 0)
            zero_flag = 1;
        else
            zero_flag = 0;

        if ((a_register & 0x80) == 0x80)
            sign_flag = 1;
        else
            sign_flag = 0;

        pc_register += 1;
        tick_count += 2;
    }
    // #endregion

    /// Addressing
    public byte ZeroPage(int c) {
        return ReadBus8(c & 0xff);
    }

    public byte ZeroPageX(int c) {
        return ReadBus8((0xff & (c + x_index_register)));
    }

    public byte ZeroPageY(int c) {
        return ReadBus8((0xff & (c + y_index_register)));
    }

    public byte Absolute(byte c, byte d) {
        return ReadBus8(MakeAddress(c, d));
    }

    public byte AbsoluteX(byte c, byte d, boolean check_page) {
        if (check_page) {
            if ((MakeAddress(c, d) & 0xFF00) != ((MakeAddress(c, d) + (x_index_register & 0xff)) & 0xFF00)) {
                tick_count += 1;
            }
            ;
        }
        return ReadBus8((MakeAddress(c, d) + (x_index_register & 0xff)));
    }

    public byte AbsoluteY(byte c, byte d, boolean check_page) {
        if (check_page) {
            if ((MakeAddress(c, d) & 0xFF00) != ((MakeAddress(c, d) + (y_index_register & 0xff)) & 0xFF00)) {
                tick_count += 1;
            }
            ;
        }
        return ReadBus8((MakeAddress(c, d) + (y_index_register & 0xff)));
    }

    public byte IndirectX(byte c) {
        return ReadBus8(ReadBus16((0xff & ((c & 0xff) + (x_index_register & 0xff)))));
    }

    public byte IndirectY(byte c, boolean check_page) {
        if (check_page) {
            if ((ReadBus16(c & 0xff) & 0xFF00) != ((ReadBus16(c & 0xff) + (y_index_register & 0xff)) & 0xFF00)) {
                tick_count += 1;
            }
            ;
        }
        return ReadBus8((ReadBus16(c & 0xff) + (y_index_register & 0xff)));
    }

    public void ZeroPageWrite(int c, byte data) {
        WriteBus8(c & 0xff, data);
    }

    public void ZeroPageXWrite(int c, byte data) {
        WriteBus8((0xff & (c + (x_index_register & 0xff))), data);
    }

    public void ZeroPageYWrite(int c, byte data) {
        WriteBus8((0xff & (c + (y_index_register & 0xff))), data);
    }

    public void AbsoluteWrite(byte c, byte d, byte data) {
        WriteBus8(MakeAddress(c, d), data);
    }

    public void AbsoluteXWrite(byte c, byte d, byte data) {
        WriteBus8((MakeAddress(c, d) + (x_index_register & 0xFF)), data);
    }

    public void AbsoluteYWrite(byte c, byte d, byte data) {
        WriteBus8((MakeAddress(c, d) + (y_index_register & 0xFF)), data);
    }

    public void IndirectXWrite(byte c, byte data) {
        WriteBus8(ReadBus16((0xff & (c + (short) (x_index_register & 0xFF)))), data);
    }

    public void IndirectYWrite(byte c, byte data) {
        WriteBus8((ReadBus16(c) + (y_index_register & 0xFF)), data);
    }
}
