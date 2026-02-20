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
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;

class LoaderThread implements Runnable
{
   private URL webPage = null;
   private ByteArrayOutputStream pageBytes = new ByteArrayOutputStream();
   private boolean finished = false;
   private Date lasthread = new Date();
   private long urlLength = 0;
   private int returnCode = -1;
   private String errorString = "";

   public LoaderThread(URL pageUrl)
   {

      webPage = pageUrl;

      try
      {
         DataStore store = DataStore.getInstance();

         // Set the HTTP proxy
         String proxy = store.getProperty("proxy.server");
         proxy = proxy.trim();
         if(proxy.length() > 0)
         {
            System.out.println("Using Proxy : " + proxy + " : " + store.getProperty("proxy.port").trim());
            System.getProperties().put("proxySet", "true");

            System.getProperties().put("proxyHost", proxy);
            System.getProperties().put("proxyPort", store.getProperty("proxy.port").trim());
         }
         else
         {
            System.getProperties().put("proxySet", "false");
            System.getProperties().put("proxyHost", "");
            System.getProperties().put("proxyPort", "");
         }

         Authenticator.setDefault(new HTTPAuthenticator());

      }
      catch (Exception e)
      {
         System.out.println("ERROR setting HTTP proxy");
         e.printStackTrace();
      }
   }

   @Override
public void run()
   {

      try
      {
         NumberFormat nf = NumberFormat.getNumberInstance();
         lasthread = new Date();
         HttpURLConnection con = null;
         InputStream is = null;

         try
         {
            String verString = DataStore.getInstance().getVersion();
            con = (HttpURLConnection)webPage.openConnection();
            con.setRequestProperty("User-Agent", "TVSchedulerPro(" + verString + ")");
            returnCode = con.getResponseCode();
            is = con.getInputStream();
         }
         catch (Exception e)
         {
            errorString += e.toString() + "\n";
            System.out.println("ERROR: URL Exception (" + e.toString() + ")");
            //e.printStackTrace();
            finished = true;
            return;
         }

         returnCode = con.getResponseCode();
         errorString += con.getResponseMessage() + "\n";

         int colCount = 0;

         lasthread = new Date();
         byte[] buff = new byte[128];

         int read = is.read(buff);
         while (read > -1)
         {
            if(colCount == 80)
            {
               System.out.println("Downloaded: " + nf.format(pageBytes.size()));
               colCount = 0;
            }
            colCount++;

            //System.out.print(".");
            lasthread = new Date();
            pageBytes.write(buff, 0, read);
            read = is.read(buff);
         }
         System.out.println("Downloaded: " + nf.format(pageBytes.size()));
      }
      catch (Exception e)
      {
         errorString += e.getMessage() + "\n";
         e.printStackTrace();
      }
      finished = true;
   }

   public void kill()
   {
      returnCode = -1;
   }

   public String getResponceMessage()
   {
      return errorString;
   }

   public int getResponceCode()
   {
      return returnCode;
   }

   public boolean isFinished()
   {

      return finished;
   }

   public String getDataString()
   {

      return pageBytes.toString();
   }

   public byte[] getDataBytes()
   {

      return pageBytes.toByteArray();
   }

   public boolean isTimedOut(int sec)
   {

      Date now = new Date();
      long timeOut = sec * 1000;
      long lifeTime = now.getTime() - lasthread.getTime();

      if(lifeTime > timeOut) {
		return true;
	  } else {
		return false;
	  }
   }

   public long getLength()
   {

      return urlLength;
   }

}
