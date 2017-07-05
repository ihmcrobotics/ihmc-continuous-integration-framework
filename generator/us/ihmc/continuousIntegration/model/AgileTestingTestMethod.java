package us.ihmc.continuousIntegration.model;

import java.util.SortedSet;
import java.util.TreeSet;

import us.ihmc.continuousIntegration.IntegrationCategory;

public class AgileTestingTestMethod
{
   private final String methodName;
   private final double duration;
   private final String testClassSimpleName;
   private SortedSet<IntegrationCategory> categories = new TreeSet<>();
   
   public AgileTestingTestMethod(String methodName, double duration, String testClassSimpleName)
   {
      this.methodName = methodName;
      this.duration = duration;
      this.testClassSimpleName = testClassSimpleName;
   }
   
   public void addCategory(IntegrationCategory category)
   {
      categories.add(category);
   }
   
   public SortedSet<IntegrationCategory> getCategories()
   {
      return categories;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public double getDuration()
   {
      return duration;
   }

   public String getTestClassSimpleName()
   {
      return testClassSimpleName;
   }
}
