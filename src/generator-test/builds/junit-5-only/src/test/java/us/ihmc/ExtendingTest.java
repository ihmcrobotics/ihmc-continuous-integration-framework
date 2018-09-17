package us.ihmc;

import org.junit.jupiter.api.Test;

public class ExtendingTest extends AbstractTest
{
   @Test
   public void imAndExtendingTest()
   {
      super.imAnAbstractTest();
   }
}
