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
using System.Linq;
using System.Text;
using System.IO;
namespace AHD.MyNes.Nes
{
    /// <summary>
    /// Class for reading the rom header ONLY
    /// </summary>
    public class CartHeaderReader
    {
        /*
         * This class used only for reading the header of INES
         * rom format file, so you can use it ONLY for browser or
         * to check the validate of rom.
         * IMPORTANT : when you add / update a mapper
         * don't forget to assign it here 
         * (in SupportedMappersNo & SupportedMapper())
         * to make it available.
         */
        // Fields
        public int ChrRomPageCount;
        public bool FourScreenVRAMLayout;
        public int MemoryMapper;
        public int PrgRomPageCount;
        public bool SRamEnabled;
        public bool TrainerPresent512;
        public bool validRom;
        public bool VerticalMirroring;
        public string FilePath = "";
        public CartHeaderReader(string fileName)
        {
            FilePath = fileName; try
            {
                using (Stream stream = new FileStream(fileName, FileMode.Open))
                {
                    Encoding aSCII = Encoding.ASCII;
                    byte[] buffer = new byte[0x80];
                    stream.Read(buffer, 0, 3);
                    this.validRom = true;
                    if (aSCII.GetString(buffer, 0, 3) != "NES")
                    {
                        this.validRom = false;
                    }
                    if (stream.ReadByte() != 0x1a)
                    {
                        this.validRom = false;
                    }
                    this.PrgRomPageCount = stream.ReadByte();
                    this.ChrRomPageCount = stream.ReadByte();
                    int num = stream.ReadByte();

                    this.VerticalMirroring = (num & 1) == 1;
                    this.SRamEnabled = (num & 2) == 2;
                    this.TrainerPresent512 = (num & 4) == 4;
                    this.FourScreenVRAMLayout = (num & 8) == 8;

                    this.MemoryMapper = num >> 4;
                    int num2 = stream.ReadByte();
                    if ((num2 & 15) != 0)
                    {
                        num2 = 0;
                    }
                    this.MemoryMapper |= num2 & 240;
                    stream.Read(buffer, 0, 8);
                    stream.Close();
                }
            }
            catch { validRom = false; }
        }
        public static int[] SupportedMappersNo
        {
            get
            {
                int[] mappersNo = { 0, 1, 2, 3, 4, 7, 9, 10, 11, 13, 15, 16,
                                      18, 22,32, 34,41, 64, 66, 71 ,225, 255};
                return mappersNo;
            }
        }
        public bool SupportedMapper()
        {
            switch (MemoryMapper)
            {
                case 0: return true;
                case 1: return true;
                case 2: return true;
                case 3: return true;
                case 4: return true;
                case 5: return true;
                case 7: return true;
                case 9: return true;
                case 10: return true;
                case 11: return true;
                case 13: return true;
                case 15: return true;
                case 16: return true;
                case 18: return true;
                case 22: return true;
                case 32: return true;
                case 34: return true;
                case 64: return true;
                case 41: return true;
                case 66: return true;
                case 71: return true;
                case 225: return true;
                case 255: return true;
                default: return false;
            }
        }
        public string GetMapperName()
        {
            switch (MemoryMapper)
            {
                case 0: return "NROM, no mapper";
                case 1: return "MMC1";
                case 2: return "UNROM";
                case 3: return "CNROM";
                case 4: return "MMC3";
                case 5: return "MMC5";
                case 6: return "FFE F4xxx";
                case 7: return "AOROM";
                case 8: return "FFE F3xxx";
                case 9: return "MMC2";
                case 10: return "MMC4";
                case 11: return "ColorDreams chip";
                case 12: return "FFE F6xxx";
                case 13: return "ColorDreams chip";
                case 15: return "100-in-1 switch";
                case 16: return "Bandai chip";
                case 17: return "FFE F8xxx";
                case 18: return "Jaleco SS8806 chip";
                case 19: return "Namcot 106 chip";
                case 20: return "Nintendo DiskSystem";
                case 21: return "Konami VRC4a";
                case 22: return "Konami VRC2a";
                case 23: return "Konami VRC2a";
                case 24: return "Konami VRC6";
                case 25: return "Konami VRC4b";
                case 32: return "Irem G-101 chip";
                case 33: return "Taito TC0190/TC0350";
                case 34: return "32 KB ROM switch";
                case 41: return "Caltron 6-in-1";
                case 64: return "Tengen RAMBO-1 chip";
                case 65: return "Irem H-3001 chip";
                case 66: return "GNROM";
                case 67: return "SunSoft3 chip";
                case 68: return "SunSoft4 chip";
                case 69: return "SunSoft5 FME-7 chip";
                case 71: return "Camerica chip";
                case 78: return "Irem 74HC161/32-based";
                case 91: return "Pirate HK-SF3 chip";
                case 225: return "X-in-1";
                case 255: return "X-in-1";
                default: return "???";
            }
        }
    }
}
