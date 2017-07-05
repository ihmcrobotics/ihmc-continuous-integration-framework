package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooPlans
{
   private BambooPlan[] plan;
   private String expand;
   private String size;

   public String getExpand()
   {
      return expand;
   }

   public void setExpand(String expand)
   {
      this.expand = expand;
   }

   public BambooPlan[] getPlan()
   {
      return plan;
   }

   public void setPlan(BambooPlan[] plan)
   {
      this.plan = plan;
   }

   public String getSize()
   {
      return size;
   }

   public void setSize(String size)
   {
      this.size = size;
   }
}
