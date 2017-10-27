package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BambooStages
{
   private String expand;
   @JsonProperty("start-index")
   private String startIndex;
   @JsonProperty("max-result")
   private String maxResult;
   private BambooStage[] stage;
   private String size;

   public String getExpand()
   {
      return expand;
   }

   public void setExpand(String expand)
   {
      this.expand = expand;
   }

   public BambooStage[] getStage()
   {
      return stage;
   }

   public void setStage(BambooStage[] stage)
   {
      this.stage = stage;
   }

   @Override
   public String toString()
   {
      String st = "expand " + expand;
      for (int i = 0; i < stage.length; i++)
      {
         st += "\n" + "stage " + stage[i];
      }
      return st;
   }

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
