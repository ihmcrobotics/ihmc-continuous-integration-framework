package us.ihmc.continuousIntegration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

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
   private static Map<String, String> projectNameReplacements = new HashMap<>();
   private static Map<String, String> hyphenationReplacements = new HashMap<>();
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
      
      projectNameReplacements.put("ihmc", "IHMC");
      projectNameReplacements.put("vt", "VT");
      projectNameReplacements.put("javafx", "JavaFX");
      projectNameReplacements.put("jmonkey", "JMonkey");
      projectNameReplacements.put("ros", "ROS");
      projectNameReplacements.put("ros1", "ROS1");
      projectNameReplacements.put("ros2", "ROS2");
      projectNameReplacements.put("lla", "LLA");
      projectNameReplacements.put("devops", "DevOps");
      projectNameReplacements.put("ejml", "EJML");
      projectNameReplacements.put("icub", "ICub");
      projectNameReplacements.put("dds", "DDS");
      projectNameReplacements.put("rtps", "RTPS");
      projectNameReplacements.put("sri", "SRI");
      projectNameReplacements.put("ethercat", "EtherCAT");
      projectNameReplacements.put("imu", "IMU");
      projectNameReplacements.put("icp", "ICP");
      projectNameReplacements.put("it", "IT");
      projectNameReplacements.put("ipxe", "IPXE");
      projectNameReplacements.put("aot", "AOT");
      projectNameReplacements.put("joctomap", "JOctoMap");
      projectNameReplacements.put("mav", "MAV");
      projectNameReplacements.put("megabots", "MegaBots");
      projectNameReplacements.put("megabot", "MegaBot");
      
      hyphenationReplacements.put("-i-h-m-c-", "-ihmc-");
      hyphenationReplacements.put("-v-t-", "-ihmc-");
      hyphenationReplacements.put("-3-d-", "-3d-");
      hyphenationReplacements.put("-java-f-x-", "-javafx-");
      hyphenationReplacements.put("-l-l-a-", "-lla-");
      hyphenationReplacements.put("-j-monkey-", "-jmonkey-");
      hyphenationReplacements.put("-r-o-s-", "-ros-");
      hyphenationReplacements.put("-r-o-s-1-", "-ros1-");
      hyphenationReplacements.put("-r-o-s-2-", "-ros2-");
      hyphenationReplacements.put("-dev-ops-", "-devops-");
      hyphenationReplacements.put("-e-j-m-l-", "-ejml-");
      hyphenationReplacements.put("-i-cub-", "-icub-");
      hyphenationReplacements.put("-d-d-s-", "-dds-");
      hyphenationReplacements.put("-r-t-p-s-", "-rtps-");
      hyphenationReplacements.put("-s-r-i-", "-sri-");
      hyphenationReplacements.put("-ether-c-a-t-", "-ethercat-");
      hyphenationReplacements.put("-i-m-u-", "-imu-");
      hyphenationReplacements.put("-i-c-p-", "-icp-");
      hyphenationReplacements.put("-i-t", "-it-");
      hyphenationReplacements.put("-i-p-x-e-", "-ipxe-");
      hyphenationReplacements.put("-a-o-t-", "-aot-");
      hyphenationReplacements.put("-j-octo-map-", "-joctomap-");
      hyphenationReplacements.put("-m-a-v-", "-mav-");
      hyphenationReplacements.put("-mega-bots-", "-megabots-");
      hyphenationReplacements.put("-mega-bot-", "-megabot-");
   }
   
   public static String hyphenatedToPascalCased(String hyphenated)
   {
      String[] split = hyphenated.split("-");
      String pascalCased = "";
      for (String section : split)
      {
         if (projectNameReplacements.containsKey(section))
         {
            pascalCased += projectNameReplacements.get(section);
         }
         else
         {
            pascalCased += StringUtils.capitalize(section);
         }
      }
      return pascalCased;
   }
   
   public static String pascalCasedToHyphenated(String pascalCased)
   {
      String hyphenated = pascalCasedToPrehyphenated(pascalCased);
      
      hyphenated = hyphenated.substring(1, hyphenated.length() - 1);

      return hyphenated;
   }
   
   public static String pascalCasedToHyphenatedWithoutJob(String pascalCased)
   {
      for (IntegrationCategory integrationCategory : IntegrationCategory.values)
      {
         if (pascalCased.endsWith(integrationCategory.getName()))
         {
            pascalCased = pascalCased.substring(0, pascalCased.length() - integrationCategory.name().length());
            
            if (integrationCategory.isLoadBalanced())
               pascalCased = pascalCased.substring(0, pascalCased.length() - 1);
               
            break;
         }
      }
      
      String hyphenated = pascalCasedToPrehyphenated(pascalCased);
      
      hyphenated = hyphenated.substring(1, hyphenated.length() - 1);

      return hyphenated;
   }
   
   private static String pascalCasedToPrehyphenated(String pascalCased)
   {
      List<String> parts = new ArrayList<>();
      String part = "";
      
      for (int i = 0; i < pascalCased.length(); i++)
      {
         String character = String.valueOf(pascalCased.charAt(i));
         if (StringUtils.isAllUpperCase(character) || StringUtils.isNumeric(character))
         {
            if (!part.isEmpty())
            {
               parts.add(part.toLowerCase());
            }
            part = character;
         }
         else
         {
            part += character;
         }
      }
      if (!part.isEmpty())
      {
         parts.add(part.toLowerCase());
      }

      String hyphenated = "";
      for (int i = 0; i < parts.size(); i++)
      {
         hyphenated += '-';
         hyphenated += parts.get(i);
      }
      hyphenated += '-';
      
      for (String replacement : hyphenationReplacements.keySet())
      {
         hyphenated = hyphenated.replaceAll(replacement, hyphenationReplacements.get(replacement));
      }
      
      return hyphenated;
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
   
   public static Map<String, AgileTestingClassPath> mapAllClassNamesToClassPaths(StandaloneProjectConfiguration configuration)
   {
      Map<String, Path> javaNameToPathMap = SourceTools.mapAllClassNamesToPaths(configuration.getProjectPath());
      Map<String, AgileTestingClassPath> javaNameToClassPathMap = new LinkedHashMap<>();
      
      for (String className : javaNameToPathMap.keySet())
      {
         javaNameToClassPathMap.put(className, new AgileTestingClassPath(javaNameToPathMap.get(className), configuration.getPascalCasedName()));
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
