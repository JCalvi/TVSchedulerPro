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

#include "CDataProducer.h"

const GUID CLSID_ATSCLocator = { 0x8872ff1b,0x98fa,0x4d7a,{0x8d,0x93,0xc9,0xf1,0x05,0x5f,0x85,0xbb}};
const GUID IID_IATSCLocator = { 0xbf8d986f,0x8c2b,0x4131,{0x94,0xd7,0x4d,0x3d,0x9f,0xcc,0x21,0xef}};
const GUID IID_IATSCChannelTuneRequest = { 0x0369b4e1,0x45b6,0x11d3,{0xb6,0x50,0x00,0xc0,0x4f,0x79,0x49,0x8e}};
const GUID IID_IDVBTuningSpace2 = { 0x843188b4,0xce62,0x43db,{0x96,0x6b,0x81,0x45,0xa0,0x94,0xe0,0x40}};
const GUID CLSID_DVBTLocator = { 0x9cd64701,0xbdf3,0x4d14,{0x8e,0x03,0xf1,0x29,0x83,0xd8,0x66,0x64}};
const GUID IID_IDVBTLocator = { 0x8664da16,0xdda2,0x42ac,{0x92,0x6a,0xc1,0x8f,0x91,0x27,0xc3,0x02}};
const GUID IID_IDVBTuneRequest = { 0x0d6f567e,0xa636,0x42bb,{0x83,0xba,0xce,0x4c,0x17,0x04,0xaf,0xa2}};
const GUID CLSID_DVBCLocator = { 0xc531d9fd,0x9685,0x4028,{0x8b,0x68,0x6e,0x12,0x32,0x07,0x9f,0x1e}};
const GUID IID_IDVBCLocator = { 0x6e42f36e,0x1dd2,0x43c4,{0x9f,0x78,0x69,0xd2,0x5a,0xe3,0x90,0x34}};
const GUID IID_IDVBSTuningSpace = { 0xcdf7be60,0xd954,0x42fd,{0xa9,0x72,0x78,0x97,0x19,0x58,0xe4,0x70}};
const GUID CLSID_DVBSLocator = { 0x1df7d126,0x4050,0x47f0,{0xa7,0xcf,0x4c,0x4c,0xa9,0x24,0x13,0x33}};
const GUID IID_IDVBSLocator = { 0x3d7c353c,0x0d04,0x45f1,{0xa7,0x42,0xf9,0x7c,0xc1,0x18,0x8d,0xc8}};
const GUID IID_ITuningSpace = { 0x061c6e30,0xe622,0x11d2,{0x94,0x93,0x00,0xc0,0x4f,0x72,0xd9,0x80}};
const GUID CLSID_DVBSTuningSpace = { 0xb64016f3,0xc9a2,0x4066,{0x96,0xf0,0xbd,0x95,0x63,0x31,0x47,0x26}};
const GUID CLSID_DVBTuningSpace = { 0xc6b14b32,0x76aa,0x4a86,{0xa7,0xac,0x5c,0x79,0xaa,0xf5,0x8d,0xa7}};
const GUID CLSID_ATSCTuningSpace = { 0xa2e30750,0x6c3d,0x11d3,{0xb6,0x53,0x00,0xc0,0x4f,0x79,0x49,0x8e}};
const GUID CLSID_SystemTuningSpaces = { 0xd02aac50,0x027e,0x11d3,{0x9d,0x8e,0x00,0xc0,0x4f,0x72,0xd9,0x80}};
const GUID IID_ITuningSpaceContainer = { 0x5b692e84,0xe2f1,0x11d2,{0x94,0x93,0x00,0xc0,0x4f,0x72,0xd9,0x80}};
const GUID IID_IScanningTuner = { 0x1dfd0a5c,0x0284,0x11d3,{0x9d,0x8e,0x00,0xc0,0x4f,0x72,0xd9,0x80}};


CDataProducer::CDataProducer(void) : m_dwGraphRegister(0)
{
}

CDataProducer::~CDataProducer(void)
{
}

void CDataProducer::setTuneData(int freq, int band)
{
	frequency = freq;
	bandwidth = band;
}

void CDataProducer::setDevice(std::string device)
{
	deviceID = device;
}

void CDataProducer::setMemoryShareName(std::string name)
{
	memShareName = name;
}

