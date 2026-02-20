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

class ScanResult
{
   StringBuffer xmlData = null;
   private Vector<Channel> scanResult = new Vector<>();
   private int freq = 0;
   private int band = 0;

   public ScanResult(int freq, int band)
   {
      this.freq = freq;
      this.band = band;
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

         NodeList programs = doc.getElementsByTagName("item");

         for(int x = 0; x < programs.getLength(); x++)
         {
            Node program = programs.item(x);

            NodeList nl = program.getChildNodes();

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

            Channel chan = null;
            if(name.length() > 0 && id.length() > 0)
            {
               int idInt = Integer.parseInt(id);
               chan = new Channel(name, freq, band, idInt, -1, -1);
            }

            if(chan != null)
            {
               for(int y = 0; y < nl.getLength(); y++)
               {
                  Node item = nl.item(y);

                  if(item.getNodeName().equals("stream"))
                  {
                     NodeList dataList = item.getChildNodes();

                     int streamID = -1;
                     int streamType = -1;

                     for(int z = 0; z < dataList.getLength(); z++)
                     {
                        Node streamData = dataList.item(z);

                        if(streamData.getNodeName().equals("id"))
                        {
                           streamID = Integer.parseInt(streamData.getTextContent());
                        }
                        else if(streamData.getNodeName().equals("type"))
                        {
                           streamType = Integer.parseInt(streamData.getTextContent());
                        }
                     }

                     if(streamID > -1 && streamType > -1)
                     {
                        int[] streamData = new int[2];
                        streamData[0] = streamID;
                        streamData[1] = streamType;
                        chan.addStream(streamData);
                     }
                  }
               }

               scanResult.add(chan);
            }
         }
      }
      catch(Exception e)
      {
         scanResult = new Vector<>();
         System.out.println("ERROR parsing channel scan XML.");
         System.out.println(xmlData);
         e.printStackTrace();
         return 0;
      }

      return scanResult.size();
   }

   public void addStream(int progID, int streamID, int type)
   {
      int[] streamData = new int[2];
      streamData[0] = streamID;
      streamData[1] = type;

      for (Channel element : scanResult) {
         Channel chan = element;
         if(chan.getProgramID() == progID)
         {
            chan.addStream(streamData);
         }
      }
   }

   public Vector<Channel> getResult()
   {
      return scanResult;
   }

}
