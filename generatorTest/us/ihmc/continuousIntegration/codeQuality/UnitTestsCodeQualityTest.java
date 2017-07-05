package us.ihmc.continuousIntegration.codeQuality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import us.ihmc.commons.PrintTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.model.AgileTestingTestClass;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.tools.SourceTools.SourceFolder;
import us.ihmc.continuousIntegration.AgileTestingJavaParserTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.HEALTH)
public class UnitTestsCodeQualityTest
{
   @ContinuousIntegrationTest(estimatedDuration = 16.5)
   @Test(timeout = 83000)
   public void testEveryClassWithAUnitTestIsCorrectlyNamedAndInTestFolder()
   {
      ArrayList<String> classesWithBadNames = new ArrayList<>();
      ArrayList<String> classesNotInTestFolder = new ArrayList<>();

      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadSourceClasses();
         }

         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
      }, SourceTools.getWorkspacePath());
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {
         for (AgileTestingClassPath classPath : bambooEnabledProject.getAllClasses())
         {
            Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> testMethodAnnotationMap = new HashMap<>();
            AgileTestingJavaParserTools.parseForTestAnnotations(classPath, testMethodAnnotationMap);

            if (!testMethodAnnotationMap.isEmpty())
            {
               if (!classPath.getSimpleName().endsWith(AgileTestingAnnotationTools.TEST_CLASS_NAME_POSTFIX))
               {
                  classesWithBadNames.add(classPath.getSimpleName());

                  PrintTools.warn("Bad name: " + classPath.getClassName());
               }

               if (classPath.getSourceFolder() != SourceFolder.test)
               {
                  classesNotInTestFolder.add(classPath.getSimpleName());

                  PrintTools.warn("Not in test folder: " + classPath.getClassName());
               }
            }
         }
      }

      assertTrue("Test class names should match *Test. Offenders: " + classesWithBadNames, classesWithBadNames.size() < 1);
      assertTrue("Test classes should be in 'test' source folder and in the same path as their application class. Offenders: " + classesNotInTestFolder,
                 classesNotInTestFolder.size() < 1);
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.3, categoriesOverride = IntegrationCategory.EXCLUDE)
   @Test(timeout = 30000)
   public void testEveryTestClassIsInSamePackageAndProjectAsItsApplicationClass()
   {
      HashSet<String> badlyNamedWhiteSet = new HashSet<>();
      badlyNamedWhiteSet.add("us.ihmc.agileTesting.bambooRestConnector.Actions");
      badlyNamedWhiteSet.add("us.ihmc.agileTesting.bambooRestConnector.Link");
      badlyNamedWhiteSet.add("jama.Matrix");
      badlyNamedWhiteSet.add("us.ihmc.concurrent.ConcurrentCopier");
      badlyNamedWhiteSet.add("us.ihmc.concurrent.ConcurrentRingBuffer");
      badlyNamedWhiteSet.add("us.ihmc.util.NativeLibraryLoader");
      badlyNamedWhiteSet.add("us.ihmc.yoboticsBiped.yoboticsBiped.GenCon.VirtualModelController");
      badlyNamedWhiteSet.add("us.ihmc.caril.phase2.bots.controllers.PDController");
      badlyNamedWhiteSet.add("us.ihmc.sensorProcessing.bubo.construct.Octree");
      badlyNamedWhiteSet.add("us.ihmc.caril.phase2.interactableObjects.Wrench");
      badlyNamedWhiteSet.add(AgileTestingClassPath.class.getName());
      List<AgileTestingClassPath> testClassesWithAMatchingApplicationClass = new ArrayList<>();
      Map<String, String> testClassesNotInSamePackageAsApplicationClass = new LinkedHashMap<>();
      Map<String, String> testClassesNotInSameProjectAsApplicationClass = new LinkedHashMap<>();

      Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getWorkspacePath());
      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadSourceClasses();
         }

         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
      }, SourceTools.getWorkspacePath());
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {
         for (AgileTestingClassPath classInTestSourceFolder : bambooEnabledProject.getTestClasses())
         {
            if (classInTestSourceFolder.getSimpleName().endsWith("Test"))
            {
               String classInTestSourceFolderSimpleName = classInTestSourceFolder.getSimpleName();
               String potentialApplicationClassMatchingSimpleName = classInTestSourceFolderSimpleName.substring(0,
                                                                                                                classInTestSourceFolderSimpleName.length() - 4);

               for (AgileTestingClassPath classInWorkspace : nameToPathMap.values())
               {
                  if (badlyNamedWhiteSet.contains(classInWorkspace.getClassName()))
                     continue;

                  if (classInWorkspace.getSimpleName().equals(potentialApplicationClassMatchingSimpleName))
                  {
                     testClassesWithAMatchingApplicationClass.add(classInTestSourceFolder);

                     if (!classInTestSourceFolder.getPackageName().equals(classInWorkspace.getPackageName()))
                     {
                        testClassesNotInSamePackageAsApplicationClass.put(classInTestSourceFolder.getClassName(), classInWorkspace.getClassName());

                        System.out.println(classInTestSourceFolder.getClassName() + "\n" + classInWorkspace.getClassName() + "\n");
                     }

                     if (!classInTestSourceFolder.getProjectName().equals(classInWorkspace.getProjectName()))
                     {
                        testClassesNotInSameProjectAsApplicationClass.put(classInTestSourceFolder.getClassName(), classInWorkspace.getClassName());

                        System.out.println(classInTestSourceFolder.getProjectName() + ": " + classInTestSourceFolder.getClassName() + "\n"
                              + classInWorkspace.getProjectName() + ": " + classInWorkspace.getClassName() + "\n");
                     }
                  }
               }
            }
         }
      }

      PrintTools.info("Test classes with matching application classes: " + testClassesWithAMatchingApplicationClass.size());

      assertEquals("Tests classes are not in same package as application class: " + testClassesNotInSamePackageAsApplicationClass, 0,
                   testClassesNotInSamePackageAsApplicationClass.values().size());
      assertEquals("Tests classes are not in same project as application class: " + testClassesNotInSameProjectAsApplicationClass, 0,
                   testClassesNotInSameProjectAsApplicationClass.values().size());
   }

   /**
    * Shouldn't run on Bamboo until green.
    */
   @ContinuousIntegrationTest(estimatedDuration = 0.1, categoriesOverride = IntegrationCategory.EXCLUDE)
   @Test(timeout = 30000)
   public void testEveryTestClassHasAMatchingApplicationClass()
   {
      List<AgileTestingClassPath> testClassesWithoutAMatchingApplicationClass = new ArrayList<>();

      Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getWorkspacePath());
      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadSourceClasses();
         }

         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
      }, SourceTools.getWorkspacePath());
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {
         for (AgileTestingClassPath classInTestSourceFolder : bambooEnabledProject.getTestClasses())
         {
            if (classInTestSourceFolder.getSimpleName().endsWith("Test"))
            {
               String classInTestSourceFolderSimpleName = classInTestSourceFolder.getSimpleName();
               String potentialApplicationClassMatchingSimpleName = classInTestSourceFolderSimpleName.substring(0,
                                                                                                                classInTestSourceFolderSimpleName.length() - 4);

               boolean foundAMatch = false;

               for (AgileTestingClassPath classInWorkspace : nameToPathMap.values())
               {
                  if (classInWorkspace.getSimpleName().equals(potentialApplicationClassMatchingSimpleName))
                  {
                     foundAMatch = true;
                     break;
                  }
               }

               if (!foundAMatch)
               {
                  testClassesWithoutAMatchingApplicationClass.add(classInTestSourceFolder);
                  System.out.println(classInTestSourceFolder.getClassName());
               }
            }
         }
      }

      PrintTools.info("Test classes without matching application classes: " + testClassesWithoutAMatchingApplicationClass.size());

      assertEquals("Tests classes do not have a matching application class: " + testClassesWithoutAMatchingApplicationClass, 0,
                   testClassesWithoutAMatchingApplicationClass.size());
   }

   @ContinuousIntegrationTest(estimatedDuration = 5.1)
   @Test(timeout = 30000)
   public void testEveryUnitTestHasATimeoutAndEstimatedDuration()
   {
      List<String> classesWithMissingTimeouts = new ArrayList<>();
      List<String> classesWithMissingEstimatedDurations = new ArrayList<>();

      final Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getWorkspacePath());
      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.loadTestCloud(nameToPathMap);
         }

         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
      }, SourceTools.getWorkspacePath());
      for (AgileTestingProject bambooEnabledProject : bambooEnabledProjects.values())
      {
         if (bambooEnabledProject.isIndependent())
            continue;

         for (AgileTestingTestClass bambooTestClass : bambooEnabledProject.getTestCloud().getTestClasses())
         {
            int numberOfTests = bambooTestClass.getNumberOfUnitTests();
            int numberOfTimeouts = bambooTestClass.getNumberOfTimeouts();
            int numberOfEstimatedDurations = bambooTestClass.getNumberOfEstimatedDurations();

            int missingTimeouts = numberOfTests - numberOfTimeouts;
            int missingEstimatedDurations = numberOfTests - numberOfEstimatedDurations;

            if (numberOfTests > numberOfTimeouts)
            {
               classesWithMissingTimeouts.add(bambooTestClass.getTestClassSimpleName() + ":" + (numberOfTests - numberOfTimeouts));

               PrintTools.warn(this, "Missing " + missingTimeouts + " timeout(s): " + bambooTestClass.getTestClassSimpleName());
            }

            if (numberOfTests > numberOfEstimatedDurations)
            {
               classesWithMissingEstimatedDurations.add(bambooTestClass.getTestClassSimpleName() + ":" + (numberOfTests - numberOfEstimatedDurations));

               PrintTools.warn(this, "Missing " + missingEstimatedDurations + " estimatedDuration parameter(s): " + bambooTestClass.getTestClassSimpleName());
            }
         }
      }

//      Assert.assertEquals(classesWithMissingTimeouts.toString(), 0, classesWithMissingTimeouts.size());
      Assert.assertEquals(classesWithMissingEstimatedDurations.toString(), 0, classesWithMissingEstimatedDurations.size());
   }

   @ContinuousIntegrationTest(estimatedDuration = 0.1, categoriesOverride = {IntegrationCategory.HEALTH, IntegrationCategory.FAST,
         IntegrationCategory.SLOW, IntegrationCategory.FLAKY, IntegrationCategory.IN_DEVELOPMENT, IntegrationCategory.UI, IntegrationCategory.VIDEO})
   @Test(timeout = 30000)
   public void testIsRunningOnBamboo()
   {
      assertTrue("ContinuousIntegrationTools.isRunningOnContinuousIntegrationServer() is false. Set RUNNING_ON_CONTINUOUS_INTEGRATION_SERVER=true",
                 ContinuousIntegrationTools.isRunningOnContinuousIntegrationServer());
   }
}