HRESULT CDataProducer::initializeGraphBuilder()
{
    HRESULT hr = S_OK;

	hr = m_pFilterGraph.CoCreateInstance(CLSID_FilterGraph);
    if (FAILED(hr))
    {
        log("Could Not CoCreateInstance CLSID_FilterGraph\r\n");
        return hr;
    }

	/*
    SYSTEMTIME st;
    GetLocalTime(&st);
	char name[256];
	StringCchPrintf(name, 256, "ProducerBuildGraph-%.4d%.2d%.2d-%.2d%.2d%.2d-%.3d.log", st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond, st.wMilliseconds);
	HANDLE buildLog = CreateFile(name, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);

	if(buildLog == INVALID_HANDLE_VALUE)
	{
        log("Could not create build graph log file\r\n");
        return E_FAIL;
	}

	hr = m_pFilterGraph->SetLogFile((DWORD_PTR)buildLog);
    if (FAILED(hr))
    {
        log("Could Not Set Log File on FilterGraph\r\n");
        return hr;
    }
	*/

    return hr;
}

HRESULT CDataProducer::buildGraph()
{
	HRESULT hr = S_OK;

	log("Starting build of capture graph\r\n");

	hr = initializeGraphBuilder();
	if(FAILED(hr))
	{
		log("Could not initialise Graph (0x%x)\r\n", hr);
		return hr;
	}

	log("Loading Tuning Provider\r\n");
	hr = loadTuningSpace();
	if(FAILED(hr))
	{
		log("Cannot load TuningSpace (0x%x)\r\n", hr);
		return hr;
	}

	log("Loading Network Provider\r\n");
	hr = loadNetworkProvider();
    if(FAILED(hr))
    {
		log("Cannot load network provider (0x%x)\r\n", hr);
		return hr;
    }

	hr = m_pNetworkProvider.QueryInterface(&m_pITuner);
    //hr = m_pNetworkProvider->QueryInterface(__uuidof (ITuner), reinterpret_cast <void**> (&m_pITuner));
    if(FAILED(hr))
    {
        log("pNetworkProvider->QI: Can't QI for ITuner.\r\n");
        return hr;
    }

	log("Creating Tune Request\r\n");
	CComPtr <IDVBTuneRequest> pDVBTTuneRequest;
	hr = createTuneRequest(0, 0, &pDVBTTuneRequest);
    if(FAILED(hr))
    {
        log("Error Creating Tune Request (0x%x)\r\n", hr);
        return hr;
    }

	log("Submitting Tune Request\r\n");
    //submit the tune request to the network provider
    hr = m_pITuner->put_TuneRequest(pDVBTTuneRequest);
    if(FAILED(hr))
    {
        log("Cannot submit the tune request(0x%x)\r\n", hr);
        return hr;
    }

	log("Loading Tuner Filter : %s\r\n", deviceID.c_str());
    // Load tuner device and connect to network provider
	hr = loadTunerFilter();
    if(FAILED(hr))
    {
        log("Cannot load tuner device and connect network provider (0x%x)\r\n", hr);
        return hr;
    }

	// Now lets try to add the Main Demuxer
	// First try to connect directly to the Tuner Filter
	// If this fails try to add a capture filter for
	// this device and tray again.
	hr = renderDemux(1);
	if(FAILED(hr))
	{
		// it look like our tuner requires a capture filter
		log("Loading Capture Filter\r\n");
		hr = loadFilter(KSCATEGORY_BDA_RECEIVER_COMPONENT, &m_pCaptureDevice, m_pTunerDevice, TRUE);
		if(FAILED(hr))
		{
			// this is terminal, exit here
			log("Cannot load capture device and connect tuner (0x%x)\r\n", hr);
			return hr;
		}

		hr = renderDemux(2);
		if(FAILED(hr))
		{
			log("Cannot Render Main Demux (0x%x)\r\n", hr);
			return hr;
		}
	}

	// submit tune request to lock freq to our mux channel
	submitTuneRequest();

	// add memory share filter
	hr = addMemorySinkFilter();
    if(FAILED(hr))
    {
        log("Failed add or setup Memory Sink Filter (0x%x)\r\n", hr);
		return hr;
    }

	log("Capture Graph Built\r\n");

	/*
    hr = addToRot(m_pFilterGraph, &m_dwGraphRegister);
    if(FAILED(hr))
    {
        log("Failed to register filter graph with ROT (0x%x)\r\n", hr);
        m_dwGraphRegister = 0;
    }
	*/

	return S_OK;
}

