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

// util.cpp : Defines the entry point for the DLL application.
//

#include <windows.h>
#include "util.h"
#include "log.h"
#include "TimerFunctions.h"
#include "DllWrapper.h"
#include "SharedMemory.h"
#include "kbLEDcontrol.h"
#pragma warning(disable : 4995) //For the _snprintf function, should replace with _snprintf_s

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
			{
				openLogFile("util-");
				log("Process Attached : util.dll\n");

				setupWakeupThread();

				int shVal = createSharedMemory();
				log("Creating Shared Memory (%d)\n", shVal);

				break;
			}
		case DLL_PROCESS_DETACH:
			{
				log("Closing Shared Memory\n");
				closeSharedMemory();

				log("Process Detached : util.dll\n");
				closeLogFile();
				
				break;
			}
		case DLL_THREAD_ATTACH:
			break;
		case DLL_THREAD_DETACH:
			break;
			
    }
    return TRUE;
}

JNIEXPORT jint JNICALL Java_DllWrapper_setWakeUpTime
  (JNIEnv *, jobject, jint year, jint month, jint day, jint hour, jint min, jint sec)
{
	// Set the time
	SYSTEMTIME st;
	st.wYear         = (WORD)year;	// Year
	st.wMonth        = (WORD)month;	// Month
	st.wDayOfWeek    = (WORD)0;		// Ignored
	st.wDay          = (WORD)day;	// The first of the month
	st.wHour         = (WORD)hour;	// 24Hour
	st.wMinute       = (WORD)min;	// minutes into the hour
	st.wSecond       = (WORD)sec;	// seconds into the minute
	st.wMilliseconds = (WORD)0;		// milliseconds into the second

	return setWakeupTime(st);
}

JNIEXPORT jint JNICALL Java_DllWrapper_setNextScheduleTime
  (JNIEnv *, jobject, jint year, jint month, jint day, jint hour, jint min, jint sec)
{
	// Set the time
	SYSTEMTIME st;
	st.wYear         = (WORD)year;	// Year
	st.wMonth        = (WORD)month;	// Month
	st.wDayOfWeek    = (WORD)0;		// Ignored
	st.wDay          = (WORD)day;		// The first of the month
	st.wHour         = (WORD)hour;	// 24Hour
	st.wMinute       = (WORD)min;		// minutes into the hour
	st.wSecond       = (WORD)sec;		// seconds into the minute
	st.wMilliseconds = (WORD)0;		// milliseconds into the second

	SHARED_DATA *data = getSharedData();
	if(data != NULL)
	{
		data->nextWake = st;
	}

	return 0;
}


JNIEXPORT jlong JNICALL Java_DllWrapper_getFreeSpace
  (JNIEnv *env, jobject, jstring loc)
{
	const char *locationFile = env->GetStringUTFChars(loc, NULL);
	
	char spaceIn[1024];
	StringCchCopy(spaceIn, 1024, locationFile);
	
	__int64 bytesFree = 0;
	
	try
	{
		GetDiskFreeSpaceEx(
			locationFile, 
			NULL,
			NULL,
			(PULARGE_INTEGER)&bytesFree);
	}
	catch(...)
	{
		bytesFree = -1;
	}
	
	env->ReleaseStringUTFChars(loc, locationFile);
	return (jlong)bytesFree;
}

JNIEXPORT void JNICALL Java_DllWrapper_setActiveCount
  (JNIEnv *env, jobject, jint amount)
{
	SHARED_DATA *data = getSharedData();
	if(data != NULL)
	{
		data->active = amount;
	}
}

JNIEXPORT void JNICALL Java_DllWrapper_setCurrentPort
  (JNIEnv *env, jobject, jint port)
{
	SHARED_DATA *data = getSharedData();
	if(data != NULL)
	{
		data->port = port;
	}
}

JNIEXPORT void JNICALL Java_DllWrapper_setKbLEDs
  (JNIEnv *env, jobject, jint value)
{
	setKBlights(value);
}

JNIEXPORT jint JNICALL Java_DllWrapper_setEvent
  (JNIEnv *env, jobject, jstring eventString)
{
	int returnCode = 0;

	const char *eventID = env->GetStringUTFChars(eventString, NULL);

	HANDLE closeEvent = OpenEvent(EVENT_MODIFY_STATE, FALSE, eventID);

	if(closeEvent == NULL)
	{
		returnCode = -1;
	}
	else
	{
		BOOL setResult = SetEvent(closeEvent);

		if(setResult == FALSE)
			returnCode = -2;
	}

	env->ReleaseStringUTFChars(eventString, eventID);

	return returnCode;
}

JNIEXPORT void JNICALL Java_DllWrapper_setNotification
  (JNIEnv *env, jobject, jstring notice)
{
	const char *noticeString = env->GetStringUTFChars(notice, NULL);

	SHARED_DATA *data = getSharedData();

	_snprintf(data->notice, 255, "%s", noticeString);

	GetLocalTime(&(data->noticeTime));

	env->ReleaseStringUTFChars(notice, noticeString);
}

