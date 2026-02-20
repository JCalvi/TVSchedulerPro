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

import java.io.FileInputStream;
import java.util.Properties;

class ChannelList
{
   private FileInputStream fileList = null;
   private Properties prop = null;

   public ChannelList(String list) throws Exception
   {
      prop = new Properties();
      fileList = new FileInputStream(list);
      prop.load(fileList);
      fileList.close();
   }

   public String[] getCountries() throws Exception
   {
      String[] countries = new String[1000];

      String country = "";
      for(int x = 0; x < countries.length; x++)
      {
         country = prop.getProperty("country." + x);
         if(country != null) {
			country = country.trim();
		 }

         countries[x] = country;
      }

      return countries;
   }

   public String[] getRegions(int country) throws Exception
   {
      String[] regions = new String[1000];

      String region = "";
      for(int x = 0; x < regions.length; x++)
      {
         region = prop.getProperty("region." + country + "." + x);
         if(region != null) {
			region = region.trim();
		 }

         regions[x] = region;
      }

      return regions;
   }

   public Channel[] getStations(int country, int region) throws Exception
   {
      Channel[] channels = new Channel[1000];

      for(int x = 0; x < channels.length; x++)
      {
         String name = prop.getProperty("station." + country + "." + region + "." + x + ".name");
         String freq = prop.getProperty("station." + country + "." + region + "." + x + ".freq");
         String band = prop.getProperty("station." + country + "." + region + "." + x + ".band");

         if(name != null && freq != null && band != null)
         {
            int freqINT = 0;
            int bandINT = 0;
            try
            {
               freqINT = Integer.parseInt(freq.trim());
               bandINT = Integer.parseInt(band.trim());
               channels[x] = new Channel(name, freqINT, bandINT, 0, 0, 0);
            }
            catch(Exception e)
            {
            e.printStackTrace();
               channels[x] = null;
            }
         }
         else
         {
            channels[x] = null;
         }
      }

      return channels;
   }

   public void close() throws Exception
   {
      fileList.close();
   }
}
