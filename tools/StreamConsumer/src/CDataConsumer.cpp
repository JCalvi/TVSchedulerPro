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

#include "CDataConsumer.h"
#include "shlobj.h"


// Define the Sections and Tables filter ID's
const CLSID CLSID_Mpeg2Data = {0xC666E115, 0xBB62, 0x4027, 0xA1, 0x13, 0x82, 0xD6, 0x43, 0xFE, 0x2D, 0x99};
const IID IID_IMpeg2Data = {0x9B396D40, 0xF380, 0x4e3c, 0xA5, 0x14, 0x1A, 0x82, 0xBF, 0x6E, 0xBF, 0xE6};


CDataConsumer::CDataConsumer(void) :
		m_dwGraphRegister(0),
		m_fallBehindCount(0)
{
}

CDataConsumer::~CDataConsumer(void)
{
}

void CDataConsumer::setmemoryShareName(std::string name)
{
	memoryShareName = name;
}

void CDataConsumer::setPIDs(int prog, int video, int audio, int audio_type)
{
	progamPID = prog;
	videoPID = video;
	audioPID = audio;
	audioTYPE = audio_type;
}

void CDataConsumer::setCaptureType(int type)
{
	captureType = type;
}

void CDataConsumer::setFileName(std::string name)
{
	fileName = name;
}

BOOLEAN CDataConsumer::hasFallenBehind()
{
	if(m_pSourceOptions == NULL)
	{
		log("hasFallenBehind(): m_pSourceOptions is NULL");
		return FALSE;
	}

	int count = 0;
	m_pSourceOptions->get_FallBehindCount(&count);

	if(count == m_fallBehindCount)
	{
		return FALSE;
	}
	else
	{
		m_fallBehindCount = count;
		log("Fall Behind Count : %d\r\n", count);
		return TRUE;
	}
}

HRESULT CDataConsumer::buildCaptureGraph()
{
	HRESULT hr = S_OK;

	hr = initializeGraphBuilder();
    if (FAILED(hr))
    {
        log("Could Not Initialise Graph (0x%x)\r\n", hr);
        return hr;
    }

	hr = addMemorySource();
    if (FAILED(hr))
    {
        log("Could Not add Memory Source Filter (0x%x)\r\n", hr);
        return hr;
    }

	// set up for this capture type
	if(captureType == 0)
	{
		log("Setting up for full TS capture.\r\n");
		hr = setupTSFullCapture();
	}
	else if(captureType == 1)
	{
		log("Setting up for DVR-MS capture.\r\n");
		hr = setupDVRMSCapture();
	}
	else if(captureType == 2)
	{
		log("Setting up for TS Mux capture.\r\n");
		hr = setupTSMuxCapture();
	}
	else if(captureType == 3)
	{
		log("Setting up for Cyberlink Mux capture.\r\n");
		hr = setupCyberMuxCapture();
	}
	else
	{
		log("Capture type not known %d\r\n", captureType);
		return E_FAIL;
	}

	if (FAILED(hr))
	{
		log("Could Not set up capture type %d (0x%x)\r\n", captureType, hr);
		return hr;
	}

	/*
    CComPtr <IMediaFilter> pMediaFilter = 0;
    hr = m_pFilterGraph.QueryInterface(&pMediaFilter);
	if(FAILED(hr))
	{
		log("QueryInterface failed for IMediaFilter on FilterGraph (0x%x)\r\n", hr);
		return hr;
	}
	log("Set SyncSource to NULL\r\n", hr);
    hr = pMediaFilter->SetSyncSource(NULL);
	if(FAILED(hr))
	{
		log("SetSyncSource failed when setting clock reference (0x%x)\r\n", hr);
		return hr;
	}
	*/

	// finally add the graph to the ROT
	/*
    hr = addToRot(m_pFilterGraph, &m_dwGraphRegister);
    if(FAILED(hr))
    {
        log("Failed to register filter graph with ROT (0x%x)", hr);
        m_dwGraphRegister = 0;
    }
	*/

	return hr;
}

HRESULT CDataConsumer::setupTSFullCapture()
{
	HRESULT hr = S_OK;

	hr = addDumpFilter();
    if (FAILED(hr))
    {
        log("Could Not add dump filter (0x%x)\r\n", hr);
        return hr;
    }

	hr = connectFilters(m_pSource, m_pDumpFilter);
    if (FAILED(hr))
    {
        log("Could Not connect dump to memory source (0x%x)\r\n", hr);
        return hr;
    }

	return hr;
}

