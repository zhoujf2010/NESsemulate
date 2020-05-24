#include "StdAfx.h"
//#include "Afx.h"
#include "Waves'NES.h"
#include "CPU.h"
#include "PPU.h"
#include <stdio.h>

COLORREF ColorTable[64] = 
{
	RGB(0x7F, 0x7F, 0x7F), RGB(0x20, 0x00, 0xB0), RGB(0x28, 0x00, 0xB8), RGB(0x60, 0x10, 0xA0),
	RGB(0x98, 0x20, 0x78), RGB(0xB0, 0x10, 0x30), RGB(0xA0, 0x30, 0x00), RGB(0x78, 0x40, 0x00),
	RGB(0x48, 0x58, 0x00), RGB(0x38, 0x68, 0x00), RGB(0x38, 0x6C, 0x00), RGB(0x30, 0x60, 0x40),
	RGB(0x30, 0x50, 0x80), RGB(0x00, 0x00, 0x00), RGB(0x00, 0x00, 0x00), RGB(0x00, 0x00, 0x00),

	RGB(0xBC, 0xBC, 0xBC), RGB(0x40, 0x60, 0xF8), RGB(0x40, 0x40, 0xFF), RGB(0x90, 0x40, 0xF0),
	RGB(0xD8, 0x40, 0xC0), RGB(0xD8, 0x40, 0x60), RGB(0xE0, 0x50, 0x00), RGB(0xC0, 0x70, 0x00),
	RGB(0x88, 0x88, 0x00), RGB(0x50, 0xA0, 0x00), RGB(0x48, 0xA8, 0x10), RGB(0x48, 0xA0, 0x68),
	RGB(0x40, 0x90, 0xC0), RGB(0x00, 0x00, 0x00), RGB(0x00, 0x00, 0x00), RGB(0x00, 0x00, 0x00),

	RGB(0xFF, 0xFF, 0xFF), RGB(0x60, 0xA0, 0xFF), RGB(0x50, 0x80, 0xFF), RGB(0xA0, 0x70, 0xFF),
	RGB(0xF0, 0x60, 0xFF), RGB(0xFF, 0x60, 0xB0), RGB(0xFF, 0x78, 0x30), RGB(0xFF, 0xA0, 0x00),
	RGB(0xE8, 0xD0, 0x20), RGB(0x98, 0xE8, 0x00), RGB(0x70, 0xF0, 0x40), RGB(0x70, 0xE0, 0x90),
	RGB(0x60, 0xD0, 0xE0), RGB(0x60, 0x60, 0x60), RGB(0x00, 0x00, 0x00), RGB(0x00, 0x00, 0x00),

	RGB(0xFF, 0xFF, 0xFF), RGB(0x90, 0xD0, 0xFF), RGB(0xA0, 0xB8, 0xFF), RGB(0xC0, 0xB0, 0xFF),
	RGB(0xE0, 0xB0, 0xFF), RGB(0xFF, 0xB8, 0xE8), RGB(0xFF, 0xC8, 0xB8), RGB(0xFF, 0xD8, 0xA0),
	RGB(0xFF, 0xF0, 0x90), RGB(0xC8, 0xF0, 0x80), RGB(0xA0, 0xF0, 0xA0), RGB(0xA0, 0xFF, 0xC8),
	RGB(0xA0, 0xFF, 0xF0), RGB(0xA0, 0xA0, 0xA0), RGB(0x00, 0x00, 0x00), RGB(0x00, 0x00, 0x00)
};

PPU6528 PPU;

PPU6528::PPU6528(void)
{
}

PPU6528::~PPU6528(void)
{
}

void PPU6528::Reset()
{
	::memset(m_REG, 0, sizeof(m_REG));
	::memset(SPRAM, 0, sizeof(SPRAM));
	m_ByteIndex = 0;
}

