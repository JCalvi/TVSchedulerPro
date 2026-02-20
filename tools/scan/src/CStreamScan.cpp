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


#include ".\CStreamScan.h"

CStreamScan::CStreamScan(void)
{
}

CStreamScan::~CStreamScan(void)
{
}

BOOL CStreamScan::arrayContains(ULONG pidArray[], ULONG value, int max)
{
	for(int x = 0; x < max; x++)
	{
		if(pidArray[x] == value)
			return TRUE;
	}
	
	return FALSE;
}

int CStreamScan::getTsPidArray(int programID, ULONG pidArray[], int arraySize)
{
	std::map<int, std::vector<CStreamInfo> >::iterator pidsItor = programs.find(programID);
	std::vector<CStreamInfo> pids = pidsItor->second;
	int count = 0;

	if(pidsItor != programs.end())
	{
		for(std::vector<CStreamInfo>::size_type x = 0; x < pids.size() && (int)x < arraySize; x++)
		{
			if(!arrayContains(pidArray, pids.at(x).getID(), count))
			{
				pidArray[count++] = pids.at(x).getID();
			}
		}
	}

	pidArray[count++] = 0;
	pidArray[count++] = 16;
	pidArray[count++] = 17;
	pidArray[count++] = 18;
	pidArray[count++] = 19;
	pidArray[count++] = 20;

	return count;
}

void CStreamScan::printPrograms()
{
	// Just print out all the available 
	// programs we have detected
	for (std::map<int, std::vector<CStreamInfo> >::iterator mi1 = programs.begin(); mi1 != programs.end(); mi1++)
	{
		int progID = mi1->first;
		std::vector<CStreamInfo> pids = mi1->second;

		char buff[1024];
		StringCchPrintf(buff, 1024, "Program (%d) :", progID);

		for(std::vector<CStreamInfo>::size_type y = 0; y < pids.size(); y++)
		{
			CStreamInfo pid = pids.at(y);

			char pidData[24];
			StringCchPrintf(pidData, 24, " (%d", pid.getID());

			switch(pid.getType())
			{
				case TYPE_VIDEO:
					StringCchCat(pidData, 24, "-VID)");
					break;
				case TYPE_AUDIO_MPG:
					StringCchCat(pidData, 24, "-MPG)");
					break;
				case TYPE_AUDIO_AC3:
					StringCchCat(pidData, 24, "-AC3)");
					break;
				case TYPE_TELETEXT:
					StringCchCat(pidData, 24, "-TXT)");
					break;
				case TYPE_PRC:
					StringCchCat(pidData, 24, "-PCR)");
					break;
				case TYPE_PRIVATE:
					StringCchCat(pidData, 24, "-PRI)");
					break;
				case TYPE_PGM:
					StringCchCat(pidData, 24, "-PGM)");
					break;
				default:
					StringCchCat(pidData, 24, ")");
					break;
			}

			StringCchCat(buff, 1024, pidData);
		}
		StringCchCat(buff, 1024, "\r\n");
		log(buff);
	}
}

int CStreamScan::findProgramID(int pid)
{

	for (std::map<int, std::vector<CStreamInfo> >::iterator mi = programs.begin(); mi != programs.end(); mi++)
	{
		int progID = mi->first;
		std::vector<CStreamInfo> pids = mi->second;

		//log("Searching for Pid (%d) in Program (%d)\r\n", pid, progID);

		for(std::vector<CStreamInfo>::size_type y = 0; y < pids.size(); y++)
		{
			if(pids.at(y).getID() == pid)
			{
				log("Found Pid (%d) in Program (%d)\r\n", pid, progID);
				return progID;
			}
		}
	}

	return -1;
}

int CStreamScan::scanForProgs(IBaseFilter *psiFilter)
{
	CComQIPtr <IMpeg2Data> pMpeg2Data = psiFilter;
	log("Starting program Scan\r\n");

	if(pMpeg2Data == NULL)
	{
		log("PSI filter is NULL!\r\n");
		return -1;
	}

	HRESULT hr = S_OK;
	int retries = 0;
	while(SUCCEEDED(hr) && programs.size() == 0 && retries++ < 10)
	{
		// Get program map 
		hr = parseMpeg2Table(pMpeg2Data, 0x0000, 0x00, 10000);

		if (FAILED(hr))
		{
			log("Unable to gather all of the Table & Sections data\r\n");
			hr = S_OK;
			Sleep(1000);
		}
	}

	printPrograms();

	return (int)programs.size();
}


