package us.ihmc.continuousIntegration.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;
import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.continuousIntegration.StandaloneProjectConfiguration;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;

public class AgileTestingStandaloneWorkspace
{
   private final StandaloneProjectConfiguration configuration;
   private final AgileTestingProject agileTestingProject;
   private Map<String, AgileTestingClassPath> nameToClassPathMap;

   private Map<IntegrationCategory, ArrayList<AgileTestingTestSuiteFile>> sortedTestSuitesByDurationMap;
   private ArrayList<AgileTestingTestMethod> allTestsSortedByDuration;
   private ArrayList<AgileTestingTestClass> allTestClassesSortedByDuration;

   public AgileTestingStandaloneWorkspace(StandaloneProjectConfiguration configuration)
   {
      this.configuration = configuration;
      agileTestingProject = new AgileTestingProject(configuration);
   }

   public void loadClasses()
   {
      nameToClassPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(configuration);
   }

   public void loadTestCloud()
   {
      if (nameToClassPathMap == null)
      {
         loadClasses();
      }

      agileTestingProject.loadTestCloud(nameToClassPathMap);
   }

   public void generateAllTestSuites()
   {
      agileTestingProject.generateAllTestSuites();

      double totalDuration = agileTestingProject.getFastTotalDuration();

      PrintTools.info(this, "Fast total duration: " + new DecimalFormat("0.0").format(Conversions.secondsToMinutes(totalDuration)) + " minutes.");

      buildMaps();
   }

   private void buildMaps()
   {
      buildDurationToTestSuiteMap();
      buildAllTestSortedByDurationMap();
      buildAllTestClassSortedByDurationMap();
   }

   private void buildAllTestClassSortedByDurationMap()
   {
      allTestClassesSortedByDuration = new ArrayList<>();

      for (AgileTestingTestClass testClass : agileTestingProject.getTestCloud().getTestClasses())
      {
         allTestClassesSortedByDuration.add(testClass);
      }

      Collections.sort(allTestClassesSortedByDuration, new Comparator<AgileTestingTestClass>()
      {
         @Override
         public int compare(AgileTestingTestClass o1, AgileTestingTestClass o2)
         {
            if (o1.getTotalDurationForAllPlans() > o2.getTotalDurationForAllPlans())
               return -1;
            else if (o1.getTotalDurationForAllPlans() == o2.getTotalDurationForAllPlans())
               return 0;
            else
               return 1;
         }
      });
   }

   private void buildDurationToTestSuiteMap()
   {
      sortedTestSuitesByDurationMap = new HashMap<>();

      for (IntegrationCategory testSuiteTarget : IntegrationCategory.includedCategories)
      {
         sortedTestSuitesByDurationMap.put(testSuiteTarget, new ArrayList<AgileTestingTestSuiteFile>());
      }

      for (IntegrationCategory testSuiteTarget : IntegrationCategory.includedCategories)
      {
         if (testSuiteTarget.isLoadBalanced())
         {
            sortedTestSuitesByDurationMap.get(testSuiteTarget)
                                         .addAll(agileTestingProject.getTestCloud().getLoadBalancedPlans().get(testSuiteTarget).getTestSuiteFiles());
         }
         else
         {
            sortedTestSuitesByDurationMap.get(testSuiteTarget).addAll(agileTestingProject.getTestCloud().getSingletonTestSuiteFiles().values());
         }
      }

      for (IntegrationCategory category : IntegrationCategory.includedCategories)
      {
         Collections.sort(sortedTestSuitesByDurationMap.get(category), new Comparator<AgileTestingTestSuiteFile>()
         {
            @Override
            public int compare(AgileTestingTestSuiteFile o1, AgileTestingTestSuiteFile o2)
            {
               if (o1.getDuration() > o2.getDuration())
                  return -1;
               else if (o1.getDuration() == o2.getDuration())
                  return 0;
               else
                  return 1;
            }
         });
      }
   }

   private void buildAllTestSortedByDurationMap()
   {
      allTestsSortedByDuration = new ArrayList<>();

      for (AgileTestingTestClass testClass : agileTestingProject.getTestCloud().getTestClasses())
      {
         allTestsSortedByDuration.addAll(testClass.getTestMethods());
      }

      Collections.sort(allTestsSortedByDuration, new Comparator<AgileTestingTestMethod>()
      {
         @Override
         public int compare(AgileTestingTestMethod o1, AgileTestingTestMethod o2)
         {
            if (o1.getDuration() > o2.getDuration())
               return -1;
            else if (o1.getDuration() == o2.getDuration())
               return 0;
            else
               return 1;
         }
      });
   }

   public void printAllStatistics()
   {
      sortAndPrintSuiteDurationsByPlanType();
      printOutTopOnePercentHighestDurationTests();
      printOutTopTwoPercentHighestDurationTestClasses();
   }

