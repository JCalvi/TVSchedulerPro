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
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class PageTemplate
{
   private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy H:mm:ss");
   private StringBuffer page = null;
   private String templateLocation = "";
   private Vector<String> cookies = new Vector<>();

   public PageTemplate(String template) throws Exception
   {
      templateLocation = template;
      loadTemplate();
   }

   public void addCookie(String n, String d) throws Exception
   {
      String name = URLEncoder.encode(n, "UTF-8");
      String data = URLEncoder.encode(d, "UTF-8");
      cookies.add(name + "=" + data + "; PATH=/");
   }

   private void loadTemplate() throws Exception
   {
      ByteArrayOutputStream ba = new ByteArrayOutputStream();
		FileInputStream fi = new FileInputStream(templateLocation);

		int read = 0;
		byte[] bytes = new byte[1024];
		read = fi.read(bytes);
		while(read > -1)
		{
			ba.write(bytes, 0, read);
			read = fi.read(bytes);
		}

		fi.close();

      page = new StringBuffer();
      page.append(ba.toString());
   }

   public void replaceAll(String placeHolder, String replace) throws Exception
   {
      if(page == null) {
		new Exception("Template not loaded or failed load");
	  }

      //System.out.println("replaceAll on " + placeHolder);
      int phLength = placeHolder.length();
      int replaceLength = replace.length();

      int phIndex = page.indexOf(placeHolder);
      while(phIndex != -1)
      {
         page.replace(phIndex, phIndex + phLength, replace);
         phIndex = page.indexOf(placeHolder, phIndex + replaceLength);
      }
   }

   public byte[] getPageBytes() throws Exception
   {
      if(page == null) {
		new Exception("Template not loaded or filed load");
	  }

      StringBuffer returnData = new StringBuffer(buildResponce());
      returnData.append(page);

      return returnData.toString().getBytes("UTF-8");
   }

   private String buildResponce()
   {
      StringBuffer header = new StringBuffer();

      header.append("HTTP/1.0 200 OK\n");
      header.append("Content-Type: text/html; charset=utf-8\n");
      header.append("Pragma: no-cache\n");
      header.append("Cache-Control: no-cache\n");

      String timeStamp = df.format(new Date());
      timeStamp += " GMT";

      header.append("Last-Modified: " + timeStamp + "\n");
      header.append("Date: " + timeStamp + "\n");
      header.append("Expires: " + timeStamp + "\n");

      for (String element : cookies) {
         String cook = element;
         header.append("Set-Cookie: " + cook + "\n");
      }

      header.append("\n");

      return header.toString();
   }
}