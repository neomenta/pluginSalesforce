package neome.plugin.salesforce.api;

import com.neome.api.meta.base.dto.StudioDtoOption;
import com.neome.plugin.base.ApiInputNull;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.forms.SysFormMapOfOptions;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.integration.sdk.objects.SObjectOpportunity;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.OpportunityRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiGetOpportunitiesDropdownList
  implements IPluginApi<ApiCtx, ApiInputNull, SysFormMapOfOptions>
{
  @Override
  public void execute(ApiCtx ctx, ApiInputNull input, IPluginApiAcceptor<SysFormMapOfOptions> output)
  {
    OpportunityRepository opportunityRepository = ctx.getSalesforceFacade().opportunityRepository;
    Mono<SysFormMapOfOptions> sysFormMapOfOptionsMono =
      ctx.getSalesforceUsername()
        .flatMap(salesforceUsername -> {
          return opportunityRepository
            .getList(salesforceUsername)
            .flatMapIterable(sObjects -> sObjects)
            .collectList()
            .map(opportunitiesSObjects -> FormUtils.getSysFormMapOfOptions(opportunitiesSObjects, opportunitySObj -> {
              String opportunityId = SObjectPlus.getStringValue(opportunitySObj, SObjectOpportunity.FIELD_ID);
              String opportunityName = SObjectPlus.getStringValue(opportunitySObj, SObjectOpportunity.FIELD_NAME);
              StudioDtoOption studioDtoOption = new StudioDtoOption();
              studioDtoOption.metaId = opportunityId;
              studioDtoOption.value = opportunityName;
              return studioDtoOption;
            }));
        });
    ctx.setOutput(output, sysFormMapOfOptionsMono);
  }
}
