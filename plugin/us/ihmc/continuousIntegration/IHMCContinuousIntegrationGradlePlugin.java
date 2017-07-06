package us.ihmc.continuousIntegration;

import java.nio.file.Path;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import us.ihmc.continuousIntegration.generator.BambooTestSuiteGenerator;

/**
 * TODO
 * 
 * - disableBalancing
 * - disableBambooConfigurationCheck
 * - bambooPlansToCheck
 * - targetSuiteDuration
 * - remove src folder check
 * 
 * Possible
 * - multi project build options
 * - excluded projects
 */
public class IHMCContinuousIntegrationGradlePlugin implements Plugin<Project>
{
   private Project project;
   private Path projectPath;
   private Path multiProjectPath;
   private TestSuiteConfiguration testSuitesConfiguration;

   @Override
   public void apply(Project project)
   {
      this.project = project;
      projectPath = project.getProjectDir().toPath();
      multiProjectPath = project.getRootDir().toPath().resolve("..");

      testSuitesConfiguration = createExtension("testSuites", new TestSuiteConfiguration());

      createTask(project, "generateTestSuitesStandalone");
      createTask(project, "generateTestSuitesMultiProject");
      createTask(project, "testConfiguration");
   }
   
   @SuppressWarnings("unchecked")
   private <T> T createExtension(String name, T pojo)
   {
      project.getExtensions().create(name, pojo.getClass(), project);
      return ((T) project.getExtensions().getByName(name));
   }
   
   private void createTask(Project project, String taskName)
   {
      project.task(taskName).doLast(new MethodClosure(this, taskName));
   }

   public void generateTestSuitesStandalone(String packageName)
   {
      BambooTestSuiteGenerator bambooTestSuiteGenerator = new BambooTestSuiteGenerator();
      bambooTestSuiteGenerator.createForStandaloneProject(projectPath);
      bambooTestSuiteGenerator.generateAllTestSuites();
      bambooTestSuiteGenerator.printAllStatistics();
   }

   public void generateTestSuitesMultiProject()
   {
      BambooTestSuiteGenerator bambooTestSuiteGenerator = new BambooTestSuiteGenerator();
      bambooTestSuiteGenerator.createForMultiProjectBuild(multiProjectPath);
      bambooTestSuiteGenerator.generateAllTestSuites();
      bambooTestSuiteGenerator.printAllStatistics();
      bambooTestSuiteGenerator.checkJobConfigurationOnBamboo();
   }

   public void testConfiguration()
   {
      System.out.println("disable: " + testSuitesConfiguration.getDisableBambooConfigurationCheck());
   }
}