HRESULT CDataConsumer::setupDVRMSCapture()
{
	HRESULT hr = S_OK;

	CComPtr <IPin> pIVPin;
	CComPtr <IPin> pIAPin;

	hr = addDemux();
    if (FAILED(hr))
    {
        log("Could Not add MS Demux (0x%x)\r\n", hr);
        return hr;
    }

	hr = addPSI();
    if (FAILED(hr))
    {
        log("Could Not add PSI filter (0x%x)\r\n", hr);
        return hr;
    }

	// Get an instance of the Demux control interface
	CComPtr <IMpeg2Demultiplexer> muxCapInterface;
	hr = m_pDemux.QueryInterface(&muxCapInterface);
    if (FAILED(hr))
    {
        log("Could Not QueryInterface for IMpeg2Demultiplexer on Demux (0x%x)\r\n", hr);
        return hr;
    }

	// SBE config setup for vista/win7
	CComPtr <IStreamBufferConfigure> pSBEconfig;
	hr = pSBEconfig.CoCreateInstance(CLSID_StreamBufferConfig);
	if (FAILED (hr))
	{
		log("CoCreateInstance failed for StreamBufferConfig (0x%x)\r\n", hr);
		return hr;
	}

	HKEY hkey = 0;
	long lRes = RegCreateKey(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\TVSchedulerPro\\SBEconfig"), &hkey);
	if(lRes != ERROR_SUCCESS)
	{
		log("Created Reg Key (SOFTWARE\\TVSchedulerPro\\SBEconfig) with result (0x%x)\r\n", lRes);
		return hr;
	}

	CComPtr<IStreamBufferInitialize> pInit;
	hr = pSBEconfig.QueryInterface(&pInit);
	if (FAILED(hr))
	{
		log("Cannot QueryInterface for IStreamBufferInitialize 01 (0x%x)\r\n", hr);
		return hr;
	}

	hr = pInit->SetHKEY(hkey);
	if (FAILED(hr))
	{
		log("IStreamBufferInitialize->SetHKEY() Failed 01 (0x%x)\r\n", hr);
		return hr;
	}

	pInit.Release();

	// get a temp dir to store our files
	WCHAR appDataPathTEMP[MAX_PATH];
	WCHAR appDataPath[MAX_PATH];
	hr = SHGetFolderPathW(NULL, CSIDL_COMMON_APPDATA, NULL, 0, appDataPathTEMP);
	if (SUCCEEDED(hr))
	{
		StringCchPrintfW(appDataPath, MAX_PATH, L"%s\\TVSchPro\\SBETemp", appDataPathTEMP);
		int result = SHCreateDirectoryExW(NULL, appDataPath, NULL);
		log("SHCreateDirectoryExW Result: %d\r\n", result);
	}
	else
	{
		StringCchPrintfW(appDataPath, MAX_PATH, L"C:\\");
	}

	log("Setting IStreamBufferConfigure->SetDirectory() : %S\r\n", appDataPath);
	hr = pSBEconfig->SetDirectory(appDataPath);
	if (FAILED(hr))
	{
		log("Error: IStreamBufferConfigure->SetDirectory() (0x%x)\r\n", hr);
		return hr;
	}

	log("Setting IStreamBufferConfigure3->SetNamespace(NULL)\r\n");
	CComPtr <IStreamBufferConfigure3> pSBEconfig3;
	hr = pSBEconfig.QueryInterface(&pSBEconfig3);
	if (FAILED(hr))
	{
		log("WARING : Cannot QueryInterface for IStreamBufferConfigure3 on Stream Buffer Sink (0x%x)\r\n", hr);
	}
	else
	{
		hr = pSBEconfig3->SetNamespace(NULL);
		if (FAILED (hr))
		{
			log("WARNING : IStreamBufferConfigure3->SetNamespace(NULL) on Stream Buffer Sink (0x%x)\r\n", hr);
		}
	}

	// create and add the stream buffer sink filter
	hr = m_pSBFilter.CoCreateInstance(CLSID_StreamBufferSink);
	if(FAILED(hr))
	{
		log("CoCreateInstance failed for CLSID_StreamBufferSink (0x%x)\r\n", hr);
		return hr;
	}

	// set reg store on the SBE
	hr = m_pSBFilter.QueryInterface(&pInit);
	if (FAILED(hr))
	{
		log("Cannot QueryInterface for IStreamBufferInitialize 02 (0x%x)\r\n", hr);
		return hr;
	}

	hr = pInit->SetHKEY(hkey);
	if (FAILED(hr))
	{
		log("IStreamBufferInitialize->SetHKEY() Failed 02 (0x%x)\r\n", hr);
		return hr;
	}


	// Add StreamBufferEngineSink filter
	hr = m_pFilterGraph->AddFilter(m_pSBFilter, L"StreamBufferEngineSink Filter");
	if(FAILED(hr))
	{
		log("Unable to add StreamBufferEngineSink Filter to graph (0x%x)\r\n");
		return hr;
	}

	//
	// Set up and connect the Video output PIN
	//

	// only add video pin if we have a video pid
	if(videoPID > -1)
	{
		AM_MEDIA_TYPE mtVideo;
		ZeroMemory(&mtVideo, sizeof(AM_MEDIA_TYPE));

		mtVideo.majortype = MEDIATYPE_Video;
		mtVideo.subtype = MEDIASUBTYPE_MPEG2_VIDEO;
		mtVideo.formattype = FORMAT_MPEG2Video;

		BYTE SeqHdr[] = {
		0x00, 0x00, 0x01, 0xb3, 0x2d, 0x02, 0x40, 0x33,
		0x24, 0x9f, 0x23, 0x81, 0x10, 0x11, 0x11, 0x12,
		0x12, 0x12, 0x13, 0x13, 0x13, 0x13, 0x14, 0x14,
		0x14, 0x14, 0x14, 0x15, 0x15, 0x15, 0x15, 0x15,
		0x15, 0x16, 0x16, 0x16, 0x16, 0x16, 0x16, 0x16,
		0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x17,
		0x18, 0x18, 0x18, 0x19, 0x18, 0x18, 0x18, 0x19,
		0x1a, 0x1a, 0x1a, 0x1a, 0x19, 0x1b, 0x1b, 0x1b,
		0x1b, 0x1b, 0x1c, 0x1c, 0x1c, 0x1c, 0x1e, 0x1e,
		0x1e, 0x1f, 0x1f, 0x21 };

		// Allocate the format block, including space for the sequence header.
		mtVideo.cbFormat = sizeof(MPEG2VIDEOINFO) + sizeof(SeqHdr);
		mtVideo.pbFormat = (BYTE*)CoTaskMemAlloc(mtVideo.cbFormat);
		ZeroMemory(mtVideo.pbFormat, mtVideo.cbFormat);

		// Cast the buffer pointer to an MPEG2VIDEOINFO struct.
		MPEG2VIDEOINFO *pMVIH = (MPEG2VIDEOINFO*)mtVideo.pbFormat;

		RECT rcSrc = {0, 576, 0, 720};        // Source rectangle.
		pMVIH->hdr.rcSource = rcSrc;
		pMVIH->hdr.dwBitRate = 6000000;       // Bit rate 6 meg bits sec.
		pMVIH->hdr.AvgTimePerFrame = 400000;  // 25 fps.
		pMVIH->hdr.dwPictAspectRatioX = 16;   // 16:9 aspect ratio.
		pMVIH->hdr.dwPictAspectRatioY = 9;

		// BITMAPINFOHEADER information.
		pMVIH->hdr.bmiHeader.biSize = 40;
		pMVIH->hdr.bmiHeader.biWidth = 720;
		pMVIH->hdr.bmiHeader.biHeight = 576;

		pMVIH->dwLevel = AM_MPEG2Profile_Main;  // MPEG-2 profile.
		pMVIH->dwProfile = AM_MPEG2Level_Main;  // MPEG-2 level.

		pMVIH->cbSequenceHeader = sizeof(SeqHdr); // Size of sequence header.
		memcpy(pMVIH->dwSequenceHeader, SeqHdr, sizeof(SeqHdr));

		hr = muxCapInterface->CreateOutputPin(&mtVideo, L"Video" ,&pIVPin);
		if(FAILED(hr))
		{
			log("CreateOutputPin Failed for Video (0x%x)\r\n", hr);
			return hr;
		}

		// Get the Pid Map interface of the pin
		// and map the pids we want.
		CComPtr <IMPEG2PIDMap> muxVMapPid;
		hr = pIVPin.QueryInterface(&muxVMapPid);
		if(FAILED(hr))
		{
			log("Could Not QueryInterface for IMPEG2PIDMap on Video Pin (0x%x)\r\n", hr);
			return hr;
		}

		log("Mapping Video PID %d\r\n", videoPID);

		ULONG pid = videoPID;
		hr = muxVMapPid->MapPID(1, &pid, MEDIA_ELEMENTARY_STREAM);
		if(FAILED(hr))
		{
			log("MapPID failed on Video Pin (0x%x)\r\n", hr);
			return hr;
		}

		// connect the SBE video pin
		hr = connectFilters(m_pDemux, m_pSBFilter);
		if (FAILED (hr))
		{
			log("Cannot connect Demux to StreamBufferEngineSink (video) (0x%x)\r\n", hr);
			return hr;
		}
	}

	//
	// Set up and connect the Audio output PIN
	//
	if(audioPID > -1)
	{
		AM_MEDIA_TYPE mtAudio;
		ZeroMemory(&mtAudio, sizeof(AM_MEDIA_TYPE));
		mtAudio.majortype = MEDIATYPE_Audio;
		mtAudio.subtype = MEDIASUBTYPE_MPEG1AudioPayload;
		mtAudio.bFixedSizeSamples = TRUE;
		mtAudio.bTemporalCompression = 0;
		mtAudio.lSampleSize = 1;
		mtAudio.formattype = FORMAT_WaveFormatEx;
		mtAudio.pUnk = NULL;

		if (audioTYPE == TYPE_AUDIO_MPG)
		{
			log("Setting Audio Sub Type MEDIASUBTYPE_MPEG2_AUDIO\r\n");
			mtAudio.subtype = MEDIASUBTYPE_MPEG2_AUDIO;
		}
		else
		{
			log("Setting Audio Sub Type MEDIASUBTYPE_DOLBY_AC3\r\n");
			mtAudio.subtype = MEDIASUBTYPE_DOLBY_AC3;
		}

		mtAudio.cbFormat = sizeof g_MPEG1AudioFormat;
		mtAudio.pbFormat = g_MPEG1AudioFormat;

		muxCapInterface->CreateOutputPin(&mtAudio, L"Audio" ,&pIAPin);

		CComPtr <IMPEG2PIDMap> muxAMapPid;
		hr = pIAPin.QueryInterface(&muxAMapPid);
		if(FAILED(hr))
		{
			log("QueryInterface for IMPEG2PIDMap on muxAMapPid failed (0x%x)\r\n", hr);
			return hr;
		}

		log("Mapping Audio PID %d\r\n", audioPID);

		ULONG AudioPID = audioPID;
		hr = muxAMapPid->MapPID(1, &AudioPID, MEDIA_ELEMENTARY_STREAM);
		if(FAILED(hr))
		{
			log("MapPID failed for audio pid (0x%x)\r\n", hr);
			return hr;
		}

		// Connect audio pin
		hr = connectFilters(m_pDemux, m_pSBFilter);
		if(FAILED(hr))
		{
			log("Cannot connect CapDemux to StreamBufferEngineSink (audio) (0x%x)\r\n", hr);
			return hr;
		}
	}

	//
	// Setup and start the Stream Buffer capture
	//

	CComPtr <IStreamBufferSink> pISBSi;
	hr = m_pSBFilter.QueryInterface(&pISBSi);
	if (FAILED (hr))
	{
		log("Cannot QueryInterface for IStreamBufferSink on Stream Buffer Sink (0x%x)\r\n", hr);
		return hr;
	}


	// set locking file
    SYSTEMTIME st;
    GetLocalTime(&st);
	WCHAR lockFilePath[MAX_PATH];
	StringCchPrintfW(lockFilePath, MAX_PATH, L"%s\\%.4d%.2d%.2d-%.2d%.2d%.2d-%.3d.dvr-ms", appDataPath, st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond, st.wMilliseconds);

	hr = pISBSi->LockProfile(lockFilePath);
	log("LockProfile(%S) = 0x%x\r\n", lockFilePath, hr);
	if(FAILED(hr))
	{
		log("Cannot LockProfile on Stream Buffer Sink (0x%x)\r\n", hr);
		return hr;
	}

	WCHAR capPath[MAX_PATH];
	MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, fileName.c_str(), -1, capPath, MAX_PATH);

	hr = pISBSi->CreateRecorder(capPath, RECORDING_TYPE_CONTENT, &m_pRecUnk);
	if(FAILED(hr))
	{
		log("Cannot CreateRecorder on Stream Buffer Sink (0x%x)\r\n", hr);
		return hr;
	}

	CComQIPtr<IStreamBufferRecordControl> pRecControl;
	hr = m_pRecUnk.QueryInterface(&pRecControl);
	if (FAILED (hr))
	{
		log("Cannot IQ IStreamBufferRecordControl 0x%x\r\n", hr);
		return hr;
	}

	REFERENCE_TIME rtStart = 0;

	hr = pRecControl->Start(&rtStart);
	if(FAILED(hr))
	{
		log("Cannot Start Capture on Stream Buffer Sink (0x%x)\r\n", hr);
		return hr;
	}

	return hr;
}

