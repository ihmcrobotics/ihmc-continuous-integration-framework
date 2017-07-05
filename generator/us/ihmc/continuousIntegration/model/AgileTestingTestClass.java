package us.ihmc.continuousIntegration.model;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;
import us.ihmc.continuousIntegration.AgileTestingJavaParserTools;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

public class AgileTestingTestClass
{
   private final AgileTestingClassPath classPath;
   private final String testClassSimpleName;
   
   // For code quality tests
   private int numberOfLocalUnitTests = 0;
   private int numberOfTimeouts = 0;
   private int numberOfDeployableTestMethods = 0;
   private int numberOfEstimatedDurations = 0;
   
   private int numberOfUnitTests = 0;
   private boolean isExtendingTest = false;
   private boolean isAbstractTest = false;
   
   private final Map<String, AgileTestingClassPath> nameToPathMap;
   private final SortedSet<IntegrationCategory> allPlanTargets = new TreeSet<>();
   private final SortedSet<IntegrationCategory> classPlanTargets = new TreeSet<>();
   private final Map<String, AgileTestingTestMethod> atTestMethods = new HashMap<>();
   private final Map<IntegrationCategory, Double> testPlanDurations = new HashMap<>();
   private double totalDuration = 0.0;
   
   private final Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> methodAnnotationMap;
   private final Pair<CompilationUnit, ClassOrInterfaceDeclaration> pair;

   public AgileTestingTestClass(AgileTestingClassPath classPath, Map<String, AgileTestingClassPath> nameToPathMap)
   {
      this.nameToPathMap = nameToPathMap;
      this.classPath = classPath;
      this.testClassSimpleName = classPath.getSimpleName();
      
      methodAnnotationMap = new HashMap<>();
      pair = AgileTestingJavaParserTools.parseForTestAnnotations(classPath, methodAnnotationMap);
      
      loadTestClass();
   }

   private void loadTestClass()
   {
      parseLocalMethodsForCodeQualityTests();
      addPlanTargetsFromClassAnnotationFields();
      
      isExtendingTest = AgileTestingJavaParserTools.classOrInterfaceExtends(pair.getRight());
      isAbstractTest = ModifierSet.isAbstract(pair.getRight().getModifiers());
      
      totalDuration += addAllEstimatedDurationsInFile(pair, methodAnnotationMap);
      if (isExtendingTest)
      {
         totalDuration += addAllDurationsOfSuperClassTests(pair, methodAnnotationMap);
      }
      
      if (allPlanTargets.isEmpty())
      {
         allPlanTargets.add(IntegrationCategory.defaultCategory);
      }
   }

