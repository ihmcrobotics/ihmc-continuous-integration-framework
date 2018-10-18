package us.ihmc.continuousIntegration.generator;

public class AgileTestingAnnotationTools
{
   //public static final double JOB_DURATION_CAP = Conversions.minutesToSeconds(5.5);
   //public static final double RECOMMENDED_CLASS_CAP = Conversions.minutesToSeconds(2.0);

   public static final String TEST_CLASS_NAME_POSTFIX = "Test";
   public static final String TEST_SUITE_NAME_POSTFIX = "TestSuite";
   public static final String ESTIMATED_DURATION = "estimatedDuration";
   public static final String TIMEOUT = "timeout";
   public static final String CLASS_TARGETS = "categories";
   public static final String METHOD_TARGETS = "categoriesOverride";
   
   public static final String TEST_CLASS_FILENAME_REGEX = ".*" + TEST_CLASS_NAME_POSTFIX + "s?\\.java$";
   public static final String TEST_SUITE_FILENAME_REGEX = ".*" + TEST_SUITE_NAME_POSTFIX + "\\.java$";
}