HRESULT CDataConsumer::setupTSMuxCapture()
{
	HRESULT hr = S_OK;

	hr = addDemux();
    if (FAILED(hr))
    {
        log("Could Not add MS Demux (0x%x)\r\n", hr);
        return hr;
    }

	hr = addPSI();
    if (FAILED(hr))
    {
        log("Could Not add PSI filter (0x%x)\r\n", hr);
        return hr;
    }

	// Get an instance of the Demux control interface
	CComPtr <IMpeg2Demultiplexer> muxCapInterface;
	hr = m_pDemux.QueryInterface(&muxCapInterface);
    if (FAILED(hr))
    {
        log("Could Not QueryInterface for IMpeg2Demultiplexer on Demux (0x%x)\r\n", hr);
        return hr;
    }

	// Create out new pin of type GUID_NULL
	AM_MEDIA_TYPE type;
	ZeroMemory(&type, sizeof(AM_MEDIA_TYPE));
	type.majortype = GUID_NULL;
	type.subtype = GUID_NULL;
	type.formattype = FORMAT_None;

	CComPtr <IPin> pIPin;
	hr = muxCapInterface->CreateOutputPin(&type, L"TS-Mux" ,&pIPin);
    if (FAILED(hr))
    {
        log("Could Not CreateOutputPin on Demux (0x%x)\r\n", hr);
        return hr;
    }

	hr = addDumpFilter();
    if (FAILED(hr))
    {
        log("Could Not add dump filter (0x%x)\r\n", hr);
        return hr;
    }

	hr = connectFilters(m_pDemux, m_pDumpFilter);
    if (FAILED(hr))
    {
        log("Could Not connect dump filter to demux (0x%x)\r\n", hr);
        return hr;
    }

	return hr;
}

