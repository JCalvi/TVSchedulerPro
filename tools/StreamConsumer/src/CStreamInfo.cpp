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


#include "CStreamInfo.h"

CStreamInfo::CStreamInfo()
{

}

CStreamInfo::~CStreamInfo()
{

}

void CStreamInfo::setType(TYPE tp)
{
	type = tp;
}

TYPE CStreamInfo::getType()
{
	return type;
}

bool CStreamInfo::isMediaType()
{
	if(type == TYPE_VIDEO || type == TYPE_AUDIO_MPG || type == TYPE_AUDIO_AC3)
		return true;
	else
		return false;
}

int CStreamInfo::getID()
{
	return id;
}

void CStreamInfo::setID(int pid)
{
	id = pid;
}