HRESULT CStreamScan::parseMpeg2Table
(
 IMpeg2Data* pMpeg2Data,
 PID pid,
 TID tid,
 DWORD dwTimeout
 )
{
    HRESULT hr = E_POINTER;

	if (pMpeg2Data != NULL)
	{
		CComPtr<ISectionList> piSectionList;
        hr = pMpeg2Data->GetTable(pid, tid, NULL, dwTimeout, &piSectionList);
		if (SUCCEEDED(hr))
		{
			WORD cSections;
            hr = piSectionList->GetNumberOfSections(&cSections);
			if (SUCCEEDED(hr))
			{
				for (WORD i = 0; i < cSections; i++)
				{
					// Iterate through the list of sections.
					SECTION* pSection;
					DWORD size;
					
					hr = piSectionList->GetSectionData(i, &size, &pSection);
					
					if (SUCCEEDED(hr))
					{
						hr = parseMpeg2Section(pMpeg2Data, pSection, size);
                        if (!SUCCEEDED(hr))
                        {
                            return hr;
                        }
					}
				}
			}
			else
			{
				log(_T("Error 0x%x getting number of sections\r\n"), hr); 
			}
		}
		else
		{
			log(_T("Timeout getting table (pid=%.4x, tid=%.2x)\r\n"), pid, tid);
		}
	}
	
    return hr;
}

