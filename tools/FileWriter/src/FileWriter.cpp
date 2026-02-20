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
#include <commdlg.h>
#include <streams.h>
#include <initguid.h>
#include <strsafe.h>


#include "FileWriterUIDs.h"
#include "FileWriter.h"
#include "RegStore.h"

// Setup data

const AMOVIESETUP_MEDIATYPE sudPinTypes =
{
    &MEDIATYPE_NULL,            // Major type
    &MEDIASUBTYPE_NULL          // Minor type
};

const AMOVIESETUP_PIN sudPins =
{
    L"Input",                   // Pin string name
    FALSE,                      // Is it rendered
    FALSE,                      // Is it an output
    FALSE,                      // Allowed none
    FALSE,                      // Likewise many
    &CLSID_NULL,                // Connects to filter
    L"Output",                  // Connects to pin
    1,                          // Number of types
    &sudPinTypes                // Pin information
};

const AMOVIESETUP_FILTER sudDump =
{
    &CLSID_Dump,                // Filter CLSID
    L"(DWSP) FileWriter",       // String name
    MERIT_DO_NOT_USE,           // Filter merit
    1,                          // Number pins
    &sudPins                    // Pin details
};


//
//  Object creation stuff
//
CFactoryTemplate g_Templates[]= {
    L"(DWSP) FileWriter", &CLSID_Dump, CDump::CreateInstance, NULL, &sudDump
};
int g_cTemplates = 1;


// Constructor

CDumpFilter::CDumpFilter(CDump *pDump,
                         LPUNKNOWN pUnk,
                         CCritSec *pLock,
                         HRESULT *phr) :
    CBaseFilter(NAME("(DWSP) FileWriter"), pUnk, pLock, CLSID_Dump),
    m_pDump(pDump)
{
}


//
// GetPin
//
CBasePin * CDumpFilter::GetPin(int n)
{
    if (n == 0)
	{
        return m_pDump->m_pPin;
    }
	else
	{
        return NULL;
    }
}


//
// GetPinCount
//
int CDumpFilter::GetPinCount()
{
    return 1;
}


//
// Stop
//
// Overridden to close the dump file
//
STDMETHODIMP CDumpFilter::Stop()
{
    CAutoLock cObjectLock(m_pLock);

    if (m_pDump)
        m_pDump->CloseFile();
    
    return CBaseFilter::Stop();
}


//
// Pause
//
// Overridden to open the dump file
//
STDMETHODIMP CDumpFilter::Pause()
{
    CAutoLock cObjectLock(m_pLock);

    if (m_pDump)
    {
        m_pDump->OpenFile();
    }

    return CBaseFilter::Pause();
}


//
// Run
//
// Overridden to open the dump file
//
STDMETHODIMP CDumpFilter::Run(REFERENCE_TIME tStart)
{
    CAutoLock cObjectLock(m_pLock);

    if (m_pDump)
        m_pDump->OpenFile();

    return CBaseFilter::Run(tStart);
}

//
//  Definition of CDumpInputPin
//
CDumpInputPin::CDumpInputPin(CDump *pDump,
                             LPUNKNOWN pUnk,
                             CBaseFilter *pFilter,
                             CCritSec *pLock,
                             CCritSec *pReceiveLock,
                             HRESULT *phr) :

    CRenderedInputPin(NAME("CDumpInputPin"),
                  pFilter,                   // Filter
                  pLock,                     // Locking
                  phr,                       // Return code
                  L"Input"),                 // Pin name
    m_pReceiveLock(pReceiveLock),
    m_pDump(pDump),
    m_tLast(0)
{
}


//
// CheckMediaType
//
// Check if the pin can support this specific proposed type and format
//
HRESULT CDumpInputPin::CheckMediaType(const CMediaType *)
{
    return S_OK;
}


//
// BreakConnect
//
// Break a connection
//
HRESULT CDumpInputPin::BreakConnect()
{
    if (m_pDump->m_pPosition != NULL)
	{
        m_pDump->m_pPosition->ForceRefresh();
    }

    return CRenderedInputPin::BreakConnect();
}


//
// ReceiveCanBlock
//
// We don't hold up source threads on Receive
//
STDMETHODIMP CDumpInputPin::ReceiveCanBlock()
{
    return S_FALSE;
}


