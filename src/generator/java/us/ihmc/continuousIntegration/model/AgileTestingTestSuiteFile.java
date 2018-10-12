package us.ihmc.continuousIntegration.model;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import us.ihmc.log.LogTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.WriteOption;
import us.ihmc.continuousIntegration.ContinuousIntegrationSuite;
import us.ihmc.continuousIntegration.ContinuousIntegrationSuite.ContinuousIntegrationSuiteCategory;
import us.ihmc.continuousIntegration.IntegrationCategory;

public class AgileTestingTestSuiteFile
{   
   private final Path path;
   
   private IntegrationCategory integrationCategory;
   private String planShortName;
   private double duration;
   
   public AgileTestingTestSuiteFile(Path path, IntegrationCategory integrationCategory, String planShortName, double duration)
   {
      this.path = path;
      this.integrationCategory = integrationCategory;
      this.planShortName = planShortName;
      this.duration = duration;
   }
   
   public void generateTestSuite(String testSuiteSimpleName, String packageName, List<Path> pathsToPutInTestSuite)
   {
      PrintWriter writer = FileTools.newPrintWriter(path, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      
      writer.print(createTestSuite(testSuiteSimpleName, packageName, pathsToPutInTestSuite));
      
      writer.close();
   }
   
   private String createTestSuite(String testSuiteClassName, String packageName, List<Path> pathsToPutInTestSuite)
   {
      String content = "";

      content += "package " + packageName + ";\n\n";

      content += "import org.junit.runner.RunWith;\n";
      content += "import org.junit.runners.Suite.SuiteClasses;\n\n";
      
      content += "import " + ContinuousIntegrationSuite.class.getName() + ";\n";
      content += "import " + ContinuousIntegrationSuite.class.getName() + "." + ContinuousIntegrationSuiteCategory.class.getSimpleName() + ";\n";
      content += "import " + IntegrationCategory.class.getName() + ";\n\n";

//      if (SourceModificationTools.projectIsIndependent(path))
//         content += "//";
//      content += "import us.ihmc.utilities.code.unitTesting.runner.BambooTestSuiteRunner;\n\n";
      
      content += "/** WARNING: AUTO-GENERATED FILE. DO NOT MAKE MANUAL CHANGES TO THIS FILE. **/\n";
      content += "@RunWith(" + ContinuousIntegrationSuite.class.getSimpleName() + ".class)\n";
      content += "@" + ContinuousIntegrationSuiteCategory.class.getSimpleName() + "(" + IntegrationCategory.class.getSimpleName() + "." + integrationCategory.name() + ")\n";
      content += "@SuiteClasses\n";
      content += "({\n";
      
      for (int i = 0; i < pathsToPutInTestSuite.size(); i++)
      {
         String className = new AgileTestingClassPath(pathsToPutInTestSuite.get(i)).getClassName();
         
         if (!className.isEmpty())
         {
            content += "   " + className + ".class";
            
            if (i < pathsToPutInTestSuite.size() - 1)
               content += ",";
            
            content += "\n";
         }
         else
         {
            LogTools.debug("Produced empty className: " + pathsToPutInTestSuite.get(i).toString());
         }
      }

      content += "})\n\n";

      content += "public class " + testSuiteClassName + "\n";
      content += "{\n";


      content += "   public static void main(String[] args)\n";
      content += "   {\n\n";
//      if (SourceModificationTools.projectIsIndependent(path))
//         content += "//";
//         content += "      new BambooTestSuiteRunner(" + testSuiteClassName + ".class);\n";
      content += "   }\n";

      content += "}\n";

      return content;
   }

   public String getPlanShortName()
   {
      return planShortName;
   }

   public Path getPath()
   {
      return path;
   }

   public double getDuration()
   {
      return duration;
   }
}
