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
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;

class FileManagementDataRes extends HTTPResponse
{
   public FileManagementDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if("01".equals(urlData.getParameter("action")))
      {
         outStream.write(deleteFile(urlData));
         return;
      }
      else if("02".equals(urlData.getParameter("action")))
      {
         outStream.write(showCommandList(urlData));
         return;
      }
      else if("03".equals(urlData.getParameter("action")))
      {
         outStream.write(runCommand(urlData));
         return;
      }

      outStream.write(showFileList(urlData));
   }

   private byte[] showFileList(HTTPurl urlData) throws Exception
   {
      String pathString = urlData.getParameter("path");
      File[] files = null;
      File baseDir = null;
      boolean inBounds = false;
      String[] paths = store.getCapturePaths();
      DllWrapper capEng = new DllWrapper();

      boolean showPlay = store.getProperty("filebrowser.showwsPlay").equals("1");

      if(pathString != null)
      {
         File thisPath = new File(pathString);
         String requestedFilePath = thisPath.getCanonicalPath();
         for (String path : paths) {
            String rootFilePath = new File(path).getCanonicalPath();

            if(requestedFilePath.indexOf(rootFilePath) == 0)
            {
               inBounds = true;
               break;
            }
         }
      }

      String tableHeader = "";

      if(!inBounds)
      {
         tableHeader = "<tr><td class='itemheading' nowrap><Strong>Capture Paths</Strong></td></tr>\n";
         files = new File[paths.length];
         for(int x = 0; x < paths.length; x++)
         {
            files[x] = new File(paths[x]);
         }
      }
      else
      {
         baseDir = new File(pathString);
         String fileMasks = DataStore.getInstance().getProperty("filebrowser.masks");
         files = baseDir.listFiles(new FileTypeFilter(fileMasks));
      }

      StringBuffer buff = new StringBuffer(2048);
      NumberFormat nf = NumberFormat.getInstance();
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "FileList.html");

      boolean dirsAtTop = "1".equals(store.getProperty("filebrowser.dirsattop"));
      Arrays.sort(files, new CompareFiles(dirsAtTop));

      // put in parent link
      if(baseDir != null)
      {
         File parent = baseDir.getParentFile();
         if(parent == null) {
			buff.append("<tr><td colspan='3'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "'>");
		 } else {
			buff.append("<tr><td colspan='3'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(baseDir.getParentFile().getCanonicalPath(), "UTF-8") + "'>");
		 }
         buff.append("<img border=0 src='/images/back.png' align='absmiddle' width='24' height='24'> Back</a></strong></td></tr>\n");
      }

      for (File file : files) {
         if(!inBounds)
         {
            buff.append("<tr><td nowrap colspan='3' class='separator'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(file.getCanonicalPath(), "UTF-8") + "'>");
            buff.append(file.getCanonicalPath() + "</a> ");
            long freeSpace = capEng.getFreeSpace(file.getCanonicalPath());
            buff.append("(" + nf.format((freeSpace / (1024 * 1024))) + " MB Free)");
            buff.append("</strong></td></tr>\n");
         }
         else if(file.isDirectory() && !file.isHidden())
         {
            buff.append("<tr><td colspan='2' nowrap width='95%' class='separator'><strong><a style='text-decoration: none; color: #FFFFFF;' href='/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(file.getCanonicalPath(), "UTF-8") + "'>");
            buff.append("&lt;" + file.getName() + "&gt;</a></strong></td>");
            buff.append("<td nowrap class='separator'><a class='noUnder' onClick='return confirmAction(\"" + URLEncoder.encode(file.getPath(), "UTF-8") + "\");' href='#'><img align='absmiddle' src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a></td></tr>\n");
         }
         else if(!file.isHidden())
         {
            buff.append("<tr><td nowrap width='95%' class='separator'>" + file.getName() + "</td><td nowrap class='separator'>" + nf.format(file.length()/1024) + " KB</td>");
            buff.append("<td nowrap class='separator'><a class='noUnder' onClick='return confirmAction(\"" + URLEncoder.encode(file.getPath(), "UTF-8") + "\");' href='#'><img align='absmiddle' src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a> ");
            buff.append("<a class='noUnder' href='/servlet/" + urlData.getServletClass() + "?action=02&file=" + URLEncoder.encode(file.getPath(), "UTF-8") + "'><img align='absmiddle' src='/images/RunTask.png' border='0' alt='Run Task' width='24' height='24'></a> ");

            if(showPlay) {
				buff.append("<a class='noUnder' href='wsplay://ws/" + URLEncoder.encode(file.getPath(), "UTF-8") + "'><img align='absmiddle' src='/images/play.png' border='0' alt='Play file using wsplay protocol' width='24' height='24'></a> ");
			}

            buff.append("</td></tr>\n");
         }

      }

      template.replaceAll("$tableHeader$", tableHeader);
      template.replaceAll("$fileList$", buff.toString());
      return template.getPageBytes();
   }

   private byte[] deleteFile(HTTPurl urlData) throws Exception
   {
      File thisFile = new File(urlData.getParameter("file"));

      String requestedFilePath = thisFile.getCanonicalPath();
      boolean inBounds = false;
      String[] paths = store.getCapturePaths();
      for (String path : paths) {
         String rootFilePath = new File(path).getCanonicalPath();

         if(requestedFilePath.indexOf(rootFilePath) == 0)
         {
            inBounds = true;
            break;
         }
      }

      // check requested file in still within the capture dir
      if(!inBounds)
      {
         throw new Exception("File out of bounds!");
      }

      if(thisFile != null && thisFile.exists())
      {
         System.out.println("Deleting File : " + thisFile.getName());
         thisFile.delete();
      }

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/FileManagementDataRes?path=" + URLEncoder.encode(thisFile.getParentFile().getAbsolutePath(), "UTF-8") + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] showCommandList(HTTPurl urlData) throws Exception
   {
      StringBuffer out = new StringBuffer(256);
      File file = new File(urlData.getParameter("file"));

      HashMap<String, TaskCommand> tasks = store.getTaskList();
      String[] keys = tasks.keySet().toArray(new String[0]);

      Arrays.sort(keys);

      for (String key : keys) {
         TaskCommand command = tasks.get(key);

         if(command != null && command.getEnabled())
         {
            out.append("<tr><td class='itemheading'><a class='noUnder' href='/servlet/" + urlData.getServletClass());
            out.append("?action=03&file=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8") + "&command=" + URLEncoder.encode(key, "UTF-8"));
            out.append("'><strong><center>" + key + "</center></strong></a></td><tr>\n");
         }
      }

      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "CommandList.html");
      template.replaceAll("$commandList", out.toString());
      template.replaceAll("$filename", file.getName());
      template.replaceAll("$backURL$", "/servlet/FileManagementDataRes?path=" + URLEncoder.encode(file.getParentFile().getAbsolutePath(), "UTF-8"));

      return template.getPageBytes();
   }

   private byte[] runCommand(HTTPurl urlData) throws Exception
   {
      String commandID = urlData.getParameter("command");

      File thisFile = new File(urlData.getParameter("file"));

      String requestedFilePath = thisFile.getCanonicalPath();
      boolean inBounds = false;
      String[] paths = store.getCapturePaths();
      for (String path : paths) {
         String rootFilePath = new File(path).getCanonicalPath();

         if(requestedFilePath.indexOf(rootFilePath) == 0)
         {
            inBounds = true;
            break;
         }
      }

      // check requested file in still within the capture dir
      if(!inBounds)
      {
         throw new Exception("File out of bounds!");
      }

      HashMap<String, TaskCommand> tasks = store.getTaskList();
      TaskCommand taskCommand = tasks.get(commandID);

      if(taskCommand != null)
      {
         String command = taskCommand.getCommand();

         StringBuffer buff = new StringBuffer(command);
         int indexOf = buff.indexOf("$filename");
         if(indexOf > -1) {
			buff = buff.replace(indexOf, indexOf + 9, thisFile.getCanonicalPath());
		 }

         System.out.println("Running : " + buff.toString());

         TaskItemThread taskItem = new TaskItemThread(taskCommand, new CommandWaitThread(buff.toString()), thisFile);
         Thread taskThread = new Thread(Thread.currentThread().getThreadGroup(), taskItem, taskItem.getClass().getName());
         taskThread.start();
      }

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/TaskManagementDataRes\n\n");
      return out.toString().getBytes();
   }

}
