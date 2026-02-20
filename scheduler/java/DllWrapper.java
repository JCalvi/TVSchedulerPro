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

public class DllWrapper
{
   public native long getFreeSpace(String location);
   public native int setWakeUpTime(int year, int month, int day, int hour, int min, int sec);
   public native int setNextScheduleTime(int year, int month, int day, int hour, int min, int sec);
   public native void setActiveCount(int amount);
   public native void setCurrentPort(int port);
   public native void setKbLEDs(int value);
   public native int setEvent(String event);
   public native void setNotification(String notice);

   static
   {
      System.loadLibrary("win/util");
   }
}
