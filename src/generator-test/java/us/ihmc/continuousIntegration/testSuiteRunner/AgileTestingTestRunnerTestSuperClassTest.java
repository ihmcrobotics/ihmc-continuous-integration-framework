package us.ihmc.continuousIntegration.testSuiteRunner;

import org.junit.Test;
import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.EXCLUDE)
public class AgileTestingTestRunnerTestSuperClassTest
{
   @ContinuousIntegrationTest(estimatedDuration = 1.5)
   @Test(timeout = 30000)
   public void testSleepOneSecond()
   {
      ThreadTools.sleepSeconds(1.0);
   }
}
