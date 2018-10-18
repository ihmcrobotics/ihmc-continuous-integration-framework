package us.ihmc.continuousIntegration.generator;

import us.ihmc.continuousIntegration.StandaloneProjectConfiguration;
import us.ihmc.continuousIntegration.TestSuiteConfiguration;
import us.ihmc.continuousIntegration.model.AgileTestingStandaloneWorkspace;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateTestSuitesDemo
{
   /**
    * Run in ihmc-ci/src/generator-test (i.e. $MODULE_DIR$)
    * @param args
    */
   public static void main(String[] args)
   {
      TestSuiteConfiguration testSuitesConfiguration = new TestSuiteConfiguration();
      testSuitesConfiguration.disableJobCheck = true;
      String projectName = "abstract-tests";
      testSuitesConfiguration.hyphenatedName = projectName;
      testSuitesConfiguration.pascalCasedName = "AbstractTests";
      Path projectPath = Paths.get("builds/" + projectName);
      AgileTestingStandaloneWorkspace workspace = new AgileTestingStandaloneWorkspace(new StandaloneProjectConfiguration(projectPath, testSuitesConfiguration));
      workspace.loadClasses();
      workspace.loadTestCloud();
      workspace.generateAllTestSuites();
      workspace.printAllStatistics();
      if (testSuitesConfiguration.getCrashOnMissingTimeouts())
      {
         workspace.checkJUnitTimeouts();
      }
      if (!testSuitesConfiguration.getDisableJobCheck())
      {
         workspace.checkJobConfigurationOnBamboo(testSuitesConfiguration.crashOnEmptyJobs);
      }
   }
}
