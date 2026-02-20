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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

public class CaptureDetailsEpNFO
{
   private DataStore store = null;

   public CaptureDetailsEpNFO()
   {

   }

   public void writeCaptureDetailsEpNFO(CaptureTask task)
   {
      try
      {
         store = DataStore.getInstance();
         String pathString = store.getProperty("capture_epnfo.path.details");

         if(pathString.trim().equalsIgnoreCase("none")) {
			return;
		 }

         File outputPath = null;

         String capFileName = task.getCurrentFileName();
         File capFile = new File(capFileName);

         if(pathString.trim().equalsIgnoreCase("same"))
         {
            outputPath = new File(getFileNameOnly(task.getCurrentFileName()) + ".nfo");
         }
         else
         {
            outputPath = new File(getFileNameOnly(pathString + File.separator + capFile.getName()) + ".nfo");
         }

         if(!outputPath.getParentFile().exists())
         {
            System.out.println("ERROR details path does not exist: " + outputPath.toString());
            return;
         }

         System.out.println("Writing Capture Details episode nfo info to: " + outputPath.toString());

         String xmlData = "";
         ScheduleItem schItem = task.getScheduleItem();

         xmlData = getXMLData(capFile, schItem);

         int count = 1;
         String originalPath = getFileNameOnly(outputPath.getCanonicalPath());
         while (outputPath.exists())
         {
            String newPath = originalPath + "-" + (count++) + ".nfo";
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
         System.out.println("ERROR writing Capture Details Episode NFO file.");
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


  private String getXMLData(File capFile, ScheduleItem schItem)
  {
    GuideStore guide = GuideStore.getInstance();

    ByteArrayOutputStream buff = new ByteArrayOutputStream();

    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      DOMImplementation di = db.getDOMImplementation();
      Document doc = di.createDocument("", "episodedetails", null);

      Element root = doc.getDocumentElement();
      Element elm = null;
      Text text = null;

      //Task Items  - None Required

      ScheduleItem item = schItem;
      GuideItem guideItem = item.getCreatedFrom();

      // Get Epoch Value as Epoch Seconds of Recording Start for Sorting & id
      Instant itemInstant = item.getStart().toInstant();
      Long episodeStartES = itemInstant.getEpochSecond();
      String episodeStart = episodeStartES.toString();

      if(guideItem == null) {

          // Use Schedule Items only if EPG data unavailable
          elm = doc.createElement("title");
          text = doc.createTextNode(item.getName());
          elm.appendChild(text);
          root.appendChild(elm);

          // Sort Titles
          elm = doc.createElement("season");
          text = doc.createTextNode("0");
          elm.appendChild(text);
          root.appendChild(elm);

          //display episode as epoch seconds for sorting
          elm = doc.createElement("displayepisode");
          text = doc.createTextNode(episodeStart);
          elm.appendChild(text);
          root.appendChild(elm);

          // Item Date Format to add to plot & premiered
          ZonedDateTime itemZDT = itemInstant.atZone( ZoneId.systemDefault() );
          DateTimeFormatter itemformatterS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          DateTimeFormatter itemformatterL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
          String itemStartS = itemZDT.format( itemformatterS );
          String itemStartL = itemZDT.format( itemformatterL );

          // Description/Plot
          elm = doc.createElement("plot");
          text = doc.createTextNode(item.getChannel() + ": " + itemStartL );
          elm.appendChild(text);
          root.appendChild(elm);

          // recording runtime
          elm = doc.createElement("runtime");
          text = doc.createTextNode(Integer.valueOf(item.getDuration()).toString());
          elm.appendChild(text);
          root.appendChild(elm);

          // times
          elm = doc.createElement("premiered");
          text = doc.createTextNode(itemStartS);
          elm.appendChild(text);
          root.appendChild(elm);
          elm = doc.createElement("aired");
          text = doc.createTextNode(itemStartS);
          elm.appendChild(text);
          root.appendChild(elm);

          // channel
          elm = doc.createElement("studio");
          text = doc.createTextNode(item.getChannel());
          elm.appendChild(text);
          root.appendChild(elm);

          // id
          elm = doc.createElement("id");
          text = doc.createTextNode(episodeStart);
          elm.appendChild(text);
          root.appendChild(elm);

      } else {

          // Guide Items - Use these if EPG Data is available (except channel)

          // Title
          elm = doc.createElement("title");
          text = doc.createTextNode(guideItem.getName());
          elm.appendChild(text);
          root.appendChild(elm);

          // Sort Titles
          elm = doc.createElement("season");
          text = doc.createTextNode("0");
          elm.appendChild(text);
          root.appendChild(elm);

          elm = doc.createElement("displayepisode");
          text = doc.createTextNode(episodeStart);
          elm.appendChild(text);
          root.appendChild(elm);

          // EPG Date Formats
          Instant epgInstant = guideItem.getStart().toInstant();
          ZonedDateTime epgZDT = epgInstant.atZone( ZoneId.systemDefault() );
          DateTimeFormatter epgformatterS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          DateTimeFormatter epgformatterL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
          String guideStartS = epgZDT.format( epgformatterS );
          String guideStartL = epgZDT.format( epgformatterL );


          String epgChan = guide.getEpgChannelFromMap(item.getChannel());
          String wsChan =  item.getChannel();
          String epgSubtitle = guideItem.getSubName();
          String epgRating = guideItem.getRatings();
          String Plot = wsChan + ": " + guideStartL + "\r\n";

          // Add info to plot as other fields don't scrape in Kodi
          if (epgSubtitle.length() > 0)
          {
            if (epgRating.length() > 0)
            {
              Plot = Plot + epgSubtitle + ": " + epgRating + "\r\n\r\n";
            } else {
              Plot = Plot + epgSubtitle + "\r\n\r\n";
            }

          } else {

            if (epgRating.length() > 0)
            {
              Plot = Plot + epgRating + "\r\n\r\n";
            }

          }

          // Description/Plot
          elm = doc.createElement("plot");
          text = doc.createTextNode(Plot + guideItem.getDescription());
          elm.appendChild(text);
          root.appendChild(elm);

          // epg subtitle
          elm = doc.createElement("tagline");
          text = doc.createTextNode(epgSubtitle);
          elm.appendChild(text);
          root.appendChild(elm);

          // epg runtime
          elm = doc.createElement("runtime");
          text = doc.createTextNode(Integer.valueOf(guideItem.getDuration()).toString());
          elm.appendChild(text);
          root.appendChild(elm);

          String artPathString = store.getProperty("capture_art.path.details");

          if(artPathString.trim().equalsIgnoreCase("none"))
          {
            //No art required.
          } else {
            String artFile = "";
            if(artPathString.trim().equalsIgnoreCase("same"))
            {
              // Same Folder as Capture - Requires an image for every capture, use media filename.
              artFile = getFileNameOnly(capFile.getName()) + ".jpg";
            } else {
              // User Specified Common path - Recommended, use epg title.
              artFile = artPathString + File.separator + item.getName() + ".jpg";
            }
            // artwork
            elm = doc.createElement("thumb");
            // Remove spaces and "-" from special characters
            artFile = artFile.replaceAll("(\\s+|-)","");
            text = doc.createTextNode(artFile);
            elm.appendChild(text);
            root.appendChild(elm);
          }

          // ratings
          elm = doc.createElement("mpaa");
          text = doc.createTextNode(epgRating);
          elm.appendChild(text);
          root.appendChild(elm);

          // times
          elm = doc.createElement("premiered");
          text = doc.createTextNode(guideStartS);
          elm.appendChild(text);
          root.appendChild(elm);
          elm = doc.createElement("aired");
          text = doc.createTextNode(guideStartS);
          elm.appendChild(text);
          root.appendChild(elm);

          // channel
          elm = doc.createElement("studio");
          text = doc.createTextNode(wsChan);
          elm.appendChild(text);
          root.appendChild(elm);

          //epg channel
          //elm = doc.createElement("studio");
          //text = doc.createTextNode(epgChan);
          //elm.appendChild(text);
          //root.appendChild(elm);

          // id
          elm = doc.createElement("id");
          text = doc.createTextNode(episodeStart);
          elm.appendChild(text);
          root.appendChild(elm);

          // vectors

          for (String epgActs : guideItem.getActors()) {
             Element actors = doc.createElement("actor");
             root.appendChild(actors);
             elm = doc.createElement("name");
             text = doc.createTextNode(epgActs);
             elm.appendChild(text);
             actors.appendChild(elm);
          }

          for (String epgCat : guideItem.getCategory()) {
             elm = doc.createElement("genre");
             text = doc.createTextNode(epgCat);
             elm.appendChild(text);
             root.appendChild(elm);
          }

          for (String epgDirs : guideItem.getDirectors()) {
             elm = doc.createElement("director");
             text = doc.createTextNode(epgDirs);
             elm.appendChild(text);
             root.appendChild(elm);
          }

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
