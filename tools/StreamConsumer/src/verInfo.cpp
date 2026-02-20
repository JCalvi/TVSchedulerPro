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

#include "verInfo.h"

std::string getVersionInfo()
{
	char versionTEXT[256] = {0};
	char fileName[_MAX_PATH+1];
	GetModuleFileName(NULL, fileName, _MAX_PATH);

	DWORD dwVerHnd;
	DWORD dwVerInfoSize;
	int ret = 0;

	dwVerInfoSize = GetFileVersionInfoSize(fileName, &dwVerHnd);

	if (dwVerInfoSize)
	{
		// If we were able to get the information, process it:
		HANDLE hMem;
		LPVOID lpvMem;
		BOOL fRet;
		UINT cchVer = 0;
		VS_FIXEDFILEINFO *vInfo;
		
		hMem = GlobalAlloc(GMEM_MOVEABLE, dwVerInfoSize);
		lpvMem = GlobalLock(hMem);

		GetFileVersionInfo(fileName, NULL, dwVerInfoSize, lpvMem);

		fRet = VerQueryValue(lpvMem, TEXT("\\"), (LPVOID*)&vInfo, &cchVer);

		if (fRet && cchVer && vInfo)
		{
            WORD maj = HIWORD(vInfo->dwFileVersionMS);
			WORD min = LOWORD(vInfo->dwFileVersionMS);
			WORD rev = HIWORD(vInfo->dwFileVersionLS);
			WORD bld = LOWORD(vInfo->dwFileVersionLS);
			ret = bld;

			StringCchPrintf(versionTEXT, 256, TEXT("%d.%d.%d.%d"), maj, min, rev, bld);
		}

		GlobalUnlock(hMem);
		GlobalFree(hMem);
	}
	else
	{
		ret = 0;
		StringCchPrintf(versionTEXT, 256, TEXT("N/A"));
	}

	return std::string(versionTEXT);
}