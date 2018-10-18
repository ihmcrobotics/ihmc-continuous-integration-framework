package us.ihmc.continuousIntegration.lineCounting;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import us.ihmc.log.LogTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;

public class JavaLineCounter
{
   private static final boolean DEBUG = false;
   
   private static final String COMMENT_REGEX = "^\\s*/\\*.*$|^\\s*//.*$|^\\s*\\*/.*$";
   private static final String BLOCK_COMMENT_START = ".*/\\*.*";
   private static final String BLOCK_COMMENT_END = ".*\\*/.*";
   private static final String STRUCTURAL_REGEX = "^\\s*[{(});]+\\s*$";
   private static final String BLANK_LINES_REGEX = "^\\s*$";
   
   private final boolean countComments;
   private final boolean countStructuralSymbols;
   private final boolean countBlankLines;
   private long count;

   public JavaLineCounter(boolean countComments, boolean countStructuralSymbols, boolean countBlankLines)
   {
      this.countComments = countComments;
      this.countStructuralSymbols = countStructuralSymbols;
      this.countBlankLines = countBlankLines;
   }
   
   public long countLines(List<Path> javaPaths)
   {
      count = 0;
      for (Path path : javaPaths)
      {
         count += countLines(path);
      }
      return count;
   }
   
   public long countLines(Path javaPath)
   {
      count = 0;
      boolean insideBlockComment = false;
      
      try
      {
         for (String line : FileTools.readAllLines(javaPath, DefaultExceptionHandler.PRINT_STACKTRACE))
         {
            if (line.matches(BLOCK_COMMENT_START))
            {
               insideBlockComment = true;
            }
            if (line.matches(BLOCK_COMMENT_END))
            {
               insideBlockComment = false;
            }
            
            if (line.matches(COMMENT_REGEX) || insideBlockComment)
            {
               LogTools.debug("Comment line: " + line);
               if (countComments)
               {
                  ++count;
               }
            }
            else if (line.matches(STRUCTURAL_REGEX))
            {
               LogTools.debug("Structural line: " + line);
               if (countStructuralSymbols)
               {
                  ++count;
               }
            }
            else if (line.matches(BLANK_LINES_REGEX))
            {
               LogTools.debug("Blank line: " + line);
               if (countBlankLines)
               {
                  ++count;
               }
            }
            else
            {
               ++count;
            }
         }
      }
      catch (NullPointerException nullPointerException)
      {
         LogTools.error("On " + javaPath + ": " + nullPointerException.getMessage());
      }
      
      return count;
   }
   
   public static void main(String[] args)
   {
      System.out.println(new JavaLineCounter(true, true, false).countLines(Paths.get("F:\\Workspace4\\AgileTesting\\src\\us\\ihmc\\agileTesting\\lineCounting\\JavaLineCounter.java")));
   }
}