BYTE PPU6528::ReadFromPort(WORD addr)
{
	BYTE value(0x00);

	switch (addr)
	{
	case 0x2002 :
		value = m_REG[2];
		m_Address.Reset();
		m_ScreenOffset.Reset();
		m_REG[2] &= ~PPU_VBLANK_FLAG;
		break;
	case 0x2004 :
		value = SPRAM[m_REG[3]++];
		break;
	case 0x2007 :
		value = m_Reg2007Temp;
		WORD address = m_Address.GetAddress();
		m_Reg2007Temp = Read(address);
		if(address >= 0x3F00 && address < 0x4000)
		{
			if(address & 0x0010)
				value = SPPal[address & 0xF];
			else
				value = BGPal[address & 0xF];
		}
		m_Address.Step(m_REG[0] & PPU_INC32_BIT ? 32 : 1);
	}

	return value;
}

void PPU6528::WriteToPort(WORD addr, BYTE val)
{
	switch (addr)
	{
	case 0x2000 :
		//命名表地址
		//(b:0000110000000000) = (d:00000011<<10)	(10-11)	
		m_ByteIndex = (m_ByteIndex & 0xF3FF) | (((WORD)val & 0x03) << 10);
		if((val & 0x80) && !(m_REG[0] & 0x80) && (m_REG[2] & 0x80))
			CPU.NMI();
		m_REG[0] = val;
		break;
	case 0x2001 :
		m_REG[1] = val;
		break;
	case 0x2003 :
		m_REG[3] = val;
		break;
	case 0x2004 :
		SPRAM[m_REG[3]++] = val;
		break;
	case 0x2005 :
		if (m_ScreenOffset.AtX())
			m_ByteIndex = (m_ByteIndex & 0xFFE0) | (val >> 3);
		else
			m_ByteIndex = (m_ByteIndex & 0xFC1F) | ((val & 0xF8) << 2);
		m_ScreenOffset.SetValue(val);
		break;
	case 0x2006 :
		if (!m_Address.AtLow())
		{
			m_ByteIndex = (m_ByteIndex & 0xF3FF) | ((val & 0x0C) << 8);
		}
		m_Address.SetAddress(val);
		break;
	case 0x2007 :
		Write(m_Address.GetAddress(), val);
		m_Address.Step(m_REG[0] & PPU_INC32_BIT ? 32 : 1);
		break;
	}
}

BYTE PPU6528::Read(WORD addr)
{
	if (addr < 0x3000)
		return PPU_MEM_BANK[addr >> 10][addr & 0x3FF];
	else if (addr < 0x3F00)
		return Read(addr - 0x1000);
	else if (addr < 0x4000)
	{
		if (addr & 0x0010)
			return SPPal[addr & 0xF];
		else
			return BGPal[addr & 0xF];
	}
	else
	{
		return Read(addr & 0x3FFF);
	}

	return 0;
}

void PPU6528::Write(WORD addr, BYTE val)
{
	if (addr < 0x3000)
		PPU_MEM_BANK[addr >> 10][addr & 0x3FF] = val;
	else if (addr < 0x3F00)
		Write(addr - 0x1000, val);
	else if (addr < 0x4000)
	{
		if (addr & 0x0010)
			SPPal[addr & 0xF] = val;
		else
			BGPal[addr & 0xF] = val;

		if (!(addr & 0x000F))
		{
			BGPal[0x0] = SPPal[0x10] = val;
			BGPal[0x04] = BGPal[0x08] = BGPal[0x0C] = BGPal[0x00];
			SPPal[0x14] = SPPal[0x18] = SPPal[0x1C] = SPPal[0x10];
		}
	}
	else
	{
		Write(addr & 0x3FFF, val);
	}
}

void PPU6528::ScanlineStart()
{
	if (PPU.m_REG[1] & (PPU_SHOWBG | PPU_SHOWSPR))
	{
		m_CurLineOft = m_ScreenOffset;
		m_CurByteIndex = m_ByteIndex;
	}
}

void PPU6528::RanderBottomBG(LPBYTE pBit)
{
	::memset(pBit, BGPal[0], 256 * 240);
}

