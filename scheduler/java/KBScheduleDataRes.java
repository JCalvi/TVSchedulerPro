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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class KBScheduleDataRes extends HTTPResponse
{
   public KBScheduleDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if ("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showCalendar(urlData, headers));
         return;
      }
      else if ("02".equals(urlData.getParameter("action")))
      {
         outStream.write(showAddForm(urlData, headers));
         return;
      }
      else if ("03".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(addUpdateItem(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("04".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(showItemOptions(urlData, headers));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("05".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(deleteItem(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("06".equals(urlData.getParameter("action")))
      {
         outStream.write(stopRunningTask(urlData));
         return;
      }
      else if ("07".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(addTime(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("08".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(skipToNext(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("09".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(showLog(urlData, headers));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("11".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(addScheduleFromGuideItem(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(getScheduleTable(urlData, headers));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }

   }

   private byte[] addScheduleFromGuideItem(HTTPurl urlData) throws Exception
   {
      String channel = urlData.getParameter("channel");
      String id = urlData.getParameter("id");

      GuideStore guide = GuideStore.getInstance();
      String epgChan = guide.getEpgChannelFromMap(channel);
      GuideItem guideItem = guide.getProgram(epgChan, id);

      if(epgChan == null || epgChan.length() == 0) {
		throw new Exception("WS Channel Not Found!");
	  }

      int captype = -1;

      ScheduleItem schItem = new ScheduleItem(guideItem, channel,
            captype, store.rand.nextLong(), false);

      String task = store.getProperty("tasks.deftask");
      schItem.setPostTask(task);

      // Add time buffer
      int startBuff = 0;
      int endBuff = 0;
      int endBuffEpg = 0;
      try
      {
         startBuff = Integer.parseInt(store.getProperty("schedule.buffer.start"));
         endBuff = Integer.parseInt(store.getProperty("schedule.buffer.end"));
         endBuffEpg = Integer.parseInt(store.getProperty("schedule.buffer.end.epg"));
      }
      catch (Exception e)
      {
      }

      String[] patterns = store.getNamePatterns();
      schItem.setFilePattern(patterns[0]);

      String keepFor = store.getProperty("autodel.keepfor");
      int keepInt = 30;
      try
      {
         keepInt = Integer.parseInt(keepFor);
      }
      catch (Exception e)
      {
      }
      schItem.setKeepFor(keepInt);

      schItem.setCapType(captype);

      // build the Calendar object and set the start time
      Calendar cal = Calendar.getInstance();
      cal.setTime(schItem.getStart());

      cal.add(Calendar.MINUTE, (startBuff * -1));

      if(endBuffEpg > 0)
      {
         endBuffEpg = endBuffEpg * (guideItem.getDuration() / 60);
      }

      schItem.setDuration(guideItem.getDuration() + startBuff + endBuff + endBuffEpg);
      schItem.setStart(cal);

      schItem.setType(ScheduleItem.ONCE);

      // Log the add action
      schItem.log("New Schedule added/edited");

      boolean isAlreadyInList = guide.isAlreadyInList(schItem, 1);

      if(!isAlreadyInList)
      {
         store.addScheduleItem(schItem);
      }

      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/" + urlData.getServletClass();
	  }

      StringBuffer out = new StringBuffer(4096);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] showLog(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String id = urlData.getParameter("id");
      if (id == null || id.length() == 0) {
		id = "";
	  }

      ScheduleItem item = store.getScheduleItem(urlData.getParameter("id"));

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "log", null);
      Element root = doc.getDocumentElement();
      root.setAttribute("id", item.toString());

      Element logitem = null;
      Element elm = null;
      Text text = null;

      if (item != null)
      {
         String[] lines = item.getLog().split("\n");
         for (String line : lines) {
            logitem = doc.createElement("logitem");
            elm = doc.createElement("line");
            text = doc.createTextNode(line);
            elm.appendChild(text);
            logitem.appendChild(elm);
            root.appendChild(logitem);
         }
      }

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-showlog.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] skipToNext(HTTPurl urlData) throws Exception
   {
      String id = urlData.getParameter("id");
      ScheduleItem item = store.getScheduleItem(urlData.getParameter("id"));

      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
	  }

      // Only allow update to waiting schedules
      if (id != null && id.length() > 0 && item != null && item.getState() == ScheduleItem.WAITING)
      {
         item.skipToNext();
         store.saveSchedule(null);
      }

      // Redirect back to the main page
      StringBuffer out = new StringBuffer();
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] addTime(HTTPurl urlData) throws Exception
   {
      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
	  }

      ScheduleItem item = store.getScheduleItem(urlData.getParameter("id"));

      if(item != null)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(item.getStart());

         // Add time buffer
         int startBuff = 0;
         int endBuff = 0;
         int endBuffEpg = 0;

         try
         {
            startBuff = Integer.parseInt(store.getProperty("schedule.buffer.start"));
            endBuff = Integer.parseInt(store.getProperty("schedule.buffer.end"));
            endBuffEpg = Integer.parseInt(store.getProperty("schedule.buffer.end.epg"));
         }
         catch(Exception e){}

         if(endBuffEpg > 0)
         {
           endBuffEpg = endBuffEpg * (item.getDuration() / 60);
         }

         // if the item is running then only add on to the duration
         if(item.getState() != ScheduleItem.RUNNING)
         {
            cal.add(Calendar.MINUTE, (startBuff * -1));
            item.setDuration(item.getDuration() + startBuff + endBuff + endBuffEpg);
            item.setStart(cal);
         }
         else
         {
            item.setDuration(item.getDuration() + endBuff + endBuffEpg);
         }

         store.saveSchedule(null);
      }

      // Redirect back to the main page
      StringBuffer out = new StringBuffer();
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] addUpdateItem(HTTPurl urlData) throws Exception
   {
      String id = urlData.getParameter("id");

      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
	  }

      ScheduleItem item = null;
      if (id != null && id.length() > 0)
      {
         item = store.getScheduleItem(id);

         // if we are trying to edit a running schedule just redirect back
         // to the main page
         if (item != null && (item.getState() != ScheduleItem.FINISHED && item.getState() != ScheduleItem.WAITING && item.getState() != ScheduleItem.SKIPPED && item.getState() != ScheduleItem.ERROR))
         {
            StringBuffer out = new StringBuffer();
            out.append("HTTP/1.0 302 Moved Temporarily\n");
            out.append("Location: " + backURL + "\n\n");
            return out.toString().getBytes();
         }
      }

      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      String mes = addSchedule(urlData, item, index);

      if (mes != null) {
		throw new Exception(mes);
	  }

      StringBuffer out = new StringBuffer(4096);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private String addSchedule(HTTPurl urlData, ScheduleItem item, int index) throws Exception
   {
      // Add time buffer
      int startBuff = 0;
      int endBuff = 0;
      try
      {
         startBuff = Integer.parseInt(store.getProperty("schedule.buffer.start"));
         endBuff = Integer.parseInt(store.getProperty("schedule.buffer.end"));
      }
      catch (Exception e)
      {
      }
      String addBuff = urlData.getParameter("buffer");

      // Get all the input data
      String y = urlData.getParameter("year");
      String m = urlData.getParameter("month");
      String d = urlData.getParameter("day");
      String h = urlData.getParameter("hour");
      String mi = urlData.getParameter("min");
      String dur = urlData.getParameter("duration");
      String channel = urlData.getParameter("channel");
      String name = urlData.getParameter("name");
      String autoDel = urlData.getParameter("autoDel");
      String namePattern = urlData.getParameter("namePattern");
      String keepFor = urlData.getParameter("keepfor");
      String task = urlData.getParameter("task");
      String capturePath = urlData.getParameter("capPath");

      // check channel name exists
      HashMap<String, Channel> channels = store.getChannels();
      if(!channels.containsKey(channel))
      {
         throw new Exception("Channel Not Found!");
      }

      // check name pattern exists
      String[] namePatterns = store.getNamePatterns();
      if(namePattern == null || namePattern.length() == 0) {
		namePattern = namePatterns[0];
	  }

      GuideStore guide = GuideStore.getInstance();

      boolean found = false;
      for (String namePattern2 : namePatterns) {
         if(namePattern2.equals(namePattern))
         {
            found = true;
            break;
         }
      }
      if(!found)
      {
         throw new Exception("Name Pattern Not Found!");
      }

      if(item != null)
      {
         store.removeScheduleItem(item.toString());
      }

      // Calc the duration in min
      int duration = Integer.parseInt(dur);

      int type = 0;
      try
      {
         type = Integer.parseInt(urlData.getParameter("type"));
      }
      catch (Exception e01)
      {
      }

      int captype = 2;
      try
      {
         captype = Integer.parseInt(store.getProperty("capture.deftype"));
      }
      catch (Exception e01)
      {
      }
      try
      {
         captype = Integer.parseInt(urlData.getParameter("captype"));
      }
      catch (Exception e01)
      {
      }

      int capPathIndex = -1;
      try
      {
         capPathIndex = Integer.parseInt(capturePath);
      }
      catch (Exception e01)
      {
      }

      // build the Calendar object and set the start time
      Calendar newDate = Calendar.getInstance();
      newDate.set(Calendar.MILLISECOND, 0);
      newDate.set(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d), Integer.parseInt(h), Integer.parseInt(mi), 0);

      // If needed add the Time buffer
      if("yes".equals(addBuff))
      {
         newDate.add(Calendar.MINUTE, (startBuff * -1));
         duration = duration + startBuff + endBuff;
      }

      // Create the new Schedule Object
      if(item == null) {
		item = new ScheduleItem(store.rand.nextLong());
	  }

      item.setCreatedFrom(null);

      item.setCapType(captype);

      item.setType(type);
      item.setName(name);

      // Set the initial status
      item.setState(ScheduleItem.WAITING);
      item.setStatus("Waiting");

      // Reset the abort if are editing
      item.resetAbort();

      // Set the start/stop and duration
      item.setStart(newDate);
      item.setDuration(duration);

      // Set the channel to capture
      item.setChannel(channel);

      // Set the Auto Del option
      if("1".equalsIgnoreCase(autoDel)) {
		item.setAutoDeletable(true);
	  } else {
		item.setAutoDeletable(false);
	  }

      item.setFilePattern(namePattern);

      item.setCapturePathIndex(capPathIndex);

      if(keepFor != null)
      {
         int keepInt = 30;
         try
         {
            keepInt = Integer.parseInt(keepFor);
         }
         catch (Exception e)
         {
         }
         item.setKeepFor(keepInt);
      }
      else
      {
         keepFor = store.getProperty("autodel.keepfor");
         int keepInt = 30;
         try
         {
            keepInt = Integer.parseInt(keepFor);
         }
         catch (Exception e)
         {
         }
         item.setKeepFor(keepInt);
      }

      if(task != null && !task.equalsIgnoreCase("none") && task.length() > 0)
      {
         HashMap<String, TaskCommand> tasks = store.getTaskList();
         if(tasks.containsKey(task)) {
			item.setPostTask(task);
		 }
      }
      else if(task != null && task.equalsIgnoreCase("none"))
      {
         item.setPostTask("");
      }

      // Log the add action
      item.log("New Schedule added/edited");

      boolean isAlreadyInLIst = guide.isAlreadyInList(item, 1);

      if(isAlreadyInLIst)
      {
         return null;
      }

      store.addScheduleItem(item);

      return null;
   }

   private byte[] showAddForm(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int day = -1;
      int month = -1;
      int year = -1;

      try
      {
         day = Integer.parseInt(urlData.getParameter("day"));
         month = Integer.parseInt(urlData.getParameter("month"));
         year = Integer.parseInt(urlData.getParameter("year"));
      }
      catch (Exception e)
      {
      }

      ScheduleItem item = null;
      String id = urlData.getParameter("id");

      if (id != null && id.length() > 0)
      {
         item = store.getScheduleItem(id);
      }

      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      // if we are trying to edit a running schedule just redirect back
      // to the main page
      if (item != null && (item.getState() != ScheduleItem.FINISHED && item.getState() != ScheduleItem.WAITING && item.getState() != ScheduleItem.SKIPPED && item.getState() != ScheduleItem.ERROR))
      {
         StringBuffer out = new StringBuffer();
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/KBScheduleDataRes\n\n");
         return out.toString().getBytes();
      }

      if (day == -1 || month == -1 || year == -1)
      {
         Date start = item.getStart();
         Calendar cal = Calendar.getInstance();
         cal.setTime(start);

         day = cal.get(Calendar.DATE);
         month = cal.get(Calendar.MONTH);
         year = cal.get(Calendar.YEAR);
      }
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "item_form", null);
      Element root = doc.getDocumentElement();

      //
      // form elements
      //

      Calendar cal = Calendar.getInstance();
      if (item != null) {
		cal.setTime(item.getStart());
	  }

      root.setAttribute("month", Integer.valueOf(month).toString());
      root.setAttribute("year", Integer.valueOf(year).toString());
      root.setAttribute("date", Integer.valueOf(day).toString());
      if (item != null) {
		root.setAttribute("id", id);
	  }

      if(index > -1) {
		root.setAttribute("index", Integer.valueOf(index).toString());
	  } else {
		root.setAttribute("index", "");
	  }

      Element formEl = doc.createElement("startTimeMin");
      formEl.setAttribute("Name", "Minute");
      formEl.setAttribute("max", "59");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "5");
      formEl.setAttribute("value", Integer.valueOf(cal.get(Calendar.MINUTE)).toString());
      root.appendChild(formEl);

      formEl = doc.createElement("startTimeHour");
      formEl.setAttribute("Name", "Hour");
      formEl.setAttribute("max", "23");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "1");
      formEl.setAttribute("value", Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY)).toString());
      root.appendChild(formEl);

      formEl = doc.createElement("duration");
      formEl.setAttribute("Name", "Duration");
      formEl.setAttribute("max", "400");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "5");
      if (item != null) {
		formEl.setAttribute("value", Integer.valueOf(item.getDuration()).toString());
	  } else {
		formEl.setAttribute("value", "5");
	  }
      root.appendChild(formEl);

      Text text = null;

      formEl = doc.createElement("name");
      if (item != null) {
		text = doc.createTextNode(item.getName());
	  } else {
		text = doc.createTextNode("");
	  }
      formEl.appendChild(text);
      root.appendChild(formEl);

      formEl = doc.createElement("channel");
      getChannelList(doc, formEl, null);
      if (item != null) {
		formEl.setAttribute("value", item.getChannel());
	  } else {
		formEl.setAttribute("value", "");
	  }
      root.appendChild(formEl);

      formEl = doc.createElement("type");
      formEl.setAttribute("Name", "Schedule Type");
      formEl.setAttribute("max", "10");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "1");
      if (item != null) {
		formEl.setAttribute("value", Integer.valueOf(item.getType()).toString());
	  } else {
		formEl.setAttribute("value", "0");
	  }
      root.appendChild(formEl);


      formEl = doc.createElement("captureType");
      getCaptureTypes(doc, formEl);
      if (item != null)
      {
         formEl.setAttribute("value", Integer.valueOf(item.getCapType()).toString());
      }
      else
      {
         formEl.setAttribute("value", "-1");
      }
      root.appendChild(formEl);


      formEl = doc.createElement("pattern");
      getPatternList(doc, formEl);
      if (item != null) {
		formEl.setAttribute("value", item.getFilePattern());
	  } else {
		formEl.setAttribute("value", "");
	  }
      root.appendChild(formEl);


      formEl = doc.createElement("capturePath");
      getCapturePaths(doc, formEl);
      if (item != null)
      {
         formEl.setAttribute("value", Integer.valueOf(item.getCapturePathIndex()).toString());
      }
      else
      {
         formEl.setAttribute("value", "-1");
      }
      root.appendChild(formEl);


      formEl = doc.createElement("autoDel");
      formEl.setAttribute("Name", "Auto Delete");
      formEl.setAttribute("max", "1");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "1");
      if (item != null)
      {
         if (item.isAutoDeletable()) {
			formEl.setAttribute("value", "1");
		 } else {
			formEl.setAttribute("value", "0");
		 }
      } else {
		formEl.setAttribute("value", "0");
	  }
      root.appendChild(formEl);

      formEl = doc.createElement("keepfor");
      formEl.setAttribute("Name", "keep For");
      formEl.setAttribute("max", "120");
      formEl.setAttribute("min", "1");
      formEl.setAttribute("amount", "1");
      if (item != null) {
		formEl.setAttribute("value", Integer.valueOf(item.getKeepFor()).toString());
	  } else
      {
         String keep = store.getProperty("autodel.keepfor");
         formEl.setAttribute("value", keep);
      }
      root.appendChild(formEl);

      formEl = doc.createElement("posttask");
      getTaskList(doc, formEl);
      if (item != null)
      {
         formEl.setAttribute("value", item.getPostTask());
      }
      else
      {
         String defTask = store.getProperty("tasks.deftask");
         formEl.setAttribute("value", defTask);
      }
      root.appendChild(formEl);

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-details.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private String getCapturePaths(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);
      String[] capturePaths = store.getCapturePaths();

      option = doc.createElement("option");
      option.setAttribute("value", "-1");
      text = doc.createTextNode("AutoSelect");
      option.appendChild(text);
      formEl.appendChild(option);

      for (int x = 0; x < capturePaths.length; x++)
      {
         option = doc.createElement("option");
         option.setAttribute("value", Integer.valueOf(x).toString());
         text = doc.createTextNode(capturePaths[x]);
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }

   private String getCaptureTypes(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);

      option = doc.createElement("option");
      option.setAttribute("value", "-1");
      text = doc.createTextNode("AutoSelect");
      option.appendChild(text);
      formEl.appendChild(option);

      Vector<CaptureCapability> capabilities = CaptureCapabilities.getInstance().getCapabilities();

      for (CaptureCapability capability : capabilities) {
         option = doc.createElement("option");
         option.setAttribute("value", Integer.valueOf(capability.getTypeID()).toString());
         text = doc.createTextNode(capability.getName());
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }

   private String getTaskList(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);
      HashMap<String, TaskCommand> tasks = store.getTaskList();

      String[] keys = tasks.keySet().toArray(new String[0]);

      Arrays.sort(keys);

      option = doc.createElement("option");
      text = doc.createTextNode("none");
      option.appendChild(text);
      formEl.appendChild(option);

      for (String key : keys) {
         option = doc.createElement("option");
         text = doc.createTextNode(key);
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }

   private String getPatternList(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      String array = "";
      String[] patterns = store.getNamePatterns();

      for (String pattern : patterns) {
         option = doc.createElement("option");
         text = doc.createTextNode(pattern);
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return array;

   }

   private void getChannelList(Document doc, Element formEl, String chanMatch)
   {
      Element option = null;
      Text text = null;
      boolean chanExists = false;

      HashMap<String, Channel> channels = store.getChannels();
      String[] keys = channels.keySet().toArray(new String[0]);
      Arrays.sort(keys);

      for (String key : keys) {
         option = doc.createElement("option");
         text = doc.createTextNode(key);
         option.appendChild(text);
         formEl.appendChild(option);
         if (key.equals(chanMatch)) {
			chanExists = true;
		 }
      }
      if (chanMatch != null)
      {
         if (!chanExists)
         {
            option = doc.createElement("option");
            text = doc.createTextNode(chanMatch);
            option.appendChild(text);
            formEl.appendChild(option);
         }
         if (".*".compareTo(chanMatch) != 0)
         {
            option = doc.createElement("option");
            text = doc.createTextNode(".*");
            option.appendChild(text);
            formEl.appendChild(option);
         }
      }
   }

   private byte[] showCalendar(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String id = urlData.getParameter("id");
      ScheduleItem item = store.getScheduleItem(id);

      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e)
      {}

      // if we are trying to edit a running schedule just redirect back
      // to the main page
      if (item != null && (item.getState() != ScheduleItem.FINISHED && item.getState() != ScheduleItem.WAITING && item.getState() != ScheduleItem.SKIPPED && item.getState() != ScheduleItem.ERROR))
      {
         StringBuffer out = new StringBuffer();
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/KBScheduleDataRes\n\n");
         return out.toString().getBytes();
      }

      int month = -1;
      int year = -1;
      int day = -1;
      try
      {
         month = Integer.parseInt(urlData.getParameter("month"));
         year = Integer.parseInt(urlData.getParameter("year"));
      }
      catch (Exception e)
      {
      }

      Calendar cal = Calendar.getInstance();

      if (item != null) {
		cal.setTime(item.getStart());
	  }

      day = cal.get(Calendar.DATE);
      cal.set(Calendar.DATE, 1);

      if (month > -1 && year > -1)
      {
         cal.set(Calendar.MONTH, month);
         cal.set(Calendar.YEAR, year);
      }

      if (day > cal.getActualMaximum(Calendar.DATE)) {
		day = cal.getActualMaximum(Calendar.DATE);
	  }

      cal.set(Calendar.DATE, day);

      month = cal.get(Calendar.MONTH);
      year = cal.get(Calendar.YEAR);

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "cal", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("selectedDay", Integer.valueOf(day).toString());
      root.setAttribute("description", store.monthNameFull.get(Integer.valueOf(month)) + " " + year);

      cal.add(Calendar.MONTH, -1);
      root.setAttribute("prevMonth", Integer.valueOf(cal.get(Calendar.MONTH)).toString());
      root.setAttribute("prevYear", Integer.valueOf(cal.get(Calendar.YEAR)).toString());

      cal.add(Calendar.MONTH, 2);
      root.setAttribute("nextMonth", Integer.valueOf(cal.get(Calendar.MONTH)).toString());
      root.setAttribute("nextYear", Integer.valueOf(cal.get(Calendar.YEAR)).toString());

      cal.add(Calendar.MONTH, -1);

      if (item != null) {
		root.setAttribute("itemID", item.toString());
	  } else {
		root.setAttribute("itemID", "");
	  }

      if (index > -1) {
		root.setAttribute("index", Integer.valueOf(index).toString());
	  } else {
		root.setAttribute("index", "");
	  }

      int currentMonth = cal.get(Calendar.MONTH);
      cal.set(Calendar.DATE, 1);

      int dayNameStart = cal.getFirstDayOfWeek();

      for (int x = 0; x < 7; x++)
      {
         String dayName = store.dayName.get(Integer.valueOf(dayNameStart));
         if(dayName == null) {
			dayName = "NULL";
		 }

         Element dayEl = doc.createElement("dayNames");
         Element dayUrl = doc.createElement("name");
         Text dayUrlTest = doc.createTextNode(dayName);
         dayUrl.appendChild(dayUrlTest);
         dayEl.appendChild(dayUrl);
         root.appendChild(dayEl);

         dayNameStart++;
         if(dayNameStart > cal.getActualMaximum(Calendar.DAY_OF_WEEK)) {
			dayNameStart = cal.getActualMinimum((Calendar.DAY_OF_WEEK));
		 }
      }

      for (int x = cal.getFirstDayOfWeek(); x < cal.get(Calendar.DAY_OF_WEEK); x++)
      {
         Element dayEl = doc.createElement("day");
         dayEl.setAttribute("date", "0");
         dayEl.setAttribute("month", Integer.valueOf(cal.get(Calendar.MONTH)).toString());
         dayEl.setAttribute("year", Integer.valueOf(cal.get(Calendar.YEAR)).toString());
         dayEl.setAttribute("DayOfWeek", Integer.valueOf(x).toString());
         dayEl.setAttribute("week", Integer.valueOf(cal.get(Calendar.WEEK_OF_MONTH)).toString());
         Element dayUrl = doc.createElement("url");
         Text dayUrlTest = doc.createTextNode("");
         dayUrl.appendChild(dayUrlTest);
         dayEl.appendChild(dayUrl);
         root.appendChild(dayEl);
      }

      while (cal.get(Calendar.MONTH) == currentMonth)
      {
         Element dayEl = doc.createElement("day");
         dayEl.setAttribute("date", Integer.valueOf(cal.get(Calendar.DATE)).toString());
         dayEl.setAttribute("month", Integer.valueOf(cal.get(Calendar.MONTH)).toString());
         dayEl.setAttribute("year", Integer.valueOf(cal.get(Calendar.YEAR)).toString());
         dayEl.setAttribute("DayOfWeek", Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK)).toString());
         dayEl.setAttribute("week", Integer.valueOf(cal.get(Calendar.WEEK_OF_MONTH)).toString());

         String action = "/servlet/" + urlData.getServletClass() + "?action=02&day=" + cal.get(Calendar.DATE) + "&month=" + cal.get(Calendar.MONTH) + "&year=" + cal.get(Calendar.YEAR);
         if (item != null) {
			action += "&id=" + URLEncoder.encode(item.toString(), "UTF-8");
		 }
         if(index > -1) {
			action += "&index=" + index;
		 }

         Element dayUrl = doc.createElement("url");
         Text dayUrlTest = doc.createTextNode(action);
         dayUrl.appendChild(dayUrlTest);

         dayEl.appendChild(dayUrl);

         root.appendChild(dayEl);

         cal.add(Calendar.DATE, 1);
      }

      cal.add(Calendar.DATE, -1);
      for (int x = cal.get(Calendar.DAY_OF_WEEK) + 1; x <= 7; x++)
      {
         Element dayEl = doc.createElement("day");
         dayEl.setAttribute("date", "0");
         dayEl.setAttribute("month", Integer.valueOf(cal.get(Calendar.MONTH)).toString());
         dayEl.setAttribute("year", Integer.valueOf(cal.get(Calendar.YEAR)).toString());
         dayEl.setAttribute("DayOfWeek", Integer.valueOf(x).toString());
         dayEl.setAttribute("week", Integer.valueOf(cal.get(Calendar.WEEK_OF_MONTH)).toString());
         Element dayUrl = doc.createElement("url");
         Text dayUrlTest = doc.createTextNode("");
         dayUrl.appendChild(dayUrlTest);
         dayEl.appendChild(dayUrl);
         root.appendChild(dayEl);
      }

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-cal.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private ScheduleItem[] filterItems(ScheduleItem[] itemsArray, boolean past)
   {
      Vector<ScheduleItem> filteredList = new Vector<>();
      Date now = new Date();

      for (ScheduleItem item : itemsArray) {
         if (past)
         {
            if (item.getStop().getTime() < now.getTime()) {
				filteredList.add(item);
			}
         }
         else
         {
            if (item.getStop().getTime() >= now.getTime()) {
				filteredList.add(item);
			}
         }

      }

      return filteredList.toArray(new ScheduleItem[0]);
   }

   private byte[] getScheduleTable(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int start = 0;
      int show = 10;

      try
      {
         start = Integer.parseInt(urlData.getParameter("start"));
         show = Integer.parseInt(urlData.getParameter("show"));
      }
      catch (Exception e)
      {
      }

      if (start < 0) {
		start = 0;
	  }

      int filter = 0;
      try
      {
         filter = Integer.parseInt(urlData.getParameter("filter"));
      }
      catch (Exception e)
      {
      }

      ScheduleItem[] itemsArray = store.getScheduleArray();

      // Filter items
      //itemsArray = filterItems(itemsArray, filterList);
      itemsArray = filterItems(itemsArray, (filter != 0));

      Arrays.sort(itemsArray);

      int end = show + start;
      if ((show + start) >= itemsArray.length) {
		end = itemsArray.length;
	  }

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "schedules", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("start", Integer.valueOf(start).toString());
      root.setAttribute("end", Integer.valueOf(end).toString());
      root.setAttribute("show", Integer.valueOf(show).toString());
      root.setAttribute("total", Integer.valueOf(itemsArray.length).toString());
      root.setAttribute("filter", Integer.valueOf(filter).toString());

      Element sch = null;
      Element elm = null;
      Text text = null;

      for (int x = start; x < end; x++)
      {
         ScheduleItem item = itemsArray[x];

         sch = doc.createElement("schedule");
         sch.setAttribute("id", item.toString());

         SimpleDateFormat df = new SimpleDateFormat("E dd MMM h:mm a");
         sch.setAttribute("start", df.format(item.getStart()));

         elm = doc.createElement("schName");
         if (item.getName().length() > 0) {
			text = doc.createTextNode(item.getName());
		 } else {
			text = doc.createTextNode("No Name");
		 }
         elm.appendChild(text);
         sch.appendChild(elm);

         elm = doc.createElement("schSubName");
         if(item.getCreatedFrom() != null && item.getCreatedFrom().getSubName().length() > 0) {
			text = doc.createTextNode(item.getCreatedFrom().getSubName());
		 } else {
			text = doc.createTextNode("No SubName");
		 }
         elm.appendChild(text);
         sch.appendChild(elm);

         elm = doc.createElement("schDur");
         text = doc.createTextNode(Integer.valueOf(item.getDuration()).toString());
         elm.appendChild(text);
         sch.appendChild(elm);

         elm = doc.createElement("schChannel");
         text = doc.createTextNode(item.getChannel());
         elm.appendChild(text);
         sch.appendChild(elm);

         elm = doc.createElement("itemState");
         text = doc.createTextNode(Integer.valueOf(item.getState()).toString());
         elm.appendChild(text);
         sch.appendChild(elm);

         elm = doc.createElement("schStatus");
         text = doc.createTextNode(item.getStatus());
         elm.appendChild(text);
         sch.appendChild(elm);

         String type = "";
         if (item.getType() == ScheduleItem.ONCE) {
			type = "Once";
		 } else if (item.getType() == ScheduleItem.DAILY) {
			type = "Daily";
		 } else if (item.getType() == ScheduleItem.WEEKLY) {
			type = "Weekly";
		 } else if (item.getType() == ScheduleItem.MONTHLY) {
			type = "Monthly";
		 } else if (item.getType() == ScheduleItem.WEEKDAY) {
			type = "Week Day";
		 } else if (item.getType() == ScheduleItem.EPG) {
			type = "EPG";
		 } else {
			type = "?" + item.getType() + "?";
		 }

         elm = doc.createElement("schType");
         text = doc.createTextNode(type);
         elm.appendChild(text);
         sch.appendChild(elm);

         String action = "/servlet/" + urlData.getServletClass() + "?action=04&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "&start=" + start + "&show=" + show;

         elm = doc.createElement("action");
         text = doc.createTextNode(action);
         elm.appendChild(text);
         sch.appendChild(elm);

         root.appendChild(sch);
      }

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-schedules.xsl", urlData, headers);

      // set current page to check point for back
      transformer.addCookie("backURL", urlData.getReqString());

      return transformer.doTransform();
   }

   private void addButton(Document doc, String name, String url, boolean confirm)
   {
      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("button");
      button.setAttribute("name", name);
      elm = doc.createElement("url");

      text = doc.createTextNode(url);
      elm.appendChild(text);
      button.appendChild(elm);

      if(confirm)
      {
         elm = doc.createElement("confirm");
         text = doc.createTextNode("true");
         elm.appendChild(text);
         button.appendChild(elm);
      }

      doc.getDocumentElement().appendChild(button);
   }

   private byte[] showItemOptions(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String id = urlData.getParameter("id");

      int start = 0;
      int show = 10;

      try
      {
         start = Integer.parseInt(urlData.getParameter("start"));
         show = Integer.parseInt(urlData.getParameter("show"));
      }
      catch (Exception e)
      {
      }

      if (start < 0) {
		start = 0;
	  }

      ScheduleItem item = store.getScheduleItem(id);

      if (item == null)
      {
         StringBuffer out = new StringBuffer(4096);
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/ApplyTransformRes?xml=root&xsl=kb-buttons\n\n");
         return out.toString().getBytes();
      }

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "buttons", null);

      String action = "javascript:jumpBack()";

      addButton(doc, "Back", action, false);

      // add stop button if running
      // add Add Time button if Running
      if(item.getState() == ScheduleItem.RUNNING)
      {
         action = "/servlet/" + urlData.getServletClass() + "?action=06&id=" + id;
         addButton(doc, "Stop Running Schedule", action, true);

         action = "/servlet/" + urlData.getServletClass() + "?action=06&id=" + id + "&deleteFiles=1&disableTasks=1";
         addButton(doc, "Stop and Delete Files", action, true);

         action = "/servlet/" + urlData.getServletClass() + "?action=07&id=" + id;
         addButton(doc, "Add Time", action, false);
      }

      // Edit item
      if(item.getType() != ScheduleItem.EPG &&
         (item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.ERROR))
      {
         action = "/servlet/" + urlData.getServletClass() + "?action=01&id=" + id;
         addButton(doc, "Edit Item", action, false);
      }

      // Delete item
      if((item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.ERROR))
      {
         action = "/servlet/" + urlData.getServletClass() + "?action=05&id=" + id + "&start=" + start + "&show=" + show;

         addButton(doc, "Delete Item", action, true);
      }

      // Skip to next repeating items
      if((item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.ERROR) &&
         item.getType() != ScheduleItem.EPG &&
         item.getType() != ScheduleItem.ONCE)
      {
         action = "/servlet/" + urlData.getServletClass() + "?action=08&id=" + id + "&start=" + start + "&show=" + show;
         addButton(doc, "Skip to Next", action, false);
      }

      // Add time buffer to start and stop times
      if((item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.ERROR) &&
         item.getType() == ScheduleItem.ONCE)
      {
         action = "/servlet/" + urlData.getServletClass() + "?action=07&id=" + id + "&start=" + start + "&show=" + show;
         addButton(doc, "Add Time", action, false);
      }

      // Show item in EPG
      Calendar cal = Calendar.getInstance();
      cal.setTime(item.getStart());
      action = "/servlet/KBEpgDataRes?action=01" + "&year=" + cal.get(Calendar.YEAR) + "&month=" + (cal.get(Calendar.MONTH) + 1) + "&day=" + cal.get(Calendar.DATE) + "&start=" + cal.get(Calendar.HOUR_OF_DAY);
      addButton(doc, "Show in EPG", action, false);

      // View item log
      action = "/servlet/" + urlData.getServletClass() + "?action=09&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "&start=" + start + "&show=" + show;
      addButton(doc, "View Item Log", action, false);

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] deleteItem(HTTPurl urlData) throws Exception
   {
      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
	  }

      String id = urlData.getParameter("id");

      StringBuffer out = new StringBuffer(4096);
      ScheduleItem item = store.getScheduleItem(id);

      if(item != null && (item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.ERROR))
      {
         store.removeScheduleItem(id);
         store.saveSchedule(null);
      }

      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] stopRunningTask(HTTPurl urlData) throws Exception
   {
      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
	  }

      StringBuffer out = new StringBuffer(4096);
      String id = urlData.getParameter("id");

      boolean disableTasks = "1".equals(urlData.getParameter("disableTasks"));
      boolean deleteFiles = "1".equals(urlData.getParameter("deleteFiles"));

      ScheduleItem si = null;

      ThreadLock.getInstance().getLock();
      try
      {
         si = store.getScheduleItem(id);
         if(si != null && si.getState() == ScheduleItem.RUNNING)
         {
            if(disableTasks)
            {
               si.setPostTaskEnabled(false);
               si.log("Post Tasks Disabled");
            }

            si.abort();
            si.setStatus("Aborting");
            si.setState(ScheduleItem.ABORTED);
            si.log("Item marked for abortion");
         }
      }
      finally
      {
         ThreadLock.getInstance().releaseLock();
      }

      // wait for the item to be stopped before returning
      int counts = 10;
      while(si.getState() == ScheduleItem.ABORTED && counts > 0)
      {
         counts--;
         Thread.sleep(1000);
      }

      // if required delete any created files
      if(deleteFiles)
      {
         Vector<File> capFiles = si.getCaptureFiles();
         for (File capFile : capFiles) {
            try
            {
               System.out.println("Deleting : " + capFile.getAbsolutePath());
               capFile.delete();
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      }

      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }
}
