package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;

public class BambooJobResultRequest
{
   private BambooTestResults testResults;
   private String buildDuration;
   private BambooLink link;
   private String expand;
   
   public String getFormattedBuildDuration()
   {
      return String.valueOf(MathTools.roundToSignificantFigures(getBuildDurationMinutes(), 2));
   }
   
   public double getBuildDurationMinutes()
   {
      return Conversions.millisecondsToMinutes(Integer.valueOf(buildDuration));
   }
   
   public BambooTestResults getTestResults()
   {
      return testResults;
   }
   public void setTestResults(BambooTestResults testResults)
   {
      this.testResults = testResults;
   }
   public String getBuildDuration()
   {
      return buildDuration;
   }
   public void setBuildDuration(String buildDuration)
   {
      this.buildDuration = buildDuration;
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
