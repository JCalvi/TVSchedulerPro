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

import java.io.File;
import java.util.Comparator;

class CompareFiles implements Comparator<Object>
{
   private boolean dirsAtTop = true;

   public CompareFiles(boolean atTop)
   {
      dirsAtTop = atTop;
   }

   @Override
public int compare(Object o1, Object o2)
   {
      File file1 = (File)o1;
      File file2 = (File)o2;

      // all dir's to the top
      if(file1.isDirectory() && !file2.isDirectory())
      {
         if(dirsAtTop) {
			return -1;
		 } else {
			return 1;
		 }
      }

      if(!file1.isDirectory() && file2.isDirectory())
      {
         if(dirsAtTop) {
			return 1;
		 } else {
			return -1;
		 }
      }

      if(file1.isDirectory() && file2.isDirectory()) {
		return 0;
	  }

      // all files sorted by last modified
      long file1Date = file1.lastModified();
      long file2Date = file2.lastModified();

      if(file1Date == file2Date) {
		return 0;
	  }

      if(file1Date < file2Date) {
		return 1;
	  }

      if(file1Date > file2Date) {
		return -1;
	  }

      return 0;
   }
}