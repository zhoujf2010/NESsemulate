// Waves'NES.cpp : 定义应用程序的入口点。
//

#include "stdafx.h"
//#include <Afxdlgs.h>
#include <stdlib.h>
#include <stdio.h>
#include <COmmdlg.h>
#include "Waves'NES.h"
#include "CPU.h"
#include "PPU.h"
#include "PPUINC.h"
#include "APU.h"
#include "Mapper.h"

#include <process.h>
#include <windef.h>
#include <mmsystem.h>
#pragma comment(lib, "Winmm.lib")


#define MAX_LOADSTRING 100

WCHAR strFileName[256];
const char* filter = "NES Files (*.nes)\0*.nes;\0\0\0";
int PROM_8K_SIZE;
int PROM_16K_SIZE;
int PROM_32K_SIZE;
LPBYTE CPU_MEM_BANK[8];		//每个元素指向8K数据

int VROM_1K_SIZE;
int VROM_2K_SIZE;
int VROM_4K_SIZE;
int VROM_8K_SIZE;
LPBYTE PPU_MEM_BANK[12];	//每个元素指向1K数据

BYTE CRAM[32 * 1024];
BYTE VRAM[ 4 * 1024];

BYTE RomHeader[16];

BYTE RAM[0x2000];			//供CPU使用的8K内存
BYTE SRAM[0x2000];
BYTE * PRGBlock = NULL;

BYTE * PatternTable = NULL;	//图案表
BYTE NameTable[0x800];		//命名表
BYTE BGPal[0x10];			//背景调色板
BYTE SPPal[0x10];			//精灵调色板
BYTE SPRAM[0x100];			//精灵RAM，256字节，可存放64个精灵
SPRITE * Sprite = (SPRITE *)SPRAM;
BYTE RevertByte[256];

BYTE ScreenBit[256 * 240];
BITMAPINFOHEADER BmpInfoHeader = { 40, 256, 240, 1, 8, 0, 256 * 240, 0, 0, 0, 0 };
RGBQUAD rgbQuard[64] =
{	
	{0x7F, 0x7F, 0x7F, 0}, {0xB0, 0x00, 0x20, 0}, {0xB8, 0x00, 0x28, 0}, {0xA0, 0x10, 0x60, 0},
	{0x78, 0x20, 0x98, 0}, {0x30, 0x10, 0xB0, 0}, {0x00, 0x30, 0xA0, 0}, {0x00, 0x40, 0x78, 0},
	{0x00, 0x58, 0x48, 0}, {0x00, 0x68, 0x38, 0}, {0x00, 0x6C, 0x38, 0}, {0x40, 0x60, 0x30, 0},
	{0x80, 0x50, 0x30, 0}, {0x00, 0x00, 0x00, 0}, {0x00, 0x00, 0x00, 0}, {0x00, 0x00, 0x00, 0},

	{0xBC, 0xBC, 0xBC, 0}, {0xF8, 0x60, 0x40, 0}, {0xFF, 0x40, 0x40, 0}, {0xF0, 0x40, 0x90, 0},
	{0xC0, 0x40, 0xD8, 0}, {0x60, 0x40, 0xD8, 0}, {0x00, 0x50, 0xE0, 0}, {0x00, 0x70, 0xC0, 0},
	{0x00, 0x88, 0x88, 0}, {0x00, 0xA0, 0x50, 0}, {0x10, 0xA8, 0x48, 0}, {0x68, 0xA0, 0x48, 0},
	{0xC0, 0x90, 0x40, 0}, {0x00, 0x00, 0x00, 0}, {0x00, 0x00, 0x00, 0}, {0x00, 0x00, 0x00, 0},

	{0xFF, 0xFF, 0xFF, 0}, {0xFF, 0xA0, 0x60, 0}, {0xFF, 0x80, 0x50, 0}, {0xFF, 0x70, 0xA0, 0},
	{0xFF, 0x60, 0xF0, 0}, {0xB0, 0x60, 0xFF, 0}, {0x30, 0x78, 0xFF, 0}, {0x00, 0xA0, 0xFF, 0},
	{0x20, 0xD0, 0xE8, 0}, {0x00, 0xE8, 0x98, 0}, {0x40, 0xF0, 0x70, 0}, {0x90, 0xE0, 0x70, 0},
	{0xE0, 0xD0, 0x60, 0}, {0x60, 0x60, 0x60, 0}, {0x00, 0x00, 0x00, 0}, {0x00, 0x00, 0x00, 0},

	{0xFF, 0xFF, 0xFF, 0}, {0xFF, 0xD0, 0x90, 0}, {0xFF, 0xB8, 0xA0, 0}, {0xFF, 0xB0, 0xC0, 0},
	{0xFF, 0xB0, 0xE0, 0}, {0xE8, 0xB8, 0xFF, 0}, {0xB8, 0xC8, 0xFF, 0}, {0xA0, 0xD8, 0xFF, 0},
	{0x90, 0xF0, 0xFF, 0}, {0x80, 0xF0, 0xC8, 0}, {0xA0, 0xF0, 0xA0, 0}, {0xC8, 0xFF, 0xA0, 0},
	{0xF0, 0xFF, 0xA0, 0}, {0xA0, 0xA0, 0xA0, 0}, {0x00, 0x00, 0x00, 0}, {0x00, 0x00, 0x00, 0}
};
LPBITMAPINFO pBmpInfo = NULL;

