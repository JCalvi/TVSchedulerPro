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

import java.io.ByteArrayInputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CaptureCapabilities
{
   private static CaptureCapabilities instance = null;

   private Vector<CaptureCapability> capabilities = new Vector<>();

   private CaptureCapabilities()
   {
      getCapabilitiesFromEXE();
   }

   private void getCapabilitiesFromEXE()
   {
      try
      {
         int timeOut = 240; // 4 min

         CommandWaitThread command = new CommandWaitThread("win/StreamConsumer.exe -capabilities");
         Thread comWait = new Thread(Thread.currentThread().getThreadGroup(), command, command.getClass().getName());
         comWait.start();

         int count = 0;
         while(true)
         {
            if(command.isFinished())
            {
               break;
            }

            count++;
            if(count == timeOut)
            {
               throw new Exception("Capture capabilities load timed out (" + timeOut + ")");
            }
            Thread.sleep(1000);
         }

         parseCapabilities(command.getInputStream());

      }
      catch(Exception e)
      {
         System.out.println("ERROR getting capture capabilities from capture tool!");
         e.printStackTrace();

         // add at least one capability
         if(capabilities.size() == 0) {
			capabilities.add(new CaptureCapability("Error", 0, ".null"));
		 }
      }
   }

   private void parseCapabilities(String data) throws Exception
   {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(data.toString().getBytes());
      Document doc = docBuilder.parse(reader);

      NodeList capabilities = doc.getElementsByTagName("capability");

      for(int x = 0; x < capabilities.getLength(); x++)
      {
         Node capability = capabilities.item(x);

         NodeList nl = capability.getChildNodes();

         String name = "";
         int id = -1;
         String ext = "";

         for(int y = 0; y < nl.getLength(); y++)
         {
            Node item = nl.item(y);

            if(item.getNodeName().equals("name"))
            {
               name = item.getTextContent();
            }
            else if(item.getNodeName().equals("id"))
            {
               id = Integer.parseInt(item.getTextContent());
            }
            else if(item.getNodeName().equals("ext"))
            {
               ext = item.getTextContent();
            }
         }

         if(name.length() > 0 && id != -1 && ext.length() > 0)
         {
            this.capabilities.add(new CaptureCapability(name, id, ext));
         }
      }
   }

   public static CaptureCapabilities getInstance()
   {
      synchronized (CaptureCapabilities.class)
      {
         if (instance == null)
         {
            instance = new CaptureCapabilities();
            return instance;
         }
         else
         {
            return instance;
         }
      }
   }

   public Vector<CaptureCapability> getCapabilities()
   {
      return capabilities;
   }

   @Override
public String toString()
   {
      return "Capture Capabilities Loaded: " + capabilities.size();
   }

   public CaptureCapability getCapabiltyWithID(int id)
   {
      for (CaptureCapability capability : capabilities) {
         if(capability.getTypeID() == id) {
			return capability;
		 }
      }

      return null;
   }
}