HRESULT CDataProducer::addMemorySinkFilter()
{
	HRESULT hr = S_OK;

	hr = m_pMemSink.CoCreateInstance(CLSID_TSMemSinkFilter);
    if(FAILED(hr))
    {
        log("Could not CoCreateInstance CLSID_TSMemSinkFilter (0x%x)\r\n", hr);
		return hr;
    }

	hr = m_pFilterGraph->AddFilter(m_pMemSink, L"Memory Sink");
	if(FAILED(hr))
	{
		log("AddFilter Failed for Memory Sink Filter (0x%x)\r\n", hr);
		return hr;
	}

	// connect mem share to inf-t
	hr = connectFilters(m_pInfTee, m_pMemSink);
	if(FAILED(hr))
	{
		log("Failed to connect Inf-T to Memory Share (0x%x)\r\n", hr);
		return hr;
	}

	hr = m_pMemSink.QueryInterface(&m_pSinkOptions);
    if(FAILED(hr))
    {
        log("Could not QueryInterface IMemShareSettings on memory sink filter (0x%x)\r\n", hr);
		return hr;
    }

	WCHAR shareName[MAX_PATH];
	MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, memShareName.c_str(), -1, shareName, MAX_PATH);
	//StringCchPrintfW(shareName, MAX_PATH, L"%d-%d", frequency, bandwidth);

	hr = m_pSinkOptions->set_ShareName(shareName);
    if(FAILED(hr))
    {
        log("Could not set memory share name (0x%x)\r\n", hr);
		return hr;
    }

	return hr;
}

HRESULT CDataProducer::renderDemux(int type)
{
	log("Doing Main Demux Render with type %d capture device.\r\n", type);

    HRESULT             hr = S_OK;
    CComPtr <IPin>      pIPin;
    CComPtr <IPin>      pDownstreamPin;
    CComPtr <IEnumPins> pIEnumPins;

	// load the demux
	log("Loading Main Demux\r\n");
	hr = loadDemux();
    if(FAILED(hr))
    {
        log("Cannot load Main Demux (0x%x)\r\n", hr);
		if(m_pDemux != NULL)
		{
			m_pFilterGraph->RemoveFilter(m_pDemux);
			m_pDemux.Release();
			m_pDemux = NULL;
		}
        return hr;
    }

	// Load the Inf Tee
	hr = loadInfTee();
    if (FAILED(hr))
    {
        log("Cannot Load Inf Tee filter (0x%x)\r\n", hr);
        return hr;
    }

	if(type == 1)
	{
		// Connecting the Int Tee to the Tuner Filter
		log("Connecting the Inf Tee to the Tuner Filter\r\n");
		hr = connectFilters (m_pTunerDevice, m_pInfTee);
		if(FAILED(hr))
		{
			log("Cannot connect Inf Tee to Tuner filter (0x%x)\r\n", hr);
			m_pFilterGraph->RemoveFilter(m_pInfTee);
			m_pInfTee.Release();
			m_pInfTee = NULL;
			m_pFilterGraph->RemoveFilter(m_pDemux);
			m_pDemux.Release();
			m_pDemux = NULL;
			return hr;
		}
	}
	else if(type == 2)
	{
		// Connecting the Int Tee to the Cap Filter
		log("Connecting the Inf Tee to the Cap Filter\r\n");
		hr = connectFilters (m_pCaptureDevice, m_pInfTee);
		if(FAILED(hr))
		{
			log("Cannot connect Inf Tee to capture filter (0x%x)\r\n", hr);
			m_pFilterGraph->RemoveFilter(m_pInfTee);
			m_pInfTee.Release();
			m_pInfTee = NULL;
			m_pFilterGraph->RemoveFilter(m_pDemux);
			m_pDemux.Release();
			m_pDemux = NULL;
			return hr;
		}
	}

    // connect the demux to inf tee
    hr = connectFilters(m_pInfTee, m_pDemux);

    if(FAILED(hr))
    {
        log("Cannot connect Inf Tee to demux (0x%x)\r\n", hr);
		m_pFilterGraph->RemoveFilter(m_pInfTee);
		m_pInfTee.Release();
		m_pInfTee = NULL;
		m_pFilterGraph->RemoveFilter(m_pDemux);
		m_pDemux.Release();
		m_pDemux = NULL;
        return hr;
    }

	// Get an instance of the Demux control interface
	CComPtr <IMpeg2Demultiplexer> muxInterface;
	hr = m_pDemux.QueryInterface (&muxInterface);

    if(FAILED(hr))
    {
        log("Cannot QI IMpeg2Demultiplexer on Demux (0x%x)\r\n", hr);
        return hr;
    }

    // load transform information filter and connect it to the demux
    hr = loadFilter(KSCATEGORY_BDA_TRANSPORT_INFORMATION, &m_pTIF, m_pDemux, TRUE);
    if(FAILED(hr))
    {
        log("Cannot load TIF (0x%x)\r\n", hr);
        return hr;
    }

    return hr;
}