   public void sortAndPrintSuiteDurationsByPlanType()
   {
      for (IntegrationCategory bambooPlanType : IntegrationCategory.includedCategories)
      {
         System.out.println("\n-- SORTED TESTS FOR " + bambooPlanType.getName().toUpperCase() + " --");
         for (AgileTestingTestSuiteFile bambooTestSuiteFile : sortedTestSuitesByDurationMap.get(bambooPlanType))
         {
            PrintTools.info(this, bambooTestSuiteFile.getPlanShortName() + ": "
                  + MathTools.roundToSignificantFigures(Conversions.secondsToMinutes(bambooTestSuiteFile.getDuration()), 2) + " m");
         }
      }
   }

   public void printOutTopOnePercentHighestDurationTests()
   {
      System.out.println("\n-- LONGEST RUNNING TESTS --");

      for (int i = 0; i < ((double) allTestsSortedByDuration.size() * 0.01); i++)
      {
         AgileTestingTestMethod bambooTestMethod = allTestsSortedByDuration.get(i);

         String durationMessage = "(" + MathTools.roundToSignificantFigures(Conversions.secondsToMinutes(bambooTestMethod.getDuration()), 2) + " min) ";

         PrintTools.info(this,
                         "Test in top 1% longest: " + durationMessage + bambooTestMethod.getTestClassSimpleName() + ":" + bambooTestMethod.getMethodName());
      }
   }

   public void printOutTopTwoPercentHighestDurationTestClasses()
   {
      System.out.println("\n-- LONGEST RUNNING CLASSES --");

      for (int i = 0; i < ((double) allTestClassesSortedByDuration.size() * 0.02); i++)
      {
         AgileTestingTestClass bambooTestClass = allTestClassesSortedByDuration.get(i);

         String durationMessage = "(" + MathTools.roundToSignificantFigures(Conversions.secondsToMinutes(bambooTestClass.getTotalDurationForAllPlans()), 2)
               + " min) ";

         PrintTools.info(this, "Class in top 2% longest: " + durationMessage + bambooTestClass.getTestClassSimpleName());
      }
   }

   public void checkJobConfigurationOnBamboo()
   {
      BambooRestApi bambooRestApi = new BambooRestApi(configuration.getBambooBaseUrl());

      SortedSet<String> existingJobsThatShouldBeEnabledOnBamboo = new TreeSet<String>();
      SortedSet<String> emptyJobsThatShouldBeDisabledOnBamboo = new TreeSet<String>();

      compareGeneratedTestSuitesWithBamboo(existingJobsThatShouldBeEnabledOnBamboo, emptyJobsThatShouldBeDisabledOnBamboo, bambooRestApi,
                                           configuration.getBambooPlans());

      if (!existingJobsThatShouldBeEnabledOnBamboo.isEmpty())
      {
         throw new RuntimeException("Test suite(s) " + existingJobsThatShouldBeEnabledOnBamboo + " are not enabled in Bamboo!");
      }
      if (!emptyJobsThatShouldBeDisabledOnBamboo.isEmpty())
      {
         throw new RuntimeException("Job(s) " + emptyJobsThatShouldBeDisabledOnBamboo + " should be disabled in Bamboo!");
      }

      bambooRestApi.destroy();
   }

   private void compareGeneratedTestSuitesWithBamboo(SortedSet<String> existingJobsThatShouldBeEnabledOnBamboo,
                                                     SortedSet<String> emptyJobsThatShouldBeDisabledOnBamboo, BambooRestApi bambooRestApi,
                                                     List<BambooRestPlan> bambooPlanList)
   {
      List<BambooRestJob> allJobsFromBambooRestApi = bambooRestApi.queryAllJobs(bambooPlanList);
      List<AgileTestingClassPath> allMappedTestSuites = new ArrayList<>();

      for (AgileTestingLoadBalancedPlan loadBalancedPlan : agileTestingProject.getTestCloud().getLoadBalancedPlans().values())
      {
         loadBalancedPlan.loadTestSuites();

         for (AgileTestingTestSuiteFile testSuiteFile : loadBalancedPlan.getTestSuiteFiles())
         {
            checkTestSuite(existingJobsThatShouldBeEnabledOnBamboo, allJobsFromBambooRestApi, allMappedTestSuites, testSuiteFile);
         }
      }

      for (BambooRestJob job : allJobsFromBambooRestApi)
      {
         Result result = checkThatEnabledJobHasAMatchingTestSuite(job, allMappedTestSuites);

         if (result.addToList)
            if (!result.jobShortName.equals("CheckLicenses"))
               if (result.jobShortName.startsWith(agileTestingProject.getModifiedProjectName()))
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

   private void checkTestSuite(SortedSet<String> existingJobsThatShouldBeEnabledOnBamboo, List<BambooRestJob> allJobs,
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

   public Map<IntegrationCategory, ArrayList<AgileTestingTestSuiteFile>> getSortedTestSuitesByDurationMap()
   {
      return sortedTestSuitesByDurationMap;
   }

   public ArrayList<AgileTestingTestMethod> getAllTestsSortedByDuration()
   {
      return allTestsSortedByDuration;
   }

   public ArrayList<AgileTestingTestClass> getAllTestClassesSortedByDuration()
   {
      return allTestClassesSortedByDuration;
   }
}
