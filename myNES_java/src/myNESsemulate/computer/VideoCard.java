package myNESsemulate.computer;

public class VideoCard
{
    MotherBoard mb;

    public VideoCard(MotherBoard motherBoard) {
        this.mb = motherBoard;
        byte[] rom = mb.card;
        
        // 将显示数据读入显存中
        int numprg_roms = rom[4];// 16kB ROM的数目
        int numchr_roms = rom[5];// 8kB VROM的数目
        chr_roms = new byte[1024 * 8 * numchr_roms];
        for (int i = 0; i < 1024 * 8 * numchr_roms; i++) {
            chr_roms[i] = rom[i + 1024 * 16 * numprg_roms + 16];
        }
        if ((rom[6] & 0x1) == 0x0)
            mirroring = MIRRORING.HORIZONTAL;
        else
            mirroring = MIRRORING.VERTICAL;
//        int offset = 0x8000 + 4096 * 7;
//        if ((mb.ReadBus8(0xfe0 + offset) == 'B') && (mb.ReadBus8(0xfe1 + offset) == 'B')
//                && (mb.ReadBus8(0xfe2 + offset) == '4') && (mb.ReadBus8(0xfe3 + offset) == '7')
//                && (mb.ReadBus8(0xfe4 + offset) == '9') && (mb.ReadBus8(0xfe5 + offset) == '5')
//                && (mb.ReadBus8(0xfe6 + offset) == '6') && (mb.ReadBus8(0xfe7 + offset) == '-')
//                && (mb.ReadBus8(0xfe8 + offset) == '1') && (mb.ReadBus8(0xfe9 + offset) == '5')
//                && (mb.ReadBus8(0xfea + offset) == '4') && (mb.ReadBus8(0xfeb + offset) == '4')
//                && (mb.ReadBus8(0xfec + offset) == '0')) {
//            fix_scrolloffset1 = true;
//        }
//        offset = 0x8000;
//        if ((mb.ReadBus8(0x9 + offset) == 0xfc) && (mb.ReadBus8(0xa + offset) == 0xfc)
//                && (mb.ReadBus8(0xb + offset) == 0xfc) && (mb.ReadBus8(0xc + offset) == 0x40)
//                && (mb.ReadBus8(0xd + offset) == 0x40) && (mb.ReadBus8(0xe + offset) == 0x40)
//                && (mb.ReadBus8(0xf + offset) == 0x40)) {
//            fix_scrolloffset2 = true;
//        }
//        if ((mb.ReadBus8(0x75 + offset) == 0x11) && (mb.ReadBus8(0x76 + offset) == 0x12)
//                && (mb.ReadBus8(0x77 + offset) == 0x13) && (mb.ReadBus8(0x78 + offset) == 0x14)
//                && (mb.ReadBus8(0x79 + offset) == 0x07) && (mb.ReadBus8(0x7a + offset) == 0x03)
//                && (mb.ReadBus8(0x7b + offset) == 0x03) && (mb.ReadBus8(0x7c + offset) == 0x03)
//                && (mb.ReadBus8(0x7d + offset) == 0x03)) {
//            fix_scrolloffset3 = true;
//        }
        
        
        //////////////////////////////
        // This value is important, in doc's, they say
        // that VBLANK is happens in SL (scan line) # 240
        // but in real test (with some roms like Castlequest),
        // this No will make a bad scroll in the game ...
        // however i test it and 248 (with NTSC) is verygood
        // for this game and others (Castlequest, Castlevania ..)
        // remember that the PPU returns VBLANK in SL # 240
        // in the status register
        // while the real one happens after that lil' bit.
        ScanlinesOfVBLANK = 248;

        spriteRam = new byte[0x100];
        nameTables = new byte[0x2000];
        offscreenBuffer = new short[256 * 240];
        sprite0Buffer = new int[256];

        RestartPPU();
    }

    public MIRRORING mirroring;
    public int mirroringBase; // For one screen mirroring

    public enum MIRRORING
    {
        HORIZONTAL, VERTICAL, FOUR_SCREEN, ONE_SCREEN
    };
//    public boolean fix_scrolloffset1;
//    public boolean fix_scrolloffset2;
//    public boolean fix_scrolloffset3;

