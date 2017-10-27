package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

import us.ihmc.commons.Conversions;
import us.ihmc.commons.MathTools;

public class BambooTestResult
{
   private String className;
   private String methodName;
   private String status;
   private String duration;
   
   public String getFormattedDuration()
   {
      return String.valueOf(MathTools.roundToSignificantFigures(getDurationMinutes(), 2));
   }
   
   public double getDurationMinutes()
   {
      return Conversions.millisecondsToMinutes(getDurationMillis());
   }
   
   public double getDurationSeconds()
   {
      return Conversions.millisecondsToSeconds(getDurationMillis());
   }
   
   public int getDurationMillis()
   {
      return Integer.valueOf(duration);
   }

   public String getClassName()
   {
      return className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public void setMethodName(String methodName)
   {
      this.methodName = methodName;
   }

   public String getStatus()
   {
      return status;
   }

   public void setStatus(String status)
   {
      this.status = status;
   }

   public String getDuration()
   {
      return duration;
   }

   public void setDuration(String duration)
   {
      this.duration = duration;
   }
}
