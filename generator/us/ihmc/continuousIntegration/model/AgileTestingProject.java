package us.ihmc.continuousIntegration.model;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import us.ihmc.commons.nio.BasicPathVisitor;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.PathTools;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.continuousIntegration.StandaloneProjectConfiguration;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.tools.SourceTools.SourceFolder;

public class AgileTestingProject implements Comparable<AgileTestingProject>
{
   private final StandaloneProjectConfiguration configuration;
   private final Path pathToProject;

   private final String rawProjectName;
   private final String modifiedProjectName;
   private final String primaryPackageName;

   private final Path generatedTestSuitesDirectory;

   private final boolean isIndependent;
   private final boolean isBambooEnabled;

   private AgileTestingProjectTestCloud testCloud;

   private List<AgileTestingClassPath> applicationClasses;
   private List<AgileTestingClassPath> testClasses;
   private List<AgileTestingClassPath> allClasses;

   private long lineCount = -1;

   public AgileTestingProject(Path pathToProject)
   {
      this(StandaloneProjectConfiguration.defaultConfiguration(pathToProject));
   }
   
   public AgileTestingProject(StandaloneProjectConfiguration configuration)
   {
      this.configuration = configuration;
      this.pathToProject = configuration.getProjectPath();

      this.rawProjectName = configuration.getPascalCasedName();
      modifiedProjectName = WordUtils.capitalize(rawProjectName.replaceAll("_.*", ""));

      generatedTestSuitesDirectory = findGeneratedTestSuitesDirectory();

      boolean directoryCreationFailed;
      try
      {
         FileTools.ensureDirectoryExists(generatedTestSuitesDirectory);
         directoryCreationFailed = false;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         directoryCreationFailed = true;
      }

      if (generatedTestSuitesDirectory != null)
         primaryPackageName = findPackageName();
      else
         primaryPackageName = null;

      isIndependent = AgileTestingTools.INDEPENDENT_PROJECT_NAMES.contains(rawProjectName);
      isBambooEnabled = !AgileTestingTools.BAMBOO_DISABLED_PROJECT_NAMES.contains(rawProjectName) && generatedTestSuitesDirectory != null
            && !directoryCreationFailed;
   }

   public void countLines()
   {
      if (allClasses == null)
      {
         loadSourceClasses();
      }

      lineCount = 0;
      for (AgileTestingClassPath atClassPath : allClasses)
      {
         atClassPath.countLines();
         lineCount += atClassPath.getLineCount();
      }
   }

   public void loadTestCloud(Map<String, AgileTestingClassPath> nameToPathMap)
   {
      testCloud = new AgileTestingProjectTestCloud(this, nameToPathMap);
   }

   private Path findGeneratedTestSuitesDirectory()
   {
      return pathToProject.resolve(SourceFolder.test.name()).resolve(SourceFolder.src.name()).resolve(AgileTestingTools.GENERATED_TEST_SUITES_DIRECTORY_PACKAGE);
   }

   private String findPackageName()
   {
      String packagePath = new AgileTestingPackagePath(generatedTestSuitesDirectory).getPackageName();

      return packagePath;
   }

   public void cleanGeneratedDirectory()
   {
      PathTools.walkRecursively(generatedTestSuitesDirectory, new BasicPathVisitor()
      {
         @Override
         public FileVisitResult visitPath(Path file, PathType pathType)
         {
            if (pathType == PathType.FILE && !file.getFileName().toString().equals(".generated"))
            {
               try
               {
                  Files.delete(file);
               }
               catch (IOException e)
               {
                  e.printStackTrace();
               }
            }

            return FileVisitResult.CONTINUE;
         }
      });
   }

   public void generateAllTestSuites()
   {
      cleanGeneratedDirectory();

      testCloud.generateAllTestSuites();
   }

   public double getFastTotalDuration()
   {
      double totalDuration = 0.0;

      for (double duration : testCloud.getLoadBalancedPlans().get(IntegrationCategory.FAST).getLoadBalancedDurations().values())
      {
         totalDuration += duration;
      }

      return totalDuration;
   }

   public void loadSourceClasses()
   {
      applicationClasses = new ArrayList<>();
      testClasses = new ArrayList<>();
      allClasses = new ArrayList<>();

      List<Path> paths = PathTools.findAllPathsRecursivelyThatMatchRegex(pathToProject, SourceTools.ALL_JAVA_FILES_REGEX);

      for (Path path : paths)
      {
         AgileTestingClassPath classPath = new AgileTestingClassPath(path);

         if (classPath.getSourceFolder() != null)
         {
            allClasses.add(classPath);

            if (classPath.getSourceFolder() == SourceFolder.src)
            {
               applicationClasses.add(classPath);
            }
            else if (classPath.getSourceFolder() == SourceFolder.test)
            {
               testClasses.add(classPath);
            }
         }
      }
   }

   public AgileTestingProjectTestCloud getTestCloud()
   {
      return testCloud;
   }
   
   public StandaloneProjectConfiguration getConfiguration()
   {
      return configuration;
   }

   public String getPackageName()
   {
      return primaryPackageName;
   }

   public boolean isIndependent()
   {
      return isIndependent;
   }

   public Path getPath()
   {
      return pathToProject;
   }

   public Path getGeneratedTestSuitesDirectory()
   {
      return generatedTestSuitesDirectory;
   }

   public List<AgileTestingClassPath> getApplicationClasses()
   {
      return applicationClasses;
   }

   public List<AgileTestingClassPath> getTestClasses()
   {
      return testClasses;
   }

   public List<AgileTestingClassPath> getAllClasses()
   {
      return allClasses;
   }

   @Override
   public int compareTo(AgileTestingProject bambooEnabledProject)
   {
      return getRawProjectName().compareTo(bambooEnabledProject.getRawProjectName());
   }

   public boolean isBambooEnabled()
   {
      return isBambooEnabled;
   }

   public String getRawProjectName()
   {
      return rawProjectName;
   }

   public String getModifiedProjectName()
   {
      return modifiedProjectName;
   }

   public long getLineCount()
   {
      return lineCount;
   }
}
