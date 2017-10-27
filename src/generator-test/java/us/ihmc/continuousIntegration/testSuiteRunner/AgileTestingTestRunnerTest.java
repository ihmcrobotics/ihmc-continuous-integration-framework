package us.ihmc.continuousIntegration.testSuiteRunner;

import org.junit.Test;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.testSuiteRunner.AgileTestingTestRunner;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = IntegrationCategory.EXCLUDE)
public class AgileTestingTestRunnerTest
{
   @ContinuousIntegrationTest(estimatedDuration = 0.1)
   @Test(timeout = 30000)
   public void testATTestRunner()
   {
      AgileTestingTestRunner.run(AgileTestingTestRunnerTestClassTest.class);
   }
}
