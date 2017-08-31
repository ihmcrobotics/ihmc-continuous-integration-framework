package us.ihmc.continuousIntegration.tools;

public class SourceFolder
{
   private String name;
   private String mavenPath;

   public SourceFolder(String name)
   {
      this.name = name;
      this.mavenPath = SourceTools.SOURCE_SETS_DIRECTORY_NAME + "/" + name + "/" + SourceTools.JAVA_SOURCE_DIRECTORY_NAME;
   }

   public String name()
   {
      return name;
   }

   public String getMavenPath()
   {
      return mavenPath;
   }
}