HRESULT CDataConsumer::setupCyberMuxCapture()
{
	HRESULT hr = S_OK;

	CComPtr <IPin> pIVPin;
	CComPtr <IPin> pIAPin;

	hr = addDemux();
    if (FAILED(hr))
    {
        log("Could Not add MS Demux (0x%x)\r\n", hr);
        return hr;
    }

	hr = addPSI();
    if (FAILED(hr))
    {
        log("Could Not add PSI filter (0x%x)\r\n", hr);
        return hr;
    }

	// Get an instance of the Demux control interface
	CComPtr <IMpeg2Demultiplexer> muxCapInterface;
	hr = m_pDemux.QueryInterface(&muxCapInterface);
    if (FAILED(hr))
    {
        log("Could Not QueryInterface for IMpeg2Demultiplexer on Demux (0x%x)\r\n", hr);
        return hr;
    }

	// add cyberlink mpg mux filter
	CLSID muxCID;
	CComBSTR bstrTemp("{6770E328-9B73-40C5-91E6-E2F321AEDE57}");
	hr = CLSIDFromString(bstrTemp, &muxCID);

	hr = m_pCyberMuxFilter.CoCreateInstance(muxCID);
    if(FAILED(hr))
    {
        log("CoCreateInstance failed for Cyberlink Mux (0x%x)\r\n", hr);
        return hr;
    }

	// Set the custom interface options
	GUID IID_IMpgMuxer =
	{0x41285420, 0x4721, 0x1052, 0x41, 0x88, 0x00, 0x00, 0x48, 0x0E, 0x8C, 0x00};

	IID_IMpgMuxer.Data4[2] = (unsigned char)0x00;
	IID_IMpgMuxer.Data4[5] = (unsigned char)0x4E;
	IID_IMpgMuxer.Data1 |= (unsigned long)0x0C158040;
	IID_IMpgMuxer.Data4[0] = (unsigned char)0xB1;
	IID_IMpgMuxer.Data4[3] = (unsigned char)0x80;
	IID_IMpgMuxer.Data2 |= (unsigned short)0x2858;
	IID_IMpgMuxer.Data4[7] = (unsigned char)0x15;
	IID_IMpgMuxer.Data4[4] = (unsigned char)0xC8;
	IID_IMpgMuxer.Data3 |= (unsigned short)0x0181;
	IID_IMpgMuxer.Data4[1] = (unsigned char)0xAB;
	IID_IMpgMuxer.Data4[6] = (unsigned char)0x9C;

	CComPtr <IMpgMuxer> pIMpgMuxer;
	hr = m_pCyberMuxFilter->QueryInterface(IID_IMpgMuxer, (void**)&pIMpgMuxer);
	if(FAILED(hr) || pIMpgMuxer == NULL)
	{
		log("Failed to Query for MpgMuxer Interface 0x%x\r\n" , hr);
	}
	else
	{
		log("Configuring mux filter\r\n" , hr);
		pIMpgMuxer->put_SEncType(ENC_MPEG2);
		pIMpgMuxer->put_VideoBitrate(30000000);
		pIMpgMuxer->put_AudioBitrate(448000);
	}


	// Add cyber link mux filter
	hr = m_pFilterGraph->AddFilter(m_pCyberMuxFilter, L"Mux Filter");
	if(FAILED(hr))
	{
		log("Unable to add Cyberlink Mux Filter to graph (0x%x)\r\n");
		return hr;
	}

	//
	// Set up and connect the Video output PIN
	//
	if(videoPID > -1)
	{
		AM_MEDIA_TYPE mtVideo;
		ZeroMemory(&mtVideo, sizeof(AM_MEDIA_TYPE));

		mtVideo.majortype = MEDIATYPE_Video;
		mtVideo.subtype = MEDIASUBTYPE_MPEG2_VIDEO;
		mtVideo.formattype = FORMAT_MPEG2Video;

		BYTE SeqHdr[] = {
		0x00, 0x00, 0x01, 0xb3, 0x2d, 0x02, 0x40, 0x33,
		0x24, 0x9f, 0x23, 0x81, 0x10, 0x11, 0x11, 0x12,
		0x12, 0x12, 0x13, 0x13, 0x13, 0x13, 0x14, 0x14,
		0x14, 0x14, 0x14, 0x15, 0x15, 0x15, 0x15, 0x15,
		0x15, 0x16, 0x16, 0x16, 0x16, 0x16, 0x16, 0x16,
		0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x17,
		0x18, 0x18, 0x18, 0x19, 0x18, 0x18, 0x18, 0x19,
		0x1a, 0x1a, 0x1a, 0x1a, 0x19, 0x1b, 0x1b, 0x1b,
		0x1b, 0x1b, 0x1c, 0x1c, 0x1c, 0x1c, 0x1e, 0x1e,
		0x1e, 0x1f, 0x1f, 0x21 };

		// Allocate the format block, including space for the sequence header.
		mtVideo.cbFormat = sizeof(MPEG2VIDEOINFO) + sizeof(SeqHdr);
		mtVideo.pbFormat = (BYTE*)CoTaskMemAlloc(mtVideo.cbFormat);
		ZeroMemory(mtVideo.pbFormat, mtVideo.cbFormat);

		// Cast the buffer pointer to an MPEG2VIDEOINFO struct.
		MPEG2VIDEOINFO *pMVIH = (MPEG2VIDEOINFO*)mtVideo.pbFormat;

		RECT rcSrc = {0, 576, 0, 720};        // Source rectangle.
		pMVIH->hdr.rcSource = rcSrc;
		pMVIH->hdr.dwBitRate = 6000000;       // Bit rate 6 meg bits sec.
		pMVIH->hdr.AvgTimePerFrame = 400000;  // 25 fps.
		pMVIH->hdr.dwPictAspectRatioX = 16;   // 16:9 aspect ratio.
		pMVIH->hdr.dwPictAspectRatioY = 9;

		// BITMAPINFOHEADER information.
		pMVIH->hdr.bmiHeader.biSize = 40;
		pMVIH->hdr.bmiHeader.biWidth = 720;
		pMVIH->hdr.bmiHeader.biHeight = 576;

		pMVIH->dwLevel = AM_MPEG2Profile_Main;  // MPEG-2 profile.
		pMVIH->dwProfile = AM_MPEG2Level_Main;  // MPEG-2 level.

		pMVIH->cbSequenceHeader = sizeof(SeqHdr); // Size of sequence header.
		memcpy(pMVIH->dwSequenceHeader, SeqHdr, sizeof(SeqHdr));

		hr = muxCapInterface->CreateOutputPin(&mtVideo, L"Video" ,&pIVPin);
		if(FAILED(hr))
		{
			log("CreateOutputPin Failed for Video (0x%x)\r\n", hr);
			return hr;
		}

		// Get the Pid Map interface of the pin
		// and map the pids we want.
		CComPtr <IMPEG2PIDMap> muxVMapPid;
		hr = pIVPin.QueryInterface(&muxVMapPid);
		if(FAILED(hr))
		{
			log("Could Not QueryInterface for IMPEG2PIDMap on Video Pin (0x%x)\r\n", hr);
			return hr;
		}

		log("Mapping Video PID %d\r\n", videoPID);

		ULONG pid = videoPID;
		hr = muxVMapPid->MapPID(1, &pid, MEDIA_ELEMENTARY_STREAM);
		if(FAILED(hr))
		{
			log("MapPID failed on Video Pin (0x%x)\r\n", hr);
			return hr;
		}

		// connect
		hr = connectFilters(m_pDemux, m_pCyberMuxFilter);
		if (FAILED (hr))
		{
			log("Cannot connect Demux to Cyberlink Mux (video) (0x%x)\r\n", hr);
			return hr;
		}
	}

	//
	// Set up and connect the Audio output PIN
	//
	if(audioPID > -1)
	{
		AM_MEDIA_TYPE mtAudio;
		ZeroMemory(&mtAudio, sizeof(AM_MEDIA_TYPE));
		mtAudio.majortype = MEDIATYPE_Audio;
		mtAudio.subtype = MEDIASUBTYPE_MPEG1AudioPayload;
		mtAudio.bFixedSizeSamples = TRUE;
		mtAudio.bTemporalCompression = 0;
		mtAudio.lSampleSize = 1;
		mtAudio.formattype = FORMAT_WaveFormatEx;
		mtAudio.pUnk = NULL;

		if (audioTYPE == TYPE_AUDIO_MPG)
		{
			log("Setting Audio Sub Type MEDIASUBTYPE_MPEG2_AUDIO\r\n");
			mtAudio.subtype = MEDIASUBTYPE_MPEG2_AUDIO;
		}
		else
		{
			log("Setting Audio Sub Type MEDIASUBTYPE_DOLBY_AC3\r\n");
			mtAudio.subtype = MEDIASUBTYPE_DOLBY_AC3;
		}

		mtAudio.cbFormat = sizeof g_MPEG1AudioFormat;
		mtAudio.pbFormat = g_MPEG1AudioFormat;

		muxCapInterface->CreateOutputPin(&mtAudio, L"Audio" ,&pIAPin);

		CComPtr <IMPEG2PIDMap> muxAMapPid;
		hr = pIAPin.QueryInterface(&muxAMapPid);
		if(FAILED(hr))
		{
			log("QueryInterface for IMPEG2PIDMap on muxAMapPid failed (0x%x)\r\n", hr);
			return hr;
		}

		log("Mapping Audio PID %d\r\n", audioPID);

		ULONG pid = audioPID;
		hr = muxAMapPid->MapPID(1, &pid, MEDIA_ELEMENTARY_STREAM);
		if(FAILED(hr))
		{
			log("MapPID failed for audio pid (0x%x)\r\n", hr);
			return hr;
		}

		// Connect audio pin
		hr = connectFilters(m_pDemux, m_pCyberMuxFilter);
		if(FAILED(hr))
		{
			log("Cannot connect Demux to Cyberlink Mux (audio) (0x%x)\r\n", hr);
			return hr;
		}
	}

	// add the dump filter
	hr = addDumpFilter();
    if (FAILED(hr))
    {
        log("Could Not add dump filter (0x%x)\r\n", hr);
        return hr;
    }

	hr = connectFilters(m_pCyberMuxFilter, m_pDumpFilter);
    if (FAILED(hr))
    {
        log("Could Not connect dump filter to demux (0x%x)\r\n", hr);
        return hr;
    }

	return hr;
}

