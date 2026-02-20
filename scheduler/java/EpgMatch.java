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

public class EpgMatch
{
   private boolean enabled = true;
   private long id = 0;

   private Vector<String> matchList = new Vector<>();

   private boolean autoDel = false;
   private int keepFor = 30;
   private int startBuffer = 0;
   private int endBuffer = 0;
   private String postTask = "";
   private int captureType = 0;
   private int capturePathIndex = -1;
   private String fileNamePattern = "(%y-%m-%d %h-%M) %n %c";
   private int existingCheckType = 1;

   public EpgMatch()
   {
      java.util.Random rand = new java.util.Random(new java.util.Date().getTime());
      id = rand.nextLong();
   }

   public int getExistingCheckType()
   {
      return existingCheckType;
   }

   public void setExistingCheckType(int check)
   {
      existingCheckType = check;
   }

   public int getCapturePathIndex()
   {
      return capturePathIndex;
   }

   public void setCapturePathIndex(int dir)
   {
      capturePathIndex = dir;
   }

   public Vector<String> getMatchListNames()
   {
      return matchList;
   }

   public void addMatchListName(String name)
   {
      if(!matchList.contains(name)) {
		matchList.add(name);
	  }
   }

   public void remMatchListName(int index)
   {
      matchList.remove(index);
   }

   public void renameMatchListName(String oldName, String newName)
   {
      if(matchList.contains(oldName))
      {
         while(matchList.remove(oldName)) {

		 }

         if(!matchList.contains(newName)) {
			matchList.add(newName);
		 }
      }
   }

   public void remMatchListName(String name)
   {
      while(matchList.remove(name)) {

	  }
   }

   public EpgMatch(Node node)
   {
      parseXML(node);
   }

   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean enable)
   {
      enabled = enable;
   }

   public long getID()
   {
      return id;
   }

   public void setID(long newID)
   {
      id = newID;
   }

   public void setCaptureType(int type)
   {
      captureType = type;
   }

   public int getCaptureType()
   {
      return captureType;
   }

   public void setPostTask(String task)
   {
      postTask = task;
   }

   public String getPostTask()
   {
      return postTask;
   }

   public void setStartBuffer(int buff)
   {
      startBuffer = buff;
   }

   public int getStartBuffer()
   {
      return startBuffer;
   }

   public void setEndBuffer(int buff)
   {
      endBuffer = buff;
   }

   public int getEndBuffer()
   {
      return endBuffer;
   }

   public int getKeepFor()
   {
      return keepFor;
   }

   public void setKeepFor(int keep)
   {
      keepFor = keep;
   }

   public boolean getAutoDel()
   {
      return autoDel;
   }

   public void setAutoDel(boolean del)
   {
      autoDel = del;
   }

   public void setFileNamePattern(String pattern)
   {
      fileNamePattern = pattern;
   }

   public String GetFileNamePattern()
   {
      return fileNamePattern;
   }

   public boolean getXML(Document doc, Element root)
   {
      Element item = doc.createElement("item");
      item.setAttribute("id", Long.valueOf(id).toString());
      item.setAttribute("enabled", Boolean.valueOf(enabled).toString());
      item.setAttribute("captureType", Integer.valueOf(captureType).toString());

      Element elm = null;
      Element elm2 = null;
      Text text = null;

      elm = doc.createElement("matchList");

      for (String name : matchList) {
         elm2 = doc.createElement("matchListName");
         text = doc.createTextNode(name);
         elm2.appendChild(text);
         elm.appendChild(elm2);
      }

      item.appendChild(elm);

      elm = doc.createElement("postTask");
      text = doc.createTextNode(postTask);
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("fileNamePattern");
      text = doc.createTextNode(fileNamePattern);
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("autoDel");
      elm.setAttribute("enabled", Boolean.valueOf(autoDel).toString());
      elm.setAttribute("keepFor", Integer.valueOf(keepFor).toString());
      item.appendChild(elm);

      elm = doc.createElement("buffer");
      elm.setAttribute("start", Integer.valueOf(startBuffer).toString());
      elm.setAttribute("end", Integer.valueOf(endBuffer).toString());
      item.appendChild(elm);

      elm = doc.createElement("capturePathIndex");
      text = doc.createTextNode(Integer.valueOf(capturePathIndex).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("existingCheckType");
      text = doc.createTextNode(Integer.valueOf(existingCheckType).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      root.appendChild(item);

      return true;
   }

   private void parseXML(Node node)
   {
      NamedNodeMap itemAttribs = node.getAttributes();
      id = Long.parseLong(itemAttribs.getNamedItem("id").getTextContent());

      if("true".equals(itemAttribs.getNamedItem("enabled").getTextContent())) {
		enabled = true;
	  } else {
		enabled = false;
	  }

      captureType = Integer.parseInt(itemAttribs.getNamedItem("captureType").getTextContent());


      NodeList nl = node.getChildNodes();
      Node found = null;

      for(int x = 0; x < nl.getLength(); x++)
      {
         found = nl.item(x);
         if(found.getNodeName().equals("matchList"))
         {
            NodeList nl2 = found.getChildNodes();
            for(int y = 0; y < nl2.getLength(); y++)
            {
               Node matches = nl2.item(y);
               if(matches.getNodeName().equals("matchListName"))
               {
                  matchList.add(matches.getTextContent());
               }
            }
         }
         else if(found.getNodeName().equals("postTask"))
         {
            postTask = found.getTextContent();
         }
         else if(found.getNodeName().equals("fileNamePattern"))
         {
            fileNamePattern = found.getTextContent();
         }
         else if(found.getNodeName().equals("capturePathIndex"))
         {
            capturePathIndex = Integer.parseInt(found.getTextContent());
         }
         else if(found.getNodeName().equals("existingCheckType"))
         {
            existingCheckType = Integer.parseInt(found.getTextContent());
         }
         else if(found.getNodeName().equals("autoDel"))
         {
            NamedNodeMap attribs = found.getAttributes();
            Node keep = attribs.getNamedItem("keepFor");
            keepFor = Integer.parseInt(keep.getTextContent());

            keep = attribs.getNamedItem("enabled");
            if("true".equals(keep.getTextContent())) {
				autoDel = true;
			} else {
				autoDel = false;
			}
         }
         else if(found.getNodeName().equals("buffer"))
         {
            NamedNodeMap attribs = found.getAttributes();
            Node buff = attribs.getNamedItem("start");
            startBuffer = Integer.parseInt(buff.getTextContent());

            buff = attribs.getNamedItem("end");
            endBuffer = Integer.parseInt(buff.getTextContent());
         }
      }
   }

}