package us.ihmc.continuousIntegration.model;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.apache.commons.lang3.tuple.MutablePair;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.nio.BasicPathVisitor;
import us.ihmc.commons.nio.PathTools;
import us.ihmc.continuousIntegration.AgileTestingJavaParserTools;
import us.ihmc.continuousIntegration.IntegrationCategory;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;
import us.ihmc.continuousIntegration.tools.SourceTools;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.*;

public class AgileTestingProjectTestCloud
{
   private final Path projectPath;
   
   private final List<AgileTestingTestClass> bambooTestClasses;
   
   private final Map<IntegrationCategory, List<Path>> unbalancedTestPathMap = new HashMap<IntegrationCategory, List<Path>>();
   
   private final Map<IntegrationCategory, AgileTestingLoadBalancedPlan> loadBalancedPlans = new HashMap<IntegrationCategory, AgileTestingLoadBalancedPlan>();
   private final Map<IntegrationCategory, AgileTestingTestSuiteFile> singletonTestSuiteFiles = new HashMap<IntegrationCategory, AgileTestingTestSuiteFile>();
   
   private final Map<String, AgileTestingClassPath> nameToPathMap;
   private final AgileTestingProject bambooEnabledProject;
   
   public AgileTestingProjectTestCloud(AgileTestingProject bambooEnabledProject, Map<String, AgileTestingClassPath> nameToPathMap)
   {
      this.nameToPathMap = nameToPathMap;
      this.projectPath = bambooEnabledProject.getPath();
      this.bambooEnabledProject = bambooEnabledProject;
      
      for (IntegrationCategory bambooPlanType : IntegrationCategory.loadBalancedCategories)
      {
         loadBalancedPlans.put(bambooPlanType, new AgileTestingLoadBalancedPlan(bambooPlanType, bambooEnabledProject));
      }
      
      for (IntegrationCategory unbalancedCategory : IntegrationCategory.unbalancedCategories)
      {
         unbalancedTestPathMap.put(unbalancedCategory, new ArrayList<Path>());
      }
      
      bambooTestClasses = loadAllBambooTestClasses();
      
      alphabetizeTestClasses();
      sortTestClassesIntoPlans();
   }

   private void alphabetizeTestClasses()
   {
      Collections.sort(bambooTestClasses, new Comparator<AgileTestingTestClass>()
      {
         @Override
         public int compare(AgileTestingTestClass testClass1, AgileTestingTestClass testClass2)
         {
            return testClass1.getTestClassName().compareToIgnoreCase(testClass2.getTestClassName());
         }
      });
   }

   private void sortTestClassesIntoPlans()
   {
      for (AgileTestingTestClass bambooTestClass : bambooTestClasses)
      {
         if (bambooTestClass.isValidUnitTest())
         {
            for (IntegrationCategory classCategory : bambooTestClass.getTestPlanTargets())
            {
               if (classCategory.isIncludedAndNotLoadBalanced())
               {
                  unbalancedTestPathMap.get(classCategory).add(bambooTestClass.getPath());
               }
               else if (classCategory.isLoadBalanced())
               {
                  loadBalancedPlans.get(classCategory).add(bambooTestClass);
               }
            }
         }
      }
   }
   
   private List<AgileTestingTestClass> loadAllBambooTestClasses()
   {
      final List<AgileTestingTestClass> bambooTestClasses = new ArrayList<>();

      final SortedSet<AgileTestingTestClass> testClassPaths = new TreeSet<>(Comparator.comparing(AgileTestingTestClass::getTestClassName));

      // Add paths ending in *Test.java
      for (Path path : PathTools.findAllPathsRecursivelyThatMatchRegex(projectPath.resolve(SourceTools.TEST_SOURCE_FOLDER.getMavenPath()),
                                                                       AgileTestingAnnotationTools.TEST_CLASS_FILENAME_REGEX))
      {
         testClassPaths.add(new AgileTestingTestClass(new AgileTestingClassPath(path), nameToPathMap));
      }

      // Add everything with an active @Test annotation
      PathTools.walkRecursively(projectPath.resolve(SourceTools.TEST_SOURCE_FOLDER.getMavenPath()), new BasicPathVisitor()
      {
         @Override
         public FileVisitResult visitPath(Path path, PathType pathType)
         {
            if (pathType == PathType.FILE)
            {
               Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> methodAnnotationMap = new HashMap<>();
               AgileTestingClassPath classPath = new AgileTestingClassPath(path);
               AgileTestingJavaParserTools.parseForTestAnnotations(classPath, methodAnnotationMap);

               for (MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>> method : methodAnnotationMap.values())
               {
                  if (method.getRight().containsKey("Test"))
                  {
                     testClassPaths.add(new AgileTestingTestClass(classPath, nameToPathMap));
                  }
               }
            }
            return FileVisitResult.CONTINUE;
         }
      });

      for (AgileTestingTestClass testPath : testClassPaths)
      {
         bambooTestClasses.add(testPath);
      }
      
      return bambooTestClasses;
   }

   public void generateAllTestSuites()
   {
      for (IntegrationCategory unbalancedCategory : IntegrationCategory.unbalancedCategories)
      {
         generateSingletonTestSuite(unbalancedTestPathMap.get(unbalancedCategory), unbalancedCategory);
      }
      
      for (AgileTestingLoadBalancedPlan bambooLoadBalancedPlan : loadBalancedPlans.values())
      {
         bambooLoadBalancedPlan.generateTestSuites();
      }
   }

   private void generateSingletonTestSuite(List<Path> testPaths, IntegrationCategory bambooPlanType)
   {
      if (!testPaths.isEmpty())
      {
         String shortName = bambooEnabledProject.getModifiedProjectName() + bambooPlanType.getName();
         String testSuiteSimpleName = shortName + "TestSuite";
         String packageName = bambooEnabledProject.getPackageName();
         
         AgileTestingTestSuiteFile bambooTestSuiteFile = new AgileTestingTestSuiteFile(getSingletonTestSuitePath(bambooPlanType), bambooPlanType, shortName, 0.0);
         singletonTestSuiteFiles.put(bambooPlanType, bambooTestSuiteFile);
         
         PrintTools.info(this, "Generating: " + "(? min) " + shortName);
         
         bambooTestSuiteFile.generateTestSuite(testSuiteSimpleName, packageName, testPaths);
      }
   }
   
   public Path getSingletonTestSuitePath(IntegrationCategory bambooPlanType)
   {
      return bambooEnabledProject.getGeneratedTestSuitesDirectory().resolve(bambooEnabledProject.getModifiedProjectName() + bambooPlanType.getName() + "TestSuite.java");
   }
   
   public Map<IntegrationCategory, AgileTestingLoadBalancedPlan> getLoadBalancedPlans()
   {
      return loadBalancedPlans;
   }

   public Map<IntegrationCategory, AgileTestingTestSuiteFile> getSingletonTestSuiteFiles()
   {
      return singletonTestSuiteFiles;
   }

   public List<AgileTestingTestClass> getTestClasses()
   {
      return bambooTestClasses;
   }

   public Map<String, AgileTestingClassPath> getNameToPathMap()
   {
      return nameToPathMap;
   }
}
