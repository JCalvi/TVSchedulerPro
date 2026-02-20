/*
* Copyright (c) 2009 Blue Bit Solutions
* Copyright (c) 2010-2023 John Calvi
*
* This file is part of TV Scheduler Pro
*
* TV Scheduler Pro is free software: you can redistribute it and/or
* modify it under the terms of the GNU General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* TV Scheduler Pro is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with TV Scheduler Pro.
* If not, see <http://www.gnu.org/licenses/>.
*/

// WSTray.cpp : Defines the entry point for the application.
//

#include <Windows.h>
#include <stdio.h>
#include <string>
#include <sstream>
#include <process.h>
#include <tchar.h>
#include "SharedMemory.h"
#include "resource.h"
#include "RegStore.h"
#include "PowrProf.h"

#define WM_USER_SHELLICON WM_USER + 996

const int sleepTime = 90; // 10sec units
static HINSTANCE appInstance = 0;
static HWND hiddenWindow = 0;
static HWND statusDlg = 0;

static int loop = 0;
static int loop2 = 0;
static int lastCount = 0;
static int port = -1;
static SYSTEMTIME nextWake;
static SYSTEMTIME noticeTime;

static int powerOverride = 1;
static int forceSleep = 0;
static HMODULE user32DLL = NULL;
typedef BOOL (WINAPI *ShutdownBlockReasonCreateEX)(HWND, LPCWSTR);
static ShutdownBlockReasonCreateEX SBRC = NULL;
typedef BOOL (WINAPI *ShutdownBlockReasonDestroyEX)(HWND);
static ShutdownBlockReasonDestroyEX SBRD = NULL;
static int shutdownBlockReasonSet = 0;
static bool captureSoon = FALSE;

//https://www.codeproject.com/Articles/7914/MessageBoxTimeout-API
//Functions & other definitions required-->
typedef int (__stdcall *MSGBOXAAPI)(IN HWND hWnd,
        IN LPCSTR lpText, IN LPCSTR lpCaption,
        IN UINT uType, IN WORD wLanguageId, IN DWORD dwMilliseconds);
typedef int (__stdcall *MSGBOXWAPI)(IN HWND hWnd,
        IN LPCWSTR lpText, IN LPCWSTR lpCaption,
        IN UINT uType, IN WORD wLanguageId, IN DWORD dwMilliseconds);

int MessageBoxTimeoutA(IN HWND hWnd, IN LPCSTR lpText,
    IN LPCSTR lpCaption, IN UINT uType,
    IN WORD wLanguageId, IN DWORD dwMilliseconds);
int MessageBoxTimeoutW(IN HWND hWnd, IN LPCWSTR lpText,
    IN LPCWSTR lpCaption, IN UINT uType,
    IN WORD wLanguageId, IN DWORD dwMilliseconds);


#ifdef UNICODE
    #define MessageBoxTimeout MessageBoxTimeoutW
#else
    #define MessageBoxTimeout MessageBoxTimeoutA
#endif

#define MB_TIMEDOUT 32000

int MessageBoxTimeoutA(HWND hWnd, LPCSTR lpText,
    LPCSTR lpCaption, UINT uType, WORD wLanguageId,
    DWORD dwMilliseconds)
{
    static MSGBOXAAPI MsgBoxTOA = NULL;

    if (!MsgBoxTOA)
    {
        HMODULE hUser32 = GetModuleHandle(_T("user32.dll"));
        if (hUser32)
        {
            MsgBoxTOA = (MSGBOXAAPI)GetProcAddress(hUser32,
                                      "MessageBoxTimeoutA");
            //fall through to 'if (MsgBoxTOA)...'
        }
        else
        {
            //stuff happened, add code to handle it here
            //(possibly just call MessageBox())
            return 0;
        }
    }

    if (MsgBoxTOA)
    {
        return MsgBoxTOA(hWnd, lpText, lpCaption,
              uType, wLanguageId, dwMilliseconds);
    }

    return 0;
}

