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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ScheduleDataRes extends HTTPResponse
{
   private DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

   public ScheduleDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      if("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showCalendar(urlData));
         return;
      }
      else if("02".equals(urlData.getParameter("action")))
      {
         outStream.write(showAddForm(urlData));
         return;
      }
      else if("03".equals(urlData.getParameter("action")))
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
      else if("04".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(deleteSchedule(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if("05".equals(urlData.getParameter("action")))
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
      else if("06".equals(urlData.getParameter("action")))
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
      else if("07".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(showItemInfo(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if("09".equals(urlData.getParameter("action")))
      {
         outStream.write(stopRunningTask(urlData));
         return;
      }
      else if("10".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(addTestSchedules(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if("12".equals(urlData.getParameter("action")))
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
      else if("13".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(deleteSchedules(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }

         return;
      }
      else if("14".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            outStream.write(updateDeleteAfter(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }

         return;
      }
      else if("15".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            showInfoDownloadPage(urlData, outStream);
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
      else if("16".equals(urlData.getParameter("action")))
      {
         ThreadLock.getInstance().getLock();
         try
         {
            showInfoDownloadPageArchive(urlData, outStream);
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
            outStream.write(getScheduleTable(urlData));
         }
         finally
         {
            ThreadLock.getInstance().releaseLock();
         }
         return;
      }
   }

   private void showInfoDownloadPage(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      String id = urlData.getParameter("id");

      ScheduleItem si = store.getScheduleItem(id);

      if(si == null)
      {
         String redirect = "HTTP/1.0 302 Moved Temporarily\n";
         redirect += "Location: /servlet/ScheduleDataRes\n\n";
         outStream.write(redirect.getBytes());
         return;
      }

      buildInfoZip(si, outStream);

      return;
   }

   private void showInfoDownloadPageArchive(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      ScheduleItem item = null;

      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         File archivePath = new File(dataPath + File.separator + "archive" + File.separator + urlData.getParameter("file"));
         FileInputStream in = new FileInputStream(archivePath);
         ObjectInputStream oin = new ObjectInputStream(in);
         item = (ScheduleItem)oin.readObject();
         oin.close();
         in.close();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }

      if(item == null)
      {
         String redirect = "HTTP/1.0 302 Moved Temporarily\n";
         redirect += "Location: /servlet/ScheduleDataRes\n\n";
         outStream.write(redirect.getBytes());
         return;
      }

      buildInfoZip(item, outStream);

      return;
   }

   private void buildInfoZip(ScheduleItem si, OutputStream outStream) throws Exception
   {

      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(bytesOut);

      Vector<String> logFiles = si.getLogFileNames();

      for (String logFile : logFiles) {
         File log = new File(logFile);

         if(log.exists())
         {
            out.putNextEntry(new ZipEntry(log.getName()));
            byte[] data = new byte[1024];
            FileInputStream is = new FileInputStream(log);
            int read = is.read(data);
            while(read > -1)
            {
               out.write(data, 0, read);
               read = is.read(data);
            }
            out.closeEntry();
         }
      }

      // add item log
      out.putNextEntry(new ZipEntry("ItemLog.txt"));
      out.write(si.getLog().getBytes("UTF-8"));
      out.closeEntry();

      // add item details
      StringBuffer buff = new StringBuffer();
      buff.append("Name              : " + si.getName() + "\r\n");
      buff.append("Start             : " + si.getStart().toString() + "\r\n");
      buff.append("Stop              : " + si.getStop().toString() + "\r\n");
      buff.append("Duration          : " + si.getDuration() + "\r\n");
      buff.append("Channel           : " + si.getChannel() + "\r\n");
      buff.append("Path Index        : " + si.getCapturePathIndex() + "\r\n");
      buff.append("Capture Type      : " + si.getCapType() + "\r\n");
      buff.append("Filename          : " + si.getFileName() + "\r\n");
      buff.append("File Pattern      : " + si.getFilePattern() + "\r\n");
      buff.append("Keep For          : " + si.getKeepFor() + "\r\n");
      buff.append("Post Task         : " + si.getPostTask() + "\r\n");
      buff.append("Post Task Enabled : " + si.getPostTaskEnabled() + "\r\n");
      buff.append("State             : " + si.getState() + "\r\n");
      buff.append("Status            : " + si.getStatus() + "\r\n");
      buff.append("Type              : " + si.getType() + "\r\n");

      // add warnings
      buff.append("\r\nWarnings:\r\n");
      Vector<String> warns = si.getWarnings();
      for (String warn : warns) {
         buff.append(warn + "\r\n");
      }
      buff.append("\r\n");

      // add logs
      buff.append("Log Files:\r\n");
      Vector<String> logs = si.getLogFileNames();
      for (String log : logs) {
         buff.append(log + "\r\n");
      }
      buff.append("\r\n");

      // add created from
      GuideItem item = si.getCreatedFrom();
      if(item != null)
      {
         buff.append("Created From:\r\n");
         buff.append("Name     : " + item.getName() + "\r\n");
         buff.append("Start    : " + item.getStart().toString() + "\r\n");
         buff.append("Stop     : " + item.getStop().toString() + "\r\n");
         buff.append("Duration : " + item.getDuration() + "\r\n");
         buff.append("\r\n");
      }

      // add signal stats
      HashMap<Date, SignalStatistic> signal = si.getSignalStatistics();
      if(signal.size() > 0)
      {
         buff.append("Signal Statistics: (Locked, Strength, Quality)\r\n");

         Date[] keys = signal.keySet().toArray(new Date[0]);
         for(int x = 0; x < signal.size(); x++)
         {
            SignalStatistic stat = signal.get(keys[x]);
            buff.append(keys[x].toString() + " - " + stat.getLocked() + ", " + stat.getStrength() + ", " + stat.getQuality() + "\r\n");
         }
         buff.append("\r\n");
      }

      // now add the details
      out.putNextEntry(new ZipEntry("ItemDetails.txt"));
      out.write(buff.toString().getBytes("UTF-8"));
      out.closeEntry();

      out.flush();
      out.close();

      // Return Zip Data
      StringBuffer header = new StringBuffer();
      header.append("HTTP/1.1 200 OK\n");
      header.append("Content-Type: application/zip\n");
      header.append("Content-Length: " + bytesOut.size() + "\n");
      header.append("Content-Disposition: attachment; filename=\"ScheduleErrorReport.zip\"\n");

      //DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'", new Locale ("En", "Us", "Unix")); //JC Deprecated
      DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'", Locale.of("En", "Us", "Unix"));
      header.append("Last-Modified: " + df.format(new Date()) + "\n");

      header.append("\n");

      // Send header data
      outStream.write(header.toString().getBytes());

      ByteArrayInputStream zipStream = new ByteArrayInputStream(bytesOut.toByteArray());
      byte[] bytes = new byte[4096];

      int read = zipStream.read(bytes);
      while (read > -1)
      {
         outStream.write(bytes, 0, read);
         outStream.flush();
         read = zipStream.read(bytes);
      }

      return;
   }

   private byte[] updateDeleteAfter(HTTPurl urlData) throws Exception
   {
      int deleteAfter = 0;
      try
      {
         deleteAfter = Integer.parseInt(urlData.getParameter("deleteafter"));
      }
      catch(Exception e){}
      store.setServerProperty("sch.autodel.time", Integer.valueOf(deleteAfter).toString());

      int deleteAction = 0;
      try
      {
         deleteAction = Integer.parseInt(urlData.getParameter("deleteaction"));
      }
      catch(Exception e){}
      store.setServerProperty("sch.autodel.action", Integer.valueOf(deleteAction).toString());

      System.out.println("Action After : " + deleteAfter);
      System.out.println("Action : " + deleteAction);

      String redirect = "HTTP/1.0 302 Moved Temporarily\n";
      redirect += "Location: /servlet/ScheduleDataRes\n\n";
      return redirect.getBytes();
   }

   private byte[] deleteSchedules(HTTPurl urlData) throws Exception
   {
      String action = urlData.getParameter("deleteaction");
      String older = urlData.getParameter("older");
      String status = urlData.getParameter("status");
      System.out.println("older="+older+"status="+status);

      int minutesOffset = 0;
      int statusInt = -1;
      try
      {
         minutesOffset = Integer.parseInt(older);
         statusInt = Integer.parseInt(status);
      }
      catch(Exception e){}

      long timeLimit = (new Date().getTime()) - (minutesOffset * 60 * 1000);

      System.out.println("Time offset = " + minutesOffset + " limit = " + timeLimit + " status=" + statusInt);

      String[] keys = store.getScheduleKeys();
      boolean deletedItem = false;

      for (String key : keys) {
         ScheduleItem item = store.getScheduleItem(key);
         System.out.println("Schedule time = " + item.getStart().getTime() + " status = " + item.getState());

         if((item.getStart().getTime() < timeLimit || minutesOffset == -1) && (item.getState() == statusInt || statusInt == -1))
         {
            if(item.getState() == ScheduleItem.WAITING || item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.SKIPPED || item.getState() == ScheduleItem.ERROR)
            {
               ScheduleItem removedItem = store.removeScheduleItem(item.toString());

               if("0".equals(action)) {
				archiveOldItem(removedItem);
			   }

               deletedItem = true;
            }
         }
      }

      if(deletedItem) {
		store.saveSchedule(null);
	  }

      String redirect = "HTTP/1.0 302 Moved Temporarily\n";
      redirect += "Location: /servlet/ScheduleDataRes\n\n";
      return redirect.getBytes();
   }

   private void archiveOldItem(ScheduleItem removedItem)
   {
      try
      {
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd@HHmmssS");
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         String archiveName = dataPath + File.separator + "archive" + File.separator + "Schedule-" + df.format(removedItem.getStart()) +
            " (" + removedItem.getChannel() + ") (" + removedItem.getName() + ").sof";

         File outFile = new File(archiveName);
         outFile = outFile.getCanonicalFile();
         File parent = outFile.getParentFile();
         if(!parent.exists()) {
			parent.mkdirs();
		 }

         FileOutputStream fos = new FileOutputStream(outFile);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(removedItem);
         oos.close();
      }
      catch(Exception e)
      {
         System.out.println("ERROR trying to archive old Schedule Item:");
         e.printStackTrace();
      }
   }

   private byte[] addTestSchedules(HTTPurl urlData) throws Exception
   {
      /*
         /servlet/ScheduleDataRes?action=10&type=1&number=20&duration=5&gap=10
      */
      StringBuffer out = new StringBuffer(4096);
      int type = 1;
      int number = 1;
      int duration = 1;
      int gap = 1;
      try
      {
         type = Integer.parseInt(urlData.getParameter("type"));
      }
      catch(Exception e){}
      try
      {
         number = Integer.parseInt(urlData.getParameter("number"));
      }
      catch(Exception e){}
      try
      {
         duration = Integer.parseInt(urlData.getParameter("duration"));
      }
      catch(Exception e){}
      try
      {
         gap = Integer.parseInt(urlData.getParameter("gap"));
      }
      catch(Exception e){}

      Calendar start = Calendar.getInstance();
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);
      start.add(Calendar.MINUTE, 1);

      HashMap<String, Channel> channels = store.getChannels();
      String[] keys = channels.keySet().toArray(new String[0]);

      for(int y = 0; y < number; y++)
      {
         for (String key : keys) {
            start.add(Calendar.MINUTE, duration + gap);
            ScheduleItem item = new ScheduleItem(store.rand.nextLong());

            item.setCapType(type);

            item.setType(ScheduleItem.ONCE);

            // Set the initial status
            item.setState(ScheduleItem.WAITING);
            item.setStatus("Waiting");

            // Set the start/stop and duration
            item.setStart(start);
            item.setDuration(duration);

            // Set the channel to capture
            item.setChannel(key);

            // Set autoDel
            item.setAutoDeletable(false);

            // Set Post task
            item.setPostTask("");

            // Set filename pattern
   		    String[] namePatterns = store.getNamePatterns();
   		    item.setFilePattern(namePatterns[0]);

            // Log the add action
            item.log("New TEST Schedule added/edited");

            // Store the new Schedule Item
            store.addScheduleItem(item);
         }
      }


      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + "/servlet/" + urlData.getServletClass() + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] stopRunningTask(HTTPurl urlData) throws Exception
   {
      StringBuffer out = new StringBuffer(4096);
      String id = urlData.getParameter("id");

      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/" + urlData.getServletClass();
	  }

      ScheduleItem si = null;

      ThreadLock.getInstance().getLock();
      try
      {
         si = store.getScheduleItem(id);
         if(si != null && si.getState() == ScheduleItem.RUNNING)
         {
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

      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private String getScheduleInfo(ScheduleItem item, HTTPurl urlData)
   {
      StringBuffer data = new StringBuffer(2048);

      data.append("<p style='border: solid 1px #FFFFFF; padding: 3px; width: 100%;'>Schedule Details:</p>\n");

      data.append("Current State : " + item.getStatus() + " ("
         + item.getState() + ")<p>");

      String type = "? " + item.getType() + " ?";
      if(item.getType() == ScheduleItem.ONCE) {
		type = "Once";
	  } else if(item.getType() == ScheduleItem.DAILY) {
		type = "Daily";
	  } else if(item.getType() == ScheduleItem.WEEKLY) {
		type = "Weekly";
	  } else if(item.getType() == ScheduleItem.MONTHLY) {
		type = "Monthly";
	  } else if(item.getType() == ScheduleItem.WEEKDAY) {
		type = "Week Day";
	  } else if(item.getType() == ScheduleItem.EPG) {
		type = "EPG";
	  }

      data.append("This Scheduled Item is set to trigger " + type + " <p>");

      data.append("<table>");
      data.append("<tr><td>Start</td><td>" + dtf.format(item.getStart()) + "</td></tr>");
      data.append("<tr><td>Stop</td><td>" + dtf.format(item.getStop()) + "</td></tr>");
      data.append("<tr><td>Duration</td><td>" + item.getDuration() + "</td></tr>");
      data.append("<tr><td>Channel</td><td>" + item.getChannel() + "</td></tr>");
      data.append("</table><p>");

      data.append("Time to next trigger : " + getTimeLeft(item.getStart())
         + "<p>");
      data.append("Name Pattern : " + item.getFilePattern() + "<br>");

      String[] paths = store.getCapturePaths();

      String capName = item.getFileName();
      data.append("File : " + capName + "<br>");

      if(item.getCapturePathIndex() == -1) {
		data.append("Path : AutoSelect<br>");
	  } else if(item.getCapturePathIndex() < 0 || item.getCapturePathIndex() > paths.length-1) {
		data.append("Path : Out of range!<br>");
	  } else
      {
         try
         {
            data.append("Path : " + new File(paths[item.getCapturePathIndex()]).getCanonicalPath() + "<br>");
         }
         catch(Exception e)
         {
            data.append("Path : Does not exist!<br>");
         }
      }

      data.append("<p>");

      if(item.getCreatedFrom() != null)
      {
         data.append("Created From :<pre>");

         data.append("Title      : " + item.getCreatedFrom().getName() + "\n");
         data.append("Sub Title  : " + item.getCreatedFrom().getSubName() + "\n");
         data.append("Start      : " + item.getCreatedFrom().getStart().toString() + "\n");
         data.append("Duration   : " + item.getCreatedFrom().getDuration() + "\n");

         data.append("</pre><p>");
      }

      Vector<CaptureCapability> capabilities = CaptureCapabilities.getInstance().getCapabilities();

      String capType = "ERROR";

      if(item.getCapType() == -1)
      {
         capType = "AutoSelect";
      }
      else
      {
         for (CaptureCapability capability : capabilities) {
            if(capability.getTypeID() == item.getCapType()) {
				capType = capability.getName();
			}
         }
      }

      data.append("Capture Type : " + capType + "<p>");

      data.append("Is Auto Deletable : " + item.isAutoDeletable() + "<br>");
      data.append("Keep for : " + item.getKeepFor()
         + " days before auto deleting.<p>");

      data.append("Post Capture Task : " + item.getPostTask() + "<p>");
      data.append("Post Capture Task Enabled : " + item.getPostTaskEnabled() + "<p>");

      // add the signal status info
      data.append("<p style='border: solid 1px #FFFFFF; padding: 3px; width: 100%;'>Signal Statistics:</p>\n");

      data.append("<table cellpadding='2' cellspacing='2'>\n");
      data.append("<tr><td>&nbsp;</td>");
      data.append("<td>Strength</td>");
      data.append("<td>Quality</td></tr>\n");

      HashMap<Date, SignalStatistic> stats = item.getSignalStatistics();
      Date[] keys = stats.keySet().toArray(new Date[0]);
      Arrays.sort(keys);

      NumberFormat nf2Dec = NumberFormat.getInstance();
      nf2Dec.setMaximumFractionDigits(2);

      double strengthMIN = -1;
      double strengthAVG = 0;
      double strengthMAX = -1;

      double qualityMIN = -1;
      double qualityAVG = 0;
      double qualityMAX = -1;

      for (Date key : keys) {
         SignalStatistic value = stats.get(key);

         if(strengthMIN == -1 || value.getStrength() < strengthMIN) {
			strengthMIN = value.getStrength();
		 }

         if(strengthMAX == -1 || value.getStrength() > strengthMAX) {
			strengthMAX = value.getStrength();
		 }

         if(qualityMIN == -1 || value.getQuality() < qualityMIN) {
			qualityMIN = value.getQuality();
		 }

         if(qualityMAX == -1 || value.getQuality() > qualityMAX) {
			qualityMAX = value.getQuality();
		 }

         strengthAVG += value.getStrength();
         qualityAVG += value.getQuality();
      }

      if(keys.length > 0)
      {
         strengthAVG /= keys.length;
         qualityAVG /= keys.length;
      }

      data.append("<tr><td align='left'>Minimum</td>");
      data.append("<td align='center'>" + nf2Dec.format(strengthMIN) + "</td>");
      data.append("<td align='center'>" + nf2Dec.format(qualityMIN) + "</td></tr>\n");

      data.append("<tr><td align='left'>Average</td>");
      data.append("<td align='center'>" + nf2Dec.format(strengthAVG) + "</td>");
      data.append("<td align='center'>" + nf2Dec.format(qualityAVG) + "</td></tr>\n");

      data.append("<tr><td align='left'>Maximum</td>");
      data.append("<td align='center'>" + nf2Dec.format(strengthMAX) + "</td>");
      data.append("<td align='center'>" + nf2Dec.format(qualityMAX) + "</td></tr>\n");

      data.append("</table>\n");

      if(keys.length > 0)
      {
         data.append("<ul>\n");
         data.append("<li><a class='nounder' href='/servlet/SignalStatisticsImageDataRes?action=01&id=" + item.toString() + "&data=strength'>Show Signal Strength Graph</a></li>\n");
         data.append("<li><a class='nounder' href='/servlet/SignalStatisticsImageDataRes?action=01&id=" + item.toString() + "&data=quality'>Show Signal Quality Graph</a></li>\n");
         data.append("</ul>\n");
      }

      data.append("<p style='border: solid 1px #FFFFFF; padding: 3px; width: 100%;'>Schedule Log:</p>\n");
      String log = item.getLog();
      data.append("<pre class='log'>" + log + "</pre>");

      return data.toString();
   }

   private  String getTimeLeft(Date start)
   {
      Date now = new Date();
      long timeLeft = start.getTime() - now.getTime();

      long days = timeLeft / (1000 * 60 * 60 *24);
      long hours = (timeLeft - (days * 1000 * 60 * 60 *24)) / (1000 * 60 * 60);
      long min = (timeLeft - (days * 1000 * 60 * 60 *24) - (hours * 1000 * 60 * 60)) / (1000 * 60);
      long seconds = (timeLeft - (days * 1000 * 60 * 60 *24) - (hours * 1000 * 60 * 60) - (min * 1000 * 60)) / 1000;

      return days + ":days " + hours + ":hours " + min + ":min " + seconds + ":sec";
   }

   private byte[] showItemInfo(HTTPurl urlData) throws Exception
   {
      ScheduleItem si = store.getScheduleItem(urlData.getParameter("id"));

      if(si != null)
      {
	      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
          + File.separator + "templates" + File.separator + "info.html");
	      template.replaceAll("$info", getScheduleInfo(si, urlData));
         template.replaceAll("$schedule_id", si.toString());
	      return template.getPageBytes();
      }
      else
      {
      	StringBuffer out = new StringBuffer(4096);
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
         return out.toString().getBytes();
      }
   }

   private byte[] skipToNext(HTTPurl urlData) throws Exception
   {
   	String id = urlData.getParameter("id");
   	ScheduleItem item = store.getScheduleItem(urlData.getParameter("id"));

      // Only allow update to waiting schedules
      if(id != null && id.length() > 0 && item != null && item.getState() == ScheduleItem.WAITING)
      {
         item.skipToNext();
         store.saveSchedule(null);
      }

		// Redirect back to the main page
      StringBuffer out = new StringBuffer();
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");

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
		backURL = "/servlet/" + urlData.getServletClass();
	  }

      String id = urlData.getParameter("id");
   	ScheduleItem item = store.getScheduleItem(id);

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
           System.out.println("Not RUNNING");
      	   cal.add(Calendar.MINUTE, (startBuff * -1));
      	   item.setDuration(item.getDuration() + startBuff + endBuff + endBuffEpg);
      	   item.setStart(cal);
      	}
      	else
      	{
            System.out.println("RUNNING");
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

   private byte[] deleteSchedule(HTTPurl urlData) throws Exception
   {
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
      String id = urlData.getParameter("id");

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

      // get merge option
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

   private byte[] addUpdateItem(HTTPurl urlData) throws Exception
   {
      String id = urlData.getParameter("id");
      ScheduleItem item = null;
      String statusPage = urlData.getParameter("status");

      String backURL = urlData.getCookie("backURL");
      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}
      if(backURL == null || backURL.length() == 0) {
		backURL = "/servlet/" + urlData.getServletClass();
	  }

      if(id != null && id.length() > 0)
      {
         item = store.getScheduleItem(id);
      }

      // if we are trying to edit a running schedule just redirect back
      // to the main page
      if(item != null && (item.getState() != ScheduleItem.FINISHED && item.getState() != ScheduleItem.WAITING && item.getState() != ScheduleItem.SKIPPED && item.getState() != ScheduleItem.ERROR))
      {
         StringBuffer out = new StringBuffer();
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/ScheduleDataRes\n\n");
         return out.toString().getBytes();
      }

      String mes = addSchedule(urlData, item);


      if(statusPage != null && statusPage.equals("1"))
      {
         StringBuffer buff = new StringBuffer();
         buff.append("HTTP/1.0 200\n");
         buff.append("Content-Type: text/xml\n\n");
         buff.append("<schedule_add>\n");

         if(mes == null)
         {
            buff.append("<status>ADDED</status>\n");
            buff.append("<message></message>\n");
         }
         else
         {
            buff.append("<status>FAILED</status>\n");
            buff.append("<message>" + mes + "</message>\n");
         }

         buff.append("</schedule_add>\n");
         return buff.toString().getBytes();
      }

      if(mes != null) {
		throw new Exception(mes);
	  }

      StringBuffer out = new StringBuffer(4096);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: " + backURL + "\n\n");

      return out.toString().getBytes();
   }

   private String addSchedule(HTTPurl urlData, ScheduleItem item) throws Exception
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

      //set the capture path
      int pathIndex = -1;
      try
      {
         pathIndex = Integer.parseInt(urlData.getParameter("path"));
      }
      catch (Exception e01)
      {
      }
      item.setCapturePathIndex(pathIndex);

      // Set the channel to capture
      item.setChannel(channel);

      // Set the Auto Del option
      if("true".equalsIgnoreCase(autoDel)) {
		item.setAutoDeletable(true);
	  } else {
		item.setAutoDeletable(false);
	  }

      item.setFilePattern(namePattern);

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
         keepFor = store.getProperty("autoDel.keepfor");
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
         return "Already In List";
      }
      else
      {
         store.addScheduleItem(item);
      }

      return null;
   }

   private byte[] showAddForm(HTTPurl urlData) throws Exception
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

      if(id != null && id.length() > 0)
      {
         item = store.getScheduleItem(id);
      }

      // if we are trying to edit a running schedule just redirect back
      // to the main page
      if(item != null
         && (item.getState() != ScheduleItem.FINISHED
         && item.getState() != ScheduleItem.WAITING
         && item.getState() != ScheduleItem.SKIPPED && item.getState() != ScheduleItem.ERROR))
      {
         StringBuffer out = new StringBuffer();
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/ScheduleDataRes\n\n");
         return out.toString().getBytes();
      }

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "itemdetails.html");

      if(item != null) {
		template.replaceAll("$duration",
                  Integer.valueOf(item.getDuration()).toString());
	  } else {
		template.replaceAll("$duration", "1");
	  }

      if(item != null) {
		template.replaceAll("$name", item.getName());
	  } else {
		template.replaceAll("$name", "");
	  }

      Calendar cal = Calendar.getInstance();
      if(item != null) {
		cal.setTime(item.getStart());
	  }

      template.replaceAll("$hour",
               store.intToStr(cal.get(Calendar.HOUR_OF_DAY)));
      template.replaceAll("$min", store.intToStr(cal.get(Calendar.MINUTE)));

      template.replaceAll("$channels", getChannelList(item));

      template.replaceAll("$item_type", getTypeList(item));

      template.replaceAll("$item_captype", getCapTypeList(item));

      String fields = "";
      fields += "<input type='hidden' name='day' value='" + day + "'>\n";
      fields += "<input type='hidden' name='month' value='" + month + "'>\n";
      fields += "<input type='hidden' name='year' value='" + year + "'>\n";

      if(item != null)
      {
         fields += "<input name='id' type='hidden' id='id' value='" + id + "'>\n";
      }

      template.replaceAll("$fields", fields);

      if(item != null && item.isAutoDeletable()) {
		template.replaceAll("$adtrue", "checked");
	  } else {
		template.replaceAll("$adtrue", "");
	  }

      template.replaceAll("$pattern", getNamePatternList(item));

      if(item == null)
      {
         String defKeepFor = store.getProperty("autodel.keepfor");
         if(defKeepFor == null) {
			defKeepFor = "30";
		 }

         template.replaceAll("$keepfor", defKeepFor);
      }
      else
      {
         template.replaceAll("$keepfor",
                  Integer.valueOf(item.getKeepFor()).toString());
      }

      template.replaceAll("$tasks", getTaskList(item));

      template.replaceAll("$CapturePaths", getCapturePathList(item));

      return template.getPageBytes();
   }

   private String getCapturePathList(ScheduleItem item) throws Exception
   {
      StringBuffer buff = new StringBuffer(1024);
      String[] paths = store.getCapturePaths();

      int activePath = -1;
      if(item != null)
      {
         activePath = item.getCapturePathIndex();
      }

      if(activePath == -1) {
		buff.append("<label><input type='radio' name='path' value='-1' checked>AutoSelect</label><br>\n");
	  } else {
		buff.append("<label><input type='radio' name='path' value='-1'>AutoSelect</label><br>\n");
	  }

      for(int x = 0; x < paths.length; x++)
      {
         String actualPath = new File(paths[x]).getCanonicalPath();
         if(x == activePath) {
			buff.append("<label><input type='radio' name='path' value='" + x + "' checked>" + actualPath + "</label><br>\n");
		 } else {
			buff.append("<label><input type='radio' name='path' value='" + x + "'>" + actualPath + "</label><br>\n");
		 }
      }

      return buff.toString();
   }

   private String getTaskList(ScheduleItem item)
   {
      StringBuffer buff = new StringBuffer(1024);
      HashMap<String, TaskCommand> tasks = store.getTaskList();

      String selectedTask = store.getProperty("tasks.deftask");
      if(item != null)
      {
         selectedTask = item.getPostTask();
      }

      String[] keys = tasks.keySet().toArray(new String[0]);
      Arrays.sort(keys);

      if(selectedTask.length() == 0) {
		buff.append("<label><input type='radio' name='task' value='none' checked>none</label><br>\n");
	  } else {
		buff.append("<label><input type='radio' name='task' value='none'>none</label><br>\n");
	  }

      for (String key : keys) {
         if(key.equals(selectedTask)) {
			buff.append("<label><input type='radio' name='task' value='" + key + "' checked>" + key + "</label><br>\n");
		 } else {
			buff.append("<label><input type='radio' name='task' value='" + key + "'>" + key + "</label><br>\n");
		 }
      }

      return buff.toString();
   }

   private String getNamePatternList(ScheduleItem item)
   {
      StringBuffer buff = new StringBuffer(1024);
      String[] namePatterns = store.getNamePatterns();

		for(int x = 0; x < namePatterns.length; x++)
		{
			if(item == null && x == 0 || item != null && namePatterns[x].equals(item.getFilePattern())) {
				buff.append("<label><input type='radio' name='namePattern' value='" + namePatterns[x] + "' checked>" + namePatterns[x] + "</label><br>\n");
			} else {
				buff.append("<label><input type='radio' name='namePattern' value='" + namePatterns[x] + "'>" + namePatterns[x] + "</label><br>\n");
			}
		}

      return buff.toString();
   }

   private String getCapTypeList(ScheduleItem item)
   {
      StringBuffer typeList = new StringBuffer(1024);
      int type = -1;

      if (item != null) {
		type = item.getCapType();
	  }

      if(type == -1)
      {
         typeList.append("<label><input name='captype' type='radio' value='-1' checked>AutoSelect</label><br>\n");
      }
      else
      {
         typeList.append("<label><input name='captype' type='radio' value='-1'>AutoSelect</label><br>\n");
      }

      Vector<CaptureCapability> capabilities = CaptureCapabilities
            .getInstance().getCapabilities();

      for (CaptureCapability capability : capabilities) {
         typeList.append("<label><input name='captype' type='radio' value='"
               + capability.getTypeID() + "' ");

         if (type == capability.getTypeID()) {
			typeList.append("checked");
		 }

         typeList.append(">" + capability.getName() + "</label><br>\n");
      }

      return typeList.toString();
   }

   private String getChannelList(ScheduleItem item)
   {
      String channelList = "";
      HashMap<String, Channel> channels = store.getChannels();
      String[] keys = channels.keySet().toArray(new String[0]);
      Arrays.sort(keys);
      String chan = null;

      Arrays.sort(keys);

      if (item != null) {
		chan = item.getChannel();
	  }

      for (String key : keys) {
         if (chan != null && key.equals(chan)) {
			channelList += "<label><input type='radio' name='channel' value='"
                  + key + "' checked>" + key + "</label><br>\n";
		 } else {
			channelList += "<label><input type='radio' name='channel' value='"
                  + key + "'>" + key + "</label><br>\n";
		 }
      }

      return channelList;
   }

   private String getTypeList(ScheduleItem item)
   {
      StringBuffer typeList = new StringBuffer(1024);
      int type = 0;

      if (item != null) {
		type = item.getType();
	  }

      typeList.append("<label><input name='type' type='radio' value='0' ");
      if (type == ScheduleItem.ONCE) {
		typeList.append("checked");
	  }
      typeList.append(">Once</label><br>\n");

      typeList.append("<label><input name='type' type='radio' value='1' ");
      if (type == ScheduleItem.DAILY) {
		typeList.append("checked");
	  }
      typeList.append(">Daily</label><br>\n");

      typeList.append("<label><input name='type' type='radio' value='2' ");
      if (type == ScheduleItem.WEEKLY) {
		typeList.append("checked");
	  }
      typeList.append(">Weekly</label><br>\n");

      typeList.append("<label><input name='type' type='radio' value='3' ");
      if (type == ScheduleItem.MONTHLY) {
		typeList.append("checked");
	  }
      typeList.append(">Monthly</label><br>\n");

      typeList.append("<label><input name='type' type='radio' value='4' ");
      if (type == ScheduleItem.WEEKDAY) {
		typeList.append("checked");
	  }
      typeList.append(">Week Day</label><br>\n");

      typeList.append("<label><input name='type' type='radio' value='5' ");
      if (type == ScheduleItem.EPG) {
		typeList.append("checked");
	  }
      typeList.append(">EPG</label><br>\n");

      return typeList.toString();
   }

   private byte[] showCalendar(HTTPurl urlData) throws Exception
   {
      String id = urlData.getParameter("id");

      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      ScheduleItem item = store.getScheduleItem(id);

      // if we are trying to edit a running schedule just redirect back
      // to the main page
      if(item != null
               && (item.getState() != ScheduleItem.FINISHED
               && item.getState() != ScheduleItem.WAITING
               && item.getState() != ScheduleItem.SKIPPED && item.getState() != ScheduleItem.ERROR))
      {
         StringBuffer out = new StringBuffer();
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/ScheduleDataRes\n\n");
         return out.toString().getBytes();
      }

      int month = -1;
      int year = -1;
      try
      {
         month = Integer.parseInt(urlData.getParameter("month"));
         year = Integer.parseInt(urlData.getParameter("year"));
      }
      catch (Exception e)
      {
      }

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "calendar.html");
      template.replaceAll("$calendar", getCalendarTable(month, year, id, index, urlData));

      return template.getPageBytes();
   }

   private String getCalendarTable(int month, int year, String id, int index,
            HTTPurl urlData) throws Exception
   {
      StringBuffer content = new StringBuffer();
      Calendar cal = Calendar.getInstance();
      ScheduleItem item = null;
      String idString = "";

      if(id != null && id.length() > 0)
      {
         item = store.getScheduleItem(id);
         idString = "&id=" + URLEncoder.encode(id, "UTF-8");
      }

      if(index > -1)
      {
         idString += "&index=" + index;
      }

      int thisDay = 0;
      cal.get(Calendar.DATE);
      int thisMonth = 0;
      cal.get(Calendar.MONTH);
      int thisYear = 0;
      cal.get(Calendar.YEAR);
      if(item != null)
      {
         Calendar currentDate = Calendar.getInstance();
         currentDate.setTime(item.getStart());
         thisDay = currentDate.get(Calendar.DATE);
         thisMonth = currentDate.get(Calendar.MONTH);
         thisYear = currentDate.get(Calendar.YEAR);
      }
      else
      {
         thisDay = cal.get(Calendar.DATE);
         thisMonth = cal.get(Calendar.MONTH);
         thisYear = cal.get(Calendar.YEAR);
      }

      cal.set(Calendar.DATE, 1);
      if(month != -1 && year != -1)
      {
         cal.set(Calendar.MONTH, month);
         cal.set(Calendar.YEAR, year);
      }
      else
      {
         year = cal.get(Calendar.YEAR);
         month = cal.get(Calendar.MONTH);
      }

      cal.add(Calendar.MONTH, 1);
      int nextMonth = cal.get(Calendar.MONTH);
      int nextYear = cal.get(Calendar.YEAR);
      cal.add(Calendar.MONTH, -2);
      int prevMonth = cal.get(Calendar.MONTH);
      int prevYear = cal.get(Calendar.YEAR);
      cal.add(Calendar.MONTH, 1);

      content.append("<tr><td class='calendarTitle' colspan='7'>");
      content.append("<a class='infoNav' href='/servlet/"
               + urlData.getServletClass() + "?action=01&month=" + prevMonth
               + "&year=" + prevYear + idString + "'><<</a> ");
      content.append(store.monthNameFull.get(Integer.valueOf(month)) + " " + year);
      content.append(" <a class='infoNav' href='/servlet/"
               + urlData.getServletClass() + "?action=01&month=" + nextMonth
               + "&year=" + nextYear + idString + "'>>></a> ");
      content.append("</td></tr>");

      content.append("<tr><td class='calendarweek'>SUN</td><td class='calendarweek'>MON</td><td class='calendarweek'>TUES</td><td class='calendarweek'>WED</td><td class='calendarweek'>THUR</td><td class='calendarweek'>FRI</td><td class='calendarweek'>SAT</td></tr>");

      int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
      int dayOfMonth = 0;
      int numberOfWeeks = (cal.getActualMaximum(Calendar.DATE) + dayOfWeek + 5) / 7;

      // System.out.println(cal.get(Calendar.MONTH));

      for (int week = 0; week < numberOfWeeks; week++)
      {
         content.append("<tr>");
         for (int day = 1; day < 8; day++)
         {
            if((day < dayOfWeek) && week == 0)
            {
               content.append("<td class='calendarday'>&nbsp;</td>");
            }
            else if(cal.get(Calendar.MONTH) != month)
            {
               content.append("<td class='calendarday'>&nbsp;</td>");
            }
            else
            {
               dayOfMonth = cal.get(Calendar.DATE);
               String dayURL = "/servlet/" + urlData.getServletClass()
                        + "?action=02&day=" + dayOfMonth + "&month=" + month
                        + "&year=" + year + idString;

               if(dayOfMonth == thisDay && month == thisMonth
                        && year == thisYear)
               {
                  content.append("<td onClick=\"document.location.href='"
                           + dayURL + "'\" class='calendarToday'>");
               }
               else
               {
                  content.append("<td onClick=\"document.location.href='"
                           + dayURL + "'\" class='calendarday'>");
               }

               content.append("<a class='noUnder' href='" + dayURL + "'>");
               content.append(dayOfMonth + "</a></td>");

               cal.add(Calendar.DATE, 1);
            }

         }
         content.append("</tr>");
      }

      return content.toString();
   }

   private byte[] getScheduleTable(HTTPurl urlData) throws Exception
   {
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "schedulelist.html");

      String showOverlap = urlData.getParameter("ShowOverlap");
      boolean showOverlapStatus = false;

      if("1".equals(showOverlap))
      {
         showOverlapStatus = true;
         String overlapIcon = "<a href='/servlet/ScheduleDataRes' class='noUnder'>" +
            "<img border=0 src='/images/stop.png' align='absmiddle' width='24' height='24'> " +
            "<span class='areaTitle'>Hide Overlaps</span></a>";
         template.replaceAll("$OverlapIcon", overlapIcon);
      }
      else
      {
         showOverlapStatus = false;
         String overlapIcon = "<a href='/servlet/ScheduleDataRes?ShowOverlap=1' class='noUnder'>" +
            "<img border=0 src='/images/log.png' align='absmiddle' width='24' height='24'> " +
            "<span class='areaTitle'>Show Overlaps</span></a>";
         template.replaceAll("$OverlapIcon", overlapIcon);
      }

      StringBuffer content = new StringBuffer();

      ScheduleItem[] itemsArray = store.getScheduleArray();

      ScheduleItem[] temp = filterItems(itemsArray, true);
      int totalItmes = 0;

      if(temp.length > 0)
      {
         totalItmes += temp.length;
         content.append("<tr><td colspan='9' class='itemheading'>Past Schedules</td></tr>");
         getSchTblData(temp, content, showOverlapStatus);
         content.append("<tr><td colspan='9'><br></td></tr>");
         template.replaceAll("$PastSchedules", content.toString());
      } else {
		template.replaceAll("$PastSchedules", "");
	  }

      temp = filterItems(itemsArray, false);
      content = new StringBuffer();

      if(temp.length > 0)
      {
         totalItmes += temp.length;
         content.append("<tr><td colspan='9' class='itemheading'>Pending Schedules</td></tr>");
         getSchTblData(temp, content, showOverlapStatus);
         template.replaceAll("$PendingSchedules", content.toString());
      } else {
		template.replaceAll("$PendingSchedules", "");
	  }

      if(totalItmes == 0)
      {
         content = new StringBuffer();
         content.append("<tr><td colspan='9' class='itemheading'>No Schedule Items</td></tr>");
         template.replaceAll("$NoSchedules", content.toString());
      } else {
		template.replaceAll("$NoSchedules", "");
	  }

      // set current page to check point for back
      template.addCookie("backURL", urlData.getReqString());

      // auto delete action
      int autoDelSchedAction = Integer.parseInt(store.getProperty("sch.autodel.action"));
      String autoAction = "";
      if(autoDelSchedAction == 0)
      {
         autoAction += "<option value='0' selected='selected'>Archive</option>\n";
         autoAction += "<option value='1'>Delete</option>";
      }
      else
      {
         autoAction += "<option value='0'>Archive</option>\n";
         autoAction += "<option value='1' selected='selected'>Delete</option>";
      }
      template.replaceAll("$deleteAction", autoAction);

      // set delete after value
      String autoDel = store.getProperty("sch.autodel.time");
      template.replaceAll("$deleteAfter", autoDel);

      return template.getPageBytes();

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

      ScheduleItem[] items = filteredList.toArray(new ScheduleItem[0]);
      Arrays.sort(items);

      return items;
   }

   private int overlapDepth(ScheduleItem item)
   {
      Calendar cal = Calendar.getInstance();

      ScheduleItem[] items = store.getScheduleArray();
      HashMap <String, Channel> channels = store.getChannels();
      Channel schChan = channels.get(item.getChannel());
      String muxString = "";
      try
      {
        muxString = schChan.getFrequency() + "-" + schChan.getBandWidth();
      } catch(Exception e){}

      Vector<ScheduleItem> overlapItems = new Vector<>();
      for (ScheduleItem item2 : items) {
         if(!item2.toString().equals(item.toString()))
         {
            if(item.isOverlapping(item2))
            {
               overlapItems.add(item2);
            }
         }
      }

      // now check the depth

      cal.setTime(item.getStart());
      int duration = item.getDuration();
      int maxCount = 0;

      for(int x = 0; x < duration; x++)
      {
         HashMap <String, Integer> muxCountMap = new HashMap <>();
         muxCountMap.put(muxString, Integer.valueOf(1));

         for (ScheduleItem checkItem : overlapItems) {
            long slice = cal.getTime().getTime();
            if(slice > checkItem.getStart().getTime() && slice < checkItem.getStop().getTime())
            {
               Channel checkChan = channels.get(checkItem.getChannel());

               String checkMuxString = "";
               try
               {
                checkMuxString = checkChan.getFrequency() + "-" + checkChan.getBandWidth();
               } catch(Exception e){}

               Integer muxCount = muxCountMap.get(checkMuxString);
               if(muxCount == null)
               {
                  muxCountMap.put(checkMuxString, Integer.valueOf(1));
               }
               else
               {
                  muxCountMap.put(checkMuxString, Integer.valueOf(muxCount.intValue() + 1));
               }
            }
         }

         String[] muxTotal = muxCountMap.keySet().toArray(new String[0]);
         if(maxCount < muxTotal.length) {
			maxCount = muxTotal.length;
		 }

         cal.add(Calendar.MINUTE, 1);
      }

      return maxCount;
   }

   private int getSchTblData(ScheduleItem[] itemsArray, StringBuffer content, boolean showOverlapStatus) throws Exception
   {
      Calendar dateFormater = Calendar.getInstance();

      //page background colour #6F92FA;
      String rowHi = "class='rowHi'";
      String rowLo = "class='rowLow'";

      for(int x = 0; x < itemsArray.length; x++)
      {
         ScheduleItem item = itemsArray[x];

         dateFormater.setTime(item.getStart());

         String type = "";
         if(item.getType() == ScheduleItem.ONCE) {
			type = "Once";
		 } else if(item.getType() == ScheduleItem.DAILY) {
			type = "Daily";
		 } else if(item.getType() == ScheduleItem.WEEKLY) {
			type = "Weekly";
		 } else if(item.getType() == ScheduleItem.MONTHLY) {
			type = "Monthly";
		 } else if(item.getType() == ScheduleItem.WEEKDAY) {
			type = "Week Day";
		 } else if(item.getType() == ScheduleItem.EPG) {
			type = "EPG";
		 } else {
			type = "?" + item.getType() + "?";
		 }

         content.append("<tr ");
         if(x%2 == 0) {
			content.append(rowHi + " >");
		 } else {
			content.append(rowLo + " >");
		 }

         content.append("<td class='itemdata'>");

         // check for warnings and if any show warning icon
         Vector<String> warnings = item.getWarnings();
         if(warnings.size() > 0)
         {
            String waringText = "";
            for (String warning : warnings) {
               waringText += " - " + warning + "\\n";
            }

            content.append("<img alt='Warning' title='This Schedule has Warnings' onClick=\"warningBox('" + waringText + "', '" + item.toString() + "');\" " +
            "src='/images/exclaim.png' border='0' width='22' height='24' style='cursor: pointer; cursor: hand;''> ");
         }

         CaptureDeviceList devList = CaptureDeviceList.getInstance();

         if(showOverlapStatus)
         {
            int depth = overlapDepth(item);

            if(depth > devList.getDeviceCount()) {
				content.append("<img alt='To many overlapping items (" + depth + ")' title='To many overlapping items (" + depth + ")' " +
				         "src='/images/exclaim.png' border='0' width='22' height='24'> ");
			} else {
				content.append("<img alt='(" + depth + ")' title='(" + depth + ")' " +
				         "src='/images/tick.png' border='0' alt='Ok' width='24' height='24'> ");
			}

         }

         content.append("<a href='/servlet/ScheduleDataRes?action=07&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "'>");
         content.append("<img src='/images/log.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");

         Calendar viewDate = Calendar.getInstance();
         viewDate.setTime(dateFormater.getTime());

         if(viewDate.get(Calendar.HOUR_OF_DAY) <= 6) {
			viewDate.add(Calendar.DATE, -1);
		 }

         String egpUrl = "/servlet/EpgDataRes?action=12&year=" + viewDate.get(Calendar.YEAR) +
            "&month=" + (viewDate.get(Calendar.MONTH)+1) + "&day=" + viewDate.get(Calendar.DATE) +
            "&scrollto=" + viewDate.get(Calendar.HOUR_OF_DAY);
			content.append("<a href='" + egpUrl + "'>");
			content.append("<img src='/images/epg.png' border='0' alt='EPG Link' width='24' height='24'></a>\n");


         content.append("</td>\n");

         content.append("<td class='itemdata'>" + item.getName() + "</td>\n");

         //
         // Add the time to the table
         //
         // I think there is a bug in the .get(Calendar.HOUR) of Calendar
         // it return 0 for 12 pm so do the following to fix this
         int hour = dateFormater.get(Calendar.HOUR);
         if(hour == 0) {
			hour = 12;
		 }
         String timeString = store.intToStr(hour) + ":" + store.intToStr(dateFormater.get(Calendar.MINUTE)) + " " +
         	store.ampm.get(Integer.valueOf(dateFormater.get(Calendar.AM_PM)));
         content.append("<td class='itemdata'><b>" + timeString  + "</b></td>\n");

         //
         // Add the date to the table
         //
         String dateString = store.dayName.get(Integer.valueOf(dateFormater.get(Calendar.DAY_OF_WEEK))) + ", " +
         	dateFormater.get(Calendar.DATE) + " " +
         	store.monthNameShort.get(Integer.valueOf(dateFormater.get(Calendar.MONTH)));// + ", " +
         	//dateFormater.get(Calendar.YEAR);
         content.append("<td class='itemdata'>" + dateString + "</td>\n");

         //
         // Add the duration to the table
         //
         content.append("<td class='itemdata'>" + item.getDuration() + "min</td>\n");

         //
         // Add the channel to the table
         //
         content.append("<td class='itemdata'>");
         content.append(item.getChannel());
         content.append("</td>\n");
         //
         // Add the type to the table
         //
         content.append("<td class='itemdata'>" + type + "</td>\n");

         //
         // Add the status to the table
         //
         content.append("<td class='itemdata'>" + item.getStatus() + "</td>\n");

         //
         // Now add all the action button
         //
         content.append("<td class='itemdata'>");

         // if this item is NOT a parent item add some buttons

         if(item.getState() == ScheduleItem.WAITING ||
                 item.getState() == ScheduleItem.SKIPPED ||
                 item.getState() == ScheduleItem.FINISHED ||
                 item.getState() == ScheduleItem.ERROR)
         {
            // Show the delete button
            content.append("<a onClick='return confirmAction(\"Delete\");' href='/servlet/ScheduleDataRes?action=04&id="
               + URLEncoder.encode(item.toString(), "UTF-8") + "'>");
            content.append("<img src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a>\n");

            if(item.getType() != ScheduleItem.EPG)
            {
               // Show the Edit button
               Calendar cal = Calendar.getInstance();
               cal.setTime(item.getStart());

               content.append(" <a href='/servlet/ScheduleDataRes?action=01&id="
                  + URLEncoder.encode(item.toString(), "UTF-8") + "&month="
                  + cal.get(Calendar.MONTH)
                  + "&year=" + cal.get(Calendar.YEAR) + "'>");
               content.append("<img src='/images/edit.png' border='0' alt='Edit' width='24' height='24'></a>\n");

               // show the add time button
               content.append("<a href='/servlet/ScheduleDataRes?action=05&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "'>");
               content.append("<img src='/images/+5.png' border='0' alt='Add Time' width='24' height='24'></a>\n");
            }

            if(   item.getType() == ScheduleItem.DAILY ||
                  item.getType() == ScheduleItem.WEEKLY ||
                  item.getType() == ScheduleItem.MONTHLY ||
                  item.getType() == ScheduleItem.WEEKDAY)
            {
               content.append("<a href='/servlet/ScheduleDataRes?action=06&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "'>");
               content.append("<img src='/images/skip.png' border='0' alt='Skip' width='24' height='24'></a>\n");
            }
         }


         // regardless if this is a parnet item or not add stop button if it is running
         if(item.getState() == ScheduleItem.RUNNING)
         {
            // Show the stop/abort button
            content.append("<a onClick='return confirmAction(\"Stop\");' href='/servlet/ScheduleDataRes?action=09&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "'>");
            content.append("<img src='/images/stop.png' border='0' alt='Stop' width='24' height='24'></a>\n");

            // show the add time button
            content.append("<a href='/servlet/ScheduleDataRes?action=05&id=" + URLEncoder.encode(item.toString(), "UTF-8") + "'>");
            content.append("<img src='/images/+5.png' border='0' alt='Add Time' width='24' height='24'></a>\n");
         }

         content.append("</td>\n");
         content.append("</tr>\n");
      }

      return 0;
   }
}
