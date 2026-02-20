/*
* Copyright (c) 2009 Blue Bit Solutions
* Copyright (c) 2010-2024 John Calvi
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

import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class KBSysInfoRes extends HTTPResponse
{

   public KBSysInfoRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if("01".equals(urlData.getParameter("action")))
      {
         return;
      }
      else
      {
         outStream.write(showSysInfo(urlData, headers));
         return;
      }
   }

   private byte[] showSysInfo(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "sys-info", null);

      Element root = doc.getDocumentElement();
      Element item = null;

      // Server Version
      item = doc.createElement("info");
      item.setAttribute("name" , "Server Version");
      item.setAttribute("units" , "");
      item.setAttribute("value" , store.getVersion());
      root.appendChild(item);

      // Num Sch
      item = doc.createElement("info");
      item.setAttribute("name" , "Number of Schedules");
      item.setAttribute("units" , "");

      int numSch = store.getScheduleCount();

      item.setAttribute("value" , Integer.valueOf(numSch).toString());
      root.appendChild(item);

      // Num Running
      CaptureDeviceList devList = CaptureDeviceList.getInstance();
      item = doc.createElement("info");
      item.setAttribute("name" , "Cards in use");
      item.setAttribute("units" , "");
      item.setAttribute("value" , Integer.valueOf(devList.getActiveDeviceCount()).toString());
      root.appendChild(item);

      // To Next
      Date now = new Date();
      int timeToNext = -1;
      ScheduleItem next = store.getNextSchedule();
      if(next != null)
      {
         timeToNext = (int)((next.getStart().getTime() - now.getTime()) / (1000 * 60));
         if(timeToNext < 0) {
			timeToNext = 1;
		 }
      }

      item = doc.createElement("info");
      item.setAttribute("name" , "Time to next");
      item.setAttribute("units" , "");
      item.setAttribute("value" , Integer.valueOf(timeToNext).toString());
      root.appendChild(item);


      // Do transform and return data
      XSL transformer = new XSL(doc, "kb-sys-info.xsl", urlData, headers);
      return transformer.doTransform();
   }
}