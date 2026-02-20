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

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

class ImageDataRes extends HTTPResponse
{
   public ImageDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream)
         throws Exception
   {
      outStream.write(getImage(urlData));
   }

   private byte[] getImage(HTTPurl urlData) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048);

      out.write("HTTP/1.0 200 OK\n".getBytes());
      out.write("Content-Type: image/png\n".getBytes());
      out.write("Pragma: no-cache\n".getBytes());
      out.write("Cache-Control: no-cache\n\n".getBytes());

      byte[] bytes = null;

      if ("1".equalsIgnoreCase(urlData.getParameter("sessionID")))
      {
         bytes = getSessionImageData();
      }
      else if ("01".equals(urlData.getParameter("action")))
      {
         bytes = getButtonImage(urlData);
      } else {
		bytes = getImageData();
	  }

      out.write(bytes);

      return out.toByteArray();
   }

   private byte[] getButtonImage(HTTPurl urlData) throws Exception
   {
      String text = urlData.getParameter("text");
      boolean blank = "blank".equals(text);

      int background = 0;
      int buttonBG = 0;
      int buttonText = 0;
      int buttonOutline = 0;
      int fontSize = 11;
      int fontType = 0;
      int fits = 0;

      int width = 10;
      int height = 10;
      try
      {
         width = Integer.parseInt(urlData.getParameter("width"));
         height = Integer.parseInt(urlData.getParameter("height"));
      }
      catch (Exception e)
      {
      }
      try
      {
         background = Integer.parseInt(urlData.getParameter("bg"));
      }
      catch (Exception e)
      {
      }
      try
      {
         buttonBG = Integer.parseInt(urlData.getParameter("bb"));
      }
      catch (Exception e)
      {
      }
      try
      {
         buttonText = Integer.parseInt(urlData.getParameter("bt"));
      }
      catch (Exception e)
      {
      }
      try
      {
         buttonOutline = Integer.parseInt(urlData.getParameter("bo"));
      }
      catch (Exception e)
      {
      }
      try
      {
         fontSize = Integer.parseInt(urlData.getParameter("fs"));
      }
      catch (Exception e)
      {
      }
      try
      {
         fontType = Integer.parseInt(urlData.getParameter("ft"));
      }
      catch (Exception e)
      {
      }
      try
      {
         fits = Integer.parseInt(urlData.getParameter("fits"));
      }
      catch (Exception e)
      {
      }

      BufferedImage img = new BufferedImage(width, height,
            BufferedImage.TYPE_4BYTE_ABGR);

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

      g.setColor(new Color(background));
      g.fillRect(0, 0, width, height);

      if (!blank)
      {
         Font myFont = new Font("Arial", fontType, fontSize);// "Helvetica"
         FontMetrics fm;

         g2d.setFont(myFont);
         fm = g2d.getFontMetrics();

         g2d.setColor(new Color(buttonBG));
         g2d.fillRect(2, 2, width - 3, height - 3);

         g.setFont(myFont);
         int x = width / 2 - fm.stringWidth(text) / 2;
         if (x < 3) {
			x = 3;
		 }
         g2d.setColor(new Color(buttonText));
         g2d.drawString(text, x, (height / 2) + (fm.getAscent() / 2) - 1);

         g2d.setColor(new Color(buttonOutline));
         g2d.drawRect(1, 1, width - 2, height - 2);

         g2d.setRenderingHints(new RenderingHints(null));
         if (fits == 1)
         {
            g2d.setColor(new Color(0xffff00));
            g2d.drawLine(1, 1, 1, height - 2);
            g2d.drawLine(2, 1, 2, height - 2);
            g2d.drawLine(width - 2, 1, width - 2, height - 2);
            g2d.drawLine(width - 3, 1, width - 3, height - 2);
         }
         if (fits == 2)
         {
            g2d.setColor(new Color(0xffff00));
            g2d.drawLine(1, 1, 1, height - 2);
            g2d.drawLine(2, 1, 2, height - 2);
         }
         if (fits == 3)
         {
            g2d.setColor(new Color(0xffff00));
            g2d.drawLine(width - 2, 1, width - 2, height - 2);
            g2d.drawLine(width - 3, 1, width - 3, height - 2);
         }
      }

      PngEncoder png = new PngEncoder(img);
      return png.pngEncode();
   }

   private byte[] getSessionImageData() throws Exception
   {
      int width = 75;
      int height = 20;

      BufferedImage img = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_ARGB);

      Graphics g;
      Font myFont = new Font("System", Font.PLAIN, 16);
      FontMetrics fm;

      g = img.getGraphics();

      g.setColor(new Color(255, 255, 255));
      g.fillRect(0, 0, width, height);

      g.setColor(new Color(255, 0, 0));
      g.drawRect(0, 0, width - 1, height - 1);

      g.setFont(myFont);
      fm = g.getFontMetrics();

      g.setColor(new Color(0, 0, 0));

      DataStore store = DataStore.getInstance();
      String sessionID = store.createSessionID();

      g.drawString(sessionID, width / 2 - fm.stringWidth(sessionID) / 2, height
            / 2 + fm.getAscent() / 2 - 2);

      PngEncoder png = new PngEncoder(img);
      return png.pngEncode();

   }

   private byte[] getImageData() throws Exception
   {
      Button holder = new Button();
      Image imgTemplate = Toolkit.getDefaultToolkit().createImage("test.png");
      MediaTracker tracker = new MediaTracker(holder);
      tracker.addImage(imgTemplate, 1);
      tracker.waitForAll();

      int width = imgTemplate.getWidth(holder);
      int height = imgTemplate.getHeight(holder);

      BufferedImage img = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_ARGB);

      // variables used for drawing hands of clock
      Graphics g;
      Font myFont = new Font("Helvetica", Font.PLAIN, 16);
      FontMetrics fm;

      g = img.getGraphics();
      g.setFont(myFont);
      fm = g.getFontMetrics();

      g.drawImage(imgTemplate, 0, 0, holder);

      g.setColor(new Color(255, 0, 0));

      g.setFont(myFont);
      g.drawString("Some Test", width / 2 - fm.stringWidth("Some Test") / 2,
            height / 2 - fm.getAscent() / 2);

      // img = addAlphaToImage(img, 150);

      PngEncoder png = new PngEncoder(img);
      return png.pngEncode();
   }

   public Image addAlphaToImage(Image img, int newAlpha)
   {
      int width = 100;
      int height = 100;

      int[] pixels = new int[width * height];

      PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0,
            width);

      try
      {
         pg.grabPixels();
      }
      catch (InterruptedException e)
      {
         System.out.println("interrupted waiting for pixels!");
         return img;
      }

      if ((pg.getStatus() & ImageObserver.ABORT) != 0)
      {
         System.out.println("image fetch aborted or errored");
         return img;
      }

      for (int i = 0; i < (width * height); i++)
      {
         int alpha = (pixels[i] >> 24) & 0xff;
         int red = (pixels[i] >> 16) & 0xff;
         int green = (pixels[i] >> 8) & 0xff;
         int blue = (pixels[i]) & 0xff;

         alpha = alpha - newAlpha;
         if (alpha > 255) {
			alpha = 255;
		 } else if (alpha < 0) {
			alpha = 0;
		 }

         alpha = (alpha << 24) & 0xFF000000;
         red = (red << 16) & 0xff0000;
         green = (green << 8) & 0xff00;
         blue = (blue) & 0xff;

         pixels[i] = (alpha | red | green | blue);
      }

      return Toolkit.getDefaultToolkit().createImage(
            new MemoryImageSource(width, height, pixels, 0, width));
   }

}
