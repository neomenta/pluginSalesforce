package neome.plugin.salesforce.integration;

import com.neome.api.meta.base.Types;
import neome.plugin.salesforce.form.SalesforceConfig;

public class SalesForceAPIConfig
{
  public final Types.EntId entId;

  public final SalesforceConfig configForm;

  public final String authUrl;

  public final String apiBaseUrl;

  public final String usernameVariableName;

  public SalesForceAPIConfig(Types.EntId entId, SalesforceConfig configForm)
  {
    this.entId = entId;
    this.configForm = configForm;
    this.apiBaseUrl = "%s/services/data/v59.0/".formatted(configForm.domain);
    this.usernameVariableName = configForm.usernameVariableName;
    this.authUrl = "https://login.salesforce.com/services/oauth2/token";
  }
}

