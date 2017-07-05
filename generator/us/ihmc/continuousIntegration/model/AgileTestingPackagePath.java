package us.ihmc.continuousIntegration.model;

import java.nio.file.Path;

import us.ihmc.continuousIntegration.tools.SourceTools;

public class AgileTestingPackagePath
{
   private final Path path;
   
   private String packageName;
   
   public AgileTestingPackagePath(Path path)
   {
      this.path = path;
      
      packageName = derivePackageName();
   }

   private String derivePackageName()
   {
      return SourceTools.derivePackageFromPath(path);
   }

   public Path getPath()
   {
      return path;
   }

   public String getPackageName()
   {
      return packageName;
   }
}
