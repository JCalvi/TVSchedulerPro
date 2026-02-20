
#if !defined(AFX_CSTREAMINFO_H__F78D95D5_8151_4F20_8987_C1AD647F5E24__INCLUDED_)
#define AFX_CSTREAMINFO_H__F78D95D5_8151_4F20_8987_C1AD647F5E24__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000


enum TYPE
{
	TYPE_VIDEO = 0,
	TYPE_AUDIO_MPG = 1,
	TYPE_AUDIO_AC3 = 2,
	TYPE_TELETEXT = 3,
	TYPE_PRC = 4,
	TYPE_PGM = 5,
	TYPE_PRIVATE = 6,
};

class CStreamInfo  
{

public:

	CStreamInfo();
	virtual ~CStreamInfo();

	int getID();
	void setID(int pid);
	void setType(TYPE tp);
	TYPE getType();

private:

	int id;
	TYPE type;

};

#endif // !defined(AFX_CSTREAMINFO_H__F78D95D5_8151_4F20_8987_C1AD647F5E24__INCLUDED_)
