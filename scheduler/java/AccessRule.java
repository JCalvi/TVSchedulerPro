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

import java.util.HashMap;

public class AccessRule
{
   private String ruleGroup = "";
   private String ruleUrl = "";
   private HashMap<String, String> parameterList = new HashMap<>();
   @SuppressWarnings("this-escape")
   public AccessRule(String ruleData) throws Exception
   {
      ruleData = ruleData.trim();

      String[] ruleBits = ruleData.split(":");
      if(ruleBits.length != 3)
      {
         throw new Exception("ERROR with rule data (" + ruleData + ")");
      }

      ruleGroup = ruleBits[0];
      ruleUrl = ruleBits[1];

      if(!"*".equals(ruleBits[2]))
      {
         String[] paramBits = ruleBits[2].split("&");

         for (String paramBit : paramBits) {
            String[] bits = paramBit.split("=");
            if(bits.length == 2)
            {
               parameterList.put(bits[0], bits[1]);
            }
         }
      }

      System.out.println("Access Rule Loaded = " + this.toString());
   }

   public boolean isMatch(String url, HTTPurl requestData)
   {
      // does the url match
      if(!isUrlMatch(url)) {
		return false;
	  }

      // now check parameters
      if(parameterList.size() > 0)
      {
         String[] keys = parameterList.keySet().toArray(new String[0]);
         for (String key : keys) {
            String matchValue = parameterList.get(key);
            String paramValue = requestData.getParameter(key);

            if(paramValue == null || !matchValue.equals(paramValue))
            {
               //System.out.println("Parameter does not match (" + keys[x] + " " + matchValue + " " + paramValue + ")");
               return false;
            }
         }
      }

      return true;

   }

   private boolean isUrlMatch(String url)
   {
      if("*".equals(ruleUrl) || url.startsWith(ruleUrl))
      {
         return true;
      }

      return false;
   }

   @Override
public String toString()
   {
      String param = "";

      if(parameterList.size() > 0)
      {
         String[] keys = parameterList.keySet().toArray(new String[0]);
         for (String key : keys) {
            String matchValue = parameterList.get(key);
            param += "(" + key + "=" + matchValue + ")";
         }
      }
      else
      {
         param = "*";
      }


      return ruleGroup + ":" + ruleUrl + ":" + param;
   }

   public String getRuleGroup()
   {
      return ruleGroup;
   }

}