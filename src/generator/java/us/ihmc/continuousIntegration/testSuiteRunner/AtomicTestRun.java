package us.ihmc.continuousIntegration.testSuiteRunner;

public class AtomicTestRun
{
   private final String className;
   private final String classSimpleName;
   private final String methodName;
   private final double duration;
   private final String status;

   public AtomicTestRun(Class<?> clazz, String methodName, double duration, String status)
   {
      this.className = clazz.getName();
      this.classSimpleName = clazz.getSimpleName();
      this.methodName = methodName;
      this.duration = duration;
      this.status = status;
   }
   
   public AtomicTestRun(String className, String methodName, double duration, String status)
   {
      this.className = className;
      String[] split = className.split("\\.");
      this.classSimpleName = split[split.length - 1];
      this.methodName = methodName;
      this.duration = duration;
      this.status = status;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public double getDuration()
   {
      return duration;
   }

   public String getClassSimpleName()
   {
      return classSimpleName;
   }

   public String getClassName()
   {
      return className;
   }

   public boolean isSuccessful()
   {
      return status.equals("successful");
   }
}