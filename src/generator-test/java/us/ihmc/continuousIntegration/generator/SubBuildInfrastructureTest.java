package us.ihmc.continuousIntegration.generator;

import org.junit.Test;
import us.ihmc.continuousIntegration.GradleSubBuildTools;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class SubBuildInfrastructureTest
{
   @Test(timeout = 60000)
   public void testGradleIsInstalled()
   {
      System.out.println("Gradle install location: " + GradleSubBuildTools.gradleExe);
      System.out.println("basicProject location: " + Paths.get(GradleSubBuildTools.buildsDir + "example-project-one").toAbsolutePath().toString());

      String output = GradleSubBuildTools.runGradleTask("--version", "example-project-one");
      assertTrue(output.contains("Gradle ") && output.contains("Build time") && output.contains("JVM:"));
   }

   @Test(timeout = 60000)
   public void testBasicProjectSucceeds()
   {
      String output = GradleSubBuildTools.runGradleTask("compileJava", "example-project-one");

      assertTrue(output.contains("BUILD SUCCESSFUL"));
   }

   @Test(timeout = 60000)
   public void testBrokenProjectFails()
   {
      String output = GradleSubBuildTools.runGradleTask("compileJava", "brokenProject");

      assertTrue(output.contains("BUILD FAILED"));
   }
}