HRESULT CDataProducer::loadDemux()
{
    HRESULT hr = S_OK;

	hr = m_pDemux.CoCreateInstance(CLSID_MPEG2Demultiplexer);
    if(FAILED(hr))
    {
        log("Could not CoCreateInstance CLSID_MPEG2Demultiplexer (0x%x)\r\n", hr);
        return hr;
    }

    hr = m_pFilterGraph->AddFilter(m_pDemux, L"MS Demux");
    if(FAILED(hr))
    {
        log("Unable to add demux filter to graph (0x%x)\r\n", hr);
        return hr;
    }

    return hr;
}

HRESULT CDataProducer::loadInfTee()
{
    HRESULT hr = S_OK;

	hr = m_pInfTee.CoCreateInstance(CLSID_InfTee);
    if(FAILED(hr))
    {
        log("Could not CoCreateInstance CLSID_InfTee (0x%x)\r\n", hr);
        return hr;
    }

    hr = m_pFilterGraph->AddFilter(m_pInfTee, L"Inf Tee");
    if(FAILED(hr))
    {
        log("Unable to add Inf Tee filter to graph (0x%x)\r\n", hr);
        return hr;
    }

    return hr;
}

HRESULT CDataProducer::loadTuningSpace()
{
	HRESULT hr = S_OK;

	m_pITuningSpace.CoCreateInstance(CLSID_DVBTuningSpace);

	CComQIPtr <IDVBTuningSpace2> piDVBTuningSpace(m_pITuningSpace);

	if (!piDVBTuningSpace)
	{
		log("loadTuningSpace Error (m_pITuningSpace == NULL)\r\n");
		return hr;
	}

	hr = piDVBTuningSpace->put_SystemType(DVB_Terrestrial);
	if (FAILED(hr))
	{
		log("loadTuningSpace Error (put_SystemType returned 0x%x)", hr);
		return hr;
	}

	CComBSTR bstrNetworkType = "{216C62DF-6D7F-4E9A-8571-05F14EDB766A}";
	hr = piDVBTuningSpace->put_NetworkType(bstrNetworkType);
	if (FAILED(hr))
	{
		log("loadTuningSpace Error (put_NetworkType returned 0x%x)", hr);
		return hr;
	}

	return hr;
}

HRESULT CDataProducer::loadNetworkProvider()
{
    HRESULT     hr = S_OK;
    CComBSTR    bstrNetworkType;
    CLSID       CLSIDNetworkType;


    // Get the current Network Type clsid
    hr = m_pITuningSpace->get_NetworkType(&bstrNetworkType);
    if (FAILED(hr))
    {
        log("ITuningSpace::get_NetworkType failed (0x%x)\r\n", hr);
        return hr;
    }

    hr = CLSIDFromString(bstrNetworkType, &CLSIDNetworkType);
    if (FAILED (hr))
    {
        log("Could Not get CLSIDFromString (0x%x)\r\n", hr);
        return hr;
    }

    // create the network provider based on the clsid obtained from the tuning space
	hr = m_pNetworkProvider.CoCreateInstance(CLSIDNetworkType);
    //hr = CoCreateInstance(CLSIDNetworkType,
	//						NULL,
	//						CLSCTX_INPROC_SERVER,
	//						IID_IBaseFilter,
	//						reinterpret_cast<void**>(&m_pNetworkProvider));
    if (FAILED(hr))
    {
        log("Could Not CoCreateInstance Network Provider (0x%x)\r\n", hr);
        return hr;
    }

    //add the Network Provider filter to the graph
    hr = m_pFilterGraph->AddFilter(m_pNetworkProvider, L"Network Provider");
    if (FAILED(hr))
    {
        log("Could Not AddFilter for the Network Provider (0x%x)\r\n", hr);
        return hr;
    }

    return hr;
}