HRESULT CStreamScan::parseMpeg2Section
(
 IMpeg2Data* pMpeg2Data,
 SECTION *pSection,
 DWORD dwPacketLength
 )
{ 
    if (!pSection)
        return E_POINTER;
	
    if (dwPacketLength < sizeof(SECTION))
    {
        log("Malformed MPEG-2 section data.\r\n");
        return E_FAIL;
    }
	
    HRESULT hr = S_OK;
	
    // Coerce the header bits to a bit field structure.
    MPEG_HEADER_BITS *pHeader = (MPEG_HEADER_BITS*)&pSection->Header.W;
	
    if (pHeader->SectionSyntaxIndicator)
    { 
        // Coerce the section structure to a long section header. 
        LONG_SECTION *pLong = (LONG_SECTION*) pSection; 
		
        MPEG_HEADER_VERSION_BITS *pVersion = (MPEG_HEADER_VERSION_BITS*)&pLong->Version.B; 
		
		switch (pSection->TableId)
		{

		case 0x00:	//PAT
			{
				BYTE* pbBuf = pLong->RemainingData;
				int i = 0;
				while (i < (pHeader->SectionLength - 5))
				{
					int programNumber = (pbBuf[i] << 8) | pbBuf[i+1];

					//Check if an NIT pid is specified, ie 0 program number
					if(!programNumber)
					{
						i += 4;
						continue;
					}

					int programId = ((pbBuf[i+2] & 0x1f) << 8) | pbBuf[i+3];
					ProgNumbSave = programNumber;
					ProgIdSave = programId;
					//log("programNumber= %d programId = %d total numb = %d\r\n", programNumber, programId, number_of_programs);
					//log("programNumber= %d programId = %d\r\n", programNumber, programId);

					if ((pbBuf[i+2] & 0xe0) == 0xe0)
					{
						if (programNumber != 0)
						{
							hr = parseMpeg2Table(pMpeg2Data, programId, 0x02, 5000);
							if (!SUCCEEDED(hr))
							{
								return hr;
							}
						}
					}
					i += 4;
				}

				break;
			}
		
        case 0x01:	//CAT
			{
				break;
			}
			
        case 0x02:	//PMT
			{
				
				int program_info_length = (NTOH_S(WORD_VALUE(pSection, 10)) & 0x0fff);
				
				BYTE* pStartEs  = (BYTE*)pSection + 12 + program_info_length;
				BYTE* pEndEs    = (BYTE*)pSection + pHeader->SectionLength + 3 - 4;   //points to the byte before CRS_323, 4
				BYTE* pTmp ;
				
				unsigned short EsNumber = 0;
				int offset = 0;
				BYTE streamType;
				
				if (ProgNumbSave != 0)
				{
					//programs[ProgNumbSave].push_back(0);

					//pgm_numbs[progcount] = ProgNumbSave;
					//progcount++;
					//log("progcount= %d Pgm = %d\r\n", progcount, pgm_numbs[progcount - 1]);
				} 
				
				if (ProgIdSave != 0)
				{
					CStreamInfo info;
					info.setID(ProgIdSave);
					info.setType(TYPE_PGM);
					programs[ProgNumbSave].push_back(info);
					//pgm_pids[ProgNumbSave] = ProgIdSave;
					//log("ProgNumbSave= %d ProgIdSave = %d\r\n", ProgNumbSave, pgm_pids[ProgNumbSave]);
				} 
				
				if ((NTOH_S(WORD_VALUE(pSection,8)) & 0x1fff) != 0)
				{
					CStreamInfo info;
					info.setID(NTOH_S(WORD_VALUE(pSection,8)) & 0x1fff);
					info.setType(TYPE_PRC);
					programs[ProgNumbSave].push_back(info);
					//pcr_pids[ProgNumbSave] = NTOH_S(WORD_VALUE(pSection,8)) & 0x1fff;
					//log("ProgNumbSave= %d pcrpid = %d\r\n", ProgNumbSave, pcr_pids[ProgNumbSave]);
				} 
				
//				if (ProgChanSave != 0)
//				{
					//log("ProgNumbSave= %d ProgChanSave = %d\r\n", ProgNumbSave, ProgChanSave);
//				}
				
				int audio_pid = -1;
				int telex_pid = -1;
				int video_pid = -1;
				int ac3aud_pid = -1;
				int private_pid = -1;
				
				for(pTmp = pStartEs; pTmp < pEndEs; pTmp += offset)
				{
					int esInfoLength = (NTOH_S(WORD_VALUE(pTmp,3)) & 0x0fff);
					offset = 5 +  esInfoLength; //5+ES_info_length
					
					int esPID = (NTOH_S(WORD_VALUE(pTmp,1)) & 0x1fff);
					
					streamType = BYTE_VALUE(pTmp,0);

					log("Stream Type=0x%x PID=%d\r\n", streamType, esPID);

					switch(streamType)
					{
						
					case 0x03:
					case 0x04:
					case 0x11:
						audio_pid = esPID;

						if (audio_pid != 0)
						{
							CStreamInfo info;
							info.setID(audio_pid);
							info.setType(TYPE_AUDIO_MPG);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_AUDIO_MPG)\r\n", ProgNumbSave, audio_pid);
						} 
						break;

                    case 0x02:
					case 0x1b:
						video_pid = esPID;
						
						if (video_pid != 0)
						{
							CStreamInfo info;
							info.setID(video_pid);
							info.setType(TYPE_VIDEO);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_VIDEO)\r\n", ProgNumbSave, video_pid);
						} 
						break;
						
                    case 0x06:
						
						log("Stream Type=0x%x Sub Type=0x%x PID=%d\r\n", streamType, pTmp[5], esPID);

						if (FindDescriptor(0x6a, &(pTmp[5]), esInfoLength, NULL, NULL))
						//if ((esInfoLength > 0) && (pTmp[5] == 0x6a))
						{
							ac3aud_pid = esPID;
							CStreamInfo info;
							info.setID(ac3aud_pid);
							info.setType(TYPE_AUDIO_AC3);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_AUDIO_AC3)\r\n", ProgNumbSave, ac3aud_pid);
						}
						else if (FindDescriptor(0x7a, &(pTmp[5]), esInfoLength, NULL, NULL))
						{
							ac3aud_pid = esPID;
							CStreamInfo info;
							info.setID(ac3aud_pid);
							info.setType(TYPE_AUDIO_AC3);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_AUDIO_AC3)\n", ProgNumbSave, ac3aud_pid);
						}
						else if (FindDescriptor(0x59, &(pTmp[5]), esInfoLength, NULL, NULL))
						//else if ((esInfoLength > 0) && (pTmp[5] == 0x59))
						{
							/* Note: The subtitling descriptor can also signal
							 * teletext subtitling, but then the teletext descriptor
							 * will also be present; so we can be quite confident
							 * that we catch DVB subtitling streams only here, w/o
							 * parsing the descriptor. */
							telex_pid = esPID;
							CStreamInfo info;
							info.setID(telex_pid);
							info.setType(TYPE_TELETEXT);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_TELETEXT)\r\n", ProgNumbSave, telex_pid);
						}
						else if (FindDescriptor(0x56, &(pTmp[5]), esInfoLength, NULL, NULL))
						//else if ((esInfoLength > 0) && (pTmp[5] == 0x56))
						{
							telex_pid = esPID;
							CStreamInfo info;
							info.setID(telex_pid);
							info.setType(TYPE_TELETEXT);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_TELETEXT)\r\n", ProgNumbSave, telex_pid);
						}
						else
						{
							private_pid = esPID;
							CStreamInfo info;
							info.setID(private_pid);
							info.setType(TYPE_PRIVATE);
							programs[ProgNumbSave].push_back(info);
							log("Add Stream %d %d(TYPE_PRIVATE)\r\n", ProgNumbSave, telex_pid);							
						}

						break;
					}
					
					EsNumber++;
				}

				/*
				//Set Private as Teletext or AC3
				if (private_pid != -1)
				{
					if (audio_pid == -1 && ac3aud_pid != -1)
					{
						CStreamInfo info;
						info.setID(private_pid);
						info.setType(TYPE_AUDIO_AC3);
						programs[ProgNumbSave].push_back(info);
						log("Private01 Add Stream %d %d(TYPE_AUDIO_AC3)\r\n", ProgNumbSave, private_pid);
					} 
					else if (ac3aud_pid == -1 && telex_pid != -1)
					{
						CStreamInfo info;
						info.setID(private_pid);
						info.setType(TYPE_AUDIO_AC3);
						programs[ProgNumbSave].push_back(info);
						log("Private02 Add Stream %d %d(TYPE_AUDIO_AC3)\r\n", ProgNumbSave, private_pid);
					}
					else if (ac3aud_pid == -1 && telex_pid == -1)
					{
						CStreamInfo info;
						info.setID(private_pid);
						info.setType(TYPE_AUDIO_AC3);
						programs[ProgNumbSave].push_back(info);
						log("Private03 Add Stream %d %d(TYPE_TELETEXT)\r\n", ProgNumbSave, private_pid);
					}
				}
				*/

				//number_of_elementary_streams = EsNumber;

				break;
			}
		
        default:
			break;
        }
    }
    else
    {
        // Not a long section header.
    }
	
    return hr;
}

