package NESDemo;

/**
 * 内存(64k内存的管理）
 */
public class Memory
{
    public byte[] bts = new byte[1024 * 64];// 64k 内存

    public byte ReadMemory8(int address) {
        return bts[address];
    }

    public void WriteMemory8(int address, byte data) {
        bts[address] = data;
    }
}
