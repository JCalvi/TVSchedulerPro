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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DataStore
{
   private Date lastCheched = new Date();
   private HashMap<String, Date> sessionIDs = new HashMap<>();

   private String version = "N/A";

   private static DataStore instance = null;

   private HashMap<String, String> agentToThemeMap = new HashMap<>();

   private HashMap<String, ScheduleItem> times = null;
   private Date lastDataChange = new Date();
   private HashMap<String, Channel> channels = null;

   private Properties serverProp = new Properties();
   public Random rand = new Random(new Date().getTime());

   public HashMap<Integer, String> monthNameFull = new HashMap<>();
   public HashMap<Integer, String> monthNameShort = new HashMap<>();
   public HashMap<Integer, String> dayName = new HashMap<>();
   public HashMap<Integer, String> dayNameFull = new HashMap<>();
   public HashMap<Integer, String> ampm = new HashMap<>();

   private HashMap<String, KeepForDetails> autoDelList = null;
   private StringBuffer autoDelLog = new StringBuffer(2048);

   private Vector<String> epgWatchList = new Vector<>();
   private Vector<EpgMatch> epgMatchList = new Vector<>();
   private HashMap<String, EpgMatchList> epgMatchLists = new HashMap<>();

   private HashMap<String, TaskCommand> tasks = new HashMap<>();
   public HashMap<String, TaskItemThread> runningTaskList = new HashMap<>();
   public int activeTaskCount = 0;

   private HashMap<String, String> mimeTypes = new HashMap<>();

   public int timerStatus = 0;
   public int adminStatus = 0;
   public String timerThreadErrorStack = "";
   public String adminThreadErrorStack = "";

   private void initMaps()
   {
      monthNameFull.put(Integer.valueOf(Calendar.JANUARY), "January");
      monthNameFull.put(Integer.valueOf(Calendar.FEBRUARY), "February");
      monthNameFull.put(Integer.valueOf(Calendar.MARCH), "March");
      monthNameFull.put(Integer.valueOf(Calendar.APRIL), "April");
      monthNameFull.put(Integer.valueOf(Calendar.MAY), "May");
      monthNameFull.put(Integer.valueOf(Calendar.JUNE), "June");
      monthNameFull.put(Integer.valueOf(Calendar.JULY), "July");
      monthNameFull.put(Integer.valueOf(Calendar.AUGUST), "August");
      monthNameFull.put(Integer.valueOf(Calendar.SEPTEMBER), "September");
      monthNameFull.put(Integer.valueOf(Calendar.OCTOBER), "October");
      monthNameFull.put(Integer.valueOf(Calendar.NOVEMBER), "November");
      monthNameFull.put(Integer.valueOf(Calendar.DECEMBER), "December");

      monthNameShort.put(Integer.valueOf(Calendar.JANUARY), "Jan");
      monthNameShort.put(Integer.valueOf(Calendar.FEBRUARY), "Feb");
      monthNameShort.put(Integer.valueOf(Calendar.MARCH), "Mar");
      monthNameShort.put(Integer.valueOf(Calendar.APRIL), "Apr");
      monthNameShort.put(Integer.valueOf(Calendar.MAY), "May");
      monthNameShort.put(Integer.valueOf(Calendar.JUNE), "Jun");
      monthNameShort.put(Integer.valueOf(Calendar.JULY), "Jul");
      monthNameShort.put(Integer.valueOf(Calendar.AUGUST), "Aug");
      monthNameShort.put(Integer.valueOf(Calendar.SEPTEMBER), "Sep");
      monthNameShort.put(Integer.valueOf(Calendar.OCTOBER), "Oct");
      monthNameShort.put(Integer.valueOf(Calendar.NOVEMBER), "Nov");
      monthNameShort.put(Integer.valueOf(Calendar.DECEMBER), "Dec");

      dayName.put(Integer.valueOf(Calendar.MONDAY), "Mon");
      dayName.put(Integer.valueOf(Calendar.TUESDAY), "Tue");
      dayName.put(Integer.valueOf(Calendar.WEDNESDAY), "Wed");
      dayName.put(Integer.valueOf(Calendar.THURSDAY), "Thu");
      dayName.put(Integer.valueOf(Calendar.FRIDAY), "Fri");
      dayName.put(Integer.valueOf(Calendar.SATURDAY), "Sat");
      dayName.put(Integer.valueOf(Calendar.SUNDAY), "Sun");

      dayNameFull.put(Integer.valueOf(Calendar.MONDAY), "Monday");
      dayNameFull.put(Integer.valueOf(Calendar.TUESDAY), "Tuesday");
      dayNameFull.put(Integer.valueOf(Calendar.WEDNESDAY), "Wednesday");
      dayNameFull.put(Integer.valueOf(Calendar.THURSDAY), "Thursday");
      dayNameFull.put(Integer.valueOf(Calendar.FRIDAY), "Friday");
      dayNameFull.put(Integer.valueOf(Calendar.SATURDAY), "Saturday");
      dayNameFull.put(Integer.valueOf(Calendar.SUNDAY), "Sunday");

      ampm.put(Integer.valueOf(Calendar.AM), "AM");
      ampm.put(Integer.valueOf(Calendar.PM), "PM");
   }

   public static DataStore getInstance()
   {
      synchronized (DataStore.class)
      {
         if (instance == null)
         {
            instance = new DataStore();
            return instance;
         }
         else
         {
            return instance;
         }
      }
   }

   private DataStore()
   {

      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      getVersionDetails();
      System.out.println("New DataStore Object created (" + version + ")");

      try
      {
         File servProp = new File(dataPath + File.separator + "prop" + File.separator + "server.prop");
         FileInputStream in = new FileInputStream(servProp);
         serverProp.load(new FileInputStream(servProp));
         in.close();
      }
      catch(Exception e)
      {
         System.out.println("ERROR loading server.prop (" + e.getMessage() + ")");
      }

      channels = new HashMap<>();

      loadAutoDelList();
      loadChannels();
      loadSchedule();
      initMaps();
      loadMatchList();
      loadEpgAutoList();
      loadEpgWatchList();
      loadTaskList();
      loadAgentToThemeMap();
      loadMineTypes();


      StringBuffer buff = new StringBuffer();
      buff.append("TV Scheduler Pro was started (" + new Date().toString() + ")\n");
      buff.append("Following is a summary of the startup details:\n\n");

      buff.append("Server Name             : " + getComputerName() + "\n");
      buff.append("Server Version          : " + getVersion() + "\n");
      buff.append("Channel Count           : " + numberOfChannels() + "\n");
      buff.append("Number of Schedules     : " + times.size() + "\n");

      sendEmailServerStarted(buff.toString());
   }

   @Override
public String toString()
   {
      return "DataStore Properties Loaded:" + serverProp.size();

   }

   // Get computername from environment
   public String getComputerName()
   {
       Map<String, String> env = System.getenv();
       if (env.containsKey("COMPUTERNAME")) {
		return env.get("COMPUTERNAME");
	   } else if (env.containsKey("HOSTNAME")) {
		return env.get("HOSTNAME");
	   } else {
		return "Unknown Computer";
	   }
   }

   // Get version from version.txt file
   private void getVersionDetails()
   {
      try
      {
         Properties verProp = new Properties();
         FileInputStream in = new FileInputStream("version.txt");
         verProp.load(in);
         in.close();
         version = verProp.getProperty("version", "N/A");
      }
      catch (Exception e)
      {
         System.out.println("ERROR could not load version info");
      }
   }

   public String createSessionID()
   {
      // Keep only Latest 5
      List<Map.Entry<String, Date>> entrylist = new ArrayList<>(sessionIDs.entrySet());
      Collections.sort(entrylist, new SecurityPinSorter());
      sessionIDs.clear();
      for(int x = 0; x < entrylist.size() && x < 4; x++)
      {
         Map.Entry<String, Date> entry = entrylist.get(x);
         sessionIDs.put(entry.getKey(), entry.getValue());
      }

      String[] ids = sessionIDs.keySet().toArray(new String[0]);
      // remove old id's
      Date now = new Date();
      for (String id : ids) {
         Date createDate = sessionIDs.get(id);
         if((createDate.getTime() + (1000 * 60 * 3)) < now.getTime())
         {
            sessionIDs.remove(id);
            System.out.println("Removed Old Session ID : " + id + " : " + ((now.getTime() - createDate.getTime()) / (1000)));
         }
      }

      Random ran = new Random();
      String ranNum = Integer.valueOf(ran.nextInt(999999)).toString();
      if(ranNum.length() == 1) {
		ranNum = "00000" + ranNum;
	  }
      if(ranNum.length() == 2) {
		ranNum = "0000" + ranNum;
	  }
      if(ranNum.length() == 3) {
		ranNum = "000" + ranNum;
	  }
      if(ranNum.length() == 4) {
		ranNum = "00" + ranNum;
	  }
      if(ranNum.length() == 5) {
		ranNum = "0" + ranNum;
	  }

      sessionIDs.put(ranNum, new Date());

      entrylist = new ArrayList<>(sessionIDs.entrySet());
      Collections.sort(entrylist, new SecurityPinSorter());
      for(int x = 0; x < entrylist.size() && x < 5; x++)
      {
         Map.Entry<String, Date> entry = entrylist.get(x);
         System.out.println("Session ID : " + x + " " + entry.getKey() + " " + (entry.getValue()).toString());
      }

      return ranNum;
   }

   public boolean checkSessionID(String id)
   {
      // are we using captcha security? if not return true
      String captcha = getProperty("security.captcha");
      if("0".equals(captcha)) {
		return true;
	  }

      synchronized(this)
      {
         long sleepFor = (new Date()).getTime() - lastCheched.getTime();
         if(sleepFor < 10000)
         {
            try
            {
               Thread.sleep(10000 - sleepFor);
            }
            catch(Exception e)
            {}
         }

         lastCheched = new Date();

         // remove old id's
         String[] ids = sessionIDs.keySet().toArray(new String[0]);
         Date now = new Date();
         for (String id2 : ids) {
            Date createDate = sessionIDs.get(id2);
            if((createDate.getTime() + (1000 * 60 * 3)) < now.getTime())
            {
               System.out.println("Removed Old Session ID : " + id2 + " - " + createDate);
               sessionIDs.remove(id2);
            }
         }

         Date testDate = sessionIDs.get(id);
         if(testDate != null)
         {
            sessionIDs.remove(id);
            System.out.println("Removed Used Session ID : " + id + " - " + testDate);
            return true;
         } else {
			return false;
		 }
      }
   }

   private void sendEmailServerStarted(String body)
   {
      String sendServerStarted = getProperty("email.send.serverstarted");
      if(!"1".equals(sendServerStarted)) {
		return;
	  }

      EmailSender sender = new EmailSender();

      sender.setSubject("TV Scheduler Pro Started");
      sender.setBody(body);

      try
      {
         Thread mailThread = new Thread(Thread.currentThread().getThreadGroup(), sender, sender.getClass().getName());
         mailThread.start();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   //  Agent to theme mapping methods

   public String getThemeForAgent(String agent)
   {
      return agentToThemeMap.get(agent);
   }

   public String[] getAgentMappingList()
   {
      return agentToThemeMap.keySet().toArray(new String[0]);
   }

   public void addAgentToThemeMap(String agent, String theme)
   {
      agentToThemeMap.put(agent, theme);
      saveAgentToThemeMap(null);
   }

   public void removeAgentToThemeMap(String agent)
   {
      agentToThemeMap.remove(agent);
      saveAgentToThemeMap(null);
   }

   public int saveAgentToThemeMap(ByteArrayOutputStream agentMapBytes)
   {
      try
      {
         ObjectOutputStream oos = null;
         if(agentMapBytes == null)
         {
            String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
            FileOutputStream fos = new FileOutputStream(dataPath + File.separator + "sof" + File.separator + "AgentMap.sof");
            oos = new ObjectOutputStream(fos);
         }
         else
         {
            oos = new ObjectOutputStream(agentMapBytes);
         }
         oos.writeObject(agentToThemeMap);
         oos.close();

         if(agentMapBytes == null) {
			System.out.println("AgentMap.sof saved.");
		 }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return -1;
      }
      return 0;
   }

   @SuppressWarnings("unchecked")
   public void importAgentToThemeMap(byte[] agentMapBytes) throws Exception
   {
      ByteArrayInputStream mapBytes = new ByteArrayInputStream(agentMapBytes);
      ObjectInputStream ois = new ObjectInputStream(mapBytes);
      agentToThemeMap = (HashMap<String, String>) ois.readObject();
      ois.close();
      System.out.println("Agent to Theme Map imported");

      this.saveAgentToThemeMap(null);
   }

   @SuppressWarnings("unchecked")
   private void loadAgentToThemeMap()
   {
      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileInputStream fis = new FileInputStream(dataPath + File.separator + "sof" + File.separator + "AgentMap.sof");
         ObjectInputStream ois = new ObjectInputStream(fis);
         agentToThemeMap = (HashMap<String, String>) ois.readObject();
         ois.close();
         System.out.println("AgentMap.sof found and loaded");
         refreshWakeupTime();
      }
      catch (Exception e)
      {
         agentToThemeMap = new HashMap<>();
         //e.printStackTrace();
         System.out.println("ERROR loading AgentMap.sof, starting with blank map");
      }
   }

   //  tasks methods

   private void loadTaskList()
   {
      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         Document doc = docBuilder.parse(new File(dataPath + File.separator + "xml" + File.separator + "Tasks.xml"));

         NodeList tasksNodes = doc.getElementsByTagName("task");

         tasks = new HashMap<>();

         for(int x = 0; x < tasksNodes.getLength(); x++)
         {
            Node item = tasksNodes.item(x);
            TaskCommand taskCommand = new TaskCommand(item);
            tasks.put(taskCommand.getName(), taskCommand);
         }

         System.out.println("Tasks.xml found and loaded (" + tasksNodes.getLength() + ")");
      }
      catch (Exception e)
      {
         tasks = new HashMap<>();
         System.out.println("ERROR loading Tasks.xml, starting with no tasks.");
         //e.printStackTrace();
      }
   }

   public void importTaskList(String data, boolean append) throws Exception
   {
      HashMap<String, TaskCommand> importedTasks = new HashMap<>();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(data.toString().getBytes());
      Document doc = docBuilder.parse(reader);

      NodeList tasksNodes = doc.getElementsByTagName("task");

      for(int x = 0; x < tasksNodes.getLength(); x++)
      {
         Node item = tasksNodes.item(x);
         TaskCommand taskCommand = new TaskCommand(item);
         importedTasks.put(taskCommand.getName(), taskCommand);
      }

      if(append)
      {
         if(tasks == null) {
			tasks = new HashMap<>();
		 }

         tasks.putAll(importedTasks);
      }
      else
      {
         tasks = importedTasks;
      }

      saveTaskList(null);
   }

   public void saveTaskList(StringBuffer output) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "tasks", null);
      Element root = doc.getDocumentElement();

      String[] keys = tasks.keySet().toArray(new String[0]);

      for (String key : keys) {
         TaskCommand taskData = tasks.get(key);
         taskData.addXML(doc, root);
      }

      ByteArrayOutputStream buff = new ByteArrayOutputStream();
      TransformerFactory factory = TransformerFactory.newInstance();

      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new DOMSource(doc);
      Result result = new StreamResult(buff);
      transformer.transform(source, result);

      if(output != null)
      {
         output.append(buff.toString());
      }
      else
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileWriter out = new FileWriter(dataPath + File.separator + "xml" + File.separator + "Tasks.xml");
         out.write(buff.toString());
         out.close();
         System.out.println("Tasks.xml saved.");
      }
   }

   public HashMap<String, TaskCommand> getTaskList()
   {
      return tasks;
   }

   //  EPG methods

   private void loadMatchList()
   {
      epgMatchLists = new HashMap<>();
      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         Document doc = docBuilder.parse(new File(dataPath + File.separator + "xml" + File.separator + "MatchList.xml"));

         NodeList items = doc.getElementsByTagName("MatchList");

         for(int x = 0; x < items.getLength(); x++)
         {
            Node item = items.item(x);
            NamedNodeMap itemAttribs = item.getAttributes();
            String name = itemAttribs.getNamedItem("name").getTextContent();

            EpgMatchList matcher = new EpgMatchList(item);

            epgMatchLists.put(name, matcher);
         }
         System.out.println("MatchList.xml found and loaded (" + epgMatchLists.size() + ")");
      }
      catch (Exception e)
      {
         epgMatchLists = new HashMap<>();
         System.out.println("ERROR loading MatchList.xml, starting with no Match Lists");
         //e.printStackTrace();
      }
   }

   public void importEpgWatchList(String data) throws Exception
   {
      epgWatchList = new Vector<>();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(data.toString().getBytes());
      Document doc = docBuilder.parse(reader);

      NodeList items = doc.getElementsByTagName("item");

      for(int x = 0; x < items.getLength(); x++)
      {
        Node item = items.item(x);
        String itemText = item.getTextContent();
        epgWatchList.add(itemText);
      }

      saveEpgWatchList(null);
   }
   public void importMatchList(String data, boolean append) throws Exception
   {
      HashMap<String, EpgMatchList> importedMatchList = new HashMap<>();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(data.toString().getBytes());
      Document doc = docBuilder.parse(reader);

      NodeList items = doc.getElementsByTagName("MatchList");

      for(int x = 0; x < items.getLength(); x++)
      {
         Node item = items.item(x);
         NamedNodeMap itemAttribs = item.getAttributes();
         String name = itemAttribs.getNamedItem("name").getTextContent();

         EpgMatchList matcher = new EpgMatchList(item);

         importedMatchList.put(name, matcher);
      }

      if(append)
      {
         epgMatchLists.putAll(importedMatchList);
      }
      else
      {
         epgMatchLists = importedMatchList;
      }

      saveMatchList(null);
   }

   public void saveMatchList(StringBuffer output) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "match-list", null);

      String[] keys = epgMatchLists.keySet().toArray(new String[0]);
      for (String key : keys) {
         EpgMatchList item = epgMatchLists.get(key);
         item.getXML(doc, key);
      }

      ByteArrayOutputStream buff = new ByteArrayOutputStream();
      TransformerFactory factory = TransformerFactory.newInstance();

      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new DOMSource(doc);
      Result result = new StreamResult(buff);
      transformer.transform(source, result);

      if(output != null)
      {
         output.append(buff.toString());
      }
      else
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileWriter out = new FileWriter(dataPath + File.separator + "xml" + File.separator + "MatchList.xml");
         out.write(buff.toString());
         out.close();
         System.out.println("MatchList.xml saved.");
      }
   }

   public HashMap<String, EpgMatchList> getMatchLists()
   {
      return epgMatchLists;
   }

   private void loadEpgAutoList()
   {
      epgMatchList = new Vector<>();
      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         Document doc = docBuilder.parse(new File(dataPath + File.separator + "xml" + File.separator + "EpgAutoAdd.xml"));

         NodeList items = doc.getElementsByTagName("item");

         for(int x = 0; x < items.getLength(); x++)
         {
            Node item = items.item(x);
            EpgMatch matcher = new EpgMatch(item);
            epgMatchList.add(matcher);
         }
         System.out.println("EpgAutoAdd.xml found and loaded (" + items.getLength() + ")");
      }
      catch (Exception e)
      {
         epgMatchList = new Vector<>();
         System.out.println("ERROR loading EpgAutoAdd.xml, starting with no AutoAdds");
         //e.printStackTrace();
      }
   }

   public void importEpgAutoList(String data, boolean append) throws Exception
   {
      Vector<EpgMatch> importedData = new Vector<>();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(data.toString().getBytes());
      Document doc = docBuilder.parse(reader);

      NodeList items = doc.getElementsByTagName("item");

      for(int x = 0; x < items.getLength(); x++)
      {
         Node item = items.item(x);
         EpgMatch matcher = new EpgMatch(item);
         importedData.add(matcher);
      }

      if(append)
      {
         epgMatchList.addAll(importedData);
      }
      else
      {
         epgMatchList = importedData;
      }


      saveEpgAutoList(null);
   }

   public void saveEpgAutoList(StringBuffer output) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "auto-add", null);
      Element root = doc.getDocumentElement();

      for (EpgMatch item : epgMatchList) {
         item.getXML(doc, root);
      }

      ByteArrayOutputStream buff = new ByteArrayOutputStream();
      TransformerFactory factory = TransformerFactory.newInstance();

      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new DOMSource(doc);
      Result result = new StreamResult(buff);
      transformer.transform(source, result);

      if(output != null)
      {
         output.append(buff.toString());
      }
      else
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileWriter out = new FileWriter(dataPath + File.separator + "xml" + File.separator + "EpgAutoAdd.xml");
         out.write(buff.toString());
         out.close();
         System.out.println("EpgAutoAdd.xml saved.");
      }
   }

   private void loadEpgWatchList()
   {
      epgWatchList = new Vector<>();

      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         Document doc = docBuilder.parse(new File(dataPath + File.separator + "xml" + File.separator + "EpgWatchList.xml"));

         NodeList items = doc.getElementsByTagName("item");

         for(int x = 0; x < items.getLength(); x++)
         {
            Node item = items.item(x);
            String itemText = item.getTextContent();
            epgWatchList.add(itemText);
         }
         System.out.println("EpgWatchList.xml found and loaded (" + items.getLength() + ")");
      }
      catch (Exception e)
      {
         epgWatchList = new Vector<>();
         System.out.println("ERROR loading EpgWatchList.xml, starting with no Watch List");
         //e.printStackTrace();
      }
   }

   public void saveEpgWatchList(StringBuffer output) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "watch-list", null);
      Element root = doc.getDocumentElement();

      for (String element : epgWatchList) {
         Element item = doc.createElement("item");
         Text text = doc.createTextNode(element);
         item.appendChild(text);
         root.appendChild(item);
      }

      ByteArrayOutputStream buff = new ByteArrayOutputStream();
      TransformerFactory factory = TransformerFactory.newInstance();

      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new DOMSource(doc);
      Result result = new StreamResult(buff);
      transformer.transform(source, result);

      if(output != null)
      {
         output.append(buff.toString());
      }
      else
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileWriter out = new FileWriter(dataPath + File.separator + "xml" + File.separator + "EpgWatchList.xml");
         out.write(buff.toString());
         out.close();
         System.out.println("EpgWatchList.xml saved.");
      }
   }

   public Vector<String> getEpgWatchList()
   {
      return epgWatchList;
   }

   public void addEpgWatchList(String watch) throws Exception
   {
      if(!epgWatchList.contains(watch))
      {
         epgWatchList.add(watch);
         saveEpgWatchList(null);
      }
   }

   public void removeEpgWatchList(String watch) throws Exception
   {
      if(epgWatchList.remove(watch)) {
		saveEpgWatchList(null);
	  }
   }

   public Vector<EpgMatch> getEpgMatchList()
   {
      return epgMatchList;
   }

   public void addEpgMatch(EpgMatch match) throws Exception
   {
      epgMatchList.add(match);
      saveEpgAutoList(null);
   }

   public void addEpgMatch(EpgMatch match, int index) throws Exception
   {
      epgMatchList.add(index, match);
      saveEpgAutoList(null);
   }

   public void remEpgMatch(int id) throws Exception
   {
      epgMatchList.remove(id);
      saveEpgAutoList(null);
   }

   public void moveEpgItem(int id, boolean direction) throws Exception
   {
      int dest = 0;

      if(direction) {
		dest = id + 1;
	  } else {
		dest = id - 1;
	  }

      if(dest >= 0 && dest < epgMatchList.size())
      {
         EpgMatch obj = epgMatchList.remove(id);

         if(direction) {
			epgMatchList.add(id+1, obj);
		 } else {
			epgMatchList.add(id-1, obj);
		 }

         saveEpgAutoList(null);
      }
   }

   public int removeEPGitems(StringBuffer buff, int format)
   {
      int num = 0;

      ScheduleItem item = null;

      String[] ids = (times.keySet()).toArray(new String[0]);

      for (String id : ids) {
         item = times.get(id);

         if (item.getType() == ScheduleItem.EPG && item.getState() == ScheduleItem.WAITING)
         {
            times.remove(id);
            num++;
         }
      }

      if (format == 1) {
		buff.append("Removed " + num + " item of type EPG from the schedule list.<br><br>\n\n");
	  } else {
		buff.append("Removed " + num + " item of type EPG from the schedule list.\n\n");
	  }

      return num;
   }

   //  Schedule methods

   public ScheduleItem[] getSchedulesWhen(Date start, Date end, String channel)
   {
      Vector<ScheduleItem> items = new Vector<>();
      Iterator<ScheduleItem> it = times.values().iterator();
      ScheduleItem item = null;

      while (it.hasNext())
      {
         item = it.next();

         if (start.getTime() < item.getStart().getTime() && end.getTime() > item.getStart().getTime())
         {
            if (channel.equals(item.getChannel()))
            {
               //System.out.println(item.getStart() + " " + item.getName());
               items.add(item);
            }
         }
      }

      ScheduleItem[] itemList = items.toArray(new ScheduleItem[0]);
      Arrays.sort(itemList);
      //System.out.println("Getting Schedules for " + start + " " + end + " " +
      // channel + " Found:" + itemList.length);
      return itemList;
   }

   public void getSchedulesWhenInc(Date start, Date end, String channel, Vector<ScheduleItem> items)
   {
      ScheduleItem[] schedules = times.values().toArray(new ScheduleItem[0]);
      Arrays.sort(schedules);

      for (ScheduleItem item : schedules) {
         if (item.getStop().getTime() > (start.getTime() + 5000) && item.getStart().getTime() < end.getTime())
         {
            if (channel.equals(item.getChannel()))
            {
               //System.out.println(item.getStart() + " " + item.getName());
               items.add(item);
            }
         }
      }
   }

   public ScheduleItem getNextSchedule()
   {
      Date now = new Date();
      ScheduleItem[] itemsArray = times.values().toArray(new ScheduleItem[0]);
      Arrays.sort(itemsArray);

      for (ScheduleItem item : itemsArray) {
         if(item.getStop().getTime() > now.getTime() && item.getState() == ScheduleItem.WAITING)
         {
            return item;
         }
      }
      return null;
   }

   public String[] getScheduleKeys()
   {
      return times.keySet().toArray(new String[0]);
   }

   public ScheduleItem[] getScheduleArray()
   {
      return times.values().toArray(new ScheduleItem[0]);
   }

   public int getScheduleCount()
   {
      return times.size();
   }

   public ScheduleItem getScheduleItem(String id)
   {
      return times.get(id);
   }

   public int addScheduleItem(ScheduleItem item)
   {
      times.put(item.toString(), item);
      saveSchedule(null);
      return 1;
   }

   public ScheduleItem removeScheduleItem(String id)
   {
      ScheduleItem removedItem = times.remove(id);
      return removedItem;
   }


   //  Channel methods


   public void addChannel(Channel chan)
   {
      channels.put(chan.getName(), chan);
   }

   public Channel getChannel(String chID)
   {
      return channels.get(chID);
   }

   public int removeChannel(String name)
   {
      channels.remove(name);
      return 0;
   }

   public HashMap<String, Channel> getChannels()
   {
      return channels;
   }

   private void loadChannels()
   {
      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         Document doc = docBuilder.parse(new File(dataPath + File.separator + "xml" + File.separator + "Channels.xml"));

         NodeList items = doc.getElementsByTagName("channel");

         channels = new HashMap<>();

         for(int x = 0; x < items.getLength(); x++)
         {
            Node item = items.item(x);
            Channel chan = new Channel(item);
            channels.put(chan.getName(), chan);
         }
         System.out.println("Channels.xml found and loaded (" + items.getLength() + ")");
      }
      catch (Exception e)
      {
         channels = new HashMap<>();
         System.out.println("ERROR loading Channels.xml, starting with blank channel list.");
         //e.printStackTrace();
      }
   }

   public void importChannels(String data, boolean append) throws Exception
   {
      HashMap<String, Channel> importedChannels = new HashMap<>();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      ByteArrayInputStream reader = new ByteArrayInputStream(data.toString().getBytes());
      Document doc = docBuilder.parse(reader);

      NodeList items = doc.getElementsByTagName("channel");

      for(int x = 0; x < items.getLength(); x++)
      {
         Node item = items.item(x);
         Channel chan = new Channel(item);
         importedChannels.put(chan.getName(), chan);
      }

      if(append)
      {
         if(channels == null) {
			channels = new HashMap<>();
		 }

         channels.putAll(importedChannels);
      }
      else
      {
         channels = importedChannels;
      }

      saveChannels(null);
   }

   public void saveChannels(StringBuffer output) throws Exception
   {
      // Creation of an XML document
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();

      Document doc = di.createDocument("", "channels", null);
      Element root = doc.getDocumentElement();

      String[] keys = channels.keySet().toArray(new String[0]);

      for (String key : keys) {
         Channel chan = channels.get(key);
         root.appendChild(chan.getXML(doc));
      }

      ByteArrayOutputStream buff = new ByteArrayOutputStream();
      TransformerFactory factory = TransformerFactory.newInstance();

      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      Source source = new DOMSource(doc);
      Result result = new StreamResult(buff);
      transformer.transform(source, result);

      if(output != null)
      {
         output.append(buff.toString());
      }
      else
      {
          String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
          FileWriter out = new FileWriter(dataPath + File.separator + "xml" + File.separator + "Channels.xml");
          out.write(buff.toString());
          out.close();
          System.out.println("Channels.xml saved.");
      }
   }

   public int numberOfChannels()
   {
      return channels.size();
   }

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////

   public void setServerProperty(String pKey, String value) throws Exception
   {
      pKey = pKey.toLowerCase();
      serverProp.setProperty(pKey, value);
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      File servProp = new File(dataPath + File.separator + "prop" + File.separator + "server.prop");
      FileOutputStream out = new FileOutputStream(servProp);
      serverProp.store(out, "TV Scheduler Pro Server Properties");
      out.close();
   }

   public String getProperty(String pKey)
   {
      pKey = pKey.toLowerCase();

      String prop = serverProp.getProperty(pKey, null);
      if (prop != null) {
		return prop;
	  }

      String def = getDefProp(pKey);

      try
      {
         setServerProperty(pKey, def);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return def;
   }

   private String getDefProp(String key)
   {
      HashMap<String, String> defprop = new HashMap<>();

      defprop.put("tools.testmode", "0");
      defprop.put("capture.path", "capture");
      defprop.put("path.theme.epg", "epg-Calvi.xsl");
      defprop.put("capture.minspacesoft", "1200");
      defprop.put("capture.minspacehard", "200");
      defprop.put("capture.deletetofreespace", "0");
      defprop.put("capture.path.details", "none");
      defprop.put("capture_tvnfo.path.details", "none");
      defprop.put("capture_epnfo.path.details", "none");
      defprop.put("capture_art.path.details", "none");
      defprop.put("schedule.wake.system", "45");
      defprop.put("guide.source.schedule", "0:0:0");
      defprop.put("capture.filename.patterns", "default;(%y-%m-%d %h-%M) %n %c;%n %c;%n;%D %n;%n\\%y-%m-%d %h-%M");
      defprop.put("path.httproot", "http");
      defprop.put("path.theme", "calvi");
      defprop.put("capture.merged.separate", "1");
      defprop.put("schedule.buffer.start", "5");
      defprop.put("schedule.buffer.end", "10");
      defprop.put("schedule.buffer.end.epg", "0");
      defprop.put("schedule.overlap", "0");
      defprop.put("autodel.keepfor", "30");
      defprop.put("capture.deftype", "2");
      defprop.put("filebrowser.masks", ".log,.mpg,.mpeg,.bin,.mpv,.mpa,.ts,.tp,.dvr-ms,.rec,.pva,.avi");
      defprop.put("guide.action.name", "0");
      defprop.put("guide.item.cfmatch.inexact", "0");
      defprop.put("guide.item.cftol.start", "0");
      defprop.put("guide.item.cftol.duration", "0");
      defprop.put("guide.item.igmatch.inexact", "0");
      defprop.put("guide.item.igtol.start", "0");
      defprop.put("guide.item.igtol.duration", "0");
      defprop.put("guide.source.type", "0");
      defprop.put("filebrowser.dirsattop", "1");
      defprop.put("filebrowser.showwsplay", "1");
      defprop.put("guide.source.file", "xmltv");
      defprop.put("sch.autodel.time", "0");
      defprop.put("sch.autodel.action", "0");
      defprop.put("server.kbled", "0");
      defprop.put("proxy.port", "8080");
      defprop.put("proxy.server", "");
      defprop.put("guide.warn.overlap", "1");
	  defprop.put("guide.clip.start", "0");
      defprop.put("guide.search.url", "http://www.google.com.au/search?q=$TITLE $SUB $CAT");
      defprop.put("capture.averagedatarate", "7000000");
      defprop.put("capture.includecalculatedUsage", "1");
      defprop.put("capture.autoselectmethod", "0");
      defprop.put("capture.capturefailedtimeout", "060");
      defprop.put("epg.showunlinked", "0");
      defprop.put("email.server.address", "");
      defprop.put("email.server.port", "25");
      defprop.put("email.from", "");
      defprop.put("email.to", "");
      defprop.put("email.auth.enabled", "0");
      defprop.put("email.auth.user", "");
      defprop.put("email.auth.password", "");
      defprop.put("email.security", "0");
      defprop.put("email.debug.enabled", "0");
      defprop.put("email.send.weeklyreport", "0");
      defprop.put("email.send.capfinished", "0");
      defprop.put("email.send.epgloaded", "0");
      defprop.put("email.send.onwarning", "0");
      defprop.put("email.send.freespacelow", "0");
      defprop.put("email.send.serverstarted", "0");
      defprop.put("security.captcha", "1");
      defprop.put("security.highsecurity", "0");
      defprop.put("security.accesslog", "0");
      defprop.put("security.authentication", "0");
      defprop.put("security.username", "user");
      defprop.put("security.password", "password");

      String defValue = defprop.get(key);

      if(defValue == null)
      {
         System.out.println("Server property (" + key + ") does not have a default value in DataStore.getDefProp()!");
         return "";
      }
      else
      {
         return defValue;
      }
   }

   public String getLastDataChange()
   {
      return lastDataChange.toString();
   }

   public void saveSchedule(ByteArrayOutputStream timesBytes)
   {
      try
      {
         ObjectOutputStream oos = null;
         if(timesBytes == null)
         {
            String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
            FileOutputStream fos = new FileOutputStream(dataPath + File.separator + "sof" + File.separator + "Times.sof");
            oos = new ObjectOutputStream(fos);
         }
         else
         {
            oos = new ObjectOutputStream(timesBytes);
         }
         oos.writeObject(times);
         oos.close();
         //System.out.println("Times.sof saved.");
         refreshWakeupTime();
         lastDataChange = new Date();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @SuppressWarnings("unchecked")
   public void importSchedule(byte[] timesBytes) throws Exception
   {
      ByteArrayInputStream timeBytes = new ByteArrayInputStream(timesBytes);
      ObjectInputStream ois = new ObjectInputStream(timeBytes);
      times = (HashMap<String, ScheduleItem>) ois.readObject();
      ois.close();
      System.out.println("Times imported (" + times.size() + ")");
      refreshWakeupTime();

      saveSchedule(null);
   }

   @SuppressWarnings("unchecked")
   private void loadSchedule()
   {
      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileInputStream fis = new FileInputStream(dataPath + File.separator + "sof" + File.separator + "Times.sof");
         ObjectInputStream ois = new ObjectInputStream(fis);
         times = (HashMap<String, ScheduleItem>) ois.readObject();
         ois.close();
         System.out.println("Times.sof found and loaded (" + times.size() + ")");
         refreshWakeupTime();
      }
      catch (Exception e)
      {
         times = new HashMap<>();
         //e.printStackTrace();
         System.out.println("ERROR loading Times.sof, starting with blank schedule");
      }
   }

   public int refreshWakeupTime()
   {
      int allow = 0;
      try
      {
         allow = Integer.parseInt(getProperty("schedule.wake.system").trim());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      if (allow == 0)
      {
         System.out.println("Not Using wake up STUFF");
         return -2;
      }

      //System.out.println("Allowing " + allow + " seconds for wake up");

      DllWrapper capEng = new DllWrapper();

      Calendar wakeAt = getClosestStart();

      int sec = 0;
      int min = 0;
      int hour = 0;
      int day = 0;
      int month = 0;
      int year = 0;

      if(wakeAt != null)
      {
         sec = wakeAt.get(Calendar.SECOND);
         min = wakeAt.get(Calendar.MINUTE);
         hour = wakeAt.get(Calendar.HOUR_OF_DAY);
         day = wakeAt.get(Calendar.DATE);
         month = wakeAt.get(Calendar.MONTH) + 1;
         year = wakeAt.get(Calendar.YEAR);

         capEng.setNextScheduleTime(year, month, day, hour, min, sec);
      }

      //
      // Now get next Sch Data Load event
      //
      String scheduleOptions = getProperty("guide.source.schedule");
      String[] schOptsArray = scheduleOptions.split(":");
      if (schOptsArray.length == 3)
      {
         if ("1".equals(schOptsArray[0]))
         {
            int data_hour = 0;
            try
            {
               data_hour = Integer.parseInt(schOptsArray[1]);
            }
            catch (Exception e)
            {
            }

            int data_min = 0;
            try
            {
               data_min = Integer.parseInt(schOptsArray[2]);
            }
            catch (Exception e)
            {
            }

            Calendar guideSch = Calendar.getInstance();
            guideSch.set(Calendar.HOUR_OF_DAY, data_hour);
            guideSch.set(Calendar.MINUTE, data_min);
            guideSch.set(Calendar.MILLISECOND, 0);
            guideSch.set(Calendar.SECOND, 0);

            if (guideSch.before(Calendar.getInstance())) {
				guideSch.add(Calendar.DATE, 1);
			}

            if (wakeAt == null || guideSch.before(wakeAt)) {
				wakeAt = guideSch;
			}
         }
      }

      // could not work out the next wake time
      if (wakeAt == null)
      {
         System.out.println("refreshWakeupTime() Failed! could not work out the next wake time.");
         return -1;
      }

      wakeAt.add(Calendar.SECOND, (0 - allow));

      sec = wakeAt.get(Calendar.SECOND);
      min = wakeAt.get(Calendar.MINUTE);
      hour = wakeAt.get(Calendar.HOUR_OF_DAY);
      day = wakeAt.get(Calendar.DATE);
      month = wakeAt.get(Calendar.MONTH) + 1;
      year = wakeAt.get(Calendar.YEAR);

      System.out.println("About to set wake up time " + day + "/" + month + "/" + year + " " + hour + ":" + min + ":" + sec + " -" + allow);

      int result = capEng.setWakeUpTime(year, month, day, hour, min, sec);

      if(result != 0) {
		System.out.println("SetWaitableTimer FAILED! (" + result + ")");
	  } else {
		System.out.println("SetWaitableTimer Succeeded (" + result + ")");
	  }

      return result;
   }

   private Calendar getClosestStart()
   {
      Calendar nearest = Calendar.getInstance();
      nearest.add(Calendar.YEAR, 10);

      Calendar start = Calendar.getInstance();
      Calendar now = Calendar.getInstance();

      String[] keys = times.keySet().toArray(new String[0]);
      Arrays.sort(keys);

      for (String key : keys) {

         ScheduleItem item = times.get(key);
         if (item == null)
         {
            System.out.println("ERROR for some reason one of your schedule items in the MAP is null : " + key);
            break;
         }

         start.setTime(item.getStart());

         if (start.after(now))
         {
            if (start.before(nearest)) {
				nearest.setTime(start.getTime());
			}
         }

      }

      return nearest;
   }


   //  Auto Del methods


   public HashMap<String, KeepForDetails>  getAutoDelList()
   {
      return autoDelList;
   }

   public void saveAutoDelList()
   {
      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileOutputStream fos = new FileOutputStream(dataPath + File.separator + "sof" + File.separator + "AutoDel.sof");
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(autoDelList);
         oos.close();
         System.out.println("AutoDel.sof saved.");
      }
      catch (Exception e)
      {
         System.out.println("ERROR loading AutoDel.sof, will start with no Auto Delete list.");
      }
   }

   @SuppressWarnings("unchecked")
   private void loadAutoDelList()
   {
      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         FileInputStream fis = new FileInputStream(dataPath + File.separator + "sof" + File.separator + "AutoDel.sof");
         ObjectInputStream ois = new ObjectInputStream(fis);
         autoDelList = (HashMap<String, KeepForDetails>) ois.readObject();
         ois.close();
         System.out.println("AutoDel.sof loaded");
      }
      catch (Exception e)
      {
         autoDelList = new HashMap<>();
         System.out.println("ERROR loading AutoDel.sof, starting with empty list.");
      }
   }

   public void addAutoDeleteItem(String fileName, int keepFor)
   {
      if (fileName == null) {
		return;
	  }

      String[] key = autoDelList.keySet().toArray(new String[0]);
      for (String element : key) {
         KeepForDetails item = autoDelList.get(element);
         if (fileName.equalsIgnoreCase(item.getFileName())) {
			autoDelList.remove(element);
		 }
      }

      KeepForDetails kpd = new KeepForDetails(fileName, keepFor);
      String key02 = Long.valueOf(new Date().getTime()).toString() + "-" + Long.valueOf(rand.nextLong()).toString();
      autoDelList.put(key02, kpd);
      saveAutoDelList();
   }

   public void resetAutoDelLog()
   {
      autoDelLog = new StringBuffer(2048);
   }

   public void autoDelLogAdd(String log)
   {
      autoDelLog.append(log + "<br>\n");
   }

   public String getAutoDelLog()
   {
      return autoDelLog.toString();
   }

   public String getVersion()
   {
      return version;
   }

   public String intToStr(int num)
   {
      if (num > -1 && num < 10) {
		return ("0" + (Integer.valueOf(num).toString()));
	  } else {
		return (Integer.valueOf(num).toString());
	  }
   }

   public String[] getCapturePaths()
   {
      String patterns = getProperty("capture.path");
      String[] line = patterns.split(";");
      String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
      for (int x = 0; x < line.length; x++)
      {
          line[x] = line[x].trim();
          File locationDir = new File(line[x]);
          // Check Location and assume its a relative path if not resolvable
          if (!locationDir.exists() || !locationDir.isDirectory() )
          {
            line[x] = dataPath + File.separator + line[x];
          }
      }
      return line;
   }

   public void addCapturePath(String newPath) throws Exception
   {
      String patterns = getProperty("capture.path");
      patterns += ";" + newPath;
      this.setServerProperty("capture.path", patterns);
   }

   public void deleteCapturePath(int index) throws Exception
   {
      String patterns = getProperty("capture.path");
      String[] line = patterns.split(";");

      String newNames = "";
      for (int x = 0; x < line.length; x++)
      {
         if(x != index)
         {
            newNames += line[x].trim() + ";";
         }
      }
      newNames = newNames.substring(0, newNames.length()-1);

      this.setServerProperty("capture.path", newNames);
   }

   public void moveCapturePath(int index, int amount) throws Exception
   {
      String patterns = getProperty("capture.path");
      String[] line = patterns.split(";");

      int newIndex = index + amount;

      if(newIndex < 0 || newIndex > line.length-1) {
		return;
	  }

      String temp = line[newIndex];
      line[newIndex] = line[index];
      line[index] = temp;

      String newNames = "";
      for (String element : line) {
            newNames += element.trim() + ";";
      }
      newNames = newNames.substring(0, newNames.length()-1);

      this.setServerProperty("capture.path", newNames);
   }

   public String[] getNamePatterns()
   {
      String patterns = getProperty("capture.filename.patterns");
      String[] line = patterns.split(";");
      for (int x = 0; x < line.length; x++)
      {
         line[x] = line[x].trim();
      }
      return line;
   }

   public void deleteNamePattern(int index) throws Exception
   {
      String patterns = getProperty("capture.filename.patterns");
      String[] line = patterns.split(";");

      String newNames = "";
      for (int x = 0; x < line.length; x++)
      {
         if(x != index)
         {
            newNames += line[x].trim() + ";";
         }
      }
      newNames = newNames.substring(0, newNames.length()-1);

      this.setServerProperty("capture.filename.patterns", newNames);
   }

   public void addNamePattern(String newPattern) throws Exception
   {
      String patterns = getProperty("capture.filename.patterns");
      patterns += ";" + newPattern;

      this.setServerProperty("capture.filename.patterns", patterns);
   }

   public void moveNamePattern(int index, int amount) throws Exception
   {
      String patterns = getProperty("capture.filename.patterns");
      String[] line = patterns.split(";");

      int newIndex = index + amount;

      if(newIndex < 0 || newIndex > line.length-1) {
		return;
	  }

      String temp = line[newIndex];
      line[newIndex] = line[index];
      line[index] = temp;

      String newNames = "";
      for (String element : line) {
            newNames += element.trim() + ";";
      }
      newNames = newNames.substring(0, newNames.length()-1);

      this.setServerProperty("capture.filename.patterns", newNames);
   }

   public String getTargetURL(String id, String def)
   {
      String data = def;
      try
      {
         String httpDir = getProperty("path.httproot");
         String themeDir = getProperty("path.theme");
         String path = httpDir + File.separator + "themes" + File.separator + themeDir + File.separator + "xsl" + File.separator + "target.urls";

         Properties prop = new Properties();
         FileInputStream in = new FileInputStream(path);
         prop.load(in);
         in.close();
         data = prop.getProperty(id, def);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      return data;
   }

   public HashMap<String, String> getMimeTypes()
   {
      return mimeTypes;
   }

   private void loadMineTypes()
   {
      try
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         BufferedReader in = new BufferedReader(new FileReader(dataPath + File.separator + "prop" + File.separator + "mime-types.prop"));

         String line = in.readLine();

         while(line != null)
         {
            String[] bits = line.split("=");
            if(bits.length == 2)
            {
               //System.out.println(line);
               mimeTypes.put(bits[0], bits[1]);
            }

            line = in.readLine();
         }

      }
      catch(Exception e)
      {
         System.out.println("ERROR loading mime types!");
         e.printStackTrace();
      }
      System.out.println("Mime-Types loaded (" + mimeTypes.size() + ")");
   }
}

