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
#pragma warning(disable: 4995)
#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <strsafe.h>
#include <vector>
#include <streams.h>
#include <atlbase.h>

#include <BDATYPES.H>
#include <ks.h>
#include <ksmedia.h>
#include <bdamedia.h>  
#include <string>


struct DeviceDetails {
	std::string name;
	std::string id;
};

BOOL scanForDevices(std::vector <DeviceDetails> *devices)
{
    HRESULT hr = S_OK;
    CComPtr <IMoniker> pIMoniker;
    CComPtr <IEnumMoniker> pIEnumMoniker;
	CComPtr <ICreateDevEnum> m_pICreateDevEnum;

	WCHAR *wszDisplayName = NULL;

    hr = m_pICreateDevEnum.CoCreateInstance(CLSID_SystemDeviceEnum);
    if (FAILED (hr))
    {
        printf("Cannot CoCreate CLSID_SystemDeviceEnum\n");
        return FALSE;
    }

    hr = m_pICreateDevEnum->CreateClassEnumerator(KSCATEGORY_BDA_NETWORK_TUNER, &pIEnumMoniker, 0);
    // the call can return S_FALSE if no moniker exists, so explicitly check S_OK
    if (FAILED (hr))
    {
        printf("win32-DLL-BDA : queryTunerDevices(): Cannot CreateClassEnumerator\n");
        return FALSE;
    }
    if (S_OK != hr)  // Class not found
    {
        printf("win32-DLL-BDA : queryTunerDevices(): Class not found, CreateClassEnumerator returned S_FALSE");
        return FALSE;
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
			printf("queryTunerDevices(): Cannot BindToStorage");
			return FALSE;
		}

		CComVariant varBSTR;
		hr = pBag->Read(L"FriendlyName", &varBSTR, NULL);
		if(FAILED(hr))
		{
			printf("queryTunerDevices(): IPropertyBag->Read method failed");
			pIMoniker = NULL;
			continue;
		}

		char buff[512];
		StringCchPrintf(buff, 512, "%S", varBSTR.bstrVal);
		DeviceDetails dev;
		dev.name = buff;

		wszDisplayName = 0;
		hr = pIMoniker->GetDisplayName(0, 0, &wszDisplayName);

		if(FAILED(hr))
		{
			printf("GetDisplayName(): method failed");
			pIMoniker = NULL;
			continue;
		}

		StringCchPrintf(buff, 512, "%S", wszDisplayName);
		dev.id = buff;

		CoTaskMemFree(wszDisplayName);

		devices->push_back(dev);
		
		pIMoniker = NULL;
	}

	return TRUE;
}

void getXML(std::vector <DeviceDetails> *devices, std::string &out)
{
	out = "";
	out.append("<items>\n");

	for(std::vector<DeviceDetails>::size_type x = 0; x < devices->size(); x++)
	{
		DeviceDetails dev = devices->at(x);

		out.append("<item>\n");
		out.append("<name>");
		out.append(dev.name.c_str());
		out.append("</name>\n");
		char buff[MAX_PATH] = {0};
		StringCchPrintf(buff, MAX_PATH, "<id><![CDATA[%s]]></id>\n", dev.id.c_str());
		out.append(buff);
		out.append("</item>\n");
	}

	out.append("</items>\n");
}

void printTestDeviceList()
{
	std::string capabilities = "";

	capabilities.append("<items>\n");

	capabilities.append("<item>\n");
	capabilities.append("<name>");
	capabilities.append("Test Device 01");
	capabilities.append("</name>\n");
	capabilities.append("<id><![CDATA[DEVICE_01]]></id>\n");
	capabilities.append("</item>\n");

	capabilities.append("<item>\n");
	capabilities.append("<name>");
	capabilities.append("Test Device 02");
	capabilities.append("</name>\n");
	capabilities.append("<id><![CDATA[DEVICE_02]]></id>\n");
	capabilities.append("</item>\n");

	capabilities.append("</items>\n");

	printf(capabilities.c_str());
	fflush(stdout);
}

int _tmain(int argc, _TCHAR* argv[])
{
	if(argc > 1 && strcmp(argv[1], "-test") == 0)
	{
		printTestDeviceList();
		ExitProcess(0);
	}

	CoInitialize(NULL);

	std::vector<DeviceDetails> tuners;
	BOOL worked = scanForDevices(&tuners);

	int code = 0;

	if(worked)
	{
		std::string out("");
		getXML(&tuners, out);

		printf("%s\n", out.c_str());
		code = 0;
	}
	else
	{
		code = -1;
	}

	CoUninitialize();

	return code;
}