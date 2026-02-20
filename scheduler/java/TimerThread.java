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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class TimerThread implements Runnable
{
   private DataStore store = null;
   private Calendar now = Calendar.getInstance();
   private Calendar start = Calendar.getInstance();
   private Calendar stop = Calendar.getInstance();

   private HashMap<String, CaptureTask> captureTasks = new HashMap<>();

   public TimerThread() throws Exception
   {
      System.out.println("Timer Thread: Created");
      store = DataStore.getInstance();
   }

   @Override
public void run()
   {
      System.out.println("Timer Thread: Started");
      ThreadLock locker = ThreadLock.getInstance();
      CaptureDeviceList devList = CaptureDeviceList.getInstance();

      try
      {
         while (true)
         {
            store.timerStatus = 1;

            locker.getLock();
            try
            {

               //////////////////////////////////////////////////////////////////////////////////////////
               // Look at all the running producers
               // if any of the process are not running then signal a restart for all consumers
               // using that producer

               StreamProducerProcess[] producers = devList.getProducers();
               for (StreamProducerProcess producer : producers) {
                  if(!producer.isProducerRunning())
                  {
                     // this producer is broken, restart the
                     System.out.println("Producer with KEY=" + producer.getKey() + " has been marked for needs restart");
                     producer.setNeedsRestart(true);
                  }
               }

               //
               //////////////////////////////////////////////////////////////////////////////////////////


               //////////////////////////////////////////////////////////////////////////////////////////
               // Look at all the running captures
               //
               store.timerStatus = 2;
               String[] keys = captureTasks.keySet().toArray(new String[0]);

               for (String key : keys) {
                  CaptureTask task = captureTasks.get(key);

                  boolean fin = task.isFinished();

                  if(fin)
                  {
                     store.timerStatus = 3;
                     task.stopCapture();
                     store.timerStatus = 4;
                     captureTasks.remove(key);

                     //Now Send update message to Listening App
                     NowRunningInfo update = new NowRunningInfo(captureTasks);
                     update.writeNowRunning();

                     new DllWrapper().setActiveCount(captureTasks.size());

                     // Force a Garbage Collection when we stop a capture
                     System.gc();
                  }
               }

               //
               //////////////////////////////////////////////////////////////////////////////////////////

               //
               // Now look at all the schedules to see if any need action
               //
               now.setTime(new Date());

               long autoDelSchedTime = 0;
               try
               {
                  autoDelSchedTime = 3600000 * Integer.parseInt(store.getProperty("sch.autodel.time"));
               }
               catch (Exception exp){}

               keys = store.getScheduleKeys();
               Arrays.sort(keys);

               for (String key : keys) {
                  ScheduleItem item = store.getScheduleItem(key);

                  if(item == null)
                  {
                     System.out.println("ERROR for some reason one of your schedule items in the MAP is null : " + key);
                     break;
                  }

                  // only look at schedules that are not in the running list.
                  if(!captureTasks.keySet().contains(item.toString()))
                  {
                     start.setTime(item.getStart());
                     stop.setTime(item.getStop());

                     //
                     // Mark items as skipped if in the past and still waiting
                     //
                     if(item.getState() == ScheduleItem.WAITING && now.after(stop))
                     {
                        item.setState(ScheduleItem.SKIPPED);
                        item.setStatus("skipped");
                        item.log("Marked as skipped");
                        store.saveSchedule(null);
                     }


                     //
                     // Check to see if any Schedule items are to be
                     // autodeleted
                     //
                     else if(autoDelSchedTime > 0 &&
                        item.getState() == ScheduleItem.FINISHED &&
                        (now.getTimeInMillis() - stop.getTimeInMillis()) > autoDelSchedTime)
                     {
                        store.autoDelLogAdd("Auto-Deleting Finished Item: (" + item.getName() +
                           ") (" + item.getStart() + ") (" + item.getDuration() + ")");
                        System.out.println("Removing Old Finished Item: " + key);
                        ScheduleItem removedItem = store.removeScheduleItem(key);
                        store.saveSchedule(null);

                        int autoDelSchedAction = 0;
                        try
                        {
                           autoDelSchedAction = Integer.parseInt(store.getProperty("sch.autodel.action"));
                        }
                        catch (Exception e){}

                        if(autoDelSchedAction == 0 && removedItem != null)
                        {
                           archiveOldItem(removedItem);
                           System.out.println("Item Archived");
                        }
                     }

                     //
                     // for repeating Schedule items create a new
                     // one for the next trigger time
                     //
                     else if(
                        (item.getType() == ScheduleItem.DAILY || item.getType() == ScheduleItem.WEEKLY || item.getType() == ScheduleItem.WEEKDAY || item.getType() == ScheduleItem.MONTHLY)
                        &&
                        (item.getState() == ScheduleItem.FINISHED || item.getState() == ScheduleItem.SKIPPED))
                     {
                        Vector<ScheduleItem> nextInstances = new Vector<>();

                        // this is a parent so look at its children
                        nextInstances.add(item.createNextInstance(now, store.rand));

                        // now if we have a new Instance add it to the schedule list
                        // and merge if needed
                        for (ScheduleItem nextInstance : nextInstances) {
                           store.addScheduleItem(nextInstance);
                        }
                     }

                     //
                     // Here a schedule should not be in RUNNING state so set it
                     // to waiting
                     // if it is past it will be set to skipped, if it is still
                     // current it will be restarted
                     //
                     else if(item.getState() == ScheduleItem.RUNNING)
                     {
                        item.setState(ScheduleItem.WAITING);
                        item.setStatus("Reset");
                        item.log("Running Item marked as Waiting, this should not happen, WS probably crashed.");
                        store.saveSchedule(null);
                     }

                     //
                     // an item should also not be able to get to this point and
                     // still be in the
                     // aborted state so set it to FINISHED.
                     //
                     else if(item.getState() == ScheduleItem.ABORTED)
                     {
                        item.setState(ScheduleItem.FINISHED);
                        item.setStatus("Reset");
                        item.log("Aborted Item marked as Finished, this should not happen.");
                        store.saveSchedule(null);
                     }

                     //
                     // We should never see RESTART items here as they should be reset by
                     // the stop capture call above before getting to this point so if an
                     // item state is RESTART then restart it here.
                     // This usually means WS crashed!
                     //
                     else if(item.getState() == ScheduleItem.RESTART)
                     {
                        item.setState(ScheduleItem.WAITING);
                        item.setStatus("Restarted");
                        item.log("Item Restarted and Set to WAITING by Timer Thread, this should not happen.");
                        store.saveSchedule(null);
                     }

                     //
                     // try to start a capture if it needs starting
                     //
                     else if(item.getState() == ScheduleItem.WAITING && now.after(start) && now.before(stop) && !item.isDealyed())
                     {
                        try
                        {
                           // Force a Garbage Collection before we start
                           System.gc();

                           store.refreshWakeupTime();

                           System.out.println("Number of cards available (" + devList.getDeviceCount() + ") number in use (" + devList.getActiveDeviceCount() + ")");

                           CaptureTask capTask = new CaptureTask(item);
                           item.setState(ScheduleItem.RUNNING);
                           item.setStatus("Starting");

                           store.timerStatus = 7;
                           int startCode = capTask.startCapture();
                           store.timerStatus = 8;

                           if(startCode < 0)
                           {
                              int delayFor = 60;
                              try
                              {
                                 delayFor = Integer.parseInt(store.getProperty("capture.capturefailedtimeout"));
                                 if(delayFor < 60) {
									delayFor = 60;
								 }
                                 if(delayFor > 240) {
									delayFor = 240;
								 }
                              }
                              catch(Exception e){}

                              // delay this item so no action will take place
                              // this is so we don't hammer the system to death
                              item.delayFor(delayFor);
                              item.setStatus("Error Delay");
                              item.log("Schedule failed to start, it will be delayed for " + delayFor + " seconds and then retried.");

                              if(startCode == -3)
                              {
                                 System.out.println("No cards available, Will retry this schedule in " + delayFor + " seconds.");
                              }
                              else
                              {
                                 System.out.println("ERROR starting capture : (" + startCode + ") " + "Will retry this schedule in " + delayFor + " seconds.");
                              }
                           }
                           else
                           {
                              item.setState(ScheduleItem.RUNNING);
                              item.setStatus("Running");
                              item.log("Capture Started");
                              captureTasks.put(item.toString(), capTask);

                              // Now Send update message to Listening App
                              NowRunningInfo update = new NowRunningInfo(captureTasks);
                              update.writeNowRunning();

                              new DllWrapper().setActiveCount(captureTasks.size());
                           }

                           store.saveSchedule(null);

                           //if(inUseCounter > 0)
                           //   System.out.println("In Use Counter = " + inUseCounter);
                        }
                        catch (Exception e)
                        {
                           System.out.println("ERROR running the action task: " + e);
                           e.printStackTrace();
                        }

                        break;
                     }
                  }

               } // end for

            } // lock
            finally
            {
               locker.releaseLock();
            }

            if("1".equals(store.getProperty("server.kbled")))
            {
               new DllWrapper().setKbLEDs(captureTasks.size());
            }

            store.timerStatus = 0;
            Thread.sleep(5000);
         } // end while
      }
      catch (Exception e)
      {
         System.out.println("The main Timer Thread has crashed!");
         e.printStackTrace();
         System.out.println("This is really bad!!!!!");
         store.timerStatus = -1;

         ByteArrayOutputStream ba = new ByteArrayOutputStream();
         PrintWriter err = new PrintWriter(ba);
         e.printStackTrace(err);
         err.flush();
         store.timerThreadErrorStack = ba.toString();

         //System.out.println("System Exiting!");
         //System.exit(-10);
      }
   }

   // archive old items
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

}