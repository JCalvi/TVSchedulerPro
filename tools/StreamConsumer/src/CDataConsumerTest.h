#pragma once

#include <string>
#include <windows.h>
#include <streams.h>
#include <fstream>
#include <iostream>

#include "CDataConsumerParent.h"

class CDataConsumerTest : public CDataConsumerParent
{
private:
	std::string fileName;

public:
	CDataConsumerTest(void);
	~CDataConsumerTest(void);

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