    public byte Read(int address) {
        if (address == 0x2002)
            return Status_Register_Read();
        else if (address == 0x2004)
            return SpriteRam_IO_Register_Read();
        else if (address == 0x2007)
            return VRAM_IO_Register_Read();
        return 0;
    }
    
    public void Write(int address, byte data) {
        if (address == 0x2000)
            Control_Register_1_Write(data);
        else if (address == 0x2001)
            Control_Register_2_Write(data);
        else if (address == 0x2003)
            SpriteRam_Address_Register_Write(data);
        else if (address == 0x2004)
            SpriteRam_IO_Register_Write(data);
        else if (address == 0x2005)
            VRAM_Address_Register_1_Write(data);
        else if (address == 0x2006)
            VRAM_Address_Register_2_Write(data);
        else if (address == 0x2007)
            VRAM_IO_Register_Write(data);
        else if (address == 0x4014)
            SpriteRam_DMA_Begin(data);
    }
    
    private int ScanlinesOfVBLANK = 248;
    public byte[] chr_roms;

    private boolean executeNMIonVBlank;
    private byte ppuMaster; // 0 = slave, 1 = master, 0xff = unset (master)
    private int spriteSize; // instead of being 'boolean', this will be 8 or 16
    private int backgroundAddress; // 0000 or 1000
    private int spriteAddress; // 0000 or 1000
    private int ppuAddressIncrement;
    private int nameTableAddress; // 2000, 2400, 2800, 2c00
    private boolean noBackgroundClipping; // false = clip left 8 bg pixels
    private boolean backgroundVisible; // false = invisible
    private boolean spritesVisible; // false = sprites invisible
    private byte sprite0Hit;
    private int[] sprite0Buffer;
    private int vramReadWriteAddress;
    private int prev_vramReadWriteAddress;
    private byte vramHiLoToggle;
    private byte vramReadBuffer;
    private int scrollV, scrollH;
    private int currentScanline;
    private byte[] nameTables;
    private byte[] spriteRam;
    private int spriteRamAddress;
    private int spritesCrossed;
    public short[] offscreenBuffer;
    
    // Video myVideo;
    public int[] Nes_Palette = {0x8410, 0x17, 0x3017, 0x8014, 0xb80d, 0xb003, 0xb000, 0x9120, 0x7940, 0x1e0, 0x241,
            0x1e4, 0x16c, 0x0, 0x20, 0x20, 0xce59, 0x2df, 0x41ff, 0xb199, 0xf995, 0xf9ab, 0xf9a3, 0xd240, 0xc300,
            0x3bc0, 0x1c22, 0x4ac, 0x438, 0x1082, 0x841, 0x841, 0xffff, 0x4bf, 0x6c3f, 0xd37f, 0xfbb9, 0xfb73, 0xfbcb,
            0xfc8b, 0xfd06, 0xa5e0, 0x56cd, 0x4eb5, 0x6df, 0x632c, 0x861, 0x861, 0xffff, 0x85ff, 0xbddf, 0xd5df, 0xfdfd,
            0xfdf9, 0xfe36, 0xfe75, 0xfed4, 0xcf13, 0xaf76, 0xafbd, 0xb77f, 0xdefb, 0x1082, 0x1082 };

    public void RestartPPU() {
        executeNMIonVBlank = false;
        ppuMaster = (byte) 0xFF;
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
    }

    public boolean RenderNextScanline() {
        if (currentScanline < 240) {
            // 清除一行显示数据
            if (nameTables[0x1f00] > 63) {
                for (int i = 0; i < 256; i++) {
                    offscreenBuffer[(currentScanline * 256) + i] = 0;
                    sprite0Buffer[i] = 0;
                }
            }
            else {
                for (int i = 0; i < 256; i++) {
                    offscreenBuffer[(currentScanline * 256) + i] = (short) Nes_Palette[nameTables[0x1f00]];
                    sprite0Buffer[i] = 0;
                }
            }
            spritesCrossed = 0;

            // 显示相应的内容
            if (spritesVisible)
                RenderSprites(0x20);
            if (backgroundVisible)
                RenderBackground();
            if (spritesVisible)
                RenderSprites(0);
            if (!noBackgroundClipping) {
                for (int i = 0; i < 8; i++)
                    offscreenBuffer[(currentScanline * 256) + i] = 0;
            }
        }
        if (currentScanline == 240) {
//            mb.RenderFrame(offscreenBuffer);
        }
        currentScanline++;

        if (currentScanline == 262) {
            currentScanline = 0;
            sprite0Hit = 0;
        }
        // Are we about to NMI on vblank?
        if ((currentScanline == ScanlinesOfVBLANK) && (executeNMIonVBlank)) {
            return true;
        }
        else {
            return false;
        }
    }

