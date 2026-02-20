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

#include <windows.h>
#include <winioctl.h>

typedef struct _KEYBOARD_INDICATOR_PARAMETERS {
    USHORT UnitId;		// Unit identifier.
    USHORT LedFlags;	// LED indicator state.

} KEYBOARD_INDICATOR_PARAMETERS, *PKEYBOARD_INDICATOR_PARAMETERS;
#define IOCTL_KEYBOARD_SET_INDICATORS CTL_CODE(FILE_DEVICE_KEYBOARD, 0x0002, METHOD_BUFFERED, FILE_ANY_ACCESS)

HANDLE OpenKeyboardDevice(int *ErrorNumber)
{
    HANDLE  hnd;
    int     *LocalErrorNumber;
    int     Dummy;

    if (ErrorNumber == NULL)
        LocalErrorNumber = &Dummy;
    else
        LocalErrorNumber = ErrorNumber;

    *LocalErrorNumber = 0;

    if (!DefineDosDevice (DDD_RAW_TARGET_PATH, "Kbd",
                "\\Device\\KeyboardClass0"))
    {
        *LocalErrorNumber = GetLastError();
        return INVALID_HANDLE_VALUE;
    }

    hnd = CreateFile("\\\\.\\Kbd", GENERIC_WRITE, 0,
                NULL,   OPEN_EXISTING,  0,  NULL);

    if (hnd == INVALID_HANDLE_VALUE)
        *LocalErrorNumber = GetLastError();

    return hnd;
}

int CloseKeyboardDevice(HANDLE hnd)
{
    int e = 0;

    if (!DefineDosDevice (DDD_REMOVE_DEFINITION, "Kbd", NULL))
        e = GetLastError();

    if (!CloseHandle(hnd))
        e = GetLastError();

    return e;
}

void setKBlights(int value)
{
	HANDLE hnd = OpenKeyboardDevice(NULL);

	KEYBOARD_INDICATOR_PARAMETERS inputBuffer;
	ULONG dataLength = sizeof(KEYBOARD_INDICATOR_PARAMETERS);
	ULONG returnedLength;

	inputBuffer.LedFlags = 0;

	if(value == 1)
		inputBuffer.LedFlags = 1;
	else if(value == 2)
		inputBuffer.LedFlags = 3;
	else if(value >= 3)
		inputBuffer.LedFlags = 7;

	DeviceIoControl(hnd, IOCTL_KEYBOARD_SET_INDICATORS,
						&inputBuffer, dataLength,
						NULL, 0, &returnedLength, NULL);		

	CloseKeyboardDevice(hnd);
}
