package us.ihmc.continuousIntegration.testSuiteRunner;

import org.junit.runner.JUnitCore;

public class AgileTestingTestRunner
{
   public static final double LOCAL_TO_MINION_SPEED_MODIFIER = 1.5; // e.g. 1.5 means your PC is 150% faster
   
   public static void run(Class<?> clazz)
   {
      JUnitCore junit = new JUnitCore();

      junit.addListener(new DurationRunListener());

      junit.run(clazz);
   }
}