HANDLE RunEvent        = NULL;
HANDLE ThreadStopEvent = NULL;
HWND g_hWnd;

NESCONFIG NESCONFIG_NTSC =
{
	21477270.0f,		// Base clock
	1789772.5f,		// Cpu clock
	262,			// Total scanlines
	1364,			// Scanline total cycles(15.75KHz)
	1024,			// H-Draw cycles
	340,			// H-Blank cycles
	4,				// End cycles
	1364 * 262,		// Frame cycles
	29830,			// FrameIRQ cycles
	60,				// Frame rate(Be originally 59.94Hz)
	1000.0f / 60.0f	// Frame period(ms)
};

NESCONFIG NESCONFIG_PAL =
{
	21281364.0f,		// Base clock
	1773447.0f,		// Cpu clock
	312,			// Total scanlines
	1362,			// Scanline total cycles(15.625KHz)
	1024,			// H-Draw cycles
	338,			// H-Blank cycles
	2,				// End cycles
	1362 * 312,		// Frame cycles
	35469,			// FrameIRQ cycles
	50,				// Frame rate(Hz)
	1000.0f / 50.0f	// Frame period(ms)
};

NESCONFIG * NesCfg = &NESCONFIG_NTSC;

BOOL bIsRunning = FALSE;




// 全局变量:
HINSTANCE hInst;								// 当前实例
TCHAR szTitle[MAX_LOADSTRING];					// 标题栏文本
TCHAR szWindowClass[MAX_LOADSTRING];			// 主窗口类名


// 此代码模块中包含的函数的前向声明:
ATOM				MyRegisterClass(HINSTANCE hInstance);
BOOL				InitInstance(HINSTANCE, int);
LRESULT CALLBACK	WndProc(HWND, UINT, WPARAM, LPARAM);
INT_PTR CALLBACK	About(HWND, UINT, WPARAM, LPARAM);

int APIENTRY _tWinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPTSTR    lpCmdLine,
                     int       nCmdShow)
{
	UNREFERENCED_PARAMETER(hPrevInstance);
	UNREFERENCED_PARAMETER(lpCmdLine);

 	// TODO: 在此放置代码。
	MSG msg;
	HACCEL hAccelTable;

	// 初始化全局字符串
	LoadString(hInstance, IDS_APP_TITLE, szTitle, MAX_LOADSTRING);
	LoadString(hInstance, IDC_WAVESNES, szWindowClass, MAX_LOADSTRING);
	MyRegisterClass(hInstance);

	// 执行应用程序初始化:
	if (!InitInstance (hInstance, nCmdShow))
	{
		return FALSE;
	}

	hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_WAVESNES));

	// 主消息循环:
	while (GetMessage(&msg, NULL, 0, 0))
	{
		if (!TranslateAccelerator(msg.hwnd, hAccelTable, &msg))
		{
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
	}

	return (int) msg.wParam;
}



