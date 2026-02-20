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

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EpgMatchList
{
   private Vector<EpgMatchListItem> matchList = new Vector<>();

   public EpgMatchList()
   {
   }

   public EpgMatchList(Node node)
   {
      parseXML(node);
   }

   public Vector<EpgMatchListItem> getMatchList()
   {
      return matchList;
   }

   public boolean isMatch(GuideItem item, String channel)
   {
      if(matchList.size() == 0) {
		return false;
	  }

      for (EpgMatchListItem matcher : matchList) {
         if(matcher.getType() == EpgMatchListItem.TYPE_TEXT)
         {
            if(!matcher.isTextMatch(item, channel)) {
				return false;
			}
         }
         else if(matcher.getType() == EpgMatchListItem.TYPE_SPAN)
         {
            if(!matcher.isSpanMatch(item)) {
				return false;
			}
         }
         else if(matcher.getType() == EpgMatchListItem.TYPE_DAYS)
         {
            if(!matcher.isDayMatch(item)) {
				return false;
			}
         }
         else if(matcher.getType() == EpgMatchListItem.TYPE_FLAG)
         {
            if(!matcher.isFlagMatch(item)) {
				return false;
			}
         }
      }

      return true;
   }

   public void getXML(Document doc, String name)
   {
      Element item = doc.createElement("MatchList");

      item.setAttribute("name", name);

      for (EpgMatchListItem matcher : matchList) {
         matcher.getXML(doc, item);
      }

      Element root = doc.getDocumentElement();
      root.appendChild(item);
   }

   private void parseXML(Node node)
   {
      NodeList nl = node.getChildNodes();
      Node found = null;

      for(int x = 0; x < nl.getLength(); x++)
      {
         found = nl.item(x);
         if(found.getNodeName().equals("MatchListItem"))
         {
            matchList.add(new EpgMatchListItem(found));
         }
      }
   }
}