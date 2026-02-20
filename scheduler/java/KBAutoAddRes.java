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

import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class KBAutoAddRes extends HTTPResponse
{

   public KBAutoAddRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if ("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showAutoEpgAddForm(urlData, headers));
         return;
      }
      if ("02".equals(urlData.getParameter("action")))
      {
         outStream.write(showAutoEpgOptions(urlData, headers));
         return;
      }
      if ("03".equals(urlData.getParameter("action")))
      {
         outStream.write(runAutoAddTest(urlData, headers));
         return;
      }
      if ("04".equals(urlData.getParameter("action")))
      {
         outStream.write(moveAutoAddItem(urlData, headers));
         return;
      }
      if ("05".equals(urlData.getParameter("action")))
      {
         outStream.write(remAutoAddItem(urlData, headers));
         return;
      }
      if ("06".equals(urlData.getParameter("action")))
      {
         outStream.write(addAutoEpgString(urlData));
         return;
      }
      else if ("07".equals(urlData.getParameter("action")))
      {
         outStream.write(enableEpgMatchItem(urlData));
         return;
      }
      else if ("08".equals(urlData.getParameter("action")))
      {
         outStream.write(showMatchListMenu(urlData, headers));
         return;
      }
      else if ("09".equals(urlData.getParameter("action")))
      {
         outStream.write(showAddMatchList(urlData, headers));
         return;
      }
      else if ("10".equals(urlData.getParameter("action")))
      {
         outStream.write(showDelMatchList(urlData, headers));
         return;
      }
      else if ("11".equals(urlData.getParameter("action")))
      {
         outStream.write(delMatchList(urlData, headers));
         return;
      }
      else if ("12".equals(urlData.getParameter("action")))
      {
         outStream.write(addMatchList(urlData, headers));
         return;
      }
      else if ("13".equals(urlData.getParameter("action")))
      {
         outStream.write(createAutoAddFromItem(urlData, headers));
         return;
      }
      else
      {
         outStream.write(getAutoAddTable(urlData, headers));
         return;
      }
   }

   private byte[] createAutoAddFromItem(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String backURL = urlData.getCookie("backURL");

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      try
      {
         backURL = URLDecoder.decode(backURL, "UTF-8");
      }
      catch(Exception e){}

      HashMap<String, EpgMatchList> matchLists = store.getMatchLists();

      String itemID = urlData.getParameter("itemID");
      String wsChan = urlData.getParameter("chan");

      GuideStore guide = GuideStore.getInstance();
      String epgChan = guide.getEpgChannelFromMap(wsChan);

      GuideItem item = guide.getProgram(epgChan, itemID);

      if(item == null)
      {
         StringBuffer out = new StringBuffer(256);
         out.append("HTTP/1.0 302 Moved Temporarily\n");
         out.append("Location: /servlet/" + urlData.getServletClass() + "?action=01" + "&start=" + start + "&show=" + show + "\n\n");
         return out.toString().getBytes();
      }

      String name = item.getName();
      int nextIndex = 0;
      boolean useInt = false;

      boolean AAChan = store.getProperty("Schedule.aachan").equals("1");
      boolean AARpt = store.getProperty("Schedule.aarpt").equals("1");

      String matchListName = name;

      if(matchLists.containsKey(matchListName))
      {
         useInt = true;
         while(matchLists.containsKey(matchListName + "_" + nextIndex))
         {
            nextIndex++;
         }
      }

      if(useInt) {
		matchListName = matchListName + "_" + nextIndex;
	  }

      EpgMatchList newMatchList = new EpgMatchList();
      Vector<EpgMatchListItem> items = newMatchList.getMatchList();

      EpgMatchListItem newItemTitle = new EpgMatchListItem(EpgMatchListItem.TYPE_TEXT);
      newItemTitle.setTextSearchData(name, EpgMatchListItem.FIELD_TITLE, true, EpgMatchListItem.FLAG_CASEINSENSITIVE);
      items.add(newItemTitle);

      if(AAChan)
      {
        EpgMatchListItem newItemChan = new EpgMatchListItem(EpgMatchListItem.TYPE_TEXT);
        newItemChan.setTextSearchData(wsChan, EpgMatchListItem.FIELD_CHANNEL, true, EpgMatchListItem.FLAG_CASEINSENSITIVE);
        items.add(newItemChan);
      }
      if(AARpt)
      {
        EpgMatchListItem newItemRpt = new EpgMatchListItem(EpgMatchListItem.TYPE_FLAG);
        newItemRpt.setFlagData(EpgMatchListItem.ITEM_FLAG_REPEAT, false);
        items.add(newItemRpt);
      }

      matchLists.put(matchListName, newMatchList);

      // save out new MatchList
      store.saveMatchList(null);

      // create the new AA item
      EpgMatch epgMatch = new EpgMatch();
      epgMatch.getMatchListNames().add(matchListName);

      int keepFor = 30;
      try
      {
         keepFor = Integer.parseInt(store.getProperty("autodel.keepfor"));
      }
      catch (Exception e){}

      epgMatch.setKeepFor(keepFor);
      epgMatch.setAutoDel(false);

      int startBuff = 0;
      int endBuffer = 0;
      try
      {
         startBuff = Integer.parseInt(store.getProperty("schedule.buffer.start"));
         endBuffer = Integer.parseInt(store.getProperty("schedule.buffer.end"));
      }
      catch (Exception e){}
      epgMatch.setStartBuffer(startBuff);
      epgMatch.setEndBuffer(endBuffer);

      epgMatch.setPostTask(store.getProperty("tasks.deftask"));

      String[] namePatterns = store.getNamePatterns();
      if(namePatterns.length > 0)
      {
         epgMatch.setFileNamePattern(namePatterns[0]);
      }
      else
      {
         epgMatch.setFileNamePattern("(%y-%m-%d %h-%M) %n %c");
      }

      epgMatch.setCaptureType(-1);

      // save new auto-add
      store.addEpgMatch(epgMatch, 0);


      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();
      root.setAttribute("back", "/servlet/KBAutoAddRes" + "?start=" + start + "&show=" + show);

      root.setAttribute("title" , "The Auto-Add item was created and saved. You should probably run the Auto-Add scan now to add any programs that match your new Auto-Add item to the schedule list.");

      Element button = null;
      Element elm = null;
      Text text = null;

      // rescan
      button = doc.createElement("button");
      button.setAttribute("name", "Run Auto-Add Scan Now");
      elm = doc.createElement("url");
      text = doc.createTextNode("/servlet/KBEpgDataRes?action=04" + "&start=" + start + "&show=" + show);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      // back to EPG
      button = doc.createElement("button");
      button.setAttribute("name", "Return to the EPG");
      elm = doc.createElement("url");
      text = doc.createTextNode(backURL);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] addMatchList(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      EpgMatch item = store.getEpgMatchList().get(index);
      if(item == null)
      {
        String out = "HTTP/1.0 302 Moved Temporarily\nLocation: " +
        "/servlet/" + urlData.getServletClass() +  "?start=" + start + "&show=" + show + "\n\n";

         return out.getBytes();
      }

      String name = urlData.getParameter("name");

      if(name != null)
      {
         if(!item.getMatchListNames().contains(name))
         {
            item.getMatchListNames().add(name);
            store.saveMatchList(null);
         }
      }

      StringBuffer buff = new StringBuffer(256);

      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show + "\n\n");

      return buff.toString().getBytes();
   }

   private byte[] delMatchList(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      EpgMatch item = store.getEpgMatchList().get(index);
      if(item == null)
      {
        String out = "HTTP/1.0 302 Moved Temporarily\nLocation: " +
        "/servlet/" + urlData.getServletClass() +  "?start=" + start + "&show=" + show + "\n\n";

         return out.getBytes();
      }

      String name = urlData.getParameter("name");

      if(name != null)
      {
         item.getMatchListNames().remove(name);
         store.saveMatchList(null);
      }

      StringBuffer buff = new StringBuffer(256);

      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show + "\n\n");

      return buff.toString().getBytes();
   }

   private byte[] showDelMatchList(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      EpgMatch item = store.getEpgMatchList().get(index);
      if(item == null)
      {
        String out = "HTTP/1.0 302 Moved Temporarily\nLocation: " +
        "/servlet/" + urlData.getServletClass() +  "?start=" + start + "&show=" + show + "\n\n";

         return out.getBytes();
      }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("start" , start);
      root.setAttribute("show" , show);

      root.setAttribute("back" , "/servlet/" + urlData.getServletClass() +
         "?action=08&index=" + index + "&start=" + start + "&show=" + show);

      root.setAttribute("title" , "Select a Match List to Delete it");

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("mainurl");
      text = doc.createTextNode("/servlet/" + urlData.getServletClass() + "?action=10&index=" + index + "&start=" + start + "&show=" + show + "&");
      button.appendChild(text);
      root.appendChild(button);

      String[] keys = item.getMatchListNames().toArray(new String[0]);
      Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);
      int total = 0;

      for (String key : keys) {
         String action = "/servlet/KBAutoAddRes?action=11&index=" + index + "&name=" +
            URLEncoder.encode(key, "UTF-8") + "&start=" + start + "&show=" + show;

         button = doc.createElement("button");
         button.setAttribute("name", key);
         elm = doc.createElement("url");
         text = doc.createTextNode(action);
         elm.appendChild(text);
         button.appendChild(elm);
         elm = doc.createElement("confirm");
         text = doc.createTextNode("true");
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);
         total++;
      }

      root.setAttribute("total" , Integer.valueOf(total).toString());

      //
      // Do transform and return data
      //
      XSL transformer = new XSL(doc, "kb-list.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showAddMatchList(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();


      EpgMatch item = store.getEpgMatchList().get(index);
      if(item == null)
      {
        String out = "HTTP/1.0 302 Moved Temporarily\nLocation: " +
        "/servlet/" + urlData.getServletClass() +  "?start=" + start + "&show=" + show + "\n\n";

         return out.getBytes();
      }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("start" , start);
      root.setAttribute("show" , show);

      root.setAttribute("back" , "/servlet/" + urlData.getServletClass() +
         "?action=08&index=" + index + "&start=" + start + "&show=" + show);

      root.setAttribute("title" , "Select a Match List to Add it");

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("mainurl");
      text = doc.createTextNode("/servlet/" + urlData.getServletClass() + "?action=09&index=" + index + "&start=" + start + "&show=" + show + "&");
      button.appendChild(text);
      root.appendChild(button);

      HashMap<String, EpgMatchList> matches = store.getMatchLists();
      String[] keys = matches.keySet().toArray(new String[0]);
      Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);

      int total = 0;

      for (String key : keys) {
         String action = "/servlet/KBAutoAddRes?action=12&index=" + index + "&name=" +
            URLEncoder.encode(key, "UTF-8") + "&start=" + start + "&show=" + show;

         button = doc.createElement("button");
         button.setAttribute("name", key);
         elm = doc.createElement("url");
         text = doc.createTextNode(action);
         elm.appendChild(text);
         button.appendChild(elm);
         elm = doc.createElement("confirm");
         text = doc.createTextNode("true");
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);
         total++;
      }

      root.setAttribute("total" , Integer.valueOf(total).toString());

      //
      // Do transform and return data
      //
      XSL transformer = new XSL(doc, "kb-list.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showMatchListMenu(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      EpgMatch item = store.getEpgMatchList().get(index);
      if(item == null)
      {
        String out = "HTTP/1.0 302 Moved Temporarily\nLocation: " +
        "/servlet/" + urlData.getServletClass() +  "?start=" + start + "&show=" + show + "\n\n";

         return out.getBytes();
      }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("back" , "/servlet/" + urlData.getServletClass() +
         "?action=02&index=" + index + "&start=" + start + "&show=" + show);

      root.setAttribute("title" , "Auto-Add Match List Menu");

      Element button = null;
      Element elm = null;
      Text text = null;
      String actionURL = "";

      button = doc.createElement("button");
      button.setAttribute("name" , "Back");
      elm = doc.createElement("url");
      actionURL = "/servlet/" + urlData.getServletClass() + "?action=02&index=" + index + "&start=" + start + "&show=" + show;
      text = doc.createTextNode(actionURL);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      if(item.getMatchListNames().size() > 0)
      {
         button = doc.createElement("button");
         button.setAttribute("name", "Show Current");
         elm = doc.createElement("url");
         actionURL = "/servlet/" + urlData.getServletClass() + "?action=10&index=" + index + "&start=" + start + "&show=" + show;
         text = doc.createTextNode(actionURL);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);
      }

      if(store.getMatchLists().size() > 0)
      {
         button = doc.createElement("button");
         button.setAttribute("name", "Add");
         elm = doc.createElement("url");
         actionURL = "/servlet/" + urlData.getServletClass() + "?action=09&index=" + index + "&start=" + start + "&show=" + show;
         text = doc.createTextNode(actionURL);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);
      }

      //
      // Do transform and return data
      //
      XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] enableEpgMatchItem(HTTPurl urlData) throws Exception
   {
      int indexOf = Integer.parseInt(urlData.getParameter("index"));

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      Vector<EpgMatch> list = store.getEpgMatchList();
      EpgMatch epgMatcher = list.get(indexOf);

      if(epgMatcher != null)
      {
         if("true".equals(urlData.getParameter("enabled"))) {
			epgMatcher.setEnabled(true);
		 } else {
			epgMatcher.setEnabled(false);
		 }

         store.saveEpgAutoList(null);
      }

      String out = "HTTP/1.0 302 Moved Temporarily\nLocation: " +
      "/servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show + "\n\n";

      return out.getBytes();
   }

   private byte[] addAutoEpgString(HTTPurl urlData) throws Exception
   {
      String ad = urlData.getParameter("autoDel");

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      boolean autoDel = false;
      if (ad != null && ad.equalsIgnoreCase("true")) {
		autoDel = true;
	  }

      int keepFor = 30;
      try
      {
         keepFor = Integer.parseInt(urlData.getParameter("keepFor"));
      }
      catch (Exception e)
      {
      }

      int startBuffer = 0;
      int endBuffer = 0;
      try
      {
         startBuffer = Integer.parseInt(urlData.getParameter("startbuffer"));
         endBuffer = Integer.parseInt(urlData.getParameter("endbuffer"));
      }
      catch (Exception e)
      {
      }

      String postTask = urlData.getParameter("task");
      if (postTask == null || postTask.equalsIgnoreCase("none")) {
		postTask = "";
	  }

      String namePattern = urlData.getParameter("filenamePatterns");
      if(namePattern == null || namePattern.length() == 0) {
		namePattern = "(%y-%m-%d %h-%M) %n %c";
	  }

      int capType = 0;
      try
      {
         capType = Integer.parseInt(urlData.getParameter("captype"));
      }
      catch (Exception e)
      {
      }

      int capPathIndex = -1;
      try
      {
         capPathIndex = Integer.parseInt(urlData.getParameter("capPath"));
      }
      catch (Exception e)
      {
      }

      EpgMatch epgMatch = null;

      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch (Exception e)
      {
      }

      if (index > -1)
      {
         epgMatch = store.getEpgMatchList().get(index);
      }
      else
      {
         epgMatch = new EpgMatch();
         store.addEpgMatch(epgMatch, 0);
      }

      epgMatch.setKeepFor(keepFor);
      epgMatch.setAutoDel(autoDel);

      epgMatch.setStartBuffer(startBuffer);
      epgMatch.setEndBuffer(endBuffer);
      epgMatch.setPostTask(postTask);
      epgMatch.setFileNamePattern(namePattern);
      epgMatch.setCaptureType(capType);
      epgMatch.setCapturePathIndex(capPathIndex);

      store.saveEpgAutoList(null);

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show + "\n\n");
      return buff.toString().getBytes();
   }

   private byte[] moveAutoAddItem(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int id = -1;
      try
      {
         id = Integer.parseInt(urlData.getParameter("id"));
      }
      catch(Exception e){}

      int dir = 1;
      try
      {
         dir = Integer.parseInt(urlData.getParameter("dir"));
      }
      catch(Exception e){}

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      boolean direction = false;
      if(dir == 1) {
		direction = true;
	  }

      if(id != -1) {
		store.moveEpgItem(id, direction);
	  }

      StringBuffer buff = new StringBuffer();
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show + "\n\n");

      return buff.toString().getBytes();
   }

   private byte[] runAutoAddTest(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = Integer.parseInt(urlData.getParameter("index"));
      EpgMatch epgMatcher = store.getEpgMatchList().get(index);

      HashMap<String, Vector<GuideItem>> results = new HashMap<>();

      Vector<String> matchNames = epgMatcher.getMatchListNames();
      HashMap<String, EpgMatchList> matchLists = store.getMatchLists();

      GuideStore guide = GuideStore.getInstance();

      EpgMatchList matcher = null;

      for (String matchListName : matchNames) {
         matcher = matchLists.get(matchListName);
         if(matcher != null)
         {
            guide.searchEPG(matcher, results);
         }
      }

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "buttons", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("id" , "");
      root.setAttribute("url" , "/servlet/KBAutoAddRes?action=02&index=" + index + "&start=" + start + "&show=" + show);
      root.setAttribute("filter" , "");

      Element logitem = null;
      Element elm = null;
      Text text = null;

      Vector<String[]> channelMap = guide.getChannelMap();
      Set<String> wsChannels = store.getChannels().keySet();

      SimpleDateFormat df = new SimpleDateFormat("EEE MMM d h:mm aa");

      int count = 0;

      for (String[] map : channelMap) {
         Vector<GuideItem> result = results.get(map[0]);

         if(result.size() > 0 && wsChannels.contains(map[0]))
         {
            logitem = doc.createElement("logitem");
            logitem.setAttribute("type", "1");
            elm = doc.createElement("line");
            text = doc.createTextNode(map[0]);
            elm.appendChild(text);
            logitem.appendChild(elm);
            root.appendChild(logitem);

            for (GuideItem item : result) {
               logitem = doc.createElement("logitem");
               logitem.setAttribute("type", "0");
               elm = doc.createElement("line");

               String matchText = item.getName();
               matchText += " (" + df.format(item.getStart(), new StringBuffer(), new FieldPosition(0)).toString() + ")";

               text = doc.createTextNode(matchText);
               elm.appendChild(text);
               logitem.appendChild(elm);
               root.appendChild(logitem);

               count++;
            }
         }
      }

      if(count == 0)
      {
         logitem = doc.createElement("logitem");
         logitem.setAttribute("type", "0");
         elm = doc.createElement("line");
         text = doc.createTextNode("No Hits");
         elm.appendChild(text);
         logitem.appendChild(elm);
         root.appendChild(logitem);
      }

   	//
   	// Do transform and return data
   	//
   	XSL transformer = new XSL(doc, "kb-searchtest.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showAutoEpgOptions(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      int index = -1;
      try
      {
         index = Integer.parseInt(urlData.getParameter("index"));
      }
      catch(Exception e){}

      EpgMatch epgMatcher = (store.getEpgMatchList().get(index));

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "buttons", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("back", "/servlet/KBAutoAddRes" + "?start=" + start + "&show=" + show);

      Element button = null;
      Element elm = null;
      Text text = null;

      // back button

      button = doc.createElement("button");
      button.setAttribute("name", "Back");
      elm = doc.createElement("url");
      text = doc.createTextNode("/servlet/KBAutoAddRes" + "?start=" + start + "&show=" + show);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      //enable disable
      button = doc.createElement("button");
      String action = "/servlet/KBAutoAddRes?action=07&index=" + index + "&start=" + start + "&show=" + show;

      if(epgMatcher.isEnabled())
      {
         button.setAttribute("name", "Disable");
         action += "&enabled=false";
      }
      else
      {
         button.setAttribute("name", "Enable");
         action += "&enabled=true";
      }

      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("false");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);


      // Match List Button

      action = "/servlet/" + urlData.getServletClass() + "?action=08&index=" + index + "&start=" + start + "&show=" + show;

      button = doc.createElement("button");
      button.setAttribute("name", "Match Lists");
      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      // Edit button

      action = "/servlet/" + urlData.getServletClass() + "?action=01&index=" + index + "&start=" + start + "&show=" + show;

      button = doc.createElement("button");
      button.setAttribute("name", "Edit Options");
      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      // show matches

      action = "/servlet/" + urlData.getServletClass() + "?action=03&index=" + index + "&start=" + start + "&show=" + show;

      button = doc.createElement("button");
      button.setAttribute("name", "Show Matches");
      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      //move up
      action = "/servlet/" + urlData.getServletClass() + "?action=04&id=" +
      	index + "&dir=0" + "&start=" + start + "&show=" + show;

      button = doc.createElement("button");
      button.setAttribute("name", "Move Up");
      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("false");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      //move down
      action = "/servlet/" + urlData.getServletClass() + "?action=04&id=" +
      	index + "&dir=1" + "&start=" + start + "&show=" + show;

      button = doc.createElement("button");
      button.setAttribute("name", "Move Down");
      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("false");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

      //delete
      action = "/servlet/KBAutoAddRes?action=05&id=" + index + "&start=" + start + "&show=" + show;

      button = doc.createElement("button");
      button.setAttribute("name", "Delete");
      elm = doc.createElement("url");
      text = doc.createTextNode(action);
      elm.appendChild(text);
      button.appendChild(elm);
      elm = doc.createElement("confirm");
      text = doc.createTextNode("true");
      elm.appendChild(text);
      button.appendChild(elm);
      root.appendChild(button);

   	//
   	// Do transform and return data
   	//
   	XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] remAutoAddItem(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String idString = urlData.getParameter("id");

      int id = -1;

      if (idString != null)
      {
         try
         {
            id = Integer.parseInt(idString);
         }
         catch (Exception e)
         {
         }
      }

      String all = urlData.getParameter("all");

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      if (id > -1)
      {
         EpgMatch item = store.getEpgMatchList().get(id);

         if(item != null)
         {
            String[] unUsed = noSharedMatchLists(item);

            if(unUsed.length == 0 || (unUsed.length > 0 && "0".equalsIgnoreCase(all)))
            {
               store.remEpgMatch(id);
            }
            else if(unUsed.length > 0 && "1".equalsIgnoreCase(all))
            {
               store.remEpgMatch(id);

               for (String element : unUsed) {
                  store.getMatchLists().remove(element);
               }
               store.saveMatchList(null);
            }
            else
            {

               // Creation of an XML document
               DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
               DocumentBuilder db = dbf.newDocumentBuilder();
               DOMImplementation di = db.getDOMImplementation();
               Document doc = di.createDocument("", "buttons", null);

               Element root = doc.getDocumentElement();

               root.setAttribute("back" , "/servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show);

               root.setAttribute("title" , "Delete any unused Match Lists as Well?");

               Element button = null;
               Element elm = null;
               Text text = null;
               String actionURL = "";

               button = doc.createElement("button");
               button.setAttribute("name" , "Yes");
               elm = doc.createElement("url");
               actionURL = "/servlet/" + urlData.getServletClass() + "?action=05&id=" + id + "&all=1"  + "&start=" + start + "&show=" + show;
               text = doc.createTextNode(actionURL);
               elm.appendChild(text);
               button.appendChild(elm);
               root.appendChild(button);

               button = doc.createElement("button");
               button.setAttribute("name" , "No");
               elm = doc.createElement("url");
               actionURL = "/servlet/" + urlData.getServletClass() + "?action=05&id=" + id + "&all=0"  + "&start=" + start + "&show=" + show;
               text = doc.createTextNode(actionURL);
               elm.appendChild(text);
               button.appendChild(elm);
               root.appendChild(button);

               //
               // Do transform and return data
               //
               XSL transformer = new XSL(doc, "kb-buttons.xsl", urlData, headers);
               return transformer.doTransform();

            }
         }
      }

      StringBuffer buff = new StringBuffer(256);
      buff.append("HTTP/1.0 302 Moved Temporarily\n");
      buff.append("Location: /servlet/" + urlData.getServletClass() + "?start=" + start + "&show=" + show + "\n\n");
      return buff.toString().getBytes();
   }

   private String[] noSharedMatchLists(EpgMatch item)
   {
      Vector<String> unUsed = new Vector<>();
      EpgMatch[] items = store.getEpgMatchList().toArray(new EpgMatch[0]);

      for(int x = 0; x < item.getMatchListNames().size(); x++)
      {
         boolean used = false;
         String name = item.getMatchListNames().get(x);

         for (EpgMatch item2 : items) {
            if(item != item2 && item2.getMatchListNames().contains(name)) {
				used = true;
			}
         }

         if(!used) {
			unUsed.add(name);
		 }
      }

      return unUsed.toArray(new String[0]);
   }

   private byte[] showAutoEpgAddForm(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      Vector<EpgMatch> list = store.getEpgMatchList();
      EpgMatch epgMatcher = null;

      String index = urlData.getParameter("index");
      if (index == null) {
		index = "";
	  }
      int indexOf = -1;

      try
      {
         indexOf = Integer.parseInt(index);
      }
      catch (Exception e)
      {
      }

      if (indexOf > -1 && indexOf < list.size())
      {
         epgMatcher = list.get(indexOf);
      }

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "item_form", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("index", Integer.valueOf(indexOf).toString());
      root.setAttribute("delete", "No");

      //
      // form elements
      //

      // Title

      Element formEl = null;

      //Start Buffer Minutes
      formEl = doc.createElement("startBuffer");
      formEl.setAttribute("Name", "Start");
      formEl.setAttribute("max", "59");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "1");
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", Integer.valueOf(epgMatcher.getStartBuffer()).toString());
	  } else {
		formEl.setAttribute("value", "5");
	  }
      root.appendChild(formEl);

      //End Buffer Minutes
      formEl = doc.createElement("endBuffer");
      formEl.setAttribute("Name", "End");
      formEl.setAttribute("max", "400");
      formEl.setAttribute("min", "0");
      formEl.setAttribute("amount", "5");
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", Integer.valueOf(epgMatcher.getEndBuffer()).toString());
	  } else {
		formEl.setAttribute("value", "10");
	  }
      root.appendChild(formEl);

      //Referrer
      formEl = doc.createElement("referrer");
      Text text = doc.createTextNode("/servlet/KBAutoAddRes" + "?start=" + start + "&show=" + show);
      formEl.appendChild(text);
      root.appendChild(formEl);

      //Capture Type
      formEl = doc.createElement("captureType");
      getCaptureTypes(doc, formEl);
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", Integer.valueOf(epgMatcher.getCaptureType()).toString());
	  } else {
		formEl.setAttribute("value", store.getProperty("capture.deftype"));
	  }
      root.appendChild(formEl);

      //Auto Delete
      formEl = doc.createElement("autoDel");
      formEl.setAttribute("Name", "Auto Delete");
      if (indexOf > -1 && indexOf < list.size()) {
		if (epgMatcher.getAutoDel()) {
			formEl.setAttribute("value", "True");
		 } else {
			formEl.setAttribute("value", "False");
		 }
	  } else {
		formEl.setAttribute("value", "False");
	  }
      root.appendChild(formEl);

      //Keep For
      formEl = doc.createElement("keepfor");
      formEl.setAttribute("Name", "keep For");
      formEl.setAttribute("max", "120");
      formEl.setAttribute("min", "1");
      formEl.setAttribute("amount", "1");
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", Integer.valueOf(epgMatcher.getKeepFor()).toString());
	  } else
      {
         String keep = store.getProperty("autoDel.keepfor");
         formEl.setAttribute("value", keep);
      }
      root.appendChild(formEl);

      //Post Task
      formEl = doc.createElement("posttask");
      getTaskList(doc, formEl);
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", epgMatcher.getPostTask());
	  } else {
		formEl.setAttribute("value", "");
	  }
      root.appendChild(formEl);

      //Filename Patterns
      formEl = doc.createElement("filenamePatterns");
      getNamePatterns(doc, formEl);
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", epgMatcher.GetFileNamePattern());
	  } else {
		formEl.setAttribute("value", "");
	  }
      root.appendChild(formEl);

      //Capture Paths
      formEl = doc.createElement("capturePaths");
      getCapturePaths(doc, formEl);
      if (indexOf > -1 && indexOf < list.size()) {
		formEl.setAttribute("value", Integer.valueOf(epgMatcher.getCapturePathIndex()).toString());
	  } else {
		formEl.setAttribute("value", "-1");
	  }
      root.appendChild(formEl);

      //Start
      formEl = doc.createElement("start");
      text = doc.createTextNode(start);
      formEl.appendChild(text);
      root.appendChild(formEl);

      //Show
      formEl = doc.createElement("show");
      text = doc.createTextNode(show);
      formEl.appendChild(text);
      root.appendChild(formEl);

   	//
   	// Do transform and return data
   	//
   	XSL transformer = new XSL(doc, "kb-aa-details.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] getAutoAddTable(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      Vector<EpgMatch> list = store.getEpgMatchList();
      EpgMatch epgMatcher = null;

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }
      start = start.trim();

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }
      show = show.trim();

      //
      // Creation of an XML document
      //
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "schedules", null);
      Element root = doc.getDocumentElement();

      root.setAttribute("start", start);
      root.setAttribute("show", show);
      root.setAttribute("total", Integer.valueOf(list.size()).toString());

      Element aa = null;
      Element elm = null;
      Text text = null;

      aa = doc.createElement("mainurl");
      text = doc.createTextNode("/servlet/KBAutoAddRes?");
      aa.appendChild(text);
      root.appendChild(aa);

      for (int x = 0; x < list.size(); x++)
      {
         epgMatcher = list.get(x);

         aa = doc.createElement("aaitem");
         aa.setAttribute("index", Integer.valueOf(x).toString());

         Vector<String> items = epgMatcher.getMatchListNames();
         String names = "";
         for(int q = 0; q < items.size(); q++)
         {
            String name = items.get(q);
            if(q == items.size()-1)
            {
               names += name;
            } else {
				names += name + ", ";
			}
         }

         if(names.length() == 0) {
			names = "No Match List Associated";
		 }

         elm = doc.createElement("title");
         text = doc.createTextNode(names);
         elm.appendChild(text);
         aa.appendChild(elm);

         // Is Enabled
         elm = doc.createElement("enabled");
         text = doc.createTextNode(Boolean.valueOf(epgMatcher.isEnabled()).toString());
         elm.appendChild(text);
         aa.appendChild(elm);

         //StartBuffer
         elm = doc.createElement("startbuffer");
         text = doc.createTextNode(Integer.valueOf(epgMatcher.getStartBuffer()).toString());
         elm.appendChild(text);
         aa.appendChild(elm);

         //EndBuffer
         elm = doc.createElement("endbuffer");
         text = doc.createTextNode(Integer.valueOf(epgMatcher.getEndBuffer()).toString());
         elm.appendChild(text);
         aa.appendChild(elm);

         //Capture Type
         elm = doc.createElement("capturetype");
         text = doc.createTextNode(Integer.valueOf(epgMatcher.getCaptureType()).toString());
         elm.appendChild(text);
         aa.appendChild(elm);

         String action = "/servlet/" + urlData.getServletClass() + "?action=02&index=" + URLEncoder.encode(Integer.valueOf(x).toString(), "UTF-8") + "&start=" + start + "&show=" + show;

         elm = doc.createElement("action");
         text = doc.createTextNode(action);
         elm.appendChild(text);
         aa.appendChild(elm);

         root.appendChild(aa);
      }

      // Do transform and return data
      XSL transformer = new XSL(doc, "kb-aa-list.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private String getCapturePaths(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);
      String[] capturePaths = store.getCapturePaths();

      option = doc.createElement("option");
      option.setAttribute("value", "-1");
      text = doc.createTextNode("AutoSelect");
      option.appendChild(text);
      formEl.appendChild(option);

      for (int x = 0; x < capturePaths.length; x++)
      {
         option = doc.createElement("option");
         option.setAttribute("value", Integer.valueOf(x).toString());
         text = doc.createTextNode(capturePaths[x]);
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }

   private String getNamePatterns(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);
      String[] namePatterns = store.getNamePatterns();

      for (String namePattern : namePatterns) {
         option = doc.createElement("option");
         text = doc.createTextNode(namePattern);
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }

   private String getCaptureTypes(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);

      option = doc.createElement("option");
      option.setAttribute("value", "-1");
      text = doc.createTextNode("AutoSelect");
      option.appendChild(text);
      formEl.appendChild(option);

      Vector<CaptureCapability> capabilities = CaptureCapabilities.getInstance().getCapabilities();

      for (CaptureCapability capability : capabilities) {
         option = doc.createElement("option");
         option.setAttribute("value", Integer.valueOf(capability.getTypeID()).toString());
         text = doc.createTextNode(capability.getName());
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }

   private String getTaskList(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      StringBuffer buff = new StringBuffer(1024);
      HashMap<String, TaskCommand> tasks = store.getTaskList();

      String[] keys = tasks.keySet().toArray(new String[0]);

      Arrays.sort(keys);

      option = doc.createElement("option");
      text = doc.createTextNode("none");
      option.appendChild(text);
      formEl.appendChild(option);

      for (String key : keys) {
         option = doc.createElement("option");
         text = doc.createTextNode(key);
         option.appendChild(text);
         formEl.appendChild(option);
      }

      return buff.toString();
   }
}