//
//  函数: MyRegisterClass()
//
//  目的: 注册窗口类。
//
//  注释:
//
//    仅当希望
//    此代码与添加到 Windows 95 中的“RegisterClassEx”
//    函数之前的 Win32 系统兼容时，才需要此函数及其用法。调用此函数十分重要，
//    这样应用程序就可以获得关联的
//    “格式正确的”小图标。
//
ATOM MyRegisterClass(HINSTANCE hInstance)
{
	WNDCLASSEX wcex;

	wcex.cbSize = sizeof(WNDCLASSEX);

	wcex.style			= CS_HREDRAW | CS_VREDRAW;
	wcex.lpfnWndProc	= WndProc;
	wcex.cbClsExtra		= 0;
	wcex.cbWndExtra		= 0;
	wcex.hInstance		= hInstance;
	wcex.hIcon			= LoadIcon(hInstance, MAKEINTRESOURCE(IDI_WAVESNES));
	wcex.hCursor		= LoadCursor(NULL, IDC_ARROW);
	wcex.hbrBackground	= (HBRUSH)(COLOR_WINDOW+1);
	wcex.lpszMenuName	= MAKEINTRESOURCE(IDC_WAVESNES);
	wcex.lpszClassName	= szWindowClass;
	wcex.hIconSm		= LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_SMALL));

	return RegisterClassEx(&wcex);
}

//
//   函数: InitInstance(HINSTANCE, int)
//
//   目的: 保存实例句柄并创建主窗口
//
//   注释:
//
//        在此函数中，我们在全局变量中保存实例句柄并
//        创建和显示主程序窗口。
//
BOOL InitInstance(HINSTANCE hInstance, int nCmdShow)
{
   HWND hWnd;

   hInst = hInstance; // 将实例句柄存储在全局变量中

   hWnd = CreateWindow(szWindowClass, szTitle, WS_OVERLAPPEDWINDOW,
      CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, NULL, NULL, hInstance, NULL);

   if (!hWnd)
   {
      return FALSE;
   }

   NES_Init();
   ShowWindow(hWnd, nCmdShow);
   UpdateWindow(hWnd);

   return TRUE;
}

//
//  函数: WndProc(HWND, UINT, WPARAM, LPARAM)
//
//  目的: 处理主窗口的消息。
//
//  WM_COMMAND	- 处理应用程序菜单
//  WM_PAINT	- 绘制主窗口
//  WM_DESTROY	- 发送退出消息并返回
//
//

LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	int wmId, wmEvent;
	PAINTSTRUCT ps;
	HDC hdc;
	g_hWnd = hWnd;
	static UINT key;

	switch (message)
	{
	case WM_COMMAND:
		wmId    = LOWORD(wParam);
		wmEvent = HIWORD(wParam);
		// 分析菜单选择:
		switch (wmId)
		{
		case IDM_OPEN:
			//CFileDialog openRomDialog(TRUE);
			LoadRomFile(hWnd);
			NES_Start();
			break;
		case IDM_ABOUT:
			DialogBox(hInst, MAKEINTRESOURCE(IDD_ABOUTBOX), hWnd, About);
			break;
		case IDM_EXIT:
			DestroyWindow(hWnd);
			break;
		default:
			return DefWindowProc(hWnd, message, wParam, lParam);
		}
		break;

	case WM_KEYDOWN:
		key = (UINT)wParam;
		switch (key)
		{
		    case 'W' :
			    JoyPad.SetState(JOY_PAD_1, JOY_PAD_UP, 1);
			    break;
		    case 'S' :
			    JoyPad.SetState(JOY_PAD_1, JOY_PAD_DOWN, 1);
			    break;
		    case 'A' :
			    JoyPad.SetState(JOY_PAD_1, JOY_PAD_LEFT, 1);
			    break;
		    case 'D' :
		    	JoyPad.SetState(JOY_PAD_1, JOY_PAD_RIGHT, 1);
		    	break;
	    	case 'J' :
		    	JoyPad.SetState(JOY_PAD_1, JOY_PAD_B, 1);
		    	break;
		    case 'K' :
		    	JoyPad.SetState(JOY_PAD_1, JOY_PAD_A, 1);
		    	//m_StatusA = 1;
		    	break;
		    case '1' :
		    	JoyPad.SetState(JOY_PAD_1, JOY_PAD_SELECT, 1);
		    	break;
	    	case '2' :
		    	JoyPad.SetState(JOY_PAD_1, JOY_PAD_START, 1);
		    	break;
		}
		break;

	case WM_KEYUP:
		key = (UINT)wParam;
		switch (key)
		{
		case 'W' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_UP, 0);
			break;
		case 'S' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_DOWN, 0);
			break;
		case 'A' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_LEFT, 0);
			break;
		case 'D' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_RIGHT, 0);
			break;
		case 'J' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_B, 0);
			break;
		case 'K' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_A, 0);
			break;
		case '1' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_SELECT, 0);
			break;
		case '2' :
			JoyPad.SetState(JOY_PAD_1, JOY_PAD_START, 0);
			break;
		}
		break;

	case WM_PAINT:
		hdc = BeginPaint(hWnd, &ps);
		// TODO: 在此添加任意绘图代码...
		EndPaint(hWnd, &ps);
		break;
	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	default:
		return DefWindowProc(hWnd, message, wParam, lParam);
	}
	return 0;
}

