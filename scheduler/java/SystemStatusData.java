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
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class SystemStatusData
{

   public SystemStatusData()
   {
   }

   public byte[] getStatusXML(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "sys-info", null);

      Element root = doc.getDocumentElement();
      Element item = null;

      DataStore store = DataStore.getInstance();

      // time
      Calendar start = Calendar.getInstance();
      start.setTime(new Date());
      item = doc.createElement("time");
      int hour = start.get(Calendar.HOUR_OF_DAY);
      if(hour > 12) {
		hour = hour - 12;
	  } else if(hour == 0) {
		hour = 12;
	  }
      item.setAttribute("hour_12" , intToXchar(hour, 2));
      item.setAttribute("hour_24" , intToXchar(start.get(Calendar.HOUR_OF_DAY), 2));
      item.setAttribute("minute" , intToXchar(start.get(Calendar.MINUTE), 2));
      if(start.get(Calendar.AM_PM) == Calendar.AM) {
		item.setAttribute("am_pm" , "am");
	  } else {
		item.setAttribute("am_pm" , "pm");
	  }
      root.appendChild(item);

      // add now running/and next
      ScheduleItem[] itemsArray = store.getScheduleArray();
      Arrays.sort(itemsArray);

      Vector<ScheduleItem> nextItems = new Vector<>();
      ScheduleItem firstNext = null;

      // add now running data
      item = doc.createElement("now_running");
      for (ScheduleItem element : itemsArray) {
         if(nextItems.size() == 0 &&
            element.getState() == ScheduleItem.WAITING &&
            element.getStop().getTime() > new Date().getTime())
         {
            firstNext = element;
            nextItems.add(element);
         }
         else if(firstNext != null &&
                 element.getState() == ScheduleItem.WAITING &&
                 element.getStart().getTime() < firstNext.getStop().getTime())
         {
            nextItems.add(element);
         }

         if(element.getState() == ScheduleItem.RUNNING)
         {
            addNowRunningItem(element, item, doc);
         }
      }
      root.appendChild(item);

      // add next item
      item = doc.createElement("next");
      for (ScheduleItem next : nextItems) {
         addNextItem(next, item, doc);
      }

      root.appendChild(item);

      // add now and next epg items
      item = doc.createElement("now_and_next");
      addNOWandNEXT(store, item, doc);
      root.appendChild(item);

      // add free space info
      NumberFormat nf = NumberFormat.getInstance();
      item = doc.createElement("freeSpace");

      String[] paths = store.getCapturePaths();

      for (String path : paths) {
         File capPath = new File(path);

         Element drive = doc.createElement("drive");
         drive.setAttribute("path", path);

         if(!capPath.exists())
         {
            drive.setAttribute("free", "Path not found");
         }
         else
         {
            DllWrapper wrapper = new DllWrapper();
            long freeSpace = wrapper.getFreeSpace(capPath.getCanonicalPath());
            freeSpace /= (1024 * 1024);
            drive.setAttribute("free", nf.format(freeSpace) + " MB");
         }

         item.appendChild(drive);
      }
      root.appendChild(item);

      // Do transform and return data
      XSL transformer = new XSL(doc, "status.xsl", urlData, headers);
      transformer.addCookie("backURL", "/");
      return transformer.doTransform(false);
   }

   private void addNOWandNEXT(DataStore store, Element item, Document doc)
   {
      GuideStore guide = GuideStore.getInstance();

      Vector<String[]> chanMap = guide.getChannelMap();
      Set<String> wsChannels = store.getChannels().keySet();
      Date now = new Date();
      Calendar startTime = Calendar.getInstance();

      for (String[] element : chanMap) {
         String[] map = element;

         if(wsChannels.contains(map[0]))
         {
            Element channel = doc.createElement("channel");
            channel.setAttribute("epg_channel", map[1]);
            channel.setAttribute("ws_channel", map[0]);

            GuideItem[] items = guide.getProgramsForChannel(map[1]);

            for (int x = 0; x < items.length; x++)
            {
               GuideItem gitem = items[x];

               startTime.setTime(gitem.getStart());

               if(gitem.getStart().before(now) && gitem.getStop().after(now))
               {
                  Element elmNow = doc.createElement("now");
                  addGuideItem(items[x], elmNow, doc);
                  channel.appendChild(elmNow);

                  if(x+1 < items.length)
                  {
                     Element elmNext = doc.createElement("next");
                     addGuideItem(items[x+1], elmNext, doc);
                     channel.appendChild(elmNext);
                  }

                  break;
               }

               if(gitem.getStart().after(now))
               {
                  Element elmNext = doc.createElement("next");
                  addGuideItem(gitem, elmNext, doc);
                  channel.appendChild(elmNext);
                  break;
               }
            }

            item.appendChild(channel);
         }
      }
   }

   private void addGuideItem(GuideItem gItem, Element parent, Document doc)
   {
      Element name = null;
      Text text = null;

      name = doc.createElement("name");
      name.setAttribute("sub_name", gItem.getSubName());
      text = doc.createTextNode(gItem.getName());
      name.appendChild(text);
      parent.appendChild(name);

      Calendar start = Calendar.getInstance();
      start.setTime(gItem.getStart());
      name = doc.createElement("start");

      int hour = start.get(Calendar.HOUR_OF_DAY);
      if(hour > 12) {
		hour = hour - 12;
	  } else if(hour == 0) {
		hour = 12;
	  }

      name.setAttribute("hour_12" , intToXchar(hour, 2));
      name.setAttribute("hour_24" , intToXchar(start.get(Calendar.HOUR_OF_DAY), 2));
      name.setAttribute("minute" , intToXchar(start.get(Calendar.MINUTE), 2));
      if(start.get(Calendar.AM_PM) == Calendar.AM) {
		name.setAttribute("am_pm" , "am");
	  } else {
		name.setAttribute("am_pm" , "pm");
	  }
      parent.appendChild(name);

      // add duration
      name = doc.createElement("duration");
      text = doc.createTextNode(Integer.valueOf(gItem.getDuration()).toString());
      name.appendChild(text);
      parent.appendChild(name);

      // add item id
      name = doc.createElement("id");
      text = doc.createTextNode(gItem.toString());
      name.appendChild(text);
      parent.appendChild(name);
   }

   private void addNextItem(ScheduleItem schItem, Element item, Document doc)
   {
      Element nextItem = doc.createElement("item");

      Element name = null;
      Text text = null;

      // add name
      name = doc.createElement("name");
      text = doc.createTextNode(schItem.getName());
      name.appendChild(text);
      nextItem.appendChild(name);

      // add start time
      Calendar start = Calendar.getInstance();
      start.setTime(schItem.getStart());
      name = doc.createElement("start");

      int hour = start.get(Calendar.HOUR_OF_DAY);
      if(hour > 12) {
		hour = hour - 12;
	  } else if(hour == 0) {
		hour = 12;
	  }

      name.setAttribute("hour_12" , intToXchar(hour, 2));
      name.setAttribute("hour_24" , intToXchar(start.get(Calendar.HOUR_OF_DAY), 2));
      name.setAttribute("minute" , intToXchar(start.get(Calendar.MINUTE), 2));
      if(start.get(Calendar.AM_PM) == Calendar.AM) {
		name.setAttribute("am_pm" , "am");
	  } else {
		name.setAttribute("am_pm" , "pm");
	  }
      nextItem.appendChild(name);


      // add duration
      name = doc.createElement("id");
      text = doc.createTextNode(schItem.toString());
      name.appendChild(text);
      nextItem.appendChild(name);

      // add duration
      name = doc.createElement("duration");
      text = doc.createTextNode(Integer.valueOf(schItem.getDuration()).toString());
      name.appendChild(text);
      nextItem.appendChild(name);

      // add channel
      name = doc.createElement("channel");
      text = doc.createTextNode(schItem.getChannel());
      name.appendChild(text);
      nextItem.appendChild(name);

      // status
      name = doc.createElement("status");
      text = doc.createTextNode(schItem.getStatus());
      name.appendChild(text);
      nextItem.appendChild(name);

      // time till run
      Date now = new Date();
      long timeLeft = schItem.getStart().getTime() - now.getTime();

      long days = timeLeft / (1000 * 60 * 60 *24);
      long hours = (timeLeft - (days * 1000 * 60 * 60 *24)) / (1000 * 60 * 60);
      long min = (timeLeft - (days * 1000 * 60 * 60 *24) - (hours * 1000 * 60 * 60)) / (1000 * 60);
      long seconds = (timeLeft - (days * 1000 * 60 * 60 *24) - (hours * 1000 * 60 * 60) - (min * 1000 * 60)) / 1000;

      name = doc.createElement("time_to_action");
      name.setAttribute("days" , Long.valueOf(days).toString());
      name.setAttribute("hours" , Long.valueOf(hours).toString());
      name.setAttribute("minutes" , Long.valueOf(min).toString());
      name.setAttribute("seconds" , Long.valueOf(seconds).toString());
      nextItem.appendChild(name);

      item.appendChild(nextItem);
   }

   private void addNowRunningItem(ScheduleItem schItem, Element item, Document doc)
   {
      Element runnintItem = null;

      Element name = null;
      Text text = null;

      runnintItem = doc.createElement("item");

      // add name
      name = doc.createElement("name");
      text = doc.createTextNode(schItem.getName());
      name.appendChild(text);
      runnintItem.appendChild(name);

      // add start time
      Calendar start = Calendar.getInstance();
      start.setTime(schItem.getStart());
      name = doc.createElement("start");
      int hour = start.get(Calendar.HOUR_OF_DAY);
      if(hour > 12) {
		hour = hour - 12;
	  } else if(hour == 0) {
		hour = 12;
	  }

      name.setAttribute("hour_12" , intToXchar(hour, 2));
      name.setAttribute("hour_24" , intToXchar(start.get(Calendar.HOUR_OF_DAY), 2));
      name.setAttribute("minute" , intToXchar(start.get(Calendar.MINUTE), 2));
      if(start.get(Calendar.AM_PM) == Calendar.AM) {
		name.setAttribute("am_pm" , "am");
	  } else {
		name.setAttribute("am_pm" , "pm");
	  }
      runnintItem.appendChild(name);

      // id
      name = doc.createElement("id");
      text = doc.createTextNode(schItem.toString());
      name.appendChild(text);
      runnintItem.appendChild(name);

      // add channel
      name = doc.createElement("channel");
      text = doc.createTextNode(schItem.getChannel());
      name.appendChild(text);
      runnintItem.appendChild(name);

      // add duration
      name = doc.createElement("duration");
      text = doc.createTextNode(Integer.valueOf(schItem.getDuration()).toString());
      name.appendChild(text);
      runnintItem.appendChild(name);

      // status
      name = doc.createElement("status");
      text = doc.createTextNode(schItem.getStatus());
      name.appendChild(text);
      runnintItem.appendChild(name);

      item.appendChild(runnintItem);
   }

   private String intToXchar(int val, int len)
   {
      String finalString = "";
      String rawInt = Integer.valueOf(val).toString();

      int toAdd = len - rawInt.length();

      for(int x = 0; x < toAdd; x ++)
      {
         finalString += "0";
      }

      finalString += rawInt;
      return finalString;
   }

}