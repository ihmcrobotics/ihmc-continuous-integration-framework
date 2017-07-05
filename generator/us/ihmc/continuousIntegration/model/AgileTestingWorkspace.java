package us.ihmc.continuousIntegration.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import us.ihmc.continuousIntegration.IntegrationCategory;

public class AgileTestingWorkspace
{
   private final Map<String, AgileTestingProject> nameToProjectMap;

   private Map<IntegrationCategory, ArrayList<AgileTestingTestSuiteFile>> sortedTestSuitesByDurationMap;
   private ArrayList<AgileTestingTestMethod> allTestsSortedByDuration;
   private ArrayList<AgileTestingTestClass> allTestClassesSortedByDuration;

   public AgileTestingWorkspace(Map<String, AgileTestingProject> nameToProjectMap)
   {
      this.nameToProjectMap = nameToProjectMap;
   }
   
   public void buildMaps()
   {
      buildDurationToTestSuiteMap();
      buildAllTestSortedByDurationMap();
      buildAllTestClassSortedByDurationMap();
   }

   private void buildAllTestClassSortedByDurationMap()
   {
      allTestClassesSortedByDuration = new ArrayList<>();
      
      for (AgileTestingProject project : nameToProjectMap.values())
      {
         for (AgileTestingTestClass testClass : project.getTestCloud().getTestClasses())
         {
            allTestClassesSortedByDuration.add(testClass);
         }
      }
      
      Collections.sort(allTestClassesSortedByDuration, new Comparator<AgileTestingTestClass>()
      {
         @Override
         public int compare(AgileTestingTestClass o1, AgileTestingTestClass o2)
         {
            if (o1.getTotalDurationForAllPlans() > o2.getTotalDurationForAllPlans())
               return -1;
            else if (o1.getTotalDurationForAllPlans() == o2.getTotalDurationForAllPlans())
               return 0;
            else
               return 1;
         }
      });
   }

   private void buildDurationToTestSuiteMap()
   {
      sortedTestSuitesByDurationMap = new HashMap<>();
      
      for (IntegrationCategory testSuiteTarget : IntegrationCategory.includedCategories)
      {
         sortedTestSuitesByDurationMap.put(testSuiteTarget, new ArrayList<AgileTestingTestSuiteFile>());
      }
      
      for (AgileTestingProject project : nameToProjectMap.values())
      {
         for (IntegrationCategory testSuiteTarget : IntegrationCategory.includedCategories)
         {
            if (testSuiteTarget.isLoadBalanced())
            {
               sortedTestSuitesByDurationMap.get(testSuiteTarget).addAll(project.getTestCloud().getLoadBalancedPlans().get(testSuiteTarget).getTestSuiteFiles());
            }
            else
            {
               sortedTestSuitesByDurationMap.get(testSuiteTarget).addAll(project.getTestCloud().getSingletonTestSuiteFiles().values());
            }
         }
      }
      
      for (IntegrationCategory category : IntegrationCategory.includedCategories)
      {
         Collections.sort(sortedTestSuitesByDurationMap.get(category), new Comparator<AgileTestingTestSuiteFile>()
         {
            @Override
            public int compare(AgileTestingTestSuiteFile o1, AgileTestingTestSuiteFile o2)
            {
               if (o1.getDuration() > o2.getDuration())
                  return -1;
               else if (o1.getDuration() == o2.getDuration())
                  return 0;
               else
                  return 1;
            }
         });
      }
   }
   
   private void buildAllTestSortedByDurationMap()
   {
      allTestsSortedByDuration = new ArrayList<>();

      for (AgileTestingProject project : nameToProjectMap.values())
      {
         for (AgileTestingTestClass testClass : project.getTestCloud().getTestClasses())
         {
            allTestsSortedByDuration.addAll(testClass.getTestMethods());
         }
      }

      Collections.sort(allTestsSortedByDuration, new Comparator<AgileTestingTestMethod>()
      {
         @Override
         public int compare(AgileTestingTestMethod o1, AgileTestingTestMethod o2)
         {
            if (o1.getDuration() > o2.getDuration())
               return -1;
            else if (o1.getDuration() == o2.getDuration())
               return 0;
            else
               return 1;
         }
      });
   }

   public Map<IntegrationCategory, ArrayList<AgileTestingTestSuiteFile>> getSortedTestSuitesByDurationMap()
   {
      return sortedTestSuitesByDurationMap;
   }

   public ArrayList<AgileTestingTestMethod> getAllTestsSortedByDuration()
   {
      return allTestsSortedByDuration;
   }

   public ArrayList<AgileTestingTestClass> getAllTestClassesSortedByDuration()
   {
      return allTestClassesSortedByDuration;
   }
}