// “关于”框的消息处理程序。
INT_PTR CALLBACK About(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
	UNREFERENCED_PARAMETER(lParam);
	switch (message)
	{
	case WM_INITDIALOG:
		return (INT_PTR)TRUE;

	case WM_COMMAND:
		if (LOWORD(wParam) == IDOK || LOWORD(wParam) == IDCANCEL)
		{
			EndDialog(hDlg, LOWORD(wParam));
			return (INT_PTR)TRUE;
		}
		break;
	}
	return (INT_PTR)FALSE;
}


void NES_Init()
{	
	for (int i = 0; i < 256; i++)
	{
		BYTE c = 0;
		BYTE mask = 0x80;
		for (int j = 0; j < 8; j++)
		{
			if (i & (1 << j))
				c |= (mask >> j);
		}
		RevertByte[i] = c;
	}
}

void NES_Start()
{
	::memset(RAM,       0, sizeof(RAM));
	::memset(SRAM,      0, sizeof(SRAM));
	::memset(NameTable, 0, sizeof(NameTable));

	pBmpInfo = (LPBITMAPINFO)new BYTE[40 + 1024];
	::memcpy(pBmpInfo, &BmpInfoHeader, sizeof(BITMAPINFOHEADER));
	::memcpy(((BYTE *)pBmpInfo) + sizeof(BITMAPINFOHEADER), rgbQuard, 64 * 4);
	::memset(((BYTE *)pBmpInfo) + sizeof(BITMAPINFOHEADER) + 64 * 4, 0, 192 * 4);

	CPU.Reset();
	PPU.Reset();

	CloseHandleSafely(&RunEvent);
	RunEvent = ::CreateEvent(NULL, TRUE, FALSE, NULL);
	::SetEvent(RunEvent);

	CloseHandleSafely(&ThreadStopEvent);
	ThreadStopEvent = ::CreateEvent(NULL, TRUE, FALSE, NULL);

	::_beginthread(NES_ThreadProc, 0, NULL);

	bIsRunning = TRUE;
}


void NES_Stop()
{
	::ResetEvent(RunEvent);
	::WaitForSingleObject(ThreadStopEvent, INFINITE);

	CloseHandleSafely(&RunEvent);
	CloseHandleSafely(&ThreadStopEvent);

	if (NES_IsRunning())
	{
		NES_ReleasePRGBlock();
		NES_ReleasePatternTable();
	}

	bIsRunning = FALSE;

	if (pBmpInfo != NULL)
	{
		delete [] pBmpInfo;
		pBmpInfo = NULL;
	}
}

void _cdecl NES_ThreadProc(LPVOID lpParam)
{
	long FrameStartTime;
	long SleepTime;
	double FramePeriod = 1000.0 / NesCfg->FrameRate;

	while (::WaitForSingleObject(RunEvent, 0) == STATUS_WAIT_0)
	{
		FrameStartTime = ::timeGetTime();

		NES_FrameExec();

		SleepTime = (long)FramePeriod - ((long)::timeGetTime() - FrameStartTime);
		if (SleepTime > 0)
			::Sleep(SleepTime - 1);
		else
			::Sleep(0);
	}

	::SetEvent(ThreadStopEvent);
}

