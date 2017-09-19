package us.ihmc.continuousIntegration.bambooRestApi.unirest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;

import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooJobResultRequest;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooPlan;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooPlanRequest;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooResult;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooResultRequest;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooStage;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooTestResult;
import us.ihmc.continuousIntegration.tools.LoginInfo;

public class BambooUnirestConnector
{
   public static final boolean DEBUG = true;
   private final LoginInfo loginInfo;
   private final String baseUrl;

   public BambooUnirestConnector(String baseUrl, LoginInfo loginInfo)
   {
      this.baseUrl = baseUrl;
      this.loginInfo = loginInfo;

      Unirest.setObjectMapper(new ObjectMapper()
      {
         private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
         {
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         }

         @Override
         public <T> T readValue(String value, Class<T> valueType)
         {
            try
            {
               return jacksonObjectMapper.readValue(value, valueType);
            }
            catch (IOException e)
            {
               throw new RuntimeException(e);
            }
         }

         @Override
         public String writeValue(Object value)
         {
            try
            {
               return jacksonObjectMapper.writeValueAsString(value);
            }
            catch (JsonProcessingException e)
            {
               throw new RuntimeException(e);
            }
         }
      });
   }

   public BambooPlanRequest queryPlanRequest(BambooRestPlan bambooRestPlan)
   {
      try
      {
         String url = baseUrl + BambooRestApi.API_PATH + BambooRestApi.PLAN + bambooRestPlan + BambooRestApi.JSON;
         PrintTools.debug(DEBUG, "Querying: " + url + "?" + BambooRestApi.EXPAND + "=" + BambooRestApi.JOB_EXPANSION);

         HttpRequest httpRequest = requestPost(url).queryString(BambooRestApi.EXPAND, BambooRestApi.JOB_EXPANSION);
         HttpResponse<BambooPlanRequest> bambooPlanRequestResponse = httpRequest.asObject(BambooPlanRequest.class);
         BambooPlanRequest bambooPlanRequest = bambooPlanRequestResponse.getBody();
         return bambooPlanRequest;
      }
      catch (UnirestException e)
      {
         handleUnirestExceptions(e);
         return null;
      }
   }

   public BambooResult queryLastestPlanResult(BambooRestPlan bambooRestPlan)
   {
      try
      {
         String url = baseUrl + BambooRestApi.API_PATH + BambooRestApi.RESULT + bambooRestPlan.getKey() + BambooRestApi.JSON;
         PrintTools.debug(DEBUG, "Querying: " + url + "?" + BambooRestApi.EXPAND + "=" + BambooRestApi.RESULT_EXPANSION);

         HttpRequest httpRequest = requestPost(url).queryString(BambooRestApi.EXPAND, BambooRestApi.RESULT_EXPANSION);
         HttpResponse<BambooResultRequest> bambooResultRequestResponse = httpRequest.asObject(BambooResultRequest.class);
         BambooResultRequest bambooResultRequest = bambooResultRequestResponse.getBody();

         try
         {
            return bambooResultRequest.getResults().getResult()[0];
         }
         catch (ArrayIndexOutOfBoundsException | NullPointerException e)
         {
            return null;
         }
      }
      catch (UnirestException e)
      {
         handleUnirestExceptions(e);
         return null;
      }
   }

