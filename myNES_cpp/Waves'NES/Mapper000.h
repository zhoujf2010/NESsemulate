#pragma once

#ifndef _MAPPER000_H_
#define _MAPPER000_H_


void	Mapper000_Reset()
{
	switch (RomHeader[4])
	{
	case 1 :	// 16K only
		SetPROM_16K_Bank(4, 0);
		SetPROM_16K_Bank(6, 0);
		break;
	case 2 :	// 32K
		SetPROM_32K_Bank(0);
		break;
	}
}

void	Mapper000_Init()
{
	NES_Mapper.Reset = Mapper000_Reset;
}

#endif
