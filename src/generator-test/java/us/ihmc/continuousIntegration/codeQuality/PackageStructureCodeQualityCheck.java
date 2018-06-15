package us.ihmc.continuousIntegration.codeQuality;

import static org.junit.Assert.assertTrue;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.commons.nio.BasicPathVisitor;
import us.ihmc.commons.nio.PathTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;

public class PackageStructureCodeQualityCheck
{
   public static void main(String[] args)
   {
      PackageStructureCodeQualityCheck packageStructureCodeQualityCheck = new PackageStructureCodeQualityCheck();
      packageStructureCodeQualityCheck.testAllPackagesStartWithUsDotIhmc();
      packageStructureCodeQualityCheck.testNoEmptyPackagesInWorkspace();
   }

   public void testNoEmptyPackagesInWorkspace()
   {
      final List<String> emptyPackages = new ArrayList<>();

      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
      }, SourceTools.getWorkspacePath());
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {
         checkForEmptyPackages(emptyPackages, bambooEnabledProject, "src");
         checkForEmptyPackages(emptyPackages, bambooEnabledProject, "test");
      }
      
      assertTrue("Packages are empty: " + emptyPackages, emptyPackages.isEmpty());
   }

   private void checkForEmptyPackages(final List<String> emptyPackages, AgileTestingProject bambooEnabledProject, String folder)
   {
      PathTools.walkRecursively(bambooEnabledProject.getPath().resolve(folder), new BasicPathVisitor()
      {
         @Override
         public FileVisitResult visitPath(Path path, PathType pathType)
         {
            if (pathType == PathType.DIRECTORY)
            {
               if (path.toFile().list().length < 1 && !path.endsWith("generatedTestSuites") && !path.endsWith("testResources") && !path.endsWith("resources"))
               {
                  PrintTools.error("Empty package in src: " + path);
                  emptyPackages.add(path.toString());
               }
            }
            
            return FileVisitResult.CONTINUE;
         }
      });
   }
   
   public void testAllPackagesStartWithUsDotIhmc()
   {
	   Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
	      @Override
	      public boolean meetsCriteria(AgileTestingProject atProject)
	      {
	         return atProject.isBambooEnabled();
	      }
      }, SourceTools.getWorkspacePath());
	   
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {         
         PackageCheckingPathVisitor pathCheckingFileVisitor = new PackageCheckingPathVisitor();
         
         PathTools.walkFlat(bambooEnabledProject.getPath().resolve("src"), pathCheckingFileVisitor);
         PathTools.walkFlat(bambooEnabledProject.getPath().resolve("test"), pathCheckingFileVisitor);
         
         assertTrue("Misplaced files: " + pathCheckingFileVisitor.misplacedFiles, pathCheckingFileVisitor.misplacedFiles.isEmpty());
         assertTrue("Missing .ihmc package: " + pathCheckingFileVisitor.missingIHMCPackage, pathCheckingFileVisitor.missingIHMCPackage.isEmpty());
         if (!bambooEnabledProject.getRawProjectName().equals("MegaBots"))
         {
            assertTrue("Misplaced packages: " + pathCheckingFileVisitor.misplacedPackages, pathCheckingFileVisitor.misplacedPackages.isEmpty());
         }
      }
   }

   private final class PackageCheckingPathVisitor extends BasicPathVisitor
   {
      public List<Path> misplacedFiles = new ArrayList<>();
      public List<Path> missingIHMCPackage = new ArrayList<>();
      public List<Path> misplacedPackages = new ArrayList<>();
      
      @Override
      public FileVisitResult visitPath(Path path, PathType pathType)
      {
         if (pathType == PathType.FILE)
         {
            PrintTools.warn("File shouldn't be here: " + path);
            misplacedFiles.add(path);
         }
         
         if (pathType == PathType.DIRECTORY)
         {
            if (path.getFileName().toString().equals("us"))
            {
              
               if (path.toFile().list().length < 1)
               {
                  PrintTools.warn(".ihmc package should be here: " + path);
                  missingIHMCPackage.add(path);
               }
               else
               {
                  PathTools.walkFlat(path, new BasicPathVisitor()
                  {
                     @Override
                     public FileVisitResult visitPath(Path path, PathType pathType)
                     {
                        if (!path.getFileName().toString().equals("ihmc"))
                        {
                           PrintTools.warn("Package should not be here: " + path);
                           misplacedPackages.add(path);
                        }
                        else
                        {
                           PathTools.walkFlat(path, new BasicPathVisitor()
                           {
                              int count = 0;
                              Path firstPath = null;
                              
                              @Override
                              public FileVisitResult visitPath(Path path, PathType pathType)
                              {
                                 ++count;
                                 
                                 if (count == 2)
                                    PrintTools.warn("us.ihmc contains multiple packages: " + firstPath);
                                 
                                 if (count > 1)
                                    PrintTools.warn("us.ihmc contains multiple packages: " + path);
                                 else
                                    firstPath = path;

                                 return FileVisitResult.CONTINUE;
                              }
                           });
                        }
                        
                        return FileVisitResult.CONTINUE;
                     }
                  });
               }
            }
            else
            {
               PrintTools.warn("Package shouldn't be here or has bad name: " + path);
               misplacedPackages.add(path);
            }
         }
         
         return FileVisitResult.CONTINUE;
      }
   }
}