void NES_FrameExec()
{
	static DWORD nScreenNum = -1;
	nScreenNum++;
	HDC hdc;

	int i;

	//CClientDC dc(&(((CMainFrame *)AfxGetApp()->m_pMainWnd)->m_wndView));
	hdc = GetDC(g_hWnd);

	PPU.RanderBottomBG(ScreenBit);

	// Scanline 0
	CPU.ExecOnBaseCycle(NesCfg->HDrawCycles);
	CPU.ExecOnBaseCycle(FETCH_CYCLES * 32);
	PPU.ScanLine(ScreenBit, 0);
	PPU.ScanlineStart();
	CPU.ExecOnBaseCycle(FETCH_CYCLES * 10 + NesCfg->ScanlineEndCycles);

	// Scanline 1~239
	for (i = 1; i < 240; i++)
	{
		PPU.ScanLine(ScreenBit, i);
		CPU.ExecOnBaseCycle(NesCfg->HDrawCycles);
		CPU.ExecOnBaseCycle(FETCH_CYCLES * 32);
		PPU.ScanlineStart();
		CPU.ExecOnBaseCycle(FETCH_CYCLES * 10 + NesCfg->ScanlineEndCycles);
	}

	::StretchDIBits(hdc, 0, 0, 512, 512, 0, 0, 256, 240,
		ScreenBit, pBmpInfo, DIB_RGB_COLORS, SRCCOPY);
/*
	if (nScreenNum == 0x89B)
	{
		BYTE buf[14] = {0x42, 0x4D, 0x36, 0xf4, 0, 0, 0, 0, 0, 0, 0x36, 0x04, 0, 0};
		CFile file;
		file.Open("D:\\FC.bmp", CFile::modeCreate | CFile::modeWrite);
		file.Write(buf, 14);
		file.Write(pBmpInfo, 1064);
		file.Write(ScreenBit, 256 * 240);
		file.Close();
		int a = 0;
	}
*/
	// Scanline 240
	CPU.ExecOnBaseCycle(NesCfg->HDrawCycles);
	CPU.ExecOnBaseCycle(NesCfg->HBlankCycles);

	// Scanline 241
	PPU.VBlankStart();
	if (PPU.m_REG[0] & PPU_VBLANK_BIT )
		CPU.NMI();
	CPU.ExecOnBaseCycle(NesCfg->HDrawCycles);
	CPU.ExecOnBaseCycle(NesCfg->HBlankCycles);

	// Scanline 242~NesCfg->TotalScanlines-2
	//	PPU.VBlankStart();		
	for (i = 242; i < NesCfg->TotalScanlines - 1; i++)
	{
		CPU.ExecOnBaseCycle(NesCfg->HDrawCycles);
		CPU.ExecOnBaseCycle(NesCfg->HBlankCycles);
	}

	// Scanline NesCfg->TotalScanlines-1
	PPU.VBlankEnd();
	CPU.ExecOnBaseCycle(NesCfg->HDrawCycles);
	CPU.ExecOnBaseCycle(NesCfg->HBlankCycles);
}

void NES_Shutdown()
{
}

void NES_ReleasePRGBlock()
{
	if (PRGBlock)
	{
		delete [] PRGBlock;
		PRGBlock = NULL;
	}
}

void NES_ReleasePatternTable()
{
	if (PatternTable)
	{
		delete [] PatternTable;
		PatternTable = NULL;
	}
}

BOOL NES_IsRunning()
{
	return bIsRunning;
}

