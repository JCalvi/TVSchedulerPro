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


#include "windows.h"

#include "CDataProducer.h"
#include "CDataProducerTest.h"

#include <sstream>
#include "watcher.h"
#include "CrashDump.h"
#include "RegStore.h"
#include "verInfo.h"

HANDLE createNamedEvent()
{
	SECURITY_ATTRIBUTES SA_ShMem;
	PSECURITY_DESCRIPTOR pSD_ShMem;

	pSD_ShMem = (PSECURITY_DESCRIPTOR)LocalAlloc(LPTR, SECURITY_DESCRIPTOR_MIN_LENGTH);

	if (pSD_ShMem == NULL)
	{
		log("createNamedEvent() : LocalAlloc Failed\r\n");
		return NULL;
	}
	if (!InitializeSecurityDescriptor(pSD_ShMem, SECURITY_DESCRIPTOR_REVISION))
	{
		log("createNamedEvent() : InitializeSecurityDescriptor Failed\r\n");
		return NULL;
	}
	if (!SetSecurityDescriptorDacl(pSD_ShMem, TRUE, (PACL)NULL, FALSE))
	{
		log("createNamedEvent() : SetSecurityDescriptorDacl Failed\r\n");
		return NULL;
	}

	SA_ShMem.nLength = sizeof(SA_ShMem);
	SA_ShMem.lpSecurityDescriptor = pSD_ShMem;
	SA_ShMem.bInheritHandle = TRUE;

	char buff[256];
	memset(buff, 22, 256);
	StringCchPrintf(buff, 255, "Global\\$ExitEvent(%x)$", (DWORD)GetCurrentProcessId());

	log("STOP_EVENT:%s\r\n", buff);
	printf("STOP_EVENT:%s\r\n", buff);
	fflush(stdout);

	HANDLE closeEvent = CreateEvent(&SA_ShMem, TRUE, FALSE, buff);
	if(closeEvent == NULL)
	{
		log("createNamedEvent() : Error, CreateEvent Failed (%d)\r\n", GetLastError());
	}

	log("createNamedEvent() : Resetting Event\r\n");
	ResetEvent(closeEvent);

	if(GetLastError() == ERROR_ALREADY_EXISTS)
		log("createNamedEvent() : WARNING, event already exists!\r\n");

	return closeEvent;
}

std::string getMemShareName()
{
	char buff[256];
	memset(buff, 22, 256);
	StringCchPrintf(buff, 255, "Global\\$MemoryShare(%x)$", (DWORD)GetCurrentProcessId());

	log("SHARE_NAME:%s\r\n", buff);
	printf("SHARE_NAME:%s\r\n", buff);
	fflush(stdout);

	return std::string(buff);
}

int _tmain(int argc, _TCHAR* argv[])
{
	BOOL testMode = FALSE;

	SetUnhandledExceptionFilter(MyUnHandleExceptionFilter);

	CRegStore *store = new CRegStore("SOFTWARE\\TVSchedulerPro\\StreamProducer");
	int useHighPriority = store->getInt("HighPriority", 1);
	delete store;

	openLogFile("Capture-");

	std::string verinfo = getVersionInfo();
	log("StreamProducer Version : %s\r\n", verinfo.c_str());

	///////////////////////////////////////////////////////////////////////////////
	// Get all the command line parameters
	///////////////////////////////////////////////////////////////////////////////
	if(argc > 1 && strcmp(argv[1], "-test") == 0)
	{
		// are we in test mode
		testMode = TRUE;
		log("Running in TEST mode\r\n");
	}
	else if(argc < 4)
	{
		log("Arg list not correct\r\n");
		printf("Arg list not correct\r\n");
		closeLogFile();
		return -1;
	}

	int frequency = 0;
	int bandwidth = -1;
	std::string deviceID = "";

	// get freq

	if(testMode == FALSE)
	{
		((std::istringstream)((std::string)(char*)(argv[1]))) >> frequency;
		((std::istringstream)((std::string)(char*)(argv[2]))) >> bandwidth;
		deviceID = std::string((char*)(argv[3]));
	}

	log("Frequency   : %d\r\n", frequency);
	log("Bandwidth   : %d\r\n", bandwidth);
	log("Device ID   : %d\r\n", deviceID.c_str());

	///////////////////////////////////////////////////////////////////////////////
	// create named exit event and start monitoring parent
	///////////////////////////////////////////////////////////////////////////////

	HANDLE closeEvent = createNamedEvent();

	if(closeEvent == NULL)
	{
		log("Named Event Creation Failed Exiting.\r\n");
		printf("Named Event Creation Failed Exiting.\r\n");
		closeLogFile();
		return -1;
	}

	// start the parent process watcher
	startWatcherThread(closeEvent);

	///////////////////////////////////////////////////////////////////////////////
	// Now set up the memory sink graph
	///////////////////////////////////////////////////////////////////////////////

	CoInitialize(NULL);

	CDataProducerParent *producer = NULL;

	if(testMode == TRUE)
	{
		producer = new CDataProducerTest();
	}
	else
	{
		producer = new CDataProducer();
	}

	producer->setDevice(deviceID);
	producer->setTuneData(frequency, bandwidth);
	producer->setMemoryShareName(getMemShareName());

	HRESULT hr = producer->buildGraph();

	if(FAILED(hr))
	{
		log("Building capture graph failed with (0x%x)\r\n", hr);
		printf("Building capture graph failed with (0x%x)\r\n", hr);
		fflush(stdout);
		delete producer;
		closeLogFile();
		ExitProcess(-3);
		return -3;
	}

	printf("GRAPH_BUILT\r\n");
	fflush(stdout);

	hr = producer->runGraph();
	if(FAILED(hr))
	{
		log("RunGraph failed (0x%x)\r\n", hr);
		delete producer;
		closeLogFile();
		return -4;
	}

	printf("GRAPH_RUNNING\r\n");
	fflush(stdout);

	// Set the thread state to keep the computer awake
	SetThreadExecutionState(ES_CONTINUOUS | ES_SYSTEM_REQUIRED);
	log("SetThreadExecutionState(ES_CONTINUOUS | ES_SYSTEM_REQUIRED)\r\n");

	if(useHighPriority == 1)
	{
		// Set priority to HIGH
		SetPriorityClass(GetCurrentProcess(), HIGH_PRIORITY_CLASS);
		log("SetPriorityClass() to HIGH_PRIORITY_CLASS\r\n");
	}

	long quality = -1;
	long strength = -1;
	BOOLEAN locked = FALSE;

	int returnCode = 0;

	DWORD waitResult = WaitForSingleObject(closeEvent, 10000);
	while(waitResult == WAIT_TIMEOUT)
	{
		BOOLEAN isDataFlowing = producer->isDataFlowing();
		hr = producer->logSignalValues();
		if(isDataFlowing == FALSE)
		{
			returnCode = -7;
			log("Data not flowing\r\n");
			break;
		}

		waitResult = WaitForSingleObject(closeEvent, 10000);
	}

	if(waitResult != WAIT_TIMEOUT)
		log("Exit event triggered.\r\n");

	CloseHandle(closeEvent);

	hr = producer->stopGraph();
	printf("GRAPH_STOPPED\r\n");
	fflush(stdout);
	delete producer;

	log("System exiting.\r\n");

	printf("SYSTEM_EXITING\r\n");
	fflush(stdout);

	closeLogFile();

	return returnCode;
}