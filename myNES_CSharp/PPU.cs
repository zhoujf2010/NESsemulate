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
/*
 * Code based on 
 * SharpNes : Copyright (c) 2005, Jonathan Turner
 * All rights reserved.
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading;
using System.Drawing;
using System.Runtime.InteropServices;
using System.ComponentModel;
namespace AHD.MyNes.Nes
{
    /// <summary>
    /// 
    /// </summary>
    public class PPU
    {
        public PPU(NesEmulator theEngine)//, Video Vid)
        {
            // myVideo = Vid;
            myEngine = theEngine;
            nameTables = new byte[0x2000];
            spriteRam = new byte[0x100];
            offscreenBuffer = new short[256 * 240];
            sprite0Buffer = new int[256];
            RestartPPU();

        }
        public bool executeNMIonVBlank;
        public byte ppuMaster; // 0 = slave, 1 = master, 0xff = unset (master) 
        public int spriteSize;  // instead of being 'boolean', this will be 8 or 16
        public int backgroundAddress; // 0000 or 1000
        public int spriteAddress; // 0000 or 1000
        public int ppuAddressIncrement;
        public int nameTableAddress; // 2000, 2400, 2800, 2c00
        public bool monochromeDisplay; // false = color
        public bool noBackgroundClipping; // false = clip left 8 bg pixels
        public bool noSpriteClipping; // false = clip left 8 sprite pixels
        public bool backgroundVisible; // false = invisible
        public bool spritesVisible; // false = sprites invisible
        public int ppuColor; // r/b/g or intensity level
        public int Scanlinesperframe = 262;
        //////////////////////////////
        //This value is important, in doc's, they say
        //that VBLANK is happens in SL (scan line) # 240
        //but in real test (with some roms like Castlequest),
        //this will make a bad scroll in the game ...
        //however i test it and 248 (with NTSC) is verygood
        //for this game and others (Castlequest, Castlevania ..)
        //remember that the PPU returns VBLANK in SL # 240
        //in the status register
        //while the real one happens after that lil' bit.
        public int ScanlinesOfVBLANK = 248;
        public byte sprite0Hit;
        public int[] sprite0Buffer;
        public int vramReadWriteAddress;
        public int prev_vramReadWriteAddress;
        public byte vramHiLoToggle;
        public byte vramReadBuffer;
        public byte scrollV, scrollH;
        public int currentScanline;
        public byte[] nameTables;
        public byte[] spriteRam;
        public uint spriteRamAddress;
        public int spritesCrossed;
        public int frameCounter;
        NesEmulator myEngine;
        public short[] offscreenBuffer;
        // Video myVideo;
        public ushort[] Nes_Palette ={
	    0x8410, 0x17, 0x3017, 0x8014, 0xb80d, 0xb003, 0xb000, 0x9120,
	    0x7940, 0x1e0, 0x241, 0x1e4, 0x16c, 0x0, 0x20, 0x20,
	    0xce59, 0x2df, 0x41ff, 0xb199, 0xf995, 0xf9ab, 0xf9a3, 0xd240,
	    0xc300, 0x3bc0, 0x1c22, 0x4ac, 0x438, 0x1082, 0x841, 0x841,
	    0xffff, 0x4bf, 0x6c3f, 0xd37f, 0xfbb9, 0xfb73, 0xfbcb, 0xfc8b,
	    0xfd06, 0xa5e0, 0x56cd, 0x4eb5, 0x6df, 0x632c, 0x861, 0x861,
	    0xffff, 0x85ff, 0xbddf, 0xd5df, 0xfdfd, 0xfdf9, 0xfe36, 0xfe75,
	    0xfed4, 0xcf13, 0xaf76, 0xafbd, 0xb77f, 0xdefb, 0x1082, 0x1082
                                     };
        double _currentFrameTime = 0;
        double _lastFrameTime = 0;
        public double FramePeriod = 0;
        public void Control_Register_1_Write(byte data)
        {
            //go bit by bit, and flag our values
            if ((data & 0x80) == 0x80)
                executeNMIonVBlank = true;
            else
                executeNMIonVBlank = false;

            if ((data & 0x20) == 0x20)
                spriteSize = 16;
            else
                spriteSize = 8;

            if ((data & 0x10) == 0x10)
                backgroundAddress = 0x1000;
            else
                backgroundAddress = 0x0000;

            if ((data & 0x8) == 0x8)
                spriteAddress = 0x1000;
            else
                spriteAddress = 0x0000;

            if ((data & 0x4) == 0x4)
                ppuAddressIncrement = 32;
            else
                ppuAddressIncrement = 1;
            if ((backgroundVisible == true) || (ppuMaster == 0xff) || (ppuMaster == 1))
            {
                switch (data & 0x3)
                {
                    case (0x0): nameTableAddress = 0x2000; break;
                    case (0x1): nameTableAddress = 0x2400; break;
                    case (0x2): nameTableAddress = 0x2800; break;
                    case (0x3): nameTableAddress = 0x2C00; break;
                }
            }
            /*if (myEngine.fix_bgchange == true)
            {
                if (currentScanline == 241)
                    nameTableAddress = 0x2000;
            }*/

            if (ppuMaster == 0xff)
            {
                if ((data & 0x40) == 0x40)
                    ppuMaster = 0;
                else
                    ppuMaster = 1;
            }
        }
        public void Control_Register_2_Write(byte data)
        {
            if ((data & 0x1) == 0x1)
                monochromeDisplay = true;
            else
                monochromeDisplay = false;
            if ((data & 0x2) == 0x2)
                noBackgroundClipping = true;
            else
                noBackgroundClipping = false;

            if ((data & 0x4) == 0x4)
                noSpriteClipping = true;
            else
                noSpriteClipping = false;

            if ((data & 0x8) == 0x8)
                backgroundVisible = true;
            else
                backgroundVisible = false;

            if ((data & 0x10) == 0x10)
                spritesVisible = true;
            else
                spritesVisible = false;

            ppuColor = (data >> 5);

        }
        public byte Status_Register_Read()
        {
            byte returnedValue = 0;

            // VBlank
            if (currentScanline == 240)
                returnedValue = (byte)(returnedValue | 0x80);

            // Sprite 0 hit
            //Sprite_Zero_Hit();

            if (sprite0Hit == 1)
            {
                returnedValue = (byte)(returnedValue | 0x40);
                //sprite0Hit = 0;
            }
            // Sprites on current scanline
            if (spritesCrossed > 8)
                returnedValue = (byte)(returnedValue | 0x20);

            vramHiLoToggle = 1;

            return returnedValue;
        }
        public void VRAM_Address_Register_1_Write(byte data)
        {
            if (vramHiLoToggle == 1)
            {
                scrollV = data;
                vramHiLoToggle = 0;
            }
            else
            {
                scrollH = data;
                if (scrollH > 239)
                {
                    scrollH = 0;
                }
                if (myEngine.fix_scrolloffset2)
                {
                    if (currentScanline < 240)
                    {
                        scrollH = (byte)(scrollH - currentScanline + 8);
                    }
                }
                if (myEngine.fix_scrolloffset1)
                {
                    if (currentScanline < 240)
                    {
                        scrollH = (byte)(scrollH - currentScanline);
                    }
                }
                if (myEngine.fix_scrolloffset3)
                {
                    if (currentScanline < 240)
                        scrollH = 238;
                }
                vramHiLoToggle = 1;
            }
        }
        public void VRAM_Address_Register_2_Write(byte data)
        {
            if (vramHiLoToggle == 1)
            {
                prev_vramReadWriteAddress = vramReadWriteAddress;
                vramReadWriteAddress = (int)data << 8;
                vramHiLoToggle = 0;
            }
            else
            {
                vramReadWriteAddress = vramReadWriteAddress + (int)data;
                if ((prev_vramReadWriteAddress == 0) && (currentScanline < 240))
                {
                    //We may have a scrolling trick
                    if ((vramReadWriteAddress >= 0x2000) && (vramReadWriteAddress <= 0x2400))
                        scrollH = (byte)(((vramReadWriteAddress - 0x2000) / 0x20) * 8 - currentScanline);
                }
                vramHiLoToggle = 1;
            }
        }
        public void VRAM_IO_Register_Write(byte data)
        {
            if (vramReadWriteAddress < 0x2000)
            {
                myEngine.myMapper.WriteChrRom((ushort)vramReadWriteAddress, data);
            }
            else if ((vramReadWriteAddress >= 0x2000) && (vramReadWriteAddress < 0x3f00))
            {
                if (myEngine.myCartridge.mirroring == MIRRORING.HORIZONTAL)
                {
                    switch (vramReadWriteAddress & 0x2C00)
                    {
                        case (0x2000): nameTables[vramReadWriteAddress - 0x2000] = data;
                            break;
                        case (0x2400): nameTables[(vramReadWriteAddress - 0x400) - 0x2000] = data;
                            break;
                        case (0x2800): nameTables[vramReadWriteAddress - 0x400 - 0x2000] = data;
                            break;
                        case (0x2C00): nameTables[(vramReadWriteAddress - 0x800) - 0x2000] = data;
                            break;
                    }
                }
                else if (myEngine.myCartridge.mirroring == MIRRORING.VERTICAL)
                {
                    switch (vramReadWriteAddress & 0x2C00)
                    {
                        case (0x2000): nameTables[vramReadWriteAddress - 0x2000] = data;
                            break;
                        case (0x2400): nameTables[vramReadWriteAddress - 0x2000] = data;
                            break;
                        case (0x2800): nameTables[vramReadWriteAddress - 0x800 - 0x2000] = data;
                            break;
                        case (0x2C00): nameTables[(vramReadWriteAddress - 0x800) - 0x2000] = data;
                            break;
                    }
                }
                else if (myEngine.myCartridge.mirroring == MIRRORING.ONE_SCREEN)
                {
                    if (myEngine.myCartridge.mirroringBase == 0x2000)
                    {
                        switch (vramReadWriteAddress & 0x2C00)
                        {
                            case (0x2000): nameTables[vramReadWriteAddress - 0x2000] = data;
                                break;
                            case (0x2400): nameTables[vramReadWriteAddress - 0x400 - 0x2000] = data;
                                break;
                            case (0x2800): nameTables[vramReadWriteAddress - 0x800 - 0x2000] = data;
                                break;
                            case (0x2C00): nameTables[vramReadWriteAddress - 0xC00 - 0x2000] = data;
                                break;
                        }
                    }
                    else if (myEngine.myCartridge.mirroringBase == 0x2400)
                    {
                        switch (vramReadWriteAddress & 0x2C00)
                        {
                            case (0x2000): nameTables[vramReadWriteAddress + 0x400 - 0x2000] = data;
                                break;
                            case (0x2400): nameTables[vramReadWriteAddress - 0x2000] = data;
                                break;
                            case (0x2800): nameTables[vramReadWriteAddress - 0x400 - 0x2000] = data;
                                break;
                            case (0x2C00): nameTables[vramReadWriteAddress - 0x800 - 0x2000] = data;
                                break;
                        }
                    }
                }
                //four screen mirroring (Croser)
                else
                {
                    nameTables[vramReadWriteAddress - 0x2000] = data;
                }
            }
            else if ((vramReadWriteAddress >= 0x3f00) && (vramReadWriteAddress < 0x3f20))
            {
                nameTables[vramReadWriteAddress - 0x2000] = data;
                if ((vramReadWriteAddress & 0x7) == 0)
                {
                    nameTables[(vramReadWriteAddress - 0x2000) ^ 0x10] = data;
                }
            }
            vramReadWriteAddress = vramReadWriteAddress + ppuAddressIncrement;
        }
        public byte VRAM_IO_Register_Read()
        {
            byte returnedValue = 0;

            if (vramReadWriteAddress < 0x3f00)
            {
                returnedValue = vramReadBuffer;
                if (vramReadWriteAddress >= 0x2000)
                {
                    vramReadBuffer = nameTables[vramReadWriteAddress - 0x2000];
                }
                else
                {
                    vramReadBuffer = myEngine.myMapper.ReadChrRom((ushort)(vramReadWriteAddress));
                }
            }
            else if (vramReadWriteAddress >= 0x4000)
            {
                Console.WriteLine("I need vram mirroring {0:x}", vramReadWriteAddress);

                myEngine.isQuitting = true;
            }
            else
            {
                returnedValue = nameTables[vramReadWriteAddress - 0x2000];
            }
            vramReadWriteAddress = vramReadWriteAddress + ppuAddressIncrement;
            return returnedValue;
        }
        public void SpriteRam_Address_Register_Write(byte data)
        {
            spriteRamAddress = (uint)data;
        }
        public void SpriteRam_IO_Register_Write(byte data)
        {
            spriteRam[spriteRamAddress] = data;
            spriteRamAddress++;
        }
        public byte SpriteRam_IO_Register_Read()
        {
            return spriteRam[spriteRamAddress];
        }
        public void SpriteRam_DMA_Begin(byte data)
        {
            int i;
            for (i = 0; i < 0x100; i++)
            {
                spriteRam[i] = myEngine.ReadMemory8((ushort)(((uint)data * 0x100) + i));
            }

            if (spriteRam[20] == 192)
            {

            }
        }
        public void Sprite_Zero_Hit()
        {
            byte sprite_y = spriteRam[0];
            byte sprite_id = spriteRam[1];
            byte sprite_attributes = spriteRam[2];
            byte sprite_x = spriteRam[3];
            if (currentScanline >= (sprite_y + spriteSize - 1))
            {
                sprite0Hit = 1;
            }
        }

        public void RenderBackground()
        {
            int currentTileColumn;
            int tileNumber;
            //int scanInsideTile;
            int tileDataOffset;
            byte tiledata1, tiledata2;
            byte paletteHighBits;
            int pixelColor;
            int virtualScanline;
            int nameTableBase;
            int i; // genero loop, I should probably name this something better
            int startColumn, endColumn;
            int vScrollSide;
            int startTilePixel, endTilePixel;

            for (vScrollSide = 0; vScrollSide < 2; vScrollSide++)
            {
                virtualScanline = currentScanline + scrollH;
                nameTableBase = nameTableAddress;
                if (vScrollSide == 0)
                {
                    if (virtualScanline >= 240)
                    {
                        switch (nameTableAddress)
                        {
                            case (0x2000): nameTableBase = 0x2800; break;
                            case (0x2400): nameTableBase = 0x2C00; break;
                            case (0x2800): nameTableBase = 0x2000; break;
                            case (0x2C00): nameTableBase = 0x2400; break;

                        }
                        virtualScanline = virtualScanline - 240;
                    }

                    startColumn = scrollV / 8;
                    endColumn = 32;
                }
                else
                {
                    if (virtualScanline >= 240)
                    {
                        switch (nameTableAddress)
                        {
                            case (0x2000): nameTableBase = 0x2C00; break;
                            case (0x2400): nameTableBase = 0x2800; break;
                            case (0x2800): nameTableBase = 0x2400; break;
                            case (0x2C00): nameTableBase = 0x2000; break;

                        }
                        virtualScanline = virtualScanline - 240;
                    }
                    else
                    {
                        switch (nameTableAddress)
                        {
                            case (0x2000): nameTableBase = 0x2400; break;
                            case (0x2400): nameTableBase = 0x2000; break;
                            case (0x2800): nameTableBase = 0x2C00; break;
                            case (0x2C00): nameTableBase = 0x2800; break;

                        }
                    }
                    startColumn = 0;
                    endColumn = (scrollV / 8) + 1;
                }

                //Mirroring step, doing it here allows for dynamic mirroring
                //like that seen in mappers
                /*
                if (myEngine.myCartridge.mirroring == MIRRORING.HORIZONTAL)
                {
                    switch (nameTableBase)
                    {
                        case (0x2400): nameTableBase = 0x2000; break;
                        case (0x2C00): nameTableBase = 0x2800; break;
                    }
                }
                else if (myEngine.myCartridge.mirroring == MIRRORING.VERTICAL)
                {
                    switch (nameTableBase)
                    {
                        case (0x2800): nameTableBase = 0x2000; break;
                        case (0x2C00): nameTableBase = 0x2400; break;
                    }
                }
                */
                //Next Try: Forcing two page only: 0x2000 and 0x2400				
                if (myEngine.myCartridge.mirroring == MIRRORING.HORIZONTAL)
                {
                    switch (nameTableBase)
                    {
                        case (0x2400): nameTableBase = 0x2000; break;
                        case (0x2800): nameTableBase = 0x2400; break;
                        case (0x2C00): nameTableBase = 0x2400; break;
                    }
                }
                else if (myEngine.myCartridge.mirroring == MIRRORING.VERTICAL)
                {
                    switch (nameTableBase)
                    {
                        case (0x2800): nameTableBase = 0x2000; break;
                        case (0x2C00): nameTableBase = 0x2400; break;
                    }
                }
                else if (myEngine.myCartridge.mirroring == MIRRORING.ONE_SCREEN)
                {
                    nameTableBase = (int)myEngine.myCartridge.mirroringBase;
                }

                for (currentTileColumn = startColumn; currentTileColumn < endColumn;
                    currentTileColumn++)
                {
                    //Starting tile row is currentScanline / 8
                    //The offset in the tile is currentScanline % 8

                    //Step #1, get the tile number
                    tileNumber = nameTables[nameTableBase - 0x2000 + ((virtualScanline / 8) * 32) + currentTileColumn];

                    //Step #2, get the offset for the tile in the tile data
                    tileDataOffset = backgroundAddress + (tileNumber * 16);

                    //Step #3, get the tile data from chr rom
                    tiledata1 = myEngine.myMapper.ReadChrRom((ushort)(tileDataOffset + (virtualScanline % 8)));
                    tiledata2 = myEngine.myMapper.ReadChrRom((ushort)(tileDataOffset + (virtualScanline % 8) + 8));

                    //Step #4, get the attribute byte for the block of tiles we're in
                    //this will put us in the correct section in the palette table
                    paletteHighBits = nameTables[((nameTableBase - 0x2000 +
                        0x3c0 + (((virtualScanline / 8) / 4) * 8) + (currentTileColumn / 4)))];
                    paletteHighBits = (byte)(paletteHighBits >> ((4 * (((virtualScanline / 8) % 4) / 2)) +
                        (2 * ((currentTileColumn % 4) / 2))));
                    paletteHighBits = (byte)((paletteHighBits & 0x3) << 2);

                    //Step #5, render the line inside the tile to the offscreen buffer
                    if (vScrollSide == 0)
                    {
                        if (currentTileColumn == startColumn)
                        {
                            startTilePixel = scrollV % 8;
                            endTilePixel = 8;
                        }
                        else
                        {
                            startTilePixel = 0;
                            endTilePixel = 8;
                        }
                    }
                    else
                    {
                        if (currentTileColumn == endColumn)
                        {
                            startTilePixel = 0;
                            endTilePixel = scrollV % 8;
                        }
                        else
                        {
                            startTilePixel = 0;
                            endTilePixel = 8;
                        }
                    }

                    if (currentTileColumn == 6)
                    {

                    }

                    for (i = startTilePixel; i < endTilePixel; i++)
                    {
                        pixelColor = paletteHighBits + (((tiledata2 & (1 << (7 - i))) >> (7 - i)) << 1) +
                            ((tiledata1 & (1 << (7 - i))) >> (7 - i));

                        if ((pixelColor % 4) != 0)
                        {
                            if (vScrollSide == 0)
                            {
                                offscreenBuffer[(currentScanline * 256) + (8 * currentTileColumn) - scrollV + i] =
                                    (short)Nes_Palette[(0x3f & nameTables[0x1f00 + pixelColor])];

                                //if (sprite0Hit == 0)
                                //    sprite0Buffer[(8 * currentTileColumn) - scrollV + i] += 4;

                            }
                            else
                            {
                                if (((8 * currentTileColumn) + (256 - scrollV) + i) < 256)
                                {
                                    offscreenBuffer[(currentScanline * 256) + (8 * currentTileColumn) + (256 - scrollV) + i] =
                                        (short)Nes_Palette[(0x3f & nameTables[0x1f00 + pixelColor])];

                                    //if (sprite0Hit == 0)
                                    //   sprite0Buffer[(8 * currentTileColumn) + (256 - scrollV) + i] += 4;
                                }
                            }
                        }
                    }
                }
            }
        }
        private void RenderSprites(int behind)
        {
            int i, j;
            int spriteLineToDraw;
            byte tiledata1, tiledata2;
            int offsetToSprite;
            byte paletteHighBits;
            int pixelColor;
            byte actualY;

            byte spriteId;

            //Step #1 loop through each sprite in sprite RAM
            //Back to front, early numbered sprites get drawing priority

            for (i = 252; i >= 0; i = i - 4)
            {
                actualY = (byte)(spriteRam[i] + 1);
                //Step #2: if the sprite falls on the current scanline, draw it
                if (((spriteRam[i + 2] & 0x20) == behind) && (actualY <= currentScanline) && ((actualY + spriteSize) > currentScanline))
                {
                    spritesCrossed++;
                    //Step #3: Draw the sprites differently if they are 8x8 or 8x16
                    if (spriteSize == 8)
                    {
                        //Step #4: calculate which line of the sprite is currently being drawn
                        //Line to draw is: currentScanline - Y coord + 1

                        if ((spriteRam[i + 2] & 0x80) != 0x80)
                            spriteLineToDraw = currentScanline - actualY;
                        else
                            spriteLineToDraw = actualY + 7 - currentScanline;

                        //Step #5: calculate the offset to the sprite's data in
                        //our chr rom data 
                        offsetToSprite = spriteAddress + spriteRam[i + 1] * 16;

                        //Step #6: extract our tile data
                        tiledata1 = myEngine.myMapper.ReadChrRom((ushort)(offsetToSprite + spriteLineToDraw));
                        tiledata2 = myEngine.myMapper.ReadChrRom((ushort)(offsetToSprite + spriteLineToDraw + 8));

                        //Step #7: get the palette attribute data
                        paletteHighBits = (byte)((spriteRam[i + 2] & 0x3) << 2);

                        //Step #8, render the line inside the tile to the offscreen buffer
                        for (j = 0; j < 8; j++)
                        {
                            if ((spriteRam[i + 2] & 0x40) == 0x40)
                            {
                                pixelColor = paletteHighBits + (((tiledata2 & (1 << (j))) >> (j)) << 1) +
                                    ((tiledata1 & (1 << (j))) >> (j));
                            }
                            else
                            {
                                pixelColor = paletteHighBits + (((tiledata2 & (1 << (7 - j))) >> (7 - j)) << 1) +
                                    ((tiledata1 & (1 << (7 - j))) >> (7 - j));
                            }
                            if ((pixelColor % 4) != 0)
                            {
                                if ((spriteRam[i + 3] + j) < 256)
                                {
                                    offscreenBuffer[(currentScanline * 256) + (spriteRam[i + 3]) + j] =
                                            (short)Nes_Palette[(0x3f & nameTables[0x1f10 + pixelColor])];
                                    if (i == 0)
                                    {
                                        sprite0Hit = 1;
                                        //sprite0Buffer[(spriteRam[i + 3]) + j] += 1;
                                    }
                                }
                            }
                        }
                    }

                    else
                    {
                        //The sprites are 8x16, to do so we draw two tiles with slightly
                        //different rules than we had before

                        //Step #4: Get the sprite ID and the offset in that 8x16 sprite
                        //Note, for vertical flip'd sprites, we start at 15, instead of
                        //8 like above to force the tiles in opposite order
                        spriteId = spriteRam[i + 1];
                        if ((spriteRam[i + 2] & 0x80) != 0x80)
                        {
                            spriteLineToDraw = currentScanline - actualY;
                        }
                        else
                        {
                            spriteLineToDraw = actualY + 15 - currentScanline;
                        }
                        //Step #5: We draw the sprite like two halves, so getting past the 
                        //first 8 puts us into the next tile
                        //If the ID is even, the tile is in 0x0000, odd 0x1000
                        if (spriteLineToDraw < 8)
                        {
                            //Draw the top tile
                            {
                                if ((spriteId % 2) == 0)
                                    offsetToSprite = 0x0000 + (spriteId) * 16;
                                else
                                    offsetToSprite = 0x1000 + (spriteId - 1) * 16;

                            }
                        }
                        else
                        {
                            //Draw the bottom tile
                            spriteLineToDraw = spriteLineToDraw - 8;

                            if ((spriteId % 2) == 0)
                                offsetToSprite = 0x0000 + (spriteId + 1) * 16;
                            else
                                offsetToSprite = 0x1000 + (spriteId) * 16;
                        }

                        //Step #6: extract our tile data
                        tiledata1 = myEngine.myMapper.ReadChrRom((ushort)(offsetToSprite + spriteLineToDraw));
                        tiledata2 = myEngine.myMapper.ReadChrRom((ushort)(offsetToSprite + spriteLineToDraw + 8));

                        //Step #7: get the palette attribute data
                        paletteHighBits = (byte)((spriteRam[i + 2] & 0x3) << 2);

                        //Step #8, render the line inside the tile to the offscreen buffer
                        for (j = 0; j < 8; j++)
                        {
                            if ((spriteRam[i + 2] & 0x40) == 0x40)
                            {
                                pixelColor = paletteHighBits + (((tiledata2 & (1 << (j))) >> (j)) << 1) +
                                    ((tiledata1 & (1 << (j))) >> (j));
                            }
                            else
                            {
                                pixelColor = paletteHighBits + (((tiledata2 & (1 << (7 - j))) >> (7 - j)) << 1) +
                                    ((tiledata1 & (1 << (7 - j))) >> (7 - j));
                            }
                            if ((pixelColor % 4) != 0)
                            {
                                if ((spriteRam[i + 3] + j) < 256)
                                {
                                    offscreenBuffer[(currentScanline * 256) + (spriteRam[i + 3]) + j] =
                                        (short)Nes_Palette[(0x3f & nameTables[0x1f10 + pixelColor])];

                                    if (i == 0)
                                    {
                                        sprite0Hit = 1;
                                        //sprite0Buffer[(spriteRam[i + 3]) + j] += 1;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        int pic = 0;
        public bool RenderNextScanline()
        {
            int i;
            if (currentScanline < 240)
            {
                //Clean up the line from before
                if ((uint)nameTables[0x1f00] > 63)
                {
                    for (i = 0; i < 256; i++)
                    {
                        offscreenBuffer[(currentScanline * 256) + i] = 0;
                        sprite0Buffer[i] = 0;
                    }
                }
                else
                {
                    for (i = 0; i < 256; i++)
                    {
                        offscreenBuffer[(currentScanline * 256) + i] = (short)Nes_Palette[(uint)nameTables[0x1f00]];
                        sprite0Buffer[i] = 0;
                    }
                }
                spritesCrossed = 0;

                if (pic == 33 && currentScanline == 194)
                {

                }
                //We are in visible territory, so render to our offscreen buffer
                if (spritesVisible)
                    RenderSprites(0x20);
                if (backgroundVisible)
                    RenderBackground();
                if (spritesVisible)
                    RenderSprites(0);
                if (!noBackgroundClipping)
                {
                    for (i = 0; i < 8; i++)
                        offscreenBuffer[(currentScanline * 256) + i] = 0;
                }
                if (sprite0Hit == 0)
                {
                    /*for (i = 0; i < 256; i++)
                    {
                        if (sprite0Buffer[i] > 4)
                        { 
                            sprite0Hit = 1;
                        }
                    }*/
                }
                //The mapper timer !!
                if (backgroundVisible || spritesVisible)
                    myEngine.myMapper.TickTimer();
            }
            if (currentScanline == 240)
            {
                //pic++;
                //if (pic == 33)
                //{
                //    StringBuilder s = new StringBuilder();
                //    for (int j = 0; j < 240; j++)
                //    {
                //        for (int x = 0; x < 256; x++)
                //        {
                //            s.Append(offscreenBuffer[j * 256 + x]+" ");
                //        }
                //        s.Append("\r\n");
                //    }
                //    File.WriteAllText("E:\\s2.txt", s.ToString());
                //}

                myEngine.RenderFrame(offscreenBuffer);
            }
            currentScanline++;
            /*if (myEngine.fix_scrolloffset1)
            {
                if (currentScanline > 244)
                {
                    sprite0Hit = 0;
                }
            }*/
            //Render sound ...
            if (currentScanline == Scanlinesperframe)
            {
                if (myEngine.SoundEnabled)
                { myEngine.myAPU.Render(myEngine.my6502.total_cycles); }
                if (!myEngine.SpeedThrottling)
                {
                    double currentTime = GetCurrentTime();
                    _currentFrameTime = currentTime - _lastFrameTime;
                    if ((_currentFrameTime >= FramePeriod))
                    {
                        //Thread.Sleep(1);
                        //delay
                    }
                    else
                    {
                        //for (; ; )
                        while (true)
                        {
                            if ((GetCurrentTime() - _lastFrameTime) >= FramePeriod)
                            {
                                break;
                            }
                            //Thread.Sleep(0);
                        }
                    }
                    _lastFrameTime = GetCurrentTime();
                }
                currentScanline = 0;
                sprite0Hit = 0;
                frameCounter++;
            }
            //Are we about to NMI on vblank?
            if ((currentScanline == ScanlinesOfVBLANK) && (executeNMIonVBlank))
            { return true; }
            else
            { return false; }
        }

        public void RestartPPU()
        {
            executeNMIonVBlank = false;
            ppuMaster = 0xff;
            spriteSize = 8;
            backgroundAddress = 0x0000;
            spriteAddress = 0x0000;
            ppuAddressIncrement = 1;
            nameTableAddress = 0x2000;
            currentScanline = 0;
            vramHiLoToggle = 1;
            vramReadBuffer = 0;
            spriteRamAddress = 0x0;
            scrollV = 0;
            scrollH = 0;
            sprite0Hit = 0;
            frameCounter = 0;
        }

        public void ResetPalette()
        {
            ushort[] Nes_Palette_Default ={
	    0x8410, 0x17, 0x3017, 0x8014, 0xb80d, 0xb003, 0xb000, 0x9120,
	    0x7940, 0x1e0, 0x241, 0x1e4, 0x16c, 0x0, 0x20, 0x20,
	    0xce59, 0x2df, 0x41ff, 0xb199, 0xf995, 0xf9ab, 0xf9a3, 0xd240,
	    0xc300, 0x3bc0, 0x1c22, 0x4ac, 0x438, 0x1082, 0x841, 0x841,
	    0xffff, 0x4bf, 0x6c3f, 0xd37f, 0xfbb9, 0xfb73, 0xfbcb, 0xfc8b,
	    0xfd06, 0xa5e0, 0x56cd, 0x4eb5, 0x6df, 0x632c, 0x861, 0x861,
	    0xffff, 0x85ff, 0xbddf, 0xd5df, 0xfdfd, 0xfdf9, 0xfe36, 0xfe75,
	    0xfed4, 0xcf13, 0xaf76, 0xafbd, 0xb77f, 0xdefb, 0x1082, 0x1082
                                     };
            //Don't do this : Nes_Palette = Nes_Palette_Default;
            //It's not useful and the palette will not reset !!
            for (int i = 0; i < Nes_Palette_Default.Length; i++)
            {
                Nes_Palette[i] = Nes_Palette_Default[i];
            }
        }
        public void SavePalette(string FilePath)
        {
            Stream STR = new FileStream(FilePath, FileMode.Create);
            for (int i = 0; i < Nes_Palette.Length; i++)
            {
                byte RedValue = (byte)((Nes_Palette[i] & 0xF800) >> 8);
                byte GreenValue = (byte)((Nes_Palette[i] & 0x7E0) >> 3);
                byte BlueValue = (byte)((Nes_Palette[i] & 0x1F) << 3);
                STR.WriteByte(RedValue);
                STR.WriteByte(GreenValue);
                STR.WriteByte(BlueValue);
            }
            STR.Close();
        }
        public void LoadPalette(string FilePath)
        {
            if (File.Exists(FilePath))
            {
                Stream STR = new FileStream(FilePath, FileMode.Open);
                byte[] buffer = new byte[192];
                STR.Read(buffer, 0, 192);
                int j = 0;
                for (int i = 0; i < 64; i++)
                {
                    byte RedValue = buffer[j]; j++;
                    byte GreenValue = buffer[j]; j++;
                    byte BlueValue = buffer[j]; j++;
                    Nes_Palette[i] = (ushort)((RedValue << 8) | (GreenValue << 3) | (BlueValue >> 3));
                }
                STR.Close();
            }
        }

        private long _frequency = 0;

        public double GetCurrentTime()
        {
            if (_frequency == 0)
            {

                if (!QueryPerformanceFrequency(out this._frequency))
                {
                    throw new Win32Exception();
                }
            }
            long num;
            QueryPerformanceCounter(out num);
            return (((double)num) / ((double)this._frequency));
        }
        [DllImport("Kernel32.dll")]
        private static extern bool QueryPerformanceCounter(out long lpPerformanceCount);
        [DllImport("Kernel32.dll")]
        private static extern bool QueryPerformanceFrequency(out long lpFrequency);
    }
}
