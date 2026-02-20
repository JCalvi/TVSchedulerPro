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
public class SignalStatistic implements Serializable
{
   private int strength = -1;
   private int quality = -1;
   private boolean locked = false;

   public SignalStatistic(String data)
   {
      parseData(data);
   }

   public int getStrength()
   {
      return strength;
   }

   public int getQuality()
   {
      return quality;
   }

   public boolean getLocked()
   {
      return locked;
   }

   private void parseData(String data)
   {
      try
      {
         data = data.substring("SIGNAL_DATA:".length(), data.length());
         String[] bits = data.split(",");
         if(bits.length == 3)
         {
            locked = "1".equals(bits[0].trim());
            strength = Integer.parseInt(bits[1].trim());
            quality = Integer.parseInt(bits[2].trim());
         }
      }
      catch(Exception e)
      {
         System.out.println("ERROR parsing signal statistic data.");
         e.printStackTrace();
      }
   }

}
