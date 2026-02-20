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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Scheduler
{
   private static int port = 8420;

   static boolean logToFile = false;
   static String portNum = "";
   static boolean dataDirCreated = false;

   public static void main(String[] args)
   {
      Runtime.getRuntime().addShutdownHook(new ShutDownHook());

      try
      {
         parseArgs(args);

         //Set first datapath to ProgramData Folder
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         File dataDir = new File(dataPath);
         if(!dataDir.exists() || !dataDir.isDirectory()) {
			dataDirCreated = new File(dataPath).mkdir();
		 }

         deleteOldLogs();

         // if started with -log
         // redirect the output to the log file
         if(logToFile)
         {
            File outStream = getLogFile();
            CustomLogPrintStream log = new CustomLogPrintStream(new FileOutputStream(outStream, true));

            System.out.println("******************************************************");
            System.out.println("TV Scheduler Pro Starting : " + new Date().toString() );
            System.out.println("******************************************************");
            System.out.println("For errors see the log files in the log directory:\n");

            log.println("");
            log.println("******************************************************");
            log.println("TV Scheduler Pro Starting : " + new Date().toString() );
            log.println("******************************************************");

            System.setOut(log);
            System.setErr(log);
         }

         printSystemProp();

         System.out.println("Application Data Path : " + dataPath);
         DataStore store = DataStore.getInstance();
         System.out.println(store.toString());
         CaptureDeviceList devList = CaptureDeviceList.getInstance();
         System.out.println(devList.toString());
         GuideStore guide = GuideStore.getInstance();
         System.out.println(guide.toString());
         CaptureCapabilities caps = CaptureCapabilities.getInstance();
         System.out.println(caps.toString());
         AccessControl ac = AccessControl.getInstance();
         //System.out.println(ac.toString()); //This is logged in the class above when called.

         // get the port number from the command line or use 8420
         if(portNum.length() > 0) {
			port = Integer.parseInt(portNum);
		 } else {
			port = 8420;
		 }

         new DllWrapper().setCurrentPort(port);

         TimerThread timer = new TimerThread();
         Thread timerThread = new Thread(Thread.currentThread().getThreadGroup(), timer, timer.getClass().getName());
         timerThread.start();

         AdminThread admin = new AdminThread();
         Thread adminThread = new Thread(Thread.currentThread().getThreadGroup(), admin, admin.getClass().getName());
         adminThread.start();

         ServerSocket listener = new ServerSocket(port);
         Socket server;

         System.out.println("Starting server listening on (" + listener.getInetAddress().toString() + ":" + port + ")");

         while(true)
         {
            server = listener.accept();
            RequestObject con = new RequestObject(server);
            Thread conThread = new Thread(Thread.currentThread().getThreadGroup(), con, con.getClass().getName());
            conThread.start();
         }
      }
      catch (Exception e)
      {
         System.out.println("Base server Exception: " + e);
         e.printStackTrace();
         System.exit(0);
      }
   }

   private static void parseArgs(String[] args)
   {
      for(int x = 0; x < args.length; x++)
      {
         if(args[x].equals("-log"))
         {
            logToFile = true;
         }
         else if(x < args.length-1 && args[x].equals("-port"))
         {
            String tempPort = args[x+1];
            if(!tempPort.startsWith("-"))
            {
               portNum = tempPort;
               x++;
            }
         }
      }
   }

   private static File getLogFile()
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss-S");
      String fileName = dataPath + File.separator +  "log" + File.separator + "Scheduler-" + df.format(new Date()) + ".log";
      File log = new File(fileName);
      File parent = log.getParentFile();
      if(!parent.exists())
      {
         parent.mkdirs();
      }

      return log;
   }

   private static void deleteOldLogs()
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";

      File logDir = new File(dataPath + File.separator + "log");

      if(!logDir.exists())
      {
         System.out.println("Log file directory does not exist!");
         return;
      }

      File[] logFiles = logDir.listFiles();
      long ageLimit = (new Date().getTime()) - (1000 * 60 * 60 * 24 * 21); //3 weeks

      for (File logFile : logFiles) {
         String name = logFile.getName();
         if(name.length() > 4 && name.substring(name.length() - 4, name.length()).equalsIgnoreCase(".log"))
         {
            if(logFile.lastModified() < ageLimit)
            {
               try
               {
                  System.out.println("Deleting old log file : " + name);
                  logFile.delete();
               }
               catch(Exception e)
               {
                  System.out.println("Could not delete old log file : " + name);
               }
            }
         }
      }
   }

   public static void printSystemProp()
   {
      Properties prop = System.getProperties();

      String[] keys = prop.keySet().toArray(new String[0]);

      System.out.println("System Info");
      System.out.println("--------------------------------------------------------------------------");
      System.out.println("Current Time: " + new Date().toString());
      for (String key : keys) {
         System.out.println(key + " : " + System.getProperty(key));
      }
      System.out.println("--------------------------------------------------------------------------");
   }

}
