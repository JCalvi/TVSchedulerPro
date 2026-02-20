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

#include "windows.h"
#include <atlbase.h>
#include <string>
#include <streams.h>
#include "log.h"
#include <tuner.h>
#include <ks.h>
#include <ksmedia.h>
#include <bdamedia.h>
#include <bdaiface.h>
#include <initguid.h>

#include "..\..\TSMemoryShare\src\TSMemSourceInterface.h"
#include "..\..\TSMemoryShare\src\Guids.h"

#include "CDataProducerParent.h"

class CDataProducer : public CDataProducerParent
{
public:
	CDataProducer(void);

	// private members data
	int frequency;
	int bandwidth;
	std::string deviceID;
	std::string memShareName;

	CComPtr <ICreateDevEnum> m_pICreateDevEnum;

	CComPtr <IGraphBuilder> m_pFilterGraph;
	CComPtr <IBaseFilter> m_pNetworkProvider;
	CComPtr <ITuningSpace> m_pITuningSpace;
	CComPtr <IScanningTuner> m_pITuner;
	CComPtr <IBaseFilter> m_pTunerDevice;
	CComPtr <IBaseFilter> m_pCaptureDevice;
	CComPtr <IBaseFilter> m_pDemux;
	CComPtr <IBaseFilter> m_pInfTee;
	CComPtr <IBaseFilter> m_pTIF;
	CComPtr <IBDA_SignalStatistics> m_pSigStats;
	CComPtr <IBaseFilter> m_pMemSink;
	CComPtr <IMemSinkSettings> m_pSinkOptions;

	DWORD m_dwGraphRegister;

	//private member methods
	HRESULT initializeGraphBuilder();
	HRESULT loadTuningSpace();  
	HRESULT loadNetworkProvider();
	HRESULT createTuneRequest(int freq, int band, IDVBTuneRequest **pTuneRequest);
	HRESULT loadTunerFilter();
	HRESULT connectFilters(IBaseFilter* pFilterUpstream, IBaseFilter* pFilterDownstream);
	HRESULT loadFilter(REFCLSID clsid, IBaseFilter** ppFilter, IBaseFilter* pConnectFilter, BOOL fIsUpstream);
	HRESULT renderDemux(int type);
	HRESULT loadDemux();
	HRESULT loadInfTee();
	HRESULT addMemorySinkFilter();
	//HRESULT addToRot(IUnknown *pUnkGraph, DWORD *pdwRegister);

public:
	virtual ~CDataProducer(void);

	void setTuneData(int freq, int band);
	void setMemoryShareName(std::string name);
	HRESULT submitTuneRequest();
	void setDevice(std::string device);
	HRESULT buildGraph();
	HRESULT runGraph();
	HRESULT stopGraph();
	BOOLEAN isDataFlowing();
	HRESULT logSignalValues();
};
