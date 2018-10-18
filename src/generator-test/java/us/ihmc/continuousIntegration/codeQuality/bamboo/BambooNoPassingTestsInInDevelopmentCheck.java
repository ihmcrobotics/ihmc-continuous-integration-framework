package us.ihmc.continuousIntegration.codeQuality.bamboo;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import us.ihmc.log.LogTools;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooResult;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooTestResult;
import us.ihmc.continuousIntegration.tools.SecurityTools;

public class BambooNoPassingTestsInInDevelopmentCheck
{
   private static final int MAX_SUCCESSFUL_TESTS_IN_DEVELOPMENT = 30;

   private static final String bambooBaseUrl = "http://bamboo.ihmc.us/";
   private static BambooRestApi bambooRestApi = new BambooRestApi(bambooBaseUrl);

   private BambooRestPlan inDevelopmentPlan = new BambooRestPlan("ROB-INDEVELOPMENT");

   public static void main(String[] args)
   {
      new BambooNoPassingTestsInInDevelopmentCheck().testNoPassingTestsInInDevelopment();
   }

   public void testNoPassingTestsInInDevelopment()
   {
      if (System.getProperty("bamboo.username") != null)
         SecurityTools.storeLoginInfo("BambooRestConnector", System.getProperty("bamboo.username"), System.getProperty("bamboo.password"));

      List<BambooRestJob> inDevJobs = bambooRestApi.queryJobsInPlan(inDevelopmentPlan, false);

      if (inDevJobs.isEmpty())
         return;

      BambooResult latestInDevelopmentResult = bambooRestApi.queryLatestPlanResults(inDevelopmentPlan);

      Map<BambooRestJob, List<BambooTestResult>> jobPlanToResultMap = new LinkedHashMap<>();
      List<BambooTestResult> successfulTests = new ArrayList<>();

      for (BambooRestJob job : inDevJobs)
      {
         jobPlanToResultMap.put(job, bambooRestApi.queryAllTestResultsFromJob(job, latestInDevelopmentResult.getBuildNumber()));

         for (BambooTestResult testResult : jobPlanToResultMap.get(job))
         {
            if (testResult.getStatus().equals("successful"))
            {
               LogTools.info(job.getKey() + ": (" + testResult.getStatus() + ") " + testResult.getClassName() + ":" + testResult.getMethodName());

               successfulTests.add(testResult);
            }
         }
      }

      String[] successfulTestNames = new String[successfulTests.size()];

      for (int i = 0; i < successfulTests.size(); i++)
      {
         successfulTestNames[i] = successfulTests.get(i).getClassName() + "." + successfulTests.get(i).getMethodName();
      }

      assertTrue(successfulTests.size() + "/" + MAX_SUCCESSFUL_TESTS_IN_DEVELOPMENT
            + " successful tests exist in InDevelopment! Move them into Fast or Slow to keep them passing.\n" + Arrays.toString(successfulTestNames),
                 successfulTests.size() < MAX_SUCCESSFUL_TESTS_IN_DEVELOPMENT);

      bambooRestApi.destroy();
   }
}
