package us.ihmc.continuousIntegration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import us.ihmc.commons.PrintTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;

public class AgileTestingTools
{
   public static final String GENERATED_TEST_SUITES_DIRECTORY_NAME = "generatedTestSuites";
   public static final String GENERATED_TEST_SUITES_DIRECTORY_PACKAGE = "us/ihmc/" + GENERATED_TEST_SUITES_DIRECTORY_NAME;
   public static final String SETTINGS_GRADLE_NAME = "settings.gradle";
   private static final String BUILD_GRADLE_NAME = "build.gradle";
   private static final String SOURCE_FOLDER_NAME = "src";
   private static final String COMPILE_PROJECT_REGEX = SourceTools.NOT_COMMENTED_REGEX + "compile.*getProjectDependency.*";
   private static final String TEST_COMPILE_PROJECT_REGEX = SourceTools.NOT_COMMENTED_REGEX + "testCompile.*getProjectDependency.*";
   private static final String RUNTIME_COMPILE_PROJECT_REGEX = SourceTools.NOT_COMMENTED_REGEX + "runtime.*getProjectDependency.*";
   
   private static final String RUN_ALL_FAST_TEST_SUITES_CLASS_NAME = "us.ihmc.runAllBambooTestSuites.RunAllFastTestSuites";
   
   public static final SortedSet<String> BAMBOO_DISABLED_PROJECT_NAMES = new TreeSet<String>();
   public static final SortedSet<String> INDEPENDENT_PROJECT_NAMES = new TreeSet<String>();
   public static final SortedSet<String> THIRD_PARTY_PACKAGE_NAMES = new TreeSet<String>();
   static
   {
      BAMBOO_DISABLED_PROJECT_NAMES.add("ROSJava");
      BAMBOO_DISABLED_PROJECT_NAMES.add("SIRCA");
      BAMBOO_DISABLED_PROJECT_NAMES.add("MavlinkInterface");
      BAMBOO_DISABLED_PROJECT_NAMES.add("ReinforcementLearning");
      BAMBOO_DISABLED_PROJECT_NAMES.add("ARDrone");
      BAMBOO_DISABLED_PROJECT_NAMES.add("ARToolkit");
      
      INDEPENDENT_PROJECT_NAMES.add("IHMCRealtime");
      
      THIRD_PARTY_PACKAGE_NAMES.add("jp");
      THIRD_PARTY_PACKAGE_NAMES.add("multiNyAR");
      THIRD_PARTY_PACKAGE_NAMES.add("it");
   }
   
   public static Map<String, AgileTestingProject> loadATProjects(AgileTestingProjectLoader atProjectLoader, Path rootProjectPath)
   {
      Map<String, AgileTestingProject> allATProjects = loadAllProjectsInWorkspace(rootProjectPath);
      Map<String, AgileTestingProject> selectedATProjects = new LinkedHashMap<>();
      
      for (AgileTestingProject atProject : allATProjects.values())
      {
         if (atProjectLoader.meetsCriteria(atProject))
         {
            atProjectLoader.setupProject(atProject);
            selectedATProjects.put(atProject.getRawProjectName(), atProject);
         }
      }
      
      return selectedATProjects;
   }
   
   public static Map<String, AgileTestingProject> loadAllProjectsInWorkspace(Path rootProjectPath)
   {
      Map<String, AgileTestingProject> nameToProjectMap = new LinkedHashMap<>();
      
      for (Path projectPath : SourceTools.findAllProjectPaths(rootProjectPath))
      {
         nameToProjectMap.put(new AgileTestingProject(projectPath).getRawProjectName(), new AgileTestingProject(projectPath));
      }
      
      return nameToProjectMap;
   }
   
