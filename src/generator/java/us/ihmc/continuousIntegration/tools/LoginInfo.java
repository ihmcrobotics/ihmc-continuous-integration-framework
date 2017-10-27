package us.ihmc.continuousIntegration.tools;

public class LoginInfo
{
   private final String username;
   private final String password;

   public LoginInfo(String username, String password)
   {
      this.username = username;
      this.password = password;
   }

   public String getPassword()
   {
      return password;
   }

   public String getUsername()
   {
      return username;
   }
}