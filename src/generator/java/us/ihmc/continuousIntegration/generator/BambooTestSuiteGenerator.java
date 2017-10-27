package us.ihmc.continuousIntegration.generator;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;
import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestApi;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestJob;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.model.AgileTestingLoadBalancedPlan;
import us.ihmc.continuousIntegration.model.AgileTestingMultiProjectWorkspace;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.model.AgileTestingTestClass;
import us.ihmc.continuousIntegration.model.AgileTestingTestMethod;
import us.ihmc.continuousIntegration.model.AgileTestingTestSuiteFile;

public class BambooTestSuiteGenerator
{
   public static final boolean DEBUG = true;

   private Map<String, AgileTestingProject> nameToProjectMap;
   private Map<String, AgileTestingClassPath> nameToClassPathMap;
   private AgileTestingMultiProjectWorkspace workspace;

   public void createForMultiProjectBuild(Path rootProjectPath)
   {
      nameToClassPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(rootProjectPath);

      nameToProjectMap = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
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
      }, rootProjectPath);

      workspace = new AgileTestingMultiProjectWorkspace(nameToProjectMap);
   }

   public void generateSpecificTestSuites(Set<String> projectNameWhiteList)
   {
      double totalDuration = 0.0;

      for (AgileTestingProject bambooEnabledProject : nameToProjectMap.values())
      {
         if (projectNameWhiteList.contains(bambooEnabledProject.getRawProjectName())
               || projectNameWhiteList.contains(bambooEnabledProject.getModifiedProjectName()))
         {
            bambooEnabledProject.generateAllTestSuites();

            totalDuration += bambooEnabledProject.getFastTotalDuration();
         }
      }

      PrintTools.info(this, "Fast total duration: " + new DecimalFormat("0.0").format(Conversions.secondsToMinutes(totalDuration)) + " minutes.");

      workspace.buildMaps();
   }

   public void generateAllTestSuites()
   {
      double totalDuration = 0.0;

      for (AgileTestingProject bambooEnabledProject : nameToProjectMap.values())
      {
         bambooEnabledProject.generateAllTestSuites();

         totalDuration += bambooEnabledProject.getFastTotalDuration();
      }

      PrintTools.info(this, "Fast total duration: " + new DecimalFormat("0.0").format(Conversions.secondsToMinutes(totalDuration)) + " minutes.");

      workspace.buildMaps();
   }

   public void generateRunAllTestSuites()
   {
      Path runAllTestSuitesPath = AgileTestingTools.getRunAllTestSuitesPath(nameToClassPathMap);

      if (runAllTestSuitesPath == null)
         return;

      for (IntegrationCategory bambooPlanType : IntegrationCategory.includedCategories)
      {
         PrintTools.info(this, "Generating: RunAll" + bambooPlanType.getName() + "TestSuites");

         ArrayList<AgileTestingTestSuiteFile> bambooTestSuiteFiles = new ArrayList<AgileTestingTestSuiteFile>();

         for (AgileTestingProject bambooEnabledProject : nameToProjectMap.values())
         {
            if (bambooPlanType.isIncludedAndNotLoadBalanced())
            {
               AgileTestingTestSuiteFile bambooSingletonTestSuiteFile = bambooEnabledProject.getTestCloud().getSingletonTestSuiteFiles().get(bambooPlanType);
               if (bambooSingletonTestSuiteFile != null)
                  bambooTestSuiteFiles.add(bambooSingletonTestSuiteFile);
            }
            else
            {
               bambooTestSuiteFiles.addAll(bambooEnabledProject.getTestCloud().getLoadBalancedPlans().get(bambooPlanType).getTestSuiteFiles());
            }
         }

         for (AgileTestingTestSuiteFile suiteFile : bambooTestSuiteFiles)
         {
            if (suiteFile.getPath().toString().matches(".*bin.*"))
               PrintTools.debug(this, "matches bin: " + suiteFile.getPath().toString());
         }

         String shortName = "RunAll" + bambooPlanType.getName();

         AgileTestingTestSuiteFile bambooRunAllTestSuitesFile = new AgileTestingTestSuiteFile(runAllTestSuitesPath.resolve(shortName + "TestSuites.java"),
                                                                                              bambooPlanType, shortName, 0.0);

         List<Path> bambooTestSuitePaths = new ArrayList<>();

         for (AgileTestingTestSuiteFile littleBambooTestSuiteFile : bambooTestSuiteFiles)
         {
            bambooTestSuitePaths.add(littleBambooTestSuiteFile.getPath());
         }

         bambooRunAllTestSuitesFile.generateTestSuite("RunAll" + bambooPlanType.getName() + "TestSuites", "us.ihmc.runAllBambooTestSuites",
                                                      bambooTestSuitePaths);
      }
   }

   public void sortAndPrintSuiteDurationsByPlanType()
   {
      Map<IntegrationCategory, ArrayList<AgileTestingTestSuiteFile>> sortedTestSuitesByDurationMap = workspace.getSortedTestSuitesByDurationMap();

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
      ArrayList<AgileTestingTestMethod> allTestsSortedByDuration = workspace.getAllTestsSortedByDuration();

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
      ArrayList<AgileTestingTestClass> allTestClassesSortedByDuration = workspace.getAllTestClassesSortedByDuration();

      System.out.println("\n-- LONGEST RUNNING CLASSES --");

      for (int i = 0; i < ((double) allTestClassesSortedByDuration.size() * 0.02); i++)
      {
         AgileTestingTestClass bambooTestClass = allTestClassesSortedByDuration.get(i);

         String durationMessage = "(" + MathTools.roundToSignificantFigures(Conversions.secondsToMinutes(bambooTestClass.getTotalDurationForAllPlans()), 2)
               + " min) ";

         PrintTools.info(this, "Class in top 2% longest: " + durationMessage + bambooTestClass.getTestClassSimpleName());
      }
   }

   public void printAllStatistics()
   {
      sortAndPrintSuiteDurationsByPlanType();
      printOutTopOnePercentHighestDurationTests();
      printOutTopTwoPercentHighestDurationTestClasses();
   }

   public void checkJobConfigurationOnBamboo(String bambooBaseUrl, List<BambooRestPlan> bambooPlanList)
   {
      BambooRestApi bambooRestApi = new BambooRestApi(bambooBaseUrl);

      SortedSet<String> existingJobsThatShouldBeEnabledOnBamboo = new TreeSet<String>();
      SortedSet<String> emptyJobsThatShouldBeDisabledOnBamboo = new TreeSet<String>();

      compareGeneratedTestSuitesWithBamboo(existingJobsThatShouldBeEnabledOnBamboo, emptyJobsThatShouldBeDisabledOnBamboo, bambooRestApi, bambooPlanList);

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

   public static void main(String[] args)
   {
      //      if (ContinuousIntegrationTools.isRunningOnContinuousIntegrationServer())
      //      {
      //         BambooTestSuiteGenerator bambooTestSuiteGenerator = new BambooTestSuiteGenerator(SourceTools.getWorkspacePath());
      //         bambooTestSuiteGenerator.generateAllTestSuites();
      //         bambooTestSuiteGenerator.generateRunAllTestSuites();
      //         bambooTestSuiteGenerator.printAllStatistics();
      //      }
      //      else
      //      {
      //         PrintTools.error("Test suites are no longer generated locally.");
      //         PrintTools.error("It's automatic on the server side now.");
      //         PrintTools.error("Please run BambooJobConfigurationTest to see which jobs to enable/disable.");
      //      }
      //      
//      BambooTestSuiteGenerator bambooTestSuiteGenerator = new BambooTestSuiteGenerator();
//      bambooTestSuiteGenerator.createForStandaloneProject(StandaloneProjectConfiguration.defaultConfiguration(Paths.get("F:/ReposMisc/IHMCCommons")));
//      bambooTestSuiteGenerator.generateAllTestSuites();
//      bambooTestSuiteGenerator.printAllStatistics();
   }
}
