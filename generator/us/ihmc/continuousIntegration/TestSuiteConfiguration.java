package us.ihmc.continuousIntegration;

public class TestSuiteConfiguration
{
   public String hyphenatedName;
   public String pascalCasedName;
   public boolean disableBambooConfigurationCheck = false;
   public boolean disableBalancing = false;
   public String bambooUrl = "http://bamboo.ihmc.us/";
   public String[] bambooPlanKeys = new String[] {};
   public double maximumSuiteDuration = 5.5;
   public double recommendedTestClassDuration = 2.0;
   
   public boolean getDisableBambooConfigurationCheck()
   {
      return disableBambooConfigurationCheck;
   }

   public boolean getDisableBalancing()
   {
      return disableBalancing;
   }
   
   public String getBambooUrl()
   {
      return bambooUrl;
   }
   
   public String[] getBambooPlanKeys()
   {
      return bambooPlanKeys;
   }
   
   public double getMaximumSuiteDuration()
   {
      return maximumSuiteDuration;
   }
   
   public double getRecommendedTestClassDuration()
   {
      return recommendedTestClassDuration;
   }
   
   public String getHyphenatedName()
   {
      return hyphenatedName;
   }
   
   public String getPascalCasedName()
   {
      return pascalCasedName;
   }
}
