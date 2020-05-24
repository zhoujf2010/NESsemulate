#pragma once

#ifndef _PPUINC_H_
#define _PPUINC_H_

#define GetBit(val, i)		(((val) & (1 << i)) >> i)

typedef struct tagScreenOffset
{
	tagScreenOffset();

	void Reset();
	void SetValue(BYTE val);
	BOOL AtX();

	BYTE * current;
	BYTE x;
	BYTE y;
} SCREENOFFSET;

typedef struct tagADDRESS
{
	tagADDRESS();

	void Reset();
	void SetAddress(BYTE val);
	WORD GetAddress();
	void Step(int add);
	BOOL AtLow();

	BYTE * current;
	BYTE LowAddr;
	BYTE HeightAddr;
} ADDRESS;

void CloseHandleSafely(HANDLE * handle);

#endif