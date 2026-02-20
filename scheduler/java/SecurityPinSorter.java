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

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

class SecurityPinSorter implements Comparator<Map.Entry<String, Date>>
{
   @Override
public int compare(Map.Entry<String, Date> o1, Map.Entry<String, Date> o2)
   {
      Map.Entry<String, Date> e1 = o1;
      Map.Entry<String, Date> e2 = o2;
      Date d1 = e1.getValue();
      Date d2 = e2.getValue();
      return d2.compareTo(d1);
   }
}