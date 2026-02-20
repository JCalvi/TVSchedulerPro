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

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;

class HTTPAuthenticator extends Authenticator
{
   @Override
protected PasswordAuthentication getPasswordAuthentication()
   {

      // Get information about the request
      String promptString = getRequestingPrompt();
      String hostname = getRequestingHost();
      InetAddress ipaddr = getRequestingSite();
      String ipAddressString = "";

      // get the type of request
      Authenticator.RequestorType requestType = getRequestorType();
      String typeText = "null";
      if(requestType == Authenticator.RequestorType.PROXY) {
		typeText = "PROXY";
	  } else if(requestType == Authenticator.RequestorType.SERVER) {
		typeText = "SERVER";
	  }

      if(ipaddr != null) {
		ipAddressString = ipaddr.toString();
	  }

      System.out.println("HTTPAuthenticator : " + promptString + " : " + hostname + " : " + ipAddressString + " : " + typeText);

      String username = "";
      String password = "";

      // now do the password auth based on details obtained
      try
      {
         DataStore store = DataStore.getInstance();

         if(requestType == Authenticator.RequestorType.SERVER)
         {
            username = store.getProperty("guide.source.http.usr");
            password = store.getProperty("guide.source.http.pwd");
         }
         else if(requestType == Authenticator.RequestorType.PROXY)
         {
            username = store.getProperty("proxy.server.usr");
            password = store.getProperty("proxy.server.pwd");
         }
         else
         {
            System.out.println("Request type not recognised");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      if(username.length() == 0 && password.length() == 0)
      {
         System.out.println("No Username or Password set");
         return null;
      }

      System.out.println("Using Username:" + username + " password:" + password);
      // Return the information
      return new PasswordAuthentication(username, password.toCharArray());
   }

}