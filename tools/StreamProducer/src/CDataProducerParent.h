#pragma once

#include "windows.h"
#include <string>

class CDataProducerParent
{
public:
	CDataProducerParent(void);
	~CDataProducerParent(void);

	virtual void setTuneData(int freq, int band) = 0;
	virtual void setMemoryShareName(std::string name) = 0;
	virtual HRESULT submitTuneRequest() = 0;
	virtual void setDevice(std::string device) = 0;
	virtual HRESULT buildGraph() = 0;
	virtual HRESULT runGraph() = 0;
	virtual HRESULT stopGraph() = 0;
	virtual BOOLEAN isDataFlowing() = 0;
	virtual HRESULT logSignalValues() = 0;

};
