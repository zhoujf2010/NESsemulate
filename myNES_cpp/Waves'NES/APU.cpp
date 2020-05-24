#include "StdAfx.h"
#include "APU.h"

APU2A03::APU2A03(void)
{
}

APU2A03::~APU2A03(void)
{
}

APU2A03 APU;

BYTE APU2A03::Read(WORD addr)
{
	BYTE data(0);

	if ( addr == 0x4017 )
	{
		data = (1 << 6);
	}

	return data;
}