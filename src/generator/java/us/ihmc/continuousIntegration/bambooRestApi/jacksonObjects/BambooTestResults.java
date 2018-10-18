package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooTestResults
{
   private BambooAllTests allTests;

   public BambooAllTests getAllTests()
   {
      return allTests;
   }

   public void setAllTests(BambooAllTests allTests)
   {
      this.allTests = allTests;
   }
}
