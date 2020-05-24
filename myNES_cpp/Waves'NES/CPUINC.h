#pragma once

#ifndef _CPUINC_H_
#define _CPUINC_H_


// STACK操作(入栈操作和出栈操作)
#define	PUSH(V)		{ RAM[(S--) + 0x100]=(V); }
#define	POP()		RAM[(++S) + 0x100]

#define	SET_ZN_FLAG(A)	{ P &= ~(Z_FLAG|N_FLAG); P |= ZN_Table[(BYTE)(A)]; }

#define	SET_FLAG(V)	{ P |=  (V); }	//设置标志位
#define	CLR_FLAG(V)	{ P &= ~(V); }	//清除标志位

#define	TST_FLAG(F,V)	{ P &= ~(V); if((F)) P |= (V); }
#define	CHK_FLAG(V)		(P&(V))

#define	MW_ZP()		ZPWR(EA,DT)
#define	MW_EA()		Write(EA,DT)

//零页寻址
#define	ZPRD(A)		(RAM[(BYTE)(A)])
#define	ZPRDW(A)	((WORD)RAM[(BYTE)(A)]+((WORD)RAM[(BYTE)((A)+1)]<<8))
#define	ZPWR(A,V)	{ RAM[(BYTE)(A)]=(V); }
#define	ZPWRW(A,V)	{ *((LPWORD)&RAM[(BYTE)(A)])=(WORD)(V); }

// WT .... WORD TEMP
// EA .... EFFECTIVE ADDRESS
// ET .... EFFECTIVE ADDRESS TEMP
// DT .... DATA

//立即数寻址
#define	MR_IM()	{		\
	DT = Read( PC++ );	\
}
//零页寻址
#define	MR_ZP()	{		\
	EA = Read( PC++ );	\
	DT = ZPRD( EA );	\
}
//零页X变址寻址
#define	MR_ZX()	{		\
	DT = Read( PC++ );	\
	EA = (BYTE)(DT + X);	\
	DT = ZPRD( EA );	\
}
//零页Y变址寻址
#define	MR_ZY()	{		\
	DT = Read( PC++ );	\
	EA = (BYTE)(DT + Y);	\
	DT = ZPRD( EA );	\
}
//绝对寻址
#define	MR_AB()	{		\
	EA = ReadW( PC );	\
	PC += 2;		\
	DT = Read( EA );	\
}
//绝对X变址寻址
#define	MR_AX()	{		\
	ET = ReadW( PC );	\
	PC += 2;		\
	EA = ET + (WORD)X;	\
	DT = Read( EA );	\
}
//绝对Y变址寻址
#define	MR_AY()	{		\
	ET = ReadW( PC );	\
	PC += 2;		\
	EA = ET + (WORD)Y;	\
	DT = Read( EA );	\
}
//先零页X变址后间址寻址
#define	MR_IX()	{		\
	DT = Read( PC++ );	\
	EA = ZPRDW( DT + X );	\
	DT = Read( EA );	\
}
//先零页间址后Y变址寻址
#define	MR_IY()	{		\
	DT = Read( PC++ );	\
	ET = ZPRDW( DT );	\
	EA = ET + (WORD)Y;	\
	DT = Read( EA );	\
}

// EFFECTIVE ADDRESS
#define	EA_ZP()	{		\
	EA = Read( PC++ );	\
}
#define	EA_ZX()	{		\
	DT = Read( PC++ );	\
	EA = (BYTE)(DT + X);	\
}
#define	EA_ZY()	{		\
	DT = Read( PC++ );	\
	EA = (BYTE)(DT + Y);	\
}
#define	EA_AB()	{		\
	EA = ReadW( PC );	\
	PC += 2;		\
}
#define	EA_AX()	{		\
	ET = ReadW( PC );	\
	PC += 2;		\
	EA = ET + X;		\
}
#define	EA_AY()	{		\
	ET = ReadW( PC );	\
	PC += 2;		\
	EA = ET + Y;		\
}
#define	EA_IX()	{		\
	DT = Read( PC++ );	\
	EA = ZPRDW( DT + X );	\
}
#define	EA_IY()	{		\
	DT = Read( PC++ );	\
	ET = ZPRDW( DT );	\
	EA = ET + (WORD)Y;	\
}

