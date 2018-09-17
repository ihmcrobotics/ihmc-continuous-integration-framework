package us.ihmc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public abstract class AbstractTest
{
   public void imAnAbstractTest()
   {

   }

   @Tag("fast")
   @Test
   public void someNonExtendedTest()
   {
      Assertions.assertTimeout(Duration.ofSeconds(30), () -> {

      });
   }
}
