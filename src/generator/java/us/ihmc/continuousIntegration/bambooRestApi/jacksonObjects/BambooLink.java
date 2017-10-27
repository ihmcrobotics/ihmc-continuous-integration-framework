package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooLink
{
   private String rel;
   private String href;

   public String getRel()
   {
      return rel;
   }

   public void setRel(String rel)
   {
      this.rel = rel;
   }

   public String getHref()
   {
      return href;
   }

   public void setHref(String href)
   {
      this.href = href;
   }

   @Override
   public String toString()
   {
      String st = "rel " + rel;
      st += "\n" + "href " + href;
      return st;
   }
}
