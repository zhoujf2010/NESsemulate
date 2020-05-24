#pragma once

#ifndef _PPU_H_
#define _PPU_H_

#include "PPUINC.h"

//PPU控制寄存器1掩码(0x2000)
#define	PPU_VBLANK_BIT		0x80
#define	PPU_SPHIT_BIT		0x40
#define	PPU_SP16_BIT		0x20
#define	PPU_BGTBL_BIT		0x10
#define	PPU_SPTBL_BIT		0x08
#define	PPU_INC32_BIT		0x04
#define	PPU_NAMETBL_BIT		0x03

//PPU控制寄存器2掩码(0x2001)
#define PPU_SHOWCOLOR		0x00	//彩色
#define PPU_NOCOLOR			0x01	//单色
#define PPU_LEFT8COL		0x02	//显示屏幕的左8列
#define PPU_SPRLEFT8COL		0x04	//可在左8列显示精灵
#define PPU_SHOWBG			0x08	//背景页显示开关
#define PPU_SHOWSPR			0x10	//卡通页显示开关

//PPU状态寄存器掩码(0x2002)
#define	PPU_VBLANK_FLAG		0x80
#define	PPU_SPHIT_FLAG		0x40
#define	PPU_SPMAX_FLAG		0x20
#define	PPU_WENABLE_FLAG	0x10


#define SP_VREVERT			0x80	//精灵垂直反转
#define SP_HREVERT			0x40	//精灵水平反转
#define SP_LEVEL			0x20	//精灵的显示层
#define SP_HIGHCOLOR		0X03	//精灵颜色的高2位


class PPU6528
{
public:
	PPU6528(void);

	void Reset();

	void ScanlineStart();
	void RanderBottomBG(LPBYTE pBit);			//渲染底背景
	void ScanLine(LPBYTE pBit, int LineNo);	//渲染扫描线
	void ScanHitPoint(BYTE LineNo);
	void ScanSprite(LPBYTE pBit, BYTE LineNo, BOOL bBackLevel);
	void ScanBG(LPBYTE pBit, BYTE LineNo);
	BYTE GetScreenBGColor(int x, int y);
	void VBlankStart();						//VBlank开始
	void VBlankEnd();						//VBlank结束

	BYTE ReadFromPort(WORD addr);
	void WriteToPort(WORD addr, BYTE val);

	void NameTableMap(const char* filename);

public:

	~PPU6528(void);

private:
	BYTE Read(WORD addr);
	void Write(WORD addr, BYTE val);

public:
	BYTE * m_PatternTable;
	BYTE * m_NameTable[4];
	BYTE m_REG[0x04];						//$2000-$2003

	SCREENOFFSET m_ScreenOffset;
	SCREENOFFSET m_CurLineOft;
	ADDRESS      m_Address;
	BYTE         m_Reg2007Temp;

	int          m_CurLineSprite;
	WORD         m_ByteIndex;
	WORD         m_CurByteIndex;
	BYTE         m_CurOffsetX8;
	BYTE         m_CurOffsetY8;

};

extern PPU6528 PPU;
extern COLORREF ColorTable[64];

#endif
