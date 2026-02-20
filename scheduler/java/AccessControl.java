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
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

public class AccessControl
{

   private Vector<UserDetails> users = new Vector<>();
   private Vector<String> trusted = new Vector<>();
   private Vector<AccessRule> ruleSet = new Vector<>();
   private long userDataLastMod = -1;
   private DataStore store = null;

   private String simpleAuth = "";
   private String simpleUser = "";
   private String simplePass = "";

   private static AccessControl instance = null;

   private AccessControl()
   {
      store = DataStore.getInstance();
      loadUsers();
   }

   @Override
public String toString()
   {
      String data = "Access Control Data (Rules=" + ruleSet.size() + ", Users=" + users.size() + ", Trusted=" + trusted.size() + ")";

      for (String element : trusted) {
         data += "\r\nTrusted=" + element;
      }

      for (UserDetails user : users) {
         data += "\r\nUser=" + user.toString();
      }

      for (AccessRule element : ruleSet) {
         data += "\r\nRule=" + element.toString();
      }

      return data;
   }

   public static AccessControl getInstance()
   {
      synchronized (AccessControl.class)
      {
         if (instance == null)
         {
            instance = new AccessControl();
            return instance;
         }
         else
         {
            return instance;
         }
      }
   }

   private synchronized void loadUsers()
   {
      boolean simpleAuthLoad = true;
      boolean fileAuthLoad = true;

      try
      {
         String simpAuth = store.getProperty("security.authentication");
         String simpUser = store.getProperty("security.username");
         String simpPass = store.getProperty("security.password");
         if(simpAuth.equals(simpleAuth) && simpUser.equals(simpleUser) && simpPass.equals(simplePass))
         {
            simpleAuthLoad = false;
         }
         else
         {
            simpleAuthLoad = true;
            simpleAuth = simpAuth;
            simpleUser = simpUser;
            simplePass = simpPass;
         }

         String dataPath = System.getenv("Programdata") + File.separator + "TV Scheduler Pro";
         File userData = new File(dataPath + File.separator + "prop" + File.separator + "authentication.prop");
         long lastMod = userData.lastModified();
         if(lastMod == userDataLastMod)
         {
            //System.out.println("authentication.prop not changed");
            fileAuthLoad = false;
         }
         else
         {
            userDataLastMod = lastMod;
            fileAuthLoad = true;
         }

         if(!simpleAuthLoad && !fileAuthLoad)
         {
            // no need to do anything
            return;
         }

         // reset access data
         users = new Vector<>();
         trusted = new Vector<>();
         ruleSet = new Vector<>();

         // if needed set our simple auth
         if("1".equals(simpAuth))
         {
            // this will add one rule and one user to the system

            ruleSet.add(new AccessRule("*:*:*"));
            users.add(new UserDetails("users:" + simpUser + ":" + simpPass));
         }

         // load new user access data from file
         FileReader fr = new FileReader(userData);
         BufferedReader br = new BufferedReader(fr);

         String line = br.readLine();
         while (line != null)
         {
            line = line.trim();

            if(line.startsWith("user="))
            {
               line = line.substring("user=".length());
               users.add(new UserDetails(line));
            }
            else if(line.startsWith("trusted="))
            {
               trusted.add(line.substring("trusted=".length(), line.length()));
            }
            else if(line.startsWith("rule="))
            {
               line = line.substring("rule=".length());
               ruleSet.add(new AccessRule(line));
            }

            line = br.readLine();
         }

         br.close();
         fr.close();

         System.out.println(this.toString());
      }
      catch (Exception e)
      {
         users = new Vector<>();
         trusted = new Vector<>();
         ruleSet = new Vector<>();

         System.out.println("ERROR Loading authentication.prop data!");
         e.printStackTrace();
      }
   }

   public boolean authenticateUser(HashMap<String, String> headers, HTTPurl requestData)
   {
      // load user data if it has changed
      loadUsers();

      // first check the trusted IP list, this is to cover task editing regardless of any rules set.
      // no auth is required of any of these IP's
      if(trusted.size() > 0)
      {
         String remote = headers.get("RemoteAddress");
         for (String element : trusted) {
            if(remote != null && remote.startsWith(element))
            {
               //System.out.println("Access allowed from trusted IP : " + remote);
               headers.put("authenticated", "ip_trusted");
               return true;
            }
         }
      }

      // check all our rules, see if one matches
      String requiredGroup = checkAccessRules(requestData);


      // now check the "Authorization" header, this uses the basic auth header from the request
      String auth = headers.get("Authorization");

      // no Authorisation header so just return no access, if auth required
      if ((auth == null || auth.length() == 0) && requiredGroup != null)
      {
         return false;
      }

      UserDetails user = verifyUserAuth(auth, requiredGroup);

      // no matching user authentication found and auth required so return no access
      if(user == null && requiredGroup != null)
      {
         return false;
      }

      // if there is no match with any rule the group is null and no auth is required
      // we have processed the auth options for tasks, so for all other items return accepted.
      if(requiredGroup == null)
      {
         return true;
      }

      // auth was required and we have a user.
      headers.put("authenticated", "user");
      return true;

   }

   private String checkAccessRules(HTTPurl requestData)
   {
      String requestURL = requestData.getReqString();
      //System.out.println(requestURL);

      for (AccessRule rule : ruleSet) {
         if(rule.isMatch(requestURL, requestData))
         {
            //System.out.println("Rule matches (" + rule.toString() + ")");
            return rule.getRuleGroup();
         }
      }

      return null;
   }

   private UserDetails verifyUserAuth(String auth, String requiredGroup)
   {
      String justAuth = "";
      try
      {
         if(!auth.startsWith("Basic ")) {
			return null;
		 }

         justAuth = auth.substring("Basic ".length(), auth.length());
         String decAuth = new String(Base64.decodeBase64(justAuth));

         //System.out.println(auth + "\n" + justAuth + "\n" + decAuth);

         // iterate through users and look for one that matches
         // if one is found return
         for (UserDetails user : users) {
            boolean groupMatch = "*".equals(requiredGroup) || user.getGroup().equals(requiredGroup);

            if(user.isMatch(decAuth) && groupMatch) {
				return user;
			}
         }

         System.out.println("User group/name/password NOT found required group(" + requiredGroup + ") entered auth(" + decAuth + ")");
      }
      catch (Exception e)
      {
         System.out.println("ERROR decoding username and password (" + justAuth + ")");
         e.printStackTrace();
      }

      return null;
   }





}