int MessageBoxTimeoutW(HWND hWnd, LPCWSTR lpText,
    LPCWSTR lpCaption, UINT uType, WORD wLanguageId, DWORD dwMilliseconds)
{
    static MSGBOXWAPI MsgBoxTOW = NULL;

    if (!MsgBoxTOW)
    {
        HMODULE hUser32 = GetModuleHandle(_T("user32.dll"));
        if (hUser32)
        {
            MsgBoxTOW = (MSGBOXWAPI)GetProcAddress(hUser32,
                                      "MessageBoxTimeoutW");
            //fall through to 'if (MsgBoxTOW)...'
        }
        else
        {
            //stuff happened, add code to handle it here
            //(possibly just call MessageBox())
            return 0;
        }
    }

    if (MsgBoxTOW)
    {
        return MsgBoxTOW(hWnd, lpText, lpCaption,
               uType, wLanguageId, dwMilliseconds);
    }

    return 0;
}

bool isTVSPPowerProfileActive()
{
	// Need to check the power profile is TVSP otherwise no forceSleep
	DWORD dataSize = 0;
	char guid[64];
	char tvspName[] = "TVSP";
	dataSize = sizeof(guid);
	char hkey1[] = "SYSTEM\\CurrentControlSet\\Control\\Power\\User\\PowerSchemes";
	char hkey2[256];
	// Get the active Power Scheme GUID
	if (ERROR_SUCCESS == RegGetValueA(HKEY_LOCAL_MACHINE, hkey1, "ActivePowerScheme", RRF_RT_REG_SZ, 0, &guid, &dataSize))
	{
		// Add the active profile guid to the base key to create the active profile key
		strcpy( hkey2, hkey1 );
		strcat( hkey2, "\\" );
		strcat( hkey2, guid );
		char frName[256];
		dataSize = sizeof(frName);
		// Get the Friendly Name of the Active Power Scheme
		if (ERROR_SUCCESS == RegGetValueA(HKEY_LOCAL_MACHINE, hkey2, "FriendlyName", RRF_RT_REG_SZ, 0, &frName, &dataSize))
		{
			// See if the Active Scheme Friendly Name has TVSP in it
			int pos_search = 0;
			int pos_text = 0;
			int len_search = sizeof(tvspName)-1;
			int len_text = sizeof(frName)-1;
			for (pos_text = 0; pos_text < len_text - len_search;++pos_text)
			{
				if(frName[pos_text] == tvspName[pos_search])
				{
					++pos_search;
					if(pos_search == len_search)
					{
						return TRUE;
					}
				}
				else
				{
					pos_text -=pos_search;
					pos_search = 0;
				}
			}
			// No Match Found
			return FALSE;
		}
		else
		{
			// Cannot Get Friendly Name of Active Profile
			return FALSE;
		}
	}
	else
	{
		// Cannot Get GUID of Active Profile
		return FALSE;
	}
}


int getActiveCount()
{
	int active = 0;

	if(openSharedMemory() == 0)
	{
		SHARED_DATA *sharedData = getSharedData();

		active = sharedData->active;
		port = sharedData->port;
		nextWake = sharedData->nextWake;

		if( (sharedData->noticeTime.wDay != 0 && sharedData->noticeTime.wDay != noticeTime.wDay) ||
			(sharedData->noticeTime.wDayOfWeek != 0 && sharedData->noticeTime.wDayOfWeek != noticeTime.wDayOfWeek) ||
			(sharedData->noticeTime.wHour != 0 && sharedData->noticeTime.wHour != noticeTime.wHour) ||
			(sharedData->noticeTime.wMilliseconds != 0 && sharedData->noticeTime.wMilliseconds != noticeTime.wMilliseconds) ||
			(sharedData->noticeTime.wMinute != 0 && sharedData->noticeTime.wMinute != noticeTime.wMinute) ||
			(sharedData->noticeTime.wMonth != 0 && sharedData->noticeTime.wMonth != noticeTime.wMonth) ||
			(sharedData->noticeTime.wSecond != 0 && sharedData->noticeTime.wSecond != noticeTime.wSecond) ||
			(sharedData->noticeTime.wYear != 0 && sharedData->noticeTime.wYear != noticeTime.wYear))
		{

			noticeTime = sharedData->noticeTime;
			char buff[256];
			_snprintf(buff, 256, "%s", sharedData->notice);
			MessageBox(NULL, buff, "TV Scheduler Pro Notice", MB_OK);
		}

		closeSharedMemory();
	}
	else
	{
		memset(&nextWake, 0, sizeof(SYSTEMTIME));
		port = -1;
		return -1;
		//MessageBox(NULL, "Not Running", "TVSP Not Running", MB_OK);
	}

	return active;
}


