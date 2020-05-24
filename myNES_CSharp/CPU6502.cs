//////////////////////////////////////////////////////////////////////////////
//This file is part of My Nes                                               //
//A Nintendo Entertainment System Emulator.                                 //
//                                                                          //
//Copyright © 2009 Ala Hadid (AHD)                                          //
//                                                                          //
//My Nes is free software; you can redistribute it and/or modify            //
//it under the terms of the GNU General Public License as published by      //
//the Free Software Foundation; either version 2 of the License, or         //
//(at your option) any later version.                                       //
//                                                                          //
//My Nes is distributed in the hope that it will be useful,                 //
//but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//GNU General Public License for more details.                              //
//                                                                          //
//You should have received a copy of the GNU General Public License         //
//along with this program; if not, write to the Free Software               //
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA//
//////////////////////////////////////////////////////////////////////////////
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
namespace AHD.MyNes.Nes
{
    /// <summary>
    /// The main cpu of the nes
    /// </summary>
    public class CPU6502
    {
        //Registers
        public byte a_register;
        public byte x_index_register;
        public byte y_index_register;
        public byte sp_register;
        public ushort pc_register;
        //Flags
        public byte carry_flag;
        public byte zero_flag;
        public byte interrupt_flag;
        public byte decimal_flag;
        public byte brk_flag;
        public byte overflow_flag;
        public byte sign_flag;
        //The machine's engine
        NesEmulator _NesEmu;
        //Ticks
        public uint tick_count;
        public uint total_cycles = 0;
        //Helper variables
        public byte currentOpcode;
        public ushort previousPC;

        /// <summary>
        /// The main cpu of the nes
        /// </summary>
        /// <param name="NesEmu">The current nes machine</param>
        public CPU6502(NesEmulator NesEmu)
        {
            _NesEmu = NesEmu;
            a_register = 0;
            x_index_register = 0;
            y_index_register = 0;
            sp_register = 0xff;
        }
        /// <summary>
        /// get two bytes into a correct address
        /// </summary>
        /// <param name="c">Byte A</param>
        /// <param name="d">Byte B</param>
        /// <returns>ushort A & B togather</returns>
        public ushort MakeAddress(byte c, byte d)
        {
            uint newAddress = (uint)d;
            newAddress = newAddress << 8;
            newAddress += (uint)c;
            return (ushort)newAddress;
        }
        #region Addressing Modes
        public byte ZeroPage(ushort c)
        {
            return _NesEmu.ReadMemory8(c);
        }
        public byte ZeroPageX(ushort c)
        {
            return _NesEmu.ReadMemory8((ushort)(0xff & (c + x_index_register)));
        }
        public byte ZeroPageY(ushort c)
        {
            return _NesEmu.ReadMemory8((ushort)(0xff & (c + y_index_register)));
        }
        public byte Absolute(byte c, byte d)
        {
            return _NesEmu.ReadMemory8(MakeAddress(c, d));
        }
        public byte AbsoluteX(byte c, byte d, bool check_page)
        {
            if (check_page)
            {
                if ((MakeAddress(c, d) & 0xFF00) !=
                    ((MakeAddress(c, d) + x_index_register) & 0xFF00))
                {
                    tick_count += 1;
                };
            }
            return _NesEmu.ReadMemory8((ushort)(MakeAddress(c, d) + x_index_register));
        }
        public byte AbsoluteY(byte c, byte d, bool check_page)
        {
            if (check_page)
            {
                if ((MakeAddress(c, d) & 0xFF00) !=
                    ((MakeAddress(c, d) + y_index_register) & 0xFF00))
                {
                    tick_count += 1;
                };
            }
            return _NesEmu.ReadMemory8((ushort)(MakeAddress(c, d) + y_index_register));
        }
        public byte IndirectX(byte c)
        {
            return _NesEmu.ReadMemory8((ushort)_NesEmu.ReadMemory16((ushort)(0xff & (c + (ushort)x_index_register))));
        }
        public byte IndirectY(byte c, bool check_page)
        {
            if (check_page)
            {
                if ((_NesEmu.ReadMemory16(c) & 0xFF00) !=
                    ((_NesEmu.ReadMemory16(c) + y_index_register) & 0xFF00))
                {
                    tick_count += 1;
                };
            }
            return _NesEmu.ReadMemory8((ushort)(_NesEmu.ReadMemory16(c) + (ushort)y_index_register));
        }

