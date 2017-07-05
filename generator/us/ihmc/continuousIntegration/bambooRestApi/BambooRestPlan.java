package us.ihmc.continuousIntegration.bambooRestApi;

public enum BambooRestPlan
{
   FAST("ROB-FAST"),
   SLOW("ROB-SLOW"),
   UI("ROB-UI"),
   //CODE_QUALITY("ROB-CODEQUALITY"),
   VIDEO("ROB-VIDEO"),
   FLAKY("ROB-FLAKY"),
   IN_DEVELOPMENT("ROB-INDEVELOPMENT"),
   //REPOSITORY_HEALTH("ROB-REPOSITORYHEALTH"),
   ;
   
   public static final BambooRestPlan[] values = values();
   
   private final String planKey;
   
   private BambooRestPlan(String planKey)
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
