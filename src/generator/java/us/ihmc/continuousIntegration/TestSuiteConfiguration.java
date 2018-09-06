package us.ihmc.continuousIntegration;

public class TestSuiteConfiguration
{
   public String hyphenatedName;
   public String pascalCasedName;
   public boolean disableBambooConfigurationCheck = false;
   public boolean crashOnEmptyJobs = false;
   public boolean disableJUnitTimeoutCheck = false;
   public boolean disableBalancing = false;
   public String bambooUrl = "https://bamboo.ihmc.us/";
   public String[] bambooPlanKeys = new String[] {};
   public double maximumSuiteDuration = 5.5;
   public double recommendedTestClassDuration = 2.0;
   
   public boolean getDisableBambooConfigurationCheck()
   {
      return disableBambooConfigurationCheck;
   }

   public boolean getCrashOnEmptyJobs()
   {
      return crashOnEmptyJobs;
   }

   public boolean getDisableJUnitTimeoutCheck()
   {
      return disableJUnitTimeoutCheck;
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

   /**
    * @deprecated Use convertJobNameToKebabCasedName instead.
    */
   public String convertJobNameToHyphenatedName(String jobName)
   {
      return convertJobNameToKebabCasedName(jobName);
   }

   /**
    * Used for artifact-test-runner to keep easy Bamboo configuration.
    * Job names are pascal cased on Bamboo and use this method to
    * resolve their kebab cased artifact counterparts.
    */
   public String convertJobNameToKebabCasedName(String jobName)
   {
      return AgileTestingTools.pascalCasedToHyphenatedWithoutJob(jobName);
   }
}
