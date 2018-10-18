package us.ihmc.continuousIntegration.lineCounting;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.WriteOption;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.lineCounting.JavaLineCounter;
import us.ihmc.continuousIntegration.IntegrationCategory;

public class JavaLineCounterTest
{
   private static final Path TEN_COMMENTED_LINES = Paths.get("TenCommentedLinesJava.txt");
   private static final Path LINE_COUNT_TEST = Paths.get("LineCountTestJava.txt");
   
   @Before
   public void setUp()
   {
      ArrayList<String> tenCommentedLines = new ArrayList<>();
      tenCommentedLines.add("// one");
      tenCommentedLines.add("/* two");
      tenCommentedLines.add(" * three");
      tenCommentedLines.add(" * four");
      tenCommentedLines.add(" * five");
      tenCommentedLines.add(" */ six");
      tenCommentedLines.add(" {}");
      tenCommentedLines.add(" // seven");
      tenCommentedLines.add(" // eight");
      tenCommentedLines.add(" /* nine */");
      tenCommentedLines.add(" ()");
      tenCommentedLines.add(" // ten //");
      FileTools.writeAllLines(tenCommentedLines, TEN_COMMENTED_LINES, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      
      ArrayList<String> lineCountTest = new ArrayList<>();
      lineCountTest.add("pacakge yourmom;");
      lineCountTest.add("");
      lineCountTest.add("comment post // dont drop me bro!");
      lineCountTest.add("");
      lineCountTest.add("// yo");
      lineCountTest.add("/** javadoc");
      lineCountTest.add(" *");
      lineCountTest.add("");
      lineCountTest.add(" */ trcky");
      lineCountTest.add("");
      lineCountTest.add(" dont                              ");
      lineCountTest.add(" edit                              ");
      lineCountTest.add(" me                                ");
      lineCountTest.add(" bro                               ");
      lineCountTest.add("                                   ");
      lineCountTest.add(" {");
      lineCountTest.add("  a little braces()                ");
      lineCountTest.add(" }                                 ");
      lineCountTest.add("");
      lineCountTest.add(" (not even java syntax yo)         ");
      lineCountTest.add(" )                                 ");
      lineCountTest.add(" (                                 ");
      lineCountTest.add("                                   ");
      lineCountTest.add(" okimdonebye                       ");
      lineCountTest.add(" ");
      FileTools.writeAllLines(lineCountTest, LINE_COUNT_TEST, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
   }
   
   @After
   public void tearDown()
   {
      FileTools.deleteQuietly(TEN_COMMENTED_LINES);
      FileTools.deleteQuietly(LINE_COUNT_TEST);
   }
   
   @ContinuousIntegrationTest(estimatedDuration = 0.0, categoriesOverride = IntegrationCategory.FAST)
   @Test(timeout = 30000)
   public void testJavaLineCounter()
   {
      long count;
      System.out.println(LINE_COUNT_TEST + ": All lines: " + new JavaLineCounter(true, true, true).countLines(LINE_COUNT_TEST));
      System.out.println(LINE_COUNT_TEST + ": w/ Comments: " + new JavaLineCounter(true, false, false).countLines(LINE_COUNT_TEST));
      System.out.println(LINE_COUNT_TEST + ": w/ Structural: " + new JavaLineCounter(false, true, false).countLines(LINE_COUNT_TEST));
      System.out.println(LINE_COUNT_TEST + ": w/ Blank: " + new JavaLineCounter(false, false, true).countLines(LINE_COUNT_TEST));
      System.out.println(LINE_COUNT_TEST + ": Source Only: " + new JavaLineCounter(false, false, false).countLines(LINE_COUNT_TEST));
      
      System.out.println(TEN_COMMENTED_LINES + ": All lines: " + new JavaLineCounter(true, true, true).countLines(TEN_COMMENTED_LINES));
      System.out.println(TEN_COMMENTED_LINES + ": w/ Comments: " + new JavaLineCounter(true, false, false).countLines(TEN_COMMENTED_LINES));
      System.out.println(TEN_COMMENTED_LINES + ": w/ Structural: " + new JavaLineCounter(false, true, false).countLines(TEN_COMMENTED_LINES));
      System.out.println(TEN_COMMENTED_LINES + ": w/ Blank: " + new JavaLineCounter(false, false, true).countLines(TEN_COMMENTED_LINES));
      System.out.println(TEN_COMMENTED_LINES + ": Source Only: " + new JavaLineCounter(false, false, false).countLines(TEN_COMMENTED_LINES));
      
      count = new JavaLineCounter(true, false, false).countLines(TEN_COMMENTED_LINES);
      assertEquals("Should be 10 comment lines", 10, count);
      
      count = new JavaLineCounter(true, true, true).countLines(LINE_COUNT_TEST);
      assertEquals("Should be 25 total lines", 25, count);
      
      count = new JavaLineCounter(false, false, false).countLines(LINE_COUNT_TEST);
      assertEquals("Should be 9 source lines", 9, count);
      
      count = new JavaLineCounter(false, true, false).countLines(LINE_COUNT_TEST);
      assertEquals("Should be 13 source & structural lines", 13, count);
      
      count = new JavaLineCounter(false, false, true).countLines(LINE_COUNT_TEST);
      assertEquals("Should be 16 source & blank lines", 16, count);
   }
}
