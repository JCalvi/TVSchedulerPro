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

import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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

class KBEpgDataRes extends HTTPResponse
{

   public KBEpgDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if ("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showEPG(urlData, headers));
         return;
      }
      else if ("03".equals(urlData.getParameter("action")))
      {
         outStream.write(reloadXMLTVdata(urlData, headers));
         return;
      }
      else if ("04".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(rescanXMLTVdata(urlData, headers));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if ("05".equals(urlData.getParameter("action")))
      {
         outStream.write(showProgInfo(urlData, headers));
         return;
      }
      else
      {
         outStream.write(showEpgIndex(urlData, headers));
         return;
      }
   }

   private byte[] showProgInfo(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      String id = urlData.getParameter("id");
      String wsChan = urlData.getParameter("channel");
      String epgChan = guide.getEpgChannelFromMap(wsChan);
      GuideItem item = guide.getProgram(epgChan, id);

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "proginfo", null);
      Element root = doc.getDocumentElement();
      root.setAttribute("id", id);

      Calendar cal = Calendar.getInstance();
      cal.setTime(item.getStart());

      root.setAttribute("hour", Integer.valueOf(cal.get(Calendar.HOUR_OF_DAY)).toString());
      root.setAttribute("day", Integer.valueOf(cal.get(Calendar.DATE)).toString());
      root.setAttribute("month", Integer.valueOf(cal.get(Calendar.MONTH)+1).toString());
      root.setAttribute("year", Integer.valueOf(cal.get(Calendar.YEAR)).toString());

      Element itemEl = null;
      Text text = null;

      itemEl = doc.createElement("title");
      text = doc.createTextNode(removeChars(item.getName()));
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      itemEl = doc.createElement("sub-title");
      text = doc.createTextNode(item.getSubName());
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      SimpleDateFormat df = new SimpleDateFormat("EEE MMM d h:mm aa");

      itemEl = doc.createElement("start");
      text = doc.createTextNode(df.format(item.getStart()));
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      itemEl = doc.createElement("stop");
      text = doc.createTextNode(df.format(item.getStop()));
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      itemEl = doc.createElement("duration");
      text = doc.createTextNode(Integer.valueOf(item.getDuration()).toString());
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      itemEl = doc.createElement("channel");
      text = doc.createTextNode(wsChan);
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      String allCats = "";
      for(int x = 0; x < item.getCategory().size(); x++)
      {
         allCats += item.getCategory().get(x);
         if(x < item.getCategory().size() - 1) {
			allCats += ",";
		 }
      }
      itemEl = doc.createElement("category");
      text = doc.createTextNode(allCats);
      itemEl.appendChild(text);
      root.appendChild(itemEl);

	  String sFlags = "";

      if (item.getHighDef()) {
		sFlags = sFlags + "HDTV ";
	  }
      if (item.getWidescreen()) {
		sFlags = sFlags + "WS ";
	  }
      if (item.getAC3()) {
		sFlags = sFlags + "AC3 ";
	  }
      if (item.getSurround()) {
		sFlags = sFlags + "SRS ";
	  }
      if (item.getCaptions()) {
		sFlags = sFlags + "CC ";
	  }
      if (item.getInteractive()) {
		sFlags = sFlags + "*INTERACTIVE* ";
	  }
      if (item.getPremiere()) {
		sFlags = sFlags + "*PREMIERE* ";
	  }
      if (item.getLive()) {
		sFlags = sFlags + "*LIVE* ";
	  }
      if (item.getLastChance()) {
		sFlags = sFlags + "*FINAL* ";
	  }
      if (item.getRepeat()) {
		sFlags = sFlags + "(REPEAT)";
	  }

      if (sFlags.length() > 0)
      {
		  itemEl = doc.createElement("flags");
		  text = doc.createTextNode(sFlags);
		  itemEl.appendChild(text);
		  root.appendChild(itemEl);
      }

      itemEl = doc.createElement("description");
      text = doc.createTextNode(item.getDescription());
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      // get schedules for this time
      Vector<ScheduleItem> schItems = new Vector<>();
      store.getSchedulesWhenInc(item.getStart(), item.getStop(), wsChan, schItems);
      ScheduleItem programSchedule = null;

