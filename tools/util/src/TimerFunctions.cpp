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

#include <Windows.h>
#include <stdio.h>
#include <process.h>
#include "TimerFunctions.h"
#include "log.h"

HANDLE hTimer = NULL;

int setWakeupTime(SYSTEMTIME st)
{
	log("Setting Wake up Time (y%d m%d d%d h%d m%d s%d)\n", st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);

	FILETIME ft;
	FILETIME ftUTC;
	LARGE_INTEGER liUTC;

	SystemTimeToFileTime(&st, &ft);

	// Convert local time to UTC time.
	LocalFileTimeToFileTime(&ft, &ftUTC);
	// Convert FILETIME to LARGE_INTEGER because of different alignment.
	liUTC.LowPart  = ftUTC.dwLowDateTime;
	liUTC.HighPart = ftUTC.dwHighDateTime;

	SetLastError(0);

	BOOL result = SetWaitableTimer(hTimer, &liUTC, 0, NULL, NULL, TRUE);

	DWORD lastError = GetLastError();

	if(result)
	{
		log("SetWaitableTimer Worked\n");
	}
	else
	{
		log("SetWaitableTimer Failed!, GetLastError=%d\n", lastError);
	}

	if(result == FALSE)
	{
		return -1;
	}
	else if(lastError == ERROR_NOT_SUPPORTED)
	{
		log("SetWaitableTimer Not Supported\n");
		return -2;
	}
	else
	{
		return 0;
	}
}

void setupWakeupThread()
{
	hTimer = (HANDLE)CreateWaitableTimer(NULL, FALSE, "TV Scheduler Pro Wake Up Timer");
	log("Waitable Timer ID %x\n", hTimer);

	unsigned int threadId = 0;
	HANDLE hThread = (HANDLE)_beginthreadex(NULL, 0, &wakeupWatchThread, &hTimer, NULL, &threadId);
	CloseHandle( hThread );
}

unsigned int __stdcall wakeupWatchThread(void* lpParameter)
{
	log("Wake up Thread Started\n");

	HANDLE *timer = (HANDLE*)lpParameter;

	unsigned long waitResult = 0;

	while(true)
	{
		waitResult = WaitForSingleObject(*timer, INFINITE);

		if(waitResult == WAIT_ABANDONED)
			log("EVENT: Waiting Thread WAIT_ABANDONED\n");
		else if(waitResult == WAIT_OBJECT_0)
			log("EVENT: Waiting Thread WAIT_OBJECT_0\n");
		else if(waitResult == WAIT_TIMEOUT)
			log("EVENT: Waiting Thread WAIT_TIMEOUT\n");
		else
			log("EVENT: Waiting Thread, unknown result (%d)\n", waitResult);

		Sleep(5000);
	}

	return 0;
}
