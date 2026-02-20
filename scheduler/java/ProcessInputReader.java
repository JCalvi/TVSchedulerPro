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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

public class ProcessInputReader implements Runnable
{
   private Process process = null;
   private Vector<String> commandQue = null;
   private BufferedReader in = null;
   private Integer exitCode = null;
   private String ProcessID = "";

   public ProcessInputReader(Process proc, Vector<String> que, String ID)
   {
      ProcessID = ID;
      process = proc;
      commandQue = que;
      in = new BufferedReader(new InputStreamReader(process.getInputStream()));
   }

   @Override
public void run()
   {
      try
      {
         String line = in.readLine();
         while(line != null)
         {
            if(line.length() > 0)
            {
               commandQue.add(line);
               //System.out.println(line);
            }
            line = in.readLine();
         }

         int code = process.waitFor();
         exitCode = Integer.valueOf(code);
         System.out.println("External app (" + ProcessID + ") exited with code : " + exitCode);
      }
      catch(Exception e)
      {
         System.out.println("ERROR in ProcessInputReader! (" + ProcessID + ")");
         e.printStackTrace();
      }
      System.out.println("ProcessInputReader Thread Exiting (" + ProcessID + ")");
   }

   public Integer getExitCode()
   {
      return exitCode;
   }
}