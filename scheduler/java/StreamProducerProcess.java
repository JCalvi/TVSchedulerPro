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

public class StreamProducerProcess
{
   private String mapKey = "";
   private Process producerApp = null;
   private Vector<String> responseData = new Vector<>();
   private boolean captureRunning = false;
   private int actionTimeOut = 60;
   private ProcessInputReader reader = null;
   private CaptureDevice capDev = null;
   private int deviceIndex = -1;
   private int usageCount = 0;
   private String stopEvent = "";
   private String memShareName = "";
   private boolean needsRestart = false;

   public StreamProducerProcess(CaptureDevice cap, int devIndex)
   {
      deviceIndex = devIndex;
      capDev = cap;
   }

   public void setNeedsRestart(boolean value)
   {
      needsRestart = value;
   }

   public boolean getNeedsRestart()
   {
      return needsRestart;
   }

   public String getMemoryShareName()
   {
      return memShareName;
   }

   public void setKey(String key)
   {
      mapKey = key;
   }

   public String getKey()
   {
      return mapKey;
   }

   public int addUsageCount()
   {
      usageCount++;
      return usageCount;
   }

   public int decUsageCount()
   {
      usageCount--;
      return usageCount;
   }

   public int getUsageCount()
   {
      return usageCount;
   }

   public int getDeviceIndex()
   {
      return deviceIndex;
   }

   public CaptureDevice getCaptureDevice()
   {
      return capDev;
   }

   public int startProducer(int freq, int bandWidth, StringBuffer log, Vector<String> logFilesNames)
   {
      if(producerApp != null)
      {
         System.out.println("Producer : Tried to start StreamProducer while one was already running!");
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
            command[0] = "win/StreamProducer.exe";
            command[1] = "-test";
         }
         else
         {
            command = new String[4];
            command[0] = "win/StreamProducer.exe";
            command[1] = Integer.valueOf(freq).toString();
            command[2] = Integer.valueOf(bandWidth).toString();
            command[3] = capDev.getID();
         }

         ProcessBuilder builder = new ProcessBuilder(command);
         producerApp = builder.start();
      }
      catch(Exception e)
      {
         System.out.println("Producer : Error launching external StreamProducer app!");
         e.printStackTrace();
         return -2;
      }

      try
      {
         // now start up a thread to read response data
         reader = new ProcessInputReader(producerApp, responseData, "Producer:" + this.toString());
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
                  System.out.println("Producer Log File " + resp);
                  logFilesNames.add(resp.substring("LOG_FILE:".length()).trim());
               }

               if(resp.startsWith("LOG:"))
               {
                  System.out.println("Producer Start " + resp);
                  log.append(resp.substring(4).trim() + "\n");
               }

               if(resp.startsWith("STOP_EVENT:"))
               {
                  stopEvent = resp.substring("STOP_EVENT:".length()).trim();
                  System.out.println("Producer : Stop Event String = " + stopEvent);
               }

               if(resp.startsWith("SHARE_NAME:"))
               {
                  memShareName = resp.substring("SHARE_NAME:".length()).trim();
                  System.out.println("Producer : Memory Share Name = " + memShareName);
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
               producerApp.destroy();  //Calvi added
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
               System.out.println("Producer : Timeout reached when starting StreamProducer external app!");
               producerApp.destroy();
               captureRunning = false;
               return -3;
            }
         }
      }
      catch(Exception e)
      {
         System.out.println("Producer : Error waiting for StreamProducer to start");
         e.printStackTrace();

         captureRunning = false;

         try
         {
            if(producerApp != null) {
				producerApp.destroy();
			}
         }
         catch(Exception e2){}

         return -4;
      }

      return 0;
   }

   public int stopProducer()
   {
      if(reader == null || reader.getExitCode() != null) {
		return -1;
	  }

      try
      {
         System.out.println("Producer : Sending stop command to external app");
         if(stopEvent.length() == 0)
         {
            System.out.println("Producer : Stop Event not valid, destroy process!");
            producerApp.destroy();
            return -4;
         }

         int eventSend = new DllWrapper().setEvent(stopEvent);
         if(eventSend != 0)
         {
            System.out.println("Producer : Event Sent Failed (" + eventSend + "), destroying process!");
            producerApp.destroy();
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
               System.out.println("Producer : Timeout reached when stopping external StreamProducer app!");
               producerApp.destroy();
               return -2;
            }
         }
      }
      catch(Exception e)
      {
         System.out.println("Producer : Error trying to stop StreamProducer!");
         e.printStackTrace();
         return -3;
      }
      finally
      {
         try
         {
            if(producerApp != null) {
				producerApp.destroy();
			}
         }
         catch(Exception e){}

         captureRunning = false;
         producerApp = null;
      }

      return 0;
   }

   public Vector<String> getresponseData()
   {
      return responseData;
   }

   public boolean isProducerRunning()
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