//------------------------------------------------------------------------------
// Name: LoadRomFile()
// Desc: Called when the user selected Open from the File menu. Loads
//       the ROM into memory and sets the program up for debugging or
//       runs the game depending on the option set.
//------------------------------------------------------------------------------
HRESULT LoadRomFile(HWND hwnd)
{
	FILE* pRomFile = NULL;  // File pointer for NES Rom.

	//PromptForFileOpen(hwnd);
	while ( pRomFile == NULL ) 
	{
		PromptForFileOpen(hwnd);
		pRomFile = _wfopen(strFileName, (WCHAR*)"r\0b\0");
	}

	::memset(CPU_MEM_BANK, 0, sizeof(CPU_MEM_BANK));
	::memset(PPU_MEM_BANK, 0, sizeof(PPU_MEM_BANK));
	::memset(CRAM, 0, sizeof(CRAM));
	::memset(VRAM, 0, sizeof(VRAM));

	fread(RomHeader, 16, 1, pRomFile);
	if (RomHeader[6] & 0x04)
	{
		fseek( pRomFile, 512, 1);
		//RomFile.Seek(512, CFile::current);
	}

	if (RomHeader[4] > 0)
	{
		PRGBlock = new BYTE[RomHeader[4] * PRG_BLOCK_SIZE];
		fread(PRGBlock, PRG_BLOCK_SIZE, RomHeader[4], pRomFile);
	}
	if (RomHeader[5] > 0)
	{
		PatternTable = new BYTE[RomHeader[5] * PAT_BLOCK_SIZE];
		fread(PatternTable, PAT_BLOCK_SIZE, RomHeader[5], pRomFile);
	}
	fclose(pRomFile);

	PROM_8K_SIZE  = RomHeader[4] * 2;
	PROM_16K_SIZE = RomHeader[4];
	PROM_32K_SIZE = RomHeader[4] / 2;

	VROM_1K_SIZE = RomHeader[5] * 8;
	VROM_2K_SIZE = RomHeader[5] * 4;
	VROM_4K_SIZE = RomHeader[5] * 2;
	VROM_8K_SIZE = RomHeader[5];
	if (VROM_8K_SIZE)
		SetVROM_8K_Bank(0);
	else
	{
		SetCRAM_8K_Bank(0);
	}

	if (RomHeader[6] & 0x2)
		CPU_MEM_BANK[3] = SRAM;

	if (RomHeader[6] & 0x01)	//垂直镜像
		SetNameTable_Bank(0, 1, 0, 1);
	else						//水平镜像
		SetNameTable_Bank(0, 0, 1, 1);

	//	int MapperNo = (RomHeader[6] >> 4) | (RomHeader[7] & 0xF0);
	int MapperNo = RomHeader[6] >> 4;
	/*
	CString str;
	str.Format("%02x  %02x", RomHeader[7], RomHeader[6]);
	AfxGetApp()->m_pMainWnd->SetWindowTextA(str);
	return FALSE;
	*/
	CreateMapper(MapperNo);
	NES_Mapper.Reset();
	return TRUE;
} // end LoadRomFile()




//------------------------------------------------------------------------------
// Name: PromptForFileOpen()
// Desc: Prompts the user with the open file common dialog and 
//       returns the file name and path of the file the user selected.
//       If the user presses cancel the this function returns FALSE.
//------------------------------------------------------------------------------
HRESULT PromptForFileOpen(HWND hwndOwner)
{
	OPENFILENAME ofn;

	// Initialize OPENFILENAME
	ZeroMemory(&ofn, sizeof(OPENFILENAME));
	ofn.lStructSize = sizeof(OPENFILENAME);
	ofn.hwndOwner = hwndOwner;
	ofn.hInstance = hInst;
	ofn.lpstrFile = (LPWSTR)strFileName;
	ofn.nMaxFile = 512;
	ofn.lpstrFilter = TEXT("NES Files (*.nes)\0*.nes;\0\0\0");
	ofn.nFilterIndex = 1;
	ofn.lpstrFileTitle = NULL;
	ofn.nMaxFileTitle = 0;
	ofn.lpstrInitialDir = NULL;
	ofn.Flags = OFN_PATHMUSTEXIST | OFN_FILEMUSTEXIST | OFN_EXPLORER;

	// Prompt the user to select the file.
	if (GetOpenFileName(&ofn) == FALSE)
		return FALSE;

	// Save the filename string for the user.
	//strcpy((LPSTR)strFileName, (char *)ofn.lpstrFile);
	//CovertRouteFormat(strFileName);

	return TRUE;
} // end PromptForFileOpen()

