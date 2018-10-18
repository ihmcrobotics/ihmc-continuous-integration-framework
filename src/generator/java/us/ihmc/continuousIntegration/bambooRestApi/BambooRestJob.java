package us.ihmc.continuousIntegration.bambooRestApi;

import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooPlan;

public class BambooRestJob
{
   private final String jobKey;
   private final boolean enabled;
   private final String name;
   
   public BambooRestJob(BambooPlan bambooPlan)
   {
      jobKey = bambooPlan.getKey();
      enabled = bambooPlan.isEnabled();
      name = bambooPlan.getShortName();
   }
   
   public String getKey()
   {
      return jobKey;
   }

   public boolean isEnabled()
   {
      return enabled;
   }

   public String getName()
   {
      return name;
   }
}
