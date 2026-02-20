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

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class HTTPurl
{
   private HashMap<String, String> args = new HashMap<>();
   private int requestType = 0;
   private String requestString = "";
   private String servletClass = "";
   private String originalGET = "";
   private HashMap<String, String> headers = null;
   private HashMap<String, String> cookies = null;
   private MultiPartParser multiPart = null;

   public HTTPurl(String requestURL, byte[] postData, HashMap<String, String> head) throws Exception
   {
      headers = head;
      getCookies();

      originalGET = requestURL;
      if (!requestURL.toUpperCase().startsWith("GET ") && !requestURL.toUpperCase().startsWith("POST ")) {
		throw new Exception("The request " + requestURL + " was not a valid type. (GET or POST)");
	  }

      int indexOfSpace = requestURL.indexOf(" ");
      requestString = requestURL.substring(indexOfSpace + 1, requestURL.indexOf(" ", indexOfSpace + 2));

      // work out if this is a multipart form post request
      String multiPartBoundary = null;
      if(requestURL.startsWith("POST") && requestString.startsWith("/servlet/") && headers != null)
      {
         String contentType = headers.get("Content-Type");
         if(contentType != null && contentType.startsWith("multipart/form-data; boundary="))
         {
            multiPartBoundary = contentType.substring("multipart/form-data; boundary=".length());
         }
      }


      if(multiPartBoundary != null)
      {
         requestType = 3;

         multiPart = new MultiPartParser(postData, multiPartBoundary);

         int paramStart = requestString.indexOf("?");

         if (paramStart > -1)
         {
            parseGetArgs(requestString.substring(paramStart + 1, requestString.length()));
         }

         if (paramStart < 1) {
			paramStart = requestString.length();
		 }

         servletClass = requestString.substring(9, paramStart);
         servletClass = servletClass.trim();
      }
      else if (requestURL.startsWith("POST") && requestString.startsWith("/servlet/"))
      {
         requestType = 3;

         parsePostArgs(postData);

         int paramStart = requestString.indexOf("?");

         if (paramStart > -1)
         {
            parseGetArgs(requestString.substring(paramStart + 1, requestString.length()));
         }

         if (paramStart < 1) {
			paramStart = requestString.length();
		 }

         servletClass = requestString.substring(9, paramStart);
         servletClass = servletClass.trim();
      }
      else if (requestString.equals("/") || requestString.startsWith("/?"))
      {
         requestType = 1;
         int paramStart = requestString.indexOf("?");
         if (paramStart > -1)
         {
            parseGetArgs(requestString.substring(paramStart + 1, requestString.length()));
         }
      }
      else if (requestString.startsWith("/servlet/"))
      {
         requestType = 3;

         int paramStart = requestString.indexOf("?");

         if (paramStart > -1)
         {
            parseGetArgs(requestString.substring(paramStart + 1, requestString.length()));
         }

         if (paramStart < 1) {
			paramStart = requestString.length();
		 }

         servletClass = requestString.substring(9, paramStart);
         servletClass = servletClass.trim();
      }
      else
      {
         requestType = 2;
      }
   }

   @Override
public String toString()
   {
      return originalGET;
   }

   public String getServletClass()
   {
      return servletClass;
   }

   public int getRequestType()
   {
      return requestType;
   }

   public String getReqString()
   {
      return requestString;
   }

   public String getParameter(String name)
   {
      return args.get(name);
   }

   public String[] getParameterList()
   {
      Set<String> params = args.keySet();

      String[] keys = new String[params.size()];

      Iterator<String> stepper = params.iterator();
      int index = 0;
      while (stepper.hasNext())
      {
         keys[index++] = stepper.next();
      }

      return keys;
   }

   private int parsePostArgs(byte[] postData) throws Exception
   {
      if (postData == null || postData.length == 0) {
		return 0;
	  }

      String reqString = new String(postData);

      //System.out.println("Post Data = " + reqString);

      String[] paramList = reqString.split("&");

      for (String element : paramList) {
         int sep = element.indexOf("=");
         if (sep > 0)
         {
            String name = element.substring(0, sep);
            String value = element.substring(sep + 1, element.length());

            name = URLDecoder.decode(name, "UTF-8");
            value = URLDecoder.decode(value, "UTF-8");

            //System.out.println(name + " = " + value);
            args.put(name, value);
         }
      }

      return 1;
   }

   private int parseGetArgs(String reqString) throws Exception
   {
      if (reqString == null) {
		return 0;
	  }

      String[] paramList = reqString.split("&");

      for (String element : paramList) {
         int sep = element.indexOf("=");
         if (sep > 0)
         {
            String name = element.substring(0, sep);
            String value = element.substring(sep + 1, element.length());

            name = URLDecoder.decode(name, "UTF-8");
            value = URLDecoder.decode(value, "UTF-8");

            //System.out.println(name + " = " + value);
            args.put(name, value);
         }
      }

      return 1;
   }

   private void getCookies()
   {
      cookies = new HashMap<>();
      String cookieList = headers.get("Cookie");
      if(cookieList == null || cookieList.length() == 0) {
		return;
	  }

      String[] cutCookies = cookieList.split("; ");

      for (String element : cutCookies) {
         String[] cookieBits = element.split("=");
         if(cookieBits.length == 2)
         {
            cookies.put(cookieBits[0], cookieBits[1]);
         }
      }
   }

   public String getCookie(String name)
   {
      return cookies.get(name);
   }

   public MultiPartParser getMultiPart()
   {
      return multiPart;
   }

}