      boolean inexactCFMatch = false;
      int startCFTol = 0;
      int durCFTol = 0;
      try
      {
         inexactCFMatch = store.getProperty("guide.item.cfmatch.inexact").equals("1");
         startCFTol = Integer.parseInt(store.getProperty("guide.item.cftol.start"));
         durCFTol = Integer.parseInt(store.getProperty("guide.item.cftol.duration"));
      }
      catch (Exception e){}

      for(int schIndex = 0; schIndex < schItems.size(); schIndex++)
      {
         ScheduleItem sch = schItems.get(schIndex);
         GuideItem createdFrom = sch.getCreatedFrom();
         if(createdFrom != null && createdFrom.matches(item,inexactCFMatch,startCFTol,durCFTol))
         {
            schItems.remove(schIndex);
            programSchedule = sch;
            break;
         }
      }

      if(programSchedule == null)
      {
         // Add URl Creation
         String addLink = "/servlet/KBScheduleDataRes?action=11" +
            "&channel=" + URLEncoder.encode(wsChan, "UTF-8") + "&id=" + item.toString();

         Element addUrlElement = doc.createElement("addURL");
         addUrlElement.setAttribute("name", "Add to Schedules");
         Text addUrlText = doc.createTextNode(addLink);
         addUrlElement.appendChild(addUrlText);
         root.appendChild(addUrlElement);
      }
      else
      {
         // Edit Schedule
         String editLink = "/servlet/KBScheduleDataRes?action=04&id=" + programSchedule.toString();

         Element addUrlElement = doc.createElement("addURL");
         addUrlElement.setAttribute("name", "View Schedule Info");
         Text addUrlText = doc.createTextNode(editLink);
         addUrlElement.appendChild(addUrlText);
         root.appendChild(addUrlElement);
      }

      String autoAddString = "/servlet/KBAutoAddRes?action=13&itemID=" + id + "&chan=" +
         URLEncoder.encode(wsChan, "UTF-8");

      Element autoAddUrlElement = doc.createElement("autoAddURL");
      Text autoAddUrlText = doc.createTextNode(autoAddString);
      autoAddUrlElement.appendChild(autoAddUrlText);
      root.appendChild(autoAddUrlElement);

      itemEl = doc.createElement("url-source");
      text = doc.createTextNode(item.getURL());
      itemEl.appendChild(text);
      root.appendChild(itemEl);

      // Do transform and return data
      XSL transformer = new XSL(doc, "kb-showproginfo.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] rescanXMLTVdata(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      StringBuffer buff1 = new StringBuffer();

      GuideStore guide = GuideStore.getInstance();

      store.removeEPGitems(buff1, 0);
      guide.addEPGmatches(buff1, 0);
      store.saveSchedule(null);

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "log", null);
      Element root = doc.getDocumentElement();
      root.setAttribute("id", "");

      Element logitem = null;
      Element elm = null;
      Text text = null;

