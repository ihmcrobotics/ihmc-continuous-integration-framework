package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooPlanRequest
{
   private String projectKey;
   private String projectName;
   private BambooProject project;
   private String description;
   private String buildName;
   private BambooPlanKey planKey;
   private BambooLink link;
   private String isFavourite;
   private String isActive;
   private String isBuilding;
   private String averageBuildTimeInSeconds;
   private BambooActions actions;
   private String enabled;
   private String type;
   private BambooStages stages;
   private String name;
   private String shortKey;
   private String expand;
   private String shortName;
   private String key;

   public String getEnabled()
   {
      return enabled;
   }

   public void setEnabled(String enabled)
   {
      this.enabled = enabled;
   }

   public String getAverageBuildTimeInSeconds()
   {
      return averageBuildTimeInSeconds;
   }

   public void setAverageBuildTimeInSeconds(String averageBuildTimeInSeconds)
   {
      this.averageBuildTimeInSeconds = averageBuildTimeInSeconds;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getProjectKey()
   {
      return projectKey;
   }

   public void setProjectKey(String projectKey)
   {
      this.projectKey = projectKey;
   }

   public String getIsActive()
   {
      return isActive;
   }

   public void setIsActive(String isActive)
   {
      this.isActive = isActive;
   }

   public String getBuildName()
   {
      return buildName;
   }

   public void setBuildName(String buildName)
   {
      this.buildName = buildName;
   }

   public String getIsBuilding()
   {
      return isBuilding;
   }

   public void setIsBuilding(String isBuilding)
   {
      this.isBuilding = isBuilding;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getShortKey()
   {
      return shortKey;
   }

   public void setShortKey(String shortKey)
   {
      this.shortKey = shortKey;
   }

   public String getExpand()
   {
      return expand;
   }

   public void setExpand(String expand)
   {
      this.expand = expand;
   }

   public String getShortName()
   {
      return shortName;
   }

   public void setShortName(String shortName)
   {
      this.shortName = shortName;
   }

   public String getProjectName()
   {
      return projectName;
   }

   public void setProjectName(String projectName)
   {
      this.projectName = projectName;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   public BambooLink getLink()
   {
      return link;
   }

   public void setLink(BambooLink link)
   {
      this.link = link;
   }

   public BambooStages getStages()
   {
      return stages;
   }

   public void setStages(BambooStages stages)
   {
      this.stages = stages;
   }

   public BambooProject getProject()
   {
      return project;
   }

   public void setProject(BambooProject project)
   {
      this.project = project;
   }

   public String getIsFavourite()
   {
      return isFavourite;
   }

   public void setIsFavourite(String isFavourite)
   {
      this.isFavourite = isFavourite;
   }

   public BambooActions getActions()
   {
      return actions;
   }

   public void setActions(BambooActions actions)
   {
      this.actions = actions;
   }

   public BambooPlanKey getPlanKey()
   {
      return planKey;
   }

   public void setPlanKey(BambooPlanKey planKey)
   {
      this.planKey = planKey;
   }
}
