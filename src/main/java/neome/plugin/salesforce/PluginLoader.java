package neome.plugin.salesforce;

import neome.plugin.salesforce.form.SalesforceConfig;

public class PluginLoader
{
  public void init(PluginCtx ctx, SalesforceConfig parameters)
  {
    ctx.setSalesforceConfig(parameters);
  }
}
