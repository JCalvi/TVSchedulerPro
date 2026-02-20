#include "CDataConsumerTest.h"

CDataConsumerTest::CDataConsumerTest(void)
{
}

CDataConsumerTest::~CDataConsumerTest(void)
{
}

void CDataConsumerTest::setmemoryShareName(std::string name)
{
	
}

void CDataConsumerTest::setPIDs(int prog, int video, int audio, int audio_type)
{

}

void CDataConsumerTest::setCaptureType(int type)
{

}

void CDataConsumerTest::setFileName(std::string name)
{
	fileName = name;
}

HRESULT CDataConsumerTest::buildCaptureGraph()
{
	return S_OK;
}

BOOLEAN CDataConsumerTest::isDataFlowing()
{
	return TRUE;
}

BOOLEAN CDataConsumerTest::getSignalStats(long *quality, long *strength)
{
	*quality = 100;
	*strength = 100;

	return TRUE;
}

BOOLEAN CDataConsumerTest::hasFallenBehind()
{
	return FALSE;
}

HRESULT CDataConsumerTest::runGraph(int *result)
{
	std::ofstream fw;
	fw.open(fileName.c_str());
	fw << "Test Capture File";
	fw.close();

	return S_OK;
}

HRESULT CDataConsumerTest::stopGraph()
{
	return S_OK;
}
