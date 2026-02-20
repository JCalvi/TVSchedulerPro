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
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

class RequestObject implements Runnable
{
   private Socket server = null;
   private HashMap<String, String> headers = new HashMap<>();
   private ByteArrayOutputStream postDataBytes = null;

   RequestObject(Socket server)
   {
      this.server=server;
   }

   @Override
public void run ()
   {
      try
      {
         InputStream in = server.getInputStream();
         OutputStream out = server.getOutputStream();

         // Get input from the client
         String request = "";
         String line = "";

         byte[] postData = new byte[0];

         while(true)
         {
            byte thisByte = (byte)in.read();
            if(thisByte == -1)
            {
               //System.out.println("Connection Closed");
               break;
            }

            if(thisByte == '\n')
            {
               //System.out.println("REQUEST =  " + line);

               if(line.length() == 0)
               {
                  //System.out.println("end of headers");
                  break;
               }

               if(request == "")
               {
                  request = line;
               }
               else
               {
                  int index = line.indexOf(": ");
                  if(index > 0)
                  {
                     headers.put(line.substring(0, index), line.substring(index + 2, line.length()));
                  }
               }

               line = "";
            }
            else if(thisByte != '\r')
            {
               line += (char)thisByte;
            }
         }

         //System.out.println(request);

         headers.put("RemoteAddress", server.getInetAddress().getHostAddress());
         headers.put("LoopbackAddress", Boolean.valueOf(server.getInetAddress().isLoopbackAddress()).toString());

         int dataLength = 0;
         String contLength = headers.get("Content-Length");
         if(contLength != null)
         {
            try
            {
               dataLength = Integer.parseInt(contLength);
            }
            catch(Exception e){}
         }

         if(dataLength > 0)
         {
            //System.out.println("Reading post data length: " + dataLength);
            postDataBytes = new ByteArrayOutputStream();

            byte[] buff = new byte[1];
            int totaleRead = 0;

            while(totaleRead < dataLength)
            {
               int read = in.read(buff);
               if(read != -1)
               {
                  //System.out.println("Read " + read + " bytes of data (" + (char)buff[0] + ")");
                  postDataBytes.write(buff, 0, read);
                  totaleRead += read;
               }
               else
               {
                  break;
               }
            }

            // IE adds 13 + 10 (CRLF) to the end of the post request, these need to be read off for
            // the browser to accept this as a valid server, if they are not the client closes the connection
            // too early.
            while(in.available() > 0)
            {
               int read = in.read();
               System.out.println("Removed Post Data Padding Byte = " + read);
            }

            postData = postDataBytes.toByteArray();
            //System.out.println("(" + postDataBytes.toString() + ")");
         }

         //System.out.println("HTTPRequest (request,headers,postData,out): (" + request + "," + headers + "," + postData + "," + out + ")");

         //Sanity check to make sure if requests contain GET or POST, Calvi added
         if(request.length() > 3 &&
           (request.substring(0, 3).toUpperCase().equals("GET")  ||
            request.substring(0, 4).toUpperCase().equals("POST")) )
         {
           Thread.currentThread().setName(request);
           HTTPRequest httpRequest = new HTTPRequest(request, headers, postData, out);
           httpRequest.sendResponseData();
         }
         else
         {
           //Empty or invalid request, just ignore.
           //System.out.println("Ignored Invalid HTTP Request: " + request);
         }
      }
      catch (Exception e)
      {
         System.out.println("Worker RequestObject Exception: " + e);
         e.printStackTrace();
      }
      finally
      {
         try
         {
            server.close();
         }
         catch(Exception e){}
      }

      //System.out.println("Request Thread Exiting");
   }
}