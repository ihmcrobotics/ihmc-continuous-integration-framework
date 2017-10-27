package us.ihmc.continuousIntegration.model;

import java.nio.file.Path;

import us.ihmc.commons.nio.PathTools;
import us.ihmc.continuousIntegration.lineCounting.JavaLineCounter;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.tools.SourceFolder;

public class AgileTestingClassPath
{
   public static final boolean DEBUG = false;
   
   private final Path path;
   private final String className;
   private final String simpleName;
   private final SourceFolder sourceFolder;
   private final String packageName;
   private final String projectName;
   private long lineCount = -1;
   
   public AgileTestingClassPath(Path path)
   {
      this.path = path;
      
      className = SourceTools.deriveClassNameFromPath(path);
      simpleName = PathTools.getBaseName(path);
      sourceFolder = SourceTools.deriveSourceFolderFromPath(path);
      packageName = SourceTools.derivePackageFromPath(path);
      projectName = SourceTools.deriveProjectNameFromPath(path);
   }
   
   public AgileTestingClassPath(Path path, String projectName)
   {
      this.path = path;
      
      className = SourceTools.deriveClassNameFromPath(path);
      simpleName = PathTools.getBaseName(path);
      sourceFolder = SourceTools.deriveSourceFolderFromPath(path);
      packageName = SourceTools.derivePackageFromPath(path);
      this.projectName = projectName;
   }
   
   public void countLines()
   {
      lineCount = new JavaLineCounter(false, false, false).countLines(path);
   }

   public String getClassName()
   {
      return className;
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   public Path getPath()
   {
      return path;
   }

   public SourceFolder getSourceFolder()
   {
      return sourceFolder;
   }

   public String getPackageName()
   {
      return packageName;
   }

   public String getProjectName()
   {
      return projectName;
   }

   public long getLineCount()
   {
      return lineCount;
   }
}
