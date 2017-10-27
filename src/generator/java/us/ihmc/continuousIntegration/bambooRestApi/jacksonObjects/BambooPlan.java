package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooPlan
{
   private boolean enabled;
   private String name;
   private BambooLink link;
   private String shortKey;
   private String type;
   private String shortName;
   private String key;

   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
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

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getShortName()
   {
      return shortName;
   }

   public void setShortName(String shortName)
   {
      this.shortName = shortName;
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
}
