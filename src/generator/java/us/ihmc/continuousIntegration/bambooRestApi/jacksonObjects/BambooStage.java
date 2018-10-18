package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooStage
{
   private String manual;
   private BambooPlans plans;
   private String description;
   private String name;
   private String expand;

   public String getManual()
   {
      return manual;
   }

   public void setManual(String manual)
   {
      this.manual = manual;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getExpand()
   {
      return expand;
   }

   public void setExpand(String expand)
   {
      this.expand = expand;
   }

   @Override
   public String toString()
   {
      String st = "name " + name;
      st += "\n" + "expand " + expand.toString();
      st += "\n" + "plans " + getPlans().toString();
      st += "\n" + "description " + description;
      st += "\n" + "manual " + manual;
      return st;
   }

   public BambooPlans getPlans()
   {
      return plans;
   }

   public void setPlans(BambooPlans plans)
   {
      this.plans = plans;
   }
}
