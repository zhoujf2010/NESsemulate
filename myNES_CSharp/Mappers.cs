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

namespace AHD.MyNes.Nes
{
    /// <summary>
    /// Class of mappers
    /// </summary>
    public class Mappers
    {
        /*
         * IMPORTANT : when you add / update a mapper
         * don't forget to assign it in CartHeaderReader.cs
         * (in SupportedMappersNo & SupportedMapper())
         * to make it available.
         * Press " CTRL + M then O " to collapes.
         */
        public Cart mapperCartridge;
        public NesEmulator myEngine;
        //IMapper CurrentMapper;
        //Mapper9 map9;
        //Mapper10 map10;
        public uint[] current_prg_rom_page;
        public uint[] current_chr_rom_page;
        public byte ReadChrRom(ushort address)
        {
            byte returnvalue = 0xff;

            if (address < 0x400)
            {
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[0]][address];
            }
            else if (address < 0x800)
            {
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[1]][address - 0x400];
            }
            else if (address < 0xC00)
            {
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[2]][address - 0x800];
            }
            else if (address < 0x1000)
            {
                //if (mapperCartridge.mapper == 9)
                //{

                //    if ((address >= 0xfd0) && (address <= 0xfdf))
                //    {
                //        map9.latch1 = 0xfd;
                //        Switch4kChrRom(map9.latch1data1, 1);
                //    }
                //    else if ((address >= 0xfe0) && (address <= 0xfef))
                //    {
                //        map9.latch1 = 0xfe;
                //        Switch4kChrRom(map9.latch1data2, 1);
                //    }
                //}
                //else if (mapperCartridge.mapper == 10)
                //{
                //    if ((address >= 0xfd0) && (address <= 0xfdf))
                //    {
                //        map10.latch1 = 0xfd;
                //        Switch4kChrRom(map10.latch1data1, 0);
                //    }
                //    else if ((address >= 0xfe0) && (address <= 0xfef))
                //    {
                //        map10.latch1 = 0xfe;
                //        Switch4kChrRom(map10.latch1data2, 0);
                //    }
                //}
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[3]][address - 0xC00];
            }
            else if (address < 0x1400)
            {
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[4]][address - 0x1000];
            }
            else if (address < 0x1800)
            {
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[5]][address - 0x1400];
            }
            else if (address < 0x1C00)
            {
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[6]][address - 0x1800];
            }
            else
            {
                if (mapperCartridge.mapper == 9)
                {
                    //if ((address >= 0x1fd0) && (address <= 0x1fdf))
                    //{
                    //    map9.latch1 = 0xfd;
                    //    Switch4kChrRom(map9.latch1data1, 1);
                    //}
                    //else if ((address >= 0x1fe0) && (address <= 0x1fef))
                    //{
                    //    map9.latch1 = 0xfe;
                    //    Switch4kChrRom(map9.latch1data2, 1);
                    //}
                }
                else if (mapperCartridge.mapper == 10)
                {
                    //if ((address >= 0x1fd0) && (address <= 0x1fdf))
                    //{
                    //    map10.latch2 = 0xfd;
                    //    Switch4kChrRom(map10.latch2data1, 1);
                    //}
                    //else if ((address >= 0x1fe0) && (address <= 0x1fef))
                    //{
                    //    map10.latch2 = 0xfe;
                    //    Switch4kChrRom(map10.latch2data2, 1);
                    //}
                }
                returnvalue = mapperCartridge.chr_rom[current_chr_rom_page[7]][address - 0x1C00];
            }
            return returnvalue;
        }
        //* 8
        public void Switch32kPrgRom(int start)
        {
            int i;
            switch (mapperCartridge.prg_rom_pages)
            {
                case (2): start = (start & 0x7); break;
                case (4): start = (start & 0xf); break;
                case (8): start = (start & 0x1f); break;
                case (16): start = (start & 0x3f); break;
                case (32): start = (start & 0x7f); break;
                case (64): start = (start & 0xff); break;
                case (128): start = (start & 0x1ff); break;
            }
            for (i = 0; i < 8; i++)
            {
                current_prg_rom_page[i] = (uint)(start + i);
            }
        }
        //* 4
        //area 0,1
        public void Switch16kPrgRom(int start, int area)
        {
            int i;
            switch (mapperCartridge.prg_rom_pages)
            {
                case (2): start = (start & 0x7); break;
                case (4): start = (start & 0xf); break;
                case (8): start = (start & 0x1f); break;
                case (16): start = (start & 0x3f); break;
                case (32): start = (start & 0x7f); break;
                case (64): start = (start & 0xff); break;
                case (128): start = (start & 0x1ff); break;
            }
            for (i = 0; i < 4; i++)
            {
                current_prg_rom_page[4 * area + i] = (uint)(start + i);
            }
        }
        //* 2
        //area 0,1,2,3
        public void Switch8kPrgRom(int start, int area)
        {
            int i;
            switch (mapperCartridge.prg_rom_pages)
            {
                case (2): start = (start & 0x7); break;
                case (4): start = (start & 0xf); break;
                case (8): start = (start & 0x1f); break;
                case (16): start = (start & 0x3f); break;
                case (32): start = (start & 0x7f); break;
                case (64): start = (start & 0xff); break;
                case (128): start = (start & 0x1ff); break;
            }
            for (i = 0; i < 2; i++)
            {
                current_prg_rom_page[2 * area + i] = (uint)(start + i);
            }
        }
        //* 8
        public void Switch8kChrRom(int start)
        {
            int i;
            switch (mapperCartridge.chr_rom_pages)
            {
                case (2): start = (start & 0xf); break;
                case (4): start = (start & 0x1f); break;
                case (8): start = (start & 0x3f); break;
                case (16): start = (start & 0x7f); break;
                case (32): start = (start & 0xff); break;
                case (64): start = (start & 0x1ff); break;
            }
            for (i = 0; i < 8; i++)
            {
                current_chr_rom_page[i] = (uint)(start + i);
            }
        }
        //* 4
        //area 0,1
        public void Switch4kChrRom(int start, int area)
        {
            int i;
            switch (mapperCartridge.chr_rom_pages)
            {
                case (2): start = (start & 0xf); break;
                case (4): start = (start & 0x1f); break;
                case (8): start = (start & 0x3f); break;
                case (16): start = (start & 0x7f); break;
                case (32): start = (start & 0xff); break;
                case (64): start = (start & 0x1ff); break;
            }
            for (i = 0; i < 4; i++)
            {
                current_chr_rom_page[4 * area + i] = (uint)(start + i);
            }
        }
        //* 2 
        //area 0,1,2,3
        public void Switch2kChrRom(int start, int area)
        {
            int i;
            switch (mapperCartridge.chr_rom_pages)
            {
                case (2): start = (start & 0xf); break;
                case (4): start = (start & 0x1f); break;
                case (8): start = (start & 0x3f); break;
                case (16): start = (start & 0x7f); break;
                case (32): start = (start & 0xff); break;
                case (64): start = (start & 0x1ff); break;
            }
            for (i = 0; i < 2; i++)
            {
                current_chr_rom_page[2 * area + i] = (uint)(start + i);
            }
        }
        //area 0,1,2,3,4,5,6,7
        public void Switch1kChrRom(int start, int area)
        {
            switch (mapperCartridge.chr_rom_pages)
            {
                case (2): start = (start & 0xf); break;
                case (4): start = (start & 0x1f); break;
                case (8): start = (start & 0x3f); break;
                case (16): start = (start & 0x7f); break;
                case (32): start = (start & 0xff); break;
                case (64): start = (start & 0x1ff); break;
            }
            current_chr_rom_page[area] = (uint)(start);
        }

        public byte ReadPrgRom(ushort address)
        {
            byte returnvalue = 0xff;
            if (address < 0x9000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[0]][address - 0x8000];
            }
            else if (address < 0xA000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[1]][address - 0x9000];
            }
            else if (address < 0xB000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[2]][address - 0xA000];
            }
            else if (address < 0xC000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[3]][address - 0xB000];
            }
            else if (address < 0xD000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[4]][address - 0xC000];
            }
            else if (address < 0xE000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[5]][address - 0xD000];
            }
            else if (address < 0xF000)
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[6]][address - 0xE000];
            }
            else
            {
                returnvalue = mapperCartridge.prg_rom[current_prg_rom_page[7]][address - 0xF000];
            }

            return returnvalue;
        }
        public void WriteChrRom(ushort address, byte data)
        {
            if (mapperCartridge.is_vram == true)
            {
                if (address < 0x400)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[0]][address] = data;
                }
                else if (address < 0x800)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[1]][address - 0x400] = data;
                }
                else if (address < 0xC00)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[2]][address - 0x800] = data;
                }
                else if (address < 0x1000)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[3]][address - 0xC00] = data;
                }
                else if (address < 0x1400)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[4]][address - 0x1000] = data;
                }
                else if (address < 0x1800)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[5]][address - 0x1400] = data;
                }
                else if (address < 0x1C00)
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[6]][address - 0x1800] = data;
                }
                else
                {
                    mapperCartridge.chr_rom[current_chr_rom_page[7]][address - 0x1C00] = data;
                }
            }
        }
        public void WritePrgRom(ushort address, byte data)
        {
            //CurrentMapper.Write(address, data);
        }
        public void TickTimer()
        {
            //CurrentMapper.TickTimer();
        }
        public void SetUpMapperDefaults()
        {
            //uint i;
            //Switch32kPrgRom(0);
            if (mapperCartridge.mapper == 0)
            {
                //CurrentMapper = new Mapper0(this);
                //CurrentMapper.SetUpMapperDefaults();
                Switch32kPrgRom(0);
                Switch8kChrRom(0);
            }
            if (mapperCartridge.prg_rom_pages == 1)
            {
                Switch16kPrgRom(0, 1);
                Switch8kChrRom(0);
            }
            //if (mapperCartridge.mapper == 1)
            //{
            //    CurrentMapper = new Mapper1(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 2)
            //{
            //    CurrentMapper = new Mapper2(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 3)
            //{
            //    CurrentMapper = new Mapper3(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 4)
            //{
            //    CurrentMapper = new Mapper4(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 5)
            //{
            //    CurrentMapper = new Mapper5(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 7)
            //{
            //    CurrentMapper = new Mapper7(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 9)
            //{
            //    CurrentMapper = new Mapper9(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //    map9 = (Mapper9)CurrentMapper;
            //}
            //if (mapperCartridge.mapper == 10)
            //{
            //    CurrentMapper = new Mapper10(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //    map10 = (Mapper10)CurrentMapper;
            //}
            //if (mapperCartridge.mapper == 11)
            //{
            //    CurrentMapper = new Mapper11(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 13)
            //{
            //    CurrentMapper = new Mapper13(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 15)
            //{
            //    CurrentMapper = new Mapper15(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 16)
            //{
            //    CurrentMapper = new Mapper16(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 18)
            //{
            //    CurrentMapper = new Mapper18(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 22)
            //{
            //    CurrentMapper = new Mapper22(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 32)
            //{
            //    CurrentMapper = new Mapper32(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 34)
            //{
            //    CurrentMapper = new Mapper34(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 41)
            //{
            //    CurrentMapper = new Mapper41(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 64)
            //{
            //    CurrentMapper = new Mapper64(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 66)
            //{
            //    CurrentMapper = new Mapper66(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 71)
            //{
            //    CurrentMapper = new Mapper71(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
            //if (mapperCartridge.mapper == 225 | mapperCartridge.mapper == 255)
            //{
            //    CurrentMapper = new Mapper225(this);
            //    CurrentMapper.SetUpMapperDefaults();
            //}
        }
        public Mappers(NesEmulator theEngine, ref Cart theCartridge)
        {
            myEngine = theEngine;
            mapperCartridge = theCartridge;
            current_prg_rom_page = new uint[8];
            current_chr_rom_page = new uint[8];
        }
    }
}