/* ADC (NV----ZC) */
#define	ADC() {							\
	WT = A+DT+(P&C_FLAG);				\
	TST_FLAG( WT > 0xFF, C_FLAG );				\
	TST_FLAG( ((~(A^DT))&(A^WT)&0x80), V_FLAG );	\
	A = (BYTE)WT;						\
	SET_ZN_FLAG(A);					\
}

/* SBC (NV----ZC) */
#define	SBC() {						\
	WT = A-DT-(~P&C_FLAG);			\
	TST_FLAG( ((A^DT) & (A^WT)&0x80), V_FLAG );	\
	TST_FLAG( WT < 0x100, C_FLAG );			\
	A = (BYTE)WT;					\
	SET_ZN_FLAG(A);				\
}

/* INC (N-----Z-) */
#define	INC() {			\
	DT++;			\
	SET_ZN_FLAG(DT);	\
}
/* INX (N-----Z-) */
#define	INX() {			\
	X++;			\
	SET_ZN_FLAG(X);	\
}
/* INY (N-----Z-) */
#define	INY() {			\
	Y++;			\
	SET_ZN_FLAG(Y);	\
}

/* DEC (N-----Z-) */
#define	DEC() {			\
	DT--;			\
	SET_ZN_FLAG(DT);	\
}
/* DEX (N-----Z-) */
#define	DEX() {			\
	X--;			\
	SET_ZN_FLAG(X);	\
}
/* DEY (N-----Z-) */
#define	DEY() {			\
	Y--;			\
	SET_ZN_FLAG(Y);	\
}

/* AND (N-----Z-) */
#define	AND() {			\
	A &= DT;		\
	SET_ZN_FLAG(A);	\
}

/* ORA (N-----Z-) */
#define	ORA() {			\
	A |= DT;		\
	SET_ZN_FLAG(A);	\
}

/* EOR (N-----Z-) */
#define	EOR() {			\
	A ^= DT;		\
	SET_ZN_FLAG(A);	\
}

/* ASL_A (N-----ZC) */
#define	ASL_A() {			\
	TST_FLAG( A&0x80, C_FLAG );	\
	A <<= 1;			\
	SET_ZN_FLAG(A);		\
}

/* ASL (N-----ZC) */
#define	ASL() {				\
	TST_FLAG( DT&0x80, C_FLAG );	\
	DT <<= 1;			\
	SET_ZN_FLAG(DT);		\
}

/* LSR_A (N-----ZC) */
#define	LSR_A() {			\
	TST_FLAG( A&0x01, C_FLAG );	\
	A >>= 1;			\
	SET_ZN_FLAG(A);		\
}
/* LSR (N-----ZC) */
#define	LSR() {				\
	TST_FLAG( DT&0x01, C_FLAG );	\
	DT >>= 1;			\
	SET_ZN_FLAG(DT);		\
}

/* ROL_A (N-----ZC) */
#define	ROL_A() {				\
	if( P & C_FLAG ) {			\
	TST_FLAG(A&0x80,C_FLAG);	\
	A = (A<<1)|0x01;		\
	} else {				\
	TST_FLAG(A&0x80,C_FLAG);	\
	A <<= 1;			\
	}					\
	SET_ZN_FLAG(A);			\
}
/* ROL (N-----ZC) */
#define	ROL() {					\
	if( P & C_FLAG ) {			\
	TST_FLAG(DT&0x80,C_FLAG);	\
	DT = (DT<<1)|0x01;		\
	} else {				\
	TST_FLAG(DT&0x80,C_FLAG);	\
	DT <<= 1;			\
	}					\
	SET_ZN_FLAG(DT);			\
}

/* ROR_A (N-----ZC) */
#define	ROR_A() {				\
	if( P & C_FLAG ) {			\
	TST_FLAG(A&0x01,C_FLAG);	\
	A = (A>>1)|0x80;		\
	} else {				\
	TST_FLAG(A&0x01,C_FLAG);	\
	A >>= 1;			\
	}					\
	SET_ZN_FLAG(A);			\
}
/* ROR (N-----ZC) */
#define	ROR() {					\
	if( P & C_FLAG ) {			\
	TST_FLAG(DT&0x01,C_FLAG);	\
	DT = (DT>>1)|0x80;		\
	} else {				\
	TST_FLAG(DT&0x01,C_FLAG);	\
	DT >>= 1;			\
	}					\
	SET_ZN_FLAG(DT);			\
}

