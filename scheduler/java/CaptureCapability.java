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

public class CaptureCapability
{
   private String name = "";
   private int typeID = 0;
   private String fileExt = "";

   public CaptureCapability(String n, int id, String ext)
   {
      name = n;
      typeID = id;
      fileExt = ext;
   }

   public String getName()
   {
      return name;
   }

   public int getTypeID()
   {
      return typeID;
   }

   public String getFileExt()
   {
      return fileExt;
   }

}
