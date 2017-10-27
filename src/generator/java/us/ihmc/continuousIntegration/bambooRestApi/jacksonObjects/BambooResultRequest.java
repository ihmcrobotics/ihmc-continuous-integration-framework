package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooResultRequest
{
   private BambooResults results;
   private BambooLink link;
   private String expand;

   public BambooResults getResults()
   {
      return results;
   }

   public void setResults(BambooResults results)
   {
      this.results = results;
   }

   public BambooLink getLink()
   {
      return link;
   }

   public void setLink(BambooLink link)
   {
      this.link = link;
   }

   public String getExpand()
   {
      return expand;
   }

   public void setExpand(String expand)
   {
      this.expand = expand;
   }
}
