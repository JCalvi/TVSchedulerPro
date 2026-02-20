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

#include <string>

#include "watcher.h"
#include <process.h>
#include <tlhelp32.h>
#include "log.h"

HANDLE parent = NULL;
HANDLE exitEvent = NULL;

DWORD getParentID()
{
	DWORD parentID = 0;
	DWORD myID = GetCurrentProcessId();
	HANDLE helper = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	if(helper != INVALID_HANDLE_VALUE)
	{
		PROCESSENTRY32 pe32;
		pe32.dwSize = sizeof(PROCESSENTRY32);

		if(Process32First(helper, &pe32))
		{
			do
			{
				if(myID == pe32.th32ProcessID)
				{
					parentID = pe32.th32ParentProcessID;
					//printf( "Process %d : Parent : %d\r\n", pe32.th32ProcessID, pe32.th32ParentProcessID );
					log("Process %d : Parent : %d\r\n", pe32.th32ProcessID, pe32.th32ParentProcessID);
					break;
				}
			}
			while(Process32Next(helper, &pe32));
		}
		CloseHandle( helper );
	}

	return parentID;
}

unsigned __stdcall watcherThread( void* pArguments )
{
	if(parent != 0)
	{
		WaitForSingleObject(parent, INFINITE);
		CloseHandle(parent);
		log("WARNING: parent process has closed so Exiting NOW!\r\n");
		SetEvent(exitEvent);
	}

	return 0;
}

void startWatcherThread(HANDLE exit)
{
	DWORD parentID = getParentID();
	parent = OpenProcess(SYNCHRONIZE, FALSE, parentID);
	exitEvent = exit;

	if(parent != 0)
	{
		unsigned threadID;
		HANDLE hThread = (HANDLE)_beginthreadex( NULL, 0, &watcherThread, NULL, 0, &threadID );
		CloseHandle(hThread);
	}
}
