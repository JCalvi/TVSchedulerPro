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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class KBFileManagerRes extends HTTPResponse
{

   public KBFileManagerRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showFileActions(urlData, headers));
         return;
      }
      if("02".equals(urlData.getParameter("action")))
      {
         outStream.write(deleteFile(urlData));
         return;
      }
      if("03".equals(urlData.getParameter("action")))
      {
         outStream.write(runCommand(urlData));
         return;
      }
      if("04".equals(urlData.getParameter("action")))
      {
         outStream.write(dirMenu(urlData, headers));
         return;
      }
      if("05".equals(urlData.getParameter("action")))
      {
         outStream.write(emptyDir(urlData));
         return;
      }
      if("06".equals(urlData.getParameter("action")))
      {
         outStream.write(deleteDir(urlData));
         return;
      }
      else
      {
         outStream.write(showFiles(urlData, headers));
         return;
      }
   }

   private byte[] dirMenu(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      File file = new File(urlData.getParameter("file"));

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "buttons", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("title" , file.getName());

      Element button = null;
      Element elm = null;
      Text text = null;

      String action = "";

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      button = doc.createElement("button");
      button.setAttribute("name" , "Back");
      elm = doc.createElement("url");
      action = "/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8") + "&start=" + start + "&show=" + show;
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      button = doc.createElement("button");
      button.setAttribute("name" , "Empty Directory");
      elm = doc.createElement("url");
      action = "/servlet/" + urlData.getServletClass() + "?action=05&path=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8") + "&start=" + start + "&show=" + show;
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("true");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      button = doc.createElement("button");
      button.setAttribute("name" , "Delete Directory");
      elm = doc.createElement("url");
      action = "/servlet/" + urlData.getServletClass() + "?action=06&path=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8") + "&start=" + start + "&show=" + show;
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("true");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
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

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(thisFile.getParent(), "UTF-8")  + "&start=" + start + "&show=" + show + "\n\n");
      return out.toString().getBytes();
   }

   private byte[] deleteDir(HTTPurl urlData) throws Exception
   {
      File thisFile = new File(urlData.getParameter("path"));

      String requestedFilePath = thisFile.getAbsolutePath();
      boolean inBounds = false;

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      String[] paths = store.getCapturePaths();
      for (String path : paths) {
         String rootFilePath = new File(path).getAbsolutePath();

         if(!requestedFilePath.equals(rootFilePath) && requestedFilePath.indexOf(rootFilePath) == 0)
         {
            inBounds = true;
            break;
         }
      }

      // check requested file in still within the capture dir
      if(inBounds)
      {
         if(thisFile.isDirectory() && thisFile.exists())
         {
            deleteFiles(thisFile);
            thisFile.delete();
         }
      }

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(thisFile.getParentFile().getAbsolutePath(), "UTF-8") + "&start=" + start + "&show=" + show + "\n\n");
      return out.toString().getBytes();
   }

   private byte[] emptyDir(HTTPurl urlData) throws Exception
   {
      File thisFile = new File(urlData.getParameter("path"));

      String requestedFilePath = thisFile.getAbsolutePath();
      boolean inBounds = false;

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      String[] paths = store.getCapturePaths();
      for (String path : paths) {
         String rootFilePath = new File(path).getAbsolutePath();

         if(requestedFilePath.indexOf(rootFilePath) == 0)
         {
            inBounds = true;
            break;
         }
      }

      // check requested file in still within the capture dir
      if(inBounds)
      {
         if(thisFile.isDirectory() && thisFile.exists())
         {
            deleteFiles(thisFile);
         }
      }

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(thisFile.getAbsolutePath(), "UTF-8") + "&start=" + start + "&show=" + show + "\n\n");
      return out.toString().getBytes();
   }

   private void deleteFiles(File root)
   {
      String fileMasks = DataStore.getInstance().getProperty("filebrowser.masks");
      File[] files = root.listFiles(new FileTypeFilter(fileMasks));

      if(files == null) {
		return;
	  }

      for (File file : files) {
         if(file.isDirectory() && !file.isHidden()) {
			deleteFiles(file);
		 }

         if(!file.isHidden())
         {
            System.out.println("Deleting File : " + file.getAbsolutePath());
            file.delete();
         }
      }
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

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      StringBuffer out = new StringBuffer(256);
      out.append("HTTP/1.0 302 Moved Temporarily\n");
      out.append("Location: /servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(thisFile.getParent(), "UTF-8") + "&start=" + start + "&show=" + show + "\n\n");
      return out.toString().getBytes();
   }

   private byte[] showFileActions(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      File file = new File(urlData.getParameter("file"));

      boolean showPlay = store.getProperty("filebrowser.showwsplay").equals("1");

      if(file == null || !file.exists())
      {
         StringBuffer out = new StringBuffer();
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/KBFileManagerRes\n\n");

         return out.toString().getBytes();
      }

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "buttons", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("title" , file.getName());

      Element button = null;
      Element elm = null;
      Text text = null;

      String action = "";

      button = doc.createElement("button");
      button.setAttribute("name" , "Back to Files");
      elm = doc.createElement("url");
      action = "/servlet/" + urlData.getServletClass() + "?path=" + URLEncoder.encode(file.getParent(), "UTF-8") + "&start=" + start + "&show=" + show;
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      button = doc.createElement("button");
      button.setAttribute("name" , "Delete File");
      elm = doc.createElement("url");
      action = "/servlet/" + urlData.getServletClass() + "?action=02&file=" + URLEncoder.encode(file.getPath(), "UTF-8") + "&start=" + start + "&show=" + show;
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("true");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      if(showPlay)
      {
         button = doc.createElement("button");
         button.setAttribute("name" , "Play File");
         elm = doc.createElement("url");
         action = "wsplay://ws/" + URLEncoder.encode(file.getPath(), "UTF-8");
         text = doc.createTextNode(action);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);
      }

      addCommands(doc, file, urlData, start, show);

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private void addCommands(Document doc, File file, HTTPurl urlData, String start, String show) throws Exception
   {
      HashMap<String, TaskCommand> tasks = store.getTaskList();
      String[] keys = tasks.keySet().toArray(new String[0]);

      Arrays.sort(keys);

      Element button = null;
      Element elm = null;
      Text text = null;
      Element root = doc.getDocumentElement();


      for (String key : keys) {
         TaskCommand command = tasks.get(key);

         if(command != null && command.getEnabled())
         {
            String action = "/servlet/" + urlData.getServletClass() + "?action=03&file=" + URLEncoder.encode(file.getPath(), "UTF-8") + "&command=" + URLEncoder.encode(key, "UTF-8") + "&start=" + start + "&show=" + show;

            button = doc.createElement("button");
            button.setAttribute("name" , "Run Task: " + key);
            elm = doc.createElement("url");
            text = doc.createTextNode(action);
            elm.appendChild(text);
            button.appendChild(elm);

            elm = doc.createElement("confirm");
            text = doc.createTextNode("true");
            elm.appendChild(text);
            button.appendChild(elm);

            root.appendChild(button);
         }
      }
   }

   private byte[] showFiles(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String pathString = urlData.getParameter("path");
      File[] files = null;
      File baseDir = null;
      boolean inBounds = false;
      String[] paths = store.getCapturePaths();
      DllWrapper capEng = new DllWrapper();

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

      if(!inBounds)
      {
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

      NumberFormat nf = NumberFormat.getInstance();

      int count = 0;

      int start = 0;
      int show = 10;

      try
      {
         show = Integer.parseInt(urlData.getParameter("show"));
      }
      catch(Exception e){}

      try
      {
         start = Integer.parseInt(urlData.getParameter("start"));
      }
      catch(Exception e){}

      if(start < 0) {
		start = 0;
	  }

      if(files == null) {
		files = new File[0];
	  }

      boolean dirsAtTop = "1".equals(store.getProperty("filebrowser.dirsattop"));
      Arrays.sort(files, new CompareFiles(dirsAtTop));

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "files", null);
      Element root = doc.getDocumentElement();

      // get title
      String title = "";

      if(baseDir != null)
      {
         for (String path : paths) {
            File basePath = new File(path);
            if(baseDir.getAbsolutePath().indexOf(basePath.getAbsolutePath()) == 0)
            {
               title += baseDir.getAbsolutePath().substring(basePath.getAbsolutePath().length());
               break;
            }
         }

         if(title.length() == 0) {
			title = "\\";
		 }
      }

      root.setAttribute("title" , title);

      if(baseDir != null)
      {
         root.setAttribute("back" , "/servlet/KBFileManagerRes?path=" + baseDir.getParentFile().getAbsolutePath());
      }
      else
      {
         root.setAttribute("back" , "0");
      }

      Element fileitem = null;
      Element elm = null;
      Text text = null;


      int fileIndex = start;

      if(inBounds && fileIndex == 0)
      {
         //
         // Delete dir
         //
         count++;

         fileitem = doc.createElement("file");

         elm = doc.createElement("name");
         text = doc.createTextNode("Directory Menu");
         elm.appendChild(text);
         fileitem.appendChild(elm);

         elm = doc.createElement("size");
         elm.setAttribute("units" , "");
         text = doc.createTextNode("");
         elm.appendChild(text);
         fileitem.appendChild(elm);

         String action = "/servlet/" + urlData.getServletClass();
         File parent = baseDir.getParentFile();
         if(parent != null) {
			action += "?action=04&file=" + URLEncoder.encode(baseDir.getCanonicalPath(), "UTF-8") + "&start=" + start + "&show=" + show;
		 }

         elm = doc.createElement("action");
         text = doc.createTextNode(action);
         elm.appendChild(text);
         fileitem.appendChild(elm);

         root.appendChild(fileitem);
      }

      while(fileIndex < files.length && (count) < show)
      {
         if(!inBounds)
         {
            count++;
            fileitem = doc.createElement("file");

            elm = doc.createElement("name");
            String nameData = files[fileIndex].getCanonicalPath();
            long freeSpace = capEng.getFreeSpace(files[fileIndex].getCanonicalPath());
            nameData += " (" + nf.format((freeSpace / (1024 * 1024))) + " MB Free)";

            text = doc.createTextNode(nameData);
            elm.appendChild(text);
            fileitem.appendChild(elm);

            elm = doc.createElement("size");
            elm.setAttribute("units" , "");
            text = doc.createTextNode("");
            elm.appendChild(text);
            fileitem.appendChild(elm);

            String action = "/servlet/" + urlData.getServletClass() +
            "?path=" + URLEncoder.encode(files[fileIndex].getCanonicalPath(), "UTF-8") + "&start=" + start + "&show=" + show;

            elm = doc.createElement("action");
            text = doc.createTextNode(action);
            elm.appendChild(text);
            fileitem.appendChild(elm);

            root.appendChild(fileitem);
         }
         else if(files[fileIndex].isDirectory() && !files[fileIndex].isHidden())
         {
            count++;
            fileitem = doc.createElement("file");

            elm = doc.createElement("name");
            text = doc.createTextNode("<" + files[fileIndex].getName() + ">");
            elm.appendChild(text);
            fileitem.appendChild(elm);

            elm = doc.createElement("size");
            elm.setAttribute("units" , "");
            text = doc.createTextNode("");
            elm.appendChild(text);
            fileitem.appendChild(elm);

            String action = "/servlet/" + urlData.getServletClass() +
            "?path=" + URLEncoder.encode(files[fileIndex].getCanonicalPath(), "UTF-8") + "&start=" + start + "&show=" + show;

            elm = doc.createElement("action");
            text = doc.createTextNode(action);
            elm.appendChild(text);
            fileitem.appendChild(elm);

            root.appendChild(fileitem);
         }
         else if(!files[fileIndex].isHidden())
         {
            count++;
            fileitem = doc.createElement("file");

            elm = doc.createElement("name");
            text = doc.createTextNode(files[fileIndex].getName());
            elm.appendChild(text);
            fileitem.appendChild(elm);

            elm = doc.createElement("size");
            elm.setAttribute("units" , "KB");
            text = doc.createTextNode(nf.format(files[fileIndex].length()/1024));
            elm.appendChild(text);
            fileitem.appendChild(elm);

            String action = "/servlet/" + urlData.getServletClass() + "?action=01" +
               "&file=" + URLEncoder.encode(files[fileIndex].getPath(), "UTF-8") + "&start=" + start + "&show=" + show;

            elm = doc.createElement("action");
            text = doc.createTextNode(action);
            elm.appendChild(text);
            fileitem.appendChild(elm);

            root.appendChild(fileitem);
         }

         fileIndex++;
      }


      root.setAttribute("start" , Integer.valueOf(start).toString());
      root.setAttribute("end" , Integer.valueOf(fileIndex).toString());
      root.setAttribute("show", Integer.valueOf(show).toString());
      root.setAttribute("total", Integer.valueOf(files.length).toString());

      if(pathString != null) {
		root.setAttribute("path", URLEncoder.encode(new File(pathString).getCanonicalPath(), "UTF-8"));
	  } else {
		root.setAttribute("path", "Root");
	  }

      //
      // Send output back
      //
      XSL transformer = new XSL(doc, "kb-showfiles.xsl", urlData, headers);
      return transformer.doTransform();
   }

}













