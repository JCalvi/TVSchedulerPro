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

//#include <tchar.h>

#include "graph.h"
#include "log.h"
//#include <stdio.h>
//#include <string>
#include <sstream>
#include "CrashDump.h"


std::string removeNonAlpha(std::string text)
{
	std::string final(text);

	for(std::string::size_type x = 0; x < text.length(); x++)
	{
		char charAt = final.at(x);

		if(((charAt >= 'a' && charAt <= 'z') ||
			(charAt >= 'A' && charAt <= 'Z') ||
            (charAt >= '0' && charAt <= '9') || 
			charAt == ' ') == false)
		{
			final.replace(x, 1, "-");
		}
	}

	return final;
}

void printTestChannels()
{
	std::string xmlOut = "";
	xmlOut.append("<items>\r\n");

	xmlOut.append("<item>\r\n");
	xmlOut.append("<name>Channel 01</name>\r\n");
	xmlOut.append("<id>101</id>\r\n");

	xmlOut.append("<stream>\r\n");
	xmlOut.append("<id>100</id>\r\n");
	xmlOut.append("<type>1</type>\r\n");
	xmlOut.append("</stream>\r\n");

	xmlOut.append("<stream>\r\n");
	xmlOut.append("<id>101</id>\r\n");
	xmlOut.append("<type>0</type>\r\n");
	xmlOut.append("</stream>\r\n");

	xmlOut.append("</item>\r\n");

	xmlOut.append("<item>\r\n");
	xmlOut.append("<name>Channel 02</name>\r\n");
	xmlOut.append("<id>201</id>\r\n");

	xmlOut.append("<stream>\r\n");
	xmlOut.append("<id>200</id>\r\n");
	xmlOut.append("<type>1</type>\r\n");
	xmlOut.append("</stream>\r\n");

	xmlOut.append("<stream>\r\n");
	xmlOut.append("<id>201</id>\r\n");
	xmlOut.append("<type>0</type>\r\n");
	xmlOut.append("</stream>\r\n");

	xmlOut.append("</item>\r\n");

	xmlOut.append("</items>\r\n");

	printf(xmlOut.c_str());
	fflush(stdout);
}


int _tmain(int argc, _TCHAR* argv[])
{
	SetUnhandledExceptionFilter(MyUnHandleExceptionFilter);

	if(argc > 1 && strcmp(argv[1], "-test") == 0)
	{
		printTestChannels();
		return 0;
	}

	if(argc < 4)
	{
		return -1;
	}

	int freq = -1;
	std::string freqS((char*)(argv[1]));
	std::istringstream f(freqS);
	if (!(f >> freq))
		freq = -1;

	int band = -1;
	std::string bandS((char*)(argv[2]));
	std::istringstream b(bandS);
	if (!(b >> band))
		band = -1;

//	int deviceID = -1;
	std::string deviceS((char*)(argv[3]));
//	std::istringstream d(deviceS);
//	if (!(d >> deviceID))
//		deviceID = -1;

	if(freq == -1 || band == -1 || deviceS.length() == 0)
	{
		printf("Parameter not correct");
		fflush(stdout);
		return -2;
	}

	openLogFile("ChannelScan-");

	CoInitialize(NULL);

	CBDAFilterGraph *p_BDAgraph = new CBDAFilterGraph();

	p_BDAgraph->SetFrequency(freq);
	p_BDAgraph->SetBandwidth(band);

	HRESULT hr = p_BDAgraph->BuildGraph(DVB_T, deviceS);
	if(FAILED(hr))
	{
		log("BuildGraph failed with : 0x%x\r\n", hr);
		delete p_BDAgraph;
		closeLogFile();
		return -3;
	}

	hr = p_BDAgraph->LoadPsiFilter();
	if(FAILED(hr))
	{
		log("LoadPsiFilter failed with : 0x%x\r\n", hr);
		delete p_BDAgraph;
		closeLogFile();
		return -4;
	}

	hr = p_BDAgraph->ChangeChannel();
	if(FAILED(hr))
	{
		log("ChangeChannel failed with : 0x%x\r\n", hr);
		delete p_BDAgraph;
		closeLogFile();
		return -5;
	}

	hr = p_BDAgraph->RunGraph(NULL);
	if(FAILED(hr))
	{
		log("RunGraph failed with : 0x%x\r\n", hr);
		p_BDAgraph->StopGraph();
		delete p_BDAgraph;
		closeLogFile();
		return -6;
	}

	std::vector<CProgramInfo> ppinfo;
	hr = p_BDAgraph->getProgramInfo(&ppinfo);
	if(FAILED(hr))
	{
		log("getProgramInfo failed with : 0x%x\r\n", hr);
		p_BDAgraph->StopGraph();
		delete p_BDAgraph;
		closeLogFile();
		return -7;
	}

	hr = p_BDAgraph->StopGraph();

	delete p_BDAgraph;

	CoUninitialize();

	log("Scan Finished now sending XML\r\n");

	std::string xmlOut = "";

	xmlOut.append("<items>\r\n");
	char buff[256];

	for(std::vector<CProgramInfo>::size_type x = 0; x < ppinfo.size(); x++)
	{
		CProgramInfo prog = ppinfo.at(x);
		xmlOut.append("<item>\r\n");
		
		StringCchPrintf(buff, 256, "<name>%s</name>\r\n", removeNonAlpha(prog.programName).c_str());
		xmlOut.append(buff);
		StringCchPrintf(buff, 256, "<id>%d</id>\r\n", prog.programID);
		xmlOut.append(buff);

		for(std::vector<CStreamInfo>::size_type y = 0; y < prog.streams.size(); y++)
		{
			CStreamInfo si = prog.streams.at(y);
			xmlOut.append("<stream>\r\n");
			StringCchPrintf(buff, 256, "<id>%d</id>\r\n", si.getID());
			xmlOut.append(buff);
			StringCchPrintf(buff, 256, "<type>%d</type>\r\n", si.getType());
			xmlOut.append(buff);
			xmlOut.append("</stream>\r\n");
		}

		xmlOut.append("</item>\r\n");
	}

	xmlOut.append("</items>\r\n");

	printf(xmlOut.c_str());
	fflush(stdout);

	log("\n%s", xmlOut.c_str());

	closeLogFile();
	ExitProcess(0);

	return 0;
}

