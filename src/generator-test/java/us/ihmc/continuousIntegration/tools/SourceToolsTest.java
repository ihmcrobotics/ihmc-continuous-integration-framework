package us.ihmc.continuousIntegration.tools;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.tools.SourceTools;

public class SourceToolsTest
{
	@ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testDerivePackageFromPath()
   {      
      Path path = getThisTestPath();
      
      String packageFromPath = SourceTools.derivePackageFromPath(path);
      
      PrintTools.info(this, "Derived package: " + packageFromPath + " Actual package: " + SourceToolsTest.class.getPackage().getName());
      
      assertTrue("Derived package not equal to actual package.", packageFromPath.equals(SourceToolsTest.class.getPackage().getName()));
   }
   
	@ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testDeriveClassNameFromPath()
   {      
      Path path = getThisTestPath();
      
      String classNameFromPath = SourceTools.deriveClassNameFromPath(path);
      
      PrintTools.info(this, "Derived className: " + classNameFromPath + " Actual className: " + SourceToolsTest.class.getName());
      
      assertTrue("Derived className not equal to actual className.", classNameFromPath.equals(SourceToolsTest.class.getName()));
   }
   
   private Path getThisTestPath()
   {
      return SourceTools.derivePathFromClass(Paths.get("src"), SourceToolsTest.class);
   }
   
	@ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testExtractSuperClassSimpleName()
   {
      Class<?> subjectClass = FakeChildClass.class;
      Class<?> parentClass = FakeParentClass.class;

      List<String> allLines = FileTools.readAllLines(Paths.get("generatorTestResources/us/ihmc/continuousIntegration/tools/sourceToolsTest/FakeChildClass.java.fake"),
                                                     DefaultExceptionHandler.PRINT_STACKTRACE);

      String superClassName = SourceTools.extractSuperClassSimpleName(allLines, subjectClass.getSimpleName());
      
      PrintTools.info(this, "Super class name: " + superClassName);
      
      assertTrue("Parent class name does not match extracted super class name.", superClassName.equals(parentClass.getSimpleName()));
   }
}
