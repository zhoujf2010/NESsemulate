#include "StdAfx.h"
#include <stdlib.h>
#include <stdio.h>
#include "CPU.h"
#include "Waves'NES.h"
#include "CPUINC.h"

BYTE gCycle[256] =
{
	7, 6, 0, 0, 0, 3, 5, 0, 3, 2, 2, 0, 0, 4, 6, 0,
	2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0,
	6, 2, 0, 0, 3, 3, 5, 0, 4, 2, 2, 0, 4, 4, 6, 0,
	2, 2, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0,
	6, 6, 0, 0, 0, 3, 5, 0, 3, 2, 2, 0, 3, 4, 6, 0,
	2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0,
	6, 6, 0, 0, 0, 3, 5, 0, 4, 2, 2, 0, 5, 4, 6, 0,
	2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0,
	0, 6, 0, 0, 3, 3, 3, 0, 2, 0, 2, 0, 4, 4, 4, 0,
	2, 6, 0, 0, 4, 4, 4, 0, 2, 5, 2, 0, 0, 5, 0, 0,
	2, 6, 2, 0, 3, 3, 3, 0, 2, 2, 2, 0, 4, 4, 4, 0,
	2, 5, 0, 0, 4, 4, 4, 0, 2, 4, 2, 0, 4, 4, 4, 0,
	2, 6, 0, 0, 3, 3, 5, 0, 2, 2, 2, 0, 4, 4, 6, 0,
	2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0,
	2, 6, 0, 0, 3, 3, 5, 0, 2, 2, 2, 0, 4, 4, 6, 0,
	2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0
};

static BYTE ZN_Table[256];

CPU6502 CPU;

CPU6502::CPU6502(void)
{
}

CPU6502::~CPU6502(void)
{
}

void CPU6502::Reset()
{
	A  = 0x00;
	X  = 0x00;
	Y  = 0x00;
	S  = 0xFF;
	PC = ReadW(RES_VECTOR);
	P  = Z_FLAG | R_FLAG;

	INT_pending = 0;
	m_BaseCycle = 0;
	m_EmuCycle  = 0;
	m_DMACycle  = 0;

	ZN_Table[0] = Z_FLAG;
	for(int i = 1; i < 256; i++)
		ZN_Table[i] = (i & 0x80) ? N_FLAG : 0;
}

BYTE CPU6502::Read(WORD addr)
{
	if( addr < 0x2000 )
	{
		return	RAM[addr & 0x07FF];
	}
	else
	{
		return NES_Read(addr);
	}

	return 0;
}

WORD CPU6502::ReadW(WORD addr)
{
	if (addr < 0x2000)
	{
		return *((WORD *)&RAM[addr & 0x07FF]);
	}
	else if (addr < 0x8000)
	{
		return (WORD)NES_Read(addr) + ((WORD)NES_Read(addr + 1) << 8);
	}

	return	*((WORD *)&CPU_MEM_BANK[addr >> 13][addr & 0x1FFF]);
}

void CPU6502::Write(WORD addr, BYTE val)
{	
	if (addr < 0x2000)
	{
		RAM[addr & 0x07FF] = val;
	}
	else
	{
		NES_Write(addr, val);
	}
}

BYTE * CPU6502::GetRAM(WORD addr)
{
	if (addr < 0x2000)
		return &RAM[addr & 0x7FF];
	else
		return NES_GetRAM(addr);
	/*
	else if (addr < 0x6000)
	{
	#ifdef __DEBUG__
	AfxMessageBox("CPU.cpp CPU6520::GetRam(WORD addr) 1", MB_OK);
	#endif
	return 0;
	}
	else if (addr < 0x8000)
	return &SRAM[addr & 0x1FFF];
	else if (addr < 0xC000)
	return &m_PRGBank[0][addr & 0x3FFF];
	else
	return &m_PRGBank[1][addr & 0x3FFF];
	*/
	return 0;
}

void CPU6502::NMI()
{
	INT_pending |= NMI_FLAG;
}

void CPU6502::ExecOnBaseCycle(int BaseCycle)
{
	m_BaseCycle += BaseCycle;
	int Cycle = (int)((m_BaseCycle / 12) - m_EmuCycle);
	if (Cycle > 0)
		m_EmuCycle += Exec(Cycle);
}

