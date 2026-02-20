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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class XSL
{
   private Document document = null;
   private HTTPurl urlData = null;
   private String xslName = "";
   private HashMap<String, String> headers = null;
   private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy H:mm:ss");
   private Vector<String> cookies = new Vector<>();

   public XSL(Document doc, String name, HTTPurl data, HashMap<String, String> head)
   {
      document = doc;
      xslName = name;
      urlData = data;
      headers = head;
   }

   public void addCookie(String n, String d) throws Exception
   {
      String name = URLEncoder.encode(n, "UTF-8");
      String data = URLEncoder.encode(d, "UTF-8");
      cookies.add(name + "=" + data + "; PATH=/");
   }

   public byte[] doTransform() throws Exception
   {
      return doTransform(true);
   }

   public byte[] doTransform(boolean theme) throws Exception
   {
      DataStore store = DataStore.getInstance();
      String path = "";
      String httpDir = store.getProperty("path.httproot");

      if(theme)
      {
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
         // KB Theme - XSL transforms
         path = httpDir + File.separator + "themes" + File.separator + themeDir + File.separator + "xsl" + File.separator + xslName;
      }
      else
      {
         // HTTP Theme - XSL transforms
         path = httpDir + File.separator + "xsl" + File.separator + xslName;
      }

      //
      // Send output back
      //
      ByteArrayOutputStream buff = new ByteArrayOutputStream();
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer xformer = null;

      // Apply the XSL template or NOT
      if (!"1".equalsIgnoreCase(urlData.getParameter("xml")))
      {
         File fileIN = new File(path);
         if(!fileIN.exists()) {
			throw new Exception("XSL file (" + path + ") not found");
		 }

         Templates template = factory.newTemplates(new StreamSource(new FileInputStream(fileIN)));
         xformer = template.newTransformer();
         buff.write("HTTP/1.0 200 OK\nContent-Type: text/html\n".getBytes());
      }
      else
      {
         buff.write("HTTP/1.0 200 OK\nContent-Type: text/xml\n".getBytes());
         xformer = factory.newTransformer();
      }

      String timeStamp = df.format(new Date());
      timeStamp += " GMT";

      buff.write(("Pragma: no-cache\n").getBytes());
      buff.write(("Cache-Control: no-cache\n").getBytes());
      buff.write(("Last-Modified: " + timeStamp + "\n").getBytes());
      buff.write(("Date: " + timeStamp + "\n").getBytes());
      buff.write(("Expires: " + timeStamp + "\n").getBytes());

      for (String element : cookies) {
         String cook = element;
         buff.write(("Set-Cookie: " + cook + "\n").getBytes());
      }

      buff.write("\n".getBytes());

      Source source = new DOMSource(document);
      Result result = new StreamResult(buff);
      xformer.transform(source, result);

      return buff.toByteArray();
   }
}
