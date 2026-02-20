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

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


public class EmailSender implements Runnable
{

   private String subject = "";
   private String body = "";

   private StringBuffer log = new StringBuffer(2048);
   private String newLine = "\r\n";

   private boolean finished = false;

   public EmailSender()
   {
   }

   public void setNewLineChar(String str)
   {
      newLine = str;
   }

   public String getLog()
   {
      return log.toString();
   }

   public void setSubject(String subject)
   {
      this.subject = subject;
   }

   private void logString(String str)
   {
      System.out.println(str);
      log.append(str + newLine);
   }

   public void setBody(String body)
   {
      this.body = body;
   }

   public boolean isFinished()
   {
      return finished;
   }

   @Override
public void run()
   {
      try
      {
         DataStore store = DataStore.getInstance();

         String to = store.getProperty("email.to");
         String from = store.getProperty("email.from");

         String server = store.getProperty("email.server.address");
         String port = store.getProperty("email.server.port");

         String auth =  store.getProperty("email.auth.enabled");
         String user =  store.getProperty("email.auth.user");
         String password =  store.getProperty("email.auth.password");

         String security =  store.getProperty("email.security");

         String edebug = store.getProperty("email.debug.enabled");

         logString("Email Thread Started (" + subject + ")");

         //////////////////////////////////////
         // check all inputs
         //////////////////////////////////////
         if(server == null || server.trim().length() == 0)
         {
            logString("Email sending failed, (server) is not set correctly!");
            return;
         }

         if(port == null || port.trim().length() == 0)
         {
            logString("Email sending failed, (port) is not set correctly!");
            return;
         }

         if(to == null || to.trim().length() == 0)
         {
            logString("Email sending failed, (to) is not set correctly!");
            return;
         }

         // extract all to addresses
         StringTokenizer tokenizer = new StringTokenizer(to, ",");
         Vector<InternetAddress> toAddresses = new Vector<InternetAddress>();
         while(tokenizer.hasMoreTokens())
         {
            String token = tokenizer.nextToken();

            if(token != null && token.trim().length() > 0)
            {
               token = token.trim();
               logString("TO: " + token);

               try
               {
                  toAddresses.add(new InternetAddress(token));
               }
               catch(Exception e)
               {
                  logString("Invalid To Address (" + token + ")");
                  return;
               }
            }
         }

         if(toAddresses.size() == 0)
         {
            logString("No Valid to addresses");
            return;
         }

         if(from == null || from.trim().length() == 0)
         {
            logString("Email sending failed, (from) is not set correctly!");
            return;
         }

         InternetAddress fromAddress = null;
         try
         {
            fromAddress = new InternetAddress(from);
         }
         catch(Exception e)
         {
            logString("Invalid From Address (" + from + ")");
            return;
         }

         // if using auth make sure we have user names and passwords
         if("1".equals(auth))
         {
            if(user == null || user.trim().length() == 0)
            {
               logString("Email sending failed, (user) is not set correctly!");
               return;
            }

            if(password == null || password.trim().length() == 0)
            {
               logString("Email sending failed, (password) is not set correctly!");
               return;
            }
         }

         ////////////////////
         // do actual sending
         ////////////////////

         // Session Properties
         Properties props = new Properties();

         // set connection timeout to 30 seconds and io timeout to 60 seconds
         props.put("mail.smtp.connectiontimeout", "30000");
         props.put("mail.smtp.timeout", "60000");

         // server settings
         logString("server address: " + server);
         logString("server port: " + port);
         props.put("mail.smtp.host", server);
         props.put("mail.smtp.port", port);

         // set auth details if needed
         if("1".equals(auth))
         {
            logString("Using Authentication");
            props.put("mail.smtp.user", user);
            props.put("mail.smtp.auth", "true");
         }

         Integer securityint = 0;
         try {
            securityint = Integer.valueOf(security);
         } catch (Exception e) {
            logString("Invalid Security integer input (" + security + ")");
            return;
         }
         // set starttls security if required, port usually 587
         if (securityint >= 10 && securityint < 20)
         {
            props.put("mail.smtp.starttls.enable","true");
            props.put("mail.smtp.socketFactory.port", port);
            //props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //Removed JC - No Longer Required
            props.put("mail.smtp.socketFactory.fallback", "false");
            if("10".equals(security))
            {
              logString("Using security type 10 (STARTTLS-TLSv1)");
              props.put("mail.smtp.ssl.protocols","TLSv1"); //Not recommended, very old
            }
             else if("11".equals(security))
             {
                logString("Using security type 11 (STARTTLS-TLSv1.1)");
                props.put("mail.smtp.ssl.protocols","TLSv1.1"); //Not recommended, very old
             }
             else if("12".equals(security))
             {
                logString("Using security type 12 (STARTTLS-TLSv1.2)");
                props.put("mail.smtp.ssl.protocols","TLSv1.2"); //Current Standard
             }
             else
             {
                logString("Using security type 13 (STARTTLS-TLSv1.3)");
                props.put("mail.smtp.ssl.protocols","TLSv1.3"); //Most Secure
             }

         }

         // set ssl security if required, port usually 465
         if (securityint >= 20 && securityint < 30)
         {
            logString("Using security type 2 (SSL)");
            props.put("mail.smtp.ssl.enable","true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.fallback", "false");
             if("22".equals(security))
             {
                logString("Using security type 22 (SSLv2Hello)");
                props.put("mail.smtp.ssl.protocols","SSLv2Hello"); //Not recommended, very old
             }
             else
             {
                logString("Using security type 23 (SSLv3)");
                props.put("mail.smtp.ssl.protocols","SSLv3"); //Most Secure
             }

         }


         Session session = null;

         if("1".equals(auth))
         {
            session = Session.getInstance(props, new SMTPAuthenticator(user, password));
         }
         else
         {
            session = Session.getInstance(props);
         }

         if("1".equals(edebug))
         {
            logString("SMTP Debug Enabled");
            props.put("mail.smtp.debug", "true");
            session.setDebug(true);
         }
         else
         {
            logString("SMTP Debug Disabled");
            props.put("mail.smtp.debug", "false");
            session.setDebug(false);
         }


         MimeMessage msg = new MimeMessage(session);
         msg.setText(body);
         msg.setSubject(subject + " (" + store.getComputerName() + ")");
         msg.setFrom(fromAddress);

         for(int x = 0; x < toAddresses.size(); x++)
         {
            msg.addRecipient(Message.RecipientType.TO, toAddresses.get(x));
         }

         // do actual sending
         logString("Starting mail transport");
         Transport.send(msg);
         logString("Email Sent");

      }
      catch(Exception e)
      {
         logString("ERROR Sending Email (" + e.getMessage() + ")");
         e.printStackTrace();
      }
      finally
      {
         logString("Email Thread Exiting (" + subject + ")");
         finished = true;
      }
   }

}