HRESULT CDataProducer::createTuneRequest(int freq, int band, IDVBTuneRequest **pTuneRequest)
{
    HRESULT hr = S_OK;

    if (pTuneRequest == NULL)
    {
        log("TuneRequest is NULL\r\n");
        return E_POINTER;
    }

    // Making sure we have a valid tuning space
    if (m_pITuningSpace == NULL)
    {
        log("m_pITuningSpaceis NULL\r\n");
        return E_FAIL;
    }

    //  Create an instance of the DVB-T tuning space
    CComQIPtr <IDVBTuningSpace2> pDVBTTuningSpace (m_pITuningSpace);
    if (!pDVBTTuningSpace)
    {
        log("Cannot QI for an IDVBTuningSpace2\r\n");
        return E_FAIL;
    }

    //  Create an empty tune request.
    CComPtr <ITuneRequest> pNewTuneRequest;
    hr = pDVBTTuningSpace->CreateTuneRequest(&pNewTuneRequest);
    if (FAILED(hr))
    {
        log("CreateTuneRequest: Can't create tune request (0x%x)\r\n", hr);
        return hr;
    }

    CComQIPtr <IDVBTuneRequest> pDVBTTuneRequest (pNewTuneRequest);
    if (!pDVBTTuneRequest)
    {
        log("createTuneRequest: Can't QI for IDVBTuneRequest.\r\n");
        return E_FAIL;
    }

	//
	// Create a new locator and set it values
	//
    CComPtr <IDVBTLocator> pDVBTLocator;
    hr = pDVBTLocator.CoCreateInstance(CLSID_DVBTLocator);
    if(FAILED(hr))
    {
        log("Failed to create the DVBT locator (0x%x)\r\n", hr);
        return hr;
    }

	hr = pDVBTLocator->put_Bandwidth(band);
    if(FAILED(hr))
    {
        log("Cannot set Bandwidth on Locator (0x%x)\r\n", hr);
        return hr;
    }

	hr = pDVBTLocator->put_CarrierFrequency(freq);
    if(FAILED(hr))
    {
        log("Cannot set CarrierFrequency on Locator (0x%x)\r\n", hr);
        return hr;
    }

	//
	// Add the locator to our created Tune Request
	//
    hr = pDVBTTuneRequest->put_Locator(pDVBTLocator);
    if(FAILED(hr))
    {
        log("Cannot add the locator (0x%x)\r\n", hr);
        return hr;
    }

	pDVBTTuneRequest->put_ONID(-1);
	pDVBTTuneRequest->put_SID(-1);
	pDVBTTuneRequest->put_TSID(-1);

    hr = pDVBTTuneRequest.QueryInterface(pTuneRequest);
    if(FAILED(hr))
    {
        log("QueryInterface for TuneRequest Failed (0x%x)\r\n", hr);
        return hr;
    }

    return hr;
}

HRESULT CDataProducer::loadTunerFilter()
{
    HRESULT hr = S_OK;
    CComPtr <IMoniker> pIMoniker;

	WCHAR wszTunerID[1024] = {0};
	MultiByteToWideChar(CP_ACP, 0, deviceID.c_str(), -1, wszTunerID, NUMELMS(wszTunerID));
	wszTunerID[1023] = 0;

	IBindCtx *lpBC = NULL;
	hr = CreateBindCtx(0, &lpBC);

    if(FAILED(hr))
    {
		log("LoadTunerFilter(): CreateBindCtx() FAILED\r\n");
		return hr;
	}

	DWORD dwEaten = 0;
	hr = MkParseDisplayName(lpBC, wszTunerID, &dwEaten, &pIMoniker);

	lpBC->Release();

	if(FAILED(hr) || pIMoniker == NULL)
	{
		log("LoadTunerFilter(): MkParseDisplayName FAILED : %S\r\n", wszTunerID);
		return hr;
	}

	// obtain filter's friendly name
	CComPtr <IPropertyBag>  pBag;
	hr = pIMoniker->BindToStorage(
								NULL,
								NULL,
								IID_IPropertyBag,
								reinterpret_cast<void**>(&pBag)
								);

	if(FAILED(hr))
	{
		log("LoadTunerFilter(): Can not BindToStorage\r\n");
		return hr;
	}

	CComVariant varBSTR;
	hr = pBag->Read(L"FriendlyName", &varBSTR, NULL);
	if(FAILED(hr))
	{
		log("LoadTunerFilter(): IPropertyBag->Read method failed\r\n");
		pIMoniker = NULL;
		return hr;
	}

	log("LoadTunerFilter(): %S\r\n", varBSTR.bstrVal);

	// bind the filter
	CComPtr <IBaseFilter> pFilter;
	hr = pIMoniker->BindToObject(
								NULL,
								NULL,
								IID_IBaseFilter,
								reinterpret_cast<void**>(&pFilter)
								);

	if(FAILED(hr) || pFilter == NULL)
	{
		pIMoniker = NULL;
		pFilter = NULL;
		return hr;
	}

	// now add the filter
	hr = m_pFilterGraph->AddFilter(pFilter, varBSTR.bstrVal);
	if(FAILED(hr))
	{
		log("LoadTunerFilter() : Cannot add filter\r\n");
		return hr;
	}

	// connect filters
	hr = connectFilters(m_pNetworkProvider, pFilter);
	if (FAILED(hr))
	{
		log("LoadTunerFilter() : ConnectFilters() FAILED\r\n");
		return hr;
	}

	hr = pFilter.QueryInterface(&m_pTunerDevice);
	if (FAILED(hr))
	{
		log("LoadTunerFilter() : QueryInterface() FAILED\r\n");
		return hr;
	}

	return S_OK;
}