   public static Map<AgileTestingProject, SortedSet<AgileTestingProject>> loadProjectDependencyMap(boolean recursivelyPopulate)
   {
      Map<AgileTestingProject, SortedSet<AgileTestingProject>> projectDependencies = new LinkedHashMap<>();
      
      Map<String, AgileTestingProject> gradleProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return Files.exists(atProject.getPath().resolve(BUILD_GRADLE_NAME)) && Files.exists(atProject.getPath().resolve(SOURCE_FOLDER_NAME));
         }
      }, SourceTools.getWorkspacePath());
      
      for (AgileTestingProject gradleProject : gradleProjects.values())
      {
         projectDependencies.put(gradleProject, new TreeSet<AgileTestingProject>());
         Path dependenciesGradlePath = gradleProject.getPath().resolve(BUILD_GRADLE_NAME);
         if (!Files.exists(dependenciesGradlePath))
            continue;
         List<String> lines = FileTools.readAllLines(dependenciesGradlePath, DefaultExceptionHandler.PRINT_STACKTRACE);
         
         for (String line : lines)
         {
            if (line.matches(COMPILE_PROJECT_REGEX) || line.matches(TEST_COMPILE_PROJECT_REGEX) || line.matches(RUNTIME_COMPILE_PROJECT_REGEX))
            {
               if (line.contains("$"))
                  continue;
               String projectName;
               if (line.contains("'"))
                  projectName = line.split("'")[1].substring(1);
               else
                  projectName = line.split("\"")[1].substring(1);
               AgileTestingProject correspondingProject = gradleProjects.get(projectName);
               if (correspondingProject == null)
                  PrintTools.error(projectName + " in " + dependenciesGradlePath + " could not be found!");
               else
                  projectDependencies.get(gradleProject).add(correspondingProject);
            }
         }
      }
      
      if (recursivelyPopulate)
      {
         for (AgileTestingProject bambooEnabledProject : gradleProjects.values())
         {
            addSubDependencies(projectDependencies.get(bambooEnabledProject), projectDependencies.get(bambooEnabledProject), projectDependencies);
         }
      }
      
      return projectDependencies;
   }
   
   private static void addSubDependencies(SortedSet<AgileTestingProject> dependencySet, SortedSet<AgileTestingProject> subDependencySet, Map<AgileTestingProject, SortedSet<AgileTestingProject>> projectDependencies)
   {
      SortedSet<AgileTestingProject> copiedSet = new TreeSet<>(subDependencySet);
      for (AgileTestingProject atProject : copiedSet)
      {
         dependencySet.addAll(projectDependencies.get(atProject));
         addSubDependencies(dependencySet, projectDependencies.get(atProject), projectDependencies);
      }
   }

   public static Map<String, AgileTestingClassPath> mapAllClassNamesToClassPaths(Path workspacePath)
   {
      Map<String, Path> javaNameToPathMap = SourceTools.mapAllClassNamesToPaths(workspacePath);
      Map<String, AgileTestingClassPath> javaNameToClassPathMap = new LinkedHashMap<>();
      
      for (String className : javaNameToPathMap.keySet())
      {
         javaNameToClassPathMap.put(className, new AgileTestingClassPath(javaNameToPathMap.get(className)));
      }
      
      return javaNameToClassPathMap;
   }
   
   public static AgileTestingClassPath getFirstMatchInMap(Map<String, AgileTestingClassPath> nameToPathMap, String className)
   {
      for (String key : nameToPathMap.keySet())
      {
         if (key.matches(".*\\." + className + "$"))
         {
            return nameToPathMap.get(key);
         }
      }
      
      return null;
   }
   
   protected static boolean projectIsBambooEnabled(String projectName)
   {
      return !BAMBOO_DISABLED_PROJECT_NAMES.contains(projectName);
   }

   public static Path getRunAllTestSuitesPath(Map<String, AgileTestingClassPath> nameToPathMap)
   {
      AgileTestingClassPath runAllFastTestSuitesPath = nameToPathMap.get(RUN_ALL_FAST_TEST_SUITES_CLASS_NAME);
      
      if (runAllFastTestSuitesPath == null)
      {
         PrintTools.error(SourceTools.class, "Please create a file RunAllFastTestSuites.java in RunAllBambooTestSuites");
         return null;
      }
      
      return runAllFastTestSuitesPath.getPath().getParent();
   }
}