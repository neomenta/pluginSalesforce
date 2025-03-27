package neome.plugin.salesforce.api;

import java.util.Optional;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.AccountDetail;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.integration.exception.SObjectNotFoundException;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SFDescribeDerived;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectAccount;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.repositories.AccountRepository;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import reactor.core.publisher.Mono;

public class ApiGetAccountDetails implements IPluginApi<ApiCtx, RecordIdInputForm, AccountDetail>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<AccountDetail> output)
    throws PluginApiException
  {
    Mono<AccountDetail> getAccountDetail = ctx.getSalesforceUsername()
      .flatMap(username -> getAccountDetails(ctx, username, input.id));
    ctx.setOutput(output, getAccountDetail);
  }

  public static Mono<AccountDetail> getAccountDetails(ApiCtx apiCtx, String username, String id)
  {
    SalesforceFacade salesforceFacade = apiCtx.pluginCtx.getSalesforceFacade();
    AccountRepository accountRepository = salesforceFacade.accountRepository;
    MetaDataRepository metaDataRepository = salesforceFacade.metaDataRepository;
    return Mono
      .zip(metaDataRepository.describe(username, SFObjectType.account), accountRepository.getById(username, id))
      .flatMap(objects -> {
        SFDescribeDerived sfDescribeDerived = objects.getT1();
        Optional<SObject> accountSObjOptional = objects.getT2();
        if(accountSObjOptional.isPresent())
        {
          SObject accountSObject = accountSObjOptional.get();
          AccountDetail accountDetail = new AccountDetail();
          accountDetail.id = SObjectPlus.getStringValue(accountSObject, SObjectTask.FIELD_ID);
          accountDetail.name = SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_NAME);
          accountDetail.billingState =
            SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_BILLING_STATE);
          accountDetail.owner = SObjectPlus.getRefFieldFieldValueOption(accountSObject,
            SFRefField.owner,
            SObjectAccount.FIELD_OWNER_ID);
          accountDetail.accountSite = SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_SITE);
          accountDetail.accountNumber =
            SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_ACCOUNT_NUMBER);
          accountDetail.phone = SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_PHONE);
          accountDetail.industry = SObjectPlus.getPickListFieldValue(sfDescribeDerived,
            accountSObject,
            SObjectAccount.FIELD_INDUSTRY);
          accountDetail.type =
            SObjectPlus.getPickListFieldValue(sfDescribeDerived, accountSObject, SObjectAccount.FIELD_TYPE);
          return Mono.just(accountDetail);
        }
        else
        {
          return Mono.error(new SObjectNotFoundException("Account not found"));
        }
      });
  }
}
