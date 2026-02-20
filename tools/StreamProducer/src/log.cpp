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

#include "log.h"
#include <stdio.h>
#include "shlobj.h"
#pragma warning(disable : 4995) //For the _snprintf function, should replace with _snprintf_s


static FILE *fp_log = 0;

void getAllUserPath(char *buff, int buffLen)
{
	char szPath[MAX_PATH] = {0};
	HRESULT hr = SHGetFolderPath(NULL, CSIDL_COMMON_APPDATA, NULL, 0, szPath);
	if (SUCCEEDED(hr))
	{
		_snprintf(buff, buffLen, "%s\\TV Scheduler Pro\\", szPath);
	}
	else
	{
		buff[0] = '0';
	}
}

/*void getAllUserPath2(char *buff, int buffLen)
{
	LPWSTR wszPath = NULL;
	HRESULT hr = SHGetKnownFolderPath(FOLDERID_ProgramData, KF_FLAG_CREATE, NULL, &wszPath);
	if (SUCCEEDED(hr))
	{
		_snprintf(buff, buffLen, "%s\\TV Scheduler Pro\\", wszPath);
	}
	else
	{
		buff[0] = '0';
	}
}*/

void openLogFile(char *logFile)
{
	char buff[MAX_PATH] = {0};
	getAllUserPath(buff, MAX_PATH);

	std::string logPath(buff);
	logPath.append("log");

	SHCreateDirectoryEx(NULL, logPath.c_str(), NULL);

    SYSTEMTIME st;
    GetLocalTime(&st);

	char name[256];
	StringCchPrintf(name, 256, "\\%s%.4d%.2d%.2d-%.2d%.2d%.2d-%.3d-Producer.log", logFile, st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond, st.wMilliseconds);

	logPath.append(name);

	fp_log = fopen(logPath.c_str(), "at");

	printf("LOG_FILE:%s\r\n", logPath.c_str());
	fflush(stdout);
}

void closeLogFile()
{
	fclose(fp_log);
}

int log(char *sz,...)
{
    char tach[2000];

    SYSTEMTIME st;
    GetLocalTime(&st);

	char timeBuff[60];
	StringCchPrintf(timeBuff, 60, "%.4d/%.2d/%.2d %.2d:%.2d:%.2d.%.3d - ", st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond, st.wMilliseconds);

    va_list va;
    va_start(va, sz);
	StringCchVPrintf(tach, 2000, sz, va);
    va_end(va);

	if(fp_log != NULL)
	{
		fputs(timeBuff, fp_log);
		fputs(tach, fp_log);
		fflush(fp_log);
	}
	else
	{
		printf(timeBuff);
		printf(tach);
		fflush(stdout);
	}

    return FALSE;
}