package us.ihmc.continuousIntegration.testSuiteRunner;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;
import us.ihmc.log.LogTools;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.WriteOption;
import us.ihmc.continuousIntegration.AgileTestingJavaParserTools;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.generator.AgileTestingAnnotationTools;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgileTestingTestMethodAnnotationWriter
{
   public static void writeAnnotationsForTestRun(AtomicTestRun atomicTestRun, Map<String, AgileTestingClassPath> nameToFileMap, AgileTestingClassPath path)
   {      
      Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> methodAnnotationMap = new HashMap<>();
      Pair<CompilationUnit, ClassOrInterfaceDeclaration> pair = AgileTestingJavaParserTools.parseForTestAnnotations(path, methodAnnotationMap);
      
      if (methodAnnotationMap.isEmpty() || !methodAnnotationMap.containsKey(atomicTestRun.getMethodName()))
      {
         try
         {
            String superClassName = pair.getRight().getExtendedTypes(0).getNameAsString();
            LogTools.error(path.getClassName() + " has a super class with tests. Super class name: " + superClassName);
            
            writeAnnotationsForTestRun(atomicTestRun, nameToFileMap, AgileTestingTools.getFirstMatchInMap(nameToFileMap, superClassName));
         }
         catch (IndexOutOfBoundsException e)
         {
            LogTools.error(path.getClassName() + " has serious issues. Please give it some love.");
            return;
         }
      }
      else
      {
         LogTools.info(atomicTestRun.getClassName() + "." + atomicTestRun.getMethodName() + ": " + new DecimalFormat("0.0").format(atomicTestRun.getDuration()) + " s");
         
         MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>> mutablePair = methodAnnotationMap.get(atomicTestRun.getMethodName());

         AnnotationExpr deployableTestMethodExpr = mutablePair.getRight().get(ContinuousIntegrationTest.class.getSimpleName());
         if (deployableTestMethodExpr == null)
         {
            LogTools.warn("No @ContinuousIntegrationTest! Skipping...");
            return;
         }
         AnnotationExpr junitTestExpr = mutablePair.getRight().get(Test.class.getSimpleName());
         
         Map<String, MemberValuePair> deployableTestMethodAnnotationFields = AgileTestingJavaParserTools.mapAnnotationFields(deployableTestMethodExpr);
         Map<String, MemberValuePair> junitTestAnnotationFields = AgileTestingJavaParserTools.mapAnnotationFields(junitTestExpr);
         
         MemberValuePair durationMemberValuePair = deployableTestMethodAnnotationFields.get(AgileTestingAnnotationTools.ESTIMATED_DURATION);
         MemberValuePair timeoutMemberValuePair = junitTestAnnotationFields.get(AgileTestingAnnotationTools.TIMEOUT);
         
         try
         {
            if (durationMemberValuePair.getBegin().get().line != durationMemberValuePair.getEnd().get().line || timeoutMemberValuePair.getBegin().get().line != timeoutMemberValuePair.getEnd().get().line)
            {
               LogTools.error(AgileTestingAnnotationTools.ESTIMATED_DURATION + " or " + AgileTestingAnnotationTools.TIMEOUT + " spans multiple lines. Skipping.");
               return;
            }
         }
         catch (NullPointerException e)
         {
            LogTools.error("Something wrong with this annotation.");
         }
         
         int durationLineNumber = durationMemberValuePair.getBegin().get().line - 1;
         int timeoutLineNumber = timeoutMemberValuePair.getBegin().get().line - 1;
         String originalDurationPair = durationMemberValuePair.toString();
         String originalTimeoutPair = timeoutMemberValuePair.toString();
         
         DoubleLiteralExpr newDurationPair = new DoubleLiteralExpr(new DecimalFormat("0.0").format(atomicTestRun.getDuration()));
         IntegerLiteralExpr newTimeoutPair = new IntegerLiteralExpr(String.valueOf(calculateTimeoutInMilliseconds(atomicTestRun.getDuration())));
         durationMemberValuePair.setValue(newDurationPair);
         timeoutMemberValuePair.setValue(newTimeoutPair);
         String modifiedDurationPair = durationMemberValuePair.toString();
         String modifiedTimeoutPair = timeoutMemberValuePair.toString();
         byte[] bytes = FileTools.readAllBytes(path.getPath(), DefaultExceptionHandler.PRINT_STACKTRACE);
         List<String> lines = FileTools.readLinesFromBytes(bytes, DefaultExceptionHandler.PRINT_STACKTRACE);
         String durationLine = lines.get(durationLineNumber);
         String timeoutLine = lines.get(timeoutLineNumber);
         durationLine = durationLine.replace(originalDurationPair, modifiedDurationPair);
         timeoutLine = timeoutLine.replace(originalTimeoutPair, modifiedTimeoutPair);
         bytes = FileTools.replaceLineInFile(durationLineNumber, durationLine, bytes, lines);
         bytes = FileTools.replaceLineInFile(timeoutLineNumber, timeoutLine, bytes, lines);
         FileTools.write(path.getPath(), bytes, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
      }
   }
   
   private static int calculateTimeoutInMilliseconds(double testDuration)
   {
      double potentialTimeout = (testDuration * 5.0);
      double timeoutInSeconds = (potentialTimeout > 30.0) ? MathTools.roundToSignificantFigures(potentialTimeout, 2) : 30.0;
      return (int) Conversions.secondsToMilliseconds(timeoutInSeconds);
   }
}
