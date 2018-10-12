package us.ihmc.continuousIntegration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import us.ihmc.log.LogTools;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;

public class StandaloneProjectConfiguration
{
   private final Path projectPath;
   private final String hyphenatedName;
   private final String pascalCasedName;
   private final String bambooUrl;
   private final List<BambooRestPlan> bambooPlans = new ArrayList<>();
   private final double maximumSuiteDuration;

   public StandaloneProjectConfiguration(Path projectPath, TestSuiteConfiguration testSuiteConfiguration)
   {
      this.projectPath = projectPath;
      this.hyphenatedName = testSuiteConfiguration.getHyphenatedName();
      this.pascalCasedName = testSuiteConfiguration.getPascalCasedName();
      this.bambooUrl = testSuiteConfiguration.bambooUrl;
      for (String planKey : testSuiteConfiguration.getBambooPlanKeys())
      {
         LogTools.info("[ihmc-ci] Adding plan to check: " + planKey);
         bambooPlans.add(new BambooRestPlan(planKey));
      }
      this.maximumSuiteDuration = testSuiteConfiguration.getMaxSuiteDuration();
   }

   public Path getProjectPath()
   {
      return projectPath;
   }

   public String getHyphenatedName()
   {
      return hyphenatedName;
   }

   public String getPascalCasedName()
   {
      return pascalCasedName;
   }
   
   public String getBambooBaseUrl()
   {
      return bambooUrl;
   }

   public List<BambooRestPlan> getBambooPlans()
   {
      return bambooPlans;
   }

   public double getMaximumSuiteDuration()
   {
      return maximumSuiteDuration;
   }
   
   public static StandaloneProjectConfiguration defaultConfiguration(Path pathToProject)
   {
      TestSuiteConfiguration testSuiteConfiguration = new TestSuiteConfiguration();
      testSuiteConfiguration.hyphenatedName = pathToProject.getFileName().toString();
      testSuiteConfiguration.pascalCasedName = pathToProject.getFileName().toString();
      StandaloneProjectConfiguration configuration = new StandaloneProjectConfiguration(pathToProject, testSuiteConfiguration);
      return configuration;
   }
}
