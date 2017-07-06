package us.ihmc.continuousIntegration;

public class TestSuiteConfiguration
{
   public boolean disableBambooConfigurationCheck = false;
   public boolean disableBalancing = false;
   public String[] bambooPlansToCheck = {};
   public double targetSuiteDuration = 5.0;

   public boolean getDisableBambooConfigurationCheck()
   {
      return disableBambooConfigurationCheck;
   }

   public boolean getDisableBalancing()
   {
      return disableBalancing;
   }
   
   public String[] getBambooPlansToCheck()
   {
      return bambooPlansToCheck;
   }
   
   public double getTargetSuiteDuration()
   {
      return targetSuiteDuration;
   }
}
