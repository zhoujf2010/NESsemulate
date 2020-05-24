#pragma once

#ifndef _CPU_H_
#define _CPU_H_

#define	NMI_FLAG	0x01		//不可屏蔽中断
#define	IRQ_FLAG	0x02		//可屏蔽中断

#define	NMI_VECTOR	0xFFFA		//不可屏蔽中断向量
#define	RES_VECTOR	0xFFFC		//Reset向量
#define	IRQ_VECTOR	0xFFFE		//IRQ向量

//6502标志位
#define	C_FLAG		0x01		// 1: Carry
#define	Z_FLAG		0x02		// 1: Zero
#define	I_FLAG		0x04		// 1: Irq disabled
#define	D_FLAG		0x08		// 1: Decimal mode flag (NES unused)
#define	B_FLAG		0x10		// 1: Break
#define	R_FLAG		0x20		// 1: Reserved (Always 1)
#define	V_FLAG		0x40		// 1: Overflow
#define	N_FLAG		0x80		// 1: Negative

class CPU6502
{

public:

	CPU6502(void);

	void Reset();

	BYTE Read(WORD addr);

	WORD ReadW(WORD addr);

	void Write(WORD addr, BYTE val);

	BYTE * GetRAM(WORD addr);

	void NMI();

	void ExecOnBaseCycle(int BaseCycle);

	int Exec(int CpuCycle);
public:

	~CPU6502(void);

public:

///////////////////////////////////////////////////////////
/*
Registers
Bits Name  Expl.
8    A     Accumulator
8    X     Index Register X
8    Y     Index Register Y
16   PC    Program Counter
8    S     Stack Pointer (see below)
8    P     Processor Status Register (see below)
*/
//////////////////////////////////////////////////////////

	BYTE A;
	BYTE X;
	BYTE Y;
	WORD PC;
	BYTE S;
	BYTE P;

	BYTE INT_pending;

public:
	signed __int64 m_BaseCycle;
	signed __int64 m_EmuCycle;
	int m_DMACycle;
};

extern CPU6502 CPU;

#endif