      String[] lines = buff1.toString().split("\n");
      for (String line : lines) {
         logitem = doc.createElement("logitem");
         elm = doc.createElement("line");
         text = doc.createTextNode(line);
         elm.appendChild(text);
         logitem.appendChild(elm);
         root.appendChild(logitem);
      }

//    Do transform and return data
      XSL transformer = new XSL(doc, "kb-redo.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showEPG(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      Calendar now = Calendar.getInstance();
      now.set(Calendar.SECOND, 0);
      now.set(Calendar.MILLISECOND, 0);

      int year = -1;
      try
      {
         year = Integer.parseInt(urlData.getParameter("year"));
      }
      catch (Exception e)
      {
      }
      if (year == -1) {
		year = now.get(Calendar.YEAR);
	  }

      int month = -1;
      try
      {
         month = Integer.parseInt(urlData.getParameter("month"));
      }
      catch (Exception e)
      {
      }
      if (month == -1) {
		month = now.get(Calendar.MONTH) + 1;
	  }

      int day = -1;
      try
      {
         day = Integer.parseInt(urlData.getParameter("day"));
      }
      catch (Exception e)
      {
      }
      if (day == -1) {
		day = now.get(Calendar.DATE);
	  }

      int startHour = -1;
      try
      {
         startHour = Integer.parseInt(urlData.getParameter("start"));
      }
      catch (Exception e)
      {
      }
      if (startHour == -1) {
		startHour = now.get(Calendar.HOUR_OF_DAY);
	  }

      int timeSpan = 3;
      try
      {
         timeSpan = Integer.parseInt(urlData.getParameter("span"));
      }
      catch (Exception e)
      {
      }

      String selected = urlData.getParameter("selected");
      if(selected == null || selected.length() == 0) {
		selected = "";
	  }

      SimpleDateFormat df = new SimpleDateFormat("h:mma");
      SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss Z");

      boolean showUnlinked = store.getProperty("epg.showunlinked").equals("1");

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "epg", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("year", Integer.valueOf(year).toString());
      root.setAttribute("month", Integer.valueOf(month).toString());
      root.setAttribute("day", Integer.valueOf(day).toString());
      root.setAttribute("start", Integer.valueOf(startHour).toString());
      root.setAttribute("show", Integer.valueOf(timeSpan).toString());

      Element itemEl = null;
      Element elm = null;
      Text text = null;

      // add now time/date
      Calendar startPointer = Calendar.getInstance();
      startPointer.set(Calendar.SECOND, 0);
      startPointer.set(Calendar.MINUTE, 0);
      startPointer.set(Calendar.MILLISECOND, 0);
      startPointer.set(Calendar.YEAR, year);
      startPointer.set(Calendar.MONTH, month-1);
      startPointer.set(Calendar.DATE, day);
      startPointer.set(Calendar.HOUR_OF_DAY, startHour);

      long nowLong = new Date().getTime();
      long startLong = startPointer.getTime().getTime();
      long minPast = (nowLong - startLong) / (1000 * 60);

      if(minPast < (timeSpan * 60) && minPast > -1)
      {
	      itemEl = doc.createElement("now_pointer");
	      itemEl.setAttribute("min", Integer.valueOf((int)minPast).toString());
	      root.appendChild(itemEl);
      }

      //////////////////////////////////////////////////////////////////
      // Add the time items
      //////////////////////////////////////////////////////////////////

      int hour = startHour;

      String xm = "am";

      if (hour > 12) {
		hour = startHour - 12;
	  }
      if (startHour >= 12) {
		xm = "pm";
	  }

      if (hour == 0) {
		hour = 12;
	  }

      int min = 0;
      int totalCols = timeSpan * 2;

      for (int x = 0; x < totalCols; x++)
      {

         itemEl = doc.createElement("time");
         text = doc.createTextNode(hour + ":" + intToStr(min) + xm);
         itemEl.appendChild(text);
         root.appendChild(itemEl);

         min += 30;
         if (min == 60)
         {
            hour += 1;
            min = 0;
         }

         if (hour == 12 && min == 0)
         {
            if (xm.equals("am")) {
				xm = "pm";
			} else {
				xm = "am";
			}
         }

         if (hour == 13)
         {
            hour = 1;
         }
      }

      //////////////////////////////////////////////////////////////////
      // Add the channels
      //////////////////////////////////////////////////////////////////

      GuideStore epgStore = GuideStore.getInstance();
      Vector<String[]> channelMap = epgStore.getChannelMap();

      for (String[] element : channelMap) {
         String[] map = element;

         Element channel = doc.createElement("channel");
         channel.setAttribute("id", map[0]);

         Element disName = doc.createElement("display-name");
         Text chaName = doc.createTextNode(map[0]);
         disName.appendChild(chaName);

         channel.appendChild(disName);

         root.appendChild(channel);

      }

      // ////////////////////////////////////////////////////////////////
      // Add pre/next navigation links
      //////////////////////////////////////////////////////////////////

      Calendar start = Calendar.getInstance();
      start.set(Calendar.YEAR, year);
      start.set(Calendar.MONTH, month - 1);
      start.set(Calendar.DATE, day);
      start.set(Calendar.HOUR_OF_DAY, startHour);
      start.set(Calendar.MINUTE, 0);
      start.set(Calendar.SECOND, 0);
      start.add(Calendar.SECOND, -1);
      start.set(Calendar.MILLISECOND, 0);

      Calendar end = Calendar.getInstance();
      end.setTime(start.getTime());
      end.add(Calendar.HOUR_OF_DAY, timeSpan);

      Calendar startTime = Calendar.getInstance();
      startTime.set(Calendar.MILLISECOND, 0);

      start.add(Calendar.HOUR_OF_DAY, (-1 * (timeSpan - 1)));

      String prevLink = "/servlet/" + urlData.getServletClass() + "?action=01&" + "year=" + start.get(Calendar.YEAR) + "&" + "month=" + (start.get(Calendar.MONTH) + 1) + "&" + "day=" + start.get(Calendar.DATE) + "&" + "start=" + start.get(Calendar.HOUR_OF_DAY) + "&" + "span=" + timeSpan;

      // set to this time span
      start.add(Calendar.HOUR_OF_DAY, (timeSpan - 1));

      root.setAttribute("title", store.dayName.get(Integer.valueOf(start.get(Calendar.DAY_OF_WEEK))) + " (" + start.get(Calendar.DATE) + "/" + (start.get(Calendar.MONTH) + 1) + "/" + start.get(Calendar.YEAR) + ")");

      // work out next time span
      start.add(Calendar.HOUR_OF_DAY, (timeSpan + 1));

      String nextLink = "/servlet/" + urlData.getServletClass() + "?action=01&" + "year=" + start.get(Calendar.YEAR) + "&" + "month=" + (start.get(Calendar.MONTH) + 1) + "&" + "day=" + start.get(Calendar.DATE) + "&" + "start=" + start.get(Calendar.HOUR_OF_DAY) + "&" + "span=" + timeSpan;

      start.add(Calendar.HOUR_OF_DAY, (-1 * (timeSpan + 1)));

      itemEl = doc.createElement("navigation");

      elm = doc.createElement("next");
      text = doc.createTextNode(nextLink);
      elm.appendChild(text);
      itemEl.appendChild(elm);

      elm = doc.createElement("previous");
      text = doc.createTextNode(prevLink);
      elm.appendChild(text);
      itemEl.appendChild(elm);

      elm = doc.createElement("selected");
      text = doc.createTextNode(selected);
      elm.appendChild(text);
      itemEl.appendChild(elm);

      root.appendChild(itemEl);

      //////////////////////////////////////////////////////////////////
      // Add the programs
      //////////////////////////////////////////////////////////////////

      HashMap<String, Vector<ScheduleItem>> schedulesLeftOver = new HashMap<>();

      Set<String> wsChannels = store.getChannels().keySet();

      boolean channelMapped = true;
      for (int x = 0; x < channelMap.size(); x++)
      {
         String[] map = channelMap.get(x);
         String channelName = map[0];

         if (channelName == null || !wsChannels.contains(map[0]))
         {
            channelName = "Not Mapped";
            channelMapped = false;
         }
         else
         {
            channelMapped = true;

            GuideItem[] programs = epgStore.getProgramsInc(start.getTime(), end.getTime(), map[1]);

            Vector<ScheduleItem> schItems = new Vector<>();
            store.getSchedulesWhenInc(start.getTime(), end.getTime(), channelName, schItems);

            int colCount = 0;

            for (int y = 0; y < programs.length; y++)
            {
               GuideItem item = programs[y];

               ///////////////////////////////////////////////////////////////////////////////////////////////////////////
               // add start padding if needed
               //
               start.add(Calendar.SECOND, 1);
               startTime.setTime(item.getStart());
               long pastStart = startTime.getTime().getTime() - start.getTime().getTime();
               if (y == 0 && pastStart > 0)
               {
                  Element program_PH = doc.createElement("programme");

                  program_PH.setAttribute("start", df.format(start.getTime()));
                  program_PH.setAttribute("stop", df.format(item.getStart()));
                  program_PH.setAttribute("channel", channelName);

                  Element titleElement = doc.createElement("title");
                  Text titleText = doc.createTextNode("EMPTY");
                  titleElement.appendChild(titleText);
                  program_PH.appendChild(titleElement);

                  Element subTitleElement = doc.createElement("sub-title");
                  Text subTitleText = doc.createTextNode("empty");
                  subTitleElement.appendChild(subTitleText);
                  program_PH.appendChild(subTitleElement);

                  Element descElement = doc.createElement("desc");
                  Text descText = doc.createTextNode("empty");
                  descElement.appendChild(descText);
                  program_PH.appendChild(descElement);

                  Element lengthElement = doc.createElement("length");
                  lengthElement.setAttribute("units", "minutes");
                  Text lengthText = doc.createTextNode(Long.valueOf(pastStart / (1000 * 60)).toString());
                  lengthElement.appendChild(lengthText);
                  program_PH.appendChild(lengthElement);

                  Element programLengthElement = doc.createElement("programLength");
                  programLengthElement.setAttribute("units", "minutes");
                  Text programLengthText = doc.createTextNode(Long.valueOf(pastStart / (1000 * 60)).toString());
                  programLengthElement.appendChild(programLengthText);
                  program_PH.appendChild(programLengthElement);

                  root.appendChild(program_PH);

                  colCount += (int) (pastStart / (1000 * 60));
               }
               start.add(Calendar.SECOND, -1);
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////

               ///////////////////////////////////////////////////////////////////////////////////////////////////////////
               // Add padding item if needed
               //
               if (y > 0)
               {
                  long skip = item.getStart().getTime() - (programs[y - 1].getStart().getTime() + (programs[y - 1].getDuration() * 1000 * 60));
                  if (skip > 0)
                  {
                     System.out.println("Skipping : " + skip);

                     Element program_PH = doc.createElement("programme");

                     program_PH.setAttribute("start", df.format(programs[y - 1].getStop()));
                     program_PH.setAttribute("stop", df.format(item.getStart()));
                     program_PH.setAttribute("channel", channelName);

                     Element titleElement = doc.createElement("title");
                     Text titleText = doc.createTextNode("EMPTY");
                     titleElement.appendChild(titleText);
                     program_PH.appendChild(titleElement);

                     Element subTitleElement = doc.createElement("sub-title");
                     Text subTitleText = doc.createTextNode("empty");
                     subTitleElement.appendChild(subTitleText);
                     program_PH.appendChild(subTitleElement);

                     Element descElement = doc.createElement("desc");
                     Text descText = doc.createTextNode("empty");
                     descElement.appendChild(descText);
                     program_PH.appendChild(descElement);

                     Element lengthElement = doc.createElement("length");
                     lengthElement.setAttribute("units", "minutes");
                     Text lengthText = doc.createTextNode(Long.valueOf(skip / (1000 * 60)).toString());
                     lengthElement.appendChild(lengthText);
                     program_PH.appendChild(lengthElement);

                     Element programLengthElement = doc.createElement("programLength");
                     programLengthElement.setAttribute("units", "minutes");
                     Text programLengthText = doc.createTextNode(Long.valueOf(skip / (1000 * 60)).toString());
                     programLengthElement.appendChild(programLengthText);
                     program_PH.appendChild(programLengthElement);

                     root.appendChild(program_PH);

                     colCount += (int) (skip / (1000 * 60));
                  }
               }
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////

               // is there a schedule for this Program

              boolean inexactCFMatch = false;
              int startCFTol = 0;
              int durCFTol = 0;
              try
              {
                 inexactCFMatch = store.getProperty("guide.item.cfmatch.inexact").equals("1");
                 startCFTol = Integer.parseInt(store.getProperty("guide.item.cftol.start"));
                 durCFTol = Integer.parseInt(store.getProperty("guide.item.cftol.duration"));
              }
              catch (Exception e){}

               ScheduleItem programSchedule = null;
               for(int schIndex = 0; schIndex < schItems.size(); schIndex++)
               {
                  ScheduleItem sch = schItems.get(schIndex);
                  GuideItem createdFrom = sch.getCreatedFrom();
                  if(createdFrom != null)
                  {
                     if(createdFrom.matches(item,inexactCFMatch,startCFTol,durCFTol))
                     {
                        schItems.remove(schIndex);
                        programSchedule = sch;
                        break;
                     }
                  }
               }

               Element program = doc.createElement("programme");

               program.setAttribute("start", df.format(item.getStart()));
               program.setAttribute("stop", df.format(item.getStop()));
               program.setAttribute("channel", channelName);

               Element titleElement = doc.createElement("title");
               Text titleText = doc.createTextNode(removeChars(item.getName()));
               titleElement.appendChild(titleText);
               program.appendChild(titleElement);

               Element subTitleElement = doc.createElement("sub-title");
               Text subTitleText = doc.createTextNode(removeChars(item.getSubName()));
               subTitleElement.appendChild(subTitleText);
               program.appendChild(subTitleElement);

               for (String element : item.getCategory()) {
                  Element catElement = doc.createElement("category");
                  Text catText = doc.createTextNode(element);
                  catElement.appendChild(catText);
                  program.appendChild(catElement);
               }

               Element descElement = doc.createElement("desc");
               Text descText = doc.createTextNode(removeChars(item.getDescription()));
               descElement.appendChild(descText);
               program.appendChild(descElement);

               int fits = 0;
               int colSpan = item.getDuration();
               if (item.getStart().getTime() < start.getTime().getTime() && item.getStop().getTime() > end.getTime().getTime())
               {
                  fits = 1;
                  colSpan = (timeSpan * 60);
               }
               else if (y == 0 && start.getTime().getTime() > item.getStart().getTime())
               {
                  fits = 2;
                  colSpan -= (int)((start.getTime().getTime() - item.getStart().getTime()) / (1000 * 60)) + 1 ;
               }
               else if (y == programs.length - 1 && (item.getStop().getTime() - 5000) > end.getTime().getTime())
               {
                  fits = 3;
                  colSpan = (timeSpan * 60) - colCount;
               }

               colCount += colSpan;

               Element lengthElement = doc.createElement("length");
               lengthElement.setAttribute("units", "minutes");
               lengthElement.setAttribute("fits", Integer.valueOf(fits).toString());
               Text lengthText = doc.createTextNode(Integer.valueOf(colSpan).toString());
               lengthElement.appendChild(lengthText);
               program.appendChild(lengthElement);

               Element programLengthElement = doc.createElement("programLength");
               programLengthElement.setAttribute("units", "minutes");
               Text programLengthText = doc.createTextNode(Long.valueOf(item.getDuration()).toString());
               programLengthElement.appendChild(programLengthText);
               program.appendChild(programLengthElement);

               String addLink = "";
               if (channelMapped)
               {
                  addLink = "/servlet/KBScheduleDataRes?action=11" + "&channel=" + URLEncoder.encode(map[0], "UTF-8") + "&id=" + item.toString();
               }

               Element infoUrlElement = doc.createElement("progAdd");
               Text infoUrlText = doc.createTextNode(addLink);
               infoUrlElement.appendChild(infoUrlText);
               program.appendChild(infoUrlElement);

               String detailsLink = "";
               if (channelMapped)
               {
                  detailsLink = "/servlet/KBEpgDataRes?action=05&" + "channel=" + URLEncoder.encode(map[0], "UTF-8") + "&id=" + URLEncoder.encode(item.toString(), "UTF-8");
               }

               Element detailsUrlElement = doc.createElement("showDetails");
               Text detailsUrlText = doc.createTextNode(detailsLink);
               detailsUrlElement.appendChild(detailsUrlText);
               program.appendChild(detailsUrlElement);

               // add full times
               Element fullTimes = doc.createElement("full_times");
               fullTimes.setAttribute("start", df2.format(item.getStart()));
               fullTimes.setAttribute("stop", df2.format(item.getStop()));
               program.appendChild(fullTimes);

               Element schElement = doc.createElement("scheduled");
               if(programSchedule == null)
               {
                  schElement.setAttribute("state", "-1");
               }
               else
               {
                  schElement.setAttribute("state", Integer.valueOf(programSchedule.getState()).toString());

                  Element schElementStart = doc.createElement("start");
                  Text schTextStart = doc.createTextNode(df.format(programSchedule.getStart()));
                  schElementStart.appendChild(schTextStart);
                  schElement.appendChild(schElementStart);

                  Element schElementStop = doc.createElement("stop");
                  Text schTextStop = doc.createTextNode(df.format(programSchedule.getStop()));
                  schElementStop.appendChild(schTextStop);
                  schElement.appendChild(schElementStop);
               }
               program.appendChild(schElement);

               root.appendChild(program);
            }

            Vector<ScheduleItem> remainingItems = schedulesLeftOver.get(channelName);
            if(remainingItems == null)
            {
               remainingItems = new Vector<>();
               schedulesLeftOver.put(channelName, remainingItems);
            }

            // now add all the schedules that were not created form a epg item
            for(int schIndex = 0; schIndex < schItems.size(); schIndex++)
            {
               ScheduleItem sch = schItems.get(schIndex);
               remainingItems.add(sch);
            }
         }
      }

      //////////////////////////////////////////////////////////////////
      // Add Schedules
      //////////////////////////////////////////////////////////////////

      Text textNode = null;
      Element elementNode = null;

      for (int x = 0; x < channelMap.size(); x++)
      {
         String[] map = channelMap.get(x);

         int padding = 0;
         int loops = 0;

         String chanName = map[0];

         if (chanName != null)
         {
            Vector<ScheduleItem> items = schedulesLeftOver.get(chanName);

            ScheduleItem[] schedules = items.toArray(new ScheduleItem[0]);

            while (schedules.length > 0)
            {
               Vector<ScheduleItem> overlap = new Vector<>();

               int colCount = 0;

               for (int sch = 0; sch < schedules.length; sch++)
               {
                  ScheduleItem item = schedules[sch];
                  startTime.setTime(item.getStart());
                  startTime.set(Calendar.MILLISECOND, 0);

                  if(!showUnlinked && item.getCreatedFrom() != null)
                  {
                     // not show this item
                  }
                  else if (sch > 0 && ((item.getStart().getTime() < schedules[sch - 1].getStop().getTime() && item.getStart().getTime() > schedules[sch - 1].getStart().getTime()) || (item.getStop().getTime() > schedules[sch - 1].getStart().getTime() && item.getStart().getTime() < schedules[sch - 1].getStop().getTime())))
                  {
                     overlap.add(item);
                  }
                  else
                  {

                     padding = 0;

                     ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                     // add start padding if needed
                     //
                     //start.add(Calendar.SECOND, 1);
                     startTime.setTime(item.getStart());
                     long pastStart = startTime.getTime().getTime() - start.getTime().getTime();
                     if (sch == 0 && pastStart > 0)
                     {
                        padding = (int) (pastStart / (1000 * 60));
                        colCount += (int) (pastStart / (1000 * 60));
                     }
                     start.add(Calendar.SECOND, -1);
                     ///////////////////////////////////////////////////////////////////////////////////////////////////////////

                     ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                     // Add padding item if needed
                     //
                     if (sch > 0)
                     {
                        long skip = item.getStart().getTime() - (schedules[sch - 1].getStart().getTime() + (schedules[sch - 1].getDuration() * 1000 * 60));
                        if (skip > 0)
                        {
                           System.out.println("Skipping : " + skip);

                           padding = ((int) (pastStart / (1000 * 60))) - colCount;
                           colCount += (int) (skip / (1000 * 60));
                        }
                     }
                     ///////////////////////////////////////////////////////////////////////////////////////////////////////////

                     int fits = 0;
                     int colSpan = item.getDuration();
                     if (item.getStart().getTime() < start.getTime().getTime() && item.getStop().getTime() > end.getTime().getTime())
                     {
                        // Just make it the full show width if it start before
                        // and ends after our show span
                        colSpan = (timeSpan * 60);
                        fits = 1;
                     }
                     else if (sch == 0 && start.getTime().getTime() > item.getStart().getTime())
                     {
                        // if it is the first one and starts before our show
                        // span then cut the beginning off
                        colSpan -= (int)((start.getTime().getTime() - item.getStart().getTime()) / (1000 * 60)) + 1;
                        fits = 2;
                     }
                     else if (item.getStop().getTime() > end.getTime().getTime())
                     {
                        // if it ends after your show span cut the end off
                        long temp = end.getTime().getTime() - item.getStart().getTime();
                        temp = temp / (1000 * 60);
                        colSpan = (int) temp + 1;
                        fits = 3;

                        //colSpan = (timeSpan * 60) - colCount;
                     }

                     colCount += colSpan;

                     Element schedule = doc.createElement("schedule");

                     schedule.setAttribute("start", df.format(item.getStart()));
                     schedule.setAttribute("stop", df.format(item.getStop()));

                     schedule.setAttribute("duration", Integer.valueOf(item.getDuration()).toString());
                     schedule.setAttribute("span", Integer.valueOf(colSpan).toString());
                     schedule.setAttribute("prePadding", Integer.valueOf(padding).toString());
                     schedule.setAttribute("overlapCount", Integer.valueOf(loops).toString());
                     schedule.setAttribute("channel", chanName);
                     schedule.setAttribute("fits", Integer.valueOf(fits).toString());

                     elementNode = doc.createElement("id");
                     textNode = doc.createTextNode(item.toString());
                     elementNode.appendChild(textNode);
                     schedule.appendChild(elementNode);

                     elementNode = doc.createElement("title");
                     textNode = doc.createTextNode(removeChars(item.getName()));
                     elementNode.appendChild(textNode);
                     schedule.appendChild(elementNode);

                     elementNode = doc.createElement("itemState");
                     textNode = doc.createTextNode(Integer.valueOf(item.getState()).toString());
                     elementNode.appendChild(textNode);
                     schedule.appendChild(elementNode);

                     elementNode = doc.createElement("itemStatus");
                     textNode = doc.createTextNode(item.getStatus());
                     elementNode.appendChild(textNode);
                     schedule.appendChild(elementNode);

                     String action = "/servlet/KBScheduleDataRes?action=04&id=" + item.toString();

                     elementNode = doc.createElement("progEdit");
                     textNode = doc.createTextNode(action);
                     elementNode.appendChild(textNode);
                     schedule.appendChild(elementNode);

                     root.appendChild(schedule);
                  }

               }

               schedules = overlap.toArray(new ScheduleItem[0]);
               loops++;

            }
         }
      }

      // Do transform and return data
      XSL transformer = new XSL(doc, "kb-epg.xsl", urlData, headers);

      // set current page to check point for back
      // remove the selected= first
      String request = urlData.getReqString();
      int indexOf = request.indexOf("&selected=");
      if(indexOf > -1)
      {
         request = request.substring(0, indexOf);
      }
      transformer.addCookie("backURL", request);

      return transformer.doTransform();
   }