int getTimeToNext(char *buff, int sizeBuff)
{
	__int64 period1second = 10000000;

	SYSTEMTIME st;
	GetLocalTime(&st);
	FILETIME ft_now;
	SystemTimeToFileTime(&st, &ft_now);
	FILETIME ft_next;
	SystemTimeToFileTime(&nextWake, &ft_next);

	__int64 *now = 0;
	now = (__int64*)&ft_now;

	__int64 *next = 0;
	next = (__int64*)&ft_next;

	__int64 timeToNext = (*next) - (*now);

	if(timeToNext < 0)
	{
		timeToNext = 0;
	}
	else
	{
		timeToNext += (period1second * 60); // add one minute
	}

	int minutes = (int)(timeToNext / (period1second * 60));

	if(minutes == 0 || minutes > (60 * 24 * 365 * 3)) // greater than 3 year into future
	{
		if(buff != NULL)
			_snprintf(buff, sizeBuff, "No future schedules");
		return -1;
	}

	if(buff != NULL)
	{
		int hoursToNext = (int)(minutes / (60));
		int minutesToNext = (int)(minutes - (hoursToNext * 60));

		_snprintf(buff, sizeBuff, "%d Hours %d Minutes", hoursToNext, minutesToNext);
	}

	return minutes;
}

void updateTrayIcon()
{
	NOTIFYICONDATA nData;
	nData.cbSize = sizeof(NOTIFYICONDATA);
	nData.hWnd = hiddenWindow;
	nData.uID = 0;
	nData.uFlags = NIF_ICON | NIF_TIP | NIF_MESSAGE;
	nData.uCallbackMessage = WM_USER_SHELLICON;

	int iconID = 0;
	int active = getActiveCount();

	int timeToNext = getTimeToNext(NULL, 0);

	if(active == -1)
	{
		strcpy(nData.szTip,"TV Scheduler Pro Not Running!");
		iconID = IDI_STATUS_STOPPED;
	}
	else if(active > 0)
	{
		strcpy(nData.szTip,"TV Scheduler Pro Capturing");
		if (active > 6)
			iconID = IDI_STATUS_CAPTURING;
		else
			iconID = IDI_STATUS_CAPTURING + active;
	}
	else if (timeToNext >= 0 && timeToNext <= 10)
	{
		strcpy(nData.szTip,"TV Scheduler Pro Capture Imminent");
		iconID = IDI_STATUS_PENDING + timeToNext;
	}
	else if(loop2 > 0)
	{
		strcpy(nData.szTip,"TV Scheduler Pro Sleep Countdown");
		iconID = IDI_STATUS_SLEEP + (sleepTime+2-loop2)/6;
	}
	else
	{
		strcpy(nData.szTip,"TV Scheduler Pro Idle");
		iconID = IDI_STATUS_RUNNING;
	}

	HINSTANCE hModule  = GetModuleHandle(NULL);
	HICON icon = LoadIcon(hModule, (LPCTSTR)iconID);
	nData.hIcon = icon;

	Shell_NotifyIcon(NIM_MODIFY, &nData);

	DeleteObject(icon);
}


void waitForTrayWnd()
{
	HWND hWndTaskBar = 0;
	int count = 0;

	while(count < 60)
	{
		hWndTaskBar = FindWindow("Shell_TrayWnd", "");

		if(hWndTaskBar != 0)
			break;

		count++;
		Sleep(500);
	}
}