void PPU6528::ScanLine(LPBYTE pBit, int LineNo)
{
	m_REG[2] &= ~PPU_SPMAX_FLAG;
	m_CurLineSprite = 0;
	m_CurOffsetY8 = (m_CurLineOft.y + LineNo) & 0x7;

	if (LineNo < 240 && m_REG[1] & PPU_SHOWSPR)
	{
		ScanSprite(pBit, LineNo, TRUE);	//扫描后台精灵
	}
	if (LineNo < 240 && m_REG[1] & PPU_SHOWBG)
		ScanBG(pBit, LineNo);			//扫描背景页
	if (LineNo < 240 && m_REG[1] & PPU_SHOWSPR)
	{
		ScanSprite(pBit, LineNo, FALSE);	//扫描前台精灵
		ScanHitPoint(LineNo);			//扫描Hit点
	}

	/*	
	ASSERT(m_CurLineOft.y < 240);

	if (m_REG[1] & PPU_SHOWBG)
	{
	BYTE NTIndex = (m_CurByteIndex >> 10) & 0x3d;	//NameTable index
	WORD ByteIndex = ((m_CurLineOft.y + LineNo) % 240 >> 3 << 5) + (m_CurLineOft.x >> 3);
	if (LineNo + m_CurLineOft.y >= 240)
	NTIndex ^= 0x2;

	BYTE YOft = (LineNo + m_CurLineOft.y) & 0x7;
	BYTE * pBGPattern = PPU_MEM_BANK[(m_REG[0] & 0x10) >> 2];
	WORD AttrIndex = (((ByteIndex >> 7) & 0x7) << 3) + ((ByteIndex >> 2) & 0x7) + 0x3C0;

	int LoopByte = m_CurLineOft.x & 0x7 ? 33 : 32;
	for (int i = 0; i < LoopByte; i++)
	{
	if (i && !(ByteIndex & 0x1F))
	{
	AttrIndex &= 0xFFF8;
	ByteIndex -=32;
	NTIndex ^= 1;
	}
	else if (i && !(ByteIndex & 0x3))
	{
	AttrIndex++;
	}

	int AttrBitOft = (ByteIndex >> 5) & 0x2;
	BYTE ByteLow  = pBGPattern[PPU_MEM_BANK[NTIndex + 8][ByteIndex] * 16 + YOft];
	BYTE ByteHigh = pBGPattern[PPU_MEM_BANK[NTIndex + 8][ByteIndex] * 16 + YOft + 8];
	if (ByteLow | ByteHigh)
	{
	AttrBitOft |= (ByteIndex & 0x2) >> 1;
	BYTE ByteAttr = PPU_MEM_BANK[NTIndex + 8][AttrIndex];
	BYTE AttrBit  = (BYTE)(((ByteAttr >> (AttrBitOft * 2)) & 0x3) << 2);
	BYTE * pBGPAL = &BGPal[AttrBit];

	for (int j = 0; j < 8; j++)
	{
	BYTE ColorIndex = ((ByteHigh >> (7 - j)) & 0x1) << 1;
	ColorIndex |= ((ByteLow >> (7 - j)) & 0x1);
	if (ColorIndex)
	{
	ColorIndex |= AttrBit;
	int xoft = i * 8 + j - (m_CurLineOft.x & 0x7);
	if (xoft >= 0 && xoft <= 255)
	{
	pBit[((239 - LineNo) << 8) + ((i << 3) + j - (m_CurLineOft.x & 0x7))] = BGPal[ColorIndex];
	}
	}
	}
	}
	ByteIndex++;
	}
	}
	*/
}

//扫描Hit点
void PPU6528::ScanHitPoint(BYTE LineNo)
{
	if (m_REG[2] & PPU_SPHIT_FLAG)
		return;

	int sp_h = (PPU.m_REG[0] & PPU_SP16_BIT) ? 15 : 7;		/* Sprite size */

	int dy = (int)LineNo - ((int)Sprite[0].Y + 1);
	if (dy < 0 || dy > sp_h)
		return;

	if (Sprite[0].Attribute & SP_VREVERT)
		dy = sp_h - dy;

	WORD spraddr;
	if (!(PPU.m_REG[0] & PPU_SP16_BIT))
	{
		// 8x8 Sprite
		spraddr = (((WORD)PPU.m_REG[0] & PPU_SPTBL_BIT) << 9)+((WORD)Sprite[0].Index << 4);
		if (!(Sprite[0].Attribute & SP_VREVERT))
			spraddr += dy;
		else
			spraddr += 7 - dy;
	}
	else
	{
		// 8x16 Sprite
		spraddr = (((WORD)Sprite[0].Index & 1) << 12) + (((WORD)Sprite[0].Index & 0xFE) << 4);
		if (!(Sprite[0].Attribute & SP_VREVERT))
			spraddr += ((dy & 8) << 1) + (dy & 7);
		else
			spraddr += ((~dy & 8) << 1) + (7 - (dy & 7));
	}

	BYTE LowByte  = PPU_MEM_BANK[spraddr >> 10][ spraddr & 0x3FF     ];
	BYTE HighByte = PPU_MEM_BANK[spraddr >> 10][(spraddr & 0x3FF) + 8];

	if (Sprite[0].Attribute & SP_HREVERT)
	{
		LowByte  = RevertByte[LowByte];
		HighByte = RevertByte[HighByte];
	}

	for (int i = 0; i < 8; i++)
	{
		if (LowByte & (1 << (7-i)) || HighByte & (1 << (7-i)))
		{
			if (GetScreenBGColor(Sprite[0].X + i, LineNo) & 0x03)
			{
				m_REG[2] |= PPU_SPHIT_FLAG;
				break;
			}
		}
	}
}

