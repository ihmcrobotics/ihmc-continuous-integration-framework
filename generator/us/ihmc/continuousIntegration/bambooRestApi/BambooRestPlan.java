package us.ihmc.continuousIntegration.bambooRestApi;

public class BambooRestPlan
{
   private final String planKey;
   
   public BambooRestPlan(String planKey)
   {
      this.planKey = planKey;
   }
   
   public String getKey()
   {
      return planKey;
   }
   
   @Override
   public String toString()
   {
      return planKey;
   }
}
