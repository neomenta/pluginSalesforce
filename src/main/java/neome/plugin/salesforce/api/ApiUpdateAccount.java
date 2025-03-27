package neome.plugin.salesforce.api;

import com.neome.plugin.base.ApiDone;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateAccount;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SObjectAccount;
import neome.plugin.salesforce.integration.sdk.repositories.AccountRepository;
import reactor.core.publisher.Mono;

public class ApiUpdateAccount implements IPluginApi<ApiCtx, CreateAccount, ApiDone>
{
  @Override
  public void execute(ApiCtx ctx, CreateAccount input, IPluginApiAcceptor<ApiDone> output)
    throws PluginApiException
  {
    String id = input.id;
    SalesforceFacade salesforceFacade = ctx.pluginCtx.getSalesforceFacade();
    AccountRepository accountRepository = salesforceFacade.accountRepository;
    Mono<ApiDone> apiDoneMono = ctx.getSalesforceUsername()
      .flatMap(username -> {
        SObject accountSObject = new SObject();
        accountSObject.setType(SObjectAccount.OBJECT_TYPE);
        accountSObject.setId(id);
        accountSObject.setSObjectField(SObjectAccount.FIELD_ACCOUNT_NUMBER, input.accountNumber);
        accountSObject.setSObjectField(SObjectAccount.FIELD_INDUSTRY,
          input.industry != null
            ? input.industry.optionId
            : null);
        accountSObject.setSObjectField(SObjectAccount.FIELD_NAME, input.name);
        accountSObject.setSObjectField(SObjectAccount.FIELD_PHONE, input.phone);
        accountSObject.setSObjectField(SObjectAccount.FIELD_BILLING_STATE, input.billingState);
        accountSObject.setSObjectField(SObjectAccount.FIELD_SITE, input.accountSite);
        accountSObject.setSObjectField(SObjectAccount.FIELD_TYPE,
          input.type != null
            ? input.type.optionId
            : null);
        accountSObject.setSObjectField(SObjectAccount.FIELD_OWNER_ID,
          input.owner != null
            ? input.owner.optionId
            : null);
        return accountRepository.update(username, id, accountSObject)
          .map(createdRecordId -> ApiDone.instance);
      });
    ctx.setOutput(output, apiDoneMono);
  }
}
