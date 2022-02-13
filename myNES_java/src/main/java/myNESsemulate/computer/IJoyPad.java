package myNESsemulate.computer;

import javax.swing.JFrame;

public interface IJoyPad
{
    
    public void Init(JFrame frame);
    
    public byte Read(int address) ;

    public void Write(int address, byte data);

}
