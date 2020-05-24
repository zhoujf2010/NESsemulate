#include "StdAfx.h"
#include "PPUINC.h"

///////////////////////////////////////////
// SCREENOFFSET

tagScreenOffset::tagScreenOffset()
{
	current = &x;
	x = 0;
	y = 0;
}

void tagScreenOffset::Reset()
{
	current = &x;
}

void tagScreenOffset::SetValue(BYTE val)
{
	if (current == &x && val & 0x7)
		int a = 0;
	*current = val;
	if (current == &x)
		current = &y;
	else
		current = &x;
}

BOOL tagScreenOffset::AtX()
{
	return (current == &x);
}


///////////////////////////////////////////
// SCREENOFFSET

tagADDRESS::tagADDRESS()
{
	current = &HeightAddr;
	HeightAddr = 0;
	LowAddr = 0;
}

void tagADDRESS::Reset()
{
	current = &HeightAddr;
}

void tagADDRESS::SetAddress(BYTE val)
{
	*current = val;
	if (current == &HeightAddr)
		current = &LowAddr;
	else
		current = &HeightAddr;
}

WORD tagADDRESS::GetAddress()
{
	return *(WORD *)&LowAddr;
}

void tagADDRESS::Step(int add)
{
	*(WORD *)&LowAddr = *(WORD *)&LowAddr + add;
}

BOOL tagADDRESS::AtLow()
{
	return (current == &LowAddr);
}

///////////////////////////////////////////

void CloseHandleSafely(HANDLE * handle)
{
	if (handle && *handle)
	{
		::CloseHandle(*handle);
		*handle = NULL;
	}
}
