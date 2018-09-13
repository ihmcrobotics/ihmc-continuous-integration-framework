package us.ihmc.continuousIntegration.model;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgileTestingStandaloneWorkspaceTest
{
   @Test(timeout = 30000)
   public void testDisableJobPatternMatching()
   {
      String matchString = AgileTestingStandaloneWorkspace.formMatchingPattern("Valkyrie");

      assertTrue("ValkyrieDVideo".matches(matchString));
      assertTrue("ValkyrieDADHUSAFast".matches(matchString));
      assertTrue("ValkyrieCompile".matches(matchString));
      assertFalse("ValkyrieAAAHealth".matches(matchString));
      assertFalse("ValkyrieLManual".matches(matchString));
      assertTrue("ValkyrieLXFlaky".matches(matchString));
   }

   @Test(timeout = 30000)
   public void testGradleIsInstalled()
   {
      System.out.println("Gradle install location: " + gradleExe);
      System.out.println("basicProject location: " + Paths.get("builds/example-project-one").toAbsolutePath().toString());

      String output = runGradleTask("--version", "example-project-one");
      assertTrue(output.contains("Gradle ") && output.contains("Build time") && output.contains("JVM:"));
   }

   @Test(timeout = 30000)
   public void testBasicProjectSucceeds()
   {
      String output = runGradleTask("compileJava", "example-project-one");

      assertTrue(output.contains("BUILD SUCCESSFUL"));
   }

   @Test(timeout = 30000)
   public void testBrokenProjectFails()
   {
      String output = runGradleTask("compileJava", "brokenProject");

      assertTrue(output.contains("BUILD FAILED"));
   }

   public String runCommand(String command, Path workingDir)
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

   public static String gradleCommand = SystemUtils.IS_OS_WINDOWS ? "gradlew.bat" : "gradlew";
   public static String gradleExe = Paths.get("builds/" + gradleCommand).toAbsolutePath().toString();

   public String runGradleTask(String command, String project)
   {
      System.out.println("Running " + gradleExe + " in " + Paths.get("builds/" + project).toAbsolutePath());
      if (command == null || command.isEmpty())
         return runCommand(gradleExe, Paths.get("builds/" + project).toAbsolutePath());
      else
         return runCommand(gradleExe + " " + command, Paths.get("builds/" + project).toAbsolutePath());
   }
}
