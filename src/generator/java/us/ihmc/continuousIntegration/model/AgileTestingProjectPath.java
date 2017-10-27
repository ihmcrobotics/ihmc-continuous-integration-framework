package us.ihmc.continuousIntegration.model;

import java.nio.file.Path;

public class AgileTestingProjectPath
{
   private final Path path;
   private final String projectName;
   
   public AgileTestingProjectPath(Path path)
   {
      this.path = path;
      
      projectName = deriveProjectName();
   }

   private String deriveProjectName()
   {
      return path.getFileName().toString();
   }

   public Path getPath()
   {
      return path;
   }

   public String getProjectName()
   {
      return projectName;
   }
}
