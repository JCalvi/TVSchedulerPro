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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class TaskItemThread implements Runnable
{
   private CommandWaitThread command = null;
   private StringBuffer control = new StringBuffer();
   private TaskCommand taskDetails = null;
   private String itemKey = null;
   private Date created = null;
   private File targetFile = null;
   private boolean finished = false;
   private int delay = 0;
   private int decAmout = 0;
   private SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
   private DataStore store = null;
   private String status = "Waiting(0)";
   private Date commandStarted = null;
   private Date commandFinished = null;
   @SuppressWarnings("this-escape")
   public TaskItemThread(TaskCommand taskCommand, CommandWaitThread task, File target)
   {
      command = task;
      taskDetails = taskCommand;
      targetFile = target;
      created = new Date();

      store = DataStore.getInstance();

      itemKey = Long.valueOf(new Date().getTime()).toString() + "-" + Long.valueOf(store.rand.nextLong()).toString();
      store.runningTaskList.put(itemKey, this);

      control.append("Task   : " + taskCommand.getName() + "\n");
      control.append("Create : " + df.format(created) + "\n");
      control.append("(" + task.getCommand() + ")\n\n");
   }

   public String getTaskName()
   {
      return taskDetails.getName();
   }

   public void logCommand(String logData)
   {
      logData = df.format(new Date()) + " :- " + logData;
      control.append(logData);
   }

   public String getTargetFile()
   {
      if(targetFile != null)
      {
         return targetFile.getName();
      }
      else
      {
         return "NULL";
      }
   }

   public String getStatus()
   {
      return status;
   }

   public Date getCreationDate()
   {
      return created;
   }

   public void stop()
   {
      logCommand("Killing Command\n");
      System.out.println("Killing Command");
      command.stop();
      finished = true;
   }

   public boolean isFinished()
   {
      return command.isFinished();
   }

   public String getOutput()
   {
      return command.getInputStream();
   }

   public String getError()
   {
      return command.getErrorStream();
   }

   public String getControl()
   {
      return control.toString();
   }

   public int getDelayLeft()
   {
      return delay;
   }

   public int getExitCode()
   {
      return command.getExitCode().intValue();
   }

   @Override
public void run()
   {
      delay = taskDetails.getDelay();

      try
      {
         if(delay > 0)
         {
            status = "Delaying";
            logCommand("Command is delaying for " + taskDetails.getDelay() + " seconds\n");
         }

         while(delay > 0 && !finished)
         {
            Thread.sleep(1000);
            delay--;
         }

         if(taskDetails.getDelay() > 0) {
			logCommand("Delay over\n");
		 }

         if(finished)
         {
            status = "Finished";
            logCommand("Exiting before running command\n");
            return;
         }

         // can we run
         boolean firstTime = true;
         while(!finished && !canRun())
         {
            if(firstTime)
            {
               firstTime = false;
               logCommand("Waiting for chance to run\n");
            }
            Thread.sleep(10000);
         }

         if(finished)
         {
            status = "Finished";
            logCommand("Exiting before running command\n");
            return;
         }

         status = "Running";
         logCommand("Executing command\n");

         // start the command
         commandStarted = new Date();
         Thread comWait = new Thread(Thread.currentThread().getThreadGroup(), command, command.getClass().getName());
         comWait.start();

         int reads = 0;
         while(true)
         {
            if(command.isFinished() && reads++ > 0)
            {
               break;
            }

            Thread.sleep(1000);
         }

         commandFinished = new Date();

      }
      catch(Exception e)
      {
         System.out.println("Exception running task:");
         e.printStackTrace();
         command.stop();
      }
      finally
      {
         // dec active count
         decCounter();

         status = "Finished";
         finished = true;

         try
         {
            if(taskDetails.getAutoRemove())
            {
               System.out.println("Auto Removing task from task list : " + itemKey);
               store.runningTaskList.remove(itemKey);
            }
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }

      logCommand("External app exited with : " + command.getExitCode() + "\n");
      logCommand("Command finished\n");
      System.out.println("Command finished");

      // calc total running time
      if(commandStarted != null && commandFinished != null)
      {
         long timeTaken = commandFinished.getTime() - commandStarted.getTime();
         logCommand("Time taken = " + timeFromLong(timeTaken));
      }

      archiveOldItem();
   }

   // archive old items
   private void archiveOldItem()
   {
      try
      {
         Date fileDate = commandStarted;
         if(commandStarted != null) {
			fileDate = new Date();
		 }

         SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd@HHmmssS");
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         String archiveName = dataPath + File.separator + "archive" + File.separator + "Task-" + df.format(fileDate) +
            " (" + taskDetails.getName() + ").sof";

         System.out.println("Archiving finished Task to : " + archiveName);

         File outFile = new File(archiveName);
         outFile = outFile.getCanonicalFile();
         File parent = outFile.getParentFile();
         if(!parent.exists()) {
			parent.mkdirs();
		 }

         HashMap<String, String> taskDetails = new HashMap<>();
         taskDetails.put("control", control.toString());
         taskDetails.put("stdout", command.getInputStream());
         taskDetails.put("stderr", command.getErrorStream());

         FileOutputStream fos = new FileOutputStream(outFile);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(taskDetails);
         oos.close();
      }
      catch(Exception e)
      {
         System.out.println("ERROR trying to archive old Task Item:");
         e.printStackTrace();
      }
   }

   private String timeFromLong(long val)
   {
      NumberFormat  nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(0);
      nf.setMaximumIntegerDigits(2);
      nf.setMinimumFractionDigits(0);
      nf.setMinimumIntegerDigits(2);

      int hours   = (int)((double)val / (double)(1000 * 60 * 60));
      int minutes = (int)((double)(val - (hours * 1000 * 60 * 60)) / (double)(1000 * 60));
      int seconds = (int)((double)(val - (minutes * 1000 * 60)) / (double)(1000));

      String timeString = nf.format(hours) + ":" + nf.format(minutes) + ":" + nf.format(seconds);// + " (" + val + ")";

      return timeString;
   }

   private synchronized void decCounter()
   {
      store.activeTaskCount += decAmout;
   }

   private synchronized boolean canRun()
   {

      // if this task is the preTask, or any of the Error tasks then dont do
      // a concurrency, not while running or not within check

      String taskName = taskDetails.getName();
      String preTaskName = store.getProperty("tasks.pretask");
      String startErrorTaskName = store.getProperty("tasks.starterrortask");
      String noDataErrorTaskName = store.getProperty("tasks.nodataerrortask");
      String captureDetailsTaskName = store.getProperty("tasks.capturedetailstask");

      if(   taskName.equals(preTaskName) ||
            taskName.equals(startErrorTaskName) ||
            taskName.equals(noDataErrorTaskName))
      {
         logCommand("This is a pre or error task so skipping can Run checks.\n");
         return true;
      }


      // do not do run check while timer thread is doing stuff
      ThreadLock locker = ThreadLock.getInstance();

      locker.getLock();
      try
      {
         // concurrent task check
         if(store.activeTaskCount >= taskDetails.getConcurrent())
         {
            status = "Waiting(Con)";
            return false;
         }

         // check for active schedules
         CaptureDeviceList devList = CaptureDeviceList.getInstance();
         if(taskDetails.getWhenNotCapturing() && devList.getActiveDeviceCount() > 0)
         {
            status = "Waiting(Act)";
            return false;
         }

         // check time to next schedule
         ScheduleItem next = store.getNextSchedule();
         if(next != null)
         {
            Date now = new Date();
            int timeToNext = (int)((next.getStart().getTime() - now.getTime()) / (1000 * 60));
            //System.out.println("Time to next:" + timeToNext + " to next:" + taskDetails.getTimeToNextSchedule());
            if(taskDetails.getTimeToNextSchedule() > 0 && timeToNext < taskDetails.getTimeToNextSchedule())
            {
               status = "Waiting(next)";
               return false;
            }
         }

         // we can run so return
         decAmout = -1;
         store.activeTaskCount++;
         return true;
      }
      finally
      {
         locker.releaseLock();
      }
   }

}
