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
import java.util.HashMap;
import java.util.Vector;

public class CaptureDeviceList
{
   private static CaptureDeviceList instance = null;
   private Vector<CaptureDevice> captureDevices = new Vector<>();
   private HashMap<String, StreamProducerProcess> producers = new HashMap<>();

   private CaptureDeviceList()
   {
      loadDeviceList(null);
   }

   @Override
public String toString()
   {
      return "Currently have " + captureDevices.size() + " devices in available device list";
   }

   public static CaptureDeviceList getInstance()
   {
      synchronized (DataStore.class)
      {
         if (instance == null)
         {
            instance = new CaptureDeviceList();
            return instance;
         }
         else
         {
            return instance;
         }
      }
   }

   @SuppressWarnings("unchecked")
   public void importDeviceList(byte[] deviceData) throws Exception
   {
      ByteArrayInputStream deviceBytes = new ByteArrayInputStream(deviceData);
      ObjectInputStream ois = new ObjectInputStream(deviceBytes);

      captureDevices = (Vector<CaptureDevice>) ois.readObject();
      ois.close();
      System.out.println("Capture Devices imported");

      this.saveDeviceList(null);
   }

   @SuppressWarnings("unchecked")
   private void loadDeviceList(byte[] deviceData)
   {
      try
      {
         ObjectInputStream ois = null;
         if(deviceData == null)
         {
            String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
            FileInputStream fis = new FileInputStream(dataPath + File.separator + "sof" + File.separator + "CaptureDevices.sof");
            ois = new ObjectInputStream(fis);
         }
         else
         {
            ByteArrayInputStream deviceBytes = new ByteArrayInputStream(deviceData);
            ois = new ObjectInputStream(deviceBytes);
         }

         captureDevices = (Vector<CaptureDevice>) ois.readObject();
         ois.close();
         System.out.println("CaptureDevices.sof found and loaded");
      }
      catch (Exception e)
      {
         captureDevices = new Vector<>();
         //e.printStackTrace();
         System.out.println("ERROR loading CaptureDevices.sof, starting with no cards selected.");
      }
   }

   public void saveDeviceList(ByteArrayOutputStream deviceBytes)
   {
      try
      {
         ObjectOutputStream oos = null;

         if(deviceBytes == null)
         {
            String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
            FileOutputStream fos = new FileOutputStream(dataPath + File.separator + "sof" + File.separator + "CaptureDevices.sof");
            oos = new ObjectOutputStream(fos);
         }
         else
         {
            oos = new ObjectOutputStream(deviceBytes);
         }

         oos.writeObject(captureDevices);
         oos.close();
         System.out.println("CaptureDevices.sof saved.");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void addDevice(CaptureDevice dev)
   {
      captureDevices.add(dev);
   }

   public void addDeviceAt(int index, CaptureDevice dev)
   {
      captureDevices.add(index, dev);
   }

   public CaptureDevice remDevice(int index)
   {
      return captureDevices.remove(index);
   }

   public int getDeviceCount()
   {
      return captureDevices.size();
   }

   public CaptureDevice getDevice(int x)
   {
      return captureDevices.get(x);
   }

   public int getActiveDeviceCount()
   {
      int active = 0;
      for (CaptureDevice cap : captureDevices) {
         if(cap.isInUse())
         {
            active++;
         }
      }
      return active;
   }

   public int getFreeDevice()
   {
      for(int x = 0; x < captureDevices.size(); x++)
      {
         CaptureDevice cap = captureDevices.get(x);
         if(!cap.isInUse())
         {
            return x;
         }
      }
      return -1;
   }

   public StreamProducerProcess getProducer(int freq, int band)
   {
      return producers.get(freq + "-" + band);
   }

   public void addProducer(StreamProducerProcess producer, int freq, int band)
   {
      producer.setKey(freq + "-" + band);
      producers.put(freq + "-" + band, producer);
   }

   public void remProducer(String key)
   {
      producers.remove(key);
   }

   public StreamProducerProcess[] getProducers()
   {
      return producers.values().toArray(new StreamProducerProcess[0]);
   }

}