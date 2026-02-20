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
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class ArchiveDataRes extends HTTPResponse
{
   public ArchiveDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      String action = urlData.getParameter("action");
      Method m = this.getClass().getMethod(action, new Class<?>[] {HTTPurl.class, OutputStream.class});
      Object ret = m.invoke(this, urlData, outStream);
      outStream.write((byte[])ret);
   }

   public byte[] showItemInfo(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File archivePath = new File(dataPath + File.separator + "archive" + File.separator + urlData.getParameter("file"));
      ScheduleItem item = null;

      FileInputStream fis = new FileInputStream(archivePath);
      ObjectInputStream ois = new ObjectInputStream(fis);
      item = (ScheduleItem) ois.readObject();
      ois.close();

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot") + File.separator + "templates" + File.separator + "ArchiveItemInfo.html");
      template.replaceAll("$info", getScheduleInfo(item, archivePath.getName()));
      template.replaceAll("$schedule_file", archivePath.getName());
      return template.getPageBytes();
   }

   private String getScheduleInfo(ScheduleItem item, String fileName) throws Exception
   {
      DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

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

      data.append("Name Pattern : " + item.getFilePattern() + "<br>");

      String capName = item.getFileName();
      data.append("File : " + capName + "<br>");
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
         data.append("<li><a class='nounder' href='/servlet/SignalStatisticsImageDataRes?action=01&file=" + URLEncoder.encode(fileName, "UTF-8") + "&data=strength'>Show Signal Strength Graph</a></li>\n");
         data.append("<li><a class='nounder' href='/servlet/SignalStatisticsImageDataRes?action=01&file=" + URLEncoder.encode(fileName, "UTF-8") + "&data=quality'>Show Signal Quality Graph</a></li>\n");
         data.append("</ul>\n");
      }

      data.append("<p style='border: solid 1px #FFFFFF; padding: 3px; width: 100%;'>Schedule Log:</p>\n");
      String log = item.getLog();
      data.append("<pre class='log'>" + log + "</pre>");

      return data.toString();
   }

   public byte[] showArchive(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot") + File.separator + "templates" + File.separator + "ArchiveList.html");

      StringBuffer buff = new StringBuffer();
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File outFile = new File(dataPath + File.separator + "archive");
      if(!outFile.exists()) {
		outFile.mkdirs();
	  }

      File[] files = outFile.listFiles();
      Arrays.sort(files);

      for(int x = files.length-1; files != null && x >= 0; x--)
      {
         File archiveFile = files[x];
         if(!archiveFile.isDirectory() && archiveFile.getName().startsWith("Schedule-"))
         {
            buff.append("<tr>\n");

            buff.append("<td>");
            buff.append("<a href='/servlet/ArchiveDataRes?action=showItemInfo&file=" + URLEncoder.encode(archiveFile.getName(), "UTF-8") + "'>");
            buff.append("<img src='/images/log.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");
            buff.append("<a href='/servlet/ArchiveDataRes?action=deleteArchiveFile&file=" + URLEncoder.encode(archiveFile.getName(), "UTF-8") + "'>");
            buff.append("<img src='/images/delete.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");
            buff.append("</td>");
            buff.append("<td style='padding-left:20px;'> " + archiveFile.getName() + " </td>");

            buff.append("</tr>\n");
         }
      }

      template.replaceAll("$ArchiveList", buff.toString());

      return template.getPageBytes();
   }


   public byte[] deleteArchiveFile(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File basePath = new File(dataPath + File.separator + "archive");
      File archivePath = new File(dataPath + File.separator + "archive" + File.separator + urlData.getParameter("file"));

      if(archivePath.getCanonicalPath().indexOf(basePath.getCanonicalPath()) == -1)
      {
         throw new Exception("Archive file to delete is not in the archive path!");
      }

      if(archivePath.exists())
      {
         archivePath.delete();
      }

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/ArchiveDataRes?action=showArchive\n\n");
      return buff.toString().getBytes();
   }

   public byte[] deleteAllArchives(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File outFile = new File(dataPath + File.separator + "archive");
      if(!outFile.exists()) {
		outFile.mkdirs();
	  }

      File[] files = outFile.listFiles();
      Arrays.sort(files);

      for(int x = files.length-1; files != null && x >= 0; x--)
      {
         File archiveFile = files[x];
         if(!archiveFile.isDirectory() && archiveFile.getName().startsWith("Schedule-"))
         {
            archiveFile.delete();
         }
      }

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/ArchiveDataRes?action=showArchive\n\n");
      return buff.toString().getBytes();
   }


}