/*
//转换API得到的文件路径为c语言格式

void CovertRouteFormat(char* strFileName)
{
	int i = 0;
	int j = 0;
	char* str = (char*)malloc(512*sizeof(char));

	while (i < 512 && j < 512)
	{
		if (*(strFileName + i) == 92)
		{
			*(str + j) = 92;
			j++;
		}
		if (*(strFileName + i) != '\0')
		{
			*(str + j) = *(strFileName + i);
			j++;
		}else if (*(strFileName + i + 1) == '\0')
		{
			*(str + j) = *(strFileName + i);
			i = 512;
		}
		i++;
	}

	i = 0;
	while (i <= j)
	{
		*(strFileName + i) = *(str + i);
		i++;
	}
}
*/

void SetPROM_8K_Bank(BYTE page, int bank)
{
	bank %= PROM_8K_SIZE;
	CPU_MEM_BANK[page] = PRGBlock + 0x2000 * bank;
}

void SetPROM_16K_Bank(BYTE page, int bank)
{
	SetPROM_8K_Bank(page + 0, bank * 2 + 0);
	SetPROM_8K_Bank(page + 1, bank * 2 + 1);
}

void SetPROM_32K_Bank(int bank)
{
	SetPROM_8K_Bank(4, bank * 4 + 0);
	SetPROM_8K_Bank(5, bank * 4 + 1);
	SetPROM_8K_Bank(6, bank * 4 + 2);
	SetPROM_8K_Bank(7, bank * 4 + 3);
}

void SetPROM_32K_Bank(int bank0, int bank1, int bank2, int bank3)
{
	SetPROM_8K_Bank(4, bank0);
	SetPROM_8K_Bank(5, bank1);
	SetPROM_8K_Bank(6, bank2);
	SetPROM_8K_Bank(7, bank3);
}

void SetVROM_1K_Bank(BYTE page, int bank)
{
	bank %= VROM_1K_SIZE;
	PPU_MEM_BANK[page] = PatternTable + 0x0400 * bank;
}

void SetVROM_2K_Bank(BYTE page, int bank)
{
	SetVROM_1K_Bank(page + 0, bank * 2 + 0);
	SetVROM_1K_Bank(page + 1, bank * 2 + 1);
}

void SetVROM_4K_Bank(BYTE page, int bank)
{
	SetVROM_1K_Bank(page + 0, bank * 4 + 0);
	SetVROM_1K_Bank(page + 1, bank * 4 + 1);
	SetVROM_1K_Bank(page + 2, bank * 4 + 2);
	SetVROM_1K_Bank(page + 3, bank * 4 + 3);
}

void SetVROM_8K_Bank(int bank)
{
	for(int i = 0; i < 8; i++)
		SetVROM_1K_Bank(i, bank * 8 + i);
}

void SetCRAM_1K_Bank(BYTE page, int bank)
{
	bank &= 0x1F;
	PPU_MEM_BANK[page] = CRAM + 0x0400 * bank;
}

void SetCRAM_2K_Bank(BYTE page, int bank)
{
	SetCRAM_1K_Bank(page + 0, bank * 2 + 0);
	SetCRAM_1K_Bank(page + 1, bank * 2 + 1);
}

void SetCRAM_4K_Bank(BYTE page, int bank)
{
	SetCRAM_1K_Bank(page + 0, bank * 4 + 0);
	SetCRAM_1K_Bank(page + 1, bank * 4 + 1);
	SetCRAM_1K_Bank(page + 2, bank * 4 + 2);
	SetCRAM_1K_Bank(page + 3, bank * 4 + 3);
}

void SetCRAM_8K_Bank(int bank)
{
	for (int i = 0; i < 8; i++)
		SetCRAM_1K_Bank(i, bank * 8 + i);
}

void SetVRAM_1K_Bank(BYTE page, int bank)
{
	bank &= 3;
	PPU_MEM_BANK[page] = VRAM + 0x0400 * bank;
}

void SetNameTable_Bank(int bank0, int bank1, int bank2, int bank3)
{
	SetVRAM_1K_Bank( 8, bank0);
	SetVRAM_1K_Bank( 9, bank1);
	SetVRAM_1K_Bank(10, bank2);
	SetVRAM_1K_Bank(11, bank3);
}

