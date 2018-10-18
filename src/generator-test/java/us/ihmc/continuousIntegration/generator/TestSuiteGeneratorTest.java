package us.ihmc.continuousIntegration.generator;

import org.junit.Test;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.continuousIntegration.GradleSubBuildTools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TestSuiteGeneratorTest
{
   @Test(timeout = 60000)
   public void testGenerateTestSuites()
   {
      String projectName = "example-project-one";
      Path testSuiteFilePath = Paths.get("builds/" + projectName + "/src/test/java/us/ihmc/generatedTestSuites/ExampleProjectOneAFastTestSuite.java");
      FileTools.deleteQuietly(testSuiteFilePath);
      String output = GradleSubBuildTools.runGradleTask("generateTestSuites", projectName);
      assertTrue(output.contains("BUILD SUCCESSFUL"));
      assertTrue(Files.exists(testSuiteFilePath));
   }

   @Test(timeout = 60000)
   public void testMissingTimeouts()
   {
      String projectName = "missing-timeouts";
      Path testSuiteFilePath = Paths.get("builds/" + projectName + "/src/test/java/us/ihmc/generatedTestSuites/MissingTimeoutsAFastTestSuite.java");
      FileTools.deleteQuietly(testSuiteFilePath);
      String output = GradleSubBuildTools.runGradleTask("generateTestSuites", projectName);
      assertTrue(output.contains("BUILD FAILED") && output.contains("are missing JUnit timeouts"));
      assertTrue(Files.exists(testSuiteFilePath));
   }

   @Test(timeout = 60000)
   public void testAbstractTests()
   {
      String projectName = "abstract-tests";
      Path testSuiteFilePath = Paths.get("builds/" + projectName + "/src/test/java/us/ihmc/generatedTestSuites/AbstractTestsAFastTestSuite.java");
      FileTools.deleteQuietly(testSuiteFilePath);
      String output = GradleSubBuildTools.runGradleTask("generateTestSuites", projectName);
      assertTrue(output.contains("BUILD SUCCESSFUL"));
      assertTrue(output.contains("us.ihmc.AbstractTest, 1 @Test(s), abstract"));
      assertTrue(output.contains("us.ihmc.ExtendingTest, 0 @Test(s), extends AbstractTest"));
      assertTrue(Files.exists(testSuiteFilePath));
   }
}
