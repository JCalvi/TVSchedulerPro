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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GuideDataLoader
{
   public GuideDataLoader(int type)
   {
   }

   public Vector<byte[]> getDataFromURL(String loadURL, StringBuffer buff, int format) throws Exception
   {
      Vector<byte[]> resultData = new Vector<>();

      System.out.println("Loading XMLTV data from URL : " + loadURL);

      Vector<byte[]> data = new Vector<>();
      //getContentUsingThread(new URL(loadURL), data, buff, format); //JC Deprecated
	  getContentUsingThread(new URI(loadURL).toURL(), data, buff, format);

      String pageData = "";
      if(data.size() == 1)
      {
         pageData = new String(data.get(0));
      }

      if(pageData.length() > 0 && pageData.indexOf("<XMLTV_FILE_LIST>") > -1)
      {
         System.out.println("Loading XMLTV data from file list");
         String[] fileList = extractNames(pageData);

         int lastIndex = loadURL.lastIndexOf("/");
         if(lastIndex > -1)
         {
            String baseUrl = loadURL.substring(0, lastIndex+1);

            for (String element : fileList) {
               String dataURL = baseUrl + element;
               System.out.println("Loading XMLTV data from URL : " + dataURL);

               Vector<byte[]> pages = new Vector<>();
               //int result = getContentUsingThread(new URL(dataURL), pages, buff, format); //JC Deprecated
               int result = getContentUsingThread(new URI(dataURL).toURL(), pages, buff, format);

               if(result == 200)
               {
	               for(int y = 0; y < pages.size(); y++)
	               {
	                  byte[] pd = pages.get(y);

	                  if(pd.length > 0) {
						resultData.add(pd);
					  }
	               }
               }
            }
         }
      }
      else if(data.size() > 0)
      {
         for(int y = 0; y < data.size(); y++)
         {
            byte[] pd = data.get(y);

            if(pd.length > 0) {
				resultData.add(pd);
			}
         }
      }

      return resultData;
   }

   private String[] extractNames(String nameList)
   {
      System.out.println("Extracting File list from Listing File");

      String names[] = nameList.split("\n");

      Vector<String> namesOfFiles = new Vector<>();

      if(names.length > 1)
      {
         for (int x = 1; x < names.length; x++)
         {
            if(names[x].trim().length() > 0)
            {
               namesOfFiles.add(names[x].trim());
               System.out.println("Adding : " + names[x].trim());
            }
         }
      }

      return namesOfFiles.toArray(new String[0]);
   }

   private int getContentUsingThread(URL location, Vector<byte[]> buffs, StringBuffer buff, int format)
   {
      Thread LoaderThread = null;
      LoaderThread loader = null;
      try
      {
         loader = new LoaderThread(location);
         LoaderThread = new Thread(Thread.currentThread().getThreadGroup(), loader, loader.getClass().getName());
         LoaderThread.start();

         while(!loader.isFinished())
         {
            if(loader.isTimedOut(30))
            {
               System.out.println("\nTimout reached : Killing loader Thread");

               if(format == 0)
               {
                  buff.append("Timeout reached : Killing loader Thread\n");
               }
               else
               {
                  buff.append("Timeout reached : Killing loader Thread<br>\n");
               }

               loader.kill();
               LoaderThread.interrupt();
               break;
            }
            Thread.sleep(500);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }

      LoaderThread.interrupt();

      if(loader.getResponceCode() != 200)
      {
         if(format == 0)
         {
            buff.append("URL Load Error (" + loader.getResponceCode() + ")\n" + loader.getResponceMessage() + "\n");
         }
         else
         {
            buff.append("URL Load Error (" + loader.getResponceCode() + ") : <pre>" + loader.getResponceMessage() + "</pre>\n");
         }

         return loader.getResponceCode();
      }

      if(location.toString().toUpperCase().indexOf(".ZIP", location.toString().length() - 4) > -1)
      {
         if(unZip(loader.getDataBytes(), buffs) > 0) {
			return 200;
		 } else {
			return 404;
		 }
      }
      else
      {
         buffs.add(loader.getDataBytes());
         return loader.getResponceCode();
      }
   }

   private int unZip(byte[] data, Vector<byte[]> extractedXML)
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      ByteArrayOutputStream baos = null;
      ZipInputStream zis = new ZipInputStream(bais);
      int count = 0;

      try
      {
         ZipEntry ze = zis.getNextEntry();

         while (ze != null)
         {
            if (ze.getName() != null && ze.getName().indexOf(".xml") > -1)
            {
               baos = new ByteArrayOutputStream();
               System.out.println("Extracting : " + ze.getName());

               int len;
               byte[] buf = new byte[1024];
               while ((len = zis.read(buf)) > 0)
               {
                  baos.write(buf, 0, len);
               }
               extractedXML.add(baos.toByteArray());
               count++;
            }
            ze = zis.getNextEntry();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return count;
   }

   public Vector<byte[]> getDataFromFiles(String loadDir, StringBuffer buff, int format)
   {
      Vector<byte[]> resultData = new Vector<>();

      File xmlDir = new File(loadDir);
      String actualFile = "";

      if (xmlDir.isDirectory())
      {
         String[] fileList = xmlDir.list();
         for (String element : fileList) {
            if (element.indexOf(".xml") > 0)
            {
               actualFile = loadDir + File.separator + element;

               System.out.println("Loading XMLTV data from " + actualFile);

               buff.append("Loading XMLTV data from " + actualFile);

               if(format == 1) {
				buff.append("<br>");
			   }
               buff.append("\n");

               byte[] data = null;
               try
               {
                  data = getFileContents(actualFile);
               }
               catch(Exception e)
               {
                  e.printStackTrace();
               }

               if(data != null && data.length > 10) {
				resultData.add(data);
			   }

            }
         }
      }

      return resultData;
   }

   private byte[] getFileContents(String fileName) throws Exception
   {
      ByteArrayOutputStream ba = new ByteArrayOutputStream();
      FileInputStream fis = new FileInputStream(fileName);

      byte[] data = new byte[1024];

      int read = fis.read(data, 0, 1024);

      while(read > -1)
      {
          ba.write(data, 0, read);
          read = fis.read(data, 0, 1024);
      }

      fis.close();

      //printHex(ba.toByteArray());

      return ba.toByteArray();
   }

   void printHex(byte[] b)
   {
      for (int i = 0; i < b.length; ++i)
      {
         if(i % 16 == 0)
         {
            System.out.print(Integer.toHexString((i & 0xFFFF) | 0x10000).substring(1, 5) + " - ");
         }

         System.out.print(Integer.toHexString((b[i] & 0xFF) | 0x100).substring(1, 3) + " ");

         if(i % 16 == 15 || i == b.length - 1)
         {
            int j;
            for (j = 16 - i % 16; j > 1; --j)
            {
               System.out.print("   ");
            }

            System.out.print(" - ");
            int start = (i / 16) * 16;
            int end = (b.length < i + 1) ? b.length : (i + 1);

            for (j = start; j < end; ++j)
            {
               if(b[j] >= 32 && b[j] <= 126)
               {
                  System.out.print((char) b[j]);
               }
               else
               {
                  System.out.print(".");
               }
            }

            System.out.println();
         }
      }
      System.out.println();
   }

}