/* BIT (NV----Z-) */
#define	BIT() {					\
	TST_FLAG( (DT&A)==0, Z_FLAG );	\
	TST_FLAG( DT&0x80, N_FLAG );		\
	TST_FLAG( DT&0x40, V_FLAG );		\
}

/* LDA (N-----Z-) */
#define	LDA()	{ A = DT; SET_ZN_FLAG(A); }
/* LDX (N-----Z-) */
#define	LDX()	{ X = DT; SET_ZN_FLAG(X); }
/* LDY (N-----Z-) */
#define	LDY()	{ Y = DT; SET_ZN_FLAG(Y); }

/* STA (--------) */
#define	STA()	{ DT = A; }
/* STX (--------) */
#define	STX()	{ DT = X; }
/* STY (--------) */
#define	STY()	{ DT = Y; }

/* TAX (N-----Z-) */
#define	TAX()	{ X = A; SET_ZN_FLAG(X); }
/* TXA (N-----Z-) */
#define	TXA()	{ A = X; SET_ZN_FLAG(A); }
/* TAY (N-----Z-) */
#define	TAY()	{ Y = A; SET_ZN_FLAG(Y); }
/* TYA (N-----Z-) */
#define	TYA()	{ A = Y; SET_ZN_FLAG(A); }
/* TSX (N-----Z-) */
#define	TSX()	{ X = S; SET_ZN_FLAG(X); }
/* TXS (--------) */
#define	TXS()	{ S = X; }

/* CMP (N-----ZC) */
#define	CMP_() {				\
	WT = (WORD)A - (WORD)DT;		\
	TST_FLAG( (WT&0x8000)==0, C_FLAG );	\
	SET_ZN_FLAG( (BYTE)WT );		\
}
/* CPX (N-----ZC) */
#define	CPX() {					\
	WT = (WORD)X - (WORD)DT;		\
	TST_FLAG( (WT&0x8000)==0, C_FLAG );	\
	SET_ZN_FLAG( (BYTE)WT );		\
}
/* CPY (N-----ZC) */
#define	CPY() {					\
	WT = (WORD)Y - (WORD)DT;		\
	TST_FLAG( (WT&0x8000)==0, C_FLAG );	\
	SET_ZN_FLAG( (BYTE)WT );		\
}

#define	JMP() {			\
	PC = ReadW( PC );	\
}
#define	JSR() {			\
	EA = ReadW( PC );	\
	PC++;			\
	PUSH( PC>>8 );	\
	PUSH( PC&0xFF );	\
	PC = EA;		\
}
#define	RTS() {			\
	PC  = POP();		\
	PC |= POP()*0x0100;	\
	PC++;			\
}
#define	RTI() {			\
	P   = POP() | R_FLAG;	\
	PC  = POP();		\
	PC |= POP()*0x0100;	\
}
#define	_NMI() {			\
	PUSH( PC>>8 );		\
	PUSH( PC&0xFF );		\
	CLR_FLAG( B_FLAG );		\
	PUSH( P );			\
	SET_FLAG( I_FLAG );		\
	PC = Cpu_RD6502W(NMI_VECTOR);	\
	exec_cycles += 7;		\
}
#define	_IRQ() {			\
	PUSH( PC>>8 );		\
	PUSH( PC&0xFF );		\
	CLR_FLAG( B_FLAG );		\
	PUSH( P );			\
	SET_FLAG( I_FLAG );		\
	PC = Cpu_RD6502W(IRQ_VECTOR);	\
	exec_cycles += 7;		\
}
#define	BRK() {				\
	PC++;				\
	PUSH( PC>>8 );		\
	PUSH( PC&0xFF );		\
	SET_FLAG( B_FLAG );		\
	PUSH( P );			\
	SET_FLAG( I_FLAG );		\
	PC = Read(IRQ_VECTOR);	\
}

#define	CHECK_EA()	{ if( (ET&0xFF00) != (EA&0xFF00) ) Cycle++; }

#define	REL_JUMP() {		\
	ET = PC;		\
	EA = PC + (char)DT;	\
	PC = EA;		\
	Cycle++;		\
	CHECK_EA();		\
}

#define	JMP_ID() {				\
	WT = ReadW(PC);			\
	EA = Read(WT);			\
	WT = (WT&0xFF00)|((WT+1)&0x00FF);	\
	PC = EA+Read(WT)*0x100;		\
}

