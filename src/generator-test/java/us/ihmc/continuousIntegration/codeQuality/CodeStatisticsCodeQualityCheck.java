package us.ihmc.continuousIntegration.codeQuality;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import us.ihmc.commons.MathTools;
import us.ihmc.log.LogTools;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.model.AgileTestingTestClass;
import us.ihmc.continuousIntegration.model.AgileTestingTestMethod;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

public class CodeStatisticsCodeQualityCheck
{
   private static final double PERCENTAGE_OF_UNFINISHED_TESTS_THRESHOLD = 10.0;

   public static void main(String[] args)
   {
      new CodeStatisticsCodeQualityCheck().testPercentOfTestClassesThatAreUnfinishedIsLessThanThreshold();
   }

   public void testPercentOfTestClassesThatAreUnfinishedIsLessThanThreshold()
   {
      int numberOfTestClasses = 0;
      int numberOfTests = 0;
      Map<IntegrationCategory, Integer> numberOfTestsInTargets = new HashMap<>();
      for (IntegrationCategory category : IntegrationCategory.values)
      {
         numberOfTestsInTargets.put(category, 0);
      }

      final Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getWorkspacePath());
      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
         
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadTestCloud(nameToPathMap);
         }
      }, SourceTools.getWorkspacePath());
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {
         for (AgileTestingTestClass bambooTestClass : bambooEnabledProject.getTestCloud().getTestClasses())
         {
            numberOfTestClasses++;
            
            for (AgileTestingTestMethod testMethod : bambooTestClass.getTestMethods())
            {
               numberOfTests++;
               
               for (IntegrationCategory integrationCategory : testMethod.getCategories())
               {
                  numberOfTestsInTargets.put(integrationCategory, numberOfTestsInTargets.get(integrationCategory) + 1);
               }
            }
         }
      }
      
      LogTools.info("Number of test classes: " + numberOfTestClasses);
      LogTools.info("Number of tests: " + numberOfTests);
      
      for (IntegrationCategory category : IntegrationCategory.values)
      {
         LogTools.info("Number of tests in " + category.getName() + ": " + numberOfTestsInTargets.get(category));
      }
      
      int numberOfUnfinishedTests = numberOfTestsInTargets.get(IntegrationCategory.EXCLUDE) + numberOfTestsInTargets.get(IntegrationCategory.IN_DEVELOPMENT) + numberOfTestsInTargets.get(IntegrationCategory.FLAKY);
      LogTools.info("Number of tests in Exclude, InDevelopment, Flaky (Unfinished): " + numberOfUnfinishedTests);
      double perecentageOfTestClassesThatAreUnfinished = (double) numberOfUnfinishedTests / (double) numberOfTests * 100.0;
      
      LogTools.info("Percentage of unfinished test classes: " + MathTools.roundToSignificantFigures(perecentageOfTestClassesThatAreUnfinished, 2) + " %");
      
      Assert.assertFalse("Percentage of unfinished test classes is greater than " + PERCENTAGE_OF_UNFINISHED_TESTS_THRESHOLD + " %.", perecentageOfTestClassesThatAreUnfinished > PERCENTAGE_OF_UNFINISHED_TESTS_THRESHOLD);
   }
}
