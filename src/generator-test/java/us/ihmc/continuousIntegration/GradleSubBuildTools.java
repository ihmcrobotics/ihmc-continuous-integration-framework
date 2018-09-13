package us.ihmc.continuousIntegration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class GradleSubBuildTools
{
   public static String buildsDir = "builds/";
   public static String gradleCommand = SystemUtils.IS_OS_WINDOWS ? "gradlew.bat" : "gradlew";
   public static String gradleExe = Paths.get(buildsDir + gradleCommand).toAbsolutePath().toString();

   public static String runCommand(String command, Path workingDir)
   {
      try
      {
         String[] parts = command.split("\\s");
         Process proc = new ProcessBuilder(parts).directory(workingDir.toFile()).redirectOutput(ProcessBuilder.Redirect.PIPE)
                                                 .redirectError(ProcessBuilder.Redirect.PIPE).start();

         proc.waitFor(60, TimeUnit.MINUTES);
         String output = IOUtils.toString(proc.getInputStream(), StandardCharsets.UTF_8) + IOUtils.toString(proc.getErrorStream(), StandardCharsets.UTF_8);
         System.out.println(output);
         return output;
      }
      catch (IOException | InterruptedException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public static String runGradleTask(String command, String project)
   {
      System.out.println("Running " + gradleExe + " in " + Paths.get(buildsDir + project).toAbsolutePath());
      if (command == null || command.isEmpty())
         return runCommand(gradleExe, Paths.get("builds/" + project).toAbsolutePath());
      else
         return runCommand(gradleExe + " " + command, Paths.get(buildsDir + project).toAbsolutePath());
   }
}