void addTrayIcon()
{
	NOTIFYICONDATA nData;
	nData.cbSize = sizeof(NOTIFYICONDATA);
	nData.hWnd = hiddenWindow;
	nData.uID = 0;

	Shell_NotifyIcon(NIM_DELETE, &nData);

	nData.cbSize = sizeof(NOTIFYICONDATA);
	nData.hWnd = hiddenWindow;
	nData.uCallbackMessage = WM_USER_SHELLICON;
	nData.uID = 0;
	nData.uFlags = NIF_ICON | NIF_TIP | NIF_MESSAGE;
	strcpy(nData.szTip,"TV Scheduler Pro Idle");
	HICON icon = LoadIcon(appInstance, (LPCTSTR)IDI_STATUS_RUNNING);
	nData.hIcon = icon;
	Shell_NotifyIcon(NIM_ADD, &nData);
	DeleteObject(icon);
}

LRESULT CALLBACK statusProc(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
	switch (message)
	{
		case WM_INITDIALOG:
		{
			return TRUE;
		}

		case WM_COMMAND:
		{
			if (LOWORD(wParam) == IDOK || LOWORD(wParam) == IDCANCEL)
			{
				DestroyWindow(hDlg);
				statusDlg = 0;
				return TRUE;
			}
			break;
		}
	}
    return FALSE;
}

void openBrowser(std::string url)
{
	char urlBuff[256];
	sprintf(urlBuff, "http://localhost:%d%s", port, url.c_str());

	ShellExecute(NULL, "open", urlBuff, NULL, NULL, SW_SHOWNORMAL);
}

LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	static UINT s_uTaskbarRestart;

	switch (message)
	{
        case WM_CREATE:
            s_uTaskbarRestart = RegisterWindowMessage(TEXT("TaskbarCreated"));
            break;

		case WM_USER+69:
		{
			if(statusDlg > 0)
			{
				DestroyWindow(statusDlg);
			}

			HINSTANCE hInst = GetModuleHandle(NULL);
			statusDlg = CreateDialogParam(hInst, (LPCTSTR)IDD_STATUS, NULL, (DLGPROC)statusProc, 0);

			int current = getActiveCount();

			char buff[128];
			if(current == -1)
			{
				strcpy(buff, "TV Scheduler Pro Not Running!");
			}
			else if(current > 0)
			{
				strcpy(buff, "TV Scheduler Pro Capturing");
			}
			else if (captureSoon)
			{
				strcpy(buff, "TV Scheduler Pro Capture Imminent");
			}
			else
			{
				strcpy(buff, "TV Scheduler Pro Idle");
			}
			SendDlgItemMessage(statusDlg, IDC_STATUS_TEXT, WM_SETTEXT, (WPARAM)127, (LPARAM)buff);

			if(current == -1)
				current = 0;

			sprintf(buff, "%d", current);
			SendDlgItemMessage(statusDlg, IDC_STATUS_CURRENT, WM_SETTEXT, (WPARAM)127, (LPARAM)buff);

			int timeToNext = getTimeToNext(buff, 127);
			SendDlgItemMessage(statusDlg, IDC_STATUS_TO_NEXT, WM_SETTEXT, (WPARAM)127, (LPARAM)buff);

			int timeToSleep = (sleepTime - loop2)*10;
			if (forceSleep > 0)
			{
				if (captureSoon)
				{
					loop2 = 0; // Reset the force sleep counter
					strcpy(buff, "Disabled by Capture");
				}
				else if (isTVSPPowerProfileActive())
				{
  					sprintf(buff, "%d", timeToSleep);
					strcat(buff," seconds");
				}
				else
				{
					loop2 = 0; // Reset the force sleep counter
					strcpy(buff, "Disabled by Power Plan");
				}
			}
			else
			{
				strcpy(buff, "Force Sleep Not Enabled");
				loop2 = 0; // Reset the force sleep counter
			}
			SendDlgItemMessage(statusDlg, IDC_STATUS_TO_SLEEP, WM_SETTEXT, (WPARAM)127, (LPARAM)buff);

			if(wParam == 1 || wParam == 2)
			{
				sprintf(buff, "  Power Action Denied: Capture Imminent");
				SetWindowText(statusDlg, buff);
			}

			updateTrayIcon();
			ShowWindow(statusDlg, 1);

			return 0;
		}

		case WM_USER_SHELLICON:
		{
			CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
			int defaultAction = store->getInt("defaultAction", 0);
			int doubleClick = store->getInt("doubleClick", 0);
			delete store;

			if(lParam == WM_RBUTTONUP)
			{
				SetForegroundWindow(hWnd);
				POINT cursor_pos;
				GetCursorPos(&cursor_pos);

				HMENU popMenu = (HMENU)LoadMenu(appInstance, MAKEINTRESOURCE(IDR_MENU1));
				HMENU subMenu = GetSubMenu(popMenu, 0);

				// setup menu item info
				MENUITEMINFO menItemInfo;
				menItemInfo.cbSize = sizeof(MENUITEMINFO);
				menItemInfo.fMask = MIIM_STATE;

				// set power override
				if(powerOverride == 1)
					menItemInfo.fState = MFS_CHECKED;
				else
					menItemInfo.fState = MFS_UNCHECKED;
				SetMenuItemInfo(subMenu, ID_SHOWSTATUS_POWEROVERRIDE, false, &menItemInfo);

				// set force sleep S3
				if(forceSleep == 3)
					menItemInfo.fState = MFS_CHECKED;
				else
					menItemInfo.fState = MFS_UNCHECKED;
				SetMenuItemInfo(subMenu, ID_FORCESLEEP_SLEEPS3, false, &menItemInfo);

				// set force sleep S4
				if(forceSleep == 4)
					menItemInfo.fState = MFS_CHECKED;
				else
					menItemInfo.fState = MFS_UNCHECKED;
				SetMenuItemInfo(subMenu, ID_FORCESLEEP_HIBERNATES4, false, &menItemInfo);

				// set double click action
				if(doubleClick == 1)
					menItemInfo.fState = MFS_CHECKED;
				else
					menItemInfo.fState = MFS_UNCHECKED;
				SetMenuItemInfo(subMenu, ID_DEFAULTACTION_DOUBLECLICK, false, &menItemInfo);

				if(defaultAction == 0)
				{
					menItemInfo.fState = MFS_CHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_SHOWSTATUS, false, &menItemInfo);
					menItemInfo.fState = MFS_UNCHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_MAINPAGE, false, &menItemInfo);
					menItemInfo.fState = MFS_UNCHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_EPGPAGE, false, &menItemInfo);
				}
				else if(defaultAction == 1)
				{
					menItemInfo.fState = MFS_UNCHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_SHOWSTATUS, false, &menItemInfo);
					menItemInfo.fState = MFS_CHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_MAINPAGE, false, &menItemInfo);
					menItemInfo.fState = MFS_UNCHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_EPGPAGE, false, &menItemInfo);
				}
				else if(defaultAction == 2)
				{
					menItemInfo.fState = MFS_UNCHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_SHOWSTATUS, false, &menItemInfo);
					menItemInfo.fState = MFS_UNCHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_MAINPAGE, false, &menItemInfo);
					menItemInfo.fState = MFS_CHECKED;
					SetMenuItemInfo(subMenu, ID_DEFAULTACTION_EPGPAGE, false, &menItemInfo);
				}

				BOOL popMenuWorked = TrackPopupMenuEx(subMenu,
					TPM_LEFTBUTTON|TPM_RIGHTBUTTON, cursor_pos.x-100, cursor_pos.y, hWnd, NULL);

				return 0;
			}
			else if(lParam == WM_LBUTTONUP)
			{
				if(doubleClick == 0)
				{
					if(defaultAction == 0)
					{
						PostMessage(hiddenWindow, WM_USER+69, 0, 0);
					}
					else if(defaultAction == 1)
					{
						openBrowser("");
					}
					else if(defaultAction == 2)
					{
						openBrowser("/servlet/EpgDataRes?action=12&scrollto=-1");
					}
				}
			}
			else if(lParam == WM_LBUTTONDBLCLK)
			{
				if(doubleClick == 1)
				{
					if(defaultAction == 0)
					{
						PostMessage(hiddenWindow, WM_USER+69, 0, 0);
					}
					else if(defaultAction == 1)
					{
						openBrowser("");
					}
					else if(defaultAction == 2)
					{
						openBrowser("/servlet/EpgDataRes?action=12&scrollto=-1");
					}
				}
			}

			break;
		}

		// this is the power off message, return FALSE to stop power off
		case WM_QUERYENDSESSION:
		{
			if(lParam == 0)
			{
				// if we are not overriding power states return TRUE
				if(powerOverride == 0)
				{
					return TRUE;
				}

				int current = getActiveCount();
				int timeToNext = getTimeToNext(NULL, 0);

				if(current != -1 && (current > 0 || (timeToNext >= 0 && timeToNext <= 10)))
				{
					PostMessage(hiddenWindow, WM_USER+69, 2, 0);
					return FALSE;
				}
			}

			return TRUE;
		}

		// suspend request
		case WM_POWERBROADCAST:
		{
			if(wParam == PBT_APMQUERYSUSPEND)
			{
				// if we are not overriding power states return TRUE
				if(powerOverride == 0)
				{
					return TRUE;
				}

				int current = getActiveCount();
				int timeToNext = getTimeToNext(NULL, 0);

				if(current != -1 && (current > 0 || (timeToNext >= 0 && timeToNext <= 10)))
				{
					PostMessage(hiddenWindow, WM_USER+69, 1, 0);
					return BROADCAST_QUERY_DENY;
				}

				return TRUE;
			}
			break;
		}

		case WM_COMMAND:
		{
			switch(wParam)
			{
				case ID_SHOWSTATUS_SHOWSTATUS:
					PostMessage(hiddenWindow, WM_USER+69, 0, 0);
					return TRUE;

				case ID_SHOWSTATUS_OPENMAINPAGE:
					openBrowser("");
					return TRUE;

				case ID_SHOWSTATUS_OPENEPGPAGE:
					openBrowser("/servlet/EpgDataRes?action=12&scrollto=-1");
					return TRUE;

				case ID_SHOWSTATUS_POWEROVERRIDE:
				{
					if(powerOverride == 0)
						powerOverride = 1;
					else if(powerOverride == 1)
						powerOverride = 0;
					else
						powerOverride = 1;

					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					store->setInt("powerOverride", powerOverride);
					delete store;

					if(powerOverride == 0)
					{

						if(SBRC != NULL && SBRD != NULL)
						{
							SBRD(hiddenWindow);
						}

						SetThreadExecutionState(ES_CONTINUOUS);
						shutdownBlockReasonSet = 0;
					}

					return TRUE;
				}
				case ID_FORCESLEEP_SLEEPS3:
				{
					if(forceSleep != 3)
					{
						forceSleep = 3;
						loop2 = 1; // Set the force sleep counter
					}
					else
					{
						forceSleep = 0;
						loop2 = 0; // Reset the force sleep counter
					}

					updateTrayIcon();
					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					store->setInt("forceSleep", forceSleep);
					delete store;
					return TRUE;
				}
				case ID_FORCESLEEP_HIBERNATES4:
				{
					if(forceSleep != 4)
					{
						forceSleep = 4;
						loop2 = 1; // Set the force sleep counter
					}
					else
					{
						forceSleep = 0;
						loop2 = 0; // Reset the force sleep counter
					}

					updateTrayIcon();
					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					store->setInt("forceSleep", forceSleep);
					delete store;
					return TRUE;
				}
				case ID_DEFAULTACTION_DOUBLECLICK:
				{
					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					int doubleClick = store->getInt("doubleClick", 0);

					if(doubleClick == 0)
						store->setInt("doubleClick", 1);
					else
						store->setInt("doubleClick", 0);

					delete store;

					return TRUE;
				}
				case ID_DEFAULTACTION_SHOWSTATUS:
				{
					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					store->setInt("defaultAction", 0);
					delete store;
					return TRUE;
				}
				case ID_DEFAULTACTION_MAINPAGE:
				{
					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					store->setInt("defaultAction", 1);
					delete store;
					return TRUE;
				}
				case ID_DEFAULTACTION_EPGPAGE:
				{
					CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
					store->setInt("defaultAction", 2);
					delete store;
					return TRUE;
				}
				case ID_EXIT_TRAY:
				{
					int answer = MessageBox(NULL, "Do you want to stop the tray icon from loading when you log in?", "TV Scheduler Pro Notice", MB_YESNO);

					if(IDYES == answer)
					{
						CRegStore *reg = new CRegStore(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run");
						reg->deleteKey("wstray");
						delete reg;
					}

					NOTIFYICONDATA nData;
					nData.cbSize = sizeof(NOTIFYICONDATA);
					nData.hWnd = hiddenWindow;
					nData.uID = 0;
					Shell_NotifyIcon(NIM_DELETE, &nData);

					PostMessage(hiddenWindow, WM_QUIT, 0, 0);
					return TRUE;
				}

			}
			break;
		}

		case WM_TIMER:
		{
			int current = getActiveCount();

			if(current != lastCount || loop++ >= 6 || loop2++ >= sleepTime)
			{
				// force sleep or hibernate after 15mins (sleepTime x 10000ms)
				if(forceSleep > 0 && !captureSoon)
				{
					if (isTVSPPowerProfileActive())
					{
						if (loop2 >= sleepTime)
						{
							// Match Found, About to Force Sleep
							loop2 = 1; // Reset the force sleep counter to 1
							int iRet = MessageBoxTimeout(NULL, _T("About to Force Sleep, would you like to keep working?"),
									_T("TV Scheduler Pro Notice"), MB_YESNO, 0, 10000);
							if((iRet == MB_TIMEDOUT) || (iRet == IDNO))
							{
								// Force Sleep Allowed, TVSP Profile is Active & timer has expired
								updateTrayIcon(); // Update for next wake
								if(forceSleep == 3) SetSuspendState(FALSE, FALSE, FALSE);
								else if(forceSleep == 4) SetSuspendState(TRUE, FALSE, FALSE);
							}
						}
					}
					else
						loop2 = 0; // Reset the force sleep counter

				}
				else
					loop2 = 0; // Reset the force sleep counter

				updateTrayIcon();
				lastCount = current;
				loop = 0;
			} // end loop++

			// if power override is enabled
			// if we are vista and have access to the ShutdownBlockReasonCreate
			// and ShutdownBlockReasonDestroy then use them
			if(powerOverride == 1 && SBRC != NULL && SBRD != NULL)
			{
				int timeToNext = getTimeToNext(NULL, 0);

				// if capturing then create the BlockReason and set thread state
				if(current != -1 && (current > 0 || (timeToNext >= 0 && timeToNext <= 10)))
				{
                    loop2 = 0; // Reset the force sleep counter
					captureSoon = TRUE; // Flag captures in progress or soon

					// only do it the first time
					if(shutdownBlockReasonSet == 0)
					{
						SBRC(hiddenWindow, L"TV Scheduler Pro capture in progress or will be very soon.");

						// for vista set thread state
						SetThreadExecutionState(ES_CONTINUOUS | ES_SYSTEM_REQUIRED | 0x00000040);//ES_AWAYMODE_REQUIRED);
						shutdownBlockReasonSet = 1;
					}
				}
				// else destroy the BlockReason and reset thread state
				else if(shutdownBlockReasonSet == 1)
				{
					// only destroy it once
					SBRD(hiddenWindow);

					// for vista set thread state
					SetThreadExecutionState(ES_CONTINUOUS);
					shutdownBlockReasonSet = 0;
				}
				else
					captureSoon = FALSE; // no captures now or next 10min
			}
			break;
		}

		default:
		{
			if(message == s_uTaskbarRestart)
				addTrayIcon();

			return DefWindowProc(hWnd, message, wParam, lParam);
		}
   }

   return 0;
}

