package us.ihmc.continuousIntegration;

public class ContinuousIntegrationTools
{
   public static boolean isRunningOnContinuousIntegrationServer()
   {
      String runningOnContinuousIntegrationServer = System.getProperty("runningOnCIServer");

      if (runningOnContinuousIntegrationServer == null || !runningOnContinuousIntegrationServer.equals("true"))
      {
         return false;
      }
      else
      {
         return true;
      }
   }
}
