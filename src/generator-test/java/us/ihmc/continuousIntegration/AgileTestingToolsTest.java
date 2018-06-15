package us.ihmc.continuousIntegration;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.EXCLUDE)
public class AgileTestingToolsTest
{
   @ContinuousIntegrationTest(estimatedDuration = 10.6)
   @Test(timeout = 53247)
   public void testFindAndLoadAllBambooEnabledProjects()
   {
      Path projectPath = Paths.get("resources");
      final Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(projectPath);
      
      Map<String, AgileTestingProject> allBambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
         
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadSourceClasses();
            atProject.loadTestCloud(nameToPathMap);
         }
      }, projectPath);
      
      for (AgileTestingProject atProject : allBambooEnabledProjects.values())
      {
         PrintTools.info("Project: " + atProject.getRawProjectName());
      }
      
      assertTrue("Didn't load any projects. Current directory: " + Paths.get("").toAbsolutePath().toString(), !allBambooEnabledProjects.isEmpty());
   }
}
