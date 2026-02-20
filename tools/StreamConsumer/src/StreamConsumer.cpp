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
#include <stdio.h>
#include <tchar.h>
#include <sstream>
#include <atlbase.h>

#include "CDataConsumer.h"
#include "CDataConsumerTest.h"

#include "log.h"
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

bool isCyberMuxAvailable()
{
	bool found = false;

	CComPtr <IBaseFilter> m_pMpgMux;

	CComBSTR bstrTemp ("{6770E328-9B73-40C5-91E6-E2F321AEDE57}");
	CLSID muxCID;
	HRESULT hr = CLSIDFromString(bstrTemp, &muxCID);

	hr = m_pMpgMux.CoCreateInstance(muxCID, NULL, CLSCTX_INPROC_SERVER);

	/*
    hr = CoCreateInstance(
                        muxCID,
                        NULL,
                        CLSCTX_INPROC_SERVER,
                        IID_IBaseFilter,
                        reinterpret_cast<void**>(&m_pMpgMux)
                        );
						*/

	if(hr == S_OK)
		return true;
	else
		return false;
}

void printCapabilities()
{
	CoInitialize(NULL);

	std::string capabilities = "";

	capabilities.append("<capabilities>\r\n");

	capabilities.append("   <capability>\r\n");
	capabilities.append("      <name>TS-Full</name>\r\n");
	capabilities.append("      <id>0</id>\r\n");
	capabilities.append("      <ext>ts</ext>\r\n");
	capabilities.append("   </capability>\r\n");

	capabilities.append("   <capability>\r\n");
	capabilities.append("      <name>DVR-MS</name>\r\n");
	capabilities.append("      <id>1</id>\r\n");
	capabilities.append("      <ext>dvr-ms</ext>\r\n");
	capabilities.append("   </capability>\r\n");

	capabilities.append("   <capability>\r\n");
	capabilities.append("      <name>TS-Mux</name>\r\n");
	capabilities.append("      <id>2</id>\r\n");
	capabilities.append("      <ext>ts</ext>\r\n");
	capabilities.append("   </capability>\r\n");

	if(isCyberMuxAvailable())
	{
		capabilities.append("   <capability>\r\n");
		capabilities.append("      <name>Cyberlink MpgMux</name>\r\n");
		capabilities.append("      <id>3</id>\r\n");
		capabilities.append("      <ext>mpg</ext>\r\n");
		capabilities.append("   </capability>\r\n");
	}

	capabilities.append("</capabilities>\r\n");

	printf(capabilities.c_str());
	fflush(stdout);

	CoUninitialize();
}

