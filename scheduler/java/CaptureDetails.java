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
import java.time.Instant;

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

public class CaptureDetails
{
   private DataStore store = null;

   public CaptureDetails()
   {

   }

   public void writeCaptureDetails(CaptureTask task)
   {
      try
      {
         store = DataStore.getInstance();
         String pathString = store.getProperty("capture.path.details");

         if(pathString.trim().equalsIgnoreCase("none")) {
			return;
		 }

         File outputPath = null;

         String capFileName = task.getCurrentFileName();
         File capFile = new File(capFileName);

         if(pathString.trim().equalsIgnoreCase("same"))
         {
            outputPath = new File(task.getCurrentFileName() + ".xml");
         }
         else
         {
            outputPath = new File(pathString + File.separator + capFile.getName() + ".xml");
         }

         if(!outputPath.getParentFile().exists())
         {
            System.out.println("ERROR details path does not exist: " + outputPath.toString());
            return;
         }

         System.out.println("Writing Capture Details info to: " + outputPath.toString());

         String xmlData = "";
         ScheduleItem schItem = task.getScheduleItem();

         xmlData = getXMLData(capFile, task.getDeviceIndex(), schItem);

         int count = 1;
         String originalPath = getFileNameOnly(outputPath.getCanonicalPath());
         while (outputPath.exists())
         {
            String newPath = originalPath + "-" + (count++) + ".xml";
            System.out.println("Details file already exists, creating new name : " + newPath);
            outputPath = new File(newPath);
         }


         // add to capture file list
         task.getScheduleItem().addCaptureFile(outputPath);

         FileWriter writer = new FileWriter(outputPath);
         writer.write(xmlData);
         writer.close();


      }
      catch (Exception e)
      {
         System.out.println("ERROR writing Capture Details XML file.");
         e.printStackTrace();
      }
   }

  private String getFileNameOnly(String FilePath)
  {
    int pos = FilePath.lastIndexOf(".");
    if (pos > 0) {
      return FilePath.substring(0, pos);
    } else {
      return FilePath;
    }
  }


   private String getXMLData(File capFile, int deviceIndex, ScheduleItem schItem)
   {
      GuideStore guide = GuideStore.getInstance();

      ByteArrayOutputStream buff = new ByteArrayOutputStream();

      try
      {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         DOMImplementation di = db.getDOMImplementation();
         Document doc = di.createDocument("", "capture", null);

         Element root = doc.getDocumentElement();
         Element elm = null;
         Text text = null;

         //Task Items
         elm = doc.createElement("ws_cardID");
         text = doc.createTextNode(Integer.valueOf(deviceIndex).toString());
         elm.appendChild(text);
         root.appendChild(elm);

         elm = doc.createElement("ws_filename");
         text = doc.createTextNode(capFile.getName());
         elm.appendChild(text);
         root.appendChild(elm);

         elm = doc.createElement("ws_fullfilename");
         text = doc.createTextNode(capFile.getCanonicalPath());
         elm.appendChild(text);
         root.appendChild(elm);

         ScheduleItem item = schItem;

         // Schedule Items
         elm = doc.createElement("ws_name");
         text = doc.createTextNode(item.getName());
         elm.appendChild(text);
         root.appendChild(elm);

         elm = doc.createElement("ws_start");
         text = doc.createTextNode(item.getStart().toString());
         elm.appendChild(text);
         root.appendChild(elm);

         elm = doc.createElement("ws_stop");
         text = doc.createTextNode(item.getStop().toString());
         elm.appendChild(text);
         root.appendChild(elm);

         elm = doc.createElement("ws_duration");
         text = doc.createTextNode(Integer.valueOf(item.getDuration()).toString());
         elm.appendChild(text);
         root.appendChild(elm);

         elm = doc.createElement("ws_channel");
         text = doc.createTextNode(item.getChannel());
         elm.appendChild(text);
         root.appendChild(elm);

         // Get Epoch Value as Epoch Seconds of Recording Start for Sorting
         Instant itemInstant = item.getStart().toInstant();
         Long episodeStartES = itemInstant.getEpochSecond();
         String episodeStart = episodeStartES.toString();

         //item start as epoch seconds
         elm = doc.createElement("episodestart_epoch");
         text = doc.createTextNode(episodeStart);
         elm.appendChild(text);
         root.appendChild(elm);

         GuideItem guideItem = item.getCreatedFrom();

         if(guideItem != null)
         {
            Element epgItem = doc.createElement("epg_item");
            root.appendChild(epgItem);

            String epgChan = guide.getEpgChannelFromMap(item.getChannel());

            // Guide Items

            // main items
            elm = doc.createElement("epg_title");
            text = doc.createTextNode(guideItem.getName());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_subtitle");
            text = doc.createTextNode(guideItem.getSubName());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_description");
            text = doc.createTextNode(guideItem.getDescription());
            elm.appendChild(text);
            epgItem.appendChild(elm);
            // channel
            elm = doc.createElement("epg_channel");
            text = doc.createTextNode(epgChan);
            elm.appendChild(text);
            epgItem.appendChild(elm);

            // times
            elm = doc.createElement("epg_start");
            text = doc.createTextNode(guideItem.getStart().toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_stop");
            text = doc.createTextNode(guideItem.getStop().toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            // derived
            elm = doc.createElement("epg_duration");
            text = doc.createTextNode(Integer.valueOf(guideItem.getDuration())
                  .toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            // strings
            elm = doc.createElement("epg_language");
            text = doc.createTextNode(guideItem.getLanguage());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_ratings");
            text = doc.createTextNode(guideItem.getRatings());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_url");
            text = doc.createTextNode(guideItem.getURL());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            // vectors
            for (String epgActs : guideItem.getActors()) {
               elm = doc.createElement("epg_actors");
               text = doc.createTextNode(epgActs);
               elm.appendChild(text);
               epgItem.appendChild(elm);
            }

            for (String epgCat : guideItem.getCategory()) {
               elm = doc.createElement("epg_category");
               text = doc.createTextNode(epgCat);
               elm.appendChild(text);
               epgItem.appendChild(elm);
            }

            for (String epgDirs : guideItem.getDirectors()) {
               elm = doc.createElement("epg_directors");
               text = doc.createTextNode(epgDirs);
               elm.appendChild(text);
               epgItem.appendChild(elm);
            }

            // special flag
            elm = doc.createElement("epg_ignored");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getIgnored()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            // flags

            elm = doc.createElement("epg_ac3");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getAC3()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_blackwhite");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getBlackWhite()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_captions");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getCaptions()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_highdef");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getHighDef()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_interactive");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getInteractive()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_lastchance");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getLastChance()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_live");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getLive()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_premiere");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getPremiere()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_repeat");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getRepeat()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_surround");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getSurround()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

            elm = doc.createElement("epg_widescreen");
            text = doc.createTextNode(Boolean.valueOf(guideItem.getWidescreen()).toString());
            elm.appendChild(text);
            epgItem.appendChild(elm);

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
