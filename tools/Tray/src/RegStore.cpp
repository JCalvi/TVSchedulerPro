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

// RegStore.cpp: implementation of the CRegStore class.
//
//////////////////////////////////////////////////////////////////////

#include "RegStore.h"
#pragma warning(disable : 4267) //For the size_t conversion to DWORD


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CRegStore::CRegStore(HKEY root, char *base)
{
	LONG resp = 0;
	DWORD action_result = 0;

	resp = RegCreateKeyEx(	root, //HKEY_CURRENT_USER, //HKEY_LOCAL_MACHINE,
							base,
							NULL,
							NULL,
							REG_OPTION_NON_VOLATILE,
							KEY_ALL_ACCESS,
							NULL,
							&rootkey,
							&action_result);

}

BOOL CRegStore::deleteKey(char *name)
{
	LONG result = RegDeleteValue(rootkey, name);
	return result == ERROR_SUCCESS;
}

BOOL CRegStore::setString(char *name, char *value)
{
	size_t len = strlen(value) +1;

	LONG result = RegSetValueEx(rootkey, name, NULL, REG_SZ, (BYTE*)value, len);

	return result == ERROR_SUCCESS;
}

BOOL CRegStore::setInt(char *name, int val)
{
	LONG result = RegSetValueEx(rootkey, name, NULL, REG_DWORD, (BYTE*)&val, 4);
  
	return result == ERROR_SUCCESS;
}

int CRegStore::getInt(char *name, int def)
{
	int val = 0;
	DWORD datalen = 4;
	DWORD type = 0;

	LONG resp = RegQueryValueEx(rootkey, name, NULL, &type, (BYTE*)&val, &datalen);

	if(resp == 2)
	{
		val = def;
		RegSetValueEx(rootkey, name, NULL, REG_DWORD, (BYTE*)&val, 4);
	}

	return val;
}

CRegStore::~CRegStore()
{
	RegCloseKey(rootkey);
}

