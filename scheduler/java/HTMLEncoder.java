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

public class HTMLEncoder
{
   private static final String[] ENTITIES = {
      "&",
      "&amp;",
      ">",
      "&gt;",
      "<",
      "&lt;",
      "\"",
      "&quot;",
      "'",
      "&#039;",
      "\\\\",
      "&#092;"};

   public static String encode(String orig)
   {
      String finalString = orig;

      for (int i = 0; i < ENTITIES.length; i += 2)
      {
         finalString = finalString.replaceAll(ENTITIES[i], ENTITIES[i + 1]);
      }

      return finalString;
   }

}
