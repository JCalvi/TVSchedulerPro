/*
   Copyright (C) 2007

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

#ifndef GRAPH_H_INCLUDED_
#define GRAPH_H_INCLUDED_

#include <streams.h>
#include <mmreg.h>
#include <msacm.h>
#include <fcntl.h>
#include <io.h>
#include <ks.h>
#include <ksmedia.h>
#include <bdatypes.h>
#include <bdamedia.h>
#include <bdaiface.h>
#include <uuids.h>
#include <tuner.h>
#include <commctrl.h>
#include <atlbase.h>
#include <string>

#include "Mpeg2data.h"
#include <mpeg2bits.h>

#include <vector>
#include <map>

#include "CProgramInfo.h"
#include "log.h"

enum NETWORK_TYPE 
{
	DVB_T			= 0X0008
};

class CBDAFilterGraph
{
private:

    CComPtr <ITuningSpace>   m_pITuningSpace;

    CComPtr <IScanningTuner> m_pITuner;

    CComPtr <IGraphBuilder>  m_pFilterGraph;         // for current graph
    CComPtr <IMediaControl>  m_pIMediaControl;       // for controlling graph state
    CComPtr <ICreateDevEnum> m_pICreateDevEnum;      // for enumerating system devices

    CComPtr <IBaseFilter>    m_pNetworkProvider;     // for network provider filter
    CComPtr <IBaseFilter>    m_pTunerDevice;         // for tuner device filter
    CComPtr <IBaseFilter>    m_pDemodulatorDevice;   // for tuner device filter
    CComPtr <IBaseFilter>    m_pCaptureDevice;       // for capture device filter
    CComPtr <IBaseFilter>    m_pDemux;               // for demux filter
    CComPtr <IBaseFilter>    m_pVideoDecoder;        // for mpeg video decoder filter
    CComPtr <IBaseFilter>    m_pAudioDecoder;        // for mpeg audio decoder filter
    CComPtr <IBaseFilter>    m_pTIF;                 // for transport information filter
    CComPtr <IBaseFilter>    m_pMPE;                 // for multiple protocol encapsulator
    CComPtr <IBaseFilter>    m_pIPSink;              // for ip sink filter
    CComPtr <IBaseFilter>    m_pOVMixer;             // for overlay mixer filter
    CComPtr <IBaseFilter>    m_pVRenderer;           // for video renderer filter
    CComPtr <IBaseFilter>    m_pDDSRenderer;         // for sound renderer filter

	CComPtr <IBaseFilter>    m_pCapDemux;
	CComPtr <IBaseFilter>    m_pInfTee;

	CComPtr <IBaseFilter>    m_pDumpInfT;

	CComPtr <IBaseFilter>    m_pMpegSections;


    // Channel pid and capture values
    LONG                     m_lFrequency;
    LONG                     m_lBandwidth;
	LONG                     m_lPgmPid;
	LONG                     m_lvPID;
	LONG                     m_laPID;
	int                      m_iaType;
	std::vector <std::string> dests;

	//registration number for the RunningObjectTable
    DWORD                    m_dwGraphRegister;

    NETWORK_TYPE             m_NetworkType;

    HRESULT InitializeGraphBuilder();
    HRESULT LoadTuningSpace();
    HRESULT LoadNetworkProvider();
    HRESULT LoadDemux();
	HRESULT RenderDemux(int type);
	HRESULT LoadCapDemux();
	HRESULT LoadInfTee();

    HRESULT LoadFilter(
        REFCLSID clsid, 
        IBaseFilter** ppFilter,
        IBaseFilter* pConnectFilter, 
        BOOL fIsUpstream,
        int index);

    HRESULT LoadFilterEX(
        REFCLSID clsid, 
        IBaseFilter** ppFilter,
        IBaseFilter* pConnectFilter, 
        BOOL fIsUpstream,
        int index,
        std::string name,
        GUID* riid);
						

	HRESULT LoadTunerFilter(IBaseFilter** ppFilter, IBaseFilter* pConnectFilter, std::string monikerDisplayName);

    HRESULT ConnectFilters(
        IBaseFilter* pFilterUpstream, 
        IBaseFilter* pFilterDownstream
        );

    HRESULT CreateDVBTTuneRequest(
        LONG lFreq,
        LONG lBand, 
        IDVBTuneRequest**   pTuneRequest
        );

	HRESULT enumComponents(ITuneRequest *pTuneRequest, CProgramInfo *info);
	HRESULT scanForProgramInfo(int *numFound, std::vector <CProgramInfo> *ppinfo);

public:

    bool            m_fGraphBuilt;
    bool            m_fGraphRunning;
    bool            m_fGraphFailure;

    CBDAFilterGraph();   
    ~CBDAFilterGraph();

    HRESULT BuildGraph(NETWORK_TYPE NetworkType, std::string deviceID);

	HRESULT RunGraph(std::string *resultText);
    HRESULT StopGraph();
    HRESULT TearDownGraph();

    HRESULT ChangeChannel(
        LONG lFreq,
        LONG lBand);

	HRESULT ChangeChannel();

	HRESULT queryTunerDevices(std::vector <std::string> *tuners);
	int getProgramInfo(std::vector <CProgramInfo> *ppinfo);
    

    // Adds/removes a DirectShow filter graph from the Running Object Table,
    // allowing GraphEdit to "spy" on a remote filter graph if enabled.
    HRESULT AddGraphToRot(
        IUnknown *pUnkGraph, 
        DWORD *pdwRegister
        );

    void RemoveGraphFromRot(
        DWORD pdwRegister
        );

	void SetFrequency(LONG freq) { m_lFrequency = freq; };
	void SetBandwidth(LONG band) { m_lBandwidth = band; };

	void SetProgramNum(LONG pPID) { m_lPgmPid = pPID; };

	void SetVideoPid(LONG vPID) { m_lvPID = vPID; };
	void SetAudioPid(LONG aPID) { m_laPID = aPID; };
	void SetAudioType(int aType) { m_iaType = aType; };

	void SetCapName(std::vector <std::string> destinations){ dests = destinations; }

	HRESULT LoadPsiFilter();

 };
 
 
#endif // GRAPH_H_INCLUDED_
