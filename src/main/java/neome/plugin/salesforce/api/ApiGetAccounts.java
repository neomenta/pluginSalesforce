package neome.plugin.salesforce.api;

import com.neome.plugin.base.ApiInputNull;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.AccountList;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectAccount;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.AccountRepository;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiGetAccounts implements IPluginApi<ApiCtx, ApiInputNull, AccountList>
{
  @Override
  public void execute(ApiCtx ctx, ApiInputNull input, IPluginApiAcceptor<AccountList> output)
  {
    AccountRepository accountRepository = ctx.getSalesforceFacade().accountRepository;
    MetaDataRepository metaDataRepository = ctx.getSalesforceFacade().metaDataRepository;
    Mono<AccountList> getAccountListMono = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> {
        return accountRepository.getAll(salesforceUsername)
          .flatMapIterable(sObjects -> sObjects)
          .collectList()
          .flatMap(accounts -> metaDataRepository.describe(salesforceUsername, SFObjectType.account)
            .map(sfDescribeDerived -> {
              AccountList accountList = new AccountList();
              accountList.account = FormUtils.getGridRowList(accounts.toArray(new SObject[0]),
                accountSObject -> {
                  AccountList.Account gridRowAccount = new AccountList.Account();
                  gridRowAccount.id = accountSObject.getId();
                  gridRowAccount.name = SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_NAME);
                  gridRowAccount.billingState =
                    SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_BILLING_STATE);
                  gridRowAccount.owner = SObjectPlus.getRefFieldFieldValueOption(accountSObject,
                    SFRefField.owner,
                    SObjectAccount.FIELD_OWNER_ID);
                  gridRowAccount.accountSite = SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_SITE);
                  gridRowAccount.accountNumber =
                    SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_ACCOUNT_NUMBER);
                  gridRowAccount.phone = SObjectPlus.getStringValue(accountSObject, SObjectAccount.FIELD_PHONE);
                  gridRowAccount.industry = SObjectPlus.getPickListFieldValue(sfDescribeDerived,
                    accountSObject,
                    SObjectAccount.FIELD_INDUSTRY);
                  gridRowAccount.type =
                    SObjectPlus.getPickListFieldValue(sfDescribeDerived, accountSObject, SObjectAccount.FIELD_TYPE);
                  return gridRowAccount;
                });
              return accountList;
            }));
      });
    ctx.setOutput(output, getAccountListMono);
  }
}