HRESULT CDataConsumer::addDumpFilter()
{
	HRESULT hr = S_OK;

	CLSID dumpCID;
	CComBSTR bstrNetworkType ("{F29F52D4-7C92-4b7c-B8AC-479767AFDF7C}");
	hr = CLSIDFromString(bstrNetworkType, &dumpCID);
	if (FAILED (hr))
	{
		log("Could not CLSIDFromString for Dump CLSID (0x%x)\r\n");
		return hr;
	}

	hr = m_pDumpFilter.CoCreateInstance(dumpCID);
	if (FAILED (hr))
	{
		log("Could not CoCreateInstance Dump Filter (0x%x)\r\n", hr);
		return hr;
	}

	hr = m_pFilterGraph->AddFilter(m_pDumpFilter, L"Dump Filter");
	if(FAILED(hr))
	{
		log("Unable to add Dump filter to graph (0x%x)\r\n", hr);
		return hr;
	}

	// Set file name the dump filter will dump to
	CComPtr <IFileSinkFilter> pIFileSink;
	hr = m_pDumpFilter.QueryInterface(&pIFileSink);
	if(FAILED(hr))
	{
		log("QueryInterface failed for IFileSinkFilter on the dump filter (0x%x)\r\n", hr);
		return hr;
	}

	WCHAR capPath[MAX_PATH];
	MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, fileName.c_str(), -1, capPath, MAX_PATH);
	//mbstowcs(capPath, fileName.c_str(), MAX_PATH);

	hr = pIFileSink->SetFileName(capPath, NULL);
	if (FAILED (hr))
	{
		log("Can not set the dump filter capture file name (0x%x)\r\n", hr);
		return hr;
	}

	return hr;
}

