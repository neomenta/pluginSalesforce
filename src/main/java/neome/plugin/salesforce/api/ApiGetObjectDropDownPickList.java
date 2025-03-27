package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.neome.plugin.base.forms.SysFormMapOfOptions;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.DropObjectDownPicklistInput;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import reactor.core.publisher.Mono;

public class ApiGetObjectDropDownPickList implements IPluginApi<ApiCtx, DropObjectDownPicklistInput, SysFormMapOfOptions>
{
  @Override
  public void execute(ApiCtx ctx, DropObjectDownPicklistInput input, IPluginApiAcceptor<SysFormMapOfOptions> output)
  {
    Mono<SysFormMapOfOptions> outputMono = ctx.getSalesforceUsername()
      .flatMap(sfUserName -> {
        String dropdownFieldName = input.dropdownFieldName;
        String objectType = input.objectType;
        SFObjectType sfObjectType = SFObjectType.getByValue(objectType);
        if(sfObjectType == null)
        {
          return Mono.error(new PluginApiException("Invalid object type provided"));
        }
        MetaDataRepository metaDataRepository = ctx.pluginCtx.getSalesforceFacade().metaDataRepository;
        return metaDataRepository.getTypeList(sfUserName, sfObjectType, dropdownFieldName);
      });
    ctx.setOutput(output, outputMono);
  }
}
