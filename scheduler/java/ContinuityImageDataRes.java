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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

class ContinuityImageDataRes extends HTTPResponse
{
   public ContinuityImageDataRes() throws Exception
   {
      super();
   }

   @Override
public void getResponse(HTTPurl urlData, OutputStream outStream) throws Exception
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

      byte[] bytes = getImageData(urlData);

      out.write(bytes);

      return out.toByteArray();
   }

   private byte[] getImageData(HTTPurl urlData) throws Exception
   {
      GuideStore guide = GuideStore.getInstance();

      int width = 600;
      int height = 20;

      BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// variables used for drawing hands of clock
		Graphics g;
		g = img.getGraphics();

      g.setColor( new Color( 188, 190, 252) );
      g.fillRect(0, 0, width, height);

      guide.detectDateRange();

      Date date = new Date();

      //Calvi changes to avoid exceptions on getTime calls
      long startData = date.getTime();
      long endData = date.getTime();

      if (guide.getMinEntry() !=null) {
		startData = guide.getMinEntry().getTime();
	  }
      if (guide.getMaxEntry() !=null) {
		endData = guide.getMaxEntry().getTime();
	  }

      GuideItem[] items = guide.getProgramsForChannel(urlData.getParameter("channel"));

      long totalLength = endData - startData;//items[items.length-1].getStart().getTime() - items[0].getStart().getTime();
      long startTime = startData;//items[0].getStart().getTime();

      //System.out.println(urlData.getParameter("channel"));

      for (int y = 0; y < items.length - 1; y++)
      {
         GuideItem item1 = items[y];
         GuideItem item2 = items[y + 1];

         long diff = item2.getStart().getTime() - item1.getStop().getTime();

         if(diff > 0)
         {
            long x1 = item1.getStop().getTime() - startTime;
            long x2 = diff;

            x1 = (long)(((double)x1 / (double)totalLength) * width);
            x2 = (long)(((double)x2 / (double)totalLength) * width);

            if(x2 < 2) {
				x2 = 2;
			}

            //g.setColor( new Color( 255, 128, 192) );
            g.setColor( new Color( 255, 255, 0) );
            g.fillRect((int)x1, 0, (int)(x2), height);
            System.out.println("DIFF : " + diff + " x1=" + x1 + " x2=" + x2);
         }

         if(diff < 0)
         {
            long x1 = item1.getStop().getTime() - startTime;
            long x2 = diff;

            x1 = (long)(((double)x1 / (double)totalLength) * width);
            x2 = (long)(((double)x2 / (double)totalLength) * width);

            x2 = x2 * -1;
            if(x2 < 2) {
				x2 = 2;
			}

            x1 = x1 - x2;

            g.setColor( new Color( 255, 0, 0) );
            g.fillRect((int)x1, 0, (int)(x2), height);

            System.out.println("DIFF : " + diff + " x1=" + x1 + " x2=" + x2);
         }

      }

      long startX = items[0].getStart().getTime() - startTime;
      startX = (long)(((double)startX / (double)totalLength) * width);
      g.setColor( new Color( 255, 255, 255) );
      if(startX < 0) {
		startX = 0;
	  }
      g.fillRect((int)startX, 0, 2, height);

      long stopX = items[items.length-1].getStop().getTime() - startTime;
      stopX = (long)(((double)stopX / (double)totalLength) * width);
      g.setColor( new Color( 255, 255, 255) );
      if(stopX > width-2) {
		stopX = width-2;
	  }
      g.fillRect((int)stopX, 0, 2, height);

      PngEncoder png =  new PngEncoder(img);
      return png.pngEncode();
   }


}
