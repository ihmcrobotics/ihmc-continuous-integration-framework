package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooAllTests
{
   private BambooTestResult[] testResult;

   public BambooTestResult[] getTestResult()
   {
      return testResult;
   }

   public void setTestResult(BambooTestResult[] testResult)
   {
      this.testResult = testResult;
   }
}
