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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class TunerScanResult
{
   private Vector<CaptureDevice> scanResult = new Vector<>();
   StringBuffer xmlData = null;

   public TunerScanResult()
   {

   }

   public int readInput(InputStream inStream) throws Exception
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
      xmlData = new StringBuffer();

      String line = reader.readLine();
      while(line != null)
      {
         if(line.length() > 0)
         {
            xmlData.append(line);
            System.out.println(line);
         }
         line = reader.readLine();
      }

      return xmlData.length();
   }

   public int parseXML() throws Exception
   {
      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

         ByteArrayInputStream reader = new ByteArrayInputStream(xmlData.toString().getBytes());
         Document doc = docBuilder.parse(reader);

         NodeList devices = doc.getElementsByTagName("item");

         for(int x = 0; x < devices.getLength(); x++)
         {
            Node device = devices.item(x);

            NodeList nl = device.getChildNodes();

            String name = "";
            String id = "";

            for(int y = 0; y < nl.getLength(); y++)
            {
               Node item = nl.item(y);

               if(item.getNodeName().equals("name"))
               {
                  name = item.getTextContent();
               }
               else if(item.getNodeName().equals("id"))
               {
                  id = item.getTextContent();
               }
            }

            if(name.length() > 0 && id.length() > 0)
            {
               CaptureDevice dev = new CaptureDevice(name, id);
               scanResult.add(dev);
            }
         }

         return scanResult.size();
      }
      catch(Exception e)
      {
         System.out.println("ERROR parsing device XML.");
         System.out.println(xmlData);
         //e.printStackTrace();
         scanResult = new Vector<>();
         return 0;
      }
   }

   public Vector<CaptureDevice> getResult()
   {
      return scanResult;
   }

}
