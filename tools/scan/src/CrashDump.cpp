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

#include "CrashDump.h"
#include <stdio.h>
#include "shlobj.h"

LONG WINAPI MyUnHandleExceptionFilter(struct _EXCEPTION_POINTERS *lpExceptionInfo)
{
    BOOL bMiniDumpSuccessful;
    char szFileName[MAX_PATH]; 
    DWORD dwBufferSize = MAX_PATH;
    HANDLE hDumpFile;
    SYSTEMTIME stLocalTime;
    MINIDUMP_EXCEPTION_INFORMATION ExpParam;

	
	char allUserPath[MAX_PATH] = {0};
	HRESULT hr = SHGetFolderPath(NULL, CSIDL_COMMON_APPDATA, NULL, 0, allUserPath);
	_snprintf(szFileName, MAX_PATH, "%s\\TV Scheduler Pro\\log\\", allUserPath);

	SHCreateDirectoryEx(NULL, szFileName, NULL);

    GetLocalTime( &stLocalTime );

	
    _snprintf( szFileName, MAX_PATH,
		"%s\\TV Scheduler Pro\\log\\scan-crash-%04d%02d%02d-%02d%02d%02d-%ld-%ld.dmp", 
		allUserPath,
        stLocalTime.wYear, stLocalTime.wMonth, stLocalTime.wDay, 
        stLocalTime.wHour, stLocalTime.wMinute, stLocalTime.wSecond, 
        GetCurrentProcessId(), GetCurrentThreadId());

    hDumpFile = CreateFile(szFileName, GENERIC_READ|GENERIC_WRITE, 
                FILE_SHARE_WRITE|FILE_SHARE_READ, 0, CREATE_ALWAYS, 0, 0);

    ExpParam.ThreadId = GetCurrentThreadId();
    ExpParam.ExceptionPointers = lpExceptionInfo;
    ExpParam.ClientPointers = TRUE;

    bMiniDumpSuccessful = MiniDumpWriteDump(GetCurrentProcess(), GetCurrentProcessId(), 
                    hDumpFile, MiniDumpWithDataSegs, &ExpParam, NULL, NULL);

	return EXCEPTION_EXECUTE_HANDLER;
}