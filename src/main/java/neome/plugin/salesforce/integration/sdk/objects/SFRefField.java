package neome.plugin.salesforce.integration.sdk.objects;

public enum SFRefField
{
  account("Account"),
  opportunity("Opportunity"),
  event("Event"),
  task("Task"),
  what("What"),
  who("Who"),
  owner("Owner");

  private final String value;

  public String getValue()
  {
    return value;
  }

  SFRefField(String value)
  {
    this.value = value;
  }

  public static SFRefField getByValue(String value)
  {
    for(SFRefField sfObjectType : values())
    {
      if(sfObjectType.getValue().equals(value))
      {
        return sfObjectType;
      }
    }
    return null;
  }
}