   public BambooJobResultRequest queryLastestJobResult(BambooRestJob bambooRestJob)
   {
      try
      {
         String url = baseUrl + BambooRestApi.API_PATH + BambooRestApi.RESULT + bambooRestJob.getKey() + BambooRestApi.JSON;
         PrintTools.debug(DEBUG, "Querying: " + url + "?" + BambooRestApi.EXPAND + "=" + BambooRestApi.RESULT_EXPANSION);

         HttpRequest httpRequest = requestPost(url).queryString(BambooRestApi.EXPAND, BambooRestApi.RESULT_EXPANSION);
         HttpResponse<BambooResultRequest> bambooResultRequestResponse = httpRequest.asObject(BambooResultRequest.class);
         BambooResultRequest bambooResultRequest = bambooResultRequestResponse.getBody();

         try
         {
            BambooResult bambooResult = bambooResultRequest.getResults().getResult()[0];
            return queryBambooJobResultRequest(bambooRestJob, bambooResult.getBuildNumber());
         }
         catch (ArrayIndexOutOfBoundsException | NullPointerException e)
         {
            return null;
         }
      }
      catch (UnirestException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public List<BambooTestResult> queryAllTestResultsFromJob(BambooRestJob bambooRestJob, int buildNumber)
   {
      BambooJobResultRequest bambooJobResultRequest = queryBambooJobResultRequest(bambooRestJob, buildNumber);
      List<BambooTestResult> allTestResults = new ArrayList<>();
      try
      {
         for (BambooTestResult testResult : bambooJobResultRequest.getTestResults().getAllTests().getTestResult())
         {
            if (testResult != null && testResult.getClassName() != null)
            {
               allTestResults.add(testResult);
            }
         }
      }
      catch (NullPointerException e)
      {

      }
      return allTestResults;
   }

   public BambooJobResultRequest queryBambooJobResultRequest(BambooRestJob bambooRestJob, int buildNumber)
   {
      try
      {
         String url = baseUrl + BambooRestApi.API_PATH + BambooRestApi.RESULT + bambooRestJob.getKey() + "/" + buildNumber + BambooRestApi.JSON;
         PrintTools.debug(DEBUG, "Querying: " + url + "?" + BambooRestApi.EXPAND + "=" + BambooRestApi.ALL_TESTS_EXPANSION);

         HttpRequest httpRequest = requestPost(url).queryString(BambooRestApi.EXPAND, BambooRestApi.ALL_TESTS_EXPANSION);
         HttpResponse<BambooJobResultRequest> bambooJobResultRequestResponse = httpRequest.asObject(BambooJobResultRequest.class);
         return bambooJobResultRequestResponse.getBody();
      }
      catch (UnirestException e)
      {
         handleUnirestExceptions(e);
         return null;
      }
   }

   public List<BambooRestJob> queryJobsInPlan(BambooRestPlan bambooRestPlan, boolean includeDisabledJobs)
   {
      try
      {
         String url = baseUrl + BambooRestApi.API_PATH + BambooRestApi.PLAN + bambooRestPlan + BambooRestApi.JSON;
         PrintTools.debug(DEBUG, "Querying: " + url + "?" + BambooRestApi.EXPAND + "=" + BambooRestApi.JOB_EXPANSION);

         HttpRequest httpRequest = requestPost(url).queryString(BambooRestApi.EXPAND, BambooRestApi.JOB_EXPANSION);
         HttpResponse<BambooPlanRequest> bambooPlanRequestResponse = httpRequest.asObject(BambooPlanRequest.class);
         BambooPlanRequest bambooPlanRequest = bambooPlanRequestResponse.getBody();

         List<BambooRestJob> jobs = new ArrayList<BambooRestJob>();
         for (BambooStage stage : bambooPlanRequest.getStages().getStage())
         {
            for (BambooPlan jobPlan : stage.getPlans().getPlan())
            {
               if (includeDisabledJobs || jobPlan.isEnabled())
               {
                  jobs.add(new BambooRestJob(jobPlan));
               }
            }
         }
         return jobs;
      }
      catch (UnirestException e)
      {
         handleUnirestExceptions(e);
         return null;
      }
   }

   public void destroy()
   {
      try
      {
         Unirest.shutdown();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void handleUnirestExceptions(UnirestException e)
   {
      if (e.getMessage().contains("Authentication Failure"))
      {
         throw new RuntimeException(e.getMessage() + ": Please ensure " + BambooRestApi.CREDENTIALS_PATH + " contains bamboo=<default pass>");
      }
      else
      {
         e.printStackTrace();
      }
   }

   private GetRequest requestPost(String url)
   {
      PrintTools.debug(DEBUG, "LOGGING IN AS: " + loginInfo.getUsername());
      return Unirest.get(url).basicAuth(loginInfo.getUsername(), loginInfo.getPassword());
   }
}
