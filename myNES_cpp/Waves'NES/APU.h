#pragma once

#ifndef _APU_H_
#define _APU_H_


class APU2A03
{
public:
	APU2A03(void);

	BYTE Read(WORD addr);

public:
	~APU2A03(void);
};

extern APU2A03 APU;

#endif
