package us.ihmc.continuousIntegration.tools;

import java.nio.file.Paths;

public class PrintClassNamesToFile
{
   public PrintClassNamesToFile()
   {
      for (String key : SourceTools.mapAllClassNamesToPaths(Paths.get("../IHMCUtilities/src")).keySet())
      {
         System.out.println(key);
      }
   }

   public static void main(String[] args)
   {
      new PrintClassNamesToFile();
   }
}
