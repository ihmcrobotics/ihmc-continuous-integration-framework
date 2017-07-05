package us.ihmc.continuousIntegration.lineCounting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;

public class WorkspaceLineCountReport
{
   public static void printWorkspaceLineCountReport()
   {
      Map<String, AgileTestingProject> atProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public void setupProject(AgileTestingProject atProject)
         {
            atProject.countLines();
         }
      }, SourceTools.getWorkspacePath());
      
      long totalCount = 0;
      ArrayList<AgileTestingProject> orderedByLineCount = new ArrayList<>();
      for (AgileTestingProject atProject : atProjects.values())
      {
         totalCount += atProject.getLineCount();
         orderedByLineCount.add(atProject);
      }
      
      Collections.sort(orderedByLineCount, new Comparator<AgileTestingProject>()
      {
         @Override
         public int compare(AgileTestingProject o1, AgileTestingProject o2)
         {
            return (int) (o2.getLineCount() - o1.getLineCount());
         }
      });
      
      System.out.format("IHMC Robot Lab Workspace Line Count: %,d total lines%n", totalCount);
      System.out.println("Rank Lines    Name");
      
      for (int i = 0; i < orderedByLineCount.size(); i++)
      {
         AgileTestingProject atProject = orderedByLineCount.get(i);
         System.out.format("%3d. %,8d " + atProject.getRawProjectName() + "%n", i + 1, atProject.getLineCount());
      }
   }
   
   public static void main(String[] args)
   {
      WorkspaceLineCountReport.printWorkspaceLineCountReport();
   }
}
