package neome.plugin.salesforce.integration.sdk.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SObjectAccount;
import neome.plugin.salesforce.integration.sdk.query.SFQueryBuilder;
import neome.plugin.salesforce.integration.sdk.query.SFWhere;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AccountRepository
{

  private final WSCInterface wscInterface;

  public AccountRepository(WSCInterface wscInterface)
  {
    this.wscInterface = wscInterface;
  }

  public Mono<String> update(String username, String id, SObject sObject)
  {
    return wscInterface.updateRecord(username, id, sObject);
  }

  public Flux<List<SObject>> getAll(String username)
  {
    return wscInterface
      .getLoggedInUserInfo(username)
      .flatMapMany(getUserInfoResult -> {
        SFQueryBuilder queryBuilder = new SFQueryBuilder()
          .addColumn(SObjectAccount.FIELD_ID)
          .addColumn(SObjectAccount.FIELD_NAME)
          .addColumn(SObjectAccount.FIELD_ACCOUNT_NUMBER)
          .addColumn(SObjectAccount.FIELD_SITE)
          .addColumn(SObjectAccount.FIELD_BILLING_STATE)
          .addColumn(SObjectAccount.FIELD_INDUSTRY)
          .addColumn(SObjectAccount.FIELD_TYPE)
          .addColumn(SObjectAccount.FIELD_PHONE)
          .addColumn(SObjectAccount.FIELD_OWNER_ID)
          .addColumn(SObjectAccount.FIELD_OWNER_NAME)
          .from(SObjectAccount.OBJECT_TYPE)
          .where(List.of(new SFWhere(SObjectAccount.FIELD_OWNER_ID, getUserInfoResult.getUserId())), true);
        queryBuilder.orderBy(SObjectAccount.FIELD_CREATED_DATE);
        queryBuilder.limit(WSCInterface.LIMIT);
        String query = queryBuilder.build();
        return wscInterface.getRecords(username, query);
      });
  }

  public Mono<Optional<SObject>> getById(String username, String id)
  {
    Set<String> extraRefFieldSet = Set.of(SObjectAccount.FIELD_OWNER_NAME);
    return wscInterface.getRecordById(username, SFObjectType.account, extraRefFieldSet, id);
  }

  public Mono<String> create(String username, SObject account)
  {
    return wscInterface.createRecord(username, account);
  }
}