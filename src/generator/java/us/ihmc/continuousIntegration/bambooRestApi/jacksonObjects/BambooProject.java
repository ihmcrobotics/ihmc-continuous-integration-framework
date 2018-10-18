package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooProject
{
   private BambooLink link;
   private String name;
   private String key;

   public BambooLink getLink()
   {
      return link;
   }

   public void setLink(BambooLink link)
   {
      this.link = link;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }
}