HRESULT CDataConsumer::scanStream()
{
	int progCount = m_StreamScanner.scanForProgs(m_pSectionsAndTables);

	if(progCount < 1)
		return E_FAIL;
	else
		return S_OK;
}

HRESULT CDataConsumer::runGraph(int *result)
{
    HRESULT hr = S_OK;

	CComPtr <IMediaControl> pIMediaControl;
    hr = m_pFilterGraph.QueryInterface(&pIMediaControl);
	if(FAILED(hr))
	{
		log("Cannot QI IMediaControl (0x%x)\r\n", hr);
		*result = -1;
		return hr;
	}

	hr = pIMediaControl->Run();
	if(FAILED(hr))
	{
		log("Cannot run graph (0x%x)\r\n", hr);
        // stop parts of the graph that ran
        pIMediaControl->Stop();
		*result = -2;
		return hr;
	}

	// only do a stream scan if we are not full ts capture
	if(captureType != 0)
	{
		// do a stream scan
		Sleep(2000);
		hr = scanStream();
		if(FAILED(hr))
		{
			log("ScanStream Failed, could not get program details (0x%x)\r\n", hr);
			printf("LOG:Scanning Transport Stream Failed\r\n");
			fflush(stdout);
			pIMediaControl->Stop();
			*result = -3;
			return hr;
		}
	}

	// for ts-mux get prod id and map pids
	if(captureType == 2)
	{
		int prodID = findTSProg();
		if(prodID < 0)
		{
			log("findTSProg Failed\r\n");
			printf("LOG:Program not found in Transport Stream\r\n");
			fflush(stdout);
			// stop parts of the graph that ran
			pIMediaControl->Stop();
			*result = -4;
			return E_FAIL;
		}

		hr = mapTSMuxPids(prodID);
		if(FAILED(hr))
		{
			log("mapTSMuxPids Failed (0x%x)\r\n", hr);
			pIMediaControl->Stop();
			*result = -5;
			return hr;
		}
	}
	else if(captureType == 1 || captureType == 3)
	{
		// for dvr-ms and mpg do a pid check to make sure the pids are there
		bool pidsOK = checkPidsExist();
		if(pidsOK == false)
		{
			log("checkPidsExist Failed, one of the pids not found\r\n");
			printf("LOG:Not all pids exist in Transport Stream\r\n");
			fflush(stdout);
			pIMediaControl->Stop();
			*result = -4;
			return E_FAIL;
		}
	}

	printf("LOG:Consumer Graph Running\r\n");
	fflush(stdout);

	return hr;
}

int CDataConsumer::findTSProg()
{
	int progID = m_StreamScanner.findProgramID(videoPID);

	if(progID < 1)
		progID = m_StreamScanner.findProgramID(audioPID);

	if(progID == -1)
	{
		log("Could not find program PID with video (%d) or audio (%d)\r\n", videoPID, audioPID);
		return -1;
	}

	return progID;
}