HRESULT CDataProducer::connectFilters(IBaseFilter* pFilterUpstream, IBaseFilter* pFilterDownstream)
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

// enumerates through registered filters
// instantiates the filter object and adds it to the graph
// it checks to see if it connects to upstream filter
// if not,  on to the next enumerated filter
// used for tuner, capture, MPE Data Filters and decoders that
// could have more than one filter object
// if pUpstreamFilter is NULL don't bother connecting
HRESULT CDataProducer::loadFilter(REFCLSID clsid, IBaseFilter** ppFilter, IBaseFilter* pConnectFilter, BOOL fIsUpstream)
{
    HRESULT                 hr = S_OK;
    BOOL                    fFoundFilter = FALSE;
    CComPtr <IMoniker>      pIMoniker;
    CComPtr <IEnumMoniker>  pIEnumMoniker;

    if (!m_pICreateDevEnum)
    {
        hr = m_pICreateDevEnum.CoCreateInstance(CLSID_SystemDeviceEnum);
        if(FAILED(hr))
        {
            log("LoadFilter(): Cannot CoCreate ICreateDevEnum (0x%x)\r\n", hr);
            return hr;
        }
    }

    // obtain the enumerator
    hr = m_pICreateDevEnum->CreateClassEnumerator(clsid, &pIEnumMoniker, 0);
    // the call can return S_FALSE if no moniker exists, so explicitly check S_OK
    if(FAILED(hr))
    {
        log("LoadFilter(): Cannot CreateClassEnumerator (0x%x)\r\n", hr);
        return hr;
    }

    if (S_OK != hr)  // Class not found
    {
        log("LoadFilter(): Class not found, CreateClassEnumerator returned S_FALSE");
        return E_UNEXPECTED;
    }

    // next filter
    while(pIEnumMoniker->Next(1, &pIMoniker, 0) == S_OK)
    {

		// obtain filter's friendly name
		CComPtr <IPropertyBag>  pBag;
		hr = pIMoniker->BindToStorage(
									NULL,
									NULL,
									IID_IPropertyBag,
									reinterpret_cast<void**>(&pBag)
									);

		if(FAILED(hr))
		{
			log("loadFilter(): Cannot BindToStorage (0x%x)", hr);
			return hr;
		}

		CComVariant varBSTR;
		hr = pBag->Read(L"FriendlyName", &varBSTR, NULL);
		if(FAILED(hr))
		{
			log("LoadFilter(): IPropertyBag->Read method failed (0x%x)", hr);
			pIMoniker = NULL;
			continue;
		}

		log("LoadFilter(): %S\r\n", varBSTR.bstrVal);

		// skip the following filters
		//BDA Slip De-Framer
		//BDA MPE Filter
		std::wstring ignoreName01(L"BDA Slip De-Framer");
		std::wstring ignoreName02(L"BDA MPE Filter");
		std::wstring thisName(varBSTR.bstrVal);
		bool matches = ignoreName01 == thisName;
		matches = matches || (ignoreName02 == thisName);

		if(matches)
		{
			log("Skipping Filter\r\n");
		}
		else
		{

			// bind the filter
			CComPtr <IBaseFilter>   pFilter;
			hr = pIMoniker->BindToObject(
										NULL,
										NULL,
										IID_IBaseFilter,
										reinterpret_cast<void**>(&pFilter)
										);

			if(FAILED(hr))
			{
				pIMoniker = NULL;
				pFilter = NULL;
				log("LoadFilter(): BindToObject Failed (0x%x)\r\n", hr);
				continue;
			}


			hr = m_pFilterGraph->AddFilter (pFilter, varBSTR.bstrVal);

			if(FAILED(hr))
			{
				log("LoadFilter(): Cannot add filter (0x%x)\r\n", hr);
				return hr;
			}

			// test connections
			// to upstream filter
			if (pConnectFilter)
			{
				if(fIsUpstream)
				{
					hr = connectFilters(pConnectFilter, pFilter);
				}
				else
				{
					hr = connectFilters(pFilter, pConnectFilter);
				}

				if(SUCCEEDED(hr))
				{
					//that's the filter we want
					fFoundFilter = TRUE;
					pFilter.QueryInterface (ppFilter);
					break;
				}
				else
				{
					fFoundFilter = FALSE;
					// that wasn't the the filter we wanted
					// so unload and try the next one
					hr = m_pFilterGraph->RemoveFilter(pFilter);

					if(FAILED(hr))
					{
						log("LoadFilter(): Failed unloading Filter (0x%x)\r\n", hr);
						return hr;
					}
				}
			}
			else
			{
				fFoundFilter = TRUE;
				pFilter.QueryInterface (ppFilter);
				break;
			}


			pFilter = NULL;
			pIMoniker = NULL;
		}// if/else

    } // while

	if(fFoundFilter == TRUE)
		return S_OK;
	else
		return -1;
}

