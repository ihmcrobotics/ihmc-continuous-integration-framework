package us.ihmc.robotics;

import java.util.regex.Matcher;

import org.junit.internal.ArrayComparisonFailure;

public class AssertionTools
{
   static public void assertTrue(String message, boolean condition)
   {
   }

   static public void assertTrue(boolean condition)
   {
      org.junit.jupiter.api.Assertions.assertTrue(condition);
   }

   static public void assertFalse(String message, boolean condition)
   {
      assertTrue(message, !condition);
   }

   static public void assertFalse(boolean condition)
   {
      assertFalse(null, condition);
   }

   static public void fail(String message)
   {
   }

   static public void fail()
   {
   }

   static public void assertEquals(String message, Object expected, Object actual)
   {
   }

   static public void assertEquals(Object expected, Object actual)
   {
   }

   static public void assertNotEquals(String message, Object first, Object second)
   {
   }

   static public void assertNotEquals(Object first, Object second)
   {
   }

   private static void failEquals(String message, Object actual)
   {
   }

   static public void assertNotEquals(String message, long first, long second)
   {
   }

   static public void assertNotEquals(long first, long second)
   {
   }

   static public void assertNotEquals(String message, double first, double second, double delta)
   {
   }

   static public void assertNotEquals(double first, double second, double delta)
   {
   }

   public static void assertArrayEquals(String message, Object[] expecteds, Object[] actuals) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(Object[] expecteds, Object[] actuals)
   {
   }

   public static void assertArrayEquals(String message, byte[] expecteds, byte[] actuals) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(byte[] expecteds, byte[] actuals)
   {
   }

   public static void assertArrayEquals(String message, char[] expecteds, char[] actuals) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(char[] expecteds, char[] actuals)
   {
   }

   public static void assertArrayEquals(String message, short[] expecteds, short[] actuals) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(short[] expecteds, short[] actuals)
   {
   }

   public static void assertArrayEquals(String message, int[] expecteds, int[] actuals) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(int[] expecteds, int[] actuals)
   {
   }

   public static void assertArrayEquals(String message, long[] expecteds, long[] actuals) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(long[] expecteds, long[] actuals)
   {
   }

   public static void assertArrayEquals(String message, double[] expecteds, double[] actuals, double delta) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(double[] expecteds, double[] actuals, double delta)
   {
   }

   public static void assertArrayEquals(String message, float[] expecteds, float[] actuals, float delta) throws ArrayComparisonFailure
   {
   }

   public static void assertArrayEquals(float[] expecteds, float[] actuals, float delta)
   {
   }

   private static void internalArrayEquals(String message, Object expecteds, Object actuals) throws ArrayComparisonFailure
   {
   }

   static public void assertEquals(String message, double expected, double actual, double delta)
   {
   }

   static public void assertEquals(String message, float expected, float actual, float delta)
   {
   }

   static public void assertEquals(long expected, long actual)
   {
   }

   static public void assertEquals(String message, long expected, long actual)
   {
   }

   @Deprecated
   static public void assertEquals(double expected, double actual)
   {
   }

   @Deprecated
   static public void assertEquals(String message, double expected, double actual)
   {
   }

   static public void assertEquals(double expected, double actual, double delta)
   {
   }

   static public void assertEquals(float expected, float actual, float delta)
   {
   }

   static public void assertNotNull(String message, Object object)
   {
   }

   static public void assertNotNull(Object object)
   {
   }

   static public void assertNull(String message, Object object)
   {
   }

   static public void assertNull(Object object)
   {
   }

   static private void failNotNull(String message, Object actual)
   {
   }

   static public void assertSame(String message, Object expected, Object actual)
   {
   }

   static public void assertSame(Object expected, Object actual)
   {
   }

   static public void assertNotSame(String message, Object unexpected, Object actual)
   {
   }

   static public void assertNotSame(Object unexpected, Object actual)
   {
   }

   static private void failSame(String message)
   {
   }

   static private void failNotSame(String message, Object expected, Object actual)
   {
   }

   static private void failNotEquals(String message, Object expected, Object actual)
   {
   }

   @Deprecated
   public static void assertEquals(String message, Object[] expecteds, Object[] actuals)
   {
   }

   @Deprecated
   public static void assertEquals(Object[] expecteds, Object[] actuals)
   {
   }
}
