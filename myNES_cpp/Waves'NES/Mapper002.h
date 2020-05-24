#pragma once

#ifndef _MAPPER002_H_
#define _MAPPER002_H_


void Mapper002_Reset()
{
	SetPROM_32K_Bank(0, 1, PROM_8K_SIZE - 2, PROM_8K_SIZE - 1);
}

void Mapper002_WriteLow(WORD addr, BYTE data)
{
	Mapper_WriteLow(addr, data);
}

void Mapper002_Write(WORD addr, BYTE data)
{
	SetPROM_16K_Bank(4, data);
}

void Mapper002_Init()
{
	NES_Mapper.Reset = Mapper002_Reset;
	NES_Mapper.Write = Mapper002_Write;
	NES_Mapper.WriteLow = Mapper002_WriteLow;
}

#endif