/*
HRESULT CDataProducer::addToRot(IUnknown *pUnkGraph, DWORD *pdwRegister)
{
    CComPtr <IMoniker>              pMoniker;
    CComPtr <IRunningObjectTable>   pROT;
    WCHAR wsz[128];
    HRESULT hr;

	hr = GetRunningObjectTable(0, &pROT);
    if (FAILED(hr))
        return hr;

	StringCchPrintfW(wsz, 128, L"FilterGraph %08x pid %08x\0", (DWORD_PTR) pUnkGraph, GetCurrentProcessId());
    //wsprintfW(wsz, L"FilterGraph %08x pid %08x\0", (DWORD_PTR) pUnkGraph,
    //          GetCurrentProcessId());

    hr = CreateItemMoniker(L"!", wsz, &pMoniker);
    if (SUCCEEDED(hr))
	{
        hr = pROT->Register(ROTFLAGS_REGISTRATIONKEEPSALIVE, pUnkGraph, pMoniker, pdwRegister);
	}

	return hr;
}
*/

HRESULT CDataProducer::runGraph()
{
    HRESULT hr = S_OK;

	CComPtr <IMediaControl> pIMediaControl;
    hr = m_pFilterGraph.QueryInterface(&pIMediaControl);
	if(FAILED(hr))
	{
		log("Cannot QI IMediaControl (0x%x)\r\n", hr);
		return hr;
	}

	hr = pIMediaControl->Run();
	if(FAILED(hr))
	{
		log("Cannot run graph (0x%x)\r\n", hr);
        // stop parts of the graph that ran
        pIMediaControl->Stop();
		return hr;
	}

	// at this point the graph should be running and
	// data should be flowing

	// wait for 2 sec before we do anything
	Sleep(2000);

	// Get the Signal Strength and show in log

	long sig = -99;
	m_pITuner->get_SignalStrength(&sig);
	log("Signal Strength : %d\r\n", sig);
	printf("LOG:Signal Strength : %d\r\n", sig);
	fflush(stdout);

	// Now get some signal stats from the tuner filter
	// using the device Topology system

	HRESULT hr03;
	int signalLocked = -1;
	CComPtr <IBDA_Topology> bdaNetTop;
	hr03 = m_pTunerDevice.QueryInterface(&bdaNetTop);

    if(SUCCEEDED(hr03))
	{
		CComPtr <IUnknown> unknown;
		hr03 = bdaNetTop->GetControlNode(0, 1, 1, &unknown);

		if(SUCCEEDED(hr03))
		{
			//log("Got Control Node 1 %x\r\n", hr03);

			hr03 = unknown.QueryInterface(&m_pSigStats);

			if(SUCCEEDED(hr03))
			{
				//log("Found IBDA_SignalStatistics interface %x\r\n", hr03);

				// we now have the signal stats interface
				// get the values we want.

				// Get the Signal Quality value
				long quality = -1;
				hr03 = m_pSigStats->get_SignalQuality(&quality);

				if(hr03 == E_PROP_SET_UNSUPPORTED)
				{
					log("get_SignalQuality Not supported (%x)\r\n", hr03);
				}
				else if(FAILED(hr03))
				{
					log("Error getting get_SignalQuality %x\r\n", hr03);
				}
				else
				{
					log("Signal Quality : %d\r\n", quality);
					printf("LOG:Signal Quality : %d\r\n", quality);
					fflush(stdout);
				}

				// Get the signal locked value form the
				// tuner, this may return not supported
				BOOLEAN locked = FALSE;
				hr03 = m_pSigStats->get_SignalLocked(&locked);

				if(hr03 == E_PROP_SET_UNSUPPORTED)
				{
					log("get_SignalLocked Not supported %x\r\n", hr03);
				}
				else if(FAILED(hr03))
				{
					log("Error getting get_SignalLocked %x\r\n", hr03);
				}
				else
				{
					log("Got Signal Locked : %d\r\n", locked);
					if(locked)
						signalLocked = 1;
					else
						signalLocked = 0;
				}
			}
			else
			{
				log("Cannot QI IBDA_SignalStatistics %x\r\n", hr03);
			}
		}
		else
		{
			log("Cannot GetControlNode %x\r\n", hr03);
		}
	}
	else
    {
		log("Cannot Find IID_IBDA_Topology %x\r\n", hr03);
	}

	// if the signalLocked value is set to zero this
	// means the get_SignalLocked returned false
	// and the tuner is not locked to a frequency

	if(signalLocked == 0)
	{
		printf("LOG:Tuner Not Locked!\r\n");
		fflush(stdout);
		log("Tuner Not Locked!\r\n");
        // stop parts of the graph that ran
        pIMediaControl->Stop();
		return E_FAIL;
	}

	printf("LOG:Producer Graph Running\r\n");
	fflush(stdout);

	// OK so all is well and just return with no errors
	log("Producer Graph Running\r\n");
	return 0;
}

