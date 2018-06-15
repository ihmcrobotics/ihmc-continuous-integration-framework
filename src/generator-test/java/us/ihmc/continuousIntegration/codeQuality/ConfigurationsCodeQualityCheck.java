package us.ihmc.continuousIntegration.codeQuality;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.commons.nio.BasicPathVisitor;
import us.ihmc.commons.nio.PathTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingTools;

public class ConfigurationsCodeQualityCheck
{
   private final static boolean AUTO_FIX = false;

   public static void main(String[] args)
   {
      ConfigurationsCodeQualityCheck configurationsCodeQualityCheck = new ConfigurationsCodeQualityCheck();
      configurationsCodeQualityCheck.testNoIntelliJIMLFilesInProjects();
      configurationsCodeQualityCheck.testNoEclipseProjectFilesInProjects();
   }

   public void testNoIntelliJIMLFilesInProjects()
   {
      final List<Path> imlFiles = new ArrayList<>();

      for (AgileTestingProject atProject : AgileTestingTools.loadAllProjectsInWorkspace(SourceTools.getWorkspacePath()).values())
      {
         PathTools.walkFlat(atProject.getPath(), new BasicPathVisitor()
         {
            @Override
            public FileVisitResult visitPath(Path path, PathType pathType)
            {
               if (path.getFileName().toString().endsWith(".iml"))
               {
                  imlFiles.add(path);
               }

               return FileVisitResult.CONTINUE;
            }
         });
      }

      for (Path imlFile : imlFiles)
      {
         PrintTools.warn("IML File found: " + imlFile);

         if (AUTO_FIX)
         {
            try
            {
               Files.delete(imlFile);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }

      assertEquals("IML files exist: " + imlFiles, 0, imlFiles.size());
   }
	
	/**
	 * Meant for running only on Bamboo.
	 */
   public void testNoEclipseProjectFilesInProjects()
   {
      final List<Path> dotProjectFiles = new ArrayList<>();

      for (AgileTestingProject atProject : AgileTestingTools.loadAllProjectsInWorkspace(SourceTools.getWorkspacePath()).values())
      {
         PathTools.walkFlat(atProject.getPath(), new BasicPathVisitor()
         {
            @Override
            public FileVisitResult visitPath(Path path, PathType pathType)
            {
               if (path.getFileName().toString().endsWith(".project"))
               {
                  dotProjectFiles.add(path);
               }

               return FileVisitResult.CONTINUE;
            }
         });
      }

      for (Path dotProjectFile : dotProjectFiles)
      {
         PrintTools.warn("Eclipse .project file found: " + dotProjectFile);

         if (AUTO_FIX)
         {
            try
            {
               Files.delete(dotProjectFile);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }

      assertEquals(".project files exist: " + dotProjectFiles, 0, dotProjectFiles.size());
   }
}
