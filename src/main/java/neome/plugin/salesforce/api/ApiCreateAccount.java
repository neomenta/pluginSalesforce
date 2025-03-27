package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateAccount;
import neome.plugin.salesforce.form.CreatedRecordId;
import neome.plugin.salesforce.integration.sdk.objects.SObjectAccount;
import neome.plugin.salesforce.integration.sdk.repositories.AccountRepository;
import reactor.core.publisher.Mono;

public class ApiCreateAccount implements IPluginApi<ApiCtx, CreateAccount, CreatedRecordId>
{
  @Override
  public void execute(ApiCtx ctx, CreateAccount input, IPluginApiAcceptor<CreatedRecordId> output)
  {
    AccountRepository accountRepository = ctx.getSalesforceFacade().accountRepository;
    Mono<CreatedRecordId> createAccountMono = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> {
        SObject accountSObject = new SObject();
        accountSObject.setType(SObjectAccount.OBJECT_TYPE);
        accountSObject.setSObjectField(SObjectAccount.FIELD_ACCOUNT_NUMBER, input.accountNumber);
        accountSObject.setSObjectField(SObjectAccount.FIELD_SITE, input.accountSite);
        accountSObject.setSObjectField(SObjectAccount.FIELD_INDUSTRY,
          input.industry != null
            ? input.industry.optionId
            : null);
        accountSObject.setSObjectField(SObjectAccount.FIELD_NAME, input.name);
        accountSObject.setSObjectField(SObjectAccount.FIELD_PHONE, input.phone);
        accountSObject.setSObjectField(SObjectAccount.FIELD_BILLING_STATE, input.billingState);
        accountSObject.setSObjectField(SObjectAccount.FIELD_TYPE,
          input.type != null
            ? input.type.optionId
            : null);
        accountSObject.setSObjectField(SObjectAccount.FIELD_OWNER_ID,
          input.owner != null
            ? input.owner.optionId
            : null);
        return accountRepository.create(salesforceUsername, accountSObject);
      })
      .map(accountId -> {
        CreatedRecordId createdRecordId = new CreatedRecordId();
        createdRecordId.id = accountId;
        return createdRecordId;
      });
    ctx.setOutput(output, createAccountMono);
  }
}

