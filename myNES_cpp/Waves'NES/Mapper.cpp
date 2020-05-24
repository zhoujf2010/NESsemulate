#include "StdAfx.h"
#include "Mapper.h"
#include "Waves'NES.h"

MAPPER NES_Mapper;

#include "Mapper000.h"
#include "Mapper002.h"

void Mapper_Init(void)
{
	NES_Mapper.Reset         = Mapper_Reset;
	NES_Mapper.Write         = Mapper_Write;
	NES_Mapper.Read          = Mapper_Read;
	NES_Mapper.WriteLow      = Mapper_WriteLow;
	NES_Mapper.ReadLow       = Mapper_ReadLow;	
	NES_Mapper.ExWrite       = Mapper_ExWrite;
	NES_Mapper.ExRead        = Mapper_ExRead;
	NES_Mapper.ExCmdWrite    = Mapper_ExCmdWrite;
	NES_Mapper.ExCmdRead     = Mapper_ExCmdRead;
	NES_Mapper.HSync         = Mapper_HSync;
	NES_Mapper.VSync         = Mapper_VSync;
	NES_Mapper.Clock         = Mapper_Clock;		
	NES_Mapper.PPU_ChrLatch  = Mapper_PPU_ChrLatch;
	NES_Mapper.PPU_ExtLatch  = Mapper_PPU_ExtLatch;
	NES_Mapper.PPU_ExtLatchX = Mapper_PPU_ExtLatchX;
	NES_Mapper.PPU_Latch     = Mapper_PPU_Latch;
	NES_Mapper.IsStateSave   = Mapper_IsStateSave;
	NES_Mapper.LoadState     = Mapper_LoadState;
	NES_Mapper.SaveState     = Mapper_SaveState;
}

BOOL CreateMapper(int no)
{
	Mapper_Init();

	switch (no)
	{
	case 0 :	Mapper000_Init();	break;
	case 2 :	Mapper002_Init();	break;
	default :
		//ASSERT(FALSE)
		;
	}

	return TRUE;
}

void Mapper_Reset(void)
{
}

// $4100-$7FFF Lower Memory read
BYTE Mapper_ReadLow( WORD addr )
{
	// $6000-$7FFF WRAM
	if( addr >= 0x6000 && addr <= 0x7FFF )
	{
		return	CPU_MEM_BANK[addr>>13][addr&0x1FFF];
	}

	return	(BYTE)(addr>>8);
}

// $4100-$7FFF Lower Memory write
void Mapper_WriteLow( WORD addr, BYTE data )
{
	// $6000-$7FFF WRAM
	if( addr >= 0x6000 && addr <= 0x7FFF )
	{
		CPU_MEM_BANK[addr>>13][addr&0x1FFF] = data;
	}
}

// $8000-$FFFF Memory write
void	Mapper_Write( WORD addr, BYTE data ) 
{
}

// $8000-$FFFF Memory read(Dummy)
void	Mapper_Read( WORD addr, BYTE data ) 
{
}

// $4018-$40FF Extention register read/write
BYTE	Mapper_ExRead( WORD addr )	
{ 
	return 0x00; 
}

void	Mapper_ExWrite( WORD addr, BYTE data ) 
{
}

BYTE	Mapper_ExCmdRead ( EXCMDRD cmd )	
{ 
	return 0x00; 
}

void	Mapper_ExCmdWrite( EXCMDWR cmd, BYTE data ) 
{
}

// H sync/V sync/Clock sync
void	Mapper_HSync( INT scanline ) {}
void	Mapper_VSync() {}
void	Mapper_Clock( INT cycles ) {}

// PPU address bus latch
void	Mapper_PPU_Latch( WORD addr ) {}

// PPU Character latch
void	Mapper_PPU_ChrLatch( WORD addr ) {}

// PPU Extention character/palette
void	Mapper_PPU_ExtLatchX( INT x ) {}
void	Mapper_PPU_ExtLatch( WORD addr, BYTE& chr_l, BYTE& chr_h, BYTE& attr ) {}

// For State save
BOOL	Mapper_IsStateSave() { return FALSE; }
void	Mapper_SaveState( LPBYTE p ) {}
void	Mapper_LoadState( LPBYTE p ) {}