        public byte ZeroPageWrite(ushort c, byte data)
        {
            return _NesEmu.WriteMemory8(c, data);
        }
        public byte ZeroPageXWrite(ushort c, byte data)
        {
            return _NesEmu.WriteMemory8((ushort)(0xff & (c + x_index_register)), data);
        }
        public byte ZeroPageYWrite(ushort c, byte data)
        {
            return _NesEmu.WriteMemory8((ushort)(0xff & (c + y_index_register)), data);
        }
        public byte AbsoluteWrite(byte c, byte d, byte data)
        {
            return _NesEmu.WriteMemory8(MakeAddress(c, d), data);
        }
        public byte AbsoluteXWrite(byte c, byte d, byte data)
        {
            return _NesEmu.WriteMemory8((ushort)(MakeAddress(c, d) + x_index_register), data);
        }
        public byte AbsoluteYWrite(byte c, byte d, byte data)
        {
            return _NesEmu.WriteMemory8((ushort)(MakeAddress(c, d) + y_index_register), data);
        }
        public byte IndirectXWrite(byte c, byte data)
        {
            return _NesEmu.WriteMemory8((ushort)_NesEmu.ReadMemory16((ushort)(0xff & (c + (short)x_index_register))), data);
        }
        public byte IndirectYWrite(byte c, byte data)
        {
            return _NesEmu.WriteMemory8((ushort)(_NesEmu.ReadMemory16(c) + (ushort)y_index_register), data);
        }
        #endregion
        #region Operations (pull, Push ...)
        public void Push8(byte data)
        {
            _NesEmu.WriteMemory8((ushort)(0x100 + sp_register), (byte)(data & 0xff));
            sp_register = (byte)(sp_register - 1);
        }
        public void Push16(ushort data)
        {
            Push8((byte)(data >> 8));
            Push8((byte)(data & 0xff));
        }
        public void PushStatus()
        {
            byte statusdata = 0;

            if (sign_flag == 1)
                statusdata = (byte)(statusdata + 0x80);

            if (overflow_flag == 1)
                statusdata = (byte)(statusdata + 0x40);

            //statusdata = (byte)(statusdata + 0x20);

            if (brk_flag == 1)
                statusdata = (byte)(statusdata + 0x10);

            if (decimal_flag == 1)
                statusdata = (byte)(statusdata + 0x8);

            if (interrupt_flag == 1)
                statusdata = (byte)(statusdata + 0x4);

            if (zero_flag == 1)
                statusdata = (byte)(statusdata + 0x2);

            if (carry_flag == 1)
                statusdata = (byte)(statusdata + 0x1);

            Push8(statusdata);
        }
        public byte Pull8()
        {
            sp_register = (byte)(sp_register + 1);
            return _NesEmu.ReadMemory8((ushort)(0x100 + sp_register));
        }
        public ushort Pull16()
        {
            byte data1, data2;
            ushort fulldata;

            data1 = Pull8();
            data2 = Pull8();

            //We use MakeAddress because it's easier
            fulldata = MakeAddress(data1, data2);

            return fulldata;
        }
        public void PullStatus()
        {
            byte statusdata = Pull8();

            if ((statusdata & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            if ((statusdata & 0x40) == 0x40)
                overflow_flag = 1;
            else
                overflow_flag = 0;

            if ((statusdata & 0x10) == 0x10)
                brk_flag = 1;
            else
                brk_flag = 0;

            if ((statusdata & 0x8) == 0x8)
                decimal_flag = 1;
            else
                decimal_flag = 0;

            if ((statusdata & 0x4) == 0x4)
                interrupt_flag = 1;
            else
                interrupt_flag = 0;

            if ((statusdata & 0x2) == 0x2)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((statusdata & 0x1) == 0x1)
                carry_flag = 1;
            else
                carry_flag = 0;

        }
        #endregion
        #region Opcodes
        public void OpcodeADC()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            //Decode
            switch (currentOpcode)
            {
                case (0x69): valueholder = arg1; break;
                case (0x65): valueholder = ZeroPage(arg1); break;
                case (0x75): valueholder = ZeroPageX(arg1); break;
                case (0x6D): valueholder = Absolute(arg1, arg2); break;
                case (0x7D): valueholder = AbsoluteX(arg1, arg2, true); break;
                case (0x79): valueholder = AbsoluteY(arg1, arg2, true); break;
                case (0x61): valueholder = IndirectX(arg1); break;
                case (0x71): valueholder = IndirectY(arg1, true); break;
                default: _NesEmu.isQuitting = true; break;
            }

            //Execute
            uint valueholder32;
            valueholder32 = (uint)(a_register + valueholder + carry_flag);
            //valueholder32 = (uint)(a_register + valueholder);
            if (valueholder32 > 255)
            {
                carry_flag = 1;
                overflow_flag = 1;
            }
            else
            {
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

            a_register = (byte)(valueholder32 & 0xff);

            //Advance PC and tick count
            //FIXME: X and Y index overflow tick
            switch (currentOpcode)
            {
                case (0x69): tick_count += 2; pc_register += 2; break;
                case (0x65): tick_count += 3; pc_register += 2; break;
                case (0x75): tick_count += 4; pc_register += 2; break;
                case (0x6D): tick_count += 4; pc_register += 3; break;
                case (0x7D): tick_count += 4; pc_register += 3; break;
                case (0x79): tick_count += 4; pc_register += 3; break;
                case (0x61): tick_count += 6; pc_register += 2; break;
                case (0x71): tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; break;
            }
        }
        public void OpcodeAND()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0x29): valueholder = arg1; break;
                case (0x25): valueholder = ZeroPage(arg1); break;
                case (0x35): valueholder = ZeroPageX(arg1); break;
                case (0x2D): valueholder = Absolute(arg1, arg2); break;
                case (0x3D): valueholder = AbsoluteX(arg1, arg2, true); break;
                case (0x39): valueholder = AbsoluteY(arg1, arg2, true); break;
                case (0x21): valueholder = IndirectX(arg1); break;
                case (0x31): valueholder = IndirectY(arg1, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken AND"); break;
            }

            a_register = (byte)(a_register & valueholder);
            if ((a_register & 0xff) == 0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((a_register & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            switch (currentOpcode)
            {
                case (0x29): tick_count += 2; pc_register += 2; break;
                case (0x25): tick_count += 3; pc_register += 2; break;
                case (0x35): tick_count += 4; pc_register += 2; break;
                case (0x2D): tick_count += 3; pc_register += 3; break;
                case (0x3D): tick_count += 4; pc_register += 3; break;
                case (0x39): tick_count += 4; pc_register += 3; break;
                case (0x21): tick_count += 6; pc_register += 2; break;
                case (0x31): tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken AND"); break;
            }
        }
        public void OpcodeASL()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0x0a): valueholder = a_register; break;
                case (0x06): valueholder = ZeroPage(arg1); break;
                case (0x16): valueholder = ZeroPageX(arg1); break;
                case (0x0E): valueholder = Absolute(arg1, arg2); break;
                case (0x1E): valueholder = AbsoluteX(arg1, arg2, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ASL"); break;
            }
            if ((valueholder & 0x80) == 0x80)
                carry_flag = 1;
            else
                carry_flag = 0;

            valueholder = (byte)(valueholder << 1);

            if ((valueholder & 0xff) == 0x0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            //This one is a little different because we actually need
            //to do more than incrementing in the last step		
            switch (currentOpcode)
            {
                case (0x0a): a_register = valueholder;
                    tick_count += 2; pc_register += 1; break;
                case (0x06): ZeroPageWrite(arg1, valueholder);
                    tick_count += 5; pc_register += 2; break;
                case (0x16): ZeroPageXWrite(arg1, valueholder);
                    tick_count += 6; pc_register += 2; break;
                case (0x0E): AbsoluteWrite(arg1, arg2, valueholder);
                    tick_count += 6; pc_register += 3; break;
                case (0x1E): AbsoluteXWrite(arg1, arg2, valueholder);
                    tick_count += 7; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ASL"); break;
            }
        }
        public void OpcodeBCC()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (carry_flag == 0)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBCS()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (carry_flag == 1)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBEQ()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (zero_flag == 1)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBIT()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0x24): valueholder = ZeroPage(arg1); break;
                case (0x2c): valueholder = Absolute(arg1, arg2); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken BIT"); break;
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

            switch (currentOpcode)
            {
                case (0x24): tick_count += 3; pc_register += 2; break;
                case (0x2c): tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken BIT"); break;
            }
        }
        public void OpcodeBMI()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (sign_flag == 1)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBNE()
        {
            byte arg1;

            //FIX ME: All these are set "wrong" to match the old emulator
            //FIXME: They should probably all be corrected when debugging is finished 
            if (zero_flag == 0)
            {
                arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBPL()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (sign_flag == 0)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBRK()
        {
            pc_register = (ushort)(pc_register + 2);
            Push16(pc_register);
            brk_flag = 1;
            PushStatus();
            interrupt_flag = 1;
            pc_register = _NesEmu.ReadMemory16((ushort)0xfffe);
            tick_count += 7;
        }
        public void OpcodeBVC()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (overflow_flag == 0)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeBVS()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));

