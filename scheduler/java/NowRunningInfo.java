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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class NowRunningInfo
{
   HashMap<String, CaptureTask> tasks = null;

   public NowRunningInfo(HashMap<String, CaptureTask> data)
   {
      tasks = data;
   }

   public void writeNowRunning()
   {
      try
      {
         String xmlData = getXMLData();
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         File runningCapList = new File(dataPath + File.separator + "xml" + File.separator + "nowRunning.xml");
         FileWriter writer = new FileWriter(runningCapList);
         writer.write(xmlData);
         writer.close();
      }
      catch (Exception e)
      {
         System.out.println("ERROR Thrown When Writing nowRunning.xml : " + e.toString());
      }
   }

   private String getXMLData()
   {
      ByteArrayOutputStream buff = new ByteArrayOutputStream();

      try
      {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         DOMImplementation di = db.getDOMImplementation();
         Document doc = di.createDocument("", "running_captures", null);

         Element root = doc.getDocumentElement();
         Element elm = null;
         Element elm2 = null;
         Text text = null;

         String[] keys = tasks.keySet().toArray(new String[0]);

         for (String key : keys) {
            CaptureTask task = tasks.get(key);
            ScheduleItem item = task.getScheduleItem();

            elm = doc.createElement("capture");
            elm.setAttribute("cardID", Integer.valueOf(task.getDeviceIndex()).toString());

            elm2 = doc.createElement("name");
            text = doc.createTextNode(item.getName());
            elm2.appendChild(text);
            elm.appendChild(elm2);

            elm2 = doc.createElement("start");
            text = doc.createTextNode(item.getStart().toString());
            elm2.appendChild(text);
            elm.appendChild(elm2);

            elm2 = doc.createElement("stop");
            text = doc.createTextNode(item.getStop().toString());
            elm2.appendChild(text);
            elm.appendChild(elm2);

            elm2 = doc.createElement("duration");
            text = doc.createTextNode(Integer.valueOf(item.getDuration()).toString());
            elm2.appendChild(text);
            elm.appendChild(elm2);

            elm2 = doc.createElement("channel");
            text = doc.createTextNode(item.getChannel());
            elm2.appendChild(text);
            elm.appendChild(elm2);

            String fileName = task.getCurrentFileName();
            elm2 = doc.createElement("filename");
            text = doc.createTextNode(fileName);
            elm2.appendChild(text);
            elm.appendChild(elm2);

            //append capture to root element
            root.appendChild(elm);
         }

         // Transform Document
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer xformer = factory.newTransformer();

         Source source = new DOMSource(doc);
         Result result = new StreamResult(buff);
         xformer.transform(source, result);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return buff.toString();
   }

}