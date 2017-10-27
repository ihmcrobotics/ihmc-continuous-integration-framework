package us.ihmc.continuousIntegration.bambooRestApi.jacksonObjects;

public class BambooResult
{
   private String key;
   private String state;
   private String lifeCycleState;
   private String number;
   private String id;
   private BambooTestResults[] testResults;
   
   public boolean isSuccessful()
   {
      return getState().toLowerCase().equals("successful");
   }
   
   public int getBuildNumber()
   {
      return Integer.valueOf(getNumber());
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   public String getLifeCycleState()
   {
      return lifeCycleState;
   }

   public void setLifeCycleState(String lifeCycleState)
   {
      this.lifeCycleState = lifeCycleState;
   }

   public String getNumber()
   {
      return number;
   }

   public void setNumber(String number)
   {
      this.number = number;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public BambooTestResults[] getTestResults()
   {
      return testResults;
   }

   public void setTestResults(BambooTestResults[] testResults)
   {
      this.testResults = testResults;
   }
}
