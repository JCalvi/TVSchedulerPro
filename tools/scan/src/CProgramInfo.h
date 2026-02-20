

#if !defined(AFX_CPROGRAMINFO_H__D1E19EDD_5164_4B0D_A182_1B9DFCCCEEC4__INCLUDED_)
#define AFX_CPROGRAMINFO_H__D1E19EDD_5164_4B0D_A182_1B9DFCCCEEC4__INCLUDED_


#include <vector>
#include "CStreamInfo.h"


class CProgramInfo  
{

public:

	CProgramInfo();
	virtual ~CProgramInfo();

	char programName[256];
	int programID;

	std::vector<CStreamInfo> streams;

};

#endif // !defined(AFX_CPROGRAMINFO_H__D1E19EDD_5164_4B0D_A182_1B9DFCCCEEC4__INCLUDED_)