//
// Receive
//
// Do something with this media sample
//
STDMETHODIMP CDumpInputPin::Receive(IMediaSample *pSample)
{
    CheckPointer(pSample,E_POINTER);

    CAutoLock lock(m_pReceiveLock);
    PBYTE pbData;

    // Has the filter been stopped yet?
    if (m_pDump->m_hFile == INVALID_HANDLE_VALUE)
	{
        return NOERROR;
    }

    REFERENCE_TIME tStart, tStop;
    pSample->GetTime(&tStart, &tStop);

    m_tLast = tStart;

    // Copy the data to the file

    HRESULT hr = pSample->GetPointer(&pbData);
    if (FAILED(hr))
	{
        return hr;
    }

    return m_pDump->Write(pbData, pSample->GetActualDataLength());
}

//
// EndOfStream
//
STDMETHODIMP CDumpInputPin::EndOfStream(void)
{
    CAutoLock lock(m_pReceiveLock);
    return CRenderedInputPin::EndOfStream();

} // EndOfStream


//
// NewSegment
//
// Called when we are seeked
//
STDMETHODIMP CDumpInputPin::NewSegment(REFERENCE_TIME tStart,
                                       REFERENCE_TIME tStop,
                                       double dRate)
{
    m_tLast = 0;
    return S_OK;

} // NewSegment


//
//  CDump class
//
CDump::CDump(LPUNKNOWN pUnk, HRESULT *phr) :
    CUnknown(NAME("CDump"), pUnk),
    m_pFilter(NULL),
    m_pPin(NULL),
    m_pPosition(NULL),
    m_hFile(INVALID_HANDLE_VALUE),
    m_pFileName(0),
	currentPosition(0),
	currentFileLength(0),
	chunkReserve(1),
	currentEndPointer(1),
	fileBuffering(1),
	flushBeforeReserve(0),
	growBy(100000000),
	m_hLogFile(INVALID_HANDLE_VALUE),
	writeLogFile(FALSE)
{
    ASSERT(phr);
    
    m_pFilter = new CDumpFilter(this, GetOwner(), &m_Lock, phr);
    if (m_pFilter == NULL)
	{
        if (phr)
		{
            *phr = E_OUTOFMEMORY;
		}
        return;
    }

    m_pPin = new CDumpInputPin(this,GetOwner(),
                               m_pFilter,
                               &m_Lock,
                               &m_ReceiveLock,
                               phr);
    if (m_pPin == NULL)
	{
        if (phr)
		{
            *phr = E_OUTOFMEMORY;
		}
        return;
    }

	// get the overrides from the registry
	CRegStore *store = new CRegStore("SOFTWARE\\TVSchedulerPro\\FileWriter");

	chunkReserve = store->getInt("chunkReserve", 1);
	flushBeforeReserve = store->getInt("flushBeforeReserve", 0);
	currentEndPointer = store->getInt("currentEndPointer", 1);
	fileBuffering = store->getInt("fileBuffering", 1);
	growBy = store->getInt("growBy", 100000000);
	writeLogFile = store->getInt("writeLog", 0);

	delete store;
}


//
// SetFileName
//
// Implemented for IFileSinkFilter support
//
STDMETHODIMP CDump::SetFileName(LPCOLESTR pszFileName,const AM_MEDIA_TYPE *pmt)
{
    // Is this a valid filename supplied

    CheckPointer(pszFileName,E_POINTER);
    if(wcslen(pszFileName) > MAX_PATH)
	{
        return ERROR_FILENAME_EXCED_RANGE;
	}

    // Take a copy of the filename

    m_pFileName = new WCHAR[MAX_PATH];
    if (m_pFileName == 0)
	{
        return E_OUTOFMEMORY;
	}

	StringCchCopyW(m_pFileName, MAX_PATH, pszFileName);

    // Create the file then close it
    HRESULT hr = OpenFile();
    CloseFile();

    return hr;

} // SetFileName


//
// GetCurFile
//
// Implemented for IFileSinkFilter support
//
STDMETHODIMP CDump::GetCurFile(LPOLESTR * ppszFileName,AM_MEDIA_TYPE *pmt)
{
    CheckPointer(ppszFileName, E_POINTER);
    *ppszFileName = NULL;

    if (m_pFileName != NULL) 
    {
        *ppszFileName = (LPOLESTR)
			QzTaskMemAlloc(sizeof(WCHAR) * MAX_PATH);

        if (*ppszFileName != NULL) 
        {
			StringCchCopyW(*ppszFileName, MAX_PATH, m_pFileName);
        }
    }

    if(pmt) 
    {
        ZeroMemory(pmt, sizeof(*pmt));
        pmt->majortype = MEDIATYPE_NULL;
        pmt->subtype = MEDIASUBTYPE_NULL;
    }

    return S_OK;

} // GetCurFile


// Destructor

