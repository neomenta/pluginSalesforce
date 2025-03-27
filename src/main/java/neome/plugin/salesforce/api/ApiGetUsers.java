package neome.plugin.salesforce.api;

import com.neome.api.meta.base.dto.StudioDtoOption;
import com.neome.plugin.base.ApiInputNull;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.forms.SysFormMapOfOptions;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.objects.SObjectUser;
import neome.plugin.salesforce.integration.sdk.repositories.UserRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiGetUsers implements IPluginApi<ApiCtx, ApiInputNull, SysFormMapOfOptions>
{
  @Override
  public void execute(ApiCtx ctx, ApiInputNull input, IPluginApiAcceptor<SysFormMapOfOptions> output)
  {
    UserRepository userRepository = ctx.getSalesforceFacade().userRepository;
    Mono<SysFormMapOfOptions> sysFormMapOfOptionsMono =
      ctx.getSalesforceUsername().flatMap(salesforceUsername -> {
        return userRepository.getStandardUsers(salesforceUsername)
          .flatMapIterable(users -> users)
          .collectList()
          .map(userSObjectList -> FormUtils.getSysFormMapOfOptions(userSObjectList, sObjectUser -> {
            StudioDtoOption studioDtoOption = new StudioDtoOption();
            studioDtoOption.metaId = sObjectUser.getId();
            studioDtoOption.value = SObjectPlus.getStringValue(sObjectUser, SObjectUser.FIELD_NAME);
            return studioDtoOption;
          }));
      });
    ctx.setOutput(output, sysFormMapOfOptionsMono);
  }
}
