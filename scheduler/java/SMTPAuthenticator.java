import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator
{
   String user = "";
   String pass = "";

   public SMTPAuthenticator(String usr, String pass)
   {
      this.user = usr;
      this.pass = pass;
   }

   public PasswordAuthentication getPasswordAuthentication()
   {
      return new PasswordAuthentication(user, pass);
   }
}