CDump::~CDump()
{
    CloseFile();

    delete m_pPin;
    delete m_pFilter;
    delete m_pPosition;
    delete m_pFileName;
}


//
// CreateInstance
//
// Provide the way for COM to create a dump filter
//
CUnknown * WINAPI CDump::CreateInstance(LPUNKNOWN punk, HRESULT *phr)
{
    ASSERT(phr);
    
    CDump *pNewObject = new CDump(punk, phr);
    if (pNewObject == NULL)
	{
        if (phr)
		{
            *phr = E_OUTOFMEMORY;
		}
    }

    return pNewObject;

} // CreateInstance


//
// NonDelegatingQueryInterface
//
// Override this to say what interfaces we support where
//
STDMETHODIMP CDump::NonDelegatingQueryInterface(REFIID riid, void ** ppv)
{
    CheckPointer(ppv, E_POINTER);
    CAutoLock lock(&m_Lock);

    // Do we have this interface

    if(riid == IID_IFileSinkFilter)
	{
        return GetInterface((IFileSinkFilter *) this, ppv);
    }
    else if(riid == IID_IBaseFilter || riid == IID_IMediaFilter || riid == IID_IPersist)
	{
        return m_pFilter->NonDelegatingQueryInterface(riid, ppv);
    } 
    else if(riid == IID_IMediaPosition || riid == IID_IMediaSeeking)
	{
        if (m_pPosition == NULL) 
        {

            HRESULT hr = S_OK;
            m_pPosition = new CPosPassThru(NAME("Dump Pass Through"),
                                           (IUnknown *) GetOwner(),
                                           (HRESULT *) &hr, m_pPin);
            if (m_pPosition == NULL) 
                return E_OUTOFMEMORY;

            if (FAILED(hr)) 
            {
                delete m_pPosition;
                m_pPosition = NULL;
                return hr;
            }
        }

        return m_pPosition->NonDelegatingQueryInterface(riid, ppv);
    } 

    return CUnknown::NonDelegatingQueryInterface(riid, ppv);

} // NonDelegatingQueryInterface


//
// OpenFile
//
// Opens the file ready for dumping
//
HRESULT CDump::OpenFile()
{
    char pFileName[MAX_PATH];

    // Is the file already opened
    if (m_hFile != INVALID_HANDLE_VALUE)
	{
        return NOERROR;
    }

    // Has a filename been set yet
    if (m_pFileName == NULL)
	{
        return ERROR_INVALID_NAME;
    }

    // Convert the UNICODE filename if necessary

    if(!WideCharToMultiByte(CP_ACP, 0, m_pFileName, -1, pFileName, MAX_PATH, 0, 0))
        return ERROR_INVALID_NAME;

	// work out the rile flags to use
	DWORD fileCreateFlags = 0;

	if(fileBuffering == 0)
		fileCreateFlags = fileCreateFlags | FILE_FLAG_WRITE_THROUGH;

    m_hFile = CreateFile((LPCTSTR) pFileName,   // The filename
                         GENERIC_WRITE,         // File access
                         FILE_SHARE_READ,       // Share access
                         NULL,                  // Security
                         CREATE_ALWAYS,         // Open flags
                         fileCreateFlags,			// More flags
                         NULL);                 // Template


    if (m_hFile == INVALID_HANDLE_VALUE) 
    {
        DWORD dwErr = GetLastError();
        return HRESULT_FROM_WIN32(dwErr);
    }

	if(currentEndPointer == 1 && chunkReserve == 1)
	{
		TCHAR infoFile[MAX_PATH];
		StringCchCopy(infoFile, MAX_PATH, pFileName);
		StringCchCat(infoFile, MAX_PATH, ".info");

		m_hInfoFile = CreateFile((LPCTSTR) infoFile,
			GENERIC_WRITE,
			FILE_SHARE_READ,
			NULL,
			CREATE_ALWAYS,
			fileCreateFlags,
			NULL);
	}

	if(writeLogFile)
	{
		TCHAR logFile[MAX_PATH];
		StringCchCopy(logFile, MAX_PATH, pFileName);
		StringCchCat(logFile, MAX_PATH, ".writer.log");

		m_hLogFile = CreateFile((LPCTSTR) logFile,
			GENERIC_WRITE,
			FILE_SHARE_READ,
			NULL,
			CREATE_ALWAYS,
			fileCreateFlags,
			NULL);

		WriteLogLine("Log file started\r\n");
	}

    return S_OK;

} // Open


