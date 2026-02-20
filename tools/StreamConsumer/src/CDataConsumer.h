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

#pragma once

#include <streams.h>
#include <atlbase.h>
#include "log.h"
#include <initguid.h>
#include "..\..\TSMemoryShare\src\TSMemSourceInterface.h"
#include "..\..\TSMemoryShare\src\Guids.h"
#include "CStreamScan.h"
#include "Dvdmedia.h"
#include "MediaFormats.h"
#include "Sbe.h"

#include "CDataConsumerParent.h"

class CDataConsumer : public CDataConsumerParent
{

private:

	// private member variables

	std::string memoryShareName;
	
	int progamPID;
	int videoPID;
	int audioPID;
	int audioTYPE;

	int captureType;

	std::string fileName;

	DWORD m_dwGraphRegister;

	int m_fallBehindCount;

	CComPtr <IGraphBuilder> m_pFilterGraph;
	CComPtr <IBaseFilter> m_pSource;
	CComPtr <IMemSourceSettings> m_pSourceOptions;
	CComPtr <IBaseFilter> m_pDemux;
	CComPtr <IBaseFilter> m_pSectionsAndTables;
	CComPtr <IBaseFilter> m_pDumpFilter;
	CComPtr <IBaseFilter> m_pSBFilter;
	CComPtr <IUnknown> m_pRecUnk;
	CComPtr <IBaseFilter> m_pCyberMuxFilter;

	CStreamScan m_StreamScanner;

	// private member methods

	HRESULT initializeGraphBuilder();
	HRESULT addMemorySource();
	HRESULT addDemux();
	HRESULT addPSI();
	HRESULT addDumpFilter();

	HRESULT setupTSFullCapture();
	HRESULT setupDVRMSCapture();
	HRESULT setupTSMuxCapture();
	HRESULT scanStream();
	int findTSProg();
	bool CDataConsumer::checkPidsExist();
	HRESULT mapTSMuxPids(int progID);
	HRESULT setupCyberMuxCapture();

	HRESULT connectFilters(IBaseFilter *pFilterUpstream, IBaseFilter *pFilterDownstream);
	//HRESULT addToRot(IUnknown *pUnkGraph, DWORD *pdwRegister);


public:

	virtual ~CDataConsumer(void);
	CDataConsumer(void);

	void setmemoryShareName(std::string name);
	void setPIDs(int prog, int video, int audio, int audio_type);
	void setCaptureType(int type);
	void setFileName(std::string name);

	HRESULT buildCaptureGraph();

	BOOLEAN isDataFlowing();
	BOOLEAN getSignalStats(long *quality, long *strength);
	BOOLEAN hasFallenBehind();

	HRESULT runGraph(int *result);
	HRESULT stopGraph();

};

enum ENC_TYPE
{
	ENC_MPEG1 = 1,
	ENC_MPEG2 = 2,
	ENC_VCD = 3,
	ENC_VOB = 4,
	ENC_DVD_VR = 5,
	ENC_SVCD = 6
};

// IMpgMuxer interface definition
DECLARE_INTERFACE_(IMpgMuxer, IUnknown)
{
	STDMETHOD(get_AEncType)(ENC_TYPE* pEncType) PURE;
	STDMETHOD(put_AEncType)(ENC_TYPE EncType) PURE;
	STDMETHOD(get_VEncType)(ENC_TYPE* pEncType) PURE;
	STDMETHOD(put_VEncType)(ENC_TYPE EncType) PURE;
	STDMETHOD(get_SEncType)(ENC_TYPE* pEncType) PURE;
	STDMETHOD(put_SEncType)(ENC_TYPE EncType) PURE;
	STDMETHOD(put_VideoBitrate)(int iRate) PURE;
	STDMETHOD(put_AudioBitrate)(int iRate) PURE;
	STDMETHOD(get_SizeSent)(LONGLONG* pSize) PURE;
	STDMETHOD(get_GetMuxBitRate)(int* iRate) PURE;
	STDMETHOD(put_Path_DVD_RTAV)(char* szPath)PURE;
	STDMETHOD(get_Path_DVD_RTAV)(char* szPath)PURE;
	STDMETHOD(put_MuxingMode)(int iMode)PURE;		//iMode = 0:file mode (default), 1:live mode
	STDMETHOD(get_VideoBitrate)(int* pnVRate)PURE;
	STDMETHOD(get_AudioBitrate)(int* pnARate)PURE;
};


