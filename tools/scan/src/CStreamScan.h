#pragma once

#include <streams.h>
#include <atlbase.h>
#include "Mpeg2data.h"
#include <vector>
#include <map>

#include "graph.h"
#include "CStreamInfo.h"

#define NTOH_S(s)   ( (((s) & 0xFF00) >> 8)  |  (((s) & 0x00FF) << 8) )
#define WORD_VALUE(pb,i)            (* (UNALIGNED WORD *) BYTE_OFFSET((pb),i))
#define BYTE_OFFSET(pb,i)           (& BYTE_VALUE((pb),i))
#define BYTE_VALUE(pb,i)            (((BYTE *) (pb))[i])

class CStreamScan
{
public:
	CStreamScan(void);
	~CStreamScan(void);

	int scanForProgs(IBaseFilter *psiFilter);
	int findProgramID(int pids);
	void printPrograms();

	int getTsPidArray(int programID, ULONG pidArray[], int arraySize);
	BOOL updateProgramInfo(CProgramInfo *info);

private:
	std::map<int, std::vector<CStreamInfo> > programs;

	int ProgNumbSave;
	int ProgIdSave;

	BOOL arrayContains(ULONG pidArray[], ULONG value, int max);
	HRESULT parseMpeg2Table(IMpeg2Data* pMpeg2Data, PID pid, TID tid, DWORD dwTimeout);
	HRESULT parseMpeg2Section(IMpeg2Data* pMpeg2Data, SECTION *pSection, DWORD dwPacketLength);
	int FindDescriptor(__int8 tag, unsigned char *buf, int remainingLength, const unsigned char **desc, int *descLen);

};
