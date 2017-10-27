package us.ihmc.continuousIntegration.codeQuality.bamboo;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooResult;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooTestResult;
import us.ihmc.continuousIntegration.tools.SecurityTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.HEALTH)
public class BambooNoPassingTestsInInDevelopmentTest
{
   private static final int MAX_SUCCESSFUL_TESTS_IN_DEVELOPMENT = 30;

   private static BambooRestApi bambooRestApi;
   private static final String bambooBaseUrl = "http://bamboo.ihmc.us/"; 
   
   private BambooRestPlan inDevelopmentPlan = new BambooRestPlan("ROB-INDEVELOPMENT");

   @BeforeClass
   public static void setUp()
   {
      bambooRestApi = new BambooRestApi(bambooBaseUrl);
   }

   @AfterClass
   public static void tearDown()
   {
      bambooRestApi.destroy();
   }

   @ContinuousIntegrationTest(estimatedDuration = 30.2)
   @Test(timeout = 150000)
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
               PrintTools.info(job.getKey() + ": (" + testResult.getStatus() + ") " + testResult.getClassName() + ":" + testResult.getMethodName());

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
   }
}
