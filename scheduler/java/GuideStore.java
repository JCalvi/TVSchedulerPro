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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class GuideStore
{
   private HashMap<String, HashMap<String, GuideItem>> progList = new HashMap<>();
   private Date maxEntry = new Date();
   private Date minEntry = new Date();
   private Vector<String[]> channelMap = new Vector<>();
   private Vector<String> categoryList = new Vector<>();
   private Vector<AddConflictDetails> conflictList = new Vector<>();

   private static GuideStore instance = null;

   private GuideStore()
   {
      loadChannelMap();
      loadEpg();
      loadCategories();
   }

   public static GuideStore getInstance()
   {
      synchronized (DataStore.class)
      {
         if (instance == null)
         {
            instance = new GuideStore();
            return instance;
         }
         else
         {
            return instance;
         }
      }
   }

   @Override
public String toString()
   {
      return "Programs Loaded:" + progList.size() + " Channel Maps Loads:" + channelMap.size() + " Categories Loaded:" + categoryList.size();
   }

   public Date getMaxEntry()
   {
      return maxEntry;
   }

   public Date getMinEntry()
   {
      return minEntry;
   }

   public Vector<String> getCategoryMap()
   {
      return categoryList;
   }

   public void setCategoryMap(Vector<String> cats)
   {
      categoryList = cats;
   }

   public int numberOfCategories()
   {
      return categoryList.size();
   }

   public HashMap<String, HashMap<String, GuideItem>> getProgramList()
   {
      return progList;
   }

   public void setProgramList(HashMap<String, HashMap<String, GuideItem>> progs)
   {
      progList = progs;
   }

   public Date getEPGmaxDate()
   {
      return getMaxEntry();
   }

   public Date getEPGminDate()
   {
      return getMinEntry();
   }

   public Vector<AddConflictDetails> getAutoAddConflicts()
   {
      return getConflictList();
   }

   public Vector<String[]> getEPGlinks(Calendar hiLight)
   {
      DataStore store = DataStore.getInstance();

      Date end = getMaxEntry();
      Date start = getMinEntry();

      if (start == null || end == null) {
		return new Vector<>();
	  }

      Vector<String[]> data = new Vector<>();
      Calendar cal = Calendar.getInstance();
      cal.setTime(start);

      Calendar now = Calendar.getInstance();

      while (cal.getTime().getTime() < end.getTime())
      {
         String[] link = new String[2];
         link[0] = "year=" + cal.get(Calendar.YEAR) + "&month=" + (cal.get(Calendar.MONTH) + 1) + "&day=" + cal.get(Calendar.DATE);

         String dayName = store.dayName.get(Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK)));
         if(now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
               now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
               now.get(Calendar.DATE) == cal.get(Calendar.DATE))
         {
            dayName = "Today";
         }

         if(hiLight != null &&
               hiLight.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
               hiLight.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
               hiLight.get(Calendar.DATE) == cal.get(Calendar.DATE))
         {
            link[1] = "(" + dayName + ")";
         }
         else
         {
            link[1] = dayName;
         }
         data.add(link);
         cal.add(Calendar.DATE, 1);
      }

      return data;
   }

   @SuppressWarnings("unchecked")
   private void loadCategories()
   {

      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileInputStream fis = new FileInputStream(dataPath + File.separator + "sof" + File.separator + "Categories.sof");
         ObjectInputStream ois = new ObjectInputStream(fis);
         Vector<String> cats = (Vector<String>) ois.readObject();
         ois.close();

         setCategoryMap(cats);

         System.out.println("Categories.sof loaded (" + cats.size() + ")");
      }
      catch (Exception e)
      {
         setCategoryMap(new Vector<>());
         System.out.println("ERROR loading Categories.sof, starting with blank category list");
      }
   }

   public void saveCategories()
   {

      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileOutputStream fos = new FileOutputStream(dataPath + File.separator + "sof" + File.separator + "Categories.sof");
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(getCategoryMap());
         oos.close();
         System.out.println("Categories.sof saved.");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private void runPostLoadActions(StringBuffer buff, int format)
   {

      if(format == 1) {
		buff.append("Running post load action<br>\n");
	  } else {
		buff.append("Running post load action\n");
	  }

      //
      // do merge if same name post action
      //
      DataStore store = DataStore.getInstance();

      boolean mergeSameName = false;
      try
      {
         mergeSameName = "1".equals(store.getProperty("guide.action.name").trim());
      }
      catch (Exception e){}

      if(mergeSameName)
      {
         if(format == 1) {
			buff.append("Merging Programs with the same name<br>\n");
		 } else {
			buff.append("Merging Programs with the same name\n");
		 }

         String[] epgChannels = progList.keySet().toArray(new String[0]);

         for (String epgChannel : epgChannels) {
            HashMap<String, GuideItem> items = progList.get(epgChannel);
            String[] itemKeys = items.keySet().toArray(new String[0]);
            Arrays.sort(itemKeys);

            GuideItem prev = null;

            for (String itemKey : itemKeys) {
               GuideItem item = items.get(itemKey);

               if(item != null && prev != null && item.getName().equalsIgnoreCase(prev.getName()))
               {
                  items.remove(prev.toString());
                  items.remove(item.toString());

                  GuideItem newItem = new GuideItem();
                  newItem.setStart(prev.getStart());
                  newItem.setStop(item.getStop());
                  newItem.setName(prev.getName());
                  newItem.setSubName(prev.getSubName() + "-" + item.getSubName());
                  newItem.setDescription(prev.getDescription() + "\n<br>\n" + item.getDescription());
                  newItem.setCategory(prev.getCategory());
                  newItem.setLanguage(prev.getLanguage());
                  newItem.setURL(prev.getURL());

                  items.put(newItem.toString(), newItem);

                  if(format == 1) {
					buff.append("Merging same name : " + newItem.getStart() + " (" + newItem.getDuration() + ") - " + newItem.getName() + "<br>\n");
				  } else {
					buff.append("Merging same name : " + newItem.getStart() + " (" + newItem.getDuration() + ") - " + newItem.getName() + "\n");
				  }

                  prev = newItem;
               } else {
				prev = item;
			   }
            }
         }
      }

      if(format == 1) {
		buff.append("Post load actions finished<br>\n");
	  } else {
		buff.append("Post load actions finished\n");
	  }

   }

   public boolean loadXMLTV(StringBuffer buff, int format)
   {
      DataStore store = DataStore.getInstance();

      try
      {
         int type = 0;
         try
         {
            type = Integer.parseInt(store.getProperty("guide.source.type"));
         }
         catch (Exception e)
         {
         }

         String location = "";
         if (type == 0)
         {
            location = store.getProperty("guide.source.file");
            File locationDir = new File(location);
            // Check Location and assume its a relative path if not resolvable
            if (!locationDir.exists() || !locationDir.isDirectory() )
            {
            String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
            location = dataPath + File.separator + location;
            }
         }
         else if(type == 1) {
			location = store.getProperty("guide.source.http");
		 } else {
			location = "error, unknown type";
		 }

         if (format == 1) {
			buff.append("About to load XMLTV Data from:<br>" + location + "<br><br>\n");
		 } else {
			buff.append("About to load XMLTV Data from:\n" + location + "\n\n");
		 }

         GuideDataLoader loader = new GuideDataLoader(type);

         Vector<byte[]> dataFiles = null;

         if (type == 0) {
			dataFiles = loader.getDataFromFiles(location, buff, format);
		 } else if (type == 1) {
			dataFiles = loader.getDataFromURL(location, buff, format);
		 } else
         {
            throw new Exception("Unknown type:" + type);
         }

         if(dataFiles.size() == 0)
         {
            if (format == 1) {
				buff.append("No Data Found in source, exiting reload.<br><br>\n");
			} else {
				buff.append("No Data Found in source, exiting reload.\n\n");
			}

            return false;
         }

         HashMap<String, HashMap<String, GuideItem>> ignoreList = getIgnoredList();

         // new loaded data
         HashMap<String, HashMap<String, GuideItem>> data = new HashMap<>();
         Vector<String> cats = new Vector<>();

         for (byte[] fileBytes : dataFiles) {
            loadXMLdata(fileBytes, data, cats);
         }

         // make sure we have some epg items
         String[] channels = data.keySet().toArray(new String[0]);
         int progs = 0;
         for (String channel : channels) {
            HashMap<String, GuideItem> chanprogs = data.get(channel);
            progs += chanprogs.size();
         }

         if(progs == 0)
         {
            if (format == 1) {
				buff.append("No EPG items found in source data, exiting reload.<br><br>\n");
			} else {
				buff.append("No EPG items found in source data, exiting reload.\n\n");
			}

            return false;
         }

         // now clear data and set to what we loaded
         progList = data;
         categoryList = cats;

         int ignoreCount = setIgnored(ignoreList);

         if (format == 1) {
			buff.append("Ignored Programs : " + ignoreCount + "<br>\n");
		 } else {
			buff.append("Ignored Programs : " + ignoreCount + "\n");
		 }
      }
      catch (Exception e)
      {
         System.out.println("ERROR Loading EPG DATA!");

         if (format == 1) {
			buff.append("<br><strong>");
		 }

         buff.append("There was an error loading the XMLTV Data!");

         if (format == 1) {
			buff.append("</strong><br>");
		 }

         buff.append("\n");

         ByteArrayOutputStream ba = new ByteArrayOutputStream();
         PrintWriter err = new PrintWriter(ba);
         e.printStackTrace(err);
         err.flush();

         if (format == 1) {
			buff.append("<pre>");
		 }

         buff.append(ba.toString());

         if (format == 1) {
			buff.append("</pre>");
		 }

         if (format == 1) {
			buff.append("<br><br>");
		 }

         buff.append("\n");

         //e.printStackTrace();
         return false;
      }

      // do post load actions
      StringBuffer actionBuff = new StringBuffer();
      runPostLoadActions(actionBuff, format);

      detectDateRange();

      if (getMinEntry() != null && getMaxEntry() != null)
      {
         if (format == 1)
         {
            buff.append("<br>Earliest Entry : " + getMinEntry().toString() + "<br>\n");
            buff.append("Latest Entry : " + getMaxEntry().toString() + "<br><br>\n");
         }
         else
         {
            buff.append("\nEarliest Entry : " + getMinEntry().toString() + "\n");
            buff.append("Latest Entry : " + getMaxEntry().toString() + "\n\n");
         }
      }

      // how many categories did we find
      if (format == 1) {
		buff.append("Number of Categories Found : " + categoryList.size() + "<br><br>\n");
	  } else {
		buff.append("Number of Categories Found : " + categoryList.size() + "\n\n");
	  }


      HashMap<String, HashMap<String, GuideItem>> progList = getProgramList();
      String[] channels = progList.keySet().toArray(new String[0]);
      Arrays.sort(channels);

      if (format == 1)
      {
         buff.append("Current EPG Data Set<br>\n");
         buff.append("<table>\n");
         for (String channel : channels) {
            HashMap<String, GuideItem> progs = progList.get(channel);
            buff.append("<tr><td>" + channel + "</td><td>: " + progs.size() + "</td></tr>\n");
         }
         buff.append("</table>\n");
         buff.append("<br>Saving loaded EPG data<br><br>\n");
      }
      else
      {
         buff.append("Current EPG Data Set\n");
         for (String channel : channels) {
            HashMap<String, GuideItem> progs = progList.get(channel);
            buff.append(channel + " : " + progs.size() + "\n");
         }
         buff.append("\nSaving loaded EPG data\n\n");
      }

      buff.append(actionBuff);

      saveEpg();
      saveCategories();
      detectDateRange();

      return true;
   }

   @SuppressWarnings("unchecked")
   public void loadEpg()
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      String loadFrom = dataPath + File.separator + "sof" + File.separator + "Epg.sof";
      try
      {
         FileInputStream fis = new FileInputStream(loadFrom);
         ObjectInputStream ois = new ObjectInputStream(fis);
         HashMap<String, HashMap<String, GuideItem>> progList = (HashMap<String, HashMap<String, GuideItem>>) ois.readObject();
         ois.close();
         System.out.println("Epg.sof loaded.");

         setProgramList(progList);
         detectDateRange();
      }
      catch (Exception e)
      {
         //e.printStackTrace();
         System.out.println("Error loading Epg.sof, starting with empty EPG data set.");
      }
   }

   public void saveEpg()
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      String saveTo = dataPath + File.separator + "sof" + File.separator + "Epg.sof";
      try
      {
         FileOutputStream fos = new FileOutputStream(saveTo);
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(getProgramList());
         oos.close();
         System.out.println("Epg.sof saved.");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("ERROR saving Epg.sof");
      }
   }

   @SuppressWarnings("unchecked")
   public void importChannelMap(byte[] mapData) throws Exception
   {
      ByteArrayInputStream mapBytes = new ByteArrayInputStream(mapData);

      ObjectInputStream ois = new ObjectInputStream(mapBytes);
      Vector<String[]> channelMap = (Vector<String[]>) ois.readObject();
      ois.close();
      System.out.println("Channel Map imported");

      setChannelMap(channelMap);

      saveChannelMap(null);
   }

   @SuppressWarnings("unchecked")
   public void loadChannelMap()
   {
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      String loadFrom = dataPath + File.separator + "sof" + File.separator + "ChannelMap.sof";
      try
      {
         FileInputStream fis = new FileInputStream(loadFrom);
         ObjectInputStream ois = new ObjectInputStream(fis);
         Vector<String[]> channelMap = (Vector<String[]>) ois.readObject();
         ois.close();
         System.out.println("ChannelMap.sof loaded.");

         setChannelMap(channelMap);
      }
      catch (Exception e)
      {
         System.out.println("ERROR loading ChannelMap.sof, starting with no channel mapping.");
      }
   }

   public void saveChannelMap(ByteArrayOutputStream chanMapBytes)
   {
      try
      {
         ObjectOutputStream oos = null;
         if(chanMapBytes == null)
         {
            String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
            String saveTo = dataPath + File.separator + "sof" + File.separator + "ChannelMap.sof";
            FileOutputStream fos = new FileOutputStream(saveTo);
            oos = new ObjectOutputStream(fos);
         }
         else
         {
            oos = new ObjectOutputStream(chanMapBytes);
         }

         oos.writeObject(getChannelMap());
         oos.close();
         System.out.println("ChannelMap.sof saved.");
      }
      catch (Exception e)
      {
         System.out.println("Problem saving ChannelMap.sof");
         e.printStackTrace();
      }
   }

   public String getWsChannelFromMap(String epgChannel)
   {
      if(epgChannel == null) {
		return null;
	  }

      for (String[] map : channelMap) {
         if(map[1].equals(epgChannel)) {
			return map[0];
		 }
      }
      return null;
   }

   public String getEpgChannelFromMap(String wsChannel)
   {
      if(wsChannel == null) {
		return null;
	  }

      for (String[] map : channelMap) {
         if(map[0].equals(wsChannel)) {
			return map[1];
		 }
      }
      return null;
   }

   public String[] getCategoryStrings()
   {
      return categoryList.toArray(new String[0]);
   }

   public void detectDateRange()
   {
      //Calvi changes to avoid exceptions on getTime calls, set min high and max low in lieu of null
      minEntry = new GregorianCalendar(2900, 0, 1).getTime();
      maxEntry = new GregorianCalendar(1900, 0, 1).getTime();

      String[] channels = progList.keySet().toArray(new String[0]);

      for (String channel : channels) {
         HashMap<String, GuideItem> progs = progList.get(channel);
         if(progs != null)
         {
            GuideItem[] items = progs.values().toArray(new GuideItem[0]);
            Arrays.sort(items);
            if(items.length > 0)
            {
               if(items[0].getStart().getTime() < minEntry.getTime()) {
				minEntry = items[0].getStart();
			   }
               if(items[items.length - 1].getStart().getTime() > maxEntry.getTime()) {
				maxEntry = items[items.length - 1].getStart();
			   }
            }
         }
      }
   }

   public int loadXMLdata(byte[] xmlFile, HashMap<String, HashMap<String, GuideItem>> epgData, Vector<String> categories) throws Exception
   {
      int found = 0;

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setIgnoringComments(true);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(xmlFile);

      Document doc = docBuilder.parse(reader);
      NodeList root = null;

      //
      // use the XMLTV format parser
      //

      root = doc.getElementsByTagName("tv");
      if(root != null)
      {
         // get the node list from the first TV node found

         Node firstNode = root.item(0);
         if(firstNode == null)
         {
            System.out.println("ERROR processing XML data, first node of <tv> not found.");
            return found;
         }

         root = firstNode.getChildNodes();
         if(root == null)
         {
            System.out.println("ERROR processing XML data, no children of first node.");
            return found;
         }

         HashMap<String, String> channels = loadChannels(root);
         found = recurseXMLData(root, channels, epgData, categories);
      }

      return found;
   }

   private HashMap<String, String> loadChannels(NodeList root)
   {
      HashMap<String, String> channels = new HashMap<>();

      Node thisNode = null;

      for (int x = 0; x < root.getLength(); x++)
      {
         thisNode = root.item(x);
         if(thisNode == null) {
			break;
		 }

         if(thisNode.getNodeName().equals("channel")) {
			parseChannel(thisNode, channels);
		 }
      }

      return channels;
   }

   public String getNodeString(Node prog, String nodename, String defaultValue)
   {
      String out = defaultValue;
      try
      {
         Node tmp = getChildByType(prog, nodename);
         if(tmp != null && tmp.getFirstChild() != null)
         {
            out = tmp.getFirstChild().getNodeValue();
         }
         tmp = null;
      }
      catch (Exception e)
      {
      }
      return out;
   }

   public Vector<String> getNodeStrings(Node node, String nodename)
   {
      Vector<String> values = new Vector<>();
      try
      {
         Vector<Node> nodeList = getChildrenByType(node, nodename);
         for(int x = 0; x < nodeList.size(); x++)
         {
            Node tmp = nodeList.get(x);
            if(tmp.getFirstChild() != null)
            {
               values.add(tmp.getFirstChild().getNodeValue());
            }
            tmp = null;
         }
      }
      catch (Exception e)
      {
      }
      return values;
   }

   public boolean getNodeExists(Node prog, String name)
   {
      Node tmp = null;
      try
      {
         tmp = getChildByType(prog, name);
         if(tmp != null) {
			return true;
		 }
      }
      catch (Exception e)
      {
      }
      return false;
   }

   public HashMap<String, HashMap<String, GuideItem>> getIgnoredList()
   {
      HashMap<String, HashMap<String, GuideItem>> ignoreList = new HashMap<>();

      String[] channels = progList.keySet().toArray(new String[0]);
      for (String channel : channels) {
         HashMap<String, GuideItem> guideItems = progList.get(channel);
         String[] ids = guideItems.keySet().toArray(new String[0]);
         for (String id : ids) {
            GuideItem item = guideItems.get(id);
            if(item.getIgnored())
            {
               HashMap<String, GuideItem> guideItems2 = ignoreList.get(channel);
               if(guideItems2 == null)
               {
                  guideItems2 = new HashMap<>();
                  ignoreList.put(channel, guideItems2);
               }

               guideItems2.put(item.toString(), item);
            }
         }
      }

      return ignoreList;
   }

   private int setIgnored(HashMap<String, HashMap<String, GuideItem>> ignoredList)
   {
      int count = 0;
      String[] channels = ignoredList.keySet().toArray(new String[0]);

      DataStore store = DataStore.getInstance();

      boolean inexactIGMatch = false;
      int startIGTol = 0;
      int durIGTol = 0;
      try
      {
         inexactIGMatch = store.getProperty("guide.item.igmatch.inexact").equals("1");
         startIGTol = Integer.parseInt(store.getProperty("guide.item.igtol.start"));
         durIGTol = Integer.parseInt(store.getProperty("guide.item.igtol.duration"));
      }
      catch (Exception e){}

      for (String channel : channels) {
         HashMap<String, GuideItem> guideItems = ignoredList.get(channel);
         String[] ids = guideItems.keySet().toArray(new String[0]);
         for (String id : ids) {
            GuideItem item = guideItems.get(id);

            HashMap<String, GuideItem> guideItems2 = progList.get(channel);
            if(guideItems2 != null)
            {
               GuideItem[] items2 = guideItems2.values().toArray(new GuideItem[0]);

               for (GuideItem element : items2) {
                  if(element.matches(item,inexactIGMatch,startIGTol,durIGTol))
                  {
                     count++;
                     System.out.println("Setting EPG item to ignored : " + element);
                     element.setIgnored(true);
                  }
               }
            }
         }
      }
      return count;
   }

   public void addProgram(GuideItem item, String channel, HashMap<String, HashMap<String, GuideItem>> epgData, Vector<String> categories)
   {
      HashMap<String, GuideItem> programs = epgData.get(channel);
      if(programs == null)
      {
         programs = new HashMap<>();
         epgData.put(channel, programs);
      }

      // add cats from this program to the cat list
      if(item.getCategory().size() > 0)
      {
         for(int index = 0; index < item.getCategory().size(); index++)
         {
            String itemCat = item.getCategory().get(index);
            if(!categories.contains(itemCat))
            {
               categories.add(itemCat);
            }
         }
      }
      programs.put(item.toString(), item);
   }

   public GuideItem[] getProgramsForChannel(String channel)
   {
      HashMap<String, GuideItem> fullList = progList.get(channel);
      if(fullList == null)
      {
         return new GuideItem[0];
      }

      Iterator<GuideItem> it = fullList.values().iterator();

      Vector<GuideItem> subList = new Vector<>();

      GuideItem item = null;

      while (it.hasNext())
      {
         item = it.next();
         subList.add(item);
      }

      GuideItem[] items = subList.toArray(new GuideItem[0]);
      Arrays.sort(items);
      return items;
   }

   public GuideItem[] getPrograms(Date start, Date end, String channel)
   {
      HashMap<String, GuideItem> fullList = progList.get(channel);
      if(fullList == null)
      {
         return new GuideItem[0];
      }

      Iterator<GuideItem> it = fullList.values().iterator();

      Vector<GuideItem> subList = new Vector<>();

      GuideItem item = null;

      while (it.hasNext())
      {
         item = it.next();

         if(start.getTime() < item.getStart().getTime()
                  && end.getTime() > item.getStart().getTime())
         {
            subList.add(item);
         }
      }

      GuideItem[] items = subList.toArray(new GuideItem[0]);
      Arrays.sort(items);
      return items;
   }

   public GuideItem[] getProgramsInc(Date start, Date end, String channel)
   {
      HashMap<String, GuideItem> fullList = progList.get(channel);
      if(fullList == null)
      {
         return new GuideItem[0];
      }

      Iterator<GuideItem> it = fullList.values().iterator();

      Vector<GuideItem> subList = new Vector<>();

      GuideItem item = null;

      while (it.hasNext())
      {
         item = it.next();

         if(item.getStop().getTime() > (start.getTime() + 5000)
                  && item.getStart().getTime() < end.getTime())
         // if(start.getTime() < item.getStart().getTime() && end.getTime() >
         // item.getStart().getTime())
         {
            subList.add(item);
         }
      }

      GuideItem[] items = subList.toArray(new GuideItem[0]);
      Arrays.sort(items);
      return items;
   }

   public GuideItem getProgram(String channel, String id)
   {
      HashMap<String, GuideItem> fullList = progList.get(channel);
      if(fullList == null) {
		return null;
	  }

      return fullList.get(id);
   }

   public int recurseXMLData(NodeList nl, HashMap<String, String> channels, HashMap<String, HashMap<String, GuideItem>> epgData, Vector<String> categories)
   {
      int found = 0;

      Node thisNode = null;
      for (int x = 0; x < nl.getLength(); x++)
      {
         thisNode = nl.item(x);
         if(thisNode == null) {
			break;
		 }

         if(thisNode.getNodeName().equals("programme"))
         {
            if(parseProgram(thisNode, channels, epgData, categories)) {
				found++;
			}
         }
      }

      return found;
   }

   public void moveChannel(int id, boolean dir)
   {
      int dest = 0;

      if(dir) {
		dest = id + 1;
	  } else {
		dest = id - 1;
	  }

      if(dest >= 0 && dest < channelMap.size())
      {
         String[] obj = channelMap.remove(id);

         if(dir) {
			channelMap.add(id + 1, obj);
		 } else {
			channelMap.add(id - 1, obj);
		 }

         try
         {
            saveChannelMap(null);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   public String[] getChannelList()
   {
      return progList.keySet().toArray(new String[0]);
   }

   public Vector<String[]> getChannelMap()
   {
      return channelMap;
   }

   public void setChannelMap(Vector<String[]> chanMap)
   {
      channelMap = chanMap;
   }

   public void addChannelToMap(String channel, String egpChannel)
   {
      String[] map = new String[2];
      map[0] = channel;
      map[1] = egpChannel;
      channelMap.add(map);
   }

   public Date parseDate(String newDate) throws NumberFormatException
   {
      String dateFormat = "";
      if(newDate.indexOf(" +") > -1 || newDate.indexOf(" -") > -1)
      {
         dateFormat = "yyyyMMddHHmmss Z";
      }
      else
      {
         dateFormat = "yyyyMMddHHmmss";
      }

      SimpleDateFormat df = new SimpleDateFormat(dateFormat);

      Date parsedDate = null;
      try
      {
         parsedDate = df.parse(newDate);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         parsedDate = new Date(0);
      }

      Calendar parsedCal = Calendar.getInstance();
      parsedCal.setTime(parsedDate);
      parsedCal.set(Calendar.MILLISECOND, 0);
      parsedCal.add(Calendar.SECOND, 30);
      parsedCal.set(Calendar.SECOND, 0);

      return parsedCal.getTime();
   }

   private void parseChannel(Node chan, HashMap<String, String> channels)
   {
      NamedNodeMap attMap = chan.getAttributes();
      Node idNode = attMap.getNamedItem("id");
      String id = idNode.getNodeValue();

      Node nameNode = getChildByType(chan, "display-name");
      String name = null;
      if ((nameNode.getFirstChild() != null) && (nameNode.getFirstChild().getNodeValue() != null)) {
        name = nameNode.getFirstChild().getNodeValue();
      }

      if(id != null && id.length() > 0 && name != null && name.length() > 0)
      {
         channels.put(id, name);
         // System.out.println("Adding Channel " + id + ":" + name);
      }
   }

   public boolean parseProgram(Node prog, HashMap<String, String> channels, HashMap<String, HashMap<String, GuideItem>> epgData, Vector<String> categories)
   {
      GuideItem item = new GuideItem();
	  DataStore store = DataStore.getInstance();

      String temp01 = "";
      String temp02 = "";

      try
      {
         NamedNodeMap attMap = prog.getAttributes();

         Node start_time = attMap.getNamedItem("start");
         temp01 = start_time.getNodeValue();
         Date start = parseDate(start_time.getNodeValue());
		 Date ndate = new Date(); // Gets the current date.

         item.setStart(start);

		 boolean clipStart = "1".equals(store.getProperty("guide.clip.start"));

		 if(clipStart && start.before(ndate)) {
			return false;
		 }

         Node stop_time = attMap.getNamedItem("stop");
         temp02 = stop_time.getNodeValue();
         Date stop = parseDate(stop_time.getNodeValue());

         // if(stop.before(start))
         // stop.add(Calendar.HOUR, 24);

         long duration = (stop.getTime() - start.getTime());
         duration = duration / (1000 * 60);

         if(duration < 0) {
			duration *= -1;
		 }

         item.setDuration((int) duration);

         Node chan = attMap.getNamedItem("channel");

         Node title = getChildByType(prog, "title");
         item.setName(title.getFirstChild().getNodeValue());

         Node subTitle = getChildByType(prog, "sub-title");
         if(subTitle != null && subTitle.getFirstChild() != null) {
			item.setSubName(subTitle.getFirstChild().getNodeValue());
		 }

         Node description = getChildByType(prog, "desc");
         if(description != null && description.getFirstChild() != null) {
			item.setDescription(description.getFirstChild().getNodeValue());
		 }

         Node url = getChildByType(prog, "url");
         if(url != null && url.getFirstChild() != null) {
			item.setURL(url.getFirstChild().getNodeValue());
		 }

         // New Guide Info - XMLTV1.1 / XMLTV2 Loader Code..

         if(getNodeExists(prog, "rating"))
         {
             Node tmp = getChildByType(prog, "rating");
             Node value = getChildByType(tmp, "value");
             Node ratingNode = value.getFirstChild();
             if(ratingNode != null) {
				item.setRatings(ratingNode.getNodeValue());
			 }
         }

         item.setLanguage(getNodeString(prog, "language", item.getLanguage()));
         item.setCategory(getNodeStrings(prog, "category"));

         if(getNodeExists(prog, "credits"))
         {
            Node tmp = getChildByType(prog, "credits");
            item.setActors(getNodeStrings(tmp, "actor"));
            item.setDirectors(getNodeStrings(tmp, "director"));
         }

         item.setLastChance(getNodeExists(prog, "last-chance"));
         item.setRepeat(getNodeExists(prog, "previously-shown"));
         item.setPremiere(getNodeExists(prog, "premiere"));
         item.setCaptions(getNodeExists(prog, "subtitles"));

         if(getNodeExists(prog, "audio"))
         {
            Node tmp = getChildByType(prog, "audio");
            String str = getNodeString(tmp, "stereo", "").toLowerCase();
            if(str.startsWith("surround")) {
				item.setSurround(true);
			}
            if(str.startsWith("ac3")) {
				item.setAC3(true);
			}
            tmp = null;
            str = "";
         }

         if(getNodeExists(prog, "video"))
         {
            Node tmp = getChildByType(prog, "video");
            String str = getNodeString(tmp, "aspect", "");
            if(str.toLowerCase().startsWith("16:9")) {
				item.setWidescreen(true);
			}
            str = getNodeString(tmp, "quality", "");
            if(str.toLowerCase().startsWith("hd")) {
				item.setHighDef(true);
			}
            tmp = null;
            str = "";
         }

         String channel = channels.get(chan.getNodeValue());
         if(channel == null) {
			channel = chan.getNodeValue();
		 }

         channel = channel.replaceAll("'", "`");
         channel = channel.replaceAll("\"", "`");
         channel = channel.replaceAll("<", "(");
         channel = channel.replaceAll(">", ")");

         addProgram(item, channel, epgData, categories);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("Temp 01:" + temp01);
         System.out.println("Temp 02:" + temp02);
         System.out.println("Available Item Information:");
         System.out.println("Name:" + item.getName());
         System.out.println("Sub Name:" + item.getSubName());
         System.out.println("StartTime:" + item.getStart());
         System.out.println("Duration:" + item.getDuration());
         System.out.println("Description:" + item.getDescription());

         return false;
      }

      return true;
   }

   public Node getChildByType(Node node, String name)
   {
      if(!node.hasChildNodes()) {
		return null;
	  }

      Node found = null;
      NodeList nl = node.getChildNodes();
      for (int x = 0; x < nl.getLength(); x++)
      {
         found = nl.item(x);
         if(found.getNodeName().equals(name)) {
			return found;
		 }
      }

      return null;
   }

   public Vector<Node> getChildrenByType(Node node, String name)
   {
      Vector<Node> children = new Vector<>();

      if(!node.hasChildNodes()) {
		return children;
	  }

      Node found = null;
      NodeList nl = node.getChildNodes();
      for (int x = 0; x < nl.getLength(); x++)
      {
         found = nl.item(x);
         if(found.getNodeName().equals(name))
         {
            children.add(found);
         }
      }

      return children;
   }

   String[] getFirstLetters()
   {
      Vector<String> firsts = new Vector<>();

      for (int x = 0; x < channelMap.size(); x++)
      {
         String[] map = channelMap.get(x);
         HashMap<String, GuideItem> progs = progList.get(map[1]);

         if(progs != null)
         {
            GuideItem[] items = progs.values().toArray(
                     new GuideItem[0]);

            for (GuideItem item : items) {
               if(item.getName() != null && item.getName().length() > 1)
               {
                  String firstLetter = item.getName().substring(0, 1).toUpperCase();
                  if(!firsts.contains(firstLetter)) {
					firsts.add(firstLetter);
				  }
               }
            }
         }
      }

      String[] result = firsts.toArray(new String[0]);
      Arrays.sort(result);
      return result;
   }

   String[] getNamesStartingWith(String starting)
   {
      Vector<String> starts = new Vector<>();

      for (int x = 0; x < channelMap.size(); x++)
      {
         String[] map = channelMap.get(x);
         HashMap<String, GuideItem> progs = progList.get(map[1]);

         if(progs != null)
         {
            GuideItem[] items = progs.values().toArray(
                     new GuideItem[0]);

            for (GuideItem item : items) {
               if(item.getName() != null && item.getName().length() > 1)
               {
                  if(item.getName().toUpperCase().startsWith(
                           starting.toUpperCase()))
                  {
                     if(!starts.contains(item.getName()))
                     {
                        starts.add(item.getName());
                     }
                  }
               }
            }
         }
      }

      String[] result = starts.toArray(new String[0]);
      Arrays.sort(result);
      return result;
   }

   GuideItem[] getItems(String name, String channel)
   {
      Vector<GuideItem> matchingItems = new Vector<>();

      HashMap<String, GuideItem> progs = progList.get(channel);
      if(progs != null)
      {
         GuideItem[] items = progs.values().toArray(new GuideItem[0]);

         for (GuideItem item : items) {
            if(name.equals(item.getName()))
            {
               matchingItems.add(item);
            }
         }
      }
      GuideItem[] result = matchingItems.toArray(new GuideItem[0]);
      Arrays.sort(result);
      return result;
   }

   public int searchEPG(EpgMatchList epgMatchList, HashMap<String, Vector<GuideItem>> result)
   {
      DataStore store = DataStore.getInstance();

      int num = 0;

      GuideItem[] progs = null;

      Vector<String[]> channelMap = getChannelMap();

      if (channelMap == null || channelMap.size() == 0)
      {
         return 0;
      }

      Set<String> wsChannels = store.getChannels().keySet();

      for(int x = 0; x < channelMap.size(); x++)
      {
         String[] map = channelMap.get(x);

         if(wsChannels.contains(map[0]))
         {
            progs = getProgramsForChannel(map[1]);

            if(progs.length > 0)
            {
               Vector<GuideItem> results = null;

               if(result.containsKey(map[0]))
               {
                  results = result.get(map[0]);
               }
               else
               {
                  results = new Vector<>();
                  result.put(map[0], results);
               }

               for (GuideItem prog : progs) {
                  if(epgMatchList.isMatch(prog, map[0]))
                  {
                     if(!results.contains(prog))
                     {
                        results.add(prog);
                        num++;
                     }
                  }
               }
            }
         }
      }

      return num;
   }

   public int addEPGmatches(StringBuffer buff, int format) throws Exception
   {
      DataStore store = DataStore.getInstance();

      int num = 0;
      SimpleDateFormat df = new SimpleDateFormat("EE d HH:mm");

      int type = 0;
      try
      {
         type = Integer.parseInt(store.getProperty("capture.deftype"));
      }
      catch (Exception e){}

      // removes all conflicts from list
      conflictList.clear();

      CaptureDeviceList devList = CaptureDeviceList.getInstance();

      //
      // now cycle through all the EPG Auto-Add items
      //
      EpgMatch epgMatch = null;
      Calendar cal = Calendar.getInstance();

      Vector<String[]> channelMap = getChannelMap();
      Set<String> wsChannels = store.getChannels().keySet();

      //
      // Check to see if we have any mapped channels at all, if not just return
      //
      if (channelMap == null || channelMap.size() == 0)
      {
         if (format == 1) {
			buff.append("Channel map not set so could not run Auto-Add system.<br>\n");
		 } else {
			buff.append("Channel map not set so could not run Auto-Add system.\n");
		 }

         System.out.println("Channel map not set so could not run Auto-Add system.");
         return 0;
      }

      if (format == 1) {
		buff.append("About to run Auto-Add system.<br><br>\n");
	  } else {
		buff.append("About to run Auto-Add system.\n\n");
	  }

      GuideItem[] progs = null;
      GuideItem guidItem = null;
      ScheduleItem schItem = null;
      int conflictNum = 0;

      Vector<EpgMatch> epgMatchList = store.getEpgMatchList();

      System.out.println("");
      //
      // For each Auto-Add item check the EPG programs for a match
      //
      for (int x = 0; x < epgMatchList.size(); x++)
      {
         epgMatch = epgMatchList.get(x);

         if(epgMatch.isEnabled())
         {

            //
            // for each Auto-Add item search the EPG programs for matches
            //
            for (int z = 0; z < channelMap.size(); z++)
            {
               String[] chanMap = channelMap.get(z);

               if(wsChannels.contains(chanMap[0]))
               {
                  progs = getProgramsForChannel(chanMap[1]);

                  for (GuideItem prog : progs) {
                     guidItem = prog;

                     //
                     // OK so we have a guide item, lets see if it matches the
                     // Auto-Add item
                     //

                     boolean foundMatch = false;
                     Vector<String> matchNames = epgMatch.getMatchListNames();
                     HashMap<String, EpgMatchList> matchLists = store.getMatchLists();

                     EpgMatchList matcher = null;

                     for (String matchListName : matchNames) {
                        matcher = matchLists.get(matchListName);
                        if(matcher != null)
                        {
                           if(matcher.isMatch(guidItem, chanMap[0]))
                           {
                              foundMatch = true;
                              break;
                           }
                        }
                     }

                     if(foundMatch && guidItem.getIgnored())
                     {
                        String itemAction = "IGNORED";
                        String itemResult = "Item Ignored";
                        String itemDetails = guidItem.getName() + " " + df.format(guidItem.getStart());
                        if(format == 1)
                        {
                           buff.append("<b><font color='#FFFF00'>"
                                    + itemAction + "</font></b> - "
                                    + itemDetails + " (" + itemResult
                                    + ")<br>\n");
                        } else {
							buff.append(itemAction + " - " + itemDetails + " ("
							            + itemResult + ")\n");
						}
                     }
                     else if(foundMatch)
                     {
                        //
                        // It looks like we have a match
                        //

                        //
                        // Create a new Schedule Item from the data in the EPG
                        // item
                        //
                        schItem = new ScheduleItem(guidItem, chanMap[0],
                                 type, store.rand.nextLong(), epgMatch.getAutoDel());
                        String[] patterns = store.getNamePatterns();
                        schItem.setFilePattern(patterns[0]);
                        schItem.setKeepFor(epgMatch.getKeepFor());
                        schItem.setPostTask(epgMatch.getPostTask());
                        schItem.setFilePattern(epgMatch.GetFileNamePattern());
                        schItem.setCapType(epgMatch.getCaptureType());
                        schItem.setCapturePathIndex(epgMatch.getCapturePathIndex());

                        cal.setTime(schItem.getStart());
                        cal.add(Calendar.MINUTE,
                                 (epgMatch.getStartBuffer() * -1));
                        schItem.setDuration(guidItem.getDuration()
                                 + epgMatch.getStartBuffer()
                                 + epgMatch.getEndBuffer());
                        schItem.setStart(cal);

                        String itemDetails = schItem.getName()
                                 + " ("
                                 + guidItem.getSubName() + " )"
                                 + " ("
                                 + df.format(schItem.getStart(),
                                          new StringBuffer(),
                                          new FieldPosition(0)) + " "
                                 + schItem.getDuration() + " "
                                 + schItem.getChannel() + ") ";

                        System.out.println("Matched Item = " + schItem.getName() + " " + schItem.getStart() + " " + schItem);
                        //
                        // Now lets work out if we can actually add this
                        // schedule item
                        // to out schedule table without conflicts etc
                        //
                        String itemAction = "";
                        StringBuffer conflictResult = new StringBuffer();
                        String itemResult = "";

                        boolean alreadyInList = isAlreadyInList(schItem, epgMatch.getExistingCheckType());

                        if(alreadyInList)
                        {
                           itemAction = "WARNING";
                           itemResult = "Already Exists";

                           AddConflictDetails acd = new AddConflictDetails(guidItem.getName());
                           acd.setDescription("Already Exists");
                           acd.setConflict("A schedule for this EPG item already Exists.");
                           acd.setReason(AddConflictDetails.REASON_WARNING);
                           conflictList.add(acd);
                        }
                        else if(cal.after(Calendar.getInstance()))
                        {
                           boolean allowOverlaps = "1".equals(store.getProperty("schedule.overlap"));
                           int overlaps = numOverlaps(schItem, conflictResult);

                           if(overlaps <= devList.getDeviceCount() || allowOverlaps)
                           {
                              store.addScheduleItem(schItem);
                              itemAction = "ADDED";
                              itemResult = "OK";
                              num++;
                           }

                           if(overlaps > devList.getDeviceCount())
                           {
                              itemAction = "ERROR";
                              itemResult = "Item overlapped";

                              AddConflictDetails acd = new AddConflictDetails(itemDetails);
                              acd.setDescription("Item overlapped");
                              acd.setConflict(conflictResult.toString());
                              acd.setReason(AddConflictDetails.REASON_ERROR);
                              conflictList.add(acd);

                              conflictNum++;
                           }
                        }
                        else
                        {
                           itemAction = "WARNING";
                           itemResult = "Before now";
                        }

                        if(format == 1)
                        {
                           if(itemAction.equalsIgnoreCase("ERROR"))
                           {
                              buff.append("<b><font color='#FF0000'>"
                                       + itemAction + "</font></b> - "
                                       + itemDetails + " (" + itemResult
                                       + ")<br>\n");
                           }
                           else if(itemAction.equalsIgnoreCase("WARNING"))
                           {
                              buff.append("<b><font color='#FFFF00'>"
                                       + itemAction + "</font></b> - "
                                       + itemDetails + " (" + itemResult
                                       + ")<br>\n");
                           }
                           else
                           {
                              buff.append("<b><font color='#00FF00'>"
                                       + itemAction + "</font></b> - "
                                       + itemDetails + " (" + itemResult
                                       + ")<br>\n");
                           }
                        } else {
							buff.append(itemAction + " - " + itemDetails + " ("
							            + itemResult + ")\n");
						}

                     }
                  } // end program loop
               } // end if channel name
            } // end channel loop
         } // end enabled if
         else
         {
            Vector<String> items = epgMatch.getMatchListNames();
            String names = "";
            for(int q = 0; q < items.size(); q++)
            {
               String name = items.get(q);
               if(format == 1)
               {
                  name = name.replaceAll("<", "&lt;");
                  name = name.replaceAll(">", "&gt;");
               }
               if(q == items.size()-1)
               {
                  names += name;
               } else {
				names += name + ", ";
			   }
            }
            if(names.length() == 0) {
				names = "No Match List Assigned";
			}

            if(format == 1)
            {
               buff.append("Auto-Add (" + names + ") is disabled.<br>\n");
            }
            else
            {
               buff.append("Auto-Add (" + names + ") is disabled.\n");
            }
         }
      }// end Auto-Add loop

      if (format == 1) {
		buff.append("<br>Matched and added " + num + " items from the EPG data.<br>\n");
	  } else {
		buff.append("\nMatched and added " + num + " items from the EPG data.\n");
	  }

      if(conflictNum > 0 && format == 1)
      {
         buff.append("<br>There were " + conflictNum + " conflicts, check the <a href='/servlet/EpgAutoAddDataRes?action=25'>CONFLICT REPORT PAGE</a> for more info.<br>\n");
      }

      return num;
   }

   public boolean isAlreadyInList(ScheduleItem schItem, int checkType)
   {
      // checkType
      // 0 = no check, return false
      // 1 = same channel, check for existence on same channel only
      // 3 = anywhere, check all schedules for created from is not null

      if(checkType == 0) {
		return false;
	  }

      DataStore store = DataStore.getInstance();

      ScheduleItem[] itemsArray = store.getScheduleArray();

      boolean inexactCFMatch = false;
      int startCFTol = 0;
      int durCFTol = 0;
      try
      {
         inexactCFMatch = store.getProperty("guide.item.cfmatch.inexact").equals("1");
         startCFTol = Integer.parseInt(store.getProperty("guide.item.cftol.start"));
         durCFTol = Integer.parseInt(store.getProperty("guide.item.cftol.duration"));
      }
      catch (Exception e){}

      for (ScheduleItem item : itemsArray) {
         if(schItem.equals(item)) {
			return true;
		 }

         // check to see if this item was already added from an EPG item
         // if checkType is 1 has to be on same channel
         // if checkType is 2 it can be on any channel
         if(checkType == 2 || schItem.getChannel().equals(item.getChannel()))
         {
            if(schItem.getCreatedFrom() != null && item.getCreatedFrom() != null)
            {
               if(schItem.getCreatedFrom().matches(item.getCreatedFrom(),inexactCFMatch,startCFTol,durCFTol))
               {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private int numOverlaps(ScheduleItem schItem, StringBuffer buff)
   {
      DataStore store = DataStore.getInstance();

      Vector <ScheduleItem> overlapping = new Vector<>();
      SimpleDateFormat df = new SimpleDateFormat("EE d HH:mm");
      ScheduleItem[] itemsArray = store.getScheduleArray();

      for (ScheduleItem item : itemsArray) {
         if(item.isOverlapping(schItem))
         {
            overlapping.add(item);
            buff.append(item.getName() + " (" + df.format(item.getStart()));
            buff.append(" " + item.getDuration() + " ");
            buff.append(item.getChannel() + ")<br>\n");
         }
      }

      // if we do not overlap then return zero
      if(overlapping.size() == 0) {
		return 1; // just this schedule
	  }

      int overlapCount = getOverlapCount(schItem, overlapping);

      return overlapCount;
   }

   private int getOverlapCount(ScheduleItem item, Vector <ScheduleItem> overlapping)
   {
      Date start = item.getStart();
      Date end = item.getStop();

      DataStore store = DataStore.getInstance();
      HashMap<String, Channel> channels = store.getChannels();
      Channel schChannel = channels.get(item.getChannel());
      String muxString = schChannel.getFrequency() + "-" + schChannel.getBandWidth();

      Calendar startCal = Calendar.getInstance();
      startCal.setTime(start);

      Calendar endCal = Calendar.getInstance();
      endCal.setTime(end);

      int maxOverlap = 0;
      while(startCal.before(endCal))
      {
         Calendar span = Calendar.getInstance();
         span.setTime(startCal.getTime());
         span.add(Calendar.MINUTE, 1);

         HashMap <String, Integer> muxOverlapCount = new HashMap<>();
         muxOverlapCount.put(muxString, Integer.valueOf(1));
         getOverlapsForMin(startCal.getTime(), span.getTime(), muxOverlapCount, overlapping, channels);

         Integer[] count = muxOverlapCount.values().toArray(new Integer[0]);
         if(maxOverlap < count.length) {
			maxOverlap = count.length;
		 }

         startCal.add(Calendar.MINUTE, 1);
      }

      return maxOverlap;
   }

   private void getOverlapsForMin(Date start, Date end, HashMap <String, Integer> muxOverlapCount, Vector <ScheduleItem> overlapping, HashMap<String, Channel> channels)
   {
      for (ScheduleItem item : overlapping) {
         Channel schChannel = channels.get(item.getChannel());
         String muxString = schChannel.getFrequency() + "-" + schChannel.getBandWidth();

         boolean overlap = false;

         if(item.getStart().getTime() >= start.getTime() && item.getStart().getTime() < end.getTime()) {
			overlap = true;
		 }

         if(item.getStop().getTime() > start.getTime() && item.getStop().getTime() <= end.getTime()) {
			overlap = true;
		 }

         if(item.getStart().getTime() <= start.getTime() && item.getStop().getTime() >= end.getTime()) {
			overlap = true;
		 }

         if(overlap)
         {
            Integer muxCount = muxOverlapCount.get(muxString);
            if(muxCount == null)
            {
               muxOverlapCount.put(muxString, Integer.valueOf(1));
            }
            else
            {
               muxOverlapCount.put(muxString, Integer.valueOf(muxCount.intValue() + 1));
            }
         }
      }
   }

   public Vector<AddConflictDetails> getConflictList()
   {
      return conflictList;
   }

   public int simpleEpgSearch(String lookFor, int type, String cat, String chan, int ignored, int[] times, HashMap<String, Vector<GuideItem>> result)
   {
      int num = 0;

      GuideItem[] progs = null;
      GuideItem guidItem = null;

      DataStore store = DataStore.getInstance();

      Vector<String[]> channelMap = getChannelMap();
      Set<String> wsChannels = store.getChannels().keySet();

      if (channelMap == null || channelMap.size() == 0)
      {
         System.out.println("Channel map not set so could not do search.");
         return 0;
      }

      // set time span
      int startHH = 0;
      int startMM = 0;
      int endHH = 23;
      int endMM = 59;
      if(times != null && times.length == 4)
      {
         startHH = times[0];
         startMM = times[1];
         endHH = times[2];
         endMM = times[3];
      }

      for (int x = 0; x < channelMap.size(); x++)
      {
         String[] chanMap = channelMap.get(x);

         if(wsChannels.contains(chanMap[0]))
         {
            progs = getProgramsForChannel(chanMap[1]);

            if(progs.length > 0)
            {
               Vector<GuideItem> results = new Vector<>();

               for (GuideItem prog : progs) {
                  guidItem = prog;
                  boolean nameMatches = false;
                  // Calvi changes.
                  boolean subnameMatches = false;
                  boolean descriptionMatches = false;
                  boolean actorMatches = false;
                  boolean textMatch = false;
                  boolean catMatches = false;
                  boolean chanMatches = false;
                  boolean ignoreMatches = false;
                  boolean timeSpanMatch = false;

                  if(guidItem.getName().toUpperCase().indexOf(lookFor.toUpperCase()) > -1) {
					nameMatches = true;
				  }

                  if(guidItem.getSubName().toUpperCase().indexOf(lookFor.toUpperCase()) > -1) {
					subnameMatches = true;
				  }

                  if(guidItem.getDescription().toUpperCase().indexOf(lookFor.toUpperCase()) > -1) {
					descriptionMatches = true;
				  }

                  // vectors
                  for (String epgActs : guidItem.getActors()) {
                     if(epgActs.toUpperCase().indexOf(lookFor.toUpperCase()) > -1) {
						actorMatches = true;
					 }
                  }

                  if(type == 0) {
					textMatch = nameMatches | subnameMatches | descriptionMatches;
				  } else if(type == 1) {
					textMatch = nameMatches;
				  } else if(type == 2) {
					textMatch = subnameMatches;
				  } else if(type == 3) {
					textMatch = descriptionMatches;
				  } else if(type == 4) {
					textMatch = actorMatches;
				  }

                  if(cat.equalsIgnoreCase("any"))
                  {
                     catMatches = true;
                  }
                  else
                  {
                     for (String itemCat : guidItem.getCategory()) {
                        if(itemCat.equalsIgnoreCase(cat))
                        {
                           catMatches = true;
                           break;
                        }
                     }
                  }

                  if(chan.equalsIgnoreCase("any")) {
					chanMatches = true;
				  }

                  if(chanMap[0].toUpperCase().indexOf(chan.toUpperCase()) > -1) {
					chanMatches = true;
				  }

                  // does ignore match
                  if(ignored == 2) {
					ignoreMatches = true;
				  } else if(guidItem.getIgnored() && ignored == 1) {
					ignoreMatches = true;
				  } else if(!guidItem.getIgnored() && ignored == 0) {
					ignoreMatches = true;
				  }


                  // do time span matching
                  if(times != null && times.length == 4)
                  {
                     Calendar cal = Calendar.getInstance();
                     cal.setTime(guidItem.getStart());
                     int startMinInDay = (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);

                     int startMinInDaySPAN = (startHH * 60) + startMM;
                     int endMinInDatSPAN = (endHH * 60) + endMM;

                     if(startMinInDaySPAN < endMinInDatSPAN)
                     {
                        if(startMinInDay >= startMinInDaySPAN && startMinInDay <= endMinInDatSPAN) {
							timeSpanMatch = true;
						}
                     }
                     else
                     {
                        if(startMinInDay >= startMinInDaySPAN || startMinInDay <= endMinInDatSPAN) {
							timeSpanMatch = true;
						}
                     }
                  }
                  else
                  {
                     timeSpanMatch = true;
                  }

                  if(catMatches && textMatch && chanMatches && ignoreMatches && timeSpanMatch)
                  {
                     results.add(guidItem);
                     num++;
                  }
               }

               if(results.size() > 0) {
				result.put(chanMap[0], results);
			   }
            }
         }
      }

      return num;
   }
}
