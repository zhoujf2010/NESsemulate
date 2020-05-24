package myNESsemulate.computer;

/**
 * 内存(64k内存的管理）
 * 
 * @作者 Jeffrey Zhou
 */
public class Memory
{
    public byte[] bts = new byte[1024 * 64];// 64k 内存

    public byte ReadMemory8(int address) {
        return bts[address];
    }

    public byte WriteMemory8(int address, byte data) {
        bts[address] = data;
        return 1;
    }

    public int ReadMemory16(int address) {
        byte dt1 = ReadMemory8(address);
        byte dt2 = ReadMemory8(address + 1);
        int data = ((dt2 & 0xFF) << 8) + (dt1 & 0xff);// java中的byte是补码形式，计算时需要与0xff才正确
        return data;
    }
}