BYTE NES_ReadReg(WORD addr)
{
	switch (addr & 0xFF)
	{
	case 0x00: case 0x01: case 0x02: case 0x03:
	case 0x04: case 0x05: case 0x06: case 0x07:
	case 0x08: case 0x09: case 0x0A: case 0x0B:
	case 0x0C: case 0x0D: case 0x0E: case 0x0F:
	case 0x10: case 0x11: case 0x12: case 0x13:
		return	APU.Read(addr);
		break;
	case	0x15:
		return	APU.Read(addr);
		break;
	case	0x14:
		return addr & 0xFF;
		break;
	case	0x16:
		return (JoyPad.GetValue(0) | 0x40);
		break;
	case	0x17:
		return JoyPad.GetValue(1) | APU.Read(addr);
		break;
	default:
		return	NES_Mapper.ExRead(addr);
		break;
	}
}

BYTE NES_Read(WORD addr)
{
	switch ( addr>>13 )
	{
	case 0x00 :	// $0000-$1FFF
		return RAM[addr & 0x07FF];
	case 0x01 :	// $2000-$3FFF
		return	PPU.ReadFromPort(addr & 0xE007);
	case 0x02 :	// $4000-$5FFF
		if (addr < 0x4100)
			return	NES_ReadReg(addr);
		else
			return	NES_Mapper.ReadLow(addr);
		break;
	case 0x03 :	// $6000-$7FFF
		return	NES_Mapper.ReadLow(addr);
	case 0x04 :	// $8000-$9FFF
	case 0x05 :	// $A000-$BFFF
	case 0x06 :	// $C000-$DFFF
	case 0x07 :	// $E000-$FFFF
		return	CPU_MEM_BANK[addr >> 13][addr & 0x1FFF];
	}

	return	0x00;	// Warning
}

void NES_WriteReg(WORD addr, BYTE val)
{
	/* 0x4000 - 0x40FF */	
	switch ( addr & 0xFF )
	{
	case 0x00: case 0x01: case 0x02: case 0x03:
	case 0x04: case 0x05: case 0x06: case 0x07:
	case 0x08: case 0x09: case 0x0A: case 0x0B:
	case 0x0C: case 0x0D: case 0x0E: case 0x0F:
	case 0x10: case 0x11: case 0x12: case 0x13:
	case 0x15 :
		break;
	case 0x14 :
		::memcpy(Sprite, CPU.GetRAM(((WORD)val) << 8), 256);
		CPU.m_DMACycle += 514;
		break;
	case 0x16 :
		NES_Mapper.ExWrite(addr, val);	// For VS-Unisystem
		JoyPad.InputBursh(val);
		break;
	case 0x17 :
		break;
		// VirtuaNES屌桳億乕僩
	case 0x18 :
		break;
	default:
		NES_Mapper.ExWrite(addr, val);
		break;
	}
}
void NES_Write(WORD addr, BYTE val)
{
	switch (addr >> 13)
	{
	case 0x00 :	// $0000-$1FFF
		RAM[addr & 0x07FF] = val;
		break;
	case 0x01 :	// $2000-$3FFF
		PPU.WriteToPort(addr & 0xE007, val);
		break;
	case 0x02 :	// $4000-$5FFF
		if (addr < 0x4100)
			NES_WriteReg(addr, val);
		else
			NES_Mapper.WriteLow(addr, val);
		break;
	case 0x03 :	// $6000-$7FFF
		NES_Mapper.WriteLow(addr, val);
		break;
	case 0x04 :	// $8000-$9FFF
	case 0x05 :	// $A000-$BFFF
	case 0x06 :	// $C000-$DFFF
	case 0x07 :	// $E000-$FFFF
		NES_Mapper.Write(addr, val);
		break;
	}
}

BYTE * NES_GetRAM(WORD addr)
{
	switch ( addr>>13 )
	{
	case 0x00 :	// $0000-$1FFF
		return &RAM[addr & 0x07FF];
	case 0x04 :	// $8000-$9FFF
	case 0x05 :	// $A000-$BFFF
	case 0x06 :	// $C000-$DFFF
	case 0x07 :	// $E000-$FFFF
		return	&CPU_MEM_BANK[addr >> 13][addr & 0x1FFF];
	}
	//ASSERT(FALSE);
	return NULL;
}


