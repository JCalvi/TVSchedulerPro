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

import java.util.Vector;

public class StreamConsumerProcess
{
   private Process consumerApp = null;
   private Vector<String> responseData = new Vector<>();
   private boolean captureRunning = false;
   private int actionTimeOut = 60;
   private ProcessInputReader reader = null;
   private String stopEvent = "";

   public StreamConsumerProcess()
   {
   }

   public int startConsumer(String memoryShareName, int pPID, int vPID, int aPID, int aType, int type, String fileName, StringBuffer log, Vector<String> logFilesNames)
   {
      if(consumerApp != null)
      {
         System.out.println("Consumer : Tried to start StreamConsumer while one was already running!");
         return -1;
      }

      try
      {
         DataStore store = DataStore.getInstance();
         boolean testMode = "1".equals(store.getProperty("tools.testmode"));

         String command[] = null;

         if(testMode)
         {
            command = new String[2];
            command[0] = "win/StreamConsumer.exe";
            command[1] = "-test";
         }
         else
         {
            command = new String[8];
            command[0] = "win/StreamConsumer.exe";
            command[1] = memoryShareName;
            command[2] = Integer.valueOf(pPID).toString();
            command[3] = Integer.valueOf(vPID).toString();
            command[4] = Integer.valueOf(aPID).toString();
            command[5] = Integer.valueOf(aType).toString();
            command[6] = Integer.valueOf(type).toString();
            command[7] = fileName;
         }

         ProcessBuilder builder = new ProcessBuilder(command);
         consumerApp = builder.start();
      }
      catch(Exception e)
      {
         System.out.println("Consumer : Error launching external StreamConsumer app!");
         e.printStackTrace();
         return -2;
      }

      try
      {
         // now start up a thread to read response data
         reader = new ProcessInputReader(consumerApp, responseData, "Consumer:" + this.toString());
         Thread monitor = new Thread(Thread.currentThread().getThreadGroup(), reader, reader.getClass().getName());
         monitor.start();

         int timeOut = actionTimeOut;

         while(true)
         {
            if(responseData.size() > 0)
            {
               String resp = responseData.remove(0);

               if(resp.startsWith("LOG_FILE:"))
               {
                  System.out.println("Consumer Log File " + resp);
                  logFilesNames.add(resp.substring("LOG_FILE:".length()).trim());
               }

               if(resp.startsWith("LOG:"))
               {
                  System.out.println("Consumer Start " + resp);
                  log.append(resp.substring("LOG:".length()).trim() + "\n");
               }

               if(resp.startsWith("STOP_EVENT:"))
               {
                  stopEvent = resp.substring("STOP_EVENT:".length()).trim();
                  System.out.println("Consumer : Stop Event String = " + stopEvent);
               }

               if("GRAPH_RUNNING".equals(resp.trim()))
               {
                  captureRunning = true;
                  break;
               }
            }
            else if(reader.getExitCode() != null)
            {
               captureRunning = false;
               int code = reader.getExitCode().intValue();
               consumerApp.destroy(); //Calvi added
               return code - 100;
            }
            else
            {
               try
               {
                  Thread.sleep(1000);
                  timeOut--;
               }
               catch(Exception e){}
            }

            if(timeOut == 0)
            {
               System.out.println("Consumer : Timeout reached when starting StreamConsumer external app!");
               consumerApp.destroy();
               captureRunning = false;
               return -3;
            }
         }
      }
      catch(Exception e)
      {
         System.out.println("Consumer : Error waiting for StreamConsumer to start");
         e.printStackTrace();

         captureRunning = false;

         try
         {
            if(consumerApp != null) {
				consumerApp.destroy();
			}
         }
         catch(Exception e2){}

         return -4;
      }

      return 0;
   }

   public int stopConsumer()
   {
      if(reader == null || reader.getExitCode() != null) {
		return -1;
	  }

      try
      {
         System.out.println("Consumer : Sending stop command to external app");
         if(stopEvent.length() == 0)
         {
            System.out.println("Consumer : Stop Event not valid, destroy process!");
            consumerApp.destroy();
            return -4;
         }

         int eventSend = new DllWrapper().setEvent(stopEvent);
         if(eventSend != 0)
         {
            System.out.println("Consumer : Event Sent Failed (" + eventSend + "), destroying process!");
            consumerApp.destroy();
            return -5;
         }

         int timeOut = actionTimeOut;

         while(true)
         {
            if(responseData.size() > 0)
            {
               String resp = responseData.remove(0);
               //System.out.println(resp);

               if("SYSTEM_EXITING".equals(resp.trim()))
               {
                  break;
               }
            }
            else if(reader.getExitCode() != null)
            {
               break;
            }
            else
            {
               try
               {
                  Thread.sleep(1000);
                  timeOut--;
               }
               catch(Exception e){}
            }

            if(timeOut == 0)
            {
               System.out.println("Consumer : Timeout reached when stopping external StreamConsumer app!");
               consumerApp.destroy();
               return -2;
            }
         }
      }
      catch(Exception e)
      {
         System.out.println("Consumer : Error trying to stop StreamConsumer!");
         e.printStackTrace();
         return -3;
      }
      finally
      {
         try
         {
            if(consumerApp != null) {
				consumerApp.destroy();
			}
         }
         catch(Exception e){}

         captureRunning = false;
         consumerApp = null;
      }

      return 0;
   }

   public Vector<String> getresponseData()
   {
      return responseData;
   }

   public boolean isConsumerRunning()
   {
      if(reader != null && reader.getExitCode() == null && captureRunning) {
		return true;
	  } else {
		return false;
	  }
   }

   public Integer getExitCode()
   {
      return reader.getExitCode();
   }
}
