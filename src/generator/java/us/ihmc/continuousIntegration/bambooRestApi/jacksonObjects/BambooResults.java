package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooResults
{
   private String expand;
   private String startIndex;
   private String maxResult;
   private String size;
   private BambooResult[] result;

   public String getExpand()
   {
      return expand;
   }

   public void setExpand(String expand)
   {
      this.expand = expand;
   }

   public String getStartIndex()
   {
      return startIndex;
   }

   public void setStartIndex(String startIndex)
   {
      this.startIndex = startIndex;
   }

   public String getMaxResult()
   {
      return maxResult;
   }

   public void setMaxResult(String maxResult)
   {
      this.maxResult = maxResult;
   }

   public String getSize()
   {
      return size;
   }

   public void setSize(String size)
   {
      this.size = size;
   }

   public BambooResult[] getResult()
   {
      return result;
   }

   public void setResult(BambooResult[] result)
   {
      this.result = result;
   }
}
