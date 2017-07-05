package us.ihmc.continuousIntegration.generator;

import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import us.ihmc.commons.PrintTools;

public class LocalTestSuiteGenerator
{
   public static void main(String[] args)
   {
      Set<String> projectNameWhiteList = new TreeSet<>();

      projectNameWhiteList.add("IHMCContinuousIntegrationCoreTools");

      BambooTestSuiteGenerator bambooTestSuiteGenerator = new BambooTestSuiteGenerator();
      bambooTestSuiteGenerator.createForStandaloneProject(Paths.get("."));
      bambooTestSuiteGenerator.generateSpecificTestSuites(projectNameWhiteList);
      bambooTestSuiteGenerator.printAllStatistics();

      PrintTools.info(BambooTestSuiteGenerator.class,
                      "Finished generating test suites. Please refresh your IDE's file system and commit the new generated test suites. You must also confirm bamboo plans match the generated test suites!");
   }
}
