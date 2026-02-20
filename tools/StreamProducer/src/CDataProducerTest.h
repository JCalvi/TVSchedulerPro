#pragma once

#include "CDataProducerParent.h"

#include "windows.h"
#include <string>

class CDataProducerTest : public CDataProducerParent
{
public:
	CDataProducerTest(void);
	~CDataProducerTest(void);

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
