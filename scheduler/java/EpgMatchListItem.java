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


import java.util.Calendar;
import java.util.Vector;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class EpgMatchListItem
{
   // what type are we
   public static final int TYPE_TEXT = 0;
   public static final int TYPE_SPAN = 1;
   public static final int TYPE_DAYS = 2;
   public static final int TYPE_FLAG = 3;

   // fields ids
   public static final int FIELD_TITLE = 0;
   public static final int FIELD_SUBTITLE = 4;
   public static final int FIELD_DESCRIPTION = 1;
   public static final int FIELD_TITSUBDESC = 5;
   public static final int FIELD_CHANNEL = 2;
   public static final int FIELD_ACTOR = 6;
   public static final int FIELD_CATEGORY = 3;

   // flag ids
   public static final int FLAG_NONE = 0;
   public static final int FLAG_CASEINSENSITIVE = 1;
   public static final int FLAG_REGEX = 2;

   // data for type TEXT
   private String searchText = "";
   private int field = 0;
   private boolean exists = true;
   private int flags = 0;

   // data for type span
   private int span_from_hour = 0;
   private int span_from_min = 0;
   private int span_to_hour = 0;
   private int span_to_min = 0;
   // Calvi changes.
   private int span_type = 0;

   // data for type days
   // bit representation of days
   private boolean day_sun = true;
   private boolean day_mon = true;
   private boolean day_tue = true;
   private boolean day_wed = true;
   private boolean day_thur = true;
   private boolean day_fri = true;
   private boolean day_sat = true;

   // days ids
   public static final int DAYS_SUN = 0;
   public static final int DAYS_MON = 1;
   public static final int DAYS_TUE = 2;
   public static final int DAYS_WED = 3;
   public static final int DAYS_THUR = 4;
   public static final int DAYS_FRI = 5;
   public static final int DAYS_SAT = 6;

   // flag ID's
   public static final int ITEM_FLAG_REPEAT = 0;
   public static final int ITEM_FLAG_LIVE = 1;
   public static final int ITEM_FLAG_WS = 2;
   public static final int ITEM_FLAG_HD = 3;
   public static final int ITEM_FLAG_CC = 4;
   public static final int ITEM_FLAG_PREMIERE = 5;

   private int flagID = -1;
   private boolean flagValue = false;

   private int type = -1;

   public EpgMatchListItem(int matchType)
   {
      type = matchType;
   }

   public EpgMatchListItem(Node node)
   {
      parseXML(node);
   }

   public int getType()
   {
      return type;
   }

   public void setFlagData(int flag, boolean value)
   {
      flagID = flag;
      flagValue = value;
   }

   public int getFlagID()
   {
      return flagID;
   }

   public boolean getFlagValue()
   {
      return flagValue;
   }

   public boolean isFlagMatch(GuideItem item)
   {
      boolean flag = false;
      if(flagID == ITEM_FLAG_REPEAT)
      {
         flag = item.getRepeat();
      }
      else if(flagID == ITEM_FLAG_LIVE)
      {
         flag = item.getLive();
      }
      else if(flagID == ITEM_FLAG_WS)
      {
         flag = item.getWidescreen();
      }
      else if(flagID == ITEM_FLAG_HD)
      {
         flag = item.getHighDef();
      }
      else if(flagID == ITEM_FLAG_CC)
      {
         flag = item.getCaptions();
      }
      else if(flagID == ITEM_FLAG_PREMIERE)
      {
         flag = item.getPremiere();
      }

      if(flag == flagValue) {
		return true;
	  } else {
		return false;
	  }
   }

   public void setDayData(int dayID, boolean value)
   {
      if(dayID == DAYS_SUN) {
		day_sun = value;
	  } else if(dayID == DAYS_MON) {
		day_mon = value;
	  } else if(dayID == DAYS_TUE) {
		day_tue = value;
	  } else if(dayID == DAYS_WED) {
		day_wed = value;
	  } else if(dayID == DAYS_THUR) {
		day_thur = value;
	  } else if(dayID == DAYS_FRI) {
		day_fri = value;
	  } else if(dayID == DAYS_SAT) {
		day_sat = value;
	  }
   }

   public boolean getDayValue(int dayID)
   {
      if(dayID == DAYS_SUN) {
		return day_sun;
	  } else if(dayID == DAYS_MON) {
		return day_mon;
	  } else if(dayID == DAYS_TUE) {
		return day_tue;
	  } else if(dayID == DAYS_WED) {
		return day_wed;
	  } else if(dayID == DAYS_THUR) {
		return day_thur;
	  } else if(dayID == DAYS_FRI) {
		return day_fri;
	  } else if(dayID == DAYS_SAT) {
		return day_sat;
	  } else {
		return false;
	  }
   }

   public void setSpanData(int fromHour, int fromMin, int toHour, int toMin, int spanType)
   {
      span_from_hour  = fromHour;
      span_from_min = fromMin;
      span_to_hour = toHour;
      span_to_min = toMin;
      // Calvi changes.
      span_type = spanType;
   }

   public int getSpanFromHour()
   {
      return span_from_hour;
   }

   public int getSpanFromMin()
   {
      return span_from_min;
   }

   public int getSpanToHour()
   {
      return span_to_hour;
   }

   public int getSpanToMin()
   {
      return span_to_min;
   }
   // Calvi changes.
   public int getSpanType()
   {
      return span_type;
   }

   public void setTextSearchString(String text)
   {
      searchText = text;
   }

   public void setTextSearchData(String text, int f, boolean ex, int fl)
   {
      searchText = text;
      field = f;
      exists = ex;
      flags = fl;
   }

   public String getTextSearch()
   {
      return searchText;
   }

   public int getField()
   {
      return field;
   }

   public boolean getExists()
   {
      return exists;
   }

   public int getFlags()
   {
      return flags;
   }

   public boolean isTextMatch(GuideItem item, String channel)
   {
      String searchString = searchText;
      Vector<String> text = new Vector<>();

      if(field == FIELD_TITLE) {
		text.add(item.getName());
	  } else if(field == FIELD_SUBTITLE) {
		text.add(item.getSubName());
	  } else if(field == FIELD_DESCRIPTION) {
		text.add(item.getDescription());
	  } else if(field == FIELD_TITSUBDESC) {
		// Strip newline characters, not necessary if (?s) used.
         // text.add((item.getName()+item.getSubName()+item.getDescription()).replaceAll("\\s\\s+|\\n|\\r", " "));
         text.add(item.getName()+item.getSubName()+item.getDescription());
	  } else if(field == FIELD_ACTOR) {
		text.addAll(item.getActors());
	  } else if(field == FIELD_CHANNEL) {
		text.add(channel);
	  } else if(field == FIELD_CATEGORY) {
		text.addAll(item.getCategory());
	  }

      if(flags == FLAG_CASEINSENSITIVE)
      {
         Vector<String> newText = new Vector<>();
         for(int x = 0; x < text.size(); x++)
         {
            newText.add(text.get(x).toUpperCase());
         }
         text = newText;

         searchString = searchString.toUpperCase();
      }

      boolean matchFound = false;
      if(flags == FLAG_REGEX)
      {
         // do regex
         for (String data : text) {
            matchFound = Pattern.matches(searchString, data);
            if(matchFound) {
				break;
			}
         }
      }
      else
      {
         for (String data : text) {
            // to text search
            if(searchString.startsWith("*") && searchString.endsWith("*"))
            {
               String search = searchString.substring(1, searchString.length() - 1);
               if(data.indexOf(search) > -1)
               {
                  matchFound = true;
                  break;
               }
            }
            else if(searchString.startsWith("*"))
            {
               String search = searchString.substring(1, searchString.length());
               matchFound = data.endsWith(search);
               if(matchFound) {
				break;
			   }
            }
            else if(searchString.endsWith("*"))
            {
               String search = searchString.substring(0, searchString.length()-1);
               matchFound = data.startsWith(search);
               if(matchFound) {
				break;
			   }
            }
            else
            {
               matchFound = data.equals(searchString);
               if(matchFound) {
				break;
			   }
            }
         }
      }

      // now work out if we have a match or not and if that is what we wanted
      if(exists && matchFound) {
		return true;
	  } else if(!exists && !matchFound) {
		return true;
	  } else {
		return false;
	  }
   }

   public boolean isSpanMatch(GuideItem item)
   {
      int fromMinInDay = (span_from_hour * 60) + span_from_min;
      int toMinInDay = (span_to_hour * 60) + span_to_min;

      int span = 0;
      if(fromMinInDay < toMinInDay)
      {
         span = toMinInDay - fromMinInDay;
      }
      else if(toMinInDay < fromMinInDay)
      {
         span = (24 * 60) - fromMinInDay;
         span += toMinInDay;
      }

      //System.out.println("Span = " + span);

      Calendar cal = Calendar.getInstance();
      cal.setTime(item.getStart());

      Calendar start = Calendar.getInstance();
      start.setTime(item.getStart());
      start.set(Calendar.HOUR_OF_DAY, span_from_hour);
      start.set(Calendar.MINUTE, span_from_min);

      Calendar end = Calendar.getInstance();
      end.setTime(start.getTime());
      end.add(Calendar.MINUTE, span);

      // Calvi changes.
      if(span_type == 0 && cal.after(start) && cal.before(end)) {
		return true;
	  } else if(span_type == 0 && (cal.equals(start) || cal.equals(end))) {
		return true;
	  } else if(span_type != 0 && item.getDuration() >= fromMinInDay && (item.getDuration() <= toMinInDay || toMinInDay == 0)) {
		return true;
	  } else {
		return false;
	  }
   }

   public boolean isDayMatch(GuideItem item)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(item.getStart());

      int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

      if(dayOfWeek == Calendar.SUNDAY && day_sun) {
		return true;
	  } else if(dayOfWeek == Calendar.MONDAY && day_mon) {
		return true;
	  } else if(dayOfWeek == Calendar.TUESDAY && day_tue) {
		return true;
	  } else if(dayOfWeek == Calendar.WEDNESDAY && day_wed) {
		return true;
	  } else if(dayOfWeek == Calendar.THURSDAY && day_thur) {
		return true;
	  } else if(dayOfWeek == Calendar.FRIDAY && day_fri) {
		return true;
	  } else if(dayOfWeek == Calendar.SATURDAY && day_sat) {
		return true;
	  } else {
		return false;
	  }
   }

   public void getXML(Document doc, Element item)
   {
      Element mainElm = null;
      mainElm = doc.createElement("MatchListItem");
      mainElm.setAttribute("type", Integer.valueOf(type).toString());

      Element elm = null;
      Text text = null;

      if(type == EpgMatchListItem.TYPE_TEXT)
      {
         elm = doc.createElement("TEXT");
         elm.setAttribute("field", Integer.valueOf(field).toString());
         elm.setAttribute("exists", Boolean.valueOf(exists).toString());
         elm.setAttribute("flags", Integer.valueOf(flags).toString());
         text = doc.createTextNode(searchText);
         elm.appendChild(text);
         mainElm.appendChild(elm);
      }
      else if(type == EpgMatchListItem.TYPE_SPAN)
      {
         elm = doc.createElement("SPAN");
         elm.setAttribute("span_from_hour", Integer.valueOf(span_from_hour).toString());
         elm.setAttribute("span_to_hour", Integer.valueOf(span_to_hour).toString());
         elm.setAttribute("span_from_min", Integer.valueOf(span_from_min).toString());
         elm.setAttribute("span_to_min", Integer.valueOf(span_to_min).toString());
         // Calvi changes.
         elm.setAttribute("span_type", Integer.valueOf(span_type).toString());
         mainElm.appendChild(elm);
      }
      else if(type == EpgMatchListItem.TYPE_DAYS)
      {
         elm = doc.createElement("DAYS");
         elm.setAttribute("sun", Boolean.valueOf(day_sun).toString());
         elm.setAttribute("mon", Boolean.valueOf(day_mon).toString());
         elm.setAttribute("tue", Boolean.valueOf(day_tue).toString());
         elm.setAttribute("wed", Boolean.valueOf(day_wed).toString());
         elm.setAttribute("thur", Boolean.valueOf(day_thur).toString());
         elm.setAttribute("fri", Boolean.valueOf(day_fri).toString());
         elm.setAttribute("sat", Boolean.valueOf(day_sat).toString());
         mainElm.appendChild(elm);
      }
      else if(type == EpgMatchListItem.TYPE_FLAG)
      {
         elm = doc.createElement("FLAG");
         elm.setAttribute("flagID", Integer.valueOf(flagID).toString());
         elm.setAttribute("value", Boolean.valueOf(flagValue).toString());
         mainElm.appendChild(elm);
      }

      item.appendChild(mainElm);
   }

   private void parseXML(Node node)
   {
      NamedNodeMap itemAttribs = node.getAttributes();
      type = Integer.parseInt(itemAttribs.getNamedItem("type").getTextContent());

      NodeList nl = node.getChildNodes();
      Node found = null;

      for(int x = 0; x < nl.getLength(); x++)
      {
         found = nl.item(x);
         if(type == EpgMatchListItem.TYPE_TEXT && found.getNodeName().equals("TEXT"))
         {
            itemAttribs = found.getAttributes();
            field = Integer.parseInt(itemAttribs.getNamedItem("field").getTextContent());
            exists = Boolean.parseBoolean(itemAttribs.getNamedItem("exists").getTextContent());
            flags = Integer.parseInt(itemAttribs.getNamedItem("flags").getTextContent());
            searchText = found.getTextContent();
         }
         else if(type == EpgMatchListItem.TYPE_SPAN && found.getNodeName().equals("SPAN"))
         {
            itemAttribs = found.getAttributes();
            span_from_hour = Integer.parseInt(itemAttribs.getNamedItem("span_from_hour").getTextContent());
            span_to_hour = Integer.parseInt(itemAttribs.getNamedItem("span_to_hour").getTextContent());
            span_from_min = Integer.parseInt(itemAttribs.getNamedItem("span_from_min").getTextContent());
            span_to_min = Integer.parseInt(itemAttribs.getNamedItem("span_to_min").getTextContent());
            // Calvi changes.
            try
            {
              span_type = Integer.parseInt(itemAttribs.getNamedItem("span_type").getTextContent());
            }
            catch(Exception e)
            {
              span_type = 0;
            }

         }
         else if(type == EpgMatchListItem.TYPE_DAYS && found.getNodeName().equals("DAYS"))
         {
            itemAttribs = found.getAttributes();
            day_sun = Boolean.parseBoolean(itemAttribs.getNamedItem("sun").getTextContent());
            day_mon = Boolean.parseBoolean(itemAttribs.getNamedItem("mon").getTextContent());
            day_tue = Boolean.parseBoolean(itemAttribs.getNamedItem("tue").getTextContent());
            day_wed = Boolean.parseBoolean(itemAttribs.getNamedItem("wed").getTextContent());
            day_thur = Boolean.parseBoolean(itemAttribs.getNamedItem("thur").getTextContent());
            day_fri = Boolean.parseBoolean(itemAttribs.getNamedItem("fri").getTextContent());
            day_sat = Boolean.parseBoolean(itemAttribs.getNamedItem("sat").getTextContent());
         }
         else if(type == EpgMatchListItem.TYPE_FLAG && found.getNodeName().equals("FLAG"))
         {
            itemAttribs = found.getAttributes();
            flagID = Integer.parseInt(itemAttribs.getNamedItem("flagID").getTextContent());
            flagValue = Boolean.parseBoolean(itemAttribs.getNamedItem("value").getTextContent());
         }
      }
   }
}
