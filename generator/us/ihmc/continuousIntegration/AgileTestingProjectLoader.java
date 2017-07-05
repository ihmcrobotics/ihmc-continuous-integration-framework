package us.ihmc.continuousIntegration;

import us.ihmc.continuousIntegration.model.AgileTestingProject;

public abstract class AgileTestingProjectLoader
{
   public boolean meetsCriteria(AgileTestingProject atProject)
   {
      return true;
   }
   
   public void setupProject(AgileTestingProject atProject)
   {
      
   }
}