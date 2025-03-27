package neome.plugin.salesforce.api;

import com.neome.api.meta.base.dto.StudioDtoOption;
import com.neome.plugin.base.ApiInputNull;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.forms.SysFormMapOfOptions;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SObjectOpportunity;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.AccountRepository;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiGetAccountsDropdownList implements IPluginApi<ApiCtx, ApiInputNull, SysFormMapOfOptions>
{
  @Override
  public void execute(ApiCtx ctx, ApiInputNull input, IPluginApiAcceptor<SysFormMapOfOptions> output)
  {
    AccountRepository accountRepository = ctx.getSalesforceFacade().accountRepository;
    MetaDataRepository metaDataRepository = ctx.getSalesforceFacade().metaDataRepository;
    Mono<SysFormMapOfOptions> sysFormMapOfOptionsMono = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> {
        return accountRepository.getAll(salesforceUsername)
          .flatMapIterable(sObjects -> sObjects)
          .collectList()
          .flatMap(accounts -> metaDataRepository.describe(salesforceUsername, SFObjectType.account)
            .map(sfDescribeDerived -> FormUtils.getSysFormMapOfOptions(accounts, accountSObj -> {
              String id = SObjectPlus.getStringValue(accountSObj, SObjectOpportunity.FIELD_ID);
              String name = SObjectPlus.getStringValue(accountSObj, SObjectOpportunity.FIELD_NAME);
              StudioDtoOption studioDtoOption = new StudioDtoOption();
              studioDtoOption.metaId = id;
              studioDtoOption.value = name;
              return studioDtoOption;
            })));
      });
    ctx.setOutput(output, sysFormMapOfOptionsMono);
  }
}
