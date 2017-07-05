package us.ihmc.continuousIntegration.model;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;
import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;

public class AgileTestingLoadBalancedPlan
{
   private IntegrationCategory bambooPlanType;
   private AgileTestingProject bambooEnabledProject;
   
   private Map<Character, List<Path>> loadBalancedTests = new HashMap<>();
   private Map<Character, Double> loadBalancedDurations = new HashMap<>();
   
   private ArrayList<AgileTestingTestSuiteFile> testSuiteFiles = new ArrayList<AgileTestingTestSuiteFile>();
   
   private Character currentLetter = 'A';
   
   private int numberOfTests = 0;

   public AgileTestingLoadBalancedPlan(IntegrationCategory bambooPlanType, AgileTestingProject bambooEnabledProject)
   {
      this.bambooPlanType = bambooPlanType;
      this.bambooEnabledProject = bambooEnabledProject;
   }

   public void add(AgileTestingTestClass bambooTestClass)
   {
      if (bambooPlanType == IntegrationCategory.FAST && bambooTestClass.getTotalDurationForTarget(bambooPlanType) > AgileTestingAnnotationTools.JOB_DURATION_CAP)
      {
         PrintTools.warn(this, "classTotalDuration > JOB_DURATION_CAP in Fast: " + bambooTestClass.getTestClassSimpleName() + " (" + MathTools.roundToSignificantFigures(Conversions.secondsToMinutes(bambooTestClass.getTotalDurationForTarget(bambooPlanType)), 2) + " m)");
      }
      if (bambooPlanType == IntegrationCategory.SLOW && bambooTestClass.getTotalDurationForTarget(bambooPlanType) < AgileTestingAnnotationTools.RECOMMENDED_CLASS_CAP)
      {
         PrintTools.warn(this, "classTotalDuration < RECOMMENDED_CLASS_CAP in Slow. Consider moving to Fast: " + bambooTestClass.getTestClassSimpleName() + " (" + MathTools.roundToSignificantFigures(Conversions.secondsToMinutes(bambooTestClass.getTotalDurationForTarget(bambooPlanType)), 2) + " m)");
      }
      
      if (loadBalancedDurations.get(currentLetter) != null && (loadBalancedDurations.get(currentLetter) + bambooTestClass.getTotalDurationForTarget(bambooPlanType)) > AgileTestingAnnotationTools.JOB_DURATION_CAP)
      {
         currentLetter++;
      }
      
      addTestClassToCurrentLetterJob(bambooTestClass);
   }

   private void addTestClassToCurrentLetterJob(AgileTestingTestClass bambooTestClass)
   {
      ensureMapsAreReadyForNewJobInLetter();
      
      loadBalancedTests.get(currentLetter).add(bambooTestClass.getPath());
      loadBalancedDurations.put(currentLetter, loadBalancedDurations.get(currentLetter) + bambooTestClass.getTotalDurationForTarget(bambooPlanType));
      
      numberOfTests++;
   }

   private void ensureMapsAreReadyForNewJobInLetter()
   {
      if (!loadBalancedTests.containsKey(currentLetter))
      {
         loadBalancedTests.put(currentLetter, new ArrayList<Path>());
         loadBalancedDurations.put(currentLetter, 0.0);
      }
   }
   
   public void loadTestSuites()
   {
      processLoadBalancedTests(false);
   }
   
   public void generateTestSuites()
   {
      processLoadBalancedTests(true);
   }
   
   private void processLoadBalancedTests(boolean generateTestSuites)
   {
      for (Character letter : loadBalancedTests.keySet())
      {
         String testSuiteSimpleName = bambooEnabledProject.getModifiedProjectName() + letter + bambooPlanType.getName() + "TestSuite";
         Path suitePath = bambooEnabledProject.getGeneratedTestSuitesDirectory().resolve(testSuiteSimpleName + ".java");
         String packageName = bambooEnabledProject.getPackageName();
         List<Path> testPathsToPutInTestSuite = loadBalancedTests.get(letter);
         double durationInMinutes = Conversions.secondsToMinutes(loadBalancedDurations.get(letter));
         String formattedDuration = new DecimalFormat("0.0").format(durationInMinutes);
         
         String planShortName = testSuiteSimpleName.replaceAll("TestSuite", "");
         
         AgileTestingTestSuiteFile bambooTestSuiteFile = new AgileTestingTestSuiteFile(suitePath, bambooPlanType, planShortName, loadBalancedDurations.get(letter));
         
         testSuiteFiles.add(bambooTestSuiteFile);
         
         if (generateTestSuites)
         {
            PrintTools.info(this, "Generating: " + "(" + formattedDuration + " min) " + planShortName);
            
            bambooTestSuiteFile.generateTestSuite(testSuiteSimpleName, packageName, testPathsToPutInTestSuite);
         }
         else
         {
            PrintTools.info(this, "Loading: " + "(" + formattedDuration + " min) " + planShortName);
         }
      }
   }

   public int getNumberOfTests()
   {
      return numberOfTests;
   }
   
   public IntegrationCategory getBambooPlanType()
   {
      return bambooPlanType;
   }

   public Map<Character, List<Path>> getLoadBalancedTests()
   {
      return loadBalancedTests;
   }

   public Map<Character, Double> getLoadBalancedDurations()
   {
      return loadBalancedDurations;
   }

   public ArrayList<AgileTestingTestSuiteFile> getTestSuiteFiles()
   {
      return testSuiteFiles;
   }
}