HWND InitInstance(HINSTANCE hInstance, int nCmdShow)
{
   HWND hWnd;

   hWnd = CreateWindow("WSTrayIcon", "WS Tray Icon", 0,
      0, 0, 0, 0, NULL, NULL, hInstance, NULL);

   return hWnd;
}

ATOM MyRegisterClass(HINSTANCE hInstance)
{
	WNDCLASSEX wcex;

	wcex.cbSize = sizeof(WNDCLASSEX);

	wcex.style			= CS_HREDRAW | CS_VREDRAW;
	wcex.lpfnWndProc	= (WNDPROC)WndProc;
	wcex.cbClsExtra		= 0;
	wcex.cbWndExtra		= 0;
	wcex.hInstance		= hInstance;
	wcex.hIcon			= NULL;
	wcex.hCursor		= NULL;
	wcex.hbrBackground	= (HBRUSH)(COLOR_WINDOW+1);
	wcex.lpszMenuName	= NULL;
	wcex.lpszClassName	= "WSTrayIcon";
	wcex.hIconSm		= NULL;

	return RegisterClassEx(&wcex);
}

int APIENTRY WinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPSTR     lpCmdLine,
                     int       nCmdShow)
{
	// load user32DLL
	user32DLL = LoadLibrary("User32.dll");
	if(user32DLL != NULL)
	{
		SBRC = (ShutdownBlockReasonCreateEX)GetProcAddress(user32DLL, "ShutdownBlockReasonCreate");
		SBRD = (ShutdownBlockReasonDestroyEX)GetProcAddress(user32DLL, "ShutdownBlockReasonDestroy");
	}

	MSG msg;

	int count = getActiveCount();

	std::string commandLine = std::string(lpCmdLine);

	// register is Run key if needed
	if(commandLine.compare("-register") == 0)
	{
		char strPathName[_MAX_PATH];
		GetModuleFileName(NULL, strPathName, _MAX_PATH);

		CRegStore *reg = new CRegStore(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run");
		reg->setString("wstray", strPathName);
		delete reg;
		return 0;
	}
	else if(commandLine.compare("-unregister") == 0)
	{
		CRegStore *reg = new CRegStore(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run");
		reg->deleteKey("wstray");
		delete reg;
		return 0;
	}
	else if(commandLine.compare("-open_home") == 0)
	{
		if(port > -1)
		{
			openBrowser("");
		}
		else
		{
			MessageBox(NULL, "Service Not Running", "Not Running", MB_OK);
		}
		return 0;
	}
	else if(commandLine.compare("-open_config") == 0)
	{
		if(port > -1)
		{
			openBrowser("/config.html");
		}
		else
		{
			MessageBox(NULL, "Service Not Running", "Not Running", MB_OK);
		}
		return 0;
	}

	CRegStore *store = new CRegStore(HKEY_CURRENT_USER, "Software\\TVSchedulerPro\\Tray");
	powerOverride = store->getInt("powerOverride", 1);
	forceSleep = store->getInt("forceSleep", 0);
	delete store;

	appInstance = hInstance;
	MyRegisterClass(hInstance);

	hiddenWindow = InitInstance (hInstance, 1);

	if (hiddenWindow == 0)
	{
		return FALSE;
	}

	waitForTrayWnd();

	addTrayIcon();

	SetTimer(hiddenWindow, 10, 10000, NULL);

	while (GetMessage(&msg, NULL, 0, 0))
	{
		if(!IsDialogMessage(statusDlg, &msg))
		{
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
	}

	closeSharedMemory();

	return 0;
}






