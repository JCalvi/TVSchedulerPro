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
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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

// reload/rescan/email
// http://localhost:8420/servlet/EpgDataRes?action=14&reload=1&rescan=1&email=1

class EpgDataRes extends HTTPResponse
{

   public EpgDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if ("02".equals(urlData.getParameter("action")))
      {
         outStream.write(checkGuideData(urlData));
         return;
      }
      else if ("03".equals(urlData.getParameter("action")))
      {
         reloadXMLTVdata(urlData, outStream);
         return;
      }
      else if ("04".equals(urlData.getParameter("action")))
      {
         outStream.write(showChannelMapping(urlData));
         return;
      }
      else if ("05".equals(urlData.getParameter("action")))
      {
         outStream.write(addChannelMapping(urlData));
         return;
      }
      else if ("06".equals(urlData.getParameter("action")))
      {
         outStream.write(showProgramDetails(urlData));
         return;
      }
      else if ("07".equals(urlData.getParameter("action")))
      {
         outStream.write(deleteChannelMap((urlData)));
         return;
      }
      else if ("12".equals(urlData.getParameter("action")))
      {
         outStream.write(xmlEPG(urlData, headers));
         return;
      }
      else if ("14".equals(urlData.getParameter("action")))
      {
         outStream.write(epgAutoAction(urlData));
         return;
      }
      else if ("16".equals(urlData.getParameter("action")))
      {
         outStream.write(showXMLTVsourceSetup(urlData));
         return;
      }
      else if ("17".equals(urlData.getParameter("action")))
      {
         outStream.write(updateXMLTVsourceData(urlData));
         return;
      }
      else if ("22".equals(urlData.getParameter("action")))
      {
         outStream.write(quicksearchEPG(urlData));
         return;
      }
      else if ("24".equals(urlData.getParameter("action")))
      {
         outStream.write(moveChannel(urlData));
         return;
      }
      else if ("25".equals(urlData.getParameter("action")))
      {
         outStream.write(showEpgItem(urlData));
         return;
      }
      else if ("26".equals(urlData.getParameter("action")))
      {
         outStream.write(setIgnoreItem(urlData));
         return;
      }
      else
      {
         outStream.write("Action Not Supported".getBytes());
         return;
      }
   }

   private byte[] setIgnoreItem(HTTPurl urlData) throws Exception
   {
      String id = urlData.getParameter("id");
      String channel = urlData.getParameter("channel");
      String task = urlData.getParameter("task");

      GuideStore guide = GuideStore.getInstance();
      String epgChan = guide.getEpgChannelFromMap(channel);
      GuideItem item = guide.getProgram(epgChan, id);

      if(item != null)
      {
         if("1".equals(task) && !item.getIgnored())
         {
            item.setIgnored(true);
            GuideStore.getInstance().saveEpg();
         }
         else if(item.getIgnored())
         {
            item.setIgnored(false);
            GuideStore.getInstance().saveEpg();
         }
      }

      String urlLocation = "/servlet/EpgDataRes?action=06&id=" + id + "&channel=" + URLEncoder.encode(channel, "UTF-8");
      StringBuffer buff = new StringBuffer();
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: " + urlLocation + "\n\n");
      return buff.toString().getBytes();
   }

   private byte[] showEpgItem(HTTPurl urlData)
   {
      String id = urlData.getParameter("id");

      ScheduleItem item = store.getScheduleItem(id);

      if(item == null)
      {
         StringBuffer buff = new StringBuffer();
         buff.append("HTTP/1.0 302 Moved Temporarily\n");
         buff.append("Location: /\n\n");
         return buff.toString().getBytes();
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(item.getStart());

      String urlLocation = "/servlet/EpgDataRes?action=12" +
         "&year=" + cal.get(Calendar.YEAR) +
         "&month=" + (cal.get(Calendar.MONTH)+1) +
         "&day=" + cal.get(Calendar.DATE) +
         "&scrollto=" + cal.get(Calendar.HOUR_OF_DAY);

      StringBuffer buff = new StringBuffer();
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: " + urlLocation + "\n\n");
      return buff.toString().getBytes();
   }

   private byte[] checkGuideData(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      StringBuffer buff = new StringBuffer();
      String[] epgChannels = guide.getChannelList();


      DecimalFormat oneDec = new DecimalFormat("0.0");
      buff.append("<table width='600'>");
      buff.append("<tr>");
      buff.append("<td class='itemheading'><strong>Channel</strong></td>");
      buff.append("<td class='itemheading' align='center'><strong>Count</strong></td>");
      buff.append("<td class='itemheading' align='center'><strong>Start</strong></td>");
      buff.append("<td class='itemheading' align='center'><strong>Span</strong></td>");
      buff.append("</tr>\n");
      for (String epgChannel : epgChannels) {
         GuideItem[] items = guide.getProgramsForChannel(epgChannel);
         buff.append("<tr><td align='left'>" + epgChannel + "</td><td align='center'>" + items.length + "</td>");

         long span = items[items.length-1].getStart().getTime() - items[0].getStart().getTime();
         double spanDays = (double)span / (double)(1000 * 60 * 60 * 24);

         long start = items[0].getStart().getTime() - new Date().getTime();
         double startDays = (double)start / (double)(1000 * 60 * 60 * 24);

         buff.append("<td align='center'>" + oneDec.format(startDays) + "</td><td align='center'>" + oneDec.format(spanDays) + "</td>");

         buff.append("</tr>\n");
      }
      buff.append("</table>\n");

      buff.append("<br>\n");

      buff.append("<table border='0' cellspacing='0' cellpadding='0' width='600'>");
      buff.append("<tr><td class='itemheading'><strong>Continuity Report</strong></td></tr><tr><td>");
      for (String epgChannel : epgChannels) {
         buff.append("<table>");
         buff.append("<tr><td>" + epgChannel + "</td></tr>");
         buff.append("<tr><td><img src='/servlet/ContinuityImageDataRes?action=1&channel=" + URLEncoder.encode(epgChannel, "UTF-8") + "'></td></tr>\n");
         buff.append("</table>");
      }
      buff.append("</td></tr></table>\n");

      StringBuffer overaps = new StringBuffer();
      SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MMM/yy");

      for (String epgChannel : epgChannels) {
         GuideItem[] items = guide.getProgramsForChannel(epgChannel);

         for (int y = 0; y < items.length - 1; y++)
         {
            GuideItem item1 = items[y];
            GuideItem item2 = items[y + 1];

            if(item2.getStart().getTime() < item1.getStop().getTime())
            {
               overaps.append("<tr>\n");
               overaps.append("<td valign='top'>" + epgChannel + "</td>\n");
               overaps.append("<td>");

               overaps.append("<table>");
               overaps.append("<tr><td>" + item1.getName() + "</td><td>" + dateFormat.format(item1.getStart()) + "</td><td>(" + item1.getDuration() + ")</td></tr>\n");
               overaps.append("<tr><td>" + item2.getName() + "</td><td>" + dateFormat.format(item2.getStart()) + "</td><td>(" + item2.getDuration() + ")</td></tr>\n");
               overaps.append("</table>");

               overaps.append("</td>");

               long diff = item1.getStop().getTime() - item2.getStart().getTime();
               diff = diff / (1000 * 60);
               overaps.append("<td valign='top'>" + diff + "</td>\n");

               overaps.append("</tr>\n");
            }
         }
      }

      if(overaps.length() > 0)
      {
         buff.append("<br><table border='0' cellspacing='5' cellpadding='5' width='600'>");
         buff.append("<tr><td nowrap class='itemheading' colspan='3'><strong>Overlapping Item Report</strong></td></tr>\n");
         buff.append("<tr>\n");
         buff.append("<td nowrap><strong>Channel</strong></td>\n");
         buff.append("<td nowrap><strong>Items</strong></td>\n");
         buff.append("<td nowrap><strong>Duration</strong></td>\n");
         buff.append(overaps);
         buff.append("</tr></table>\n");
      }


      StringBuffer gaps = new StringBuffer();

      for (String epgChannel : epgChannels) {
         GuideItem[] items = guide.getProgramsForChannel(epgChannel);

         for (int y = 0; y < items.length - 1; y++)
         {
            GuideItem item1 = items[y];
            GuideItem item2 = items[y + 1];

            if(item2.getStart().getTime() > item1.getStop().getTime())
            {
               gaps.append("<tr>\n");
               gaps.append("<td valign='top'>" + epgChannel + "</td>\n");
               gaps.append("<td>" + dateFormat.format(item1.getStop()) + "</td>\n");
               gaps.append("<td>" + dateFormat.format(item2.getStart()) + "</td>\n");

               long diff = item2.getStart().getTime() - item1.getStop().getTime();
               diff = diff / (1000 * 60);
               gaps.append("<td valign='top'>" + diff + "</td>\n");

               gaps.append("</tr>\n");
            }
         }
      }

      if(gaps.length() > 0)
      {
         buff.append("<br><table border='0' cellspacing='5' cellpadding='5' width='600'>");
         buff.append("<tr><td nowrap class='itemheading' colspan='4'><strong>Gap Report</strong></td></tr>\n");
         buff.append("<tr>\n");
         buff.append("<td nowrap><strong>Channel</strong></td>\n");
         buff.append("<td nowrap><strong>From</strong></td>\n");
         buff.append("<td nowrap><strong>To</strong></td>\n");
         buff.append("<td nowrap><strong>Duration</strong></td>\n");
         buff.append(gaps);
         buff.append("</tr></table>\n");
      }


      // write out template
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "epg-conflicts.html");
      template.replaceAll("$title" , "EPG Data Report");
      template.replaceAll("$list", buff.toString());

      return template.getPageBytes();
   }

   private byte[] deleteChannelMap(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      int id = -1;
      try
      {
         id = Integer.parseInt(urlData.getParameter("id"));
      }
      catch(Exception e){}

      Vector<String[]> chanMap = guide.getChannelMap();

      if(id > -1 && id < chanMap.size())
      {
         chanMap.remove(id);
         guide.saveChannelMap(null);
      }

      StringBuffer buff = new StringBuffer();
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/EpgDataRes?action=04\n\n");

      return buff.toString().getBytes();
   }

   private byte[] moveChannel(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      int id = -1;
      try
      {
         id = Integer.parseInt(urlData.getParameter("id"));
      }
      catch(Exception e){}

      int dir = 1;
      try
      {
         dir = Integer.parseInt(urlData.getParameter("dir"));
      }
      catch(Exception e){}

      boolean direction = false;
      if(dir == 1) {
		direction = true;
	  }

      if(id != -1) {
		guide.moveChannel(id, direction);
	  }

      StringBuffer buff = new StringBuffer();
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/EpgDataRes?action=04\n\n");

      return buff.toString().getBytes();

   }

   private byte[] updateXMLTVsourceData(HTTPurl urlData) throws Exception
   {
      String out = "";
      String sessionID = urlData.getParameter("sessionID");
      if(!store.checkSessionID(sessionID))
      {
         out = "Security Warning: The Security Session ID you entered is not correct.";
         return out.getBytes();
      }

      String searchURL = urlData.getParameter("searchURL");
      if (searchURL == null) {
		searchURL = "";
	  }
      store.setServerProperty("guide.search.url", searchURL);

      String fileSource = urlData.getParameter("fileDIR");
      if (fileSource == null) {
		fileSource = "";
	  }
      store.setServerProperty("guide.source.file", fileSource);

      String httpURL = urlData.getParameter("httpURL");
      if (httpURL == null) {
		httpURL = "";
	  }
      store.setServerProperty("guide.source.http", httpURL);

      String httpUsr = urlData.getParameter("httpUser");
      if (httpUsr == null) {
		httpUsr = "";
	  }
      store.setServerProperty("guide.source.http.usr", httpUsr);

      String httpPwd = urlData.getParameter("httpPassword");
      if (httpPwd == null) {
		httpPwd = "";
	  }
      store.setServerProperty("guide.source.http.pwd", httpPwd);

      String httpProxyServer = urlData.getParameter("httpProxyServer");
      if (httpProxyServer == null) {
		httpProxyServer = "";
	  }
      store.setServerProperty("proxy.server", httpProxyServer);

      String httpProxyPort = urlData.getParameter("httpProxyPort");
      if (httpProxyPort == null) {
		httpProxyPort = "";
	  }
      store.setServerProperty("proxy.port", httpProxyPort);

      String httpProxyUsr = urlData.getParameter("proxyUser");
      if (httpProxyUsr == null) {
		httpProxyUsr = "";
	  }
      store.setServerProperty("proxy.server.usr", httpProxyUsr);

      String httpProxyPwd = urlData.getParameter("proxyPassword");
      if (httpProxyPwd == null) {
		httpProxyPwd = "";
	  }
      store.setServerProperty("proxy.server.pwd", httpProxyPwd);

      String type = urlData.getParameter("sourceType");
      if (type == null) {
		type = "0";
	  }
      store.setServerProperty("guide.source.type", type);

      String warnOverlapEnabled = urlData.getParameter("warnOverlapEnabled");
      if (warnOverlapEnabled == null) {
		warnOverlapEnabled = "0";
	  }
      store.setServerProperty("guide.warn.overlap", warnOverlapEnabled);

      String clipStartEnabled = urlData.getParameter("clipStartEnabled");
      if (clipStartEnabled == null) {
		clipStartEnabled = "0";
	  }
      store.setServerProperty("guide.clip.start", clipStartEnabled);

      String postActionNameEnabled = urlData.getParameter("postActionNameEnabled");
      if (postActionNameEnabled == null) {
		postActionNameEnabled = "0";
	  }
      store.setServerProperty("guide.action.name", postActionNameEnabled);

      String schOptions = "";
      String schEnabled = urlData.getParameter("scheduleImportEnabled");
      if (schEnabled == null) {
		schEnabled = "0";
	  }
      schOptions += schEnabled.trim() + ":";

      String schHour = urlData.getParameter("scheduleHour");
      if (schHour == null) {
		schHour = "0";
	  }
      schOptions += schHour.trim() + ":";

      String schMin = urlData.getParameter("scheduleMin");
      if (schMin == null) {
		schMin = "0";
	  }
      schOptions += schMin.trim();

      store.setServerProperty("guide.source.schedule", schOptions);

      String preLoadTask = urlData.getParameter("guide.source.schedule.pretask");
      if (preLoadTask == null) {
		preLoadTask = "";
	  }
      store.setServerProperty("guide.source.schedule.pretask", preLoadTask);

      store.refreshWakeupTime();

      StringBuffer buff = new StringBuffer();
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /settings.html\n\n");

      return buff.toString().getBytes();
   }

   private byte[] showXMLTVsourceSetup(HTTPurl urlData) throws Exception
   {

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "epg-sourceSettings.html");

      //
      // is captcha required
      //
      String captcha = store.getProperty("security.captcha");
      if("1".equals(captcha))
      {
         template.replaceAll("$usingCAPTCHA$", "true");
      }
      else
      {
         template.replaceAll("$usingCAPTCHA$", "false");
      }

      template.replaceAll("$searchURL", store.getProperty("guide.search.url"));

      template.replaceAll("$fileDIR", store.getProperty("guide.source.file"));

      template.replaceAll("$httpURL", store.getProperty("guide.source.http"));
      template.replaceAll("$httpUser", store.getProperty("guide.source.http.usr"));
      template.replaceAll("$httpPassword", store.getProperty("guide.source.http.pwd"));

      template.replaceAll("$httpProxyServer", store.getProperty("proxy.server"));
      template.replaceAll("$httpProxyPort", store.getProperty("proxy.port"));
      template.replaceAll("$proxyUser", store.getProperty("proxy.server.usr"));
      template.replaceAll("$proxyPassword", store.getProperty("proxy.server.pwd"));

      String scheduleOptions = store.getProperty("guide.source.schedule");
      String[] schOptsArray = scheduleOptions.split(":");

      // epg clip items before now.
      boolean clipStart = "1".equals(store.getProperty("guide.clip.start"));
      String clipOnStart = "";
      if(clipStart) {
		clipOnStart = "checked";
	  }
      template.replaceAll("$clipStartEnabled", clipOnStart);

      // epg view overlap warning
      boolean warnOverlap = "1".equals(store.getProperty("guide.warn.overlap"));
      String warnOnOverlap = "";
      if(warnOverlap) {
		warnOnOverlap = "checked";
	  }
      template.replaceAll("$warnOverlapEnabled", warnOnOverlap);

      String postActionName = store.getProperty("guide.action.name").trim();
      String postActionEnabled = "";

      if(postActionName.equals("1")) {
		postActionEnabled = "checked";
	  }

      template.replaceAll("$postActionNameEnabled", postActionEnabled);

      int scheduleDownload = 0;
      try
      {
         if (schOptsArray.length > 0) {
			scheduleDownload = Integer.parseInt(schOptsArray[0].trim());
		 }
      }
      catch (Exception e)
      {
      }

      if (scheduleDownload == 0) {
		template.replaceAll("$scheduleImportEnabled", "");
	  } else {
		template.replaceAll("$scheduleImportEnabled", "checked");
	  }

      if (schOptsArray.length > 1) {
		template.replaceAll("$scheduleHour", schOptsArray[1].trim());
	  } else {
		template.replaceAll("$scheduleHour", "");
	  }

      if (schOptsArray.length > 2) {
		template.replaceAll("$scheduleMin", schOptsArray[2].trim());
	  } else {
		template.replaceAll("$scheduleMin", "");
	  }

      template.replaceAll("$preLoadEpgTaskSelect", getTaskSelect("guide.source.schedule.pretask"));

      int type = 0;
      try
      {
         type = Integer.parseInt(store.getProperty("guide.source.type"));
      }
      catch (Exception e)
      {
      }

      if (type == 0)
      {
         template.replaceAll("$FileSelected", "checked");
         template.replaceAll("$HttpSelected", "");
      }
      else if (type == 1)
      {
         template.replaceAll("$FileSelected", "");
         template.replaceAll("$HttpSelected", "checked");
      }

      return template.getPageBytes();
   }

   private String getTaskSelect(String selected)
   {
      String selectedTask = store.getProperty(selected);

      StringBuffer buff = new StringBuffer(1024);
      HashMap<String, TaskCommand> tasks = store.getTaskList();

      String[] keys = tasks.keySet().toArray(new String[0]);

      Arrays.sort(keys);

      buff.append("<select name='" + selected + "'>\n");

      if(selectedTask.length() == 0) {
		buff.append("<option value='' selected>none</option>\n");
	  } else {
		buff.append("<option value='' >none</option>\n");
	  }

      for (String key : keys) {
         if(selectedTask.equalsIgnoreCase(key)) {
			buff.append("<option value='" + key + "' selected>" + key + "</option>\n");
		 } else {
			buff.append("<option value='" + key + "'>" + key + "</option>\n");
		 }
      }

      buff.append("</select>\n");

      return buff.toString();
   }

   private byte[] epgAutoAction(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      StringBuffer buff = new StringBuffer();

      String reload = urlData.getParameter("reload");
      String rescan = urlData.getParameter("rescan");

      boolean loadWorked = true;

      if (reload != null && reload.equals("1"))
      {
         loadWorked = guide.loadXMLTV(buff, 0);
      }

      if(!loadWorked)
      {
         buff.append("EPG Data Load Failed!\n");
      }

      if (rescan != null && rescan.equals("1") && loadWorked)
      {
         System.out.println("ApgAutoAction, waiting for global instance lock : " + new Date());
         ThreadLock.getInstance().getLock();
         try
         {
            System.out.println("EpgAutoAction, got lock : " + new Date());
            store.removeEPGitems(buff, 0);
            guide.addEPGmatches(buff, 0);
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
      }

      store.saveSchedule(null);

      String sendServerStarted = store.getProperty("email.send.epgloaded");
      if("1".equals(sendServerStarted))
      {
         EmailSender sender = new EmailSender();

         sender.setSubject("TV Scheduler Pro EPG Reload Results");
         sender.setBody("EPG Reload Result:\n\n" + buff.toString());

         try
         {
            Thread mailThread = new Thread(Thread.currentThread().getThreadGroup(), sender, sender.getClass().getName());
            mailThread.start();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      String returnData = "";
      returnData += "HTTP/1.0 200 OK\n";
      returnData += "Content-Type: text/plain\n";
      returnData += "Pragma: no-cache\n";
      returnData += "Cache-Control: no-cache\n\n";
      returnData += "EPG Reload/Rescan Results\n";
      returnData += "==========================\n";

      returnData += buff.toString();

      return returnData.getBytes();
   }

   private byte[] xmlEPG(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      Calendar start = Calendar.getInstance();

      if(start.get(Calendar.HOUR_OF_DAY) < 6)
      {
         start.add(Calendar.DATE, -1);
      }

      int month = start.get(Calendar.MONTH) + 1;
      int day = start.get(Calendar.DATE);
      int year = start.get(Calendar.YEAR);

      try
      {
         year = Integer.parseInt(urlData.getParameter("year"));
         month = Integer.parseInt(urlData.getParameter("month"));
         day = Integer.parseInt(urlData.getParameter("day"));
      }
      catch (Exception e)
      {
      }

      int scrollto = -2;
      try
      {
         scrollto = Integer.parseInt(urlData.getParameter("scrollto"));
      }
      catch (Exception e)
      {
      }

      if(scrollto == -1)
      {
         scrollto = start.get(Calendar.HOUR_OF_DAY);
      }

      start.set(Calendar.YEAR, year);
      start.set(Calendar.MONTH, month - 1);
      start.set(Calendar.DATE, day);
      start.set(Calendar.HOUR_OF_DAY, 6);
      start.set(Calendar.MINUTE, 0);
      start.set(Calendar.SECOND, 0);
      start.add(Calendar.SECOND, -1);

      Calendar end = Calendar.getInstance();
      end.setTime(start.getTime());
      end.add(Calendar.HOUR, 24);

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("Test", "tv", null);

      GuideStore epgStore = GuideStore.getInstance();

      Vector<String[]> channelMap = epgStore.getChannelMap();
      Set<String> wsChannels = store.getChannels().keySet();

      Element root = doc.getDocumentElement();

      root.setAttribute("date", Integer.valueOf(start.get(Calendar.DATE)).toString());
      root.setAttribute("month", store.monthNameShort.get(Integer.valueOf(start.get(Calendar.MONTH))));
      root.setAttribute("year", Integer.valueOf(start.get(Calendar.YEAR)).toString());
      root.setAttribute("day", store.dayNameFull.get(Integer.valueOf(start.get(Calendar.DAY_OF_WEEK))));
      root.setAttribute("scrollto", Integer.valueOf(scrollto).toString());

      // add info for now line
      Calendar now = Calendar.getInstance();
      if(now.after(start) && now.before(end))
      {
         Element nowLine = doc.createElement("nowLine");
         nowLine.setAttribute("hour", Integer.valueOf(now.get(Calendar.HOUR_OF_DAY)).toString());
         nowLine.setAttribute("minute", Integer.valueOf(now.get(Calendar.MINUTE)).toString());
         root.appendChild(nowLine);
      }

      String link = "/servlet/" + urlData.getServletClass() + "?action=12&";
      Vector<String[]> links = epgStore.getEPGlinks(start);

      for (String[] data : links) {
         Element dayEl = doc.createElement("days");
         dayEl.setAttribute("name", data[1]);

         Element dayUrl = doc.createElement("url");
         Text dayUrlTest = doc.createTextNode(link + data[0]);
         dayUrl.appendChild(dayUrlTest);

         dayEl.appendChild(dayUrl);

         root.appendChild(dayEl);
      }

      //
      // Output the channels
      //
      for (String[] map : channelMap) {
         Channel wsChannel = store.getChannels().get(map[0]);

         Element channel = doc.createElement("channel");
         channel.setAttribute("id", map[0]);

         if(wsChannel != null)
         {
            channel.setAttribute("mux_id", wsChannel.getFrequency() + "-" + wsChannel.getBandWidth());
         }
         else
         {
            channel.setAttribute("mux_id", "none");
         }

         Element disName = doc.createElement("display-name");
         Text chaName = doc.createTextNode(map[0]);
         disName.appendChild(chaName);

         channel.appendChild(disName);
         root.appendChild(channel);
      }

      SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMddHHmmss Z");
      StringBuffer dateBuff = new StringBuffer();

      Calendar startTime = Calendar.getInstance();
      startTime.set(Calendar.MILLISECOND, 0);

      //
      // Output the programs
      //
      boolean overlapDetected = false;

      for (String[] map : channelMap) {
         GuideItem prevItem = null;
         String channelName = map[0];

         if (!wsChannels.contains(map[0]))
         {
            channelName = "Not Mapped";
         }
         else
         {
            GuideItem[] programs = epgStore.getPrograms(start.getTime(), end.getTime(), map[1]);

            for (int y = 0; y < programs.length; y++)
            {
               GuideItem item = programs[y];

               if(prevItem != null)
               {
                  if(item.getStart().before(prevItem.getStop()))
                  {
                     overlapDetected = true;
                  }
               }
               prevItem = item;

               ///////////////////////////////////////////////////////////////////////////////////////////////////////////
               // add start padding if needed
               //
               start.add(Calendar.SECOND, 1);
               startTime.setTime(item.getStart());
               long pastStart = startTime.getTime().getTime() - start.getTime().getTime();
               if (y == 0 && pastStart > 0)
               {
                  Element program_PH = doc.createElement("programme");

                  dateBuff = new StringBuffer();
                  dateFormater.format(start.getTime(), dateBuff, new FieldPosition(0));
                  program_PH.setAttribute("start", dateBuff.toString());

                  dateBuff = new StringBuffer();
                  dateFormater.format(item.getStart(), dateBuff, new FieldPosition(0));
                  program_PH.setAttribute("stop", dateBuff.toString());

                  program_PH.setAttribute("channel", channelName);

                  Element titleElement = doc.createElement("title");
                  Text titleText = doc.createTextNode("EMPTY");
                  titleElement.appendChild(titleText);
                  program_PH.appendChild(titleElement);

                  Element subTitleElement = doc.createElement("sub-title");
                  Text subTitleText = doc.createTextNode("empty");
                  subTitleElement.appendChild(subTitleText);
                  program_PH.appendChild(subTitleElement);

                  Element catElement = doc.createElement("category");
                  Text catText = doc.createTextNode("epgProgramEmpty");
                  catElement.appendChild(catText);
                  program_PH.appendChild(catElement);

                  Element descElement = doc.createElement("desc");
                  Text descText = doc.createTextNode("empty");
                  descElement.appendChild(descText);
                  program_PH.appendChild(descElement);

                  Element lengthElement = doc.createElement("length");
                  lengthElement.setAttribute("units", "minutes");
                  Text lengthText = doc.createTextNode(Long.valueOf(pastStart / (1000 * 60) + 1).toString());
                  lengthElement.appendChild(lengthText);
                  program_PH.appendChild(lengthElement);

                  root.appendChild(program_PH);
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

                     dateBuff = new StringBuffer();
                     dateFormater.format(programs[y - 1].getStop(), dateBuff, new FieldPosition(0));
                     program_PH.setAttribute("start", dateBuff.toString());

                     dateBuff = new StringBuffer();
                     dateFormater.format(item.getStart(), dateBuff, new FieldPosition(0));
                     program_PH.setAttribute("stop", dateBuff.toString());

                     program_PH.setAttribute("channel", channelName);

                     Element titleElement = doc.createElement("title");
                     Text titleText = doc.createTextNode("EMPTY");
                     titleElement.appendChild(titleText);
                     program_PH.appendChild(titleElement);

                     Element subTitleElement = doc.createElement("sub-title");
                     Text subTitleText = doc.createTextNode("empty");
                     subTitleElement.appendChild(subTitleText);
                     program_PH.appendChild(subTitleElement);

                     Element catElement = doc.createElement("category");
                     Text catText = doc.createTextNode("epgProgramEmpty");
                     catElement.appendChild(catText);
                     program_PH.appendChild(catElement);

                     Element descElement = doc.createElement("desc");
                     Text descText = doc.createTextNode("empty");
                     descElement.appendChild(descText);
                     program_PH.appendChild(descElement);

                     Element lengthElement = doc.createElement("length");
                     lengthElement.setAttribute("units", "minutes");
                     Text lengthText = doc.createTextNode(Long.valueOf(skip / (1000 * 60)).toString());
                     lengthElement.appendChild(lengthText);
                     program_PH.appendChild(lengthElement);

                     root.appendChild(program_PH);
                  }
               }
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////

               Element program = doc.createElement("programme");

               dateBuff = new StringBuffer();
               dateFormater.format(item.getStart(), dateBuff, new FieldPosition(0));
               program.setAttribute("start", dateBuff.toString());

               dateBuff = new StringBuffer();
               dateFormater.format(item.getStop(), dateBuff, new FieldPosition(0));
               program.setAttribute("stop", dateBuff.toString());

               program.setAttribute("channel", channelName);

               Element titleElement = doc.createElement("title");
               Text titleText = doc.createTextNode(item.getName());
               titleElement.appendChild(titleText);
               program.appendChild(titleElement);

               Element subTitleElement = doc.createElement("sub-title");
               Text subTitleText = doc.createTextNode(item.getSubName());
               subTitleElement.appendChild(subTitleText);
               program.appendChild(subTitleElement);

               for (String itemCat : item.getCategory()) {
                  Element catElement = doc.createElement("category");
                  Text catText = doc.createTextNode(itemCat);
                  catElement.appendChild(catText);
                  program.appendChild(catElement);
               }

               Element descElement = doc.createElement("desc");
               Text descText = doc.createTextNode(item.getDescription());
               descElement.appendChild(descText);
               program.appendChild(descElement);

               Element lengthElement = doc.createElement("length");
               lengthElement.setAttribute("units", "minutes");
               Text lengthText = doc.createTextNode(Integer.valueOf(item.getDuration()).toString());
               lengthElement.appendChild(lengthText);
               program.appendChild(lengthElement);

               Element ignoreElement = doc.createElement("ignored");
               Text ignoreText = null;
               if(item.getIgnored()) {
				ignoreText = doc.createTextNode("1");
			   } else {
				ignoreText = doc.createTextNode("0");
			   }
               ignoreElement.appendChild(ignoreText);
               program.appendChild(ignoreElement);

               String detailsUrl = "/servlet/EpgDataRes?action=06&id=" + item.toString() + "&channel=" + URLEncoder.encode(map[0], "UTF-8");
               Element infoUrlElement = doc.createElement("detailsUrl");
               Text infoUrlText = doc.createTextNode(detailsUrl);
               infoUrlElement.appendChild(infoUrlText);
               program.appendChild(infoUrlElement);

               root.appendChild(program);
            }
         }
      }

      boolean overlapWarning = "1".equals(store.getProperty("guide.warn.overlap"));

      if(overlapWarning) {
		root.setAttribute("overlapDetected", Boolean.valueOf(overlapDetected).toString());
	  } else {
		root.setAttribute("overlapDetected", "false");
	  }


      //
      // Add Schedules
      //
      Text textNode = null;
      Element elementNode = null;

      for (String[] map : channelMap) {
         String channelName = map[0];

         if (channelName != null)
         {
            Vector<ScheduleItem> items = new Vector<>();
            store.getSchedulesWhenInc(start.getTime(), end.getTime(), channelName, items);

            for (ScheduleItem item : items) {
               Element schedule = doc.createElement("schedule");

               dateBuff = new StringBuffer();
               dateFormater.format(item.getStart(), dateBuff, new FieldPosition(0));
               schedule.setAttribute("start", dateBuff.toString());

               dateBuff = new StringBuffer();
               dateFormater.format(item.getStop(), dateBuff, new FieldPosition(0));
               schedule.setAttribute("stop", dateBuff.toString());

               schedule.setAttribute("duration", Integer.valueOf(item.getDuration()).toString());

               schedule.setAttribute("channel", channelName);

               startTime.setTime(item.getStart());
               long pastStart = startTime.getTime().getTime() - start.getTime().getTime();

               elementNode = doc.createElement("id");
               textNode = doc.createTextNode(item.toString());
               elementNode.appendChild(textNode);
               schedule.appendChild(elementNode);

               Element topElement = doc.createElement("from_top");
               Text topText = doc.createTextNode(Long.valueOf(pastStart / (1000 * 60)).toString());
               topElement.appendChild(topText);
               schedule.appendChild(topElement);

               Element stateElement = doc.createElement("itemState");
               Text stateText = doc.createTextNode(Integer.valueOf(item.getState()).toString());
               stateElement.appendChild(stateText);
               schedule.appendChild(stateElement);

               if (item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.ERROR)
               {
                  String delUrl = "/servlet/ScheduleDataRes?action=04&id=" + item.toString();
                  addActionUrl(delUrl, item, "D", schedule, doc);

                  if(item.getType() != ScheduleItem.EPG)
                  {
                     String editUrl = "/servlet/ScheduleDataRes?action=01&id=" + item.toString() + "&month=" + startTime.get(Calendar.MONTH) + "&year=" + startTime.get(Calendar.YEAR);
                     addActionUrl(editUrl, item, "E", schedule, doc);

                     String addTimeUrl = "/servlet/ScheduleDataRes?action=05&id=" + item.toString();
                     addActionUrl(addTimeUrl, item, "+", schedule, doc);
                  }
               }
               else if (item.getState() == ScheduleItem.RUNNING)
               {
                  String stopUrl = "/servlet/ScheduleDataRes?action=09&id=" + item.toString();
                  addActionUrl(stopUrl, item, "S", schedule, doc);

                  String addTimeUrl = "/servlet/ScheduleDataRes?action=05&id=" + item.toString();
                  addActionUrl(addTimeUrl, item, "+", schedule, doc);
               }

               root.appendChild(schedule);
            }
         }

      }

      //
      // Do transform and return data
      //
      String currentEPGTheme = store.getProperty("path.theme.epg");
      XSL transformer = new XSL(doc, currentEPGTheme, urlData, headers);

      // set current page to check point for back
      // Remove the &scrollto=-1 first
      String request = urlData.getReqString();
      request = request.replace("&scrollto=-1", "");
      transformer.addCookie("backURL", request);

      return transformer.doTransform(false);
   }

   private void addActionUrl(String url, ScheduleItem item, String name, Element root, Document doc)
   {
      Text textNode = null;
      Element elementNode = null;
      Element elementNode02 = null;

      elementNode = doc.createElement("actionUrl");
      elementNode.setAttribute("name", name);
      elementNode.setAttribute("item_id", item.toString());
      textNode = doc.createTextNode(url);
      elementNode02 = doc.createElement("url");
      elementNode02.appendChild(textNode);
      elementNode.appendChild(elementNode02);
      root.appendChild(elementNode);
   }

   private String programAddURL(GuideItem item, Calendar startTime, String chanName) throws Exception
   {
      StringBuffer buff = new StringBuffer();
      buff.append("/servlet/ScheduleDataRes?action=12");
      buff.append("&channel=" + URLEncoder.encode(chanName, "UTF-8") + "&id=" + item.toString());

      return buff.toString();
   }

   private byte[] quicksearchEPG(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      String doSearch = urlData.getParameter("search");

      StringBuffer buff = new StringBuffer();

      String kwMatch = urlData.getParameter("kwMatch");
      if (kwMatch == null) {
		kwMatch = "";
	  }

      kwMatch = kwMatch.trim();

      String catMatch = urlData.getParameter("catMatch");
      if (catMatch == null || catMatch.length() < 2) {
		catMatch = "any";
	  }

      String chanMatch = urlData.getParameter("chanMatch");
      if (chanMatch == null || chanMatch.length() == 0) {
		chanMatch = "any";
	  }

      int typeMatch = 0;
      try
      {
         typeMatch = Integer.parseInt(urlData.getParameter("typeMatch"));
      }
      catch(Exception e){}

      // get time span date
      int startHH = 0;
      try
      {
         startHH = Integer.parseInt(urlData.getParameter("startHH"));
      }
      catch(Exception e){}
      int startMM = 0;
      try
      {
         startMM = Integer.parseInt(urlData.getParameter("startMM"));
      }
      catch(Exception e){}
      int endHH = 23;
      try
      {
         endHH = Integer.parseInt(urlData.getParameter("endHH"));
      }
      catch(Exception e){}
      int endMM = 59;
      try
      {
         endMM = Integer.parseInt(urlData.getParameter("endMM"));
      }
      catch(Exception e){}

      int ignored = 2;
      try
      {
         ignored = Integer.parseInt(urlData.getParameter("ignored"));
      }
      catch(Exception e){}

      HashMap<String, Vector<GuideItem>> results = new HashMap<>();
      int num = 0;

      if (doSearch != null && doSearch.equalsIgnoreCase("yes"))
      {
         int[] times = new int[4];
         times[0] = startHH;
         times[1] = startMM;
         times[2] = endHH;
         times[3] = endMM;

         num = guide.simpleEpgSearch(kwMatch, typeMatch, catMatch, chanMatch, ignored, times, results);

         buff.append("Your quick search for (" + HTMLEncoder.encode(kwMatch) + ") returned " + num + " results.<p>\n");

         if (results.size() > 0)
         {
            buff.append("<span class='areaTitle'>Search Results:</span><br><br>\n");
            buff.append("<table class='epgSearchResults'>\n");
            buff.append("<tr>");
            buff.append("<td>Program Name</td>");
            buff.append("<td>Time</td>");
            buff.append("<td>Duration</td>");
            buff.append("<td>Action</td>");
            buff.append("</tr>");

            Calendar startTime = Calendar.getInstance();

            String[] keys = results.keySet().toArray(new String[0]);

            for (String key : keys) {
               Vector<GuideItem> result = results.get(key);

               if (result.size() > 0)
               {
                  buff.append("<tr><td colspan='4'>\n");
                  buff.append("<span class='areaTitle'>Channel : " + key + "</span><br>\n");
                  buff.append("</td></tr\n");

                  for (GuideItem item : result) {
                     startTime.setTime(item.getStart());

                     int hour = startTime.get(Calendar.HOUR);
                     if(hour == 0) {
						hour = 12;
					 }
                     String startString = intToStr(hour) + ":" + intToStr(startTime.get(Calendar.MINUTE)) + " " + store.ampm.get(Integer.valueOf(startTime.get(Calendar.AM_PM)));
                     String dateString = store.dayName.get(Integer.valueOf(startTime.get(Calendar.DAY_OF_WEEK))) + ", " + startTime.get(Calendar.DATE) + " " + store.monthNameShort.get(Integer.valueOf(startTime.get(Calendar.MONTH)));

                     buff.append("<tr>\n");
                     buff.append("<td class='epgSearchResults'>" + item.getName());
                     if(item.getSubName() != null && item.getSubName().length() > 0) {
						buff.append(" (" + item.getSubName() + ")");
					 }
                     buff.append("</td>");
                     buff.append("<td class='epgSearchResults'>" + dateString + " at " + startString + "</td>");
                     buff.append("<td class='epgSearchResults'>" + item.getDuration() + "</td>");

                     buff.append("<td class='epgSearchResults'>");

                     String infoUrl = "/servlet/EpgDataRes?action=06&id=" + item.toString() + "&channel=" + URLEncoder.encode(key, "UTF-8");
                     buff.append("<span class='programName' onClick=\"openDetails('" + infoUrl + "')\">");
                     buff.append("Details");
                     buff.append("</span> | ");

                     if(startTime.get(Calendar.HOUR_OF_DAY) < 6) {
						startTime.add(Calendar.DATE, -1);
					 }

                     String epgUrl = "/servlet/EpgDataRes?action=12" +
                        "&year=" + startTime.get(Calendar.YEAR) +
                        "&month=" + (startTime.get(Calendar.MONTH)+1) +
                        "&day=" + startTime.get(Calendar.DATE) +
                        "&scrollto=" + startTime.get(Calendar.HOUR_OF_DAY);
                     buff.append("<a class='infoNav' href='" + epgUrl + "'>EPG</a>");

                     buff.append("</td>");

                     buff.append("</tr>\n");
                  }
               }
            }

            buff.append("</table><p>\n");
         }
      }

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "epg-qsearch.html");

      template.replaceAll("$result", buff.toString());

      // do keyword
      if (kwMatch.length() > 0) {
		template.replaceAll("$kwMatch", HTMLEncoder.encode(kwMatch));
	  } else {
		template.replaceAll("$kwMatch", "");
	  }

      // do cats
      String[] cats = guide.getCategoryStrings();
      Arrays.sort(cats, String.CASE_INSENSITIVE_ORDER);
      StringBuffer bufc = new StringBuffer();

      if (cats.length > 0)
      {
         bufc.append("<option value='any'");
         if(catMatch.equalsIgnoreCase("any")) {
			bufc.append(" Selected");
		 }
         bufc.append(">Any</option>\n");

         for (String cat : cats) {
            bufc.append("<option value='" + cat + "'");
            if (catMatch.equalsIgnoreCase(cat))
            {
               bufc.append(" Selected");
            }
            bufc.append(">" + cat + "</option>\n");
         }
      } else {
		bufc.append("<option value='any'>N/A</option>\n");
	  }

      template.replaceAll("$catList", bufc.toString());

      // do channels
      StringBuffer chanBuff = new StringBuffer();
      String[] channels = store.getChannels().keySet().toArray(new String[0]);
      Arrays.sort(channels);

      chanBuff.append("<option value='any'");
      if(chanMatch.equalsIgnoreCase("any")) {
		chanBuff.append(" Selected");
	  }
      chanBuff.append(">Any</option>\n");

      for (String channel : channels) {
         chanBuff.append("<option value='" + channel + "'");
         if (chanMatch.equalsIgnoreCase(channel)) {
			chanBuff.append(" Selected");
		 }
         chanBuff.append(">" + channel + "</option>\n");
      }

      template.replaceAll("$chanList", chanBuff.toString());

      // do types
      StringBuffer typeBuff = new StringBuffer();

      typeBuff.append("<option value='0'");
      if(typeMatch == 0) {
		typeBuff.append(" Selected");
	  }
      // Calvi changes.
      typeBuff.append(">Title, Subtitle or Description</option>\n");

      typeBuff.append("<option value='1'");
      if(typeMatch == 1) {
		typeBuff.append(" Selected");
	  }
      typeBuff.append(">Title</option>\n");

      typeBuff.append("<option value='2'");
      if(typeMatch == 2) {
		typeBuff.append(" Selected");
	  }
      // Calvi changes.
      typeBuff.append(">Subtitle</option>\n");

      typeBuff.append("<option value='3'");
      if(typeMatch == 3) {
		typeBuff.append(" Selected");
	  }
      typeBuff.append(">Description</option>\n");

      typeBuff.append("<option value='4'");
      if(typeMatch == 4) {
		typeBuff.append(" Selected");
	  }
      typeBuff.append(">Actor</option>\n");

      template.replaceAll("$typeList", typeBuff.toString());


      // do ignored
      StringBuffer ignoredBuff = new StringBuffer();

      ignoredBuff.append("<option value='0'");
      if(ignored == 0) {
		ignoredBuff.append(" Selected");
	  }
      ignoredBuff.append(">No</option>\n");

      ignoredBuff.append("<option value='1'");
      if(ignored == 1) {
		ignoredBuff.append(" Selected");
	  }
      ignoredBuff.append(">Yes</option>\n");

      ignoredBuff.append("<option value='2'");
      if(ignored == 2) {
		ignoredBuff.append(" Selected");
	  }
      // Calvi changes.
      ignoredBuff.append(">Any</option>\n");

      template.replaceAll("$ignoredList", ignoredBuff.toString());

      // set time values
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(0);
      nf.setMaximumIntegerDigits(2);
      nf.setMinimumIntegerDigits(2);
      template.replaceAll("$startHH", nf.format(startHH));
      template.replaceAll("$startMM", nf.format(startMM));
      template.replaceAll("$endHH", nf.format(endHH));
      template.replaceAll("$endMM", nf.format(endMM));

      template.addCookie("backURL", urlData.getReqString());

      return template.getPageBytes();
   }

   private byte[] showProgramDetails(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      String channel = urlData.getParameter("channel");
      String id = urlData.getParameter("id");

      String epgChan = guide.getEpgChannelFromMap(channel);

      GuideItem item = guide.getProgram(epgChan, id);

      StringBuffer buff = new StringBuffer(256);

      if (item == null)
      {
         buff.append("No info available for the requested Item.");
         return buff.toString().getBytes();
      }

      buff.append("<table align='center' border='0' cellpadding='4' cellspacing='2' width='90%'>");

      buff.append("<tr><td align='right' valign='top' colspan='2'>");

      Calendar startTime = Calendar.getInstance();
      startTime.setTime(item.getStart());
      buff.append("<a href='#' onClick=\"addItem('" + programAddURL(item, startTime, channel) + "');\">");
      buff.append("<img align='absmiddle' src='/images/add.png' border='0' title='Add Schedule' alt='Add Schedule' width='24' height='24'>");
      buff.append("</a>\n");

      String searchURL = "/servlet/EpgAutoAddDataRes?action=14" +
            "&itemID=" + URLEncoder.encode(item.toString(), "UTF-8") +
            "&chan=" + URLEncoder.encode(channel, "UTF-8");

      buff.append(" | <a href='#' onClick=\"doSearch('" + searchURL + "');\">");
      buff.append("<img align='absmiddle' src='/images/reload.png' border='0' title='Create Auto-Add Item' alt='Create Auto-Add Item' width='24' height='24'>");
      buff.append("</a>\n");

      // add ignore icon
      String ignoreURL = "/servlet/EpgDataRes?action=26" +
      "&id=" + URLEncoder.encode(item.toString(), "UTF-8") +
      "&channel=" + URLEncoder.encode(channel, "UTF-8");

      String imageName = "";
      String ignoreTitle = "";
      if(item.getIgnored())
      {
         ignoreURL += "&task=0";
         imageName = "addsch.png";
         ignoreTitle = "Remove ignore flag";
      }
      else
      {
         ignoreURL += "&task=1";
         imageName = "stopItem.png";
         ignoreTitle = "Ignore this program in Auto-Add scans";
      }
      buff.append(" | <a href=\"" + ignoreURL + "\">");
      buff.append("<img align='absmiddle' src='/images/" + imageName  +"' border='0' title='" + ignoreTitle + "' alt='' width='24' height='24'>");
      buff.append("</a>\n");

      String matchesURL = "/servlet/EpgAutoAddDataRes?action=37" +
         "&itemID=" + URLEncoder.encode(item.toString(), "UTF-8") +
         "&chan=" + URLEncoder.encode(channel, "UTF-8");

      buff.append(" | <a href='#' onClick=\"showMatchListMatches('" + matchesURL + "');\">");
      buff.append("<img align='absmiddle' src='/images/log.png' border='0' title='Show match list item matches' alt='' width='24' height='24'>");
      buff.append("</a>\n");

      buff.append("</td></tr>\n");

      buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Name:</b></td><td width='100%'>" + item.getName());

      if (item.getSubName().length() > 0) {
		buff.append(" (" + item.getSubName() + ")");
	  }

      buff.append("</td></tr>");

      SimpleDateFormat df = new SimpleDateFormat("h:mm aa");
      buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Time:</b></td><td>" +
         df.format(item.getStart()) + "</td></tr>\n");

      df = new SimpleDateFormat("EEE MMM d");
      buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Date:</b></td><td>" +
         df.format(item.getStart()) + "</td></tr>\n");

      buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Duration:</b></td><td>" + item.getDuration() + " min</td></tr>\n");

      buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Channel:</b></td><td>" + channel + "</td></tr>\n");

      buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Ignored:</b></td><td>");
      if(!item.getIgnored())
      {
         buff.append("No");
      }
      else
      {
         buff.append("<span style='color: #f9fa00; font-weight: bold;'>Yes (ignored in Auto-Add scans)</span>");
      }
      buff.append("</td></tr>\n");

      // add source URL link
      if (item.getURL().length() > 0)
      {
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Source:</b></td><td><font size=2><b>\n");
         buff.append("<a href='" + item.getURL() + "' target='_sourceData'>More Info</a>");
         buff.append("</b></font></td></tr>");
      }

      // add category
      if (item.getCategory().size() > 0)
      {
    	 String allCats = "";
         for(int x = 0; x < item.getCategory().size(); x++)
         {
            allCats += item.getCategory().get(x);
            if(x < item.getCategory().size() - 1) {
				allCats += ", ";
			}
         }
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Category:</b></td><td>" + allCats + "</td></tr>\n");
      }

      // add actors
      if (item.getActors().size() > 0)
      {
         String allActs = "";
         for(int x = 0; x < item.getActors().size(); x++)
         {
            allActs += item.getActors().get(x);
            if(x < item.getActors().size() - 1) {
				allActs += ", ";
			}
         }
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Actors:</b></td><td>" + allActs + "</td></tr>\n");
      }

      // add directors
      if (item.getDirectors().size() > 0)
      {
         String allDirect = "";
         for(int x = 0; x < item.getDirectors().size(); x++)
         {
            allDirect += item.getDirectors().get(x);
            if(x < item.getDirectors().size() - 1) {
				allDirect += ", ";
			}
         }
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Directors:</b></td><td>" + allDirect + "</td></tr>\n");
      }

      if(item.getRatings().length() > 0)
      {
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Rating:</b></td><td><font size=2><b>\n");
         buff.append(item.getRatings());
         buff.append("</b></font></td></tr>");
      }

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
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Flags:</b></td><td><font size=2><b>\n");
         buff.append(sFlags);
         buff.append("</b></font></td></tr>");
      }

      String sInfo = "";

      String externalSearchURL = store.getProperty("guide.search.url");

      externalSearchURL = externalSearchURL.replaceAll("\\$TITLE", URLEncoder.encode(item.getName(), "UTF-8"));
      externalSearchURL = externalSearchURL.replaceAll("\\$SUB", URLEncoder.encode(item.getSubName(), "UTF-8"));

      String catForSearch = "";
      for(int x = 0; x < item.getCategory().size(); x++)
      {
    	  catForSearch += item.getCategory().get(x);
    	  if(x < item.getCategory().size() - 1) {
			catForSearch += " ";
		  }
      }
      externalSearchURL = externalSearchURL.replaceAll("\\$CAT", URLEncoder.encode(catForSearch, "UTF-8"));

      sInfo = "<a target='_epg_search' href='" + externalSearchURL + "'>SEARCH</a>&nbsp;\n";

      if (sInfo.length() > 0)
      {
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='right' valign='top'><b>Info:</b></td><td><font size=2><b>\n");
         buff.append(sInfo);
         buff.append("</b></font></td></tr>");
      }

      // add description
      if(item.getDescription() != null && item.getDescription().length() > 0)
      {
         buff.append("<tr><td style='border: 1px solid #FFFFFF;' align='left' valign='top' colspan='2'>" + item.getDescription() + "<br></td></tr>\n");
      }

      buff.append("</table>\n");

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "epg-details.html");
      template.replaceAll("$details", buff.toString());

      return template.getPageBytes();
   }

   private byte[] addChannelMapping(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      String wsChannel = urlData.getParameter("wsChannel");
      String epgChannel = urlData.getParameter("epgChannel");

      // make sure WS channel exists
      Set<String> wsChanSet = store.getChannels().keySet();
      String[] wsChannels = wsChanSet.toArray(new String[0]);
      boolean foundWSChannel = false;
      for (String wsChannel2 : wsChannels) {
         if(wsChannel2.equals(wsChannel))
         {
            foundWSChannel = true;
            break;
         }
      }

      // make sure EPG channel exists
      String[] epgChannels = guide.getChannelList();
      boolean foundEPGChannel = false;
      for (String epgChannel2 : epgChannels) {
         if(epgChannel2.equals(epgChannel))
         {
            foundEPGChannel = true;
            break;
         }
      }

      if(foundWSChannel && foundEPGChannel)
      {
         guide.addChannelToMap(wsChannel, epgChannel);
         guide.saveChannelMap(null);
      }

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/" + urlData.getServletClass() + "?action=04\n\n");

      return buff.toString().getBytes();
   }

   private byte[] showChannelMapping(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      StringBuffer buff = new StringBuffer();
      StringBuffer warnings = new StringBuffer();

      Vector<String[]> chanMap = guide.getChannelMap();
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "epg-mapping.html");

      Set<String> wsChanSet = store.getChannels().keySet();
      String[] wsChannels = wsChanSet.toArray(new String[0]);

      for (int x = 0; x < chanMap.size(); x++)
      {
         int problem = 0;
         String[] map = chanMap.get(x);
         GuideItem[] items = guide.getProgramsForChannel(map[1]);

         if(!wsChanSet.contains(map[0]))
         {
            warnings.append("TV Scheduler Pro Channel (" + map[0] + ") does not exist!<br>\n");
            problem = 1;
         }

         if(items.length == 0)
         {
            warnings.append("There is currently no data for EPG channel (" + map[1] + ")<br>\n");
            problem += 2;
         }

         buff.append("<tr>");

         buff.append("<td>");
         if(problem == 1 || problem == 3)
         {
            buff.append("<img align='absmiddle' src='/images/exclaim.png' border='0' alt='Error' width='22' height='24'>");
         }
         buff.append(map[0] + "</td><td>");
         if(problem == 2 || problem == 3)
         {
            buff.append("<img align='absmiddle' src='/images/exclaim.png' border='0' alt='Error' width='22' height='24'>");
         }
         buff.append(map[1] + "</td>\n");

         buff.append("<td>");
         buff.append("<a href='/servlet/EpgDataRes?action=07&id=" + x + "'><img align='absmiddle' border='0' alt='Delete' src='/images/delete.png' width='24' height='24'></a>\n");
         buff.append("<a href='/servlet/EpgDataRes?action=24&id=" + x + "&dir=0'><img align='absmiddle' border='0' alt='Up' src='/images/up01.png' width='7' height='7'></a>\n");
         buff.append("<a href='/servlet/EpgDataRes?action=24&id=" + x + "&dir=1'><img align='absmiddle' border='0' alt='Down' src='/images/down01.png' width='7' height='7'></a>\n");
         buff.append("</td>\n");

         buff.append("</tr>\n");
      }
      template.replaceAll("$channelmap", buff.toString());

      Arrays.sort(wsChannels, String.CASE_INSENSITIVE_ORDER);
      buff = new StringBuffer();
      for (String wsChannel : wsChannels) {
         buff.append("<OPTION VALUE=\"" + wsChannel + "\"> " + wsChannel + "\n");
      }
      template.replaceAll("$wsChannels", buff.toString());

      String[] epgChannels = guide.getChannelList();
      Arrays.sort(epgChannels, String.CASE_INSENSITIVE_ORDER);
      buff = new StringBuffer();
      for (String epgChannel : epgChannels) {
         buff.append("<OPTION VALUE=\"" + epgChannel + "\"> " + epgChannel + "\n");
      }
      template.replaceAll("$epgChannels", buff.toString());

      //
      // Now show any warnings
      //
      if(warnings.length() > 0)
      {
         String warningText = "<table><tr><td style='border: 1px solid #FFFFFF;'>" +
            "<img align='absmiddle' src='/images/exclaim.png' border='0' alt='Error' width='22' height='24'><b>Warnings</b>" +
            "</td></tr><tr><td>";
         warningText += warnings.toString() + "</td></tr></table>";

         template.replaceAll("$warning", warningText);
      } else {
		template.replaceAll("$warning", "");
	  }

      return template.getPageBytes();
   }

   private void reloadXMLTVdata(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "epg-reload.html");
      outStream.write(template.getPageBytes());

      StringBuffer buff = new StringBuffer();
      boolean loadWorked = guide.loadXMLTV(buff, 1);

      if(!loadWorked)
      {
         buff.append("EPG Data Load Failed!<br>\n");
      }

      outStream.write(buff.toString().getBytes());

      outStream.write("</td></tr></table><br><br></body></html>".getBytes());
   }

   private String intToStr(int num)
   {
      if (num < 10) {
		return "0" + num;
	  } else {
		return Integer.valueOf(num).toString();
	  }

   }

}


