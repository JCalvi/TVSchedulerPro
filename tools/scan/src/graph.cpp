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

#include "graph.h"
#include "iMpeg2PsiParser.h"
#include "Bdatif.h"
#include "Sbe.h"
#include "MediaFormats.h"
#include <initguid.h>
#include "CStreamScan.h"

#include "Dvdmedia.h"

//
// An application can advertise the existence of its filter graph
// by registering the graph with a global Running Object Table (ROT).
// The GraphEdit application can detect and remotely view the running
// filter graph, allowing you to 'spy' on the graph with GraphEdit.
//
// To enable registration in this sample, define REGISTER_FILTERGRAPH.
//
//#define REGISTER_FILTERGRAPH

// We use channel 46 internally for testing.  Change this constant to any value.
#define DEFAULT_PHYSICAL_CHANNEL    46L

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



int messageText(std::string *buffer, char *sz,...)
{
	if(buffer != NULL)
	{
		char tach[2000];

		va_list va;
		va_start(va, sz);
		StringCchVPrintf(tach, 2000, sz, va);
		va_end(va);

		buffer->append(tach);
	}

    return FALSE;
}

// Constructor, initialises member variables
// and calls InitializeGraphBuilder
CBDAFilterGraph::CBDAFilterGraph() :
    m_fGraphBuilt(FALSE),
    m_fGraphRunning(FALSE),
    m_NetworkType(DVB_T),
    m_dwGraphRegister (0),
	m_lFrequency(191625),
	m_lBandwidth(7)
{
    if(FAILED(InitializeGraphBuilder()))
        m_fGraphFailure = TRUE;
    else
        m_fGraphFailure = FALSE;
}


// Destructor
CBDAFilterGraph::~CBDAFilterGraph()
{
    if(m_fGraphRunning)
    {
        StopGraph();
    }

    if(m_fGraphBuilt || m_fGraphFailure)
    {
        TearDownGraph();
    }
}


// Instantiate graph object for filter graph building
HRESULT
CBDAFilterGraph::InitializeGraphBuilder()
{
    HRESULT hr = S_OK;

    // we have a graph already
    if (m_pFilterGraph)
        return S_OK;

    // create the filter graph

	hr = m_pFilterGraph.CoCreateInstance (CLSID_FilterGraph);

    if (FAILED(hr))
    {
        log("Couldn't CoCreate IGraphBuilder\r\n");
        m_fGraphFailure = true;
        return hr;
    }

    return hr;
}

int
CBDAFilterGraph::getProgramInfo(std::vector <CProgramInfo> *ppinfo)
{
	Sleep(2000);

	int found = 0;
	for(int x = 0; x < 6; x++)
	{
		scanForProgramInfo(&found, ppinfo);
		if(found > 0)
			break;
		log("No Programs found, sleeping for 2 sec and try again\r\n");
		Sleep(2000);
	}

	char buff[256];
	StringCchPrintf(buff, 256, "Number of programs found = %d", found);
	DbgOutString(buff);

	return found;
}

// BuildGraph sets up devices, adds and connects filters
HRESULT
CBDAFilterGraph::BuildGraph(NETWORK_TYPE NetType, std::string deviceID)
{
    HRESULT hr = S_OK;
    m_NetworkType = NetType;

    // if we have already have a filter graph, tear it down
    if(m_fGraphBuilt)
    {
        if(m_fGraphRunning)
        {
            hr = StopGraph ();
        }

        hr = TearDownGraph ();
    }

	log("Starting build of capture graph\r\n");

    // STEP 1: load network provider first so that it can configure other
    // filters, such as configuring the demux to sprout output pins.
    // We also need to submit a tune request to the Network Provider so it will
    // tune to a channel
	log("Loading Network Provider\r\n");
    if(FAILED (hr = LoadNetworkProvider()))
    {
        log("Cannot load network provider\r\n");
        TearDownGraph();
        m_fGraphFailure = true;
        return hr;
    }

    hr = m_pNetworkProvider->QueryInterface(__uuidof (ITuner), reinterpret_cast <void**> (&m_pITuner));
    if(FAILED (hr))
    {
        log("pNetworkProvider->QI: Can't QI for ITuner.\r\n");
        TearDownGraph();
        m_fGraphFailure = true;
        return hr;
    }

	log("Creating Tune Request\r\n");
    // create a tune request to initialise the network provider
    // before connecting other filters
    CComPtr <IDVBTuneRequest>  pDVBTTuneRequest;
    if(FAILED (hr = CreateDVBTTuneRequest(
                                        0, //m_lFrequency,
                                        0, //m_lBandwidth,
                                        &pDVBTTuneRequest
                                        )))
    {
        log("Cannot create tune request\r\n");
        TearDownGraph();
        m_fGraphFailure = true;
        return hr;
    }

	log("Submitting Tune Request\r\n");
    //submit the tune request to the network provider
    hr = m_pITuner->put_TuneRequest(pDVBTTuneRequest);
    if(FAILED(hr))
    {
        log("Cannot submit the tune request\r\n");
        TearDownGraph();
        m_fGraphFailure = true;
        return hr;
    }

    // Load tuner device and connect to network provider
    if(FAILED (hr = LoadTunerFilter(&m_pTunerDevice, m_pNetworkProvider, deviceID)))
    {
        log("Cannot load tuner device and connect network provider\r\n");
        TearDownGraph();
        m_fGraphFailure = true;
        return hr;
    }

	// Now lets try to add the Main Demuxer
	// First try to connect directly to the Tuner Filter
	// If this fails try to add a capture filter for
	// this device and tray again.
	if(FAILED (hr = RenderDemux(1)))
	{
		log("Loading Capture Filter\r\n");

		if(FAILED (hr = LoadFilter (
									KSCATEGORY_BDA_RECEIVER_COMPONENT,
									&m_pCaptureDevice,
									m_pTunerDevice,
									TRUE, -1
									)))
		{
			log("Cannot load capture device and connect tuner\r\n");
			TearDownGraph();
			m_fGraphFailure = true;
			return hr;
		}

		if(FAILED (hr = RenderDemux(2)))
		{
			log("Cannot Render Main Demux\r\n");
			TearDownGraph();
			m_fGraphFailure = true;
			return hr;
		}
	}

    m_fGraphBuilt = true;
    m_fGraphFailure = false;

	log("Capture Graph Built\r\n");

#ifdef REGISTER_FILTERGRAPH

	log("Registering Graph in ROT\r\n");
    hr = AddGraphToRot (m_pFilterGraph, &m_dwGraphRegister);
    if (FAILED(hr))
    {
        log("Failed to register filter graph with ROT!  hr=0x%x", hr);
        m_dwGraphRegister = 0;
    }

#endif

    return S_OK;
}