   private byte[] showEpgIndex(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();
      Vector<String[]> links = guide.getEPGlinks(null);

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "files", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("back", store.getTargetURL("epg-sub", "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons"));

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("button");
      button.setAttribute("name", "Back");
      elm = doc.createElement("url");
      text = doc.createTextNode(store.getTargetURL("epg-sub", "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons"));
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      for (String[] link : links) {
         String[] item = link;
         button = doc.createElement("button");
         button.setAttribute("name", item[1]);
         elm = doc.createElement("url");
         text = doc.createTextNode("/servlet/" + urlData.getServletClass() + "?action=01&" + item[0]);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);
      }

      // Do transform and return data
      XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] reloadXMLTVdata(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      StringBuffer buff1 = new StringBuffer();

      GuideStore guide = GuideStore.getInstance();

      guide.loadXMLTV(buff1, 0);

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "log", null);
      Element root = doc.getDocumentElement();
      root.setAttribute("id", "");

      Element logitem = null;
      Element elm = null;
      Text text = null;

      String[] lines = buff1.toString().split("\n");
      for (String line : lines) {
         logitem = doc.createElement("logitem");
         elm = doc.createElement("line");
         text = doc.createTextNode(line);
         elm.appendChild(text);
         logitem.appendChild(elm);
         root.appendChild(logitem);
      }

      // Do transform and return data
      XSL transformer = new XSL(doc, "kb-redo.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private String intToStr(int num)
   {
      if (num < 10) {
		return "0" + num;
	  } else {
		return Integer.valueOf(num).toString();
	  }

   }

   private String removeChars(String data)
   {
      data = data.replaceAll("'", "`");
      data = data.replaceAll("\"", "`");

      return data;
   }
}