   private double addAllEstimatedDurationsInFile(Pair<CompilationUnit, ClassOrInterfaceDeclaration> pair, Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> methodAnnotationMap)
   {
      double totalDuration = 0.0;
      for (MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>> method : methodAnnotationMap.values())
      {
         if (!atTestMethods.containsKey(method.getLeft().getName()))
         {
            numberOfUnitTests++;

            if (method.getRight().containsKey(ContinuousIntegrationTest.class.getSimpleName()))
            {
               Map<String, MemberValuePair> deployableTestAnnotationFields = AgileTestingJavaParserTools.mapAnnotationFields(method.getRight().get(ContinuousIntegrationTest.class.getSimpleName()));

               Double methodDuration = Double.valueOf(((StringLiteralExpr) deployableTestAnnotationFields.get(AgileTestingAnnotationTools.ESTIMATED_DURATION).getValue()).getValue());

               atTestMethods.put(method.getLeft().getName(), new AgileTestingTestMethod(method.getLeft().getName(), methodDuration, pair.getRight().getName()));
               totalDuration += methodDuration;

               if (deployableTestAnnotationFields.containsKey(AgileTestingAnnotationTools.METHOD_TARGETS))
               {
                  Expression expression = deployableTestAnnotationFields.get(AgileTestingAnnotationTools.METHOD_TARGETS).getValue();
                  if (expression instanceof FieldAccessExpr)
                  {
                     FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) expression;
                     IntegrationCategory integrationCategory = IntegrationCategory.fromString(fieldAccessExpr.getField());
                     addDurationToMap(integrationCategory, methodDuration);
                     atTestMethods.get(method.getLeft().getName()).addCategory(integrationCategory);
                  }
                  else if (expression instanceof ArrayInitializerExpr)
                  {
                     ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) expression;

                     for (Expression arrayField : arrayInitializerExpr.getValues())
                     {
                        FieldAccessExpr arrayFieldAccessExpr = (FieldAccessExpr) arrayField;
                        IntegrationCategory integrationCategory = IntegrationCategory.fromString(arrayFieldAccessExpr.getField());
                        addDurationToMap(integrationCategory, methodDuration);
                        atTestMethods.get(method.getLeft().getName()).addCategory(integrationCategory);
                     }
                  }
               }
               else
               {
                  for (IntegrationCategory classTarget : classPlanTargets)
                  {
                     addDurationToMap(classTarget, methodDuration);
                     atTestMethods.get(method.getLeft().getName()).addCategory(classTarget);
                  }

                  if (atTestMethods.get(method.getLeft().getName()).getCategories().isEmpty())
                  {
                     addDurationToMap(IntegrationCategory.defaultCategory, methodDuration);
                     atTestMethods.get(method.getLeft().getName()).addCategory(IntegrationCategory.defaultCategory);
                  }
               }
               
               for (IntegrationCategory integrationCategory : atTestMethods.get(method.getLeft().getName()).getCategories())
               {
                  allPlanTargets.add(integrationCategory);
               }
            }
         }
      }

      return totalDuration;
   }
   
   private void addDurationToMap(IntegrationCategory integrationCategory, double duration)
   {
      if (!testPlanDurations.containsKey(integrationCategory))
      {
         testPlanDurations.put(integrationCategory, 0.0);
      }
      
      testPlanDurations.put(integrationCategory, testPlanDurations.get(integrationCategory) + duration);
   }
   
   private double addAllDurationsOfSuperClassTests(Pair<CompilationUnit, ClassOrInterfaceDeclaration> pair, Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> methodAnnotationMap)
   {
      AgileTestingClassPath superClassPath = AgileTestingTools.getFirstMatchInMap(nameToPathMap, pair.getRight().getExtends().get(0).getName());
      
      if (superClassPath == null)
         return 0.0;
      
      Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> superClassMethodAnnotationMap = new HashMap<>();
      Pair<CompilationUnit, ClassOrInterfaceDeclaration> superClassPair = AgileTestingJavaParserTools.parseForTestAnnotations(superClassPath, superClassMethodAnnotationMap);
      
      boolean isExtendingSuperClass = AgileTestingJavaParserTools.classOrInterfaceExtends(superClassPair.getRight());
      
      if (isExtendingSuperClass)
      {
         return addAllEstimatedDurationsInFile(superClassPair, superClassMethodAnnotationMap) + addAllDurationsOfSuperClassTests(superClassPair, superClassMethodAnnotationMap);
      }
      else
      {
         return addAllEstimatedDurationsInFile(superClassPair, superClassMethodAnnotationMap);
      }
   }

   private void addPlanTargetsFromClassAnnotationFields()
   {
      for (AnnotationExpr annotationExpr : pair.getRight().getAnnotations())
      {
         if (annotationExpr.getName().getName().equals(ContinuousIntegrationPlan.class.getName()) || annotationExpr.getName().getName().equals(ContinuousIntegrationPlan.class.getSimpleName()))
         {
            Map<String, MemberValuePair> classAnnotationFields = AgileTestingJavaParserTools.mapAnnotationFields(annotationExpr);
            
            if (classAnnotationFields.containsKey(AgileTestingAnnotationTools.CLASS_TARGETS))
            {
               Expression expression = classAnnotationFields.get(AgileTestingAnnotationTools.CLASS_TARGETS).getValue();
               if (expression instanceof FieldAccessExpr)
               {
                  FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) expression;
                  classPlanTargets.add(IntegrationCategory.fromString(fieldAccessExpr.getField()));
               }
               else if (expression instanceof ArrayInitializerExpr)
               {
                  ArrayInitializerExpr arrayInitializerExpr = (ArrayInitializerExpr) expression;
                  
                  for (Expression arrayField : arrayInitializerExpr.getValues())
                  {
                     FieldAccessExpr arrayFieldAccessExpr = (FieldAccessExpr) arrayField;
                     classPlanTargets.add(IntegrationCategory.fromString(arrayFieldAccessExpr.getField()));
                  }
               }
            }
         }
      }
   }

   private void parseLocalMethodsForCodeQualityTests()
   {
      for (MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>> method : methodAnnotationMap.values())
      {
         numberOfLocalUnitTests++;
         
         Map<String, MemberValuePair> testAnnotationFields = AgileTestingJavaParserTools.mapAnnotationFields(method.getRight().get(Test.class.getSimpleName()));
         if (testAnnotationFields.containsKey(AgileTestingAnnotationTools.TIMEOUT))
         {
            numberOfTimeouts++;
         }
         
         if (method.getRight().containsKey(ContinuousIntegrationTest.class.getSimpleName()))
         {
            numberOfDeployableTestMethods++;
            
            Map<String, MemberValuePair> deployableTestAnnotationFields = AgileTestingJavaParserTools.mapAnnotationFields(method.getRight().get(ContinuousIntegrationTest.class.getSimpleName()));

            if (deployableTestAnnotationFields.containsKey(AgileTestingAnnotationTools.ESTIMATED_DURATION))
            {
               numberOfEstimatedDurations++;
            }
         }
      }
   }

   public String getTestClassSimpleName()
   {
      return testClassSimpleName;
   }
   
   public String getTestClassName()
   {
      return classPath.getClassName();
   }

   public int getNumberOfUnitTests()
   {
      return numberOfLocalUnitTests;
   }

   public Collection<AgileTestingTestMethod> getTestMethods()
   {
      return atTestMethods.values();
   }

   public int getNumberOfTimeouts()
   {
      return numberOfTimeouts;
   }

   public int getNumberOfDeployableTestMethods()
   {
      return numberOfDeployableTestMethods;
   }
   
   public int getNumberOfEstimatedDurations()
   {
      return numberOfEstimatedDurations;
   }

   public boolean isExtendingTest()
   {
      return isExtendingTest;
   }

   public boolean isValidUnitTest()
   {
      return numberOfUnitTests > 0 && !isAbstractTest;
   }

   public boolean isAbstractTest()
   {
      return isAbstractTest;
   }

   public double getTotalDurationForTarget(IntegrationCategory integrationCategory)
   {
      if (!testPlanDurations.containsKey(integrationCategory))
      {
         return 0.0;
      }
      else
      {
         return testPlanDurations.get(integrationCategory);
      }
   }
   
   public double getTotalDurationForAllPlans()
   {
      return totalDuration;
   }

   public SortedSet<IntegrationCategory> getTestPlanTargets()
   {
      return allPlanTargets;
   }

   public Path getPath()
   {
      return classPath.getPath();
   }
}
