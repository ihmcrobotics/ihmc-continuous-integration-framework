package us.ihmc;

import org.junit.Test;

public class ExtendingTest extends AbstractTest
{
   @Test
   public void imAndExtendingTest()
   {
      super.imAnAbstractTest();
   }
}