    private void RenderBackground() {
        int currentTileColumn;
        int tileNumber;
        // int scanInsideTile;
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

        for (vScrollSide = 0; vScrollSide < 2; vScrollSide++) {
            virtualScanline = currentScanline + scrollH;
            nameTableBase = nameTableAddress;
            if (vScrollSide == 0) {
                if (virtualScanline >= 240) {
                    switch (nameTableAddress) {
                        case (0x2000):
                            nameTableBase = 0x2800;
                            break;
                        case (0x2400):
                            nameTableBase = 0x2C00;
                            break;
                        case (0x2800):
                            nameTableBase = 0x2000;
                            break;
                        case (0x2C00):
                            nameTableBase = 0x2400;
                            break;

                    }
                    virtualScanline = virtualScanline - 240;
                }

                startColumn = scrollV / 8;
                endColumn = 32;
            }
            else {
                if (virtualScanline >= 240) {
                    switch (nameTableAddress) {
                        case (0x2000):
                            nameTableBase = 0x2C00;
                            break;
                        case (0x2400):
                            nameTableBase = 0x2800;
                            break;
                        case (0x2800):
                            nameTableBase = 0x2400;
                            break;
                        case (0x2C00):
                            nameTableBase = 0x2000;
                            break;

                    }
                    virtualScanline = virtualScanline - 240;
                }
                else {
                    switch (nameTableAddress) {
                        case (0x2000):
                            nameTableBase = 0x2400;
                            break;
                        case (0x2400):
                            nameTableBase = 0x2000;
                            break;
                        case (0x2800):
                            nameTableBase = 0x2C00;
                            break;
                        case (0x2C00):
                            nameTableBase = 0x2800;
                            break;

                    }
                }
                startColumn = 0;
                endColumn = (scrollV / 8) + 1;
            }

            // Mirroring step, doing it here allows for dynamic mirroring
            // like that seen in mappers
            /*
             * if (myEngine.myCartridge.mirroring == MIRRORING.HORIZONTAL)
             * {
             * switch (nameTableBase)
             * {
             * case (0x2400): nameTableBase = 0x2000; break;
             * case (0x2C00): nameTableBase = 0x2800; break;
             * }
             * }
             * else if (myEngine.myCartridge.mirroring == MIRRORING.VERTICAL)
             * {
             * switch (nameTableBase)
             * {
             * case (0x2800): nameTableBase = 0x2000; break;
             * case (0x2C00): nameTableBase = 0x2400; break;
             * }
             * }
             */
            // Next Try: Forcing two page only: 0x2000 and 0x2400
            if (mirroring == MIRRORING.HORIZONTAL) {
                switch (nameTableBase) {
                    case (0x2400):
                        nameTableBase = 0x2000;
                        break;
                    case (0x2800):
                        nameTableBase = 0x2400;
                        break;
                    case (0x2C00):
                        nameTableBase = 0x2400;
                        break;
                }
            }
            else if (mirroring == MIRRORING.VERTICAL) {
                switch (nameTableBase) {
                    case (0x2800):
                        nameTableBase = 0x2000;
                        break;
                    case (0x2C00):
                        nameTableBase = 0x2400;
                        break;
                }
            }
            else if (mirroring == MIRRORING.ONE_SCREEN) {
                nameTableBase = (int) mirroringBase;
            }

            for (currentTileColumn = startColumn; currentTileColumn < endColumn; currentTileColumn++) {
                // Starting tile row is currentScanline / 8
                // The offset in the tile is currentScanline % 8

                // Step #1, get the tile number
                tileNumber = nameTables[nameTableBase - 0x2000 + ((virtualScanline / 8) * 32) + currentTileColumn];

                // Step #2, get the offset for the tile in the tile data
                tileDataOffset = backgroundAddress + ((tileNumber & 0xff) * 16);

                // Step #3, get the tile data from chr rom
                tiledata1 = chr_roms[tileDataOffset + (virtualScanline % 8)];
                tiledata2 = chr_roms[tileDataOffset + (virtualScanline % 8) + 8];

                // Step #4, get the attribute byte for the block of tiles we're in
                // this will put us in the correct section in the palette table
                paletteHighBits = nameTables[((nameTableBase - 0x2000 + 0x3c0 + (((virtualScanline / 8) / 4) * 8)
                        + (currentTileColumn / 4)))];
                paletteHighBits = (byte) (paletteHighBits >> ((4 * (((virtualScanline / 8) % 4) / 2))
                        + (2 * ((currentTileColumn % 4) / 2))));
                paletteHighBits = (byte) ((paletteHighBits & 0x3) << 2);

                // Step #5, render the line inside the tile to the offscreen buffer
                if (vScrollSide == 0) {
                    if (currentTileColumn == startColumn) {
                        startTilePixel = scrollV % 8;
                        endTilePixel = 8;
                    }
                    else {
                        startTilePixel = 0;
                        endTilePixel = 8;
                    }
                }
                else {
                    if (currentTileColumn == endColumn) {
                        startTilePixel = 0;
                        endTilePixel = scrollV % 8;
                    }
                    else {
                        startTilePixel = 0;
                        endTilePixel = 8;
                    }
                }

                for (i = startTilePixel; i < endTilePixel; i++) {
                    pixelColor = paletteHighBits + (((tiledata2 & (1 << (7 - i))) >> (7 - i)) << 1)
                            + ((tiledata1 & (1 << (7 - i))) >> (7 - i));

                    if ((pixelColor % 4) != 0) {
                        if (vScrollSide == 0) {
                            offscreenBuffer[(currentScanline * 256) + (8 * currentTileColumn) - scrollV
                                    + i] = (short) Nes_Palette[(0x3f & nameTables[0x1f00 + pixelColor])];

                            // if (sprite0Hit == 0)
                            // sprite0Buffer[(8 * currentTileColumn) - scrollV + i] += 4;

                        }
                        else {
                            if (((8 * currentTileColumn) + (256 - scrollV) + i) < 256) {
                                offscreenBuffer[(currentScanline * 256) + (8 * currentTileColumn) + (256 - scrollV)
                                        + i] = (short) Nes_Palette[(0x3f & nameTables[0x1f00 + pixelColor])];

                                // if (sprite0Hit == 0)
                                // sprite0Buffer[(8 * currentTileColumn) + (256 - scrollV) + i] += 4;
                            }
                        }
                    }
                }
            }
        }

    }

