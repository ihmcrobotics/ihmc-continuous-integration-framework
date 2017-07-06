package us.ihmc.continuousIntegration;

import java.nio.file.Path;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import us.ihmc.continuousIntegration.generator.BambooTestSuiteGenerator;

public class IHMCContinuousIntegrationGradlePlugin implements Plugin<Project>
{
   private Path projectPath;
   private Path multiProjectPath;
   private Project project;
   
   @Override
   public void apply(Project project)
   {
      projectPath = project.getProjectDir().toPath();
      multiProjectPath = project.getRootDir().toPath().resolve("..");
      
      createTask(project, "generateTestSuitesStandalone");
      createTask(project, "generateTestSuitesMultiProject");
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
}
