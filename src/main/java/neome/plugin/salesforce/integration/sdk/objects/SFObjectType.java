package neome.plugin.salesforce.integration.sdk.objects;

public enum SFObjectType
{
  account("Account"),
  opportunity("Opportunity"),
  event("Event"),
  task("Task");

  private final String value;

  public String getValue()
  {
    return value;
  }

  SFObjectType(String value)
  {
    this.value = value;
  }

  public static SFObjectType getByValue(String value)
  {
    if(value == null)
    {
      return null;
    }
    for(SFObjectType sfObjectType : values())
    {
      if(sfObjectType.getValue().equals(value))
      {
        return sfObjectType;
      }
    }
    return null;
  }
}