bool CDataConsumer::checkPidsExist()
{
	int progID = -1;
	bool videoPidFound = false;
	bool audioPidFound = false;

	progID = m_StreamScanner.findProgramID(videoPID);
	if(progID > -1)
		videoPidFound = true;

	progID = m_StreamScanner.findProgramID(audioPID);
	if(progID > -1)
		audioPidFound = true;

	// override for single video or audio streams

	// if video if -1 get video to found
	if(videoPID == -1)
	{
		videoPidFound = true;

	}
	// if audio -1 get audio to found
	if(audioPID == -1)
	{
		audioPidFound = true;

	}
	// if both are -1 then return not found
	if(videoPID == -1 && audioPID == -1)
	{
		videoPidFound = false;
		audioPidFound = false;
	}

	if(videoPidFound == false || audioPidFound == false)
	{
		log("Could not find either video or audio pid (video %d = %d audio %d =%d)\r\n", videoPID, videoPidFound, audioPID, audioPidFound);
		return false;
	}

	return true;
}

HRESULT CDataConsumer::mapTSMuxPids(int progID)
{
	HRESULT hr = S_OK;

	ULONG pidsArray[30];
	int numberPIDS = m_StreamScanner.getTsPidArray(progID, pidsArray, 29);

	CComPtr <IPin> pTSMuxIPin;
	hr = m_pDemux->FindPin(L"TS-Mux", &pTSMuxIPin);
	if(FAILED(hr))
	{
		log("FindPin Failed on Demux for TS-Mux Pin (0x%x)\r\n", hr);
		return hr;
	}

	CComPtr <IMPEG2PIDMap> muxMapPid;
	hr = pTSMuxIPin.QueryInterface(&muxMapPid);
	if(FAILED(hr))
	{
		log("QueryInterface for IMPEG2PIDMap on TS-Mux Pin Failed (0x%x)\r\n", hr);
		return hr;
	}

	hr = muxMapPid->MapPID(numberPIDS, pidsArray, MEDIA_TRANSPORT_PACKET);
	if(FAILED(hr))
	{
		log("MapPID Failed on Demux TS-Mux output pin (0x%x)\r\n", hr);
		return hr;
	}

	std::string mappedPids = "PIDs Mapped : ";

	char buff[5];
	for(int x = 0; x < numberPIDS; x++)
	{
		StringCchPrintf(buff, 5, "%d", pidsArray[x]);
		mappedPids.append(buff);
		if(x < numberPIDS-1)
			mappedPids.append(", ");
	}

	printf("LOG:%s\r\n", mappedPids.c_str());
	fflush(stdout);

	log("%s\r\n", mappedPids.c_str());

	return hr;
}

HRESULT CDataConsumer::stopGraph()
{
    HRESULT hr = S_OK;

	CComPtr <IMediaControl> pIMediaControl;
    hr = m_pFilterGraph.QueryInterface(&pIMediaControl);
	if(FAILED(hr))
	{
		log("Cannot QI IMediaControl (0x%x)\r\n", hr);
		return hr;
	}

    //hr = pIMediaControl->Pause();
    hr = pIMediaControl->Stop();

    return hr;
}

HRESULT CDataConsumer::initializeGraphBuilder()
{
    HRESULT hr = S_OK;

	hr = m_pFilterGraph.CoCreateInstance(CLSID_FilterGraph);

    if (FAILED(hr))
    {
        log("Could Not CoCreateInstance CLSID_FilterGraph (0x%x)\r\n", hr);
        return hr;
    }

    return hr;
}