void RAMMap()
{
	FILE * file;
	file = fopen("d:\\ram.txt", "w");
	fwrite(RAM, 0x2000, 1, file);
	fclose(file);
}

int CPU6502::Exec(int CpuCycle)
{
	int Cycle = 0;
	static DWORD aa = 0x157ab;
	if (CPU.m_EmuCycle >= aa)
	{
		//RAMMap();
		int a = 0;
	}
	register WORD EA;
	register WORD ET;
	register WORD WT;
	register BYTE DT;

	while (Cycle < CpuCycle)
	{
		if (m_DMACycle > 0)
		{
			if ((CpuCycle - Cycle) <= m_DMACycle)
			{
				m_DMACycle -= (CpuCycle - Cycle);
				Cycle = CpuCycle;
				break;
			}
			else
			{
				Cycle += m_DMACycle;
				m_DMACycle = 0;
			}
		}

		BYTE OpCode = Read(PC++);

		switch (OpCode)
		{
		case 0x00 :		//BRK
			BRK();
			break;
		case 0x01 :		//ORA ($n,X)
			MR_IX(); ORA();
			break;
		case 0x03 :		// SLO ($??,X)
			MR_IX(); SLO(); MW_EA();
			break;
		case 0x05 :		//ORA $n
			MR_ZP(); ORA();
			break;
		case 0x06 :		//ASL $n
			MR_ZP(); ASL(); MW_ZP();
			break;
		case 0x07:		// SLO $??
			MR_ZP(); SLO(); MW_ZP();
			break;
		case 0x08 :		//PHP
			PUSH(P | B_FLAG);
			break;
		case 0x09 :		//ORA #$n
			MR_IM(); ORA();
			break;
		case 0x0A :		//ASL
			ASL_A();
			break;
		case 0x0B :		// ANC #$n
			MR_IM(); ANC();
			break;
		case 0x0D :		//ORA $n2n1
			MR_AB(); ORA();
			break;
		case 0x0E :		//ASL $n2n1
			MR_AB(); ASL(); MW_EA();
			break;
		case 0x0F :		// SLO $????
			MR_AB(); SLO(); MW_EA();
			break;
		case 0x10 :		//BPL $n
			MR_IM(); BPL();
			break;
		case 0x11 :		//ORA ($n),Y
			MR_IY(); ORA(); CHECK_EA();
			break;
		case 0x13 :		// SLO ($??),Y
			MR_IY(); SLO(); MW_EA();
			Cycle += 8;
			break;
		case 0x15 :		//ORA $n,X
			MR_ZX(); ORA();
			break;
		case 0x16 :		//ASL $n,X
			MR_ZX(); ASL(); MW_ZP();
			break;
		case 0x17 :		// SLO $??,X
			MR_ZX(); SLO(); MW_ZP();
			Cycle += 6;
			break;
		case 0x18 :		//CLC
			CLC();
			break;
		case 0x19 :		//ORA $n2n1,Y
			MR_AY(); ORA(); CHECK_EA();
			break;
		case 0x1B :		// SLO $????,Y
			MR_AY(); SLO(); MW_EA();
			Cycle += 7;
			break;
		case 0x1D :		//ORA $n2n1,X
			MR_AX(); ORA(); CHECK_EA();
			break;
		case 0x1E :		//ASL $n2n1,X
			MR_AX(); ASL(); MW_EA();
			break;
		case 0x1F :		// SLO $????,X
			MR_AX(); SLO(); MW_EA();
			Cycle += 7;
			break;
		case 0x20 :		//JSR $n2n1
			JSR();
			break;
		case 0x21 :		//AND ($n,X)
			MR_IX(); AND();
			break;
		case 0x23 :		// RLA ($??,X)
			MR_IX(); RLA(); MW_EA();
			Cycle += 8;
			break;
		case 0x24 :		//BIT $n
			MR_ZP(); BIT();
			break;
		case 0x25 :		//AND $n
			//	MR_ZP();			
			EA = Read( PC++ );
			DT = (RAM[(BYTE)(EA)]);
			AND();
			break;
		case 0x26 :		//ROL $n
			MR_ZP(); ROL(); MW_ZP();
			break;
		case 0x27 :		// RLA $??
			MR_ZP(); RLA(); MW_ZP();
			Cycle += 5;
			break;
		case 0x28 :		//PLP
			P = POP() | R_FLAG;
			break;
		case 0x29 :		//AND #$n
			MR_IM(); AND();
			break;
		case 0x2A :		//ROL
			ROL_A();
			break;
		case 0x2B :		// ANC #$n
			MR_IM(); ANC();
			break;
		case 0x2C :		//BIT $n22n1
			MR_AB(); BIT();
			break;
		case 0x2D :		//AND $n2n1
			MR_AB(); AND();
			break;
		case 0x2E :		//ROL $n2n1
			MR_AB(); ROL(); MW_EA();
			break;
		case 0x2F :		// RLA $????
			MR_AB(); RLA(); MW_EA();
			Cycle += 6;
			break;
		case 0x30 :		//BMI $n
			MR_IM(); BMI();
			break;
		case 0x31 :		//AND ($n),Y
			MR_IY(); AND(); CHECK_EA();
			break;
		case 0x33 :		// RLA ($??),Y
			MR_IY(); RLA(); MW_EA();
			Cycle += 8;
			break;
		case 0x35 :		//AND $n,X
			MR_ZX(); AND();
			break;
		case 0x36 :		//ROL $n,X
			MR_ZX(); ROL(); MW_ZP();
			break;
		case 0x37 :		// RLA $??,X
			MR_ZX(); RLA(); MW_ZP();
			Cycle += 6;
			break;
		case 0x38 :		//SEC
			SEC();
			break;
		case 0x39 :		//AND $n2n1,Y
			MR_AY(); AND(); CHECK_EA();
			break;
		case 0x3B :		// RLA $????,Y
			MR_AY(); RLA(); MW_EA();
			Cycle += 7;
			break;
		case 0x3D :		//AND $n2n1,X
			MR_AX(); AND(); CHECK_EA();
			break;
		case 0x3E :		//ROL $n2n1,X
			MR_AX(); ROL(); MW_EA();
			break;
		case 0x3F :		// RLA $????,X
			MR_AX(); RLA(); MW_EA();
			Cycle += 7;
			break;
		case 0x40 :		//RTI
			RTI();
			break;
		case 0x41 :		//EOR ($n,X)
			MR_IX(); EOR();
			break;
		case 0x43 :		// SRE ($??,X)
			MR_IX(); SRE(); MW_EA();
			Cycle += 8;
			break;
		case 0x45 :		//EOR $n
			MR_ZP(); EOR();
			break;
		case 0x46 :		//LSR $n
			MR_ZP(); LSR(); MW_ZP();
			break;
		case 0x47 :		// SRE $??
			MR_ZP(); SRE(); MW_ZP();
			Cycle += 5;
			break;
		case 0x48 :		//PHA
			PUSH(A);
			break;
		case 0x49 :		//EOR #$n
			MR_IM(); EOR();
			break;
		case 0x4A :		//LSR
			LSR_A();
			break;
		case 0x4B :		//ASR #$??
			MR_IM(); ASR();
			break;
		case 0x4C :		//JMP $n2n1
			JMP();
			break;
		case 0x4D :		//EOR $n2n1
			MR_AB(); EOR();
			break;
		case 0x4E :		//LSR $n2n1
			MR_AB(); LSR(); MW_EA();
			break;
		case 0x4F :		// SRE $????
			MR_AB(); SRE(); MW_EA();
			Cycle += 6;
			break;
		case 0x50 :		//BVC $n
			MR_IM(); BVC();
			break;
		case 0x51 :		//EOR ($n),Y
			MR_IY(); EOR(); CHECK_EA();
			break;
		case 0x53 :		// SRE ($??),Y
			MR_IY(); SRE(); MW_EA();
			Cycle += 8;
			break;
		case 0x55 :		//EOR $n,X
			MR_ZX(); EOR();
			break;
		case 0x56 :		//LSR $n,X
			MR_ZX(); LSR(); MW_ZP();
			break;
		case 0x57 :		// SRE $??,X
			MR_ZX(); SRE(); MW_ZP();
			Cycle += 6;
			break;
		case 0x58 :		//CLI
			CLI();
			break;
		case 0x59 :		//EOR $n2n1,Y
			MR_AY(); EOR(); CHECK_EA();
			break;
		case 0x5B :		// SRE $????,Y
			MR_AY(); SRE(); MW_EA();
			Cycle += 7;
			break;
		case 0x5D :		//EOR $n2n1,X
			MR_AX(); EOR(); CHECK_EA();
			break;
		case 0x5E :		//LSR $n2n1,X
			MR_AX(); LSR(); MW_EA();
			break;
		case 0x5F :		// SRE $????,X
			MR_AX(); SRE(); MW_EA();
			Cycle += 7;
			break;
		case 0x60 :		//RTS
			RTS();
			break;
		case 0x61 :		//ADC ($n,X)
			MR_IX(); ADC();
			break;
		case 0x63 :		// RRA ($??,X)
			MR_IX(); RRA(); MW_EA();
			Cycle += 8;
			break;
		case 0x65 :		//ADC $n
			MR_ZP(); ADC();
			break;
		case 0x66 :		//ROR $n
			MR_ZP(); ROR(); MW_ZP();
			break;
		case 0x67 :		// RRA $??
			MR_ZP(); RRA(); MW_ZP();
			Cycle += 5;
			break;
		case 0x68 :		//PLA
			A = POP(); SET_ZN_FLAG(A);
			break;
		case 0x69 :		//ADC #$n
			MR_IM(); ADC();
			break;
		case 0x6A :		//ROR
			ROR_A();
			break;
		case 0x6B :		//ARR #$??
			MR_IM(); ARR();
			break;
		case 0x6C :		//JMP ($n2n1)
			JMP_ID();
			break;
		case 0x6D :		//ADC $n2n1
			MR_AB(); ADC();
			break;
		case 0x6E :		//ROR $n2n1
			MR_AB(); ROR(); MW_EA();
			break;
		case 0x6F :		// RRA $????
			MR_AB(); RRA(); MW_EA();
			Cycle += 6;
			break;
		case 0x70 :		//BVS $n
			MR_IM(); BVS();
			break;
		case 0x71 :		//ADC ($n),Y
			MR_IY(); ADC(); CHECK_EA();
			break;
		case 0x73 :		// RRA ($??),Y
			MR_IY(); RRA(); MW_EA();
			Cycle += 8;
			break;
		case 0x75 :		//ADC $n,X
			MR_ZX(); ADC();
			break;
		case 0x76 :		//ROR $n,X
			MR_ZX(); ROR(); MW_ZP();
			break;
		case 0x77 :		// RRA $??,X
			MR_ZX(); RRA(); MW_ZP();
			Cycle += 6;
			break;
		case 0x78 :		//SEI
			SEI();
			break;
		case 0x79 :		//ADC $n2n1,Y
			MR_AY(); ADC(); CHECK_EA();
			break;
		case 0x7B :		// RRA $????,Y
			MR_AY(); RRA(); MW_EA();
			Cycle += 7;
			break;
		case 0x7D :		//ADC $n2n1,X
			MR_AX(); ADC(); CHECK_EA();
			break;
		case 0x7E :		//ROR $n2n1,X
			MR_AX(); ROR(); MW_EA();
			break;
		case 0x7F :		// RRA $????,X
			MR_AX(); RRA(); MW_EA();
			Cycle += 7;
			break;
		case 0x81 :		//STA ($n,X)
			EA_IX(); STA(); MW_EA();
			break;
		case 0x83 :		// SAX ($??,X)
			MR_IX(); SAX(); MW_EA();
			Cycle += 6;
			break;
		case 0x84 :		//STX $n
			EA_ZP(); STY(); MW_ZP();
			break;
		case 0x85 :		//STA $n
			EA_ZP(); STA(); MW_ZP();
			break;
		case 0x86 :		//STX $n
			EA_ZP(); STX(); MW_ZP();
			break;
		case 0x87 :		// SAX $??
			MR_ZP(); SAX(); MW_ZP();
			Cycle += 3;
			break;
		case 0x88 :		//DEY
			DEY();
			break;
		case 0x8A :		//TXA
			TXA();
			break;
		case 0x8B :		//ANE #$??
			MR_IM(); ANE();
			break;
		case 0x8C :		//STY $n2n1
			EA_AB(); STY(); MW_EA();
			break;
		case 0x8D :		//STA $n2n1
			EA_AB(); STA(); MW_EA();
			break;
		case 0x8E :		//STX $n2n1
			EA_AB(); STX(); MW_EA();
			break;
		case 0x8F :		// SAX $????
			MR_AB(); SAX(); MW_EA();
			Cycle += 4;
			break;
		case 0x90 :		//BCC $n
			MR_IM(); BCC();
			break;
		case 0x91 :		//STA ($n),Y
			EA_IY(); STA(); MW_EA();
			break;
		case 0x93 :		// SHA ($??),Y
			MR_IY(); SHA(); MW_EA();
			Cycle += 6;
			break;
		case 0x94 :		//STY $n,X
			EA_ZX(); STY(); MW_ZP();
			break;
		case 0x95 :		//STA $n,X
			EA_ZX(); STA(); MW_ZP();
			break;
		case 0x96 :		//STX $n,Y
			EA_ZY(); STX(); MW_ZP();
			break;
		case 0x97 :		// SAX $??,Y
			MR_ZY(); SAX(); MW_ZP();
			Cycle += 4;
			break;
		case 0x98 :		//TYA
			TYA();
			break;
		case 0x99 :		//STA $n2n1,Y
			EA_AY(); STA(); MW_EA();
			break;
		case 0x9A :		//TXS
			TXS();
			break;
		case 0x9B :		// SHS $????,Y
			MR_AY(); SHS(); MW_EA();
			Cycle += 5;
			break;
		case 0x9C :		// SHY $????,X
			MR_AX(); SHY(); MW_EA();
			Cycle += 5;
			break;
		case 0x9D :		//STA $n2n1, X
			EA_AX(); STA(); MW_EA();
			break;
		case 0x9E :		// SHX $????,Y
			MR_AY(); SHX(); MW_EA();
			Cycle += 5;
			break;
		case 0x9F :		// SHA $????,Y
			MR_AY(); SHA(); MW_EA();
			Cycle += 5;
			break;
		case 0xA0 :		//LDY #$n
			MR_IM(); LDY();
			break;
		case 0xA1 :		//LDA ($n,X)
			MR_IX(); LDA();
			break;
		case 0xA2 :		//LDX #$n
			MR_IM(); LDX();
			break;
		case 0xA3 :		// LAX ($??,X)
			MR_IX(); LAX();
			Cycle += 6;
			break;
		case 0xA4 :		//LDY $n
			MR_ZP(); LDY();
			break;
		case 0xA5 :		//LDA $n
			MR_ZP();
			//	EA = Read( PC++ );
			//	DT = ZPRD( EA );
			LDA();
			break;
		case 0xA6 :		//LDX $n
			MR_ZP(); LDX();
			break;
		case 0xA7 :		// LAX $??
			MR_ZP(); LAX();
			Cycle += 3;
			break;
		case 0xA8 :		//TAY
			TAY();
			break;
		case 0xA9 :		//LDA #$n
			MR_IM(); LDA();
			break;
		case 0xAA :		//TAX
			TAX();
			break;
		case 0xAB :		// LXA #$??
			MR_IM(); LXA();
			Cycle += 2;
			break;
		case 0xAC :		//LDY $n2n1
			MR_AB(); LDY();
			break;
		case 0xAD :		//LDA $n2n1
			MR_AB(); LDA();
			break;
		case 0xAE :		//LDX $n2n1
			MR_AB(); LDX();
			break;
		case 0xAF :		// LAX $????
			MR_AB(); LAX();
			Cycle += 4;
			break;
		case 0xB0 :		//BCS $n
			MR_IM(); BCS();
			break;
		case 0xB1 :		//LDA ($n),Y
			MR_IY(); LDA(); CHECK_EA();
			break;
		case 0xB3 :		// LAX ($??),Y
			MR_IY(); LAX(); CHECK_EA();
			Cycle += 5;
			break;
		case 0xB4 :		//LDY $n,X
			MR_ZX(); LDY();
			break;
		case 0xB5 :		//LDA $n,X
			MR_ZX(); LDA();
			break;
		case 0xB6 :		//LDX $n,Y
			MR_ZY(); LDX();
			break;
		case 0xB7 :		// LAX $??,Y
			MR_ZY(); LAX();
			Cycle += 4;
			break;
		case 0xB8 :		//CLV
			CLV();
			break;
		case 0xB9 :		//LDA $n2n1,Y
			MR_AY(); LDA(); CHECK_EA();
			break;
		case 0xBA :		//TSX
			TSX();
			break;
		case 0xBB :		// LAS $????,Y
			MR_AY(); LAS(); CHECK_EA();
			Cycle += 4;
			break;
		case 0xBC :		//LDY $n2n1,X
			MR_AX(); LDY(); CHECK_EA();
			break;
		case 0xBD :		//LDA $n2n1,X
			MR_AX(); LDA(); CHECK_EA();
			break;
		case 0xBE :		//LDX $n2n1,Y
			MR_AY(); LDX(); CHECK_EA();
			break;
		case 0xBF :		// LAX $????,Y
			MR_AY(); LAX(); CHECK_EA();
			Cycle += 4;
			break;
		case 0xC0 :		//CPY #$n
			MR_IM(); CPY();
			break;
		case 0xC1 :		//CMP ($n,X)
			MR_IX(); CMP_();
			break;
		case 0xC3 :		// DCP ($??,X)
			MR_IX(); DCP(); MW_EA();
			break;
		case 0xC4 :		//CPY $n
			MR_ZP(); CPY();
			break;
		case 0xC5 :		//CMP $n
			MR_ZP(); CMP_();
			break;
		case 0xC6 :		//DEC $n
			MR_ZP(); DEC();	MW_ZP();
			break;
		case 0xC7 :		// DCP $??
			MR_ZP(); DCP(); MW_ZP();
			break;
		case 0xC8 :		//INY
			INY();
			break;
		case 0xC9 :		//CMP #$n
			MR_IM(); CMP_();
			break;
		case 0xCA :		//DEX
			DEX();
			break;
		case 0xCB :		// SBX #$??
			MR_IM(); SBX();
			Cycle += 2;
			break;
		case 0xCC :		//CPY $n2n1
			MR_AB(); CPY();
			break;
		case 0xCD :		//CMP $21n1
			MR_AB(); CMP_();
			break;
		case 0xCE :		//DEC $n2n1
			MR_AB(); DEC(); MW_EA();
			break;
		case 0xCF :		// DCP $????
			MR_AB(); DCP(); MW_EA();
			break;
		case 0xD0 :		//BNE $n
			MR_IM(); BNE();
			break;
		case 0xD1 :		//CMP ($n),Y
			MR_IY(); CMP_(); CHECK_EA();
			break;
		case 0xD3 :		// DCP ($??),Y
			MR_IY(); DCP(); MW_EA();
			break;
		case 0xD5 :		//CMP $n,X
			MR_ZX(); CMP_();
			break;
		case 0xD6 :		//DEC $n,X
			MR_ZX(); DEC(); MW_ZP();
			break;
		case 0xD7 :		// DCP $??,X
			MR_ZX(); DCP(); MW_ZP();
			break;
		case 0xD8 :		//CLD
			CLD();
			break;
		case 0xD9 :		//CMP $n2n1,Y
			MR_AY(); CMP_(); CHECK_EA();
			break;
		case 0xDB :		// DCP $????,Y
			MR_AY(); DCP(); MW_EA();
			break;
		case 0xDD :		//CMP $n2n1,X
			MR_AX(); CMP_(); CHECK_EA();
			break;
		case 0xDE :		//DEC $n2n1,X
			MR_AX(); DEC(); MW_EA();
			break;
		case 0xDF :		// DCP $????,X
			MR_AX(); DCP(); MW_EA();
			break;
		case 0xE0 :		//CPX #$n
			MR_IM(); CPX();
			break;
		case 0xE1 :		//SBC ($n,X)
			MR_IX(); SBC();
			break;
		case 0xE4 :		//CPX $n
			MR_ZP(); CPX();
			break;
		case 0xE3 :		// ISB ($??,X)
			MR_IX(); ISB(); MW_EA();
			Cycle += 5;
			break;
		case 0xE5 :		//SBC $n
			MR_ZP(); SBC();
			break;
		case 0xE6 :		//INC $n
			MR_ZP(); INC(); MW_ZP();
			break;
		case 0xE7 :		// ISB $??
			MR_ZP(); ISB(); MW_ZP();
			break;
		case 0xE8 :		//INX
			INX();
			break;
		case 0xE9 :		//SBC #$n
			MR_IM(); SBC();
			break;
		case 0xEA :		//NOP
			break;
		case 0xEB :		// SBC #$?? (Unofficial)
			MR_IM(); SBC();
			Cycle += 2;
			break;
		case 0xEC :		//CPX $n2n1
			MR_AB(); CPX();
			break;
		case 0xED :		//SBC $n2n1
			MR_AB(); SBC();
			break;
		case 0xEE :		//INC $n2n1
			MR_AB(); INC(); MW_EA();
			break;
		case 0xEF :		// ISB $????
			MR_AB(); ISB(); MW_EA();
			Cycle += 5;
		case 0xF0 :		//BEQ $n
			MR_IM(); BEQ();
			break;
		case 0xF1 :		//SBC ($n),Y
			MR_IY(); SBC(); CHECK_EA();
			break;
		case 0xF3 :		// ISB ($??),Y
			MR_IY(); ISB(); MW_EA();
			Cycle += 5;
			break;
		case 0xF5 :		//SBC $n,X
			MR_ZX(); SBC();
			break;
		case 0xF6 :		//INC $n,X
			MR_ZX(); INC(); MW_ZP();
			break;
		case 0xF7 :		// ISB $??,X
			MR_ZX(); ISB(); MW_ZP();
			Cycle += 5;
			break;
		case 0xF8 :		//SED
			SED();
			break;
		case 0xF9 :		//SBC $n2n1,Y
			MR_AY(); SBC(); CHECK_EA();
			break;
		case 0xFB :		// ISB $????,Y
			MR_AY(); ISB(); MW_EA();
			Cycle += 5;
			break;
		case 0xFD :		//SBC $n2n1,X
			MR_AX(); SBC(); CHECK_EA();
			break;
		case 0xFE :		//INC $n2n1,X
			MR_AX(); INC(); MW_EA();
			break;
		case 0xFF :		// ISB $????,X
			MR_AX(); ISB(); MW_EA();
			Cycle += 5;
			break;

		case 0x1A :		// NOP (Unofficial)
		case 0x3A :		// NOP (Unofficial)
		case 0x5A :		// NOP (Unofficial)
		case 0x7A :		// NOP (Unofficial)
		case 0xDA :		// NOP (Unofficial)
		case 0xFA :		// NOP (Unofficial)
			Cycle += 2;
			break;
		case 0x80 :		// DOP (CYCLES 2)
		case 0x82 :		// DOP (CYCLES 2)
		case 0x89 :		// DOP (CYCLES 2)
		case 0xC2 :		// DOP (CYCLES 2)
		case 0xE2 :		// DOP (CYCLES 2)
			Cycle += 2;
			PC++;
			break;
		case 0x04 :		// DOP (CYCLES 3)
		case 0x44 :		// DOP (CYCLES 3)
		case 0x64 :		// DOP (CYCLES 3)
			Cycle += 3;
			PC++;
			break;
		case 0x14 :		// DOP (CYCLES 4)
		case 0x34 :		// DOP (CYCLES 4)
		case 0x54 :		// DOP (CYCLES 4)
		case 0x74 :		// DOP (CYCLES 4)
		case 0xD4 :		// DOP (CYCLES 4)
		case 0xF4 :		// DOP (CYCLES 4)
			Cycle += 4;
			PC++;
			break;
		case 0x0C :		// TOP
		case 0x1C :		// TOP
		case 0x3C :		// TOP
		case 0x5C :		// TOP
		case 0x7C :		// TOP
		case 0xDC :		// TOP
		case 0xFC :		// TOP
			Cycle += 2;
			PC+=2;
			break;
		default :
			{
				::MessageBox(NULL ,_T("CPU.cpp CPU6502::Exec(int Cycle)"), _T("ERROR"), MB_OK);
				break;
			}
		}
		Cycle += gCycle[OpCode];

		if(INT_pending & NMI_FLAG)
		{
			INT_pending &= ~NMI_FLAG;
			PUSH(PC>>8);
			PUSH(PC&0xFF);
			CLR_FLAG(B_FLAG);
			PUSH(P);
			SET_FLAG( I_FLAG );
			PC = ReadW(NMI_VECTOR);
			Cycle += 7;
		}
	}

	return Cycle;
}
