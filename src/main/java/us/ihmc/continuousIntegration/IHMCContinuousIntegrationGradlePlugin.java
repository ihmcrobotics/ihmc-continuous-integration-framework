package us.ihmc.continuousIntegration;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions;
import us.ihmc.continuousIntegration.bambooRestApi.BambooRestPlan;
import us.ihmc.continuousIntegration.generator.BambooTestSuiteGenerator;
import us.ihmc.continuousIntegration.model.AgileTestingStandaloneWorkspace;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * - bambooPlansToCheck
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

      configureJUnitPlatform(project);

      //      testSuitesConfiguration = new TestSuiteConfiguration(project.getProperties());
//      testSuitesConfiguration.hyphenatedName = project.getName();
//      testSuitesConfiguration.pascalCasedName = AgileTestingTools.hyphenatedToPascalCased(project.getName());
//
//      createTask(project, "generateTestSuites");
//      createTask(project, "generateTestSuitesMultiProject");
   }

   private void configureJUnitPlatform(Project project)
   {
      try
      {
         Project testProject = project.project(project.getName() + "-test"); // assumes ihmc-build plugin
         Test testExtension = (Test) testProject.getTasks().getByName("test");

         testExtension.useJUnitPlatform(jUnitPlatformOptions -> {
            project.getLogger().info("[ihmc-ci] Using JUnit platform");

            includeTags(project, jUnitPlatformOptions, "includeTags");
            includeTags(project, jUnitPlatformOptions, "includeTag");
            excludeTags(project, jUnitPlatformOptions, "excludeTags");
            excludeTags(project, jUnitPlatformOptions, "excludeTag");
         });
      }
      catch (UnknownDomainObjectException e)
      {
         // do nothing
      }
   }

   private void includeTags(Project project, JUnitPlatformOptions jUnitPlatformOptions, String propertyName)
   {
      if (project.hasProperty(propertyName) && !project.property(propertyName).equals("all"))
      {
         for (String includeTag : ((String) project.property(propertyName)).trim().toLowerCase().split(","))
         {
            project.getLogger().info("[ihmc-ci] Including tag: " + includeTag);
            jUnitPlatformOptions.includeTags(includeTag);
         }
      }
   }

   private void excludeTags(Project project, JUnitPlatformOptions jUnitPlatformOptions, String propertyName)
   {
      if (project.hasProperty(propertyName) && !project.property(propertyName).equals("none"))
      {
         for (String excludeTag : ((String) project.property(propertyName)).trim().toLowerCase().split(","))
         {
            project.getLogger().info("[ihmc-ci] Excluding tag: " + excludeTag);
            jUnitPlatformOptions.excludeTags(excludeTag);
         }
      }
   }

   @SuppressWarnings("unchecked")
   private <T> T createExtension(String name, T pojo)
   {
      project.getExtensions().create(name, pojo.getClass());
      return ((T) project.getExtensions().getByName(name));
   }

   private void createTask(Project project, String taskName)
   {
      project.task(taskName).doLast(new MethodClosure(this, taskName));
   }

   public void generateTestSuites()
   {
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

   public void generateTestSuitesMultiProject()
   {
      BambooTestSuiteGenerator bambooTestSuiteGenerator = new BambooTestSuiteGenerator();
      bambooTestSuiteGenerator.createForMultiProjectBuild(multiProjectPath);
      bambooTestSuiteGenerator.generateAllTestSuites();
      bambooTestSuiteGenerator.printAllStatistics();
      if (!testSuitesConfiguration.getDisableJobCheck())
      {
         List<BambooRestPlan> bambooPlanList = new ArrayList<>();
         for (String planKey : testSuitesConfiguration.getBambooPlanKeys())
         {
            bambooPlanList.add(new BambooRestPlan(planKey));
         }
         bambooTestSuiteGenerator.checkJobConfigurationOnBamboo(testSuitesConfiguration.getBambooUrl(), bambooPlanList);
      }
   }
}
