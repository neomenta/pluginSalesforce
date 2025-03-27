package neome.plugin.salesforce.integration.sdk;

import com.google.gson.annotations.SerializedName;

public enum GrantType
{
  @SerializedName("password")
  PASSWORD("password"),

  @SerializedName("authorization_code")
  AUTHORIZATION_CODE("authorization_code"),

  @SerializedName("urn:ietf:params:oauth:grant-type:jwt-bearer")
  JWT("urn:ietf:params:oauth:grant-type:jwt-bearer");

  private String value;

  GrantType(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
