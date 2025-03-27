package neome.plugin.salesforce.integration.sdk.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.form.CreatedRecordId;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SObjectOpportunity;
import neome.plugin.salesforce.integration.sdk.query.SFQueryBuilder;
import neome.plugin.salesforce.integration.sdk.query.SFWhere;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OpportunityRepository
{
  private final WSCInterface wscInterface;

  public OpportunityRepository(WSCInterface wscInterface)
  {
    this.wscInterface = wscInterface;
  }

  public Flux<List<SObject>> getList(String username)
  {
    SFQueryBuilder queryBuilder = new SFQueryBuilder()
      .addColumn(SObjectOpportunity.FIELD_ID)
      .addColumn(SObjectOpportunity.FIELD_NAME)
      .addColumn(SObjectOpportunity.FIELD_AMOUNT)
      .addColumn(SObjectOpportunity.FIELD_DESCRIPTION)
      .addColumn(SObjectOpportunity.FIELD_CLOSE_DATE)
      .addColumn(SObjectOpportunity.FIELD_STAGE_NAME)
      .addColumn(SObjectOpportunity.FIELD_OWNER_ID)
      .from(SObjectOpportunity.OBJECT_TYPE);
    queryBuilder.orderBy(SObjectOpportunity.FIELD_CREATED_DATE);
    queryBuilder.limit(WSCInterface.LIMIT);
    String query = queryBuilder.build();
    return wscInterface.getRecords(username, query);
  }

  public Mono<CreatedRecordId> create(String username, SObject opportunity)
  {
    return wscInterface.createRecord(username, opportunity)
      .map(recordId -> {
        CreatedRecordId createdRecordId = new CreatedRecordId();
        createdRecordId.id = recordId;
        return createdRecordId;
      });
  }

  public Mono<CreatedRecordId> update(String username, String id, SObject opportunity)
  {
    return wscInterface.updateRecord(username, id, opportunity)
      .map(recordId -> {
        CreatedRecordId createdRecordId = new CreatedRecordId();
        createdRecordId.id = recordId;
        return createdRecordId;
      });
  }

  public Mono<Optional<SObject>> getById(String username, String opportunityId)
  {
    Set<String> refFieldLabelColumns = Set.of("Account.Name");
    return wscInterface
      .getRecordById(username, SFObjectType.opportunity, refFieldLabelColumns, opportunityId);
  }

  public Flux<List<SObject>> getByAccountId(
    String username,
    String accountId)
  {
    return wscInterface
      .getLoggedInUserInfo(username)
      .flatMapMany(getUserInfoResult -> {
        String userId = getUserInfoResult.getUserId();
        SFQueryBuilder queryBuilder = new SFQueryBuilder()
          .addColumn(SObjectOpportunity.FIELD_ID)
          .addColumn(SObjectOpportunity.FIELD_NAME)
          .addColumn(SObjectOpportunity.FIELD_AMOUNT)
          .addColumn(SObjectOpportunity.FIELD_DESCRIPTION)
          .addColumn(SObjectOpportunity.FIELD_CLOSE_DATE)
          .addColumn(SObjectOpportunity.FIELD_STAGE_NAME)
          .addColumn(SObjectOpportunity.FIELD_OWNER_ID)
          .addColumn(SObjectOpportunity.FIELD_OWNER_NAME)
          .from(SObjectOpportunity.OBJECT_TYPE)
          .where(List.of(
              new SFWhere(SObjectOpportunity.FIELD_ACCOUNT_ID, accountId),
              new SFWhere(SObjectOpportunity.FIELD_OWNER_ID, userId)),
            true);
        queryBuilder.orderBy(SObjectOpportunity.FIELD_CREATED_DATE);
        queryBuilder.limit(WSCInterface.LIMIT);
        String query = queryBuilder.build();
        return wscInterface.getRecords(username, query);
      });
  }
}