HRESULT CDataProducer::stopGraph()
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

BOOLEAN CDataProducer::isDataFlowing()
{
	HRESULT hr = S_OK;

    if(m_pSinkOptions == NULL)
    {
        log("Could not QueryInterface IMemSinkSettings on memory sink filter (0x%x)", hr);
		return FALSE;
    }

	BOOL isDataFlowing = FALSE;
	m_pSinkOptions->get_IsDataFlowing(&isDataFlowing);

	return isDataFlowing;
}

HRESULT CDataProducer::logSignalValues()
{
	long strength = -1;
	BOOLEAN locked = FALSE;
	long quality = -1;

	if(m_pITuner != NULL)
	{
		m_pITuner->get_SignalStrength(&strength);
	}

	if(m_pSigStats != NULL)
	{
		m_pSigStats->get_SignalQuality(&quality);
		m_pSigStats->get_SignalLocked(&locked);
	}

	if(m_pSinkOptions != NULL)
	{
		m_pSinkOptions->set_SignalData(quality, strength);
	}

	return S_OK;
}

HRESULT CDataProducer::submitTuneRequest()
{
    HRESULT hr = S_OK;

	log("Tune Settings: Frequency = %d Bandwidth = %d.\r\n", frequency, bandwidth);

    if (!m_pNetworkProvider)
    {
        log("The FilterGraph is not yet built.\r\n");
        return E_FAIL;
    }

    // create tune request
    CComPtr <IDVBTuneRequest> pTuneRequest;
    hr = createTuneRequest(frequency, bandwidth, &pTuneRequest);

	if(FAILED(hr))
	{
		log("Cannot create tune request (0x%x)\r\n", hr);
		return hr;
	}

    hr = m_pITuner->put_TuneRequest(pTuneRequest);

	if (FAILED(hr))
	{
		log("Cannot submit tune request (0x%x)\r\n", hr);
    }

    return hr;

}