int CStreamScan::FindDescriptor(__int8 tag, unsigned char *buf, int remainingLength, const unsigned char **desc, int *descLen)
{
	while (remainingLength > 0) 
	{
		unsigned char descriptorTag = buf[0];
		unsigned char descriptorLen = buf[1];
		
		if (!descriptorLen)
		{
			log("descriptorTag == 0x%02x, len is 0\r\n", descriptorTag);
			break;
		}
		
		if (tag == descriptorTag)
		{
			if (desc)
				*desc = buf + 2;
			if (descLen)
				*descLen = descriptorLen;
			return 1;
		}
		
		buf += (descriptorLen + 2);
		remainingLength -= (descriptorLen + 2);
	}
	return 0;
}

BOOL CStreamScan::updateProgramInfo(CProgramInfo *info)
{
	//log("Updating Program (%s) : %d\r\n", info->programName, info->programID);
	std::map<int, std::vector<CStreamInfo> >::iterator pidsItor = programs.find(info->programID);

	if(pidsItor != programs.end())
	{
		std::vector<CStreamInfo> pids = pidsItor->second;

		for(std::vector<CStreamInfo>::size_type x = 0; x < pids.size(); x++)
		{
			if(	pids.at(x).getType() == TYPE_AUDIO_AC3 || 
				pids.at(x).getType() == TYPE_AUDIO_MPG || 
				pids.at(x).getType() == TYPE_VIDEO)
			{
				CStreamInfo newStream;
				newStream.setID(pids.at(x).getID());
				newStream.setType(pids.at(x).getType());
				info->streams.push_back(newStream);
			}
		}
	}
	else
	{
		info->programID = -1;
		StringCchCopy(info->programName, 256, "ERROR");
	}

	return TRUE;
}
