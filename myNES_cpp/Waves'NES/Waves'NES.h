#pragma once

#ifndef _WAVESNES_H_
#define _WAVESNES_H_

#include "resource.h"
#include "JOYPAD.h"

#define PRG_BLOCK_SIZE	0x4000
#define PAT_BLOCK_SIZE	0x2000

#define	FETCH_CYCLES	8

typedef struct tagNESCONFIG
{
	float BaseClock;		// NTSC:21477270.0  PAL:21281364.0
	float CpuClock;			// NTSC: 1789772.5  PAL: 1773447.0
	int	TotalScanlines;		// NTSC: 262  PAL: 312
	int	ScanlineCycles;		// NTSC:1364  PAL:1362
	int	HDrawCycles;		// NTSC:1024  PAL:1024
	int	HBlankCycles;		// NTSC: 340  PAL: 338
	int	ScanlineEndCycles;	// NTSC:   4  PAL:   2
	int	FrameCycles;		// NTSC:29829.52  PAL:35468.94
	int	FrameIrqCycles;		// NTSC:29829.52  PAL:35468.94
	int	FrameRate;			// NTSC:60(59.94) PAL:50
	float	FramePeriod;	// NTSC:16.683    PAL:20.0
} NESCONFIG, * LPNESCONFIG;

typedef struct tagSPRITE
{
	BYTE Y;
	BYTE Index;
	BYTE Attribute;
	BYTE X;
} SPRITE;

extern BYTE RevertByte[256];

extern BYTE RomHeader[16];

extern BYTE RAM[0x2000];			//供CPU使用的8K内存
extern BYTE SRAM[0x2000];			//卡带的SRAM
extern BYTE * PRGBlock;

extern BYTE * PatternTable;			//图案表
extern BYTE NameTable[0x800];		//命名表
extern BYTE BGPal[0x10];			//背景调色板
extern BYTE SPPal[0x10];			//精灵调色板
extern BYTE SPRAM[0x100];			//精灵RAM，256字节，可存放64个精灵
extern SPRITE * Sprite;

extern BYTE RevertByte[256];

extern int PROM_8K_SIZE;
extern LPBYTE CPU_MEM_BANK[8];		//每个元素指向8K数据
extern LPBYTE PPU_MEM_BANK[12];		//每个元素指向1K数据


void NES_Init();

void NES_Start();

void NES_Stop();

void _cdecl NES_ThreadProc(LPVOID lpParam);

void NES_FrameExec();

void NES_Shutdown();
void NES_ReleasePRGBlock();
void NES_ReleasePatternTable();
BOOL NES_IsRunning();

HRESULT LoadRomFile(HWND hwnd);

HRESULT PromptForFileOpen(HWND hwndOwner);

void CovertRouteFormat(char* strFileName);

BYTE NES_Read(WORD addr);

void NES_Write(WORD addr, BYTE val);

BYTE * NES_GetRAM(WORD addr);

BYTE NES_ReadReg(WORD addr);

void NES_WriteReg(WORD addr, BYTE val);

void SetPROM_8K_Bank(BYTE page, int bank);
void SetPROM_16K_Bank(BYTE page, int bank);
void SetPROM_32K_Bank(int bank);
void SetPROM_32K_Bank(int bank0, int bank1, int bank2, int bank3);

void SetVROM_1K_Bank(BYTE page, int bank);
void SetVROM_2K_Bank(BYTE page, int bank);
void SetVROM_4K_Bank(BYTE page, int bank);
void SetVROM_8K_Bank(int bank);

void SetCRAM_1K_Bank(BYTE page, int bank);
void SetCRAM_2K_Bank(BYTE page, int bank);
void SetCRAM_4K_Bank(BYTE page, int bank);
void SetCRAM_8K_Bank(int bank);

void SetNameTable_Bank(int bank0, int bank1, int bank2, int bank3);

#endif
