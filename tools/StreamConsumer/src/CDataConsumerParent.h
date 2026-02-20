#pragma once

#include <string>
#include <windows.h>

class CDataConsumerParent
{
public:
	CDataConsumerParent(void);
	~CDataConsumerParent(void);

	virtual void setmemoryShareName(std::string name) = 0;
	virtual void setPIDs(int prog, int video, int audio, int audio_type) = 0;
	virtual void setCaptureType(int type) = 0;
	virtual void setFileName(std::string name) = 0;

	virtual HRESULT buildCaptureGraph() = 0;

	virtual BOOLEAN isDataFlowing() = 0;
	virtual BOOLEAN getSignalStats(long *quality, long *strength) = 0;
	virtual BOOLEAN hasFallenBehind() = 0;

	virtual HRESULT runGraph(int *result) = 0;
	virtual HRESULT stopGraph() = 0;
};
