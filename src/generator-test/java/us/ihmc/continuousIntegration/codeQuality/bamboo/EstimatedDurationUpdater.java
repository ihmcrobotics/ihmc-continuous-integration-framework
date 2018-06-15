package us.ihmc.continuousIntegration.codeQuality.bamboo;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;
import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooJobResultRequest;
import us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects.BambooTestResult;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.testSuiteRunner.AgileTestingTestMethodAnnotationWriter;
import us.ihmc.continuousIntegration.testSuiteRunner.AtomicTestRun;
import us.ihmc.continuousIntegration.tools.SourceTools;

import java.util.*;

/**
 * For AUTO_FIX, run from your repository-group directory!
 */
public class EstimatedDurationUpdater
{
   private static final boolean AUTO_FIX = false;
   
   private static final String bambooBaseUrl = "http://bamboo.ihmc.us/";
   private static BambooRestApi bambooRestApi = new BambooRestApi(bambooBaseUrl);
   private static final List<BambooRestPlan> planList = new ArrayList<>();
   static
   {
      planList.add(new BambooRestPlan("LIBS-IHMCOPENROBOTICSSOFTWAREFAST"));
   }

   public static void main(String[] args)
   {
      new EstimatedDurationUpdater().checkAllDurationsOnBambooAndUpdate();
   }

   public void checkAllDurationsOnBambooAndUpdate()
   {
      final Map<BambooRestJob, Double> jobToDurationMap = new HashMap<>();
      final Map<BambooRestPlan, Map<BambooRestJob, List<BambooTestResult>>> planToTestResultMap = new HashMap<>();
      
      for (BambooRestPlan plan : planList)
      {
         if (!plan.getKey().contains("VIDEO"))
         {
            fillResultsMap(plan, jobToDurationMap, planToTestResultMap);
         }
      }
      
      System.out.println();
      
      List<BambooRestJob> allJobs = new ArrayList<>();
      allJobs.addAll(jobToDurationMap.keySet());

      Collections.sort(allJobs, (o1, o2) -> {
         {
            double o1Time = jobToDurationMap.get(o1);
            double o2Time = jobToDurationMap.get(o2);

            if (o1Time > o2Time)
            {
               return -1;
            }
            else if (o1Time < o2Time)
            {
               return 1;
            }
            else
            {
               return 0;
            }
         }
      });
      
      System.out.println("\n\n-- SORTED BY JOB DURATION --");
      
      for (BambooRestJob job : allJobs)
      {
         String format = String.valueOf(MathTools.roundToSignificantFigures(jobToDurationMap.get(job), 2));
         System.out.println(job.getName() + ": " + format + " (m)");
      }
      
      for (BambooRestPlan plan : planToTestResultMap.keySet())
      {
         System.out.println("\n\n-- SORTED BY TEST DURATION " + plan + " --");
         
         List<BambooTestResult> testResults = new ArrayList<>();
         
         for (BambooRestJob job : planToTestResultMap.get(plan).keySet())
         {
            testResults.addAll(planToTestResultMap.get(plan).get(job));
         }
         
         Collections.sort(testResults, new Comparator<BambooTestResult>()
         {
            @Override
            public int compare(BambooTestResult o1, BambooTestResult o2)
            {
               return o2.getDurationMillis() - o1.getDurationMillis();
            }
         });
         
         for (BambooTestResult testResult : testResults)
         {
            System.out.println(testResult.getClassName() + ":" + testResult.getMethodName() + ": " + testResult.getFormattedDuration() + " (m)");
         }
      }
      
      if (AUTO_FIX)
      {
         Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getWorkspacePath());
         
         for (BambooRestPlan plan : planToTestResultMap.keySet())
         {
            for (BambooRestJob job : planToTestResultMap.get(plan).keySet())
            {
               for (BambooTestResult testResult : planToTestResultMap.get(plan).get(job))
               {
                  AtomicTestRun atomicTestRun = new AtomicTestRun(testResult.getClassName(), testResult.getMethodName(), testResult.getDurationSeconds(), testResult.getStatus());
                  AgileTestingClassPath atClassPath = nameToPathMap.get(testResult.getClassName());
                  PrintTools.info("Writing annotation for " + testResult.getClassName() + ". ATClassPath ref: " + atClassPath);
                  
                  if (atClassPath != null && atomicTestRun.isSuccessful())
                  {
                     AgileTestingTestMethodAnnotationWriter.writeAnnotationsForTestRun(atomicTestRun, nameToPathMap, atClassPath);
                  }
               }
            }
         }
      }

      bambooRestApi.destroy();
   }

   private void fillResultsMap(BambooRestPlan plan, final Map<BambooRestJob, Double> jobToDurationMap, Map<BambooRestPlan, Map<BambooRestJob, List<BambooTestResult>>> planToTestResultMap)
   {
      List<BambooRestJob> jobsInPlan = new ArrayList<>();
      jobsInPlan.addAll(bambooRestApi.queryJobsInPlan(plan, false));
      
      planToTestResultMap.put(plan, new HashMap<BambooRestJob, List<BambooTestResult>>());
      
      System.out.println("Loading " + plan);
      int i = 0, tot = jobsInPlan.size();
      for (BambooRestJob job : jobsInPlan)
      {
         BambooJobResultRequest jobResultRequest = bambooRestApi.queryLatestJobResults(job);
         if (jobResultRequest == null || jobResultRequest.getTestResults().getAllTests().getTestResult() == null)
         {
            System.err.println("(" + i++ + "/" + tot + ") 404'd");
            continue;
         }
         else
         {
            System.out.println("(" + i++ + "/" + tot + ")");
         }
         
         Integer millis = Integer.valueOf(jobResultRequest.getBuildDuration());
         double minutes = Conversions.millisecondsToMinutes(millis);
         
         jobToDurationMap.put(job, minutes);
         
         List<BambooTestResult> testResults = new ArrayList<>();
         planToTestResultMap.get(plan).put(job, testResults);
         
         for (BambooTestResult testResult : jobResultRequest.getTestResults().getAllTests().getTestResult())
         {
            if (testResult != null && testResult.getDuration() != null)
               planToTestResultMap.get(plan).get(job).add(testResult);
         }
      }
   }
}
