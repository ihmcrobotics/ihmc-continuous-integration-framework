package us.ihmc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class BadlyNamed
{
   @Tag("fast")
   @Test
   public void badlyNamedTest()
   {
      Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
         Thread.sleep(500);
         Assertions.fail();
      });
   }
}
