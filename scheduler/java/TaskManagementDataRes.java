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
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

class TaskManagementDataRes extends HTTPResponse
{
   public TaskManagementDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {

      if("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showTaskOutput(urlData));
         return;
      }
      else if("02".equals(urlData.getParameter("action")))
      {
         outStream.write(killTask(urlData));
         return;
      }
      else if("03".equals(urlData.getParameter("action")))
      {
         outStream.write(removeTask(urlData));
         return;
      }
      else if("04".equals(urlData.getParameter("action")))
      {
         outStream.write(removeFinished(urlData));
         return;
      }
      else if("05".equals(urlData.getParameter("action")))
      {
         outStream.write(showArchiveList(urlData));
         return;
      }
      else if("06".equals(urlData.getParameter("action")))
      {
         outStream.write(showArchivedTaskOutput(urlData));
         return;
      }
      else if("07".equals(urlData.getParameter("action")))
      {
         outStream.write(deleteArchiveFile(urlData));
         return;
      }
      else if("08".equals(urlData.getParameter("action")))
      {
         outStream.write(deleteAllArchives(urlData));
         return;
      }
      else
      {
         outStream.write(showTaskList(urlData));
      }
   }

   private byte[] showArchiveList(HTTPurl urlData) throws Exception
   {
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "ArchiveTaskList.html");

      StringBuffer buff = new StringBuffer();
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File outFile = new File(dataPath + File.separator + "archive");
      if(!outFile.exists()) {
		outFile.mkdirs();
	  }

      File[] files = outFile.listFiles();

      for(int x = 0; files != null && x < files.length; x++)
      {
         File archiveFile = files[x];
         if(!archiveFile.isDirectory() && archiveFile.getName().startsWith("Task-"))
         {
            buff.append("<tr>\n");

            buff.append("<td>");
            buff.append("<a href='/servlet/TaskManagementDataRes?action=06&file=" + URLEncoder.encode(archiveFile.getName(), "UTF-8") + "'>");
            buff.append("<img src='/images/log.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");
            buff.append("<a href='/servlet/TaskManagementDataRes?action=07&file=" + URLEncoder.encode(archiveFile.getName(), "UTF-8") + "'>");
            buff.append("<img src='/images/delete.png' border='0' alt='Schedule Log' width='24' height='24'></a> ");
            buff.append("</td>");

            buff.append("<td style='padding-left:20px;'>" + archiveFile.getName() + "</td>\n");

            buff.append("</tr>\n");
         }
      }

      template.replaceAll("$ArchiveList", buff.toString());

