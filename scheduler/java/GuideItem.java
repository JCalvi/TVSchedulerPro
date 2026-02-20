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
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

@SuppressWarnings("serial")
class GuideItem implements Serializable, Comparable<GuideItem>
{
   // main items
   private String name = "";
   private String subName = "";
   private String description = "";
   // times
   private Date startTime = null;
   private Date endTime = null;
   // derived
   private int duration = 0;
   // strings
   private String language = "English";
   private String ratings = "";
   private String url = "";
   // vectors
   private Vector<String> category = new Vector<>();
   private Vector<String> actors = new Vector<>();
   private Vector<String> directors = new Vector<>();
   // special flag
   private boolean ignored = false;
   // flags
   private boolean ac3 = false;
   private boolean bw = false;
   private boolean closedcaptions = false;
   private boolean highdef = false;
   private boolean interactive = false;
   private boolean lastchance = false;
   private boolean live = false;
   private boolean premiere = false;
   private boolean repeat = false;
   private boolean surround = false;
   private boolean widescreen = false;


   public GuideItem()
   {
   }

   @Override
public GuideItem clone()
   {
      GuideItem clonedItem = new GuideItem();

      // main items
      clonedItem.name = name;
      clonedItem.subName = subName;
      clonedItem.description = description;
      // times
      clonedItem.startTime = startTime;
      clonedItem.setStop(endTime);
      // strings
      clonedItem.language = language;
      clonedItem.ratings = ratings;
      clonedItem.url = url;
      // vectors
      clonedItem.actors = actors;
      clonedItem.category = category;
      clonedItem.directors = directors;
      // special flag
      clonedItem.ignored = ignored;
      // flags
      clonedItem.ac3 = ac3;
      clonedItem.bw = bw;
      clonedItem.closedcaptions = closedcaptions;
      clonedItem.highdef = highdef;
      clonedItem.interactive = interactive;
      clonedItem.lastchance = lastchance;
      clonedItem.live = live;
      clonedItem.premiere = premiere;
      clonedItem.repeat = repeat;
      clonedItem.surround = surround;
      clonedItem.widescreen = widescreen;

      return clonedItem;
   }

   public boolean matches(GuideItem compItem, boolean inexactMatch, int startTol, int durTol)
   {
      if(   this.getStart().getTime() >= (compItem.getStart().getTime() - 60*1000*startTol) &&
            this.getStart().getTime() <= (compItem.getStart().getTime() + 60*1000*startTol) &&
            this.getDuration() >= (compItem.getDuration() - 60*1000*durTol) &&
            this.getDuration() <= (compItem.getDuration() + 60*1000*durTol) &&
            (
              this.getName().equals(compItem.getName()) ||
              ( inexactMatch &&
                  ( this.getName().toLowerCase().startsWith(compItem.getName().toLowerCase()) ||
                    compItem.getName().toLowerCase().startsWith(this.getName().toLowerCase())
                  )
              )
            )
        )

      {
         return true;
      }

      return false;
   }

   public boolean getIgnored()
   {
      return ignored;
   }

   public void setIgnored(boolean value)
   {
      ignored = value;
   }

   public String getURL()
   {
      return url;
   }

   public void setURL(String data)
   {
      url = data;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   public void setSubName(String sub)
   {
      this.subName = sub;
   }
   public void setDescription(String desc)
   {
      this.description = desc;
   }

   public void setStart(Date start)
   {
      startTime = start;

      if(duration != 0)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(startTime);
         cal.add(Calendar.MINUTE, duration);
         endTime = cal.getTime();
      }
   }

   public void setDuration(int duration)
   {
      this.duration = duration;

      if(startTime !=null)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime(startTime);
         cal.add(Calendar.MINUTE, duration);
         endTime = cal.getTime();
      }
   }

   public void setStop(Date stop)
   {
      endTime = stop;

      if(duration == 0 && startTime != null)
      {
         duration = (int)((endTime.getTime() - startTime.getTime()) / (1000 * 60));
      }
   }

   public void setDirectors(Vector<String> dir) {this.directors = dir;}
   public void addDirector(String dir) {this.directors.add(dir);}
   public void setActors(Vector<String> act) {this.actors = act;}
   public void addActor(String act) {this.actors.add(act);}
   public void setCategory(Vector<String> cats) {this.category = cats;}
   public void addCategory(String cat) {this.category.add(cat);}
   public void setLanguage(String lng)  {this.language = lng;}
   public void setRatings(String rtngs)  {this.ratings = rtngs;}
   public void setWidescreen(boolean ws) { this.widescreen = ws;}
   public void setHighDef(boolean hd) { this.highdef = hd;}
   public void setRepeat(boolean rpt) { this.repeat = rpt;}
   public void setPremiere(boolean prm) { this.premiere = prm;}
   public void setLive(boolean lv) { this.live = lv;}
   public void setInteractive(boolean mhp) { this.interactive = mhp;}
   public void setBlackWhite(boolean bw) { this.bw = bw;}
   public void setSurround(boolean srnd) { this.surround = srnd;}
   public void setAC3(boolean digsnd) { this.ac3 = digsnd;}
   public void setLastChance(boolean fin) { this.lastchance = fin;}
   public void setCaptions(boolean cpt) { this.closedcaptions = cpt;}

   public String getName()
   {
      return name;
   }
   public String getSubName()
   {
      return subName;
   }
   public String getDescription()
   {
      return description;
   }
   public Date getStart()
   {
      return startTime;
   }
   public Date getStop()
   {
      return endTime;
   }
   public int getDuration()
   {
      return duration;
   }

   public Vector<String> getDirectors() {return directors;}
   public Vector<String> getActors() {return actors;}
   public Vector<String> getCategory() {return category;}
   public String getLanguage() {return language;}
   public String getRatings() {return ratings;}
   public boolean getWidescreen() {return widescreen;}
   public boolean getHighDef() {return highdef;}
   public boolean getRepeat() {return repeat;}
   public boolean getPremiere() {return premiere;}
   public boolean getLive() {return live;}
   public boolean getInteractive() {return interactive;}
   public boolean getBlackWhite() {return bw;}
   public boolean getSurround() {return surround;}
   public boolean getAC3() {return ac3;}
   public boolean getLastChance() {return lastchance;}
   public boolean getCaptions() {return closedcaptions;}

   @Override
public String toString()
   {
      return Long.valueOf(startTime.getTime()).toString();
   }

   @Override
public int compareTo(GuideItem com)
   {
      if(com.getStart().getTime() > getStart().getTime()) {
		return -1;
	  } else if(com.getStart().getTime() == getStart().getTime()) {
		return 0;
	  } else {
		return 1;
	  }
   }

}