int _tmain(int argc, _TCHAR* argv[])
{
	BOOL testMode = FALSE;

	SetUnhandledExceptionFilter(MyUnHandleExceptionFilter);

	CRegStore *store = new CRegStore("SOFTWARE\\TVSchedulerPro\\StreamConsumer");
	int useHighPriority = store->getInt("HighPriority", 1);
	delete store;

	// what mode are we running in
	if(argc > 1 && strcmp(argv[1], "-capabilities") == 0)
	{
		printCapabilities();
		return 0;
	}
	else if(argc > 1 && strcmp(argv[1], "-test") == 0)
	{
		// are we in test mode
		testMode = TRUE;
	}
	else if(argc < 8)
	{
		printf("Arg list not correct: StreamConsumer.exe <share_name> <progPID> <videoPID> <audioPID> <audioTYPE> <captureTYPE> \"<filename>\"\r\n");
		closeLogFile();
		return -1;
	}

	openLogFile("Capture-");

	if(testMode == TRUE)
	{
		log("Running in TEST mode\r\n");
	}

	std::string verinfo = getVersionInfo();
	log("StreamConsumer Version : %s\r\n", verinfo.c_str());

	///////////////////////////////////////////////////////////////////////////////
	// Get all the command line parameters
	///////////////////////////////////////////////////////////////////////////////

	// params
	std::string shareName = "";
	std::string fileName = "";
	int progPID = 0;
	int videoPID = 0;
	int audioPID = 0;
	int audioTYPE = 0;
	int captureTYPE = 0;

	if(testMode == FALSE)
	{
		// get share name
		shareName = std::string((char*)(argv[1]));
		// get progPID
		((std::istringstream)((std::string)(char*)(argv[2]))) >> progPID;
		// get videoPID
		((std::istringstream)((std::string)(char*)(argv[3]))) >> videoPID;
		// get audioPID
		((std::istringstream)((std::string)(char*)(argv[4]))) >> audioPID;
		// get audioTYPE
		((std::istringstream)((std::string)(char*)(argv[5]))) >> audioTYPE;
		// get Capture Type
		((std::istringstream)((std::string)(char*)(argv[6]))) >> captureTYPE;
		// get file name
		fileName = std::string((char*)(argv[7]));
	}

	log("Memory Share: %s\r\n", shareName.c_str());
	log("Program PID : %d\r\n", progPID);
	log("Video PID   : %d\r\n", videoPID);
	log("Audio PID   : %d\r\n", audioPID);
	log("Audio TYPE  : %d\r\n", audioTYPE);
	log("Audio TYPE  : %d\r\n", audioTYPE);
	log("Capture TYPE: %d\r\n", captureTYPE);
	log("File Name   : %s\r\n", fileName.c_str());

	// check the options
	if(captureTYPE < 0 || captureTYPE > 3)
	{
		log("Capture type not correct (%d)\r\n", captureTYPE);
		printf("Capture type not correct (%d)\r\n", captureTYPE);
		closeLogFile();
		return -2;
	}

	///////////////////////////////////////////////////////////////////////////////
	// create named exit event and start monitoring parent
	///////////////////////////////////////////////////////////////////////////////

	HANDLE closeEvent = createNamedEvent();

	if(closeEvent == NULL)
	{
		log("Named Event Creation Failed Exiting.\r\n");
		printf("Named Event Creation Failed Exiting.\r\n");
		closeLogFile();
		return -3;
	}

	// start the parent process watcher
	startWatcherThread(closeEvent);

	///////////////////////////////////////////////////////////////////////////////
	// Do capture stuff
	///////////////////////////////////////////////////////////////////////////////

	CoInitialize(NULL);

	CDataConsumerParent *consumer = NULL;

	if(testMode == TRUE)
	{
		consumer = new CDataConsumerTest();
	}
	else
	{
		consumer = new CDataConsumer();
	}

	consumer->setmemoryShareName(shareName);
	consumer->setPIDs(progPID, videoPID, audioPID, audioTYPE);
	consumer->setCaptureType(captureTYPE);
	consumer->setFileName(fileName);

	HRESULT hr = S_OK;

	hr = consumer->buildCaptureGraph();
	if(FAILED(hr))
	{
		log("Build Capture Graph Failed (0x%x)\r\n", hr);
		logErrorMessage(hr);
		closeLogFile();
		delete consumer;
		return -4;
	}

	log("Capture Graph Built\r\n");
	printf("GRAPH_BUILT\r\n");
	fflush(stdout);

	int runResult = 0;
	hr = consumer->runGraph(&runResult);
	if(FAILED(hr))
	{
		log("Run Graph Failed (0x%x)\r\n", hr);
		logErrorMessage(hr);
		closeLogFile();
		delete consumer;

		if(runResult == -4)
			return -6;
		else
			return -5;
	}

	log("Capture Graph Running\r\n");
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

	long quality = -5;
	long strength = -5;
	int loops = 0;

	int returnCode = 0;

	// now wait for stop event
	log("Waiting for Close Event\r\n");
	DWORD waitResult = WaitForSingleObject(closeEvent, 10000);
	while(waitResult == WAIT_TIMEOUT)
	{
		BOOL isDataFlowing = consumer->isDataFlowing();
		if(isDataFlowing == FALSE)
		{
			returnCode = -7;
			log("Data not flowing\r\n");
			break;
		}

		if(consumer->hasFallenBehind())
		{
			log("WARNING:Consumer has fallen behind the producer, Data Lost!\r\n");
			printf("WARNING:Consumer has fallen behind the producer, Data Lost!\r\n");
			fflush(stdout);
		}

		if(loops++ == 3)
		{
			consumer->getSignalStats(&quality, &strength);
			printf("SIGNAL_DATA:1,%d,%d\r\n", strength, quality);
			fflush(stdout);
			log("SIGNAL_DATA:L=1,S=%d,Q=%d\r\n", strength, quality);
			loops = 0;
		}

		waitResult = WaitForSingleObject(closeEvent, 10000);
	}

	if(waitResult != WAIT_TIMEOUT)
		log("Exit event triggered.\r\n");

	CloseHandle(closeEvent);

	// stop and exit

	consumer->stopGraph();
	printf("GRAPH_STOPPED\r\n");
	fflush(stdout);

	delete consumer;

	log("System exiting.\r\n");

	closeLogFile();

	printf("SYSTEM_EXITING\r\n");
	fflush(stdout);

	return returnCode;
}

