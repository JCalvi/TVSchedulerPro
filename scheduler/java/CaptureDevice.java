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

import java.io.Serializable;

@SuppressWarnings("serial")
public class CaptureDevice implements Serializable
{
   private String deviceName = "";
   private String deviceID = "";
   private boolean inUse = false;

   public CaptureDevice(String name, String id)
   {
      deviceName = name;
      deviceID = id;
   }

   public String getName()
   {
      return deviceName;
   }

   public String getID()
   {
      return deviceID;
   }

   public boolean isInUse()
   {
      return inUse;
   }

   public void setInUse(boolean used)
   {
      inUse = used;
   }
}