// Loads the correct tuning space based on NETWORK_TYPE that got
// passed into BuildGraph()
HRESULT
CBDAFilterGraph::LoadTuningSpace()
{
	HRESULT hr = S_OK;
/*
    CComPtr <ITuningSpaceContainer>  pITuningSpaceContainer;

    // get the tuning space container for all the tuning spaces from SYSTEM_TUNING_SPACES
    hr = pITuningSpaceContainer.CoCreateInstance(CLSID_SystemTuningSpaces);
    if (FAILED (hr))
    {
        printf("Could not CoCreate SystemTuningSpaces\r\n");
        return hr;
    }

    CComVariant var (m_NetworkType);

    hr = pITuningSpaceContainer->get_Item(var, &m_pITuningSpace);

    if(FAILED(hr))
    {
        printf("Unable to retrieve Tuning Space\r\n");
    }
*/


	m_pITuningSpace.CoCreateInstance(CLSID_DVBTuningSpace);

	CComQIPtr <IDVBTuningSpace2> piDVBTuningSpace(m_pITuningSpace);

	if (!piDVBTuningSpace)
	{
		m_pITuningSpace.Release();
		return E_FAIL;
	}

	if (FAILED(hr = piDVBTuningSpace->put_SystemType(DVB_Terrestrial)))
	{
		piDVBTuningSpace.Release();
		m_pITuningSpace.Release();
		return E_FAIL;
	}

	CComBSTR bstrNetworkType = "{216C62DF-6D7F-4E9A-8571-05F14EDB766A}";

	if (FAILED(hr = piDVBTuningSpace->put_NetworkType(bstrNetworkType)))
	{
		piDVBTuningSpace.Release();
		m_pITuningSpace.Release();
		return E_FAIL;
	}

	piDVBTuningSpace.Release();


    return hr;
}



// Creates an DVB Tune Request
HRESULT
CBDAFilterGraph::CreateDVBTTuneRequest(
        LONG lFreq,
        LONG lBand,
        IDVBTuneRequest**   pTuneRequest
    )
{
    HRESULT hr = S_OK;

    if (pTuneRequest == NULL)
    {
        log("Invalid pointer\r\n");
        return E_POINTER;
    }

    // Making sure we have a valid tuning space
    if (m_pITuningSpace == NULL)
    {
        log("Tuning Space is NULL\r\n");
        return E_FAIL;
    }

    //  Create an instance of the DVB-T tuning space
    CComQIPtr <IDVBTuningSpace2> pDVBTTuningSpace (m_pITuningSpace);
    if (!pDVBTTuningSpace)
    {
        log("Cannot QI for an IDVBTTuningSpace\r\n");
        return E_FAIL;
    }

    //  Create an empty tune request.
    CComPtr <ITuneRequest> pNewTuneRequest;
    hr = pDVBTTuningSpace->CreateTuneRequest(&pNewTuneRequest);

    if (FAILED (hr))
    {
        log("CreateTuneRequest: Can't create tune request.\r\n");
        return hr;
    }


    CComQIPtr <IDVBTuneRequest> pDVBTTuneRequest (pNewTuneRequest);
    if (!pDVBTTuneRequest)
    {
        log("CreateDVBTuneRequest: Can't QI for IDVBTuneRequest.\r\n");
        return E_FAIL;
    }


	//
	// Create a new locator and set it values
	//
    CComPtr <IDVBTLocator> pDVBTLocator;
    hr = pDVBTLocator.CoCreateInstance (CLSID_DVBTLocator);
    if (FAILED( hr))
    {
        log("Cannot create the DVBT locator failed\r\n");
        return hr;
    }
	hr = pDVBTLocator->put_Bandwidth(lBand);
    if (FAILED( hr))
    {
        log("Cannot set Bandwidth on Locator\r\n");
        return hr;
    }
	hr = pDVBTLocator->put_CarrierFrequency(lFreq);
    if (FAILED( hr))
    {
        log("Cannot set CarrierFrequency on Locator\r\n");
        return hr;
    }

	//
	// Add the locator to our created Tune Request
	//
    hr = pDVBTTuneRequest->put_Locator(pDVBTLocator);
    if (FAILED (hr))
    {
        log("Cannot put the locator\r\n");
        return hr;
    }

	pDVBTTuneRequest->put_ONID(-1);
	pDVBTTuneRequest->put_SID(-1);
	pDVBTTuneRequest->put_TSID(-1);

    hr = pDVBTTuneRequest.QueryInterface (pTuneRequest);

    return hr;
}


// LoadNetworkProvider loads network provider
HRESULT
CBDAFilterGraph::LoadNetworkProvider()
{
    HRESULT     hr = S_OK;
    CComBSTR    bstrNetworkType;
    CLSID       CLSIDNetworkType;

    // obtain tuning space then load network provider
    if(m_pITuningSpace == NULL)
    {
        hr = LoadTuningSpace();
        if(FAILED(hr))
        {
            log("Cannot load TuningSpace\r\n");
            return hr;
        }
    }

    // Get the current Network Type clsid
    hr = m_pITuningSpace->get_NetworkType(&bstrNetworkType);
    if (FAILED (hr))
    {
        log("ITuningSpace::Get Network Type failed\r\n");
        return hr;
    }

    hr = CLSIDFromString(bstrNetworkType, &CLSIDNetworkType);
    if (FAILED (hr))
    {
        log("Couldn't get CLSIDFromString\r\n");
        return hr;
    }

    // create the network provider based on the clsid obtained from the tuning space
    hr = CoCreateInstance(CLSIDNetworkType, NULL, CLSCTX_INPROC_SERVER,
                          IID_IBaseFilter,
                          reinterpret_cast<void**>(&m_pNetworkProvider));
    if (FAILED (hr))
    {
        log("Couldn't CoCreate Network Provider\r\n");
        return hr;
    }

    //add the Network Provider filter to the graph
    hr = m_pFilterGraph->AddFilter(m_pNetworkProvider, L"Network Provider");

    return hr;
}


