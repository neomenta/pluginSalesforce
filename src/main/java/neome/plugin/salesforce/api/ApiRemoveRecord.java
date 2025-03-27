package neome.plugin.salesforce.api;

import com.neome.plugin.base.ApiDone;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import reactor.core.publisher.Mono;

public class ApiRemoveRecord implements IPluginApi<ApiCtx, RecordIdInputForm, ApiDone>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<ApiDone> output) throws PluginApiException
  {
    SalesforceFacade salesforceFacade = ctx.pluginCtx.getSalesforceFacade();
    WSCInterface wscInterface = salesforceFacade.wscInterface;
    Mono<ApiDone> deleteRecord = ctx.getSalesforceUsername()
      .flatMap(username -> wscInterface.deleteRecords(username, input.id)
        .map(done -> ApiDone.instance));
    ctx.setOutput(output, deleteRecord);
  }
}
