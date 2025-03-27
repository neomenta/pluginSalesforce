package neome.plugin.salesforce;

import com.neome.api.meta.base.Types;
import com.neome.plugin.base.AgentPluginCtx;
import com.neome.util.plus.StringPlus;
import neome.plugin.salesforce.form.SalesforceConfig;
import neome.plugin.salesforce.integration.SalesForceAPIConfig;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;

public class PluginCtx extends AgentPluginCtx
{
  private SalesforceFacade salesforceFacade;

  private SalesforceConfig salesforceConfig;

  public PluginCtx(Types.EntId entId)
  {
    super(entId);
  }

  public void setSalesforceConfig(SalesforceConfig salesforceConfig)
  {
    this.salesforceConfig = salesforceConfig;
  }

  public SalesforceFacade getSalesforceFacade()
  {
    if(salesforceFacade == null)
    {
      SalesForceAPIConfig salesForceAPIConfig = new SalesForceAPIConfig(entId, salesforceConfig);
      validateSalesforceConfig(salesForceAPIConfig);
      salesforceFacade = new SalesforceFacade(salesForceAPIConfig);
    }
    return salesforceFacade;
  }

  private void validateSalesforceConfig(SalesForceAPIConfig salesForceAPIConfig)
  {
    if(StringPlus.isNullOrEmpty(salesForceAPIConfig.apiBaseUrl))
    {
      throw new IllegalStateException("Please provide api base url");
    }
    if(StringPlus.isNullOrEmpty(salesForceAPIConfig.authUrl))
    {
      throw new IllegalStateException("Please provider auth url");
    }
  }
}
