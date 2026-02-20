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
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class HTTPRequest
{
   private HashMap<String, String> headers = null;
   private HTTPurl urlData = null;
   private OutputStream outStream = null;
   private DataStore store = null;
   private SimpleDateFormat df = null;

   public HTTPRequest(String req, HashMap<String, String> head, byte[] postData, OutputStream out) throws Exception
   {
      outStream = out;
      headers = head;
      store = DataStore.getInstance();
      urlData = new HTTPurl(req, postData, head);
      df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

      if("1".equals(store.getProperty("security.accesslog")))
      {
         String[] params = urlData.getParameterList();
         String paramList = "";
         if(params.length > 0)
         {

            for(int x = 0; x <  params.length; x++)
            {
               String value = urlData.getParameter(params[x]);
               if(value.length() > 160)
               {
                  value = value.substring(160) + "(truncated)";
               }
               paramList += "\r\nAccessLog : Param(" + (x+1) + ") : " + params[x] + "=" + value;
            }
         }

         System.out.println("AccessLog : " + df.format(new Date()) + " : " + urlData.getRequestType() + " : " + urlData.getReqString() + paramList);
      }
   }

   // /////////////////////////////////////////////////
   // This is the main function to call it handles
   // the request and returns the correct data
   // /////////////////////////////////////////////////
   @SuppressWarnings("unchecked")
   public void sendResponseData()
   {
      try
      {
         // if the timer thread is not running then panic!
         if (store.timerStatus == -1 || store.adminStatus == -1)
         {
            String timerError = "<html><head><title>Thread Error</title></head><body>"
                  + "<h1>Thread Error</h1>The main admin or timer thread is not running, it has crashed with the following error:<p>"
                  + "<hr>"
                  + "<pre>Timer Thread StackTrace:\n"
                  + store.timerThreadErrorStack
                  + "</pre><p>"
                  + "<hr>"
                  + "<pre>Admin Thread StackTrace:\n"
                  + store.adminThreadErrorStack
                  + "</pre><p>"
                  + "<hr>"
                  + "Use this information in any error report you submit."
                  + "</body>";
            outStream.write(timerError.getBytes());
            return;
         }

         //
         // If needed check the username:password for authentication
         //
         AccessControl ac = AccessControl.getInstance();
         if (!ac.authenticateUser(headers, urlData))
         {
            System.out.println("Access denied from IP : " + headers.get("RemoteAddress"));
            StringBuffer out = new StringBuffer(4096);
            out.append("HTTP/1.0 401 Unauthorized\r\n");
            out.append("WWW-Authenticate: BASIC realm=\"TV Scheduler Pro\"\r\n");
            out.append("Cache-Control: no-cache\r\n\r\nAccess denied for area.");

            outStream.write(out.toString().getBytes());
            return;
         }

         //
         // We need to build the return data dynamically :-)
         // This is done by using our dynamic class loader
         // it loads the class specified on the URL and
         // calls the getResponce method
         //
         else if (urlData.getRequestType() == 3)
         {
            Class<?> paramTypes[] = {};
            Constructor<?> c = Class.forName(urlData.getServletClass()).getConstructor(paramTypes);
            Object params[] = {};
            HTTPResponse resp = (HTTPResponse) c.newInstance(params);

            resp.getResponse(urlData, outStream, headers);
            return;
         }

         //
         // Just return a file from the local system.
         //
         else if (urlData.getRequestType() == 2)
         {
            returnFileContent(urlData.getReqString());
            return;
         }

         //
         // this the the main status index page
         //
         else if (urlData.getRequestType() == 1)
         {
            SystemStatusData sd = new SystemStatusData();
            outStream.write(sd.getStatusXML(urlData, headers));
            return;
         }

         PageTemplate page = new PageTemplate(store.getProperty("path.httproot")
            + File.separator + "templates" + File.separator + "error.html");
         page.replaceAll("$error", "Request not known\n\n" + requestInfo());
         outStream.write(page.getPageBytes());
      }
      catch (Exception e)
      {
         ByteArrayOutputStream ba = new ByteArrayOutputStream();
         PrintWriter err = new PrintWriter(ba);

         try
         {
            e.printStackTrace(err);
            err.flush();

            PageTemplate page = new PageTemplate(store.getProperty("path.httproot")
              + File.separator + "templates" + File.separator + "error.html");
            page.replaceAll("$error", HTMLEncoder.encode(urlData.toString())
                  + "\n\n" + HTMLEncoder.encode(ba.toString()));
            outStream.write(page.getPageBytes());
         }
         catch (Exception e2)
         {
            try
            {
               outStream.write(ba.toString().getBytes());
            }
            catch (Exception e3)
            {
            }
         }

         System.out.println("HTTP Request Exception: " + e);
         e.printStackTrace();
      }

   }

   // /////////////////////////////////////////////////
   // This returns the file 4kB at a time, this is for
   // returning large files from the system.
   // /////////////////////////////////////////////////
   private int returnFileContent(String urlString) throws Exception
   {

      boolean doRange = false;
      long rangeStart = -1;
      long rangeEnd = -1;
      long dataSent = 0;
      long totalDataToSend = 0;


      // must encode the + sign first
      urlString = urlString.replaceAll("\\+", "%2B");

      String fileName = URLDecoder.decode(urlString, "UTF-8");
      String[] capPathStrings = store.getCapturePaths();

      //System.out.println(urlString + " - " + fileName);

      //
      // build the file path from the URL
      //
      boolean capPathFound = false;
      for(int x = 0; x < capPathStrings.length; x++)
      {
         if(fileName.indexOf("/$path" + x + "$") > -1)
         {
            capPathFound = true;
            //System.out.println(fileName);
            fileName = fileName.replace("/$path" + x + "$", capPathStrings[x]);
            //System.out.println(fileName);
            break;
         }
      }

      // if a path was not found then use the standard http data path
      if(!capPathFound)
      {
         fileName = store.getProperty("path.httproot") + fileName;
      }

      //
      // Check the file in an area we are allowed to server from
      //
      File thisFile = new File(fileName);
      String requestedFilePath = thisFile.getCanonicalPath();
      File root = new File(store.getProperty("path.httproot"));
      String rootFilePath = root.getCanonicalPath();
      boolean isOutOfBounds = true;

      for (String capPathString : capPathStrings) {
         if(requestedFilePath.indexOf(new File(capPathString).getCanonicalPath()) == 0)
         {
            isOutOfBounds = false;
            break;
         }
      }

      // check requested file in still within the root HTTP store
      if(isOutOfBounds && requestedFilePath.indexOf(rootFilePath) < 0)
      {
         throw new Exception("File out of bounds! (" + thisFile.getCanonicalPath() + ")");
      }

      //
      // build file list if required and just return that
      //

      if(thisFile.getName().equals("dir.list"))
      {
         StringBuffer data = new StringBuffer();

         data.append("HTTP/1.0 200 OK\n");
         data.append("Content-Type: text/html\n");
         data.append("\n");

         data.append("<html>");
         data.append("<body>\n");

         File[] files = thisFile.getParentFile().listFiles();

         if(files != null)
         {
            for (File file : files) {
               if(file.isDirectory() && !file.isHidden())
               {
                  data.append("<a href=\"" + file.getName() + "/dir.list\">[" + file.getName() + "]</a><br>\n");
               }
               else if(!file.isHidden())
               {
                  data.append("<a href=\"" + file.getName() + "\">" + file.getName() + "</a><br>\n");
               }
            }
         }
         else
         {
            data.append("Path not found!<br>\n");
         }

         data.append("</body>");
         data.append("</html>\n");

         outStream.write(data.toString().getBytes());

         return 1;
      }

      //
      // now do the actual file response stuff
      //
      FileInputStream fi = new FileInputStream(thisFile);
      long fileLength = thisFile.length();

      //
      // are we doing a ranged request
      //
      String rangeString = headers.get("Range");
      if(rangeString != null && rangeString.startsWith("bytes="))
      {
         doRange = true;
         rangeString = rangeString.substring("bytes=".length());
         System.out.println(this + " - Doing a Ranged Return (" + rangeString + ")");

      // just last bit of file
         if(rangeString.startsWith("-"))
         {
            rangeEnd = Long.parseLong(rangeString.substring(1));

            rangeStart = fileLength - rangeEnd;
            rangeEnd = fileLength;
         }
         // from set byte to end of file
         else if(rangeString.endsWith("-"))
         {
            rangeStart = Long.parseLong(rangeString.substring(0, rangeString.length()-1));
            rangeEnd = fileLength;
         }
         // a range of the file
         else
         {
            String[] bits = rangeString.split("-");
            rangeStart = Long.parseLong(bits[0]);
            rangeEnd = Long.parseLong(bits[1]);
         }

         System.out.println(this + " - Range (" + rangeStart + "-" + rangeEnd + ")");
      }

      int read = 0;
      byte[] bytes = new byte[4096];

      try
      {
         String header = "";

         if(doRange)
         {
            header += "HTTP/1.0 206 OK\n";
            header += "Content-Length: " + ((rangeEnd - rangeStart)+1) + "\n";
            header += "Content-Range: bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength + "\n";

            System.out.println(this + " - Content-Length: " + ((rangeEnd - rangeStart)+1));
            System.out.println(this + " - Content-Range: bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
         }
         else
         {
            header += "HTTP/1.0 200 OK\n";
            header += "Content-Length: " + fileLength + "\n";
         }

         //
         // Add a mine type to the return
         //
         HashMap<String, String> mineTypes = store.getMimeTypes();
         String mineType = "application/octet-stream";
         String ext = "";
         int lastDot = thisFile.getName().lastIndexOf(".");
         if(lastDot > -1 && lastDot != thisFile.length())
         {
            ext = thisFile.getName().toLowerCase().substring(lastDot+1);
            mineType = mineTypes.get(ext);
            if(mineType == null)
            {
               mineType = "application/octet-stream";
            }
         }
         //System.out.println("mine-type : " + mineType + " " + ext);
         header += "Content-Type: " + mineType + "\n";

         //
         // we accept ranged requests
         //
         header += "Accept-Ranges: bytes\n";

         //
         // send the response header info
         //
         header += "\n";
         //System.out.println(header);
         outStream.write(header.getBytes());

         totalDataToSend = 0;
         //
         // set range start if needed
         //
         if(doRange)
         {
            fi.skip(rangeStart);
            totalDataToSend = (rangeEnd - rangeStart) + 1;
            System.out.println(this + " - totalDataToSend = " + totalDataToSend);
         }
         else
         {
            totalDataToSend = fileLength;
         }

         while(true)
         {
            read = fi.read(bytes);
            if(read == -1) {
				break;
			}

            if((read + dataSent) > totalDataToSend)
            {
               System.out.println(this + " - Data Length Overlap (read=" + read + ", needed=" + (int)(totalDataToSend - dataSent) + ")");
               read = (int)(totalDataToSend - dataSent);
            }

            outStream.write(bytes, 0, read);
            dataSent += read;

            if(dataSent >= totalDataToSend)
            {
               //System.out.println(this + " - End of ranged data reached, breaking (" + dataSent + ", " + totalDataToSend + ")");
               break;
            }
         }

         // System.out.println(this + "Finished : " + fileName);
      }
      catch (Exception e)
      {
         System.out.println(this + " - ERROR : URL = " + urlData.getReqString());
         String[] keys = headers.keySet().toArray(new String[0]);
         for (String key : keys) {
            System.out.println(this + " - ERROR : REQUEST HEADER : " + key + " = " + headers.get(key));
         }
         System.out.println(this + " - ERROR : doRange = " + doRange);
         System.out.println(this + " - ERROR : totalDataToSend = " + totalDataToSend);
         System.out.println(this + " - ERROR : rangeStart = " + rangeStart);
         System.out.println(this + " - ERROR : rangeEnd = " + rangeEnd);
         System.out.println(this + " - ERROR : dataSent = " + dataSent);
         e.printStackTrace();
      }
      finally
      {
         try
         {
            fi.close();
         }
         catch (Exception e2)
         {
         }
      }

      return 1;
   }

   // /////////////////////////////////////////////////
   // return the request URL and headers as a string
   // /////////////////////////////////////////////////
   private String requestInfo() throws Exception
   {
      StringBuffer out = new StringBuffer(1024);

      // Print out the request info
      out.append("Request String = (" + urlData.getReqString() + ")\n");
      out.append("Request Type   = (" + urlData.getRequestType() + ")\n\n");

      out.append("Parameter List:\n");
      String[] names = urlData.getParameterList();
      for (String name : names) {
		out.append(name + " = " + urlData.getParameter(name) + "\n");
	  }

      // Print header info
      out.append("\nRequest Headers:\n");
      String[] keys = headers.keySet().toArray(new String[0]);
      for (String key : keys) {
         out.append(key + ": " + headers.get(key) + "\n");
      }

      return out.toString();

   }

}
