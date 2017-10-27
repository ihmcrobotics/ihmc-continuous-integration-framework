package us.ihmc.continuousIntegration.codeQuality.gradle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.WriteOption;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.gradle.GradleSettingsFormatter;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.EXCLUDE)
public class GradleCodeQualityTest
{
   public static final boolean DEBUG = false;
   public static final boolean AUTO_FIX = false;
   
   private static final String INCLUDE_FLAT_REGEX = SourceTools.NOT_COMMENTED_REGEX + "include.*";

	@ContinuousIntegrationTest(estimatedDuration = 7.6)
   @Test(timeout = 38000)
   public void testEveryProjectDependencyIncludesFlatInSettingsDotGradle()
   {
      Map<AgileTestingProject, SortedSet<AgileTestingProject>> projectDependencyMap = AgileTestingTools.loadProjectDependencyMap(true);
      
      if (DEBUG)
      {
         for (AgileTestingProject atProject : projectDependencyMap.keySet())
         {
            System.out.println(atProject.getRawProjectName() + ": ");
            
            for (AgileTestingProject dependency : projectDependencyMap.get(atProject))
            {
               System.out.println("\t" + dependency.getRawProjectName());
            }
            
            System.out.println();
         }
      }
      
      Map<String, AgileTestingProject> nameToProjectMap = new LinkedHashMap<>();
      for (AgileTestingProject atProject : projectDependencyMap.keySet())
      {
         nameToProjectMap.put(atProject.getRawProjectName(), atProject);
      }
      
      SortedSet<String> projectsWithBadGradleSettings = new TreeSet<>();
      
      for (AgileTestingProject gradleProject : projectDependencyMap.keySet())
      {
         if (gradleProject.getRawProjectName().startsWith("_") || gradleProject.getRawProjectName().startsWith("Bamboo") || gradleProject.getRawProjectName().startsWith("RunAll"))
            continue;
         
         Path settingsGradlePath = gradleProject.getPath().resolve(AgileTestingTools.SETTINGS_GRADLE_NAME);
         if (!Files.exists(settingsGradlePath))
            continue;
         List<String> lines = FileTools.readAllLines(settingsGradlePath, DefaultExceptionHandler.PRINT_STACKTRACE);
         
         SortedSet<AgileTestingProject> settingsGradleProjects = new TreeSet<>();
         
         for (String line : lines)
         {
            if (line.matches(INCLUDE_FLAT_REGEX))
            {
               if (!(line.contains("'") || line.contains("\"")))
                  continue;
               String projectName;
               if (line.contains("'"))
                  projectName = line.split("'")[1];
               else
                  projectName = line.split("\"")[1].substring(1);
               try
               {
                  settingsGradleProjects.add(nameToProjectMap.get(projectName));
               }
               catch (NullPointerException e)
               {
                  PrintTools.error(projectName + " does not exist in " + settingsGradlePath);                  
                  fail(projectName + " does not exist in " + settingsGradlePath);
               }
            }
         }
         
         PrintWriter printWriter = null;
         if (AUTO_FIX)
         {
            printWriter = FileTools.newPrintWriter(settingsGradlePath, WriteOption.APPEND, DefaultExceptionHandler.PRINT_STACKTRACE);
            printWriter.println();
         }
         
         for (AgileTestingProject dependency : projectDependencyMap.get(gradleProject))
         {
            if (!settingsGradleProjects.contains(dependency))
            {
               if (AUTO_FIX)
               {
                  printWriter.println("includeFlat '" + dependency.getRawProjectName() + "'");
               }
               
               projectsWithBadGradleSettings.add(gradleProject.getRawProjectName());
               System.out.println(gradleProject.getRawProjectName() + ": " + AgileTestingTools.SETTINGS_GRADLE_NAME + " does not contain " + dependency.getRawProjectName());
            }
         }
         
         for (AgileTestingProject includedProject : settingsGradleProjects)
         {
            if (!projectDependencyMap.get(gradleProject).contains(includedProject))
            {
               System.out.println(gradleProject.getRawProjectName() + ": " + AgileTestingTools.SETTINGS_GRADLE_NAME + " includes " + includedProject.getRawProjectName() + " and should not.");
            }
         }
         
         if (AUTO_FIX)
         {
            printWriter.close();
         }
      }
      
      if (AUTO_FIX)
      {
         GradleSettingsFormatter.format();
      }
      
      assertEquals("Projects have bad gradle settings: " + projectsWithBadGradleSettings, 0, projectsWithBadGradleSettings.size());
   }
}
