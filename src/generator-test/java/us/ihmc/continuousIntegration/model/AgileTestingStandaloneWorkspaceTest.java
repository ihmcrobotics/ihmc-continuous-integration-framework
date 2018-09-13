package us.ihmc.continuousIntegration.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgileTestingStandaloneWorkspaceTest
{
   @Test(timeout = 30000)
   public void testDisableJobPatternMatching()
   {
      String matchString = AgileTestingStandaloneWorkspace.formMatchingPattern("Valkyrie");

      assertTrue("ValkyrieDVideo".matches(matchString));
      assertTrue("ValkyrieDADHUSAFast".matches(matchString));
      assertTrue("ValkyrieCompile".matches(matchString));
      assertFalse("ValkyrieAAAHealth".matches(matchString));
      assertFalse("ValkyrieLManual".matches(matchString));
      assertTrue("ValkyrieLXFlaky".matches(matchString));
   }
}