void PPU6528::ScanSprite(LPBYTE pBit, BYTE LineNo, BOOL bBackLevel)
{
	int sp_h = (PPU.m_REG[0] & PPU_SP16_BIT) ? 15 : 7;		//精灵尺寸，7或15

	for (int i = 0; i < 64; i++)
	{
		if (Sprite[i].Attribute & SP_LEVEL && !bBackLevel)
			continue;
		if (!(Sprite[i].Attribute & SP_LEVEL) && bBackLevel)
			continue;
		int dy = (int)LineNo - ((int)Sprite[i].Y + 1);
		if (dy != (dy & sp_h))
			continue;

		m_CurLineSprite++;
		if (m_CurLineSprite >= 8)
			m_REG[2] |= PPU_SPMAX_FLAG;

		WORD spraddr;
		if (!(PPU.m_REG[0] & PPU_SP16_BIT))
		{
			// 8x8 Sprite
			spraddr = (((WORD)PPU.m_REG[0] & PPU_SPTBL_BIT) << 9)+((WORD)Sprite[i].Index << 4);
			if (!(Sprite[i].Attribute & SP_VREVERT))
				spraddr += dy;
			else
				spraddr += 7 - dy;
		}
		else
		{
			// 8x16 Sprite
			spraddr = (((WORD)Sprite[i].Index & 1) << 12) + (((WORD)Sprite[i].Index & 0xFE) << 4);
			if (!(Sprite[i].Attribute & SP_VREVERT))
				spraddr += ((dy & 8) << 1) + (dy & 7);
			else
				spraddr += ((~dy & 8) << 1) + (7 - (dy & 7));
		}

		BYTE LowByte  = PPU_MEM_BANK[spraddr >> 10][ spraddr & 0x3FF     ];
		BYTE HighByte = PPU_MEM_BANK[spraddr >> 10][(spraddr & 0x3FF) + 8];

		if (Sprite[i].Attribute & SP_HREVERT)
		{
			LowByte  = RevertByte[LowByte];
			HighByte = RevertByte[HighByte];
		}
		BYTE Color;
		BYTE HighColor = (Sprite[i].Attribute & SP_HIGHCOLOR) << 2;
		for (int j = 0; j < 8; j++)
		{
			Color = HighColor | ((HighByte >> (7 - j) << 1) & 0x02) | ((LowByte >> (7 - j)) & 0x01);
			if (Color & 0x03 && Sprite[i].X + j <= 255)
				pBit[(239 - LineNo) * 256 + Sprite[i].X + j] = SPPal[Color];
		}
	}
}

