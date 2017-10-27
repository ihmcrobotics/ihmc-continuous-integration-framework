package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BambooActions
{
   @JsonProperty("start-index")
   private String startIndex;
   @JsonProperty("max-result")
   private String maxResult;
   private String size;

   public String getSize()
   {
      return size;
   }

   public void setSize(String size)
   {
      this.size = size;
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
}
