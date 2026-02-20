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

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

class ApplyTransformRes extends HTTPResponse
{

   public ApplyTransformRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      outStream.write(doTransform(urlData, headers));
   }

   private byte[] doTransform(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String xml = urlData.getParameter("xml");
      String xsl = urlData.getParameter("xsl");

      String httpDir = store.getProperty("path.httproot");
      String themeDir = store.getProperty("path.theme");

      String userAgent = headers.get("User-Agent");

      if(userAgent != null && userAgent.length() > 0)
      {
         String[] agents = store.getAgentMappingList();
         for (String agent : agents) {
            if(userAgent.indexOf(agent) > -1)
            {
               themeDir = store.getThemeForAgent(agent);
            }
         }
      }

      xml = httpDir + File.separator + "themes" + File.separator + themeDir + File.separator + "xml" + File.separator + xml + ".xml";

   	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
   	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      File xmlSource = new File(xml);
      if(!xmlSource.exists()) {
		throw new Exception("Source XML file (" + xml + ") not found");
	  }

   	Document doc = docBuilder.parse(xmlSource);

   	//
   	// Do transform and return data
   	//
   	XSL transformer = new XSL(doc, xsl + ".xsl", urlData, headers);

      //if(mark != null && mark.equalsIgnoreCase("yes"))
         transformer.addCookie("backURL", urlData.getReqString());

      return transformer.doTransform();
   }

}