HRESULT CDataConsumer::addMemorySource()
{
	HRESULT hr = S_OK;

	hr = m_pSource.CoCreateInstance(CLSID_TSMemSourceFilter);
	if(FAILED(hr))
	{
		log("CoCreate Failed for CLSID_TSMemSourceFilter (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = m_pFilterGraph->AddFilter(m_pSource, L"Memory Source");
	if(FAILED(hr))
	{
		log("AddFilter Failed for Memory Source Filter (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = m_pSource.QueryInterface(&m_pSourceOptions);
    if(FAILED(hr))
    {
        log("Could not QueryInterface IMemSourceSettings on memory source filter (0x%x)", hr);
		return hr;
    }

	WCHAR shareName[MAX_PATH];
	MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, memoryShareName.c_str(), -1, shareName, MAX_PATH);

	hr = m_pSourceOptions->set_ShareName(shareName);
    if(FAILED(hr))
    {
        log("Could not set memory share name (0x%x)", hr);
		return hr;
    }

	return hr;
}

HRESULT CDataConsumer::addDemux()
{
	HRESULT hr = S_OK;

	hr = m_pDemux.CoCreateInstance(CLSID_MPEG2Demultiplexer);
	if(FAILED(hr))
	{
		log("CoCreate Failed for CLSID_MPEG2Demultiplexer (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = m_pFilterGraph->AddFilter(m_pDemux, L"MS Demux");
	if(FAILED(hr))
	{
		log("AddFilter Failed for MS Demux (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = connectFilters(m_pSource, m_pDemux);
	if(FAILED(hr))
	{
		log("ConnectFilters Failed for Mem Source and Demux (0x%x)\r\r\n", hr);
		return hr;
	}

	return hr;
}

HRESULT CDataConsumer::addPSI()
{
	HRESULT hr = S_OK;

	CComPtr <IMpeg2Demultiplexer> pDemuxInterface;
	hr = m_pDemux.QueryInterface(&pDemuxInterface);
	if(FAILED(hr))
	{
		log("QueryInterface Failed for IMpeg2Demultiplexer on Demux (0x%x)\r\r\n", hr);
		return hr;
	}

	AM_MEDIA_TYPE mtPSI;
	ZeroMemory(&mtPSI, sizeof(AM_MEDIA_TYPE));
	mtPSI.majortype = MEDIATYPE_MPEG2_SECTIONS;
	mtPSI.subtype = MEDIASUBTYPE_MPEG2DATA;

	CComPtr <IPin> pIPSIPin;
	hr = pDemuxInterface->CreateOutputPin(&mtPSI, L"PSI", &pIPSIPin);
	if(FAILED(hr))
	{
		log("CreateOutputPin Failed for PSI Pin on Demux (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = m_pSectionsAndTables.CoCreateInstance(CLSID_Mpeg2Data);
	if(FAILED(hr))
	{
		log("CoCreateInstance Failed for PSI filter (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = m_pFilterGraph->AddFilter(m_pSectionsAndTables, L"Sections and Tables");
	if(FAILED(hr))
	{
		log("AddFilter Failed for Sections and Tables (0x%x)\r\r\n", hr);
		return hr;
	}

	hr = connectFilters(m_pDemux, m_pSectionsAndTables);
	if(FAILED(hr))
	{
		log("ConnectFilters Failed for pDemux and pSectionsAndTables (0x%x)\r\r\n", hr);
		return hr;
	}

	return hr;
}


HRESULT CDataConsumer::connectFilters(IBaseFilter* pFilterUpstream, IBaseFilter* pFilterDownstream)
{
    HRESULT         hr = E_FAIL;
    CComPtr <IPin>  pIPinUpstream;
    PIN_INFO        PinInfoUpstream;
    PIN_INFO        PinInfoDownstream;

    // validate passed in filters
    ASSERT (pFilterUpstream);
    ASSERT (pFilterDownstream);

    // grab upstream filter's enumerator
    CComPtr <IEnumPins> pIEnumPinsUpstream;
    hr = pFilterUpstream->EnumPins(&pIEnumPinsUpstream);

    if(FAILED(hr))
    {
        log("ConnectFilters, Cannot Enumerate Upstream Filter's Pins\r\n");
        return hr;
    }

    // iterate through upstream filter's pins
    while (pIEnumPinsUpstream->Next (1, &pIPinUpstream, 0) == S_OK)
    {
        hr = pIPinUpstream->QueryPinInfo (&PinInfoUpstream);
        if(FAILED(hr))
        {
            log("ConnectFilters, Cannot Obtain Upstream Filter's PIN_INFO\r\n");
            return hr;
        }

        CComPtr <IPin>  pPinDown;
        pIPinUpstream->ConnectedTo (&pPinDown);

        // bail if pins are connected
        // otherwise check direction and connect
        if ((PINDIR_OUTPUT == PinInfoUpstream.dir) && (pPinDown == NULL))
        {
            // grab downstream filter's enumerator
            CComPtr <IEnumPins> pIEnumPinsDownstream;
            hr = pFilterDownstream->EnumPins (&pIEnumPinsDownstream);
            if(FAILED(hr))
            {
                log("ConnectFilters, Cannot enumerate pins on downstream filter!\r\n");
                return hr;
            }

            // iterate through downstream filter's pins
            CComPtr <IPin>  pIPinDownstream;
            while (pIEnumPinsDownstream->Next (1, &pIPinDownstream, 0) == S_OK)
            {
                // make sure it is an input pin
                hr = pIPinDownstream->QueryPinInfo(&PinInfoDownstream);
                if(SUCCEEDED(hr))
                {
                    CComPtr <IPin>  pPinUp;

                    // Determine if the pin is already connected.  Note that
                    // VFW_E_NOT_CONNECTED is expected if the pin isn't yet connected.
                    hr = pIPinDownstream->ConnectedTo (&pPinUp);
                    if(FAILED(hr) && hr != VFW_E_NOT_CONNECTED)
                    {
                        log("ConnectFilters, Failed in pIPinDownstream->ConnectedTo()!\r\n");
						pIPinDownstream = NULL;
                        continue;
                    }

                    if ((PINDIR_INPUT == PinInfoDownstream.dir) && (pPinUp == NULL))
                    {
                        if (SUCCEEDED (m_pFilterGraph->Connect(
                                        pIPinUpstream,
                                        pIPinDownstream))
                                        )
                        {
                            PinInfoDownstream.pFilter->Release();
                            PinInfoUpstream.pFilter->Release();
							pIPinDownstream = NULL;
                            return S_OK;
                        }
                    }

                }

                PinInfoDownstream.pFilter->Release();
                pIPinDownstream = NULL;
            } // while next downstream filter pin

            //We are now back into the upstream pin loop
        } // if output pin

        pIPinUpstream = NULL;
        PinInfoUpstream.pFilter->Release();
    } // while next upstream filter pin

    return E_FAIL;
}

/*
HRESULT CDataConsumer::addToRot(IUnknown *pUnkGraph, DWORD *pdwRegister)
{
    CComPtr <IMoniker>              pMoniker;
    CComPtr <IRunningObjectTable>   pROT;
    WCHAR wsz[128];
    HRESULT hr = S_OK;

	hr = GetRunningObjectTable(0, &pROT);
    if (FAILED(hr))
        return hr;

	StringCchPrintfW(wsz, 128, L"FilterGraph %08x pid %08x\0", (DWORD_PTR) pUnkGraph, GetCurrentProcessId());

    hr = CreateItemMoniker(L"!", wsz, &pMoniker);
    if (SUCCEEDED(hr))
	{
        hr = pROT->Register(ROTFLAGS_REGISTRATIONKEEPSALIVE, pUnkGraph, pMoniker, pdwRegister);
	}

	return hr;
}
*/

BOOLEAN CDataConsumer::isDataFlowing()
{
    if(m_pSourceOptions == NULL)
    {
		log("isDataFlowing(): m_pSourceOptions is NULL");
		return FALSE;
    }

	BOOL isDataFlowing = FALSE;
	m_pSourceOptions->get_IsDataFlowing(&isDataFlowing);

	return isDataFlowing;
}

BOOLEAN CDataConsumer::getSignalStats(long *quality, long *strength)
{
	m_pSourceOptions->get_SignalData(quality, strength);
	return TRUE;
}