void PPU6528::ScanBG(LPBYTE pBit, BYTE LineNo)
{
	//ASSERT(m_CurLineOft.y < 240);//可能有问题
	/*
	if (m_CurLineOft.y < 240)
	{
		return;
	}
	*/

	BYTE NTIndex = (m_CurByteIndex >> 10) & 0x3d;	//NameTable index
	WORD ByteIndex = ((m_CurLineOft.y + LineNo) % 240 >> 3 << 5) + (m_CurLineOft.x >> 3);
	if (LineNo + m_CurLineOft.y >= 240)
	{
		NTIndex ^= 0x2;
	}

	BYTE YOft = (LineNo + m_CurLineOft.y) & 0x7;
	BYTE * pBGPattern = PPU_MEM_BANK[(m_REG[0] & 0x10) >> 2];
	WORD AttrIndex = (((ByteIndex >> 7) & 0x7) << 3) + ((ByteIndex >> 2) & 0x7) + 0x3C0;

	int LoopByte = m_CurLineOft.x & 0x7 ? 33 : 32;
	for (int i = 0; i < LoopByte; i++)
	{
		if (i && !(ByteIndex & 0x1F))
		{
			AttrIndex &= 0xFFF8;
			ByteIndex -=32;
			NTIndex ^= 1;
		}
		else if (i && !(ByteIndex & 0x3))
		{
			AttrIndex++;
		}

		int AttrBitOft = (ByteIndex >> 5) & 0x2;
		BYTE ByteLow  = pBGPattern[PPU_MEM_BANK[NTIndex + 8][ByteIndex] * 16 + YOft];
		BYTE ByteHigh = pBGPattern[PPU_MEM_BANK[NTIndex + 8][ByteIndex] * 16 + YOft + 8];
		if (ByteLow | ByteHigh)
		{
			AttrBitOft |= (ByteIndex & 0x2) >> 1;
			BYTE ByteAttr = PPU_MEM_BANK[NTIndex + 8][AttrIndex];
			BYTE AttrBit  = (BYTE)(((ByteAttr >> (AttrBitOft * 2)) & 0x3) << 2);
			for (int j = 0; j < 8; j++)
			{
				BYTE ColorIndex = ((ByteHigh >> (7 - j)) & 0x1) << 1;
				ColorIndex |= ((ByteLow >> (7 - j)) & 0x1);
				if (ColorIndex)
				{
					ColorIndex |= AttrBit;
					int xoft = i * 8 + j - (m_CurLineOft.x & 0x7);
					if (xoft >= 0 && xoft <= 255)
						pBit[(239 - LineNo) * 256 + (i * 8 + j - (m_CurLineOft.x & 0x7))] = BGPal[ColorIndex];
				}
			}
		}

		ByteIndex++;
	}
}

BYTE PPU6528::GetScreenBGColor(int x, int y)
{
	int CurNameTable = (m_CurByteIndex >> 10) & 0x3;
	int OffsetX = (m_CurLineOft.x + x) & 0xFF;
	int TotalY = m_CurLineOft.y + y;

	WORD NameByte = PPU_MEM_BANK[CurNameTable + 8][(TotalY >> 3 << 5)  + (OffsetX >> 3)];
	BYTE * pBGPattern = PPU_MEM_BANK[(m_REG[0] & 0x10) >> 2];
	BYTE LowByte  = pBGPattern[(NameByte << 4) + (TotalY & 0x7)];
	BYTE HighByte = pBGPattern[(NameByte << 4) + 8 + (TotalY & 0x7)];
	if (!(LowByte | HighByte))
		return 0;

	BYTE ByteBit = 7 - (OffsetX & 0x7);
	return GetBit(LowByte, ByteBit) | (GetBit(HighByte, ByteBit) << 1);
}

void PPU6528::VBlankStart()
{
	m_REG[2] |= PPU_VBLANK_FLAG;
	if (m_REG[0] & PPU_VBLANK_BIT)
		CPU.NMI();
}

void PPU6528::VBlankEnd()
{
	m_REG[2] &= ~PPU_VBLANK_FLAG;
	m_REG[2] &= ~PPU_SPHIT_FLAG;
}

void PPU6528::NameTableMap(const char* filename)//可能有问题
{
	char buf[4] = {0};

	FILE * file;
	file = (FILE *)fopen(filename, "wb");
	if (file == NULL)
	{
		return;
	}
	for (int i = 0; i < 4; i++)
	{
		for (int j = 0; j < 1024; j++)
		{
			if (!(j & 0x1F))
				fwrite("\r\n", 2, 1, file);
			if (j == 960)
				fwrite("\r\n", 2, 1, file);
			sprintf(buf, "%02X ", m_NameTable[i][j]);
			fwrite(buf, 3, 1, file);//可能有问题
		}
		fwrite("\r\n", 2, 1, file);
		fwrite("\r\n", 2, 1, file);
	}
	fclose(file);
}
