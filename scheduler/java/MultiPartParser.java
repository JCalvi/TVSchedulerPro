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

public class MultiPartParser
{
   private String boundary = "";
   private byte[] multiPartData = null;

   public MultiPartParser(byte[] data, String bound)
   {
      this.boundary = bound;
      this.multiPartData = data;

      //System.out.println("Boundary = " + this.boundary);
      //System.out.println(new String(this.multiPartData));
   }

   public byte[] getPart(String name)
   {
      int startOfPart = 0;
      while(true)
      {

         startOfPart = findNextPart(startOfPart);

         if(startOfPart > -1)
         {
            String partName = getName(startOfPart);

            if(partName != null && partName.equals(name))
            {
               return extractPartData(startOfPart);
            }
         }
         else
         {
            return null;
         }
      }
   }

   private byte[] extractPartData(int start)
   {
      // look for first blank line

      String line = "";
      int x = start;
      for(; x < this.multiPartData.length; x++)
      {
         if(this.multiPartData[x] == '\n')
         {
            if(line == "") //this is the first blank line
            {
               x++; //move on to next char
               break;
            } else {
				line = "";
			}
         }
         else if(this.multiPartData[x] != '\r')
         {
            line += (char)this.multiPartData[x];
         }
      }

      int partEnd = findPartEnd(x);

      ByteArrayOutputStream partBytes = new ByteArrayOutputStream();

      for(; x < partEnd; x++)
      {
         if(x < partEnd - 2)
         {
            // for any data before the last 2 bytes just add it
            partBytes.write(this.multiPartData[x]);
         }
         else if((x == (partEnd - 2)) && this.multiPartData[x] != '\r')
         {
            // only add the 2nd last byte if it is not a CR
            partBytes.write(this.multiPartData[x]);
         }
         else if((x == (partEnd - 1)) && this.multiPartData[x] != '\n')
         {
            // only add the last byte if it is not a new LF
            partBytes.write(this.multiPartData[x]);
         }
      }

      return partBytes.toByteArray();
   }

   private String getName(int start)
   {
      StringBuffer buff = new StringBuffer();

      int lineEnd = readLine(start, buff);

      if(lineEnd > -1)
      {
         //System.out.println(buff.toString());
         String name = null;
         int indexOf = buff.toString().indexOf("name=\"");
         if(indexOf > -1)
         {
            name = buff.substring(indexOf+6, buff.indexOf("\"", indexOf+6));
         }

         //System.out.println(name);
         return name;
      } else {
		return null;
	  }
   }

   private int findPartEnd(int start)
   {
      StringBuffer buff = new StringBuffer();

      int lineStart = start;
      int end = start;
      end = readLine(end, buff);

      while(end > -1)
      {
         String myData = buff.toString();
         if(myData.equals("--" + boundary) || myData.equals("--" + boundary + "--")) {
			return lineStart;
		 }

         buff = new StringBuffer();
         lineStart = end;
         end = readLine(end, buff);
      }

      return -1;
   }

   private int findNextPart(int start)
   {
      StringBuffer buff = new StringBuffer();

      int end = start;
      end = readLine(end, buff);

      while(end > -1)
      {
         String myData = buff.toString();
         if(myData.equals("--" + boundary)) {
			return end;
		 }

         buff = new StringBuffer();
         end = readLine(end, buff);
      }

      return -1;
   }

   private int readLine(int start, StringBuffer buff)
   {
      if(start >= this.multiPartData.length -1) {
		return -1;
	  }

      int x = start;
      for(; x < this.multiPartData.length; x++)
      {
         if(this.multiPartData[x] == '\n')
         {
            return x+1;
         }
         else if(this.multiPartData[x] != '\r')
         {
            buff.append((char)this.multiPartData[x]);
         }
      }

      if(x > this.multiPartData.length - 1) {
		x = this.multiPartData.length - 1;
	  }

      return x;
   }

}
