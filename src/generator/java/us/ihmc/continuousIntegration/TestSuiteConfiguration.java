package us.ihmc.continuousIntegration;

import groovy.util.Eval;
import us.ihmc.log.LogTools;

import java.util.ArrayList;
import java.util.Map;

public class TestSuiteConfiguration
{
   public String hyphenatedName;
   public String pascalCasedName;

   public boolean disableJobCheck = true;
   public boolean crashOnEmptyJobs = false;
   public boolean crashOnMissingTimeouts = true;
   public boolean disableSuiteBalancing = false;
   public double maxSuiteDuration = 5.5;
   public String bambooUrl = "https://bamboo.ihmc.us/";
   public ArrayList<String> bambooPlanKeys = new ArrayList<>();

   // Load defaults
   public TestSuiteConfiguration()
   {
      // do nothing
   }

   public TestSuiteConfiguration(Map<String, ?> properties)
   {
      if (propertyIsSet("disableJobCheck", properties))
      {
         disableJobCheck = ((String) properties.get("disableJobCheck")).trim().toLowerCase().contains("true");
         LogTools.info("Set disableJobCheck to " + disableJobCheck + " from property: " + properties.get("disableJobCheck"));
      }
      if (propertyIsSet("crashOnEmptyJobs", properties))
      {
         crashOnEmptyJobs = ((String) properties.get("crashOnEmptyJobs")).trim().toLowerCase().contains("true");
         LogTools.info("Set crashOnEmptyJobs to " + crashOnEmptyJobs + " from property: " + properties.get("crashOnEmptyJobs"));
      }
      if (propertyIsSet("crashOnMissingTimeouts", properties))
      {
         crashOnMissingTimeouts = ((String) properties.get("crashOnMissingTimeouts")).trim().toLowerCase().contains("true");
         LogTools.info("Set crashOnMissingTimeouts to " + crashOnMissingTimeouts + " from property: " + properties.get("crashOnMissingTimeouts"));
      }
      if (propertyIsSet("disableSuiteBalancing", properties))
      {
         disableSuiteBalancing = ((String) properties.get("disableSuiteBalancing")).trim().toLowerCase().contains("true");
         LogTools.info("Set disableSuiteBalancing to " + disableSuiteBalancing + " from property: " + properties.get("disableSuiteBalancing"));
      }
      if (propertyIsSet("maxSuiteDuration", properties))
      {
         maxSuiteDuration = Double.valueOf(((String) properties.get("maxSuiteDuration")).trim());
         LogTools.info("Set maxSuiteDuration to " + maxSuiteDuration + " from property: " + properties.get("maxSuiteDuration"));
      }
      if (propertyIsSet("bambooUrl", properties))
      {
         bambooUrl = ((String) properties.get("bambooUrl")).trim();
         LogTools.info("Set bambooUrl to " + bambooUrl + " from property: " + properties.get("bambooUrl"));
      }
      if (propertyIsSet("bambooPlanKeys", properties))
      {
         bambooPlanKeys = (ArrayList<String>) Eval.me(((String) properties.get("bambooPlanKeys")).trim());
         LogTools.info("Set bambooPlanKeys to " + bambooPlanKeys + " from property: " + properties.get("bambooPlanKeys"));
      }
   }

   private boolean propertyIsSet(String name, Map<String, ?> properties)
   {
      return properties.containsKey(name) && properties.get(name) != null && properties.get(name) instanceof String;
   }

   public boolean getDisableJobCheck()
   {
      return disableJobCheck;
   }

   public boolean getCrashOnEmptyJobs()
   {
      return crashOnEmptyJobs;
   }

   public boolean getCrashOnMissingTimeouts()
   {
      return crashOnMissingTimeouts;
   }

   public boolean getDisableSuiteBalancing()
   {
      return disableSuiteBalancing;
   }

   public String getBambooUrl()
   {
      return bambooUrl;
   }

   public ArrayList<String> getBambooPlanKeys()
   {
      return bambooPlanKeys;
   }

   public double getMaxSuiteDuration()
   {
      return maxSuiteDuration;
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