            //FIX ME: Branching to a new page takes a 1 tick penalty
            if (overflow_flag == 1)
            {
                pc_register += 2;
                if ((pc_register & 0xFF00) != ((pc_register + (sbyte)arg1 + 2) & 0xFF00))
                {
                    tick_count += 1;
                }
                pc_register = (ushort)(pc_register + (sbyte)arg1);
                tick_count += 1;
            }
            else
            {
                pc_register += 2;
            }
            tick_count += 2;
        }
        public void OpcodeCLC()
        {
            carry_flag = 0;
            pc_register += 1;
            tick_count += 2;
        }
        public void OpcodeCLD()
        {
            decimal_flag = 0;
            pc_register += 1;
            tick_count += 2;
        }
        public void OpcodeCLI()
        {
            interrupt_flag = 0;
            pc_register += 1;
            tick_count += 2;
        }
        public void OpcodeCLV()
        {
            overflow_flag = 0;
            pc_register += 1;
            tick_count += 2;
        }
        public void OpcodeCMP()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0xC9): valueholder = arg1; break;
                case (0xC5): valueholder = ZeroPage(arg1); break;
                case (0xD5): valueholder = ZeroPageX(arg1); break;
                case (0xCD): valueholder = Absolute(arg1, arg2); break;
                case (0xDD): valueholder = AbsoluteX(arg1, arg2, true); break;
                case (0xD9): valueholder = AbsoluteY(arg1, arg2, true); break;
                case (0xC1): valueholder = IndirectX(arg1); break;
                case (0xD1): valueholder = IndirectY(arg1, true); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken CMP"); break;
            }
            if (a_register >= valueholder)
                carry_flag = 1;
            else
                carry_flag = 0;

            valueholder = (byte)(a_register - valueholder);

            if (valueholder == 0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            //FIXME: X and Y index overflow tick
            switch (currentOpcode)
            {
                case (0xC9): tick_count += 2; pc_register += 2; break;
                case (0xC5): tick_count += 3; pc_register += 2; break;
                case (0xD5): tick_count += 4; pc_register += 2; break;
                case (0xCD): tick_count += 4; pc_register += 3; break;
                case (0xDD): tick_count += 4; pc_register += 3; break;
                case (0xD9): tick_count += 4; pc_register += 3; break;
                case (0xC1): tick_count += 6; pc_register += 2; break;
                case (0xD1): tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken CMP"); break;
            }
        }
        public void OpcodeCPX()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0xE0): valueholder = arg1; break;
                case (0xE4): valueholder = ZeroPage(arg1); break;
                case (0xEC): valueholder = Absolute(arg1, arg2); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken CPX"); break;
            }

            if (x_index_register >= valueholder)
                carry_flag = 1;
            else
                carry_flag = 0;

            valueholder = (byte)(x_index_register - valueholder);

            if (valueholder == 0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            switch (currentOpcode)
            {
                case (0xE0): tick_count += 2; pc_register += 2; break;
                case (0xE4): tick_count += 3; pc_register += 2; break;
                case (0xEC): tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken CPX"); break;
            }
        }
        public void OpcodeCPY()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0xC0): valueholder = arg1; break;
                case (0xC4): valueholder = ZeroPage(arg1); break;
                case (0xCC): valueholder = Absolute(arg1, arg2); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken CPY"); break;
            }

            if (y_index_register >= valueholder)
                carry_flag = 1;
            else
                carry_flag = 0;

            valueholder = (byte)(y_index_register - valueholder);

            if (valueholder == 0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            switch (currentOpcode)
            {
                case (0xC0): tick_count += 2; pc_register += 2; break;
                case (0xC4): tick_count += 3; pc_register += 2; break;
                case (0xCC): tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken CPY"); break;
            }
        }
        public void OpcodeDEC()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                //case (0xCA): valueholder = a_register; break;
                case (0xC6): valueholder = ZeroPage(arg1); break;
                case (0xD6): valueholder = ZeroPageX(arg1); break;
                case (0xCE): valueholder = Absolute(arg1, arg2); break;
                case (0xDE): valueholder = AbsoluteX(arg1, arg2, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken DEC"); break;
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

            //This one is a little different because we actually need
            //to do more than incrementing in the last step		
            switch (currentOpcode)
            {
                //case (0xCA): a_register = valueholder; 
                //	tick_count += 2; pc_register += 1; break;
                case (0xC6): ZeroPageWrite(arg1, valueholder);
                    tick_count += 5; pc_register += 2; break;
                case (0xD6): ZeroPageXWrite(arg1, valueholder);
                    tick_count += 6; pc_register += 2; break;
                case (0xCE): AbsoluteWrite(arg1, arg2, valueholder);
                    tick_count += 6; pc_register += 3; break;
                case (0xDE): AbsoluteXWrite(arg1, arg2, valueholder);
                    tick_count += 7; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken DEC"); break;
            }
        }
        public void OpcodeDEX()
        {
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
        public void OpcodeDEY()
        {
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
        public void OpcodeEOR()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0x49): valueholder = arg1; break;
                case (0x45): valueholder = ZeroPage(arg1); break;
                case (0x55): valueholder = ZeroPageX(arg1); break;
                case (0x4D): valueholder = Absolute(arg1, arg2); break;
                case (0x5D): valueholder = AbsoluteX(arg1, arg2, true); break;
                case (0x59): valueholder = AbsoluteY(arg1, arg2, true); break;
                case (0x41): valueholder = IndirectX(arg1); break;
                case (0x51): valueholder = IndirectY(arg1, true); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken EOR"); break;
            }

            a_register = (byte)(a_register ^ valueholder);
            if ((a_register & 0xff) == 0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((a_register & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            switch (currentOpcode)
            {
                case (0x49): tick_count += 2; pc_register += 2; break;
                case (0x45): tick_count += 3; pc_register += 2; break;
                case (0x55): tick_count += 4; pc_register += 2; break;
                case (0x4D): tick_count += 3; pc_register += 3; break;
                case (0x5D): tick_count += 4; pc_register += 3; break;
                case (0x59): tick_count += 4; pc_register += 3; break;
                case (0x41): tick_count += 6; pc_register += 2; break;
                case (0x51): tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken EOR"); break;
            }
        }
        public void OpcodeINC()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                //case (0xCA): valueholder = a_register; break;
                case (0xE6): valueholder = ZeroPage(arg1); break;
                case (0xF6): valueholder = ZeroPageX(arg1); break;
                case (0xEE): valueholder = Absolute(arg1, arg2); break;
                case (0xFE): valueholder = AbsoluteX(arg1, arg2, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken INC"); break;
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

            //This one is a little different because we actually need
            //to do more than incrementing in the last step		
            switch (currentOpcode)
            {
                //case (0xCA): a_register = valueholder; 
                //	tick_count += 2; pc_register += 1; break;
                case (0xE6): ZeroPageWrite(arg1, valueholder);
                    tick_count += 5; pc_register += 2; break;
                case (0xF6): ZeroPageXWrite(arg1, valueholder);
                    tick_count += 6; pc_register += 2; break;
                case (0xEE): AbsoluteWrite(arg1, arg2, valueholder);
                    tick_count += 6; pc_register += 3; break;
                case (0xFE): AbsoluteXWrite(arg1, arg2, valueholder);
                    tick_count += 7; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken INC"); break;
            }
        }
        public void OpcodeINX()
        {
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
        public void OpcodeINY()
        {
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
        public void OpcodeJMP()
        {
            //byte arg1 = myEngine.ReadMemory8((ushort)(pc_register + 1));
            //byte arg2 = myEngine.ReadMemory8((ushort)(pc_register + 2));
            ushort myAddress = _NesEmu.ReadMemory16((ushort)(pc_register + 1));

            switch (currentOpcode)
            {
                case (0x4c): //pc_register = MakeAddress(arg1, arg2); 
                    pc_register = myAddress;
                    //Console.WriteLine("Jumping to: {0:x}", pc_register);
                    tick_count += 3; break;
                case (0x6c): //pc_register = myEngine.ReadMemory16(MakeAddress(arg1, arg2));
                    pc_register = _NesEmu.ReadMemory16(myAddress);
                    //Console.WriteLine("Jumping to: {0:x}", pc_register);
                    tick_count += 5; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken JMP"); break;
            }
        }
        public void OpcodeJSR()
        {
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            Push16((ushort)(pc_register + 2));
            pc_register = MakeAddress(arg1, arg2);
            tick_count += 6;
        }
        public void OpcodeLDA()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2;
            switch (currentOpcode)
            {
                case (0xA9): a_register = arg1;
                    tick_count += 2; pc_register += 2; break;
                case (0xA5): a_register = ZeroPage(arg1);
                    tick_count += 3; pc_register += 2; break;
                case (0xB5): a_register = ZeroPageX(arg1);
                    tick_count += 4; pc_register += 2; break;
                case (0xAD): arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
                    a_register = Absolute(arg1, arg2);
                    tick_count += 4; pc_register += 3; break;
                case (0xBD): arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
                    a_register = AbsoluteX(arg1, arg2, true);     //CHECK FOR PAGE BOUNDARIES


                    tick_count += 4; pc_register += 3; break;
                case (0xB9): arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
                    a_register = AbsoluteY(arg1, arg2, true);     //CHECK FOR PAGE BOUNDARIES
                    tick_count += 4; pc_register += 3; break;
                case (0xA1): a_register = IndirectX(arg1);
                    tick_count += 6; pc_register += 2; break;
                case (0xB1): a_register = IndirectY(arg1, true);     //CHECK FOR PAGE BOUNDARIES
                    tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken LDA"); break;
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
        public void OpcodeLDX()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            switch (currentOpcode)
            {
                case (0xA2): x_index_register = arg1;
                    tick_count += 2; pc_register += 2; break;
                case (0xA6): x_index_register = ZeroPage(arg1);
                    tick_count += 3; pc_register += 2; break;
                case (0xB6): x_index_register = ZeroPageY(arg1);
                    tick_count += 4; pc_register += 2; break;
                case (0xAE): x_index_register = Absolute(arg1, arg2);
                    tick_count += 4; pc_register += 3; break;
                case (0xBE): x_index_register = AbsoluteY(arg1, arg2, true);
                    tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken LDX"); break;
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
        public void OpcodeLDY()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            //byte valueholder = 0xff;
            switch (currentOpcode)
            {
                case (0xA0): y_index_register = arg1;
                    tick_count += 2; pc_register += 2; break;
                case (0xA4): y_index_register = ZeroPage(arg1);
                    tick_count += 3; pc_register += 2; break;
                case (0xB4): y_index_register = ZeroPageX(arg1);
                    tick_count += 4; pc_register += 2; break;
                case (0xAC): y_index_register = Absolute(arg1, arg2);
                    tick_count += 4; pc_register += 3; break;
                case (0xBC): y_index_register = AbsoluteX(arg1, arg2, true);
                    tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken LDY"); break;
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
        public void OpcodeLSR()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0x4a): valueholder = a_register; break;
                case (0x46): valueholder = ZeroPage(arg1); break;
                case (0x56): valueholder = ZeroPageX(arg1); break;
                case (0x4E): valueholder = Absolute(arg1, arg2); break;
                case (0x5E): valueholder = AbsoluteX(arg1, arg2, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken LSR"); break;
            }
            if ((valueholder & 0x1) == 0x1)
                carry_flag = 1;
            else
                carry_flag = 0;

            valueholder = (byte)(valueholder >> 1);

            if ((valueholder & 0xff) == 0x0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            //This one is a little different because we actually need
            //to do more than incrementing in the last step		
            switch (currentOpcode)
            {
                case (0x4a): a_register = valueholder;
                    tick_count += 2; pc_register += 1; break;
                case (0x46): ZeroPageWrite(arg1, valueholder);
                    tick_count += 5; pc_register += 2; break;
                case (0x56): ZeroPageXWrite(arg1, valueholder);
                    tick_count += 6; pc_register += 2; break;
                case (0x4E): AbsoluteWrite(arg1, arg2, valueholder);
                    tick_count += 6; pc_register += 3; break;
                case (0x5E): AbsoluteXWrite(arg1, arg2, valueholder);
                    tick_count += 7; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken LSR"); break;
            }
        }
        public void OpcodeNOP()
        {
            if (currentOpcode != 0xEA)
            {
                //Console.WriteLine("Illegal Instruction");
                //myEngine.isQuitting = true;
            }
            pc_register += 1;
            tick_count += 2;
        }
        public void OpcodeORA()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            switch (currentOpcode)
            {
                case (0x09): valueholder = arg1; break;
                case (0x05): valueholder = ZeroPage(arg1); break;
                case (0x15): valueholder = ZeroPageX(arg1); break;
                case (0x0D): valueholder = Absolute(arg1, arg2); break;
                case (0x1D): valueholder = AbsoluteX(arg1, arg2, true); break;
                case (0x19): valueholder = AbsoluteY(arg1, arg2, true); break;
                case (0x01): valueholder = IndirectX(arg1); break;
                case (0x11): valueholder = IndirectY(arg1, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ORA"); break;
            }

            a_register = (byte)(a_register | valueholder);
            if ((a_register & 0xff) == 0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((a_register & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            //FIXME: X and Y index overflow tick
            switch (currentOpcode)
            {
                case (0x09): tick_count += 2; pc_register += 2; break;
                case (0x05): tick_count += 3; pc_register += 2; break;
                case (0x15): tick_count += 4; pc_register += 2; break;
                case (0x0D): tick_count += 4; pc_register += 3; break;
                case (0x1D): tick_count += 4; pc_register += 3; break;
                case (0x19): tick_count += 4; pc_register += 3; break;
                case (0x01): tick_count += 6; pc_register += 2; break;
                case (0x11): tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ORA"); break;
            }
        }
        public void OpcodePHA()
        {
            Push8(a_register);
            pc_register += 1;
            tick_count += 3;
        }
        public void OpcodePHP()
        {
            PushStatus();
            pc_register += 1;
            tick_count += 3;
        }
        public void OpcodePLA()
        {
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
        public void OpcodePLP()
        {
            PullStatus();
            pc_register += 1;
            tick_count += 4;
        }
        public void OpcodeROL()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;
            byte bitholder = 0;

            switch (currentOpcode)
            {
                case (0x2a): valueholder = a_register; break;
                case (0x26): valueholder = ZeroPage(arg1); break;
                case (0x36): valueholder = ZeroPageX(arg1); break;
                case (0x2E): valueholder = Absolute(arg1, arg2); break;
                case (0x3E): valueholder = AbsoluteX(arg1, arg2, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ROL"); break;
            }
            if ((valueholder & 0x80) == 0x80)
                bitholder = 1;
            else
                bitholder = 0;

            valueholder = (byte)(valueholder << 1);
            valueholder = (byte)(valueholder | carry_flag);

            carry_flag = bitholder;

            if ((valueholder & 0xff) == 0x0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            //This one is a little different because we actually need
            //to do more than incrementing in the last step		
            switch (currentOpcode)
            {
                case (0x2a): a_register = valueholder;
                    tick_count += 2; pc_register += 1; break;
                case (0x26): ZeroPageWrite(arg1, valueholder);
                    tick_count += 5; pc_register += 2; break;
                case (0x36): ZeroPageXWrite(arg1, valueholder);
                    tick_count += 6; pc_register += 2; break;
                case (0x2E): AbsoluteWrite(arg1, arg2, valueholder);
                    tick_count += 6; pc_register += 3; break;
                case (0x3E): AbsoluteXWrite(arg1, arg2, valueholder);
                    tick_count += 7; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ROL"); break;
            }
        }
        public void OpcodeROR()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;
            byte bitholder = 0;

            switch (currentOpcode)
            {
                case (0x6a): valueholder = a_register; break;
                case (0x66): valueholder = ZeroPage(arg1); break;
                case (0x76): valueholder = ZeroPageX(arg1); break;
                case (0x6E): valueholder = Absolute(arg1, arg2); break;
                case (0x7E): valueholder = AbsoluteX(arg1, arg2, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ROR"); break;
            }

            if ((valueholder & 0x1) == 0x1)
                bitholder = 1;
            else
                bitholder = 0;

            valueholder = (byte)(valueholder >> 1);

            if (carry_flag == 1)
                valueholder = (byte)(valueholder | 0x80);

            carry_flag = bitholder;

            if ((valueholder & 0xff) == 0x0)
                zero_flag = 1;
            else
                zero_flag = 0;

            if ((valueholder & 0x80) == 0x80)
                sign_flag = 1;
            else
                sign_flag = 0;

            //This one is a little different because we actually need
            //to do more than incrementing in the last step		
            switch (currentOpcode)
            {
                case (0x6a): a_register = valueholder;
                    tick_count += 2; pc_register += 1; break;
                case (0x66): ZeroPageWrite(arg1, valueholder);
                    tick_count += 5; pc_register += 2; break;
                case (0x76): ZeroPageXWrite(arg1, valueholder);
                    tick_count += 6; pc_register += 2; break;
                case (0x6E): AbsoluteWrite(arg1, arg2, valueholder);
                    tick_count += 6; pc_register += 3; break;
                case (0x7E): AbsoluteXWrite(arg1, arg2, valueholder);
                    tick_count += 7; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken ROR"); break;
            }
        }
        public void OpcodeRTI()
        {
            PullStatus();
            pc_register = Pull16();
            tick_count += 6;
        }
        public void OpcodeRTS()
        {
            pc_register = Pull16();
            tick_count += 6;
            pc_register += 1;
        }
        public void OpcodeSBC()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            byte valueholder = 0xff;

            //Decode
            switch (currentOpcode)
            {
                case (0xE9): valueholder = arg1; break;
                case (0xE5): valueholder = ZeroPage(arg1); break;
                case (0xF5): valueholder = ZeroPageX(arg1); break;
                case (0xED): valueholder = Absolute(arg1, arg2); break;
                case (0xFD): valueholder = AbsoluteX(arg1, arg2, true); break;
                case (0xF9): valueholder = AbsoluteY(arg1, arg2, true); break;
                case (0xE1): valueholder = IndirectX(arg1); break;
                case (0xF1): valueholder = IndirectY(arg1, false); break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken SBC"); break;
            }

            //Execute
            uint valueholder32;
            valueholder32 = (uint)(a_register - valueholder);
            if (carry_flag == 0)
                valueholder32 = valueholder32 - 1;

            if (valueholder32 > 255)
            {
                carry_flag = 0;
                overflow_flag = 1;
            }
            else
            {
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

            a_register = (byte)(valueholder32 & 0xff);

            //Advance PC and tick count
            //FIXME: X and Y index overflow tick
            switch (currentOpcode)
            {
                case (0xE9): tick_count += 2; pc_register += 2; break;
                case (0xE5): tick_count += 3; pc_register += 2; break;
                case (0xF5): tick_count += 4; pc_register += 2; break;
                case (0xED): tick_count += 4; pc_register += 3; break;
                case (0xFD): tick_count += 4; pc_register += 3; break;
                case (0xF9): tick_count += 4; pc_register += 3; break;
                case (0xE1): tick_count += 6; pc_register += 2; break;
                case (0xF1): tick_count += 5; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken SBC"); break;
            }
        }
        public void OpcodeSEC()
        {
            carry_flag = 1;
            tick_count += 2;
            pc_register += 1;
        }
        public void OpcodeSED()
        {
            decimal_flag = 1;
            tick_count += 2;
            pc_register += 1;
        }
        public void OpcodeSEI()
        {
            interrupt_flag = 1;
            tick_count += 2;
            pc_register += 1;
        }
        public void OpcodeSTA()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            //Decode
            switch (currentOpcode)
            {
                case (0x85): ZeroPageWrite(arg1, a_register);
                    tick_count += 3; pc_register += 2; break;
                case (0x95): ZeroPageXWrite(arg1, a_register);
                    tick_count += 4; pc_register += 2; break;
                case (0x8D): AbsoluteWrite(arg1, arg2, a_register);
                    tick_count += 4; pc_register += 3; break;
                case (0x9D): AbsoluteXWrite(arg1, arg2, a_register);
                    tick_count += 5; pc_register += 3; break;
                case (0x99): AbsoluteYWrite(arg1, arg2, a_register);
                    tick_count += 5; pc_register += 3; break;
                case (0x81): IndirectXWrite(arg1, a_register);
                    tick_count += 6; pc_register += 2; break;
                case (0x91): IndirectYWrite(arg1, a_register);
                    tick_count += 6; pc_register += 2; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken STA"); break;
            }
        }
        public void OpcodeSTX()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            //Decode
            switch (currentOpcode)
            {
                case (0x86): ZeroPageWrite(arg1, x_index_register);
                    tick_count += 3; pc_register += 2; break;
                case (0x96): ZeroPageYWrite(arg1, x_index_register);
                    tick_count += 4; pc_register += 2; break;
                case (0x8E): AbsoluteWrite(arg1, arg2, x_index_register);
                    tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken STX"); break;
            }
        }
        public void OpcodeSTY()
        {
            // We may not use both, but it's easier to grab them now
            byte arg1 = _NesEmu.ReadMemory8((ushort)(pc_register + 1));
            byte arg2 = _NesEmu.ReadMemory8((ushort)(pc_register + 2));
            //Decode
            switch (currentOpcode)
            {
                case (0x84): ZeroPageWrite(arg1, y_index_register);
                    tick_count += 3; pc_register += 2; break;
                case (0x94): ZeroPageXWrite(arg1, y_index_register);
                    tick_count += 4; pc_register += 2; break;
                case (0x8C): AbsoluteWrite(arg1, arg2, y_index_register);
                    tick_count += 4; pc_register += 3; break;
                default: _NesEmu.isQuitting = true; Console.WriteLine("Broken STY"); break;
            }
        }
        public void OpcodeTAX()
        {
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
        public void OpcodeTAY()
        {
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
        public void OpcodeTSX()
        {
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
        public void OpcodeTXA()
        {
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
        public void OpcodeTXS()
        {
            sp_register = x_index_register;

            pc_register += 1;
            tick_count += 2;
        }
        public void OpcodeTYA()
        {
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
        #endregion

        [System.Runtime.InteropServices.DllImport("Kernel32.dll")]
        static extern bool QueryPerformanceCounter(ref long count);
        [System.Runtime.InteropServices.DllImport("Kernel32.dll")]
        static extern bool QueryPerformanceFrequency(ref long count);


        static long freq = 0;

        /// <summary>
        /// 取得记时开始
        /// </summary>
        /// <returns></returns>
        public static long GetStartTick()
        {
            long count = 0;
            if (freq == 0)
                QueryPerformanceFrequency(ref freq);
            QueryPerformanceCounter(ref count);
            return count;
        }

        /// <summary>
        /// 取得时间跨度(单位秒)
        /// </summary>
        /// <param name="startTick"></param>
        /// <returns></returns>
        public static string GetSpanSecond(long startTick)
        {
            long count1 = 0;
            QueryPerformanceCounter(ref count1);
            long count = count1 - startTick;
            return ((double)(count) / (double)freq).ToString();
        }

        int i = 0;
        int num = 0;
        StringBuilder sb = new StringBuilder();
        public void RunProcessor()
        {

            previousPC = pc_register;
            long s1 = GetStartTick();
            while (!_NesEmu.isQuitting)
            {
            //    if (a_register == 30)
            //    {

            //    }
            //    if (pc_register == 303778)
            //    {
            //       // System.Console.WriteLine(pc_register);
            //        i++;
            //    }
            //    if (num == 303949)
            //    {
            //        // System.Console.WriteLine(pc_register);

            //    }
            //    if (num >= 300000 && num < 400000)
            //    {
            //        int val = carry_flag | zero_flag << 1 | interrupt_flag << 2 | decimal_flag << 3 | brk_flag << 4 | overflow_flag << 5 | sign_flag << 6;
                
            //        sb.Append(num + "\t" + pc_register + "\t" + a_register + "\t" + x_index_register + "\t" + y_index_register + "\t" + sp_register +"\t" + val+ "\r\n");
            //          }
            //    else if (num == 400000)
            //{
            //    File.WriteAllText("E:\\s2.txt", sb.ToString());
            //}
            //    num++;
                currentOpcode = _NesEmu.ReadMemory8(pc_register);
                if (!_NesEmu.isPaused)
                {
                    #region Do Opcode
                    switch (currentOpcode)
                    {
                        case (0x00): OpcodeBRK(); break;
                        case (0x01): OpcodeORA(); break;
                        case (0x05): OpcodeORA(); break;
                        case (0x06): OpcodeASL(); break;
                        case (0x08): OpcodePHP(); break;
                        case (0x09): OpcodeORA(); break;
                        case (0x0a): OpcodeASL(); break;
                        case (0x0d): OpcodeORA(); break;
                        case (0x0e): OpcodeASL(); break;
                        case (0x10): OpcodeBPL(); break;
                        case (0x11): OpcodeORA(); break;
                        case (0x15): OpcodeORA(); break;
                        case (0x16): OpcodeASL(); break;
                        case (0x18): OpcodeCLC(); break;
                        case (0x19): OpcodeORA(); break;
                        case (0x1d): OpcodeORA(); break;
                        case (0x1e): OpcodeASL(); break;
                        case (0x20): OpcodeJSR(); break;
                        case (0x21): OpcodeAND(); break;
                        case (0x24): OpcodeBIT(); break;
                        case (0x25): OpcodeAND(); break;
                        case (0x26): OpcodeROL(); break;
                        case (0x28): OpcodePLP(); break;
                        case (0x29): OpcodeAND(); break;
                        case (0x2a): OpcodeROL(); break;
                        case (0x2c): OpcodeBIT(); break;
                        case (0x2d): OpcodeAND(); break;
                        case (0x2e): OpcodeROL(); break;
                        case (0x30): OpcodeBMI(); break;
                        case (0x31): OpcodeAND(); break;
                        case (0x32): OpcodeNOP(); break;
                        case (0x33): OpcodeNOP(); break;
                        case (0x34): OpcodeNOP(); break;
                        case (0x35): OpcodeAND(); break;
                        case (0x36): OpcodeROL(); break;
                        case (0x38): OpcodeSEC(); break;
                        case (0x39): OpcodeAND(); break;
                        case (0x3d): OpcodeAND(); break;
                        case (0x3e): OpcodeROL(); break;
                        case (0x40): OpcodeRTI(); break;
                        case (0x41): OpcodeEOR(); break;
                        case (0x45): OpcodeEOR(); break;
                        case (0x46): OpcodeLSR(); break;
                        case (0x48): OpcodePHA(); break;
                        case (0x49): OpcodeEOR(); break;
                        case (0x4a): OpcodeLSR(); break;
                        case (0x4c): OpcodeJMP(); break;
                        case (0x4d): OpcodeEOR(); break;
                        case (0x4e): OpcodeLSR(); break;
                        case (0x50): OpcodeBVC(); break;
                        case (0x51): OpcodeEOR(); break;
                        case (0x55): OpcodeEOR(); break;
                        case (0x56): OpcodeLSR(); break;
                        case (0x58): OpcodeCLI(); break;
                        case (0x59): OpcodeEOR(); break;
                        case (0x5d): OpcodeEOR(); break;
                        case (0x5e): OpcodeLSR(); break;
                        case (0x60): OpcodeRTS(); break;
                        case (0x61): OpcodeADC(); break;
                        case (0x65): OpcodeADC(); break;
                        case (0x66): OpcodeROR(); break;
                        case (0x68): OpcodePLA(); break;
                        case (0x69): OpcodeADC(); break;
                        case (0x6a): OpcodeROR(); break;
                        case (0x6c): OpcodeJMP(); break;
                        case (0x6d): OpcodeADC(); break;
                        case (0x6e): OpcodeROR(); break;
                        case (0x70): OpcodeBVS(); break;
                        case (0x71): OpcodeADC(); break;
                        case (0x75): OpcodeADC(); break;
                        case (0x76): OpcodeROR(); break;
                        case (0x78): OpcodeSEI(); break;
                        case (0x79): OpcodeADC(); break;
                        case (0x7d): OpcodeADC(); break;
                        case (0x7e): OpcodeROR(); break;
                        case (0x81): OpcodeSTA(); break;
                        case (0x84): OpcodeSTY(); break;
                        case (0x85): OpcodeSTA(); break;
                        case (0x86): OpcodeSTX(); break;
                        case (0x88): OpcodeDEY(); break;
                        case (0x8a): OpcodeTXA(); break;
                        case (0x8c): OpcodeSTY(); break;
                        case (0x8d): OpcodeSTA(); break;
                        case (0x8e): OpcodeSTX(); break;
                        case (0x90): OpcodeBCC(); break;
                        case (0x91): OpcodeSTA(); break;
                        case (0x94): OpcodeSTY(); break;
                        case (0x95): OpcodeSTA(); break;
                        case (0x96): OpcodeSTX(); break;
                        case (0x98): OpcodeTYA(); break;
                        case (0x99): OpcodeSTA(); break;
                        case (0x9a): OpcodeTXS(); break;
                        case (0x9d): OpcodeSTA(); break;
                        case (0xa0): OpcodeLDY(); break;
                        case (0xa1): OpcodeLDA(); break;
                        case (0xa2): OpcodeLDX(); break;
                        case (0xa4): OpcodeLDY(); break;
                        case (0xa5): OpcodeLDA(); break;
                        case (0xa6): OpcodeLDX(); break;
                        case (0xa8): OpcodeTAY(); break;
                        case (0xa9): OpcodeLDA(); break;
                        case (0xaa): OpcodeTAX(); break;
                        case (0xac): OpcodeLDY(); break;
                        case (0xad): OpcodeLDA(); break;
                        case (0xae): OpcodeLDX(); break;
                        case (0xb0): OpcodeBCS(); break;
                        case (0xb1): OpcodeLDA(); break;
                        case (0xb4): OpcodeLDY(); break;
                        case (0xb5): OpcodeLDA(); break;
                        case (0xb6): OpcodeLDX(); break;
                        case (0xb8): OpcodeCLV(); break;
                        case (0xb9): OpcodeLDA(); break;
                        case (0xba): OpcodeTSX(); break;
                        case (0xbc): OpcodeLDY(); break;
                        case (0xbd): OpcodeLDA(); break;
                        case (0xbe): OpcodeLDX(); break;
                        case (0xc0): OpcodeCPY(); break;
                        case (0xc1): OpcodeCMP(); break;
                        case (0xc4): OpcodeCPY(); break;
                        case (0xc5): OpcodeCMP(); break;
                        case (0xc6): OpcodeDEC(); break;
                        case (0xc8): OpcodeINY(); break;
                        case (0xc9): OpcodeCMP(); break;
                        case (0xca): OpcodeDEX(); break;
                        case (0xcc): OpcodeCPY(); break;
                        case (0xcd): OpcodeCMP(); break;
                        case (0xce): OpcodeDEC(); break;
                        case (0xd0): OpcodeBNE(); break;
                        case (0xd1): OpcodeCMP(); break;
                        case (0xd5): OpcodeCMP(); break;
                        case (0xd6): OpcodeDEC(); break;
                        case (0xd8): OpcodeCLD(); break;
                        case (0xd9): OpcodeCMP(); break;
                        case (0xdd): OpcodeCMP(); break;
                        case (0xde): OpcodeDEC(); break;
                        case (0xe0): OpcodeCPX(); break;
                        case (0xe1): OpcodeSBC(); break;
                        case (0xe4): OpcodeCPX(); break;
                        case (0xe5): OpcodeSBC(); break;
                        case (0xe6): OpcodeINC(); break;
                        case (0xe8): OpcodeINX(); break;
                        case (0xe9): OpcodeSBC(); break;
                        case (0xec): OpcodeCPX(); break;
                        case (0xed): OpcodeSBC(); break;
                        case (0xee): OpcodeINC(); break;
                        case (0xf0): OpcodeBEQ(); break;
                        case (0xf1): OpcodeSBC(); break;
                        case (0xf5): OpcodeSBC(); break;
                        case (0xf6): OpcodeINC(); break;
                        case (0xf8): OpcodeSED(); break;
                        case (0xf9): OpcodeSBC(); break;
                        case (0xfd): OpcodeSBC(); break;
                        case (0xfe): OpcodeINC(); break;
                        default: OpcodeNOP(); break;
                    };
                    #endregion
                    _NesEmu.myAPU.Play();//if the sound is paused.
                }
                else
                {
                    _NesEmu.myAPU.Pause();
                    Thread.Sleep(100);
                }
                total_cycles += tick_count;
                if (tick_count >= _NesEmu.Ticks_Per_Scanline)
                {
                    if (_NesEmu.myPPU.RenderNextScanline())
                    {
                        Push16(pc_register);
                        PushStatus();
                        pc_register = _NesEmu.ReadMemory16(0xFFFA);
                    }
                    tick_count = tick_count - _NesEmu.Ticks_Per_Scanline;
                }

                //if (num == 100000)
                //{
                //    System.Console.WriteLine("用时:" + GetSpanSecond(s1));
                //    s1 = GetStartTick();
                //    num = 0;
                //}
                //num++;
            }
        }
    }
}