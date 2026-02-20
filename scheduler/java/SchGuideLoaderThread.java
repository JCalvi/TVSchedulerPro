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

import java.util.Date;

class SchGuideLoaderThread implements Runnable
{
   DataStore store = null;

   public SchGuideLoaderThread()
   {
      try
      {
         store = DataStore.getInstance();
      }
      catch(Exception e)
      {
         System.out.println("ERROR: Exception in Scheduled Guide Data Loader");
         e.printStackTrace();
      }
   }

   @Override
public void run()
   {
      System.out.println("SchGuideLoaderThread, started : " + new Date().toString());

      try
      {
         StringBuffer buff = new StringBuffer();
         int exitCode = 0;

         // run pretask action
         String preLoadTask = store.getProperty("guide.source.schedule.pretask");
         if(preLoadTask != null && preLoadTask.length() > 0)
         {
            System.out.println("Loading Pre EPG Data Load Task (" + preLoadTask + ")");
            buff.append("Loading Pre EPG Data Load Task (" + preLoadTask + ")");
            TaskCommand taskCommand = store.getTaskList().get(preLoadTask);
            if(taskCommand != null)
            {
               exitCode = runPreTask(taskCommand, buff);
               buff.append("Pre Load Task Finished\n\n");
               System.out.println("Pre Load Task Finished");
            }
            else
            {
               buff.append("Pre Load Task not found!\n\n");
               System.out.println("Pre Load Task not found");
            }
         }
         else
         {
            buff.append("No Pre Load Task set\n\n");
            System.out.println("No Pre Load Task set");
         }

         if(exitCode != 0)
         {
            buff.append("Exit code is not zero, load action aborted\n\n");
            System.out.println("Exit code is not zero, load action aborted");
         }

         // now do data load
         GuideStore guide = GuideStore.getInstance();

         boolean loadWorked = guide.loadXMLTV(buff, 0);

         if(loadWorked)
         {
            System.out.println("SchGuideLoaderThread, waiting for global instance lock");
            ThreadLock.getInstance().getLock();
            try
            {
               System.out.println("SchGuideLoaderThread, Got Lock");
               store.removeEPGitems(buff, 0);
               guide.addEPGmatches(buff, 0);
            }
            finally
            {
               ThreadLock.getInstance().releaseLock();
            }
         }
         else
         {
            buff.append("EPG Data Load Failed!\n");
         }

         store.saveSchedule(null);

         //
         // Now send email if required
         //
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
            catch(Exception e)
            {
               e.printStackTrace();
            }
         }
         System.out.println("SchGuideLoaderThread exiting");
      }
      catch(Exception e)
      {
         System.out.println("ERROR: Exception in Scheduled Guide Data Loader");
         e.printStackTrace();
      }
   }

   private int runPreTask(TaskCommand taskCommand, StringBuffer buff) throws Exception
   {
      if(!taskCommand.getEnabled())
      {
         buff.append("Task is disabled.\n");
         System.out.println(this + " : Task (" + taskCommand.getName() + ") is disabled.");
         return 0;
      }

      if(taskCommand.getDelay() > 0)
      {
         buff.append("The command has a delayed start of " + taskCommand.getDelay() + " seconds\n");
         System.out.println(this + " : The command has a delayed start of " + taskCommand.getDelay() + " seconds");
      }

      TaskItemThread taskItem = new TaskItemThread(taskCommand, new CommandWaitThread(taskCommand.getCommand()), null);
      Thread taskThread = new Thread(Thread.currentThread().getThreadGroup(), taskItem, taskItem.getClass().getName());
      taskThread.start();

      int exitCode = -1;
      long timeout = 360;
      long started = new Date().getTime();
      while (true)
      {
         if(taskItem.isFinished())
         {
            buff.append("Command finished normally");
            System.out.println(this + " : Command finished normally");
            break;
         }
         else if(((new Date().getTime()) - started) > (1000 * 60 * timeout))
         {
            buff.append("Command timed out after " + timeout + " minutes of waiting, killing it now\n");
            System.out.println(this + " : Command timed out after " + timeout + " minutes, killing it now");
            taskItem.logCommand("Timed out after " + timeout + " minutes of waiting\n");
            taskItem.logCommand("EPG data load aborted\n");
            taskItem.stop();
            exitCode = -999;
            break;
         }
         System.out.println(this + " : Waiting for command to finish.....");
         Thread.sleep(10000);
      }

      buff.append("Task finished, Output of task follows:\n");
      buff.append("*****************************\n");

      System.out.println(this + " : Pre Task finished, Output of task follows");
      System.out.println(this + " : *****************************");

      buff.append("Standard Out:\n" + taskItem.getOutput() + "\n\n");
      System.out.println(this + " : Standard Out:\n" + taskItem.getOutput());
      buff.append("Standard Error:\n" + taskItem.getError() + "\n\n");
      System.out.println(this + " : Standard Error:\n" + taskItem.getError());

      buff.append("*****************************\n");
      System.out.println(this + " : *****************************");

      if(exitCode != -999)
      {
         exitCode = taskItem.getExitCode();
         if(exitCode != 0) {
			taskItem.logCommand("Return code is not zero, EPG data load aborted\n");
		 }
      }

      return exitCode;
   }
}
