package us.ihmc.continuousIntegration.gradle;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.commons.nio.WriteOption;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.log.LogTools;

public class GradleSettingsFormatter
{
   private static final String ROOT_PROJECT_REGEX = "^\\s*rootProject.name.*$";
   private String handledRegex = "";
   {
      handledRegex += "|";
      handledRegex += "|" + "^\\s*$";
      handledRegex += "|" + "^\\s*includeFlat\\s*'.*'\\s*$";
      handledRegex += "|" + "^\\s*includeFlat\\s*\".*\"\\s*$";
      handledRegex += "|" + "^\\s*include\\s*'.*'\\s*$";
      handledRegex += "|" + "^\\s*include\\s*\".*\"\\s*$";
      handledRegex += "|" + ROOT_PROJECT_REGEX;
   }
   
   private GradleSettingsFormatter()
   {
      Map<String, AgileTestingProject> projects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         // defaults
      }, SourceTools.getWorkspacePath());

      for (AgileTestingProject project : projects.values())
      {
         Path settingsGradlePath = project.getPath().resolve(AgileTestingTools.SETTINGS_GRADLE_NAME);
         if (Files.exists(settingsGradlePath))
         {
            List<String> lines = FileTools.readAllLines(settingsGradlePath, DefaultExceptionHandler.PRINT_STACKTRACE);
            List<String> existingLines = new ArrayList<>();
            List<String> formattedIncludes = new ArrayList<>();
            
            boolean doTheWrite = true;
            for (String line : lines)
            {
               if (!line.matches(handledRegex))
               {
                  LogTools.warn(project.getRawProjectName()+ "\\" + AgileTestingTools.SETTINGS_GRADLE_NAME + " does not conform: " + line);
                  doTheWrite = false;
                  break;
               }
               
               if (!line.trim().isEmpty())
               {
                  if (line.matches(ROOT_PROJECT_REGEX))
                  {
                     existingLines.add(line);
                  }
                  else
                  {
                     String[] split = line.split("\\s|'|\"");
                     String lineToAdd = split[0] + " '" + split[2] + "'";
                     formattedIncludes.add(lineToAdd);
                  }
               }
            }
            
            if (doTheWrite)
            {
               Collections.sort(formattedIncludes);
               
               PrintWriter writer = FileTools.newPrintWriter(settingsGradlePath, WriteOption.TRUNCATE, DefaultExceptionHandler.PRINT_STACKTRACE);
               for (String line : existingLines)
               {
                  writer.print(line + "\n");
               }
               for (String line : formattedIncludes)
               {
                  writer.print(line + "\n");
               }
               writer.close();
            }
         }
      }
   }

   public static void format()
   {
      new GradleSettingsFormatter();
   }

   public static void main(String[] args)
   {
      GradleSettingsFormatter.format();
   }
}
