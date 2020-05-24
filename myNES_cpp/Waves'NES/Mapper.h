#pragma once

#ifndef _MAPPER_H_
#define _MAPPER_H_

// Extension commands
// For ExCmdRead command
enum	EXCMDRD {
	EXCMDRD_NONE = 0,
	EXCMDRD_DISKACCESS,
};
// For ExCmdWrite command
enum	EXCMDWR {
	EXCMDWR_NONE = 0,
	EXCMDWR_DISKINSERT,
	EXCMDWR_DISKEJECT,
};

// Mapper base class
typedef struct
{
	// For Mapper
	// Reset
	void	(*Reset)();

	// $8000-$FFFF Memory write
	void	(*Write)( WORD addr, BYTE data );

	// $8000-$FFFF Memory read(Dummy)
	void	(*Read)( WORD addr, BYTE data );

	// $4100-$7FFF Lower Memory read/write
	BYTE	(*ReadLow)( WORD addr );
	void	(*WriteLow)( WORD addr, BYTE data );

	// $4018-$40FF Extention register read/write
	BYTE	(*ExRead)( WORD addr );
	void	(*ExWrite)( WORD addr, BYTE data );

	BYTE	(*ExCmdRead)( EXCMDRD cmd );
	void	(*ExCmdWrite)( EXCMDWR cmd, BYTE data );

	// H sync/V sync/Clock sync
	void	(*HSync)( INT scanline );
	void	(*VSync)();
	void	(*Clock)( INT cycles );

	// PPU address bus latch
	void	(*PPU_Latch)( WORD addr );

	// PPU Character latch
	void	(*PPU_ChrLatch)( WORD addr );

	// PPU Extention character/palette
	void	(*PPU_ExtLatchX)( INT x );
	void	(*PPU_ExtLatch)( WORD addr, BYTE& chr_l, BYTE& chr_h, BYTE& attr );

	// For State save
	BOOL	(*IsStateSave)();
	void	(*SaveState)( LPBYTE p );
	void	(*LoadState)( LPBYTE p );
} MAPPER;

extern MAPPER NES_Mapper;

void Mapper_Init(void);
BOOL CreateMapper(int no);


extern 	void	Mapper_Reset();

// $8000-$FFFF Memory write
void Mapper_Write( WORD addr, BYTE data );

// $8000-$FFFF Memory read(Dummy)
void Mapper_Read( WORD addr, BYTE data );

// $4100-$7FFF Lower Memory read/write
BYTE Mapper_ReadLow( WORD addr );
void Mapper_WriteLow( WORD addr, BYTE data );

// $4018-$40FF Extention register read/write
BYTE Mapper_ExRead( WORD addr );
void Mapper_ExWrite( WORD addr, BYTE data );

BYTE Mapper_ExCmdRead( EXCMDRD cmd );
void Mapper_ExCmdWrite( EXCMDWR cmd, BYTE data );

// H sync/V sync/Clock sync
void Mapper_HSync( INT scanline );
void Mapper_VSync();
void Mapper_Clock( INT cycles );

// PPU address bus latch
void Mapper_PPU_Latch( WORD addr );

// PPU Character latch
void Mapper_PPU_ChrLatch( WORD addr );

// PPU Extention character/palette
void Mapper_PPU_ExtLatchX( INT x );
void Mapper_PPU_ExtLatch( WORD addr, BYTE& chr_l, BYTE& chr_h, BYTE& attr );

// For State save
BOOL Mapper_IsStateSave();
void Mapper_SaveState( LPBYTE p );
void Mapper_LoadState( LPBYTE p );

//BOOL CreateMapper(int no);

#endif