    private void RenderSprites(int behind) {
        int i, j;
        int spriteLineToDraw;
        byte tiledata1, tiledata2;
        int offsetToSprite;
        byte paletteHighBits;
        int pixelColor;
        byte actualY;

        byte spriteId;

        // Step #1 loop through each sprite in sprite RAM
        // Back to front, early numbered sprites get drawing priority

        for (i = 252; i >= 0; i = i - 4) {
            actualY = (byte) (spriteRam[i] + 1);
            // Step #2: if the sprite falls on the current scanline, draw it
            if (((spriteRam[i + 2] & 0x20 & 0xff) == behind) && ((actualY & 0xff) <= currentScanline)
                    && (((actualY & 0xff) + spriteSize) > currentScanline)) {
                spritesCrossed++;
                // Step #3: Draw the sprites differently if they are 8x8 or 8x16
                if (spriteSize == 8) {
                    // Step #4: calculate which line of the sprite is currently being drawn
                    // Line to draw is: currentScanline - Y coord + 1

                    if ((spriteRam[i + 2] & 0x80) != 0x80)
                        spriteLineToDraw = currentScanline - (actualY & 0xff);
                    else
                        spriteLineToDraw = (actualY & 0xff) + 7 - currentScanline;

                    // Step #5: calculate the offset to the sprite's data in
                    // our chr rom data
                    offsetToSprite = spriteAddress + (spriteRam[i + 1] & 0xFF) * 16;

                    // Step #6: extract our tile data
                    tiledata1 = chr_roms[offsetToSprite + spriteLineToDraw];
                    tiledata2 = chr_roms[offsetToSprite + spriteLineToDraw + 8];

                    // Step #7: get the palette attribute data
                    paletteHighBits = (byte) ((spriteRam[i + 2] & 0x3) << 2);

                    // Step #8, render the line inside the tile to the offscreen buffer
                    for (j = 0; j < 8; j++) {
                        if ((spriteRam[i + 2] & 0x40) == 0x40) {
                            pixelColor = (paletteHighBits & 0xff) + (((tiledata2 & (1 << (j))) >> (j)) << 1)
                                    + ((tiledata1 & (1 << (j))) >> (j));
                        }
                        else {
                            pixelColor = (paletteHighBits & 0xff) + (((tiledata2 & (1 << (7 - j))) >> (7 - j)) << 1)
                                    + ((tiledata1 & (1 << (7 - j))) >> (7 - j));
                        }
                        if ((pixelColor % 4) != 0) {
                            if (((spriteRam[i + 3] & 0xff) + j) < 256) {
                                offscreenBuffer[(currentScanline * 256) + (spriteRam[i + 3] & 0xff)
                                        + j] = (short) Nes_Palette[(0x3f & nameTables[0x1f10 + pixelColor])];
                                if (i == 0) {
                                    sprite0Hit = 1;
                                    // sprite0Buffer[(spriteRam[i + 3]) + j] += 1;
                                }
                            }
                        }
                    }
                }

                else {
                    // The sprites are 8x16, to do so we draw two tiles with slightly
                    // different rules than we had before

                    // Step #4: Get the sprite ID and the offset in that 8x16 sprite
                    // Note, for vertical flip'd sprites, we start at 15, instead of
                    // 8 like above to force the tiles in opposite order
                    spriteId = spriteRam[i + 1];
                    if ((spriteRam[i + 2] & 0x80) != 0x80) {
                        spriteLineToDraw = currentScanline - (actualY & 0xff);
                    }
                    else {
                        spriteLineToDraw = (actualY & 0xff) + 15 - currentScanline;
                    }
                    // Step #5: We draw the sprite like two halves, so getting past the
                    // first 8 puts us into the next tile
                    // If the ID is even, the tile is in 0x0000, odd 0x1000
                    if (spriteLineToDraw < 8) {
                        // Draw the top tile
                        {
                            if ((spriteId % 2) == 0)
                                offsetToSprite = 0x0000 + (spriteId) * 16;
                            else
                                offsetToSprite = 0x1000 + (spriteId - 1) * 16;

                        }
                    }
                    else {
                        // Draw the bottom tile
                        spriteLineToDraw = spriteLineToDraw - 8;

                        if ((spriteId % 2) == 0)
                            offsetToSprite = 0x0000 + (spriteId + 1) * 16;
                        else
                            offsetToSprite = 0x1000 + (spriteId) * 16;
                    }

                    // Step #6: extract our tile data
                    tiledata1 = chr_roms[offsetToSprite + spriteLineToDraw];
                    tiledata2 = chr_roms[offsetToSprite + spriteLineToDraw + 8];

                    // Step #7: get the palette attribute data
                    paletteHighBits = (byte) ((spriteRam[i + 2] & 0x3) << 2);

                    // Step #8, render the line inside the tile to the offscreen buffer
                    for (j = 0; j < 8; j++) {
                        if ((spriteRam[i + 2] & 0x40) == 0x40) {
                            pixelColor = (paletteHighBits & 0xff) + (((tiledata2 & (1 << (j))) >> (j)) << 1)
                                    + ((tiledata1 & (1 << (j))) >> (j));
                        }
                        else {
                            pixelColor = (paletteHighBits & 0xff) + (((tiledata2 & (1 << (7 - j))) >> (7 - j)) << 1)
                                    + ((tiledata1 & (1 << (7 - j))) >> (7 - j));
                        }
                        if ((pixelColor % 4) != 0) {
                            if ((spriteRam[i + 3] + j) < 256) {
                                offscreenBuffer[(currentScanline * 256) + (spriteRam[i + 3])
                                        + j] = (short) Nes_Palette[(0x3f & nameTables[0x1f10 + pixelColor])];

                                if (i == 0) {
                                    sprite0Hit = 1;
                                    // sprite0Buffer[(spriteRam[i + 3]) + j] += 1;
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public byte Status_Register_Read() {
        byte returnedValue = 0;

        // VBlank
        if (currentScanline == 240)
            returnedValue = (byte) (returnedValue | 0x80);

        // Sprite 0 hit
        // Sprite_Zero_Hit();

        if (sprite0Hit == 1) {
            returnedValue = (byte) (returnedValue | 0x40);
            // sprite0Hit = 0;
        }
        // Sprites on current scanline
        if (spritesCrossed > 8)
            returnedValue = (byte) (returnedValue | 0x20);

        vramHiLoToggle = 1;

        return returnedValue;
    }

    public byte SpriteRam_IO_Register_Read() {
        return spriteRam[spriteRamAddress];
    }

    public byte VRAM_IO_Register_Read() {
        byte returnedValue = 0;

        if (vramReadWriteAddress < 0x3f00) {
            returnedValue = vramReadBuffer;
            if (vramReadWriteAddress >= 0x2000) {
                vramReadBuffer = nameTables[vramReadWriteAddress - 0x2000];
            }
            else {
                vramReadBuffer = chr_roms[vramReadWriteAddress];// myEngine.myMapper.ReadChrRom((ushort)(vramReadWriteAddress));
            }
        }
        else if (vramReadWriteAddress >= 0x4000) {
            // Console.WriteLine("I need vram mirroring {0:x}", vramReadWriteAddress);

            // myEngine.isQuitting = true;
        }
        else {
            returnedValue = nameTables[vramReadWriteAddress - 0x2000];
        }
        vramReadWriteAddress = vramReadWriteAddress + ppuAddressIncrement;
        return returnedValue;
    }

    public void Control_Register_1_Write(byte data) {
        // go bit by bit, and flag our values
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
        if ((backgroundVisible == true) || (ppuMaster == 0xff) || (ppuMaster == 1)) {
            switch (data & 0x3) {
                case (0x0):
                    nameTableAddress = 0x2000;
                    break;
                case (0x1):
                    nameTableAddress = 0x2400;
                    break;
                case (0x2):
                    nameTableAddress = 0x2800;
                    break;
                case (0x3):
                    nameTableAddress = 0x2C00;
                    break;
            }
        }
        /*
         * if (myEngine.fix_bgchange == true)
         * {
         * if (currentScanline == 241)
         * nameTableAddress = 0x2000;
         * }
         */

        if (ppuMaster == 0xff) {
            if ((data & 0x40) == 0x40)
                ppuMaster = 0;
            else
                ppuMaster = 1;
        }

    }

    public void Control_Register_2_Write(byte data) {
        if ((data & 0x2) == 0x2)
            noBackgroundClipping = true;
        else
            noBackgroundClipping = false;


        if ((data & 0x8) == 0x8)
            backgroundVisible = true;
        else
            backgroundVisible = false;

        if ((data & 0x10) == 0x10)
            spritesVisible = true;
        else
            spritesVisible = false;
    }

    public void SpriteRam_Address_Register_Write(byte data) {
        spriteRamAddress = data;
    }

    public void SpriteRam_IO_Register_Write(byte data) {
        spriteRam[spriteRamAddress] = data;
        spriteRamAddress++;
    }

    public void VRAM_Address_Register_1_Write(byte data) {
        if (vramHiLoToggle == 1) {
            scrollV = data & 0xff;
            vramHiLoToggle = 0;
        }
        else {
            scrollH = data & 0xff;
            if (scrollH > 239) {
                scrollH = 0;
            }
//            if (fix_scrolloffset2) {
//                if (currentScanline < 240) {
//                    scrollH = (byte) (scrollH - currentScanline + 8) & 0xff;
//                }
//            }
//            if (fix_scrolloffset1) {
//                if (currentScanline < 240) {
//                    scrollH = (byte) (scrollH - currentScanline) & 0xff;
//                }
//            }
//            if (fix_scrolloffset3) {
//                if (currentScanline < 240)
//                    scrollH = 238;
//            }
            vramHiLoToggle = 1;
        }
    }

    public void VRAM_Address_Register_2_Write(byte data) {
        if (vramHiLoToggle == 1) {
            prev_vramReadWriteAddress = vramReadWriteAddress;
            vramReadWriteAddress = (int) data << 8;
            vramHiLoToggle = 0;
        }
        else {
            vramReadWriteAddress = vramReadWriteAddress + (data & 0xff);
            if ((prev_vramReadWriteAddress == 0) && (currentScanline < 240)) {
                // We may have a scrolling trick
                if ((vramReadWriteAddress >= 0x2000) && (vramReadWriteAddress <= 0x2400))
                    scrollH = (byte) (((vramReadWriteAddress - 0x2000) / 0x20) * 8 - currentScanline);
            }
            vramHiLoToggle = 1;
        }
    }

    public void VRAM_IO_Register_Write(byte data) {
        if (vramReadWriteAddress < 0x2000) {
            chr_roms[vramReadWriteAddress] = data;
            // _NesEmu.myMapper.WriteChrRom((ushort)vramReadWriteAddress, data);
        }
        else if ((vramReadWriteAddress >= 0x2000) && (vramReadWriteAddress < 0x3f00)) {
            if (mirroring == MIRRORING.HORIZONTAL) {
                switch (vramReadWriteAddress & 0x2C00) {
                    case (0x2000):
                        nameTables[vramReadWriteAddress - 0x2000] = data;
                        break;
                    case (0x2400):
                        nameTables[(vramReadWriteAddress - 0x400) - 0x2000] = data;
                        break;
                    case (0x2800):
                        nameTables[vramReadWriteAddress - 0x400 - 0x2000] = data;
                        break;
                    case (0x2C00):
                        nameTables[(vramReadWriteAddress - 0x800) - 0x2000] = data;
                        break;
                }
            }
            else if (mirroring == MIRRORING.VERTICAL) {
                switch (vramReadWriteAddress & 0x2C00) {
                    case (0x2000):
                        nameTables[vramReadWriteAddress - 0x2000] = data;
                        break;
                    case (0x2400):
                        nameTables[vramReadWriteAddress - 0x2000] = data;
                        break;
                    case (0x2800):
                        nameTables[vramReadWriteAddress - 0x800 - 0x2000] = data;
                        break;
                    case (0x2C00):
                        nameTables[(vramReadWriteAddress - 0x800) - 0x2000] = data;
                        break;
                }
            }
            else if (mirroring == MIRRORING.ONE_SCREEN) {
                if (mirroringBase == 0x2000) {
                    switch (vramReadWriteAddress & 0x2C00) {
                        case (0x2000):
                            nameTables[vramReadWriteAddress - 0x2000] = data;
                            break;
                        case (0x2400):
                            nameTables[vramReadWriteAddress - 0x400 - 0x2000] = data;
                            break;
                        case (0x2800):
                            nameTables[vramReadWriteAddress - 0x800 - 0x2000] = data;
                            break;
                        case (0x2C00):
                            nameTables[vramReadWriteAddress - 0xC00 - 0x2000] = data;
                            break;
                    }
                }
                else if (mirroringBase == 0x2400) {
                    switch (vramReadWriteAddress & 0x2C00) {
                        case (0x2000):
                            nameTables[vramReadWriteAddress + 0x400 - 0x2000] = data;
                            break;
                        case (0x2400):
                            nameTables[vramReadWriteAddress - 0x2000] = data;
                            break;
                        case (0x2800):
                            nameTables[vramReadWriteAddress - 0x400 - 0x2000] = data;
                            break;
                        case (0x2C00):
                            nameTables[vramReadWriteAddress - 0x800 - 0x2000] = data;
                            break;
                    }
                }
            }
            // four screen mirroring (Croser)
            else {
                nameTables[vramReadWriteAddress - 0x2000] = data;
            }
        }
        else if ((vramReadWriteAddress >= 0x3f00) && (vramReadWriteAddress < 0x3f20)) {
            nameTables[vramReadWriteAddress - 0x2000] = data;
            if ((vramReadWriteAddress & 0x7) == 0) {
                nameTables[(vramReadWriteAddress - 0x2000) ^ 0x10] = data;
            }
        }
        vramReadWriteAddress = vramReadWriteAddress + ppuAddressIncrement;
    }

    public void SpriteRam_DMA_Begin(byte data) {
        int i;
        for (i = 0; i < 0x100; i++) {
            spriteRam[i] = mb.ReadBus8((data & 0xFF) * 0x100 + i);
        }
    }
}
