package us.ihmc.continuousIntegration.codeQuality.bamboo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.model.AgileTestingLoadBalancedPlan;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.model.AgileTestingTestSuiteFile;
import us.ihmc.continuousIntegration.model.AgileTestingWorkspace;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.COMPILE)
public class BambooJobConfigurationTest
{
   private static BambooRestApi bambooRestApi;

   @BeforeClass
   public static void setUp()
   {
      bambooRestApi = new BambooRestApi();
   }

   @AfterClass
   public static void tearDown()
   {
      bambooRestApi.destroy();
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.9)
   @Test(timeout = 100000)
   public void testAllGeneratedTestSuitesMatchUpWithEnabledBambooJobsAndViceVersa()
   {
      ArrayList<String> existingJobsThatShouldBeEnabledOnBamboo = new ArrayList<String>();
      ArrayList<String> emptyJobsThatShouldBeDisabledOnBamboo = new ArrayList<String>();

      compareGeneratedTestSuitesWithBamboo(existingJobsThatShouldBeEnabledOnBamboo, emptyJobsThatShouldBeDisabledOnBamboo);

      Assert.assertTrue("Test suite(s) " + existingJobsThatShouldBeEnabledOnBamboo + " are not enabled in Bamboo!",
                        existingJobsThatShouldBeEnabledOnBamboo.isEmpty());
      Assert.assertTrue("Job(s) " + emptyJobsThatShouldBeDisabledOnBamboo + " should be disabled in Bamboo!", emptyJobsThatShouldBeDisabledOnBamboo.isEmpty());
   }

   public void compareGeneratedTestSuitesWithBamboo(ArrayList<String> existingJobsThatShouldBeEnabledOnBamboo,
                                                    ArrayList<String> emptyJobsThatShouldBeDisabledOnBamboo)
   {
      Map<String, AgileTestingClassPath> nameToClassPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getWorkspacePath());

      Map<String, AgileTestingProject> nameToProjectMap = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }

         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadTestCloud(nameToClassPathMap);
         }
      }, SourceTools.getWorkspacePath());

      AgileTestingWorkspace workspace = new AgileTestingWorkspace(nameToProjectMap);
      workspace.buildMaps();
      
      List<BambooRestJob> allJobsFromBambooRestApi = bambooRestApi.queryAllJobs();
      List<AgileTestingClassPath> allMappedTestSuites = new ArrayList<>();

      for (AgileTestingProject bambooEnabledProject : nameToProjectMap.values())
      {
         for (AgileTestingLoadBalancedPlan loadBalancedPlan : bambooEnabledProject.getTestCloud().getLoadBalancedPlans().values())
         {
            loadBalancedPlan.loadTestSuites();
            
            for (AgileTestingTestSuiteFile testSuiteFile : loadBalancedPlan.getTestSuiteFiles())
            {
               checkTestSuite(existingJobsThatShouldBeEnabledOnBamboo, allJobsFromBambooRestApi, allMappedTestSuites, testSuiteFile);
            }
         }
      }

      for (BambooRestJob job : allJobsFromBambooRestApi)
      {
         Result result = checkThatEnabledJobHasAMatchingTestSuite(job, allMappedTestSuites);

         if (result.addToList)
            if (!result.jobShortName.equals("CheckLicenses"))
               emptyJobsThatShouldBeDisabledOnBamboo.add(result.jobShortName);
      }

      for (String jobShortName : existingJobsThatShouldBeEnabledOnBamboo)
      {
         PrintTools.error(this, jobShortName + " is not enabled in Bamboo!");
      }

      for (String jobShortName : emptyJobsThatShouldBeDisabledOnBamboo)
      {
         PrintTools.error(this, jobShortName + " in Bamboo does not have a matching test suite and should be disabled.");
      }
   }

   private void checkTestSuite(ArrayList<String> existingJobsThatShouldBeEnabledOnBamboo, List<BambooRestJob> allJobs,
                               List<AgileTestingClassPath> allGeneratedTestSuites, AgileTestingTestSuiteFile testSuiteFile)
   {
      Result result = checkIfJobIsEnabledInBamboo(testSuiteFile.getPlanShortName(), allJobs);

      if (result.addToList)
         existingJobsThatShouldBeEnabledOnBamboo.add(result.jobShortName);

      allGeneratedTestSuites.add(new AgileTestingClassPath(testSuiteFile.getPath()));
   }

   private String deriveJobNameFromGeneratedTestSuite(AgileTestingClassPath generatedTestSuite)
   {
      String simpleName = generatedTestSuite.getSimpleName();
      return simpleName.substring(0, simpleName.length() - AgileTestingAnnotationTools.TEST_SUITE_NAME_POSTFIX.length());
   }

   private Result checkIfJobIsEnabledInBamboo(String jobShortName, List<BambooRestJob> allJobs)
   {
      boolean jobIsEnabled = false;

      for (BambooRestJob job : allJobs)
      {
         if (job.getName().equals(jobShortName) && job.isEnabled())
         {
            jobIsEnabled = true;
            break;
         }
      }

      return new Result(jobShortName, !jobIsEnabled);
   }

   private Result checkThatEnabledJobHasAMatchingTestSuite(BambooRestJob job, List<AgileTestingClassPath> generatedTestSuites)
   {
      if (job.isEnabled())
      {
         boolean generatedTestSuiteExists = false;

         for (AgileTestingClassPath generatedTestSuite : generatedTestSuites)
         {
            if (deriveJobNameFromGeneratedTestSuite(generatedTestSuite).equals(job.getName()))
            {
               generatedTestSuiteExists = true;
               break;
            }
         }

         return new Result(job.getName(), !generatedTestSuiteExists);
      }
      else
      {
         return new Result(null, false);
      }
   }

   private class Result
   {
      String jobShortName;
      boolean addToList;

      Result(String jobShortName, boolean addToList)
      {
         this.jobShortName = jobShortName;
         this.addToList = addToList;

         if (jobShortName != null
               && (jobShortName.matches(".*" + IntegrationCategory.VIDEO.getName() + ".*") || jobShortName.endsWith(IntegrationCategory.MANUAL.getName())
                     || jobShortName.endsWith(IntegrationCategory.HEALTH.getName()) || jobShortName.endsWith(IntegrationCategory.COMPILE.getName())
                     || jobShortName.endsWith("Dependencies") || jobShortName.matches("^RunAll.*TestSuites$") || jobShortName.startsWith("_")))
            this.addToList = false;
      }
   }
}
