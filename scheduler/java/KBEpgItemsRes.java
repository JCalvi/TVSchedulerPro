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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class KBEpgItemsRes extends HTTPResponse
{

   public KBEpgItemsRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream, HashMap<String, String> headers) throws Exception
   {
      if("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showNames(urlData, headers));
         return;
      }
      else if("02".equals(urlData.getParameter("action")))
      {
         outStream.write(showItemInstances(urlData, headers));
         return;
      }
      else if("03".equals(urlData.getParameter("action")))
      {
         outStream.write(showSearchForm(urlData, headers));
         return;
      }
      else if("04".equals(urlData.getParameter("action")))
      {
         outStream.write(showSearchResults(urlData, headers));
         return;
      }
      else
      {
         outStream.write(showFirstLetter(urlData, headers));
         return;
      }
   }

   private byte[] showItemInstances(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String name = urlData.getParameter("name");
      if(name == null) {
		name = "";
	  }

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("title" , name);

      root.setAttribute("start" , start);
      root.setAttribute("show" , show);

      root.setAttribute("back" , "/servlet/ApplyTransformRes?xml=epg-index&xsl=kb-buttons" + "&start=" + start + "&show=" + show);

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("mainurl");
      text = doc.createTextNode("/servlet/KBEpgItemsRes?action=02&name=" + URLEncoder.encode(name, "UTF-8") + "&start=" + start + "&show=" + show + "&");
      button.appendChild(text);
      root.appendChild(button);

      SimpleDateFormat df = new SimpleDateFormat("EEE MMM d h:mm aa");

      GuideStore guide = GuideStore.getInstance();
      Vector<String[]> chanMap = guide.getChannelMap();

      int total = 0;

      for (String[] map : chanMap) {
         GuideItem[] items = guide.getItems(name, map[1]);

         for (GuideItem item : items) {
            String butText = df.format(item.getStart());
            if(map[0] != null) {
				butText += " (" + map[0] + ")";
			} else {
				butText += " (Not Mapped)";
			}

            button = doc.createElement("button");
            button.setAttribute("name", butText);
            elm = doc.createElement("url");

            Calendar cal = Calendar.getInstance();
            cal.setTime(item.getStart());

            String buttonUrl = "/servlet/KBEpgDataRes?action=05&channel=" +
               URLEncoder.encode(map[0], "UTF-8") +
               "&id=" + item.toString() + "&start=" + start + "&show=" + show;

            text = doc.createTextNode(buttonUrl);
            elm.appendChild(text);
            button.appendChild(elm);
            root.appendChild(button);

            total++;
         }
      }

      root.setAttribute("total" , Integer.valueOf(total).toString());

      // send back data
      XSL transformer = new XSL(doc, "kb-list.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showSearchForm(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {

      String start = urlData.getParameter("start");
      if(start == null || start.length() == 0) {
		start = "0";
	  }

      String show = urlData.getParameter("show");
      if(show == null || show.length() == 0) {
		show = "10";
	  }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "search", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("back" , "/servlet/ApplyTransformRes?xml=epg-index&xsl=kb-buttons" + "&start=" + start + "&show=" + show);

      //Channel
      Element formEl = doc.createElement("channel");
      getChannelList(doc, formEl);
      formEl.setAttribute("value", "Any");
      root.appendChild(formEl);

      // Category
      formEl = doc.createElement("category");
      getCatList(doc, formEl);
      formEl.setAttribute("value", "Any");
      root.appendChild(formEl);

      //referrer
      formEl = doc.createElement("referrer");
      Text text = doc.createTextNode("/servlet/KBAutoAddRes" + "?start=" + start + "&show=" + show);
      formEl.appendChild(text);
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

      // send back data
      XSL transformer = new XSL(doc, "kb-SearchEpg.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private void getChannelList(Document doc, Element formEl)
   {
      Element option = null;
      Text text = null;

      HashMap<String, Channel> channels = store.getChannels();
      String[] keys = channels.keySet().toArray(new String[0]);
      Arrays.sort(keys);

      option = doc.createElement("option");
      text = doc.createTextNode("Any");
      option.appendChild(text);
      formEl.appendChild(option);

      for (String key : keys) {
         option = doc.createElement("option");
         text = doc.createTextNode(key);
         option.appendChild(text);
         formEl.appendChild(option);
      }

   }

   private void getCatList(Document doc, Element formEl)
   {
	   Element option = null;
	   Text text = null;

      GuideStore guide = GuideStore.getInstance();
	    String[] cats = guide.getCategoryStrings();

      option = doc.createElement("option");
      text = doc.createTextNode("Any");
      option.appendChild(text);
      formEl.appendChild(option);

	   for (String cat : cats) {
	      option = doc.createElement("option");
	      text = doc.createTextNode(cat);
	      option.appendChild(text);
	      formEl.appendChild(option);
	   }
   }

   private byte[] showSearchResults(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String name = urlData.getParameter("name");
      if(name == null || name.length() == 0) {
		name = "";
	  }

      String type = urlData.getParameter("type");
      if(type == null || type.length() == 0) {
		type = "Title/Sub/Desc";
	  }

      int searchType = 0;
      if("title/sub/desc".equalsIgnoreCase(type)) {
		searchType = 0;
	  } else if("title".equalsIgnoreCase(type)) {
		searchType = 1;
	  } else if("subtitle".equalsIgnoreCase(type)) {
		searchType = 2;
	  } else if("description".equalsIgnoreCase(type)) {
		searchType = 3;
	  } else if("actor".equalsIgnoreCase(type)) {
		searchType = 4;
	  } else {
		searchType = 0;
	  }

      String cat = urlData.getParameter("cat");
      if(cat == null || cat.length() == 0) {
		cat = "any";
	  }

      String chan = urlData.getParameter("chan");
      if(chan == null || chan.length() == 0) {
		chan = "any";
	  }

      String start = urlData.getParameter("start");
      if(start == null || start.length() == 0) {
		start = "0";
	  }

      String show = urlData.getParameter("show");
      if(show == null || show.length() == 0) {
		show = "10";
	  }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("back" , "/servlet/KBEpgItemsRes?action=03" + "&start=" + start + "&show=" + show);

      root.setAttribute("start" , start);
      root.setAttribute("show" , show);

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("mainurl");
      String actionText = "/servlet/KBEpgItemsRes?action=04" +
         "&name=" + URLEncoder.encode(name, "UTF-8") +
         "&type=" + URLEncoder.encode(type, "UTF-8") +
         "&cat=" + URLEncoder.encode(cat, "UTF-8") +
         "&chan=" + URLEncoder.encode(chan, "UTF-8") + "&start=" + start + "&show=" + show + "&";
      text = doc.createTextNode(actionText);
      button.appendChild(text);
      root.appendChild(button);

      actionText = "(" + name + ") Category(" + cat + ") Channel(" + chan + ")";
      root.setAttribute("title", actionText);

      GuideStore guide = GuideStore.getInstance();
      HashMap<String, Vector<GuideItem>> results = new HashMap<>();
      guide.simpleEpgSearch(name, searchType, cat, chan, 0, null, results);

      String[] keys = results.keySet().toArray(new String[0]);
      int total = 0;

      for (String key : keys) {
         Vector<GuideItem> result = results.get(key);

         if (result != null && result.size() > 0)
         {
            for (GuideItem item : result) {
               button = doc.createElement("button");
               // Calvi changes.
               button.setAttribute("name" , item.getName() + " (" + key + ")");
               elm = doc.createElement("url");

               String actionURL = "/servlet/KBEpgDataRes?action=05&channel=" +
               URLEncoder.encode(key, "UTF-8") +
               "&id=" + item.toString() + "&start=" + start + "&show=" + show;

               text = doc.createTextNode(actionURL);
               elm.appendChild(text);
               button.appendChild(elm);
               root.appendChild(button);

               total++;
            }
         }
      }

      root.setAttribute("total" , Integer.valueOf(total).toString());

      // send back data
      XSL transformer = new XSL(doc, "kb-list.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showNames(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      String letter = urlData.getParameter("letter");

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }

      GuideStore guide = GuideStore.getInstance();
      String[] progs = guide.getNamesStartingWith(letter);

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("start" , start);
      root.setAttribute("show" , show);
      root.setAttribute("back" , "/servlet/KBEpgItemsRes" + "&start=" + start + "&show=" + show);

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("mainurl");
      text = doc.createTextNode("/servlet/KBEpgItemsRes?action=01&letter=" + letter + "&start=" + start + "&show=" + show + "&");
      button.appendChild(text);
      root.appendChild(button);

      int total = 0;

      for (String prog : progs) {
         button = doc.createElement("button");
         button.setAttribute("name" , prog);
         elm = doc.createElement("url");

         String actionURL = "/servlet/KBEpgItemsRes?action=02&name=" + URLEncoder.encode(prog, "UTF-8") + "&start=" + start + "&show=" + show;

         text = doc.createTextNode(actionURL);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);

         total++;
      }

      root.setAttribute("total" , Integer.valueOf(total).toString());

      // send back data
      XSL transformer = new XSL(doc, "kb-list.xsl", urlData, headers);
      return transformer.doTransform();
   }

   private byte[] showFirstLetter(HTTPurl urlData, HashMap<String, String> headers) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();
      String[] firsts = guide.getFirstLetters();

      String start = urlData.getParameter("start");
      if(start == null) {
		start = "0";
	  }

      String show = urlData.getParameter("show");
      if(show == null) {
		show = "10";
	  }

      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "buttons", null);

      Element root = doc.getDocumentElement();

      root.setAttribute("start" , start);
      root.setAttribute("show" , show);

      root.setAttribute("back" , store.getTargetURL("epg-sub", "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons" + "&start=" + start + "&show=" + show));

      Element button = null;
      Element elm = null;
      Text text = null;

      button = doc.createElement("mainurl");
      text = doc.createTextNode("/servlet/KBEpgItemsRes?" + "&start=" + start + "&show=" + show);
      button.appendChild(text);
      root.appendChild(button);

      int total = 0;

      if(firsts.length == 0)
      {
         button = doc.createElement("button");
         button.setAttribute("name" , "None");
         elm = doc.createElement("url");
         text = doc.createTextNode("/servlet/KBEpgItemsRes" + "&start=" + start + "&show=" + show);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);

         total++;
      }

      for (String first : firsts) {
         button = doc.createElement("button");
         button.setAttribute("name" , first);
         elm = doc.createElement("url");
         text = doc.createTextNode("/servlet/KBEpgItemsRes?action=01&letter=" + first + "&start=" + start + "&show=" + show);
         elm.appendChild(text);
         button.appendChild(elm);
         root.appendChild(button);

         total++;
      }

      root.setAttribute("total" , Integer.valueOf(total).toString());

      // send back data
      XSL transformer = new XSL(doc, "kb-list.xsl", urlData, headers);
      return transformer.doTransform();
   }
}