//
// CloseFile
//
// Closes any dump file we have opened
//
HRESULT CDump::CloseFile()
{
    // Must lock this section to prevent problems related to
    // closing the file while still receiving data in Receive()
    CAutoLock lock(&m_Lock);

    if (m_hFile == INVALID_HANDLE_VALUE)
	{
        return NOERROR;
    }


	if(chunkReserve == 1)
	{
		LARGE_INTEGER li;
		li.QuadPart = currentPosition;

		SetFilePointer(
			m_hFile,
			li.LowPart,
			&li.HighPart,
			FILE_BEGIN);

		SetEndOfFile(m_hFile);

		currentPosition = 0;
		currentFileLength = 0;
	}


	CloseHandle(m_hFile);
	m_hFile = INVALID_HANDLE_VALUE; // Invalidate the file 

	if(writeLogFile)
	{
		CloseHandle(m_hLogFile);
	}

	if(currentEndPointer == 1 && chunkReserve == 1)
	{
		if (m_hInfoFile != INVALID_HANDLE_VALUE)
		{
			CloseHandle(m_hInfoFile);
			m_hInfoFile = INVALID_HANDLE_VALUE;
		}

		char pFileName[MAX_PATH];
		if(!WideCharToMultiByte(CP_ACP, 0, m_pFileName, -1, pFileName, MAX_PATH, 0, 0))
			return ERROR_INVALID_NAME;

		TCHAR infoFile[MAX_PATH];
		StringCchCopy(infoFile, MAX_PATH, pFileName);
		StringCchCat(infoFile, MAX_PATH, ".info");

		DeleteFile(infoFile);
	}

    return NOERROR;

} // Open

//
// Write
//
// Write raw data to the file
//
HRESULT CDump::Write(PBYTE pbData, LONG lDataLength)
{
    CAutoLock lock(&m_Lock);
	// If the file has already been closed, don't continue
    if (m_hFile == INVALID_HANDLE_VALUE)
	{
        return S_FALSE;
    }

	HRESULT hr = S_OK;
	LARGE_INTEGER li;

	// do the reserve chunk is needed

	if((lDataLength + currentPosition) > currentFileLength)
	{
		// increase the length of the file by growFile
		currentFileLength = currentFileLength + growBy;

		if(flushBeforeReserve == 1)
		{
			FlushFileBuffers(m_hFile);

			if(currentEndPointer == 1)
				FlushFileBuffers(m_hInfoFile);
		}

		if(chunkReserve == 1)
		{
			li.QuadPart = currentFileLength;

			SetFilePointer(
				m_hFile,
				li.LowPart,
				&li.HighPart,
				FILE_BEGIN);

			SetEndOfFile(m_hFile);

			li.QuadPart = currentPosition;

			SetFilePointer(
				m_hFile,
				li.LowPart,
				&li.HighPart,
				FILE_BEGIN);
		}
	}

	DWORD written = 0;
	WriteFile(m_hFile, pbData, lDataLength, &written, NULL);

	currentPosition = currentPosition + lDataLength;

	// only write to end pointer file if it is needed
	if(currentEndPointer == 1 && chunkReserve == 1)
	{
		li.QuadPart = 0;
		SetFilePointer(
				m_hInfoFile,
				li.LowPart,
				&li.HighPart,
				FILE_BEGIN);

		WriteFile(m_hInfoFile, &currentPosition, sizeof(__int64), &written, NULL);
	}

    return S_OK;
}

void CDump::WriteLogLine(TCHAR *sz, ...)
{
	if(writeLogFile)
	{
		DWORD written = 0;
		TCHAR buff[2000];
		va_list va;
		va_start(va, sz);
		StringCchVPrintf(buff, 2000, sz, va);
		WriteFile(m_hLogFile, buff, (DWORD)strlen(buff), &written, NULL);
	}
}

////////////////////////////////////////////////////////////////////////
//
// Exported entry points for registration and unregistration 
// (in this case they only call through to default implementations).
//
////////////////////////////////////////////////////////////////////////

//
// DllRegisterSever
//
// Handle the registration of this filter
//
STDAPI DllRegisterServer()
{
    return AMovieDllRegisterServer2( TRUE );

} // DllRegisterServer


//
// DllUnregisterServer
//
STDAPI DllUnregisterServer()
{
    return AMovieDllRegisterServer2( FALSE );

} // DllUnregisterServer


//
// DllEntryPoint
//
extern "C" BOOL WINAPI DllEntryPoint(HINSTANCE, ULONG, LPVOID);

BOOL APIENTRY DllMain(HANDLE hModule, 
                      DWORD  dwReason, 
                      LPVOID lpReserved)
{
	return DllEntryPoint((HINSTANCE)(hModule), dwReason, lpReserved);
}

