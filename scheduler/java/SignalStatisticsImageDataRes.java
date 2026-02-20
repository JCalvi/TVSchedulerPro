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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

class SignalStatisticsImageDataRes extends HTTPResponse
{
   public SignalStatisticsImageDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream) throws Exception
   {
      if("01".equals(urlData.getParameter("action")))
      {
         outStream.write(showPage(urlData));
         return;
      }

      outStream.write(getImage(urlData));
   }

   private byte[] showPage(HTTPurl urlData) throws Exception
   {
      PageTemplate template = new PageTemplate(store.getProperty("path.httproot")
        + File.separator + "templates" + File.separator + "StatisticsGraph.html");

      String id = urlData.getParameter("id");
      if(id == null) {
		id = "";
	  }
      String data = urlData.getParameter("data");
      if(data == null) {
		data = "";
	  }
      String file = urlData.getParameter("file");
      if(file == null) {
		file = "";
	  }

      int maxNorm = 0;
      String normMax = urlData.getParameter("normMax");
      if(normMax == null || normMax.length() == 0) {
		maxNorm = 0;
	  } else
      {
         try
         {
            maxNorm = Integer.parseInt(normMax);
         }
         catch(Exception e){}
      }

      int minNorm = 0;
      String normMin = urlData.getParameter("normMin");
      if(normMin == null || normMin.length() == 0) {
		minNorm = 0;
	  } else
      {
         try
         {
            minNorm = Integer.parseInt(normMin);
         }
         catch(Exception e){}
      }

      String backLink = "";

      if(id.length() == 0) {
		backLink = "/servlet/ArchiveDataRes?action=showItemInfo&file=" + URLEncoder.encode(file, "UTF-8");
	  } else {
		backLink = "/servlet/ScheduleDataRes?action=07&id=" + URLEncoder.encode(id, "UTF-8");
	  }

      template.replaceAll("$backLink", backLink);

      template.replaceAll("$itemID", URLEncoder.encode(id, "UTF-8"));
      template.replaceAll("$dataType", URLEncoder.encode(data, "UTF-8"));
      template.replaceAll("$Encoded_FileName", URLEncoder.encode(file, "UTF-8"));
      template.replaceAll("$fileName", file);

      template.replaceAll("$normMax", Integer.valueOf(maxNorm).toString());
      template.replaceAll("$normMin", Integer.valueOf(minNorm).toString());

      if("strength".equalsIgnoreCase(data)) {
		template.replaceAll("$title", "Signal Strength Statistics");
	  } else if("quality".equalsIgnoreCase(data)) {
		template.replaceAll("$title", "Signal Quality Statistics");
	  } else {
		template.replaceAll("$title", "Data Type Not Available");
	  }

      return template.getPageBytes();
   }

   private byte[] getImage(HTTPurl urlData) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048);

      out.write("HTTP/1.0 200 OK\n".getBytes());
      out.write("Content-Type: image/png\n".getBytes());
      out.write("Pragma: no-cache\n".getBytes());
      out.write("Cache-Control: no-cache\n\n".getBytes());

      byte[] bytes = getImageData(urlData);

      out.write(bytes);

      return out.toByteArray();
   }

   private byte[] getImageData(HTTPurl urlData) throws Exception
   {
      ScheduleItem item = null;
      String id = urlData.getParameter("id");
      if(id == null || id.length() == 0)
      {
         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         File archivePath = new File(dataPath + File.separator + "archive" + File.separator + urlData.getParameter("file"));
         FileInputStream fis = new FileInputStream(archivePath);
         ObjectInputStream ois = new ObjectInputStream(fis);
         item = (ScheduleItem) ois.readObject();
         ois.close();
      }
      else
      {
         item = store.getScheduleItem(id);
      }

      NumberFormat nf2Dec = NumberFormat.getInstance();
      nf2Dec.setMaximumFractionDigits(2);
      NumberFormat nf0Dec = NumberFormat.getInstance();
      nf0Dec.setMaximumFractionDigits(0);

      int maxNorm = 0;
      String normMax = urlData.getParameter("normMax");
      if(normMax == null || normMax.length() == 0) {
		maxNorm = 0;
	  } else
      {
         try
         {
            maxNorm = Integer.parseInt(normMax);
         }
         catch(Exception e){}
      }

      int minNorm = 0;
      String normMin = urlData.getParameter("normMin");
      if(normMin == null || normMin.length() == 0) {
		minNorm = 0;
	  } else
      {
         try
         {
            minNorm = Integer.parseInt(normMin);
         }
         catch(Exception e){}
      }


      String data = urlData.getParameter("data");

      int width = 800;
      int height = 500;

      BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

      // variables used for drawing hands of clock
      Graphics g;
      g = img.getGraphics();

      Graphics2D g2d = (Graphics2D) g;
      RenderingHints hints = new RenderingHints(null);
      hints.put(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
      hints.put(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
      hints.put(RenderingHints.KEY_DITHERING,
            RenderingHints.VALUE_DITHER_ENABLE);
      hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      hints.put(RenderingHints.KEY_COLOR_RENDERING,
            RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      g2d.setRenderingHints(hints);
      //g2d.setRenderingHints(new RenderingHints(null));

      //g2d.setColor( new Color( 188, 190, 252) );
      //g2d.fillRect(0, 0, width, height);

      Font myFont = new Font("Arial", Font.PLAIN, 16);
      g2d.setFont(myFont);

      Color myLine01 = new Color(255, 255, 255);
      Color myLine02 = new Color(100, 100, 100);
      Color myTransparent = new Color(255, 255, 255, 0);

      FontMetrics fm = g2d.getFontMetrics();

      if(item != null)
      {
         HashMap<Date, SignalStatistic> stats = item.getSignalStatistics();
         Date[] keys = stats.keySet().toArray(new Date[0]);
         Arrays.sort(keys);

         //System.out.println("Number of values:" + keys.length);

         double val = -1;
         double MIN = -1;
         double AVG = 0;
         double MAX = -1;

         for (Date key : keys) {
            SignalStatistic value = stats.get(key);

            if("strength".equalsIgnoreCase(data)) {
				val = value.getStrength();
			} else if("quality".equalsIgnoreCase(data)) {
				val = value.getQuality();
			} else {
				val = 0;
			}

            val = applyNorm(val, minNorm, maxNorm);

            if(MIN == -1 || val < MIN) {
				MIN = val;
			}

            if(MAX == -1 || val > MAX) {
				MAX = val;
			}

            AVG += val;
         }

         if(keys.length > 0)
         {
            AVG /= keys.length;
         }

         //System.out.println("AVG=" + AVG);

         if(keys.length > 0)
         {
            int leftInset = 75;

            double max_Y = -1;
            if(max_Y < MAX) {
				max_Y = MAX;
			}

            max_Y = max_Y + (max_Y * 0.05);
            //System.out.println("max_Y=" + max_Y);

            double min_Y = max_Y;
            if(min_Y > MIN && MIN != -1) {
				min_Y = MIN;
			}

            min_Y = min_Y - ((int)(min_Y * 0.05));
            //System.out.println("min_Y=" + min_Y);

            g2d.setColor(myLine02);

            //g.drawLine(leftInset, height-30, width-10, height-30);
            //g.drawLine(leftInset, 10, leftInset, height-30);

            double yMultiplyer = (height - 30) / (max_Y - min_Y);
            //System.out.println("mult = " + yMultiplyer);

            int y = (int)(height - 30 - (yMultiplyer * (MAX - min_Y)));
            g.drawLine(leftInset, y, width-10, y);
            g2d.setColor(myLine01);
            g2d.drawString(nf2Dec.format(MAX), 10, y + (fm.getAscent() / 2) - 1);
            g2d.setColor(myLine02);
            //System.out.println("MAX=" + MAX + " y=" + y);

            y = (int)(height - 30 - (yMultiplyer * (MIN - min_Y)));
            g.drawLine(leftInset, y, width-10, y);
            g2d.setColor(myLine01);
            g2d.drawString(nf2Dec.format(MIN), 10, y + (fm.getAscent() / 2) - 1);
            g2d.setColor(myLine02);
            //System.out.println("MIN=" + MIN + "y=" + y);

            y = (int)(height - 30 - (yMultiplyer * (AVG - min_Y)));
            g.drawLine(leftInset, y, width-10, y);
            g2d.setColor(myLine01);
            g2d.drawString(nf2Dec.format(AVG), 10, y + (fm.getAscent() / 2) - 1);
            g2d.setColor(myLine02);
            //System.out.println("AVG=" + MIN + "y=" + y);

            int prevY = 0;
            int prevX = leftInset;
            double xMultiplyer = (double)(width - 30) / (double)(keys.length);

            for(int x = 0; x < keys.length; x++)
            {
               SignalStatistic value = stats.get(keys[x]);
               if("strength".equalsIgnoreCase(data)) {
				val = value.getStrength();
			   } else if("quality".equalsIgnoreCase(data)) {
				val = value.getQuality();
			   } else {
				val = 0;
			   }

               val = applyNorm(val, minNorm, maxNorm);

               y = (int)(height - 30 - (yMultiplyer * (val - min_Y)));
               //System.out.println("VAL=" + val + "y=" + y);
               int x_offset = ((int)((x * xMultiplyer))) + leftInset;

               if(x == 0)
               {
                  g2d.setColor(myTransparent);
                  g.drawLine(prevX, prevY, x_offset, y);
                  //g.drawLine(x_offset, height-28, x_offset, height-32);
                  g2d.setColor(myLine01);
               }
               else
               {
                  g.drawLine(prevX, prevY, x_offset, y);
                  //g.drawLine(x_offset, height-28, x_offset, height-32);
               }

               prevY = y;
               prevX = x_offset;

            }

         }
         else
         {
            String message = "No Statistic Data";
            int x = width / 2 - fm.stringWidth(message) / 2;
            int y = (height / 2) + (fm.getAscent() / 2) - 1;
            g2d.drawString(message, x, y);
         }
      }
      else
      {
         String message = "Schedule Item Not Found";
         int x = width / 2 - fm.stringWidth(message) / 2;
         int y = (height / 2) + (fm.getAscent() / 2) - 1;
         g2d.drawString(message, x, y);
      }

      PngEncoder png =  new PngEncoder(img);
      return png.pngEncode();
   }

   private double applyNorm(double val, int min, int max)
   {
      if(min == 0 && max == 0) {
		return val;
	  }

      int shift = 0;
      int spread = 0;

      if(max < min)
      {
         shift = max * -1;
         spread = min + shift;
         val = val + shift;
         val = spread - val;
      }
      else
      {
         shift = min * -1;
         spread = max + shift;
         val = val + shift;
      }

      //System.out.println("Shift  = " + shift);
      //System.out.println("Spread = " + spread);
      //System.out.println("Val    = " + val);

      double x = (val / (spread)) * 100;
      //System.out.println(x);

      return x;
   }

}
