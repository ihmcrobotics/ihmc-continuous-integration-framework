package us.ihmc.continuousIntegration.testSuiteRunner;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import us.ihmc.commons.PrintTools;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.model.AgileTestingClassPath;
import us.ihmc.continuousIntegration.tools.SourceTools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class DurationRunListener extends RunListener
{
   ArrayList<AtomicTestRun> runs = new ArrayList<>();

   Stopwatch timer = new Stopwatch().start();

   public DurationRunListener()
   {
      timer.resetLap();
   }

   @Override
   public void testRunStarted(Description description) throws Exception
   {
      timer.resetLap();
   }

   @Override
   public void testFinished(Description description) throws Exception
   {
      double duration = timer.lap();

      runs.add(new AtomicTestRun(description.getTestClass(), description.getMethodName(), duration * AgileTestingTestRunner.LOCAL_TO_MINION_SPEED_MODIFIER, "unknown"));
   }

   @Override
   public void testRunFinished(Result result) throws Exception
   {
      try
      {
         performAnalyticsAndGenerateAnnotations(result);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void performAnalyticsAndGenerateAnnotations(Result result)
   {      
      Map<String, AgileTestingClassPath> nameToPathMap = AgileTestingTools.mapAllClassNamesToClassPaths(SourceTools.getProjectPath());

      for (AtomicTestRun atomicTestRun : runs)
      {
         AgileTestingTestMethodAnnotationWriter.writeAnnotationsForTestRun(atomicTestRun, nameToPathMap, nameToPathMap.get(atomicTestRun.getClassName()));
      }
      
      PrintTools.info(this, "Total run time: " + new DecimalFormat("0.0").format(((double) result.getRunTime()) / 1000.0) + " s");
   }
}
