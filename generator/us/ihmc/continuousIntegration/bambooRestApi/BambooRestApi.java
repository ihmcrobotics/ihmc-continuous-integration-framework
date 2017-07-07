package us.ihmc.continuousIntegration.bambooRestApi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooJobResultRequest;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooResult;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooTestResult;
import us.ihmc.continuousIntegration.bambooRestApi.unirest.BambooUnirestConnector;
import us.ihmc.continuousIntegration.tools.LoginInfo;
import us.ihmc.continuousIntegration.tools.SecurityTools;

public class BambooRestApi
{
   // EXAMPLE URLS
   // https://bamboo.ihmc.us/rest/api/latest/plan/RC-TNS.json?expand=stages.stage.plans
   // https://bamboo.ihmc.us/rest/api/latest/plan/RC-FASTLOOP.xml?expand=stages.stage.plans
   // https://bamboo.ihmc.us/rest/api/latest/result/RC-FASTLOOP.xml?expand=results[0].result
   // https://bamboo.ihmc.us/rest/api/latest/result/RC-INDEV-ATLASIINDEVELOPMENT/319?expand=testResults.allTests.testResult
   public static final String API_PATH = "rest/api/latest/";
   public static final String XML = ".xml";
   public static final String JSON = ".json";
   public static final String PLAN = "plan/";
   public static final String RESULT = "result/";

   public static final String EXPAND = "expand";

   public static final String JOB_EXPANSION = "stages.stage.plans";
   public static final String RESULT_EXPANSION = "results[0].result";
   public static final String ALL_TESTS_EXPANSION = "testResults.allTests.testResult";
   
   public static final Path CREDENTIALS_PATH = Paths.get(System.getProperty("user.home"), ".ihmc", "bamboo-rest-login.properties");
   
   private final LoginInfo loginInfo;
   private final BambooUnirestConnector unirestConnector;
   
   public BambooRestApi(String baseUrl)
   {
      loginInfo = SecurityTools.loadLoginInfo(CREDENTIALS_PATH);
      unirestConnector = new BambooUnirestConnector(baseUrl, loginInfo);
   }
   
   public BambooRestApi(String baseUrl, LoginInfo loginInfo)
   {
      this.loginInfo = loginInfo;
      unirestConnector = new BambooUnirestConnector(baseUrl, loginInfo);
   }
   
   public void destroy()
   {
      unirestConnector.destroy();
   }
   
   public List<BambooTestResult> queryAllTestResultsFromJob(BambooRestJob bambooRestJob, int buildNumber)
   {      
      return unirestConnector.queryAllTestResultsFromJob(bambooRestJob, buildNumber);
   }
   
   public List<BambooRestJob> queryJobsInPlan(BambooRestPlan bambooRestPlan, boolean includeDisabledJobs)
   {
      return unirestConnector.queryJobsInPlan(bambooRestPlan, includeDisabledJobs);
   }
   
   public List<BambooRestJob> queryAllJobs(List<BambooRestPlan> planList)
   {
      return unirestConnector.queryAllJobs(planList);
   }

   public BambooResult queryLatestPlanResults(BambooRestPlan bambooRestPlan)
   {
      return unirestConnector.queryLastestPlanResult(bambooRestPlan);
   }
   
   public BambooJobResultRequest queryLatestJobResults(BambooRestJob bambooRestJob)
   {
      return unirestConnector.queryLastestJobResult(bambooRestJob);
   }
}
