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

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Channel
{
   public static final int TYPE_VIDEO = 0;
   public static final int TYPE_AUDIO_MPG = 1;
   public static final int TYPE_AUDIO_AC3 = 2;
   public static final int TYPE_TELETEXT = 3;
   public static final int TYPE_PRC = 4;
   public static final int TYPE_PGM = 5;
   public static final int TYPE_PRIVATE = 6;

   private String name = "";
   private int frequency = 0;
   private int bandWidth = 0;
   private int programID = -1;
   private int vPID = -1;
   private int aPID = -1;
   private int aType = -1;
   private int captureType = -1;

   private Vector<int[]> streams = new Vector<>();

   public Channel(String id, int freq, int band, int progID, int vpid, int apid)
   {
      name = id;
      frequency = freq;
      bandWidth = band;
      programID = progID;
      vPID = vpid;
      aPID = apid;
   }

   public void addStream(int[] streamData)
   {
      streams.add(streamData);
   }

   public Vector<int[]> getStreams()
   {
      return streams;
   }

   public Channel(Node item)
   {
      NamedNodeMap itemAttribs = item.getAttributes();
      name = itemAttribs.getNamedItem("name").getTextContent();

      NodeList itemNodes = item.getChildNodes();
      Node itemNode = null;
      for(int y = 0; y < itemNodes.getLength(); y++)
      {
         itemNode = itemNodes.item(y);

         if(itemNode.getNodeName().equals("frequency"))
         {
            frequency = Integer.parseInt(itemNode.getTextContent());
         }
         else if(itemNode.getNodeName().equals("bandwidth"))
         {
            bandWidth = Integer.parseInt(itemNode.getTextContent());
         }
         else if(itemNode.getNodeName().equals("program_id"))
         {
            programID = Integer.parseInt(itemNode.getTextContent());
         }
         else if(itemNode.getNodeName().equals("video_id"))
         {
            vPID = Integer.parseInt(itemNode.getTextContent());
         }
         else if(itemNode.getNodeName().equals("audio_id"))
         {
            aPID = Integer.parseInt(itemNode.getTextContent());
            NamedNodeMap audioAttribs = itemNode.getAttributes();
            aType = Integer.parseInt(audioAttribs.getNamedItem("type").getTextContent());
         }
         else if(itemNode.getNodeName().equals("capture_type"))
         {
            captureType = Integer.parseInt(itemNode.getTextContent());
         }
      }
   }

   public int getCaptureType()
   {
      return captureType;
   }

   public void setCaptureType(int type)
   {
      captureType = type;
   }

   public int getVideoPid()
   {
      return vPID;
   }

   public int getAudioPid()
   {
      return aPID;
   }

   public void setVideoPid(int id)
   {
      vPID = id;
   }

   public void setAudioPid(int id)
   {
      aPID = id;
   }

   public void setAudioType(int type)
   {
      aType = type;
   }

   public int getAudioType()
   {
      return aType;
   }

   public int getBandWidth()
   {
      return bandWidth;
   }

   public void setBandWidth(int band)
   {
      bandWidth = band;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String id)
   {
      name = id;
   }

   public int getFrequency()
   {
      return frequency;
   }

   public void setFrequency(int freq)
   {
      frequency = freq;
   }

   public void setProgramID(int id)
   {
      programID = id;
   }

   public int getProgramID()
   {
      return programID;
   }

   public Element getXML(Document doc)
   {
      Element elm = null;
      Text text = null;

      Element item = doc.createElement("channel");
      item.setAttribute("name", name);

      elm = doc.createElement("frequency");
      text = doc.createTextNode(Integer.valueOf(frequency).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("bandwidth");
      text = doc.createTextNode(Integer.valueOf(bandWidth).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("program_id");
      text = doc.createTextNode(Integer.valueOf(programID).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("video_id");
      text = doc.createTextNode(Integer.valueOf(vPID).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("audio_id");
      elm.setAttribute("type", Integer.valueOf(aType).toString());
      text = doc.createTextNode(Integer.valueOf(aPID).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("capture_type");
      text = doc.createTextNode(Integer.valueOf(captureType).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      return item;
   }
}
