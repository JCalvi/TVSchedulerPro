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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class AdminThread implements Runnable
{
   private DataStore store = null;
   private Calendar autoLoadNextRun = Calendar.getInstance();
   private Calendar reportNextRun = null;
   private Date lowSpaceEmailLastRun = new Date(0);

   public AdminThread()
   {
      System.out.println("Admin Thread: Created");
      store = DataStore.getInstance();
   }

   @Override
public void run()
   {
      System.out.println("Admin Thread: Started");

      try
      {
         //loop forever
         while(true)
         {
            //
            // Nuke of AutoDelete files
            //
            try
            {
               nukeOldFiles();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

            //
            // Import any guide info if required
            //
            String scheduleOptions = store.getProperty("guide.source.schedule");
            String[] schOptsArray = scheduleOptions.split(":");

            if(schOptsArray.length == 3 && "1".equals(schOptsArray[0]))
            {
               int hour = -1;
               int min = -1;
               try
               {
                  hour = Integer.parseInt(schOptsArray[1]);
                  min = Integer.parseInt(schOptsArray[2]);
               }
               catch (Exception e)
               {}

               // if the next run time has been changed
               // work out next run time
               if(autoLoadNextRun.get(Calendar.HOUR_OF_DAY) != hour || autoLoadNextRun.get(Calendar.MINUTE) != min)
               {
                  autoLoadNextRun.set(Calendar.HOUR_OF_DAY, hour);
                  autoLoadNextRun.set(Calendar.MINUTE, min);
                  autoLoadNextRun.set(Calendar.MILLISECOND, 0);
                  autoLoadNextRun.set(Calendar.SECOND, 0);
                  autoLoadNextRun.add(Calendar.DATE, -2); // make sure it is in the past

                  // if time is in the past add a day until it is in the future
                  while (autoLoadNextRun.before(Calendar.getInstance()))
                  {
                     autoLoadNextRun.add(Calendar.DATE, 1);
                  }

                  System.out.println("Auto EPG data load next run at : " + autoLoadNextRun.getTime().toString());
               }

               //
               // if auto epg load time is in past, i.e it is time to do our
               // thing then do it and add a day to next run time.
               //
               if(autoLoadNextRun.before(Calendar.getInstance()))
               {
                  System.out.println("Auto Loading EPG Data");

                  // add a day until next run is in the future
                  while (autoLoadNextRun.before(Calendar.getInstance()))
                  {
                     autoLoadNextRun.add(Calendar.DATE, 1);
                  }
                  System.out.println("Auto EPG data load next run at : " + autoLoadNextRun.getTime().toString());

                  // Use the Sch Guide Loader Thread to do the actual work
                  SchGuideLoaderThread sch = new SchGuideLoaderThread();
                  Thread loader = new Thread(Thread.currentThread().getThreadGroup(), sch, sch.getClass().getName());
                  loader.start();
               }
            }


            //
            // send weekly schedule report
            //
            String sendWeeklyReport = store.getProperty("email.send.weeklyreport");
            if(!"1".equals(sendWeeklyReport))
            {
               reportNextRun = null;
            }
            else
            {
               // set up next report run time
               if(reportNextRun == null || reportNextRun.get(Calendar.HOUR_OF_DAY) != 23 || reportNextRun.get(Calendar.MINUTE) != 59)
               {
                  reportNextRun = Calendar.getInstance();
                  reportNextRun.set(Calendar.HOUR_OF_DAY, 23);
                  reportNextRun.set(Calendar.MINUTE, 59);
                  reportNextRun.set(Calendar.MILLISECOND, 0);
                  reportNextRun.set(Calendar.SECOND, 0);
                  reportNextRun.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                  reportNextRun.add(Calendar.DATE, -14); // make sure it is in the past

                  // if time is in the past add a day until it is in the future
                  //while (reportNextRun.before(Calendar.getInstance()))
                  //{
                  //   reportNextRun.add(Calendar.DATE, 7);
                  //}

                  System.out.println("Weekly report next run at : " + reportNextRun.getTime().toString());
               }

               //
               // if report time is in past, i.e it is time to do our
               // thing then do it and add a day to next run time.
               //
               if(reportNextRun.before(Calendar.getInstance()))
               {
                  System.out.println("Sending Weekly Report");

                  // add a day until next run is in the future
                  while (reportNextRun.before(Calendar.getInstance()))
                  {
                     reportNextRun.add(Calendar.DATE, 7);
                  }
                  System.out.println("Weekly report next run at : " + reportNextRun.getTime().toString());

                  String notificationBody = buildReportBody(7);
                  System.out.println(notificationBody);

                  // now send weekly report email
                  EmailSender sender = new EmailSender();

                  sender.setSubject("TV Scheduler Pro Weekly Report");
                  sender.setBody(notificationBody);

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
            }

            //
            // run every 60 sec
            //
            Thread.sleep(60000);
         }
      }
      catch (Exception e)
      {
         System.out.println("The main Admin Thread has crashed!");
         e.printStackTrace();
         System.out.println("This is really bad!!!!!");
         store.adminStatus = -1;

         ByteArrayOutputStream ba = new ByteArrayOutputStream();
         PrintWriter err = new PrintWriter(ba);
         e.printStackTrace(err);
         err.flush();
         store.adminThreadErrorStack = ba.toString();
      }

      System.out.println("Admin Thread: Exited");
   }

   private String buildReportBody(int prevDays)
   {
      String data = "TV Scheduler Pro Weekly Report.\n\n";

      try
      {

         Calendar now = Calendar.getInstance();
         Calendar till = Calendar.getInstance();
         till.add(Calendar.DATE, prevDays*-1);

         Vector<ScheduleItem> items = new Vector<>();

         // add all archive items to report list
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         File archiveDir = new File(dataPath + File.separator + "archive");
         File[] itemFiles = archiveDir.listFiles();

         if(itemFiles != null && itemFiles.length > 0)
         {
            for (File itemFile : itemFiles) {
               if(!itemFile.isDirectory() && itemFile.getName().startsWith("Schedule-"))
               {
                  try
                  {
                     FileInputStream in = new FileInputStream(itemFile);
                     ObjectInputStream oin = new ObjectInputStream(in);
                     ScheduleItem item = (ScheduleItem)oin.readObject();
                     oin.close();
                     in.close();

                     if(item.getStart().getTime() < now.getTime().getTime() && item.getStart().getTime() > till.getTime().getTime())
                     {
                        items.add(item);
                     }
                  }
                  catch(Exception e)
                  {
                     e.printStackTrace();
                  }
               }
            }
         }

         // now add all schedules that are in the list
         String[] keys = store.getScheduleKeys();

         for (String key : keys) {
            ScheduleItem item = store.getScheduleItem(key);

            if(item.getStart().getTime() < now.getTime().getTime() && item.getStart().getTime() > till.getTime().getTime())
            {
               items.add(item);
            }
         }

         // now sort them
         ScheduleItem[] sch_items = items.toArray(new ScheduleItem[0]);
         Arrays.sort(sch_items);
         HashMap<String, Integer> warningCount = new HashMap<>();

         SimpleDateFormat df = new SimpleDateFormat("EE MMM d hh:mm a");
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMinimumIntegerDigits(3);

         data += "Schedule Activity List:\n";

         // now print them
         for (ScheduleItem sch_item : sch_items) {
            data += df.format(sch_item.getStart()) + " (" + nf.format(sch_item.getDuration()) + ") : " + sch_item.getName() + " : " + sch_item.getStatus() + " : warnings " + sch_item.getWarnings().size() + "\r\n";

            for (String warning : sch_item.getWarnings()) {
               if(warningCount.containsKey(warning))
               {
                  Integer count = warningCount.get(warning);
                  warningCount.put(warning, Integer.valueOf(count.intValue() + 1));
               }
               else
               {
                  warningCount.put(warning, Integer.valueOf(1));
               }
            }
         }

         data += "\n";
         data += "Warning Summary:\n";

         String[] warnkeys = warningCount.keySet().toArray(new String[0]);
         for (String warnkey : warnkeys) {
            Integer count = warningCount.get(warnkey);
            data += warnkey + " (" + count.intValue() + ")";
         }

         data += "\n\n";

         data += "Capture Path Details:\n";

         String[] paths = store.getCapturePaths();
         for (String path : paths) {
            File capPath = new File(path);

            if(!capPath.exists())
            {
               data += "Path not found : " + capPath.getAbsolutePath() + "\n";
            }
            else
            {
               DllWrapper wrapper = new DllWrapper();
               long freeSpace = wrapper.getFreeSpace(capPath.getAbsolutePath());
               freeSpace /= (1024 * 1024);

               data += capPath.getAbsolutePath() + "\n";

               data += "   Free Space            : " + nf.format(freeSpace) + " MB\n";

               long[] statsData = new long[5];
               for(int y = 0; y < statsData.length; y++) {
				statsData[y] = 0;
			   }

               fileStats(capPath, statsData);

               nf.setMinimumIntegerDigits(0);
               data += "   Media Data on Disk    : " + nf.format(statsData[0] / (1024 * 1024)) + " MB\n";
               data += "   Number of Media Files : " + statsData[1] + "\n";
               data += "   Number of Directories : " + statsData[2] + "\n";
            }
         }

         data += "\n";

         data += "Next Weekly Report : " + reportNextRun.getTime().toString();

         data += "\n";
      }
      catch(Exception e)
      {
         ByteArrayOutputStream ba = new ByteArrayOutputStream();
         PrintWriter err = new PrintWriter(ba);
         e.printStackTrace(err);
         err.flush();

         return "Error creating report data!\n\n" + ba.toString();
      }

      return data;
   }

   private void fileStats(File path, long[] statsData)
   {
      if(path == null || !path.exists() || !path.isDirectory()) {
		return;
	  }

      statsData[2] += 1;

      File[] list = path.listFiles();

      if(list == null) {
		return;
	  }

      for (File element : list) {
         if(element.exists() && element.isDirectory()) {
			fileStats(element, statsData);
		 }

         if(element.getName().endsWith(".mpg") || element.getName().endsWith(".ts") || element.getName().endsWith(".dvr-ms"))
         {
            long size = element.length();

            statsData[0] = statsData[0] + size;
            statsData[1] += 1;
         }
      }
   }

   private void nukeOldFiles()
   {
      try
      {
         boolean updated = false;

         //System.out.println("Nuking AutoDel Files");

         HashMap<String, KeepForDetails> adList = store.getAutoDelList();
         String[] key = adList.keySet().toArray(new String[0]);
         Arrays.sort(key);

         Calendar limit = Calendar.getInstance();
         Calendar itemDate = Calendar.getInstance();

         for (String element : key) {
            KeepForDetails item = adList.get(element);

            File delFile = new File(item.getFileName());

            limit.setTime(new Date());
            limit.add(Calendar.DATE, (-1 * item.getKeepFor()));

            itemDate.setTime(item.getCreated());

            if(!delFile.exists())
            {
               store.autoDelLogAdd("AutoDelete : " + item.getCreated().toString() + " - " + item.getFileName() + " : No longer exists, removing");
               System.out.println("AutoDelete : " + item.getCreated().toString() + " - " + item.getFileName() + " : No longer exists, removing");
               adList.remove(element);
               updated = true;
            }
            else if(itemDate.before(limit))
            {
               store.autoDelLogAdd("AutoDelete : " + item.getCreated().toString() + " - " + item.getFileName() + " : To old (" + item.getKeepFor() + ") DELETED");
               System.out.println("AutoDelete : " + item.getCreated().toString() + " - " + item.getFileName() + " : To old (" + item.getKeepFor() + ") DELETED");
               delFile.delete();
               adList.remove(element);
               updated = true;
            }
         }

         //
         // check free space on paths to see if we need to try to delete any delete-able files
         //

         String minSpaceWarning = "";
         NumberFormat nf = NumberFormat.getInstance();

         String[] paths = store.getCapturePaths();
         DllWrapper wrapper = new DllWrapper();

         int minSpaceSoft = 1200;
         try
         {
            minSpaceSoft = Integer.parseInt(store.getProperty("capture.minspacesoft"));
         }
         catch(Exception e){}

         boolean deletetofreespace = "1".equals(store.getProperty("capture.deletetofreespace"));

         for(int x = 0; x < paths.length; x++)
         {
            File capPath = new File(paths[x]);

            long freeSpace = wrapper.getFreeSpace(capPath.getCanonicalPath()) / (1024 * 1024);

            //System.out.println("Delete To Free Space : enabled=" + deletetofreespace + " (" + capPath + ") free=" + nf.format(freeSpace) + " " + " softLimit=" + nf.format(minSpaceSoft));

            if(deletetofreespace && (freeSpace < minSpaceSoft))
            {
               System.out.println("Delete To Free Space : checking auto-delete items");
               // ok so lets try to delete some stuff

               HashMap<String, KeepForDetails> deleteList = store.getAutoDelList();
               String[] deletekeys = deleteList.keySet().toArray(new String[0]);
               Arrays.sort(deletekeys);

               for (int index = 0; ((index < deletekeys.length) && (freeSpace < minSpaceSoft)); index++)
               {
                  KeepForDetails autoDelItem = deleteList.get(deletekeys[index]);

                  File autoDelFile = new File(autoDelItem.getFileName());

                  System.out.println("Delete To Free Space : " + deletekeys[index] + " " + autoDelFile.getAbsolutePath());

                  if(!autoDelFile.exists())
                  {
                     store.autoDelLogAdd("AutoDelete : " + autoDelItem.getCreated().toString() + " - " + autoDelItem.getFileName() + " : No longer exists, removing");
                     System.out.println("AutoDelete : " + autoDelItem.getCreated().toString() + " - " + autoDelItem.getFileName() + " : No longer exists, removing");
                     deleteList.remove(deletekeys[x]);
                     updated = true;
                  }

                  // check to see if this auto delete item is on the path with the free space warning
                  // if it is try to remove it.
                  else if(autoDelFile.getAbsolutePath().startsWith(capPath.getAbsolutePath()))
                  {
                     store.autoDelLogAdd("AutoDelete : " + autoDelItem.getCreated().toString() + " - " + autoDelItem.getFileName() + " : Removed to free space DELETED");
                     System.out.println("AutoDelete : " + autoDelItem.getCreated().toString() + " - " + autoDelItem.getFileName() + " : Deleted to free space DELETED");
                     autoDelFile.delete();
                     deleteList.remove(deletekeys[x]);
                     updated = true;

                     // after deleting something refresh the free space value
                     freeSpace = wrapper.getFreeSpace(capPath.getCanonicalPath()) / (1024 * 1024);
                  }
               }
            }

            // once we have tried to delete any auto delete files then check one last time to see if we need to send a warning
            freeSpace = wrapper.getFreeSpace(capPath.getCanonicalPath()) / (1024 * 1024);

            if(freeSpace < minSpaceSoft)
            {
               // add free space warning
               minSpaceWarning += " - " + capPath.getAbsolutePath() + " (" + nf.format(freeSpace) + " MB)\r\n";
               System.out.println(" - " + capPath.getAbsolutePath() + " (" + nf.format(freeSpace) + " MB)");
            }

         }

         // send email low space warning if needed
         boolean sendLowSpaceWarning = "1".equals(store.getProperty("email.send.freespacelow"));
         if(sendLowSpaceWarning)
         {
            if(minSpaceWarning.length() > 0)
            {
               long timeSinceLast = new Date().getTime() - lowSpaceEmailLastRun.getTime();
               if(timeSinceLast > (1000 * 60 * 60 * 24))
               {
                  System.out.println("Sending Low Space Warning Email");
                  EmailSender sender = new EmailSender();

                  sender.setSubject("TV Scheduler Pro: Low Space Warning");

                  sender.setBody(minSpaceWarning);

                  Thread mailThread = new Thread(Thread.currentThread().getThreadGroup(), sender, sender.getClass().getName());
                  mailThread.start();

                  lowSpaceEmailLastRun = new Date();
               }
            }
            else
            {
               lowSpaceEmailLastRun = new Date(0);
            }
         }
         else
         {
            lowSpaceEmailLastRun = new Date(0);
         }

         if(updated) {
			store.saveAutoDelList();
		 }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }


}