#define	BCC()	{ if( !(P & C_FLAG) ) REL_JUMP(); }
#define	BCS()	{ if(  (P & C_FLAG) ) REL_JUMP(); }
#define	BNE()	{ if( !(P & Z_FLAG) ) REL_JUMP(); }
#define	BEQ()	{ if(  (P & Z_FLAG) ) REL_JUMP(); }
#define	BPL()	{ if( !(P & N_FLAG) ) REL_JUMP(); }
#define	BMI()	{ if(  (P & N_FLAG) ) REL_JUMP(); }
#define	BVC()	{ if( !(P & V_FLAG) ) REL_JUMP(); }
#define	BVS()	{ if(  (P & V_FLAG) ) REL_JUMP(); }

#define	CLC()	{ P &= ~C_FLAG; }
#define	CLD()	{ P &= ~D_FLAG; }
#define	CLI()	{ P &= ~I_FLAG; }
#define	CLV()	{ P &= ~V_FLAG; }
#define	SEC()	{ P |= C_FLAG; }
#define	SED()	{ P |= D_FLAG; }
#define	SEI()	{ P |= I_FLAG; }

#define	ANC()	{			\
	A &= DT;			\
	SET_ZN_FLAG( A );		\
	TST_FLAG( P&N_FLAG, C_FLAG );	\
}

#define	ANE()	{			\
	A = (A|0xEE)&X&DT;	\
	SET_ZN_FLAG( A );		\
}

#define	ARR()	{				\
	DT &= A;				\
	A = (DT>>1)|((P&C_FLAG)<<7);	\
	SET_ZN_FLAG( A );			\
	TST_FLAG( A&0x40, C_FLAG );		\
	TST_FLAG( (A>>6)^(A>>5), V_FLAG );	\
}

#define	ASR()	{			\
	DT &= A;			\
	TST_FLAG( DT&0x01, C_FLAG );	\
	A = DT>>1;			\
	SET_ZN_FLAG( A );		\
}

#define	DCP()	{			\
	DT--;				\
	CMP_();				\
}

#define	ISB()	{			\
	DT++;				\
	SBC();				\
}

#define	LAS()	{			\
	A = X = S = (S & DT);	\
	SET_ZN_FLAG( A );		\
}

#define	LAX()	{			\
	A = DT;			\
	X = A;			\
	SET_ZN_FLAG( A );		\
}

#define	LXA()	{			\
	A = X = ((A|0xEE)&DT);	\
	SET_ZN_FLAG( A );		\
}

#define	RLA()	{				\
	if( P & C_FLAG ) {			\
	TST_FLAG( DT&0x80, C_FLAG );	\
	DT = (DT<<1)|1;			\
	} else {				\
	TST_FLAG( DT&0x80, C_FLAG );	\
	DT <<= 1;			\
	}					\
	A &= DT;				\
	SET_ZN_FLAG( A );			\
}

#define	RRA()	{				\
	if( P & C_FLAG ) {			\
	TST_FLAG( DT&0x01, C_FLAG );	\
	DT = (DT>>1)|0x80;		\
	} else {				\
	TST_FLAG( DT&0x01, C_FLAG );	\
	DT >>= 1;			\
	}					\
	ADC();					\
}

#define	SAX()	{			\
	DT = A & X;			\
}

#define	SBX()	{			\
	WT = (A&X)-DT;		\
	TST_FLAG( WT < 0x100, C_FLAG );	\
	X = WT&0xFF;			\
	SET_ZN_FLAG( X );		\
}

#define	SHA()	{				\
	DT = A & X & (BYTE)((EA>>8)+1);	\
}

#define	SHS()	{			\
	S = A & X;		\
	DT = S & (BYTE)((EA>>8)+1);	\
}

#define	SHX()	{			\
	DT = X & (BYTE)((EA>>8)+1);	\
}

#define	SHY()	{			\
	DT = Y & (BYTE)((EA>>8)+1);	\
}

#define	SLO()	{			\
	TST_FLAG( DT&0x80, C_FLAG );	\
	DT <<= 1;			\
	A |= DT;			\
	SET_ZN_FLAG( A );		\
}

#define	SRE()	{			\
	TST_FLAG( DT&0x01, C_FLAG );	\
	DT >>= 1;			\
	A ^= DT;			\
	SET_ZN_FLAG( A );		\
}

#endif
