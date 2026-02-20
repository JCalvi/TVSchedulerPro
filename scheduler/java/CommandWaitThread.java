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
import java.io.InputStream;
import java.io.PrintWriter;

public class CommandWaitThread implements Runnable
{
   private String command = "";
   private Process process = null;
   private boolean finished = false;

   private StringBuffer out = new StringBuffer();
   private StringBuffer err = new StringBuffer();

   private Integer exitCode = null;

   public CommandWaitThread(String com)
   {
      command = com;
   }

   public String getCommand()
   {
      return command;
   }

   public boolean isFinished()
   {
      return this.finished;
   }

   public String getInputStream()
   {
      return out.toString();
   }

   public String getErrorStream()
   {
      return err.toString();
   }

   public Integer getExitCode()
   {
      return exitCode;
   }

   public void stop()
   {
      try
      {
         if(process != null) {
			process.destroy();
		 }
      }
      catch(Exception e){}

      this.finished = true;
   }

   @Override
public void run()
   {
      try
      {
         if(!this.finished)
         {
            Runtime runner = Runtime.getRuntime();
			//Split by spaces unless in quotes, after removing leading quote if exists.
			String[] cmdArray = command.replaceAll("^\"", "").split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?");
            //process = runner.exec(command); //JC Deprecated
            process = runner.exec(cmdArray);
            new ProcessInputReaderThread(process.getInputStream(), out);
            new ProcessInputReaderThread(process.getErrorStream(), err);

            exitCode = Integer.valueOf(process.waitFor());
         }
      }
      catch(Exception e)
      {
         err.append("Error running command.\n");
         ByteArrayOutputStream ba = new ByteArrayOutputStream();
         PrintWriter errData = new PrintWriter(ba);
         e.printStackTrace(errData);
         errData.flush();
         err.append(ba.toString());
         e.printStackTrace();
      }

      this.finished = true;
   }

   private class ProcessInputReaderThread implements Runnable
   {
      InputStream stream = null;
      StringBuffer data = null;

      public ProcessInputReaderThread(InputStream in, StringBuffer buff)
      {
         stream = in;
         data = buff;

         new Thread(Thread.currentThread().getThreadGroup(), this , this.getClass().getName()).start();
      }

      @Override
	public void run()
      {
         try
         {
            int ch;
            while((ch = stream.read()) != -1)
            {
               data.append((char)ch);
            }
         }
         catch (Exception e){}
         //System.out.println("ProcessInputReaderThread Exiting");
      }
   }
}