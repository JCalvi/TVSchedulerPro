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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class TaskCommand
{
   private String name = "";
   private String command = "";
   private boolean enabled = true;
   private boolean autoRemove = false;
   private int delay = 0;
   private int concurrent = 20;
   private int timeToNextSchedule = 0;
   private boolean whenNotCapturing = true;

   public TaskCommand(String nameID)
   {
      name = nameID;
   }

   public TaskCommand(Node item)
   {
      NamedNodeMap itemAttribs = item.getAttributes();
      name = itemAttribs.getNamedItem("name").getTextContent();

      NodeList itemNodes = item.getChildNodes();
      Node itemNode = null;
      for(int y = 0; y < itemNodes.getLength(); y++)
      {
         itemNode = itemNodes.item(y);

         if(itemNode.getNodeName().equals("enabled"))
         {
            if("true".equals(itemNode.getTextContent())) {
				enabled = true;
			} else {
				enabled = false;
			}
         }
         else if(itemNode.getNodeName().equals("command"))
         {
            command = itemNode.getTextContent();
         }
         else if(itemNode.getNodeName().equals("autoRemove"))
         {
            if("true".equals(itemNode.getTextContent())) {
				autoRemove = true;
			} else {
				autoRemove = false;
			}
         }
         else if(itemNode.getNodeName().equals("delay"))
         {
            delay = Integer.valueOf(itemNode.getTextContent()).intValue();
         }
         else if(itemNode.getNodeName().equals("concurrent"))
         {
            concurrent = Integer.valueOf(itemNode.getTextContent()).intValue();
         }
         else if(itemNode.getNodeName().equals("timeToNextSchedule"))
         {
            timeToNextSchedule = Integer.valueOf(itemNode.getTextContent()).intValue();
         }
         else if(itemNode.getNodeName().equals("whenNotCapturing"))
         {
            if("true".equals(itemNode.getTextContent())) {
				whenNotCapturing = true;
			} else {
				whenNotCapturing = false;
			}
         }
      }
   }

   public void setWhenNotCapturing(boolean val)
   {
      whenNotCapturing = val;
   }

   public boolean getWhenNotCapturing()
   {
      return whenNotCapturing;
   }

   public void setTimeToNextSchedule(int val)
   {
      timeToNextSchedule = val;
   }

   public int getTimeToNextSchedule()
   {
      return timeToNextSchedule;
   }

   public void setConcurrent(int val)
   {
      concurrent = val;
   }

   public int getConcurrent()
   {
      return concurrent;
   }

   public void setCommand(String com)
   {
      command = com;
   }

   public void setEnabled(boolean en)
   {
      enabled = en;
   }

   public void setAutoRemove(boolean autoRem)
   {
      autoRemove = autoRem;
   }

   public boolean getAutoRemove()
   {
      return autoRemove;
   }

   public boolean getEnabled()
   {
      return enabled;
   }

   public String getCommand()
   {
      return command;
   }

   public String getName()
   {
      return name;
   }

   public int getDelay()
   {
      return delay;
   }

   public void setDelay(int pause)
   {
      delay = pause;
   }

   public void addXML(Document doc, Element root)
   {
      Element elm = null;
      Text text = null;

      Element item = doc.createElement("task");
      item.setAttribute("name", name);

      elm = doc.createElement("enabled");
      text = doc.createTextNode(Boolean.valueOf(enabled).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("command");
      text = doc.createTextNode(command);
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("autoRemove");
      text = doc.createTextNode(Boolean.valueOf(autoRemove).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("delay");
      text = doc.createTextNode(Integer.valueOf(delay).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("concurrent");
      text = doc.createTextNode(Integer.valueOf(concurrent).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("timeToNextSchedule");
      text = doc.createTextNode(Integer.valueOf(timeToNextSchedule).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      elm = doc.createElement("whenNotCapturing");
      text = doc.createTextNode(Boolean.valueOf(whenNotCapturing).toString());
      elm.appendChild(text);
      item.appendChild(elm);

      root.appendChild(item);
   }

}