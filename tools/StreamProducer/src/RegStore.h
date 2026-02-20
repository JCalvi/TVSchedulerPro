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

// RegStore.h: interface for the CRegStore class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_REGSTORE_H__73FB5892_2012_4F2F_831C_C10AD6E7C5B5__INCLUDED_)
#define AFX_REGSTORE_H__73FB5892_2012_4F2F_831C_C10AD6E7C5B5__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Windows.h"

class CRegStore  
{
public:
	CRegStore(char *base);
	virtual ~CRegStore();

	int getInt(char *name, int def);
	BOOL setInt(char *name, int val);

private:
	HKEY rootkey;
};

#endif // !defined(AFX_REGSTORE_H__73FB5892_2012_4F2F_831C_C10AD6E7C5B5__INCLUDED_)