      return template.getPageBytes();
   }

   @SuppressWarnings("unchecked")
   private byte[] showArchivedTaskOutput(HTTPurl urlData) throws Exception
   {
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "ArchivedTaskOutPut.html");
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File archivePath = new File(dataPath + File.separator + "archive" + File.separator + urlData.getParameter("file"));

      FileInputStream fis = new FileInputStream(archivePath);
      ObjectInputStream ois = new ObjectInputStream(fis);
      HashMap<?,?> item = (HashMap<?,?>)ois.readObject();
      ois.close();

      if(item != null)
      {
         template.replaceAll("$taskControl", (String)item.get("control"));
         template.replaceAll("$taskOutput", (String)item.get("stdout"));
         template.replaceAll("$taskError", (String)item.get("stderr"));
      }
      else
      {
         StringBuffer out = new StringBuffer(256);
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
         return out.toString().getBytes();
      }

      return template.getPageBytes();
   }

   private byte[] removeFinished(HTTPurl urlData) throws Exception
   {
      String[] keys = store.runningTaskList.keySet().toArray(new String[0]);
      for (String key : keys) {
         TaskItemThread task = store.runningTaskList.get(key);
         if(task.isFinished())
         {
            store.runningTaskList.remove(key);
         }
      }

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] killTask(HTTPurl urlData) throws Exception
   {
      String dateID = urlData.getParameter("id");

      TaskItemThread item = store.runningTaskList.get(dateID);

      if(item != null)
      {
         item.stop();
      }

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] removeTask(HTTPurl urlData) throws Exception
   {
      String id = urlData.getParameter("id");

      store.runningTaskList.remove(id);

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");

      return out.toString().getBytes();
   }

   private byte[] showTaskOutput(HTTPurl urlData) throws Exception
   {
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "TaskOutPut.html");

      String id = urlData.getParameter("id");
      TaskItemThread item = store.runningTaskList.get(id);

      if(item != null)
      {
         template.replaceAll("$taskControl", item.getControl());
         template.replaceAll("$taskOutput", item.getOutput());
         template.replaceAll("$taskError", item.getError());
      }
      else
      {
         StringBuffer out = new StringBuffer(256);
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/" + urlData.getServletClass() + "\n\n");
         return out.toString().getBytes();
      }

      return template.getPageBytes();
   }

   private byte[] showTaskList(HTTPurl urlData) throws Exception
   {
      StringBuffer buff = new StringBuffer(2048);
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "RunningTaskList.html");

      String[] keys = store.runningTaskList.keySet().toArray(new String[0]);
      Arrays.sort(keys);

      SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");

      for (String key : keys) {
         TaskItemThread item = store.runningTaskList.get(key);

         buff.append("<tr>");
         buff.append("<td nowrap>" + df.format(item.getCreationDate()) + "</td>");
         buff.append("<td nowrap>" + item.getTaskName() + "</td>");
         buff.append("<td nowrap>" + item.getTargetFile() + "</td>");
         buff.append("<td align='center' nowrap>" + item.getDelayLeft() + "</td>");
         buff.append("<td align='center' nowrap>" + item.getStatus() + "</td>");
         buff.append("<td>");

         buff.append("<a class='noUnder' href='/servlet/" + urlData.getServletClass() + "?action=01&id=" + URLEncoder.encode(key, "UTF-8") + "'><img align='absmiddle' src='/images/log.png' border='0' alt='Show Output' width='24' height='24'></a> ");

         if(!item.isFinished())
         {
            buff.append(" <a class='noUnder' onClick='return confirmAction(\"Kill\");' href='/servlet/" + urlData.getServletClass() + "?action=02&id=" + URLEncoder.encode(key, "UTF-8") + "'><img align='absmiddle' src='/images/stop.png' border='0' alt='Kill' width='24' height='24'></a> ");
         }
         else
         {
            buff.append(" <a class='noUnder' onClick='return confirmAction(\"Delete\");' href='/servlet/" + urlData.getServletClass() + "?action=03&id=" + URLEncoder.encode(key, "UTF-8") + "'><img align='absmiddle' src='/images/delete.png' border='0' alt='Delete' width='24' height='24'></a>");
         }

         buff.append("</td></tr>\n");
      }

      template.replaceAll("$taskList", buff.toString());
      return template.getPageBytes();
   }

   public byte[] deleteArchiveFile(HTTPurl urlData) throws Exception
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File basePath = new File(dataPath + File.separator + "archive");
      File archivePath = new File(dataPath + File.separator + "archive" + File.separator + urlData.getParameter("file"));

      if(archivePath.getCanonicalPath().indexOf(basePath.getCanonicalPath()) == -1)
      {
         throw new Exception("Archive file to delete is not in the archive path!");
      }

      if(archivePath.exists())
      {
         archivePath.delete();
      }

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/TaskManagementDataRes?action=05\n\n");
      return buff.toString().getBytes();
   }

   public byte[] deleteAllArchives(HTTPurl urlData) throws Exception
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File outFile = new File(dataPath + File.separator + "archive");
      if(!outFile.exists()) {
		outFile.mkdirs();
	  }

      File[] files = outFile.listFiles();
      Arrays.sort(files);

      for(int x = files.length-1; files != null && x >= 0; x--)
      {
         File archiveFile = files[x];
         if(!archiveFile.isDirectory() && archiveFile.getName().startsWith("Task-"))
         {
            archiveFile.delete();
         }
      }

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/TaskManagementDataRes?action=05\n\n");
      return buff.toString().getBytes();
   }

}