// enumerates through registered filters
// instantiates the the filter object and adds it to the graph
// it checks to see if it connects to upstream filter
// if not,  on to the next enumerated filter
// used for tuner, capture, MPE Data Filters and decoders that
// could have more than one filter object
// if pUpstreamFilter is NULL don't bother connecting
HRESULT
CBDAFilterGraph::LoadFilter(
    REFCLSID clsid,
    IBaseFilter** ppFilter,
    IBaseFilter* pConnectFilter,
    BOOL fIsUpstream,
    int index)
{

    return LoadFilterEX(clsid, ppFilter, pConnectFilter, fIsUpstream, index, "", NULL);
}


HRESULT
CBDAFilterGraph::LoadFilterEX(
    REFCLSID clsid,
    IBaseFilter** ppFilter,
    IBaseFilter* pConnectFilter,
    BOOL fIsUpstream,
    int index,
    std::string name,
    GUID* riid)
{
	if (!m_pFilterGraph)
		return E_FAIL;

    HRESULT                 hr = S_OK;
    BOOL                    fFoundFilter = FALSE;
    CComPtr <IMoniker>      pIMoniker;
    CComPtr <IEnumMoniker>  pIEnumMoniker;

    if (!m_pICreateDevEnum)
    {
        hr = m_pICreateDevEnum.CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER);
        if (FAILED (hr))
        {
            log("LoadFilter(): Cannot CoCreate ICreateDevEnum\r\n");
            return hr;
        }
    }

    // obtain the enumerator
    hr = m_pICreateDevEnum->CreateClassEnumerator(clsid, &pIEnumMoniker, 0);
    // the call can return S_FALSE if no moniker exists, so explicitly check S_OK
    if (FAILED (hr))
    {
        log("LoadFilter(): Cannot CreateClassEnumerator\r\n");
        return hr;
    }
    if (S_OK != hr)  // Class not found
    {
        log("LoadFilter(): Class not found, CreateClassEnumerator returned S_FALSE \r\n");
        return E_UNEXPECTED;
    }

	int count = 0;
    // next filter
    while(pIEnumMoniker->Next(1, &pIMoniker, 0) == S_OK)
    {
		if(index == -1 || (index == count && name.length() == 0) || name.length() != 0)
		{

			CComPtr <IBindCtx> pBindCtx;
			hr = CreateBindCtx(0, &pBindCtx);
			if (FAILED(hr))
			{
				return hr;
			}

			// obtain filter's friendly name
			CComPtr <IPropertyBag>  pBag;
			hr = pIMoniker->BindToStorage(
										pBindCtx,
										NULL,
										IID_IPropertyBag,
										reinterpret_cast<void**>(&pBag)
										);

			if(FAILED(hr))
			{
//				OutputDebugString (TEXT("LoadFilter(): Cannot BindToStorage"));
				return hr;
			}

			CComVariant varBSTR;
			hr = pBag->Read(L"FriendlyName", &varBSTR, NULL);
			if(FAILED(hr))
			{
	//			OutputDebugString (TEXT("LoadFilter(): IPropertyBag->Read method failed"));
				pIMoniker = NULL;
				continue;
			}

			char fName[512];
			StringCchPrintf(fName, 512, "%S", varBSTR.bstrVal);

			if (name.length())
			{
				if (index != -1)
				{
					CComVariant varBSTR2;
					// Read its device path (This is very long string generally)
					hr = pBag->Read(L"DevicePath", &varBSTR2, NULL);
					if(FAILED(hr))
					{
			//			OutputDebugString (TEXT("queryTunerDevices(): IPropertyBag->Read method failed"));
						pIMoniker = NULL;
						continue;
					}
					StringCchPrintf(fName, 512, "%S%S", varBSTR.bstrVal, varBSTR2.bstrVal);
				}

				if (index != -1 && strcmp(name.c_str(), fName) != 0)
				{
					pIMoniker = NULL;
					continue;
				}

				if (index == -1 && strstr(fName, name.c_str()) == NULL)
				{
					pIMoniker = NULL;
					continue;
				}
			}

			log("LoadFilter(): %S\r\n", varBSTR.bstrVal);

			// bind the filter
			CComPtr <IBaseFilter> pFilter;
			hr = pIMoniker->BindToObject(
										pBindCtx,
										NULL,
										IID_IBaseFilter,
										reinterpret_cast<void**>(&pFilter)
										);

			if (FAILED(hr))
			{
				if (hr == MK_E_NOOBJECT)
					log("LoadFilter(): BindToObject error MK_E_NOOBJECT\r\n");
				else if(hr == MK_E_EXCEEDEDDEADLINE )
					log("LoadFilter(): BindToObject error MK_E_EXCEEDEDDEADLINE\r\n");
				else if(hr == MK_E_CONNECTMANUALLY  )
					log("LoadFilter(): BindToObject error MK_E_CONNECTMANUALLY \r\n");
				else if(hr == MK_E_INTERMEDIATEINTERFACENOTSUPPORTED)
					log("LoadFilter(): BindToObject error MK_E_INTERMEDIATEINTERFACENOTSUPPORTED \r\n");
				else if(hr == STG_E_ACCESSDENIED)
					log("LoadFilter(): BindToObject error STG_E_ACCESSDENIED\r\n");
				else
					log("LoadFilter(): BindToObject error 0x%x\r\n", hr);

				pIMoniker = NULL;
				pFilter = NULL;
				continue;
			}


			hr = m_pFilterGraph->AddFilter (pFilter, varBSTR.bstrVal);

			if (FAILED(hr))
			{
//				OutputDebugString (TEXT("Cannot add filter\r\n"));
				return hr;
			}

			// test connections
			// to upstream filter
			if (pConnectFilter)
			{
				if(fIsUpstream)
				{
					hr = ConnectFilters (pConnectFilter, pFilter);
				}
				else
				{
					hr = ConnectFilters (pFilter, pConnectFilter);
				}

				void* pvObject;
				if(SUCCEEDED(hr) && riid == NULL)
				{
					//that's the filter we want
					fFoundFilter = TRUE;
					pFilter.QueryInterface (ppFilter);
					break;
				}
				else if(SUCCEEDED(hr) && riid != NULL && SUCCEEDED(pFilter->QueryInterface((*riid), (void**) &pvObject)))
				{
					((IUnknown*)pvObject)->Release();

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
//						OutputDebugString(TEXT("Failed unloading Filter\r\n"));
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
		}

        pIMoniker = NULL;
		count++;

    } // while

	if(fFoundFilter == TRUE)
		return S_OK;
	else
		return -1;
}

HRESULT
CBDAFilterGraph::LoadTunerFilter(IBaseFilter** ppFilter, IBaseFilter* pConnectFilter, std::string monikerDisplayName)
{
	log("Loading Tuner Filter : %s\r\n", monikerDisplayName.c_str());

    HRESULT hr = S_OK;
    CComPtr <IMoniker> pIMoniker;

	WCHAR wszTunerID[1024] = {0};
	MultiByteToWideChar(CP_ACP, 0, monikerDisplayName.c_str(), -1, wszTunerID, NUMELMS(wszTunerID));
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
		log("LoadTunerFilter(): Can not BindToStorage");
		return hr;
	}

	CComVariant varBSTR;
	hr = pBag->Read(L"FriendlyName", &varBSTR, NULL);
	if(FAILED(hr))
	{
		log("LoadTunerFilter(): IPropertyBag->Read method failed");
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
	hr = ConnectFilters(pConnectFilter, pFilter);

	if (FAILED(hr))
	{
		log("LoadTunerFilter() : ConnectFilters() FAILED\r\n");
		return hr;
	}

	hr = pFilter.QueryInterface(ppFilter);

	if (FAILED(hr))
	{
		log("LoadTunerFilter() : QueryInterface() FAILED\r\n");
		return hr;
	}

	return S_OK;
}

// loads the demux into the FilterGraph
HRESULT
CBDAFilterGraph::LoadDemux()
{
    HRESULT hr = S_OK;

    hr = CoCreateInstance(
                        CLSID_MPEG2Demultiplexer,
                        NULL,
                        CLSCTX_INPROC_SERVER,
                        IID_IBaseFilter,
                        reinterpret_cast<void**>(&m_pDemux)
                        );
    if (FAILED (hr))
    {
        log("Could not CoCreateInstance CLSID_MPEG2Demultiplexer\r\n");
        return hr;
    }

    hr = m_pFilterGraph->AddFilter(m_pDemux, L"Demux");
    if(FAILED(hr))
    {
        log("Unable to add demux filter to graph\r\n");
        return hr;
    }

    return hr;
}


// loads the demux into the FilterGraph
HRESULT
CBDAFilterGraph::LoadCapDemux()
{
    HRESULT hr = S_OK;

    hr = CoCreateInstance(
                        CLSID_MPEG2Demultiplexer,
                        NULL,
                        CLSCTX_INPROC_SERVER,
                        IID_IBaseFilter,
                        reinterpret_cast<void**>(&m_pCapDemux)
                        );
    if (FAILED (hr))
    {
        log("Could not CoCreateInstance CLSID_MPEG2Demultiplexer\r\n");
        return hr;
    }

    hr = m_pFilterGraph->AddFilter(m_pCapDemux, L"Cap Demux");
    if(FAILED(hr))
    {
        log("Unable to add Cap demux filter to graph\r\n");
        return hr;
    }

    return hr;
}


// loads the demux into the FilterGraph
HRESULT
CBDAFilterGraph::LoadInfTee()
{
    HRESULT hr = S_OK;

    hr = CoCreateInstance(
                        CLSID_InfTee,
                        NULL,
                        CLSCTX_INPROC_SERVER,
                        IID_IBaseFilter,
                        reinterpret_cast<void**>(&m_pInfTee)
                        );
    if (FAILED (hr))
    {
        log("Could not CoCreateInstance CLSID_InfTee\r\n");
        return hr;
    }

    hr = m_pFilterGraph->AddFilter(m_pInfTee, L"Inf Tee");
    if(FAILED(hr))
    {
        log("Unable to add Inf Tee filter to graph\r\n");
        return hr;
    }

    return hr;
}

// renders demux output pins
HRESULT
CBDAFilterGraph::RenderDemux(int type)
{
	log("Doing Main Demux Render with type %d capture device.\r\n", type);

    HRESULT             hr = S_OK;
    CComPtr <IPin>      pIPin;
    CComPtr <IPin>      pDownstreamPin;
    CComPtr <IEnumPins> pIEnumPins;

	// load the demux
	log("Loading Main Demux\r\n");
    if(FAILED (hr = LoadDemux()))
    {
        log("Cannot load Main Demux\r\n");
		if(m_pDemux != NULL)
		{
			m_pFilterGraph->RemoveFilter(m_pDemux);
			m_pDemux.Release();
			m_pDemux = NULL;
		}
        m_fGraphFailure = true;
        return hr;
    }

	// Load the Inf Tee
	hr = LoadInfTee();
    if (FAILED (hr))
    {
        log("Cannot Load Inf Tee filter\r\n");
        return hr;
    }

	if(type == 1)
	{
		// Connecting the Int Tee to the Tuner Filter
		log("Connecting the Inf Tee to the Tuner Filter\r\n");
		hr = ConnectFilters (m_pTunerDevice, m_pInfTee);
		if (FAILED (hr))
		{
			log("Cannot connect Inf Tee to Tuner filter\r\n");
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
		hr = ConnectFilters (m_pCaptureDevice, m_pInfTee);
		if (FAILED (hr))
		{
			log("Cannot connect Inf Tee to capture filter\r\n");
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
    hr = ConnectFilters (m_pInfTee, m_pDemux);

    if (FAILED (hr))
    {
        log("Cannot connect Inf Tee to demux\r\n");
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

    if (FAILED (hr))
    {
        log("Cannot QI IMpeg2Demultiplexer on Demux\r\n");
        return hr;
    }

	//muxInterface->DeleteOutputPin(L"1");
	//muxInterface->DeleteOutputPin(L"2");
	//muxInterface->DeleteOutputPin(L"3");
	//muxInterface->DeleteOutputPin(L"4");
	//muxInterface->DeleteOutputPin(L"5");

	m_pCapDemux = NULL;

    // load transform information filter and connect it to the demux
    hr = LoadFilter (
                    KSCATEGORY_BDA_TRANSPORT_INFORMATION,
                    &m_pTIF,
                    m_pDemux,
                    TRUE, -1
                    );
    if (FAILED (hr))
    {
        log("Cannot load TIF\r\n");
        return hr;
    }

    return hr;
}

// removes each filter from the graph
HRESULT
CBDAFilterGraph::TearDownGraph()
{
    HRESULT hr = S_OK;
    CComPtr <IBaseFilter> pFilter;
    CComPtr <IEnumFilters> pIFilterEnum;

    m_pITuningSpace = NULL;

    if(m_fGraphBuilt || m_fGraphFailure)
    {
        // unload manually added filters
        m_pFilterGraph->RemoveFilter(m_pIPSink);
        m_pFilterGraph->RemoveFilter(m_pMPE);
        m_pFilterGraph->RemoveFilter(m_pTIF);
        m_pFilterGraph->RemoveFilter(m_pDemux);
        m_pFilterGraph->RemoveFilter(m_pNetworkProvider);
        m_pFilterGraph->RemoveFilter(m_pTunerDevice);
        m_pFilterGraph->RemoveFilter(m_pCaptureDevice);
		m_pFilterGraph->RemoveFilter(m_pCapDemux);
		m_pFilterGraph->RemoveFilter(m_pInfTee);
		m_pFilterGraph->RemoveFilter(m_pDumpInfT);

        m_pIPSink = NULL;
        m_pMPE = NULL;
        m_pTIF = NULL;
        m_pDemux = NULL;
        m_pNetworkProvider = NULL;
        m_pTunerDevice = NULL;
        m_pDemodulatorDevice = NULL;
        m_pCaptureDevice = NULL;
		m_pCapDemux = NULL;
		m_pInfTee = NULL;
		m_pDumpInfT = NULL;

		if (m_pMpegSections != NULL)
			m_pFilterGraph->RemoveFilter(m_pMpegSections);
		m_pMpegSections = NULL;

        // now go unload rendered filters
        hr = m_pFilterGraph->EnumFilters(&pIFilterEnum);

        if(FAILED(hr))
        {
            log("TearDownGraph: cannot EnumFilters\r\n");
            return E_FAIL;
        }

        pIFilterEnum->Reset();

        while(pIFilterEnum->Next(1, &pFilter, 0) == S_OK) // add refs filter
        {
            hr = m_pFilterGraph->RemoveFilter(pFilter);

            if (FAILED (hr))
                return hr;

            pIFilterEnum->Reset();
            pFilter.Release ();
        }
    }

#ifdef REGISTER_FILTERGRAPH
    if (m_dwGraphRegister)
    {
        RemoveGraphFromRot(m_dwGraphRegister);
        m_dwGraphRegister = 0;
    }
#endif

    m_fGraphBuilt = FALSE;
    return S_OK;
}


// ConnectFilters is called from BuildGraph
// to enumerate and connect pins
HRESULT
CBDAFilterGraph::ConnectFilters(
    IBaseFilter* pFilterUpstream,
    IBaseFilter* pFilterDownstream
    )
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
        log("Cannot Enumerate Upstream Filter's Pins\r\n");
        return hr;
    }

    // iterate through upstream filter's pins
    while (pIEnumPinsUpstream->Next (1, &pIPinUpstream, 0) == S_OK)
    {
        hr = pIPinUpstream->QueryPinInfo (&PinInfoUpstream);
        if(FAILED(hr))
        {
            log("Cannot Obtain Upstream Filter's PIN_INFO\r\n");
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
                log("Cannot enumerate pins on downstream filter!\r\n");
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
                        log("Failed in pIPinDownstream->ConnectedTo()!\r\n");
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

// RunGraph checks to see if a graph has been built
// if not it calls BuildGraph
// RunGraph then calls MediaCtrl-Run
HRESULT
CBDAFilterGraph::RunGraph(std::string *resultText)
{
    // check to see if the graph is already running
    if(m_fGraphRunning)
        return S_OK;

    HRESULT hr01 = S_OK;
    if (m_pIMediaControl == NULL)
	{
        hr01 = m_pFilterGraph.QueryInterface (&m_pIMediaControl);
	}

    if (SUCCEEDED (hr01))
    {
        // run the graph
        hr01 = m_pIMediaControl->Run();
        if(SUCCEEDED(hr01))
        {
            m_fGraphRunning = true;
        }
        else
        {
            // stop parts of the graph that ran
            m_pIMediaControl->Stop();
            log("Cannot run graph %x\r\n", hr01);
			messageText(resultText, "Cannot run graph %x\r\n", hr01);
			return hr01;
        }
    }
	else
	{
		log("Cannot QI IMediaControl %x\r\n", hr01);
		return hr01;
	}

	// at this point the graph should be running and
	// data should be flowing

	// wait for 2 sec before we do anything
	Sleep(2000);

	// Get the Signal Strength and show in log

	long sig = -99;
	m_pITuner->get_SignalStrength(&sig);
	log("Signal Strength : %d\r\n", sig);
	messageText(resultText, "Signal Strength : %d\r\n", sig);

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

			CComPtr <IBDA_SignalStatistics> pSigStats;
			hr03 = unknown.QueryInterface(&pSigStats);

			if(SUCCEEDED(hr03))
			{
				//log("Found IBDA_SignalStatistics interface %x\r\n", hr03);

				// we now have the signal stats interface
				// get the values we want.

				// Get the Signal Quality value
				long quality = -1;
				hr03 = pSigStats->get_SignalQuality(&quality);

				if(hr03 == E_PROP_SET_UNSUPPORTED)
					log("get_SignalQuality Not supported (%x)\r\n", hr03);
				else if(FAILED(hr03))
					log("Error getting get_SignalQuality %x\r\n", hr03);
				else
				{
					log("Signal Quality : %d\r\n", quality);
					messageText(resultText, "Signal Quality : %d\r\n", quality);
				}

				// Get the signal locked value form the
				// tuner, this may return not supported
				BOOLEAN locked = FALSE;
				hr03 = pSigStats->get_SignalLocked(&locked);

				if(hr03 == E_PROP_SET_UNSUPPORTED)
					log("get_SignalLocked Not supported %x\r\n", hr03);
				else if(FAILED(hr03))
					log("Error getting get_SignalLocked %x\r\n", hr03);
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
		log("Tuner Not Locked, returning with error -1\r\n");
		messageText(resultText, "Tuner Not Locked, returning with error -1\r\n");
		return -1;
	}

	// Stop the TIF filter, this is due to a bug in the TIF
	// filter and one day may be removed, the tiff filter can not
	// be running in two different graphs at the same time

	//log("Stopping TIF filter\r\n");
	//m_pTIF->Stop();

	//
	// If we are a TS Mux capture parse all the extra PIDS we need
	//

	HRESULT hr02 = S_OK;
	CComPtr <IPin> pIPin;
	if(m_pCapDemux != NULL && SUCCEEDED(m_pCapDemux->FindPin(L"TS", &pIPin)))
	{

		CStreamScan scanner;
		scanner.scanForProgs(m_pMpegSections);

		int programID = scanner.findProgramID(m_lvPID);

		if(programID < 1)
			programID = scanner.findProgramID(m_laPID);

		if(programID > 0)
		{

			ULONG pidsArray[30];

			int numberPIDS = scanner.getTsPidArray(programID, pidsArray, 29);

			char buff[1024];
			StringCchPrintf(buff, 1024, "%d Pids mapped : ", numberPIDS);
			char pidString[10];

			for (int i = 0; i < numberPIDS; i++)
			{
				StringCchPrintf(pidString, 10, " %d ", pidsArray[i]);
				StringCchCat(buff, 1024, pidString);
			}

			StringCchCat(buff, 1024, "\r\n");
			log(buff);

			messageText(resultText, buff);

			CComPtr <IMPEG2PIDMap> muxMapPid;
			hr02 = pIPin.QueryInterface(&muxMapPid);
			if (SUCCEEDED (hr02))
			{
				if (SUCCEEDED (hr01))
				{
					hr02 = muxMapPid->MapPID(numberPIDS, pidsArray, MEDIA_TRANSPORT_PACKET);
				}
				else
				{
					log("Cannot MapPID on IMPEG2PIDMap interface %x\r\n", hr02);
					return hr02;
				}
			}
			else
			{
				log("Cannot QI IMPEG2PIDMap %x\r\n", hr02);
				return hr02;
			}

		}
		else
		{
			//Map Pids the old way
			CComPtr <IMPEG2PIDMap> muxMapPid;
			hr02 = pIPin.QueryInterface (&muxMapPid);

			messageText(resultText, "Stream scan failed, mapping default PIDS : %d %d\r\n", m_lvPID, m_laPID);

			log("Mapping Default PIDS, pid scan failed!\r\n");
			log("Setting streams stream1 = %d stream2 = %d\r\n", m_lvPID, m_laPID);

			if (SUCCEEDED (hr02))
			{
				ULONG pid = m_lvPID;
				hr02 = muxMapPid->MapPID(1, &pid, MEDIA_TRANSPORT_PACKET);
				if (FAILED(hr02))
				{
					log("Cannot MapPID (video) on IMPEG2PIDMap interface %x\r\n", hr02);
					return hr02;
				}

				pid = m_laPID;
				hr02 = muxMapPid->MapPID(1, &pid, MEDIA_TRANSPORT_PACKET);
				if (FAILED(hr02))
				{
					log("Cannot MapPID (audio) on IMPEG2PIDMap interface %x\r\n", hr02);
					return hr02;
				}
			}
			else
			{
				log("Cannot QI IMPEG2PIDMap %x\r\n", hr02);
				return hr02;
			}
		}
	}

	// OK so all is well and just return with no errors
	log("About to leave RunGraph with code 0\r\n");
	return 0;
}

HRESULT
CBDAFilterGraph::scanForProgramInfo(int *numFound, std::vector <CProgramInfo> *ppinfo)
{
	HRESULT hr;
	(*numFound) = 0;

	if(!m_fGraphRunning)
	{
		log("Can not scan for program info as Graph is not running!\r\n");
		return -1;
	}

	// Get the GuideData interface from the TIF filter
	CComPtr <IGuideData> pGuideData;
	hr = m_pTIF.QueryInterface(&pGuideData);
    if (FAILED(hr))
    {
		log("Failed to QI for IGuideData on TIF Filter\r\n");
	}

	// Get the TuneRequestinfo interface from the TIF filter
	CComPtr <ITuneRequestInfo> pTuneRequestInfo;
	hr = m_pTIF.QueryInterface(&pTuneRequestInfo);
    if (FAILED(hr))
    {
		log("Failed to QI for ITuneRequestInfo on pGuideData\r\n");
		return hr;
	}

	// Get a list of services from the GuideData interface
	CComPtr <IEnumTuneRequests>  piEnumTuneRequests;
	hr = pGuideData->GetServices(&piEnumTuneRequests);
    if (FAILED(hr))
    {
		log("Failed to get IEnumTuneRequests\r\n");
		return hr;
	}

	CProgramInfo info;

	//
	// Now loop through all the available services on the frequency
	//

	unsigned long ulRetrieved = 1;
	ITuneRequest *piTuneRequest = NULL;

	while (SUCCEEDED(piEnumTuneRequests->Next(1, &piTuneRequest, &ulRetrieved)) && ulRetrieved > 0)
	{
		memset(&info, 0, sizeof(CProgramInfo));
		StringCchCopy(info.programName, 256, "Not Available");

		// Get the service properties
		CComPtr <IEnumGuideDataProperties> serviceProp;

		hr = pGuideData->GetServiceProperties(piTuneRequest, &serviceProp);
		if(FAILED(hr))
		{
			log("Cannot Get GetServiceProperties\r\n");
		}
		else
		{

			// Now Print the service Properties
			IGuideDataProperty *servProp;
			unsigned long num = 1;
			int nameNum = 0;

			while(SUCCEEDED(serviceProp->Next(1, &servProp, &num)) && num > 0)
			{
				char buff[256];

				CComBSTR name;
				servProp->get_Name(&name);

				CComVariant value;
				servProp->get_Value(&value);
				StringCchPrintf(buff, 256, "%S : %S\r\n", name.m_str, value.bstrVal);
				log(buff);

				if(nameNum == 1)
				{
					StringCchPrintf(info.programName, 256, "%S", value.bstrVal);
				}

				nameNum++;
			}
		}

		// Fill in the Components lists for the tune request
		hr = pTuneRequestInfo->CreateComponentList(piTuneRequest);
		if(FAILED(hr))
		{
			log("Failed to GetNextProgram from TIF ITuneRequestInfo (0x%x)\r\n", hr);
		}
		else
		{
			enumComponents(piTuneRequest, &info);
		}


		ppinfo->push_back(info);

		(*numFound)++;

	}

	//
	// Now do the internal PID scan for this channel
	//
	CStreamScan scanner;
	int numProgsFound = scanner.scanForProgs(m_pMpegSections);
	log("Internal Scan for Programs found %d\r\n", numProgsFound);


	//
	// Now update the programs with AC3 data
	//

	size_t size = ppinfo->size();

	for(size_t x = 0; x < size; x++)
	{
		scanner.updateProgramInfo(&(ppinfo->at(x)));
	}

	return hr;
}

HRESULT
CBDAFilterGraph::enumComponents(ITuneRequest *pTuneRequest, CProgramInfo *info)
{
	HRESULT hr;

	CComPtr <IComponents> pConponents;
	hr = pTuneRequest->get_Components(&pConponents);
    if(FAILED(hr))
    {
		log("Cannot Get Components\r\n");
		return hr;
	}

    CComPtr<IEnumComponents> pEnum;
    hr = pConponents->EnumComponents(&pEnum);

    if (SUCCEEDED(hr))
    {
        CComPtr <IComponent> pComponent;
        ULONG cFetched = 1;

        while (SUCCEEDED(pEnum->Next(1, &pComponent, &cFetched)) && cFetched > 0)
        {

			char buff[256];

			CComPtr <IMPEG2Component> mpegComponent;

            hr = pComponent.QueryInterface(&mpegComponent);

			long PID = -1;
			hr = mpegComponent->get_PID(&PID);

			long progPID = -1;
			mpegComponent->get_ProgramNumber(&progPID);

			long pcrPID = -1;
			mpegComponent->get_PCRPID(&pcrPID);

			CComPtr <IComponentType> comType;
			mpegComponent->get_Type(&comType);

			ComponentCategory cat;
			comType->get_Category(&cat);

			info->programID = progPID;

			StringCchPrintf(buff, 256, "progPID = %d : PID = %d : pcrPID = %d : CAT = %d\r\n", progPID, PID, pcrPID, cat);
			log(buff);

			pComponent.Release();
        }
    }

	return hr;
}

// StopGraph calls MediaCtrl - Stop
HRESULT
CBDAFilterGraph::StopGraph()
{

	// check to see if the graph is already stopped
    if(m_fGraphRunning == false)
        return S_OK;

	HRESULT hr = S_OK;

    ASSERT (m_pIMediaControl);
    // pause before stopping
    hr = m_pIMediaControl->Pause();

    // stop the graph
    hr = m_pIMediaControl->Stop();

    m_fGraphRunning = (FAILED (hr))?true:false;
    return hr;
}


HRESULT
CBDAFilterGraph::ChangeChannel()
{
	return ChangeChannel(m_lFrequency, m_lBandwidth);
}


HRESULT
CBDAFilterGraph::ChangeChannel(
        LONG lFreq,
        LONG lBand
        )
{
    HRESULT hr = S_OK;
    m_lFrequency = lFreq;
    m_lBandwidth = lBand;

	log("Changing Frequency %d bandwidth %d.\r\n", m_lFrequency, m_lBandwidth);

    if (!m_pNetworkProvider)
    {
        log("The FilterGraph is not yet built.\r\n");
        return E_FAIL;
    }


    // create tune request
    CComPtr <IDVBTuneRequest> pTuneRequest;
    hr = CreateDVBTTuneRequest(
                            lFreq,
                            lBand,
                            &pTuneRequest
                            );

    if(SUCCEEDED(hr))
    {
        hr = m_pITuner->put_TuneRequest(pTuneRequest);
        if (FAILED (hr))
            log("Cannot submit tune request\r\n");
    }
    else
    {
        log("Cannot Submit Channel Request\r\n");
		return hr;
    }

    return hr;
}

HRESULT CBDAFilterGraph::queryTunerDevices(std::vector <std::string> *tuners)
{
    HRESULT hr = S_OK;
    CComPtr <IMoniker> pIMoniker;
    CComPtr <IEnumMoniker> pIEnumMoniker;

    if (!m_pICreateDevEnum)
    {
        hr = m_pICreateDevEnum.CoCreateInstance(CLSID_SystemDeviceEnum);
        if (FAILED (hr))
        {
            log("queryTunerDevices(): Cannot CoCreate ICreateDevEnum\r\n");
            return hr;
        }
    }

    // obtain the enumerator
    hr = m_pICreateDevEnum->CreateClassEnumerator(KSCATEGORY_BDA_NETWORK_TUNER, &pIEnumMoniker, 0);
    // the call can return S_FALSE if no moniker exists, so explicitly check S_OK
    if (FAILED (hr))
    {
        log("queryTunerDevices(): Cannot CreateClassEnumerator\r\n");
        return hr;
    }
    if (S_OK != hr)  // Class not found
    {
        log("queryTunerDevices(): Class not found, CreateClassEnumerator returned S_FALSE");
        return E_UNEXPECTED;
    }


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
			OutputDebugString (TEXT("queryTunerDevices(): Cannot BindToStorage"));
			return hr;
		}

		CComVariant varBSTR;
		hr = pBag->Read(L"FriendlyName", &varBSTR, NULL);
		if(FAILED(hr))
		{
			OutputDebugString (TEXT("queryTunerDevices(): IPropertyBag->Read method failed"));
			pIMoniker = NULL;
			continue;
		}

		char buff[512];
		StringCchPrintf(buff, 512, "%S", varBSTR.bstrVal);

		tuners->push_back(buff);

		//log("queryTunerDevices(): %S\r\n", varBSTR.bstrVal);

		pIMoniker = NULL;
	}

	return hr;
}


#ifdef REGISTER_FILTERGRAPH

// Adds a DirectShow filter graph to the Running Object Table,
// allowing GraphEdit to "spy" on a remote filter graph.
HRESULT CBDAFilterGraph::AddGraphToRot(
        IUnknown *pUnkGraph,
        DWORD *pdwRegister
        )
{
    CComPtr <IMoniker>              pMoniker;
    CComPtr <IRunningObjectTable>   pROT;
    WCHAR wsz[128];
    HRESULT hr;

    if (FAILED(GetRunningObjectTable(0, &pROT)))
        return E_FAIL;

	StringCchPrintfW(wsz, 128, L"FilterGraph %08x pid %08x\0", (DWORD_PTR) pUnkGraph,
              GetCurrentProcessId());
    //wsprintfW(wsz, L"FilterGraph %08x pid %08x\0", (DWORD_PTR) pUnkGraph,
    //          GetCurrentProcessId());

    hr = CreateItemMoniker(L"!", wsz, &pMoniker);
    if (SUCCEEDED(hr))
        hr = pROT->Register(ROTFLAGS_REGISTRATIONKEEPSALIVE, pUnkGraph,
                            pMoniker, pdwRegister);

    return hr;
}

// Removes a filter graph from the Running Object Table
void CBDAFilterGraph::RemoveGraphFromRot(
        DWORD pdwRegister
        )
{
    CComPtr <IRunningObjectTable> pROT;

    if (SUCCEEDED(GetRunningObjectTable(0, &pROT)))
        pROT->Revoke(pdwRegister);

}

#endif

HRESULT
CBDAFilterGraph::LoadPsiFilter()
{
	if (m_pMpegSections != NULL)
		return S_OK;

	log("Deleting pins from demux & loading/connecting PSI filter.\r\n");

	if (!m_pFilterGraph)
		return E_FAIL;

    HRESULT hr = S_OK;

//	m_pMpegSections = NULL;

	// Find the Sections & tables Pin & delete the rest.
	wchar_t pinName[128];
    PIN_DIRECTION  direction;
	CComPtr <IPin> pIPin = NULL;
	CComPtr <IPin> pDownstreamPin = NULL;
	AM_MEDIA_TYPE *type;

	// Get an instance of the Demux control interface
	CComPtr <IMpeg2Demultiplexer> muxInterface;
	hr = m_pDemux.QueryInterface (&muxInterface);
    if (FAILED (hr))
    {
        log("Cannot get IMpeg2Demultiplexer on Demux \r\n");
        return hr;
    }

	// Enumerate the Demux pins
    CComPtr <IEnumPins> pIEnumPins;
    hr = m_pDemux->EnumPins (&pIEnumPins);

    if (FAILED (hr))
    {
        log("Cannot get enumpins on Demux \r\n");
        return hr;
    }

    while(pIEnumPins->Next(1, &pIPin, 0) == S_OK)
    {
        hr = pIPin->QueryDirection(&direction);

        if(direction == PINDIR_OUTPUT)
        {
            pIPin->ConnectedTo(&pDownstreamPin);

            if(pDownstreamPin == NULL)
            {
				PIN_INFO pinInfo;
				if (FAILED(pIPin->QueryPinInfo(&pinInfo)))
				{
					log("Cannot Get Demux Output Pin Info\r\n");
					return hr;
				}

				CComPtr <IEnumMediaTypes> ppEnum;
				if (SUCCEEDED (pIPin->EnumMediaTypes(&ppEnum)))
				{
					while(ppEnum->Next(1, &type, 0) == S_OK)
					{
						//Save if Name of Pin for Sections & tables render
						if (type->majortype == KSDATAFORMAT_TYPE_MPEG2_SECTIONS &&
							type->subtype == MEDIASUBTYPE_MPEG2DATA)
						{
							StringCchCopyW(pinName, 128, pinInfo.achName);
						}
						else
						{
							muxInterface->DeleteOutputPin(pinInfo.achName);
						}
						DeleteMediaType(type);
					}
				}
				ppEnum = NULL;
            }
            pDownstreamPin = NULL;
        }
        pIPin = NULL;
    }

GUID guidIMpeg2Data = {0x9B396D40, 0xF380, 0x4e3c, {0xA5, 0x14, 0x1A, 0x82, 0xBF, 0x6E, 0xBF, 0xE6}}; //IID_IMpeg2Data

	// MPEG2 Sections and Tables Loading
    if (FAILED(LoadFilterEX(
		KSCATEGORY_BDA_TRANSPORT_INFORMATION,
		&m_pMpegSections,
		m_pDemux,
		TRUE, -1, "", &guidIMpeg2Data
		)))
    {
		log("Rendering MPEG-2 Sections and Tables Filter \r\n");
		CComPtr <IPin> pIPin;
		CComPtr <IPin> pIPin2;

		if (FAILED(m_pDemux->FindPin(pinName, &pIPin)))
		{
			log("Cannot find Demux MPEG-2 Sections and Tables Pin \r\n");
			return hr;
		}

		if (FAILED(m_pFilterGraph->Render(pIPin)))
		{
			log("Cannot load MPEG-2 Sections and Tables\r\n");
			return hr;
		}

		if (FAILED(pIPin->ConnectedTo(&pIPin2)))
		{
			log("Cannot Get MPEG-2 Sections and Tables Input Pin\r\n");
			return hr;
		}

		PIN_INFO pinInfo;
		if (FAILED(pIPin2->QueryPinInfo(&pinInfo)))
		{
			log("Cannot Get MPEG-2 Sections and Tables Input Pin Info\r\n");
			return hr;
		}

		m_pMpegSections = pinInfo.pFilter;
    }

	log("PSI loaded and added with result hr=0x%x\r\n", hr);

	return hr;


}



