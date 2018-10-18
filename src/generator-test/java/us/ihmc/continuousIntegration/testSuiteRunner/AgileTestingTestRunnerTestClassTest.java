package us.ihmc.continuousIntegration.testSuiteRunner;

import org.junit.Test;

import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.EXCLUDE)
public class AgileTestingTestRunnerTestClassTest extends AgileTestingTestRunnerTestSuperClassTest
{
   @ContinuousIntegrationTest(estimatedDuration = 3.0)
   @Test(timeout = 30000)
   public void testSleep2Seconds()
   {
      ThreadTools.sleepSeconds(2.0);
   }
}
